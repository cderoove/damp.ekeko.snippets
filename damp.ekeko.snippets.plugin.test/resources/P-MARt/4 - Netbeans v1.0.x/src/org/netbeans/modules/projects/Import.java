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


import java.util.Vector;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import org.openide.filesystems.*;
import org.openide.TopManager;
import java.io.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataFolder;
import org.netbeans.modules.java.JavaDataObject;
import org.openide.util.*;
import org.openide.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

/**
 * Main class which performe import
 * @author  pzajac
 * @version
 */
public class Import extends Object {

    public static final boolean debug = false ;

    /**
     * Copy  project files into mounded
     * filesystem for true or only mount filesystem with source.
     * It it false at deault.
     */
    protected boolean             copy = false;

    /**
     * Panel that swoh information project
     */
    protected ImportPanel2        panel = null;

    /**
     * Filesystem where it will copy project files
     */
    protected LocalFileSystem     fileSystem = null;

    /**
     * Panel that allow insert choose mounting of jar and zip system files.
     */
    protected ImportClassPathPane importClassPathPane = null;

    /**
     * This dialog show filesystems a files and customize project import.
     */
    protected Dialog              dialog = null;

    /**
     * Information and data about imported file.
     */
    protected ImportProject       importProject = null;

    static protected File                currentDirectory = null;


    public static boolean  isImport = false ;
    /**
     * Creates new Import
     */
    public Import () {}

    /**
     * Testatic if filesystem is mounted
     * @param fileSystem if is in repository allready
     * @return return mounted filesystem or null if isn't mounted
     */
    org.openide.filesystems.FileSystem mounted (org.openide.filesystems.FileSystem fileSystem) {
        Repository                          rep =
            TopManager.getDefault ().getRepository ();
        java.util.Enumeration               fileSystems = rep.getFileSystems ();

        File                                root = null;
        org.openide.filesystems.FileSystem  fileSystem2;

        if (fileSystem instanceof org.openide.filesystems.JarFileSystem) {
            root = ((JarFileSystem) fileSystem).getJarFile ();
        }

        if (fileSystem instanceof LocalFileSystem) {
            root = ((LocalFileSystem) fileSystem).getRootDirectory ();
        }

        while (fileSystems.hasMoreElements ()) {
            fileSystem2 =
                (org.openide.filesystems.FileSystem) fileSystems.nextElement ();

            if (fileSystem2 instanceof LocalFileSystem) {
                if (root.equals (((LocalFileSystem) fileSystem2).getRootDirectory ())) {
                    return fileSystem2;
                }
            } else if (fileSystem2 instanceof JarFileSystem) {
                if (root.equals (((JarFileSystem) fileSystem2).getJarFile ())) {
                    return fileSystem2;
                }
            }
        }

        return null;
    }

    /**
     * it import files into default project
     * @param information about project
     */
    public void importToDefaultProject(ImportProject prj) throws Exception {
        File[]  files = prj.getFiles ();
        File[]  filesystems = prj.getFileSystems ();

        try {
            mountFilesystems (filesystems);
            mountJars (prj.getJarsAndZips ());
        } catch (Exception e) {
            TopManager.getDefault ().setStatusText (e.getMessage ());
        }

        org.openide.nodes.Node  n =
            TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
        ProjectDataObject       project =
            (ProjectDataObject) n.getCookie (ProjectDataObject.class);
        DataObject              dataObject = null;

        // add files
        for (int i = 0; i < files.length; i++) {
            try {
                dataObject = getDataObject (files[i], filesystems);

                if (dataObject == null) {
                    TopManager.getDefault ().setStatusText (getLocalizedString ("MSG_ImportObjectNotAdded", // NOI18N
                                                            files[i].getAbsolutePath()));

                    continue;
                }
                project.add (dataObject);
            } catch (Exception e) {
                if (Import.debug ) {
                    e.printStackTrace ();
                }
                TopManager.getDefault ().setStatusText (e.getMessage ());
            }
        }

        // Set main class
        //
        String  mainClass = importProject.getMainClass ();

        if (mainClass != null) {
            //      project.setMainClass (mainClass);
            // [TODO] - implement getMainClass()
        }

    }

