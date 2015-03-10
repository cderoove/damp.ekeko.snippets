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
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.JComponent;

import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.LineCookie;
import org.openide.debugger.*;
import org.openide.src.ConstructorElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.EnvironmentNotSupportedException;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.debugger.support.util.*;
//[PENDING]
import org.netbeans.modules.debugger.support.actions.*;

import javax.swing.SwingUtilities;

/**
* Main Corona debugger class
*
* @author   Jan Jancura
* @version  0.47, May 26, 1998
*/
public abstract class AbstractDebugger extends Debugger implements java.io.Serializable {


    // static ........................................................................

    static final long                         serialVersionUID = 2791304615739651906L;

    public static final int                   ERR_OUT = 1;
    public static final int                   STD_OUT = 2;
    public static final int                   STL_OUT = 4;
    public static final int                   ALL_OUT = ERR_OUT + STD_OUT + STL_OUT;

    /** Last action property constants. */
    public static final int                   ACTION_BREAKPOINT_HIT = 1;
    public static final int                   ACTION_TRACE_OVER = 2;
    public static final int                   ACTION_TRACE_INTO = 3;
    public static final int                   ACTION_STEP_OUT = 4;

    public static final int                   ACTION_GO = 5;
    public static final int                   ACTION_START = 6;

    /** Property name constant */
    public static final String                PROP_CURRENT_THREAD = "currentThread"; // NOI18N
    public static final String                PROP_LAST_ACTION = "lastAction"; // NOI18N

    /** bundle to obtain text information from */
    static ResourceBundle                     bundle = org.openide.util.NbBundle.getBundle
            (AbstractDebugger.class);

    /** Settings for debugger */
    protected static final DebuggerSettings   settings = new DebuggerSettings ();


    // variables .................................................................

    /** Support for multisession debugging. */
    private transient boolean                 multisession = false;
    /** Helper calss for updating values of variales */
    private transient Validator               validator;
    // DebuggerListener support */


    /**
     * @associates DebuggerListener 
     */
    private transient Vector                  listener = new Vector (10,20);
    private transient PropertyChangeSupport   pcs;
    /** Current line support */
    private transient Line                    currentLine = null;
    private transient int                     lastAction = ACTION_START;

    /** Output window */
    private transient OutputWriter            output;
    private transient OutputWriter            error;
    protected transient InputOutput           io = null;

    /** output writer Thread */
    private transient Thread                  owThread = null;

    // breakpoints


    /**
     * @associates CoreBreakpoint 
     */
    protected Vector                          breakpoint = new Vector (10, 10);       //[SERIALIZED]


    /**
     * @associates CoreBreakpoint 
     */
    protected Vector                          hiddenBreakpoint = new Vector (10, 10); //[SERIALIZED]

    // watches


    /**
     * @associates AbstractWatch 
     */
    protected Vector                          watch = new Vector (10, 10);           //[SERIALIZED]

    // properties
    private transient int                     debuggerState = DEBUGGER_NOT_RUNNING;
    boolean                                   showMessages = false;                  //[SERIALIZED]
    boolean                                   followedByEditor = true;               //[SERIALIZED]


    // init .......................................................................

    public AbstractDebugger (
        boolean multisession,
        Validator validator
    ) {
        this.multisession = multisession;
        pcs = new PropertyChangeSupport (this);
        if (validator == null)
            this.validator = new Validator ();
        else
            this.validator = validator;
    }

    /**
    * Deserializes debugger.
    */
    protected void setDebugger (AbstractDebugger debugger) {
        // remove all debugger objects.
        removeAllBreakpoints ();
        Vector v = (Vector) hiddenBreakpoint.clone ();
        int i, k = v.size ();
        for (i = k - 1; i >= 0; i--)
            ((CoreBreakpoint) v.elementAt (i)).remove ();
        removeAllWatches ();

        // add a new one
        k = debugger.breakpoint.size ();
        for (i = 0; i < k; i++) {
            CoreBreakpoint b = (CoreBreakpoint) debugger.breakpoint.elementAt (i);
            breakpoint.addElement (b);
            fireBreakpointCreated (b);
        }

        k = debugger.hiddenBreakpoint.size ();
        for (i = 0; i < k; i++) {
            CoreBreakpoint b = (CoreBreakpoint) debugger.hiddenBreakpoint.elementAt (i);
            hiddenBreakpoint.addElement (b);
            fireBreakpointCreated (b);
        }
        k = debugger.watch.size ();
        for (i = 0; i < k; i++) {
            AbstractWatch w = (AbstractWatch) debugger.watch.elementAt (i);
            watch.addElement (w);
            fireWatchCreated (w);
        }
        showMessages = debugger.showMessages;
        followedByEditor = debugger.followedByEditor;

        lastAction = ACTION_START;
    }


