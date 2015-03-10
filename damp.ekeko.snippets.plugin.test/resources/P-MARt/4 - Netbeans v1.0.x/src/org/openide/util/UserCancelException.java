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

package org.openide.util;

/** Exception that is thrown when user cancels interaction so the
* requested result cannot be produced.
*
* @author Jaroslav Tulach
* @version 0.10, Jan 26, 1998
*/
public class UserCancelException extends java.io.IOException {
    static final long serialVersionUID =-935122105568373266L;
    /** Creates new exception UserCancelException
    */
    public UserCancelException () {
        super ();
    }

    /** Creates new exception UserCancelException with text specified
    * string s.
    * @param s the text describing the exception
    */
    public UserCancelException (String s) {
        super (s);
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
