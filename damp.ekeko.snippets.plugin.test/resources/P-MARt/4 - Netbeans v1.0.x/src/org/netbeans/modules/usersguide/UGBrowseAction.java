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

package org.netbeans.modules.usersguide;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public class UGBrowseAction extends CallableSystemAction {

    static final long serialVersionUID =3795404149112187590L;
    public String getName () {
        return NbBundle.getBundle (UGBrowseAction.class).getString ("LAB_browse_users_guide");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (UGBrowseAction.class);
    }

    protected String iconResource () {
        return "/org/netbeans/modules/usersguide/ugBrowse.gif"; // NOI18N
    }

    public void performAction () {
        TopManager.getDefault ().showHelp (new HelpCtx ("org.netbeans.modules.usersguide.HOMEID")); // NOI18N
    }

}

/*
 * Log
 *  2    Gandalf   1.1         1/13/00  Ian Formanek    NOI18N
 *  1    Gandalf   1.0         1/12/00  Patrick Keegan  
 * $
 */
