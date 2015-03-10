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

package org.netbeans.modules.rmi;

import java.lang.reflect.*;

import org.openide.TopManager;
import org.openide.compiler.Compiler;
import org.openide.compiler.*;
import org.openide.loaders.*;
import org.openide.modules.ModuleInstall;
import org.openide.src.nodes.*;
import org.openide.util.*;

import org.openidex.util.*;

/** Module installer.
*
* @author Martin Ryzl
*/
public class RMIModule extends ModuleInstall {

    /** My own request processor. */
    static RequestProcessor rp = null;

    /** Serial version UID. */
    static final long serialVersionUID = -4791113880926743826L;

    public void installed() {
        try {
            DataFolder toolsFolder = DataFolder.create (TopManager.getDefault().getPlaces().folders().menus(), "Tools"); // NOI18N
            if (toolsFolder != null) {
                Utilities2.createAction(org.netbeans.modules.rmi.wizard.RMIWizardAction.class, toolsFolder, "UnmountFSAction", true, true, false, false); // NOI18N
            }
        } catch (Exception ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
        }

        copyTemplates ();
        copyOther ();
        restored();
    }

    public void restored() {
        startRP();
    }

    public void uninstalled() {
        // [PENDING - remove installed files]
        try {
            DataFolder toolsFolder = DataFolder.create (TopManager.getDefault().getPlaces().folders().menus(), "Tools"); // NOI18N
            if (toolsFolder != null) {
                Utilities2.removeAction(org.netbeans.modules.rmi.wizard.RMIWizardAction.class, toolsFolder);
            }
        } catch (Exception ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
        }
    }

    public boolean closing() {
        stopRP();
        return true;
    }

    // -----------------------------------------------------------------------------
    // Private methods

    /** Stop RMI RequestProcessor.
    */
    private static void stopRP() {
        if (rp != null) {
            rp.stop();
            rp = null;
        }
    }

    /** Start RMI RequestProcessor.
    */
    private static void startRP() {
        stopRP();
        rp = new RequestProcessor("RMI Request Processor"); // NOI18N
    }

    /** Get RMI RequestProcessor.
    */
    public static RequestProcessor getRP() {
        if (rp == null) startRP();
        return rp;
    }

    /** Install templates.
    */
    private void copyTemplates () {
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
                getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/rmi/toinstall/templates.jar") // NOI18N
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }

    /** Install other files.
    */
    private void copyOther () {
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault ().getRepository ().getDefaultFileSystem ().getRoot (),
                getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/rmi/toinstall/rmi.jar") // NOI18N
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }

    // Remote patterns
    /** Dynamicaly registers ElementFactory.
     * @param className Name of class which registers the factories.
     * @param methodName Name of method for registering factories.
     * @param factory The factory to register.
     */
    private void invokeDynamic( String className, String methodName, FilterFactory factory ) {
        try {
            Class dataObject = TopManager.getDefault().systemClassLoader().loadClass( className );

            if ( dataObject == null )  return;

            Method method = dataObject.getDeclaredMethod( methodName, new Class[] { FilterFactory.class }  );
            if ( method == null ) return;

            method.invoke( null, new Object[] { factory } );
        }
        catch (Exception ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
        }
    }
}




/*
 * <<Log>>
 *  18   Gandalf-post-FCS1.16.1.0    3/20/00  Martin Ryzl     localization
 *  17   Gandalf   1.16        1/28/00  Martin Ryzl     installation/uninstallation
 *         of actions fixed
 *  16   Gandalf   1.15        11/27/99 Patrik Knakal   
 *  15   Gandalf   1.14        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        10/6/99  Martin Ryzl     
 *  13   Gandalf   1.12        10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  12   Gandalf   1.11        9/13/99  Martin Ryzl     
 *  11   Gandalf   1.10        9/10/99  Jaroslav Tulach Changes to services.
 *  10   Gandalf   1.9         8/27/99  Martin Ryzl     
 *  9    Gandalf   1.8         8/18/99  Martin Ryzl     corrected localization
 *  8    Gandalf   1.7         7/27/99  Martin Ryzl     
 *  7    Gandalf   1.6         7/12/99  Martin Ryzl     large changes  
 *  6    Gandalf   1.5         6/10/99  Ian Formanek    Copying templates on 
 *       install
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/28/99  Martin Ryzl     
 *  3    Gandalf   1.2         5/27/99  Martin Ryzl     many fixes
 *  2    Gandalf   1.1         5/4/99   Martin Ryzl     
 *  1    Gandalf   1.0         4/20/99  Martin Ryzl     
 * $
 */
