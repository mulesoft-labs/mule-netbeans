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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.mule.tooling.netbeans.api.Application;
import org.mule.tooling.netbeans.api.Domain;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.MuleRuntimeRegistry;
import org.mule.tooling.netbeans.api.RuntimeVersion;
import org.mule.tooling.netbeans.api.Status;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.mule.tooling.netbeans.api.IDGenerationStrategy;
import org.mule.tooling.netbeans.api.Library;
import static org.mule.tooling.netbeans.api.MuleRuntime.BRANCH_API_GW;
import static org.mule.tooling.netbeans.api.MuleRuntime.BRANCH_MULE;
import static org.mule.tooling.netbeans.api.MuleRuntime.BRANCH_MULE_EE;
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
public class DefaultMuleRuntime implements MuleRuntime {

    private static final Pattern MULE_BOOT_PATTERN = Pattern.compile("mule-module-(.*?)boot(.*?)\\.jar"); // NOI18N
    private static final String LIB_SUBDIR = File.separator + "lib";
    private static final String LIB_BOOT_SUBDIR = LIB_SUBDIR + File.separator + "boot";
    private static final String LIB_USER_SUBDIR = LIB_SUBDIR + File.separator + "user";
    private static final String BIN_SUBDIR = File.separator + "bin";
    private static final String APPS_SUBDIR = File.separator + "apps";
    private static final String DOMAINS_SUBDIR = File.separator + "domains";
    private static final String MF_IMPLEMENTATION_VENDOR_ID = Attributes.Name.IMPLEMENTATION_VENDOR_ID.toString();
    private static final String MF_SPECIFICATION_VERSION = Attributes.Name.SPECIFICATION_VERSION.toString();
    private static final IDGenerationStrategy IDGENERATOR = Lookup.getDefault().lookup(IDGenerationStrategy.class);
    private static final RequestProcessor RP = new RequestProcessor("Mule Instance", 5); // NOI18N
    private final ChangeSupport cs = new ChangeSupport(this);
    private final MuleRuntimeRegistry registry;
    private final File muleHome;
    private final PidFileChangeListener pidFileChangeListener = new PidFileChangeListener();
    private final AtomicReference<Future<Integer>> processHolder = new AtomicReference<Future<Integer>>();
    private String wrapperExec;
    private String id;
    private JarFile bootJar;
    private RuntimeVersion version;
    private Status status = Status.DOWN;

    public DefaultMuleRuntime(MuleRuntimeRegistry registry, File muleHome) {
        this.registry = registry;
        this.muleHome = muleHome;
        init();
    }
    
    /**
     * 
     */
    private void init() {
        if (registry.isRegistered(this)) {
            throw new IllegalStateException("Duplicate Mule instance");
        }
        if (!getMuleHome().exists()) {
            throw new IllegalStateException("Invalid Mule installation");
        }
        if (!new File(muleHome, LIB_SUBDIR).exists()) {
            throw new IllegalStateException("Invalid Mule installation");
        }
        File libBoot = new File(muleHome, LIB_BOOT_SUBDIR);
        if (!libBoot.exists()) {
            throw new IllegalStateException("Invalid Mule installation");
        }
        wrapperExec = RuntimeUtils.detectWrapperExec(muleHome);
        File[] children = libBoot.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return MULE_BOOT_PATTERN.matcher(name).matches();
            }
        });
        if (children.length == 0) {
            throw new IllegalStateException("Could not locate boot jar");
        }
        try {
            bootJar = new JarFile(children[0]);
            Manifest mf = bootJar.getManifest();
            String vendorId = mf.getMainAttributes().getValue(MF_IMPLEMENTATION_VENDOR_ID);
            String branch = vendorId.contains("muleesb") ? BRANCH_MULE_EE : (vendorId.contains("anypoint") ? BRANCH_API_GW : BRANCH_MULE);
            String number = mf.getMainAttributes().getValue(MF_SPECIFICATION_VERSION);
            version = new RuntimeVersion(branch, number);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid boot jar", e);
        }
    }
    
    public void update() {
        cs.fireChange();
    }

    @Override
    public boolean isRegistered() {
        return registry.isRegistered(this);
    }

    @Override
    public void register() {
        if(isRegistered()) {
            throw new IllegalStateException("Runtime already registered");
        }
        if (id == null) {
            this.id = IDGENERATOR.newId();
        }
        registry.register(this);
        FileUtil.addFileChangeListener(pidFileChangeListener, getPidFile());
        FileUtil.addFileChangeListener(pidFileChangeListener, new File(muleHome, LIB_USER_SUBDIR));
        FileUtil.addFileChangeListener(pidFileChangeListener, new File(muleHome, APPS_SUBDIR));
        FileUtil.addFileChangeListener(pidFileChangeListener, new File(muleHome, DOMAINS_SUBDIR));
        cs.fireChange();
    }

    @Override
    public void unregister() {
        registry.unregister(this);
        id = null;
        FileUtil.removeFileChangeListener(pidFileChangeListener);
        cs.fireChange();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }
    
    //---Instance handling methods

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
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
    
    @Override
    public boolean canStart() {
        return wrapperExec != null;
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
                    ExecutionService service = ExecutionService.newService(startProcessBuilder(), descriptor, getName());
                    processHolder.set(service.run());
                }
            }
        });
    }

    private ExternalProcessBuilder startProcessBuilder() {
        //TODO: allow all the parameter to be configurable
        String muleHomeString = getMuleHome().getAbsolutePath();
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
        return new File(getMuleHome(), BIN_SUBDIR + File.separator + RuntimeUtils.pidFileForVersion(version));
    }
    
    private String getPidFromFile() throws IOException {
        return new String(Files.readAllBytes(getPidFile().toPath())).replaceAll("[ \n]", "");
    }
    
    @Override
    public boolean canStop() {
        return processHolder.get() != null;
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
    
    //---MuleRuntime implementation

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return version.getBranch() + " " + version.getNumber();
    }

    @Override
    public RuntimeVersion getVersion() {
        return version;
    }

    @Override
    public File getMuleHome() {
        return muleHome;
    }

    @Override
    public List<Application> getApplications() {
        File[] children = new File(muleHome, APPS_SUBDIR).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        List<Application> apps = new ArrayList<Application>();
        for (File file : children) {
            apps.add(new DirectoryApplication(file));
        }
        return apps;
    }

    @Override
    public List<Domain> getDomains() {
        File[] children = new File(muleHome, DOMAINS_SUBDIR).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        List<Domain> domains = new ArrayList<Domain>();
        for (File file : children) {
            domains.add(new DirectoryDomain(file));
        }
        return domains;
    }

    private static final Pattern JAR_PATTERN = Pattern.compile("(.*?)\\.jar"); // NOI18N
    @Override
    public List<Library> getLibraries() {
        File[] children = new File(muleHome, LIB_SUBDIR).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return JAR_PATTERN.matcher(name).matches();
            }
        });
        List<Library> libs = new ArrayList<Library>(children.length);
        for (File file : children) {
            libs.add(new JarLibrary(file));
        }
        return libs;
    }

    private class PidFileChangeListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            update();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            update();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            update();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            update();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
}
