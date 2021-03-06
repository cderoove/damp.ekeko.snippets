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

package org.netbeans.modules.web.core.jsploader;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
* Edit an object.
* @see EditCookie
*
* @author Jaroslav Tulach
*/
public class EditServletAction extends CookieAction {

    /** serialVersionUID */
    private static final long serialVersionUID = 183706095337315796L;

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
        return NbBundle.getBundle(EditServletAction.class).getString("EditServlet");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (EditServletAction.class);
    }

    /* The resource string to our icon.
    * @return the icon resource string
    */
    protected String iconResource () {
        return "/org/netbeans/modules/html/htmlEdit.gif"; // NOI18N
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
        return new Class[] { JspDataObject.ServletOpenCookie.class };
    }

    /* Actually performs the action.
    * Calls edit on all activated nodes which supports
    * HtmlDataObject cookie.
    */
    protected void performAction (final Node[] activatedNodes) {
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              for (int i = 0; i < activatedNodes.length; i++) {
                                                  JspDataObject.ServletOpenCookie es = (JspDataObject.ServletOpenCookie)activatedNodes[i].getCookie(JspDataObject.ServletOpenCookie.class);
                                                  if (es != null) {
                                                      es.open ();
                                                  }
                                              }
                                          }
                                      });
    }
}

/*
 * Log
 *  3    Gandalf   1.2         1/12/00  Petr Jiricka    i18n phase 1
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/22/99  Petr Jiricka    
 * $
 */
