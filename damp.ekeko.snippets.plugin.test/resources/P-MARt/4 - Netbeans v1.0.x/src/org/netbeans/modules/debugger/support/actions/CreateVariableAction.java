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
import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerNotFoundException;


/**
* CreateVariableAction on action.
*
* @author Jan Jancura
*/
public class CreateVariableAction extends NodeAction {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7764852450085036874L;

    private boolean enabled = true;

    /** @return the action's name */
    public String getName () {
        return NbBundle.getBundle (CreateVariableAction.class).getString ("CTL_CreateVariable");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (CreateVariableAction.class);
    }

    /** Resource name for the icon.
    * @return resource name
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/actions/switchOn.gif"; // NOI18N
    }

    public void changeEnabled (boolean e) {
        enabled = e;
        setEnabled (enable (getActivatedNodes ()));
    }

    protected boolean enable (Node[] activatedNodes) {
        if (!enabled) {
            return false;
        }
        if ((activatedNodes == null)||(activatedNodes.length != 1)) {
            return false;
        }
        if (activatedNodes[0].getCookie (CreateVariableCookie.class) == null) {
            return false;
        }
        try {
            if (TopManager.getDefault ().getDebugger ().getState () == Debugger.DEBUGGER_NOT_RUNNING)
                return false;
        } catch (DebuggerNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
    * @param activatedNodes Currently activated nodes.
    */
    protected void performAction (final Node[] activatedNodes) {
        ((CreateVariableCookie) activatedNodes [0].getCookie (CreateVariableCookie.class)).createVariable ();
    }

}

/*
 * Log
 */