    // Debugger implementation ...................................................

    /**
    * Opens output window & changes debugger state to STARTING
    *
    * @param info debugger info about class to start
    * @exception DebuggerException if an error occures during the start of 
    * the debugger
    */
    public void startDebugger (DebuggerInfo info)
    throws DebuggerException {
        if (!multisession) {
            io = TopManager.getDefault ().getIO (bundle.getString ("CTL_Debugger"), false);
            if (io == null)
                io = TopManager.getDefault ().getIO (bundle.getString ("CTL_Debugger"), true);
        } else {
            io = TopManager.getDefault ().getIO (getProcessName (info));
        }
        io.setErrSeparated (true);
        io.setOutputVisible (true);
        io.setErrVisible (true);
        io.setInputVisible (false);
        output = io.getOut ();
        error = io.getErr ();
        io.setFocusTaken (true);
        io.select ();
        try {
            output.reset ();
            error.reset ();
        } catch (java.io.IOException e) {
            throw new DebuggerException (e);
        }
        startDebugger (info, io);
    }

    /**
    * Opens output window & changes debugger state to STARTING
    *
    * @param info debugger info about class to start
    * @exception DebuggerException if an error occures during the start of 
    * the debugger
    */
    public void startDebugger (DebuggerInfo info, InputOutput io) {

        // output window ........................................................
        io.select ();
        println (bundle.getString ("CTL_Debugger_starting"), STL_OUT);
        setDebuggerState (DEBUGGER_STARTING);
        setLastAction (ACTION_START);
        ((CreateVariableAction) SystemAction.get (CreateVariableAction.class)).
        changeEnabled (true);
    }

    /**
    * Destroyes threads, breakpoints, watches and current line. Debugger
    * state is changed to NOT_RUNNING.
    */
    public void finishDebugger () throws DebuggerException {
        // finish all existing threads and groups ...
        //    SwingUtilities.invokeLater (new Runnable () {
        //      public void run () {
        if ((owThread != null) && (owThread.isAlive ()))
            try {
                owThread.stop ();
                owThread = null;
            }
            catch (SecurityException e) {
            }
        AbstractThreadGroup ttg = getThreadGroupRoot ();
        if (ttg != null) ttg.removeAll ();
        // refresh breakpoint, watches and current line
        setDebuggerState (DEBUGGER_NOT_RUNNING);
        ((CreateVariableAction) SystemAction.get (CreateVariableAction.class)).changeEnabled (false);
        setBreakpoints ();
        getValidator ().clear ();
        updateWatches ();
        unmarkCurrent ();
        //      }
        //    });
    }

    /**
    * Trace into support.
    */
    public void traceInto () throws DebuggerException {
        println (bundle.getString ("CTL_Debugger_running"), STL_OUT);
        setDebuggerState (DEBUGGER_RUNNING);
        setLastAction (ACTION_TRACE_INTO);
        unmarkCurrent ();
    }

    /**
    * Trace over support.
    */
    public void traceOver () throws DebuggerException {
        println (bundle.getString ("CTL_Debugger_running"), STL_OUT);
        setDebuggerState (DEBUGGER_RUNNING);
        setLastAction (ACTION_TRACE_OVER);
        unmarkCurrent ();
    }

    /**
    * Go support.
    */
    public void go () throws DebuggerException {
        println (bundle.getString ("CTL_Debugger_running"), STL_OUT);
        setDebuggerState (DEBUGGER_RUNNING);
        setLastAction (ACTION_GO);
        unmarkCurrent ();
    }

