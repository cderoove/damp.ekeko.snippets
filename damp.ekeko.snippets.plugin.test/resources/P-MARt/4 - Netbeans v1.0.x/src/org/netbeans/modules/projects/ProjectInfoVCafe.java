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
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openide.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Reading Visual Cafe project.
 * Read Visual cafe system file that is in VCafe/bin/sc.ini
 * 
 * @author Petr Zajac
 * 
 */
public class ProjectInfoVCafe extends ProjectInfo {

    /**
     * Binary parser for Visual Cafe project
     */
    protected VCafeProjectReader vcafe;

    /**
     * name of parameter in sc.ini
     * CLASSPATH = c:/lib/tool.jar ....
     */
    public final String               SYSTEMCLASSPATH = "CLASSPATH"; // NOI18N

    /**
     * Vector of Files. It contain system classpath (from sc.ini)
     * @associates File
     */
    protected Vector                  systemClasspath = new Vector ();

    /** default sysFile
     */
    protected java.io.File currentSysFile = null ;

    /**
     * Creates new ProjectInfoVCafe
     */

    public ProjectInfoVCafe () {
        vcafe = new VCafeProjectReader ();
    }

    /**
     * Loading project data.
     * @param projectFile input file *.vep
     * @return interface which contain configuration of project
     */
    public ImportProject load (java.io.File projectFile) throws Exception {
        try {
            ImportProjectImpl prj =
                (ImportProjectImpl) vcafe.extractFiles (projectFile.getAbsolutePath ());
            File              file = getSystemIniFile ();

            /*       if (prj == null ) {
                    System.out.println("null prj ");
                  } else {
                    System.out.println("prj is ok");
                  }*/
            loadSystemClassPath (file);


            prj.systemClasspath = systemClasspath;

            return prj;
        } catch (Exception e) {
            if (Import.debug ) {
                e.printStackTrace ();
            }
            throw e;
        }
    }

    /**
     * Project file filter. It is *.vep at default
     * @return Project file filter
     */
    public javax.swing.filechooser.FileFilter getFileFilter () {
        return new javax.swing.filechooser.FileFilter () {
                   public boolean accept (java.io.File f) {
                       return (f.getName ().endsWith (".vep") || f.isDirectory ()); // NOI18N
                   }

                   public String getDescription () {
                       return Import.getLocalizedString ("CTL_VCafeFileFilterDescription"); // NOI18N
                   }

               };
    }

    /**
     * Show JFileChooser that choose configuretion file.
     * @return Visual Cafe system configuration file
     */
    protected java.io.File getSystemIniFile () {

        File file = null;
        javax.swing.filechooser.FileFilter filter = new FileFilter () {
                    public boolean accept (File file) {
                        return (file.isDirectory ()
                                | file.getName ().toLowerCase ().endsWith ("sc.ini")); // NOI18N
                    }
                    public String getDescription () {
                        return  "sc.ini" ; // NOI18N
                    }
                };
        String fileDialogTitle = Import.getLocalizedString ("CTL_ImportChooseSystemVCafeFileMain" ); // NOI18N
        String fileDescription = Import.getLocalizedString ("CTL_ImportTipJVCafe" ); // NOI18N
        String dialogDescription = Import.getLocalizedString ( "CTL_ImportChooseSysFileVCafe" ); // NOI18N

        file = currentSysFile ;
        file = chooseSystemFile ( filter,
                                  fileDialogTitle,
                                  fileDescription,
                                  dialogDescription,
                                  file);
        currentSysFile = file ;
        return file ;

    }

    /**
     * Get system classpath that is jbuilder.ini. It on line which begin ClassPath.
     * @param jbuilder.ini Visual Cafe system configuration file
     * 
     */
    public void loadSystemClassPath (File sysFile) throws java.io.IOException {
        if (sysFile == null ) {
            return;
        }
        BufferedReader            istream =
            new java.io.BufferedReader (new java.io.FileReader (sysFile));
        String                    line = null;
        java.io.File              parFile = null;
        java.util.StringTokenizer tokenizer = null;
        String                    token = null;

        parFile = new File (sysFile.getParent ());

        String  parentName = parFile.getAbsolutePath ();
        int     index = -1;

        try {
            while (true) {
                line = istream.readLine ();

                if (line == null) {
                    break;
                }

                line = line.trim ();


                index = line.indexOf (SYSTEMCLASSPATH);

                if (index == 0) {
                    line = line.substring (SYSTEMCLASSPATH.length ());
                    index = line.indexOf ("="); // NOI18N

                    if (index >= 0) {
                        line = line.substring (index + 1);
                    }

                    tokenizer = new java.util.StringTokenizer (line, ";"); // NOI18N

                    try {
                        while (true) {
                            token = tokenizer.nextToken ().trim ();

                            if (token.equals (".")) { // NOI18N
                                continue;
                            }

                            index = token.indexOf ("%@P%"); // NOI18N

                            if (index != -1) {
                                token = token.substring (index + 4);
                                //             System.out.println(parentName + token);
                                token = Import.optimalizePath (parentName + token);
                            }
                            if (token != null ) {
                                systemClasspath.addElement (new File (token));
                            }

                        }
                    } catch (java.util.NoSuchElementException e) {}
                }
            }
        } catch (java.io.EOFException e) {}
    }

    public static void main (String[] arg ) {
        //File f = new File ("Z:\\import\\ide-samples\\VCafe2.5\\Symantec\\Applets\\Animator\\Animator.vpj" ); // NOI18N
        File f = new File ("/home/pzajac/import/VCafe3.0/Bin/sc.ini"); // NOI18N
        ProjectInfoVCafe pvcafe = new ProjectInfoVCafe ();

        try {
            pvcafe.loadSystemClassPath(f);
        } catch (Exception e) {
            e.printStackTrace  ();
        }

    }
}

/*
 * Log
 *  5    Gandalf   1.4         2/4/00   Martin Ryzl     import fix  
 *  4    Gandalf   1.3         1/20/00  Petr Zajac      
 *  3    Gandalf   1.2         1/15/00  Ian Formanek    NOI18N
 *  2    Gandalf   1.1         1/13/00  Martin Ryzl     heavy localization
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */



