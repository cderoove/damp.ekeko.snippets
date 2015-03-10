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
import org.openide.util.enum.ArrayEnumeration;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node;
import org.openide.TopManager;


/** Open an Explorer window with a particular root node.
* Final only for better performance.
* @see TopManager.NodeOperation#explore
* @author   Ian Formanek
*/
public final class OpenLocalExplorerAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2703311250025273778L;

    protected void performAction (Node[] activatedNodes) {
        TopManager.getDefault().getNodeOperation ().explore(activatedNodes[0]);
    }

    protected boolean enable (Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1) ||
                (activatedNodes[0].isLeaf()))
            return false;
        return true;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("OpenLocalExplorer");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (OpenLocalExplorerAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/openLocalExplorer.gif"; // NOI18N
    }
}

/*
 * Log
 *  13   src-jtulach1.12        1/12/00  Ian Formanek    NOI18N
 *  12   src-jtulach1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   src-jtulach1.10        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  10   src-jtulach1.9         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  9    src-jtulach1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    src-jtulach1.7         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  7    src-jtulach1.6         5/2/99   Ian Formanek    Fixed last change
 *  6    src-jtulach1.5         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  5    src-jtulach1.4         3/26/99  Jesse Glick     [JavaDoc]
 *  4    src-jtulach1.3         3/26/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/3/99   David Simonek   mainly icons, little 
 *       changes
 *  2    src-jtulach1.1         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  1    src-jtulach1.0         2/8/99   Petr Hamernik   
 * $
 */