    /**
     * Mount array of jars into repozitory
     * param jars array of jar which will be mounted
     */
    protected void mountJars (File[] jars) throws Exception {
        Repository    repository = TopManager.getDefault ().getRepository ();
        JarFileSystem fileSystem = new JarFileSystem ();

        for (int i = 0; i < jars.length; i++) {
            try {
                fileSystem = new JarFileSystem ();
                if (jars[i].isDirectory ()) {
                    continue;
                }
                fileSystem.setJarFile (jars[i]);

                if (mounted (fileSystem) != null) {
                    continue;
                }

                repository.addFileSystem (fileSystem);
                fileSystem.setHidden (true);
            } catch (Exception e) {
                throw new Exception (getLocalizedString ("MSG_ImportFilesystemNotAdded", // NOI18N
                                     jars[i].getAbsolutePath ()));
            }
        }

    }

    /**
     * Mount LocalFilesystem into repozitory
     * @param filesystems for mounting
     */
    protected void mountFilesystems (File[] filesystems) throws Exception {
        Repository      repository = TopManager.getDefault ().getRepository ();
        LocalFileSystem fileSystem = null;

        for (int i = 0; i < filesystems.length; i++) {
            try {
                if (filesystems[i].isDirectory () == false) {
                    continue;
                }
                fileSystem = new LocalFileSystem (new FileSystemCapability.Bean ());

                fileSystem.setRootDirectory (filesystems[i]);


                if (mounted (fileSystem) != null) {
                    continue;
                }

                repository.addFileSystem (fileSystem);
            } catch (Exception e) {
                throw new Exception (getLocalizedString ("MSG_ImportFilesystemNotAdded", // NOI18N
                                     fileSystem.getSystemName ()));
            }
        }

    }


    /**
     * get DataObject from repository and which have parent in array filesystems.
     * It must be founded FileObject for File
     * @param file input file for aquirement DataObject
     * @param filesystem filesystem where can leave file
     * @return DataObject of file or null
     */
    private DataObject getDataObject (File file,
                                      File[] filesystems) throws Exception {
        String            fpath = optimalizePath (file.getAbsolutePath ());
        String            relativeFilePath = null;
        String            dirpath = null;
        String            extension = null;
        String            name = null;
        String            packageName = null;
        int               index;
        FileObject        fileObject = null;
        DataObject        dataObject = null;
        DataInputStream   istream = null;
        DataOutputStream  ostream = null;
        java.util.Vector  folders = null;
        FileObject        destinationFolder = null;
        if (fpath == null ) {
            return null;
        }
        Repository        repository = TopManager.getDefault ().getRepository ();

        for (int i = 0; i < filesystems.length; i++) {

            dirpath = optimalizePath (filesystems[i].getAbsolutePath ());
            index = fpath.indexOf (dirpath);

            if (index == 0) {

                // file is found
                fpath = fpath.substring (dirpath.length ());
                index = fpath.lastIndexOf ("."); // NOI18N
                if (index == -1) {
                    extension = ""; // NOI18N
                } else {
                    extension = fpath.substring (index + 1);
                }

                fpath = fpath.substring (0, index);

                if (fpath.indexOf (".") != -1) { // NOI18N
                    throw new Exception (Import.getLocalizedString ("MSG_BadFormatFile", // NOI18N
                                         file.getAbsolutePath ()));
                }

                relativeFilePath = fpath;
                fpath = fpath.replace (File.separatorChar, '.');
                index = fpath.lastIndexOf ('.');
                name = fpath.substring (index + 1);
                packageName = fpath.substring (0, index);

                if (packageName == null) {
                    packageName = " "; // NOI18N
                }

                fileObject = repository.find (packageName, name, extension);

                if (fileObject != null) {

                    try {
                        dataObject = DataObject.find (fileObject);

                        if (copy) {

                            // it must be copied into fileSystem
                            if (dataObject instanceof JavaDataObject) {
                                try {
                                    org.openide.src.SourceElement source =
                                        ((JavaDataObject) dataObject).getSource ();
                                    org.openide.src.Identifier    identifier =
                                        source.getPackage ();

                                    if (identifier == null) {
                                        packageName = ""; // NOI18N
                                    } else {
                                        packageName = identifier.getFullName ();
                                    }
                                } catch (Exception ex) {
                                    // package wasn't readed in File
                                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                                }

                                // I must parse package
                            }

                            // if package doesn't exist in FileSystem (filesystem), it will create  it
                            destinationFolder = findFolder (packageName);

                            if (destinationFolder != null) {
                                try {
                                    fileObject = FileUtil.copyFile (fileObject,
                                                                    destinationFolder, name);
                                } catch (Exception e) {
                                    TopManager.getDefault ().setStatusText (getLocalizedString ("MSG_ImportFileExist", // NOI18N
                                                                            destinationFolder.getName ()));
                                }

                                dataObject = DataObject.find (fileObject);
                            }

                            // We must read find new FileObject
                            //
                        }

                        return dataObject;

                    } catch (DataObjectNotFoundException e) {
                        e.printStackTrace ();
                    }
                } else {}
            }
        }

        return null;
    }

