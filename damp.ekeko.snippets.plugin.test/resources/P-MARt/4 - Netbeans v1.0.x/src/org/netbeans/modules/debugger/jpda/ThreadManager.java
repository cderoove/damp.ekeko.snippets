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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import com.sun.jdi.*;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.modules.debugger.jpda.util.Operator;


/**
* Manages thread hierarchy. Listen on Operator for creating new threads and removing old ones.
* 
* Synchronization:
*   finish & create hierarchy are synchronized
*   addThread & removeThread are called from one thread...
*
* @author   Jan Jancura
* @version  0.23, May 26, 1998
*/
class ThreadManager implements Executor {

    //  private EventRequestManager        requestManager;

    // stores ThreadReference & ThreadGroupReference => JPDAThread & JPDAThreadGroup
    private HashMap                    referenceToThread;
    private JPDADebugger               debugger;

    /**
    * Creates ThreadManager. It adds Thread requests to given requestManager, and
    * creates default hierarchy of threads with given root thread group.
    * Debugger should be suspended when this method is called.
    */
    ThreadManager (
        JPDADebugger debugger
    ) {
        referenceToThread = new HashMap ();
        this.debugger = debugger;

        createHierarchy (debugger.virtualMachine.topLevelThreadGroups ());

        ThreadStartRequest req1 = debugger.requestManager.createThreadStartRequest ();
        req1.setSuspendPolicy (EventRequest.SUSPEND_ALL);
        debugger.operator.register (req1, this);
        req1.enable ();

        ThreadDeathRequest req3 = debugger.requestManager.createThreadDeathRequest ();
        req3.setSuspendPolicy (EventRequest.SUSPEND_ALL);
        debugger.operator.register (req3, this);
        req3.enable ();
    }


    // interface Executor .....................................................................

    /**
    * Executes thread start and thread death events.
    */
    public void exec (com.sun.jdi.event.Event event) {
        if (event instanceof ThreadStartEvent) {
            try {
                addThread (((ThreadStartEvent) event).thread ());
            } catch (ObjectCollectedException e) {
                // thread add is off-line
            } catch (VMDisconnectedException e) {
                // thread add is off-line
            } catch (Exception e) {
                e.printStackTrace (); //some other problem? [PENDING]
            }
            debugger.operator.resume ();
            return;
        }
        try {
            removeThread (((ThreadDeathEvent) event).thread ());
        } catch (ObjectCollectedException e) {
            // thread remove is off-line
        } catch (VMDisconnectedException e) {
            // thread add is off-line
        } catch (Exception e) {
            e.printStackTrace (); //some other problem? [PENDING]
        }
        debugger.operator.resume ();
    }


    // main methods ........................................................................

    /**
    * Removes al threads and thread groups.
    */
    public synchronized void finish () {
        Iterator i = referenceToThread.values ().iterator ();
        Object ttg;
        while (i.hasNext ())
            if ((ttg = i.next ()) instanceof JPDAThreadGroup)
                ((JPDAThreadGroup) ttg).removeAllD ();
        debugger.threadGroup.removeAllD ();
    }

