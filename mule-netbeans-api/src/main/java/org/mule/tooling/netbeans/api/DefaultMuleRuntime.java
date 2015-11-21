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
package org.mule.tooling.netbeans.api;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class DefaultMuleRuntime implements MuleRuntime {

    private static final Pattern MULE_BOOT_PATTERN = Pattern.compile("mule-module-(.*?)boot(.*?).jar"); // NOI18N
    private static final Pattern MULE_PIDFILE_PATTERN = Pattern.compile("\\.mule(.*?)\\.pid"); // NOI18N
    private static final String LIB_SUBDIR = File.separator + "lib";
    private static final String LIB_BOOT_SUBDIR = LIB_SUBDIR + File.separator + "boot";
    private static final String LIB_USER_SUBDIR = LIB_SUBDIR + File.separator + "user";
    private static final String BIN_SUBDIR = File.separator + "bin";
    private static final String MF_SUPPORTED_JDKS = "Supported-Jdks";
    private static final String MF_IMPLEMENTATION_VENDOR_ID = Attributes.Name.IMPLEMENTATION_VENDOR_ID.toString();
    private static final String MF_SPECIFICATION_VERSION = Attributes.Name.SPECIFICATION_VERSION.toString();
    private final File muleHome;
    private String id;
    private JarFile bootJar;
    private RuntimeVersion version;
    private String supportedJDK;

    public DefaultMuleRuntime(File muleHome) {
        this.muleHome = muleHome;
        init();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return version.getBranch() + " " + version.getNumber();
    }

    @Override
    public Status getStatus() {
        return isRunning() ? Status.RUNNING : Status.DOWN;
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

    protected File getLibBootDir() {
        return new File(getMuleHome().getAbsolutePath() + LIB_BOOT_SUBDIR);
    }

    public boolean isRunning() {
        File bin = new File(getMuleHome().getAbsolutePath() + BIN_SUBDIR);
        if (!bin.exists()) {
            return false;
        }
        File[] children = bin.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return MULE_PIDFILE_PATTERN.matcher(name).matches();
            }
        });
        if (children.length == 0) {
            return false;
        }
        return true;
    }

    @Override
    public void register() {
        if(id == null) {
            this.id = UUID.randomUUID().toString();
        }
        MuleSupport.getStore().store(this);
    }

    @Override
    public void unregister() {
        MuleSupport.getStore().remove(this);
        id = null;
    }

    private void init() {
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
            supportedJDK = mf.getMainAttributes().getValue(MF_SUPPORTED_JDKS);
            String vendorId = mf.getMainAttributes().getValue(MF_IMPLEMENTATION_VENDOR_ID);
            String branch = vendorId.contains("muleesb") ? BRANCH_MULE_EE : (vendorId.contains("anypoint") ? BRANCH_API_GW : BRANCH_MULE);
            String number = mf.getMainAttributes().getValue(MF_SPECIFICATION_VERSION);
            version = new RuntimeVersion(branch, number);
        } catch (Exception e) {
            throw new IllegalStateException("Could not locate boot jar", e);
        }
    }
}
