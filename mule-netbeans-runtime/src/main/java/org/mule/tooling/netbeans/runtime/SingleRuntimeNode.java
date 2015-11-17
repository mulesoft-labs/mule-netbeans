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

import java.io.IOException;
import javax.swing.Action;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.MuleSupport;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public class SingleRuntimeNode extends AbstractNode {
    static final String BADGE = "org/mule/tooling/netbeans/runtime/resources/mule16.png"; // NOI18N

    private MuleRuntime muleRuntime;
    public SingleRuntimeNode(MuleRuntime muleRuntime) {
        super(Children.LEAF);
        this.muleRuntime = muleRuntime;
        setDisplayName(muleRuntime.getInformation().getName());
        setShortDescription(muleRuntime.getVersion().toString());
        setIconBaseWithExtension(BADGE);
    }

    @Override
    public void destroy() throws IOException {
        MuleSupport.getStore().remove(muleRuntime.getInformation());
        super.destroy();
    }

    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            DeleteAction.get(DeleteAction.class),
        };
    }
}
