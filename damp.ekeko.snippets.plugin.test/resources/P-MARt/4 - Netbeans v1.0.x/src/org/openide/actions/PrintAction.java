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
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.cookies.PrintCookie;


/** Print the selected object.
* @see PrintCookie
*
* @author Ales Novak
*/
public class PrintAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 8566654780658436581L;

    /** @return PrintCookie.class */
    protected Class[] cookieClasses() {
        return new Class[] { PrintCookie.class };
    }

    protected void performAction(final Node[] activatedNodes) {
        Thread t = new Thread() {
                       public void run() {
                           for (int i = 0; i < activatedNodes.length; i++) {
                               PrintCookie pc = (PrintCookie)activatedNodes[i].getCookie (PrintCookie.class);
                               if (pc != null) {
                                   pc.print();
                               }
                           }
                       }
                   };
        t.setPriority(Thread.MIN_PRIORITY + 1);
        t.start();
    }

    protected int mode () {
        return MODE_ALL;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("Print");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (PrintAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/print.gif"; // NOI18N
    }
}

/*
 * Log
 *  12   src-jtulach1.11        1/12/00  Ian Formanek    NOI18N
 *  11   src-jtulach1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   src-jtulach1.9         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  9    src-jtulach1.8         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  8    src-jtulach1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    src-jtulach1.6         6/8/99   Ales Novak      # 1683
 *  6    src-jtulach1.5         5/4/99   Ales Novak      new thread started
 *  5    src-jtulach1.4         5/2/99   Ian Formanek    Fixed last change
 *  4    src-jtulach1.3         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  3    src-jtulach1.2         4/9/99   Ian Formanek    Removed debug printlns
 *  2    src-jtulach1.1         3/26/99  Jesse Glick     [JavaDoc]
 *  1    src-jtulach1.0         2/19/99  Ales Novak      
 * $
 */
