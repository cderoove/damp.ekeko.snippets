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

/** Holder for current project. Also provides the default one.
*
* @author Jaroslav Tulach
*/
class NbProjectOperation extends Object {
    /** current project */
    private static ProjectCookie project;

    /** node to be used to mark that the project does not have desktop */
    private static final Node NO_DESKTOP = new DesktopNode ();

    /** desktop node for current project */
    private static Node projectDesktop = NO_DESKTOP;

    // ----------------------------------------------------------------------------------------
    // Default simple ProjectCookie persistence

    /** Save the basic project.
    */
    static void saveBasicProject () throws IOException {
        project.projectSave ();
    }

    // ----------------------------------------------------------------------------------------
    // Static methods for manipulation with projects

    static void storeLastProject () {
        //    if (new IDESettings ().getConfirmSaveOnExit ()) {
        //      if (TopManager.notify (...)
        //    }
        try {
            project.projectSave ();
            NbProjectOperation.saveLastProjectUsed ();
        } catch (IOException exc) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                if (exc instanceof SafeException)
                    ((SafeException)exc).getException().printStackTrace();
                else
                    exc.printStackTrace();
            }
        }
    }


    static void saveLastProjectUsed () throws IOException {
        FileObject nameFile = getLastProjectFile (true);
        FileLock lock = nameFile.lock ();
        try {
            ObjectOutputStream oos = new NbObjectOutputStream (nameFile.getOutputStream (lock));
            try {
                oos.writeObject (project);
            } finally {
                oos.close ();
            }
        } finally {
            lock.releaseLock ();
        }
    }

    /** Getter for current project.
    * @return the project
    */
    public static ProjectCookie getProject () {
        return project;
    }

    /** Getter for the project desktop node.
    */
    public static Node getProjectDesktop () {
        return projectDesktop;
    }

    /** Test if the project has own desktop or not */
    public static boolean hasProjectDesktop () {
        return projectDesktop != NO_DESKTOP;
    }

    /** Setter for changing the project.
    * @param p the new project to use
    * @exception IOException if the previous project cannot be closed or new opened
    */
    public static void setProject (final ProjectCookie p) throws IOException {
        if (project == p) {
            // no change in project
            return;
        }

        if (project != null) {
            if (System.getProperty("netbeans.close") != null) {
                project.projectSave();
            } else {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation (
                                          Main.getString ("MSG_DoSavePreviousProject")
                                      );

                Object res = Main.getDefault ().notify (nd);

                if (res == NotifyDescriptor.YES_OPTION) {
                    project.projectSave ();
                }

                if (res == NotifyDescriptor.CANCEL_OPTION) {
                    throw new org.openide.util.UserCancelException ();
                }
            }
        }

        // open the project
        p.projectOpen ();
        if (project != null) project.projectClose();

        project = p;
        projectDesktop = p.projectDesktop ();
        if (projectDesktop == null) {
            projectDesktop = NO_DESKTOP;
        }

        if (!(project instanceof NbProject)) {
            saveLastProjectUsed ();
        }

        NbTopManager.change.firePropertyChange (NbTopManager.PROP_PLACES, null, null);
    }

    /** Opens last used project or creates a default one. */
    public static synchronized void openOrCreateProject () throws IOException {
        ProjectCookie project = null;
        // the file, but do not create new one
        try {
            FileObject nameFile = getLastProjectFile (false);
            if (nameFile != null) {
                ObjectInputStream ois = new NbObjectInputStream (nameFile.getInputStream ());
                try {
                    project = (ProjectCookie)ois.readObject ();
                } finally {
                    ois.close ();
                }
            }
        } catch (SafeException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null)
                ex.getException().printStackTrace();
            // [PENDING - notify error]
        } catch (IOException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
            // [PENDING - notify error]
        } catch (ClassNotFoundException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
            // [PENDING - notify error]
        } catch (Exception ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
            // [PENDING - notify error]
        }

        try {
            if (project == null) {
                project = createDefaultProject();
                setProject(project);
            } else {
                try {
                    setProject (project);
                } catch (IOException exc) {
                    if (System.getProperty("netbeans.debug.exceptions") != null)
                        exc.printStackTrace();
                    // perform default initialization if something is badly damaged
                    System.out.println(NbBundle.getBundle(NbProjectOperation.class).
                                       getString("EXC_CorruptedProject"));
                    project = createDefaultProject();
                }
            }
        } finally {
            NbProjectOperation.project = project;
        }

    }

    /** Creates and returns newly created default project */
    private static ProjectCookie createDefaultProject () {
        //System.out.println(NbBundle.getBundle(NbProjectOperation.class).
        //                   getString("MSG_DefaultProject"));
        // create empty new
        ProjectCookie project = new NbProject();
        defaultProjectInit();
        WindowManagerImpl.createFromScratch();
        return project;
    }


    //
    // default project initialization
    //

    /** The list of JAR archives located under $netbeans.home/lib, which are mounted on startup */
    private static final String[] jarsToMount = new String[] {
            };

    private static void defaultProjectInit () {
        TopManager.getDefault ().setStatusText (Main.getString("MSG_DefaultProjectInit"));

        // Initialize the Repository
        String classPath = System.getProperty("java.class.path", "");
        java.util.StringTokenizer st = new java.util.StringTokenizer(classPath,
                                       System.getProperty("path.separator", ";"),
                                       false
                                                                    );

        // *************************  only two fss ***********************
        LocalFileSystem localFS = new ExLocalFileSystem();
        String canpath = System.getProperty("netbeans.user") + File.separator + Main.getString ("FILE_Development");
        try {
            File dir = new File(canpath);
            canpath = dir.getCanonicalPath ();
        } catch (IOException e) {
            // no problem, just use the non-canonical path
        }
        try {
            localFS.setRootDirectory(new File (canpath));
            NbTopManager.getDefaultRepository().addFileSystem (localFS);
        } catch (IOException ex) {
            Object[] arg = new Object[] {canpath};
            System.out.println (new MessageFormat(Main.getString("CTL_Local_not_mounted")).format(arg));
        } catch (java.beans.PropertyVetoException ex) {
            Object[] arg = new Object[] {canpath};
            System.out.println (new MessageFormat(Main.getString("CTL_Local_not_mounted")).format(arg));
        }

        for (int i = 0; i < jarsToMount.length; i++) {
            JarFileSystem jarFS = new JarFileSystem();
            canpath = System.getProperty("netbeans.home") + File.separator + "lib" + File.separator + jarsToMount[i];
            try {
                File dir = new File(canpath);
                canpath = dir.getCanonicalPath ();
            } catch (IOException e) {
                // no problem, just use the non-canonical path
            }
            try {
                jarFS.setJarFile(new File(canpath));
                NbTopManager.getDefaultRepository().addFileSystem(jarFS);
                jarFS.setHidden(true);
            } catch (java.beans.PropertyVetoException ex) {
                Object[] arg = new Object[] {canpath};
                System.out.println (new MessageFormat(Main.getString("CTL_Jar_not_found")).format(arg));
            } catch (java.io.IOException ex) {
                Object[] arg = new Object[] {canpath};
                System.out.println (new MessageFormat(Main.getString("CTL_Jar_not_found")).format(arg));
            }
        }

    }

    /** name and extension for basic serialized project file */
    private static final String LAST_PROJECT_NAME = "project"; // NOI18N
    private static final String LAST_PROJECT_EXT = "last"; // NOI18N

    /** Returns file object (file) where the name of last opened project is stored.
    * @param create true if the file should be created
    * @return the file object
    */
    private static FileObject getLastProjectFile (boolean create) throws java.io.IOException {
        org.openide.filesystems.FileSystem def =
            TopManager.getDefault().getRepository().getDefaultFileSystem ();

        FileObject fo = def.find ("", LAST_PROJECT_NAME, LAST_PROJECT_EXT); // NOI18N
        if (fo == null && create) {
            fo = def.getRoot ().createData (LAST_PROJECT_NAME, LAST_PROJECT_EXT);
        }
        return fo;

    }

}


