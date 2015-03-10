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

/** Should be thrown when a feature is not implemented.
* Usage of this exception should allow us to distingush between
* errors and unimplemented features.
* <P>
* Also this exception can easily be located in source code. That is
* why finding of unimplemented features should be simplified.
*
* @author Jaroslav Tulach
* @version 0.10 September 25, 1997
*/
public class NotImplementedException extends RuntimeException {

    static final long serialVersionUID =465319326004943323L;
    /** Creates new exception NotImplementedException
    */
    public NotImplementedException () {
        super ();
    }

    /** Creates new exception NotImplementedException with text specified
    * string s.
    * @param s the text describing the exception
    */
    public NotImplementedException (String s) {
        super (s);
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
