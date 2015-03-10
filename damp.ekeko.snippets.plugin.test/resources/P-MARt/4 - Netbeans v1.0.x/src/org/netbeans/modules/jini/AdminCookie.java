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
 *
 * @author  pkuzel
 * @version 
 */
public class AdminCookie implements org.openide.nodes.Node.Cookie {

    private net.jini.admin.Administrable admin;

    /** Creates new AdminCookie */
    public AdminCookie(net.jini.admin.Administrable admin) {
        this.admin = admin;
    }

    public net.jini.admin.Administrable getAdmin() {
        return admin;
    }

}


/*
* <<Log>>
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

