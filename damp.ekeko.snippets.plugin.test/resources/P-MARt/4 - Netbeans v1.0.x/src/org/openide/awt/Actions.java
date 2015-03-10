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

package org.openide.awt;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;

import org.openide.TopManager;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;

/** Supporting class for manipulation with menu and toolbar presenters.
*
* @author   Jaroslav Tulach
*/
public class Actions extends Object {
    /** Method that finds the keydescription assigned to this action.
    * @param action action to find key for
    * @return the text representing the key or null if  there is no text assigned
    */
    public static String findKey (SystemAction action) {
        TopManager t = TopManager.getDefault ();
        if (t == null) {
            return null;
        }
        KeyStroke[] arr = t.getGlobalKeymap ().getKeyStrokesForAction (action);

        if (arr.length == 0) {
            return null;
        }

        KeyStroke accelerator = arr[0];
        int modifiers = accelerator.getModifiers();
        String acceleratorText = ""; // NOI18N
        if (modifiers > 0) {
            acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
            acceleratorText += "+"; // NOI18N
        } else if (accelerator.getKeyCode() == KeyEvent.VK_UNDEFINED) {
            return ""; // NOI18N
        }
        acceleratorText += KeyEvent.getKeyText(accelerator.getKeyCode());
        return acceleratorText;
    }

    /** Attaches menu item to an action.
    * @param item menu item
    * @param action action
    * @param popup create popup or menu item
    */
    public static void connect (JMenuItem item, SystemAction action, boolean popup) {
        Bridge b = new MenuBridge (item, action, popup);
        b.updateState (null);
    }

    /** Attaches checkbox menu item to boolean state action.
    * @param item menu item
    * @param action action
    * @param popup create popup or menu item
    */
    public static void connect (JCheckBoxMenuItem item, BooleanStateAction action, boolean popup) {
        Bridge b = new CheckMenuBridge (item, action, popup);
        b.updateState (null);
    }

    /** Connects buttons to action.
    * @param button the button
    * @param action the action
    */
    public static void connect (AbstractButton button, SystemAction action) {
        Bridge b = new ButtonBridge (button, action);
        b.updateState (null);
    }

    /** Connects buttons to action.
    * @param button the button
    * @param action the action
    */
    public static void connect (AbstractButton button, BooleanStateAction action) {
        Bridge b = new BooleanButtonBridge (button, action);
        b.updateState (null);
    }

    /** Adds a change listener to TopManager's keymap.
    */
    private static void addKeymapListener (PropertyChangeListener l) {
        TopManager t = TopManager.getDefault ();
        if (t != null) {
            t.addPropertyChangeListener (l);
        }
    }

    /** Find key strokes for action.
    * @param action
    * @return array of keystrokes that invoke that action
    */
    private static KeyStroke[] getKeyStrokesForAction (SystemAction action) {
        TopManager t = TopManager.getDefault ();
        if (t != null) {
            return t.getGlobalKeymap ().getKeyStrokesForAction (action);
        } else {
            return new KeyStroke[0];
        }
    }

    /** Sets the text for the menu item. Cut from the name '&' char.
    * @param item MenuItem
    * @param text new label
    * @param useMnemonic if true and '&' char found in new text, next char is used
    *           as Mnemonic.
    */
    static void setMenuText(JMenuItem item, String text, boolean useMnemonic) {
        int i = text.indexOf('&');
        String newText = text;

        if (i < 0) {
            item.setText(text);
        }
        else {
            item.setText(text.substring(0, i) + text.substring(i + 1));
            if (useMnemonic) {
                item.setMnemonic(text.charAt(i + 1));
            }
        }
    }

    /** Cuts first occurence of '&' and
    * @return string without first '&' if there was any.
    */
    public static String cutAmpersand(String text) {
        int i = text.indexOf('&');
        return (i < 0) ? text : (text.substring(0, i) + text.substring(i + 1));
    }

