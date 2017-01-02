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
package org.mule.tooling.netbeans.runtime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.MuleRuntimeFactory;
import org.mule.tooling.netbeans.api.MuleRuntimeRegistry;
import org.mule.tooling.netbeans.api.Store;
import org.mule.tooling.netbeans.api.change.AbstractChangeSource;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@ServiceProviders({
    @ServiceProvider(service = MuleRuntimeFactory.class),
    @ServiceProvider(service = MuleRuntimeRegistry.class)
})
public class DefaultMuleRuntimeFactory extends AbstractChangeSource implements MuleRuntimeFactory, MuleRuntimeRegistry {

    private static final Logger LOGGER = Logger.getLogger(DefaultMuleRuntimeFactory.class.getName());
    private static final String RUNTIME_AREA = "runtimes";
    private final Map<Path, MuleRuntime> registry = new HashMap<Path, MuleRuntime>();
    private final Lock lock = new ReentrantLock();
    private AtomicBoolean loaded = new AtomicBoolean(false);

    @Override
    public MuleRuntime create(Path muleHome) {
        lock.lock();
        try {
            if (registry.containsKey(muleHome)) {
                return getRegistry().get(muleHome);
            }
            return doCreate(muleHome, true);
        } finally {
            lock.unlock();
        }
    }

    private MuleRuntime doCreate(Path muleHome, boolean doRegistration) {
        return new DefaultMuleRuntime(this, muleHome, doRegistration);
    }

    @Override
    public boolean isRegistered(MuleRuntime runtime) {
        lock.lock();
        try {
            return getRegistry().containsKey(runtime.getMuleHome());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void register(MuleRuntime runtime) {
        lock.lock();
        try {
            if (getRegistry().putIfAbsent(runtime.getMuleHome(), runtime) != null) {
                throw new IllegalArgumentException("Runtime already registered");
            }
            Preferences pref = getStore().get(RUNTIME_AREA, runtime.getId(), true);
            pref.put("muleHome", runtime.getMuleHome().toAbsolutePath().toString());
            pref.put("name", runtime.getName());
            pref.flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException("Could not process request", ex);
        } finally {
            lock.unlock();
        }
        fireChange();
    }

    @Override
    public void unregister(MuleRuntime runtime) {
        lock.lock();
        try {
            if (getRegistry().remove(runtime.getMuleHome()) != null) {
                getStore().remove(RUNTIME_AREA, runtime.getId());
            }
        } finally {
            lock.unlock();
        }
        fireChange();
    }

    @Override
    public List<MuleRuntime> getRegisteredRuntimes() {
        lock.lock();
        try {
            return new ArrayList(getRegistry().values());
        } finally {
            lock.unlock();
        }
    }

    protected Store getStore() {
        return Lookup.getDefault().lookup(Store.class);
    }

    private Map<Path, MuleRuntime> getRegistry() {
        if (loaded.compareAndSet(false, true)) {
            for (String id : getStore().getIds(RUNTIME_AREA)) {
                Preferences prefs = getStore().get(RUNTIME_AREA, id, false);
                Path muleHome = Paths.get(prefs.get("muleHome", null));
                MuleRuntime runtime = doCreate(muleHome, false);
                runtime.register();
                registry.put(muleHome, runtime);
            }
        }
        return registry;
    }
}
