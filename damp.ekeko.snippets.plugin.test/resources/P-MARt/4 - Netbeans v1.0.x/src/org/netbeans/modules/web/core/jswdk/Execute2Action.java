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

package org.netbeans.modules.web.core.jswdk;

import java.util.*;

import org.openide.actions.ExecuteAction;
import org.openide.nodes.Node;
import org.openide.loaders.ExecSupport;
import org.openide.loaders.MultiDataObject;
import org.openide.execution.Executor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.core.WebExecSupport;

/** Execute a class.
* Is enabled if the only selected node implements
* {@link ExecCookie}.
* @see org.openide.execution
*
* @author Petr Jiricka
*/
public class Execute2Action extends ExecuteAction {

    /** serialVersionUID */
    private static final long serialVersionUID = -2297027897144460552L;
    /* Checks the cookies and starts them.
    */
    protected void performAction (final Node[] activatedNodes) {
        ServletJspExecutor.forceRestart();
        super.performAction(activatedNodes);
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(Execute2Action.class).getString("CTL_ExecuteRestart");
    }

    /* Help context where to find more about the acion.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (Execute2Action.class);
    }

    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes))
            return false;
        if (activatedNodes.length == 0)
            return false;
        MultiDataObject mdo = (MultiDataObject)activatedNodes[0].getCookie(MultiDataObject.class);
        if (mdo == null)
            return false;
        Executor exec = ExecSupport.getExecutor(mdo.getPrimaryEntry());
        if (exec == null) {
            WebExecSupport wes = (WebExecSupport)mdo.getCookie(WebExecSupport.class);
            if (wes != null)
                exec = wes.defaultExecutor();
        }
        return (exec instanceof ServletJspExecutor);
    }

    /* Has to be just one.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

}

/*
 * Log
 *  4    Gandalf   1.3         2/4/00   Petr Jiricka    Restart the engine if 
 *       the execution parameters change - fixes bugs 5561, 5515, 5581, 5291, 
 *       5587  
 *  3    Gandalf   1.2         1/17/00  Petr Jiricka    WebExecSupport - related
 *       changes.
 *  2    Gandalf   1.1         1/3/00   Petr Jiricka    
 *  1    Gandalf   1.0         12/29/99 Petr Jiricka    
 * $
 */
