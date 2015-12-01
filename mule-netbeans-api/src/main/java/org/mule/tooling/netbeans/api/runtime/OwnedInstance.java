/*
 * Copyright 2015 Facundo Lopez Kaufmann.
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
import org.mule.tooling.netbeans.api.Status;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class OwnedInstance implements Instance {

    private static RequestProcessor RP = new RequestProcessor("Mule Instance", 5); // NOI18N
    private DefaultMuleRuntime runtime;
    private Status status = Status.DOWN;
    private PidFileChangeListener pidFileChangeListener = new PidFileChangeListener();
    private final AtomicReference<Future<Integer>> processHolder = new AtomicReference<Future<Integer>>();
    private String wrapperExec;

    public OwnedInstance(DefaultMuleRuntime runtime) {
        this.runtime = runtime;
        wrapperExec = detectWrapperExec();
        System.out.println(wrapperExec);
    }

    public void setUp() {
        FileUtil.addFileChangeListener(pidFileChangeListener, runtime.getPidFile());
    }

    public void tearDown() {
        FileUtil.removeFileChangeListener(pidFileChangeListener);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean isRunning() {
        File pidFile = runtime.getPidFile();
        if (!pidFile.exists()) {
            return false;
        }
        try {
            String pid = getPidFromFile();
            boolean processRunning = RuntimeUtils.isProcessRunning(pid);
            if(!processRunning) {
                pidFile.delete();
            }
            return processRunning;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    private String getPidFromFile() throws IOException {
        return new String(Files.readAllBytes(runtime.getPidFile().toPath())).replaceAll("[ \n]", "");
    }

    @Override
    public boolean canStart() {
        return wrapperExec != null;
    }

    @Override
    public boolean canStop() {
        return processHolder.get() != null;
    }

    @Override
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
                            .frontWindow(true)
                            .controllable(true)
                            .postExecution(new Runnable() {
                                @Override
                                public void run() {
                                    processHolder.set(null);
                                }
                            });
                    ExecutionService service = ExecutionService.newService(startProcessBuilder(), descriptor, runtime.getName());
                    processHolder.set(service.run());
                }
            }
        });
    }

    @Override
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
                if(forced) {
                    processHolder.get().cancel(true);
                } else {
                    if(RuntimeUtils.isWindows()) {
                        processHolder.get().cancel(false);
                    } else {
                        try {
                            RuntimeUtils.softStop(getPidFromFile());
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        });
    }

    private ExternalProcessBuilder startProcessBuilder() {
        //TODO: allow all the parameter to be configurable
        String muleHome = runtime.getMuleHome().getAbsolutePath();
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(wrapperExec)
                .addArgument("--console")
                .addEnvironmentVariable("MULE_APP_LONG", "Mule")
                .addEnvironmentVariable("MULE_APP", "mule")
                .addEnvironmentVariable("PWD", muleHome + File.separator + "bin")
                .addEnvironmentVariable("MULE_HOME", muleHome)
                .addEnvironmentVariable("MULE_BASE", muleHome)
                .workingDirectory(new File(muleHome, "/bin"));
        processBuilder = processBuilder.addArgument(muleHome + File.separator + "conf" + File.separator + "wrapper.conf");
//        processBuilder = processBuilder.addArgument("wrapper.console.format=PM");
        processBuilder = processBuilder.addArgument("wrapper.console.format=M");
        processBuilder = processBuilder.addArgument("wrapper.console.flush=TRUE");
        processBuilder = processBuilder.addArgument("wrapper.syslog.ident=mule");
        processBuilder = processBuilder.addArgument("wrapper.pidfile=" + runtime.getPidFile().getAbsolutePath());
        processBuilder = processBuilder.addArgument("wrapper.working.dir=" + muleHome);
        for (int i = 1; i < 9; i++) {
            processBuilder = processBuilder.addArgument("wrapper.app.parameter." + i + "=console0");
        }
        processBuilder = processBuilder.addArgument("wrapper.app.parameter.9=");
        return processBuilder;
    }
    
    protected String detectWrapperExec() {
        File libBootExec = new File(runtime.getMuleHome(), "/lib/boot/exec");
        File exec;
        if(RuntimeUtils.RUNNING_OS.equals(RuntimeUtils.OS.MACOS)) {
            exec = new File(libBootExec, "wrapper-macosx-universal-32");
            if(exec.exists()) {
                return exec.getAbsolutePath();
            }
        } else if(RuntimeUtils.RUNNING_OS.equals(RuntimeUtils.OS.WINDOWS)) {
            if(RuntimeUtils.IS64) {
                exec = new File(libBootExec, "wrapper-windows-x86-64.exe");
                if(exec.exists()) {
                    return exec.getAbsolutePath();
                }
            }
            exec = new File(libBootExec, "wrapper-windows-x86-32.exe");
            if(exec.exists()) {
                return exec.getAbsolutePath();
            }
        } else if(RuntimeUtils.RUNNING_OS.equals(RuntimeUtils.OS.LINUX)) {
            if(RuntimeUtils.IS64) {
                exec = new File(libBootExec, "wrapper-linux-x86-64");
                if(exec.exists()) {
                    return exec.getAbsolutePath();
                }
            }
            exec = new File(libBootExec, "wrapper-linux-x86-32");
            if(exec.exists()) {
                return exec.getAbsolutePath();
            }
        }
        return null;
    }
    private class PidFileChangeListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            runtime.update();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            runtime.update();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            runtime.update();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            runtime.update();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
}
