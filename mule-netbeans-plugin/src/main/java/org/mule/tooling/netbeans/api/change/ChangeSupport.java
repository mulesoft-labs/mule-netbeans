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
package org.mule.tooling.netbeans.api.change;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;

/**
 *
 * @author Facundo Lopez Kaufmann
 * @see org.openide.util.ChangeSupport
 */
public class ChangeSupport {

    protected final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    public void addChangeListener(ChangeListener changeListener) {
        if (changeListener == null) {
            return;
        }
        listeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        if (changeListener == null) {
            return;
        }
        listeners.remove(changeListener);
    }

    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    public void fireChange(Object source, String attributeName, Object value) {
        fireChange(new AttributeChangeEvent(source, attributeName, value));
    }

    public void fireChange(Object source) {
        fireChange(new ChangeEvent(source));
    }

    protected void fireChange(ChangeEvent event) {
        if (listeners.isEmpty()) {
            return;
        }
        for (ChangeListener listener : listeners) {
            try {
                listener.stateChanged(event);
            } catch (RuntimeException re) {
                Exceptions.printStackTrace(re);
            }
        }
    }
}
