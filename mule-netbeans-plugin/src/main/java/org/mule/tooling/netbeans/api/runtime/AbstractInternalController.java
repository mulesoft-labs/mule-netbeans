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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mule.tooling.netbeans.api.Lifecycle;
import org.mule.tooling.netbeans.api.Named;
import org.mule.tooling.netbeans.api.change.ChangeSupport;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public abstract class AbstractInternalController<T extends Named> extends FileChangeAdapter implements InternalController, RuntimeConstants {

    private final Map<String, T> artifacts = new ConcurrentHashMap<String, T>();
    private final File targetDir;
    private final ChangeSupport cs;

    public AbstractInternalController(File deploymentDir, ChangeSupport cs) {
        this.targetDir = deploymentDir;
        this.cs = cs;
    }

    public List<T> getArtefacts() {
        return new ArrayList<T>(artifacts.values());
    }

    @Override
    public void initialize() {
        FileUtil.addFileChangeListener(this, targetDir);
        detect();
    }

    protected void detect() {
        File[] children = targetDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return doAccept(pathname);
            }
        });
        if (children == null) {
            return;
        }
        for (File file : children) {
            add(file, false);
        }
    }

    protected abstract boolean doAccept(File pathname);

    protected void add(File file, boolean notify) {
        T artifact = doCreate(file);
        artifacts.put(artifact.getName(), artifact);
        if (notify) {
            cs.fireChange(getAttributeName(), artifact);
        }
        if(artifact instanceof Lifecycle) {
            ((Lifecycle) artifact).initialize();
        }
    }

    protected abstract T doCreate(File file);

    protected void remove(String name) {
        T artifact = artifacts.remove(name);
        cs.fireChange(getAttributeName(), artifact);
        if(artifact instanceof Lifecycle) {
            ((Lifecycle) artifact).shutdown();
        }
    }

    protected abstract String getAttributeName();

    @Override
    public void shutdown() {
        FileUtil.removeFileChangeListener(this, targetDir);
        artifacts.clear();
    }
}
