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
package org.mule.tooling.netbeans.runtime.node;

import java.awt.Image;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mule.tooling.netbeans.api.Configuration;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.common.IconUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
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

    public ConfigurationsNode(MuleRuntime runtime) {
        super(Children.create(new ConfigurationsChildFactory(runtime), true));
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

    //--- ChildFactory ---
    private static class ConfigurationsChildFactory extends ChildFactory.Detachable<Configuration> implements ChangeListener {

        private MuleRuntime runtime;

        private ConfigurationsChildFactory(MuleRuntime muleRuntime) {
            this.runtime = muleRuntime;
        }

        @Override
        protected boolean createKeys(List<Configuration> toPopulate) {
            toPopulate.addAll(runtime.getConfigurations());
            return true;
        }

        @Override
        protected Node createNodeForKey(Configuration key) {
            FileObject fileObject = FileUtil.toFileObject(key.getFile());
            if (fileObject != null) {
                DataObject dataObject = null;
                try {
                    dataObject = DataObject.find(fileObject);
                } catch(DataObjectNotFoundException ex) {
                    Logger.getLogger(ConfigurationsNode.class.getName()).log(Level.INFO, null, ex);
                }
                if (dataObject != null) {
                    return dataObject.getNodeDelegate();
                }
            }
            return  null;
        }

        @Override
        protected void addNotify() {
            runtime.addChangeListener(this);
        }

        @Override
        protected void removeNotify() {
            runtime.removeChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }
    }
}