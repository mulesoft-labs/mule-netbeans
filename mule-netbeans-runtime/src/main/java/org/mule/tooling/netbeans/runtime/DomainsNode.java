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
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
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
    private static class DomainsChildFactory extends ChildFactory.Detachable<String> implements FileChangeListener {

        private MuleRuntime runtime;

        private DomainsChildFactory(MuleRuntime muleRuntime) {
            this.runtime = muleRuntime;
        }

        @Override
        protected boolean createKeys(List<String> toPopulate) {
            File[] children = runtime.getDomainsDir().listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            for (File file : children) {
                toPopulate.add(file.getName());
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(String key) {
            return new DomainNode(key);
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

    public static class DomainNode extends AbstractNode {

        public DomainNode(String domainName) {
            super(Children.LEAF);
            setDisplayName(domainName);
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
        public void destroy() throws IOException {
            super.destroy();
        }

        @Override
        public boolean canDestroy() {
            return false;
        }
    }
}