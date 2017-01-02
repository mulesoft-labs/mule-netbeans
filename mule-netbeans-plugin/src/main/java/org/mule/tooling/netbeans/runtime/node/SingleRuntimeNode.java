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
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.change.AttributeChangeEvent;
import org.mule.tooling.netbeans.common.IconUtil;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@Messages({
    "SingleRuntimeNode_shortDescription_name=Name:<b> {0} </b><p>",
    "SingleRuntimeNode_shortDescription_version=Version:<b> {0} </b><p>",
    "SingleRuntimeNode_shortDescription_muleHome=Mule Home:<b> {0} </b><p>",
    "SingleRuntimeNode_shortDescription_status=Status:<b> {0} </b><p>",})
public class SingleRuntimeNode extends AbstractNode implements ChangeListener {

    protected static final RequestProcessor RP = new RequestProcessor("Mule server control", 10);

    public SingleRuntimeNode(MuleRuntime muleRuntime) {
        super(Children.create(new RuntimeNodeChildFactory(muleRuntime), true));
        setName(muleRuntime.getId());
        setDisplayName(muleRuntime.getName());
        getCookieSet().add(new RuntimeCookie(muleRuntime));
        muleRuntime.addChangeListener(this);
    }

    @Override
    public Image getIcon(int type) {
        return IconUtil.getMuleServerIcon(getMuleRuntime().getRuntimeProcess().getStatus(), getCookieSet().getCookie(RuntimeCookie.class).isDebugging());
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e instanceof AttributeChangeEvent && ((AttributeChangeEvent) e).getAttributeName().equals("status")) {
            fireIconChange();
        }
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();
        getMuleRuntime().removeChangeListener(this);
        getMuleRuntime().unregister();
    }

    @Override
    public String getShortDescription() {
        MuleRuntime muleRuntime = getMuleRuntime();
        StringBuilder buffer = new StringBuilder();
        buffer.append("<html>");//NOI18N
        buffer.append(Bundle.SingleRuntimeNode_shortDescription_name(muleRuntime.getName()));
        buffer.append(Bundle.SingleRuntimeNode_shortDescription_version(muleRuntime.getVersion()));
        buffer.append(Bundle.SingleRuntimeNode_shortDescription_muleHome(muleRuntime.getMuleHome()));
        buffer.append(Bundle.SingleRuntimeNode_shortDescription_status(muleRuntime.getRuntimeProcess().getStatus()));
        buffer.append("</html>");//NOI18N
        return buffer.toString();
    }

    protected MuleRuntime getMuleRuntime() {
        return getCookieSet().getCookie(RuntimeCookie.class).getRuntime();
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    //--- Actions ---
    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            SystemAction.get(StartRuntimeAction.class),
            SystemAction.get(DebugRuntimeAction.class),
            SystemAction.get(StopRuntimeAction.class),
            SystemAction.get(ViewRuntimeLogsAction.class),
            null,
            SystemAction.get(TerminateRuntimeAction.class),
            null,
            SystemAction.get(TerminateRuntimeAction.class),
            null,
            DeleteAction.get(DeleteAction.class)};
    }

    //--- ChildFactory ---
    private static class RuntimeNodeChildFactory extends AbstractChildFactory<Class<? extends AbstractNode>> {

        private RuntimeNodeChildFactory(MuleRuntime muleRuntime) {
            super(Lookups.singleton(muleRuntime));
        }

        @Override
        protected boolean createKeys(List<Class<? extends AbstractNode>> toPopulate) {
            toPopulate.add(ConfigurationsNode.class);
            toPopulate.add(ApplicationsNode.class);
            toPopulate.add(DomainsNode.class);
            toPopulate.add(LibrariesNode.class);
            return true;
        }

        @Override
        protected Node createNodeForKey(Class<? extends AbstractNode> key) {
            MuleRuntime runtime = lookup.lookup(MuleRuntime.class);
            if (ConfigurationsNode.class.equals(key)) {
                return new ConfigurationsNode(Lookups.singleton(runtime));
            } else if (ApplicationsNode.class.equals(key)) {
                return new ApplicationsNode(runtime);
            } else if (DomainsNode.class.equals(key)) {
                return new DomainsNode(runtime);
            } else if (LibrariesNode.class.equals(key)) {
                return new LibrariesNode("User Libraries", Lookups.singleton(runtime));
            }
            throw new IllegalStateException("Unrecognized node type: " + key);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e instanceof AttributeChangeEvent && ((AttributeChangeEvent) e).getAttributeName().equals("status")) {
                refresh(true);
            }
        }
    }
}
