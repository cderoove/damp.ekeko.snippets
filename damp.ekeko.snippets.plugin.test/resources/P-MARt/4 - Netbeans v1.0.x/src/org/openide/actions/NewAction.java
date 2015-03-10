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

import java.awt.MenuShortcut;
import javax.swing.ImageIcon;
import javax.swing.event.*;

import org.openide.util.datatransfer.NewType;
import org.openide.loaders.DataObject;
import org.openide.util.enum.ArrayEnumeration;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.windows.TopComponent;

/** Creates a new child of the activated node, if appropriate.
* @see Node#getNewTypes
*
* @author   Petr Hamernik, Ian Formanek
* @version  0.15, Jun 19, 1998
*/
public final class NewAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5569219524388004456L;
    /** Imlementation of ActSubMenuInt */
    private static ActSubMenuModel model = new ActSubMenuModel();

    protected void performAction (Node[] activatedNodes) {
        performAction (activatedNodes, 0);
    }

    /** Performs action on index.
    */
    private void performAction (int indx) {
        performAction (TopComponent.getRegistry ().getCurrentNodes (), indx);
    }


    /** Performs action on index and nodes.
    */
    private void performAction (Node[] activatedNodes, int indx) {
        NewType[] types = getNewTypes (activatedNodes);

        if (types.length <= indx) {
            return;
        }

        try {
            types[indx].create();
        } catch (java.io.IOException e) {
            TopManager.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    /** Getter for array of activated new types.
    */
    private static  NewType[] getNewTypes () {
        return getNewTypes (TopComponent.getRegistry ().getCurrentNodes ());
    }

    /** Getter for array of activated new types.
    */
    private static  NewType[] getNewTypes (Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length != 1) {
            return new NewType[0];
        } else {
            return activatedNodes[0].getNewTypes ();
        }
    }

    protected boolean enable (Node[] activatedNodes) {
        NewType[] types = getNewTypes ();

        // notify listeners
        Object[] listeners = model.getListenerList();
        if (listeners.length > 0) {
            ChangeEvent ev = new ChangeEvent (model);
            for (int i = listeners.length-1; i>=0; i-=2) {
                ((ChangeListener)listeners[i]).stateChanged (ev);
            }
        }

        return (types.length > 0);
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        NewType[] newTypes = getNewTypes();
        if ((newTypes != null) && (newTypes.length == 1))
            return ActionConstants.getString("NewArg", newTypes [0].getName ());
        else
            return ActionConstants.BUNDLE.getString("New");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (NewAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/new.gif"; // NOI18N
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a MenuBar.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getMenuPresenter() {
        return new Actions.SubMenu(this, model, false);
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a PopuMenu.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getPopupPresenter() {
        return new Actions.SubMenu(this, model, true);
    }

    /** Implementation of ActSubMenuInt */
    private static class ActSubMenuModel extends EventListenerList implements Actions.SubMenuModel {
        static final long serialVersionUID =-4273674308662494596L;
        public int getCount() {
            return getNewTypes ().length;
        }

        public String getLabel(int index) {
            NewType[] newTypes = getNewTypes();
            if (newTypes.length <= index)
                return null;
            else
                return newTypes[index].getName();
        }

        public HelpCtx getHelpCtx (int index) {
            NewType[] newTypes = getNewTypes();
            if (newTypes.length <= index)
                return null;
            else
                return newTypes[index].getHelpCtx();
        }

        public void performActionAt(int index) {
            NewAction a = (NewAction)findObject (NewAction.class);
            if (a == null) return;
            a.performAction(index);
        }

        /** Adds change listener for changes of the model.
        */
        public void addChangeListener (ChangeListener l) {
            add (ChangeListener.class, l);
        }

        /** Removes change listener for changes of the model.
        */
        public void removeChangeListener (ChangeListener l) {
            remove (ChangeListener.class, l);
        }

    }
}

/*
 * Log
 *  25   Gandalf   1.24        1/12/00  Ian Formanek    NOI18N
 *  24   Gandalf   1.23        11/24/99 Jaroslav Tulach New "New From Template" 
 *       Dialog
 *  23   Gandalf   1.22        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  22   Gandalf   1.21        9/29/99  Petr Hamernik   IOException notification
 *       improved
 *  21   Gandalf   1.20        8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  20   Gandalf   1.19        8/5/99   Jaroslav Tulach Tools & New action in 
 *       editor.
 *  19   Gandalf   1.18        7/16/99  Jesse Glick     Actions.SubMenuModel.getHelpCtx
 *       
 *  18   Gandalf   1.17        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  17   Gandalf   1.16        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  16   Gandalf   1.15        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  15   Gandalf   1.14        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  14   Gandalf   1.13        5/2/99   Ian Formanek    Fixed last change
 *  13   Gandalf   1.12        5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  12   Gandalf   1.11        3/26/99  Jesse Glick     [JavaDoc]
 *  11   Gandalf   1.10        3/2/99   Jaroslav Tulach Icon changes
 *  10   Gandalf   1.9         3/1/99   Jaroslav Tulach 
 *  9    Gandalf   1.8         3/1/99   David Simonek   icons etc..
 *  8    Gandalf   1.7         2/19/99  Jaroslav Tulach Deleted 
 *       CreateOperationException
 *  7    Gandalf   1.6         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  6    Gandalf   1.5         2/11/99  Ian Formanek    Last change undone
 *  5    Gandalf   1.4         2/11/99  Ian Formanek    getXXXPresenter -> 
 *       createXXXPresenter (XXX={Menu, Toolbar})
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
 *  3    Gandalf   1.2         1/6/99   Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
