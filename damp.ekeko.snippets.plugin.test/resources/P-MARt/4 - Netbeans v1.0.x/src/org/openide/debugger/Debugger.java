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

package org.openide.debugger;

import java.beans.PropertyChangeListener;

import org.openide.text.Line;
import org.openide.src.ConstructorElement;

/**
* Provides a minimal interface between the IDE and a debugger.
* It permits
* control of the state of the debugger and creation of breakpoints and
* watches.
*
* @author   Jan Jancura, Jaroslav Tulach
*/
public abstract class Debugger {
    /** Debugger state when the debugger is not running at all. */
    public static final int DEBUGGER_NOT_RUNNING = 1;
    /** Debugger state when the debugger is starting to run. */
    public static final int DEBUGGER_STARTING = 2;
    /** Debugger state when the debugger is running user code. */
    public static final int DEBUGGER_RUNNING = 3;
    /** Debugger state when the debugger is stopped, e.g.<!-- --> at a breakpoint. */
    public static final int DEBUGGER_STOPPED = 4;

    /** Name of property for the debugger's state. */
    public static final String PROP_STATE = "state"; // NOI18N
    /** Name of property for the set of breakpoints in the system. */
    public static final String PROP_BREAKPOINTS = "breakpoints"; // NOI18N
    /** Name of property for the set of watches in the system. */
    public static final String PROP_WATCHES = "watches"; // NOI18N
    /** Name of property for the debugger's current line. */
    public static final String PROP_CURRENT_LINE = "currentLine"; // NOI18N


    /** Start a new debugging session.
    * The current debugging session, if any, should be stopped first.
    * The provided information specifies the class to start and
    * arguments to pass it, and the name of class to stop debugging in, if applicable.
    *
    * @param info debugger startup info
    * @exception DebuggerException if an error occurs while starting the debugger
    */
    public abstract void startDebugger (DebuggerInfo info) throws DebuggerException;

    /**
    * Finish the debugger session.
    * @throws DebuggerException if there was problem during cleanup
    */
    public abstract void finishDebugger () throws DebuggerException;

    /**
    * Trace into (a statement).
    * @throws DebuggerException if there is a problem during execution
    */
    public abstract void traceInto () throws DebuggerException;

    /**
    * Trace over (a statement).
    * @throws DebuggerException if there is a problem during execution
    */
    public abstract void traceOver () throws DebuggerException;

    /**
    * Go.
    * This should continue executing user code until a breakpoint is hit or the debugger finishes.
    * @throws DebuggerException if there is a problem during execution
    */
    public abstract void go () throws DebuggerException;

    /**
    * Step out (of a statement).
    * @throws DebuggerException if there is a problem during execution
    */
    public abstract void stepOut () throws DebuggerException;

    // BREAKPOINTS

    /** Create a new breakpoint assigned to a specific line.
    * The line is represented by a line object that can change its
    * position as the text is modified.
    *
    * @param l line to create breakpoint at
    * @return the new breakpoint
    */
    public abstract Breakpoint createBreakpoint (Line l);

    /** Create a new breakpoint assigned to a specific line.
    * Allows creation of a hidden breakpoint.
    *
    * @param l line to create breakpoint at
    * @param hidden <code>true</code> if the breakpoint should be hidden from the user
    * @return the new breakpoint
    */
    public abstract Breakpoint createBreakpoint (Line l, boolean hidden);

    /** Find the breakpoint assigned to a given line.
    *
    * @param l line to find the breakpoint at
    * @return the breakpoint or <code>null</code> if there is no such breakpoint
    */
    public abstract Breakpoint findBreakpoint (Line l);

    /** Create a new breakpoint assigned to a method (or constructor).
    * The method is represented by a method (or constructor) source element that
    * must have a declaring class.
    *
    * @param method method or constructor with {@link MemberElement#getDeclaringClass valid} declaring class
    * @return the new breakpoint
    * @exception IllegalArgumentException if the method does not have a declaring class
    */
    public abstract Breakpoint createBreakpoint (ConstructorElement method);

