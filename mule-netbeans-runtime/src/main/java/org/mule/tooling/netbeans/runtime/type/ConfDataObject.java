/*
 * Copyright 2016 facundolopezkaufmann.
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
package org.mule.tooling.netbeans.runtime.type;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;

@Messages({
    "LBL_Conf_LOADER=Files of Conf"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Conf_LOADER",
        mimeType = "text/x-conf-properties+x-properties",
        extension = {"conf"}
)
@DataObject.Registration(
        mimeType = "text/x-conf-properties+x-properties",
        iconBase = "org/mule/tooling/netbeans/runtime/resources/conf-type.png",
        displayName = "#LBL_Conf_LOADER",
        position = 300
)
public class ConfDataObject extends MultiDataObject {

    public ConfDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/x-conf-properties+x-properties", false);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
}
