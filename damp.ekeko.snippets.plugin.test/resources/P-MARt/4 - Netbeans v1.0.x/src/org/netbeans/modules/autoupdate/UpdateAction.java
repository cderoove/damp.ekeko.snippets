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

package org.netbeans.modules.autoupdate;

import java.util.ResourceBundle;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/** Update action.
 *
 * @author Petr Hrebejk
 */
public class UpdateAction extends CallableSystemAction {

    /** Resource bundle */
    private static final ResourceBundle bundle = NbBundle.getBundle( UpdateAction.class );

    /** generated Serialized Version UID
     */
    static final long serialVersionUID = 1544145343804094269L;

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return NbBundle.getBundle (UpdateAction.class).getString ("CTL_Update"); // "IDE Update..."
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (UpdateAction.class);
    }

    /** Resource name for the icon.
     * @return resource name
     */
    protected String iconResource () {
        return "/org/netbeans/modules/autoupdate/resources/updateAction.gif"; // NOI18N
    }

    /** Return true if the action should be enabled in menu
     * @return True if action should be enabled.
     */
    public boolean isEnabled() {
        return !Autoupdater.isRunning();
    }

    /** The action should be executed. Opens the UpdateTopComponent if not open
     * and asks user if to check the web for new updates.
     */

    /*
    public void performAction () {
      //UpdateTopComponent.getDefault().open();
      //UpdateTopComponent.getDefault().requestFocus();
      
      NotifyDescriptor.Confirmation nd = new NotifyDescriptor.Confirmation(
              bundle.getString( "MSG_CheckWebConfirmation" ),
              bundle.getString( "CTL_CheckWebConfirmation" ),
              NotifyDescriptor.OK_CANCEL_OPTION );
      if ( TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.OK_OPTION ) ) {
        Autoupdater.doAutoupdate(); 
      }
}
    */

    public void performAction () {
        Wizard wizard = new Wizard();
        wizard.go();
        //AutoChecker.doCheck();
    }

    /** For testing only */
    public static void main( String args[] ) {

        Wizard wizard = new Wizard();
        wizard.go();

        /*
        UpdateTopComponent.getDefault().open();
        UpdateTopComponent.getDefault().requestFocus();

        NotifyDescriptor.Confirmation nd = new NotifyDescriptor.Confirmation(
                bundle.getString( "MSG_CheckWebConfirmation" ),
                bundle.getString( "CTL_CheckWebConfirmation" ),
                NotifyDescriptor.OK_CANCEL_OPTION );
        if ( TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.OK_OPTION ) ) {
          Autoupdater.doAutoupdate(); 
    }
        */
    }

}

/*
 * Log
 *  13   Gandalf   1.12        1/12/00  Petr Hrebejk    i18n
 *  12   Gandalf   1.11        1/9/00   Petr Hrebejk    Proxy Config and 
 *       Registration number added
 *  11   Gandalf   1.10        12/1/99  Petr Hrebejk    Checkin signatures of 
 *       NBM files & automatic autoupdate check added
 *  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/10/99 Petr Hrebejk    AutoUpdate made to 
 *       wizard
 *  8    Gandalf   1.7         10/8/99  Petr Hrebejk    Next development version
 *  7    Gandalf   1.6         10/6/99  Petr Hrebejk    New autoupdate
 *  6    Gandalf   1.5         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  5    Gandalf   1.4         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/7/99   Petr Hrebejk    
 *  2    Gandalf   1.1         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  1    Gandalf   1.0         4/25/99  Ian Formanek    
 * $
 */
