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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.beans.*;
import javax.swing.ImageIcon;
import javax.swing.event.*;

import org.openide.util.datatransfer.*;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.windows.TopComponent;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.awt.*;
import org.openide.util.UserCancelException;

/** Paste from clipboard.
*
* @author   Petr Hamernik, Jan Jancura
* @version  0.21, Apr 19, 1998
*/
public final class PasteAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6620328110138256516L;
    /** Imlementation of ActSubMenuInt */
    private static ActSubMenuModel model = new ActSubMenuModel();

    /** All currently possible paste types. */
    private static PasteType[] types;

    /** Selected paste type which will be performed when performAction is called.
    * It can be null if no types are given. */
    private static PasteType type;

    /** does this action survive change of focus? */
    private static FocusL focusListener;

    /* Overrides superclass initialization */
    protected void initialize () {
        super.initialize();
        // add it from the list of top listeners
        focusListener = new FocusL();
        TopComponent.getRegistry().addPropertyChangeListener (focusListener);
        setEnabled (false);
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("Paste");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (PasteAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/paste.gif"; // NOI18N
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a MenuBar.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getMenuPresenter() {
        return new Actions.SubMenu(this, model, false);
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a PopupMenu.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getPopupPresenter() {
        return new Actions.SubMenu(this, model, true);
    }

    public void performAction() {
        PasteType t = type;
        if (t == null) return;

        try {
            Transferable trans = t.paste();
            Clipboard clipboard = TopManager.getDefault().getClipboard();


            if (trans != null) {
                ClipboardOwner owner = trans instanceof ClipboardOwner ?
                                       (ClipboardOwner)trans
                                       :
                                       new StringSelection (""); // NOI18N
                clipboard.setContents(trans, owner);
            }
        } catch (UserCancelException exc) {
            // ignore - user just pressed cancel in some dialog....
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault().notify(
		new NotifyDescriptor.Exception(e));
        }
    }

    /** Set possible paste types.
    * Automatically enables or disables the paste action according to whether there are any.
    * @param types the new types to allow, or <code>null</code>
    */
    public void setPasteTypes(PasteType[] types) {
        this.types = types;
        if ((types == null) || (types.length == 0)) {
            setEnabled(false);
            type = null;
        }
        else {
            setEnabled(true);
            type = types[0];
        }
        model.fireStateChanged ();
    }

    /** Get all paste types.
     * @return all possible paste types, or <code>null</code> */
    public PasteType[] getPasteTypes() {
        return types;
    }

    /** @return count of all possible paste types. */
    int getPasteTypesCount() {
        return types == null ? 0 : types.length;
    }

    /** Sets selected paste type to paste type with the given index */
    void setPasteTypeIndex(int index) {
        if (types == null)
            return;
        if (index < 0)
            index = 0;
        if (index >= types.length)
            index = types.length - 1;
        type = types[index];
    }

    /** Implementation of ActSubMenuInt */
    private static class ActSubMenuModel extends EventListenerList implements Actions.SubMenuModel {
        public int getCount() {
            PasteAction a = (PasteAction)findObject (PasteAction.class);
            return a == null ? 0 : a.getPasteTypesCount();
        }

        public String getLabel(int index) {
            PasteAction a = (PasteAction)findObject (PasteAction.class);
            if (a == null) return null;
            PasteType[] t = a.getPasteTypes ();
            return t == null ? null : t[index].getName ();
        }

        public HelpCtx getHelpCtx (int index) {
            PasteAction a = (PasteAction)findObject (PasteAction.class);
            if (a == null) return null;
            PasteType[] t = a.getPasteTypes ();
            return t == null ? null : t[index].getHelpCtx ();
        }

        public MenuShortcut getMenuShortcut(int index) {
            return null;
        }

        public void performActionAt(int index) {
            PasteAction a = (PasteAction)findObject (PasteAction.class);
            if (a != null) {
                a.setPasteTypeIndex(index);
                a.performAction();
            }
        }


        /** Registers .ChangeListener to receive events.
         *@param listener The listener to register.
         */
        public synchronized void addChangeListener(javax.swing.event.ChangeListener listener) {
            add (javax.swing.event.ChangeListener.class, listener);
        }
        /** Removes .ChangeListener from the list of listeners.
         *@param listener The listener to remove.
         */
        public synchronized void removeChangeListener(javax.swing.event.ChangeListener listener) {
            remove (javax.swing.event.ChangeListener.class, listener);
        }
        /** Notifies all registered listeners about the event.
         *
         *@param param1 Parameter #1 of the <CODE>.ChangeEvent<CODE> constructor.
         */
        protected void fireStateChanged() {
            Object[] listeners = getListenerList ();
            if (listeners.length == 0) {
                return;
            }
            javax.swing.event.ChangeEvent e = new javax.swing.event.ChangeEvent (
                                                  this
                                              );

            for (int i = listeners.length-1; i>=0; i-=2) {
                ((javax.swing.event.ChangeListener)listeners[i]).stateChanged (e);
            }
        }
    }

    /** Listener for survive focus change */
    private static class FocusL implements PropertyChangeListener {
        /** Called when a top window lost its focus.
        * @param ev event describing the situation
        */
        public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getPropertyName().equals(TopComponent.Registry.PROP_ACTIVATED)) {
                PasteAction a = (PasteAction)findObject (PasteAction.class);
                if (a != null) {
                    a.setPasteTypes(null);
                }
            }
        }
    }

}

/*
 * Log
 *  24   Gandalf   1.23        1/12/00  Ian Formanek    NOI18N
 *  23   Gandalf   1.22        12/21/99 Jaroslav Tulach Updates presenters when 
 *       paste types are changed when presenters are already shown.
 *  22   Gandalf   1.21        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  21   Gandalf   1.20        7/16/99  Jesse Glick     Actions.SubMenuModel.getHelpCtx
 *       
 *  20   Gandalf   1.19        7/11/99  David Simonek   window system change...
 *  19   Gandalf   1.18        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  18   Gandalf   1.17        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  17   Gandalf   1.16        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  16   Gandalf   1.15        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  15   Gandalf   1.14        5/2/99   Ian Formanek    Fixed last change
 *  14   Gandalf   1.13        5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  13   Gandalf   1.12        4/8/99   David Simonek   action now don't throw 
 *       an exception when user presses cancel in some  paste dialog
 *  12   Gandalf   1.11        3/26/99  Jesse Glick     [JavaDoc]
 *  11   Gandalf   1.10        3/19/99  Jaroslav Tulach 
 *  10   Gandalf   1.9         3/15/99  Ian Formanek    Fixed enabling of 
 *       actions in initialize ()
 *  9    Gandalf   1.8         3/2/99   Jaroslav Tulach Icon changes
 *  8    Gandalf   1.7         3/1/99   Jaroslav Tulach Changed actions 
 *       presenters.
 *  7    Gandalf   1.6         2/25/99  Jaroslav Tulach Change of clipboard 
 *       management  
 *  6    Gandalf   1.5         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  5    Gandalf   1.4         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  4    Gandalf   1.3         2/11/99  Ian Formanek    Last change undone
 *  3    Gandalf   1.2         2/11/99  Ian Formanek    getXXXPresenter -> 
 *       createXXXPresenter (XXX={Menu, Toolbar})
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
