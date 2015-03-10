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

package org.netbeans.modules.debugger.support.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;


/**
* Deletes all items in the current item.
* This class is final only for performance reasons, can be
* happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class DeleteAllAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7764853975085036874L;

    /** Actually performs the SwitchOn action.
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        DeleteAllCookie dac = (DeleteAllCookie) activatedNodes [0].getCookie (DeleteAllCookie.class);
        dac.deleteAll ();
    }

    /**
    * Returns MODE_EXACTLY_ONE.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /**
    * Returns ThreadCookie
    */
    protected Class[] cookieClasses () {
        return new Class [] {
                   DeleteAllCookie.class
               };
    }

    /** @return the action's name */
    public String getName () {
        return NbBundle.getBundle (DeleteAllAction.class).getString ("CTL_DeleteAll");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (DeleteAllAction.class);
    }
}

/*
* Log
*/
