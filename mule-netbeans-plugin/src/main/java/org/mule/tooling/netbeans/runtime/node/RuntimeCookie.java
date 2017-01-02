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
package org.mule.tooling.netbeans.runtime.node;

import java.util.concurrent.atomic.AtomicBoolean;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.RuntimeProcess;
import org.openide.nodes.Node;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class RuntimeCookie implements Node.Cookie {

    private final MuleRuntime runtime;
    private AtomicBoolean debug = new AtomicBoolean(false);

    public RuntimeCookie(MuleRuntime runtime) {
        this.runtime = runtime;
    }

    public MuleRuntime getRuntime() {
        return runtime;
    }
    
    public void start(boolean debug) {
        getRuntimeProcess().start(debug);
        setDebbuging(debug);
    }

    private RuntimeProcess getRuntimeProcess() {
        return runtime.getRuntimeProcess();
    }
    
    public boolean canStart() {
        return getRuntimeProcess().canStart();
    }
    
    public void stop(boolean forced) {
        getRuntimeProcess().stop(forced);
    }
    
    public boolean canStop() {
        return getRuntimeProcess().canStop();
    }
    
    public boolean isRunning() {
        return getRuntimeProcess().isRunning();
    }
    
    public boolean isDebugging() {
        return debug.get();
    }
    
    public void setDebbuging(boolean debug) {
        this.debug.set(debug);
    }
}
