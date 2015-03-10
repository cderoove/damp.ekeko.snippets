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

import java.awt.EventQueue;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.SwingUtilities;

/** Read-many/write-one lock.
* Allows control over resources that
* can be read by several readers at once but only written by one writer.
* <P>
* It is guaranteed that if you are a writer you can also enter the
* mutex as a reader. Conversely, if you are the <em>only</em> reader you
* are allowed to enter the mutex as a writer.
* <P>
* If the mutex is used only by one thread, the thread can repeatedly
* enter it as a writer or reader. So one thread can never deadlock itself,
* whichever order operations are performed in.
* <P>
* There is no strategy to prevent starvation.
* Even if there is a writer waiting to enter, another reader might enter
* the section instead.
* <P>
* Examples of use:
*
* <p><code><PRE>
* Mutex m = new Mutex ();
* 
* // Grant write access, compute an integer and return it:
* return (Integer)m.writeAccess (new Mutex.Action () {
*   public Object run () {
*     return new Integer (1);
*   }
* });
* 
* // Obtain read access, do some computation, possibly throw an IOException:
* try {
*   m.readAccess (new Mutex.ExceptionAction () {
*     public Object run () throws IOException {
*       if (...) throw new IOException ();
* 
*       return null;
*     }
*   });
* } catch (MutexException ex) {
*   throw (IOException)ex.getException ();
* }
* </PRE></code>
*
* @author Ales Novak
*/
public final class Mutex extends Object {
    /** Mutex that allows code to be synchronized with the AWT event dispatch thread. */
    public static final Mutex EVENT = new Mutex (null);

    // lock mode constants
    /** Lock free */
    private static final int NONE = 0x0;
    /** Enqueue all requests */
    private static final int CHAIN = 0x1;
    /** eXclusive */
    private static final int X = 0x2;
    /** Shared */
    private static final int S = 0x3;

    /** number of modes */
    private static final int MODE_COUNT = 0x4;

    /** timeout for awttreelock holder */
    private static final long TIMEOUT = 30000L;

    /** compatibility matrix */ // [requested][granted]
    private static final boolean[][] cmatrix = {null, null, // NONE, CHAIN
            /* NONE */ /* CHAIN */ /* X */  /* S */  // granted
            /*r X */     {true,     false,      false,   false},
            /*e S */     {true,     false,      false,   true}
            /*q */
            //uested
                                               };

    /** Decides whether two locks are compatible.ÿ
     * @param grantedÿ
     * @param requestedÿ
     * @return <tt>true</tt> iff they are compatibleÿ
     */
    private static boolean compatibleLocks(int granted, int requested) {
        return cmatrix[requested][granted];
    }
    /** granted mode */
    private int grantedMode = NONE;
    /** protects internal data structures */
    private /*final*/ Object LOCK;
    /** threads that - owns or waits for this mutex 
     * @associates ThreadInfo*/
    private /*final*/ Map registeredThreads;
    /** number of threads that holds S mode (readersNo == "count of threads in registeredThreads that holds S") */ // NOI18N
    private int readersNo = 0;
    /** a queue of waiting threads for this mutex 
     * @associates QueueCell*/
    private List waiters;

    /** Enhanced constructor that permits specifying an object to use as a lock.
    * The lock is used on entry and exit to {@link #readAccess} and during the
    * whole execution of {@link #writeAccess}. The ability to specify locks
    * allows several <code>Mutex</code>es to synchronize on one object or to synchronize
    * a mutex with another critical section.
    *
    * @param lock lock to use
    */
    public Mutex (Object lock) {
        this.LOCK = lock;
        this.registeredThreads = new HashMap(7);
        this.waiters = new LinkedList();
    }

    /** Default constructor.
    */
    public Mutex() {
        this(new InternalLock());
    }

    /** Run an action only with read access.
    * See class description re. entering for write access within the dynamic scope.
    * @param action the action to perform
    * @return the object returned from {@link Mutex.Action#run}
    */
    public Object readAccess (Action action) {

        if (this == EVENT) {
            try {
                return doEventAccess (action);
            } catch (MutexException e) {
                throw new InternalError();
            }
        }

        Thread t = Thread.currentThread();
        readEnter(t);
        try {
            return action.run();
        } finally {
            leave(t);
        }
    }

