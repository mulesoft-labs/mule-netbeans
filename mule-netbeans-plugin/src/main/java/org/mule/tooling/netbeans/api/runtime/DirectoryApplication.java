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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.mule.tooling.netbeans.api.Application;
import org.mule.tooling.netbeans.api.Configuration;
import org.mule.tooling.netbeans.api.Library;
import org.mule.tooling.netbeans.api.Status;
import org.mule.tooling.netbeans.api.change.AbstractChangeSource;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class DirectoryApplication extends AbstractChangeSource implements Application {
    
    private static final Logger LOGGER = Logger.getLogger(DirectoryApplication.class.getName());
    private final File path;
    private final Properties muleDeployProperties;
    private Status status = Status.DOWN;
    private List<Library> libs = new ArrayList<Library>();

    public DirectoryApplication(String path) {
        this(new File(path));
    }
    
    public DirectoryApplication(File path) {
        this.path = path;
        muleDeployProperties = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(path, "mule-deploy.properties"));
            muleDeployProperties.load(fis);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                }
            }
        }
        File[] children = new File(path, "lib").listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return FileHelper.isJar(pathname.getName());
            }
        });
        if(children == null) {
            return;
        }
        for (File file : children) {
            libs.add(new JarLibrary(file));
        }
    }

    @Override
    public String getName() {
        return path.getName();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getDomainName() {
        return muleDeployProperties.getProperty("domain", "");
    }

    @Override
    public List<Configuration> getConfigurations() {
        List<String> resources = Arrays.asList(muleDeployProperties.getProperty("config.resources", "").split(","));
        List<Configuration> configs = new ArrayList<Configuration>(resources.size());
        for (String resource : resources) {
            configs.add(new FileConfiguration(resource, new File(path, resource)));
        }
        return configs;
    }

    @Override
    public List<Library> getLibraries() {
        return libs;
    }
}