    /**
     * Extract all parent folders names into @return Vector of String
     */
    protected java.util.Vector parsePackage (String packageName) {
        java.util.Vector          v = new java.util.Vector ();
        java.util.StringTokenizer tokenizer =
            new java.util.StringTokenizer (packageName, "."); // NOI18N

        try {
            while (true) {
                v.addElement (tokenizer.nextToken ());
            }
        } catch (java.util.NoSuchElementException e) {}

        return v;
    }

    /**
     * It found or try create folder in fileSystem
     * @param packageName  input package name
     * @return null if folder wasn't created or folder of package
     * 
     */
    protected FileObject findFolder (String packageName) throws Exception {

        FileObject  parentFileObject = fileSystem.getRoot ();

        if (packageName.trim ().length () == 0) {

            // System.out.println("null package name length"); // NOI18N
            return fileSystem.getRoot ();
        }

        int                       index = 0;
        FileObject                fileObject = null;
        java.util.StringTokenizer tokenizer =
            new java.util.StringTokenizer (packageName, "."); // NOI18N
        String                    folder = ""; // NOI18N
        String                    newPackage = ""; // NOI18N

        try {

            // find or create subfolders
            //
            while (true) {
                folder = tokenizer.nextToken ();

                if (newPackage == "") { // NOI18N
                    newPackage = folder;
                } else {
                    newPackage = newPackage + "." + folder; // NOI18N
                }

                fileObject = fileSystem.find (newPackage, null, null);

                if (fileObject == null) {

                    try {
                        parentFileObject = parentFileObject.createFolder (folder);
                    } catch (java.io.IOException e) {

                        return null;
                    }
                } else {
                    parentFileObject = fileObject;
                }
            }
        } catch (java.util.NoSuchElementException e) {}

        return parentFileObject;
    }

    /**
     * It show import dialog and add files into fileSystem.
     * @param prj contain project environment (files, classPath, fileSystems)
     */
    public void go (ImportProject prj) {
        importProject = prj;

        File[]  files = prj.getFiles ();

        panel = new ImportPanel2 (prj);

        DialogDescriptor  desc = new DialogDescriptor (panel,
                                 Import.getLocalizedString ("CTL_ImportMain"), false, // NOI18N
                                 new ActionListener () {
                                     public void actionPerformed (ActionEvent ev) {
                                         dialog.setVisible (false);

                                         // dialog = null;
                                         if (ev.getSource ().toString ().equals ("0")) { // NOI18N

                                             // Ok button
                                             //
                                             dialog.dispose ();
                                             okPressed ();

                                         }

                                         // dialog.dispose();

                                     }

                                 });

        dialog = TopManager.getDefault ().createDialog (desc);

        dialog.show ();

    }

