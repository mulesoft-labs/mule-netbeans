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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.RuntimeVersion;
import org.mule.tooling.netbeans.api.Status;
import org.mule.tooling.netbeans.api.change.AttributeChangeEvent;
import org.mule.tooling.netbeans.api.change.ChangeSupport;
import org.mule.tooling.netbeans.common.IconUtil;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
class MuleProcess extends FileChangeAdapter implements InternalController, RuntimeConstants {

    private static final Logger LOGGER = Logger.getLogger(MuleProcess.class.getName());
    private static final String BIN_SUBDIR = "bin";
    private static final RequestProcessor RP = new RequestProcessor("Mule Instance", 5); // NOI18N
    private final AtomicReference<Future<Integer>> processHolder = new AtomicReference<Future<Integer>>();
    private final Path wrapperExec;
    private final DefaultMuleRuntime runtime;
    private final ChangeSupport cs;
    private InputOutput io;
    private Status status = Status.DOWN;
    private Path pidFilePath;

    MuleProcess(DefaultMuleRuntime runtime, ChangeSupport changeSupport) {
        this.runtime = runtime;
        this.cs = changeSupport;
        this.wrapperExec = RuntimeUtils.detectWrapperExecPath(runtime.getMuleHome());
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
        pidFilePath = runtime.getMuleHome().resolve(BIN_SUBDIR + File.separator + RuntimeUtils.pidFileForVersion(runtime.getVersion()));
        FileUtil.addFileChangeListener(this, pidFilePath.toFile());
        io = createInputOutput(runtime);
        io.closeInputOutput();
    }

    @Override
    public void shutdown() {
        FileUtil.removeFileChangeListener(this, pidFilePath.toFile());
        io.closeInputOutput();
        io = null;
    }

    public boolean isRunning() {
        if (!Files.exists(getPidFilePath())) {
            return false;
        }
        try {
            String pid = getPidFromFile();
            if (pid == null) {
                return false;
            }
            boolean processRunning = RuntimeUtils.isProcessRunning(pid);
            if (!processRunning) {
                Files.delete(getPidFilePath());
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

    public void start(final boolean debug) {
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
                    ExecutionService service = ExecutionService.newService(startProcessBuilder(debug), descriptor, runtime.getName());
                    processHolder.set(service.run());
                }
            }
        });
    }

    private ExternalProcessBuilder startProcessBuilder(boolean debug) {
        //TODO: allow all the parameter to be configurable
        String muleHomeString = runtime.getMuleHome().toAbsolutePath().toString();
        RuntimeVersion version = runtime.getVersion();
        String muleApp = version.isEnterprise() ? "mule_ee" : "mule";
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(wrapperExec.toAbsolutePath().toString())
                .addArgument("--console")
                .addEnvironmentVariable("MULE_APP_LONG", version.getBranch())
                .addEnvironmentVariable("MULE_APP", muleApp)
                .addEnvironmentVariable("PWD", muleHomeString + File.separator + "bin")
                .addEnvironmentVariable("MULE_HOME", muleHomeString)
                .addEnvironmentVariable("MULE_BASE", muleHomeString)
                .addEnvironmentVariable("JAVA_TOOL_OPTIONS", debug ? "-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n" : "")
                .workingDirectory(new File(muleHomeString, "/bin"));
        processBuilder = processBuilder.addArgument(muleHomeString + File.separator + "conf" + File.separator + "wrapper.conf");
//        processBuilder = processBuilder.addArgument("wrapper.console.format=PM");
        processBuilder = processBuilder.addArgument("wrapper.console.format=M");
        processBuilder = processBuilder.addArgument("wrapper.console.flush=TRUE");
        processBuilder = processBuilder.addArgument("wrapper.syslog.ident=mule");
        processBuilder = processBuilder.addArgument("wrapper.pidfile=" + getPidFilePath().toAbsolutePath().toString());
        processBuilder = processBuilder.addArgument("wrapper.working.dir=" + muleHomeString);
        processBuilder = processBuilder.addArgument("wrapper.app.parameter.1=console0");
        return processBuilder;
    }

    private Path getPidFilePath() {
        return pidFilePath;
    }

    private String getPidFromFile() throws IOException {
        Path pidpath = getPidFilePath();
        if (Files.exists(pidpath)) {
            return new String(Files.readAllBytes(pidpath)).replaceAll("[ \n]", "");
        } else {
            return null;
        }
    }

    public boolean canStop() {
        return isRunning() && processHolder.get() != null;
    }

    public void stop(final boolean forced) {
        if (processHolder.get() == null) {
            LOGGER.info("Instance not running");
            return;
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
                        String pidFromFile = getPidFromFile();
                        if (pidFromFile != null) {
                            RuntimeUtils.softStop(pidFromFile);
                        }
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

    protected InputOutput createInputOutput(final MuleRuntime runtime) {
        Action[] actions = new Action[]{
            new StartAction(runtime),
            new DebugAction(runtime),
            new StopAction(runtime)
        };
        InputOutput io = IOProvider.getDefault().getIO(runtime.getName(), actions);
        return io;
    }

    private static abstract class AbstractRuntimeAction extends AbstractAction implements ChangeListener {

        protected final MuleRuntime runtime;

        public AbstractRuntimeAction(MuleRuntime runtime, String name, Icon icon) {
            super(name, icon);
            this.runtime = runtime;
            this.runtime.addChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            LOGGER.log(Level.INFO, "{0} received the event {1} (registered={2})", new Object[]{this, e, runtime.isRegistered()});
            if (!(e instanceof AttributeChangeEvent)) {
                return;
            }
            AttributeChangeEvent ace = (AttributeChangeEvent) e;
            if (ace.getAttributeName().equals(ATTRIBUTE_STATUS)) {
                updateEnabled();
            } else if (ace.getAttributeName().equals(ATTRIBUTE_REGISTERED) && Boolean.FALSE.equals(ace.getValue())) {
                runtime.removeChangeListener(this);                
            }
        }

        protected void updateEnabled() {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    firePropertyChange("enabled", null, isEnabled() ? Boolean.TRUE : Boolean.FALSE);
                }
            });
        }
    }

    @NbBundle.Messages({
        "NBPSupport_StartAction_name=Start"
    })
    private static class StartAction extends AbstractRuntimeAction {

        public StartAction(MuleRuntime runtime) {
            super(runtime, Bundle.NBPSupport_StartAction_name(), IconUtil.getStartIcon());
        }

        @Override
        public boolean isEnabled() {
            return runtime.canStart();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            runtime.start(false);
        }
    }

    @NbBundle.Messages({
        "NBPSupport_DebugAction_name=Start in debug mode"
    })
    private static class DebugAction extends AbstractRuntimeAction {

        public DebugAction(MuleRuntime runtime) {
            super(runtime, Bundle.NBPSupport_StartAction_name(), IconUtil.getDebugIcon());
        }

        @Override
        public boolean isEnabled() {
            return runtime.canStart();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            runtime.start(true);
        }
    }

    @NbBundle.Messages({
        "NBPSupport_StopAction_name=Stop"
    })
    private static class StopAction extends AbstractRuntimeAction {

        public StopAction(MuleRuntime runtime) {
            super(runtime, Bundle.NBPSupport_StopAction_name(), IconUtil.getStopIcon());
        }

        @Override
        public boolean isEnabled() {
            return runtime.canStop();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            runtime.stop(false);
        }
    }
}
