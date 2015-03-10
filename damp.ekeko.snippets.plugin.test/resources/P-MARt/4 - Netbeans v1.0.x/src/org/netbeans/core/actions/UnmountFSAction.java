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

package org.netbeans.core.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.nodes.Node;

import org.netbeans.core.UnmountFSCookie;

/** Unmount FS action.
*
* @author   Petr Hamernik
*/
public class UnmountFSAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1108017847125209182L;

    protected Class[] cookieClasses () {
        return new Class[] { UnmountFSCookie.class };
    }

    protected int mode () {
        return MODE_ALL;
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(UnmountFSAction.class).getString("UnmountFS");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (UnmountFSAction.class);
    }

    /** Resource string to action's icon.
    * @return resource string of the icon
    */
    protected String iconResource() {
        return "/org/netbeans/core/resources/actions/unmountFS.gif"; // NOI18N
    }

    /**
    * Standart perform action extended by actually activated nodes.
    * @see CallableSystemAction#performAction
    *
    * @param activatedNodes gives array of actually activated nodes.
    */
    public void performAction (Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            UnmountFSCookie c = (UnmountFSCookie)activatedNodes[i].getCookie (UnmountFSCookie.class);
            if (c != null) {
                c.unmount ();
            }
        }
    }

}

/*
 * Log
 *  13   Gandalf   1.12        1/12/00  Ian Formanek    NOI18N
 *  12   Gandalf   1.11        1/12/00  Ales Novak      i18n
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  9    Gandalf   1.8         6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  8    Gandalf   1.7         6/9/99   Ian Formanek    ToolsAction
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  5    Gandalf   1.4         3/21/99  Jaroslav Tulach Repository displayed ok.
 *  4    Gandalf   1.3         3/2/99   David Simonek   icons repair
 *  3    Gandalf   1.2         1/21/99  David Simonek   Removed references to 
 *       "Actions" class
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
