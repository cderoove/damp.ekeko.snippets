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

package org.netbeans.modules.jini.admins;

import java.util.*;
import java.rmi.*;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import org.netbeans.modules.jini.*;

import com.sun.jini.admin.*;

/**
 * One well-known admin is DestroyAdmin.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class DestroyAdminModel extends AbstractAdmin {


    /** Creates new DestroyAdmin */
    public DestroyAdminModel() {
    }

    /** Do remote call may spend a lot of time
    */
    void destroy() {

        // TODO confirmation

        Vector undestroyed = new Vector();

        Enumeration ads = enum();
        while (ads.hasMoreElements()) {

            com.sun.jini.admin.DestroyAdmin admin =
                (com.sun.jini.admin.DestroyAdmin) ads.nextElement();
            if (admin != null)
                try {
                    admin.destroy();
                } catch (RemoteException ex) {
                    System.err.println("Can not destroy: " + admin);
                    undestroyed.add(".");
                }
        }

        if (undestroyed.size() > 0) {
            // TODO notify undestroyed
        }

        fireCloseAll();
    }

    public String getName() {
        return "Destroy...";
    }


    /** Test whether all added object are administrable.
     * Semantics can not be mixed with canBatch() semantics.
     * @return true if all added objects are administrable by this admin
     */
    public boolean enabled() {
        Enumeration ads = enum();

        while (ads.hasMoreElements()) {
            Object next = ads.nextElement();
            //      System.err.println("Destroy? " + next);

            if (! (next instanceof com.sun.jini.admin.DestroyAdmin) ) return false;
        }
        //    System.err.println("acknowledged");
        return true;
    }

    /**
     * Not batchable() but enabled() Admin can provide write-only interface.
     * @param Admin.RW, Admin.WO, .....
     * @return JComponent or null if not available.
     */
    public javax.swing.JComponent getUI(Object type) {
        return new DestroyAdminView(this);
    }

    /**
     * Determine if this Admin can admin more services. e.g. DestroyAdmin
     * The problem is particularly with get properties. These may
     * differ accross admined objects and therefore undisplayable.
     * @return true if more administred object can share this admin.
     * (i.e. if only one were added then return true)
     */
    public boolean canBatch() {
        return true;
    }
}


/*
* <<Log>>
*  1    Gandalf   1.0         2/3/00   Petr Kuzel      
* $ 
*/ 

