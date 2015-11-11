/*
 * Copyright 2015 facundolopezkaufmann.
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

import org.mule.tooling.netbeans.api.MuleRuntimeSupport;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@ServicesTabNodeRegistration(
        name = MuleRuntimeServicesNode.NODE_NAME,
        displayName = "#MuleRuntimeSupport_DisplayName",
        shortDescription = "#MuleRuntimeSupport_ShortDescription",
        iconResource = MuleRuntimeServicesNode.MULE_RUNTIME_ICON,
        position = 1000)
@NbBundle.Messages({
    "MuleRuntimeSupport_DisplayName=Mule Runtimes",
    "MuleRuntimeSupport_ShortDescription=Mule Runtime Support"
})
public class MuleRuntimeServicesNode extends AbstractNode {

    static final @StaticResource
    String MULE_RUNTIME_ICON = "org/mule/tooling/netbeans/runtime/resources/mule16.png";  //NOI18N
    static final @StaticResource
    String RUNNING_BADGE = "org/mule/tooling/netbeans/runtime/resources/mule16.png"; // NOI18N
    static final @StaticResource
    String WAITING_BADGE = "org/mule/tooling/netbeans/runtime/resources/mule16.png"; // NOI18N
    static final String NODE_NAME = "muleruntime"; // NOI18N

    private MuleRuntimeSupport support;

    private MuleRuntimeServicesNode() {
        super(Children.LEAF);
        setName(NODE_NAME);
        setDisplayName(NbBundle.getMessage(MuleRuntimeServicesNode.class, "MuleRuntimeSupport_DisplayName"));
        setIconBaseWithExtension(MULE_RUNTIME_ICON);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            new AddAction()
        };
    }

    private static class AddAction extends AbstractAction {

        public AddAction() {
            super("Add runtime...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
}
