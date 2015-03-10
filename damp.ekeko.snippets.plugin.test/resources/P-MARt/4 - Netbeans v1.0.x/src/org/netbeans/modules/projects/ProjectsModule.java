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

package org.netbeans.modules.projects;

import java.io.*;
import java.util.LinkedList;

import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.cookies.ExecCookie;
import org.openide.cookies.ProjectCookie;
import org.openide.execution.ThreadExecutor;
import org.openide.loaders.*;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.*;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;
import org.openide.util.io.NbMarshalledObject;

import org.openidex.util.Utilities2;

import org.netbeans.modules.projects.settings.*;

/**
 * Module installation class for Form Editor
 * 
 * @author Ian Formanek
 */
public class ProjectsModule extends ModuleInstall {

    static int numberOfStarts = 0;

    public static final String DEFAULT_PROJECT_NAME = "Default"; // NOI18N
    public static final String PROJECT_LAST_NAME = "project"; // NOI18N
    public static final String PROJECT_LAST_EXT = "last"; // NOI18N

    /** */
    private static final int WAIT_PISHVEITSH = 650;

    static final long serialVersionUID = -8515854707471314270L;

    private static final boolean DEBUG = true;

    /** This variable indicates that the initial filesystem was created  */
    static boolean defaultProjectCreated = false;

    /**
     * Module initialization for the first time.
     */
    public void restored () {
        numberOfStarts++;

        if (!lastExists()) createDefaultProject();

        // save default workspaces
        NbMarshalledObject nbo = null;
        try {
            nbo = PSupport.getGlobalProjectContent().getWindowManager();
        } catch (IOException ex) {
            //
        }
        if (nbo == null) {
            final java.awt.Window mainWindow =  TopManager.getDefault ().getWindowManager ().getMainWindow ();
            mainWindow.addWindowListener(
                new java.awt.event.WindowAdapter () {
                    public void windowOpened (java.awt.event.WindowEvent ev) {
                        // notify me no more
                        mainWindow.removeWindowListener(this);
                        // wait
                        RequestProcessor.postRequest(new WMSaver(), WAIT_PISHVEITSH);
                    }
                }
            );
        }
    }

    /**
     * Module installed for the first time.
     */
    public void installed () {
        installActions();
        //    createDefaultProject();
    }

    /**
     * Module was uninstalled.
     */
    public void uninstalled () {
        uninstallActions ();
        removeLast();
    }

    // -----------------------------------------------------------------------------------
    // Private methods

    private void installActions () {
        try {


            // remove old actions
            Class clazz = null;

            DataFolder  toolsActionPool =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (),
                                   "Tools"); // NOI18N
            if ((clazz = Class.forName("org.netbeans.core.actions.OptionsAction")) != null) { // NOI18N
                Utilities2.removeAction (clazz, toolsActionPool);
            }

            if ((clazz = Class.forName("org.netbeans.core.actions.SettingsAction")) != null) { // NOI18N
                Utilities2.removeAction (clazz, toolsActionPool);
            }

            Utilities2.createAction (OptionsAction.class, toolsActionPool);

            DataFolder  toolsFolder =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().menus (),
                                   "Tools"); // NOI18N

            if ((clazz = Class.forName("org.netbeans.core.actions.OptionsAction")) != null) { // NOI18N
                Utilities2.removeAction (clazz, toolsFolder);
            }

            if ((clazz = Class.forName("org.netbeans.core.actions.SettingsAction")) != null) { // NOI18N
                Utilities2.removeAction (clazz, toolsFolder);
            }

            Utilities2.createAction (OptionsAction.class, toolsFolder, "ConfigureShortcutsAction", true, false, false, false);   // NOI18N

