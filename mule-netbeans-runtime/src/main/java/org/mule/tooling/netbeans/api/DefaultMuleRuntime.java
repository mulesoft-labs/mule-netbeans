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
    private static final String LIB_BOOT_SUBDIR = File.separator + "lib" + File.separator + "boot";
    private static final String MF_SUPPORTED_JDKS = "Supported-Jdks";
    private static final String MF_IMPLEMENTATION_VENDOR_ID = Attributes.Name.IMPLEMENTATION_VENDOR_ID.toString();
    private static final String MF_SPECIFICATION_VERSION = Attributes.Name.SPECIFICATION_VERSION.toString();
    private final MuleRuntimeInformation information;
    private JarFile bootJar;
    private RuntimeVersion version;
    private String supportedJDK;

    public DefaultMuleRuntime(MuleRuntimeInformation information)  {
        this.information = information;
        init();
    }
    
    @Override
    public MuleRuntimeInformation getInformation() {
        return information;
    }
    
    @Override
    public Status getStatus() {
        return Status.DOWN;
    }
    
    @Override
    public RuntimeVersion getVersion() {
        return version;
    }

    @Override
    public File getMuleHome() {
        return information.getMuleHome();
    }

    private void init() {
        if(!getMuleHome().exists()) {
            throw new IllegalStateException("Invalid Mule installation");
        }
        File libBoot = new File(information.getMuleHome().getAbsolutePath() + LIB_BOOT_SUBDIR);
        if(!libBoot.exists()) {
            throw new IllegalStateException("Invalid Mule installation");
        }
        File[] children = libBoot.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return MULE_BOOT_PATTERN.matcher(name).matches();
            }
        });
        if(children.length == 0) {
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
