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

package org.openide.filesystems;

/** Signals that the file object is somehow corrupted.
* The required operation is not possible due to a previous deletion, or
* an unexpected (external) change in the file system.
*
* @author Jaroslav Tulach
* @version 0.10 October 7, 1997
*/
public class FileStateInvalidException extends java.io.IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4987532595879330362L;

    /** Create new <code>FileStateInvalidException</code>.
    */
    public FileStateInvalidException () {
        super ();
    }

    /** Create new <code>FileStateInvalidException</code> with the specified text.
    * @param s the text describing the exception
    */
    public FileStateInvalidException (String s) {
        super (s);
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         2/4/99   Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
