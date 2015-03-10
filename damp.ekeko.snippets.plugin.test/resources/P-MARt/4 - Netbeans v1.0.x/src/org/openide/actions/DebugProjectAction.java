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
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.TopManager;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.DebuggerCookie;
import org.openide.windows.Workspace;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.ProjectSensitiveAction;
import org.openide.nodes.Node;

/**
* Start the debugger.
* @see Debugger#startDebugger
* @see Debugger#go
* @see DebuggerCookie
*
* @author Martin Ryzl
*/
public class DebugProjectAction extends ProjectSensitiveAction {


    // static ..........................................................................................

    static final long serialVersionUID = 3511110123019236122L;

    // variables ..........................................................................................

    /** Initializes and keeps DebuggerPerformer */
    private DebuggerPerformer debuggerPerformer = DebuggerPerformer.getDefault ();

    // other methods......................................................................................

    /* @return the action's name */
    public String getName() {
        return NbBundle.getBundle(StartDebuggerAction.class).getString("ProjectDebug");
    }

    /* @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (StartDebuggerAction.class);
    }

    /* @return the action's icon */
    protected String iconResource() {
        return "/org/openide/resources/actions/projectDebug.gif"; // NOI18N
    }

    /* This performer starts the debugger
    *
    * @param activatedNodes Currently activated nodes.
    */
    protected void performAction (final Node project) {
        StartDebuggerAction sda = (StartDebuggerAction) StartDebuggerAction.get(StartDebuggerAction.class);
        try {
            int state = TopManager.getDefault ().getDebugger ().getState ();
            if (sda.getMultisession() || (state == Debugger.DEBUGGER_NOT_RUNNING)) {
                debuggerPerformer.setDebuggerRunning (true);
                // start in different thread
                debuggerPerformer.new StartDebugThread (new Node[] { project }, false).start ();
            }
        } catch (DebuggerNotFoundException e) {
        }
    }

    /* Enables go action when only one data object which supports
    * debugging (isDebuggingAllowed () == true) is selected.
    *
    * @param activatedNodes Currently activated nodes.
    */
    protected boolean enable (final Node project) {
        StartDebuggerAction sda = (StartDebuggerAction) StartDebuggerAction.get(StartDebuggerAction.class);
        if (!sda.getEnabledFlag() && !sda.getMultisession()) return false;
        try {
            int state = TopManager.getDefault ().getDebugger ().getState ();
            if (!sda.getMultisession() && (state != Debugger.DEBUGGER_NOT_RUNNING)) return false;
            if ((project != null) && (project.getCookie(DebuggerCookie.class) != null)) return true;
        } catch (DebuggerNotFoundException e) {
            // continue to false
        }
        return false;
    }
}

/*
 * Log
 */
