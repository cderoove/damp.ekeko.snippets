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

package org.netbeans.modules.debugger.debug;

import java.util.*;
import javax.swing.SwingUtilities;

import sun.tools.debug.RemoteThreadGroup;
import sun.tools.debug.RemoteThread;

import org.openide.debugger.DebuggerException;

import org.netbeans.modules.debugger.support.AbstractThreadGroup;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.util.*;


/**
* Inner representation of one thread group.
*/
public class ToolsThreadGroup extends AbstractThreadGroup {

    // variables .................................................................

    ToolsDebugger                    debugger;

    private HashMap                  oldContent = new HashMap ();


    /** Thread group which are representated by this AbstractThreadGroup instance */
    private RemoteThreadGroup       threadGroup;


    // init ............................................................................

    /**
    * Creates empty ThreadGroup.
    */
    ToolsThreadGroup (
        ToolsDebugger debugger,
        ToolsThreadGroup parentThreadGroup
    ) {
        this (debugger, parentThreadGroup, null);
    }

    /**
    * Creates ThreadGroup for some remote thread group.
    *
    * @param threadGroup RemoteThreadGroup which must be represented by this AbstractThread
    * instance. Id can be <CODE>null</CODE>.
    */
    ToolsThreadGroup (
        ToolsDebugger debugger,
        ToolsThreadGroup parentThreadGroup,
        RemoteThreadGroup threadGroup
    ) {
        super (parentThreadGroup);
        this.threadGroup = threadGroup;
        this.debugger = debugger;
    }


    // implementation of AbstractThreadGroup .............................................

