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

package org.netbeans.modules.java;

import org.openide.cookies.ConnectionCookie;
import org.openide.util.actions.CookieAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;

/** This action starts the process of synchronization
* among implementation and the interfaces.
*
* @author Petr Hamernik
*/
public final class SynchronizeAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 287995853453453479L;

    /** Creates and starts a thread for generating documentation
    */
    protected void performAction(Node[] activatedNodes) {
        JavaConnections.Type type = new JavaConnections.Type(JavaConnections.TYPE_SOURCE_CHECK_SELF);
        JavaConnections.Change[] changes = new JavaConnections.Change[] {
                                               new JavaConnections.Change(JavaConnections.TYPE_SOURCE_CHECK_SELF)
                                           };
        for (int i = 0; i < activatedNodes.length; i++) {
            ConnectionCookie.Listener l = (ConnectionCookie.Listener)
                                          activatedNodes[i].getCookie(ConnectionCookie.Listener.class);
            if (l != null) {
                JavaConnections.Event event = new JavaConnections.Event(
                                                  activatedNodes[i], changes, type
                                              );
                l.notify(event);
            }
        }
    }

    /** Cookie classes contains one class returned by cookie () method.
    */
    protected final Class[] cookieClasses () {
        return new Class[] { JavaDataObject.class };
    }

    /** All must be DataFolders or JavaDataObjects
    */
    protected int mode () {
        return MODE_ALL;
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (SynchronizeAction.class);
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return Util.getString("LAB_SynchronizeAction");
    }
}

/*
 * Log
 */
