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
import javax.swing.Action;
import org.mule.tooling.netbeans.api.Domain;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.common.IconUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@Messages({
    "DomainsNode_displayName=Domains",
    "DomainsNode_shortDescription=List of the deployed domains"
})
public class DomainsNode extends AbstractNode {

    public DomainsNode(MuleRuntime runtime) {
        super(Children.create(new DomainsChildFactory(runtime), true));
        setDisplayName(Bundle.DomainsNode_displayName());
        setShortDescription(Bundle.DomainsNode_shortDescription());
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
    private static class DomainsChildFactory extends AbstractChildFactory<Domain> {

        private MuleRuntime runtime;

        private DomainsChildFactory(MuleRuntime muleRuntime) {
            super(Lookups.singleton(muleRuntime));
            this.runtime = muleRuntime;
        }

        @Override
        protected boolean createKeys(List<Domain> toPopulate) {
            toPopulate.addAll(runtime.getDomains());
            return true;
        }

        @Override
        protected Node createNodeForKey(Domain key) {
            return new DomainNode(key);
        }
    }
    
    private static Children createDomainNodeChildren(Domain domain) {
        Children.Array arr = new Children.Array();
        arr.add(new Node[]{
            new ConfigurationsNode(Lookups.singleton(domain)),
            new LibrariesNode("Libraries", Lookups.singleton(domain))
        });
        return arr;
    }

    public static class DomainNode extends AbstractNode {

        public DomainNode(Domain domain) {
            super(createDomainNodeChildren(domain));
            setDisplayName(domain.getName());
        }

        @Override
        public Image getIcon(int type) {
            return IconUtil.getMuleIcon();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public boolean canDestroy() {
            return false;
        }
    }
}