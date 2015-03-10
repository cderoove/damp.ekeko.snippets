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

package org.netbeans.modules.projects.content;

import org.openide.filesystems.*;
import org.openidex.projects.*;

/**
 *
 * @author  mryzl
 */

public class XMLProjectContentFactory implements ProjectContentFactory {

    FileObject folder;

    /** Creates new XMLProjectContentFactory with specific folder.
    */
    public XMLProjectContentFactory(FileObject folder) {
        this.folder = folder;
    }

    /** Create a new ProjectConent.
    */
    public ProjectContent createProjectContent(ProjectContent superProject) throws java.io.IOException {
        return new XMLProjectContent(folder, superProject);
    }
}

/*
* Log
*  2    Gandalf   1.1         1/13/00  Martin Ryzl     heavy localization
*  1    Gandalf   1.0         12/22/99 Martin Ryzl     
* $ 
*/ 
