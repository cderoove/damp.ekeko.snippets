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

package org.openide.modules;

/**
* Thrown to indicate that there was a problem installing or otherwise handling a module.
*
* @author Jaroslav Tulach
* @version 0.10, Aug 13, 1998
*/
public class IllegalModuleException extends java.io.IOException {
    private Exception exc = null;

    static final long serialVersionUID =4570383937614537348L;
    /** Create exception. */
    public IllegalModuleException () {
    }

    /** Create exception with detail string.
     * @param msg message for the exception */
    public IllegalModuleException (String msg) {
        super (msg);
    }

    /** Create exception based on another checked exception.
    * @param exc the underlying exception
    */
    public IllegalModuleException (Exception exc) {
        super (exc.toString ());
        this.exc = exc;
    }

    /** Get the underlying exception, if any.
    * @return the exception or <code>null</code>
    */
    public Exception getException () {
        return exc;
    }

    public String getMessage () {
        if (exc != null)
            return exc.getMessage ();
        else
            return super.getMessage ();
    }
}

/*
* Log
*  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         4/28/99  Jesse Glick     
*  3    Gandalf   1.2         4/28/99  Jesse Glick     Added delegating 
*       constructor.
*  2    Gandalf   1.1         3/5/99   Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
