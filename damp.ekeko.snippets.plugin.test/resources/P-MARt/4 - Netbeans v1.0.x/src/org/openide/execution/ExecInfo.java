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

package org.openide.execution;

import org.openide.filesystems.FileObject;

/** Provides basic information required to start executing a class.
*
* @author Ales Novak
* @version 0.12 April 23, 1998
*/
public class ExecInfo {

    /** param for execution */
    private String[] argv;
    /** class to exec */
    private String className;

    /** Create a new descriptor.
    * @param className the name of the class to execute
    * @param argv an array of arguments for the class (may be empty but not <code>null</code>)
    */
    public ExecInfo (String className, String[] argv) {
        this.argv = argv;
        this.className = className;
    }

    /** Create a new descriptor with no arguments.
    * @param className the name of the class to execute
    */
    public ExecInfo (String className) {
        this(className, new String[] {});
    }

    /** Get the arguments (typically passed to <code>main(String[])</code>).
    * @return the arguments (never <code>null</code>)
    */
    public String[] getArguments () {
        return argv;
    }

    /** Get the name of the class to execute.
    * This must typically have a <code>public static void main(String[])</code> method.
    * @return the class name
    */
    public String getClassName () {
        return className;
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/23/99  Jesse Glick     [JavaDoc], and no longer
 *       using null for the args list.
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