    /**
     * Import into project (Ok button pressesd)
     * @return true when it is succefully
     */
    protected void okPressed () {
        try {
            if (panel.isCreateNewProject ()) {

                // I  create new project
                Import.isImport = true;
                NewProjectAction  newAction = new NewProjectAction ();
                if (newAction.perform () == false  ) {
                    // System.out.println("new action performe = false "); // NOI18N
                    Import.isImport = false ;
                }

            } else {
                importAll ();
            }
        } catch (Exception e) {
            Import.isImport = false;
            TopManager.getDefault ().setStatusText (e.getMessage ());
        }
        // System.out.println("Import.isImport = " + Import.isImport ); // NOI18N
    }

    public void importAll () {
        // import or copy
        try {
            copy = panel.isCopy ();
            fileSystem = panel.getSelectedFileSystem ();

            importToDefaultProject (importProject);

            // insert system classpath
            File[]  files = importProject.getSystemJarsAndZips ();

            if (files.length > 0) {
                importClassPathPane = new ImportClassPathPane (files);

                // show ImportClasspathPane

                DialogDescriptor  desc = new DialogDescriptor (importClassPathPane,
                                         Import.getLocalizedString ("CTL_ImportClasspathMain"), false, // NOI18N
                                         new ActionListener () {
                                             public void actionPerformed (ActionEvent ev) {
                                                 dialog.setVisible (false);

                                                 // dialog = null;
                                                 if (ev.getSource ().toString ().equals ("0")) { // NOI18N

                                                     // Ok button
                                                     //
                                                     dialog.dispose ();
                                                     okPressedAddClassPath ();

                                                 }
                                             }

                                         });

                dialog = TopManager.getDefault ().createDialog (desc);

                dialog.show ();
            }

        } catch (Exception e) {
            TopManager.getDefault ().setStatusText (e.getMessage ());
        }
        Import.isImport = false ;
    }

    /**
     * add system classpath
     */
    protected void okPressedAddClassPath () {
        try {
            mountJars (importClassPathPane.getOutputFiles ());
        } catch (Exception e) {
            TopManager.getDefault ().setStatusText (e.getMessage ());
        }
    }

    /**
     * imput filepath , output path is without /../
     * @param path full path name
     * @return optimalized pathname or null if it cannot exist
     */

    public static String optimalizePath (String path) {
        String                    sep =
            String.valueOf (java.io.File.separatorChar);
        String                    strng = null;
        Vector                    vec = new Vector ();


        path = path.replace ('/','\\');
        //   System.out.println(path);
        File f = new File ("paths"); // NOI18N
        PrintStream ps = null ;
        try {
            ps = new PrintStream (new FileOutputStream (f ));
        } catch (java.io.IOException e) {
            e.printStackTrace ();
        }

        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer (path, "\\"); // NOI18N

        try {
            while (true) {
                strng = tokenizer.nextToken ().trim ();

                // System.out.println(strng);

                if (strng.equals ("..")) { // NOI18N
                    if (vec.size () > 0) {
                        int jj = 0;

                        vec.removeElementAt (vec.size () - 1);


                    } else {
                        return null;
                    }
                } else {
                    vec.addElement (strng);
                }
            }
        } catch (java.util.NoSuchElementException e) {}
        ps.close ();
        strng = ""; // NOI18N

        if (vec.size () > 0) {
            for (int i = 0; i < vec.size (); i++) {
                if (strng.length () == 0) {
                    strng = (String) vec.elementAt (i);

                    continue;
                }

                strng = strng + sep + (String) vec.elementAt (i);

            }
        }
        //  System.out.println(strng);
        if (File.separator.equals ("/")) { // NOI18N
            strng = strng.replace ('\\','/');
            if (strng.charAt(0) != '/') {
                strng = "/" + strng; // NOI18N
            }
            strng = winFileToUnix (strng);
        }
        //      System.out.println(strng);
        return strng;
    }

