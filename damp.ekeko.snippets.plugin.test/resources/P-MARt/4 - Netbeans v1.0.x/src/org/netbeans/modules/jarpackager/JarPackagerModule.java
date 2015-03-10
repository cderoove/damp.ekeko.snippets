/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager;

import java.io.IOException;
import java.util.ResourceBundle;

import org.openide.modules.*;
import org.openide.filesystems.*;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.loaders.InstanceDataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.windows.OutputWriter;
import org.openide.Places;

import org.openidex.util.Utilities2;

import org.netbeans.modules.jarpackager.actions.*;


/** Jar packager module. Adds / removes menu items
* on installation / deinstallation of this module. 
*
* @author Dafe Simonek
*/
public class JarPackagerModule extends ModuleInstall {

    static final long serialVersionUID = -4504892982741084436L;


    /** Called when the module is first installed into the IDE.
    * Adds actions to the menu and actions pool */
    public void installed () {
        installActions();
        copyTemplates();
    }

    /** Called when the module is uninstalled (from a running IDE).
    * Removes menu items 
    */
    public void uninstalled () {
        uninstallActions();
        // clear singleton instance of the jar packager manager top component
        PackagingView.clearPackagingView();
    }

    /** Installs our actions to the actions pool and the menu */
    void installActions () {
        try {
            Places.Folders folders = TopManager.getDefault().getPlaces().folders();
            DataFolder toolsFolder = DataFolder.create(folders.actions(), "Tools"); // NOI18N
            ResourceBundle bundle = NbBundle.getBundle(JarPackagerModule.class);
            // install into actions pool
            Utilities2.createAction(JarPackagerAction.class, toolsFolder);
            Utilities2.createAction(ManageJarAction.class, toolsFolder);
            Utilities2.createAction(AddToJarAction.class, toolsFolder);
            Utilities2.createAction(UpdateJarAction.class, toolsFolder);
            Utilities2.createAction(DeployJarAction.class, toolsFolder);
            // install into menu
            DataFolder menuFolder = DataFolder.create(folders.menus(), "Tools"); // NOI18N
            Utilities2.createAction(
                JarPackagerAction.class, menuFolder,
                "UnmountFSAction", true, true, false, false // NOI18N
            );
        } catch (Exception exc) {
            if (System.getProperty("netbeans.debug.exceptions") != null) {
                exc.printStackTrace();
            }
            // notify user through standard ide output
            OutputWriter ow = TopManager.getDefault().getStdOut();
            ow.println(NbBundle.getBundle(JarPackagerModule.class).
                       getString("MSG_ActionProblem"));
        }
    }

    /** copies our templates to the system template dir */
    void copyTemplates () {
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault().getPlaces().folders().templates().getPrimaryFile(),
                NbBundle.getLocalizedFile ("org.netbeans.modules.jarpackager.toinstall.templates", "jar").openStream () // NOI18N
            );
        } catch (java.io.IOException exc) {
            org.openide.TopManager.getDefault().notifyException(exc);
        }
    }


    /** Uninstalls our actions from actions pool and the menu */
    void uninstallActions () {
        try {
            Places.Folders folders = TopManager.getDefault().getPlaces().folders();
            DataFolder toolsFolder = DataFolder.create(folders.actions(), "Tools"); // NOI18N
            ResourceBundle bundle = NbBundle.getBundle(JarPackagerModule.class);
            // remove from actions pool
            Utilities2.removeAction(JarPackagerAction.class, toolsFolder);
            Utilities2.removeAction(ManageJarAction.class, toolsFolder);
            Utilities2.removeAction(AddToJarAction.class, toolsFolder);
            Utilities2.removeAction(UpdateJarAction.class, toolsFolder);
            Utilities2.removeAction(DeployJarAction.class, toolsFolder);
            // remove from menu
            Utilities2.removeAction(
                JarPackagerAction.class,
                DataFolder.create(folders.menus(), "Tools") // NOI18N
            );
        } catch (Exception exc) {
            if (System.getProperty("netbeans.debug.exceptions") != null) {
                exc.printStackTrace();
            }
            // notify user through standard ide output
            OutputWriter ow = TopManager.getDefault().getStdOut();
            ow.println(NbBundle.getBundle(JarPackagerModule.class).
                       getString("MSG_NotRemoved"));
        }
    }


}

/*
* <<Log>>
*  15   Gandalf   1.14        2/14/00  David Simonek   Right installing of our 
*       tools action
*  14   Gandalf   1.13        1/26/00  David Simonek   Minor changes concerning 
*       correct action installation / removal
*  13   Gandalf   1.12        1/25/00  David Simonek   Various bugfixes and i18n
*  12   Gandalf   1.11        1/16/00  Jesse Glick     Localized jars.
*  11   Gandalf   1.10        1/16/00  David Simonek   i18n
*  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    Gandalf   1.8         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
*        changed to class + some methods added
*  8    Gandalf   1.7         9/16/99  David Simonek   a lot of bugfixes (RE 
*       filters, empty jar content etc)  added templates
*  7    Gandalf   1.6         8/18/99  David Simonek   stupid bugs fixes
*  6    Gandalf   1.5         8/17/99  David Simonek   installations of actions,
*       icon changing
*  5    Gandalf   1.4         6/10/99  David Simonek   progress indocator + 
*       minor bugfixes....
*  4    Gandalf   1.3         6/9/99   David Simonek   bugfixes, progress 
*       dialog, compiling progress..
*  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         6/3/99   David Simonek   
*  1    Gandalf   1.0         5/26/99  David Simonek   
* $
*/