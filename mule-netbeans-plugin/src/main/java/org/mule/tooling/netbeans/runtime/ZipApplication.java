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

import java.io.File;
import java.util.Collections;
import java.util.List;
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
public class ZipApplication extends AbstractChangeSource implements Application {
    
    private static final Logger LOGGER = Logger.getLogger(ZipApplication.class.getName());
    private final File path;
    private Status status = Status.DOWN;

    public ZipApplication(String path) {
        this(new File(path));
    }
    
    public ZipApplication(File path) {
        this.path = path;
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
        return "";
    }

    @Override
    public List<Configuration> getConfigurations() {
//        List<String> resources = Arrays.asList(muleDeployProperties.getProperty("config.resources", "").split(","));
//        List<Configuration> configs = new ArrayList<Configuration>(resources.size());
//        for (String resource : resources) {
//            configs.add(new FileConfiguration(resource, new File(path, resource)));
//        }
//        return configs;
        return Collections.<Configuration>emptyList();
    }

    @Override
    public List<Library> getLibraries() {
//        return libs;
        return Collections.<Library>emptyList();
    }

    @Override
    public String toString() {
        return "ZipApplication{" + "name=" + getName() + ", domain=" + getDomainName()+ '}';
    }
}