    /**
     * File chooser for import
     * @return Importing project
     */
    public ImportProject chooseImportProject() {
        JFileChooser  chooser = new JFileChooser ();
        int           i;
        ProjectInfo   prj = null;

        Vector        vec = ProjectInfo.getProjectsInformations ();

        for (i = 0; i < vec.size (); i++) {
            chooser.addChoosableFileFilter (((ProjectInfo) vec.elementAt (i)).getFileFilter ());
        }

        chooser.setDialogTitle (Import.getLocalizedString ("CTL_ImportFileMain")); // NOI18N
        if (currentDirectory != null) {
            chooser.setCurrentDirectory (currentDirectory);
        }
        if (chooser.showDialog (TopManager.getDefault ().getWindowManager ().getMainWindow (),
                                Import.getLocalizedString ("CTL_ImportFileOk")) // NOI18N
                == JFileChooser.APPROVE_OPTION) {
            File  f = chooser.getSelectedFile ();

            currentDirectory = chooser.getCurrentDirectory ();
            if (Import.debug) {
                if (currentDirectory == null) {
                    System.out.println("current directory == null");   // NOI18N
                } else {
                    System.out.println("current directory isn't null");  // NOI18N
                }
            }

            if ((f != null) && (f.isFile ())) {
                String  fname = f.getName ().trim ();

                try {
                    return ProjectInfo.getImportProject (f);
                } catch (Exception e) {
                    e.printStackTrace ();
                }
            }

        }
        if (Import.debug) {
            System.out.println("Import:671 null ProjectImport"); // NOI18N
        }
        return null;
    }

    /**
     * Getter for bundle string.
     * @param s input string
     */

    public static String getLocalizedString (String s) {
        return NbBundle.getBundle (Import.class).getString (s);
    }

    /**
     * Getter for bundle string.
     * @param s bundle name
     * @param parameter fo bundle
     * @return translated string
     */
    static String getLocalizedString (String s, Object val) {
        return java.text.MessageFormat.format (getLocalizedString (s),
                                               new Object[] {
                                                   val
                                               });
    }

    /**
     * Convert file ingnore case sensitive to case sensitive 
     * @param f input file 
     * @return converted file if exists else null if not exist 
     */
    public static String winFileToUnix (String fullPath) {

        //String fullPath = f.getAbsolutePath();
        String path = "/"; // NOI18N
        File file = new File (path);
        String name = null ;
        String strFiles []  = null ;
        int i = 0 ;
        StringTokenizer tokenizer = new StringTokenizer (fullPath,"/\\"); // NOI18N
        try {
            String token = tokenizer.nextToken ();
            while (token != null ) {
                file = new File (path);
                if (token.equals("..") || token == "." ) { // NOI18N
                    if (path.equals ("/") ) { // NOI18N
                        path = path +  token;
                    } else {
                        path = path + "/" + token; // NOI18N
                    }
                } else if (token.equals ("") == false ) { // NOI18N
                    strFiles  = file.list();
                    for ( i = 0 ; i < strFiles.length ; i ++ ) {
                        name = strFiles[i];
                        if (name.compareToIgnoreCase(token) == 0 ) {
                            if (path.equals ("/") ) { // NOI18N
                                path = path +  name ;
                            } else {
                                path = path + "/" + name; // NOI18N
                            }
                            break;
                        }
                    }
                    if (i == strFiles.length ) {
                        return null;
                    }
                }
                token =  tokenizer.nextToken ();
            }
        } catch (java.util.NoSuchElementException e) {}
        return path;
    }

    public static void main (String arg [] ) {
        // if (arg.length ==1 ) {
        String s = "/home/pzajac/import/ide-samples/VCafe2.5/Symantec/VMApps/JavaPad/JAVAPAD.java"; // NOI18N
        //    File f = new File ();
        System.out.println(winFileToUnix(s));
        //  } else {
        //  System.out.println (" Wrong count of arguments .") ; // NOI18N
        // }
    }
}
/*
 * Log
 *  6    Gandalf   1.5         2/4/00   Martin Ryzl     import fix  
 *  5    Gandalf   1.4         1/20/00  Petr Zajac      
 *  4    Gandalf   1.3         1/15/00  Ian Formanek    NOI18N
 *  3    Gandalf   1.2         1/13/00  Martin Ryzl     heavy localization
 *  2    Gandalf   1.1         1/9/00   Martin Ryzl     
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */
