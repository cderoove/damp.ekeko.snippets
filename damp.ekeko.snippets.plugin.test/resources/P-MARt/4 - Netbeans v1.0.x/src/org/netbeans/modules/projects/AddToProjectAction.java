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

package org.netbeans.modules.projects;

import org.openide.TopManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;

/** AddToProject action.
*
* @author   Ian Formanek
*/
public class AddToProjectAction extends NodeAction {
    static final long serialVersionUID =-6471281373153172312L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = -5280204757097896304L;

    /** Enabled only if the current project is ProjectDataObject.
    */
    public boolean enable (Node[] arr) {
        org.openide.nodes.Node n = TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
        if (n.getCookie (ProjectDataObject.class) == null) {
            return false;
        };

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getCookie (DataObject.class) == null) {
                return false;
            }
            // exclude project node
            if (arr[i].getCookie (ProjectDataObject.class) != null) {
                return false;
            }
        }

        return true;
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle (AddToProjectAction.class).getString ("ACT_AddToProject");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AddToProjectAction.class);
    }

    /** Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /**
    * Standard perform action extended by actually activated nodes.
    *
    * @param activatedNodes gives array of actually activated nodes.
    */
    protected void performAction (Node[] activatedNodes) {
        org.openide.nodes.Node n = TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
        ProjectDataObject project = (ProjectDataObject)n.getCookie (ProjectDataObject.class);

        for (int i = 0; i < activatedNodes.length; i++) {
            DataObject obj = (DataObject)activatedNodes[i].getCookie (DataObject.class);
            if (obj != null) {
                try {
                    project.add (obj);
                } catch (java.io.IOException ex) {
                    TopManager.getDefault ().notifyException (ex);
                }
            }
        }
    }

}

/*
 * Log
 *  2    Gandalf   1.1         1/13/00  Martin Ryzl     heavy localization
 *  1    Gandalf   1.0         1/9/00   Martin Ryzl     
 * $
 */
