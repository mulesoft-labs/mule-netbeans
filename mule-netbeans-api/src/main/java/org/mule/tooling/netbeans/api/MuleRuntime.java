/*
 * Copyright 2015 facundolopezkaufmann.
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
import java.util.List;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public interface MuleRuntime extends ChangeSource {

    public static final String BRANCH_MULE = "Mule";
    public static final String BRANCH_API_GW = "API Gateway";
    public static final String BRANCH_MULE_EE = "Mule EE";

    public String getId();

    public String getName();

    public RuntimeVersion getVersion();

    public File getMuleHome();
    
    public List<Application> getApplications();
    
    public List<Domain> getDomains();
    
    public List<Library> getLibraries();

    public Status getStatus();

    public boolean isRunning();

    public boolean canStart();

    public void start();

    public boolean canStop();

    public void stop(boolean forced);
    
    /**
     * Returns if the mule runtime is registered in the
     * {@link MuleRuntimeRegistry} or not. This should be equivalent to:
     * 
     * muleRuntimeRegistry.isRegistered(muleRuntime);
     *
     * @return if the runtime is running or not.
     */
    public boolean isRegistered();
    
    public void register();

    public void unregister();
}
