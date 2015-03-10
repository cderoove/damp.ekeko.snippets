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

import java.io.IOException;
import java.util.*;

import org.openide.TopManager;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import org.netbeans.modules.projects.settings.*;

/**
 *
 * @author  mryzl
 */

public class AddNewAction extends NodeAction {

    /** Creates new AddNewAction. */
    public AddNewAction() {
    }


    private static TemplateWizard wizard = null;
    static TemplateWizard getWizard() {
        if (wizard == null) {
            wizard = new TemplateWizard();
        }
        return wizard;
    }

    /**
    * Perform the action based on the currently activated nodes.
    * Note that if the source of the event triggering this action was itself
    * a node, that node will be the sole argument to this method, rather
    * than the activated nodes.
    *
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    */
    protected boolean enable(Node[] activatedNodes) {
        return true;
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
        try {

            ProjectDataLoader.listener.enabled = false;

            // Allways add to the project.
            Set set = getWizard ().instantiate ();

            // [WARNING! when the wizard ends, events about newly created objects are
            // fired and they are waiting in the event queue.
            // Files should be added to the project before this method is left ...
            if (set != null) {
                Iterator it = set.iterator();
                while (it.hasNext()) {
                    DataObject dobj = (DataObject) it.next();
                    ProjectDataObject pdo = (ProjectDataObject) TopManager.getDefault().getPlaces().nodes().projectDesktop().getCookie(ProjectDataObject.class);
                    if (pdo != null) {
                        if (!pdo.isAccessibleFromFolder(dobj)) {
                            pdo.add(dobj);
                        }
                    }
                }
            }
            ProjectDataLoader.listener.enabled = true;

        } catch (IOException ex) {
            TopManager.getDefault ().notifyException (ex);
        }
    }

    /** Get a human presentable name of the action.
    * This may be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle(AddNewAction.class).getString("CTL_AddNewAction");
    }

    /** Get a help context for the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(AddNewAction.class);
    }
}

/*
* Log
*  2    Gandalf   1.1         2/4/00   Martin Ryzl     
*  1    Gandalf   1.0         1/17/00  Martin Ryzl     
* $ 
*/ 
