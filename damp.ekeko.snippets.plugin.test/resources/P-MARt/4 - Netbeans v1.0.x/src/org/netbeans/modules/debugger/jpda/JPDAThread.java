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

import java.beans.*;
import java.util.*;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Method;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.AbsentInformationException;

import javax.swing.SwingUtilities;

import org.openide.debugger.DebuggerException;
import org.openide.text.Line;
import org.openide.util.RequestProcessor.Task;

import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.util.Utils;
import org.netbeans.modules.debugger.support.util.Protector;

/**
* JPDA Implementation of debugger thread.
*/
public class JPDAThread extends AbstractThread {

    // variables .......................................................................

    private JPDADebugger                debugger;
    private ThreadReference             thread;
    private boolean                     current = false;
    /** Cache for old locales value. */
    private HashMap                     oldLocales = new HashMap ();
    /** Cache for old call stack. */
    private JPDACallStackFrame []       oldCallStack = new JPDACallStackFrame [0];

    // init ............................................................................

    JPDAThread (
        JPDADebugger debugger,
        JPDAThreadGroup parentThreadGroup,
        ThreadReference thread
    ) {
        super (debugger, parentThreadGroup);
        this.thread = thread;
        this.debugger = debugger;
    }


    // implementation of DebuggerThread ................................................

    /**
    * Getter for the name of thread property.
    *
    * @throw DebuggerException if some problem occurs.
    * @return name of thread.
    */
    public String getName () throws DebuggerException {
        Task t = Protector.register ("JPDAThread.getName"); // NOI18N
        try {
            return thread.name ();
        } catch (Exception e) {
            return "Thread"; // NOI18N
        } finally {
            t.cancel ();
        }
    }

    /**
    * If this thread is suspended returns line number where this thread is stopped.
    *
    * @throw DebuggerException if some problem occurs.
    * @return line number where this thread is stopped.
    */
    public int getLineNumber () throws DebuggerException {
        Task t = Protector.register ("JPDAThread.getLineNumber"); // NOI18N
        try {
            if (thread.frameCount () < 1)
                return -1;
            return thread.frame (0).location ().lineNumber ();
        } catch (Exception e) {
            return -1;
        } finally {
            t.cancel ();
        }
    }

    /**
    * Returns string representation of the current state of this thread (depends on
    * debugger implementation).
    *
    * @throw DebuggerException if some problem occurs.
    * @return string representation of the current state of this thread.
    */
    public String getState () throws DebuggerException {
        Task t = Protector.register ("JPDAThread.getState"); // NOI18N
        try {
            int s = thread.status ();
            switch (s) {
            case ThreadReference.THREAD_STATUS_UNKNOWN: // [PENDING] - localization!!!
                return "Unknown"; // NOI18N
            case ThreadReference.THREAD_STATUS_ZOMBIE:
                return "Zombie"; // NOI18N
            case ThreadReference.THREAD_STATUS_RUNNING:
                return "Running"; // NOI18N
            case ThreadReference.THREAD_STATUS_SLEEPING:
                return "Sleeping"; // NOI18N
            case ThreadReference.THREAD_STATUS_MONITOR:
                return "Monitor"; // NOI18N
            case ThreadReference.THREAD_STATUS_WAIT:
                return "Wait"; // NOI18N
            case ThreadReference.THREAD_STATUS_NOT_STARTED:
                return "Not started"; // NOI18N
            }
            return "Unknown"; // NOI18N
        } catch (Exception e) {
            return "Undefined"; // NOI18N
        } finally {
            t.cancel ();
        }
    }

    /**
    * If this thread is suspended returns class name where this thread is stopped.
    *
    * @throw DebuggerException if some problem occurs.
    * @return class name where this thread is stopped.
    */
    public String getClassName () throws DebuggerException {
        Task t = Protector.register ("JPDAThread.getClassName"); // NOI18N
        try {
            if (thread.frameCount () < 1)
                return ""; // NOI18N
            return thread.frame (0).location ().declaringType ().name ();
        } catch (Exception e) {
            return ""; // NOI18N
        } finally {
            t.cancel ();
        }
    }

    /**
    * If this thread is suspended returns method name where this thread is stopped.
    *
    * @throw DebuggerException if some problem occurs.
    * @return method name where this thread is stopped.
    */
    public String getMethod () throws DebuggerException {
        Task t = Protector.register ("JPDAThread.getMethod"); // NOI18N
        try {
            if (thread.frameCount () < 1)
                return ""; // NOI18N
            Method m = thread.frame (0).location ().method ();
            if (m == null)
                return ""; // NOI18N
            return m.name ();
        } catch (Exception e) {
            return ""; // NOI18N
        } finally {
            t.cancel ();
        }
    }

    /**
    * Returns current stack depth.
    *
    * @throw DebuggerException if some problem occurs.
    * @return current stack depth.
    */
    public int getStackDepth () throws DebuggerException {
        Task t = Protector.register ("JPDAThread.getStackDepth"); // NOI18N
        try {
            return thread.frameCount ();
        } catch (Exception e) {
            return 0;
        } finally {
            t.cancel ();
        }
    }

    /**
    * Returns true if this thread is suspended.
    *
    * @throw DebuggerException if some problem occurs.
    * @return true if this thread is suspended.
    */
    public boolean isSuspended () throws DebuggerException {
        Task t = Protector.register ("JPDAThread.isSuspended"); // NOI18N
        try {
            return thread.isSuspended ();
        } catch (Exception e) {
            return false;
        } finally {
            t.cancel ();
        }
    }

