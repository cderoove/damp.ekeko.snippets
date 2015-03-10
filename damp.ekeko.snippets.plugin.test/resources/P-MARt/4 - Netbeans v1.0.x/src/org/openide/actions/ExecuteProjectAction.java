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

package org.openide.actions;

import org.openide.TopManager;
import org.openide.cookies.ExecCookie;
import org.openide.nodes.Node;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.ProjectSensitiveAction;

/** Executes the current project if it supports execution.
*
* @author   Ian Formanek
*/
public class ExecuteProjectAction extends ProjectSensitiveAction {
    static final long serialVersionUID =-7908144941318263878L;
    /** generated Serialized Version UID */
    //static final long serialVersionUID = -3365766607488094613L;

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(ExecuteProjectAction.class).getString("ExecuteProject");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (ExecuteProjectAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/executeProject.gif"; // NOI18N
    }

    public void performAction(final Node projectDesktop) {
        new ExecuteAction() {
            public void performAction(Node[] nodes) {
                super.performAction(nodes);
            }
        }.performAction(new Node[] { projectDesktop });
    }

    /** Test whether the action is currently enabled.
    * @return <code>true</code> if so
    */
    public boolean enable (Node projectDesktop) {
        return projectDesktop.getCookie (ExecCookie.class) != null;
    }
}

/*
 * Log
 *  7    Gandalf   1.6         1/17/00  Martin Ryzl     delegates to the 
 *       ExecuteAction
 *  6    Gandalf   1.5         1/12/00  Ian Formanek    NOI18N
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  3    Gandalf   1.2         8/1/99   Jaroslav Tulach ProjectSensitiveAction
 *  2    Gandalf   1.1         7/25/99  Ian Formanek    Non-abstract now
 *  1    Gandalf   1.0         7/13/99  Ian Formanek    
 * $
 */
