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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mule.tooling.netbeans.api.Application;
import org.mule.tooling.netbeans.api.Library;
import org.mule.tooling.netbeans.api.MuleRuntime;
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
    "ApplicationsNode_displayName=Applications",
    "ApplicationsNode_shortDescription=List of the deployed applications"
})
public class ApplicationsNode extends AbstractNode {

    public ApplicationsNode(MuleRuntime runtime) {
        super(Children.create(new ApplicationsChildFactory(runtime), true));
        setDisplayName(Bundle.ApplicationsNode_displayName());
        setShortDescription(Bundle.ApplicationsNode_shortDescription());
    }

    @Override
    public Image getIcon(int param) {
        return IconUtil.getTreeFolderIcon(false);
    }

    @Override
    public Image getOpenedIcon(int param) {
        return IconUtil.getTreeFolderIcon(true);
    }

    //--- Actions ---
    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
        };
    }

    //--- ChildFactory ---
    private static class ApplicationsChildFactory extends ChildFactory.Detachable<Application> implements ChangeListener {

        private MuleRuntime runtime;

        private ApplicationsChildFactory(MuleRuntime muleRuntime) {
            this.runtime = muleRuntime;
        }

        @Override
        protected boolean createKeys(List<Application> toPopulate) {
            toPopulate.addAll(runtime.getApplications());
            return true;
        }

        @Override
        protected Node createNodeForKey(Application key) {
            return new ApplicationNode(key);
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
    
    @Messages({
        "ApplicationsNode_ApplicationNode_name=Name:<b> {0} </b><p>",
        "ApplicationsNode_ApplicationNode_domain=Domain:<b> {0} </b><p>",
        "ApplicationsNode_ApplicationNode_libs=Libs:<p>",
        "ApplicationsNode_ApplicationNode_libitem=<b> - {0} </b><p>",
    })
    public static class ApplicationNode extends AbstractNode {
        
        private Application app;
        
        public ApplicationNode(Application app) {
            super(Children.LEAF);
            this.app = app;
            setDisplayName(app.getName());
        }

        @Override
        public Image getIcon(int type) {
            return IconUtil.getMuleIcon();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{};
        }
    
        @Override 
        public String getShortDescription() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("<html>");//NOI18N
            buffer.append(Bundle.ApplicationsNode_ApplicationNode_name(app.getName()));
            buffer.append(Bundle.ApplicationsNode_ApplicationNode_domain(app.getDomainName()));
            buffer.append(Bundle.ApplicationsNode_ApplicationNode_libs());
            for (Library lib : app.getLibraries()) {
                buffer.append(Bundle.ApplicationsNode_ApplicationNode_libitem(lib.getName()));
            }
            buffer.append("</html>");//NOI18N
            return buffer.toString();
        }

        @Override
        public void destroy() throws IOException {
            super.destroy();
        }

        @Override
        public boolean canDestroy() {
            return false;
        }
    }
}