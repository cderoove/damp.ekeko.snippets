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

package org.netbeans.core.actions;

import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.netbeans.core.windows.WindowManagerImpl;

/** Action which switches to next Workspace
*
* @author Ales Novak
*/
public final class NextWorkspaceAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5222855932489007529L;
    /** name */
    private static String name;
    /** reference to Topmanager */
    private static TopManager manager;

    /** performs action */
    public void performAction() {
        ((WindowManagerImpl)TopManager.getDefault().getWindowManager()).
        nextWorkspace();
    }

    /** help for the action */
    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx (NextWorkspaceAction.class);
    }

    /** name of the action */
    public String getName() {
        if (name == null)
            name = NbBundle.getBundle(NextWorkspaceAction.class).getString("NextWorkspace");
        return name;
    }

    /** always enabled */
    public boolean isEnabled() {
        return true;
    }

    /** Resource string to the icon.
    * @return resource string of actions' icon
    */
    protected String iconResource() {
        return "/org/netbeans/core/resources/actions/nextWorkspace.gif"; // NOI18N
    }

}

/*
 * Log
 */
