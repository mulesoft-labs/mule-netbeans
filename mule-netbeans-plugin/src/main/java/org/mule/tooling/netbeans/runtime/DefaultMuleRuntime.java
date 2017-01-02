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
package org.mule.tooling.netbeans.runtime;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.mule.tooling.netbeans.api.Application;
import org.mule.tooling.netbeans.api.Configuration;
import org.mule.tooling.netbeans.api.Domain;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.MuleRuntimeRegistry;
import org.mule.tooling.netbeans.api.RuntimeVersion;
import org.openide.util.Lookup;
import org.mule.tooling.netbeans.api.IDGenerationStrategy;
import org.mule.tooling.netbeans.api.Library;
import static org.mule.tooling.netbeans.api.MuleRuntime.BRANCH_API_GW;
import static org.mule.tooling.netbeans.api.MuleRuntime.BRANCH_MULE;
import static org.mule.tooling.netbeans.api.MuleRuntime.BRANCH_MULE_EE;
import org.mule.tooling.netbeans.api.change.AbstractChangeSource;
import org.openide.filesystems.FileEvent;
import org.mule.tooling.netbeans.api.RuntimeProcess;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class DefaultMuleRuntime extends AbstractChangeSource implements MuleRuntime, RuntimeConstants {

    private static final Logger LOGGER = Logger.getLogger(DefaultMuleRuntime.class.getName());
    private static final Pattern MULE_BOOT_PATTERN = Pattern.compile("mule-module-(.*?)boot(.*?)\\.jar"); // NOI18N
    private static final String LIB_SUBDIR = "lib";
    private static final String LIB_BOOT_SUBDIR = LIB_SUBDIR + File.separator + "boot";
    private static final String LIB_USER_SUBDIR = LIB_SUBDIR + File.separator + "user";
    private static final String APPS_SUBDIR = "apps";
    private static final String DOMAINS_SUBDIR = "domains";
    private static final String MF_IMPLEMENTATION_VENDOR_ID = Attributes.Name.IMPLEMENTATION_VENDOR_ID.toString();
    private static final String MF_SPECIFICATION_VERSION = Attributes.Name.SPECIFICATION_VERSION.toString();
    private static final IDGenerationStrategy IDGENERATOR = Lookup.getDefault().lookup(IDGenerationStrategy.class);
    private final MuleRuntimeRegistry registry;
    private final Path muleHome;
    private final Lock lock = new ReentrantLock();
    private ApplicationsInternalController apps;
    private DomainsInternalController domains;
    private UserLibrariesInternalController libs;
    private List<Configuration> configurations = new ArrayList<Configuration>();
    private String id;
    private RuntimeVersion version;
    private LocalRuntimeProcess process;
    private boolean doRegistration = true;

    public DefaultMuleRuntime(MuleRuntimeRegistry registry, Path muleHome) {
        super();
        this.registry = registry;
        this.muleHome = muleHome;
        init();
    }

    public DefaultMuleRuntime(MuleRuntimeRegistry registry, Path muleHome, boolean doRegistration) {
        this(registry, muleHome);
        this.doRegistration = doRegistration;
    }

    /**
     *
     */
    private void init() {
        if (!Files.exists(muleHome)) {
            throw new IllegalStateException("Invalid Mule installation");
        }
        if (!Files.exists(muleHome.resolve(LIB_SUBDIR))) {
            throw new IllegalStateException("Invalid Mule installation");
        }
        File libBoot = muleHome.resolve(LIB_BOOT_SUBDIR).toFile();
        if (!libBoot.exists() || !libBoot.isDirectory()) {
            throw new IllegalStateException("Invalid Mule installation");
        }
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
            JarFile bootJar = new JarFile(children[0]);
            Manifest mf = bootJar.getManifest();
            String vendorId = mf.getMainAttributes().getValue(MF_IMPLEMENTATION_VENDOR_ID);
            String branch = vendorId.contains("muleesb") ? BRANCH_MULE_EE : (vendorId.contains("anypoint") ? BRANCH_API_GW : BRANCH_MULE);
            String number = mf.getMainAttributes().getValue(MF_SPECIFICATION_VERSION);
            version = new RuntimeVersion(branch, number);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid boot jar", e);
        }
        process = new LocalRuntimeProcess(this, changeSupport);
        File appsFolder = muleHome.resolve(APPS_SUBDIR).toFile();
        File libsFolder = muleHome.resolve(LIB_USER_SUBDIR).toFile();
        File domainsFolder = muleHome.resolve(DOMAINS_SUBDIR).toFile();
        LOGGER.log(Level.INFO, "{0} apps folder: {1}", new Object[]{getName(), appsFolder});
        LOGGER.log(Level.INFO, "{0} libs folder: {1}", new Object[]{getName(), libsFolder});
        LOGGER.log(Level.INFO, "{0} domains folder: {1}", new Object[]{getName(), domainsFolder});
        apps = new ApplicationsInternalController(appsFolder, changeSupport);
        libs = new UserLibrariesInternalController(libsFolder, changeSupport);
        domains = new DomainsInternalController(domainsFolder, changeSupport);

        addConfiguration(Configuration.TLS, muleHome.resolve("conf/tls-default.conf").toFile());
        addConfiguration(Configuration.WRAPPER, muleHome.resolve("conf/wrapper.conf").toFile());
        addConfiguration(Configuration.AGENT, muleHome.resolve("conf/mule-agent.yml").toFile());
        String[] parts = version.getNumber().split("\\.");
        int major = Integer.valueOf(parts[0]);
        int minor = Integer.valueOf(parts[1]);
        // we check if this version uses log4j
        if ((version.getBranch().equals(BRANCH_API_GW) && major == 1) 
                || (!version.getBranch().equals(BRANCH_API_GW) && major == 3 && minor < 6)) {
            addConfiguration(Configuration.LOGS, muleHome.resolve("conf/log4j.properties").toFile());
        } else {
            addConfiguration(Configuration.LOGS, muleHome.resolve("conf/log4j2.xml").toFile());
        }
    }

    protected void addConfiguration(String name, File file) {
        if (file.exists()) {
            configurations.add(new FileConfiguration(name, file));
        }
    }

    public void update(FileEvent fe) {
        fireChange();
    }

    @Override
    public boolean isRegistered() {
        return registry.isRegistered(this);
    }

    @Override
    public void register() {
        lock.lock();
        try {
            if (doRegistration && isRegistered()) {
                throw new IllegalStateException("Runtime already registered");
            }
            if (id == null) {
                this.id = IDGENERATOR.newId();
            }
            if(doRegistration) {
                registry.register(this);
            }
            domains.initialize();
            apps.initialize();
            libs.initialize();
            process.initialize();
            fireChange(ATTRIBUTE_REGISTERED, true);
            doRegistration = true;
            LOGGER.log(Level.INFO, "{0} registered!", new Object[]{getName()});
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unregister() {
        lock.lock();
        try {
            registry.unregister(this);
            id = null;
            domains.shutdown();
            apps.shutdown();
            libs.shutdown();
            process.shutdown();
            fireChange(ATTRIBUTE_REGISTERED, false);
            LOGGER.log(Level.INFO, "{0} unregistered!", new Object[]{getName()});
        } finally {
            lock.unlock();
        }
    }

    @Override
    public RuntimeProcess getRuntimeProcess() {
        return process;
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
    public Path getMuleHome() {
        return muleHome;
    }

    @Override
    public List<Application> getApplications() {
        return apps.getArtefacts();
    }

    @Override
    public List<Domain> getDomains() {
        return domains.getArtefacts();
    }

    @Override
    public List<Library> getLibraries() {
        return libs.getArtefacts();
    }

    @Override
    public List<Configuration> getConfigurations() {
        return configurations;
    }

    @Override
    public String toString() {
        return "DefaultMuleRuntime[name=" + getName() + ", status=" + getRuntimeProcess().getStatus() + ']';
    }
}
