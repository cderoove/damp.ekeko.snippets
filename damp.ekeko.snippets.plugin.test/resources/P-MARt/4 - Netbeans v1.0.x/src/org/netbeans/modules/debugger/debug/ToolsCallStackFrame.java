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

import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.util.Protector;
import org.netbeans.modules.debugger.support.util.Utils;


/**
* Class representing one line of callstack.
*/
public class ToolsCallStackFrame extends CallStackFrame {

    /** Stack frame. */
    private RemoteStackFrame              stackFrame;
    /** Debugger link. */
    private ToolsDebugger                 debugger;
    /** Cache for old locales value. */
    HashMap                               oldLocales = new HashMap ();

    /**
    * Creates a new call stack line.
    *
    * @param lineNumber
    * @param methodName
    * @param className
    */
    ToolsCallStackFrame (
        ToolsDebugger debugger,
        RemoteStackFrame stackFrame
    ) {
        this.debugger = debugger;
        this.stackFrame = stackFrame;
    }

    /**
    * Returns line number of this frame in this callstack.
    *
    * @return Returns line number of this frame in this callstack.
    */
    public int getLineNumber () throws DebuggerException {
        if (debugger.synchronizer == null) return -1;
        try {
            return ((Integer) new Protector ("TheThread.getLineNumber") { // NOI18N
                        public Object protect () throws Exception {
                            return new Integer (stackFrame.getLineNumber ());
                        }
                    }.throwAndWait (debugger.synchronizer, debugger.killer)).intValue ();
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            throw new DebuggerException (e);
        }
    }

    /**
    * Returns method name of this frame in this callstack.
    *
    * @return Returns method name of this frame in this callstack.
    */
    public String getMethodName () throws DebuggerException {
        if (debugger.synchronizer == null) return ""; // NOI18N
        try {
            return (String) new Protector ("TheThread.getMethodName") { // NOI18N
                       public Object protect () throws Exception {
                           return stackFrame.getMethodName ();
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            throw new DebuggerException (e);
        }
    }

    /**
    * Returns class name of this frame in this callstack.
    *
    * @return Returns class name of this frame in this callstack.
    */
    public String getClassName () throws DebuggerException {
        if (debugger.synchronizer == null) return ""; // NOI18N
        try {
            return (String) new Protector ("TheThread.getClassName") { // NOI18N
                       public Object protect () throws Exception {
                           return stackFrame.getRemoteClass ().getName ();
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            throw new DebuggerException (e);
        }
    }

    /**
    * Returns current local variables for this frame stack.
    *
    * @return Current local variables for this frame stack.
    */
    public AbstractVariable[] getLocales () {
        if (debugger.synchronizer == null) return new AbstractVariable [0];
        try {
            return (AbstractVariable[]) new Protector ("TheThread.getClassName") { // NOI18N
                       public Object protect () throws Exception {
                           RemoteStackVariable[] variables = stackFrame.getLocalVariables ();
                           HashMap newLocales = new HashMap ();
                           int i, k = variables.length;

                           ToolsVariable[] rVar = new ToolsVariable [k];
                           for (i = 0; i < k; i++) {
                               rVar [i] = (ToolsVariable) oldLocales.get (variables [i].getName ());
                               if (rVar [i] == null)
                                   rVar [i] = new ToolsVariable (
                                                  debugger,
                                                  variables [i].getName (),
                                                  variables [i].getValue (),
                                                  variables [i].getType ().toString ()
                                              );
                               else {
                                   rVar [i].update (
                                       variables [i].getName (),
                                       variables [i].getValue (),
                                       variables [i].getType ().toString ()
                                   );
                                   rVar [i].firePropertyChange ();
                               }
                               newLocales.put (variables [i].getName (), rVar [i]);
                           }
                           oldLocales = newLocales;
                           return rVar;
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            return new ToolsVariable [0];
        }
    }

    /**
    * Returns name of file of this frame.
    *
    * @return Returns name of file of this frame.
    * @throws DebuggerException if informations about source are not included or some other error
    *   occurres.
    */
    public String getSourceName () throws DebuggerException {
        if (debugger.synchronizer == null) return ""; // NOI18N
        try {
            return (String) new Protector ("TheThread.getSourceName") { // NOI18N
                       public Object protect () throws Exception {
                           return stackFrame.getRemoteClass ().getSourceFileName ();
                       }
                   }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Exception e) {
            throw new DebuggerException (e);
        }
    }
}

/*
* Log
*  9    Gandalf-post-FCS1.7.3.0     3/28/00  Daniel Prusa    
*  8    Gandalf   1.7         1/13/00  Daniel Prusa    NOI18N
*  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
*  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         9/15/99  Jan Jancura     
*  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         6/4/99   Jan Jancura     
*  2    Gandalf   1.1         6/4/99   Jan Jancura     
*  1    Gandalf   1.0         6/1/99   Jan Jancura     
* $
*/



