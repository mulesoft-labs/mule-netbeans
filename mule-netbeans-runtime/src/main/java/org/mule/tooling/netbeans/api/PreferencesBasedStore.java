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
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class PreferencesBasedStore implements Store<MuleRuntimeInformation> {

    private static final String RUNTIMES_NODE = "nodes";
    private static final String NODES_RUNTIME_MH = "muleHome";
    private static final String NODES_RUNTIME_NAME = "name";
    private final ChangeSupport cs = new ChangeSupport(this);
    private final Preferences PREFERENCES = NbPreferences.forModule(MuleSupport.class).node(RUNTIMES_NODE);
    
    public static MuleRuntime getMuleRuntime(MuleRuntimeInformation information) {
        return new DefaultMuleRuntime(information);
    }
    
    @Override
    public MuleRuntimeInformation get(String id) {
        if(id == null || id.trim().length() == 0) {
            throw new IllegalStateException("Invalid id");
        }
        try {
            if(!PREFERENCES.nodeExists(id)) {
                throw new IllegalStateException("Runtime not registered");
            }
            Preferences runtimeNode = PREFERENCES.node(id);
            String name = runtimeNode.get(NODES_RUNTIME_NAME, null);
            String muleHome = runtimeNode.get(NODES_RUNTIME_MH, null);
            return new MuleRuntimeInformation(id, name, new File(muleHome));
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException("Could not process request", ex);
        }
    }
    
    @Override
    public List<String> getIds() {
        try {
            List<String> ids = Arrays.asList(PREFERENCES.childrenNames());
            return ids;
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException("Could not process request", ex);
        }
    }
    
    @Override
    public void store(MuleRuntimeInformation mri) {
        Preferences runtimeNode = PREFERENCES.node(mri.getId());
        runtimeNode.put(NODES_RUNTIME_NAME, mri.getName());
        runtimeNode.put(NODES_RUNTIME_MH, mri.getMuleHome().getAbsolutePath());
        try {
            PREFERENCES.flush();
            cs.fireChange();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public void remove(MuleRuntimeInformation mri) {
        try {
            if(PREFERENCES.nodeExists(mri.getId())) {
                PREFERENCES.node(mri.getId()).removeNode();
                PREFERENCES.flush();
                cs.fireChange();
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }
}
