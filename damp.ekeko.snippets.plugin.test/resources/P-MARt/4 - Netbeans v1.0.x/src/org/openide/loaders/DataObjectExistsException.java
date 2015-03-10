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

/** Exception signalling that the data object for this file cannot
* be created because there already is an object for the primary file.
*
* @author Jaroslav Tulach
* @version 0.10, Mar 30, 1998
*/
public class DataObjectExistsException extends java.io.IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4719319528535266801L;
    /** data object */
    private DataObject obj;

    /** Create new exception.
    * @param obj data object which already exists
    */
    public DataObjectExistsException (DataObject obj) {
        this.obj = obj;
    }

    /** Get the object which already exists.
     * @return the data object
    */
    public DataObject getDataObject () {
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
