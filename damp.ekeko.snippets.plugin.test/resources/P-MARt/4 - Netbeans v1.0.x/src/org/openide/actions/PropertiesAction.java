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
import org.openide.TopManager;
import org.openide.actions.CustomizeAction;
import org.openide.util.enum.ArrayEnumeration;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.awt.*;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;

import javax.swing.JMenuItem;

/** Get properties of a node.
*
* @see TopManager.NodeOperation#showProperties
* @author   Ian Formanek, Jan Jancura
*/
public class PropertiesAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5485687384586248747L;

    /* Actually performs action of showing properties
    * @param activatedNodes Array of activated nodes
    */
    protected void performAction (Node[] activatedNodes) {
        if (activatedNodes.length == 1)
            TopManager.getDefault().getNodeOperation().
            showProperties(activatedNodes[0]);
        else
            TopManager.getDefault().getNodeOperation().
            showProperties(activatedNodes);
    }

    /* Manages enable - disable logic
    * @param activatedNodes Array of activated nodes
    * @return true if action should be enabled, false otherwise
    */
    protected boolean enable (Node[] activatedNodes) {
        return (activatedNodes != null) && (activatedNodes.length > 0);
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a Popup Menu.
    * @return the JMenuItem representation for the Action
    */
    public JMenuItem getPopupPresenter() {
        JInlineMenu mi = new JInlineMenu ();
        CustomizeAction customizeAction =
            (CustomizeAction)SystemAction.get(CustomizeAction.class);
        if (customizeAction.isEnabled ())
            mi.setMenuItems (new JMenuItem [] {
                                 new Actions.MenuItem (customizeAction, false),
                                 new Actions.MenuItem (this, false)
                             });
        else
            mi.setMenuItems (new JMenuItem [] {
                                 new Actions.MenuItem (this, false)
                             });
        return mi;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("Properties");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (PropertiesAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/properties.gif"; // NOI18N
    }
}

/*
 * Log
 *  16   Gandalf   1.15        1/12/00  Ian Formanek    NOI18N
 *  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  13   Gandalf   1.12        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  12   Gandalf   1.11        6/8/99   Ian Formanek    Minor changes
 *  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   Gandalf   1.9         5/2/99   Ian Formanek    Fixed last change
 *  9    Gandalf   1.8         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  8    Gandalf   1.7         3/26/99  Jesse Glick     [JavaDoc]
 *  7    Gandalf   1.6         3/1/99   Jaroslav Tulach Changed actions 
 *       presenters.
 *  6    Gandalf   1.5         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  5    Gandalf   1.4         1/7/99   Ian Formanek    fixed resource names
 *  4    Gandalf   1.3         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  3    Gandalf   1.2         1/6/99   Ales Novak      
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
