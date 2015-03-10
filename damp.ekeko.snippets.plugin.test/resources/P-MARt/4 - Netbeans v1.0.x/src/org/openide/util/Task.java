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

package org.openide.util;

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
public class Task extends Object implements Runnable {
    /** Dummy task which is already finished. */
    public static final Task EMPTY = new Task(null);

    /** what to run */
    private Runnable run;
    /** flag if we have finished */
    private boolean finished;
    /** listeners for the finish of task (TaskListener) 
     * @associates TaskListener*/
    private HashSet list;

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

            // fire the listeners
            if (list == null) return;

            it = ((HashSet)list.clone ()).iterator ();
        }

        while (it.hasNext ()) {
            TaskListener l = (TaskListener)it.next ();
            l.taskFinished (this);
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
                System.out.println("Exception occurred in request processor:"); // NOI18N
                t.printStackTrace ();
            }
        } finally {
            notifyFinished ();
        }
    }

    /** Add a listener to the task.
    * @param l the listener to add
    */
    public synchronized void addTaskListener (TaskListener l) {
        if (list == null) list = new HashSet ();
        list.add (l);
        if (finished) {
            l.taskFinished(this);
        }
    }

    /** Remove a listener from the task.
    * @param l the listener to remove
    */
    public synchronized void removeTaskListener (TaskListener l) {
        if (list == null) return;
        list.remove (l);
    }

    public String toString () {
        return "task " + run; // NOI18N
    }
}

/*
* Log
*  14   Gandalf   1.13        1/12/00  Pavel Buzek     I18N
*  13   Gandalf   1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  12   Gandalf   1.11        8/11/99  Jaroslav Tulach Contains debugging code
*  11   Gandalf   1.10        7/28/99  Jaroslav Tulach Solves LineSet 
*       starvation.
*  10   Gandalf   1.9         7/25/99  Ian Formanek    Exceptions printed to 
*       console only on "netbeans.debug.exceptions" flag
*  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  8    Gandalf   1.7         5/16/99  Ian Formanek    Better safety against 
*       errors in tasks
*  7    Gandalf   1.6         5/15/99  Jesse Glick     [JavaDoc]
*  6    Gandalf   1.5         4/24/99  Jaroslav Tulach 
*  5    Gandalf   1.4         4/21/99  Petr Hamernik   empty task is finished.
*  4    Gandalf   1.3         4/13/99  Petr Hamernik   deadlocks prevention
*  3    Gandalf   1.2         4/8/99   Petr Hamernik   checking deadlocks in 
*       RequestProcessor
*  2    Gandalf   1.1         2/11/99  David Simonek   
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
