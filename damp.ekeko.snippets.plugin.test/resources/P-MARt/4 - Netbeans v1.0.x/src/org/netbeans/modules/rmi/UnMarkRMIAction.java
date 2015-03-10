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

/**
 *
 * @author  mryzl
 */

public class UnMarkRMIAction extends CookieAction {

    static final long serialVersionUID =-6756156667859979345L;
    /** Creates new CreateRMIAction. */
    public UnMarkRMIAction() {
    }

    /** Get the cookies that this action requires.
    *
    * @return a list of cookies
    */  
    protected Class[] cookieClasses() {
        return new Class[] { RMIDataObject.class };
    }

    /** Get the mode of the action, i.e.<!-- --> how strict it should be about
    * cookie support.
    * @return the mode of the action. Possible values are disjunctions of the <code>MODE_XXX</code>
    * constants. */  
    protected int mode() {
        return MODE_ALL;
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
            RMIDataObject rdo = (RMIDataObject) activatedNodes[i].getCookie(RMIDataObject.class);
            if (rdo != null) {
                try {
                    RMIDataLoader.markRMI(rdo, false);
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
        return NbBundle.getBundle(UnMarkRMIAction.class).getString("CTL_UNMARK_RMI");
    }

    /** Get a help context for the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(UnMarkRMIAction.class);
    }
}

/*
* <<Log>>
*  4    Gandalf   1.3         11/27/99 Patrik Knakal   
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         10/12/99 Martin Ryzl     Automatic detection of 
*       RMI
*  1    Gandalf   1.0         10/12/99 Martin Ryzl     
* $ 
*/ 