    /** Run an action with read access and possibly throw a checked exception.
    * The exception if thrown is then encapsulated
    * in a <code>MutexException</code> and thrown from this method. One is encouraged
    * to catch <code>MutexException</code>, obtain the inner exception, and rethrow it.
    * Here is an example:
    * <p><code><PRE>
    * try {
    *   mutex.readAccess (new ExceptionAction () {
    *     public void run () throws IOException {
    *       throw new IOException ();
    *     }
    *   });
    *  } catch (MutexException ex) {
    *    throw (IOException) ex.getException ();
    *  }
    * </PRE></code>
    * Note that <em>runtime exceptions</em> are always passed through, and neither
    * require this invocation style, nor are encapsulated.
    * @param action the action to execute
    * @return the object returned from {@link Mutex.ExceptionAction#run}
    * @exception MutexException encapsulates a user exception
    * @exception RuntimeException if any runtime exception is thrown from the run method
    * @see #readAccess(Mutex.Action)
    */
    public Object readAccess (ExceptionAction action) throws MutexException {

        if (this == EVENT) {
            return doEventAccess (action);
        }

        Thread t = Thread.currentThread();
        readEnter(t);
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MutexException(e);
        } finally {
            leave(t);
        }
    }

    /** Run an action with read access, returning no result.
    * It may be run asynchronously.
    *
    * @param action the action to perform
    * @see #readAccess(Mutex.Action)
    */
    public void readAccess (final Runnable action) {

        if (this == EVENT) {
            doEvent (action);
            return;
        }

        Thread t = Thread.currentThread();
        readEnter(t);
        try {
            action.run();
        } finally {
            leave(t);
        }
    }


    /** Run an action with write access.
    * The same thread may meanwhile reenter the mutex; see the class description for details.
    *
    * @param action the action to perform
    * @return the result of {@link Mutex.Action#run}
    */
    public Object writeAccess (Action action) {

        if (this == EVENT) {
            try {
                return doEventAccess (action);
            } catch (MutexException e) {
                throw new InternalError();
            }
        }

        Thread t = Thread.currentThread();
        writeEnter(t);
        try {
            return action.run();
        } finally {
            leave(t);
        }
    }

    /** Run an action with write access and possibly throw an exception.
    * Here is an example:
    * <p><code><PRE>
    * try {
    *   mutex.writeAccess (new ExceptionAction () {
    *     public void run () throws IOException {
    *       throw new IOException ();
    *     }
    *   });
    *  } catch (MutexException ex) {
    *    throw (IOException) ex.getException ();
    *  }
    * </PRE></code>
    *
    * @param action the action to execute
    * @return the result of {@link Mutex.ExceptionAction#run}
    * @exception MutexException an encapsulated checked exception, if any
    * @exception RuntimeException if a runtime exception is thrown in the action
    * @see #writeAccess(Mutex.Action)
    * @see #readAccess(Mutex.ExceptionAction)
    */
    public Object writeAccess (ExceptionAction action) throws MutexException {

        if (this == EVENT) {
            return doEventAccess (action);
        }

        Thread t = Thread.currentThread();
        writeEnter(t);
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MutexException(e);
        } finally {
            leave(t);
        }
    }

    /** Run an action with write access and return no result.
    * It may be run asynchronously.
    *
    * @param action the action to perform
    * @see #writeAccess(Mutex.Action)
    * @see #readAccess(Runnable)
    */
    public void writeAccess (final Runnable action) {

        if (this == EVENT) {
            doEvent (action);
            return;
        }

        Thread t = Thread.currentThread();
        writeEnter(t);
        try {
            action.run();
        } finally {
            leave(t);
        }
    }


    /** Not implemented
    *
    * @param run runnable to run
    * @deprecated
    */
    public void postReadRequest (final Runnable run) {
        postRequest(S, run);
    }

    /** Run an action with write access as soon as that is permitted.
    * If there is no writer & reader, it will be run immediately;
    * otherwise it will be run later when the last writer and/or reader finishes.
    *
    * @param run runnable to run
    */
    public void postWriteRequest (Runnable run) {
        postRequest(X, run);
    }

    /** toString */
    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer sbuff = new StringBuffer(512);
        synchronized(LOCK){
            sbuff.append("threads: ").append(registeredThreads).append(newline); // NOI18N
            sbuff.append("readersNo: ").append(readersNo).append(newline); // NOI18N
            sbuff.append("waiters: ").append(waiters).append(newline); // NOI18N
            sbuff.append("grantedMode: ").append(grantedMode).append(newline); // NOI18N
        }
        return sbuff.toString();
    }

    // priv methods  -----------------------------------------

    /** enters this mutex for writing */
    private void writeEnter(Thread t) {
        enter(X, t, true);
    }
    /** enters this mutex for reading */
    private void readEnter(Thread t) {
        enter(S, t, true);
    }

    /** enters this mutex with given mode
    * @param requested one of S, X
    * @param t
    */
    private boolean enter(int requested, Thread t, boolean block) {
        QueueCell cell = null;

        for (;;) {
            synchronized (LOCK) {
                //        System.out.println("ENTER: " + t + " mode: " + requested + " granted: " + grantedMode); // NOI18N
                // does the thread reenter this mutex?
                ThreadInfo info = getThreadInfo(t);

                if (info != null) {
                    if (grantedMode == NONE) {
                        // defensive
                        throw new IllegalStateException();
                    }
                    // reenters
                    // requested == S -> always succeeds
                    // info.mode == X -> always succeeds
                    if (((info.mode == S) && (grantedMode == X)) ||
                            ((info.mode == X) && (grantedMode == S))) {
                        // defensive
                        throw new IllegalStateException();
                    }
                    if ((info.mode == X) ||
                            (info.mode == requested)) {  // X - X, X - S, S - S
                        if (info.forced) {
                            info.forced = false;
                        } else {
                            info.counts[requested]++;
                            if ((requested == S) && (info.counts[requested] == 1)) {
                                readersNo++;
                            }
                        }
                        return true;
                    } else if (canUpgrade(info.mode, requested)) { // S - X and no holders
                        info.mode = X;
                        info.counts[requested]++;
                        info.rsnapshot = info.counts[S];
                        if (grantedMode == S) {
                            grantedMode = X;
                        } else if (grantedMode == X) {
                            // defensive
                            throw new IllegalStateException();
                        } // else if grantedMode == CHAIN - let it be

                        return true;
                    } else { // S - X and holders
                        if (Boolean.getBoolean("netbeans.debug.threads")) { // NOI18N
                            System.err.println("WARNING: Going from readAccess to writeAccess");
                            Thread.dumpStack();
                        }
                        // chain follows
                    }
                } else {  // first acquisition
                    if (isCompatible(requested)) {  // NONE -> S,X or S -> S
                        grantedMode = requested;
                        registeredThreads.put(t, new ThreadInfo(t, requested));
                        if (requested == S) {
                            readersNo++;
                        }
                        return true;
                    } // else {
                    // granted is S and requested is X
                    // granted is X and requested is S or X
                    //}
                }

                if (! block) {
                    return false;
                }
                grantedMode = CHAIN;
                cell = chain(requested, t);
                //        System.out.println(System.identityHashCode(this) + " ENTEREND"); // NOI18N

            } // sync
            cell.sleep();
        } // for
    }


    /** Leaves this mutex */
    private void leave(Thread t) {
        ThreadInfo info;

        synchronized (LOCK) {
            info = getThreadInfo(t);


            switch (grantedMode) {
            case NONE:
                throw new IllegalStateException();

            case CHAIN:
                if (info.counts[X] > 0) {
                    // it matters that X is handled first - see ThreadInfo.rsnapshot
                    leaveX(info);
                } else if (info.counts[S] > 0) {
                    leaveS(info);
                } else {
                    throw new IllegalStateException();
                }
                break;

            case X:
                leaveX(info);
                break;

            case S:
                leaveS(info);
                break;
            } // switch
        } // sync

        // check posted requests
        if (info.mode == NONE) { // last enter to mutex being left
            List runnables = info.dequeue();
            if (runnables != null) { // pending requests
                final int size = runnables.size();
                for (int i = 0; i < size; i++) {
                    try {
                        Runnable r = (Runnable) runnables.get(i);
                        writeAccess(r);
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable e) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            e.printStackTrace();
                        }
                    } // try
                } // for
            }
        } // mode
    }

    /** Leaves the lock supposing that info.counts[X] is greater than zero */
    private void leaveX(ThreadInfo info) {

        if ((info.counts[X] <= 0) ||
                (info.rsnapshot > info.counts[S])) {
            // defensive
            throw new IllegalStateException();
        }

        if (info.rsnapshot == info.counts[S]) {
            info.counts[X]--;
            if (info.counts[X] == 0) {
                info.rsnapshot = 0;
                // downgrade the lock
                if (info.counts[S] > 0) {
                    info.mode = grantedMode = S;
                } else {
                    info.mode = grantedMode = NONE;
                    registeredThreads.remove(info.t);
                }
                // mode has changed
                wakeUpOthers();
            }
        } else {
            // rsnapshot < counts[S]

            if (info.counts[S] <= 0) {
                // defensive
                throw new IllegalStateException();
            }

            if (--info.counts[S] == 0) {
                if (readersNo <= 0) {
                    throw new IllegalStateException();
                }
                readersNo--;
            }
        }
    }

    /** Leaves the lock supposing that info.counts[X] is greater than zero */
    private void leaveS(ThreadInfo info) {
        if ((info.counts[S] <= 0) ||
                (info.counts[X] > 0)) {
            // defensive
            throw new IllegalStateException();
        }

        info.counts[S]--;
        if (info.counts[S] == 0) {

            // remove the thread
            info.mode = NONE;
            registeredThreads.remove(info.t);

            // downsize readersNo
            if (readersNo <= 0) {
                throw new IllegalStateException();
            }
            readersNo--;
            if (readersNo == 0) {
                grantedMode = NONE;
                wakeUpOthers();
            } else if ((grantedMode == CHAIN) &&
                       (readersNo == 1)) {
                // can be the mode advanced from CHAIN? Examine first item of waiters!

                for (int i = 0; i < waiters.size(); i++) {
                    QueueCell qc = (QueueCell) waiters.get(i);
                    synchronized (qc) {
                        if (qc.isGotOut()) {
                            waiters.remove(i--);
                            continue;
                        }

                        ThreadInfo tinfo = getThreadInfo(qc.t);

                        if (tinfo != null) {
                            if (tinfo.mode == S) {
                                if (qc.mode != X) {
                                    // defensive
                                    throw new IllegalStateException();
                                }

                                if (waiters.size() == 1) {
                                    grantedMode = X;
                                } // else let CHAIN
                                tinfo.mode = X;
                                waiters.remove(i);
                                qc.wakeMeUp();
                            }
                        } // else first request is a first X request of some thread
                        break;
                    } // sync (qc)
                } // for
            } // else
        } // count[S] == 0
    }

    /** Adds this thread to the queue of waiting threads
    * @warning LOCK must be held
    */
    private QueueCell chain(int requested, Thread t) {

        long timeout = 0;

        if (killDeadlocksOn) {
            checkDeadlock(requested, t);
            timeout = (isDispatchThread() || checkAwtTreeLock() ? TIMEOUT : 0);
        }

        QueueCell qc = new QueueCell(requested, t);
        qc.timeout = timeout;

        final int size = waiters.size();
        if (size == 0) {
            waiters.add(qc);
        } else {
            QueueCell cursor;
            int i = 0;
            do {
                cursor = (QueueCell) waiters.get(i);
                if (cursor.getPriority() < qc.getPriority()) {
                    waiters.add(i, qc);
                    break;
                }
                i++;
            } while (i < size);
            if (i == size) {
                waiters.add(qc);
            }
        }
        return qc;
    }

    /** Scans through waiters and wakes up them */
    private void wakeUpOthers() {

        if ((grantedMode == X) ||
                (grantedMode == CHAIN)) {
            // defensive
            throw new IllegalStateException();
        }

        if (waiters.size() == 0) {
            return;
        }

        for (int i = 0; i < waiters.size(); i++) {
            QueueCell qc = (QueueCell) waiters.get(i);

            synchronized (qc) {
                if (qc.isGotOut()) {
                    // bogus waiter
                    waiters.remove(i--);
                    continue;
                }

                if (compatibleLocks(grantedMode, qc.mode)) {  // woken S -> should I wake X? -> no
                    waiters.remove(i--);
                    qc.wakeMeUp();
                    grantedMode = qc.mode;
                    if (getThreadInfo(qc.t) == null) {
                        // force to have a record since recorded threads
                        // do not use isCompatible call
                        ThreadInfo ti = new ThreadInfo(qc.t, qc.mode);
                        ti.forced = true;
                        if (qc.mode == S) {
                            readersNo++;
                        }
                        registeredThreads.put(qc.t, ti);
                    }
                } else {
                    grantedMode = CHAIN;
                    break;
                }
            } // sync (qc)
        }
    }

    /** Posts new request for current thread
    * @param mutexMode mutex mode for which the action is rquested
    * @param run the action
    */
    private void postRequest(int mutexMode, Runnable run) {

        if (this == EVENT) {
            doEventRequest(run);
            return;
        }

        Thread t = Thread.currentThread();
        ThreadInfo info;

        synchronized (LOCK) {
            info = getThreadInfo(t);
            if (info != null) {
                if (mutexMode == info.mode) {
                    enter(mutexMode, t, true);
                } else {  // the mutex is held but can not be entered in X mode
                    info.enqueue(run);
                    return;
                }
            }
        }

        // this mutex is not held
        if (info == null) {
            enter(mutexMode, t, true);
            try {
                run.run();
            } finally {
                leave(t);
            }
            return;
        }

        // run it immediately
        // info != null so enter(...) succeeded
        try {
            run.run();
        } finally {
            leave(t);
        }
    }

    /** @param requested is requested mode of locking
    * @return <tt>true</tt> if and only if current mode and requested mode are compatible
    */
    private boolean isCompatible(int requested) {
        return compatibleLocks(grantedMode, requested);
    }

    private ThreadInfo getThreadInfo(Thread t) {
        return (ThreadInfo) registeredThreads.get(t);
    }

    private boolean canUpgrade(int threadGranted, int requested) {
        return (threadGranted == S) && (requested == X) && (readersNo == 1);
    }

    // deadlock detector vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv

    /** Simple deadlock detector */
    private void checkDeadlock(int requested, Thread t) {
        if (grantedMode == CHAIN) {
            Set roots = getOwners();

            if (roots == null) {
                return;
            }

            if (! roots.contains(t)) {
                return; // new thread
            }

            // we know requested == X now - since t is in roots

            int size = waiters.size();

            if (size == 0) {
                return;
            }

            QueueCell qc = (QueueCell) waiters.get(0);
            if (qc.mode == S) {
                // defensive
                throw new IllegalStateException();
            } else {
                // t is in roots
                // and a thread is in the waiters queue
                // the thread waits for completition
                // deadlock
                throwDeadlockAvoidance();
            }
        }
    }

    /** @return a Set of threads that are registered - owns this lock
    * In X case returns null;
    * In S case it can be several threads
    * (however grantedMode is CHAIN)
    */
    private Set getOwners() {
        int mode = 0;
        Iterator regs = registeredThreads.entrySet().iterator();
        Set ret = new HashSet((int) (1.3 * registeredThreads.size()));
        while (regs.hasNext()) {
            Entry e = (Entry) regs.next();
            if (((ThreadInfo) e.getValue()).mode == X) {
                if (mode == S) {
                    // defensive
                    throw new IllegalStateException();
                }
                return null;
            } else {
                mode = S;
            }
            ret.add(e.getKey());
        }
        return ret;
    }

    /** AWTTreeLock (java.awt.Component.getTreeLock) */
    private static Object TREELOCK;

    /** @return true if the check succeed, that is, if current thread holds AWTTreeLock */
    private boolean checkAwtTreeLock() {
        if (TREELOCK == null) {
            TREELOCK = new java.awt.Panel().getTreeLock();
        }
        for (;;) {
            try {
                TREELOCK.notify();
                // oh god I gotcha
                return true;
            } catch (IllegalMonitorStateException e) {
                return false;  // OK do not have it
            }
        }
    }

    /** Never returns normally - always throws DeadlockAvoidanceException */
    private void throwDeadlockAvoidance() {
        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
            System.err.println(toString());
        }

        throw new DeadlockAvoidanceException();
    }

    // deadlock detector ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    // ------------------------------- EVENT METHODS ----------------------------
    /** Runs the runnable in event queue, either immediatelly,
    * or it posts it into the queue.
    */
    private static void doEvent (Runnable run) {
        if (EventQueue.isDispatchThread ()) {
            run.run ();
        } else {
            EventQueue.invokeLater (run);
        }
    }

    /** Methods for access to event queue.
    * @param run runabble to post later
    */
    private static void doEventRequest (Runnable run) {
        EventQueue.invokeLater (run);
    }

    /** Methods for access to event queue and waiting for result.
    * @param run runabble to post later
    */
    private static Object doEventAccess (final ExceptionAction run) throws MutexException {

        if (isDispatchThread()) {
            try {
                return run.run ();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new MutexException (e);
            }
        }

        final Exception[] arr = new Exception[1];
        try {
            final Object[] res = new Object[1];
            EventQueue.invokeAndWait (new Runnable () {
                                          public void run () {
                                              try {
                                                  res[0] = run.run ();
                                              } catch (Exception e) {
                                                  arr[0] = e;
                                              }
                                          }
                                      });
            return res[0];
        } catch (InterruptedException e) {
            arr[0] = e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            arr[0] = (Exception)e.getTargetException ();
        }
        if (arr[0] instanceof RuntimeException) {
            throw (RuntimeException)arr[0];
        }
        throw new MutexException (arr[0]);
    }

    /** @return true iff current thread is EventDispatchThread */
    static boolean isDispatchThread() {
        boolean dispatch = EventQueue.isDispatchThread ();
        if (!dispatch && Utilities.getOperatingSystem () == Utilities.OS_SOLARIS) {
            // on solaris the event queue is not always recognized correctly
            // => try to guess by name
            dispatch = (Thread.currentThread().getClass().getName().indexOf("EventDispatchThread") >= 0); // NOI18N
        }
        return dispatch;
    }

    // --------------------------------------------- END OF EVENT METHODS ------------------------------

    /** Action to be executed in a mutex without throwing any checked exceptions.
    * Unchecked exceptions will be propagated to calling code.
    */
    public static interface Action extends ExceptionAction {
        /** Execute the action.
        * @return any object, then returned from {@link Mutex#readAccess(Mutex.Action) or {@link Mutex#writeAccess(Mutex.Action)}
        */
        public Object run ();
    }

    /** Action to be executed in a mutex, possibly throwing checked exceptions.
    * May throw a checked exception, in which case calling
    * code should catch the encapsulating exception and rethrow the
    * real one.
    * Unchecked exceptions will be propagated to calling code without encapsulation.
    */
    public static interface ExceptionAction {
        /** Execute the action.
        * Can throw an exception.
        * @return any object, then returned from {@link Mutex#readAccess(Mutex.ExceptionAction) or {@link Mutex#writeAccess(Mutex.ExceptionAction)}
        * @exception Exception any exception the body needs to throw
        */
        public Object run () throws Exception;
    }

    private static class ThreadInfo {

        /** t is forcibly sent from waiters to enter() by wakeUpOthers() */
        boolean forced;
        /** ThreadInfo for this Thread */
        final Thread t;
        /** granted mode */
        int mode;
        // 0 - NONE, 1 - CHAIN, 2 - X, 3 - S
        /** enter counter */
        int[] counts;
        /** queue of runnable rquests that are to be executed (in X mode) right after S mode is left
        * deadlock avoidance technique 
        */
        List[] queues;

        /** value of counts[S] when the mode was upgraded
        * rsnapshot works as follows:
        * if a thread holds the mutex in the S mode and it reenters the mutex
        * and requests X and the mode can be granted (no other readers) then this
        * variable is set to counts[S]. This is used in the leave method in the X branch.
        * (X mode is granted by other words)
        * If rsnapshot is less than counts[S] then the counter is decremented etc. If the rsnapshot is
        * equal to count[S] then count[X] is decremented. If the X counter is zeroed then
        * rsnapshot is zeroed as well and current mode is downgraded to S mode.
        * rsnapshot gets less than counts[S] if current mode is X and the mutex is reentered
        * with S request.
        */
        int rsnapshot;

        public ThreadInfo(Thread t, int mode) {
            this.t = t;
            this.mode = mode;
            this.counts = new int[MODE_COUNT];
            this.queues = new List[MODE_COUNT];
            counts[mode] = 1;
        }

        public String toString() {
            return super.toString() + " thread: " + t + " mode: " + mode + " X: " + counts[2] + " S: " + counts[3]; // NOI18N
        }

        /** Adds the Runnable into the queue of waiting requests */
        public void enqueue(Runnable run) {
            if (queues[X] == null) {
                queues[X] = new ArrayList(13);
            }
            queues[X].add(run);
        }

        /** @return a List of enqueued Runnables - may be null */
        public List dequeue() {
            List ret = queues[X];
            queues[X] = null;
            return ret;
        }
    }

    /** This class is defined only for better understanding of thread dumps where are informations like
    * java.lang.Object@xxxxxxxx owner thread_x
    *   wait for enter thread_y
    */
    private static class InternalLock {
    }

    private class QueueCell {

        int mode;
        Thread t;
        boolean signal;
        /** if the thread is owner of AWTTreeLock then the timeout is greater than zero */
        long timeout;
        boolean left;

        public QueueCell(int mode, Thread t) {
            this.mode = mode;
            this.t = t;
            this.timeout = 0;
            this.left = false;
        }

        public String toString() {
            return super.toString() + " mode: " + mode + " thread: " + t; // NOI18N
        }

        /** @return priority of this cell */
        public int getPriority() {
            return t.getPriority();
        }

        /** @return true iff the thread left sleep */
        public boolean isGotOut() {
            return left;
        }

        /** current thread will sleep until wakeMeUp is called
        * if wakeMeUp was already called then the thread will not sleep
        */
        public synchronized void sleep() {
            try {
                if (! signal) {
                    for (;;) {
                        try {
                            wait(timeout);
                            if ((timeout > 0) && !signal) {
                                // timed out
                                throwDeadlockAvoidance();
                            }
                            return;
                        } catch (InterruptedException e) { // IMHO should be propagated to r/wAccess(action)
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } finally {
                left = true;
            }
        }

        /** sends signal to a sleeper - to a thread that is in the sleep() */
        public void wakeMeUp() {
            signal = true;
            notify();
        }
    }

    /** Thrown when a deadlock appears. */
    static final class DeadlockAvoidanceException extends RuntimeException {

        static final long serialVersionUID =8409658204844386128L;
        public DeadlockAvoidanceException() {
            super ();
        }
    }

    /** if <tt>true</tt> then deadlock avoidance is switched on */
    private static final boolean killDeadlocksOn = false;
}

/*
* Log
*  27   Gandalf   1.26        1/13/00  Ian Formanek    NOI18N
*  26   Gandalf   1.25        1/12/00  Pavel Buzek     I18N
*  25   Gandalf   1.24        12/20/99 Ales Novak      getHoldingMode methods 
*       removed
*  24   Gandalf   1.23        12/14/99 Ales Novak      posting of runnables
*  23   Gandalf   1.22        12/14/99 Ales Novak      
*  22   Gandalf   1.21        12/10/99 Ales Novak      error
*  21   Gandalf   1.20        12/10/99 Ales Novak      getHoldingMode methods 
*       added
*  20   Gandalf   1.19        11/26/99 Patrik Knakal   
*  19   Gandalf   1.18        11/2/99  Ales Novak      better synchronization & 
*       bugfix
*  18   Gandalf   1.17        10/27/99 Ales Novak      new implementation of 
*       this class
*  17   Gandalf   1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  16   Gandalf   1.15        9/2/99   Jaroslav Tulach Runs what is posted as 
*       run later.
*  15   Gandalf   1.14        9/2/99   Jaroslav Tulach Exclusive access is 
*       really exclusive.
*  14   Gandalf   1.13        9/1/99   Jaroslav Tulach No debug messages.
*  13   Gandalf   1.12        9/1/99   Jaroslav Tulach Mutex.postWriteRequest
*  12   Gandalf   1.11        8/30/99  Jaroslav Tulach postWriteRequest
*  11   Gandalf   1.10        8/27/99  Jaroslav Tulach New threading model & 
*       Children.
*  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  9    Gandalf   1.8         6/7/99   Jaroslav Tulach Try to solve #1858
*  8    Gandalf   1.7         5/14/99  Jesse Glick     [JavaDoc]
*  7    Gandalf   1.6         4/20/99  Jaroslav Tulach Children supports weak 
*       references.
*  6    Gandalf   1.5         4/19/99  Jesse Glick     [JavaDoc]
*  5    Gandalf   1.4         2/11/99  Jaroslav Tulach No more postReadRequests.
*  4    Gandalf   1.3         2/4/99   Jaroslav Tulach Children.MUTEX 
*       synchronizes on Component Tree Lock.
*  3    Gandalf   1.2         2/3/99   Jaroslav Tulach invokeSafely
*  2    Gandalf   1.1         1/29/99  Ales Novak      
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
