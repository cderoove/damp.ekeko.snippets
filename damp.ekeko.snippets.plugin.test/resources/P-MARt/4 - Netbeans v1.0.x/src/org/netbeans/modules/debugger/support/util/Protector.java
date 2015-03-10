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
import org.openide.util.RequestProcessor.Task;

/**
* Helper class for returning values and exceptions from 
* RequestSynchronizer.
*
* @author Jan Jancura
*/
public abstract class Protector implements Runnable {

    // static ....................................................................

    private static int        timeout = 8000;

    public static void setTimeout (int t) {
        timeout = t;
    }

    public static int getTimeout () {
        return timeout;
    }


    // variables .................................................................

    private String            name;
    private Object            result;
    private Task              task;


    // init ......................................................................

    /**
    * Create a new instance of protector with given name.
    * Write code to protect to method protect (), and call
    * one of go (), wait () and throwAndWait () methods.
    */
    public Protector (
        String name
    ) {
        this.name = name;
    }


    // main methods ..............................................................

    /**
    * Runs code in protect () method asynchronly in a new 
    * thread in the given synchronizer.
    */
    public void go (
        RequestSynchronizer synchronizer,
        RequestSynchronizer.RequestWaiter killer
    ) {
        if ((synchronizer.requestorThread != null) &&
                Thread.currentThread ().equals (synchronizer.requestorThread)
           ) {
            //S ystem.out.println ("  PROTECTOR g " + name); // NOI18N
            run ();
            //S ystem.out.println ("  PROTECTOR ge " + name); // NOI18N
        } else {
            //S ystem.out.println ("PROTECTOR g " + name); // NOI18N
            synchronizer.postRequest (this, timeout, killer);
            //S ystem.out.println ("PROTECTOR ge " + name); // NOI18N
        }
    }

    /**
    * Runs code in protect () method synchronly in a new 
    * thread in the given synchronizer and returns value.
    */
    public Object wait (
        RequestSynchronizer synchronizer,
        RequestSynchronizer.RequestWaiter killer
    ) {
        if ((synchronizer.requestorThread != null) &&
                Thread.currentThread ().equals (synchronizer.requestorThread)
           ) {
            //S ystem.out.println ("  PROTECTOR w " + name); // NOI18N
            run ();
            //S ystem.out.println ("  PROTECTOR we " + name); // NOI18N
        } else {
            //S ystem.out.println ("PROTECTOR w " + name); // NOI18N
            synchronizer.postRequestAndWait (this, timeout, killer);
            //S ystem.out.println ("PROTECTOR we " + name); // NOI18N
        }
        return getResult ();
    }

    /**
    * Runs code in protect () method synchronly in a new 
    * thread in the given synchronizer and returns value
    * or throws exception.
    */
    public Object throwAndWait (
        RequestSynchronizer synchronizer,
        RequestSynchronizer.RequestWaiter killer
    ) throws Exception {
        if ((synchronizer.requestorThread != null) &&
                Thread.currentThread ().equals (synchronizer.requestorThread)
           ) {
            //S ystem.out.println ("  PROTECTOR tw " + name); // NOI18N
            run ();
            //S ystem.out.println ("  PROTECTOR twe " + name); // NOI18N
        } else {
            //S ystem.out.println ("PROTECTOR tw " + name); // NOI18N
            synchronizer.postRequestAndWait (this, timeout, killer);
            //S ystem.out.println ("PROTECTOR twe " + name); // NOI18N
        }
        return getResultOrThrow ();
    }

    // .....................................................

    /**
    * Runs code in protect () method asynchronly in a new 
    * thread.
    */
    public void go (
        final RequestSynchronizer.RequestWaiter killer
    ) {
        final Thread t = new Thread (this, "Thread " + name + " protector"); // NOI18N
        if (killer == null)
            task = RequestProcessor.postRequest (
                       new Runnable () {
                       public void run () {
                           t.interrupt ();
                           t.stop ();
                       }
                   },
                   timeout
               );
        else
            task = RequestProcessor.postRequest (
                       new Runnable () {
                       public void run () {
                           killer.run (t);
                       }
                   },
                   timeout
               );
        t.start ();
    }

