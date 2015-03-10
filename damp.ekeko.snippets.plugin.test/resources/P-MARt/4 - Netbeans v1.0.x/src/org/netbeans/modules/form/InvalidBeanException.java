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

package org.netbeans.modules.form;

/** The InvalidBeanException is thrown when an invalid bean used in
* FormEditor is used.
*
* @author Ian Formanek, Petr Hamernik
* @version 0.10, May 14, 1998
*/
public class InvalidBeanException extends Exception {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1338307778718678583L;

    /** Constructs a new InvalidBeanException */
    public InvalidBeanException () {
    }

    /** Constructs a new InvalidBeanException for specified underlying exception*/
    public InvalidBeanException (Exception e) {
        super (e.getClass ().getName ());
        exception = e;
    }

    /** Constructs a new InvalidBeanException with specified string description */
    public InvalidBeanException (String s) {
        super (s);
    }

    /** @return An underlying exception that caused the bean to be invalid
    * or null if not specified 
    */
    public Exception getException () {
        return exception;
    }

    /** The underlying exception that caused the bean to be invalid */
    private Exception exception;
}

/*
 * Log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         3/26/99  Ian Formanek    
 * $
 */
