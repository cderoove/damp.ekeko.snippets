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

import org.netbeans.modules.debugger.support.LineBreakpointEvent;
import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.StopEvent;
import org.netbeans.modules.debugger.support.StopAction;
import org.netbeans.modules.debugger.support.PrintAction;
import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.util.*;

/**
*
* @author   Jan Jancura
*/
public class LineBreakpoint extends LineBreakpointEvent implements StopEvent {

    static final long serialVersionUID =-2076421213106468334L;
    /** Class the breakpoint is set on. */
    private transient RemoteClass         remoteClass;
    /** Thread which stops on theis breakpoint. */
    private transient RemoteThread        thread;
    /** Helper field, stores lineNumber property. */
    private transient int                 remoteLineNumber;


    // Event impl ......................................................................................

    /**
    * Returns the new instance of Breakpoint.Event.
    */
    public CoreBreakpoint.Event getNewInstance () {
        return new LineBreakpoint ();
    }

    /**
    * Sets breakpoint with specified properties.
    */
    public boolean set () {
        final ToolsDebugger debugger = (ToolsDebugger) getDebugger ();
        if (debugger.synchronizer == null) return false;
        if (remoteClass != null)
            remove ();
        final String className = getClassName ();
        final int lineNumber = getLineNumber ();
        if ((className == null) || (className.length () < 1) ||
                (lineNumber < 0)) return false;
        try {
            return ((Boolean) new Protector ("JavaDebugger.addBreakpoint") { // NOI18N
                        public Object protect () throws Exception {
                            // in outerclass
                            remoteClass = debugger.remoteDebugger.findClass (className);
                            if (remoteClass == null) return new Boolean (false);
                            remoteLineNumber = lineNumber;

                            // PATCH for breakpoints in top-level package private classes, try to obtain Line
                            CoreBreakpoint breakpoint = getBreakpoint ();
                            if ((breakpoint.getLine () == null) && (!breakpoint.isHidden ())) {
                                Line newLine = Utils.getLineForSource (className, remoteClass.getSourceFileName (), lineNumber);
                                if (newLine != null)
                                    breakpoint.setLine (newLine);
                            }
                            // end of PATCH

                            String s = remoteClass.setBreakpointLine (remoteLineNumber);
                            if (s.trim ().equals ("")) return new Boolean (true); // NOI18N

                            // in innerclasses
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
                                    s = remoteClass.setBreakpointLine (lineNumber);
                                    if (s.trim ().equals ("")) return new Boolean (true); // NOI18N
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
                    String s = remoteClass.clearBreakpointLine (remoteLineNumber);
                    remoteClass = null;
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
    * Returns actions available specially for this version of event.
    */
    public CoreBreakpoint.Action[] getBreakpointActions () {
        CoreBreakpoint.Action[] myActions = new CoreBreakpoint.Action[] {
                                                new StopAction (),
                                                new ToolsPrintAction (PrintAction.BREAKPOINT_TEXT),
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
}

/*
* Log
*  22   Gandalf-post-FCS1.19.4.1    4/19/00  Daniel Prusa    PATCH for breakpoints in 
*       top-level package private classes
*  21   Gandalf-post-FCS1.19.4.0    3/28/00  Daniel Prusa    
*  20   Gandalf   1.19        1/14/00  Daniel Prusa    NOI18N
*  19   Gandalf   1.18        1/13/00  Daniel Prusa    Bugfix for 5281
*  18   Gandalf   1.17        1/13/00  Daniel Prusa    NOI18N
*  17   Gandalf   1.16        12/10/99 Daniel Prusa    Bug 4881 - order of 
*       actions changed
*  16   Gandalf   1.15        11/29/99 Jan Jancura     Bug 3341 - bad \n in 
*       output of debugger
*  15   Gandalf   1.14        11/8/99  Jan Jancura     Somma classes renamed
*  14   Gandalf   1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  13   Gandalf   1.12        10/1/99  Jan Jancura     Current thread & bug 4108
*  12   Gandalf   1.11        9/2/99   Jan Jancura     
*  11   Gandalf   1.10        8/17/99  Jan Jancura     Actions for session added
*       & Thread group current property
*  10   Gandalf   1.9         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  9    Gandalf   1.8         8/3/99   Jan Jancura     
*  8    Gandalf   1.7         8/2/99   Jan Jancura     
*  7    Gandalf   1.6         7/30/99  Jan Jancura     
*  6    Gandalf   1.5         7/2/99   Jan Jancura     Session debugging support
*  5    Gandalf   1.4         6/10/99  Jan Jancura     
*  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         6/4/99   Jan Jancura     
*  2    Gandalf   1.1         6/4/99   Jan Jancura     
*  1    Gandalf   1.0         6/1/99   Jan Jancura     
* $
*/
