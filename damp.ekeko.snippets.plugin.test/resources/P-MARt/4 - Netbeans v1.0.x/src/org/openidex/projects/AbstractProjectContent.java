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

import java.util.*;

import org.openide.util.SharedClassObject;
import org.openide.util.io.NbMarshalledObject;

/**
 *
 * @author  mryzl
 */

abstract public class AbstractProjectContent extends Object implements ProjectContent {

    /** super project link */
    protected ProjectContent superProject;

    /** projects 
     * @associates ProjectContent*/
    protected Collection projects = new LinkedList();

    /** Creates new ProjectContentImpl.
    */
    public AbstractProjectContent() {
    }

    /**
    * @return super project content or null if there is not one
    */
    public ProjectContent getSuperProject() {
        return superProject;
    }

    /**
    * @return true if this ProjectContent is read only
    */
    public boolean isReadOnly() {
        return false;
    }

    /**
    * @return get subprojects
    */
    public ProjectContent[] getProjects() {
        return (ProjectContent[]) projects.toArray(new ProjectContent[] {});
    }

    /** Add a new subproject.
    * @param project - project to be added
    */
    public void addProject(ProjectContent project) {
        projects.add(project);
    }

    /** Create a new subproject.
    */
    public abstract ProjectContent createProject() throws java.io.IOException;

    /** Remove the subproject.
    * @param project - project to be removed
    */
    public void removeProject(ProjectContent project) {
        projects.remove(project);
    }

}

/*
* Log
*  2    Gandalf   1.1         12/22/99 Martin Ryzl     
*  1    Gandalf   1.0         12/20/99 Martin Ryzl     
* $ 
*/ 
