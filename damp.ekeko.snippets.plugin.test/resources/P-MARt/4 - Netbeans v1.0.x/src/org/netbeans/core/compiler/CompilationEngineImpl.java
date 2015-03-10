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

package org.netbeans.core.compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.File;
import java.io.FileFilter;
import java.lang.ref.SoftReference;

import org.openide.compiler.DependencyException;
import org.openide.compiler.CompilationEngine;
import org.openide.compiler.CompilerTask;
import org.openide.compiler.CompilerGroup;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.CompilerGroupException;
import org.openide.compiler.CompilerListener;

/** A class that makes compiling.
*
* @author Ales Novak
*/
public class CompilationEngineImpl extends CompilationEngine {

    /** a queue 
     * @associates Object*/
    private LinkedList queue;
    /** a thread */
    private CompilerThread t;
    /** an event listener */
    CompilerDisplayer displayer;

    /** new CompilationEngineImpl */
    public CompilationEngineImpl() {
        queue = new LinkedList();
        displayer = new CompilerDisplayer();
        t = new CompilerThread(queue, displayer);
        t.start();
    }

    /** Starts asynchronous compilation of a compiler job.
    * @param job the job to compile
    * @return the task object, one can wait for it to finish
    *    and obtain its results
    */
    protected CompilerTask start(CompilerJob job) {
        synchronized (queue) {
            Object[] twins = new Object[2];
            twins[0] = job;
            CompilerTaskImpl task = new CompilerTaskImpl(job, this, twins);
            twins[1] = task;
            queue.addLast(twins);
            queue.notify();
            if (! t.isAlive()) {
                (t = new CompilerThread(queue, displayer)).start();
            } else {
                Thread current = Thread.currentThread();
                if (current.getClass() == CompilerThread.GroupCompiler.class) {
                    // deadlock avoidance
                    t.stopIt();
                    // new thr
                    (t = new CompilerThread(queue, displayer)).start();
                }
            }
            return task;
        }
    }

    // not API inherited methods - user must cast to this Class

    /** Restarts compiler thread. */
    public void stop() {
        synchronized (queue) {
            t.stopIt();
            CompilerThread.GroupCompiler.interruptAll();
            Iterator it = queue.iterator();
            while (it.hasNext()) {
                ((CompilerTaskImpl) ((Object[]) it.next())[1]).done();
            }
            queue.clear();
            queue.notify();
            t = new CompilerThread(queue, displayer);
            t.start();
        }
    }

    /** @return <tt>true</tt> if compiling is executed */
    public boolean isCompiling() {
        return CompilerThread.GroupCompiler.all.size() > 0;
    }

    /** stops specified job */
    void stopTask(CompilerTaskImpl job) {
        synchronized (queue) {
            if (queue.remove(job.ref)) {
                return;
            } else {
                stop();
            }
        }
    }

    /** makes it public */
    static List createLevels(CompilerJob job) throws DependencyException {
        return createComputationLevels(job);
    }

    static Collection createGroups(Collection c) throws CompilerGroupException {
        return createCompilerGroups(c);
    }

    /** A thread of control that compiles. */
    protected static class CompilerThread extends Thread {
        /** a queue */
        public LinkedList queue;
        /** an EventListener */
        public CompilerDisplayer displayer;
        /** stop flag */
        private boolean stop;
        /** current job */
        public CompilerJob currentJob;
        /** current task */
        public CompilerTaskImpl currentTask;

        /** new thread */
        public CompilerThread(LinkedList queue, CompilerDisplayer displayer) {
            setName("Compilation"); // NOI18N
            setPriority(2);
            setDaemon(true);
            this.queue = queue;
            this.displayer = displayer;
        }

        /** stops the thread */
        public void stopIt() {
            stop = true;
        }

        /** @return next CompilerJob from the queue */
        private void nextJobAndTask() throws InterruptedException {
            synchronized (queue) {
                currentJob = null;
                while (queue.size() == 0) {
                    queue.wait();
                }
                Object[] twins = (Object[]) queue.removeFirst();
                currentJob = (CompilerJob) twins[0];
                currentTask = (CompilerTaskImpl) twins[1];
            }
        }

