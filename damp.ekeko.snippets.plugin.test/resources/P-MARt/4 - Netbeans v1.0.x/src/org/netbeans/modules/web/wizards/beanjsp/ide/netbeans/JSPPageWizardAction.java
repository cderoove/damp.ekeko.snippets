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

package  org.netbeans.modules.web.wizards.beanjsp.ide.netbeans;

import java.awt.event.*;

import java.util.HashSet;
import java.sql.*;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.CallableSystemAction;
import org.openide.nodes.Node;


import org.netbeans.modules.web.wizards.beanjsp.ui.*;


public class JSPPageWizardAction  extends CallableSystemAction {

    /** Creates new JSPWizardAction */
    public JSPPageWizardAction() { }

    /** Name of the action. */
    public String getName () {
        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        return resBundle.getString("JBW_JSPPageWizardActionName");				 //NOI18N
    }

    /** No help yet. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (JSPPageWizardAction.class);
    }


    protected String iconResource () {
        return "/org/netbeans/modules/web/wizards/beanjsp/resources/images/JSPPageWizardIcon.gif";		 //NOI18N
    }

    /** Open the view.
    * @param arr this should contain one FolderNode, e.g.
    */
    public  void performAction() {
        //NB remove it for using Netbeans Wizard impl
        // WizardManager.setDefault(new WizardManager());

        JSPPageWizard.showWizard();
    }
}

