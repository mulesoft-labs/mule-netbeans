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
package org.mule.tooling.netbeans.runtime;

import java.util.UUID;
import org.openide.util.lookup.ServiceProvider;
import org.mule.tooling.netbeans.api.IDGenerationStrategy;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@ServiceProvider(service = IDGenerationStrategy.class)
public class SimpleIDGenerationStrategy implements IDGenerationStrategy {

    @Override
    public String newId() {
        return UUID.randomUUID().toString();
    }
}
