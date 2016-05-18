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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mule.tooling.netbeans.api.Configuration;
import org.mule.tooling.netbeans.api.Domain;
import org.mule.tooling.netbeans.api.Library;
import org.mule.tooling.netbeans.api.change.AbstractChangeSource;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class DirectoryDomain extends AbstractChangeSource implements Domain {
    
    private static final String CONFIG_NAME = "mule-domain-config.xml";
    private final File path;
    private List<Library> libs = new ArrayList<Library>();

    public DirectoryDomain(File path) {
        this.path = path;
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
    public List<Library> getLibraries() {
        return libs;
    }

    @Override
    public List<Configuration> getConfigurations() {
        return Collections.<Configuration>singletonList(new FileConfiguration(CONFIG_NAME, new File(path, CONFIG_NAME)));
    }

    @Override
    public String toString() {
        return "DirectoryDomain{" + "name=" + getName() + '}';
    }
}
