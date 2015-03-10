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

package org.netbeans.modules.jini;

import org.openide.nodes.Node;
import org.openide.util.actions.CookieAction;
import org.openide.util.HelpCtx;

/**
 *
 */
public class RefreshAction extends CookieAction {


    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            RefreshCookie rc = (RefreshCookie) activatedNodes[i].getCookie(RefreshCookie.class);
            if (rc != null) rc.refresh();
        }
    }


    protected Class[] cookieClasses() {
        return new Class[] { RefreshCookie.class };
    }

    protected int mode() {
        return MODE_ALL;
    }

    public String getName() {
        return Util.getString("PROP_Refresh_Action_Name");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ServiceTemplateAction.class);
    }

}


/*
* <<Log>>
*  3    Gandalf   1.2         2/2/00   Petr Kuzel      Jini module upon 1.1alpha
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         6/11/99  Martin Ryzl     
* $ 
*/ 

