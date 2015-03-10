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
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

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
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class MethodBreakpoint extends MethodBreakpointEvent implements StopEvent {

    static final long serialVersionUID =4718302661899335262L;

    /** Class the breakpoint is set on. */
    private transient RemoteClass         remoteClass;
    /** Class the breakpoint is set on. */
    private transient RemoteField         remoteField;
    /** Thread which stops on theis breakpoint. */
    private transient RemoteThread        thread;
    /** Curent method line. */
    private transient Line                line;


    // Event impl ......................................................................................

    /**
    * Returns the new instance of Breakpoint.Event.
    */
    public CoreBreakpoint.Event getNewInstance () {
        return new MethodBreakpoint ();
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
                            // in outerclass
                            remoteClass = debugger.remoteDebugger.findClass (getClassName ());
                            if (remoteClass == null) return new Boolean (false);
                            remoteField = remoteClass.getMethod (getMethodName ());
                            if (remoteField == null) return new Boolean (false);
                            String s = remoteClass.setBreakpointMethod (remoteField);
                            if (s.trim ().equals ("")) { // NOI18N
                                try {
                                    setMethodLine (remoteClass.getMethodLineNumber (getMethodName ()));
                                } catch (Exception e) {
                                    e.printStackTrace ();
                                }
                                return new Boolean (true);
                            }

                            // in innerclasses
                            String className = getClassName ();
                            String name = Utils.getClassName (className);
                            FileObject mainFile = TopManager.getDefault().getRepository ().find (
                                                      Utils.getPackageName (className),
                                                      name,
                                                      "class" // NOI18N
                                                  );
                            FileObject[] file = mainFile.getParent ().getChildren ();

                            int i, k = file.length;
                            for (i = 0; i < k; i++)
                                if (file [i].getName ().startsWith (name) &&
                                        file [i].getExt ().equals ("class") // NOI18N
                                   ) {
                                    remoteClass = debugger.remoteDebugger.findClass (file [i].getPackageName ('.'));
                                    if (remoteClass == null) return new Boolean (false);
                                    remoteField = remoteClass.getMethod (getMethodName ());
                                    if (remoteField == null) return new Boolean (false);
                                    s = remoteClass.setBreakpointMethod (remoteField);
                                    if (s.trim ().equals ("")) { // NOI18N
                                        try {
                                            setMethodLine (remoteClass.getMethodLineNumber (getMethodName ()));
                                        } catch (Exception e) {
                                            //e.p rintStackTrace ();
                                        }
                                        return new Boolean (true);
                                    }
                                }

                            //          if (s.trim ().equals ("")) return new Boolean (true); // NOI18N
                            debugger.println (NbBundle.getBundle (ToolsDebugger.class).getString (
                                                  "CTL_Cannot_set_breakpoint") + ": " + s, // NOI18N
                                              ToolsDebugger.ERR_OUT
                                             );
                            return new Boolean (false);
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
                    line = null;
                    String s = remoteClass.clearBreakpointMethod (remoteField);
                    remoteClass = null;
                    remoteField = null;
                    if (s.trim ().equals ("")) return null; // NOI18N
                    debugger.println (NbBundle.getBundle (ToolsDebugger.class).getString (
                                          "CTL_Cannot_clear_breakpoint") + ": " + s, // NOI18N
                                      ToolsDebugger.ERR_OUT
                                     );
                    return null;
                }
            }.throwAndWait (debugger.synchronizer, debugger.killer);
        } catch (Exception e) {
        }
    }

    /**
    * Returns line of breakpoint.
    */
    public Line[] getLines () {
        if (line == null) return null;
        return new Line [] {line};
    }

    /**
    * Returns actions available specially for this version of event.
    */
    public CoreBreakpoint.Action[] getBreakpointActions () {
        CoreBreakpoint.Action[] myActions = new CoreBreakpoint.Action[] {
                                                new StopAction (),
                                                new PrintAction (PrintAction.BREAKPOINT_METHOD_TEXT),
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
        try {
            RemoteStackVariable rsv = thread.getCurrentFrame ().getLocalVariable ("this"); // NOI18N
            return new ToolsVariable (
                       (ToolsDebugger) getDebugger (),
                       rsv.getName (),
                       rsv.getValue (),
                       rsv.getType ().toString ()
                   );
        } catch (Exception e) {
            return null;
        }
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
    * Sets current method line.
    */
    private void setMethodLine (int ln) {
        line = Utils.getLine (className, ln);
    }
}

/*
* Log
*  15   Gandalf-post-FCS1.13.4.0    3/28/00  Daniel Prusa    
*  14   Gandalf   1.13        1/14/00  Daniel Prusa    NOI18N
*  13   Gandalf   1.12        1/13/00  Daniel Prusa    Bugfix for 5281
*  12   Gandalf   1.11        1/13/00  Daniel Prusa    NOI18N
*  11   Gandalf   1.10        12/9/99  Daniel Prusa    Default print text
*  10   Gandalf   1.9         11/29/99 Jan Jancura     Bug 3341 - bad \n in 
*       output of debugger
*  9    Gandalf   1.8         11/8/99  Jan Jancura     Somma classes renamed
*  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         10/1/99  Jan Jancura     Current thread & bug 4108
*  6    Gandalf   1.5         9/2/99   Jan Jancura     
*  5    Gandalf   1.4         8/18/99  Jan Jancura     Localization & Current 
*       thread & Current session
*  4    Gandalf   1.3         8/17/99  Jan Jancura     Actions for session added
*       & Thread group current property
*  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  2    Gandalf   1.1         7/21/99  Jan Jancura     
*  1    Gandalf   1.0         7/2/99   Jan Jancura     
* $
*/