    /**
    * Step out support.
    */
    public void stepOut () throws DebuggerException {
        println (bundle.getString ("CTL_Debugger_running"), STL_OUT);
        setDebuggerState (DEBUGGER_RUNNING);
        setLastAction (ACTION_STEP_OUT);
        unmarkCurrent ();
    }


    // BREAKPOINTS .......................................................

    /** Creates new breakpoint.
    *
    * @return new line breakpoint
    */
    public Breakpoint createBreakpoint (boolean hidden) {
        //S ystem.out.println ("JavaD.createBreakpoint "); // NOI18N
        CoreBreakpoint b = new CoreBreakpoint (this, hidden);
        if (hidden)
            hiddenBreakpoint.addElement (b);
        else
            breakpoint.addElement (b);
        fireBreakpointCreated (b);
        return b;
    }

    /** Creates new breakpoint that is assigned to specific line.
    * The line is represented by the Line object that changes its
    * position as the text is modified, etc.
    *
    * @param l line to create breakpoint for
    * @return new line breakpoint
    */
    public Breakpoint createBreakpoint (Line l) {
        CoreBreakpoint b = (CoreBreakpoint) createBreakpoint (false);
        b.setLine (l);
        return b;
    }

    /** Creates new breakpoint that is assigned to specific line.
    * Allows creation of a hidden breakpoint
    *
    * @param l line to create breakpoint for
    * @param hidden <code>true</code> if the breakpoint should be hidden from the user
    * @return new line breakpoint
    */
    public Breakpoint createBreakpoint (Line l, boolean hidden) {
        CoreBreakpoint b = (CoreBreakpoint) createBreakpoint (hidden);
        b.setLine (l);
        return b;
    }

    /** Find an enabled breakpoint assigned to a specific line.
    * The line is represented by a line object that can change its
    * position as the text is modified.
    *
    * @param l line to find breakpoint at
    * @return the breakpoint or null
    */
    public Breakpoint findBreakpoint (Line l) {
        Breakpoint[] b = getHiddenBreakpoints ();
        int i, k = b.length;
        for (i = 0; i < k; i++) {
            if (!b [i].isEnabled ()) continue;
            if ((b [i].getLine () != null) && b [i].getLine ().equals (l)) break;
        }
        if (i != k) {
            return b [i];
        }

        b = getBreakpoints ();
        k = b.length;
        for (i = 0; i < k; i++) {
            if (!b [i].isEnabled ()) continue;
            if ((b [i].getLine () != null) && b [i].getLine ().equals (l)) break;
        }
        if (i == k) return null;
        return b [i];
    }

    /** Creates new breakpoint assigned to a method.
    * The method is represented by method source element that
    * must have declaring class.
    *
    * @param method method with valid declaring class (method.getDeclaringClass () != null)
    * @return new line breakpoint
    * @exception IllegalArgumentException if the method does not have declaring class
    */
    public Breakpoint createBreakpoint (ConstructorElement method) {
        CoreBreakpoint b = (CoreBreakpoint) createBreakpoint (false);
        return b;
    }

    /** Creates new breakpoint assigned to a method.
    * The method is represented by method source element that
    * must have declaring class.
    *
    * @param method method with valid declaring class (method.getDeclaringClass () != null)
    * @return new line breakpoint
    * @exception IllegalArgumentException if the method does not have declaring class
    */
    public Breakpoint createBreakpoint (ConstructorElement method, boolean hidden) {
        CoreBreakpoint b = (CoreBreakpoint) createBreakpoint (hidden);
        return b;
    }

    /** Find an enabled breakpoint assigned to a method.
    * The method is represented by a method source element that
    * must have a declaring class.
    *
    * @param method method with {@link ConstructorElement#getDeclaringClass valid} declaring class
    * @return the breakpoint or null
    * @exception IllegalArgumentException if the method does not have a declaring class
    */
    public Breakpoint findBreakpoint (ConstructorElement method) {
        Breakpoint[] b = getHiddenBreakpoints ();
        int i, k = b.length;
        for (i = 0; i < k; i++) {
            if (!b [i].isEnabled ()) continue;
            if (b [i].getMethod ().equals (method)) break;
        }
        if (i != k) return b [i];

        b = getBreakpoints ();
        k = b.length;
        for (i = 0; i < k; i++) {
            if (!b [i].isEnabled ()) continue;
            if (b [i].getMethod ().equals (method)) break;
        }
        if (i == k) return null;
        return b [i];
    }

