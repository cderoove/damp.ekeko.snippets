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
import java.util.Vector;


/**
 * Implementaion of interface ImportProject.
 * @author Petr Zajac
 * 
 */
public class ImportProjectImpl implements ImportProject {

    /**
     * Vector of java.io.File that contain all project files
     */

    public Vector files = new Vector ();

    /**
     * Vector of java.io.File that contain all project LocalFilesystems
     */
    public Vector fileSystems = new Vector ();

    /**
     * Vector of java.io.File that contain all jar and zip files in project
     */
    public Vector classpath = new Vector ();

    /**
     * Vector of java.io.File that contain all jar and zip  system files
     */

    public Vector systemClasspath = new Vector ();

    /**
     * Main class of preject
     */
    public String mainClass;

    /**
     * name of project
     */
    public String name;

    /**
     * ImportProjectImpl constructor
     * @param files  Vector of java.io.File that contain  files of project
     * @param filesSystems  Vector of java.io.File that contain  FileSystems of project
     * @param mainClass mainClass of project
     * @param name name of project
     */
    public ImportProjectImpl (Vector files, Vector fileSystems,
                              Vector classpath, String mainClass, String name) {
        this.files = files;
        this.fileSystems = fileSystems;
        this.classpath = classpath;
        this.mainClass = mainClass;
        this.name = name;
    }

    /** Default constructor
     */
    public ImportProjectImpl () {}


    /**
     * Get filesystems.
     * @return filesystems for mounting into repository
     */
    public java.io.File[] getFileSystems() {
        File[]  fileSystems = new File[this.fileSystems.size ()];

        this.fileSystems.copyInto (fileSystems);

        return fileSystems;

    }

    /**
     * Get project files.
     * @return all files that are in project
     */
    public java.io.File[] getFiles () {
        File[]  files = new File[this.files.size ()];

        this.files.copyInto (files);

        return files;

    }

    /**
     * Get project classpath.
     * @return jar and zip files which are in CLASSPATH of project
     */
    public java.io.File[] getJarsAndZips () {
        File[]  files = new File[this.classpath.size ()];

        classpath.copyInto (files);

        return files;
    }

    /**
     * Get main class
     * @return MainClass of Project, when project hasn't MainClass, it is returned null
     */
    public String getMainClass () {
        return mainClass;
    }

    /**
     * Get name of project.
     * @return name of project which be imported
     */
    public String getName () {
        return name;
    }

    /**
     * Get system classpath.
     * @return system classpath
     */
    public java.io.File[] getSystemJarsAndZips () {
        File[]  files = new File[this.systemClasspath.size ()];

        systemClasspath.copyInto (files);

        return files;
    }

}

/*
 * Log
 *  2    Gandalf   1.1         2/4/00   Martin Ryzl     import fix  
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */



