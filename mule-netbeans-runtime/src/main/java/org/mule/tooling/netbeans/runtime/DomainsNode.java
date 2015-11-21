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
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
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
    private static final String FOLDER_ICON_BASE = "org/openide/loaders/defaultFolder.gif"; // NOI18N
    
    public DomainsNode() {
        super(Children.LEAF);
        setDisplayName(Bundle.DomainsNode_displayName());
        setShortDescription(Bundle.DomainsNode_shortDescription());
    }
    
    @Override
    public Image getIcon(int param) {
        return IconUtil.getTreeFolderIconWithBadge(false, IconUtil.getLibraryBadge());
    }
    
    @Override
    public Image getOpenedIcon(int param) {
        return IconUtil.getTreeFolderIconWithBadge(true, IconUtil.getLibraryBadge());
    }
}