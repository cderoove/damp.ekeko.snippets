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

import java.beans.*;
import java.util.*;

import javax.swing.SwingUtilities;

import org.openide.text.Line;
import org.openide.debugger.DebuggerException;

import org.netbeans.modules.debugger.support.util.Utils;

public abstract class AbstractThread {

    // variables .......................................................................

    protected AbstractDebugger           debugger;
    private boolean                      current = false;
    private PropertyChangeSupport        pcs;
    /** Points to parent thread group or contains null. */
    private AbstractThreadGroup          parentThreadGroup;


    // init ............................................................................

    protected AbstractThread (AbstractDebugger debugger, AbstractThreadGroup parentThreadGroup) {
        this.parentThreadGroup = parentThreadGroup;
        this.debugger = debugger;
        pcs = new PropertyChangeSupport (this);
    }


    // main methods ....................................................................

    /**
    * Getter for the name of thread property.
    *
    * @throw DebuggerException if some problem occurs.
    * @return name of thread.
    */
    public abstract String getName () throws DebuggerException;

    /**
    * Returns parent thread group.
    *
    * @return parent thread group.
    */
    public AbstractThreadGroup getParentThreadGroup () {
        return parentThreadGroup;
    }

    /**
    * If this thread is suspended returns line number where this thread is stopped.
    *
    * @throw DebuggerException if some problem occurs.
    * @return line number where this thread is stopped.
    */
    public abstract int getLineNumber () throws DebuggerException;

    /**
    * Returns string representation of the current state of this thread (depends on
    * debugger implementation).
    *
    * @throw DebuggerException if some problem occurs.
    * @return string representation of the current state of this thread.
    */
    public abstract String getState () throws DebuggerException;

    /**
    * If this thread is suspended returns class name where this thread is stopped.
    *
    * @throw DebuggerException if some problem occurs.
    * @return class name where this thread is stopped.
    */
    public abstract String getClassName () throws DebuggerException;

    /**
    * If this thread is suspended returns method name where this thread is stopped.
    *
    * @throw DebuggerException if some problem occurs.
    * @return method name where this thread is stopped.
    */
    public abstract String getMethod () throws DebuggerException;

    /**
    * Returns current stack depth.
    *
    * @throw DebuggerException if some problem occurs.
    * @return current stack depth.
    */
    public abstract int getStackDepth () throws DebuggerException;

    /**
    * Returns true if this thread is suspended.
    *
    * @throw DebuggerException if some problem occurs.
    * @return true if this thread is suspended.
    */
    public abstract boolean isSuspended () throws DebuggerException;

    /**
    * Setter method for the suspend property.
    *
    * @throw DebuggerException if some problem occurs.
    * @param suspend true if this thread might be suspend.
    */
    public void setSuspended (final boolean suspend) throws DebuggerException {
        //S ystem.out.println ("AbstractThread.setSuspended "+ suspend + " : " + isCurrent ()); // NOI18N
        if (isCurrent ()) {
            //S ystem.out.println ("AbstractThread.setSuspended "+ debugger); // NOI18N
            if (suspend) {
                setCurrent (true);
                debugger.updateWatches ();
                debugger.setDebuggerState (debugger.DEBUGGER_STOPPED);
            } else {
                debugger.unmarkCurrent ();
                debugger.setDebuggerState (debugger.DEBUGGER_RUNNING);
            }
        }
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            firePropertyChange (null, null, null);
                                        }
                                    });
    }

    /**
    * Returns true if this thread is current.
    */
    public boolean isCurrent () {
        return current;
    }

    /**
    * Sets this thread current thread.
    * Calls debugger.setCurrentThread (this) and
    * marks current line in editor or show cursor on some stack frame
    * above, if current class is sourceless.
    *
    * @return true if the current thread switching is successfull.
    */
    public boolean setCurrent (boolean current) {
        this.current = current;
        AbstractThread tt = debugger.getCurrentThread ();
        if (!current) {
            // set non-current
            if ( (tt != null) &&
                    tt.equals (this)
               ) debugger.setCurrentThread (null);
            if (getParentThreadGroup () != null)
                getParentThreadGroup ().setCurrent (false);
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                pcs.firePropertyChange (null, null, null);
                                            }
                                        });
            debugger.unmarkCurrent ();
            return true;
        }

        // make old current thread non-current
        if ((tt != null) && (tt != this))
            tt.setCurrent (false);

        // change thread state (icon..)
        debugger.setCurrentThread (this);
        if (getParentThreadGroup () != null)
            getParentThreadGroup ().setCurrent (true);
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            pcs.firePropertyChange (null, null, null);
                                        }
                                    });

        if (debugger.isFollowedByEditor ()) {
            // mark current line in editor
            debugger.unmarkCurrent ();
            try {
                int ln = getLineNumber ();
                if (ln >=0) {
                    Line l = getLine ();
                    if (l != null) {
                        debugger.markCurrent (l);
                        return true;
                    }
                }
            } catch (DebuggerException e) {
            }

            // set cursor otherwise
            CallStackFrame[] stack = getCallStack ();
            int i, k = stack.length;
            for (i = 1; i < k; i++) {
                try {
                    if ( Utils.showInEditor (
                                stack [i].getLine ()
                            ) != null
                       ) return true;
                } catch (DebuggerException ee) {
                }
            }
        }
        return true;
    }

    /**
    * Stops this thread.
    *
    * @throw DebuggerException if some problem occurs.
    */
    public abstract void stop () throws DebuggerException;

    /**
    * If this thread is suspended returns line object representing position
    * in the editor where this thread is stopped. If thread has no stack returns null.
    *
    * @throws DebuggerException if informations about source are not included or some other error
    *   occurres.
    * @return line object representing position where this thread is stopped.
    */
    public org.openide.text.Line getLine () throws DebuggerException {
        try {
            String sn = getSourceName ();
            if (sn == null) return null;
            return Utils.getLineForSource (
                       getClassName (),
                       getSourceName (),
                       getLineNumber ()
                   );
        } catch (DebuggerException e) {
            Line l = Utils.getLine (
                         getClassName (),
                         getLineNumber ()
                     );
            if (l != null) return l;
            throw e;
        }
    }

    /**
    * If this thread is suspended returns current call stack.
    *
    * @return current call stack.
    */
    public abstract CallStackFrame[] getCallStack ();

    /**
    * If this thread is suspended returns current local variables.
    *
    * @return current local variables.
    */
    public abstract AbstractVariable[] getLocales ();

    /**
    * Returns name of file of this frame or null if thread has no frame.
    *
    * @return Returns name of file of this frame.
    * @throws DebuggerException if informations about source are not included or some other error
    *   occurres.
    */
    public abstract String getSourceName () throws DebuggerException;

    /**
    * Adds property change listener for this thread. All property changes are fired
    * together in one PropertyChangeEvent with name of property == null.
    *
    * @param l listener to be added.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Removes property change listener from this thread.
    *
    * @param l listener to be removed.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    protected void firePropertyChange (String s, Object o, Object n) {
        pcs.firePropertyChange (s, o, n);
    }

    public String toString () {
        try {
            return "The Thread: " + getName () + " (" + super.toString () + ")"; // NOI18N
        } catch (Exception e) {
            return super.toString ();
        }
    }
}

/*
* Log
*  4    Gandalf-post-FCS1.2.3.0     3/28/00  Daniel Prusa    
*  3    Gandalf   1.2         1/14/00  Daniel Prusa    NOI18N
*  2    Gandalf   1.1         1/13/00  Daniel Prusa    NOI18N
*  1    Gandalf   1.0         11/8/99  Jan Jancura     
* $
*/
