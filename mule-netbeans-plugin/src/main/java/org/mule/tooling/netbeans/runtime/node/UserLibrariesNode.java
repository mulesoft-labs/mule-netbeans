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
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mule.tooling.netbeans.api.Library;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.common.IconUtil;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@NbBundle.Messages({
    "UserLibrariesNode_displayName=User Libraries",
    "UserLibrariesNode_shortDescription=List of the deployed applications"
})
public class UserLibrariesNode extends AbstractNode {

    private MuleRuntime runtime;

    public UserLibrariesNode(MuleRuntime runtime) {
        super(Children.create(new LibrariesChildFactory(runtime), true));
        setDisplayName(Bundle.UserLibrariesNode_displayName());
        setShortDescription(Bundle.UserLibrariesNode_shortDescription());
        this.runtime = runtime;
    }

    @Override
    public Image getIcon(int param) {
        return IconUtil.getTreeFolderIconWithBadge(false, IconUtil.BADGE_LIBRARIES);
    }

    @Override
    public Image getOpenedIcon(int param) {
        return IconUtil.getTreeFolderIconWithBadge(true, IconUtil.BADGE_LIBRARIES);
    }

    //--- Actions ---
    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            new AddLibraryAction()
        };
    }

    public static class AddLibraryAction extends AbstractAction {

        public AddLibraryAction() {
            putValue(Action.NAME, "Add");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    //--- ChildFactory ---
    private static class LibrariesChildFactory extends ChildFactory.Detachable<Library> implements ChangeListener {

        private MuleRuntime runtime;

        private LibrariesChildFactory(MuleRuntime muleRuntime) {
            this.runtime = muleRuntime;
        }

        @Override
        protected boolean createKeys(List<Library> toPopulate) {
            toPopulate.addAll(runtime.getLibraries());
            return true;
        }

        @Override
        protected Node createNodeForKey(Library key) {
            return new LibraryNode(key);
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

    public static class LibraryNode extends AbstractNode {

        public LibraryNode(Library jar) {
            super(Children.LEAF);
            setDisplayName(jar.getName());
        }

        @Override
        public Image getIcon(int type) {
            return IconUtil.getJarIcon();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                DeleteAction.get(DeleteAction.class),};
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
