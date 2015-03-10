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

package org.openide.loaders;

import org.openide.filesystems.FileObject;

/** Exception signalling that the data object for a given file object could not
* be found in {@link DataObject#find}.
*
* @author Jaroslav Tulach
* @version 0.10, Mar 31, 1998
*/
public class DataObjectNotFoundException extends java.io.IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1646623156535839081L;
    /** data object */
    private FileObject obj;

    /** Create a new exception.
    * @param obj the file that does not have a data object
    */
    public DataObjectNotFoundException (FileObject obj) {
        this.obj = obj;
    }

    /** Get the file which does not have a data object.
     * @return the file
    */
    public FileObject getFileObject () {
        return obj;
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/9/99   Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
