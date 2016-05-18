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
import org.mule.tooling.netbeans.api.Application;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.common.IconUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

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

    //--- ChildFactory ---
    private static class ApplicationsChildFactory extends AbstractChildFactory<Application> {

        private MuleRuntime runtime;

        private ApplicationsChildFactory(MuleRuntime muleRuntime) {
            super(Lookups.singleton(muleRuntime));
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
    }

    @NbBundle.Messages({
        "ApplicationNode_name=Name:<b> {0} </b><p>",
        "ApplicationNode_domain=Domain:<b> {0} </b><p>"})
    public static class ApplicationNode extends AbstractNode {

        private static Children createChildren(Application app) {
            Children.Array arr = new Children.Array();
            arr.add(new Node[]{
                new ConfigurationsNode(Lookups.singleton(app)),
                new LibrariesNode("Libraries", Lookups.singleton(app))
            });
            return arr;
        }

        private Application app;

        public ApplicationNode(Application app) {
            super(createChildren(app));
            this.app = app;
            String name = app.getName();
            if (app.getDomainName().length() != 0) {
                name += " (" + app.getDomainName() + ")";
            }
            setDisplayName(name);
        }

        @Override
        public Image getIcon(int type) {
            return IconUtil.getMuleIcon();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return IconUtil.getMuleIcon();
        }

        @Override
        public String getShortDescription() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("<html>");//NOI18N
            buffer.append(Bundle.ApplicationNode_name(app.getName()));
            buffer.append(Bundle.ApplicationNode_domain(app.getDomainName()));
            buffer.append("</html>");//NOI18N
            return buffer.toString();
        }

        @Override
        public boolean canDestroy() {
            return false;
        }
    }
}
