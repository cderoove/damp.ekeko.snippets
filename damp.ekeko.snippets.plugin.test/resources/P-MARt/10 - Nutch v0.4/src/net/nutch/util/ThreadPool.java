/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.util.*;
import java.util.logging.*;
import net.nutch.util.LogFormatter;

/************************************************
 * ThreadPool.java                                                 
 *
 * ThreadPool maintains a large set of threads, which
 * can be dedicated to a certain task, and then recycled.
 ***********************************************/
public class ThreadPool {
    /**
     * A TaskThread sits in a loop, asking the pool
     * for a job, and servicing it.  That's all it does.
     */
    class TaskThread extends Thread {
        /**
         * Get a job from the pool, run it, repeat.
         * If the job is null, we exit the loop.
         */
        public void run() {
            while (true) {
                Runnable r = obtainJob();
                if (r == null) {
                    break;
                }
                try {
                    r.run();
                } catch (Exception e) {
                    System.err.println("E: " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    int numThreads;
    boolean running = false;
    Vector jobs;

    /**
     * Creates a pool of numThreads size.
     * These threads sit around waiting for jobs to
     * be posted to the list.
     */
    public ThreadPool(int numThreads) {
        this.numThreads = numThreads;
        jobs = new Vector(37);
        running = true;

        for (int i = 0; i < numThreads; i++) {
            TaskThread t = new TaskThread();
            t.start();
        }
        Logger l = LogFormatter.getLogger("net.nutch.util");
        l.fine("ThreadPool created with " + numThreads + " threads.");
    }

    /**
     * Gets a job from the queue, returns to worker thread.
     * When the pool is closed down, return null for all
     * obtainJob() requests.  That tells the thread to
     * shut down.
     */
    Runnable obtainJob() {
        Runnable job = null;

        synchronized (jobs) {
            while (job == null && running) {
                try {
                    if (jobs.size() == 0) {
                        jobs.wait();
                    }
                } catch (InterruptedException ie) {
                }

                if (jobs.size() > 0) {
                    job = (Runnable) jobs.firstElement();
                    jobs.removeElementAt(0);
                }
            }
        }

        if (running) {
            // Got a job from the queue
            return job;
        } else {
            // Shut down the pool
            return null;
        }
    }

    /**
     * Post a Runnable to the queue.  This will be
     * picked up by an active thread.
     */
    public void addJob(Runnable runnable) {
        synchronized (jobs) {
            jobs.add(runnable);
            jobs.notifyAll();
        }
    }

    /**
     * Turn off the pool.  Every thread, when finished with
     * its current work, will realize that the pool is no
     * longer running, and will exit.
     */
    public void shutdown() {
        running = false;
        Logger l = LogFormatter.getLogger("net.nutch.util");
        l.fine("ThreadPool shutting down.");
    }
}
