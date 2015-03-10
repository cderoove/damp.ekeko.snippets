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
* Resumes currently selected objects.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class ResumeDebuggerAction extends CallbackSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 287995876365009779L;

    /** @return the action's name */
    public String getName () {
        return NbBundle.getBundle (ResumeDebuggerAction.class).getString ("CTL_ResumeAll");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ResumeDebuggerAction.class);
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
