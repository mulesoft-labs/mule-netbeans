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
package org.mule.tooling.netbeans.api.store;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.MuleRuntimeStore;
import org.mule.tooling.netbeans.api.MuleSupport;
import org.mule.tooling.netbeans.api.change.AbstractChangeSource;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@ServiceProvider(service = MuleRuntimeStore.class)
public class PreferencesBasedStore extends AbstractChangeSource implements MuleRuntimeStore {

    private static final String RUNTIMES_NODE = "nodes";
    private static final String NODES_RUNTIME_MH = "muleHome";
    private static final String NODES_RUNTIME_NAME = "name";
    private final Preferences PREFERENCES = NbPreferences.forModule(MuleSupport.class).node(RUNTIMES_NODE);

    
    @Override
    public MuleRuntime get(String id) {
        if(id == null || id.trim().length() == 0) {
            throw new IllegalStateException("Invalid id");
        }
        try {
            if(!PREFERENCES.nodeExists(id)) {
                throw new IllegalStateException("Runtime not registered");
            }
            Preferences runtimeNode = PREFERENCES.node(id);
            String muleHome = runtimeNode.get(NODES_RUNTIME_MH, null);
            return MuleSupport.getMuleRuntime(new File(muleHome));
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
    public void store(MuleRuntime runtime) {
        Preferences runtimeNode = PREFERENCES.node(runtime.getId());
        if(runtime.getName() != null) {
            runtimeNode.put(NODES_RUNTIME_NAME, runtime.getName());
        }
        runtimeNode.put(NODES_RUNTIME_MH, runtime.getMuleHome().getAbsolutePath());
        try {
            PREFERENCES.flush();
            fireChange();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public void remove(MuleRuntime runtime) {
        try {
            if(PREFERENCES.nodeExists(runtime.getId())) {
                PREFERENCES.node(runtime.getId()).removeNode();
                PREFERENCES.flush();
                fireChange();
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
