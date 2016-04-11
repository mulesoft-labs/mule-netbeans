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

import org.mule.tooling.netbeans.api.runtime.DefaultMuleRuntimeFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class MuleRuntimeTest {

    private Path getMasterCopy() {
        String muleHomeBase = new StringBuilder()
                .append(System.getProperty("user.dir"))
                .append(File.separator).append("src")
                .append(File.separator).append("test")
                .append(File.separator).append("mule-install").toString();
        return Paths.get(muleHomeBase);
    }

    private Path getMuleHome() {
        String muleHome = new StringBuilder()
                .append(System.getProperty("user.dir"))
                .append(File.separator).append("target")
                .append(File.separator).append("mule-install").toString();
        return Paths.get(muleHome);
    }

    @Before
    public void init() throws IOException {
        final Path basePath = getMasterCopy();
        final Path targetPath = getMuleHome();
        Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(targetPath.resolve(basePath.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                if (!file.endsWith(".DS_Store")) {
                    Files.copy(file, targetPath.resolve(basePath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    private DefaultMuleRuntimeFactory factory = new DefaultMuleRuntimeFactory();

    @Test(expected = IllegalStateException.class)
    public void muleHomeDoesNotExist() throws Exception {
        factory.create(getMuleHome().resolve("doesNotExists"));
    }

    @Test(expected = IllegalStateException.class)
    public void libFolderDoesNotExist() throws Exception {
        deleteDirectory(getMuleHome().resolve("lib").toFile());
        factory.create(getMuleHome());
    }

    @Test(expected = IllegalStateException.class)
    public void libBootFolderDoesNotExist() throws Exception {
        deleteDirectory(getMuleHome().resolve("lib" + File.separator + "boot").toFile());
        factory.create(getMuleHome());
    }

    @Test(expected = IllegalStateException.class)
    public void bootJarDoesNotExist() throws Exception {
        Files.delete(getMuleHome().resolve("lib" + File.separator + "boot" + File.separator + "mule-module-boot.jar"));
        factory.create(getMuleHome());
    }

    @Test
    public void muleHome() throws Exception {
        MuleRuntime runtime = factory.create(getMuleHome());
        Assert.assertEquals(Status.DOWN, runtime.getStatus());
    }
}
