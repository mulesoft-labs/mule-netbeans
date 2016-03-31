/*
 * Copyright 2016 Facundo Lopez Kaufmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mule.tooling.netbeans.api.runtime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mule.tooling.netbeans.api.Status;
import org.mule.tooling.netbeans.api.change.AbstractChangeSource;
import org.mule.tooling.netbeans.api.change.ChangeSupport;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
class MuleProcess extends FileChangeAdapter implements InternalController {

    private static final Logger LOGGER = Logger.getLogger(MuleProcess.class.getName());
    private static final String ATTRIBUTE_STATUS = "status";
    private static final String BIN_SUBDIR = File.separator + "bin";
    private static final RequestProcessor RP = new RequestProcessor("Mule Instance", 5); // NOI18N
    private final AtomicReference<Future<Integer>> processHolder = new AtomicReference<Future<Integer>>();
    private String wrapperExec;
    private InputOutput io;
    private DefaultMuleRuntime runtime;
    private Status status = Status.DOWN;
    private ChangeSupport cs;

    MuleProcess(DefaultMuleRuntime runtime, ChangeSupport changeSupport) {
        this.runtime = runtime;
        this.cs = changeSupport;
        this.wrapperExec = RuntimeUtils.detectWrapperExec(runtime.getMuleHome());
    }

    protected void updateStatus(Status newStatus) {
        Status old = status;
        if (newStatus == null) {
            status = isRunning() ? Status.RUNNING : Status.DOWN;
        } else {
            status = newStatus;
        }
        if (status.equals(old)) {
            return;
        }
        cs.fireChange(ATTRIBUTE_STATUS, status);
        LOGGER.log(Level.INFO, "updated status: {0}", this);
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        FileUtil.addFileChangeListener(this, getPidFile());
        io = NBPSupport.getInputOutput(runtime);
    }

    @Override
    public void shutdown() {
        FileUtil.removeFileChangeListener(this, getPidFile());
        io.closeInputOutput();
        io = null;
    }

    public boolean isRunning() {
        File pidFile = getPidFile();
        if (!pidFile.exists()) {
            return false;
        }
        try {
            String pid = getPidFromFile();
            boolean processRunning = RuntimeUtils.isProcessRunning(pid);
            if (!processRunning) {
                pidFile.delete();
            }
            return processRunning;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    public boolean canStart() {
        return !isRunning() && wrapperExec != null;
    }

    public void start() {
        if (processHolder.get() != null) {
            throw new IllegalStateException("Instance already running");
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                synchronized (processHolder) {
                    if (processHolder.get() != null) {
                        return;
                    }
                    ExecutionDescriptor descriptor = new ExecutionDescriptor()
                            .inputOutput(io)
                            .frontWindow(true)
                            .controllable(true)
                            .outProcessorFactory(new LogTrackingInputProcessorFactory())
                            .postExecution(new Runnable() {
                                @Override
                                public void run() {
                                    processHolder.set(null);
                                    updateStatus(Status.DOWN);
                                }
                            });
                    ExecutionService service = ExecutionService.newService(startProcessBuilder(), descriptor, runtime.getName());
                    processHolder.set(service.run());
                }
            }
        });
    }

    private ExternalProcessBuilder startProcessBuilder() {
        //TODO: allow all the parameter to be configurable
        String muleHomeString = runtime.getMuleHome().getAbsolutePath();
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(wrapperExec)
                .addArgument("--console")
                .addEnvironmentVariable("MULE_APP_LONG", "Mule")
                .addEnvironmentVariable("MULE_APP", "mule")
                .addEnvironmentVariable("PWD", muleHomeString + File.separator + "bin")
                .addEnvironmentVariable("MULE_HOME", muleHomeString)
                .addEnvironmentVariable("MULE_BASE", muleHomeString)
                .workingDirectory(new File(muleHomeString, "/bin"));
        processBuilder = processBuilder.addArgument(muleHomeString + File.separator + "conf" + File.separator + "wrapper.conf");
//        processBuilder = processBuilder.addArgument("wrapper.console.format=PM");
        processBuilder = processBuilder.addArgument("wrapper.console.format=M");
        processBuilder = processBuilder.addArgument("wrapper.console.flush=TRUE");
        processBuilder = processBuilder.addArgument("wrapper.syslog.ident=mule");
        processBuilder = processBuilder.addArgument("wrapper.pidfile=" + getPidFile().getAbsolutePath());
        processBuilder = processBuilder.addArgument("wrapper.working.dir=" + muleHomeString);
        for (int i = 1; i < 9; i++) {
            processBuilder = processBuilder.addArgument("wrapper.app.parameter." + i + "=console0");
        }
        processBuilder = processBuilder.addArgument("wrapper.app.parameter.9=");
        return processBuilder;
    }

    private File getPidFile() {
        return new File(runtime.getMuleHome(), BIN_SUBDIR + File.separator + RuntimeUtils.pidFileForVersion(runtime.getVersion()));
    }

    private String getPidFromFile() throws IOException {
        return new String(Files.readAllBytes(getPidFile().toPath())).replaceAll("[ \n]", "");
    }

    public boolean canStop() {
        return isRunning() && processHolder.get() != null;
    }

    public void stop(final boolean forced) {
        if (processHolder.get() == null) {
            throw new IllegalStateException("Instance not running");
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                if (processHolder.get() == null) {
                    return;
                }
                if (forced) {
                    processHolder.get().cancel(true);
                } else if (RuntimeUtils.isWindows()) {
                    processHolder.get().cancel(false);
                } else {
                    try {
                        RuntimeUtils.softStop(getPidFromFile());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }

    public void viewLogs() {
        io.select();
    }

    private class LogTrackingInputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

        @Override
        public org.netbeans.api.extexecution.input.InputProcessor newInputProcessor(org.netbeans.api.extexecution.input.InputProcessor defaultProcessor) {
            return org.netbeans.api.extexecution.input.InputProcessors.proxy(defaultProcessor, new org.netbeans.api.extexecution.input.InputProcessor() {
                @Override
                public void processInput(char[] chars) throws IOException {
                    String line = new String(chars);
                    if (line.startsWith("-->")) {
                        updateStatus(Status.STARTING);
                    } else if (line.contains(" Mule is up and kicking ")) {
                        updateStatus(Status.RUNNING);
                    }
                }

                @Override
                public void reset() throws IOException {
                }

                @Override
                public void close() throws IOException {
                }
            });
        }
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        cs.fireChange();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        cs.fireChange();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        cs.fireChange();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        cs.fireChange();
    }
}
