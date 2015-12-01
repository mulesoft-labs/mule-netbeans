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
import org.openide.util.Lookup;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class MuleSupport {
    
    private static final MuleRuntimeStore STORE;
    private static final MuleRuntimeRegistry REGISTRY;
    private static final MuleRuntimeFactory FACTORY;
    static {
        FACTORY = Lookup.getDefault().lookup(MuleRuntimeFactory.class);
        STORE = Lookup.getDefault().lookup(MuleRuntimeStore.class);
        REGISTRY = Lookup.getDefault().lookup(MuleRuntimeRegistry.class);
    }
    
    public static MuleRuntime getMuleRuntime(File muleHome) {
        return FACTORY.create(muleHome);
    }
    
    public static MuleRuntime getMuleRuntime(String id) {
        return getStore().get(id);
    }
    
    public static MuleRuntimeStore getStore() {
        return STORE;
    }
    
    public static MuleRuntimeRegistry getRegistry() {
        return REGISTRY;
    }
}
