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

package org.netbeans.modules.debugger.support.util;

import java.util.HashSet;
import java.util.Iterator;

/** A task that may be executed in a separate thread and permits examination of its status.
* Other threads can check if it is finished or wait for it
* to finish.
* <P>
* For example:
* <p><code><PRE>
* Runnable r = new Runnable () {
*   public void run () {
*     // do something
*   }
* };
* Task task = new Task (r);
* RequestProcessor.postRequest (task);
* </PRE></code>
* <p>In a different thread one can then test <CODE>task.isFinished ()</CODE>
* or wait for it with <CODE>task.waitFinished ()</CODE>.
*
* @author Jaroslav Tulach
*/
class Task extends Object implements Runnable {
    /** Dummy task which is already finished. */
    public static final Task EMPTY = new Task(null);

    /** what to run */
    private Runnable run;
    /** flag if we have finished */
    private boolean finished;

    /** Create a new task.
    * The runnable should provide its own error-handling, as
    * by default thrown exceptions are simply logged and not rethrown.
    * @param run runnable to run that computes the task
    */
    public Task(Runnable run) {
        this.run = run;
        if (run == null) {
            finished = true;
        }
    }

    /** Test whether the task has finished running.
    * @return <code>true</code> if so
    */
    public final boolean isFinished () {
        return finished;
    }

    /** Wait until the task is finished.
    */
    public final void waitFinished () {
        waitFinishedImpl();
    }

    /** This method is an implementation of the waitFinished method
    * This implemetation run the task and then returns.
    */
    void waitFinishedImpl () {
        if (!finished) {
            synchronized (this) {
                while (!finished) {
                    try {
                        wait ();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }

    /** Notify all waiters that this task has finished.
    * @see #run
    */
    protected final void notifyFinished () {

        Iterator it;

        synchronized (this) {
            finished = true;
            notifyAll ();
        }

    }

    /** Start the task.
    * When it finishes (even with an exception) it calls
    * {@link #notifyFinished}.
    * Subclasses may override this method, but they
    * then need to call {@link #notifyFinished} explicitly.
    * <p>Note that this call runs synchronously, but typically the creator
    * of the task will call this method in a separate thread.
    */
    public void run () {
        try {
            finished = false;
            if (run != null) run.run ();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath)t;
            }
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                System.out.println("Exception occurred in request processor:");
                t.printStackTrace ();
            }
        } finally {
            notifyFinished ();
        }
    }

    public String toString () {
        return "task " + run;
    }
}

/*
* Log
*  1    Jaga      1.0         3/27/00  Daniel Prusa    
* $
*/
