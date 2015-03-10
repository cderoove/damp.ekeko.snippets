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

package org.openide.cookies;

import java.io.*;

import org.openide.nodes.Node;

/** A cookie that provides project manipulation functionality.
* Such a cookie should be attached to any data object that represents
* a "Project" in the IDE.
* <P>
* A project should consist of a set of settings. When the project
* is opened, it should control the current settings of the
* IDE (change workplaces, control panel options, and the content of
* filesystem pool). Moreover the project has a so-called "Project Desktop"
* that should provide a default working node for the developer to store working
* files, and also to describe the capabilities of the project (for example
* if the project can the compiled, the node should support {@link CompilerCookie}).
* <P>
* To let the project save settings when explicitly asked to or when the project is 
* closed, {@link #projectSave} is used. It is called when another project is opened. 
* The method should store the current state of IDE to be restored on next opening.
* <P>
* To allow the IDE to remember the current project when it the IDE is closed
* and to reopen it on next startup, the project is required to be serializable.
*
* @author Jaroslav Tulach
*/
public interface ProjectCookie extends Node.Cookie, java.io.Serializable {
    /** Open the project by loading its settings into the IDE.
    *
    * @exception IOException if an error occurred during opening of the project
    */
    public void projectOpen () throws IOException;

    /** Save the project. This method instructs the project to
    * store the current settings of the IDE (that could be modified during
    * work in the project) to be restored on the next open of the project.
    * It is up to the project to decide which settings to store and how.
    *
    * @exception IOException if an error occurs during saving
    */
    public void projectSave () throws IOException;

    /** Close the project. This method instructs the project that another project
     * is becoming the active project and that the project can drop allocated 
     * resources.
     *
     * @exception IOException if an error occurs during saving
     */
    public void projectClose () throws IOException;

    /** Get the "Project Desktop" node.
     * This should contain
    * the main functionality associated with the project. For example, if the
    * project can be compiled or executed, appropriate cookies ({@link CompilerCookie},
    * {@link ExecCookie}) should be attached to this node.
    * <P>
    * Moreover such a node may allow the user to store his "working files" to
    * simplify access to them.
    *
    * @return the project destop node
    */
    public Node projectDesktop ();
}

/*
* Log
*  8    src-jtulach1.7         1/14/00  Martin Ryzl     projectClose() added
*  7    src-jtulach1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    src-jtulach1.5         8/19/99  Ian Formanek    Removed serial version 
*       UID from interface
*  5    src-jtulach1.4         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  4    src-jtulach1.3         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    src-jtulach1.2         5/30/99  Ian Formanek    renamed projectClose () -
*       > projectSave ()
*  2    src-jtulach1.1         3/10/99  Jesse Glick     [JavaDoc]
*  1    src-jtulach1.0         1/20/99  Jaroslav Tulach 
* $
*/
