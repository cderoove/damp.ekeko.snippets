/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.openide.options;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;

import java.util.*;

/** Extends the functionality of <CODE>SystemOption</CODE>
* by providing support for veto listeners.
*
* @author Jaroslav Tulach
* @version 0.11 Dec 6, 1997
*/
public abstract class VetoSystemOption extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -614731095908156413L;

    /** vetoable listener property */
    private static final String PROP_VETO_SUPPORT = "vetoSupport"; // NOI18N

    /** Default constructor. */
    public VetoSystemOption() {}

    /** Lazy getter for veto hashtable.
    * @return the hashtable
    */
    private HashSet getVeto () {
        HashSet set = (HashSet)getProperty (PROP_VETO_SUPPORT);
        if (set == null) {
            set = new HashSet ();
            putProperty (PROP_VETO_SUPPORT, set);
        }
        return set;
    }

    /** Add a new veto listener to all instances of this exact class.
    * @param list the listener to add
    */
    public final void addVetoableChangeListener (VetoableChangeListener list) {
        synchronized (getLock ()) {
            getVeto ().add (list);
        }
    }

    /** Remove a veto listener from all instances of this exact class.
    * @param list the listener to remove
    */
    public final void removeVetoableChangeListener (VetoableChangeListener list) {
        synchronized (getLock ()) {
            getVeto ().remove (list);
        }
    }

    /** Fire a property change event.
    * @param name the name of the property
    * @param oldValue the old value
    * @param newValue the new value
    * @exception PropertyVetoException if the change is vetoed
    */
    public final void fireVetoableChange (
        String name, Object oldValue, Object newValue
    ) throws PropertyVetoException {
        PropertyChangeEvent ev = new PropertyChangeEvent (
                                     this, name, oldValue, newValue
                                 );

        Iterator en;
        synchronized (getLock ()) {
            en = ((HashSet)getVeto ().clone ()).iterator ();
        }

        while (en.hasNext ()) {
            ((VetoableChangeListener)en.next ()).vetoableChange (ev);
        }
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/12/00  Ian Formanek    NOI18N
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/22/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