        /** a run method */
        public void run() {

            ListIterator iterator;
            CompilerGroup[] groups;
            GroupCompiler compiler;

            boolean success;
            for (;!stop;) {
                try {
                    iterator = null;
                    groups = new CompilerGroup[0];
                    compiler = null;

                    nextJobAndTask();  // set currentTask and currentJob
                    displayer.compilationStarted(currentTask);
                    iterator = createLevels(currentJob).listIterator();
                    success = true;
                    while (iterator.hasNext() && success) { // through levels  // if one level fails - stop it
                        groups = (CompilerGroup[]) createGroups((Collection) iterator.next()).toArray(groups);
                        // an array of groups - do parallel compilation
                        compiler = null;
                        for (int i = 0; i < groups.length; i++) {
                            if (groups[i] == null) break;
                            groups[i].addCompilerListener(displayer);
                            compiler = new GroupCompiler(groups[i], compiler);
                        }
                        if (compiler != null) {  // is not upToDate
                            success &= compiler.stay(displayer);  // wait for one level
                        }
                    }
                    currentTask.success = success;
                } catch (ThreadDeath td) {
                    stop = true;
                } catch (InterruptedException e) { // ignore
                    if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace();
                    currentTask.success = false;
                } catch (Throwable t) {
                    if (System.getProperty ("netbeans.debug.exceptions") != null) t.printStackTrace();
                    currentTask.success = false;
                } finally {
                    if (currentTask != null) {
                        currentTask.done();
                        displayer.compilationFinished(currentTask);
                    }
                    currentJob = null;
                    currentTask = null;

                    iterator = null;
                    groups = null;
                    compiler = null;
                }
            }
        }
        /** Compiles CompilerGroup */
        protected static class GroupCompiler extends Thread {
            /**
             * @associates GroupCompiler 
             */
            static Hashtable all = new Hashtable(11);

            public CompilerGroup grp;
            public GroupCompiler parent;
            public boolean status;
            public GroupCompiler(CompilerGroup grp, GroupCompiler parent) {
                this.grp = grp;
                this.parent = parent;
                status = true;  // OK
                this.start();
            }

            /* called by compilation thread */
            public boolean stay(CompilerListener listener) throws InterruptedException {
                try {
                    all.put(this, this);
                    join();
                    grp.removeCompilerListener(listener);
                    if (parent != null) {
                        return parent.stay(listener) && status;
                    } else {
                        return status;
                    }
                } finally {
                    all.remove(this);
                }
            }

            public void run() {
                try {
                    status = grp.start();
                } catch (Throwable t) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                        t.printStackTrace();
                    }
                    status = false;
                }
            }

            /** interrupts all groups threads */
            static void interruptAll() {
                Enumeration e = all.keys();
                while (e.hasMoreElements()) {
                    Thread tt = (Thread) e.nextElement();
                    tt.interrupt();
                }
            }
        }
    }


}

/*
 * Log
 *  17   Gandalf   1.16        1/12/00  Ales Novak      i18n
 *  16   Gandalf   1.15        1/12/00  Ales Novak      stopAction
 *  15   Gandalf   1.14        12/23/99 Jaroslav Tulach 
 *  14   Gandalf   1.13        11/9/99  Ales Novak      better report of 
 *       exceptions
 *  13   Gandalf   1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        10/5/99  Ales Novak      NullPointerException fix
 *  11   Gandalf   1.10        10/1/99  Ales Novak      major change of 
 *       execution
 *  10   Gandalf   1.9         7/24/99  Ian Formanek    Printing stack trace on 
 *       netbeans.debug.exceptions property only
 *  9    Gandalf   1.8         7/21/99  Ales Novak      deadlock
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         5/31/99  Jaroslav Tulach External Execution & 
 *       Compilation
 *  6    Gandalf   1.5         5/17/99  Ales Novak      bugfix #1773
 *  5    Gandalf   1.4         5/7/99   Ales Novak      getAllLibraries moved to
 *       CompilationEngine
 *  4    Gandalf   1.3         4/28/99  Ales Novak      fixed changes from Task
 *  3    Gandalf   1.2         4/23/99  Ales Novak      compilation cancelled 
 *       after one level fails
 *  2    Gandalf   1.1         3/18/99  Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
