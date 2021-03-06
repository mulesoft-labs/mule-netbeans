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
package org.mule.tooling.netbeans.ui.runtime.node;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@Messages({
    "TerminateRuntimeAction_name=Forced Stop"
})
public class TerminateRuntimeAction extends NodeAction {

    public TerminateRuntimeAction() {
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            node.getLookup().lookup(RuntimeCookie.class).stop(true);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            RuntimeCookie cookie = node.getLookup().lookup(RuntimeCookie.class);
            if(cookie == null) {
                return false;
            }
            if (!cookie.isRunning() || !cookie.canStop()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return Bundle.TerminateRuntimeAction_name();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
