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
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.cookies.*;

import org.netbeans.modules.java.*;

/** Action that is enabled on any folder and if selected opens
* a new Explorer with a simple version of the Object Browser.
*
* @author Martin Ryzl
*/
public class RMIEncapsulationAction extends CookieAction {

    static final long serialVersionUID =-6662661683495170298L;
    /** Create. new ObjectViewAction. */
    public RMIEncapsulationAction() {
    }

    /** Accept folders. */
    protected Class[] cookieClasses () {
        // add ClassDataObject without dependency on ClassModule
        Class clazzClass = null;
        try {
            clazzClass = Class.forName("org.netbeans.modules.clazz.ClassDataObject", false, TopManager.getDefault().systemClassLoader());
            return new Class[] { JavaDataObject.class, clazzClass };
        } catch (Exception ex) {
            return new Class[] { JavaDataObject.class };
        }
    }

    /** Activated on only one folder. */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** Name of the action. */
    public String getName () {
        return NbBundle.getBundle (RMIEncapsulationAction.class).getString ("CTL_RMIEncapsulationAction");
    }

    /** No help yet. */
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Open the view.
    * @param arr this should contain one FolderNode, e.g.
    */
    public void performAction (Node[] arr) {
        // there will be migrating
        RMIWizardData data = new RMIWizardData(new EncapsulationCodeGenerator());
        WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[] {new RMITypePanel(), new RMINamePanel(), new SelectMethodsPanel(), new SelectExecutorPanel()};

        DataObject dobj = (DataObject) arr[0].getCookie(DataObject.class);
        SourceCookie sc = (SourceCookie) arr[0].getCookie(SourceCookie.class);

        data.setTargetFolder(dobj.getFolder());
        data.source = sc.getSource();
        data.name = dobj.getName();
        data.sourceName = dobj.getPrimaryFile().getPackageName('.');

        data.wizardType = RMIWizardData.ENCAPSULATION;
        data.type = RMIWizardData.TYPE_OTHER;
        data.lockType = true;
        new RMIWizard(data, panels).run();
    }
}

/*
 * <<Log>>
 *  6    Gandalf   1.5         11/27/99 Patrik Knakal   
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/19/99  Martin Ryzl     dependence od 
 *       classdataobject removed
 *  3    Gandalf   1.2         7/28/99  Martin Ryzl     added selection of 
 *       executor
 *  2    Gandalf   1.1         7/28/99  Martin Ryzl     
 *  1    Gandalf   1.0         7/27/99  Martin Ryzl     
 * $
 */
