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

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;

/** Action that is enabled on any folder and if selected opens
* a new Explorer with a simple version of the Object Browser.
*
* @author Jaroslav Tulach
*/
public class RMIWizardAction extends CallableSystemAction {

    public static final String WIZARD_ICON = "/org/netbeans/modules/rmi/resources/rmiWizard.gif";

    static final long serialVersionUID =2151471284881427046L;
    /** Create. new ObjectViewAction. */
    public RMIWizardAction() {
    }

    /** Name of the action. */
    public String getName () {
        return NbBundle.getBundle (RMIWizardAction.class).getString ("CTL_RMIWizardAction");
    }

    /** No help yet. */
    public org.openide.util.HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Open the view.
    * @param arr this should contain one FolderNode, e.g.
    */
    public void performAction () {
        new RMIWizard().run();
    }

    protected String iconResource() {
        return WIZARD_ICON;
    }
}

/*
 * <<Log>>
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/13/99 Martin Ryzl     icon updated
 *  1    Gandalf   1.0         7/27/99  Martin Ryzl     
 * $
 */