    /** Getter for all breakpoints in the system.
    *
    * @return array of all breakpoints.
    */
    public Breakpoint[] getBreakpoints () {
        CoreBreakpoint[] b;
        synchronized (breakpoint) {
            b = new CoreBreakpoint [breakpoint.size ()];
            breakpoint.copyInto (b);
        }
        return b;
    }

    /** Getter for all hidden breakpoints in the system.
    *
    * @return array of all breakpoints.
    */
    public Breakpoint[] getHiddenBreakpoints () {
        CoreBreakpoint[] b;
        synchronized (hiddenBreakpoint) {
            b = new CoreBreakpoint [hiddenBreakpoint.size ()];
            hiddenBreakpoint.copyInto (b);
        }
        return b;
    }

    /**
    * Removes all non hidden breakpoints.
    */
    public void removeAllBreakpoints () {
        Vector v = (Vector) breakpoint.clone ();
        int i, k = v.size ();
        for (i = k - 1; i >= 0; i--)
            ((CoreBreakpoint) v.elementAt (i)).remove ();
    }


    // WATCHES ..............................................................

    /**
    * Returns array of all watches.
    *
    * @return array of all watches.
    */
    public Watch[] getWatches () {
        AbstractWatch[] w;
        synchronized (watch) {
            w = new AbstractWatch [watch.size ()];
            watch.copyInto (w);
        }
        return w;
    }

    /**
    * Removes all watches.
    */
    public void removeAllWatches () {
        Vector v = (Vector) watch.clone ();
        int i, k = v.size ();
        for (i = k - 1; i >= 0; i--)
            removeWatch ((AbstractWatch) v.elementAt (i));
    }


    // PROPERTIES .........................................

    /**
    * Getter for state of debugger.
    *
    * @return one of DEBUGGER_XXX constants
    */
    public int getState () {
        return debuggerState;
    }