            DataFolder  systemActionPool =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (),
                                   "System"); // NOI18N

            DataFolder  fileFolder =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().menus (),
                                   "File");     // NOI18N

            if ((clazz = Class.forName("org.netbeans.core.actions.SaveSettingsAction")) != null) { // NOI18N
                Utilities2.removeAction (clazz, systemActionPool);
                Utilities2.removeAction (clazz, fileFolder);
            }

            InstanceDataObject.remove(fileFolder, "Separator2", javax.swing.JSeparator.class); // NOI18N

            // 1. install into actions pool

            DataFolder  projActionPool =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (),
                                   "Project"); // NOI18N

            try {
                projActionPool.getPrimaryFile ().setAttribute ("SystemFileSystem.localizingBundle", "org.netbeans.modules.projects.Bundle"); // NOI18N
            } catch (IOException ioe) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ioe.printStackTrace ();
            }

            Utilities2.createAction (NewProjectAction.class, projActionPool);
            Utilities2.createAction (org.openide.actions.OpenProjectAction.class, projActionPool);
            Utilities2.createAction (SetMainClassAction.class, projActionPool);
            Utilities2.createAction (SaveProjectAction.class, projActionPool);
            Utilities2.createAction (BuildProjectAction.class, projActionPool);
            Utilities2.createAction (CompileProjectAction.class, projActionPool);
            Utilities2.createAction (ExecuteProjectAction.class, projActionPool);
            Utilities2.createAction (DebugProjectAction.class, projActionPool);
            Utilities2.createAction (SettingsAction.class, projActionPool);
            Utilities2.createAction (ImportProjectAction.class, projActionPool);
            Utilities2.createAction (AddToProjectAction.class, projActionPool);
            Utilities2.createAction (Add2ProjectAction.class, projActionPool);
            Utilities2.createAction (AddNewAction.class, projActionPool);
            Utilities2.createAction (SetMainClassCookieAction.class, projActionPool);

            // 2. install into menu
            boolean alreadyExists = false;

            DataObject[]  folders =
                org.openide.TopManager.getDefault ().getPlaces ().folders ().menus ().getChildren ();

            for (int i = 0; i < folders.length; i++) {
                if ("Project".equals (folders[i].getName ())) { // NOI18N
                    alreadyExists = true;
                    break;
                }
            }

            // [PENDING - check whether there is already the projects menu]

            int index = -1;

            if (!alreadyExists) {
                for (int i = 0; i < folders.length; i++) {
                    if ("Build".equals (folders[i].getName ())) { // NOI18N
                        index = i;
                        break;
                    }
                }
            }

            DataFolder  projectsFolder =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().menus (),
                                   "Project");    // NOI18N

            try {
                projectsFolder.getPrimaryFile ().setAttribute ("SystemFileSystem.localizingBundle", "org.netbeans.modules.projects.Bundle"); // NOI18N
            } catch (IOException ioe) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ioe.printStackTrace ();
            }

            InstanceDataObject[] idos = new InstanceDataObject[] {
                                            Utilities2.createAction (NewProjectAction.class, projectsFolder),
                                            Utilities2.createAction (OpenProjectAction.class, projectsFolder),
                                            InstanceDataObject.create(projectsFolder, sepName (1), javax.swing.JSeparator.class),
                                            Utilities2.createAction (SaveProjectAction.class, projectsFolder),
                                            InstanceDataObject.create(projectsFolder, sepName (2), javax.swing.JSeparator.class),
                                            Utilities2.createAction (CompileProjectAction.class, projectsFolder),
                                            Utilities2.createAction (BuildProjectAction.class, projectsFolder),
                                            InstanceDataObject.create(projectsFolder, sepName (3), javax.swing.JSeparator.class),
                                            Utilities2.createAction (SetMainClassAction.class, projectsFolder),
                                            Utilities2.createAction (ExecuteProjectAction.class, projectsFolder),
                                            Utilities2.createAction (DebugProjectAction.class, projectsFolder),
                                            InstanceDataObject.create(projectsFolder, sepName (4), javax.swing.JSeparator.class),
                                            Utilities2.createAction (ImportProjectAction.class, projectsFolder),
                                            InstanceDataObject.create(projectsFolder, sepName (5), javax.swing.JSeparator.class),
                                            Utilities2.createAction (SettingsAction.class, projectsFolder),
                                        };

            projectsFolder.setOrder(idos);
            if (index != -1) {
                try {
                    DataObject[]  menusOrder = new DataObject[folders.length + 1];
                    System.arraycopy (folders, 0, menusOrder, 0, index);
                    menusOrder[index] = projectsFolder;
                    System.arraycopy (folders, index, menusOrder, index + 1,
                                      folders.length - index);
                    org.openide.TopManager.getDefault ().getPlaces ().folders ().menus ().setOrder (menusOrder);
                } catch (java.io.IOException e) {
                    // ignore failure to set order
                }
            }

        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) { // NOI18N
                e.printStackTrace ();
            }

            // ignore failure to install
        }
    }

    private static String sepName (int i) {
        return java.text.MessageFormat.format (org.openide.util.NbBundle.getBundle (ProjectsModule.class).getString ("LBL_menu_separator"),
                                               new Object[] { new Integer (i) });
    }

    private void uninstallActions () {
        try {

            Class clazz = null;

            // remove from actions pool
            DataFolder  projActionPool =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (),
                                   "Project"); // NOI18N

            Utilities2.removeAction (NewProjectAction.class, projActionPool);
            Utilities2.removeAction (OpenProjectAction.class, projActionPool);
            Utilities2.removeAction (SetMainClassAction.class, projActionPool);
            Utilities2.removeAction (SaveProjectAction.class, projActionPool);
            Utilities2.removeAction (BuildProjectAction.class, projActionPool);
            Utilities2.removeAction (CompileProjectAction.class, projActionPool);
            Utilities2.removeAction (ExecuteProjectAction.class, projActionPool);
            Utilities2.removeAction (DebugProjectAction.class, projActionPool);
            Utilities2.removeAction (ImportProjectAction.class, projActionPool);
            Utilities2.removeAction (SettingsAction.class, projActionPool);
            Utilities2.removeAction (AddToProjectAction.class, projActionPool);
            Utilities2.removeAction (Add2ProjectAction.class, projActionPool);
            Utilities2.removeAction (AddNewAction.class, projActionPool);
            Utilities2.removeAction (SetMainClassCookieAction.class, projActionPool);

            // remove the projects menu folder if it is empty
            if (projActionPool.getChildren ().length == 0) {
                try {
                    projActionPool.delete ();
                } catch (java.io.IOException e) {

                    // ignore failure to delete the folder
                }
            }
            // remove from menu
            DataFolder  projectsFolder =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().menus (),
                                   "Project");   // NOI18N

            Utilities2.removeAction (NewProjectAction.class, projectsFolder);
            Utilities2.removeAction (OpenProjectAction.class, projectsFolder);
            InstanceDataObject.remove(projectsFolder, sepName (1), javax.swing.JSeparator.class);
            Utilities2.removeAction (SaveProjectAction.class, projectsFolder);
            InstanceDataObject.remove(projectsFolder, sepName (2), javax.swing.JSeparator.class);
            Utilities2.removeAction (CompileProjectAction.class, projectsFolder);
            Utilities2.removeAction (BuildProjectAction.class, projectsFolder);
            InstanceDataObject.remove(projectsFolder, sepName (3), javax.swing.JSeparator.class);
            Utilities2.removeAction (SetMainClassAction.class, projectsFolder);
            Utilities2.removeAction (ExecuteProjectAction.class, projectsFolder);
            Utilities2.removeAction (DebugProjectAction.class, projectsFolder);
            InstanceDataObject.remove(projectsFolder, sepName (4), javax.swing.JSeparator.class);
            Utilities2.removeAction (ImportProjectAction.class, projectsFolder);
            InstanceDataObject.remove(projectsFolder, sepName (5), javax.swing.JSeparator.class);
            Utilities2.removeAction (SettingsAction.class, projectsFolder);

            // remove the projects menu folder if it is empty
            if (projectsFolder.getChildren ().length == 0) {
                try {
                    projectsFolder.delete ();
                } catch (java.io.IOException e) {

                    // ignore failure to delete the folder
                }
            }

            DataFolder  toolsActionPool =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (),
                                   "Tools"); // NOI18N
            Utilities2.removeAction (OptionsAction.class, toolsActionPool);

            if ((clazz = Class.forName("org.netbeans.core.actions.OptionsAction")) != null) { // NOI18N
                Utilities2.createAction (clazz, toolsActionPool);
            }

            if ((clazz = Class.forName("org.netbeans.core.actions.SettingsAction")) != null) { // NOI18N
                Utilities2.createAction (clazz, toolsActionPool);
            }


            DataFolder  toolsFolder =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().menus (),
                                   "Tools");     // NOI18N

            Utilities2.removeAction (OptionsAction.class, toolsFolder);

            if ((clazz = Class.forName("org.netbeans.core.actions.OptionsAction")) != null) { // NOI18N
                Utilities2.createAction (clazz, toolsFolder, "ConfigureShortcutsAction", true, false, false, false); // NOI18N
            }

            if ((clazz = Class.forName("org.netbeans.core.actions.SettingsAction")) != null) { // NOI18N
                Utilities2.createAction (clazz, toolsFolder, "OptionsAction", true, false, false, false); // NOI18N
            }

            // add to system actions pool
            DataFolder  systemActionPool =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (),
                                   "System"); // NOI18N

            DataFolder  fileFolder =
                DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().menus (),
                                   "File");     // NOI18N

            if ((clazz = Class.forName("org.netbeans.core.actions.SaveSettingsAction")) != null) { // NOI18N
                Utilities2.createAction (clazz, fileFolder, "SaveAllAction", true, false, true, false); // NOI18N
                Utilities2.createAction (clazz, systemActionPool);
            }

        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) { // NOI18N
                e.printStackTrace ();
            }

            // ignore failure to uninstall
        }
    }

    private void createDefaultProject() {
        DataFolder projects = org.openide.TopManager.getDefault().getPlaces().folders().projects();
        FileObject folder = projects.getPrimaryFile();
        FileObject fo = folder.getFileObject(DEFAULT_PROJECT_NAME);
        ProjectDataObject pdo = null;
        try {
            if (fo != null) {
                pdo = (ProjectDataObject) DataObject.find(fo);
            } else {
                pdo = ProjectDataObject.createProject(projects, DEFAULT_PROJECT_NAME);
                defaultProjectCreated = true;
            }

        } catch (Exception ex) {
            // [PENDING] hmmm, it's bad and I have no time to do it ...
        }
        if (pdo != null) {
            saveLast(pdo.support);
        }
    }

    static boolean lastExists() {
        FileSystem fs = org.openide.TopManager.getDefault().getRepository().getDefaultFileSystem();
        FileObject system = fs.getRoot();
        FileObject last = system.getFileObject(PROJECT_LAST_NAME, PROJECT_LAST_EXT);
        return last != null;
    }

    static void saveLast(ProjectCookie project) {
        FileLock lock = null;
        ObjectOutputStream oos = null;
        try {
            FileSystem fs = org.openide.TopManager.getDefault().getRepository().getDefaultFileSystem();
            FileObject system = fs.getRoot();
            FileObject last = system.getFileObject(PROJECT_LAST_NAME, PROJECT_LAST_EXT);


            if (last == null) {
                last = system.createData(PROJECT_LAST_NAME, PROJECT_LAST_EXT);
                lock = last.lock();
                oos = new ObjectOutputStream(last.getOutputStream(lock));
                oos.writeObject(project);
            }
        } catch (IOException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
        } finally {
            if (lock != null) lock.releaseLock();
            try {
                if (oos != null) oos.close();
            } catch (IOException ex) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            }
        }
    }

    static void removeLast() {
        FileLock lock = null;
        try {
            FileSystem fs = org.openide.TopManager.getDefault().getRepository().getDefaultFileSystem();
            FileObject system = fs.getRoot();
            FileObject last = system.getFileObject(PROJECT_LAST_NAME, PROJECT_LAST_EXT);
            if (last != null) {
                lock = last.lock();
                last.delete(lock);
            }
        } catch (IOException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
        } finally {
            if (lock != null) lock.releaseLock();
        }
    }

    /** */
    private static class WMSaver implements Runnable {
        public void run() {
            try {
                PSupport.getGlobalProjectContent().storeWindowManager();
            } catch (IOException ex) {
                //
            }
        }
    }

    // Implementation of java.io.Externalizable ------------------

    public void readExternal(final java.io.ObjectInput objectInput )
    throws java.io.IOException, java.lang.ClassNotFoundException {

        super.readExternal( objectInput );

        numberOfStarts = objectInput.readInt();

    }

    public void writeExternal(final java.io.ObjectOutput objectOutput )
    throws java.io.IOException {
        super.writeExternal( objectOutput );

        objectOutput.writeInt( numberOfStarts );
    }
}

