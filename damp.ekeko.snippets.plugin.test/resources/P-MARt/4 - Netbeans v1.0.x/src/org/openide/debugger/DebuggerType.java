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

import java.util.Enumeration;

import org.openide.TopManager;
import org.openide.execution.ExecInfo;
import org.openide.ServiceType;
import org.openide.util.HelpCtx;

/** Defines one debugger type. It has method start that accepts ExecInfo and should
* probably create new DebuggerInfo and call Debugger.startDebug (debuggerInfo).
* The type should be serializable, so it can be attached to file attributes
* of any object that wishes to be especially debugged.
*
* <p>This class <em>currently</em> has a property editor in the IDE's
* default editor search path.
* 
* @author Jaroslav Tulach
*/
public abstract class DebuggerType extends ServiceType {
    static final long serialVersionUID =-3659300496270314301L;
    /** Should start the debugging of this type.
    * @param info class and parameters to run
    * @param stopOnMain should the debugging stop on main method or go to first breakpoint
    * @exception DebuggerException if debugger is not installed or cannot be started
    */
    public abstract void startDebugger (ExecInfo info, boolean stopOnMain) throws DebuggerException;

    public HelpCtx getHelpCtx () {
        return new HelpCtx (DebuggerType.class);
    }

    /** Get all registered executors in the system's execution engine.
    * @return enumeration of <code>DebuggerType</code>s
    */
    public static Enumeration debuggerTypes () {
        return TopManager.getDefault ().getServices ().services (DebuggerType.class);
    }

    /** Find the
    * debugger implemented as a given class, among the executors registered to the
    * execution engine.
    * <P>
    * This should be used during (de-)serialization
    * of the specific debugger for a data object: only store its class name
    * and then try to find the debugger implemented by that class later.
    *
    * @param clazz the class of the debugger looked for
    * @return the desired debugger or <code>null</code> if it does not exist
    */
    public static DebuggerType find (Class clazz) {
        ServiceType t = TopManager.getDefault ().getServices ().find (clazz);
        if (t instanceof DebuggerType) {
            return (DebuggerType)t;
        } else {
            return null;
        }
    }

    /** Find the
    * debugger with requested name, among the executors registered to the
    * execution engine.
    * <P>
    * This should be used during (de-)serialization
    * of the specific debugger for a data object: only store its name
    * and then try to find the debugger later.
    *
    * @param name (display) name of debugger to find
    * @return the desired debugger or <code>null</code> if it does not exist
    */
    public static DebuggerType find (String name) {
        ServiceType t = TopManager.getDefault ().getServices ().find (name);
        if (t instanceof DebuggerType) {
            return (DebuggerType)t;
        } else {
            return null;
        }
    }

    /** Get the default debugger for the system's execution engine.
    * <p>You may actually want {@link org.openide.loaders.ExecSupport#getExecutor}.
    * @return the default debugger
    */
    public static DebuggerType getDefault () {
        Enumeration en = debuggerTypes ();
        if (en.hasMoreElements ()) {
            return (DebuggerType)en.nextElement ();
        } else {
            return new Default ();
        }
    }

    /** Default debugger type. */
    public static class Default extends DebuggerType {
        static final long serialVersionUID =6286540187114472027L;

        /* Gets the display name for this debugger type. */
        public String displayName() {
            return org.openide.util.NbBundle.getBundle(
                       Default.class
                   ).getString("LAB_DefaultDebuggerType");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (Default.class);
        }

        /* Starts the debugger. */
        public void startDebugger(ExecInfo info, boolean stopOnMain) throws DebuggerException {
            if (stopOnMain)
                TopManager.getDefault().getDebugger().startDebugger(new DebuggerInfo(
                            info.getClassName(), info.getArguments()));
            else
                TopManager.getDefault().getDebugger().startDebugger(new DebuggerInfo(
                            info.getClassName(), info.getArguments(), null));
        }
    } // end of inner class DefaultDebuggerType

}

/*
* Log
*  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         9/10/99  Jaroslav Tulach Changes in services APIs.
*  6    Gandalf   1.5         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  5    Gandalf   1.4         7/2/99   Jesse Glick     Help IDs for debugger & 
*       executor types.
*  4    Gandalf   1.3         6/30/99  Jesse Glick     [JavaDoc]
*  3    Gandalf   1.2         6/28/99  Jaroslav Tulach Debugger types are like 
*       Executors
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         4/21/99  Jaroslav Tulach 
* $
*/
