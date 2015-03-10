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

package org.netbeans.core;

import java.io.FileDescriptor;
import java.net.InetAddress;

import org.openide.TopManager;

/** The ThreadGroup for catching uncaught exceptions in Corona.
*
* @author   Ian Formanek
* @version  0.13, May 14, 1998
*/
public class TopThreadGroup extends ThreadGroup {
    /** Constructs a new thread group. The parent of this new group is
    * the thread group of the currently running thread.
    *
    * @param name the name of the new thread group.
    */
    public TopThreadGroup(String name) {
        super(name);
    }

    /** Creates a new thread group. The parent of this new group is the
    * specified thread group.
    * <p>
    * The <code>checkAccess</code> method of the parent thread group is
    * called with no arguments; this may result in a security exception.
    *
    * @param parent the parent thread group.
    * @param name the name of the new thread group.
    * @exception  NullPointerException  if the thread group argument is
    *             <code>null</code>.
    * @exception  SecurityException  if the current thread cannot create a
    *             thread in the specified thread group.
    * @see java.lang.SecurityException
    * @see java.lang.ThreadGroup#checkAccess()
    */
    public TopThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);
    }

    /** The method that gets called when an uncaught exception occurs.
    * It notifies the user via the standard IDE's exception notification
    * system.
    * @param t The Thread in which the exception occured
    * @param e The exception
    */
    public void uncaughtException(Thread t, Throwable e) {
        if (!(e instanceof ThreadDeath)) {
            //      System.err.println("UncaughtException:");
            //      e.printStackTrace();
            //      if (e instanceof org.netbeans.core.execution.ExitSecurityException) return;
            System.err.flush();
            TopManager tm = TopManager.getDefault();
            if (tm != null) {
                tm.notifyException(e);
            } else {
                if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace();
            }
        }
        else super.uncaughtException(t, e);
    }

    /** A Thread that starts the IDE with command line parameters
    * specified in constructor - calls CoronaTopManager.main(args).
    */
    public static class TopThread extends Thread {
        /** The command line args */
        String[] args;

        /** Constructs a new TopThread with specified command-line params.
        * @param args The command-line parameters to be passed to the IDE
        */
        public TopThread(ThreadGroup tg, String[] args) {
            super (tg ,"main"); // NOI18N
            this.args = args;
        }

        /** Starts the thread */
        public void run() {
            Main.main(args);
        }
    }

}

/*
 * Log
 *  7    Gandalf   1.6         1/20/00  Petr Hamernik   rolled back
 *  6    Gandalf   1.5         1/19/00  Petr Nejedly    Commented out debug 
 *       messages
 *  5    Gandalf   1.4         1/13/00  Jaroslav Tulach I18N
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/24/99  Ian Formanek    Printing stack trace on 
 *       netbeans.debug.exceptions property only
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    ThreadDeath is passed to the super in uncaughtException
 *  0    Tuborg    0.13        --/--/98 Ales Novak      ExitSecurityException is passed by
 */