    /**
    * Create hierarchy for given list of thread groups.
    * Creates top level thread groups, and calls createHierarchy for all of them.
    */
    synchronized void createHierarchy (List l) {
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ThreadGroupReference tgr = (ThreadGroupReference) l.get (i);
            JPDAThreadGroup ttg = createThreadGroup (tgr, debugger.threadGroup);
            createHierarchy (tgr, ttg);
        }
    }

    /**
    * Returns JPDAThread for given ThreadReference.
    */
    JPDAThread findThread (ThreadReference tr) {
        return (JPDAThread) referenceToThread.get (tr);
    }

    /**
    * Returns JPDAThread for given ThreadReference.
    */
    JPDAThread getThread (ThreadReference tr) {
        JPDAThread t = findThread (tr);
        if (t != null) return t;
        return addThread (tr);
    }

    /**
    * Returns JPDAThreadGroup for given ThreadReferenceGroup.
    */
    JPDAThreadGroup getThreadGroup (ThreadGroupReference tgr) {
        return (JPDAThreadGroup) referenceToThread.get (tgr);
    }


    // other methods ........................................................................

    /**
    * Create hierarchy for given threadGroup and its JPDAThreadGroup.
    */
    private void createHierarchy (ThreadGroupReference tgr, JPDAThreadGroup ttg) {
        List l = tgr.threadGroups ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ThreadGroupReference ntgr = (ThreadGroupReference) l.get (i);
            createHierarchy (ntgr, createThreadGroup (ntgr, ttg));
        }
        l = tgr.threads ();
        k = l.size ();
        for (i = 0; i < k; i++)
            createThread ((ThreadReference) l.get (i), ttg);
    }

    /**
    * Adds new thread to the thread hierarchy.
    */
    private JPDAThread addThread (ThreadReference tr) {
        JPDAThread tt = (JPDAThread) referenceToThread.get (tr);
        if (tt != null)
            return tt;
        //removeThread (tr);
        try {
            return createThread (tr, createThreadGroup (tr.threadGroup ()));
        } catch (NullPointerException e) {
            //S ystem.out.println("!!! ThreadManager.thread null " + tr.name ()); // NOI18N
            return null;
        }
    }

    /**
    * Removes existing thread from the thread hierarchy.
    */
    private JPDAThread removeThread (ThreadReference tr) {
        JPDAThreadGroup ttg = (JPDAThreadGroup) referenceToThread.get (tr.threadGroup ());
        if (ttg == null) return null;
        JPDAThread tt = (JPDAThread) referenceToThread.remove (tr);
        ttg.removeThread (tt);
        return tt;
    }

    /**
    * Finds or creates JPDAThreadGroup for given ThreadGroupReference. Creates whole 
    * hierarchy of parents of this thread. 
    *
    * @returns JPDAThreadGroup for given ThreadGroupReference. (Not null!).
    */
    private JPDAThreadGroup createThreadGroup (ThreadGroupReference tgr) {
        JPDAThreadGroup ttg = (JPDAThreadGroup) referenceToThread.get (tgr);
        if (ttg != null) return ttg;
        ThreadGroupReference ptgr = tgr.parent ();
        //    if (ptgr == null) return debugger.threadGroup;
        JPDAThreadGroup pttg = createThreadGroup (ptgr);
        return createThreadGroup (tgr, pttg);
    }

    /**
    * Create JPDAThreadGroup for given ThreadGroupReference and parent thread group.
    */
    private JPDAThreadGroup createThreadGroup (ThreadGroupReference tgr, JPDAThreadGroup pttg) {
        JPDAThreadGroup ttg = new JPDAThreadGroup (pttg, tgr);
        referenceToThread.put (tgr, ttg);
        pttg.addThreadGroup (ttg);
        return ttg;
    }

    /**
    * Create JPDAThread for given ThreadReference and parent thread group.
    */
    private JPDAThread createThread (ThreadReference tr, JPDAThreadGroup pttg) {
        JPDAThread tt = new JPDAThread (debugger, pttg, tr);
        referenceToThread.put (tr, tt);
        pttg.addThread (tt);
        return tt;
    }
}

/*
* Log
*  7    Gandalf   1.6         1/14/00  Daniel Prusa    NOI18N
*  6    Gandalf   1.5         11/9/99  Jan Jancura     Synchronize adding of 
*       threads
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         9/28/99  Jan Jancura     
*  3    Gandalf   1.2         9/2/99   Jan Jancura     
*  2    Gandalf   1.1         8/17/99  Jan Jancura     Actions for session added
*       & Thread group current property
*  1    Gandalf   1.0         7/13/99  Jan Jancura     
* $
*/
