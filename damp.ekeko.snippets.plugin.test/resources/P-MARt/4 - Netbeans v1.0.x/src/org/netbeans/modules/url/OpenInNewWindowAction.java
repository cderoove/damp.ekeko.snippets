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

package org.netbeans.modules.url;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/** OpenInNewWindow action - subclass of CookieAction - enables nodes which has
* cookie implementing URLNodeCookie
*
* @author   Ian Formanek
*/
public class OpenInNewWindowAction extends CookieAction {
    static final long serialVersionUID =4271078855412352632L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = -5847763658433081444L;

    /** @return set of needed cookies */
    protected Class[] cookieClasses () {
        return new Class[] { URLNodeCookie.class };
    }

    /** @return false */
    protected boolean surviveFocusChange () {
        return false;
    }

    /** @return all */
    protected int mode () {
        return MODE_ALL;
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle (OpenInNewWindowAction.class).getString ("CTL_OpenInNewWindow"); // "Open In New Window"
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (OpenInNewWindowAction.class);
    }

    /** Resource name for the icon.
    * @return resource name
    */
    protected String iconResource () {
        // [PENDING]
        return "/org/openide/resources/actions/empty.gif"; // no icon // NOI18N
    }

    /**
    * Standart perform action extended by actually activated nodes.
    * @see CallableSystemAction#performAction
    *
    * @param activatedNodes gives array of actually activated nodes.
    */
    protected void performAction (final Node[] activatedNodes) {
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              for (int i = 0; i < activatedNodes.length; i++) {
                                                  URLNodeCookie unc = (URLNodeCookie)activatedNodes[i].getCookie(URLNodeCookie.class);
                                                  if (unc != null) {
                                                      unc.openInNewWindow();
                                                  }
                                              }
                                          }
                                      });
    }
}

/*
 * Log
 *  9    Gandalf   1.8         1/13/00  Ian Formanek    NOI18N #2
 *  8    Gandalf   1.7         11/27/99 Patrik Knakal   
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  2    Gandalf   1.1         3/9/99   Ian Formanek    
 *  1    Gandalf   1.0         2/25/99  Ian Formanek    
 * $
 */
