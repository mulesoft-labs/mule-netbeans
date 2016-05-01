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
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.mule.tooling.netbeans.api.Library;
import org.mule.tooling.netbeans.common.IconUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.mule.tooling.netbeans.api.LibrariesContainer;
import org.openide.util.Lookup;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@NbBundle.Messages({
    "LibrariesNode_shortDescription=List of libraries"
})
public class LibrariesNode extends AbstractNode {

    public LibrariesNode(String name, Lookup lookup) {
        super(Children.create(new AbstractFileChildFactory(lookup) {
            @Override
            protected boolean createKeys(List<File> toPopulate) {
                LibrariesContainer lc = lookup.lookup(LibrariesContainer.class);
                for (Library library : lc.getLibraries()) {
                    toPopulate.add(library.getFile());
                }
                return true;
            }
        }, true));
        setDisplayName(name);
        setShortDescription(Bundle.LibrariesNode_shortDescription());
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
}
