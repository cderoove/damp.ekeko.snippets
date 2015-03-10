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

import java.beans.*;
import java.util.*;
import javax.swing.SwingUtilities;

import sun.tools.debug.RemoteThread;
import sun.tools.debug.RemoteStackFrame;
import sun.tools.debug.RemoteStackVariable;

import org.openide.debugger.DebuggerException;
import org.openide.text.Line;

import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.util.Protector;
import org.netbeans.modules.debugger.support.util.Utils;


public class ToolsThread extends AbstractThread {

    // variables .......................................................................

    /** This debugger. */
    private ToolsDebugger               debugger;
    /** Representing thread. */
    private RemoteThread                thread;
    /** Cache for old locales value. */
    private HashMap                     oldLocales = new HashMap ();
    /** Cache for old call stack. */
    private ToolsCallStackFrame []      oldCallStack = new ToolsCallStackFrame [0];

    // last stack depth
    transient private int               lastStackDepth = 0;
    // last action performed on the thread
    transient private int               lastAction = AbstractDebugger.ACTION_START;

    // init ............................................................................

    ToolsThread (
        ToolsDebugger debugger,
        ToolsThreadGroup parentThreadGroup,
        RemoteThread thread
    ) {
        super (debugger, parentThreadGroup);
        this.debugger = debugger;
        this.thread = thread;
        if ( (parentThreadGroup != null) &&
                (debugger.lastCurrentThread != null) &&
                thread.equals (debugger.lastCurrentThread)
           ) {
            setCurrent (true);
            debugger.lastCurrentThread = null;
        }
    }


    // implementation of AbstractThread .....................................................

