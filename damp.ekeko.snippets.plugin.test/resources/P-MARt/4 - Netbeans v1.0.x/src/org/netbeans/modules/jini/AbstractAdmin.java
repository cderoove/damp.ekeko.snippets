/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jini;

import java.util.*;
import java.beans.*;

import net.jini.admin.Administrable;

/**
 *
 * @author  pkuzel
 * @version 
 */
public abstract class AbstractAdmin implements Admin {

    protected HashSet administrables;
    protected PropertyChangeSupport listeners;

    /** Creates new AbstrastAdmin */
    public AbstractAdmin() {
        administrables = new HashSet();
        listeners = new PropertyChangeSupport(this);
    }

    /**
     * Add administrable object to new construct admin object.
     * @param objs are object to administrate. (null must be treated)
     */
    public void addAdministrables(Administrable[] objs) {
        if (objs == null) return;
        administrables.addAll(Arrays.asList(objs));
        // if classcastexception is returned no
        // service exist any more
    }

    protected Enumeration enum() {
        return Collections.enumeration(administrables);
    }

    /** Test whether all added object are administrable.
     * Semantics can not be mixed with canBatch() semantics.
     * @return true if all added objects are administrable by this admin
     */
    public abstract boolean enabled();

    /**
     * Not batchable() but enabled() Admin can provide write-only interface.
     * @param Admin.RW, Admin.WO, .....
     * @return JComponent or null if not available.
     */
    public abstract javax.swing.JComponent getUI(Object type);

    /**
     * Determine if this Admin can admin more services. e.g. DestroyAdmin
     * The problem is particularly with get properties. These may
     * differ accross admined objects and therefore undisplayable.
     * @return true if more administred object can share this admin.
     * (i.e. if only one were added then return true)
     */
    public abstract boolean canBatch();

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.removePropertyChangeListener(l);
    }

    protected void fireCloseAll() {
        listeners.firePropertyChange(EVENT_CLOSE_ALL, null, null);
    }

}


/*
* <<Log>>
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

