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

package org.netbeans.modules.debugger.delegator;

import java.util.*;
import java.beans.PropertyChangeEvent;
import javax.swing.SwingUtilities;

import sun.tools.debug.RemoteThreadGroup;
import sun.tools.debug.RemoteThread;

import org.openide.debugger.DebuggerException;
import org.netbeans.modules.debugger.support.AbstractThreadGroup;
import org.netbeans.modules.debugger.support.ThreadGroupListener;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.util.*;


/**
* Core thread group delegates all functionality on inner instance of thread group.
*/
public class DelegatingThreadGroup extends AbstractThreadGroup {

    // variables .................................................................

    /** Delegating group */
    private AbstractThreadGroup  threadGroup;
    private Delegator            delegator = new Delegator ();


    // init ............................................................................

    /**
    * Creates DelegatingThreadGroup without delegating object.
    */
    DelegatingThreadGroup () {
        super (null);
    }


    // implementation of AbstractThreadGroup .............................................

    /**
    * Getter for the name of thread group property.
    *
    * @return name of thread.
    */
    public String getName () throws DebuggerException {
        if (threadGroup == null)
            return org.openide.util.NbBundle.getBundle (DelegatingThreadGroup.class).
                   getString ("CTL_Thread_group_root");
        return threadGroup.getName ();
    }

    /**
    * Returns array of thread in this thread group.
    *
    * @return array of thread in this thread group.
    */
    public AbstractThread[] getThreads () {
        if (threadGroup == null)
            return new AbstractThread [0];
        return threadGroup.getThreads ();
    }

    /**
    * Returns array of thread groups in this thread group.
    *
    * @return array of thread groups in this thread group.
    */
    public AbstractThreadGroup[] getThreadGroups () {
        if (threadGroup == null)
            return new AbstractThreadGroup [0];
        return threadGroup.getThreadGroups ();
    }

    /**
    * Suspends / resumes all threads in this thread group recursivelly.
    */
    public void setSuspended (boolean suspended) {
        if (threadGroup == null)
            return;
        threadGroup.setSuspended (suspended);
    }


    // main methods .....................................................................

    /**
    * Sets RemoteThreadGroup for this object.
    *
    * @param threadGroup RemoteThreadGroup which must be represented by this ToolsThread
    * instance. Id can be <CODE>null</CODE>.
    */
    void setRemoteThreadGroup (AbstractThreadGroup threadGroup) {
        if (this.threadGroup != null)
            this.threadGroup.removeThreadGroupListener (delegator);
        this.threadGroup = threadGroup;
        updateContent ();
        threadGroup.addThreadGroupListener (delegator);
    }


    // helper methods .....................................................................

    private void updateContent () {
        AbstractThreadGroup[] ttg = threadGroup.getThreadGroups ();
        int i, k = ttg.length;
        for (i = 0; i < k; i++)
            fireThreadGroupCreated (ttg [i]);
        AbstractThread[] tt = threadGroup.getThreads ();
        k = tt.length;
        for (i = 0; i < k; i++)
            fireThreadCreated (tt [i]);
    }

    void firePropertyChangeHelper (PropertyChangeEvent e) {
        firePropertyChange (e.getPropertyName (), e.getOldValue (), e.getNewValue ());
    }

    void fireThreadCreatedHelper (AbstractThread dt) {
        fireThreadCreated (dt);
    }

    void fireThreadDeathHelper (AbstractThread dt) {
        fireThreadDeath (dt);
    }

    void fireThreadGroupCreatedHelper (AbstractThreadGroup dtg) {
        fireThreadGroupCreated (dtg);
    }

    void fireThreadGroupDeathHelper (AbstractThreadGroup dtg) {
        fireThreadGroupDeath (dtg);
    }


    // innerclasses .....................................................................

    class Delegator implements ThreadGroupListener {

        /**  Called when some propertyvlaue is changed in this thread group.
        *
        * @param e event
        */
        public void propertyChange (PropertyChangeEvent e) {
            firePropertyChangeHelper (e);
        }

        /**  Called when some thread is created in this thread group.
        *
        * @param t this new thread
        */
        public void threadCreated (AbstractThread t) {
            fireThreadCreatedHelper (t);
        }
        /**  Called when some thread is destroyed in this thread group.
        *
        * @param t destroyed thread
        */
        public void threadDeath (AbstractThread t) {
            fireThreadDeathHelper (t);
        }
        /**  Called when some thread group is created in this thread group.
        *
        * @param t this new thread group.
        */
        public void threadGroupCreated (AbstractThreadGroup g) {
            fireThreadGroupCreatedHelper (g);
        }
        /**  Called when some thread group is destroyed in this thread group.
        *
        * @param t destroyed thread group.
        */
        public void threadGroupDeath (AbstractThreadGroup g) {
            fireThreadGroupDeathHelper (g);
        }
    }
}


/*
* Log
*  1    Gandalf   1.0         11/9/99  Jan Jancura     
* $
*/
