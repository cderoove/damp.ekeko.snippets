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

import org.openide.text.Line;
import org.openide.nodes.Node;

/** Cookie for data objects that want to provide support for accessing
* lines in a document.
* Lines may change absolute position as changes are made around them in a document.
*
* @see Line
* @see Line.Set
*
* @author Jaroslav Tulach
*/
public interface LineCookie extends Node.Cookie {
    /** Creates new line set.
    *
    * @return line set for current state of the node
    */
    public Line.Set getLineSet ();
}

/*
 * Log
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         3/10/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         2/1/99   Jaroslav Tulach 
 *  3    Gandalf   1.2         1/28/99  Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
