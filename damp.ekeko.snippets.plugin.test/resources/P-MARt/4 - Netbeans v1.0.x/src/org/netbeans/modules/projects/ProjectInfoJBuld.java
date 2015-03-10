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

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.swing.JFileChooser;
import org.openide.*;
import javax.swing.filechooser.FileFilter;
import java.util.Vector ;

/**
 * Project info for Borland JBuilder 1.0
 * @author Petr Zajac
 */
public class ProjectInfoJBuld extends ProjectInfo {

    protected String fileSystem = null;
    /**
     * First chararacter  at readed line.
     */
    protected char              firstChar;

    /**
     * Vector of Strings which contain classpath of project.
     * @associates Object
     */
    protected java.util.Vector  classPath = new java.util.Vector ();

    /**
     * Vector of Strings which contain path of LocalFIleSystems.
     * @associates String
     */
    protected java.util.Vector  fileSystems = new java.util.Vector ();

    /**
     * Vector of String names project files.
     * @associates String
     */
    protected java.util.Vector  files = new java.util.Vector ();

    /**
     * Vector of String that contain classpath.
     * @associates File
     */
    protected java.util.Vector  systemClasspath = new java.util.Vector ();

    /**
     * Projct main class.
     */
    protected String            mainClass;
    /**It is version of project file. For JBuilder 1.0  it is 1 and
     * for JBulder 3.x it is 2
     */

    protected int               version;
    /**
     * type of block in project file
     * It can be PROJECT, CONTENTS, PARAMS
     */
    protected int               typeOfBlock;

    /**
     * PROJECT block
     */
    protected final int         PROJECT = 0;

    /**
     * CONTENTS block
     */
    protected final int         CONTENTS = 1;

    /**
     * PARAMS block
     */
    protected final int         PARAMS = 2;

    /**
     * variable name of classpath in JBuilder 1 configuration file
     */
    protected final String      SYSTEMCLASSPATH1 = "ClassPath"; // NOI18N

    /**
     * variable name of classpath in JBuilder 3 configuration file
     */

    protected final String      SYSTEMCLASSPATH2 = "Classpath"; // NOI18N

    /** System file (JBuilder.ini) for JBuilder 1.x.
     */
    protected File currentSysFile1 = null;

    /** System file (Library.ini) for JBuilder 3.x.
     */
    protected File currentSysFile2 = null;


    /**
     * Creates new ProjectInfoJBuld
     */
    public ProjectInfoJBuld () {}

    /**
     * Loading project data.
     * @param projectFile input file
     * @return interface which contain configuration of project
     */
    public ImportProject load(java.io.File file) {
        java.io.BufferedReader  istream;
        int                     i;

        File tmpFile = null;
        classPath.removeAllElements ();
        fileSystems.removeAllElements ();
        mainClass = null;
        files.removeAllElements ();
        systemClasspath.removeAllElements ();
        try {
            istream = new java.io.BufferedReader (new java.io.FileReader (file));
        } catch (java.io.IOException e) {
            if (Import.debug) {
                System.out.println("error - ProjectInfoJBuld " + 139); // NOI18N
            }
            return null;
        }

        String  line = null;

        try {
            line = istream.readLine ();
        } catch (java.io.IOException e) {
            return null;
        }
        version = testVersion (line);
        while (true) {
            try {
                line = istream.readLine ();
            } catch (java.io.EOFException e) {
                break;
            } catch (java.io.IOException e) {
                return null;
            }

            if (line == null) {
                break;
            }

            parseLine (line.trim ());
        }

        java.io.File  systemIniFile = null ;
        systemIniFile = getSystemIniFile ();

        if (systemIniFile != null ) {
            try {
                loadSystemClassPath (systemIniFile);
            } catch (java.io.IOException e) {
                e.printStackTrace ();
            }
        } else {
            TopManager.getDefault ().setStatusText (Import.getLocalizedString ("MSG_JBuilderConfFileNotSelected")); // NOI18N
        }

        // now Create import project structure
        //
        ImportProjectImpl prj = new ImportProjectImpl ();
        File              parent = file.getParentFile ();

        for (i = 0; i < files.size (); i++) {
            tmpFile = createFile (parent, (String ) files.elementAt (i));
            // System.out.println(tmpFile.getAbsolutePath());
            if (tmpFile.exists ()) {
                prj.files.addElement (tmpFile);
            }
        }

        for (i = 0; i < fileSystems.size (); i++) {
            tmpFile = createFile (parent, (String) fileSystems.elementAt (i));
            if (tmpFile.exists () ) {
                prj.fileSystems.addElement (tmpFile) ;
            }
        }

        for (i = 0; i < classPath.size (); i++) {
            tmpFile = new File (parent, (String) classPath.elementAt (i));
            if (tmpFile.exists ()) {
                prj.classpath.addElement (tmpFile);
            }
        }

        for (i = 0; i < systemClasspath.size (); i++) {
            prj.systemClasspath.add (systemClasspath.elementAt (i));
        }

        prj.mainClass = mainClass;
        File files [] = prj.getFileSystems() ;
        // int m ;
        // for ( m = 0 ; m < files.length ; m++) {
        //  System.out.println( files[m].getAbsolutePath());
        //  System.out.println(prj.fileSystems.elementAt (m));
        //}
        updateFileSystems (prj.files,prj.fileSystems);

        // files  = prj.getFileSystems() ;
        // for ( m = 0 ; m < files.length ; m++) {
        //   System.out.println( files[m].getAbsolutePath());
        //   System.out.println(prj.fileSystems.elementAt (m));
        // }
        if (Import.debug) {
            System.out.println("error - JBUild prj is null"); // NOI18N
        }
        return prj;
    }


