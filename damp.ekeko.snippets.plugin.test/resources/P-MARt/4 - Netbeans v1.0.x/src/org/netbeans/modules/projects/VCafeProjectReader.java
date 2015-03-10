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

import org.netbeans.modules.projects.*;
import org.netbeans.modules.projects.ImportProject;


import java.io.*;
import java.util.Enumeration;



/**
 * Class declaration
 * 
 * 
 * @author Petr Zajac
 * @version %I%, %G%
 */
public final class VCafeProjectReader {
    protected ImportProjectImpl project = new ImportProjectImpl ();

    /**
     * Default Constructor
     */
    public VCafeProjectReader () {}


    /**
     * Test file version
     * 
     * 
     * @param string contain version of Visual Cafe
     * 
     * @return
     * 
     * @see
     */
    public boolean checkProjectFileVersion (String string) {
        boolean flag = false;
        String  version = getProjectFileVersion (string);

        if (version != null && version.equals ("1.0")) { // NOI18N
            flag = true;
        }

        return flag;
    }

    /**
     * Extract files from project file
     * 
     * @return ImportProject of VCafe
     * 
     * @see
     */
    public ImportProject extractFiles (String prjName) {

        project = new ImportProjectImpl ();

        byte  byteSequence[] = new byte[10];
        long  tmpOffset = -1L;

        byteSequence[0] = -1;
        byteSequence[1] = -1;
        byteSequence[2] = -1;
        byteSequence[3] = -1;
        byteSequence[4] = -1;
        byteSequence[5] = -1;
        byteSequence[6] = -1;
        byteSequence[7] = -1;
        byteSequence[8] = -35;
        byteSequence[9] = 102;

        try {
            //      System.out.println (prjName);

            File  prjFile = new File (prjName);
            File  parentFile = prjFile.getParentFile ();

            project.fileSystems.addElement (parentFile);

            BinFile  binaryfile = new BinFile (prjName, "r"); // NOI18N

            binaryfile.setSearchDirection (2);
            binaryfile.bottom ();

            long  offset;

            if ((offset = binaryfile.search (byteSequence)) != -1L) {
                offset -= 2L;
                tmpOffset = offset;

                binaryfile.seek (offset);

                int   i = binaryfile.readShortValue ();
                long  offset2 = offset + i;

                for (offset += 24L; offset < offset2; offset += 20L) {
                    binaryfile.seek (offset);

                    long  step = binaryfile.readIntValue ();

                    offset += 4L;

                    byte  stringBytes[] = new byte[(int) step];

                    binaryfile.readFully (stringBytes);

                    offset += step;
                    offset += 10L;

                    binaryfile.seek (offset);

                    byte  typeOfElement = binaryfile.readByte ();

                    if ((typeOfElement & 0x1) == 1) {
                        String  fileName = new String (stringBytes);

                        // it is java sorce read here
                        //
                        //            System.out.println (fileName);
                        //            System.out.println (fileName);

                        File    file = new File (parentFile, fileName);
                        String  extension =
                            fileName.substring (fileName.lastIndexOf ("."),  // NOI18N
                                                fileName.length ());

                        //            System.out.println (extension);

                        if (extension.compareToIgnoreCase ("zip") == 0  // NOI18N
                                || extension.compareToIgnoreCase ("jar") == 0) { // NOI18N
                            project.classpath.addElement (file);
                        } else {
                            project.files.addElement (file);
                        }



                    }

                }
            }

            binaryfile.close ();
        } catch (Exception ex) {
            if (Import.debug) {
                ex.printStackTrace ();
            }
            return null;
        }

        // return project;
        return project;
    }

    // /////////////////////////////////////////

    /**
     * Extract project
     */

    /*
     * public Project extractProject()
     * {
     * //   Project project = extractFiles(projectFile.getProjectFileName());
     * return null;
     * }
     */

    public String getProjectFileVersion (String fileName) {
        String  version = new String ();
        byte    byteSequence[] = new byte[3];

        try {
            RandomAccessFile  randomaccessfile = new RandomAccessFile (fileName,
                                                 "r"); // NOI18N
            long              offset = 48L;

            randomaccessfile.seek (offset);
            randomaccessfile.readFully (byteSequence);

            version = version + (char) byteSequence[0];
            version = version + (char) byteSequence[1];
            version = version + (char) byteSequence[2];

            randomaccessfile.close ();
        } catch (IOException ex) {
            version = null;
        }

        return version;
    }


    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @see
     */
    protected boolean isExtractClassFiles () {
        return (option & 0x2) == 2;
    }

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @see
     */
    protected boolean isExtractDocFiles () {
        return (option & 0x10) == 16;
    }

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @see
     */
    protected boolean isExtractPackages () {
        return (option & 0x4) == 4;
    }

    /*
     * public JavaTool getTool()
     * {
     * return tool;
     * }
     */

    /**
     * Method declaration
     * 
     * 
     * @param s
     * 
     * @return
     * 
     * @see
     */
    public boolean checkPackage (String packageName) {
        String  packageUpperCaseName = packageName.toUpperCase ();
        boolean flag = false;

        if (!packageUpperCaseName.equals (".")  // NOI18N
                &&!packageUpperCaseName.endsWith ("JAVA" + File.separator + "LIB")  // NOI18N
                &&!packageUpperCaseName.endsWith ("CLASSES.ZIP")  // NOI18N
                &&!packageUpperCaseName.equals ("%CLASSPATH%")  // NOI18N
                &&!packageUpperCaseName.endsWith ("SWINGALL.JAR")) { // NOI18N
            flag = true;
        }

        return flag;
    }


    /*
     * public String changePackagePath(String s, String s1, String fileName)
     * {
     * String s3 = s;
     * FileNameSplitter filenamesplitter = new FileNameSplitter(s);
     * String s4 = filenamesplitter.getFileType().toUpperCase();
     * if(s4.equals("JAR"))
     * {
     * String s5 = fileName + File.separator + s1;
     * File file = new File(s5);
     * if(file.exists())
     * s3 = s5;
     * }
     * return s3;
     * }
     */

    private static final String   ENVIRONMENT_SECTION = "Environment"; // NOI18N
    private static final String   CLASSPATH_KEY = "CLASSPATH"; // NOI18N
    protected static final String FILTER_SOURCE = "*.java"; // NOI18N
    protected static final String FILTER_CLASS = "*.class"; // NOI18N
    protected static final String CLASSES_ZIP = "CLASSES.ZIP"; // NOI18N
    protected static final String SWING_JAR = "SWINGALL.JAR"; // NOI18N
    protected static final String CLASSPATH_REF = "%CLASSPATH%"; // NOI18N

    protected int                 option;

}

/*
 * Log
 *  2    Gandalf   1.1         1/13/00  Martin Ryzl     heavy localization
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */