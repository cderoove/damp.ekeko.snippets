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
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.loaders.*;
import org.openide.src.ClassElement;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

/** Allows to set the main class of the project.
*
* @author Jaroslav Tulach
*/
public class SetMainClassAction extends NodeAction {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 862817653968029273L;

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle(SetMainClassAction.class).getString("ACT_SetMainClass");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (SetMainClassAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/netbeans/modules/projects/resources/setMainClass.gif"; // NOI18N
    }

    protected boolean enable (Node[] arr) {
        Node p = TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
        return p.getCookie (ProjectDataObject.class) != null;
    }

    protected void performAction (Node[] arr) {
        Node p = TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
        ProjectDataObject pdo = (ProjectDataObject) p.getCookie (ProjectDataObject.class);
        if (pdo != null) {
            try {
                MainClassHelper.setMainClassDialog(pdo);
            } catch (java.io.IOException ex) {
                TopManager.getDefault().notifyException(ex);
            }
        }
    }

}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Martin Ryzl     heavy localization
 *  4    Gandalf   1.3         1/12/00  Martin Ryzl     
 *  3    Gandalf   1.2         1/8/00   Martin Ryzl     
 *  2    Gandalf   1.1         1/8/00   Martin Ryzl       
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */
