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
package org.mule.tooling.netbeans.ui.runtime.node;

import org.mule.tooling.netbeans.ui.runtime.view.MuleHomeView;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle.Messages;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.MuleSupport;
import org.mule.tooling.netbeans.api.change.ChangeSource;
import org.mule.tooling.netbeans.common.IconUtil;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@ServicesTabNodeRegistration(
        name = MuleRuntimeServicesNode.NODE_NAME,
        displayName = "#MuleRuntimeSupport_DisplayName",
        shortDescription = "#MuleRuntimeSupport_ShortDescription",
        iconResource = IconUtil.MULE_ICON,
        position = 1000)
@Messages({
    "MuleRuntimeSupport_DisplayName=Mule Runtimes",
    "MuleRuntimeSupport_ShortDescription=Mule Runtime Support",    
    "MuleRuntimeSupport_AddRuntimeActionTitle=Add Runtime",
    "MuleRuntimeSupport_RefreshRuntimeActionTitle=Refresh"
})
public class MuleRuntimeServicesNode extends AbstractNode {

    static final String NODE_NAME = "muleruntimes"; // NOI18N

    private MuleRuntimeServicesNode() {
        super(Children.create(new MuleRuntimeNodesFactory(MuleSupport.getRegistry()), true));
        setName(NODE_NAME);
        setDisplayName(Bundle.MuleRuntimeSupport_DisplayName());
        setIconBaseWithExtension(IconUtil.MULE_ICON);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            new AddRuntimeAction(),
            null,
            new RefreshRuntimeAction()
        };
    }
    
    private void refresh() {
    }

    private class AddRuntimeAction extends AbstractAction {

        public AddRuntimeAction() {
            super(Bundle.MuleRuntimeSupport_AddRuntimeActionTitle());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Path muleHome = MuleHomeView.capture();
            if(muleHome != null) {
                MuleSupport.getMuleRuntime(muleHome).register();
            }
        }
    }

    private class RefreshRuntimeAction extends AbstractAction {

        public RefreshRuntimeAction() {
            super(Bundle.MuleRuntimeSupport_RefreshRuntimeActionTitle());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    private static class MuleRuntimeNodesFactory extends AbstractChildFactory<MuleRuntime> {

        public MuleRuntimeNodesFactory(ChangeSource changeSource) {
            super(Lookups.singleton(changeSource));
        }

        @Override
        protected boolean createKeys(List<MuleRuntime> toPopulate) {
            toPopulate.addAll(MuleSupport.getRegistry().getRegisteredRuntimes());
            Collections.sort(toPopulate, new Comparator<MuleRuntime>() {
                @Override
                public int compare(MuleRuntime o1, MuleRuntime o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return true;
        }

        @Override
        protected Node createNodeForKey(MuleRuntime key) {
            return new SingleRuntimeNode(key);
        }
    }
}
