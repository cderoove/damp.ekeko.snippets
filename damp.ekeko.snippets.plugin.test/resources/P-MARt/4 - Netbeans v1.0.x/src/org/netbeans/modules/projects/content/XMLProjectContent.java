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

package org.netbeans.modules.projects.content;

import java.io.*;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.*;
import org.openide.loaders.XMLDataObject;
import org.openide.options.SystemOption;
import org.openide.util.SharedClassObject;
import org.openide.util.io.NbMarshalledObject;
import org.openidex.projects.*;

import org.openidex.projects.*;

import org.xml.sax.*;



/**
 *
 * @author  mryzl
 */

public class XMLProjectContent extends AbstractProjectContent {

    /** Extension for project data files. */
    public static final String EXT_PROJECT_DATA = "xml"; // NOI18N
    /** Extension for project data files. */
    public static final String EXT_SER = "ser"; // NOI18N
    /** ControlPanel file name. */
    public static final String NAME_CONTROL_PANEL = "cpanel"; // NOI18N
    /** Modules file name. */
    public static final String NAME_MODULES = "modules"; // NOI18N
    /** Loaders file name. */
    public static final String NAME_LOADERS = "loaders"; // NOI18N
    /** Repository file name. */
    public static final String NAME_REPOSITORY = "repository"; // NOI18N
    /** Services file name. */
    public static final String NAME_SERVICES = "services"; // NOI18N
    /** Services file name. */
    public static final String NAME_WORKSPACE = "workspace"; // NOI18N

    private transient XMLSettingsSet cpanel, modules, loaders;
    private transient XMLDiffSet repository, services;

    FileObject folder;

    /** Creates new XMLProjectContent.
    * @param folder - folder where project files are stored
    * @param superProject - superProject or null for main project
    */
    public XMLProjectContent(FileObject folder, ProjectContent superProject) {
        this.folder = folder;
        this.superProject = superProject;
    }

    /** Get control panel.
    *
    * @return control panel
    */
    public SettingsSet getControlPanel(boolean force) throws IOException {
        if (cpanel == null) {
            try {
                cpanel = new XMLSettingsSet(folder, NAME_CONTROL_PANEL, EXT_PROJECT_DATA, true);
                cpanel.load();
            } catch (IOException ex) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                if (!force) throw ex;
            }
        }
        return cpanel;
    }

    /** Get modules.
    *
    * @return modules
    */
    public SettingsSet getModules(boolean force) throws IOException {
        return null;
    }

    /** Get loaders pool.
    *
    * @return loaders
    */
    public SettingsSet getLoaderPool(boolean force) throws IOException {
        if (loaders == null) {
            // create SettingsSet and load loaders
            try {
                loaders = new XMLSettingsSet(folder, NAME_LOADERS, EXT_PROJECT_DATA, true);
                loaders.load();
            } catch (IOException ex) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                if (!force) throw ex;
            }
        }
        return loaders;
    }

    /** Get repository.
    *
    * @return repository
    */
    public DiffSet getRepository(boolean force) throws IOException {
        if (repository == null) {
            try {
                repository = new XMLDiffSet(folder, NAME_REPOSITORY, EXT_PROJECT_DATA, true);
                repository.load();
            } catch (IOException ex) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                if (!force) throw ex;
            }
        }
        return repository;
    }

    /** Get services.
    *
    * @return services
    */
    public DiffSet getServices(boolean force) throws IOException {
        if (services == null) {
            try {
                services = new XMLDiffSet(folder, NAME_SERVICES, EXT_PROJECT_DATA, true);
                services.load();
            } catch (IOException ex) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace ();  // NOI18N
                if (!force) throw ex;
            }
        }
        return services;
    }

    /**
    * @return window manager or null if there is no one.
    */
    public NbMarshalledObject getWindowManager() throws IOException {
        return loadWindowManager();
    }

    /**
    * @return window manager or null if there is no one.
    */
    public void storeWindowManager() throws java.io.IOException {
        storeWindowManager(new NbMarshalledObject(org.openide.TopManager.getDefault().getWindowManager()));
    }

    /**
    */
    protected void storeWindowManager(NbMarshalledObject windowManager) throws java.io.IOException {
        FileObject fo = folder.getFileObject(NAME_WORKSPACE, EXT_SER);
        FileLock lock = null;
        if (fo == null) {
            fo = folder.createData(NAME_WORKSPACE, EXT_SER);
        }
        ObjectOutputStream oos = null;
        try {
            lock = fo.lock();
            oos = new ObjectOutputStream(fo.getOutputStream(lock));
            oos.writeObject(windowManager);
            oos.close();
        } finally {
            if (lock != null) lock.releaseLock();
            if (oos != null) oos.close();
        }
    }

    /**
    */
    protected NbMarshalledObject loadWindowManager() throws java.io.IOException {
        NbMarshalledObject nmo = null;
        FileObject fo = folder.getFileObject(NAME_WORKSPACE, EXT_SER);
        if (fo != null) {
            ObjectInputStream ois = null;
            try {
                ois = new org.openide.util.io.NbObjectInputStream(fo.getInputStream());
                nmo = (NbMarshalledObject) ois.readObject();
            } catch (ClassNotFoundException ex) {
                // hmmm
            } finally {
                if (ois != null) ois.close();
            }
        }
        return nmo;
    }

    /** Store the ProjectContent.
     */
    public void store() throws java.io.IOException {
    }

    /** Create a new subproject.
    */
    public ProjectContent createProject() throws IOException {
        XMLProjectContent pc = new XMLProjectContent(createFolder(), this);
        addProject(pc);
        return pc;
    }

    protected FileObject createFolder() throws IOException {
        // find first unused file name ProjectX
        for(int i = 0;; i++) {
            String name = "Project" + i; // NOI18N
            if (folder.getFileObject(name) == null) {
                return folder.createFolder(name);
            }
        }
    }

    /** Debug!
    */
    public static void main(String[] args) throws Exception {
    }
}

/*
* Log
*  7    Gandalf   1.6         4/14/00  Ales Novak      repackaging
*  6    Gandalf   1.5         2/4/00   Martin Ryzl     correct handling of wrong
*       XML files
*  5    Gandalf   1.4         1/17/00  Martin Ryzl     
*  4    Gandalf   1.3         1/13/00  Martin Ryzl     heavy localization
*  3    Gandalf   1.2         1/7/00   Martin Ryzl     
*  2    Gandalf   1.1         1/4/00   Martin Ryzl     
*  1    Gandalf   1.0         12/22/99 Martin Ryzl     
* $ 
*/ 
