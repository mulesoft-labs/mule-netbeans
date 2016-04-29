/*
 * Copyright 2016 Facundo Lopez Kaufmann.
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
package org.mule.tooling.netbeans.api.runtime;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mule.tooling.netbeans.api.MuleRuntime;
import org.mule.tooling.netbeans.api.change.AttributeChangeEvent;
import org.mule.tooling.netbeans.common.IconUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Support tools for NetBeans Platform functionality.
 * 
 * @author Facundo Lopez Kaufmann
 */
public class NBPSupport {
    
    public static InputOutput getInputOutput(final MuleRuntime runtime) {
        Action[] actions = new Action[] {
            new StartAction(runtime),
            new DebugAction(runtime),
            new StopAction(runtime)
        };
        InputOutput io = IOProvider.getDefault().getIO(runtime.getName(), actions);
        return io;
    }
    
    private static abstract class AbstractRuntimeAction extends AbstractAction implements ChangeListener {
        protected final MuleRuntime runtime;

        public AbstractRuntimeAction(MuleRuntime runtime, String name, Icon icon) {
            super(name, icon);
            this.runtime = runtime;
            this.runtime.addChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            System.out.println(this + " " + e);
            if(!runtime.isRegistered()) {
                runtime.removeChangeListener(this);
            }
            if (e instanceof AttributeChangeEvent && ((AttributeChangeEvent) e).getAttributeName().equals("status")) {
                updateEnabled();
            }
        }
        
        protected void updateEnabled() {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    firePropertyChange("enabled", null, isEnabled() ? Boolean.TRUE : Boolean.FALSE);
                }
            });
        }
    }
    
    @Messages({
        "NBPSupport_StartAction_name=Start"
    })
    private static class StartAction extends AbstractRuntimeAction {

        public StartAction(MuleRuntime runtime) {
            super(runtime, Bundle.NBPSupport_StartAction_name(), IconUtil.getStartIcon());
        }

        @Override
        public boolean isEnabled() {
            return runtime.canStart();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            runtime.start(false);
        }
    }
    
    @Messages({
        "NBPSupport_DebugAction_name=Start in debug mode"
    })
    private static class DebugAction extends AbstractRuntimeAction {

        public DebugAction(MuleRuntime runtime) {
            super(runtime, Bundle.NBPSupport_StartAction_name(), IconUtil.getDebugIcon());
        }

        @Override
        public boolean isEnabled() {
            return runtime.canStart();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            runtime.start(true);
        }
    }
    
    @Messages({
        "NBPSupport_StopAction_name=Stop"
    })
    private static class StopAction extends AbstractRuntimeAction {

        public StopAction(MuleRuntime runtime) {
            super(runtime, Bundle.NBPSupport_StopAction_name(), IconUtil.getStopIcon());
        }

        @Override
        public boolean isEnabled() {
            return runtime.canStop();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            runtime.stop(false);
        }
    }
}
