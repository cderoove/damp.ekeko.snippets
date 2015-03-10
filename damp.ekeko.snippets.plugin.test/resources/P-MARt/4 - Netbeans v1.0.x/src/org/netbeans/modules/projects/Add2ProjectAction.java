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

import java.awt.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

/**
 *
 * @author  mryzl
 */

public class Add2ProjectAction extends NodeAction {

    /** Creates new Add2ProjectAction. */
    public Add2ProjectAction() {
    }

    /** Enabled only if the current project is ProjectDataObject.
     */
    public boolean enable (Node[] arr) {
        Node n = TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
        return n.getCookie(ProjectDataObject.class) != null;
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
        Node n = TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
        ProjectDataObject pdo = (ProjectDataObject) n.getCookie(ProjectDataObject.class);
        if (pdo != null) {
            try {
                DataObject dobjs[] = selectDataObjects();
                if (dobjs != null) {
                    for (int i = 0; i < dobjs.length; i++) {
                        dobjs[i].createShadow(pdo.getFileFolder());
                    }
                }
            } catch (java.io.IOException ex) {
                if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace ();   // NOI18N
            }
        }
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle (Add2ProjectAction.class).getString ("ACT_Add2Project");
    }

    /** Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/empty.gif";  // NOI18N
    }

    /** Get a help context for the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(Add2ProjectAction.class);
    }

    /** Select a Data Object from the repository.
    * @return DataObject or null
    */
    private DataObject[] selectDataObjects() {
        Node root = TopManager.getDefault().getPlaces().nodes().repository();
        ExplorerView view = new ExplorerView(root, null, NbBundle.getBundle(Add2ProjectAction.class).getString("CTL_SelectObjects"));

        ExplorerView.DialogAcceptor acc = new ExplorerView.DialogAcceptor();

        DialogDescriptor desc = new DialogDescriptor (
                                    view, view.getName(), true,
                                    DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                                    DialogDescriptor.BOTTOM_ALIGN, getHelpCtx(), acc
                                );

        Dialog dlg = TopManager.getDefault().createDialog(desc);
        dlg.setModal(true);
        dlg.show();

        if (!acc.ok) return null;

        Node[] nodes = view.getSelected();
        int i, k;
        for(i = 0, k = 0; i < nodes.length; i++) {
            if (nodes[i].getCookie(DataObject.class) != null) k++;
        }
        DataObject dobjs[] = new DataObject[k];
        for(i = 0, k = 0; i < nodes.length; i++) {
            if (nodes[i].getCookie(DataObject.class) != null) {
                dobjs[k++] = (DataObject)nodes[i].getCookie(DataObject.class);
            }
        }
        return dobjs;
    }
}

/*
* Log
*  3    Gandalf   1.2         1/19/00  Martin Ryzl     
*  2    Gandalf   1.1         1/13/00  Martin Ryzl     heavy localization
*  1    Gandalf   1.0         1/9/00   Martin Ryzl     
* $ 
*/ 
