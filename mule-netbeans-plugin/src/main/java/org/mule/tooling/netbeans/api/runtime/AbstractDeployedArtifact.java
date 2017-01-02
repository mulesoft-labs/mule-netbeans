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

import java.util.List;
import javax.swing.event.ChangeListener;
import org.mule.tooling.netbeans.api.Configuration;
import org.mule.tooling.netbeans.api.ConfigurationsContainer;
import org.mule.tooling.netbeans.api.LibrariesContainer;
import org.mule.tooling.netbeans.api.Library;
import org.mule.tooling.netbeans.api.Named;
import org.mule.tooling.netbeans.api.change.ChangeSource;
import org.mule.tooling.netbeans.api.change.ChangeSupport;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class AbstractDeployedArtifact implements Named, ChangeSource, LibrariesContainer, ConfigurationsContainer {
    private ChangeSupport changeSource = new ChangeSupport();

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Library> getLibraries() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Configuration> getConfigurations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
