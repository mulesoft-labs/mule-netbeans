/*
 * Copyright 2017 Facundo Lopez Kaufmann.
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
package org.mule.tooling.netbeans.ui.runtime.node;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.mule.tooling.netbeans.ui.runtime.view.MuleLicenseView;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@NbBundle.Messages({
    "InstallLicenseAction_name=Install License"
})
public class InstallLicenseAction extends NodeAction {
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        Path license = MuleLicenseView.capture();
        try {
            if(license != null) {
                Path tmpLicense = Paths.get(System.getProperty("java.io.tmpdir"), "license" + System.currentTimeMillis() + ".lic");
                Files.copy(license, tmpLicense);
                activatedNodes[0].getLookup()
                    .lookup(RuntimeCookie.class)
                    .installLicense(tmpLicense);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        RuntimeCookie cookie = activatedNodes[0].getLookup().lookup(RuntimeCookie.class);
        if(cookie == null) {
            return false;
        }
        if (cookie.isRunning()) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return Bundle.InstallLicenseAction_name();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
