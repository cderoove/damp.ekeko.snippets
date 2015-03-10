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
import org.openide.util.actions.CallbackSystemAction;
import org.openide.nodes.Node;

/**
* Suspends currently selected objects.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class SuspendDebuggerAction extends CallbackSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 287995876365009779L;

    /** @return the action's name */
    public String getName () {
        return NbBundle.getBundle (SuspendDebuggerAction.class).getString ("CTL_SuspendAll");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (SuspendDebuggerAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/debugger/resources/suspend.gif"; // NOI18N
    }
}

/*
* Log
*/
