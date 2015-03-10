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

package org.netbeans.core;

import java.beans.*;

import org.openide.options.*;

/**
* Our implementation of options pool that allows listening on changes of
* number of options.
*
* @author Jaroslav Tulach
*/
final class NbControlPanel extends ControlPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -851381803002021197L;

    /** default */
    private static NbControlPanel controlPanel;

    // support to use
    private PropertyChangeSupport supp = new PropertyChangeSupport (this);



    /** Singleton.
    */
    private NbControlPanel () {
    }

    /** Default instanace.
    * @return default instance
    */
    public static NbControlPanel getDefault () {
        if (controlPanel == null) {
            controlPanel = new NbControlPanel ();
        }
        return controlPanel;
    }


    /** Adds new option to the pool.
    * @param so the option
    * @return true if the object was added (was not there before)
    */
    public boolean add (SystemOption so) {
        if (super.add (so)) {
            supp.firePropertyChange ("added", null, so); // NOI18N
            return true;
        }
        return false;
    }

    /** Removes option.
    * @param so option to remove
    * @return true if the option has been realy removed (was there before)
    */
    public boolean remove (SystemOption so) {
        if (super.remove (so)) {
            supp.firePropertyChange ("removed", so, null); // NOI18N
            return true;
        }
        return false;
    }

    /** Adds a listener. This is not real property change listener!!!!!!
    * Used internally for comunication with OptionsNode.
    */
    void addPropertyChangeListener (PropertyChangeListener l) {
        supp.addPropertyChangeListener (l);
    }

    /** Removes a listener.
    */
    void removePropertyChangeListener (PropertyChangeListener l) {
        supp.removePropertyChangeListener (l);
    }
}

/*
* Log
*  4    Gandalf   1.3         1/13/00  Jaroslav Tulach I18N
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
