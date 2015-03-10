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


/** Action which switches to previous Workspace
*
* @author Ales Novak
*/
public final class PreviousWorkspaceAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2767323146043177917L;
    /** name */
    private static String name;

    /** performs action */
    public void performAction() {
        ((WindowManagerImpl)TopManager.getDefault().getWindowManager()).
        previousWorkspace();
    }

    /** help for the action */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (PreviousWorkspaceAction.class);
    }

    /** name of the action */
    public String getName() {
        if (name == null)
            name = NbBundle.getBundle(PreviousWorkspaceAction.class).getString("PreviousWorkspace");
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
        return "/org/netbeans/core/resources/actions/previousWorkspace.gif"; // NOI18N
    }

}

/*
 * Log
 */
