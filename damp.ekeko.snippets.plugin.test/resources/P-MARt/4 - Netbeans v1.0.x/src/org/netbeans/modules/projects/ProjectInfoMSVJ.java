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
import java.util.Vector;
import java.util.StringTokenizer;

/**
 * Reading Microsoft Visual J++ project. Files ends with *.vjp
 * 
 * @author Petr Zajac
 * 
 */
public class ProjectInfoMSVJ extends ProjectInfo {

    /**
     * Creates new ProjectInfoMSVJ
     */
    DataInputStream istream;

    /**
     * main class of project
     */
    String          mainClass = null;

    /**
     * project name
     */
    String          prjName = null;

    /**
     * class path separed by
     */
    String          classPath = null;

    /**
     * Vector of strings
     * @associates String
     */
    Vector          fileFilters = new Vector ();


    /**
     * Loading project data.
     * @param projectFile input file *.vep
     * @return interface which contain configuration of project
     */
    public ImportProject load (java.io.File file) throws Exception {
        String            line = null;
        ParseStruct       parseStruct = null;
        ImportProjectImpl prj = new ImportProjectImpl ();

        try {
            openFile (file);

            while (true) {
                try {
                    line = readLine ();

                    //          System.out.println (line);
                } catch (java.io.EOFException e) {
                    break;
                }

                parseStruct = parseLine (line);

                if (parseStruct == null) {
                    // break;
                    continue;
                } else if (parseStruct.value == null) {
                    continue;
                }

                if (parseStruct.name.trim ().equals ("VJSTARTUP")) { // NOI18N
                    prj.mainClass = parseStruct.value;
                } else if (parseStruct.name.equals ("VJCPATH")) { // NOI18N
                    classPath = parseStruct.value;
                } else if (parseStruct.name.equals ("VJFILEFILTER")) { // NOI18N

                    StringTokenizer tokenizer = new StringTokenizer (parseStruct.value, ";"); // NOI18N
                    String          token = null;
                    int             index = 0;

                    try {
                        token = tokenizer.nextToken ();

                        while (token != null) {
                            index = token.indexOf (".");    // extension of file // NOI18N

                            fileFilters.add (token.substring (index + 1));

                            token = tokenizer.nextToken ();
                        }
                    } catch (java.util.NoSuchElementException e) {}
                    ;

                    // Add Files into project
                    //
                    File  directory = file.getParentFile ();

                    prj.fileSystems.addElement (directory);


                    addFiles (directory, prj.files, fileFilters);

                    //          for (int i = 0; i < prj.files.size (); i++) {
                    //            System.out.println (((File) prj.files.elementAt (i)).getPath ());
                    //          }
                }
            }
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return prj;
    }

    /**
     * It's parse line. to Parse struct. Separator is "="
     */
    ParseStruct parseLine (String line) {
        StringTokenizer tokenizer = new StringTokenizer (line, "="); // NOI18N
        ParseStruct     parseStruct = new ParseStruct ();

        try {
            parseStruct.name = tokenizer.nextToken ();
            parseStruct.value = tokenizer.nextToken ();
        } catch (java.util.NoSuchElementException e) {
            if (parseStruct.name == null && parseStruct.value == null) {
                return null;
            }

        }

        return parseStruct;
    }



    /**
     * Open file and read header
     * @param file project file
     */
    protected void openFile (java.io.File file) throws java.io.IOException {


        istream = new DataInputStream (new FileInputStream (file));

        // header
        for (int i = 0; i < 6; i++) {
            istream.readByte ();
        }
    }

    /**
    * add all files that have sufix from fileFilters and are in directory
    */

    public static  void  addFiles (File directory, Vector files, Vector fileFilters) {
        int     i = 0;
        String  strings[] = directory.list (new MSVJFilter (fileFilters));
        File    file = null;

        for (i = 0; i < strings.length; i++) {
            file = new File (directory, strings[i]);

            if (file.isDirectory ()) {
                addFiles (file, files, fileFilters);
            } else {
                files.addElement (file);
            }
        }

        strings = directory.list ();

        for (i = 0; i < strings.length; i++) {
            file = new File (directory, strings[i]);

            if (file.isDirectory ()) {
                addFiles (file, files, fileFilters);
            }
        }

    }

    /**
     * Read line from project file
     * @return line
     */
    String readLine () throws Exception {


        StringBuffer  buffer = new StringBuffer ();


        char          mchar;
        byte          mbyte;

        try {
            while (true) {
                do {
                    mbyte = istream.readByte ();
                } while (mbyte == 0);

                if (mbyte == 0x0a /* end of line */) {
                    break;
                }

                buffer.append ((char) mbyte);

                mbyte = istream.readByte ();
            }

        } catch (Exception e) {
            //      System.out.println (buffer);

            throw e;
        }

        return buffer.toString ();

    }

    /**
     * Get Filefilter of opreject file
     * @return Project file filter
     */
    public javax.swing.filechooser.FileFilter getFileFilter () {
        return new javax.swing.filechooser.FileFilter () {
                   public boolean accept (java.io.File f) {
                       return (f.getName ().endsWith (".vjp") || f.isDirectory ()); // NOI18N
                   }

                   public String getDescription () {
                       return Import.getLocalizedString ("CTL_MSVJFileFilterDescription"); // NOI18N

                   }

               };
    }

}


final class ParseStruct {
    public String name;
    public String value;

    public ParseStruct (String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ParseStruct () {
        name = null;
        value = null;
    }

}


/**
 * FileName filter for MSVJ files. It filter files for sufix that is in Vector
 */
final class MSVJFilter implements FilenameFilter {
    protected Vector  fileFilters;

    /**
     * konstrktor
     * @param filters  is Vector of String sufix files  (*.java, ...) that will be inserted into project
     */
    public MSVJFilter (Vector filters) {
        this.fileFilters = filters;
    }

    /**
     * see FilenameFilter.accept()
     * @param parentFile directory
     * @param name filename
     */

    public boolean accept (final java.io.File parentFile,
                           java.lang.String name) {
        for (int j = 0; j < fileFilters.size (); j++) {
            if (name.endsWith ((String) fileFilters.elementAt (j))) {
                return true;
            }
        }

        return false;
    }

}


/*
 * Log
 *  3    Gandalf   1.2         1/15/00  Ian Formanek    NOI18N
 *  2    Gandalf   1.1         1/13/00  Martin Ryzl     heavy localization
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */



