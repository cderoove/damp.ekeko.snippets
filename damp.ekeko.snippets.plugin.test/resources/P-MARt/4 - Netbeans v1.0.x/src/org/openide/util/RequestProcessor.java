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

import java.lang.ref.*;
import java.util.*;

import org.openide.TopManager;

/** Request processor that is capable to execute actions in
* special thread.
*
* @author Jaroslav Tulach
*/
public final class RequestProcessor extends Object {
    // JST: final can be removed if needed

    /** the static instance for users that do not want to have own processor */
    private static RequestProcessor DEFAULT = new RequestProcessor ();

    /** number of processors */
    private static int processorCount = 0;

    /** sorted set of all task that are waiting to be processed
    * objects of type (Holder), sorted by time
    */
    final SortedSet waiting = new TreeSet (new TimeComp ());

    /** thread to process requests */
    private ProcessorThread thread;


    /** name of the request processor or null */
    private String name;

    /** Default constructor.
    */
    public RequestProcessor () {
    }

    /** Constructor.
    * @param name the name to use for the request processor thread
    */
    public RequestProcessor (String name) {
        this.name = name;
    }

    /** When finalized, stops the thread.
    */
    protected void finalize () {
        stop ();
    }

    /** This methods asks the request processor to start given
    * runnable after timeToWait milliseconds.
    *
    * @param run class to run
    * @return the task to control the request
    */
    public Task post (Runnable run) {
        return post (run, 0, Thread.MIN_PRIORITY);
    }

    /** This methods asks the request processor to start given
    * runnable after timeToWait milliseconds. The default priority is Thread.MIN_PRIORITY.
    *
    * @param run class to run
    * @param timeToWait to wait before execution
    * @return the task to control the request
    */
    public Task post (final Runnable run, int timeToWait) {
        return post (run, timeToWait, Thread.MIN_PRIORITY);
    }

    /** This methods asks the request processor to start given
    * runnable after timeToWait milliseconds. Given priority is assigned to the
    * request.
    *
    *
    * @param run class to run
    * @param timeToWait to wait before execution
    * @param priority the priority from Thread.MIN_PRIORITY to Thread.MAX_PRIORITY
    * @return the task to control the request
    */
    public Task post (final Runnable run, int timeToWait, int priority) {
        Task t = new Task (run, timeToWait, priority);
        synchronized (waiting) {
            waiting.add (t.createHolder ());
            checkTimerQueue ();
        }
        // debug code
        if (debug != null && RequestProcessor.this == DEFAULT) {
            debug.notifyPost (t);
        }
        // end of debug
        return t;
    }

    /** Creates request that can be later started by setting its delay.
    * The request is not immediatelly put into the queue. It is planned after
    * setting its delay by setDelay method.
    *
    * @param run action to run in the process
    * @return the task to control execution of given action
    */
    public Task create (Runnable run) {
        return new Task (run, 0, Thread.MIN_PRIORITY);
    }

    /** Tests if the current thread is request processor thread.
    * This method could be used to prevent the deadlocks using
    * <CODE>waitFinished</CODE> method. Any two tasks created
    * by request processor must not wait for themself.
    *
    * @return <CODE>true</CODE> if the current thread is request processor
    *          thread, otherwise <CODE>false</CODE>
    */
    public boolean isRequestProcessorThread () {
        return Thread.currentThread().equals(thread);
    }

    /** Stops processing of runnables processor.
    * The currently running runnable is finished and no new is started.
    */
    public void stop () {
        synchronized (waiting) {
            if (thread != null) {
                thread.stopProcessing ();
                thread = null;
            }
        }
    }


    //
    // Static methods communicating with default request processor
    //

    /** This methods asks the request processor to start given
    * runnable after timeToWait milliseconds.
    *
    * @param run class to run
    * @return the task to control the request
    */
    public static Task postRequest (Runnable run) {
        return DEFAULT.post (run);
    }

    /** This methods asks the request processor to start given
    * runnable after timeToWait milliseconds. The default priority is Thread.MIN_PRIORITY.
    *
    * @param run class to run
    * @param timeToWait to wait before execution
    * @return the task to control the request
    */
    public static Task postRequest (final Runnable run, int timeToWait) {
        return DEFAULT.post (run, timeToWait);
    }

    /** This methods asks the request processor to start given
    * runnable after timeToWait milliseconds. Given priority is assigned to the
    * request.
    *
    *
    * @param run class to run
    * @param timeToWait to wait before execution
    * @param priority the priority from Thread.MIN_PRIORITY to Thread.MAX_PRIORITY
    * @return the task to control the request
    */
    public static Task postRequest (final Runnable run, int timeToWait, int priority) {
        return DEFAULT.post (run, timeToWait, priority);
    }

