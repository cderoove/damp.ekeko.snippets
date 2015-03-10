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

package org.netbeans.modules.debugger.support;

import java.util.*;
import java.beans.PropertyChangeEvent;

import javax.swing.SwingUtilities;

import org.openide.debugger.DebuggerException;


public abstract class AbstractThreadGroup {

    /** Current property name. */
    public static final String      PROP_CURRENT = "current"; // NOI18N

    // variables .................................................................

    /** AbstractThreadGroup-s are stored here.
        used in: getThreadGroups () 
     * @associates AbstractThreadGroup*/
    private ArrayList               threadGroups = new ArrayList ();
    /** AbstractThread-s are stored here.
        used in: getThreads () 
     * @associates AbstractThread*/
    private ArrayList               threads = new ArrayList ();

    /** ThreadGroupListener-s 
     * @associates ThreadGroupListener*/
    private Vector                  listener = new Vector (10, 10);
    /** Points to parent thread group or contains null. */
    private AbstractThreadGroup          parentThreadGroup;
    /** Current property value. */
    private boolean                 current = false;


    // init ............................................................................

    /**
    * Creates ThreadGroup for some remote thread group.
    */
    protected AbstractThreadGroup (AbstractThreadGroup parentThreadGroup) {
        this.parentThreadGroup = parentThreadGroup;
    }


    // main methods ...................................................................

    /**
    * Getter for the name of thread group property.
    *
    * @return name of thread.
    */
    public abstract String getName () throws DebuggerException;

    /**
    * Getter for the current property.
    *
    * @return true if this thread group contains current thread or current 
    *  thread group.
    */
    public boolean isCurrent () {
        return current;
    }

    /**
    * Returns parent thread group.
    *
    * @return parent thread group.
    */
    public AbstractThreadGroup getParentThreadGroup () {
        return parentThreadGroup;
    }

    /**
    * Returns array of thread in this thread group.
    *
    * @return array of thread in this thread group.
    */
    public AbstractThread[] getThreads () {
        return (AbstractThread[]) threads.toArray (new AbstractThread [threads.size ()]);
    }

    /**
    * Returns array of thread groups in this thread group.
    *
    * @return array of thread groups in this thread group.
    */
    public AbstractThreadGroup[] getThreadGroups () {
        return (AbstractThreadGroup[]) threadGroups.toArray (
                   new AbstractThreadGroup [threadGroups.size ()]
               );
    }

    /**
    * Suspends / resumes all threads in this thread group recursivelly.
    */
    public void setSuspended (boolean suspended) {
        AbstractThread[] threads = getThreads ();
        int i, k = threads.length;
        for (i = 0; i < k; i++)
            try {
                threads [i].setSuspended (suspended);
            } catch (DebuggerException e ) {
            }
        AbstractThreadGroup[] groups = getThreadGroups ();
        k = groups.length;
        for (i = 0; i < k; i++)
            groups [i].setSuspended (suspended);
    }

    /**
    * Adds thread group listener for this thread group.
    *
    * @param l listener to be added.
    */
    public void addThreadGroupListener (ThreadGroupListener l) {
        listener.addElement (l);
    }

    /**
    * Removes thread group listener from this thread group.
    *
    * @param l listener to be removed.
    */
    public void removeThreadGroupListener (ThreadGroupListener l) {
        listener.removeElement (l);
    }


    // helper methods .....................................................................

    /**
    * Sets property current. Sets this property for parent thread group too.
    */
    void setCurrent (boolean current) {
        if (this.current == current) return;
        this.current = current;
        firePropertyChange (PROP_CURRENT, new Boolean (!current), new Boolean (current));
        AbstractThreadGroup ttg = getParentThreadGroup ();
        if (ttg == null) return;
        ttg.setCurrent (current);
    }

    /**
    * Adds given thread group to this threadgroup.
    */
    protected void addThreadGroup (AbstractThreadGroup threadGroup) {
        threadGroups.add (threadGroup);
        fireThreadGroupCreated (threadGroup);
    }

    /**
    * Removes given thread group from this thread group.
    */
    protected void removeThreadGroup (AbstractThreadGroup threadGroup) {
        threadGroups.remove (threadGroup);
        fireThreadGroupDeath (threadGroup);
    }

    /**
    * Adds given thread to this threadgroup.
    */
    protected void addThread (AbstractThread thread) {
        threads.add (thread);
        fireThreadCreated (thread);
    }

    /**
    * Removes given thread from this thread group.
    */
    protected void removeThread (AbstractThread thread) {
        threads.remove (thread);
        fireThreadDeath (thread);
    }

    /**
    * Removes all threads & thread groups.
    */
    protected void removeAll () {
        int i = threadGroups.size () - 1;
        for (; i >= 0; i--)
            removeThreadGroup ((AbstractThreadGroup) threadGroups.get (i));
        i = threads.size () - 1;
        for (; i >= 0; i--)
            removeThread ((AbstractThread) threads.get (i));
    }

    protected void firePropertyChange (String name, Object oldValue, Object newValue) {
        Vector v = (Vector)listener.clone ();
        int i , k = v.size ();
        for (i = 0; i < k; i++)
            ((ThreadGroupListener)v.elementAt (i)).propertyChange (new PropertyChangeEvent (
                        this, name, oldValue, newValue
                    ));
    }

    protected void fireThreadCreated (final AbstractThread dt) {
        Vector v = (Vector)listener.clone ();
        int i , k = v.size ();
        for (i = 0; i < k; i++)
            ((ThreadGroupListener)v.elementAt (i)).threadCreated (dt);
    }

    protected void fireThreadDeath (final AbstractThread dt) {
        Vector v = (Vector)listener.clone ();
        int i , k = v.size ();
        for (i = 0; i < k; i++)
            ((ThreadGroupListener)v.elementAt (i)).threadDeath (dt);
    }

    protected void fireThreadGroupCreated (final AbstractThreadGroup dtg) {
        Vector v = (Vector)listener.clone ();
        int i , k = v.size ();
        for (i = 0; i < k; i++)
            ((ThreadGroupListener)v.elementAt (i)).threadGroupCreated (dtg);
    }

    protected void fireThreadGroupDeath (final AbstractThreadGroup dtg) {
        Vector v = (Vector)listener.clone ();
        int i , k = v.size ();
        for (i = 0; i < k; i++)
            ((ThreadGroupListener)v.elementAt (i)).threadGroupDeath (dtg);
    }

    public String toString () {
        try {
            return "The Thread Group: " + getName () + " (" + super.toString () + ")"; // NOI18N
        } catch (Exception e) {
            return super.toString ();
        }
    }
}

/*
* Log
*  2    Gandalf   1.1         1/13/00  Daniel Prusa    NOI18N
*  1    Gandalf   1.0         11/8/99  Jan Jancura     
* $
*/
