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

package org.openide.debugger;

/**
* An exception occurring in the debugger.
* This exception
* is thrown in the debugger from all methods which may involve running user code.
*
* @author   Jan Jancura
* @version  0.11, Jan 30, 1998
*/
public class DebuggerException extends java.lang.reflect.InvocationTargetException {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -3112649137515905742L;

    /**
    * Construct a <code>DebuggerException</code> for a specified inner exception.
    * @param throwable the basic exception
    */
    public DebuggerException (Throwable throwable) {
        super (throwable);
    }

    /**
    * Construct a <code>DebuggerException</code> with a description.
    *
    * @param message message text
    */
    public DebuggerException (String description) {
        super (new Exception (), description);
    }

    /**
    * Construct a <code>DebuggerException</code> with a description and base error.
    *
    * @param message description of the exception
    * @param throwable original exception
    */
    public DebuggerException (String description, Throwable throwable) {
        super (throwable, description);
    }

}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/22/99  Jesse Glick     [JavaDoc] and removed 
 *       "message" parameter.
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
