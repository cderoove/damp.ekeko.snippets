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

import java.io.File;

import org.openide.util.NbBundle;
import org.openide.modules.ModuleInstall;
import org.openide.TopManager;
import org.openide.util.Utilities;
import org.openide.loaders.*;

import org.openidex.util.Utilities2;

/**
 * Module installation class for Auto Update module
 *
 * @author Petr Hrebejk
 */
public class AutoUpdateModule extends ModuleInstall {

    static final long serialVersionUID =263540113962844813L;
    /** Called when module is installed for the first time.
     */
    public void installed () {

        try {
            Utilities2.createAction (UpdateAction.class,
                                     DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Help"), // NOI18N
                                     "AboutAction", false, false, false, true // NOI18N
                                    );

            // Create Action in action pool
            DataFolder helpActions = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Help"); // NOI18N
            Utilities2.createAction (UpdateAction.class, helpActions);
        }
        catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                e.printStackTrace ();
            }
            // ignore failure to install
        }

        //restored();
    }

    /** Module is being loaded into the IDE.
     */
    public void restored () {
        new AutoChecker().install();
    }

    /** Module was uninstalled.
     */
    public void uninstalled () {
        try {
            Utilities2.removeAction (UpdateAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Help")); // NOI18N

            // remove actions from action pool
            DataFolder helpActions = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Help"); // NOI18N
            Utilities2.removeAction (UpdateAction.class, helpActions);
        }
        catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                e.printStackTrace ();
            }
        }      // ignore failure to uninstall
    }

    /** Module is being closed.
     * @return True if the close is O.K.
     */
    public boolean closing () {
        return true; // agree to close
    }

}


/*
 * Log
 *  20   Gandalf   1.19        1/15/00  Jesse Glick     Actions pool 
 *       installation.
 *  19   Gandalf   1.18        1/12/00  Petr Hrebejk    i18n
 *  18   Gandalf   1.17        1/9/00   Petr Hrebejk    Proxy Config and 
 *       Registration number added
 *  17   Gandalf   1.16        1/6/00   Petr Hrebejk    Debug message removed
 *  16   Gandalf   1.15        1/3/00   Petr Hrebejk    Various bug fixes - 
 *       5097, 5098, 5110, 5099, 5108
 *  15   Gandalf   1.14        12/22/99 Petr Hrebejk    Various bugfixes
 *  14   Gandalf   1.13        12/20/99 Petr Hrebejk    Autocheck & security 
 *       finished
 *  13   Gandalf   1.12        12/1/99  Petr Hrebejk    Checkin signatures of 
 *       NBM files & automatic autoupdate check added
 *  12   Gandalf   1.11        11/27/99 Patrik Knakal   
 *  11   Gandalf   1.10        11/25/99 Ian Formanek    Uses Utilities module
 *  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/6/99  Petr Hrebejk    New autoupdate
 *  8    Gandalf   1.7         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  7    Gandalf   1.6         8/1/99   Petr Hrebejk    Action install & 
 *       multiuser install fix
 *  6    Gandalf   1.5         7/21/99  Petr Hrebejk    Action installation fix
 *  5    Gandalf   1.4         7/15/99  Petr Hrebejk    Installation of action 
 *       to menu on module install added
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/7/99   Petr Hrebejk    
 *  2    Gandalf   1.1         6/7/99   Petr Hrebejk    
 *  1    Gandalf   1.0         4/25/99  Ian Formanek    
 * $
 */
