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

/** Encapsulates other exceptions thrown from a mutex method.
*
* @see Mutex.ExceptionAction
* @see Mutex#readAccess(Mutex.ExceptionAction)
* @see Mutex#writeAccess(Mutex.ExceptionAction)
*
* @author Jaroslav Tulach
* @version 1.00, Sep 3, 1998
*/
public class MutexException extends Exception {
    /** encapsulate exception*/
    private Exception ex;

    static final long serialVersionUID =2806363561939985219L;
    /** Create an encapsulated exception.
    * @param ex the exception
    */
    public MutexException(Exception ex) {
        this.ex = ex;
    }

    /** Get the encapsulated exception.
    * @return the exception
    */
    public Exception getException () {
        return ex;
    }

}

/*
* Log
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         5/14/99  Jesse Glick     [JavaDoc]
*  2    Gandalf   1.1         4/19/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
