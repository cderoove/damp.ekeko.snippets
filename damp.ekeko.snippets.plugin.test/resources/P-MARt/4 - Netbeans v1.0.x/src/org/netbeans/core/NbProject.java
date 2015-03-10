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

package org.netbeans.core;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.MessageFormat;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;
import org.openide.util.io.*;
import org.openide.nodes.*;

import org.netbeans.core.windows.WindowManagerImpl;

/** Default implementation of a project.
*
* @author Jaroslav Tulach
*/
class NbProject extends Object implements ProjectCookie {
    /** serial version UID */
    static final long serialVersionUID=8726895988034807614L;

    //
    // implementation of the default project
    //

    // PENDING:
    // must store option pool, filesystems pool, workspaces and loader pool
    //   this settings can be put into the serialized state of this object!
    //


    public static byte SAVE_CONTROL_PANEL = 1;
    public static byte SAVE_REPOSITORY = 2;
    public static byte SAVE_WORKSPACES = 4;
    public static byte SAVE_SERVICES = 8;

    // ----------------------------------------------------------------------------------------
    // Default simple ProjectCookie implementation

    /** Opens the project by loading its settings to the IDE.
    *
    * @exception IOException if an error occured during opening the project
    */
    public void projectOpen () throws IOException {
        FileObject projectFile = getSerializedProjectFile (false);
        if (projectFile == null) return; // project does not exist

        ObjectInputStream ois = null;
        try {
            ois = new NbObjectInputStream (projectFile.getInputStream ());

            // A. load info about what is stored in the project
            byte loadFlags = ois.readByte ();

            // B. load all items that are to be loaded
            // 1. Repository
            try {
                if ((loadFlags & SAVE_REPOSITORY) != 0) NbObjectInputStream.readSafely (ois);
            } catch (SafeException exc) {
                if (System.getProperty ("netbeans.debug.exceptions") != null)
                    exc.getException().printStackTrace();
            }

            // 2. Control panel
            try {
                if ((loadFlags & SAVE_CONTROL_PANEL) != 0) NbObjectInputStream.readSafely (ois);
            } catch (SafeException exc) {
                if (System.getProperty ("netbeans.debug.exceptions") != null)
                    exc.getException().printStackTrace();
            }

            // 3. Workspaces
            WindowManagerImpl wmImpl = (WindowManagerImpl)TopManager.getDefault().getWindowManager();
            if (((loadFlags & SAVE_WORKSPACES) != 0) && (System.getProperty ("netbeans.workspaces.noload") == null)) {
                try {
                    NbObjectInputStream.readSafely (ois);
                } catch (SafeException exc) {
                    TopManager.getDefault().notify(
                        new NotifyDescriptor.Exception(exc.getException(),
                                                       NbBundle.getBundle(NbProject.class).getString(
                                                           "EXC_WorkspaceLoadFail")) // NOI18N
                    );
                    if (System.getProperty ("netbeans.debug.exceptions") != null)
                        exc.getException().printStackTrace();
                    // create default
                    if (!wmImpl.isCreated())
                        WindowManagerImpl.createFromScratch();
                }
            } else {
                if ((loadFlags & SAVE_WORKSPACES) != 0) {
                    NbObjectInputStream.skipSafely (ois); // when the workspaces are saved but should not be loaded, skip it
                }
                if (!wmImpl.isCreated())
                    WindowManagerImpl.createFromScratch();
            }

            // 4. Executors
            try {
                if ((loadFlags & SAVE_SERVICES) != 0) NbObjectInputStream.readSafely (ois);
            } catch (SafeException exc) {
                if (System.getProperty ("netbeans.debug.exceptions") != null)
                    exc.getException().printStackTrace();
            }
        }
        finally {
            if (ois != null)
                ois.close();
        }
    }

