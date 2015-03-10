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

package org.openide.compiler;

/** Exception thrown from {@link CompilationEngine#createCompilerGroups}.
* Module authors need not pay attention.
*
* @author Jaroslav Tulach
*/
public class CompilerGroupException extends Exception {
    /** Th class which was not able to produce a valid instance. */
    public Class compilerGroupClass;
    /** The enclosed exception. */
    public Exception exception;

    static final long serialVersionUID =-5410528565078219787L;
    /** Constructor.
    * @param c class for which we cannot create the instance
    * @param ex the exception that occurred
    */
    public CompilerGroupException (Class c, Exception ex) {
        compilerGroupClass = c;
        exception = ex;
    }

}

/*
* Log
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         3/24/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
