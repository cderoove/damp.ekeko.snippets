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

import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import java.util.Vector;
import java.io.*;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Abstract class which performe loading project.
 * For every Developer IDE exist ProjectInfo if it's supported by NetBeans Developer
 * @author Petr Zajca
 * @version
 */
public abstract class ProjectInfo extends Object {

    /**
     * Vector of all available  projects thats can be imported
     * @associates ProjectInfo
     */
    protected static java.util.Vector projectInfos = init ();

    /**
     * Name type of project
     */
    protected String                  label;

    /**
     * Get file filter for File dialog.
     * @return filter for filename of project
     */
    public abstract javax.swing.filechooser.FileFilter getFileFilter ();

    /** dialog that show dialog for choosing system File   */
    protected java.awt.Dialog dialog = null;
    /** ok pressed on dialog from SystemIniPanel */
    private boolean okPressed = false;
    /**
     * Get label of project type.
     * @return Label for type of project
     */
    public String getLabel () {
        return label;
    }



    /**
     * Load project from file
     * @param projectFile  input file
     * @return ImportProject
     */
    public abstract ImportProject load (java.io.File projectFile)
    throws Exception;

    /**
     * Add new type of Environment that can be import.
     * @param info import info
     */
    public static void addProjectInfo (ProjectInfo info) {
        projectInfos.addElement (info);
    }

    /**
     * It gat all available ProjectInfo.
     * @return Vector of ProjectInfo
     */
    public static java.util.Vector getProjectsInformations () {
        return ProjectInfo.projectInfos;
    }


