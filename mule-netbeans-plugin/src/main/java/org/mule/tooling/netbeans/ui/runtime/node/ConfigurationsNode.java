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
package org.mule.tooling.netbeans.ui.runtime.node;

import java.awt.Image;
import java.io.File;
import java.util.List;
import javax.swing.Action;
import org.mule.tooling.netbeans.api.Configuration;
import org.mule.tooling.netbeans.api.ConfigurationsContainer;
import org.mule.tooling.netbeans.common.IconUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@Messages({
    "ConfigurationsNode_displayName=Configurations",
    "ConfigurationsNode_shortDescription=Runtime configuration files"
})
public class ConfigurationsNode extends AbstractNode {

    public ConfigurationsNode(final Lookup lookup) {
        super(Children.create(new AbstractFileChildFactory(lookup) {
            @Override
            protected boolean createKeys(List<File> toPopulate) {
                ConfigurationsContainer cc = lookup.lookup(ConfigurationsContainer.class);
                for (Configuration configuration : cc.getConfigurations()) {
                    toPopulate.add(configuration.getFile());
                }
                return true;
            }
        }, true));
        setDisplayName(Bundle.ConfigurationsNode_displayName());
        setShortDescription(Bundle.ConfigurationsNode_displayName());
    }

    @Override
    public Image getIcon(int param) {
        return IconUtil.getTreeFolderIconWithBadge(false, IconUtil.BADGE_CONFIG);
    }

    @Override
    public Image getOpenedIcon(int param) {
        return IconUtil.getTreeFolderIconWithBadge(true, IconUtil.BADGE_CONFIG);
    }

    //--- Actions ---
    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
        };
    }
}