    /** Creates request that can be later started by setting its delay.
    * The request is not immediatelly put into the queue. It is planned after
    * setting its delay by setDelay method.
    *
    * @param run action to run in the process
    * @return the task to control execution of given action
    */
    public static Task createRequest (Runnable run) {
        return DEFAULT.create (run);
    }


    //
    // Implementation of the queue
    //

    /** Checks the timer queue. First of all sees whether there is
    * the processor thread running. If not, starts one. Then checks if
    * the time of execution of the first task has not changed. If so,
    * wakeups the thread so it can replan itself.
    * <P>
    * The methods is always called with synchronization on waiting.
    */
    void checkTimerQueue () {
        if (thread == null) {
            // only start processor thread
            thread = new ProcessorThread (name, this);
            thread.start ();
        } else {
            if (waiting.size () == 0) {
                // nothing to plan
                return;
            }
            // wakeup the thread to change what should be changed
            waiting.notify ();
        }
    }

    /** Holder for a task */
    private final class Holder extends Object {

        /** Comment out when subclasing exception.
        */
        public void printStackTrace () {
        }


        public Task task;
        public int priority;
        public long time;

        public Holder (Task t) {
            task = t;
            priority = t.priority;
            time = t.time;
        }

        public String toString () {
            Task t = task;
            return t == null ? "null" : t.toString (); // NOI18N
        }
    }

    /** The task describing the request task send to the processor.
    */
    public final class Task extends org.openide.util.Task {
        /** time to run */
        long time;
        /** priority */
        int priority;

        /** holder that is currently representing the task in the queue */
        Holder holder;

        /** @param run runnable to start
        * @param delay amount of millis to wait
        * @param priority the priorty of the task
        */
        Task (Runnable run, long delay, int priority) {
            super (run);
            time = System.currentTimeMillis () + delay;
            this.priority = priority;
        }

        /** Creates new holder canceling the previous one (if any).
        * !Can be called only under waiting lock!
        */
        Holder createHolder () {
            if (holder != null) {
                holder.task = null;
            }
            return holder = new Holder (this);
        }

        /** Getter for amount of millis till this task
        * is started.
        * @return amount of millis
        */
        public int getDelay () {
            long delay = time - System.currentTimeMillis ();
            if (delay < 0L) return 0;
            if (delay > (long)Integer.MAX_VALUE) return Integer.MAX_VALUE;
            return (int)delay;
        }

        /** Changes the delay to different level,
        * if the task has not been run yet, it is replaned to
        * the new time. If it has been finished, it is replaned
        * to be started again.
        *
        * @param delay time in millis to wait
        */
        public void schedule (int delay) {
            synchronized (waiting) {
                time = System.currentTimeMillis () + delay;
                waiting.add (createHolder ());
                checkTimerQueue ();
            }
        }

        /** Removes the task from the queue.
        *
        * @return true if the task has been removed from the queue,
        *   false it the task has already been processed
        */
        public boolean cancel () {
            synchronized (waiting) {
                if (holder != null) {
                    holder.task = null;
                    holder = null;
                    return true;
                } else {
                    return false;
                }
            }
        }

        /** Current priority of the task.
        */
        public int getPriority () {
            return priority;
        }

        /** Changes priority to new one. If the task has been
        * already run, do not plan it again.
        *
        */
        public void setPriority (int priority) {
            if (this.priority != priority) {
                if (priority < Thread.MIN_PRIORITY) priority = Thread.MIN_PRIORITY;
                if (priority > Thread.MAX_PRIORITY) priority = Thread.MAX_PRIORITY;

                synchronized (waiting) {
                    this.priority = priority;
                    if (holder != null) {
                        waiting.add (createHolder ());
                        checkTimerQueue ();
                    }
                }
            }
        }

        /** This method is an implementation of the waitFinished method
        * in the RequestProcessor.Task. It check the current thread if it is
        * request processor thread.
        *
        * This implemetation run the task and then returns.
        */
        void waitFinishedImpl () {
            if (isRequestProcessorThread()) {
                // one thread runnig
                boolean toRun = false;

                synchronized (waiting) {
                    if (holder != null) {
                        holder.task = null;
                        holder = null;
                        toRun = true;
                    }
                }
                if (toRun) {
                    run();
                }
            }
            else {
                /* JST: new threading model does not need this.
                        if (System.getProperty("netbeans.debug.threads") != null) {
                          if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                            System.out.println ("WARNING: EventDispathThread is waiting for request processor task.");
                            Thread.dumpStack();
                          }
                        }
                */        
                super.waitFinishedImpl();
            }
        }

        /** @return string representation */
        public String toString () {
            return super.toString () + " [" + (time - System.currentTimeMillis ()) + ", " + priority + ']'; // NOI18N
        }
    }

    /** Processor thread.
    */
    private final static class ProcessorThread extends Thread {
        /** sorted set of all task that are waiting to be processed
        * objects of type (Holder), sorted by priority
        * @associates Holder
        */
        private TreeSet pending = new TreeSet (new PriorityComp ());

        /** reference to the processor */
        private Reference requestProcessor;

        /** stops the processor if true */
        private boolean stop;
        /** previous priority */
        private int priority;
        /** last sleep time */
        Object sleep;
        /** last delay */
        long at;


        /** Constructor. */
        public ProcessorThread (String name, RequestProcessor requestProcessor) {
            super (
                getTopLevelThreadGroup(),
                name == null ?
                ("OpenIDE Request Processor-" + processorCount++) // NOI18N
                :
                name
            );
            setDaemon (true);
            priority = getPriority ();

            this.requestProcessor = new WeakReference (requestProcessor);
        }

        /** Stops the processing (must have synch on the waiting) */
        public void stopProcessing () {
            stop = true;
            interrupt ();
        }


        /** Copies objects from the waiting queue that are ready to be
        * processed into pending queue. This method blocks when the
        * the pending queue is empty.
        *
        * <P>
        * Called under lock on waiting.
        * @return the amount of time to wait till next event should be
        *   posted from waiting to pending (0 means infinity)
        */
        private long waitingToPending () {
            RequestProcessor rp = (RequestProcessor)requestProcessor.get ();


            if (rp == null || rp.waiting.isEmpty ()) {
                // wait for ever
                return 0L;
            }

            Iterator it = rp.waiting.iterator ();
            while (it.hasNext ()) {
                Holder holder = (Holder)it.next ();

                if (holder.task == null) {
                    // continue
                    it.remove ();
                    continue;
                }

                long diff = holder.time - System.currentTimeMillis ();
                /*        if (debug != null && pending.size () > 15) {
                System.err.println ("waitingToPending: " + holder + " has diff: " + diff);
                        }
                */        
                if (diff > 0L) {
                    // this task should be run in future =>
                    // return the amount of time to wait
                    return diff;
                }

                it.remove ();
                // put the holder into the pending queue
                pending.add (holder);
            }

            // pending is not empty => do not wait
            // pending is empty => wait forever
            return 0L;
        }

        /** Synchronization object or exception.
        */
        private Object synch () {
            RequestProcessor rp = (RequestProcessor)requestProcessor.get ();
            if (rp == null) {
                throw new IllegalStateException ();
            }
            return rp.waiting;
        }

        /** Processor of requests.
        */
        public void run () {
            int priority = getPriority ();

            while (!stop) {
                Task t;
                Holder h;
                Iterator first;

                synchronized (synch ()) {

                    for (;;) {
                        h = null;
                        t = null;
                        first = null;

                        // either fills the pending set or return time to
                        // wait for another request processing
                        long w = waitingToPending ();

                        if (!pending.isEmpty ()) {
                            break;
                        }

                        // wait the given time
                        try {
                            if (debug != null && requestProcessor.get () == DEFAULT) {
                                debug.notifySleep ((int)w);
                            }
                            synch ().wait (w);
                        } catch (InterruptedException ex) {
                            if (stop) {
                                return;
                            } else {
                                throw new InternalError ();
                            }
                        }
                    }

                    // take first holder (pending is not empty)
                    first = pending.iterator ();
                    h = (Holder)first.next ();
                    first.remove ();

                    // take the task
                    t = h.task;
                    if (t != null) {
                        // mark as being processed
                        t.holder = null;
                    } else {
                        continue;
                    }
                }

                // run the task
                int p = t.getPriority ();
                if (priority != p) {
                    setPriority (priority = p);
                }

                try {
                    // debug code
                    if (debug != null && requestProcessor.get () == DEFAULT) {
                        System.err.println ("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
                        debug.notifyRun (t, h);
                        h.printStackTrace();
                    }
                    // end of debug
                    t.run ();
                    if (debug != null && requestProcessor.get () == DEFAULT) {
                        System.err.println ("Task finished: " + t);
                        debug.printRequestProcessor ();
                        System.err.println ("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    }
                } catch (ThreadDeath td) {
                    // do not catch this
                    throw td;
                } catch (Throwable ex) {
                    if (System.getProperty ("netbeans.debug.exceptions") != null) {
                        System.err.println("Request processor thread exception!");
                        ex.printStackTrace();
                    }
                }
            }
        } // run
    }

    /**
    * @return a top level ThreadGroup. The method ensures that even
    * RequestProcessors created by internal execution will survive the
    * end of the task.
    */
    static ThreadGroup getTopLevelThreadGroup() {
        java.security.PrivilegedAction run = new java.security.PrivilegedAction() {
                                                 public Object run() {
                                                     ThreadGroup current = Thread.currentThread().getThreadGroup();
                                                     while (current.getParent() != null) {
                                                         current = current.getParent();
                                                     }
                                                     return current;
                                                 }
                                             };

        return (ThreadGroup) java.security.AccessController.doPrivileged(run);
    }




    /** Comparator that compares times */
    private static final class TimeComp extends Object implements Comparator {
        /** Compares to different RequestProcessor.Task object.
        * First of all compares the time and then it looks for the priority.
        *
        */
        public int compare (Object o1, Object o2) {
            Holder h1 = (Holder)o1;
            Holder h2 = (Holder)o2;

            if (h1.time == h2.time) {
                Task t1 = h1.task;
                Task t2 = h2.task;
                // choose anything
                return System.identityHashCode (t1) - System.identityHashCode (t2);
            } else {
                // compare on time
                if (h1.time > h2.time) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }

    /** Comparator that compares priorities */
    private static final class PriorityComp extends Object implements Comparator {
        /** Compares to different RequestProcessor.Task object.
        * First of all compares the time and then it looks for the priority.
        *
        */
        public int compare (Object o1, Object o2) {
            Holder h1 = (Holder)o1;
            Holder h2 = (Holder)o2;


            if (h1.priority == h2.priority) {
                Task t1 = h1.task;
                Task t2 = h2.task;
                // choose anything
                return System.identityHashCode (t1) - System.identityHashCode (t2);
            } else {
                // compare on time
                if (h1.priority < h2.priority) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }

    //
    // Hacking code to allow debugging of request processors
    //

    private static Debug debug;

    static {
        if (System.getProperty ("netbeans.debug.requests") != null) {
            debug = new Debug ();
        } else {
            TopManager tm = TopManager.getDefault ();
            if (tm != null) {
                tm.getWindowManager ().getMainWindow ().addKeyListener (
                    new Debug ()
                );
            }
        }
    }

    private static class Debug extends Object implements java.awt.event.KeyListener {
        public void keyReleased(final java.awt.event.KeyEvent p0) {
        }

        public void keyPressed(final java.awt.event.KeyEvent ev) {
            if (
                (ev.getModifiers () & java.awt.event.KeyEvent.CTRL_MASK) != 0 &&
                (ev.getModifiers () & java.awt.event.KeyEvent.ALT_MASK) != 0 &&
                ev.getKeyCode () == java.awt.event.KeyEvent.VK_F10
            ) {
                debug = this;
                printRequestProcessor ();
            }
        }

        public void keyTyped(final java.awt.event.KeyEvent ev) {
        }

        private void printRequestProcessor () {
            ProcessorThread thread = DEFAULT.thread;
            if (thread != null) {
                System.err.println("Content of " + thread + " sleep for: " + thread.sleep + " ago: " + (System.currentTimeMillis () - thread.at));
            } else {
                System.err.println("Content of " + thread);
            }

            java.util.LinkedList ll = new java.util.LinkedList (DEFAULT.waiting);
            print (ll);

            if (thread != null) {
                System.err.println("Pending requests");
                print (new LinkedList (thread.pending));
            }
        }

        private void print (Collection ll) {
            Iterator it = ll.iterator ();

            int i = 0;
            while (it.hasNext ()) {
                Holder h = (Holder)it.next ();
                Task t = h.task;
                System.err.print("  ");
                System.err.print(++i);
                System.err.print(". ");
                System.err.println(t);
            }
        }

        public void notifySleep (int timeOut) {
            ProcessorThread thread = DEFAULT.thread;
            System.err.println("Sleeping for " + timeOut + " " + thread);
        }

        public void notifyPost (Task t) {
            ProcessorThread thread = DEFAULT.thread;
            System.err.println("Post: " + t + " to " + thread);
            //      Thread.dumpStack();
            printRequestProcessor ();
        }

        public void notifyRun (Task t, Holder h) {
            ProcessorThread thread = DEFAULT.thread;
            System.err.println("Run: " + t + " to " + thread + " because of holder: " + h);
        }

    }

    //
    // End of hacking code
    //


    /*
      public static void main (String [] args) throws Exception {
        class Run extends Object implements Runnable {
          private String name;
          
          public Run (String n) {
            name = n;
          }
          
          public void run () {
            try {
              System.out.println("In: " + name);
              Thread.sleep (3000);
              System.out.println("Out: " + name);
            } catch (Exception ex) {
            }
          }
          
          public String toString () {
            return name;
          }
        }
        
        
        Task t1 = RequestProcessor.postRequest (new Run ("First"));
        Task t2 = RequestProcessor.postRequest (new Run ("Min"), 500, Thread.MIN_PRIORITY);
        Task t3 = RequestProcessor.postRequest (new Run ("Max"), 2500, Thread.MAX_PRIORITY);
        
        t1.waitFinished ();
        System.out.println("t1 finished");

        t2.waitFinished ();
        System.out.println("t2 finished");
        
        t3.waitFinished ();
        System.out.println("t3 finished");
        
        System.out.println("Finished");
      }
    */  
}

/*
* Log
*  33   Gandalf   1.32        1/12/00  Pavel Buzek     I18N
*  32   Gandalf   1.31        11/30/99 Jaroslav Tulach Can be garbage collected.
*  31   Gandalf   1.30        11/29/99 Jaroslav Tulach RequestProcessor can now 
*       be stopped and finalized.
*  30   Gandalf   1.29        11/26/99 Patrik Knakal   
*  29   Gandalf   1.28        10/27/99 Ales Novak      getTopLevelThreadGroup
*  28   Gandalf   1.27        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  27   Gandalf   1.26        9/15/99  Jaroslav Tulach More debug code.
*  26   Gandalf   1.25        9/13/99  Jaroslav Tulach Debug code again.
*  25   Gandalf   1.24        8/30/99  Jaroslav Tulach Deleted thread warning.
*  24   Gandalf   1.23        8/17/99  Petr Hamernik   fixed bug - starting the 
*       task more than once.
*  23   Gandalf   1.22        8/16/99  Jaroslav Tulach Rewriten to use 
*       waiting/pending
*  22   Gandalf   1.21        8/12/99  Jaroslav Tulach Improved trace format. 
*       Constructor with name of the processor.
*  21   Gandalf   1.20        8/11/99  Jaroslav Tulach 
*  20   Gandalf   1.19        8/11/99  Jaroslav Tulach Contains debugging code
*  19   Gandalf   1.18        8/7/99   Ian Formanek    netbeans.debug.exceptions->netbeans.debug.threads
*        for threads waiting warnings
*  18   Gandalf   1.17        8/6/99   Petr Hamernik   debug WARNING - deadlock 
*       prevention
*  17   Gandalf   1.16        7/28/99  Jaroslav Tulach Solves LineSet 
*       starvation.
*  16   Gandalf   1.15        7/25/99  Ian Formanek    Exceptions printed to 
*       console only on "netbeans.debug.exceptions" flag
*  15   Gandalf   1.14        6/22/99  Jesse Glick     Thread name -> OpenIDE.
*  14   Gandalf   1.13        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  13   Gandalf   1.12        5/17/99  Jaroslav Tulach Survives exception in 
*       posted requests.
*  12   Gandalf   1.11        5/16/99  Ian Formanek    Undone last change
*  11   Gandalf   1.10        5/16/99  Ian Formanek    Added prevention of 
*       RequestProcessor diyng from exceptions inside tasks - the exceptions are
*       printed on -Dnetbeans.debug.rp
*  10   Gandalf   1.9         4/15/99  Petr Hamernik   changed it to non-static
*  9    Gandalf   1.8         4/13/99  Petr Hamernik   deadlocks prevention
*  8    Gandalf   1.7         4/8/99   Petr Hamernik   bugfix
*  7    Gandalf   1.6         4/8/99   Petr Hamernik   
*  6    Gandalf   1.5         4/8/99   Petr Hamernik   checking deadlocks in 
*       RequestProcessor
*  5    Gandalf   1.4         4/7/99   Ian Formanek    isRequestProcessorThread 
*       method added
*  4    Gandalf   1.3         2/3/99   Jaroslav Tulach 
*  3    Gandalf   1.2         2/1/99   David Simonek   
*  2    Gandalf   1.1         2/1/99   Jaroslav Tulach Enhancements to replan, 
*       use priority and control the execution of the processor.  
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
