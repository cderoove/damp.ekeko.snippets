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

package org.netbeans.modules.form.actions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.NodeOp;
import org.openide.nodes.Node;
import org.netbeans.modules.form.FormCookie;

/** GotoEditor action - subclass of CookieAction - enabled on nodes which has
* cookie implementing FormCookie
*
* @author   Ian Formanek
*/
public class GotoEditorAction extends CookieAction {
    static final long serialVersionUID =-2390395053230449353L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 2181741222092820534L;

    /** @return the mode of action. Possible values are disjunctions of MODE_XXX
    * constants. */
    protected int mode() {
        return MODE_ALL;
    }

    /** Creates new set of classes that are tested by the cookie.
    *
    * @return list of classes the that the cookie tests
    */
    protected Class[] cookieClasses () {
        return new Class[] { FormCookie.class };
    }

    /**
    * Standard perform action extended by actually activated nodes.
    *
    * @param activatedNodes gives array of actually activated nodes.
    */
    protected void performAction (Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            FormCookie cookie = ((FormCookie) activatedNodes[i].getCookie(FormCookie.class));
            if (cookie != null) {
                cookie.gotoEditor();
                break;
            }
        }
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle (GotoEditorAction.class).getString("ACT_GotoEditor");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(GotoEditorAction.class);
    }

    /** Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/netbeans/modules/form/resources/gotoEditor.gif"; // NOI18N
    }

}

/*
 * Log
 *  9    Gandalf   1.8         1/5/00   Ian Formanek    NOI18N
 *  8    Gandalf   1.7         11/27/99 Patrik Knakal   
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         3/17/99  Ian Formanek    
 *  3    Gandalf   1.2         3/16/99  Ian Formanek    
 *  2    Gandalf   1.1         3/10/99  Ian Formanek    Gandalf updated
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
