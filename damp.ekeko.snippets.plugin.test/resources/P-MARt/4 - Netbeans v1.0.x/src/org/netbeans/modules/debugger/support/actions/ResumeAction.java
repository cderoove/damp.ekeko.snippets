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
* Resumes currently selected objects.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class ResumeAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 287995896325009779L;

    /**
    * @param activatedNodes Currently activated nodes.
    */
    protected void performAction (final Node[] activatedNodes) {
        int i, k = activatedNodes.length;
        for (i = 0; i < k; i ++)
            ((SuspendCookie) activatedNodes [i].getCookie (SuspendCookie.class)).setSuspended (false);
    }

    /**
    * @param activatedNodes Currently activated nodes.
    */
    /*  protected boolean enable (final Node[] activatedNodes) {
        if (activatedNodes == null) return false;
        int i, k = activatedNodes.length;
        for (i = 0; i < k; i++)
          if (activatedNodes [i].getCookie (SuspendCookie.class) == null
          ) return false;
        return true;
      }*/

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
        return NbBundle.getBundle (ResumeAction.class).getString ("CTL_Resume");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ResumeAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/debugger/resources/resume.gif"; // NOI18N
    }
}

/*
* Log
*/
