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

import javax.swing.event.ChangeListener;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
public abstract class AbstractChangeSource implements ChangeSource {

    protected ChangeSupport changeSupport;

    protected AbstractChangeSource() {
        this.changeSupport = new ChangeSupport();
    }

    protected AbstractChangeSource(ChangeSupport changeSupport) {
        this.changeSupport = changeSupport;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
    protected void fireChange(String changeName, Object value) {
        changeSupport.fireChange(this, changeName, value);
    }
    
    protected void fireChange() {
        changeSupport.fireChange(this);
    }
}
