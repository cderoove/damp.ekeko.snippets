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

import java.awt.AWTEvent;
import java.awt.EventQueue;

/**
*
* @author Ales Novak
* @version 0.10 Mar 19, 1998
*/
class TaskThreadGroup extends ThreadGroup {

    /** Class object for Thread */
    private static final Class threadClass = Thread.class;

    /** lock for waitFor/wakeUpProcesses */
    private Object TIMER = new Object();

    /** true if RunClass thread entered waitFor - out of main method */
    private boolean runClassThreadOut = false;

    /** true iff the ThreadGroup can be finalized - that is after all threads die */
    private boolean finalizable;

    /** Is the process dead? The group can be marked as dead but still have running threads */
    private boolean dead;

    TaskThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);

        dead = false;
    }

    /**
    * @return true iff there is not any window there is not more than two threads
    * and first one if any must be waiting for next event in the queue (EventDispatchThread) and
    * the second must be instanceof ExecutionEngine$RunClass thread and must wait
    * external processes are never "dead"
    */
    private boolean isProcessDead() {

        if (dead) {
            return true;
        }

        int count;
        // first System.out.println must be before sync block
        if (! finalizable) {
            return false;
        }
        synchronized (this) {
            count = activeCount();

            /* Following lines patch bug in implementaion of java.lang.-Thread/ThreadGroup
               The bug - if new Thread(...) call is issued and the thread is not started.
               The thread is added as a member to its thread group - such threads are counted in
               calls activeCount() and enumerate(Thread[] ts, ...) although they do not live - e.g
               call destroy() to threadgroup always throws an exception. Solution is to start suspicious
               threads. I added try - catch block for already started threads.
               This is patch for some code used inside Swing - code like this " ... = new Thread().getThreadGroup();"
               see - javax.swing.SystemEventQueueUtilities$RunnableCanvas.maybeRegisterEventDispatchThread()
               Original error was that TaskThreadGroups that had not any threads at thread dump were not removed.
               !!! remove spagetti code if the bug is repaired in JDK.
            */
            if (count > 1) {
                int active = 0;
                Thread[] threads =
                    new Thread[count];
                enumerate(threads);
                for (int i = threads.length; --i >= 0;) {
                    if ((threads[i] != null) &&
                            (threads[i].isAlive())
                       ) {
                        if (++active > 1) {
                            return false;
                        }
                    }
                }
                count = active;
            }

            if (ExecutionEngine.hasWindows(this)) {
                return false;
            }

            if (count == 0) {
                return true; // no thread, no wins
            }

            // one thread remains - RunClass
            if (runClassThreadOut) {
                return true;
            }
            return false;
        }
    }

    /** blocks until this ThreadGroup die - isProcessDead = true
    */
    void waitFor() {
        synchronized (TIMER) {
            try {
                while (! isProcessDead()) {
                    if (Thread.currentThread() instanceof ExecutionEngine.RunClass)
                        runClassThreadOut = true;
                    try {
                        TIMER.wait(1000);
                    } catch (InterruptedException ex) {
                    }
                }
            } finally {
                TIMER.notifyAll();
                dead = true;
            }
        }
    }

    /** Marks this group as finalizable. */
    void setFinalizable() {
        finalizable = true;
    }

    /** sets the group as dead */
    void kill() {
        synchronized (TIMER) {
            if (! dead) {
                dead = true;
                TIMER.notifyAll();
                try {
                    TIMER.wait(3000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}

/*
 * Log
 *  10   Gandalf   1.9         11/9/99  Ales Novak      improved killing of 
 *       tasks
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/1/99  Ales Novak      NullPointerException
 *  7    Gandalf   1.6         5/6/99   Ales Novak      not changed
 *  6    Gandalf   1.5         3/31/99  Ales Novak      
 *  5    Gandalf   1.4         3/25/99  Ales Novak      
 *  4    Gandalf   1.3         3/25/99  Ales Novak      
 *  3    Gandalf   1.2         3/24/99  Ales Novak      
 *  2    Gandalf   1.1         1/8/99   Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
