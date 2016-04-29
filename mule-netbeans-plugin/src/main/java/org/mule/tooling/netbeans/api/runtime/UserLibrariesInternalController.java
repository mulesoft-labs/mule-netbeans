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
import java.util.regex.Pattern;
import org.mule.tooling.netbeans.api.Library;
import org.mule.tooling.netbeans.api.change.ChangeSupport;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class UserLibrariesInternalController extends AbstractInternalController<Library> {

    private static final Pattern JAR_PATTERN = Pattern.compile("(.*?)\\.jar"); // NOI18N

    public UserLibrariesInternalController(File libPath, ChangeSupport cs) {
        super(libPath, cs);
    }

    @Override
    protected boolean doAccept(File pathname) {
        return JAR_PATTERN.matcher(pathname.getName()).matches();
    }

    @Override
    protected Library doCreate(File file) {
        return new JarLibrary(file);
    }

    @Override
    protected String getAttributeName() {
        return USERLIBS;
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        add(new File(fe.getFile().getPath()), true);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        if (JAR_PATTERN.matcher(fe.getFile().getName()).matches()) {
            remove(fe.getFile().getName());
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        if (JAR_PATTERN.matcher(fe.getFile().getName()).matches()) {
            remove(fe.getName());
            add(new File(fe.getFile().getPath()), true);
        }
    }
}
