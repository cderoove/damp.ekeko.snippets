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

import org.openide.cookies.ViewCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
* View an object (but do not edit it).
* @see ViewCookie
*
* @author Jan Jancura, Dafe Simonek
*/
public class ViewAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 2532281523106530739L;

    /* Default constructor */
    public ViewAction() {
        super();
    }

    /* Returns false - action should be disabled when a window with no
    * activated nodes is selected.
    *
    * @return false do not survive the change of focus
    */
    protected boolean surviveFocusChange () {
        return false;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle(ViewAction.class).getString("View");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ViewAction.class);
    }

    /* The resource string to our icon.
    * @return the icon resource string
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/view.gif"; // NOI18N
    }

    /* @return the mode of action. */
    protected int mode() {
        return MODE_ALL;
    }

    /* Creates a set of classes that are tested by this cookie.
    * Here only HtmlDataObject class is tested.
    *
    * @return list of classes the that this cookie tests
    */
    protected Class[] cookieClasses () {
        return new Class[] { ViewCookie.class };
    }

    /* Actually performs the action.
    * Calls edit on all activated nodes which supports
    * HtmlDataObject cookie.
    */
    protected void performAction (final Node[] activatedNodes) {
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              for (int i = 0; i < activatedNodes.length; i++) {
                                                  ViewCookie es = (ViewCookie)activatedNodes[i].getCookie(ViewCookie.class);
                                                  if (es != null) {
                                                      es.view();
                                                  }
                                              }
                                          }
                                      });
    }
}

/*
 * Log
 *  10   Gandalf   1.9         1/16/00  Jesse Glick     Fixing resource path.
 *  9    Gandalf   1.8         1/12/00  Ian Formanek    NOI18N
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  4    Gandalf   1.3         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  3    Gandalf   1.2         3/26/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  1    Gandalf   1.0         1/22/99  Jan Jancura     
 * $
 */
