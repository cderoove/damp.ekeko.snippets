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

import javax.swing.*;
import javax.swing.event.*;

import org.openide.awt.JInlineMenu;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;
import org.openide.util.actions.*;

/** A "meta-action" that displays (in a submenu) a list of enabled actions provided by modules.
* Such registered actions are called "service actions":
* they are provided externally but seem to provide additional services on existing components.
* Often they will be {@link NodeAction}s or {@link CookieAction}s so that they will
* be enabled based on the node selection, i.e. the node containing this popup.
* It is desirable for most nodes to include this action somewhere in their popup menu.
*
* <p><em>Note:</em> you do not need to touch this class to add a service action!
* Just add the action to a module manifest in an <code>Action</code> section.
*
* <p>The list of registered service actions is provided to this action from the implementation
* by means of {@link ToolsAction.Model}.
*
* @author Jaroslav Tulach
*/
public class ToolsAction extends SystemAction
    implements Presenter.Menu, Presenter.Popup {
    static final long serialVersionUID =4906417339959070129L;

    /** the model to use for this action */
    static Model model; // package private because used in Menu subclass


    /* @return name
    */
    public String getName () {
        return getActionName ();
    }

    /* @return help for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ToolsAction.class);
    }

    /* @return menu presenter for the action
    */
    public JMenuItem getMenuPresenter () {
        return createMenu (true);
    }

    /* @return menu presenter for the action
    */
    public JMenuItem getPopupPresenter () {
        return createMenu (false);
    }

    /* Does nothing.
    */
    public void actionPerformed (java.awt.event.ActionEvent ev) {
    }

    /** Assigns a model which can provide a list of service actions.
    * This can be done only once.
    * @param m a model that provides all actions installed by modules
    * @exception SecurityException if a model is already present
    */
    public synchronized static void setModel (Model m) {
        if (model != null) throw new SecurityException ();
        model = m;
    }

    /* @return name
    */
    private static String getActionName () {
        return ActionConstants.BUNDLE.getString ("CTL_Tools");
    }

    /* Creates the menu.
    */
    private static JMenuItem createMenu (boolean menu) {
        return new Inline (menu);
    }

    /** A model that provides a list of all service actions.
    * Normally these will be all actions installed by modules via their manifests.
    */
    public static interface Model {
        /** Get all registered actions.
        * Can contain <code>null</code>s that will be replaced by separators.
        */
        public SystemAction[] getActions ();

        /** Add a change listener to listen on changes of the set of actions.
        * @param l the listener to add
        */
        public void addChangeListener (javax.swing.event.ChangeListener l);

        /** Remove a change listener.
        * @param l the listener to remove
        */    
        public void removeChangeListener (javax.swing.event.ChangeListener l);
    }

    /** Inline menu. That is registered to changes of model.
    */
    private static final class Inline extends JInlineMenu {
        /** sub menu */
        private Menu menu;

        static final long serialVersionUID =2269006599727576059L;
        public Inline (boolean isForMenu) {
            this.menu = new Menu (this, isForMenu);
            if (isForMenu) this.menu.setIcon (SystemAction.get (ToolsAction.class).getIcon ());
        }

        void update () {
            if (menu.getMenuComponentCount () > 0) {
                setMenuItems (new JMenuItem[] { menu });
            } else {
                setMenuItems (new JMenuItem[0]);
            }
        }
    }

    /** Menu that displayes all activated actions.
    */
    private static final class Menu extends org.openide.awt.JMenuPlus implements ChangeListener {
        /** true if generation for menu, false otherwise */
        private boolean menu;
        /** reference to inline menu */
        private Inline il;
        /** true if the actions should be regenerated */
        private boolean dirty;

        static final long serialVersionUID =4339180785531920683L;

        /** @param menu true if generating for menu */
        Menu (Inline il, boolean menu) {
            super (getActionName ());
            this.menu = menu;
            this.il = il;

            HelpCtx.setHelpIDString (this, ToolsAction.class.getName ());
            generate ();

            ToolsAction.model.addChangeListener (WeakListener.change (this, ToolsAction.model));
        }

        /** Click does nothing.
        */
        public void doClick (int ms) {
            // ignore
        }

        /** Change of model.
        */
        public void stateChanged (ChangeEvent ev) {
            generate ();
        }

        /** Generate menu.
        */
        private void generate () {
            dirty = true;
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                if (!dirty) {
                                                    // ok, return
                                                    return;
                                                }
                                                // mark not dirty and continue
                                                dirty = false;

                                                removeAll ();
                                                SystemAction[] actions = ToolsAction.model.getActions ();
                                                boolean separator = false;
                                                boolean firstItemAdded = false; // flag to prevent adding separator before actual menu items

                                                for (int i = 0; i < actions.length; i++) {
                                                    SystemAction a = actions[i];
                                                    if (a == null) {
                                                        if (firstItemAdded) separator = true;
                                                        continue;
                                                    }

                                                    // not null
                                                    if (a.isEnabled ()) {

                                                        // only enabled actions
                                                        if (menu && a instanceof Presenter.Menu) {
                                                            if (separator) {
                                                                addSeparator ();
                                                                separator = false;
                                                            }
                                                            add (((Presenter.Menu)a).getMenuPresenter ());
                                                            firstItemAdded = true;
                                                            continue;
                                                        }

                                                        // popup
                                                        if (!menu && a instanceof Presenter.Popup) {
                                                            if (separator) {
                                                                addSeparator ();
                                                                separator = false;
                                                            }
                                                            add (((Presenter.Popup)a).getPopupPresenter ());
                                                            firstItemAdded = true;
                                                            continue;
                                                        }
                                                    }
                                                }

                                                il.update ();
                                            }
                                        });
        }


    } // end of Menu
}

/*
* Log
*  20   Gandalf   1.19        2/6/00   Jaroslav Tulach Should survive clicking 
*       on it.    
*  19   Gandalf   1.18        12/21/99 Ian Formanek    Fixed last change
*  18   Gandalf   1.17        12/20/99 Ian Formanek    Fixed icon in menu
*  17   Gandalf   1.16        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  16   Gandalf   1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  15   Gandalf   1.14        8/17/99  Ian Formanek    Generated serial version 
*       UID
*  14   Gandalf   1.13        8/17/99  Jaroslav Tulach Does not regenerate the 
*       state so often.
*  13   Gandalf   1.12        8/17/99  Jaroslav Tulach Appears again.
*  12   Gandalf   1.11        8/16/99  Jaroslav Tulach Deadlock solved.
*  11   Gandalf   1.10        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  10   Gandalf   1.9         8/5/99   Jaroslav Tulach Tools & New action in 
*       editor.
*  9    Gandalf   1.8         7/19/99  Ian Formanek    Preventing adding 
*       separator as the first item
*  8    Gandalf   1.7         7/19/99  Jesse Glick     Context help.
*  7    Gandalf   1.6         6/28/99  Ian Formanek    NbJMenu renamed to 
*       JMenuPlus
*  6    Gandalf   1.5         6/28/99  Ian Formanek    Fixed bug 2043 - It is 
*       virtually impossible to choose lower items of New From Template  from 
*       popup menu on 1024x768
*  5    Gandalf   1.4         6/24/99  Jesse Glick     Gosh-honest HelpID's.
*  4    Gandalf   1.3         6/10/99  Jesse Glick     [JavaDoc]
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         5/13/99  Ian Formanek    Services -> Tools
*  1    Gandalf   1.0         5/13/99  Jaroslav Tulach 
* $
*/
