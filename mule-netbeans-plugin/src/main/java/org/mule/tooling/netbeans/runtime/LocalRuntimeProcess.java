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
package org.mule.tooling.netbeans.runtime;

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
import org.mule.tooling.netbeans.api.RuntimeProcess;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class LocalRuntimeProcess implements InternalController, RuntimeConstants, RuntimeProcess {

    private static final Logger LOGGER = Logger.getLogger(LocalRuntimeProcess.class.getName());
    private static final String BIN_SUBDIR = "bin";
    private static final RequestProcessor RP = new RequestProcessor("Mule Instance", 5); // NOI18N
    private final AtomicReference<Future<Integer>> processHolder = new AtomicReference<>();
    private final Path wrapperExec;
    private final DefaultMuleRuntime runtime;
    private final ChangeSupport cs;
    private InputOutput io;
    private Status status = Status.DOWN;
    private Path pidFilePath;
    private FileChangeAdapter fileChangeAdapter;

    LocalRuntimeProcess(DefaultMuleRuntime runtime, ChangeSupport changeSupport) {
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
        cs.fireChange(this, ATTRIBUTE_STATUS, status);
        LOGGER.log(Level.INFO, "updated status: {0}", this);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        pidFilePath = runtime.getMuleHome().resolve(BIN_SUBDIR + File.separator + RuntimeUtils.pidFileForVersion(runtime.getVersion()));
        fileChangeAdapter = new FileChangeAdapter() {
            @Override
            public void fileDataCreated(FileEvent fe) {
                cs.fireChange(LocalRuntimeProcess.this);
            }

            @Override
            public void fileChanged(FileEvent fe) {
                cs.fireChange(LocalRuntimeProcess.this);
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                cs.fireChange(LocalRuntimeProcess.this);
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                cs.fireChange(LocalRuntimeProcess.this);
            }
        };
        FileUtil.addFileChangeListener(fileChangeAdapter, pidFilePath.toFile());
        io = createInputOutput(runtime);
        io.closeInputOutput();
    }

    @Override
    public void shutdown() {
        FileUtil.removeFileChangeListener(fileChangeAdapter, pidFilePath.toFile());
        io.closeInputOutput();
        io = null;
    }

    @Override
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

    @Override
    public boolean canStart() {
        return !isRunning() && wrapperExec != null;
    }

    @Override
    public void start(final boolean debug) {
        if (processHolder.get() != null) {
            throw new IllegalStateException("Instance already running");
        }
        final ExternalProcessBuilder processBuilder = createBasicProcessBuilder()
                .addEnvironmentVariable("JAVA_TOOL_OPTIONS", debug ? "-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n" : "")
                .addArgument("wrapper.app.parameter.1=console0");;
        RP.post(new Runnable() {
            @Override
            public void run() {
                synchronized (processHolder) {
                    if (processHolder.get() != null) {
                        return;
                    }
                    ExecutionService service = ExecutionService.newService(processBuilder, LocalRuntimeProcess.this.createExecutionDescriptor(), runtime.getName());
                    processHolder.set(service.run());
                }
            }
        });
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

    @Override
    public boolean canStop() {
        return isRunning() && processHolder.get() != null;
    }

    @Override
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

    @Override
    public void viewLogs() {
        io.select();
    }

    @Override
    public void installLicense(Path license) {
        if (!runtime.getVersion().isEnterprise()) {
            throw new IllegalStateException("Can't install license on a CE runtime");
        }
        if (isRunning()) {
            throw new IllegalStateException("Can't install license on a running runtime");
        }

        final ExternalProcessBuilder processBuilder = createBasicProcessBuilder()
                .addArgument("wrapper.app.parameter.1=--installLicense")
                .addArgument("wrapper.app.parameter.2=" + license.toAbsolutePath().toString());

        RP.post(new Runnable() {
            @Override
            public void run() {
                synchronized (processHolder) {
                    if (processHolder.get() != null) {
                        return;
                    }
                    ExecutionService service = ExecutionService.newService(processBuilder, LocalRuntimeProcess.this.createExecutionDescriptor(), runtime.getName());
                    processHolder.set(service.run());
                }
            }
        });
    }
    
    private ExternalProcessBuilder createBasicProcessBuilder() {
        RuntimeVersion version = runtime.getVersion();
        String muleHomeString = runtime.getMuleHome().toAbsolutePath().toString();
        
        return new ExternalProcessBuilder(wrapperExec.toAbsolutePath().toString())
                .addEnvironmentVariable("MULE_APP_LONG", version.getBranch())
                .addEnvironmentVariable("MULE_APP", version.isEnterprise() ? "mule_ee" : "mule")
                .addEnvironmentVariable("PWD", muleHomeString + File.separator + "bin")
                .addEnvironmentVariable("MULE_HOME", muleHomeString)
                .addEnvironmentVariable("MULE_BASE", muleHomeString)
                .addArgument(muleHomeString + File.separator + "conf" + File.separator + "wrapper.conf")
                .addArgument("wrapper.console.format=M")
                .addArgument("wrapper.console.flush=TRUE")
                .addArgument("wrapper.syslog.ident=mule")
                .addArgument("wrapper.pidfile=" + getPidFilePath().toAbsolutePath().toString())
                .addArgument("wrapper.working.dir=" + muleHomeString)
                .workingDirectory(new File(muleHomeString, "/bin"));
    }

    private ExecutionDescriptor createExecutionDescriptor() {
        return new ExecutionDescriptor()
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

    @NbBundle.Messages({
        "NBPSupport_StartAction_name=Start",
        "NBPSupport_DebugAction_name=Start in debug mode",
        "NBPSupport_StopAction_name=Stop"
    })
    protected InputOutput createInputOutput(final MuleRuntime runtime) {
        Action[] actions = new Action[]{
            new AbstractRuntimeAction(runtime, Bundle.NBPSupport_StartAction_name(), IconUtil.getStartIcon()) {
                @Override
                public boolean isEnabled() {
                    return canStart();
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    start(false);
                }
            },
            new AbstractRuntimeAction(runtime, Bundle.NBPSupport_StartAction_name(), IconUtil.getDebugIcon()) {
                @Override
                public boolean isEnabled() {
                    return canStart();
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    start(true);
                }
            },
            new AbstractRuntimeAction(runtime, Bundle.NBPSupport_StopAction_name(), IconUtil.getStopIcon()) {
                @Override
                public boolean isEnabled() {
                    return canStop();
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    stop(false);
                }
            }
        };
        return IOProvider.getDefault().getIO(runtime.getName(), actions);
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
}