    /** Get the current line of debugger.
    *
    * @return current line
    */
    public Line getCurrentLine () {
        return currentLine;
    }

    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }


    // other methods ..................................................................


    // properties ........................

    /**
    * Getter for last action property.
    *
    * @return one of XXX_ACTION constants
    */
    public int getLastAction () {
        return lastAction;
    }

    /**
    * Sets last action property value.
    */
    protected void setLastAction (int lastAction) {
        int old = this.lastAction;
        this.lastAction = lastAction;
        firePropertyChange (
            PROP_LAST_ACTION,
            new Integer (old),
            new Integer (lastAction)
        );
    }

    /**
    * Sets debugger state.
    */
    public void setSuspended (boolean suspended) {
        getThreadGroupRoot ().setSuspended (suspended);
    }

    /**
    * Setter method for debugger state property.
    *
    * @param newState
    */
    public void setDebuggerState (final int newState) {
        if (newState == debuggerState) return;
        Object old = new Integer (debuggerState);
        debuggerState = newState;
        firePropertyChange (PROP_STATE, old, new Integer (debuggerState));
    }

    /**
    * Returns version of this debugger.
    */
    public abstract String getVersion ();

    /**
    * Returns validator.
    */
    public Validator getValidator () {
        return validator;
    }
    /**
    * Display debugger messages in the debugger output window property.
    *
    * @return true if messages are displayed in the debugger output window.
    */
    public boolean isShowMessages () {
        return showMessages;
    }

    /**
    * Display debugger messages in the debugger output window property.
    *
    * @param b true if messages are displayed in the debugger output window.
    */
    public void setShowMessages (boolean showMessages) {
        this.showMessages = showMessages;
    }

    /**
    * Show current line in the editor.
    *
    * @return true if current line is showen in the editor.
    */
    public boolean isFollowedByEditor () {
        return followedByEditor;
    }

    /**
    * Show current line in the editor.
    *
    * @param b true if current line should be showen in the editor.
    */
    public void setFollowedByEditor (boolean followedByEditor) {
        this.followedByEditor = followedByEditor;
    }

    /**
    * Returns true if multisession mode is supported by this version
    * of debugger implementation.
    */
    public boolean isMultiSession () {
        return false;
    }

    public InputOutput getInputOutput () {
        return io;
    }

    /**
    * Returns size of memory.
    */
    public abstract int getTotalMemory () throws DebuggerException;

    /**
    * Returns size of free memory.
    */
    public abstract int getFreeMemory () throws DebuggerException;

    /**
    * @return newly constructed string containing classpath obtained from filesystems
    */
    public abstract String getClasspath ();

    /**
    * @return Connect Panel for this version of debugger.
    */
    public abstract JComponent getConnectPanel ();

    /**
    * @return name of proces for given DebuggerInfo.
    */
    public abstract String getProcessName (DebuggerInfo info);

    /**
    * @return name of location for given DebuggerInfo.
    */
    public abstract String getLocationName (DebuggerInfo info);

    /**
    * True, if debugger supports evaluation of expressions.
    */
    public abstract boolean supportsExpressions ();

    // breakpoints ........................

    /**
    * Returns events available for this version of debugger.
    */
    public abstract CoreBreakpoint.Event[] getBreakpointEvents ();

    /**
    * Returns breakpoint event with given name.
    */
    public CoreBreakpoint.Event getBreakpointEvent (String name) {
        CoreBreakpoint.Event[] e = getBreakpointEvents ();
        int i, k = e.length;
        for (i = 0; i < k; i++)
            if (e [i].getTypeName ().equals (name))
                return e [i];
        return null;
    }

    /**
    * Returns actions available for this version of debugger.
    */
    public abstract CoreBreakpoint.Action[] getBreakpointActions ();

    /**
    * Find an enabled breakpoint assigned to a specific line number.
    */
    public CoreBreakpoint findBreakpoint (String className, int lineNumber) {
        String cn = Utils.getTopClassName (className);
        CoreBreakpoint[] b = (CoreBreakpoint[]) getHiddenBreakpoints ();
        int i, k = b.length;
        for (i = 0; i < k; i++) {
            if (!b [i].isEnabled ()) continue;
            if ((b [i].getClassName () != null) && (Utils.getTopClassName (b [i].getClassName ()).equals (cn)) &&
                    (b [i].getLineNumber () == lineNumber)
               ) return b [i];
        }
        b = (CoreBreakpoint[]) getBreakpoints ();
        k = b.length;
        for (i = 0; i < k; i++) {
            if (!b [i].isEnabled ()) continue;
            if ((b [i].getClassName () != null) && (Utils.getTopClassName (b [i].getClassName ()).equals (cn)) &&
                    (b [i].getLineNumber () == lineNumber)
               ) return b [i];
        }
        return null;
    }

    /**
    * Find an enabled breakpoint assigned to a specific method.
    */
    public CoreBreakpoint findBreakpoint (String className, String methodName) {
        String cn = Utils.getTopClassName (className);
        CoreBreakpoint[] b = (CoreBreakpoint[]) getHiddenBreakpoints ();
        int i, k = b.length;
        for (i = 0; i < k; i++) {
            if (!b [i].isEnabled ()) continue;
            if ((b [i].getClassName () != null) && (Utils.getTopClassName (b [i].getClassName ()).equals (cn)) &&
                    (b [i].getMethodName () != null) && (b [i].getMethodName ().equals (methodName))
               ) return b [i];
        }
        b = (CoreBreakpoint[]) getBreakpoints ();
        k = b.length;
        for (i = 0; i < k; i++) {
            if (!b [i].isEnabled ()) continue;
            if ((b [i].getClassName () != null) && (Utils.getTopClassName (b [i].getClassName ()).equals (cn)) &&
                    (b [i].getMethodName () != null) && (b [i].getMethodName ().equals (methodName))
               ) return b [i];
        }
        return null;
    }

    /**
    * Finds all enabled breakpoints according to className
    */
    public CoreBreakpoint[] findBreakpoints (String className) {
        String cn = Utils.getTopClassName (className);
        Vector br = new Vector (10, 10);
        CoreBreakpoint[] b = (CoreBreakpoint[]) getHiddenBreakpoints ();
        int i, k = b.length;
        for (i = 0; i < k; i++) {
            if (!b [i].isEnabled ()) continue;
            if ((b [i].getClassName () != null) && (Utils.getTopClassName (b [i].getClassName ()).equals (cn))
               ) br.add (b [i]);
        }
        b = (CoreBreakpoint[]) getBreakpoints ();
        k = b.length;
        for (i = 0; i < k; i++) {
            if (!b [i].isEnabled ()) continue;
            if ((b [i].getClassName () != null) && (Utils.getTopClassName (b [i].getClassName ()).equals (cn))
               ) br.add (b [i]);
        }
        CoreBreakpoint[] res = new CoreBreakpoint [br.size ()];
        br.copyInto (res);
        return res;
    }

    /**
    * Removes breakpoint.
    *
    * @param b breakpoint to be removed
    */
    void removeBreakpoint (CoreBreakpoint b) {
        //    ((CoreBreakpoint)b).clearBreakpoint ();
        hiddenBreakpoint.removeElement (b);
        breakpoint.removeElement (b);
        fireBreakpointRemoved (b);
    }

    /**
    * Tries to set or remove breakpoints. Is called when debugger starts and finishs.
    */
    protected void setBreakpoints () {
        Vector v = (Vector) breakpoint.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((CoreBreakpoint) v.elementAt (i)).setBreakpoint ();
        v = (Vector) hiddenBreakpoint.clone ();
        k = v.size ();
        for (i = 0; i < k; i++)
            ((CoreBreakpoint) v.elementAt (i)).setBreakpoint ();
    }

    protected void changeHidden (CoreBreakpoint b) {
        if (b.isHidden ()) {
            breakpoint.remove (b);
            fireBreakpointRemoved (b);
            hiddenBreakpoint.add (b);
            fireBreakpointCreated (b);
        } else {
            hiddenBreakpoint.remove (b);
            fireBreakpointRemoved (b);
            breakpoint.add (b);
            fireBreakpointCreated (b);
        }
    }


    // threads ........................

    /**
    * Returns root of all threads.
    */
    public abstract AbstractThreadGroup getThreadGroupRoot ();

    /**
    * Returns current thread or null.
    */
    public abstract AbstractThread getCurrentThread ();

    /**
    * Sets current thread. If thread is null, unsets curent thread.
    */
    public abstract void setCurrentThread (AbstractThread thread);


    // watches .................................

    /**
    * Removes watch.
    *
    * @param b watch to be removed
    */
    public void removeWatch (AbstractWatch w) {
        watch.removeElement (w);
        fireWatchRemoved (w);
    }

    /**
    * Update values of watches.
    */
    public void updateWatches () {
        getValidator ().validate ();
        //    int i, k = watch.size ();
        //    for (i = 0; i < k; i++) ((AbstractWatch)watch.elementAt (i)).validate ();
    }


    // sessions .................................

    /**
    * Disconnects from Virtual Machine.
    */
    public void disconnect () throws DebuggerException {
    }

    /**
    * Reconnects to disconnected Virtual Machine.
    */
    public void reconnect () throws DebuggerException {
    }


    // helper methods .................................

    /**
    * Updates state of debugger.
    *
    * @param newState
    */
    /*  public void updateDebuggerState () {
        try {
          AbstractThread currentThread = getCurrentThread ();
          if ((currentThread != null) && currentThread.isSuspended ())
            setDebuggerState (DEBUGGER_STOPPED);
          else 
            setDebuggerState (DEBUGGER_RUNNING);
        } catch (Throwable e) {
          if (e instanceof ThreadDeath) throw (ThreadDeath)e;
        }
      }*/

    /**
    * Unmarks current line.
    */
    public void unmarkCurrent () {
        if (currentLine == null) return;
        currentLine.unmarkCurrentLine ();
        Object old = currentLine;
        currentLine = null;
        firePropertyChange (PROP_CURRENT_LINE, old, currentLine);
    }

    /**
    * Unmarks current line.
    */
    public void markCurrent (Line l) {
        Object old = currentLine;
        Utils.showInEditor (l);
        (currentLine = l).markCurrentLine ();
        firePropertyChange (PROP_CURRENT_LINE, old, currentLine);
    }

    /**
    * Unmarks current line.
    */
    public boolean canBeCurrent (Line l, boolean onBreakpoint) {
        /*return l.canBeMarkedCurrent (
            onBreakpoint ? ACTION_BREAKPOINT_HIT : getLastAction (), 
            currentLine
        );*/
        return true;
    }

    /**
    * Prints given text to the output.
    */
    public void print (final String text, final int where) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            if ((where & STD_OUT) != 0) output.print (text);
                                            if ((where & ERR_OUT) != 0) error.print (text);
                                            if ((where & STL_OUT) != 0) TopManager.getDefault ().setStatusText (text);
                                        }
                                    });
    }

    /**
    * Prints given text to the output.
    */
    public void println (final String text, final int where) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            if ((where & STD_OUT) != 0) output.println (text);
                                            if ((where & ERR_OUT) != 0) error.println (text);
                                            if ((where & STL_OUT) != 0) TopManager.getDefault ().setStatusText (text);
                                        }
                                    });
    }

    /**
    * Shows output and error from this proces in output window.
    */
    public void showOutput (final Process process, int what, final int where) {
        if (process == null) throw new NullPointerException ();
        Thread t = null;
        if ((what | STD_OUT) != 0) {
            t = new Thread (new Runnable () {
                                public void run () {
                                    BufferedReader input = new BufferedReader (
                                                               new InputStreamReader (process.getInputStream ())
                                                           );
                                    String s;
                                    try {
                                        while ((s = input.readLine ()) != null)
                                            println (s, where);
                                    } catch (IOException e) {
                                    }
                                }
                            }, "Debugger input reader thread"); // NOI18N
            t.setPriority (Thread.MIN_PRIORITY);
            t.start ();
        }

        if ((what | ERR_OUT) != 0) {
            t = new Thread (new Runnable () {
                                public void run () {
                                    BufferedReader error = new BufferedReader (
                                                               new InputStreamReader (process.getErrorStream ())
                                                           );
                                    String d;
                                    try {
                                        while ((d = error.readLine ()) != null)
                                            println (d, where);
                                    } catch (IOException e) {
                                    }
                                }
                            }, "Debugger error reader thread"); // NOI18N
            t.setPriority (Thread.MIN_PRIORITY);
            t.start ();
        }
    }

    public void connectInput (final Process process) {
        if (process == null) throw new NullPointerException ();
        if (io == null) return;
        io.setInputVisible (true);
        final Reader input = io.getIn ();
        owThread = new Thread (new Runnable () {
                                   public void run () {
                                       OutputStream out = process.getOutputStream ();
                                       int b;
                                       try {
                                           while ((b = input.read ()) != -1) {
                                               out.write (b);
                                               out.flush ();
                                           }
                                       } catch (IOException e) {
                                       }
                                   }
                               }, "Debugger output writer thread"); // NOI18N
        owThread.setPriority (Thread.MIN_PRIORITY);
        owThread.start ();
    }

    // DebuggerListener support .......................................................

    /**
    * This listener notificates about cahnges of breakpoints, watches and threads.
    *
    * @param l listener object.
    */
    public void addDebuggerListener (DebuggerListener l) {
        listener.addElement (l);
    }

    /**
    * Removes debugger listener.
    *
    * @param l listener object.
    */
    public void removeDebuggerListener (DebuggerListener l) {
        listener.removeElement (l);
    }

    /**
    * Notificates about creating s thread.
    *
    * @param thread
    */
    protected void fireThreadCreated (final AbstractThread thread) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            int i, k = listener.size ();
                                            for (i = 0; i < k; i++)
                                                ((DebuggerListener)listener.elementAt (i)).threadCreated (thread);
                                        }
                                    });
    }

    /**
    * Notificates about removing thread.
    *
    * @param thread
    */
    protected void fireThreadDeath (final AbstractThread thread) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            int i, k = listener.size ();
                                            for (i = 0; i < k; i++)
                                                ((DebuggerListener)listener.elementAt (i)).threadDeath (thread);
                                        }
                                    });
    }

    /**
    * Notificates about creating a breakpoint.
    *
    * @param breakpoint
    */
    protected void fireBreakpointCreated (final CoreBreakpoint breakpoint) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            int i, k = listener.size ();
                                            for (i = 0; i < k; i++)
                                                ((DebuggerListener)listener.elementAt (i)).breakpointAdded (breakpoint);
                                            pcs.firePropertyChange (PROP_BREAKPOINTS, null, null);
                                        }
                                    });
    }

    /**
    * Notificates about removing a breakpoint.
    *
    * @param breakpoint
    */
    protected void fireBreakpointRemoved (final CoreBreakpoint breakpoint) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            int i, k = listener.size ();
                                            for (i = 0; i < k; i++)
                                                ((DebuggerListener)listener.elementAt (i)).breakpointRemoved (breakpoint);
                                            pcs.firePropertyChange (PROP_BREAKPOINTS, null, null);
                                        }
                                    });
    }

    /**
    * Notificates about creating a AbstractWatch.
    *
    * @param AbstractWatch
    */
    protected void fireWatchCreated (final AbstractWatch watch) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            int i, k = listener.size ();
                                            for (i = 0; i < k; i++)
                                                ((DebuggerListener)listener.elementAt (i)).watchAdded (watch);
                                            pcs.firePropertyChange (PROP_WATCHES, null, null);
                                        }
                                    });
    }

    /**
    * Notificates about removing a AbstractWatch.
    *
    * @param AbstractWatch
    */
    protected void fireWatchRemoved (final AbstractWatch watch) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            int i, k = listener.size ();
                                            for (i = 0; i < k; i++)
                                                ((DebuggerListener)listener.elementAt (i)).watchRemoved (watch);
                                            pcs.firePropertyChange (PROP_WATCHES, null, null);
                                        }
                                    });
    }


    // PropertyChangeListener support .......................................................

    /**
    * Fires property change.
    */
    protected void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
    }
}