/*
 * Log
 *  46   Gandalf   1.45        1/19/00  Petr Nejedly    Commented out debug 
 *       messages
 *  45   Gandalf   1.44        1/18/00  Jesse Glick     Localization.
 *  44   Gandalf   1.43        1/16/00  Martin Ryzl     save project dialog 
 *       fixed
 *  43   Gandalf   1.42        1/14/00  Martin Ryzl     
 *  42   Gandalf   1.41        1/14/00  Martin Ryzl     ProjectCookie.projectClose()
 *        added
 *  41   Gandalf   1.40        1/13/00  Jaroslav Tulach I18N
 *  40   Gandalf   1.39        1/7/00   Martin Ryzl     property 
 *       netbeans.project.confirm changed to netbeans.close
 *  39   Gandalf   1.38        1/7/00   Martin Ryzl     if property 
 *       netbeans.project.confirm is set, setProject() method considers the save
 *       dialog confirmed
 *  38   Gandalf   1.37        12/21/99 David Simonek   "workspace1" problem 
 *       fixed
 *  37   Gandalf   1.36        11/25/99 Jaroslav Tulach LocalFileSystem with 
 *       backup & JarFileSystem with filesystem.attributes.
 *  36   Gandalf   1.35        10/29/99 Jaroslav Tulach MultiFileSystem + 
 *       FileStatusEvent
 *  35   Gandalf   1.34        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  34   Gandalf   1.33        9/9/99   Ian Formanek    AbsoluteLayout is not 
 *       automatically mounted
 *  33   Gandalf   1.32        8/19/99  Jaroslav Tulach Default project is not 
 *       saved on open.
 *  32   Gandalf   1.31        8/16/99  David Simonek   setting some project 
 *       event if deserialization failed hard
 *  31   Gandalf   1.30        8/14/99  David Simonek   reporting problems 
 *       during load / store of projects added
 *  30   Gandalf   1.29        8/13/99  Jaroslav Tulach New Main Explorer
 *  29   Gandalf   1.28        8/1/99   Jaroslav Tulach MainExplorer now listens
 *       to changes in root elements.
 *  28   Gandalf   1.27        7/31/99  David Simonek   getProject() method made
 *       not synchronized
 *  27   Gandalf   1.26        7/31/99  David Simonek   WS serialization turned 
 *       on by default
 *  26   Gandalf   1.25        7/28/99  David Simonek   exception dumping - just
 *       for better testing of workspace serialization....
 *  25   Gandalf   1.24        7/24/99  Ian Formanek    Fixed bug 2670 - 
 *       StreamCorruptedException thrown on first startup.
 *  24   Gandalf   1.23        7/20/99  Ian Formanek    
 *  23   Gandalf   1.22        7/16/99  Ian Formanek    Project initialization 
 *       code modified
 *  22   Gandalf   1.21        7/12/99  Jaroslav Tulach To be compilable.
 *  21   Gandalf   1.20        7/11/99  David Simonek   window system change...
 *  20   Gandalf   1.19        6/25/99  Ian Formanek    Does not stop loading of
 *       SafeExceptions
 *  19   Gandalf   1.18        6/18/99  David Simonek   default workspaces 
 *       initialization bug fixed
 *  18   Gandalf   1.17        6/17/99  David Simonek   various serialization 
 *       bugfixes
 *  17   Gandalf   1.16        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  16   Gandalf   1.15        6/3/99   Jaroslav Tulach Executors are serialized
 *       in project.
 *  15   Gandalf   1.14        5/30/99  Ian Formanek    New project saving 
 *       mechanism
 *  14   Gandalf   1.13        4/15/99  Michal Fadljevic 
 *  13   Gandalf   1.12        4/8/99   Jaroslav Tulach Bugfix 1432
 *  12   Gandalf   1.11        3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  11   Gandalf   1.10        3/4/99   Jaroslav Tulach ChangeListener in 
 *       TopManager
 *  10   Gandalf   1.9         3/4/99   Ian Formanek    WorkspaceNode->DesktopNode
 *       
 *  9    Gandalf   1.8         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  8    Gandalf   1.7         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  7    Gandalf   1.6         1/25/99  Jaroslav Tulach Saves filesystempool & 
 *       control panel in the default project
 *  6    Gandalf   1.5         1/25/99  Jaroslav Tulach Added default project, 
 *       its desktop and changed default explorer in Main.
 *  5    Gandalf   1.4         1/20/99  Jaroslav Tulach 
 *  4    Gandalf   1.3         1/13/99  David Simonek   
 *  3    Gandalf   1.2         1/6/99   Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
