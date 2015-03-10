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

package org.netbeans.modules.web.core;

import java.util.Enumeration;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.openide.modules.ModuleInstall;
import org.openide.execution.Executor;
import org.openide.execution.ExecutorTask;
import org.openide.execution.ProcessExecutor;
import org.openide.util.NbBundle;
import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.loaders.InstanceDataObject;
import org.openide.loaders.DataFolder;

import org.openidex.util.Utilities2;

import org.netbeans.modules.web.core.jswdk.ServletJspExecutor;
import org.netbeans.modules.web.core.jswdk.Execute2Action;
import org.netbeans.modules.web.core.jsploader.EditServletAction;

import org.netbeans.modules.web.wizards.beanjsp.ide.netbeans.JSPPageWizardAction;

/**
* Module installation class for servlet support
*
* @author Petr Jiricka
*/
public class ServletSupportModule extends ModuleInstall {

    /** serialVersionUID */
    private static final long serialVersionUID = -4736324822902413381L;

    /** Module installed for the first time. */
    public void installed() {
        // 1. copy Servlet templates
        copyTemplates ();

        // install actions
        installActions();

        restored ();
    }

    /** Module installed again.
    * Add applet executor
    */
    public void restored() {

        // install JSP syntax coloring
        addEditorInitializer();
        installEditorOptions();

        // Jetty config mapping
        //HttpServerSettings.OPTIONS.mapServlet(JettyConstants.JETTY_CONFIG_PATH, JettyConfigServlet.class.getName());

    }

    /** Module was uninstalled. */
    public void uninstalled() {

        // uninstall options from the editor context option
        uninstallEditorOptions();

        // uninstall actions
        uninstallActions();
    }

    /** Module is being closed. */
    public boolean closing () {

        ServletJspExecutor.killServerIfRunning();
        return true; // agree to close
    }


    // -----------------------------------------------------------------------------
    // Private methods

    // editor
    private void addEditorInitializer() {
        try {
            Class settings = Class.forName
                             ("org.netbeans.editor.Settings", // NOI18N
                              false, this.getClass().getClassLoader()); // only test for editor module

            Class restore = Class.forName
                            ("org.netbeans.modules.web.core.syntax.RestoreColoring", // NOI18N
                             false, this.getClass().getClassLoader());
            Method restoreMethod = restore.getMethod ("addInitializer", null); // NOI18N
            restoreMethod.invoke (restore.newInstance(), null);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        }
    }

    // editor
    private void installEditorOptions() {
        try {
            Class settings = Class.forName
                             ("org.netbeans.editor.Settings", // NOI18N
                              false, this.getClass().getClassLoader()); // only test for editor module

            Class restore = Class.forName
                            ("org.netbeans.modules.web.core.syntax.RestoreColoring", // NOI18N
                             false, this.getClass().getClassLoader());
            Method restoreMethod = restore.getMethod ("installOptions", null); // NOI18N
            restoreMethod.invoke (restore.newInstance(), null);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        }
    }

    // editor
    private void uninstallEditorOptions() {
        try {
            Class settings = Class.forName
                             ("org.netbeans.editor.Settings", // NOI18N
                              false, this.getClass().getClassLoader()); // only test for editor module

            Class restore = Class.forName
                            ("org.netbeans.modules.web.core.syntax.RestoreColoring", // NOI18N
                             false, this.getClass().getClassLoader());
            Method restoreMethod = restore.getMethod ("uninstallOptions", null); // NOI18N
            restoreMethod.invoke (restore.newInstance(), null);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        }
    }


    private void installActions () {
        try {
            Utilities2.createAction (EditQueryStringAction.class,
                                     DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Build")); // NOI18N

            Utilities2.createAction (Execute2Action.class,
                                     DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Build")); // NOI18N

            Utilities2.createAction (EditServletAction.class,
                                     DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "System")); // NOI18N

            installWizardActions();
        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                e.printStackTrace ();
            }
            // ignore failure to install
        }
    }

