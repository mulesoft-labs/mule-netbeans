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
package org.mule.tooling.netbeans.api.runtime;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.MuleRuntimeFactory;
import org.mule.tooling.netbeans.api.MuleRuntimeRegistry;
import org.mule.tooling.netbeans.api.MuleSupport;
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
public class DefaultMuleRuntimeFactory implements MuleRuntimeFactory, MuleRuntimeRegistry {

    private final ConcurrentHashMap<Path, MuleRuntime> registry = new ConcurrentHashMap<Path, MuleRuntime>();

    @Override
    public MuleRuntime create(Path muleHome) {
        return registry.getOrDefault(muleHome, doCreate(muleHome));
    }

    private MuleRuntime doCreate(Path muleHome) {
        return new DefaultMuleRuntime(this, muleHome);
    }

    @Override
    public boolean isRegistered(MuleRuntime runtime) {
        return registry.containsKey(runtime.getMuleHome());
    }

    @Override
    public void register(MuleRuntime runtime) {
        if (registry.putIfAbsent(runtime.getMuleHome(), runtime) != null) {
            throw new IllegalArgumentException("Runtime already registered");
        }
        MuleSupport.getStore().store(runtime);
    }

    @Override
    public void unregister(MuleRuntime runtime) {
        if (registry.remove(runtime.getMuleHome()) != null) {
            MuleSupport.getStore().remove(runtime);
        }
    }
}
