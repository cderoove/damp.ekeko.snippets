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

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.nodes.Index;

/** Reorder items in a list with a dialog.
* @see Index
*
* @author   Petr Hamernik, Dafe Simonek
*/
public class ReorderAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2388678563650229890L;

    /* Constructs new reorder action */
    public ReorderAction() {
        super();
    }

    /* Returns false - action should be disabled when a window with no
    * activated nodes is selected.
    *
    * @return false do not survive the change of focus
    */
    protected boolean surviveFocusChange () {
        return false;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(ReorderAction.class).getString("Reorder");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (ReorderAction.class);
    }

    /* Icon of this action.
    * @return Name of the action icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /* Creates a set of classes that are tested by the cookie.
    *
    * @return list of classes this cookie tests
    */
    protected Class[] cookieClasses () {
        return new Class[] { Index.class };
    }

    /* Overrides abstract method from CookieAction.
    * @return returns the mode of this action.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    protected void performAction (Node[] activatedNodes) {
        Node n = activatedNodes[0]; // we supposed that one node is activated
        Index order = (Index)n.getCookie(Index.class);
        if (order != null)
            order.reorder();
    }

}

/*
 * Log
 *  14   src-jtulach1.13        1/12/00  Ian Formanek    NOI18N
 *  13   src-jtulach1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   src-jtulach1.11        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  11   src-jtulach1.10        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  10   src-jtulach1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    src-jtulach1.8         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  8    src-jtulach1.7         5/2/99   Ian Formanek    Fixed last change
 *  7    src-jtulach1.6         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  6    src-jtulach1.5         3/26/99  Jesse Glick     [JavaDoc]
 *  5    src-jtulach1.4         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  4    src-jtulach1.3         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  3    src-jtulach1.2         1/20/99  Jaroslav Tulach 
 *  2    src-jtulach1.1         1/7/99   David Simonek   
 *  1    src-jtulach1.0         1/7/99   David Simonek   
 * $
 */
