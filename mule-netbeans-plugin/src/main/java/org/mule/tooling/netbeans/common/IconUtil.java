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
package org.mule.tooling.netbeans.common;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import org.mule.tooling.netbeans.api.Status;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class IconUtil {

    @StaticResource
    public static final String BADGE_CONFIG = "org/mule/tooling/netbeans/resources/badges/config-badge.png";  //NOI18N
    @StaticResource
    public static final String BADGE_DEBUGGING = "org/mule/tooling/netbeans/resources/badges/debugging-badge.png";  //NOI18N
    @StaticResource
    public static final String BADGE_ERROR = "org/mule/tooling/netbeans/resources/badges/error-badge.png";  //NOI18N
    @StaticResource
    public static final String BADGE_LIBRARIES = "org/mule/tooling/netbeans/resources/badges/libraries-badge.png";  //NOI18N
    @StaticResource
    public static final String BADGE_MULE = "org/mule/tooling/netbeans/resources/badges/mule-badge.png";  //NOI18N
    @StaticResource
    public static final String BADGE_MULEBG = "org/mule/tooling/netbeans/resources/badges/mulebg-badge.png";  //NOI18N
    @StaticResource
    public static final String BADGE_RUNNING = "org/mule/tooling/netbeans/resources/badges/running-badge.png";  //NOI18N
    @StaticResource
    public static final String BADGE_SERVER = "org/mule/tooling/netbeans/resources/badges/server-badge.png";  //NOI18N
    @StaticResource
    public static final String BADGE_WAITING = "org/mule/tooling/netbeans/resources/badges/waiting-badge.png";  //NOI18N
    @StaticResource
    public static final String MULE_ICON = "org/mule/tooling/netbeans/resources/mule16.png";  //NOI18N
    @StaticResource
    public static final String JAR_ICON = "org/mule/tooling/netbeans/resources/jar.png"; // NOI18N
    @StaticResource
    public static final String SERVER_ICON = "org/mule/tooling/netbeans/resources/server2.png";  //NOI18N
    
    @StaticResource
    public static final String MULE_XML_TYPE_ICON = "org/mule/tooling/netbeans/resources/mule-type.png";
    @StaticResource
    public static final String CONF_TYPE_ICON = "org/mule/tooling/netbeans/resources/conf-type.png";
    @StaticResource
    private static final String DEBUG_ICON = "org/mule/tooling/netbeans/resources/debug.png";
    @StaticResource
    private static final String START_ICON = "org/mule/tooling/netbeans/resources/start.png";
    @StaticResource
    private static final String STOP_ICON = "org/mule/tooling/netbeans/resources/stop.png";
    //Default variables in NetBeans platform
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N ̰
    @StaticResource
    private static final String FOLDER_OPENED_ICON_PATH = "org/mule/tooling/netbeans/resources/defaultFolderOpen.gif";  //NOI18N
    @StaticResource
    private static final String FOLDER_ICON_PATH = "org/mule/tooling/netbeans/resources/defaultFolder.gif";  //NOI18N

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

    public static Image getTreeFolderIconWithBadge(boolean opened, String badge) {
        return ImageUtilities.mergeImages(IconUtil.getTreeFolderIcon(opened),
                ImageUtilities.loadImage(badge, true), //NOI18N
                8, 8);
    }

    public static Image getLibraryBadge() {
        return ImageUtilities.loadImage(BADGE_LIBRARIES);
    }

    public static Image getJarIcon() {
        return ImageUtilities.loadImage(JAR_ICON, true);
    }

    public static Image getMuleIcon() {
        return ImageUtilities.loadImage(MULE_ICON, true);
    }

    public static ImageIcon getStartIcon() {
        return ImageUtilities.loadImageIcon(START_ICON, true);
    }

    public static ImageIcon getStopIcon() {
        return ImageUtilities.loadImageIcon(STOP_ICON, true);
    }

    public static ImageIcon getDebugIcon() {
        return ImageUtilities.loadImageIcon(DEBUG_ICON, true);
    }

    public static Image getMuleServerIcon(Status status, boolean debug) {
        Image icon = ImageUtilities.mergeImages(ImageUtilities.loadImage(SERVER_ICON),
                ImageUtilities.loadImage(BADGE_MULE), //NOI18N
                8, 0);
        if (status.equals(Status.RUNNING)) {
            icon = ImageUtilities.mergeImages(icon,
                    ImageUtilities.loadImage(debug ? BADGE_DEBUGGING : BADGE_RUNNING), //NOI18N
                    9, 9);
        } else if (status.equals(Status.STARTING)) {
            icon = ImageUtilities.mergeImages(icon,
                    ImageUtilities.loadImage(BADGE_WAITING), //NOI18N
                    9, 9);
        }
        return icon;
    }
}
