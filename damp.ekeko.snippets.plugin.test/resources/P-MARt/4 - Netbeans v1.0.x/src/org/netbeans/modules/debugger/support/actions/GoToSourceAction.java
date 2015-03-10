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
* GoToSourceAction action.
*
* @author   Jan Jancura
*/
public class GoToSourceAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8487176709444303658L;

    /** Actually performs the SwitchOn action.
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        GoToSourceCookie sc = (GoToSourceCookie) activatedNodes [0].getCookie (
                                  GoToSourceCookie.class
                              );
        sc.goToSource ();
    }

    /**
    * @param activatedNodes Currently activated nodes.
    */
    protected boolean enable (final Node[] activatedNodes) {
        if (!super.enable (activatedNodes)) return false;
        GoToSourceCookie sc = (GoToSourceCookie) activatedNodes [0].getCookie (GoToSourceCookie.class);
        return sc.canGoToSource ();
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
                   GoToSourceCookie.class
               };
    }

    /** @return the action's icon */
    public String getName () {
        return NbBundle.getBundle (GoToSourceAction.class).getString ("CTL_GoToSource");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (GoToSourceAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        // [PENDING] this resource does not exist, and anyway referring to OpenIDE resources externally
        // is impermissible since the location of resources is not documented
        return "/org/openide/resources/actions/goToSource.gif"; // NOI18N
    }
}

/*
 * Log
 *  9    Gandalf   1.8         1/13/00  Daniel Prusa    NOI18N
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/28/99  Jan Jancura     
 *  6    Gandalf   1.5         8/2/99   Jan Jancura     A lot of bugs...
 *  5    Gandalf   1.4         7/2/99   Jan Jancura     
 *  4    Gandalf   1.3         6/25/99  Ian Formanek    Fixed HelpCtx
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
