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

import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.mule.tooling.netbeans.api.MuleSupport;
import org.mule.tooling.netbeans.api.Store;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@ServiceProvider(service = Store.class)
public class BasicStore implements Store {

    private final Preferences ROOT = NbPreferences.forModule(MuleSupport.class);

    private Preferences getArea(String area) {
        return ROOT.node(area);
    }

    @Override
    public Preferences get(String areaName, String id, boolean forceCreation) {
        if (id == null || id.trim().length() == 0) {
            throw new IllegalStateException("Invalid id");
        }
        try {
            Preferences area = getArea(areaName);
            if (!area.nodeExists(id) && !forceCreation) {
                throw new IllegalStateException("Runtime not registered");
            }
            Preferences runtimeNode = area.node(id);
            area.flush();
            return runtimeNode;
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException("Could not process request", ex);
        }
    }

    @Override
    public List<String> getIds(String areaName) {
        try {
            return Arrays.asList(getArea(areaName).childrenNames());
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException("Could not process request", ex);
        }
    }

    @Override
    public void remove(String areaName, String id) {
        try {
            Preferences areaPref = getArea(areaName);
            if (areaPref.nodeExists(id)) {
                areaPref.node(id).removeNode();
                areaPref.flush();
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException("Could not process request", ex);
        }
    }
}
