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

/** Support for Executor beans and for their SysProcess subclasses.
*
* @author Ales Novak
*/
public final class DefaultSysProcess extends ExecutorTask {

    /** reference count of instances */
    static int processCount;
    /** reference to SysProcess ThreadGroup */
    private final TaskThreadGroup group;
    /** flag deciding whether is the process destroyed or not */
    private boolean destroyed = false;
    /** InputOutput for this Context */
    private final InputOutput io;
    /** Name */
    private final String name;

    /**
    * @param grp is a ThreadGroup of this process
    */
    public DefaultSysProcess(Runnable run, TaskThreadGroup grp, InputOutput io, String name) {
        super(run);
        group = grp;
        this.io = io;
        this.name = name;
    }

    /** terminates the process by killing all its thread (ThreadGroup) */
    public synchronized void stop() {

        int loopcount = 0;
        if (destroyed) return;
        destroyed = true;
        boolean e;
        do {
            e = false;
            try {
                group.interrupt();
                group.stop();
                group.destroy();
            } catch (IllegalThreadStateException ex) {
                e = true;
            }
            loopcount++;
        } while(e && (!group.isDestroyed() && loopcount < 5));
        ExecutionEngine.closeGroup(group);
        group.kill();  // force RunClass thread get out - end of exec is fired
        notifyFinished();
    }

    /** waits for this process is done
    * @return 0
    */
    public int result() {
        // called by an instance of RunClass thread - kill() in previous stop() forces calling thread
        // return from waitFor()
        group.waitFor();
        notifyFinished();
        return 0;
    }

    /** @return an InputOutput */
    public InputOutput getInputOutput() {
        return io;
    }

    public void run() {
    }

    public String getName() {
        return name;
    }
}

/*
 * Log
 *  10   Gandalf   1.9         11/9/99  Ales Novak      improved killing of 
 *       tasks
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/8/99  Ales Novak      notifyFinished added
 *  7    Gandalf   1.6         10/1/99  Ales Novak      major change of 
 *       execution
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         3/31/99  Ales Novak      
 *  4    Gandalf   1.3         3/31/99  Ales Novak      
 *  3    Gandalf   1.2         3/24/99  Ales Novak      
 *  2    Gandalf   1.1         1/15/99  Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
