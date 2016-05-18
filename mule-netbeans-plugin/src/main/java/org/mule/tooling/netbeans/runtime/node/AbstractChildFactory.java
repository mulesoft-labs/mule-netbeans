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
package org.mule.tooling.netbeans.runtime.node;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mule.tooling.netbeans.api.change.ChangeSource;
import org.openide.nodes.ChildFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public abstract class AbstractChildFactory<T> extends ChildFactory.Detachable<T> implements ChangeListener {

    protected Lookup lookup;

    protected AbstractChildFactory(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    protected void addNotify() {
        ChangeSource changeSource = lookup.lookup(ChangeSource.class);
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Registering to change source {0}", new Object[]{changeSource});
        if(changeSource != null) {
            changeSource.addChangeListener(this);
        }
    }

    @Override
    protected void removeNotify() {
        ChangeSource changeSource = lookup.lookup(ChangeSource.class);
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Unregistering from change source {0}", new Object[]{changeSource});
        if(changeSource != null) {
            changeSource.removeChangeListener(this);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refresh(false);
    }
}