    /**
    * Getter for the name of thread group property.
    *
    * @return name of thread.
    */
    public String getName () throws DebuggerException {
        if (debugger.synchronizer == null)
            return debugger.bundle.getString ("CTL_Thread_group_root");;
        if (threadGroup == null)
            return debugger.bundle.getString ("CTL_Thread_group_root");

        try {
            return (String) new Protector ("AbstractThreadGroup.getName") { // NOI18N
                       public Object protect () throws Exception {
                           return threadGroup.getName ();
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            throw new DebuggerException (e);
        }
    }


    // helper methods .....................................................................

    /**
    * Sets RemoteThreadGroup for this object.
    *
    * @param threadGroup RemoteThreadGroup which must be represented by this ToolsThread
    * instance. Id can be <CODE>null</CODE>.
    */
    void setRemoteThreadGroup (RemoteThreadGroup threadGroup) {
        this.threadGroup = threadGroup;
        try {
            threadChanged ();
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            debugger.println (debugger.bundle.getString ("EXC_Debugger") + ": " + e, debugger.ERR_OUT);
            return;
        }
    }

    /**
    * Updates state of threads in this thread group.
    */
    void threadChanged () {
        RequestSynchronizer rs = debugger.synchronizer;
        if (rs == null) return;
        //    synchronized (rs) {
        try {
            Iterator g = directChildren ();
            HashMap newContent = new HashMap ();
            ToolsThreadGroup ttg;
            while (g.hasNext ()) {
                RemoteThreadGroup rtg = (RemoteThreadGroup) g.next ();
                if ((ttg = (ToolsThreadGroup) oldContent.get (rtg)) == null) {
                    //new threadGroup
                    ttg = new ToolsThreadGroup (debugger, this, rtg);
                    try {
                        debugger.println (debugger.bundle.getString ("CTL_New_thread_group") +
                                          ": " + ttg.getName (), debugger.ERR_OUT); // NOI18N
                    } catch (Throwable e) {
                        if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                    }
                    addThreadGroup (ttg);
                } else {
                    //existing threadGroup
                    oldContent.remove (rtg);
                }
                ttg.threadChanged ();
                newContent.put (rtg, ttg);
            }

            //threads
            if (threadGroup != null) {
                RemoteThread[] t = listThreads ();
                ToolsThread tt;
                int i, k = t.length;
                for (i = 0; i < k; i++) {
                    try {
                        String stat = getStatus (t [i]);
                        if ((stat != null) && stat.equals ("zombie")) { // NOI18N
                            continue;
                        }
                    } catch (Throwable e) {
                        if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                    }
                    if ((tt = (ToolsThread) oldContent.get (t [i])) == null) {
                        //new thread
                        tt = new ToolsThread (debugger, this, t [i]);
                        try {
                            debugger.println (debugger.bundle.getString ("CTL_New_thread") +
                                              ": " + tt.getName (), debugger.ERR_OUT); // NOI18N
                        } catch (Throwable e) {
                            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                        }
                        addThread (tt);
                    } else {
                        //existing thread
                        tt.threadChanged ();
                        oldContent.remove (t [i]);
                    }
                    newContent.put (t [i], tt);
                }
            }

            //remove threads
            Iterator it = oldContent.values ().iterator ();
            while (it.hasNext ()) {
                Object o = it.next ();
                if (o instanceof ToolsThread) {
                    ToolsThread t = (ToolsThread) o;
                    try {
                        debugger.println (debugger.bundle.getString ("CTL_Thread_destroyed") +
                                          ": " + t.getName (), debugger.ERR_OUT); // NOI18N
                    } catch (Throwable e) {
                        if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                    }
                    removeThread (t);
                } else {
                    ToolsThreadGroup gg = (ToolsThreadGroup) o;
                    try {
                        debugger.println (debugger.bundle.getString ("CTL_Thread_group_destroyed") +
                                          ": " + gg.getName (), debugger.ERR_OUT); // NOI18N
                    } catch (Throwable e) {
                        if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                    }
                    removeThreadGroup (gg);
                }
            }
            oldContent = newContent;
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
        }
        //    }
    }

    /**
    * Finds ToolsThread for given RemoteThread recursivelly.
    */
    ToolsThread getThread (RemoteThread rt) {
        AbstractThread[] tt = getThreads ();
        ToolsThread r;
        int i, k = tt.length;
        for (i = 0; i < k; i++)
            if ((r = (ToolsThread) tt [i]).getRemoteThread ().equals (rt))
                return r;
        AbstractThreadGroup[] ttg = getThreadGroups ();
        k = ttg.length;
        for (i = 0; i < k; i++)
            if ((r = ((ToolsThreadGroup) ttg [i]).getThread (rt)) != null)
                return r;
        return null;
    }


    // pricvate methods ..........................................................................................

    /**
    * Returns threadgroups for this thread group.
    * UNSAFE METHOD !!!!!!!!!!!!!!!
    * @return children of the threadgroup not recursively.
    */
    private Iterator directChildren () throws Exception {
        RemoteThreadGroup[] all = listThreadGroups ();
        HashSet ch = new HashSet (Arrays.asList (all));
        int i, k = all.length;
        for (i = 0; i < k; i++) {
            RemoteThreadGroup[] subChildren = debugger.remoteDebugger.listThreadGroups (
                                                  all [i]
                                              );
            int j, l = subChildren.length;
            for (j = 0; j < l; j++)
                ch.remove (subChildren [j]);
        }
        return ch.iterator ();
    }

    /**
    * Returns array of thread from current thread group.
    */
    private RemoteThread[] listThreads () {
        if (debugger.synchronizer == null) return new RemoteThread [0];
        return (RemoteThread[]) new Protector ("ToolsThreadGroup.listThreads") { // NOI18N
                   public Object protect () throws Exception {
                       try {
                           return threadGroup.listThreads (false);
                       } catch (Throwable e) {
                           if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                       }
                       return new RemoteThread [0];
                   }
               }.wait (debugger.synchronizer, debugger.killer);
    }

    /**
    * Returns array of all thread groups from current thread group.
    */
    private RemoteThreadGroup[] listThreadGroups () {
        if (debugger.synchronizer == null) return new RemoteThreadGroup [0];
        return (RemoteThreadGroup[]) new Protector ("ToolsThreadGroup.listThreadGroups") { // NOI18N
                   public Object protect () throws Exception {
                       try {
                           return debugger.remoteDebugger.listThreadGroups (threadGroup);
                       } catch (Throwable e) {
                           if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                       }
                       return new RemoteThreadGroup [0];
                   }
               }.wait (debugger.synchronizer, debugger.killer);
    }

    /**
    * Returns status of given thread.
    */
    private String getStatus (final RemoteThread t) {
        if (debugger.synchronizer == null) return ""; // NOI18N
        return (String) new Protector ("ToolsThreadGroup.getStatus") { // NOI18N
                   public Object protect () throws Exception {
                       try {
                           return t.getStatus ();
                       } catch (Throwable e) {
                           if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                       }
                       return new RemoteThreadGroup [0];
                   }
               }.wait (debugger.synchronizer, debugger.killer);
    }
}

/*
 * Log
 *  12   Gandalf   1.11        1/13/00  Daniel Prusa    NOI18N
 *  11   Gandalf   1.10        1/6/00   Jan Jancura     Refresh of Threads & 
 *       Watches, Weakization of Nodes
 *  10   Gandalf   1.9         11/29/99 Jan Jancura     Bug 3341 - bad \n in 
 *       output of debugger
 *  9    Gandalf   1.8         11/8/99  Jan Jancura     Somma classes renamed
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/17/99  Jan Jancura     Actions for session 
 *       added & Thread group current property
 *  6    Gandalf   1.5         7/24/99  Jan Jancura     Bug in Suspend / resume 
 *       thread in enterprise deb.
 *  5    Gandalf   1.4         7/13/99  Jan Jancura     
 *  4    Gandalf   1.3         7/2/99   Jan Jancura     Session debugging 
 *       support
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/4/99   Jan Jancura     
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
