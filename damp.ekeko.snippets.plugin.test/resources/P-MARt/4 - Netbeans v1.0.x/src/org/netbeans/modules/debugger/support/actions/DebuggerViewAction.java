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

package org.netbeans.modules.debugger.support.actions;

import java.awt.Toolkit;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Iterator;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.debugger.DebuggerNotFoundException;

import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.DebuggerModule;


/** DebuggerView action.
*
* @author   Jan Jancura
*/
public class DebuggerViewAction extends CallableSystemAction {

    /** generated Serialized Version UID */
    static final long               serialVersionUID = 1391479985940417455L;


    // action implementation ...............................................................

    /** Default constructor, initializes debugger mode */
    public DebuggerViewAction () {
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle (DebuggerViewAction.class).getString ("CTL_Debugger_view");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (DebuggerViewAction.class);

    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/debuggerView.gif"; // NOI18N
    }

    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction () {
        showDebuggerView (
            TopManager.getDefault ().getWindowManager ().getCurrentWorkspace ()
        );
    }

    // main methods ........................................................................

    /**
    * Opens debugger window.
    */
    public void showDebuggerView (Workspace workspace) {
        Iterator i = DebuggerModule.getViews ().iterator ();
        while (i.hasNext ()) {
            TopComponent c = (TopComponent) i.next ();
            if (!c.isOpened (workspace))
                c.open (workspace);
            c.requestFocus ();
        }
    }

}

/*
 * Log
 *  14   Gandalf-post-FCS1.12.3.0    3/28/00  Daniel Prusa    
 *  13   Gandalf   1.12        1/13/00  Daniel Prusa    NOI18N
 *  12   Gandalf   1.11        11/8/99  Jan Jancura     Somma classes renamed
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         8/9/99   Jan Jancura     Functionality of modes 
 *       moved to Module
 *  9    Gandalf   1.8         7/29/99  David Simonek   changes concerning 
 *       window system
 *  8    Gandalf   1.7         7/29/99  David Simonek   opening on all 
 *       workspaces together in debugger window
 *  7    Gandalf   1.6         7/22/99  David Simonek   workspace initialization
 *  6    Gandalf   1.5         7/21/99  Jan Jancura     
 *  5    Gandalf   1.4         7/13/99  Jan Jancura     
 *  4    Gandalf   1.3         7/2/99   Jan Jancura     Session debugging 
 *       support
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/4/99   Jan Jancura     
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    changes (position, serialization)
 */
