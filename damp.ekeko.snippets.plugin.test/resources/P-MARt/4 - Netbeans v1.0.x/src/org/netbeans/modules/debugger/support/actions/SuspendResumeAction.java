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


/** SuspendResumeAction action.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class SuspendResumeAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 287995876365009779L;

    // static ..........................................................................................

    static boolean suspend;

    // init ..........................................................................................

    /**
    * @param activatedNodes Currently activated nodes.
    */
    protected void performAction (final Node[] activatedNodes) {
        int i, k = activatedNodes.length;
        for (i = 0; i < k; i ++)
            ((SuspendCookie) activatedNodes [i].getCookie (SuspendCookie.class)).setSuspended (suspend);
    }

    /**
    * @param activatedNodes Currently activated nodes.
    */
    protected boolean enable (final Node[] activatedNodes) {
        if (activatedNodes == null) return false;
        int i, k = activatedNodes.length;
        if (k < 1) return false;
        SuspendCookie sc = (SuspendCookie) activatedNodes [0].getCookie (SuspendCookie.class);
        if (sc == null) return false;
        suspend = !sc.isSuspended ();
        for (i = 1; i < k; i++)
            if ( (null == (sc = (SuspendCookie) activatedNodes [i].getCookie (SuspendCookie.class))) ||
                    (suspend == sc.isSuspended ())
               ) return false;
        return true;
    }

    /**
    * Returns MODE_ALL.
    */
    protected int mode () {
        return MODE_ALL;
    }

    /**
    * Returns SuspendCookie
    */
    protected Class[] cookieClasses () {
        return new Class [] {
                   SuspendCookie.class
               };
    }

    /** @return the action's name */
    public String getName () {
        return suspend == true
               ? NbBundle.getBundle(SuspendAction.class).getString ("CTL_Suspend")
               : NbBundle.getBundle(SuspendAction.class).getString ("CTL_Resume");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (SuspendResumeAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/suspend.gif"; // NOI18N
    }

    /**
    * Returns menu item which representates this submenu.
    *
    * @return <CODE>JMenuItem</CODE> Submenu representated with this context.
    */
    public javax.swing.JMenuItem getMenuPresenter () {
        setEnabled (enable (getActivatedNodes ()));
        return super.getMenuPresenter ();
    }
}

/*
* Log
*/
