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
import org.openide.cookies.CompilerCookie;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.ProjectSensitiveAction;

/** Builds the current project if it supports builing.
*
* @author   Ian Formanek
*/
public class BuildProjectAction extends ProjectSensitiveAction {
    static final long serialVersionUID =-4069461654908775643L;
    /** generated Serialized Version UID */
    //static final long serialVersionUID = -3365766607488094613L;

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(BuildProjectAction.class).getString("BuildProject");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (BuildProjectAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/buildProject.gif"; // NOI18N
    }

    protected void performAction(Node projectDesktop) {
        CompilerCookie cc = (CompilerCookie)projectDesktop.getCookie (
                                CompilerCookie.Build.class
                            );
        CompilerJob job = new CompilerJob (Compiler.DEPTH_INFINITE);

        cc.addToJob (job, Compiler.DEPTH_INFINITE);

        job.setDisplayName (projectDesktop.getDisplayName ());
        job.start ();
    }

    /** Test whether the action is currently enabled.
    * @return <code>true</code> if so
    */
    protected boolean enable (Node projectDesktop) {
        return projectDesktop.getCookie (CompilerCookie.Build.class) != null;
    }

}

/*
 * Log
 *  10   Gandalf   1.9         1/12/00  Ian Formanek    NOI18N
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/15/99 Jaroslav Tulach Really compiles (calls 
 *       addToJob).  
 *  7    Gandalf   1.6         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  6    Gandalf   1.5         8/1/99   Jaroslav Tulach ProjectSensitiveAction
 *  5    Gandalf   1.4         7/25/99  Ian Formanek    Non-abstract now
 *  4    Gandalf   1.3         7/13/99  Ian Formanek    Provided icon for this 
 *       action
 *  3    Gandalf   1.2         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/30/99  Ian Formanek    
 * $
 */
