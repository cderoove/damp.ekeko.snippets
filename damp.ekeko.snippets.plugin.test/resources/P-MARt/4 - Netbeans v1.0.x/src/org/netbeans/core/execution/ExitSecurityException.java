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

package org.netbeans.core.execution;

/** Thrown during exit of the task, not reported as exception
*
* @author Ales Novak
* @version 0.10 May 14, 1998
*/
public class ExitSecurityException extends SecurityException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8973677308554045785L;

    /** Creates new exception ExitSecurityException
    */
    public ExitSecurityException () {
        super ();
    }

    /** Creates new exception ExitSecurityException with text specified
    * string s.
    * @param s the text describing the exception
    */
    public ExitSecurityException (String s) {
        super (s);
    }

    public void printStackTrace() {
    }
}

/*
 * Log
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         7/28/99  Ales Novak      new window system/#1409
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
