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
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.ProjectSensitiveAction;

/** Saves the currently opened project.
*
* @author   Jaroslav Tulach
*/
public class SaveProjectAction extends ProjectSensitiveAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 862817653968029273L;

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(SaveProjectAction.class).getString("SaveProject");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (SaveProjectAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/saveProject.gif"; // NOI18N
    }

    /** Performs the action
    */
    public void performAction(Node projectDesktop) {
        SaveCookie sc = (SaveCookie)projectDesktop.getCookie (SaveCookie.class);
        try {
            if (sc != null) {
                sc.save ();
            }
        } catch (java.io.IOException e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    /** Enabled if the project desktop supports save cookie
    */
    public boolean enable (Node projectDesktop) {
        return (projectDesktop.getCookie (SaveCookie.class) != null);
    }
}

/*
 * Log
 *  9    Gandalf   1.8         1/12/00  Ian Formanek    NOI18N
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/3/99   Jaroslav Tulach Project settings node.
 *  6    Gandalf   1.5         8/1/99   Ian Formanek    Fixed to compile
 *  5    Gandalf   1.4         8/1/99   Jaroslav Tulach ProjectSensitiveAction
 *  4    Gandalf   1.3         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/30/99  Ian Formanek    
 * $
 */
