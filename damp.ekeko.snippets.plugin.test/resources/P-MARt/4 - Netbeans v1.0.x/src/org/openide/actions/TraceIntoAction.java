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

package org.openide.actions;

import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerTask;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.DebuggerCookie;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node;

/** Trace into a method in the debugger.
* Starts the debugger if needed.
*
* @see Debugger#traceInto
* @author   Jan Jancura
*/
public class TraceIntoAction extends GoAction {

    static final long serialVersionUID = -2094716396729169502L;

    /** Initializes and keeps DebuggerPerformer */
    private DebuggerPerformer debuggerPerformer = DebuggerPerformer.getDefault ();

    private boolean enabled = true;

    /* This performer starts the debugger (if isn't started yet),
    * or calls the traceInto method of debugger in the other case.
    *
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        try {
            debuggerPerformer.setDebuggerRunning (true);
            int state = TopManager.getDefault ().getDebugger ().getState ();
            if (state == Debugger.DEBUGGER_NOT_RUNNING)
                // start in different thread
                debuggerPerformer.new StartDebugThread (activatedNodes, true).start ();
            else
                if (state == Debugger.DEBUGGER_STOPPED)
                    try {
                        TopManager.getDefault ().getDebugger ().traceInto ();
                    } catch (org.openide.debugger.DebuggerException e) {
                        debuggerPerformer.notifyDebuggerException (e);
                    }
        } catch (DebuggerNotFoundException e) {
        }
    }

    /* Enables Trace into action when only one data object which supports
    * debugging (isDebuggingAllowed () == true) is selected.
    *
    * @param activatedNodes Currently activated nodes.
    */
    protected boolean enable (final Node[] activatedNodes) {
        if (!enabled) return false;
        try {
            int state = TopManager.getDefault ().getDebugger ().getState ();
            if (state != Debugger.DEBUGGER_NOT_RUNNING) return state == Debugger.DEBUGGER_STOPPED;
            if ((activatedNodes == null) || (activatedNodes.length != 1)) return false;
            return null != activatedNodes[0].getCookie(DebuggerCookie.class);
        } catch (DebuggerNotFoundException e) {
            return false;
        }
    }

    /** Set whether the debugger action is enabled in general.
    * @param e <code>true</code> if so
    */
    public void changeEnabled (boolean e) {
        enabled = e;
        setEnabled (enable (getActivatedNodes ()));
    }

    /* @return the action's icon */
    public String getName () {
        return NbBundle.getBundle(TraceIntoAction.class).getString("TraceInto");
    }

    /* @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (TraceIntoAction.class);
    }

    /* @return the action's icon */
    protected String iconResource () {
        return "/org/openide/resources/actions/traceInto.gif"; // NOI18N
    }

}

/*
 * Log
 *  24   Gandalf   1.23        1/19/00  Daniel Prusa    bugfix for 
 *       enable/disable
 *  23   Gandalf   1.22        1/13/00  Ian Formanek    I18N
 *  22   Gandalf   1.21        1/12/00  Ian Formanek    NOI18N
 *  21   Gandalf   1.20        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  20   Gandalf   1.19        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  19   Gandalf   1.18        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  18   Gandalf   1.17        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  17   Gandalf   1.16        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  16   Gandalf   1.15        5/2/99   Ian Formanek    Fixed last change
 *  15   Gandalf   1.14        5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  14   Gandalf   1.13        4/16/99  Libor Martinek  
 *  13   Gandalf   1.12        3/26/99  Jesse Glick     [JavaDoc]
 *  12   Gandalf   1.11        3/10/99  Jan Jancura     
 *  11   Gandalf   1.10        3/9/99   Jan Jancura     Debugger actions updated
 *  10   Gandalf   1.9         3/4/99   Jan Jancura     impl dependencies 
 *       removed
 *  9    Gandalf   1.8         3/2/99   David Simonek   icons repair
 *  8    Gandalf   1.7         2/26/99  Jaroslav Tulach To compile after Open 
 *       API changes in debugger
 *  7    Gandalf   1.6         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  6    Gandalf   1.5         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  5    Gandalf   1.4         1/20/99  Jaroslav Tulach 
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
 *  3    Gandalf   1.2         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