    /** Create a new breakpoint assigned to a method (or constructor).
    * The method is represented by a method (or constructor) source element that
    * must have a declaring class.
    * Allows creation of a hidden breakpoint.
    *
    * @param method method or constructor with {@link MemberElement#getDeclaringClass valid} declaring class
    * @param hidden <code>true</code> if the breakpoint should be hidden from the user
    * @return the new breakpoint
    * @exception IllegalArgumentException if the method does not have a declaring class
    */
    public abstract Breakpoint createBreakpoint (ConstructorElement method, boolean hidden);

    /** Find the breakpoint assigned to a method (or constructor).
    *
    * @param method method or constructor to find the breakpoint of
    * @return the breakpoint or <code>null</code> if there is no such breakpoint
    * @exception IllegalArgumentException if the method does not have a declaring class
    */
    public abstract Breakpoint findBreakpoint (ConstructorElement method);

    /** Get all breakpoints in the system.
    *
    * @return all breakpoints
    */
    public abstract Breakpoint[] getBreakpoints ();

    /**
    * Remove all breakpoints from the system.
    */
    public abstract void removeAllBreakpoints ();



    // WATCHES

    /** Create new uninitialized watch. The watch is visible (not hidden).
    *
    * @return the new watch
    */
    public abstract Watch createWatch ();

    /** Create a watch with its expression set to an initial value.
    * Also
    * allows creation of a hidden watch (not presented to the user), for example
    * for internal use in the editor to obtain values of variables
    * under the mouse pointer.
    *
    * @param expr expression to watch for (the format is the responsibility of the debugger implementation, but it is typically a variable name)
    * @param hidden <code>true</code> if the watch should be hidden from the user
    * @return the new watch
    */
    public abstract Watch createWatch (String expr, boolean hidden);

    /**
    * Get all watches in the system.
    *
    * @return all watches
    */
    public abstract Watch[] getWatches ();

    /**
    * Remove all watches from the system.
    */
    public abstract void removeAllWatches ();

    // PROPERTIES

    /** Get the state of the debugger.
    *
    * @return {@link #DEBUGGER_NOT_RUNNING}, {@link #DEBUGGER_RUNNING}, {@link #DEBUGGER_STOPPED}, or {@link #DEBUGGER_STARTING}
    */
    public abstract int getState ();

    /** Get the current line of debugger.
    *
    * @return current line
    */
    public abstract Line getCurrentLine ();

    /**
    * Add a property change listener.
    *
    * @param l the listener to add
    */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
    * Remove a property change listener.
    *
    * @param l the listener to remove
    */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
}

/*
 * Log
 *  15   Gandalf   1.14        1/12/00  Ian Formanek    NOI18N
 *  14   Gandalf   1.13        12/30/99 Daniel Prusa    goToCursor () method 
 *       removed
 *  13   Gandalf   1.12        12/21/99 Daniel Prusa    Interfaces changed to 
 *       abstract classes.
 *  12   Gandalf   1.11        11/10/99 Jan Jancura     CurrentLine property 
 *       added.
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         7/13/99  Jesse Glick     Breakpoints should 
 *       handle constructors as well as regular methods.
 *  9    Gandalf   1.8         6/28/99  Jaroslav Tulach Debugger types are like 
 *       Executors
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         4/21/99  Jaroslav Tulach Debugger types.
 *  6    Gandalf   1.5         3/23/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         3/23/99  Jan Jancura     findBreakpoint methods 
 *       aded
 *  4    Gandalf   1.3         3/22/99  Jesse Glick     [JavaDoc] and corrected 
 *       a typo in PROP_BREAKPOINTS.
 *  3    Gandalf   1.2         2/26/99  Jaroslav Tulach Open API
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.20        --/--/98 Jan Formanek    reflecing move of DebuggerCookie to org.openide.cookies
 */
