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

/** Cookie permitting objects to be edited.
*
* @author Jaroslav Tulach
*/
public interface EditCookie extends Node.Cookie {
    /** Instructs an editor to be opened. The operation can
    * return immediately and the editor be opened later.
    * There can be more than one editor open, so one of them is
    * arbitrarily chosen and opened.
    */
    public void edit ();
}

/*
* Log
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         7/12/99  Libor Kramolis  
*  1    Gandalf   1.0         7/9/99   Jaroslav Tulach 
* $
*/
