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

package org.netbeans.examples.modules.globalactions;

import org.openide.*;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/** An example of a service action.
* Note that it is installed (and uninstalled) entirely by the manifest entry.
* <p>Any node, menu item, etc. providing {@link org.openide.actions.ToolsAction}
* will display it (at least when it is enabled). So for example, nodes by default
* will display this action in the popup menu if they can be opened somehow.
* Ditto for a group selection of nodes, all of which can be opened.
* <p>If you add an empty instance file named e.g. <code>Tools[org.openide-actions-ToolsAction].instance</code>
* to <code>$NBINSTALL/system/Toolbars/Tools/</code>, then Gandalf will display this list
* of tool actions in the global menu, and this action will then be enabled under it
* provided there is an acceptable node selection in the active window.
*/
public class TestServiceAction extends CookieAction {
    static final long serialVersionUID =-3122862332875637686L;
    public String getName () {
        return "Test Service Action";
    }
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
    /** Be sensitive to objects which can be opened. */
    protected Class[] cookieClasses () {
        return new Class[] { OpenCookie.class };
    }
    /** Require that all selected objects be openable. */
    protected int mode () {
        return MODE_ALL;
    }
    /** Open the selected files, after asking for user confirmation. */
    protected void performAction (Node[] nodes) {
        if (NotifyDescriptor.OK_OPTION == TopManager.getDefault ().notify
                (new NotifyDescriptor.Confirmation
                 ("Really open these files?", NotifyDescriptor.YES_NO_OPTION)))
            for (int i = 0; i < nodes.length; i++) {
                OpenCookie cookie = (OpenCookie) nodes[i].getCookie (OpenCookie.class);
                // Probably should never be null, but just in case:
                if (cookie != null) cookie.open ();
            }
    }
}