    private void installWizardActions () throws IOException {
        Utilities2.createAction (JSPPageWizardAction.class,
                                 DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Tools"), // NOI18N
                                 "UnmountFSAction", true, true, false, false // NOI18N
                                );
        Utilities2.createAction (JSPPageWizardAction.class,
                                 DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().actions (), "Tools") // NOI18N
                                );
    }


    private void uninstallActions () {
        try {
            Utilities2.removeAction (EditQueryStringAction.class,
                                     DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Build")); // NOI18N

            Utilities2.removeAction (Execute2Action.class,
                                     DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Build")); // NOI18N

            Utilities2.removeAction (EditServletAction.class,
                                     DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "System")); // NOI18N

            uninstallWizardActions();
        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                e.printStackTrace ();
            }
            // ignore failure to install
        }
    }

    private void uninstallWizardActions () throws IOException {
        Utilities2.removeAction (JSPPageWizardAction.class,
                                 DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Tools")); // NOI18N
        Utilities2.removeAction (JSPPageWizardAction.class,
                                 DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().actions (), "Tools")); // NOI18N
    }

    private void copyTemplates () {
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
                NbBundle.getLocalizedFile ("org.netbeans.modules.web.core.templates", "jar").openStream () // NOI18N
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }

}

/*
 * Log
 *  30   Gandalf   1.29        1/17/00  Petr Jiricka    Fixed bug : Coloring 
 *       options not uninstalled when uninstalling the module.
 *  29   Gandalf   1.28        1/16/00  Petr Jiricka    ViewServletAction moved 
 *       to System menu.
 *  28   Gandalf   1.27        1/16/00  Jesse Glick     Actions pool; localized 
 *       jars.
 *  27   Gandalf   1.26        1/15/00  Jesse Glick     Actions pool.
 *  26   Gandalf   1.25        1/13/00  Petr Jiricka    Package name change for 
 *       JSP wizard
 *  25   Gandalf   1.24        1/12/00  Petr Jiricka    Fully I18n-ed
 *  24   Gandalf   1.23        1/12/00  Petr Jiricka    i18n phase 1
 *  23   Gandalf   1.22        1/9/00   Petr Jiricka    Added installation of 
 *       JSP wizard action
 *  22   Gandalf   1.21        1/4/00   Petr Jiricka    Changed the way of 
 *       installing actions
 *  21   Gandalf   1.20        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  20   Gandalf   1.19        10/12/99 Petr Jiricka    Security policy removed
 *  19   Gandalf   1.18        10/9/99  Petr Jiricka    Kill server process 
 *       before exiting the IDE
 *  18   Gandalf   1.17        10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  17   Gandalf   1.16        9/30/99  Petr Jiricka    Removed Jetty 
 *       initialization and destruction
 *  16   Gandalf   1.15        9/13/99  Petr Jiricka    Added registration of 
 *       JSP coloring - dynamic dependency on the editor module
 *  15   Gandalf   1.14        9/10/99  Petr Jiricka    
 *  14   Gandalf   1.13        8/27/99  Petr Jiricka    ???
 *  13   Gandalf   1.12        7/27/99  Petr Jiricka    
 *  12   Gandalf   1.11        7/24/99  Petr Jiricka    
 *  11   Gandalf   1.10        7/16/99  Petr Jiricka    Fixed stopping JSWDK
 *  10   Gandalf   1.9         7/15/99  Petr Jiricka    
 *  9    Gandalf   1.8         7/3/99   Petr Jiricka    
 *  8    Gandalf   1.7         7/3/99   Petr Jiricka    
 *  7    Gandalf   1.6         6/30/99  Petr Jiricka    
 *  6    Gandalf   1.5         6/10/99  Ian Formanek    Copying templates on 
 *       install
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         6/3/99   Petr Jiricka    
 *  3    Gandalf   1.2         6/1/99   Petr Jiricka    
 *  2    Gandalf   1.1         5/28/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/25/99  Petr Jiricka    
 * $
 */
