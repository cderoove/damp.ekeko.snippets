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

import java.util.ResourceBundle;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.debugger.DebuggerException;

import sun.tools.debug.*;

import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.StopEvent;
import org.netbeans.modules.debugger.support.PrintAction;
import org.netbeans.modules.debugger.support.StopAction;
import org.netbeans.modules.debugger.support.MethodBreakpointEvent;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.util.*;

/**
* Implementation of breakpoint on exception.
*
* @author   Daniel Prusa
*/

public class ExceptionBreakpoint extends ExceptionBreakpointEvent implements StopEvent {

    //static final long serialVersionUID =4718302661899335262L;
    static final long serialVersionUID =8888882661899335262L;

    /** Class the breakpoint is set on. */
    private transient RemoteClass         remoteClass;
    /** Class the breakpoint is set on. */
    private transient RemoteField         remoteField;
    /** Thread which stops on this breakpoint. */
    private transient RemoteThread        thread;
    /** Curent method line. */
    private transient Line                line;

    // variables .......................................................................................
    /** thrown exception name */
    public String exceptionName;

    // Event impl ......................................................................................

    /**
    * Returns the new instance of Breakpoint.Event.
    */
    public CoreBreakpoint.Event getNewInstance () {
        return new ExceptionBreakpoint ();
    }

    /**
    * Sets breakpoint with specified properties.
    */
    public boolean set () {
        final ToolsDebugger debugger = (ToolsDebugger) getDebugger ();
        if (debugger.synchronizer == null) return false;
        if (remoteClass != null)
            remove ();
        try {
            return ((Boolean) new Protector ("JavaDebugger.addBreakpoint") { // NOI18N
                        public Object protect () throws Exception {
                            remoteClass = debugger.remoteDebugger.findClass (getClassName ());
                            if (remoteClass == null) return new Boolean (false);
                            try {
                                remoteClass.catchExceptions ();
                            }
                            catch (ClassCastException ex) {
                                return new Boolean (false);
                            }
                            return new Boolean (true);
                        }
                    }.throwAndWait (debugger.synchronizer, debugger.killer)).booleanValue ();
        } catch (Exception e) {
            return false;
        }
    }

    /**
    * Removes breakpoint.
    */
    public void remove () {
        final ToolsDebugger debugger = (ToolsDebugger) getDebugger ();
        if (debugger.synchronizer == null) return;
        try {
            new Protector ("JavaDebugger.addBreakpoint") { // NOI18N
                public Object protect () throws Exception {
                    try {
                        remoteClass.ignoreExceptions ();
                    }
                    catch (ClassCastException ex) {}
                    remoteClass = null;

                    return null;
                }
            }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Exception e) {
        }
    }

    /**
    * Returns actions available specially for this version of event.
    */
    public CoreBreakpoint.Action[] getBreakpointActions () {
        CoreBreakpoint.Action[] myActions = new CoreBreakpoint.Action[] {
                                                new StopAction (),
                                                new ExceptionPrintAction (NbBundle.getBundle (LineBreakpoint.class).getString ("CTL_Default_exception_print_text")),
                                            };
        CoreBreakpoint.Action[] actions = new CoreBreakpoint.Action [super.getBreakpointActions ().length + myActions.length];
        System.arraycopy (super.getBreakpointActions (), 0, actions, 0, super.getBreakpointActions ().length);
        System.arraycopy (myActions, 0, actions, super.getBreakpointActions ().length, myActions.length);
        return actions;
    }

    /**
    * Aditional ifno about debugger state when this event occures.
    * If event do not produce this type of info, null is returned.
    */
    public AbstractThread getThread () {
        //S ystem.out.println ("LineBreakpoint.getThread " + this + " : " + getDebugger ()); // NOI18N
        ToolsDebugger debugger = (ToolsDebugger) getDebugger ();
        ToolsThread tt = ((ToolsThreadGroup) debugger.getThreadGroupRoot ()).getThread (thread);
        if (tt != null) return tt;
        debugger.lastCurrentThread = thread;
        return new ToolsThread ((ToolsDebugger) getDebugger (), null, thread);
    }

    /**
    * Aditional ifno about debugger state when this event occures.
    * If event do not produce this type of info, null is returned.
    */
    public CallStackFrame[] getCallStack () {
        return getThread ().getCallStack ();
    }

    /**
    * Aditional ifno about debugger state when this event occures.
    * If event do not produce this type of info, null is returned.
    */
    public AbstractVariable getVariable () {
        return new ExceptionVariable ();
    }


    // StopEvent impl ......................................................................................

    /**
    * Performs stop action.
    */
    public void stop (boolean stop) {
        ((ToolsDebugger) getDebugger ()).stop (stop, getThread ());
    }


    // other methods ......................................................................................

    void perform (RemoteThread thread) {
        this.thread = thread;
        perform ();
    }

    /**
    * Returns line of breakpoint.
    */
    public Line[] getLines () {
        if (line == null) return null;
        return new Line [] {line};
    }

    // innerclasses ......................................................................................

    class ExceptionPrintAction extends PrintAction {

        /**
        * Creates the new Exception Print action with default text.
        */
        ExceptionPrintAction (String s) {
            super (s);
        }

        /**
        * Returns new initialized instance of Exception Print action.
        */
        protected CoreBreakpoint.Action getNewInstance () {
            return new ExceptionPrintAction (text);
        }

    }

    /**
    * Helper class allowing to obtain exception name, its instance is returned by getVariable ().
    */
    private class ExceptionVariable implements AbstractVariable {

        public String getVariableName() {
            return null;
        }

        public String getAsText() {
            return null;
        }

        public void setAsText(String value) throws DebuggerException {
            return;
        }

        public String getType() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            return;
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            return;
        }

        /**
        * Returns name of exception.
        */
        public String getInnerType() {
            return exceptionName;
        }

        public boolean isObject() {
            return true;
        }

        public boolean isArray() {
            return false;
        }

        public boolean isLeaf() {
            return false;
        }

        public String getModifiers() {
            return null;
        }

        public AbstractVariable[] getFields() {
            return null;
        }

        public void validate() {
            return;
        }

        public boolean canValidate () {
            return false;
        }

        public boolean canRemove () {
            return false;
        }

    }

}

/*
* Log
*  5    Gandalf-post-FCS1.3.4.0     3/28/00  Daniel Prusa    
*  4    Gandalf   1.3         1/14/00  Daniel Prusa    NOI18N
*  3    Gandalf   1.2         1/13/00  Daniel Prusa    Bugfix for 5281
*  2    Gandalf   1.1         1/13/00  Daniel Prusa    NOI18N
*  1    Gandalf   1.0         12/9/99  Daniel Prusa    
* $
*/