    /**
     * Test load project
     * @param file project file
     * @return null when project isn't loaded
     */
    public static ImportProject getImportProject (java.io.File file) {
        int                                 i;
        ProjectInfo                         prj = null;

        ImportProject                       imPrj = null;
        javax.swing.filechooser.FileFilter  filter = null;

        for (i = 0; i < projectInfos.size (); i++) {
            prj = (ProjectInfo) projectInfos.elementAt (i);
            filter = prj.getFileFilter ();

            if (filter.accept (file)) {
                try {
                    imPrj = prj.load (file);

                    if (imPrj != null) {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // project isn't loaded , continue
                }
            }
        }

        return imPrj;
    }

    /**
     * insert all ProjectInfo types
     * @return Vector of ProjectInfo
     */
    private static Vector init () {
        Vector  projectInfos = new Vector ();

        projectInfos.addElement (new ProjectInfoMSVJ ());
        projectInfos.addElement (new ProjectInfoJBuld ());
        projectInfos.addElement (new ProjectInfoVCafe ());

        return projectInfos;
    }




    /** Find package name in java file.
     * @param reader input java file
     * @return pacakge name ,e.g. java.io or null if it is default package
     */

    public static String parsePackage(BufferedReader reader) {
        String str ;
        int index1 =0, index2 = 0,  packageIndex = 0;
        try {
            while (true) {
                str = (reader.readLine ()).trim () ;
                if (str.length () == 0 ) {
                    continue;
                }

                index1 = str.indexOf ("/*"); // NOI18N

                if (index1 != 0 ) {
                    index2 = str.indexOf ("//"); // NOI18N

                    if (index2 != 0 ) {
                        packageIndex = str.indexOf ("package"); // NOI18N
                        if (packageIndex != -1) {
                            // package is founded
                            str = str.substring (7);
                            index1 = str.indexOf ('/') ;
                            if (index1 != -1 ) {
                                str = str.substring (0,index1 - 1) ;
                            }
                            index1 = str.indexOf (';');
                            if (index1 != -1) {
                                str = str.substring (0,index1);
                            }
                            return str;
                        } else {
                            return null;
                        }
                    } else {
                        continue;
                    }
                } else {
                    searchEndComment (str.substring (index1 + 2),reader) ;
                    continue;
                }

            }
        } catch (java.io.IOException e) {
            if ( (e instanceof java.io.EOFException) == false ) {
                e.printStackTrace ();
            }
        }
        return null;
    }
    /** search end of comment  * /
     * @param string string where
     */
    private static void searchEndComment(String string,BufferedReader reader) throws java.io.IOException {
        int index;
        do {

            index = string.indexOf ("*/");  // NOI18N
            if (index != -1 ) {
                return ;
            }
            string = reader.readLine () ;
        } while (true);
    }

    /** Test  if files are in fileSystems with correct package.
     * If filesystem package doesn't exist , it  is add .
     * @param fileSystems Vector of File whitch containt roots for files
     * @param files Vector of File 
     */
    public static void updateFileSystems(Vector files,Vector fileSystems) {
        // for each file if is java file get package , if package isn't founded in fileSystem
        // add FileSystem
        //
        File file = null ;
        File folder = null ;
        String name  = null ;
        String packageName = null ; // parsed package name in Java file
        String tmpFolder = null;
        boolean exist = false;
        int packageIndex = 0;

        for (int i = 0 ; i < files.size () ; i++) {
            file = (File) files.elementAt (i);
            name = file.getAbsolutePath ();
            if (name.endsWith (".java") ) { // NOI18N
                name = Import.optimalizePath (name);
                if (name == null ) {
                    continue;
                }
                try {
                    packageName = ProjectInfo.parsePackage (new BufferedReader (new FileReader (file)));

                } catch (java.io.IOException e) {
                    e.printStackTrace ();
                    continue;
                }

                if (packageName != null) {
                    packageName = packageName.replace ('.' ,'\\');
                    name = name.replace ('/', '\\');
                    packageName = packageName.trim();

                    packageIndex = name.indexOf ( packageName );
                } else {
                    packageIndex = name.lastIndexOf('\\') + 1;

                }


                if (packageIndex == -1 ) {
                    if (Import.debug ) {
                        System.out.println ("Bad package name (ProjectInfo.updateFileSystem) " + packageName + " " + name ); // NOI18N
                    }
                } else if (packageIndex == 0)  {
                    folder = new File (File.separator) ; // NOI18N
                } else {
                    if (File.separator == "/") { // NOI18N
                        name = name.replace ('\\','/');
                    }
                    tmpFolder = name.substring (0,packageIndex - 1);
                    tmpFolder = Import.optimalizePath(tmpFolder);
                    if (tmpFolder != null ) {
                        folder = new File (tmpFolder  );
                    } else {
                        folder = null;
                    }
                }
                if (folder != null ) {
                    exist = false ;
                    for (int j = 0 ; j < fileSystems.size () ; j++ ) {
                        if (folder.equals (fileSystems.elementAt (j)) ) {
                            if (Import.debug ) {
                                System.out.println("exist"); // NOI18N
                            }
                            exist = true ;
                            break;
                        }
                    }
                    if (exist == false ) {
                        if (Import.debug) {
                            System.out.println("add folder" + folder ) ; // NOI18N
                        }
                        //folder = Import.optimalizePath(folder);
                        fileSystems.addElement (folder) ;
                    }
                } else {
                    if (Import.debug) {
                        System.out.println("ProjectInfo UpdateFileSystems folder not added"); // NOI18N
                    }

                }
            }
        }
    }
    /** Choose system file. For example JBuilder 1.0 require JBuilder\\bin\\jbuilder.ini.
     * In this file is classpath.
     * @param fileFilter filter for system IDE file
     * @param fileDialogTitle title of JFileChooser dialog that be showed to select system file
     * @param fileDescription description for user where he find file
     * @param dialogDescripton description for SystemIniPanel
     * @param currentDirectory directory that will be used in JFileChooser, when it is null,
     *    it will used default 
     * @return system file or null if file isn't selected
     */
    protected java.io.File chooseSystemFile (javax.swing.filechooser.FileFilter fileFilter,
            java.lang.String fileDialogTitle,
            java.lang.String fileDescription,
            java.lang.String dialogDescription,
            java.io.File currentFile
                                            ) {
        javax.swing.JFileChooser fileChooser = new JFileChooser ();
        fileChooser.setFileFilter (fileFilter);
        okPressed = false ;
        SystemIniPanel panel = new SystemIniPanel (fileDescription,fileChooser,fileDialogTitle,currentFile);

        DialogDescriptor  desc = new DialogDescriptor (panel, dialogDescription , true,
                                 new ActionListener () {
                                     public void actionPerformed (ActionEvent ev) {
                                         dialog.setVisible (false);

                                         // dialog = null;
                                         if (ev.getSource ().toString ().equals ("0")) { // NOI18N
                                             okPressed = true;
                                             dialog.dispose ();
                                         }
                                     }
                                 }
                                                      );
        dialog = TopManager.getDefault ().createDialog (desc);

        dialog.show ();

        if (okPressed) {
            return panel.getSelectedFile ();
        } else {
            return null;
        }
    }
}
/*
 * Log
 *  6    Gandalf   1.5         2/4/00   Martin Ryzl     import fix  
 *  5    Gandalf   1.4         1/20/00  Petr Zajac      
 *  4    Gandalf   1.3         1/17/00  Petr Zajac      
 *  3    Gandalf   1.2         1/17/00  Petr Zajac      correct sys classpath
 *  2    Gandalf   1.1         1/13/00  Martin Ryzl     heavy localization
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */
