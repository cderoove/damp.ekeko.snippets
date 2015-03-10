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

import java.util.*;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;

import javax.swing.SwingUtilities;

import org.openide.debugger.DebuggerException;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.AbstractThreadGroup;


/**
* JPDA Implementation of debugger thread group.
*/
public class JPDAThreadGroup extends AbstractThreadGroup {

    // variables .................................................................

    /** Thread group which are representated by this AbstractThreadGroup instance */
    private ThreadGroupReference    threadGroup;

    /** ThreadGroupListener-s */
    private Vector                  listener = new Vector (10, 10);


    // init ............................................................................

    /**
    * Creates ThreadGroup for some remote thread group.
    */
    JPDAThreadGroup (JPDAThreadGroup parentThreadGroup) {
        this (parentThreadGroup, null);
    }

    /**
    * Creates ThreadGroup for some remote thread group.
    */
    JPDAThreadGroup (JPDAThreadGroup parentThreadGroup, ThreadGroupReference threadGroup) {
        super (parentThreadGroup);
        this.threadGroup = threadGroup;
    }


    // implementation of AbstractThreadGroup .............................................

    /**
    * Getter for the name of thread group property.
    *
    * @return name of thread.
    */
    public String getName () throws DebuggerException {
        try {
            return threadGroup.name ();
        } catch (Exception e) {
        }
        return "Thread"; // NOI18N
    }


    // helper methods .....................................................................

    ThreadGroupReference getThreadGroupReference () {
        return threadGroup;
    }

    public String toString () {
        try {
            return "Thread Group: " + getName () + " (" + super.toString () + ")"; // NOI18N
        } catch (Exception e) {
            return super.toString ();
        }
    }

    /**
    * Adds given thread group to this threadgroup.
    */
    void addThreadGroup (JPDAThreadGroup threadGroup) {
        super.addThreadGroup (threadGroup);
    }

    /**
    * Removes given thread group from this thread group.
    */
    void removeThreadGroup (JPDAThreadGroup threadGroup) {
        super.removeThreadGroup (threadGroup);
    }

    /**
    * Adds given thread to this threadgroup.
    */
    void addThread (JPDAThread thread) {
        super.addThread (thread);
    }

    /**
    * Removes given thread from this thread group.
    */
    void removeThread (JPDAThread thread) {
        super.removeThread (thread);
    }

    void removeAllD () {
        removeAll ();
    }

    void refresh () {
        AbstractThread[] threads = getThreads ();
        int i, k = threads.length;
        for (i = 0; i < k; i++)
            ((JPDAThread) threads [i]).refresh ();
        AbstractThreadGroup[] groups = getThreadGroups ();
        k = groups.length;
        for (i = 0; i < k; i++)
            ((JPDAThreadGroup) groups [i]).refresh ();
    }
}

/*
 * Log
 *  7    Gandalf   1.6         1/13/00  Daniel Prusa    NOI18N
 *  6    Gandalf   1.5         11/8/99  Jan Jancura     Somma classes renamed
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/15/99  Jan Jancura     
 *  3    Gandalf   1.2         9/2/99   Jan Jancura     
 *  2    Gandalf   1.1         8/17/99  Jan Jancura     Actions for session 
 *       added & Thread group current property
 *  1    Gandalf   1.0         7/13/99  Jan Jancura     
 * $
 */
