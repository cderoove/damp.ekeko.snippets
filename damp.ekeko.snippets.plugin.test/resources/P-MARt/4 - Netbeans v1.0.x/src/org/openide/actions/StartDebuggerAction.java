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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dialog;
import java.beans.PropertyVetoException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.openide.loaders.DataObject;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.TopManager;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.DebuggerCookie;
import org.openide.windows.Workspace;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node;

/**
* Start the debugger.
* @see Debugger#startDebugger
* @see Debugger#go
* @see DebuggerCookie
*
* @author   Daniel Prusa
*/
public class StartDebuggerAction extends NodeAction {


    // static ..........................................................................................

    static final long serialVersionUID = 3565920123469616122L;


    // variables ..........................................................................................

    /** Initializes and keeps DebuggerPerformer */
    private DebuggerPerformer debuggerPerformer = DebuggerPerformer.getDefault ();

    private static boolean enabled = true;

    private boolean multisession = false;

    /** Should be a DO compiled before debugging? */
    private static boolean runCompilation = true;
    /** Workspace */
    private static String workspace = "Debugging"; // NOI18N


    // other methods......................................................................................

    /** Set whether the debugger action is enabled in general.
    * @param e <code>true</code> if so
    */
    public void changeEnabled (boolean e) {
        enabled = e;
        setEnabled (enable (getActivatedNodes ()));
    }

    /** Getter for local enabled flag.
    * @return true if debugging is enabled
    */
    boolean getEnabledFlag() {
        return enabled;
    }

    /* @return the action's name */
    public String getName() {
        return NbBundle.getBundle(StartDebuggerAction.class).getString("StartDebugger");
    }

    /* @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (StartDebuggerAction.class);
    }

    /* @return the action's icon */
    protected String iconResource() {
        return "/org/openide/resources/actions/startDebugger.gif"; // NOI18N
    }

    public void setMultisession (boolean b) {
        multisession = b;
    }

    /** Getter for multisession.
    * @return true if multisession debugger
    */
    boolean getMultisession() {
        return multisession;
    }

    /* This performer starts the debugger
    *
    * @param activatedNodes Currently activated nodes.
    */
    protected void performAction (final Node[] activatedNodes) {
        try {
            int state = TopManager.getDefault ().getDebugger ().getState ();
            if (multisession || (state == Debugger.DEBUGGER_NOT_RUNNING)) {
                debuggerPerformer.setDebuggerRunning (true);
                // start in different thread
                debuggerPerformer.new StartDebugThread (activatedNodes, false).start ();
            }
        } catch (DebuggerNotFoundException e) {
        }
    }

    /* Enables go action when only one data object which supports
    * debugging (isDebuggingAllowed () == true) is selected.
    *
    * @param activatedNodes Currently activated nodes.
    */
    protected boolean enable (final Node[] activatedNodes) {
        if ((!enabled) && (!multisession)) return false;
        try {
            int state = TopManager.getDefault ().getDebugger ().getState ();
            if ((!multisession)&&(state != Debugger.DEBUGGER_NOT_RUNNING)) return false;
            if ((activatedNodes == null) || (activatedNodes.length != 1)) return false;
            return null != activatedNodes[0].getCookie(DebuggerCookie.class);
        } catch (DebuggerNotFoundException e) {
            return false;
        }
    }

    /** Set whether to run compilation before debugging.
    * @param r <code>true</code> if so
    */
    public static void setRunCompilation(boolean r) {
        runCompilation = r;
    }
    /** Test whether compilation is to be run before debugging.
    * @return <code>true</code> if so
    */
    public static boolean getRunCompilation() {
        return runCompilation;
    }
    /**
    * Get the name of the workspace in which debugging is performed.
    * By default, the "Debugging" workspace.
    * @return the workspace name
    */
    public static String getWorkspace () {
        return workspace;
    }
    /**
    * Set the name of the workspace in which debugging is to be performed.
    * @param workspace the new workspace name
    */
    public static void setWorkspace (String workspace) {
        StartDebuggerAction.workspace = workspace;
    }
}

/*
 * Log
 */
