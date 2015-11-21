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
import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class IconUtil {
    @StaticResource
    private static final String MULE_RUNTIME_ICON_BASE = "org/mule/tooling/netbeans/runtime/resources/mule16.png";  //NOI18N
    @StaticResource
    private static final String JAR_ICON_BASE = "org/mule/tooling/netbeans/runtime/resources/jar.png"; // NOI18N
//    private static final @StaticResource String LIBS_BADGE = "org/netbeans/modules/maven/libraries-badge.png";
//    private static final @StaticResource String DEF_FOLDER = "org/netbeans/modules/maven/defaultFolder.gif";    
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N
    @StaticResource
    private static final String FOLDER_OPENED_ICON_PATH = "org/mule/tooling/netbeans/runtime/resources/defaultFolderOpen.gif"; // NOI18N
    @StaticResource
    private static final String FOLDER_ICON_PATH = "org/mule/tooling/netbeans/runtime/resources/defaultFolder.gif"; // NOI18N
    @StaticResource
    private static final String BADGE_LIBRARIES_PATH = "org/mule/tooling/netbeans/runtime/resources/libraries-badge.png"; // NOI18N
    
    /**
     * Returns default folder icon as {@link java.awt.Image}. Never returns
     * <code>null</code>.
     *
     * @param opened wheter closed or opened icon should be returned.
     * 
     * copied from apisupport/project
     */
    public static Image getTreeFolderIcon(boolean opened) {
        Image base;
        Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
        if (baseIcon != null) {
            base = ImageUtilities.icon2Image(baseIcon);
        } else {
            base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263
            if (base == null) { // fallback to our owns
                base = ImageUtilities.loadImage(opened ? FOLDER_OPENED_ICON_PATH : FOLDER_ICON_PATH, true);
            }
        }
        assert base != null;
        return base;
    }
    
    public static Image getTreeFolderIconWithBadge(boolean opened, Image badge) {
        return ImageUtilities.mergeImages(IconUtil.getTreeFolderIcon(true),
                badge, //NOI18N
                8, 8);
    }
    
    public static Image getLibraryBadge() {
        return ImageUtilities.loadImage(BADGE_LIBRARIES_PATH);
    }
    
    public static Image getJarIcon() {
        return ImageUtilities.loadImage(JAR_ICON_BASE, true);
    }
}
