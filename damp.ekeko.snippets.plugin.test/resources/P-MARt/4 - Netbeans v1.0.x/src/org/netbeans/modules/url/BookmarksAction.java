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

import javax.swing.*;

import org.openide.awt.Actions;
import org.openide.explorer.view.MenuView;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/** This action installs new bean into the system.
*
* @author Ian Formanek
*/
public class BookmarksAction extends CallableSystemAction {
    static final long serialVersionUID =2183102479251760123L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 7755319389083740521L;

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle (BookmarksAction.class).getString("ACT_Bookmarks");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (BookmarksAction.class);
    }

    /** Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/netbeans/modules/url/bookmarks.gif"; // NOI18N
    }

    /* Creates presenter that invokes the associated presenter.
    */
    public JMenuItem getMenuPresenter() {
        JMenuItem menu = getPopupPresenter ();
        menu.setIcon (new ImageIcon(BookmarksAction.class.getResource ("/org/netbeans/modules/url/bookmarks.gif"))); // NOI18N
        return menu;
    }

    /* Creates presenter that displayes submenu with all
    * bookmarks.
    */
    public JMenuItem getPopupPresenter() {
        JMenuItem menu = new MenuView.Menu (new BookmarksNode (), new BookmarksActionListener (), false);
        Actions.connect (menu, this, true);
        return menu;
    }

    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    */
    public void performAction() {
    }

    /** Actions listener which opens the clicked bookmark */
    private static class BookmarksActionListener implements MenuView.Acceptor, DataFilter {

        static final long serialVersionUID =7600742133604718373L;
        public boolean accept (Node n) {
            URLNodeCookie cookie = (URLNodeCookie)n.getCookie (URLNodeCookie.class);
            if (cookie == null) {
                // do not accept
                return false;
            }
            cookie.openInNewWindow ();
            return true;
        }

        /** Data filter impl.
        */
        public boolean acceptDataObject (DataObject obj) {
            return (obj.getNodeDelegate ().getCookie (URLNodeCookie.class) != null);
        }
    }

}

/*
 * Log
 *  4    Gandalf   1.3         1/5/00   Ian Formanek    NOI18N
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  1    Gandalf   1.0         7/5/99   Ian Formanek    
 * $
 */
