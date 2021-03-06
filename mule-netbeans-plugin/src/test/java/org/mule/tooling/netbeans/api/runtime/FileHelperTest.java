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
package org.mule.tooling.netbeans.api.runtime;

import org.mule.tooling.netbeans.runtime.FileHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class FileHelperTest {

    /**
     * Test of isJar method, of class FileHelper.
     */
    @Test
    public void testIsJar() {
        Assert.assertTrue(FileHelper.isJar("parapam.jar"));
        Assert.assertFalse(FileHelper.isJar(".DS_Store"));
        Assert.assertFalse(FileHelper.isJar("file"));
    }
}
