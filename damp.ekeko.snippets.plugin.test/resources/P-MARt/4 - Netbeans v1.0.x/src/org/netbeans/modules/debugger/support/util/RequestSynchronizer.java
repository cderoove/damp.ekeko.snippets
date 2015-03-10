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

import org.openide.util.RequestProcessor;

/**
* RequestSynchronizer executes requests synchronically. Moreover it watch them and
* can kill them (or run some other action) on some timeout.
*
* @author Jan Jancura
* @version
*/
public class RequestSynchronizer extends Object {

    Integer                 LOCK = new Integer (0);
    Thread                  requestorThread = null;
    RequestProcessor        processor = new RequestProcessor ();
    RequestProcessor.Task   task;

    long                    lastStart;
    boolean                 releaseOn = false;

    /**
    * Creates new RequestSynchronizer.
    */
    public RequestSynchronizer() {
    }

    /**
    * Executes runnable. If wait == false first thread is executed assynchronly, the others
    * waits for the previous requests.
    *
    * @param r Request.
    * @param wait If true postRequest waits for end of request.
    */
    public synchronized void postRequest (final Runnable r, boolean wait) {
        if (releaseOn) return;
        //if (requestorThread != null) S ystem.out.println ("Waiting " + r); // NOI18N
        synchronized (LOCK) {
            if (requestorThread != null)
                try {
                    LOCK.wait ();
                    if (releaseOn) return;
                } catch (InterruptedException e) {
                }
            (requestorThread = new Thread ("Request " + r + " thread") { // NOI18N
                                   public void run () {
                                       synchronized (LOCK) {
                                           //S ystem.out.println ("Start " + r); // NOI18N
                                           lastStart = System.currentTimeMillis ();
                                           try {
                                               r.run ();
                                           } catch (Throwable e) {
                                               //e.printStackTrace ();
                                           }
                                           //t = System.currentTimeMillis () - lastStart;
                                           requestorThread = null;
                                           //S ystem.out.println ("End " + r + " time: " + t); // NOI18N
                                           LOCK.notify ();
                                       }
                                   }
                               }).start ();
            try {
                if (wait) LOCK.wait ();
            } catch (InterruptedException e) {
            }
        } // synchronized LOCK end
    }

    /**
    * Executes runnable. First thread is executed assynchronly, the others
    * waits for the previous requests.
    *
    * @param r Request.
    */
    public synchronized void postRequest (final Runnable r) {
        postRequest (r, false);
    }

    /**
    * Executes runnable. All threads is executed synchronly and waits
    * for the previous requests.
    *
    * @param r Request.
    */
    public synchronized void postRequestAndWait (final Runnable r) {
        postRequest (r, true);
    }

    /**
    * Executes runnable. First thread is executed assynchronly, the others
    * waits for the previous requests. Requests is killed (probably) after timeout.
    *
    * @param quest Request.
    * @param time Timeout for request.
    */
    public synchronized void postRequest (Runnable r, int time) {
        postRequest (r, time, new RequestWaiter () {
                         public void run (Thread t) {
                             requestorThread.interrupt ();
                             requestorThread.stop ();
                             //S ystem.out.println ("Kill " + requestorThread.getName ()); // NOI18N
                         }
                     });
    }

    /**
    * Executes runnable. All threads is executed synchronly and waits
    * for the previous requests. Requests is killed (probably) after timeout.
    *
    * @param quest Request.
    * @param time Timeout for request.
    */
    public synchronized void postRequestAndWait (Runnable r, int time) {
        postRequest (r, time, new RequestWaiter () {
                         public void run (Thread t) {
                             requestorThread.interrupt ();
                             requestorThread.stop ();
                             //S ystem.out.println ("Kill " + requestorThread.getName ()); // NOI18N
                         }
                     });
    }

    /**
    * Executes runnable. First thread is executed assynchronly, the others
    * waits for the previous requests. Waiter is executed after timeout.
    *
    * @param quest Request.
    * @param time Timeout for request.
    * @param waiter Waiter will be executed if quest will not stop before the timeout.
    */
    public synchronized void postRequest (
        final Runnable quest,
        int time,
        final RequestWaiter waiter
    ) {
        if (releaseOn) return;
        postRequest (quest);
        final Thread t = requestorThread;
        if (task != null) task.cancel ();
        task = processor.postRequest (new Runnable () {
                                          public void run () {
                                              if (requestorThread == null) return;
                                              if (t == requestorThread) {
                                                  long timeR = System.currentTimeMillis () - lastStart;
                                                  //S ystem.out.println ("Start waiter for " + quest + " time: " + timeR); // NOI18N
                                                  waiter.run (requestorThread);
                                              }
                                          }
                                      }, time);
    }

    /**
    * Executes runnable. All threads is executed synchronly and waits
    * for the previous requests. Waiter is executed after timeout.
    *
    * @param quest Request.
    * @param time Timeout for request. 
    * @param waiter Waiter will be executed if quest will not stop before the timeout.
    */
    public synchronized void postRequestAndWait (
        final Runnable quest,
        int time,
        final RequestWaiter waiter
    ) {
        if (releaseOn) return;
        synchronized (LOCK) {
            postRequest (quest);
            final Thread t = requestorThread;
            if (task != null) task.cancel ();
            task = processor.postRequest (new Runnable () {
                                              public void run () {
                                                  if (requestorThread == null) return;
                                                  if (t == requestorThread) {
                                                      long timeR = System.currentTimeMillis () - lastStart;
                                                      //S ystem.out.println ("Start waiter for " + quest + " time: " + timeR); // NOI18N
                                                      waiter.run (requestorThread);
                                                  }
                                              }
                                          }, time);
            try {
                LOCK.wait ();
            } catch (InterruptedException e) {
            }
        } // synchronized LOCK end
    }

    /**
    * Finishs executing.
    */
    public void releaseAll () {
        releaseOn = true;
        if (requestorThread != null) {
            requestorThread.interrupt ();
            requestorThread.stop ();
            //S ystem.out.println ("Kill " + requestorThread.getName ()); // NOI18N
        }
    }

    public static interface RequestWaiter {
        public void run (Thread t);
    }
}

/*
 * Log
 *  9    Gandalf   1.8         1/14/00  Daniel Prusa    NOI18N
 *  8    Gandalf   1.7         1/13/00  Daniel Prusa    NOI18N
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         7/2/99   Jan Jancura     Session debugging 
 *       support
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         6/4/99   Jan Jancura     
 *  3    Gandalf   1.2         6/4/99   Jan Jancura     
 *  2    Gandalf   1.1         6/4/99   Jan Jancura     
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