    /** Listener on showing/hiding state of the component.
    * Is attached to menu or toolbar item in prepareXXX methods and
    * method addNotify is called when the item is showing and
    * the method removeNotify is called when the item is hidding.
    * <P>
    * There is a special support listening on changes in the action and
    * if such change occures, updateState method is called to
    * reflect it.
    */
    private static abstract class Bridge extends Object
        implements PropertyChangeListener {
        /** component to work with */
        protected JComponent comp;
        /** action to associate */
        protected SystemAction action;
        /** property change listener.
        */
        private PropertyChangeListener propL;

        /** @param comp component
        * @param action the action
        */
        public Bridge (JComponent comp, SystemAction action) {
            this.comp = comp;
            this.action = action;

            // attaches visibility listener safely in event thread
            Bridge.this.comp.addPropertyChangeListener (Bridge.this);
            if (Bridge.this.comp.isShowing ()) {
                addNotify ();
            }

            // listener on keys used for this object
            addKeymapListener (WeakListener.propertyChange (this, TopManager.getDefault ()));

            // associate context help, if applicable
            HelpCtx help = action.getHelpCtx ();
            if (help != null && ! help.equals (HelpCtx.DEFAULT_HELP) && help.getHelpID () != null)
                HelpCtx.setHelpIDString (comp, help.getHelpID ());
        }

        /** Attaches listener to given action */
        public void addNotify () {
            propL = WeakListener.propertyChange (this, action);
            action.addPropertyChangeListener (propL);
            updateState (null);
        }

        /** Remove the listener */
        public void removeNotify () {
            action.removePropertyChangeListener (propL);
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        public abstract void updateState (String changedProperty);

        /** Listener to changes of some properties.
        * Multicast - reacts to keymap changes and ancestor changes
        * together.
        */
        public void propertyChange (final PropertyChangeEvent ev) {
            // fire later
            javax.swing.SwingUtilities.invokeLater (new Runnable () {
                                                        public void run () {
                                                            if ("ancestor".equals(ev.getPropertyName())) {
                                                                // ancestor change - decide if parent is null or not
                                                                if (ev.getNewValue() != null)
                                                                    addNotify();
                                                                else
                                                                    removeNotify();

                                                                return;
                                                            }
                                                            updateState (ev.getPropertyName ());
                                                        }
                                                    });
        }
    }


    /** Bridge between an action and button.
    */
    private static class ButtonBridge extends Bridge {
        /** the button */
        protected AbstractButton button;

        public ButtonBridge (AbstractButton button, SystemAction action) {
            super (button, action);
            button.addActionListener (action);
            this.button = button;
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        public void updateState (String changedProperty) {
            if (changedProperty == null || changedProperty.equals ("enabled")) { // NOI18N
                button.setEnabled (action.isEnabled ());
            }
            if (changedProperty == null || changedProperty.equals ("icon")) { // NOI18N
                button.setIcon (action.getIcon ());
            }

            if (changedProperty == null || changedProperty.equals (TopManager.PROP_GLOBAL_KEYMAP)) {
                String tip = findKey (action);
                if (tip == null || tip.equals("")) { // NOI18N
                    button.setToolTipText(cutAmpersand(action.getName()));
                } else {
                    button.setToolTipText(java.text.MessageFormat.format (
                                              org.openide.util.NbBundle.getBundle(Actions.class).getString("FMT_ButtonHint"),
                                              new Object[] { cutAmpersand(action.getName()), tip }
                                          )
                                         );
                }
            }
        }
    }

    /** Bridge for button and boolean action.
    */
    private static class BooleanButtonBridge extends ButtonBridge {

        public BooleanButtonBridge (AbstractButton button, BooleanStateAction action) {
            super (button, action);
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        public void updateState (String changedProperty) {
            super.updateState (changedProperty);
            if (changedProperty == null || changedProperty.equals (BooleanStateAction.PROP_BOOLEAN_STATE)) {
                button.setSelected (((BooleanStateAction)action).getBooleanState ());
            }
        }

    }

    /** Menu item bridge.
    */
    private static class MenuBridge extends ButtonBridge {
        /** behave like menu or popup */
        private boolean popup;

        /** Constructor.
        * @param popup pop-up menu
        */
        public MenuBridge (JMenuItem item, SystemAction action, boolean popup) {
            super (item, action);
            this.popup = popup;

            if (popup) {
                prepareMargins (item, action);
            }
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        public void updateState (String changedProperty) {
            if (changedProperty == null || changedProperty.equals ("enabled")) { // NOI18N
                button.setEnabled (action.isEnabled ());
            }

            if (changedProperty == null || !changedProperty.equals ("accelerator")) { // NOI18N
                updateKey ((JMenuItem)comp, action);
            }

            if (!popup) {

                if (changedProperty == null || changedProperty.equals ("icon")) { // NOI18N
                    button.setIcon (action.getIcon ());
                }
            }

            if (changedProperty == null || changedProperty.equals ("name")) { // NOI18N
                setMenuText (((JMenuItem)comp), action.getName (), !popup);
            }
        }
    }

    /** Check menu item bridge.
    */
    private static final class CheckMenuBridge extends BooleanButtonBridge {
        /** is popup or menu */
        private boolean popup;

        /** Popup menu */
        public CheckMenuBridge (JCheckBoxMenuItem item, BooleanStateAction action, boolean popup) {
            super (item, action);
            this.popup = popup;

            if (popup) {
                prepareMargins (item, action);
            }
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        public void updateState (String changedProperty) {
            super.updateState (changedProperty);

            updateKey ((JMenuItem)comp, action);

            if (changedProperty == null || changedProperty.equals ("name")) { // NOI18N
                setMenuText (((JMenuItem)comp), action.getName (), !popup);
            }
        }
    }

    /** Sub menu bridge.
    */
    private static final class SubMenuBridge extends MenuBridge
        implements ChangeListener {
        /** model to obtain subitems from */
        private SubMenuModel model;
        /** submenu */
        private SubMenu menu;

        /** Constructor.
        */
        public SubMenuBridge (SubMenu item, SystemAction action, SubMenuModel model, boolean popup) {
            super (item, action, popup);
            prepareMargins (item, action);


            menu = item;
            this.model = model;
            model.addChangeListener (WeakListener.change (this, model));
        }

        public void addNotify () {
            super.addNotify ();
            generateSubMenu ();
        }

        /** Called when model changes. Regenerates the model.
        */
        public void stateChanged (ChangeEvent ev) {
            // change in keys or in submenu model
            generateSubMenu ();
        }

        /** Regenerates the menu
        */
        private void generateSubMenu() {
            boolean shouldUpdate = false;
            try {
                menu.removeAll ();

                int cnt = model.getCount ();

                if (cnt != menu.previousCount) {
                    // update UI
                    shouldUpdate = true;
                }
                // in all cases remeber the previous
                menu.previousCount = cnt;

                // remove if there is an previous listener
                if (menu.oneItemListener != null) {
                    menu.removeActionListener(menu.oneItemListener);
                }
                if (cnt == 0) {
                    // menu disabled
                    menu.setEnabled (false);
                    return;
                } else {
                    menu.setEnabled (true);
                    // go on
                }

                if (cnt == 1) {
                    // generate without submenu
                    menu.addActionListener(menu.oneItemListener = new ISubActionListener(0, model));
                    HelpCtx help = model.getHelpCtx (0);
                    associateHelp (menu, help == null ? action.getHelpCtx () : help);
                } else {
                    for (int i = 0; i < model.getCount(); i++) {
                        String label = model.getLabel(i);
                        //          MenuShortcut shortcut = support.getMenuShortcut(i);
                        if (label == null)
                            menu.addSeparator();
                        else {
                            //       if (shortcut == null)
                            JMenuItem item = new JMenuItem(label);
                            //       else
                            //         item = new JMenuItem(label, shortcut);
                            item.addActionListener(new ISubActionListener(i, model));
                            HelpCtx help = model.getHelpCtx (i);
                            associateHelp (item, help == null ? action.getHelpCtx () : help);
                            menu.add(item);
                        }
                    }
                    associateHelp (menu, action.getHelpCtx ());
                }
            } finally {
                if (shouldUpdate) {
                    menu.updateUI ();
                }
            }
        }
        private void associateHelp (JComponent comp, HelpCtx help) {
            if (help != null && ! help.equals (HelpCtx.DEFAULT_HELP) && help.getHelpID () != null)
                HelpCtx.setHelpIDString (comp, help.getHelpID ());
            else
                HelpCtx.setHelpIDString (comp, null);
        }

        /** The class that listens to the menu item selections and forwards it to the
        * action class via the performAction() method.
        */
        private static class ISubActionListener implements java.awt.event.ActionListener {
            int index;
            SubMenuModel support;

            public ISubActionListener(int index, SubMenuModel support) {
                this.index = index;
                this.support = support;
            }

            /** called when a user clicks on this menu item */
            public void actionPerformed(ActionEvent e) {
                support.performActionAt(index);
            }
        }

    }


    //
    // Methods for configuration of MenuItems
    //


    /** Method to prepare the margins and text positions.
    */
    static void prepareMargins (JMenuItem item, SystemAction action) {
        Insets margin = item.getMargin ();
        margin.left = 0;
        item.setMargin(margin);
        item.setHorizontalTextPosition(JMenuItem.RIGHT);
        item.setHorizontalAlignment(JMenuItem.LEFT);
    }

    /** Updates value of the key
    * @param item item to update
    * @param action the action to update
    */
    static void updateKey (JMenuItem item, SystemAction action) {
        if (!(item instanceof JMenu)) {
            // menu does not have accelerators
            // key
            KeyStroke[] arr = getKeyStrokesForAction (action);
            if (arr.length != 0) {
                // assign the key
                item.setAccelerator (arr[0]);
            } else {
                item.setAccelerator (null);
            }
        }
    }



    //
    //
    // The presenter classes
    //
    //

    /** Actions.MenuItem extends the java.awt.MenuItem and adds a connection to Corona
    * system actions. The ActMenuItem processes the MenuEvents itself and
    * calls the action.performAction() method.
    * It also tracks the enabled state of the action and reflects it as its
    * visual enabled state.
    *
    */
    public static class MenuItem extends javax.swing.JMenuItem {
        static final long serialVersionUID =-21757335363267194L;
        /** Constructs a new ActMenuItem with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * @param action the action to which this menu item should be connected
        * @param label a string label for the check box menu item,
        *              or null for an unlabeled menu item.
        * @param showIcon if true, the menu item has an icon of the action
        * @param useMnemonic if true, the menu try to find mnemonic in action label
        */
        public MenuItem (SystemAction aAction, boolean useMnemonic) {
            Actions.connect (this, aAction, !useMnemonic);
        }
    }

    /** CheckboxMenuItem extends the java.awt.CheckboxMenuItem and adds
    * a connection to Corona boolean state actions. The ActCheckboxMenuItem
    * processes the ItemEvents itself and calls the action.seBooleanState() method.
    * It also tracks the enabled and boolean state of the action and reflects it
    * as its visual enabled/check state.
    *
    * @author   Ian Formanek, Jan Jancura
    */
    public static class CheckboxMenuItem extends javax.swing.JCheckBoxMenuItem {
        static final long serialVersionUID =6190621106981774043L;
        /** Constructs a new ActCheckboxMenuItem with the specified label
        *  and connects it to the given BooleanStateAction.
        * @param action the action to which this menu item should be connected
        * @param label a string label for the check box menu item,
        *              or null for an unlabeled menu item.
        * @param useMnemonic if true, the menu try to find mnemonic in action label
        */
        public CheckboxMenuItem (BooleanStateAction aAction, boolean useMnemonic) {
            Actions.connect (this, aAction, !useMnemonic);
        }
    }

    /** Component shown in toolbar, representing an action.
    *
    */
    public static class ToolbarButton extends org.openide.awt.ToolbarButton {
        static final long serialVersionUID =6564434578524381134L;
        public ToolbarButton (SystemAction aAction) {
            super (null);
            Actions.connect (this, aAction);
        }

        /**
         * Gets the maximum size of this component.
         * @return A dimension object indicating this component's maximum size.
         * @see #getMinimumSize
         * @see #getPreferredSize
         * @see LayoutManager
         */
        public Dimension getMaximumSize() {
            return this.getPreferredSize ();
        }

        public Dimension getMinimumSize() {
            return this.getPreferredSize ();
        }
    }


    /** The Component for BooleeanState action that is to be shown
    * in a toolbar.
    *
    */
    public static class ToolbarToggleButton extends org.openide.awt.ToolbarToggleButton {
        static final long serialVersionUID =-4783163952526348942L;
        /** Constructs a new ActToolbarToggleButton for specified action */
        public ToolbarToggleButton (BooleanStateAction aAction) {
            super(null, false);
            Actions.connect (this, aAction);
        }

        /**
         * Gets the maximum size of this component.
         * @return A dimension object indicating this component's maximum size.
         * @see #getMinimumSize
         * @see #getPreferredSize
         * @see LayoutManager
         */
        public Dimension getMaximumSize() {
            return this.getPreferredSize ();
        }

        public Dimension getMinimumSize() {
            return this.getPreferredSize ();
        }
    }


    /** Interface for the creating Actions.SubMenu. It provides the methods for
    * all items in submenu: name shortcut and perform method. Also has methods
    * for notification of changes of the model.
    */
    public static interface SubMenuModel {
        /** @return count of the submenu items. */
        public int getCount();

        /** Gets label for specific index
        * @index of the submenu item
        * @return label for this menu item
        */
        public String getLabel(int index);

        /** Gets shortcut for specific index
        * @index of the submenu item
        * @return menushortcut for this menu item
        */
        //    public MenuShortcut getMenuShortcut(int index);

        /** Get context help for the specified item.
        * This can be used to associate help with individual items.
        * You may return <code>null</code> to just use the context help for
        * the associated system action (if any).
        * Note that only help IDs will work, not URLs.
        * @return the context help, or <code>null</code>
        */
        public HelpCtx getHelpCtx (int index);

        /** Perform the action on the specific index
        * @index of the submenu item which should be performed
        */
        public void performActionAt(int index);

        /** Adds change listener for changes of the model.
        */
        public void addChangeListener (ChangeListener l);

        /** Removes change listener for changes of the model.
        */
        public void removeChangeListener (ChangeListener l);

    }

    /** SubMenu provides easy way of displaying submenu items based on
    * SubMenuModel.
    */
    public static class SubMenu extends org.openide.awt.JMenuPlus {
        /** number of previous sub items */
        int previousCount = -1;
        /** listener to remove from this menu or <CODE>null</CODE> */
        ActionListener oneItemListener;

        /** Constructs a new ActMenuItem with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * No icon is used by default.
        * @param action the action to which this menu item should be connected
        * @param label a string label for the check box menu item,
        *              or null for an unlabeled menu item.
        * @param support the support for the menu items
        */
        public SubMenu(SystemAction aAction, SubMenuModel model) {
            this (aAction, model, true);
        }

        static final long serialVersionUID =-4446966671302959091L;
        /** Constructs a new ActMenuItem with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * No icon is used by default.
        * @param action the action to which this menu item should be connected
        * @param label a string label for the check box menu item,
        *              or null for an unlabeled menu item.
        * @param support the support for the menu items
        */
        public SubMenu(SystemAction aAction, SubMenuModel model, boolean popup) {
            new SubMenuBridge (this, aAction, model, popup).updateState (null);
        }

        /** Request for either MenuUI or MenuItemUI if the only one subitem should not
        * use submenu.
        */
        public String getUIClassID () {
            if (previousCount == 0) {
                return "MenuItemUI"; // NOI18N
            }
            return previousCount == 1 ? "MenuItemUI" : "MenuUI"; // NOI18N
        }


        public void menuSelectionChanged(boolean isIncluded) {
            if (previousCount == 1)
                setArmed(isIncluded); // JMenuItem behaviour
            else
                super.menuSelectionChanged(isIncluded);
        }

        /** Menu cannot be selected when it represents MenuItem.
        */
        public void setSelected (boolean s) {
            // disabled menu cannot be selected
            if (isEnabled () || !s) {
                super.setSelected (s);
            }
        }

        /** Seting menu to disabled also sets the item as not selected
        */
        public void setEnabled (boolean e) {
            super.setEnabled (e);
            if (!e) {
                super.setSelected (false);
            }

        }

        public void doClick(int pressTime) {
            if (!isEnabled ()) {
                // do nothing if not enabled
                return;
            }

            if (oneItemListener != null) {
                oneItemListener.actionPerformed (null);
            } else {
                super.doClick (pressTime);
            }
        }
    }

}


/*
* Log
*  33   src-jtulach1.32        1/20/00  Libor Kramolis  
*  32   src-jtulach1.31        1/18/00  Libor Kramolis  
*  31   src-jtulach1.30        1/13/00  Ian Formanek    NOI18N
*  30   src-jtulach1.29        1/13/00  Ian Formanek    I18N
*  29   src-jtulach1.28        1/12/00  Ian Formanek    NOI18N
*  28   src-jtulach1.27        1/7/00   Ian Formanek    Accelerator key displayed
*       on popup menus as well
*  27   src-jtulach1.26        1/6/00   Jesse Glick     #2279 -- context help for
*       submenu presenters with only one item in the model.
*  26   src-jtulach1.25        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  25   src-jtulach1.24        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  24   src-jtulach1.23        8/17/99  Ian Formanek    Generated serial version 
*       UID
*  23   src-jtulach1.22        8/13/99  Jaroslav Tulach New Main Explorer
*  22   src-jtulach1.21        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  21   src-jtulach1.20        8/5/99   Jaroslav Tulach Tools & New action in 
*       editor.
*  20   src-jtulach1.19        7/16/99  Jesse Glick     Actions.SubMenuModel now 
*       has context help.
*  19   src-jtulach1.18        7/12/99  Jesse Glick     Context help.
*  18   src-jtulach1.17        6/28/99  Ian Formanek    NbJMenu renamed to 
*       JMenuPlus
*  17   src-jtulach1.16        6/28/99  Ian Formanek    Fixed bug 2043 - It is 
*       virtually impossible to choose lower items of New From Template  from 
*       popup menu on 1024x768
*  16   src-jtulach1.15        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  15   src-jtulach1.14        5/18/99  Ian Formanek    Undone last change as it 
*       caused popup menus to be weird...
*  14   src-jtulach1.13        5/17/99  Miloslav Metelka invokeLater() in all 
*       updateStatus()
*  13   src-jtulach1.12        5/5/99   Jaroslav Tulach Works with 122
*  12   src-jtulach1.11        4/1/99   David Simonek   separators added to 
*       docking action
*  11   src-jtulach1.10        3/26/99  Jesse Glick     BooleanStateAction.PROP_BOOLEAN_STATE
*        is now public.
*  10   src-jtulach1.9         3/21/99  Jaroslav Tulach Keys.
*  9    src-jtulach1.8         3/17/99  Ian Formanek    Fixed BooleanStateAction 
*       behavior
*  8    src-jtulach1.7         3/11/99  Jaroslav Tulach SubMenu regenerates menu 
*       on addNotify.
*  7    src-jtulach1.6         3/9/99   Jaroslav Tulach Node actions releases 
*       sometimes its listeners.
*  6    src-jtulach1.5         3/4/99   David Simonek   
*  5    src-jtulach1.4         3/4/99   Jaroslav Tulach Keymap change is fired by
*       TopManager
*  4    src-jtulach1.3         3/2/99   Jaroslav Tulach Icon changes
*  3    src-jtulach1.2         3/2/99   Jaroslav Tulach 
*  2    src-jtulach1.1         3/2/99   Jaroslav Tulach 
*  1    src-jtulach1.0         3/1/99   Jaroslav Tulach 
* $
*/
