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

import org.openide.cookies.OpenCookie;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.*;
import org.openide.nodes.Node;

/** Opens a node (e.g.<!-- --> in a web browser, or in the Editor).
* @see OpenCookie
*
* @author   Petr Hamernik
*/
public class OpenAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5847763658433081444L;

    /* @return set of needed cookies */
    protected Class[] cookieClasses () {
        return new Class[] { OpenCookie.class };
    }

    /* @return false */
    protected boolean surviveFocusChange () {
        return false;
    }

    /* @return any */
    protected int mode () {
        return MODE_ANY;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("Open");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (OpenAction.class);
    }

    /* Resource name for the icon.
    * @return resource name
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /*
    * Standart perform action extended by actually activated nodes.
    * @see CallableSystemAction#performAction
    *
    * @param activatedNodes gives array of actually activated nodes.
    */
    protected void performAction (final Node[] activatedNodes) {
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              for (int i = 0; i < activatedNodes.length; i++) {
                                                  OpenCookie oc =
                                                      (OpenCookie)activatedNodes[i].getCookie(OpenCookie.class);
                                                  if (oc != null) {
                                                      oc.open();
                                                  }
                                              }
                                          }
                                      });
    }
}

/*
 * Log
 *  14   Gandalf   1.13        1/12/00  Ian Formanek    NOI18N
 *  13   Gandalf   1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  11   Gandalf   1.10        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  8    Gandalf   1.7         5/2/99   Ian Formanek    Fixed last change
 *  7    Gandalf   1.6         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  6    Gandalf   1.5         3/31/99  David Simonek   ugly ugly ugly 
 *       requestFocus bungs fixed
 *  5    Gandalf   1.4         3/26/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/26/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
