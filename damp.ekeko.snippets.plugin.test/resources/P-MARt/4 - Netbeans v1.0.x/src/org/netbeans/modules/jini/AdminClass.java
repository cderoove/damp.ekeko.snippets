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

/**
 * Narrows acceptable classes to Admins only.  
 *
 * @author  Petr Kuzel
 * @version 
 */
public class AdminClass {

    Class admin;

    /** Creates new AdminClass */
    public AdminClass(Class admin) {
        if ( ! Admin.class.isAssignableFrom(admin) ) throw new IllegalArgumentException();
        this.admin = admin;
    }

    public Admin newInstance() {
        if (admin==null) return null;
        try {
            return (Admin) admin.newInstance();
        } catch (InstantiationException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        }
    }

}


/*
* <<Log>>
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

