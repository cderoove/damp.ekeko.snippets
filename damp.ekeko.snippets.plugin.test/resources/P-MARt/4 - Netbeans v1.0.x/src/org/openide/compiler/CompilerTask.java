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

import org.openide.util.Task;

/** Represents an asynchronously
* running compilation task.
* One can stop the computation or check whether it was successful.
*
* @author Jaroslav Tulach
*/
public abstract class CompilerTask extends Task {
    /** Create the task.
    * @param run runnable to run
    */
    protected CompilerTask (Runnable run) {
        super (run);
    }

    /** Stop the computation.
    */
    public abstract void stop ();

    /** Test the result of compilation. If the compilation
    * is not finished, the calling thread will be blocked until it is.
    *
    * @return <code>true</code> if the compilation was successful,
    *        <code>false</code> if there was a problem
    */
    public abstract boolean isSuccessful ();
}

/*
* Log
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         3/24/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