    /**
     * Test JBuilder project version from first line
     * @param line first line
     * @return project version
     */
    int testVersion (String line) {
        int start, end;

        start = line.indexOf ("{"); // NOI18N
        end = line.indexOf ("}"); // NOI18N

        if (start == -1 || end == -1) {
            return -1;
        }

        String  substr = line.substring (start + 1, end);

        //    System.out.println (substr);

        float retValue = 0;

        try {
            retValue = Float.parseFloat (substr);
        } catch (java.lang.NumberFormatException e) {
            return (-1);
        }

        return (int) retValue;
    }


    /**
     * parse line of project file
     * @param line input line
     */
    protected void parseLine (String line) {
        if (line.length () == 0) {
            return;
        }

        firstChar = line.charAt (0);

        if (!parseComment (line)) {
            if (version == 1) { //JBulder Project pvesion 1
                if (!parseBeginBlock (line)) {
                    switch (typeOfBlock) {

                    case PROJECT:
                        parseProject (line);

                        break;

                    case CONTENTS:
                        parseContents (line);

                        break;

                    case PARAMS:
                        parseParams (line);

                        break;
                    }
                }
            } else if (version == 2 ) { // JBuilder project version 2
                parseJBuilder3(line);
            }
        }
    }

    /**
     * ignore comment
     * @param line line of project file
     */
    protected boolean parseComment (String line) {
        if (firstChar == ';' || firstChar == '\n') {
            return true;
        } else {
            return false;
        }
    }


