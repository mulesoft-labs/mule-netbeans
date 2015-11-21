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
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.openide.actions.DeleteAction;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
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

    private static final Pattern JAR_PATTERN = Pattern.compile("(.*?)\\.jar"); // NOI18N
    private MuleRuntime runtime;

    public UserLibrariesNode(MuleRuntime runtime) {
        super(Children.create(new LibrariesChildFactory(runtime), true));
        setDisplayName(Bundle.UserLibrariesNode_displayName());
        setShortDescription(Bundle.UserLibrariesNode_shortDescription());
        this.runtime = runtime;
    }

    @Override
    public Image getIcon(int param) {
        return IconUtil.getTreeFolderIconWithBadge(false, IconUtil.getLibraryBadge());
    }

    @Override
    public Image getOpenedIcon(int param) {
        return IconUtil.getTreeFolderIconWithBadge(true, IconUtil.getLibraryBadge());
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
    private static class LibrariesChildFactory extends ChildFactory.Detachable<String> implements FileChangeListener {

        private MuleRuntime runtime;

        private LibrariesChildFactory(MuleRuntime muleRuntime) {
            this.runtime = muleRuntime;
        }

        @Override
        protected boolean createKeys(List<String> toPopulate) {
            File[] children = runtime.getLibUserDir().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return JAR_PATTERN.matcher(name).matches();
                }
            });
            for (File file : children) {
                toPopulate.add(file.getName());
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(String key) {
            return new LibraryNode(key);
        }

        @Override
        protected void addNotify() {
            FileUtil.addFileChangeListener(this, runtime.getLibUserDir());
        }

        @Override
        protected void removeNotify() {
            FileUtil.removeFileChangeListener(this, runtime.getLibUserDir());
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            refresh(false);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            refresh(false);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            refresh(false);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            refresh(false);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }

    public static class LibraryNode extends AbstractNode {

        public LibraryNode(String jar) {
            super(Children.LEAF);
            setDisplayName(jar);
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