    /**
    * Runs code in protect () method synchronly in a new 
    * thread in the given synchronizer and returns value.
    */
    public synchronized Object wait (
        final RequestSynchronizer.RequestWaiter killer
    ) {
        final Thread t = new Thread (this, "Thread " + name + " protector"); // NOI18N
        if (killer == null)
            task = RequestProcessor.postRequest (
                       new Runnable () {
                       public void run () {
                           //S ystem.out.println("FINISH REQUEST " + name); // NOI18N
                           t.interrupt ();
                           t.stop ();
                       }
                   },
                   timeout
               );
        else
            task = RequestProcessor.postRequest (
                       new Runnable () {
                       public void run () {
                           killer.run (t);
                       }
                   },
                   timeout
               );
        t.start ();
        try {
            wait ();
        } catch (InterruptedException e) {
        }
        return getResult ();
    }

    /**
    * Runs code in protect () method synchronly in a new 
    * thread in the given synchronizer and returns value
    * or throws exception.
    */
    public synchronized Object throwAndWait (
        final RequestSynchronizer.RequestWaiter killer
    ) throws Exception {
        final Thread t = new Thread (this, "Thread " + name + " protector"); // NOI18N
        if (killer == null)
            task = RequestProcessor.postRequest (
                       new Runnable () {
                       public void run () {
                           //S ystem.out.println("FINISH REQUEST " + name); // NOI18N
                           t.interrupt ();
                           t.stop ();
                       }
                   },
                   timeout
               );
        else
            task = RequestProcessor.postRequest (
                       new Runnable () {
                       public void run () {
                           killer.run (t);
                       }
                   },
                   timeout
               );
        t.start ();
        try {
            wait ();
        } catch (InterruptedException e) {
        }
        return getResultOrThrow ();
    }


    /**
    * Method where to store protected code.
    */
    public abstract Object protect () throws Exception;

    // .....................................................

    /**
    * 
    */
    public static Task register (final String s) {
        final Thread t = Thread.currentThread ();
        Runnable r;
        Task ta = RequestProcessor.postRequest (
                      r = new Runnable () {
                              public void run () {
                                  //S ystem.out.println("FINISH THREAD " + s + " (" + t + ")"); // NOI18N
                                  //t.stop (new InternalError ());
                                  t.interrupt ();
                              }
                          },
                      timeout
                  );
        //S ystem.out.println("START TASK thr " + t + "\n    :ta " + ta + "\n    : " + r); // NOI18N
        return ta;
    }


    // helper methods ............................................................

    public synchronized final void run () {
        //S ystem.out.println ("    PROTECTOR RUN " + name); // NOI18N
        try {
            result = protect ();
        } catch (Throwable t) {
            result = t;
            if (t instanceof ThreadDeath) {
                notify ();
                throw (ThreadDeath)t;
            }
        }
        if (task != null) task.cancel ();
        notify ();
        //S ystem.out.println ("    PROTECTOR END " + name); // NOI18N
    }

    public String toString () {
        return name;
    }

    private Object getResultOrThrow () throws Exception {
        if (result instanceof Throwable) {
            if (result instanceof Error) throw (Error) result;
            if (result instanceof Exception) throw (Exception) result;
        }
        return result;
    }

    private Object getResult () {
        if (result instanceof Throwable) {
            if (result instanceof Error) throw (Error) result;
            if (result instanceof RuntimeException) throw (RuntimeException) result;
        }
        return result;
    }
}

/*
 * Log
 *  9    Gandalf-post-FCS1.7.4.0     3/28/00  Daniel Prusa    
 *  8    Gandalf   1.7         1/14/00  Daniel Prusa    NOI18N
 *  7    Gandalf   1.6         1/13/00  Daniel Prusa    NOI18N
 *  6    Gandalf   1.5         12/10/99 Jan Jancura     Deadlock protection for 
 *       JPDA
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         7/2/99   Jan Jancura     Session debugging 
 *       support
 *  3    Gandalf   1.2         6/4/99   Jan Jancura     
 *  2    Gandalf   1.1         6/4/99   Jan Jancura     
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
