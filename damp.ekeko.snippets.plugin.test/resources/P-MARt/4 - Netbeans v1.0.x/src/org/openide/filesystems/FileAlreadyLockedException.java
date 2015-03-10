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

/** Exception raised when a file is already locked.
*
* @see FileObject#lock
*
* @author Jaroslav Tulach
* @version 0.10 September 11, 1997
*/
public class FileAlreadyLockedException extends java.io.IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4466344756249789982L;

    /** Creates new <code>FileAlreadyLockedException</code>.
    */
    public FileAlreadyLockedException () {
        super ();
    }

    /** Creates new <code>FileAlreadyLockedException</code> with specified text.
    *
    * @param s the text describing the exception
    */
    public FileAlreadyLockedException (String s) {
        super (s);
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         2/1/99   Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
