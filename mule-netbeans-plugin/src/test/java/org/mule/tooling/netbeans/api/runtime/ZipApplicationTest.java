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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Test;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class ZipApplicationTest {
    
    @Test
    public void testZipApplication() throws URISyntaxException, IOException {
        URL resource = ZipApplicationTest.class.getResource("/basic.zip");
        ZipFile zf = new ZipFile(new File(resource.toURI()));
        ZipEntry entry = zf.getEntry("mule-deploy.properties");
        if (entry != null) {
            Scanner s = new Scanner(zf.getInputStream(entry)).useDelimiter("\\A");
            System.out.println(s.hasNext() ? s.next() : "");
        }
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            System.out.println(entries.nextElement());
        }
    }
}
