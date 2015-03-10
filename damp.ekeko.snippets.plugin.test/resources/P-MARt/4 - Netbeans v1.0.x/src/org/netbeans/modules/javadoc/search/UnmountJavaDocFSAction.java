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

package org.netbeans.modules.javadoc.search;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileSystem;

/** Unmount FS action.
*
* @author   Petr Hrebejk
*/
public class UnmountJavaDocFSAction extends NodeAction {

    static final long serialVersionUID =-60357217713366217L;
    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(UnmountJavaDocFSAction.class).getString("CTL_UnmountFS");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (UnmountJavaDocFSAction.class);
    }

    /** Resource string to action's icon.
    * @return resource string of the icon
    */
    protected String iconResource() {
        //return "/org/netbeans/core/resources/actions/unmountFS.gif"; // NOI18N
        return null;
    }

    /**
    * Standart perform action extended by actually activated nodes.
    * @see CallableSystemAction#performAction
    *
    * @param activatedNodes gives array of actually activated nodes.
    */
    public void performAction (Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            JavaDocFSNode jdn = (JavaDocFSNode)activatedNodes[i].getCookie ( JavaDocFSNode.class );
            FileSystem fs = jdn.getFileSystem();
            if (fs != null) {
                TopManager.getDefault().getRepository().removeFileSystem( fs );
            }
        }
    }

    /** Should the action be enabled for the set of nodes.
     *@return true if the action should be enbled
     */
    public boolean enable (Node[] activatedNodes) {
        return true;
    }

}


/*
 * Log
 *  4    Gandalf   1.3         1/13/00  Petr Hrebejk    i18n mk3  
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         8/13/99  Petr Hrebejk    
 * $
 */
