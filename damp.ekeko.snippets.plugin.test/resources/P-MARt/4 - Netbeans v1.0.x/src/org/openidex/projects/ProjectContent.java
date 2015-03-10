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

package org.openidex.projects;

import java.io.IOException;

import org.openide.util.io.NbMarshalledObject;

/**
 *
 * @author  mryzl
 */

public interface ProjectContent extends java.io.Serializable {

    /** Get super project of the project.
    * @return super project or null if there is no one
    */
    public ProjectContent getSuperProject();

    /** Get control panel of the project.
    * @param force force creating new instead of throwing an exception
    * @return control panel
    */
    public SettingsSet getControlPanel(boolean force) throws IOException;

    /** Get set of loaders specific for this project.
    * @param force force creating new instead of throwing an exception
    * @return loaders
    */
    public SettingsSet getLoaderPool(boolean force) throws IOException;

    /** Get set of modules specific for the project.
    * @param force force creating new instead of throwing an exception
    * @return loaders
    */
    public SettingsSet getModules(boolean force) throws IOException;

    /** Get repository settings for the project.
    * @param force force creating new instead of throwing an exception
    * @return repository diff set
    */
    public DiffSet getRepository(boolean force) throws IOException;

    /** Get services settings for the project.
    * @param force force creating new instead of throwing an exception
    * @return services diff set
    */
    public DiffSet getServices(boolean force) throws IOException;

    /** Get windows for the project.
    * @return window manager
    */
    public NbMarshalledObject getWindowManager() throws java.io.IOException;

    /** Store windows of the project.
    * @return window manager
    */
    public void storeWindowManager() throws java.io.IOException;

    /**
    * @return true if the project is read only
    */
    public boolean isReadOnly();


    /** Get all subprojects.
    * @return an array of all projects
    */
    public ProjectContent[] getProjects();

    /** Add subproject.
    * @param project - a new subproject
    */
    public void addProject(ProjectContent project);

    /** Create subproject.
    */
    public ProjectContent createProject() throws java.io.IOException;

    /** Remove subproject.
    * @param project - project to be removed.
    */
    public void removeProject(ProjectContent project);

    /** Store the project.
    */
    public void store() throws java.io.IOException;
}

/*
* Log
*  4    Gandalf   1.3         2/4/00   Martin Ryzl     fixed
*  3    Gandalf   1.2         1/17/00  Martin Ryzl     
*  2    Gandalf   1.1         1/4/00   Martin Ryzl     
*  1    Gandalf   1.0         12/20/99 Martin Ryzl     
* $ 
*/ 
