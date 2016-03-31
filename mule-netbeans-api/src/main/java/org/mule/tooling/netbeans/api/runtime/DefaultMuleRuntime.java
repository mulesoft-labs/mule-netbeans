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
import java.io.FilenameFilter;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.mule.tooling.netbeans.api.Application;
import org.mule.tooling.netbeans.api.Domain;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.MuleRuntimeRegistry;
import org.mule.tooling.netbeans.api.RuntimeVersion;
import org.mule.tooling.netbeans.api.Status;
import org.openide.util.Lookup;
import org.mule.tooling.netbeans.api.IDGenerationStrategy;
import org.mule.tooling.netbeans.api.Library;
import static org.mule.tooling.netbeans.api.MuleRuntime.BRANCH_API_GW;
import static org.mule.tooling.netbeans.api.MuleRuntime.BRANCH_MULE;
import static org.mule.tooling.netbeans.api.MuleRuntime.BRANCH_MULE_EE;
import org.mule.tooling.netbeans.api.change.AbstractChangeSource;
import org.openide.filesystems.FileEvent;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class DefaultMuleRuntime extends AbstractChangeSource implements MuleRuntime {

    private static final Logger LOGGER = Logger.getLogger(DefaultMuleRuntime.class.getName());
    private static final Pattern MULE_BOOT_PATTERN = Pattern.compile("mule-module-(.*?)boot(.*?)\\.jar"); // NOI18N
    private static final String LIB_SUBDIR = File.separator + "lib";
    private static final String LIB_BOOT_SUBDIR = LIB_SUBDIR + File.separator + "boot";
    private static final String LIB_USER_SUBDIR = LIB_SUBDIR + File.separator + "user";
    private static final String APPS_SUBDIR = File.separator + "apps";
    private static final String DOMAINS_SUBDIR = File.separator + "domains";
    private static final String MF_IMPLEMENTATION_VENDOR_ID = Attributes.Name.IMPLEMENTATION_VENDOR_ID.toString();
    private static final String MF_SPECIFICATION_VERSION = Attributes.Name.SPECIFICATION_VERSION.toString();
    private static final IDGenerationStrategy IDGENERATOR = Lookup.getDefault().lookup(IDGenerationStrategy.class);
    private final MuleRuntimeRegistry registry;
    private final File muleHome;
    private ApplicationsInternalController apps;
    private DomainsInternalController domains;
    private UserLibrariesInternalController libs;
    private String id;
    private JarFile bootJar;
    private RuntimeVersion version;
    private MuleProcess process;

    public DefaultMuleRuntime(MuleRuntimeRegistry registry, File muleHome) {
        super();
        this.registry = registry;
        this.muleHome = muleHome;
        init();
    }

    /**
     *
     */
    private void init() {
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
        process = new MuleProcess(this, changeSupport);
        apps = new ApplicationsInternalController(new File(muleHome, APPS_SUBDIR), changeSupport);
        libs = new UserLibrariesInternalController(new File(muleHome, LIB_USER_SUBDIR), changeSupport);
        domains = new DomainsInternalController(new File(muleHome, DOMAINS_SUBDIR), changeSupport);
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
        if (isRegistered()) {
            throw new IllegalStateException("Runtime already registered");
        }
        if (id == null) {
            this.id = IDGENERATOR.newId();
        }
        registry.register(this);
        domains.initialize();
        apps.initialize();
        libs.initialize();
        process.initialize();
        fireChange();
    }

    @Override
    public void unregister() {
        registry.unregister(this);
        id = null;
        domains.shutdown();
        apps.shutdown();
        libs.shutdown();
        process.shutdown();
        fireChange();
    }

    //---Instance handling methods
    @Override
    public Status getStatus() {
        return process.getStatus();
    }

    @Override
    public boolean isRunning() {
        return process.isRunning();
    }

    @Override
    public boolean canStart() {
        return process.canStart();
    }

    @Override
    public void start() {
        process.start();
    }

    @Override
    public boolean canStop() {
        return process.canStop();
    }

    @Override
    public void stop(final boolean forced) {
        process.stop(forced);
    }

    @Override
    public void viewLogs() {
        process.viewLogs();
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
    public String toString() {
        return "DefaultMuleRuntime[name=" + getName() + ", status=" + getStatus() + ']';
    }
}
