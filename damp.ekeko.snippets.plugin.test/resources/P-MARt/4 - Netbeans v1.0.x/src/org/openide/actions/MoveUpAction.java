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

import java.io.IOException;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.nodes.Index;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;

/** Move an item up in a list.
* This action is final only for performance reasons.
*
* @see Index
* @author Ian Formanek, Jan Jancura, Dafe Simonek
*/
public final class MoveUpAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8201315242813084212L;
    /** the key to listener to reorder of selected nodes */
    private static final String PROP_ORDER_LISTENER = "sellistener"; // NOI18N
    /** Holds index cookie on which we are listening */
    private Index curIndexCookie;

    /** Initilizes the set of properties.
    */
    protected void initialize () {
        super.initialize();
        // initializes the listener
        OrderingListener sl = new OrderingListener();
        putProperty(PROP_ORDER_LISTENER, sl);
    }

    /** Actually performs the action of moving up
    * in the order.
    * @param activatedNodes The nodes on which to perform the action.
    */
    protected void performAction (Node[] activatedNodes) {
        // we need to check activatedNodes, because there's no
        // guarantee that they not changed between enable() and
        // performAction calls
        Index cookie = getIndexCookie(activatedNodes);
        if (cookie == null) return;
        int nodeIndex = cookie.indexOf(activatedNodes[0]);
        if (nodeIndex > 0) {
            cookie.moveUp(nodeIndex);
        }
    }

    /** Manages enable - disable logic of this action */
    protected boolean enable (Node[] activatedNodes) {
        // remove old listener, if any
        if (curIndexCookie != null) {
            curIndexCookie.removeChangeListener(
                (OrderingListener)getProperty(PROP_ORDER_LISTENER));
            curIndexCookie = null;
        }
        Index cookie = getIndexCookie(activatedNodes);
        if (cookie == null) return false;
        // now start listening to reordering changes
        cookie.addChangeListener(
            (OrderingListener)getProperty(PROP_ORDER_LISTENER));
        curIndexCookie = cookie;
        return cookie.indexOf(activatedNodes[0])>0;
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("MoveUp");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (MoveUpAction.class);
    }

    /** Resource name for the icon.
    * @return resource name
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/moveUp.gif"; // NOI18N
    }

    /** Helper method. Returns index cookie or null, if some
    * conditions weren't satisfied */
    private Index getIndexCookie (Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1))
            return null;
        Node parent = activatedNodes[0].getParentNode();
        if (parent == null) return null;
        return (Index)parent.getCookie(Index.class);
    }

    /** Listens to the ordering changes and enables/disables the
    * action if appropriate */
    private final class OrderingListener implements ChangeListener {
        public void stateChanged (ChangeEvent e) {
            Node[] activatedNodes = getActivatedNodes();
            Index cookie = getIndexCookie(activatedNodes);
            setEnabled((cookie != null) &&
                       (cookie.indexOf(activatedNodes[0])>0));
        }
    }

}

/*
 * Log
 *  14   Gandalf   1.13        1/12/00  Ian Formanek    NOI18N
 *  13   Gandalf   1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  11   Gandalf   1.10        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  8    Gandalf   1.7         5/2/99   Ian Formanek    Fixed last change
 *  7    Gandalf   1.6         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  6    Gandalf   1.5         3/26/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         3/15/99  Ian Formanek    Fixed enabling of 
 *       actions in initialize ()
 *  4    Gandalf   1.3         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  3    Gandalf   1.2         2/16/99  David Simonek   
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
