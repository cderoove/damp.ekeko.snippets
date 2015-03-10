/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi.wizard;


import org.openide.*;
import org.openide.src.*;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;

import org.netbeans.modules.java.*;

/** Action that is enabled on any folder and if selected opens
* a new Explorer with a simple version of the Object Browser.
*
* @author Jaroslav Tulach
*/
public class RMIMigrationAction extends CookieAction {

    static final long serialVersionUID =2573319116359248010L;
    /** Create. new ObjectViewAction. */
    public RMIMigrationAction() {
    }

    /** Accept folders. */
    protected Class[] cookieClasses () {
        return new Class[] { ClassElement.class, JavaDataObject.class };
    }

    /** Activated on only one folder. */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** Name of the action. */
    public String getName () {
        return NbBundle.getBundle (RMIWizardAction.class).getString ("CTL_RMIMigrationAction");
    }

    /** No help yet. */
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Open the view.
    * @param arr this should contain one FolderNode, e.g.
    */
    public void performAction (Node[] arr) {
        /*
        // there will be migrating
        RMIWizardData data = new RMIWizardData();
        WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[] {new RMITypePanel(), new RMINamePanel(), new SelectMethodsPanel()};
        ClassElement ce = (ClassElement)arr[0].getCookie(ClassElement.class);
        data.type = RMIWizardData.TYPE_OTHER;
        data.lockType = true;
        new RMIWizard(data, panels).run();

        */
    }
}

/*
 * <<Log>>
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/29/99  Martin Ryzl     executor selection is 
 *       working
 *  2    Gandalf   1.1         7/27/99  Martin Ryzl     new version of generator
 *       is working
 *  1    Gandalf   1.0         7/27/99  Martin Ryzl     
 * $
 */