    /**
     * parse block of filesystem
     * @param line line of project file
     * @return false when line doesn't contain block definition
     */
    protected boolean parseBeginBlock (String line) {
        if (firstChar == '[') {
            int     index = line.lastIndexOf ("]"); // NOI18N
            String  type = line.substring (1, index);

            type = type.trim ();

            if (type.equalsIgnoreCase ("Params")) { // NOI18N
                typeOfBlock = PARAMS;
            } else if (type.equalsIgnoreCase ("Project")) { // NOI18N
                typeOfBlock = PROJECT;
            } else if (type.equalsIgnoreCase ("Contents")) { // NOI18N
                typeOfBlock = CONTENTS;
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Parsse [Project] section
     * @param line line of project file
     * @return false when it is sysntax error
     */
    protected boolean parseProject (String line) {
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer (line,
                                              "="); // NOI18N
        String                    left = null;
        String                    right = null;
        java.util.Vector          files = null;
        String                    file = null;

        int                       i = 0;

        try {
            left = tokenizer.nextToken ();
            right = tokenizer.nextToken ();
        } catch (java.util.NoSuchElementException e) {}

        if (right == null || left == null) {
            return true;
        }

        left = left.trim ();
        right = right.trim ();


        if (left.equalsIgnoreCase ("Classpath")) { // NOI18N
            files = parseFiles (right);

            for (i = 0; i < files.size (); i++) {
                classPath.addElement (files.elementAt (i));
            }


        } else if (left.equalsIgnoreCase ("SourcePath")) { // NOI18N
            files = parseFiles (right);

            for (i = 0; i < files.size (); i++) {
                file = (String) files.elementAt (i);

                fileSystems.addElement (file);
            }


        } else if (left.equalsIgnoreCase ("DefaultRunnable")) { // NOI18N
            mainClass = right.trim ();
        }

        return true;
    }

    /**
     * parse files which is separed by ";"
     * @param strng string of paths separed by ";"
     * return Vector of  String file name
     */
    java.util.Vector parseFiles (String strng) {
        String strFile = null ;
        java.util.StringTokenizer tokenizer =
            new java.util.StringTokenizer (strng, ";"); // NOI18N
        java.util.Vector          vec = new java.util.Vector ();

        try {
            strFile = tokenizer.nextToken ().trim ();
            strFile = Import.optimalizePath(strFile);
            if (strFile != null ) {
                vec.addElement (strFile);
            }
        } catch (java.util.NoSuchElementException e) {}

        return vec;
    }

    /**
     * parseContents section
     * @param one line of project
     * @return false on sysntax error
     */
    protected boolean parseContents (String line) {
        int     index1 = line.indexOf ("file:///"); // NOI18N
        int     index2 = line.indexOf ("file:///.\\"); // NOI18N

        String  file = null;

        if (index2 != -1) {
            file = line.substring (10);

            // System.out.println(file);
            file = Import.optimalizePath(file);
            if (file != null ) {
                files.addElement (file);
            }

            return true;

        }

        if (index1 != -1) {
            file = line.substring (8);

            // System.out.println(file);
            file = Import.optimalizePath(file);
            if (file != null ) {
                files.addElement (file);
            }
        }

        return true;
    }

    /**
     * parsing [Params] section
     * @param line  line of project file
     * @return  false on sytax error
     */
    protected boolean parseParams (String line) {
        return true;
    }


    /**
     * Get File Filter for project name.
     * @return Project file filter
     */
    public javax.swing.filechooser.FileFilter getFileFilter () {
        return new javax.swing.filechooser.FileFilter () {
                   protected String description = Import.getLocalizedString ("CTL_JBuilderFileFilterDescription"); // NOI18N
                   protected String extension = ".jpr"; // NOI18N
                   public boolean accept (java.io.File f) {
                       return (f.getName ().endsWith (extension) || f.isDirectory ());
                   }

                   public String getDescription () {
                       return description;
                   }

               };
    }

    /**
     * Get System configuration file from JFileChooser
     * @return JBuilder system configuration file
     */
    protected java.io.File getSystemIniFile () {
        File file = null ;
        javax.swing.filechooser.FileFilter filter = new FileFilter () {
                    public boolean accept (File file) {
                        return (file.isDirectory ()
                                | file.getName ().toLowerCase ().endsWith (version == 1 ?  "jbuilder.ini" : "library.ini")); // NOI18N
                    }
                    public String getDescription () {
                        return  version == 1 ? "(jbuilder.ini)" : "(library.ini)" ; // NOI18N
                    }
                };
        String fileDialogTitle = Import.getLocalizedString (
                                     version == 1 ? "CTL_ImportChooseSystemJBuilderFileMain1" : // NOI18N
                                     "CTL_ImportChooseSystemJBuilderFileMain2"); // NOI18N
        String fileDescription = Import.getLocalizedString (
                                     version == 1 ? "CTL_ImportTipJBuilder1" : // NOI18N
                                     "CTL_ImportTipJBuilder3"); // NOI18N
        String dialogDescription = Import.getLocalizedString (
                                       version == 1 ? "CTL_ImportChooseSysFileJBuild1" : // NOI18N
                                       "CTL_ImportChooseSysFileJBuild3" ); // NOI18N
        file = (version == 1 ? currentSysFile1 : currentSysFile2) ;
        file =  chooseSystemFile ( filter,
                                   fileDialogTitle,
                                   fileDescription,
                                   dialogDescription,
                                   file);
        if (version == 1) {
            currentSysFile1 = file;
        } else {
            currentSysFile2 = file;
        }
        return file;
    }


    /** Parse JBuilder project vresion 2.00 (it is JBuildre 3)
     * @param line imput line 
     */
    protected void parseJBuilder3 (String line) {
        int index = 0;
        int index2 = 0;
        String tmp = null;

        if (line.indexOf ('#') == 0) {
            if (line.endsWith (".jpr") ) { // NOI18N
                return ;
            } else {
                index2 = line.indexOf ("="); // NOI18N
                line = line.substring (index2);
                index = line.indexOf ("."); // NOI18N


                if (line.indexOf ('/') == index + 1 || line.indexOf ('\\') == index +1 ) {
                    tmp = line.substring (index + 1);
                    files.addElement (tmp );
                } else {
                    tmp = line.substring (index2-1);
                    files.addElement (tmp);
                }
            }
        } else if (line.indexOf ("sys[0].DefaultDir") == 0) { // NOI18N
            // Default directroy of files
            //sys[0].DefaultDir=..\samples\com.borland\samples\apps\chess\server\
            /* if (Import.debug) {
               System.out.println(line);
             }
             line = line.substring (line.indexOf ('=') + 1 , line.lastIndexOf ('.' - 1 ));
             line = line.substring ( 0, line.lastIndexOf( '\\') - 1 );
             System.out.println (line);
             fileSystems.addElement (line ); */ // id must be added prefix !!!

        } else if (line.indexOf ("sys[0].DefaultRunnablePath") == 0) { // NOI18N
            // path of MainClass in format ./filename.java
            //sys[0].SourcePath=..\..\..\..\..\..\..\samples
            line = line.substring (line.indexOf (".") + 1 ) ; // NOI18N
            Vector fileSystemsTmp = new Vector ();
            for (index = 0 ; index < fileSystems.size () ; index ++ ) {
                tmp = (String) fileSystems.elementAt ( index);
                String str = Import.optimalizePath (line + "\\" + tmp ); // NOI18N

                //fileSystems.setElementAt (str,index); // NOI18N
                if (str != null ) {
                    fileSystemsTmp.addElement  (str); // NOI18N
                }

            }
            fileSystems = fileSystemsTmp;

        } else if (line.indexOf ("sys[0].SourcePath") == 0 ) { // NOI18N
            // it isn't used
            //
        }


    }

    /**
     * Get system classpath that is jbuilder.ini. It is on line which begin ClassPath.
     * @param jbuilder.ini file
     */

    protected void loadSystemClassPath(java.io.File systemFile) throws java.io.IOException {
        if (systemFile == null ) {
            return ;
        }
        BufferedReader            istream =
            new java.io.BufferedReader (new java.io.FileReader (systemFile));
        String                    line = null;
        File                      parentFile = systemFile.getParentFile ();
        java.util.StringTokenizer tokenizer = null;
        String                    token = null;
        int                       index = -1;
        String sysClasspath = version == 1  ? SYSTEMCLASSPATH1 : SYSTEMCLASSPATH2 ;

        try {
            while (true) {
                line = istream.readLine ();

                if (line == null) {
                    break;
                }

                line = line.trim ();

                index = line.indexOf (sysClasspath);
                if (index == 0) {
                    if (Import.debug) {
                        System.out.println(line);
                    }
                    line = line.substring (sysClasspath.length ());
                    index = line.indexOf ("="); // NOI18N

                    if (index >= 0) {
                        line = line.substring (index + 1);
                    }

                    tokenizer = new java.util.StringTokenizer (line, ";"); // NOI18N

                    try {
                        while (true) {
                            token = tokenizer.nextToken ();

                            //              System.out.println (token);
                            String strFile = null;
                            //   System.out.println ("pred " + parentFile.getAbsolutePath() + " " + token); // NOI18N
                            strFile = Import.optimalizePath(parentFile.getAbsolutePath() + "\\" + token); // NOI18N

                            //      System.out.println(strFile );
                            if (strFile != null ) {
                                File file =new File (strFile);
                                if (file.exists ()) {
                                    systemClasspath.addElement (file);
                                }
                            }
                        }
                    } catch (java.util.NoSuchElementException e) {}
                }
            }
        } catch (java.io.EOFException e) {}
    }

    /** It create file with system separators
     * @param file parent file 
     * @param path of file relative from parent
     * @return file/name 
     */
    java.io.File createFile(File file,String name) {
        name = name.replace ('\\',File.separatorChar );
        name = name.replace ('/',File.separatorChar );
        return new File (file,name);
    }


    public static void  main (String[] arg) {


        ProjectInfoJBuld jbuld = new ProjectInfoJBuld () ;
        try {
            ImportProject prj = jbuld.load (new File ("/home/pzajac/import/jbproject/untitled3/untitled3.jpr")); // NOI18N
            File files [] = prj.getFiles();
            for (int i = 0 ; i < files.length;  i++)
                System.out.println(files[i].getPath());

            //  prj.getFiles();
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }
}
