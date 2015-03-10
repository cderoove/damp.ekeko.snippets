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

import org.openide.nodes.Node;

/** The cookie for the save operation.
*
* @author Dafe Simonek
*/
public interface SaveCookie extends Node.Cookie {

    /** Invoke the save operation.
     * @throws IOException if the object could not be saved
     */
    public void save () throws java.io.IOException;

}

/*
* Log
*  4    src-jtulach1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    src-jtulach1.1         3/10/99  Jesse Glick     [JavaDoc]
*  1    src-jtulach1.0         1/14/99  David Simonek   
* $
*/
