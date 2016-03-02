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
package org.mule.tooling.netbeans.runtime;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import javax.swing.Action;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@Messages({
        "SingleRuntimeNode_shortDescription_name=Name:<b> {0} </b><p>",
        "SingleRuntimeNode_shortDescription_version=Version:<b> {0} </b><p>",
        "SingleRuntimeNode_shortDescription_muleHome=Mule Home:<b> {0} </b><p>",
        "SingleRuntimeNode_shortDescription_status=Status:<b> {0} </b><p>",
    })
public class SingleRuntimeNode extends AbstractNode {

    protected static final RequestProcessor RP = new RequestProcessor("Mule server control", 10);

    public SingleRuntimeNode(MuleRuntime muleRuntime) {
        super(Children.create(new RuntimeNodeChildFactory(muleRuntime), true));
        setName(muleRuntime.getId());
        setDisplayName(muleRuntime.getName());
        getCookieSet().add(new RuntimeCookie(muleRuntime));
    }

    @Override
    public Image getIcon(int type) {
        return IconUtil.getMuleServerIcon(getMuleRuntime().isRunning());
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();
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
        buffer.append(Bundle.SingleRuntimeNode_shortDescription_status(muleRuntime.getStatus()));
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
            SystemAction.get(StopRuntimeAction.class),
            null,
            SystemAction.get(TerminateRuntimeAction.class),
            null,
            DeleteAction.get(DeleteAction.class),
        };
    }
    
    //--- ChildFactory ---
    
    private static class RuntimeNodeChildFactory extends ChildFactory.Detachable<Class<? extends AbstractNode>> {
        private MuleRuntime runtime;

        private RuntimeNodeChildFactory(MuleRuntime muleRuntime) {
            this.runtime = muleRuntime;
        }
        
        @Override
        protected boolean createKeys(List<Class<? extends AbstractNode>> toPopulate) {
            toPopulate.add(ApplicationsNode.class);
            toPopulate.add(DomainsNode.class);
            toPopulate.add(UserLibrariesNode.class);
            return true;
        }

        @Override
        protected Node createNodeForKey(Class<? extends AbstractNode> key) {
            try {
                Constructor<? extends AbstractNode> constructor = key.getConstructor(MuleRuntime.class);
                return constructor.newInstance(runtime);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                throw new IllegalStateException(ex);
            }
        }

        @Override
        protected void addNotify() {
        }

        @Override
        protected void removeNotify() {
        }
    }
}
