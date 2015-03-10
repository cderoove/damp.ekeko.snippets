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

import org.netbeans.modules.web.util.*;

import java.util.Enumeration;
import java.io.*;

import org.openide.modules.ModuleInstall;
import org.openide.execution.Executor;
import org.openide.execution.ExecutorTask;
import org.openide.execution.ProcessExecutor;
import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.loaders.InstanceDataObject;
import org.openide.loaders.DataFolder;




/**
* Module installation class for Jasper JSP & Servlet support
*
* @author Srinivasan Chikkala
*/
public class JasperModule extends ModuleInstall {


    /** Module installed for the first time. */
    public void installed() {
        // install actions
        installActions();
        restored ();
    }

    /** Module installed again.
    * 
    */
    public void restored() {
    }

    /** Module was uninstalled. */
    public void uninstalled() {
        // uninstall actions
        uninstallActions();
    }

    /** Module is being closed. */
    public boolean closing () {
        return true; // agree to close
    }


    // -----------------------------------------------------------------------------
    // Private methods

    private static final String JSP_PAGE_WIZ = "JSPPageWizard";           //NOI18N

    private void installActions () {
        try {

            InstanceDataObject obj;
            DataFolder toolsFolder = DataFolder.create (TopManager.getDefault().getPlaces().folders().menus(), "Tools");		 //NOI18N

            if (InstanceDataObject.find (toolsFolder, JSP_PAGE_WIZ, JSPPageWizardAction.class) != null) return;
            obj = InstanceDataObject.create (toolsFolder, JSP_PAGE_WIZ, JSPPageWizardAction.class);

        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {					 //NOI18N
                e.printStackTrace ();
            }
            // ignore failure to install
        }
    }

    private void uninstallActions () {
        try {

            DataFolder toolsFolder = DataFolder.create (TopManager.getDefault().getPlaces().folders().menus(), "Tools");		 //NOI18N
            if (toolsFolder != null) {
                if (!InstanceDataObject.remove(DataFolder.findFolder(toolsFolder.getPrimaryFile()), JSP_PAGE_WIZ, JSPPageWizardAction.class)) {
                    throw new Exception("unable to remove JSP Page Wizard Action");			 //NOI18N
                }
            }


        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {				 //NOI18N
                e.printStackTrace ();
            }
            // ignore failure to uninstall
        }
    }

}

/*
 * Log
 *  3    Gandalf   1.2         1/21/00  Petr Jiricka    Update 20/01/2000
 *  2    Gandalf   1.1         1/18/00  Petr Jiricka    Update 18.1.2000
 *  1    Gandalf   1.0         1/13/00  Petr Jiricka    
 * $
 */
