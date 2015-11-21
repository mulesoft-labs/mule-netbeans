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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@NbBundle.Messages({
        "SingleRuntimeNode_shortDescription_name=Name:<b> {0} </b><p>",
        "SingleRuntimeNode_shortDescription_version=Version:<b> {0} </b><p>",
        "SingleRuntimeNode_shortDescription_muleHome=Mule Home:<b> {0} </b><p>",
        "SingleRuntimeNode_shortDescription_status=Status:<b> {0} </b><p>",
    })
public class SingleRuntimeNode extends AbstractNode {
    
    static final String BADGE = "org/mule/tooling/netbeans/runtime/resources/mule16.png"; // NOI18N

    private MuleRuntime muleRuntime;
    
    public SingleRuntimeNode(MuleRuntime muleRuntime) {
        super(Children.create(new RuntimeNodeChildFactory(muleRuntime), true));
        this.muleRuntime = muleRuntime;
        setDisplayName(muleRuntime.getName());
        setIconBaseWithExtension(BADGE);
    }

    @Override
    public void destroy() throws IOException {
        muleRuntime.unregister();
        super.destroy();
    }
    
    @Override 
    public String getShortDescription() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<html>");//NOI18N
        buffer.append(Bundle.SingleRuntimeNode_shortDescription_name(muleRuntime.getName()));
        buffer.append(Bundle.SingleRuntimeNode_shortDescription_version(muleRuntime.getVersion()));
        buffer.append(Bundle.SingleRuntimeNode_shortDescription_muleHome(muleRuntime.getMuleHome()));
        buffer.append(Bundle.SingleRuntimeNode_shortDescription_status(muleRuntime.getStatus()));
        buffer.append("</html>");//NOI18N
        return buffer.toString();
    }

    @Override
    public boolean canDestroy() {
        return true;
    }
    
    //--- Actions ---
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            new StartAction(),
            new StopAction(),
            null,
            DeleteAction.get(DeleteAction.class),
        };
    }
    
    public class StartAction extends AbstractAction {
        
        public StartAction() {
            putValue(Action.NAME, "Start");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
    
    public class StopAction extends AbstractAction {
        
        public StopAction() {
            putValue(Action.NAME, "Stop");
            setEnabled(false);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }
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
            if(key.equals(UserLibrariesNode.class)) {
                return new UserLibrariesNode(runtime);
//                File libUserFolder  = new File(runtime.getMuleHome() + File.separator + "lib" + File.separator + "user");
//                System.out.println(libUserFolder);
//                FileObject fo = FileUtil.toFileObject(libUserFolder);
//                System.out.println(fo);
//                DataFolder f = DataFolder.findFolder(fo);
//                return f.getNodeDelegate();
            }
            try {
                return key.newInstance();
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
