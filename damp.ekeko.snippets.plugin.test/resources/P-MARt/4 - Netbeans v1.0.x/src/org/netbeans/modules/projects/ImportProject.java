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

/**
 * This interface contain setting of project that is imported
 * @author  Petr Zajac
 * @version 1
 */
public interface ImportProject {

    /**
     * Get filesystems that contain project
     * @return filesystems for mounting into repository
     */
    java.io.File[] getFileSystems ();

    /**
     * Get project files
     * @return all files that are in project
     */
    java.io.File[] getFiles ();

    /**
     * Get jar and zip files whitch will be mounted
     * @return jar and zip files whitch are in CLASSPATH of project
     */
    java.io.File[] getJarsAndZips ();

    /**
     * Get Main clas of project.
     * @return MainClass of Project, when project hasn't MainClass, it is returned null
     */
    String getMainClass ();

    /**
     * Get Name of project.
     * @return name of project which be imported
     */
    String getName ();

    /**
     * get System jar and zip files. It can be founded in system configuratin
     * file of environment
     * @return systems classpath
     */
    java.io.File[] getSystemJarsAndZips ();
}

/*
 * Log
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */



