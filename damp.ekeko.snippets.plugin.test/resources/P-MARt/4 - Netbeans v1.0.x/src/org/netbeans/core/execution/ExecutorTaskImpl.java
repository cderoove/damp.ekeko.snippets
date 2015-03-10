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

package org.netbeans.core.execution;

import org.openide.execution.ExecutorTask;
import org.openide.windows.InputOutput;

/** Purpose ???
*
* @author Ales Novak
* @version 1.0, November 18, 1998
*/
class ExecutorTaskImpl extends ExecutorTask {
    /** result */
    int result = -1;
    /** SysProcess ref */
    DefaultSysProcess proc;
    /** lock */
    Object lock = this;

    /** constructor */
    ExecutorTaskImpl() {
        super(new Runnable() {
                  public void run() {}
              }
             );
    }

    /** Stops the task. */
    public void stop() {
        try {
            synchronized (lock) {
                while (proc == null) lock.wait();
                proc.stop();
            }
        } catch (InterruptedException e) {
        }
    }
    /** @return result 0 means success. Blocking operation. */
    public int result() {
        waitFinished();
        return result;
    }
    // hack off
    final void finished() {
        notifyFinished();
    }
    public void run() {
        waitFinished();
    }

    public InputOutput getInputOutput() {
        return proc.getInputOutput();
    }
}

/*
 * Log
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/1/99  Ales Novak      major change of 
 *       execution
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/25/99  Petr Jiricka    Fixed bug in tracing the
 *       finished status of the task
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
