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

import net.jini.admin.Administrable;
import java.beans.*;

/**
 * Interface for plug-in Admins of administrable objects.
 *
 * @author  Petr Kuzel
 * @version 
 */
public interface Admin {

    /** Indicates that UI should be read/write. */
    public final String RW = "RW";

    /** Indicates that UI should be write only. */
    public final String WO = "WO";

    /**
    * Add administrable object to new construct admin object.
    * @param objs are object to administrate. (null must be treated)
    */  
    public void addAdministrables(Administrable[] objs);

    /**
    * Test whether all added object are administrable.
    * Semantics can not be mixed with canBatch() semantics.
    * @return true if all added objects are administrable by this admin 
    */
    public boolean enabled();

    /**
    * Not batchable() but enabled() Admin can provide write-only interface.  
    * @param Admin.RW, Admin.WO, .....
    * @return JComponent or null if not available.
    */
    public javax.swing.JComponent getUI(Object type);

    /**
    * Determine if this Admin can admin more services. e.g. DestroyAdmin
    * The problem is particularly with get properties. These may
    * differ accross admined objects and therefore undisplayable.
    * @return true if more administred object can share this admin. 
    * (i.e. if only one were added then return true)
    */
    public boolean canBatch();

    /**
    * If fired presenter of all admins should be closed.
    * Should be fired only by destroy type of admins.
    */
    public final String EVENT_CLOSE_ALL = "closeAll()";

    public void addPropertyChangeListener(PropertyChangeListener l);

    public void removePropertyChangeListener(PropertyChangeListener l);

}


/*
* <<Log>>
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

