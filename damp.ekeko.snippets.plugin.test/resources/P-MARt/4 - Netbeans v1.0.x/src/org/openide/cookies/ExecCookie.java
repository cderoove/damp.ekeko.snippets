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

// XXX huh? --jglick
/* This interface signals that this cookie is able to "start"
* itself. Any operations needed for the start must be implemented
* by this cookie itself.
* <P>
* This is the difference with ExecCookie that only provides information
* about which class to start and does not do any other actions. They
* are handled by execution.
* <P>
* The ExecAction should react to the ExecCookie and also to this StartCookie.
*
* @see ExecCookie
* @see StartCookie
*/
/**
 * Cookie for objects which may be executed.
*
* @author Jaroslav Tulach
* @version 0.10, Jul 27, 1998
*/
public interface ExecCookie extends Node.Cookie {
    /** Start execution.
    */
    public void start ();
}

/*
* Log
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         3/10/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
