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

package org.netbeans.core.windows.nodes;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** NewType for workspace - e.g. new workspace can be created in the treeview under the
* "all workspaces node".
*
* @author Ales Novak
*/
final class WorkspaceNewType extends org.openide.util.datatransfer.NewType {
    /** our WorkspacePoolContext */
    private WorkspacePoolContext parent;

    /**
    * @param parent is a WorkspacePoolContext in use
    */
    WorkspaceNewType(WorkspacePoolContext parent) {
        this.parent = parent;
    }

    /** Human presentable name of the paste type. This should be
    * presented as an item in a menu.
    *
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle (WorkspaceNewType.class).getString("New_Workspace");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (WorkspaceNewType.class);
    }

    /**
    * creates new subnode in parent
    */
    public void create() {
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine (
                                            NbBundle.getBundle (WorkspaceNewType.class).getString("NewWorkspaceLabel"),
                                            NbBundle.getBundle (WorkspaceNewType.class).getString("NewWorkspaceDialog")
                                        );

        String o = NbBundle.getBundle (WorkspaceNewType.class).getString("New_Workspace");
        nd.setInputText(o);

        Object ok = TopManager.getDefault ().notify (nd);

        if (ok == NotifyDescriptor.OK_OPTION) {
            String s = nd.getInputText();
            if (! s.equals("")) o = s; // NOI18N
            parent.newChildWorkspace(o);
        }
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/12/00  Ian Formanek    NOI18N
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         7/12/99  Jesse Glick     Context help.
 *  1    Gandalf   1.0         7/11/99  David Simonek   
 * $
 */
