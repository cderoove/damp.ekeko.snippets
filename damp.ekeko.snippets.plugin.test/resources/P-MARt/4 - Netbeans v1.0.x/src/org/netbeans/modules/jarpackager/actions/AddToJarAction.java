/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager.actions;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

import org.openide.util.actions.NodeAction;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;

import org.netbeans.modules.jarpackager.*;
import org.netbeans.modules.jarpackager.util.JarUtils;

/**
*
* @author Dafe Simonek
*/
public final class AddToJarAction extends NodeAction {

    /** Add selected nodes to actual jar content and
    * open and select packaging view.
    */
    protected void performAction (Node[] activatedNodes) {
        PackagingView pv = PackagingView.getPackagingView();
        JarContent jc = pv.getJarContent();
        // now add to roots and set new jar content
        JarUtils.addFileList(jc, activatedNodes);
        pv.setJarContent(jc);
        pv.open();
        pv.requestFocus();
    }

    /** Enables this action only if selected activated nodes
    * can all be added to the jar */
    protected boolean enable (Node[] activatedNodes) {
        return JarUtils.canAdd(
                   PackagingView.getPackagingView().getJarContent(),
                   activatedNodes,
                   null
               );
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle(AddToJarAction.class).
               getString ("CTL_AddToJar");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(AddToJarAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/jarpackager/resources/addToJar.gif"; // NOI18N
    }


}

/*
* <<Log>>
*  7    Gandalf   1.6         1/16/00  David Simonek   i18n
*  6    Gandalf   1.5         12/7/99  David Simonek   
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         10/4/99  David Simonek   
*  3    Gandalf   1.2         9/8/99   David Simonek   new version of jar 
*       packager
*  2    Gandalf   1.1         8/17/99  David Simonek   installations of actions,
*       icon changing
*  1    Gandalf   1.0         8/1/99   David Simonek   
* $
*/