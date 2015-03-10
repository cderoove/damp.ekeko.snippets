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

import javax.swing.JEditorPane;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.debugger.Debugger;
import org.openide.debugger.Breakpoint;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.TopManager;
import org.openide.cookies.DebuggerCookie;
import org.openide.cookies.EditorCookie;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.text.Line;


/** Go to the cursor.
* @author   Jan Jancura (checked [PENDING HelpCtx])
*/
public class GoToCursorAction extends GoAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1407195105130821880L;

    /** Initializes and keeps DebuggerPerformer */
    private DebuggerPerformer debuggerPerformer = DebuggerPerformer.getDefault ();

    private boolean enabled = true;

    /* @return the action's icon */
    public String getName() {
        return ActionConstants.BUNDLE.getString("GoToCursor");
    }

    /* @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (GoToCursorAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/goToCursor.gif"; // NOI18N
    }

    /** Set whether the debugger action is enabled in general.
    * @param e <code>true</code> if so
    */
    public void changeEnabled (boolean e) {
        enabled = e;
        setEnabled (enable (getActivatedNodes ()));
    }

    /* Enables goToCursor action when only one data object which supports
    * debugging (isDebuggingAllowed () == true) is selected.
    *
    * @param activatedNodes Currently activated nodes.
    */
    protected boolean enable (final Node[] activatedNodes) {
        if (!enabled) return false;
        try {
            EditorCookie edCookie;
            if ((activatedNodes == null) || (activatedNodes.length != 1)) return false;
            if ((activatedNodes[0].getCookie(DebuggerCookie.class)==null) ||
                    ((edCookie=(EditorCookie)activatedNodes[0].getCookie(EditorCookie.class))==null))
                return false;

            //EditorCookie edCookie = (EditorCookie)activatedNodes[0].getCookie (EditorCookie.class);
            JEditorPane [] panes = edCookie.getOpenedPanes();
            if (panes == null) return false;
            /*
            int index = -1;
            for (int x = 0; x < panes.length; x++)
              if (panes[x].isManagingFocus()) {
                index = x;
                break;
              }
            if (index == -1) return false;
            */

            int state = TopManager.getDefault ().getDebugger ().getState ();
            if (state != Debugger.DEBUGGER_NOT_RUNNING) return state == Debugger.DEBUGGER_STOPPED;
            return true;
        } catch (DebuggerNotFoundException e) {
            return false;
        }
    }

    /* This performer starts the debugger (if isn't started yet),
    * or calls the goToCursor method of debugger in the other case.
    *
    * @param activatedNodes Currently activated nodes.
    */
    protected void performAction (final Node[] activatedNodes) {
        EditorCookie edCookie = (EditorCookie)activatedNodes[0].getCookie (EditorCookie.class);
        JEditorPane [] panes = edCookie.getOpenedPanes();
        int index = -1;
        if (panes == null) return;
        for (int x = 0; x < panes.length; x++)
            if (panes[x].isManagingFocus()) {
                index = x;
                break;
            }
        if (index == -1) return;
        //.getCaret ().getDot ()
        int l = NbDocument.findLineNumber (
                    edCookie.getDocument (),
                    panes[index].getCaret ().getDot ()
                );
        Line ll = edCookie.getLineSet ().getCurrent (l);
        if (ll == null) return;

        try {
            final Debugger debugger = TopManager.getDefault ().getDebugger ();
            final Breakpoint b = debugger.createBreakpoint (ll, true);
            PropertyChangeListener pcl;
            debugger.addPropertyChangeListener (pcl = new PropertyChangeListener () {
                                                    public void propertyChange (PropertyChangeEvent ev) {
                                                        if (ev.getPropertyName ().equals (debugger.PROP_STATE)) {
                                                            if ((((Integer)ev.getNewValue ()).intValue () == debugger.DEBUGGER_STOPPED) ||
                                                                    (((Integer)ev.getNewValue ()).intValue () == debugger.DEBUGGER_NOT_RUNNING)) {
                                                                b.remove();
                                                                debugger.removePropertyChangeListener(this);
                                                            }
                                                        }
                                                    }
                                                });
            debuggerPerformer.setDebuggerRunning (true);
            int state = debugger.getState ();
            if (state == Debugger.DEBUGGER_NOT_RUNNING) {
                // start in different thread
                DebuggerPerformer.StartDebugThread dt = debuggerPerformer.new StartDebugThread (activatedNodes, false);
                dt.storeGoToCursorInfo (pcl, b);
                dt.start ();
            }
            else
                if (state == Debugger.DEBUGGER_STOPPED)
                    try {
                        TopManager.getDefault ().getDebugger ().go ();
                    } catch (org.openide.debugger.DebuggerException e) {
                        debuggerPerformer.notifyDebuggerException (e);
                    }
        } catch (DebuggerNotFoundException e) {
        }
    }

}

/*
 * Log
 *  16   Gandalf   1.15        1/13/00  Ian Formanek    I18N
 *  15   Gandalf   1.14        1/12/00  Ian Formanek    NOI18N
 *  14   Gandalf   1.13        1/10/00  Daniel Prusa    changeEnabled method 
 *       added
 *  13   Gandalf   1.12        12/30/99 Daniel Prusa    GoToCursorAction 
 *       implemented
 *  12   Gandalf   1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  10   Gandalf   1.9         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         5/27/99  Jesse Glick     [JavaDoc]
 *  7    Gandalf   1.6         5/26/99  Ian Formanek    Actions cleanup
 *  6    Gandalf   1.5         5/2/99   Ian Formanek    Fixed last change
 *  5    Gandalf   1.4         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  4    Gandalf   1.3         3/26/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
