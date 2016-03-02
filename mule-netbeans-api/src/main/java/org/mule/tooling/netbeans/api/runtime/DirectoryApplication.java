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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.mule.tooling.netbeans.api.Application;
import org.mule.tooling.netbeans.api.Library;
import org.openide.util.Exceptions;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class DirectoryApplication implements Application {
    
    private final File path;
    private final Properties muleDeployProperties;

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
    }

    @Override
    public String getName() {
        return path.getName();
    }

    @Override
    public String getDomainName() {
        return muleDeployProperties.getProperty("domain");
    }

    @Override
    public List<Library> getLibraries() {
        return Collections.emptyList();
    }
}