    /**
    * Getter for the name of thread property.
    *
    * @throw DebuggerException if some problem occurs.
    * @return name of thread.
    */
    public String getName () throws DebuggerException {
        if (debugger.synchronizer == null) return "Thread"; // NOI18N
        try {
            return (String) new Protector ("ToolsThread.getName") { // NOI18N
                       public Object protect () throws Exception {
                           return thread.getName ();
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            throw new DebuggerException (e);
        }
    }

    /**
    * If this thread is suspended returns line number where this thread is stopped.
    *
    * @throw DebuggerException if some problem occurs.
    * @return line number where this thread is stopped.
    */
    public int getLineNumber () throws DebuggerException {
        if (debugger.synchronizer == null) return -1;
        try {
            return ((Integer) new Protector ("ToolsThread.getLineNumber") { // NOI18N
                        public Object protect () throws Exception {
                            return new Integer (thread.getCurrentFrame ().getLineNumber ());
                        }
                    }.throwAndWait (debugger.synchronizer, debugger.killer)).intValue ();
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            return -1;
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
        if (debugger.synchronizer == null) return ""; // NOI18N
        try {
            return (String) new Protector ("ToolsThread.getState") { // NOI18N
                       public Object protect () throws Exception {
                           return thread.getStatus ();
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            throw new DebuggerException (e);
        }
    }

    /**
    * If this thread is suspended returns class name where this thread is stopped.
    *
    * @throw DebuggerException if some problem occurs.
    * @return class name where this thread is stopped.
    */
    public String getClassName () throws DebuggerException {
        if (debugger.synchronizer == null) return ""; // NOI18N
        try {
            return (String) new Protector ("ToolsThread.getClassName") { // NOI18N
                       public Object protect () throws Exception {
                           return thread.getCurrentFrame ().getRemoteClass ().getName ();
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            return ""; // NOI18N
        }
    }

    /**
    * If this thread is suspended returns method name where this thread is stopped.
    *
    * @throw DebuggerException if some problem occurs.
    * @return method name where this thread is stopped.
    */
    public String getMethod () throws DebuggerException {
        if (debugger.synchronizer == null) return ""; // NOI18N
        try {
            return (String) new Protector ("ToolsThread.getClassName") { // NOI18N
                       public Object protect () throws Exception {
                           return thread.getCurrentFrame ().getMethodName ();
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            return ""; // NOI18N
        }
    }

    /**
    * Returns current stack depth.
    *
    * @throw DebuggerException if some problem occurs.
    * @return current stack depth.
    */
    public int getStackDepth () throws DebuggerException {
        if (debugger.synchronizer == null) return -1;
        try {
            return ((Integer) new Protector ("ToolsThread.getStackDepth") { // NOI18N
                        public Object protect () throws Exception {
                            return new Integer (thread.dumpStack ().length);
                        }
                    }.throwAndWait (debugger.synchronizer, debugger.killer)).intValue ();
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            return -1;
        }
    }

    /**
    * Returns true if this thread is suspended.
    *
    * @throw DebuggerException if some problem occurs.
    * @return true if this thread is suspended.
    */
    public boolean isSuspended () throws DebuggerException {
        if (debugger.synchronizer == null) return false;
        try {
            return ((Boolean) new Protector ("ToolsThread.isSuspended") { // NOI18N
                        public Object protect () throws Exception {
                            return new Boolean (thread.isSuspended ());
                        }
                    }.throwAndWait (debugger.synchronizer, debugger.killer)).booleanValue ();
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            throw new DebuggerException (e);
        }
    }

    /**
    * Setter method for the suspend property.
    *
    * @throw DebuggerException if some problem occurs.
    * @param suspend true if this thread might be suspend.
    */
    public void setSuspended (final boolean suspend) throws DebuggerException {
        if (debugger.synchronizer == null) return;
        final boolean o = isSuspendedIn ();
        if (o == suspend) return;
        try {
            if (suspend)
                suspendIn ();
            else {
                resumeIn ();
                contIn ();
            }
            if (o != isSuspendedIn ())
                super.setSuspended (suspend);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            throw new DebuggerException (e);
        }
    }

    /**
    * Stops this thread.
    *
    * @throw DebuggerException if some problem occurs.
    */
    public void stop () throws DebuggerException {
        if (debugger.synchronizer == null) return;
        try {
            new Protector ("ToolsThread.setSuspend") { // NOI18N
                public Object protect () throws Exception {
                    thread.stop ();
                    return null;
                }
            }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            throw new DebuggerException (e);
        }
    }

    /**
    * If this thread is suspended returns current call stack.
    *
    * @return current call stack.
    */
    public CallStackFrame[] getCallStack () {
        if (debugger.synchronizer == null) return new ToolsCallStackFrame [0];
        RemoteStackFrame[] rStack = null;
        try {
            if (!isSuspended ()) return new ToolsCallStackFrame [0];
            rStack = (RemoteStackFrame[]) new Protector ("ToolsThread.getCallStack") { // NOI18N
                         public Object protect () throws Exception {
                             return thread.dumpStack ();
                         }
                     }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            return new ToolsCallStackFrame [0];
        }
        int i, k = Math.min (rStack.length, oldCallStack.length);
        ToolsCallStackFrame[] theStack = new ToolsCallStackFrame [rStack.length];
        for (i = 0; i < k; i++) {
            theStack [theStack.length-i-1] = new ToolsCallStackFrame (debugger, rStack [theStack.length-i-1]);
            theStack [theStack.length-i-1].oldLocales = oldCallStack [oldCallStack.length-i-1].oldLocales;
        }
        for (i = k; i < theStack.length; i++)
            theStack [theStack.length -i-1] = new ToolsCallStackFrame (debugger, rStack [theStack.length -i-1]);
        oldCallStack = theStack;
        return theStack;
    }

    /**
    * If this thread is suspended returns current local variables.
    *
    * @return current local variables.
    */
    public AbstractVariable[] getLocales () {
        if (debugger.synchronizer == null) return new AbstractVariable [0];
        try {
            return (AbstractVariable[]) new Protector ("ToolsThread.getLocales") { // NOI18N
                       public Object protect () throws Exception {
                           RemoteStackVariable[] v = thread.getStackVariables ();
                           HashMap newLocales = new HashMap ();
                           ToolsVariable[] rVariables = new ToolsVariable [v.length];

                           int i, k = v.length;
                           for (i = 0; i < k; i++) {
                               rVariables [i] = (ToolsVariable) oldLocales.get (v [i].getName ());
                               if (rVariables [i] == null)
                                   rVariables [i] = new ToolsVariable (
                                                        debugger,
                                                        v [i].getName (),
                                                        v [i].getValue (),
                                                        v [i].getType ().toString ()
                                                    );
                               else {
                                   rVariables [i].update (
                                       v [i].getName (),
                                       v [i].getValue (),
                                       v [i].getType ().toString ()
                                   );
                                   rVariables [i].firePropertyChange ();
                               }
                               newLocales.put (v [i].getName (), rVariables [i]);
                           }
                           oldLocales = newLocales;
                           return rVariables;
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            return new ToolsVariable [0];
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
        if (debugger.synchronizer == null) return ""; // NOI18N
        try {
            return (String) new Protector ("ToolsThread.getSourceName") { // NOI18N
                       public Object protect () throws Exception {
                           return thread.getCurrentFrame ().getRemoteClass ().getSourceFileName ();
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            return ""; // NOI18N
        }
    }


    // other methods ..............................................................

    int getLastStackDepth () {
        return lastStackDepth;
    }

    int getLastAction () {
        return lastAction;
    }

    /** Sets last action performed on the thread and sets lastStackDepth value. */
    void setLastAction (int action) {
        lastAction = action;
        try {
            lastStackDepth = getStackDepth ();
        }
        catch (DebuggerException e) {
            lastStackDepth = 10000; // troubles, what now ...
        }
    }

    /**
    * Returns RemoteThread for this thread.
    */
    RemoteThread getRemoteThread () {
        return thread;
    }

    /**
    * Updates state of this thread.
    */
    void threadChanged () {
        //    updateDebuggerState ();
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            firePropertyChange ();
                                        }
                                    });
    }

    /**
    * Helper method.
    */
    protected void firePropertyChange () {
        super.firePropertyChange (null, null, null);
    }

    /**
    * Equals if RemoteThreads equals.
    */
    public boolean equals (Object o) {
        if (!(o instanceof ToolsThread)) return false;
        return thread.equals (((ToolsThread) o).thread);
    }

    /**
    * Helper method.
    */
    public int hashCode () {
        return thread.hashCode ();
    }


    // unsafe methods ..............................................................

    RemoteStackFrame getCurrentStackIn () throws Exception {
        if (debugger.synchronizer == null) return null;
        return (RemoteStackFrame) new Protector ("ToolsThread.getCurrentStackIn") { // NOI18N
                   public Object protect () throws Exception {
                       return thread.getCurrentFrame ();
                   }
               }.throwAndWait (debugger.synchronizer, debugger.killer);
    }

    boolean isSuspendedIn () {
        if (debugger.synchronizer == null) return false;
        return ((Boolean) new Protector ("ToolsThread.dumpStackIn") { // NOI18N
                    public Object protect () {
                        return new Boolean (thread.isSuspended ());
                    }
                }.wait (debugger.synchronizer, debugger.killer)).booleanValue ();
    }

    RemoteStackFrame[] dumpStackIn () throws Exception {
        if (debugger.synchronizer == null) return null;
        return (RemoteStackFrame[]) new Protector ("ToolsThread.dumpStackIn") { // NOI18N
                   public Object protect () throws Exception {
                       return thread.dumpStack ();
                   }
               }.throwAndWait (debugger.synchronizer, debugger.killer);
    }

    void suspendIn () throws Exception {
        if (debugger.synchronizer == null) return;
        new Protector ("ToolsThread.suspendIn") { // NOI18N
            public Object protect () throws Exception {
                thread.suspend ();
                return null;
            }
        }.throwAndWait (debugger.synchronizer, debugger.killer);
    }

    void resumeIn () throws Exception {
        if (debugger.synchronizer == null) return;
        new Protector ("ToolsThread.resumeIn") { // NOI18N
            public Object protect () throws Exception {
                thread.resume ();
                return null;
            }
        }.throwAndWait (debugger.synchronizer, debugger.killer);
    }

    void contIn () throws Exception {
        if (debugger.synchronizer == null) return;
        new Protector ("ToolsThread.contIn") { // NOI18N
            public Object protect () throws Exception {
                thread.cont ();
                return null;
            }
        }.throwAndWait (debugger.synchronizer, debugger.killer);
    }
}

/*
* Log
*  15   Gandalf-post-FCS1.13.3.0    3/28/00  Daniel Prusa    
*  14   Gandalf   1.13        1/13/00  Daniel Prusa    NOI18N
*  13   Gandalf   1.12        1/10/00  Jan Jancura     Refresh of locales 
*       updated
*  12   Gandalf   1.11        1/6/00   Jan Jancura     Rename from Abstract
*  11   Gandalf   1.10        11/8/99  Jan Jancura     Somma classes renamed
*  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    Gandalf   1.8         10/1/99  Jan Jancura     Current thread & bug 4108
*  8    Gandalf   1.7         9/15/99  Jan Jancura     
*  7    Gandalf   1.6         8/17/99  Jan Jancura     Actions for session added
*       & Thread group current property
*  6    Gandalf   1.5         7/24/99  Jan Jancura     Bug in Suspend / resume 
*       thread in enterprise deb.
*  5    Gandalf   1.4         7/13/99  Jan Jancura     
*  4    Gandalf   1.3         6/9/99   Jan Jancura     
*  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         6/4/99   Jan Jancura     
*  1    Gandalf   1.0         6/1/99   Jan Jancura     
* $
*/
