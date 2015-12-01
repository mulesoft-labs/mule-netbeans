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
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.MuleRuntimeRegistry;
import org.mule.tooling.netbeans.api.RuntimeVersion;
import org.mule.tooling.netbeans.api.Status;
import org.openide.util.ChangeSupport;

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
    private final ChangeSupport cs = new ChangeSupport(this);
    private final MuleRuntimeRegistry registry;
    private final File muleHome;
    private Instance instance;
    private String id;
    private JarFile bootJar;
    private RuntimeVersion version;

    public DefaultMuleRuntime(MuleRuntimeRegistry registry, File muleHome) {
        this.registry = registry;
        this.muleHome = muleHome;
        instance = new OwnedInstance(this);
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
        if (!getLibUserDir().exists()) {
            throw new IllegalStateException("Invalid Mule installation");
        }
        File libBoot = getLibBootDir();
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
            this.id = registry.newId();
        }
        registry.register(this);
        instance.setUp();
        cs.fireChange();
    }

    @Override
    public void unregister() {
        registry.unregister(this);
        id = null;
        instance.tearDown();
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
        return instance.getStatus();
    }

    @Override
    public boolean isRunning() {
        return instance.isRunning();
    }
    
    @Override
    public boolean canStart() {
        return instance.canStart();
    }

    @Override
    public void start() {
        instance.start();
    }
    
    @Override
    public boolean canStop() {
        return instance.canStop();
    }

    @Override
    public void stop(boolean forced) {
        instance.stop(forced);
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
    public File getLibUserDir() {
        return new File(getMuleHome().getAbsolutePath() + LIB_USER_SUBDIR);
    }

    @Override
    public File getApplicationsDir() {
        return new File(getMuleHome().getAbsolutePath() + APPS_SUBDIR);
    }

    @Override
    public File getDomainsDir() {
        return new File(getMuleHome().getAbsolutePath() + DOMAINS_SUBDIR);
    }

    protected File getLibBootDir() {
        return new File(getMuleHome().getAbsolutePath() + LIB_BOOT_SUBDIR);
    }
    
    public File getPidFile() {
        return new File(getMuleHome(), BIN_SUBDIR + File.separator + RuntimeUtils.pidFileForVersion(version));
    }
}