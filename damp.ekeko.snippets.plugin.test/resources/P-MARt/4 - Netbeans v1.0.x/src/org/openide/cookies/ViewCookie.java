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

import java.io.IOException;

import javax.swing.text.StyledDocument;

import org.openide.util.Task;
import org.openide.nodes.Node;

/** Cookie permitting objects to be viewed.
*
* @author Jan Jancura
*/
public interface ViewCookie extends Node.Cookie {
    /** Instructs an viewer to be opened. The operation can
    * return immediately and the viewer be opened later.
    * There can be more than one viewer open, so one of them is
    * arbitrarily chosen and opened.
    */
    public void view ();

}

/*
* Log
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         3/10/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/22/99  Jan Jancura     
* $
*/