    /**
    * Setter method for the suspend property.
    *
    * @throw DebuggerException if some problem occurs.
    * @param suspend true if this thread might be suspend.
    */
    public void setSuspended (final boolean suspend) throws DebuggerException {
        Task t = Protector.register ("JPDAThread.setSuspended"); // NOI18N
        try {
            if (thread.status () != ThreadReference.THREAD_STATUS_ZOMBIE) {
                if (suspend) {
                    if (!isSuspended ()) {
                        thread.suspend ();
                        // firePropertyChange (null, null, null);
                    }
                } else
                    if (isSuspended ()) {
                        debugger.removeStepRequest ();
                        thread.resume ();
                        // firePropertyChange (null, null, null);
                    }
                super.setSuspended (suspend);
            }
        } catch (Exception e) {
        } finally {
            t.cancel ();
        }
    }

    /**
    * Stops this thread.
    *
    * @throw DebuggerException if some problem occurs.
    */
    public void stop () throws DebuggerException {
        Task t = Protector.register ("JPDAThread.stop"); // NOI18N
        try {
            thread.stop (null);
            firePropertyChange (null, null, null);
        } catch (Exception e) {
        } finally {
            t.cancel ();
        }
    }

    /**
    * If this thread is suspended returns current call stack.
    *
    * @return current call stack.
    */
    public CallStackFrame[] getCallStack () {
        Task t = Protector.register ("JPDAThread.getCallStack"); // NOI18N
        try {
            List l = thread.frames ();
            JPDACallStackFrame[] sfs = new JPDACallStackFrame [l.size ()];
            int i, k = Math.min (sfs.length, oldCallStack.length);
            for (i = 0; i < k; i++) {
                sfs [sfs.length-i-1] = new JPDACallStackFrame (debugger, (StackFrame) l.get (sfs.length-i-1));
                sfs [sfs.length-i-1].oldLocales = oldCallStack [oldCallStack.length-i-1].oldLocales;
            }
            for (i = k; i < sfs.length; i++)
                sfs [sfs.length-i-1] = new JPDACallStackFrame (debugger, (StackFrame) l.get (sfs.length-i-1));
            oldCallStack = sfs;
            return sfs;
        } catch (Exception e) {
            oldCallStack = new JPDACallStackFrame [0];
            return oldCallStack;
        } finally {
            t.cancel ();
        }
    }

    /**
    * If this thread is suspended returns current local variables.
    *
    * @return current local variables.
    */
    public AbstractVariable[] getLocales () {
        Task t = Protector.register ("JPDAThread.getLocales"); // NOI18N
        try {
            if (thread.frameCount () < 1)
                return new AbstractVariable [0];
            StackFrame stackFrame = thread.frame (0);
            HashMap newLocales = new HashMap ();
            List l = stackFrame.visibleVariables ();

            int i, k = l.size ();
            JPDAVariable[] variables = new JPDAVariable [k];
            for (i = 0; i < k; i++) {
                LocalVariable lv = (LocalVariable) l.get (i);
                variables [i] = (JPDAVariable) oldLocales.get (lv.name ());
                if (variables [i] == null)
                    variables [i] = new JPDAVariable (
                                        debugger,
                                        lv.name (),
                                        stackFrame.getValue (lv),
                                        lv.typeName (),
                                        stackFrame
                                    );
                else {
                    variables [i].update (
                        lv.name (),
                        stackFrame.getValue (lv),
                        lv.typeName (),
                        stackFrame
                    );
                    variables [i].firePropertyChange ();
                }
                newLocales.put (lv.name (), variables [i]);
            }
            oldLocales = newLocales;
            return variables;
        } catch (Exception e) {
            return new AbstractVariable [0];
        } finally {
            t.cancel ();
        }
    }

    /**
    * Returns name of file of this frame or null if thread has no frame.
    *
    * @return Returns name of file of this frame.
    * @throws DebuggerException if informations about source are not included or some other error
    *   occurres.
    */
    public String getSourceName () throws DebuggerException {
        Task t = Protector.register ("JPDAThread.getSourceName"); // NOI18N
        try {
            if (thread.frameCount () < 1) return null;
            return thread.frame (0).location ().sourceName ();
        } catch (Exception e) {
            throw new DebuggerException (e);
        } finally {
            t.cancel ();
        }
    }

    public ThreadReference getThreadReference () {
        return thread;
    }

    /**
    * Refresh of thread properties - fires changes.
    */
    void refresh () {
        firePropertyChange (null, null, null);
    }

    public String toString () {
        try {
            return "Thread: " + getName () + " (" + super.toString () + ")"; // NOI18N
        } catch (Exception e) {
            return super.toString ();
        }
    }
}

/*
 * Log
 *  12   Gandalf-post-FCS1.10.3.0    3/28/00  Daniel Prusa    
 *  11   Gandalf   1.10        1/13/00  Daniel Prusa    NOI18N
 *  10   Gandalf   1.9         12/10/99 Jan Jancura     Deadlock protection for 
 *       JPDA
 *  9    Gandalf   1.8         11/29/99 Jan Jancura     
 *  8    Gandalf   1.7         11/8/99  Jan Jancura     Somma classes renamed
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/28/99  Jan Jancura     
 *  5    Gandalf   1.4         9/15/99  Jan Jancura     
 *  4    Gandalf   1.3         9/9/99   Jan Jancura     catching exceptions & 
 *       locales repaired
 *  3    Gandalf   1.2         9/2/99   Jan Jancura     
 *  2    Gandalf   1.1         8/17/99  Jan Jancura     Actions for session 
 *       added & Thread group current property
 *  1    Gandalf   1.0         7/13/99  Jan Jancura     
 * $
 */
