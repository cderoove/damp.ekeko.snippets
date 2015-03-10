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

import java.util.ResourceBundle;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;

/**
* CreateVariableAccessBreakpointAction action.
*
* @author   Jan Jancura
*/
public class CreateVariableAccessBreakpointAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8487176709442343658L;

    /** Actually performs the SwitchOn action.
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        /*    GoToSourceCookie sc = (GoToSourceCookie) activatedNodes [0].getCookie (
              GoToSourceCookie.class
            );
            sc.goToSource ();*/
    }

    /**
    * @param activatedNodes Currently activated nodes.
    */
    protected boolean enable (final Node[] activatedNodes) {
        if (!super.enable (activatedNodes)) return false;
        GetVariableCookie sc = (GetVariableCookie) activatedNodes [0].getCookie (GetVariableCookie.class);
        return sc.getVariable () != null;
    }

    /**
    * Returns MODE_EXACTLY_ONE.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /**
    * Returns GoToSourceCookie
    */
    protected Class[] cookieClasses () {
        return new Class [] {
                   GetVariableCookie.class
               };
    }

    /** @return the action's icon */
    public String getName () {
        return NbBundle.getBundle (CreateVariableAccessBreakpointAction.class).getString ("CTL_CreateVariableAccessBreakpoint");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (CreateVariableAccessBreakpointAction.class);
    }
}

/*
* Log
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         9/28/99  Jan Jancura     
* $
*/
