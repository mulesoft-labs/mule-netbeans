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
package org.mule.tooling.netbeans.runtime.node;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public abstract class AbstractFileChildFactory extends AbstractChildFactory<File> {

    public AbstractFileChildFactory(Lookup lookup) {
        super(lookup);
    }
    
    public AbstractFileChildFactory() {
        super(Lookups.fixed());
    }
    
    @Override
    protected Node createNodeForKey(File key) {
        FileObject fileObject = FileUtil.toFileObject(key);
        if (fileObject != null) {
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fileObject);
            } catch (DataObjectNotFoundException ex) {
                Logger.getLogger(ConfigurationsNode.class.getName()).log(Level.INFO, null, ex);
            }
            if (dataObject != null) {
                return dataObject.getNodeDelegate();
            }
        }
        return null;
    }
}
