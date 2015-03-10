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

import org.openide.util.Task;
import org.openide.windows.InputOutput;

/* not true  --jglick
* One can check whether
* the execution is finished or not, or
*/
/** A task object that represents an asynchronously
* running execution task.
* Module authors do not need to subclass this.
* @see Executor#execute
* @author Jaroslav Tulach
*/
public abstract class ExecutorTask extends Task {
    /** Create the task.
    * @param run runnable to run that computes the task
    */
    protected ExecutorTask(Runnable run) {
        super (run);
    }

    /** Stop the computation.
    */
    public abstract void stop ();

    /** Check the result of execution. If the execution
    * is not finished, the calling thread is blocked until it is.
    *
    * @return the result of execution. Zero means successful execution; other numbers may indicate various error conditions.
    */
    public abstract int result ();

    /**
    * @return InputOutput assigned to this process
    */
    public abstract InputOutput getInputOutput();
}

/*
* Log
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         10/1/99  Ales Novak      major change of execution
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         3/23/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
