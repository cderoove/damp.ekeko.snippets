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




/**
 * Import project from other developer envirnments.
 * @author  Petr Zajac
 * @version
 */
public class ImportProjectAction extends NodeAction {

    public static final String EMPTY_STRING = "";  // NOI18N

    /** import core */
    protected static Import im = new Import ();

    /**
     * Creates new ImportProjectAction
     */
    public ImportProjectAction () {}

    /**
     * Detect if import is in progress. It is usefully when it imports into new project.
     * because create new project is assynchronous.
     */
    protected static boolean doImport = false ;

    /** test if import is in progress
    */
    public boolean isImport () {
        return doImport;
    }

    /**
     * It is enabled every time
     * @return true
     */
    public boolean enable (Node[] nodes) {
        return true;
    }

    public Import getDefaultImport () {
        return im;
    }


    /* Icon resource.
     * @return name of resource for icon
     */
    protected String iconResource () {
        return "/org/netbeans/modules/projects/resources/ImportProject.gif"; // NOI18N
    }

    /**
     * Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName () {
        return org.openide.util.NbBundle.getBundle (ImportProjectAction.class).getString ("ACT_ImportProject");
    }

    /**
     * Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ImportProjectAction.class);
    }



    protected void performAction (Node[] activatedNodes) {
        doImport = true ;
        ImportProject importPrj = im.chooseImportProject ();
        String msg = null;
        msg = org.openide.util.NbBundle.getBundle (ImportProjectAction.class).getString ("MSG_ImportProject");

        TopManager.getDefault ().setStatusText (msg);


        if (importPrj != null) {
            im.go (importPrj);

            TopManager.getDefault ().setStatusText (EMPTY_STRING);
        } else {

            msg = org.openide.util.NbBundle.getBundle (ImportProjectAction.class).getString ("MSG_WrongImportFile");

            TopManager.getDefault ().setStatusText (msg);
        }
        doImport = false ;
    }

}

/*
 * Log
 *  4    Gandalf   1.3         2/4/00   Martin Ryzl     import fix  
 *  3    Gandalf   1.2         1/13/00  Martin Ryzl     heavy localization
 *  2    Gandalf   1.1         1/8/00   Martin Ryzl       
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */



