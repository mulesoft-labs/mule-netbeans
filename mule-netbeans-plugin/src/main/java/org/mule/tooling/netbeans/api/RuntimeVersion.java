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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class RuntimeVersion {

    private static final Set<String> ENTERPRISE = new HashSet<String>(Arrays.asList(MuleRuntime.BRANCH_API_GW, MuleRuntime.BRANCH_MULE_EE));
    private final String branch;
    private final String number;

    public RuntimeVersion(String branch, String number) {
        this.branch = branch;
        this.number = number;
    }

    public String getBranch() {
        return branch;
    }

    public String getNumber() {
        return number;
    }

    public boolean isEnterprise() {
        return ENTERPRISE.contains(branch);
    }

    @Override
    public String toString() {
        return branch + " [" + number + "]";
    }
}
