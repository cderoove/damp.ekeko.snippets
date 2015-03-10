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
import org.openide.nodes.Node;
import org.netbeans.modules.form.FormCookie;

/** GotoForm action - subclass of CookieAction - enabled on nodes which has
* cookie implementing FormCookie
*
* @author   Ian Formanek
*/
public class GotoFormAction extends CookieAction {
    static final long serialVersionUID =-7607104072546680339L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = -4872957746435487814L;

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
        //System.out.println("GotoFormAction.java:52"); // NOI18N
        for (int i = 0; i < activatedNodes.length; i++) {
            //System.out.println("GotoFormAction.java:54:"+activatedNodes[i]); // NOI18N
            FormCookie cookie = ((FormCookie) activatedNodes[i].getCookie(FormCookie.class));
            if (cookie != null) {
                cookie.gotoForm();
                break;
            }
        }
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle (GotoFormAction.class).getString("ACT_GotoForm");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(GotoFormAction.class);
    }

    /** Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/netbeans/modules/form/resources/gotoForm.gif"; // NOI18N
    }

}

/*
 * Log
 *  12   Gandalf   1.11        1/13/00  Ian Formanek    NOI18N #2
 *  11   Gandalf   1.10        1/5/00   Ian Formanek    NOI18N
 *  10   Gandalf   1.9         11/27/99 Patrik Knakal   
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         5/16/99  Ian Formanek    
 *  5    Gandalf   1.4         5/11/99  Ian Formanek    Build 318 version
 *  4    Gandalf   1.3         3/17/99  Ian Formanek    
 *  3    Gandalf   1.2         3/16/99  Ian Formanek    
 *  2    Gandalf   1.1         3/10/99  Ian Formanek    Gandalf updated
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