    /** Save the project. This method instructs the project to
    * store the current settings of the IDE (that could be modified during
    * work in the project) to be restored on the next open of the project.
    * It is up to the project to decide which settings to store and how.
    *
    * @exception IOException if an error occurs during saving
    */
    public void projectSave () throws IOException {
        byte saveFlags = 0;
        //    if (ideSettings.getStoreRepository ())
        saveFlags += SAVE_REPOSITORY;

        //    if (ideSettings.getStoreControlPanel ())
        saveFlags += SAVE_CONTROL_PANEL;

        //    if (ideSettings.getStoreWorkspaces ())
        if (System.getProperty("netbeans.workspaces.nosave") == null) {
            saveFlags += SAVE_WORKSPACES;
        }

        saveFlags += SAVE_SERVICES;

        FileObject projectFile = getSerializedProjectFile (true);
        FileLock lock = null;
        ObjectOutputStream oos = null;
        try {
            lock = projectFile.lock ();
            oos = new NbObjectOutputStream (projectFile.getOutputStream (lock));

            // 1. store info about what will be stored
            oos.writeByte (saveFlags);
            // 2. store all items that are to be stored
            // Repository
            try {
                if ((saveFlags & SAVE_REPOSITORY) != 0) {
                    NbObjectOutputStream.writeSafely (oos,
                                                      NbTopManager.getDefaultRepository ());
                }
            } catch (SafeException exc) {
                // notify if needed, but continue with saving
                if (System.getProperty ("netbeans.debug.exceptions") != null)
                    exc.getException().printStackTrace();
            }
            // Control panel
            try {
                if ((saveFlags & SAVE_CONTROL_PANEL) != 0) {
                    NbObjectOutputStream.writeSafely (oos,
                                                      TopManager.getDefault ().getControlPanel ());
                }
            } catch (SafeException exc) {
                // notify if needed, but continue with saving
                if (System.getProperty ("netbeans.debug.exceptions") != null)
                    exc.getException().printStackTrace();
            }
            // Workspaces
            try {
                if ((saveFlags & SAVE_WORKSPACES) != 0) {
                    NbObjectOutputStream.writeSafely (oos,
                                                      TopManager.getDefault ().getWindowManager ());
                }
            } catch (SafeException exc) {
                // notify if needed, but continue with saving
                if (System.getProperty ("netbeans.debug.exceptions") != null)
                    exc.getException().printStackTrace();
            }
            // Executors
            try {
                if ((saveFlags & SAVE_SERVICES) != 0) {
                    NbObjectOutputStream.writeSafely (oos,
                                                      TopManager.getDefault ().getServices ());
                }
            } catch (SafeException exc) {
                // notify if needed, but continue with saving
                if (System.getProperty ("netbeans.debug.exceptions") != null)
                    exc.getException().printStackTrace();
            }
        } finally {
            if (oos != null)  oos.close ();
            if (lock != null) lock.releaseLock ();
        }
    }

    /** Close the project. This method instructs the project that another project
     * is becoming the active project and that the project can drop allocated 
     * resources.
     *
     * @exception IOException if an error occurs during saving
     */
    public void projectClose () throws IOException {
    }

    public synchronized Node projectDesktop () {
        return null;
    }

    /** name and extension for basic serialized project file */
    private static final String SERIALIZED_PROJECT_NAME = "project"; // NOI18N
    private static final String SERIALIZED_PROJECT_EXT = "basic"; // NOI18N

    /** Returns file object (file) where the current project is serialized.
    * @param create true if the file should be created
    * @return the file object
    */
    private static FileObject getSerializedProjectFile (boolean create) throws java.io.IOException {
        org.openide.filesystems.FileSystem def =
            TopManager.getDefault().getRepository().getDefaultFileSystem ();

        FileObject fo = def.find ("", SERIALIZED_PROJECT_NAME, SERIALIZED_PROJECT_EXT); // NOI18N
        if (fo == null && create) {
            fo = def.getRoot ().createData (SERIALIZED_PROJECT_NAME, SERIALIZED_PROJECT_EXT);
        }
        return fo;
    }

}

/*
* Log
*  11   Gandalf   1.10        1/14/00  Martin Ryzl     ProjectCookie.projectClose()
*        added
*  10   Gandalf   1.9         1/13/00  Jaroslav Tulach I18N
*  9    Gandalf   1.8         10/29/99 Jaroslav Tulach MultiFileSystem + 
*       FileStatusEvent
*  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         10/8/99  Petr Hamernik   closing input stream bug 
*       fix
*  6    Gandalf   1.5         9/10/99  Jaroslav Tulach Services API.
*  5    Gandalf   1.4         8/16/99  David Simonek   reapired projectSave() to
*       hold on posiible serialization problems
*  4    Gandalf   1.3         8/14/99  David Simonek   reporting problems during
*       load / store of projects added
*  3    Gandalf   1.2         8/3/99   Ian Formanek    netbeans.workspaces.noload/nosave
*       
*  2    Gandalf   1.1         8/2/99   Jaroslav Tulach 
*  1    Gandalf   1.0         8/1/99   Jaroslav Tulach 
* $
*/