/*
 * Log
 *  12   Gandalf-post-FCS1.10.4.0    03/28/00 Daniel Prusa    
 *  11   Gandalf   1.10        01/14/00 Daniel Prusa    NOI18N
 *  10   Gandalf   1.9         01/14/00 Daniel Prusa    the order of finding hidden
 *       and non-hidden breakpoints changed
 *  9    Gandalf   1.8         01/13/00 Daniel Prusa    NOI18N
 *  8    Gandalf   1.7         01/04/00 Daniel Prusa    enabling/disabling of
 *       Create fixed watch action
 *  7    Gandalf   1.6         12/30/99 Daniel Prusa    goToCursor () method
 *       removed
 *  6    Gandalf   1.5         12/21/99 Daniel Prusa    Interfaces Debugger, Watch,
 *       Breakpoint changed to abstract classes.
 *  5    Gandalf   1.4         12/09/99 Daniel Prusa    findBreakpoint methods
 *       changed
 *  4    Gandalf   1.3         12/07/99 Daniel Prusa    findBreakpoint (className,
 *       methodName) changed
 *       getLastAtion renamed to getLastAction
 *  3    Gandalf   1.2         11/29/99 Jan Jancura     Bug 3341 - bad \n in output
 *       of debugger
 *       Some implementation moved to AbstractDebugger
 *       Bug 4108 - current line selection in the editor
 *  2    Gandalf   1.1         11/10/99 Jan Jancura     Current line property added
 *  1    Gandalf   1.0         11/08/99 Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.42        --/--/98 Jan Formanek    reflecing move of DebuggerCookie to org.netbeans.ide.cookies
 *  0    Tuborg    0.46        --/--/98 Jaroslav Tulach passing arguments to debugged programs
 *  0    Tuborg    0.47        --/--/98 Jan Formanek    reflecting changes in cookies
 */
