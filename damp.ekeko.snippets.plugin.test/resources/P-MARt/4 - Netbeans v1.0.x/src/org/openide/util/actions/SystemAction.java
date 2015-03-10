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

package org.openide.util.actions;

import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.MessageFormat;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.*;

import org.openide.util.SharedClassObject;
import org.openide.util.HelpCtx;

import org.openide.TopManager;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.NbBundle;
import org.openide.awt.JPopupMenuPlus;

/* not relevant here --jglick
* The actions system allows connection between action
* "invokers" and an action "performer", where in some cases the
* performer of the action can be the action class itself, while in
* other cases it can be a class that implements the ActionPerformer
* interface and is registered at the action via setActionPerformer.
*/
/**
* The common predecessor of callable actions in the IDE.
* <P>
* Also implements the Swing {@link Action} to enable use
* with the Swing action model.
* <p>An action class is a <em>singleton</em>, i.e. should generally contain no instance state.
* Rather, subclassing and use of abstract protected methods should be used
* to create variants of the action.
* <p>While it is possible to subclass this class directly--for example, if your "action"
* is really a placeholder for a popup menu that shows other actions--most people will
* prefer to use one of the subclasses, which are more convenient.
*
* @author   Ian Formanek, Jaroslav Tulach
*/
public abstract class SystemAction extends SharedClassObject
    implements Action {
    /** Name of property indicating whether or not the action is enabled. */
    public static final String PROP_ENABLED = "enabled"; // NOI18N
    /** Name of property for the action's display icon. */
    public static final String PROP_ICON = "icon"; // NOI18N

    /** serialVersionUID */
    static final long serialVersionUID = 7131978192935797459L;

    /** Obtain a singleton instance of the action with a specified class.
    * If there already is a instance then it is returned, otherwise
    * a new one is created.
    *
    * @param actionClass the class of the action to find
    * @return the singleton action instance
    * @exception ClassCastException if the class is not <code>SystemAction</code>
    * @exception IllegalArgumentException if the instance cannot be created
    */
    public static SystemAction get (Class actionClass) {
        return (SystemAction)findObject (actionClass, true);
    }

    /** Get a human presentable name of the action.
    * This may be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public abstract String getName ();

    /** Get a help context for the action.
    * @return the help context for this action
    */
    public abstract HelpCtx getHelpCtx ();

    /** Test whether the action is currently enabled.
    * @return <code>true</code> if so
    */
    public boolean isEnabled() {
        return getProperty (PROP_ENABLED).equals (Boolean.TRUE);
    }

    /** Set whether the action should be enabled.
    * @param value <code>true</code> to enable it
    */
    public void setEnabled(boolean value) {
        putProperty (PROP_ENABLED, value ? Boolean.TRUE : Boolean.FALSE, true);
    }

    /** Set a property in the singleton. This property is common for all instances
    * of the same class.
    *
    * @param name the name of the property
    * @param value the value
    */
    public final void putValue (String name, Object value) {
        putProperty (name, value, true);
        // Could handle putValue (SMALL_ICON, ImageIcon icon) but not
        // really that important.
    }

    /** Get a property in the singleton. Values are shared among all instances of the same class.
    * The special tokens {@link Action#NAME} and {@link Action#SMALL_ICON} are also recognized
    * and delegated to {@link #getName} and {@link #getIcon}, resp.
    * @param name the name of the property
    * @return the value
    */
    public final Object getValue (String name) {
        Object val = getProperty (name);
        if (val == null) {
            if (NAME.equals (name))
                val = getName ();
            else if (SMALL_ICON.equals (name))
                val = getIcon ();
        }
        return val;
    }

    /** Actually perform the action.
    * Specified in {@link java.awt.event.ActionListener#actionPerformed}.
    * <p>In some cases, the implementation may have an empty body,
    * if the presenters handle the performing of the action in a different way
    * than by calling this method.
    * @param ev the event triggering the action
    */
    public abstract void actionPerformed (java.awt.event.ActionEvent ev);


    /** Initialize the action.
    * The default implementation just enabled it.
    */
    protected void initialize () {
        putProperty (PROP_ENABLED, Boolean.TRUE);

        super.initialize ();
    }

    /** Indicate whether action state should be cleared after the last action of this class is deleted.
    * @return <code>false</code> in the default implementation
    */
    protected boolean clearSharedData () {
        return false;
    }

    /** Set the action's display icon.
    * @param icon the icon
    */
    public final void setIcon (ImageIcon icon) {
        putProperty (PROP_ICON, icon, true);
    }

    /** Get the action's display icon.
    * @return the icon
    */
    public final ImageIcon getIcon () {
        synchronized (getLock ()) {
            ImageIcon img = (ImageIcon)getProperty (PROP_ICON);
            if (img == null) {
                // create the icon from the resource
                final String resName = iconResource ();

                URL url = resName == null ? null : getClass ().getResource (resName);
                if (url == null) {
                    //        TopManager.getDefault ().notify (new Message ("Icon \"" + iconResource () + "\" not found"); // NOI18N
                    //        System.out.println ("Icon for " + getClass ().getName () + " with resource \"" + iconResource () + "\" not found" // NOI18N
                    throw new IllegalStateException (MessageFormat.format(NbBundle.getBundle(SystemAction.class).getString("MSG_FMT_IconNotFound"), new Object [] {getClass ().getName (), iconResource () }));
                }
                img = new FixedIcon (url);

                putProperty (PROP_ICON, img);
            }
            return img;
        }
    }

    /** Specify the proper resource name for the action's icon.
    * Should be overridden by subclasses.
    * Typically this should be a 16x16 color GIF.
    * @return the resource name for the icon, e.g. <code>/com/mycom/mymodule/myIcon.gif</code>
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/empty.gif"; // NOI18N
    }


    /** Create the default toolbar representation of an array of actions.
    * Null items in the array will add a separator to the toolbar.
    *
    * @param actions actions to show in the generated toolbar
    * @return a toolbar instance displaying them
    */
    public static JToolBar createToolbarPresenter (SystemAction[] actions) {
        JToolBar p = new JToolBar ();
        int i, k = actions.length;
        for (i = 0; i < k; i++) {
            if (actions [i] == null)
                p.addSeparator();
            else
                if (actions [i] instanceof Presenter.Toolbar)
                    p.add (((Presenter.Toolbar)actions [i]).getToolbarPresenter ());
        }
        return p;
    }

    /** Concatenate two arrays of actions.
    * @param actions1 first array of actions to link
    * @param actions1 second array of actions to link
    * @return an array of both sets of actions in the same order
    */
    public static SystemAction[] linkActions (SystemAction[] actions1, SystemAction[] actions2) {
        List l = new Vector (Arrays.asList (actions1));
        l.addAll (Arrays.asList (actions2));
        return (SystemAction[]) l.toArray (actions1);
    }

    /** Create the default popup menu representation of an array of actions.
    * @param actions actions to show in the generated menu
    * @return a popup menu displaying them
    */
    public static JPopupMenu createPopupMenu(SystemAction []actions) {
        JPopupMenu popupMenu = new JPopupMenuPlus();
        JMenuItem item;

        for(int i = 0; i < actions.length; i++) {
            if (actions[i] == null) {
                popupMenu.addSeparator ();
                continue;
            }
            if (actions[i] instanceof Presenter.Popup) {
                item = ((Presenter.Popup)actions[i]).getPopupPresenter ();
            } else {
                item = new JMenuItem (actions[i].getName ());
                item.setEnabled(false);
            }
            popupMenu.add (item);
        }

        return popupMenu;

    }


    /** Icon with fixed size 16x16.
    */
    private static final class FixedIcon extends ImageIcon {
        public FixedIcon (java.net.URL u) {
            super (u);
        }

        /** Does not wait for the icon because we know the size.
        */
        protected void loadImage (java.awt.Image i) {
        }

        /**
         * Draw the icon at the specified location.  Icon implementations
         * may use the Component argument to get properties useful for 
         * painting, e.g. the foreground or background color.
         */
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            // waits till the icon is ok.
            super.loadImage (getImage ());
            super.paintIcon (c, g, x, y);
        }

        /**
         * Returns the icon's width.
         *
         * @return an int specifying the fixed width of the icon.
         */
        public int getIconWidth() {
            return 16;
        }

        /**
         * Returns the icon's height.
         *
         * @return an int specifying the fixed height of the icon.
         */
        public int getIconHeight() {
            return 16;
        }
    }
}


/*
 * Log
 *  4    Tuborg    1.3         07/29/98 Jaroslav Tulach Does not allow shared data
 *                                                      to be cleared from memory.
 *  3    Tuborg    1.2         07/29/98 Jaroslav Tulach Removed internal field
 *                                                      because of quick
 *                                                      initialization.
 *
 *  2    Tuborg    1.1         06/15/98 Ian Formanek
 *  1    Tuborg    1.0         06/11/98 David Peroutka
 * $
 * Beta Change History:
 *  0    Tuborg    0.33        --/--/98 Jaroslav Tulach Removed performAction (we have talk about it and in
 *  0    Tuborg    0.33        --/--/98 Jaroslav Tulach BooleanStateAction it is missing) and added firePropertyChange event
 *  0    Tuborg    0.34        --/--/98 Jaroslav Tulach Import of org.openide.util.ActionDescription
 *  0    Tuborg    0.35        --/--/98 Jaroslav Tulach Minimal type
 */
