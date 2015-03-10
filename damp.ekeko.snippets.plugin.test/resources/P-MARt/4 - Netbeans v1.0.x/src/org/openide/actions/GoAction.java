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
* Continue debugging.
* @see Debugger#startDebugger
* @see Debugger#go
* @see DebuggerCookie
*
* @author   Jan Jancura
*/
public class GoAction extends NodeAction {


    // static ..........................................................................................

    static final long serialVersionUID = 3403920608369616104L;


    // variables ..........................................................................................

    /** Initializes and keeps DebuggerPerformer */
    private DebuggerPerformer debuggerPerformer = DebuggerPerformer.getDefault ();

    private boolean enabled = true;


    // other methods......................................................................................

    /** Set whether the debugger action is enabled in general.
    * @param e <code>true</code> if so
    */
    public void changeEnabled (boolean e) {
        enabled = e;
        setEnabled (enable (getActivatedNodes ()));
    }

    /* @return the action's icon */
    public String getName() {
        return NbBundle.getBundle(GoAction.class).getString("Go");
    }

    /* @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (GoAction.class);
    }

    /* @return the action's icon */
    protected String iconResource() {
        return "/org/openide/resources/actions/go.gif"; // NOI18N
    }


    /* This performer starts the debugger (if isn't started yet),
    * or calls the go method of debugger in the other case.
    *
    * @param activatedNodes Currently activated nodes.
    */
    protected void performAction (final Node[] activatedNodes) {
        try {
            int state = TopManager.getDefault ().getDebugger ().getState ();
            if (state == Debugger.DEBUGGER_STOPPED)
                debuggerPerformer.setDebuggerRunning (true);
            try {
                TopManager.getDefault ().getDebugger ().go ();
            } catch (org.openide.debugger.DebuggerException e) {
                debuggerPerformer.notifyDebuggerException (e);
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
        if (!enabled) return false;
        try {
            int state = TopManager.getDefault ().getDebugger ().getState ();
            return state == Debugger.DEBUGGER_STOPPED;
        } catch (DebuggerNotFoundException e) {
            return false;
        }
    }

}

/*
 * Log
 *  31   Gandalf   1.30        1/19/00  Daniel Prusa    bugfix for 
 *       enable/disable
 *  30   Gandalf   1.29        1/18/00  Daniel Prusa    StartDebugger action
 *  29   Gandalf   1.28        1/13/00  Ian Formanek    I18N
 *  28   Gandalf   1.27        1/12/00  Ian Formanek    NOI18N
 *  27   Gandalf   1.26        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  26   Gandalf   1.25        7/11/99  David Simonek   window system change...
 *  25   Gandalf   1.24        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  24   Gandalf   1.23        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  23   Gandalf   1.22        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  22   Gandalf   1.21        6/7/99   Ian Formanek    Removed unused imports
 *  21   Gandalf   1.20        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  20   Gandalf   1.19        5/15/99  Jesse Glick     [JavaDoc]
 *  19   Gandalf   1.18        5/14/99  Ales Novak      bugfix for #1667 #1598 
 *       #1625
 *  18   Gandalf   1.17        5/2/99   Ian Formanek    Fixed last change
 *  17   Gandalf   1.16        5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  16   Gandalf   1.15        3/26/99  Jesse Glick     [JavaDoc]
 *  15   Gandalf   1.14        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  14   Gandalf   1.13        3/16/99  Jan Jancura     
 *  13   Gandalf   1.12        3/10/99  Jan Jancura     
 *  12   Gandalf   1.11        3/9/99   Jan Jancura     Debugger actions updated
 *  11   Gandalf   1.10        3/4/99   Jan Jancura     impl dependence 
 *       removed...
 *  10   Gandalf   1.9         3/2/99   David Simonek   icons repair
 *  9    Gandalf   1.8         2/26/99  Jaroslav Tulach To compile after Open 
 *       API changes in debugger
 *  8    Gandalf   1.7         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  7    Gandalf   1.6         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  6    Gandalf   1.5         1/20/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         1/7/99   Ian Formanek    fixed resource names
 *  4    Gandalf   1.3         1/6/99   David Simonek   java 2 modifications
 *  3    Gandalf   1.2         1/6/99   Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
