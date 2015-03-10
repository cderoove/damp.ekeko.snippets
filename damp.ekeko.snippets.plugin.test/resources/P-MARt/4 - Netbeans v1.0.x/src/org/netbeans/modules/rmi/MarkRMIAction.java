/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi;

import org.openide.nodes.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import org.netbeans.modules.java.*;

/**
 *
 * @author  mryzl
 */

public class MarkRMIAction extends NodeAction {

    static final long serialVersionUID =7765418462331402194L;
    /** Creates new CreateRMIAction. */
    public MarkRMIAction() {
    }


    /**
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    */
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) return false;

        for(int i = 0; i < activatedNodes.length; i++) {
            Node.Cookie cookie = activatedNodes[i].getCookie(JavaDataObject.class);
            if (cookie == null) return false;
            if (!cookie.getClass().equals(JavaDataObject.class)) return false;
        }
        return true;
    }

    /**
    * Perform the action based on the currently activated nodes.
    * Note that if the source of the event triggering this action was itself
    * a node, that node will be the sole argument to this method, rather
    * than the activated nodes.
    *
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    */
    protected void performAction(Node[] activatedNodes) {
        for(int i = 0; i < activatedNodes.length; i++) {
            JavaDataObject jdo = (JavaDataObject) activatedNodes[i].getCookie(JavaDataObject.class);
            if ((jdo != null) && (jdo.getClass().equals(JavaDataObject.class))) {
                try {
                    RMIDataLoader.markRMI(jdo, true);
                } catch (Exception ex) {
                    org.openide.TopManager.getDefault().notifyException(ex);
                }
            }
        }
    }

    /** Get a human presentable name of the action.
    * This may be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle(MarkRMIAction.class).getString("CTL_MARK_RMI");
    }

    /** Get a help context for the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(MarkRMIAction.class);
    }
}

/*
* <<Log>>
*  5    Gandalf   1.4         1/28/00  Martin Ryzl     now works only with JDO
*  4    Gandalf   1.3         11/27/99 Patrik Knakal   
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         10/12/99 Martin Ryzl     Automatic detection of 
*       RMI
*  1    Gandalf   1.0         10/12/99 Martin Ryzl     
* $ 
*/ 
