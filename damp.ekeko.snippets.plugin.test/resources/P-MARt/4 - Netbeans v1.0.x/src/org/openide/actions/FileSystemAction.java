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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.openide.awt.JInlineMenu;
import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.windows.Workspace;
import org.openide.windows.TopComponent;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.enum.*;


/** Action that presents standard file system-related actions.
* Listens until a node representing a {@link DataObject}
* is selected and then retrieves {@link SystemAction}s from its
* {@link FileSystem}.
*
* @author  Jaroslav Tulach
* @version 0.10, Jun 16, 1998
*/

public class FileSystemAction extends SystemAction
    implements Presenter.Menu, Presenter.Popup {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7572252564167491022L;
    /** empty array of menu items */
    static JMenuItem[] NONE = new JMenuItem[] {};


    /** Creates menu for currently selected nodes.
    * @param popUp create popup or normal menu
    */
    static JMenuItem[] createMenu (boolean popUp) {
        Node[] n = TopComponent.getRegistry ().getActivatedNodes ();
        Node.Cookie cookie;

        if (n != null) {
            // enumeration of enumeration of SystemActions
            Enumeration en = new AlterEnumeration (new ArrayEnumeration (n)) {
                                 public Object alter (Object o) {
                                     Node node = (Node)o;
                                     DataObject obj = (DataObject)node.getCookie (DataObject.class);
                                     if (obj == null) return EmptyEnumeration.EMPTY;

                                     try {
                                         FileSystem fs = obj.getPrimaryFile ().getFileSystem ();
                                         return new ArrayEnumeration (fs.getActions ());
                                     } catch (FileStateInvalidException ex) {
                                         return EmptyEnumeration.EMPTY;
                                     }
                                 }
                             };

            return createMenu (new SequenceEnumeration (en), popUp);
        }
        return NONE;
    }

    /** Creates list of menu items that should be used for given
    * data object.
    * @param en enumeration of SystemAction that should be added
    *   into the menu if enabled and if not duplicated
    */
    static JMenuItem[] createMenu (Enumeration en, boolean popUp) {
        en = new RemoveDuplicatesEnumeration (en);

        ArrayList items = new ArrayList ();
        while (en.hasMoreElements ()) {
            SystemAction a = (SystemAction)en.nextElement ();
            if (a.isEnabled ()) {
                JMenuItem item = null;
                if (popUp) {
                    if (a instanceof Presenter.Popup) {
                        item = ((Presenter.Popup)a).getPopupPresenter ();
                    }
                } else {
                    if (a instanceof Presenter.Menu) {
                        item = ((Presenter.Menu)a).getMenuPresenter ();
                    }
                }
                // test if we obtained the item
                if (item != null) {
                    items.add (item);
                }
            }
        }
        JMenuItem[] array = new JMenuItem [items.size ()];
        items.toArray (array);
        return array;
    }

    /* @return menu presenter.
    */
    public JMenuItem getMenuPresenter () {
        return new Menu (false);
    }

    /* @return popup presenter.
    */
    public JMenuItem getPopupPresenter () {
        return new Menu (true);
    }

    /* Getter for name
    */
    public String getName () {
        return ActionConstants.BUNDLE.getString("ACT_FileSystemAction");
    }

    /* Getter for help.
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (FileSystemAction.class);
    }

    /* Do nothing.
    * This action itself does nothing, it only presents other actions.
    * @param ev ignored
    */
    public void actionPerformed (java.awt.event.ActionEvent e) {}

    /** Presenter for this action.
    */
    private class Menu extends JInlineMenu {
        /** menu presenter (true) or popup presenter (false) */
        private boolean popup;
        /** last registered items */
        private JMenuItem[] last = NONE;
        /** own property change listner */
        private PropL propL = new PropL ();

        static final long serialVersionUID =2650151487189209766L;

        /** Creates new instance for menu/popup presenter.
        * @param popup true if this should represent popup
        */
        Menu (boolean popup) {
            this.popup = popup;
            changeMenuItems (createMenu (popup));
            TopComponent.getRegistry ().addPropertyChangeListener (
                org.openide.util.WeakListener.propertyChange (propL, TopComponent.getRegistry ())
            );
        }

        /** Changes the selection to new items.
        * @param items the new items
        */
        synchronized void changeMenuItems (JMenuItem[] items) {
            removeListeners (last);
            addListeners (items);
            last = items;
            setMenuItems (items);
        }


        /** Add listeners to menu items.
        * @param items the items
        */
        private void addListeners (JMenuItem[] items) {
            int len = items.length;
            for (int i = 0; i < len; i++) {
                items[i].addPropertyChangeListener (propL);
            }
        }

        /** Remove all listeners from menu items.
        * @param items the items
        */
        private void removeListeners (JMenuItem[] items) {
            int len = items.length;
            for (int i = 0; i < len; i++) {
                items[i].removePropertyChangeListener (propL);
            }
        }

        /** Property listnener to watch changes of enable state.
        */
        private class PropL implements PropertyChangeListener, Runnable {
            public void propertyChange (PropertyChangeEvent ev) {
                String name = ev.getPropertyName ();
                if (
                    name == null ||
                    name.equals (SystemAction.PROP_ENABLED) ||
                    name.equals (TopComponent.Registry.PROP_ACTIVATED_NODES)
                ) {
                    // change items later
                    SwingUtilities.invokeLater (this);
                }
            }

            public void run() {
                changeMenuItems (createMenu (popup));
            }
        }

    }
}

/*
 * Log
 *  18   Gandalf   1.17        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  17   Gandalf   1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        10/8/99  Jaroslav Tulach Enabled on more then one
 *       node.
 *  15   Gandalf   1.14        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  14   Gandalf   1.13        7/11/99  David Simonek   window system change...
 *  13   Gandalf   1.12        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  12   Gandalf   1.11        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   Gandalf   1.10        4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  10   Gandalf   1.9         3/26/99  Jesse Glick     [JavaDoc]
 *  9    Gandalf   1.8         3/26/99  Jesse Glick     SystemAction.actionPerformed(ActionEvent)
 *        is now abstract; you must explicitly provide an empty body if that is 
 *       desired.
 *  8    Gandalf   1.7         3/22/99  Jaroslav Tulach Fixed creation from 
 *       template
 *  7    Gandalf   1.6         3/4/99   Petr Hamernik   
 *  6    Gandalf   1.5         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  5    Gandalf   1.4         2/11/99  Ian Formanek    Last change undone
 *  4    Gandalf   1.3         2/11/99  Ian Formanek    getXXXPresenter -> 
 *       createXXXPresenter (XXX={Menu, Toolbar})
 *  3    Gandalf   1.2         1/6/99   Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
