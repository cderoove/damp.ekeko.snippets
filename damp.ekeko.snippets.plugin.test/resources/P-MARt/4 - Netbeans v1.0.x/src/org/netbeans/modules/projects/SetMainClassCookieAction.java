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
import org.openide.cookies.ExecCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

/**
 *
 * @author  mryzl
 */

public class SetMainClassCookieAction extends NodeAction {

    /** Creates new SetMainClassCookieAction. */
    public SetMainClassCookieAction() {
    }

    /** Test whether the action should be enabled based on the currently activated nodes.
    *
    * @param activatedNodes - current activated nodes, may be empty but not null
    * @return true to be enabled, false to be disabled
    */  
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            if ((activatedNodes[0].getCookie(ExecCookie.class) != null) &&
                (activatedNodes[0].getCookie(ProjectDataObject.class) == null)) return true;
        }
        return false;
    }

    /**
    * Perform the action based on the currently activated nodes.
    * Note that if the source of the event triggering this action was itself
    * a node, that node will be the sole argument to this method, rather
    * than the activated nodes.
    *
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    */
    protected void performAction(Node[] activatedNodes) {
        org.openide.nodes.Node n = TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
        ProjectDataObject project = (ProjectDataObject)n.getCookie (ProjectDataObject.class);

        if (project != null) {
            if (activatedNodes.length > 0) {
                ExecCookie ec = (ExecCookie) activatedNodes[0].getCookie (ExecCookie.class);
                DataObject dobj = (DataObject) activatedNodes[0].getCookie (DataObject.class);
                if ((ec != null) && (dobj != null)) {
                    try {
                        if (!project.isAccessibleFromFolder(dobj) && MainClassHelper.canAddToProject(dobj.getName())) {
                            project.add (dobj);
                        }
                        MainClassHelper.setMainClass(project, dobj);
                    } catch (java.io.IOException ex) {
                        TopManager.getDefault ().notifyException (ex);
                    }
                }
            }
        }
    }

    /** Get a human presentable name of the action.
    * This may be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle(SetMainClassCookieAction.class).getString("ACT_SetMainClassCA");
    }

    /** Get a help context for the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(SetMainClassCookieAction.class);
    }
}

/*
* Log
*  1    Gandalf   1.0         1/12/00  Martin Ryzl     
* $ 
*/ 