/*
 * Log
 *  13   Gandalf   1.12        2/10/00  Martin Ryzl     DebugProjectAction added
 *  12   Gandalf   1.11        2/8/00   Martin Ryzl     javadoc hack
 *  11   Gandalf   1.10        2/4/00   Martin Ryzl     #5564 fix
 *  10   Gandalf   1.9         1/19/00  Martin Ryzl     
 *  9    Gandalf   1.8         1/19/00  Jesse Glick     Localized filenames.
 *  8    Gandalf   1.7         1/17/00  Martin Ryzl     debug messages removed
 *  7    Gandalf   1.6         1/17/00  Martin Ryzl     
 *  6    Gandalf   1.5         1/16/00  Jesse Glick     Actions pool.
 *  5    Gandalf   1.4         1/15/00  Jesse Glick     Actions pool.
 *  4    Gandalf   1.3         1/15/00  Martin Ryzl     Projects menu folder 
 *       renamed to Project
 *  3    Gandalf   1.2         1/13/00  Martin Ryzl     heavy localization
 *  2    Gandalf   1.1         1/11/00  Martin Ryzl     SaveSettingsAction is 
 *       now removed/added on installation/deinstallation
 *  1    Gandalf   1.0         1/10/00  Martin Ryzl     
 * $
 */

