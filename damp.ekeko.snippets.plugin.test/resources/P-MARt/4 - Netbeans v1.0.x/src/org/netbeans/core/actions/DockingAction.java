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

import java.io.Serializable;
import java.awt.MenuShortcut;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.event.*;
import javax.swing.JMenuItem;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WorkspaceImpl;
import org.netbeans.core.windows.WindowManagerImpl;

import org.openide.TopManager;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.awt.*;

/** Dock the top component to the mode.
*
* @author Dafe Simonek
*/
public final class DockingAction extends CallableSystemAction {

    /** Implementation of ActSubMenuInt */
    private static ActSubMenuModel model;

    /* constant special items in docking menu */
    static final SpecialItem NEW_SINGLE_ITEM = new SpecialItem(
                NbBundle.getBundle(DockingAction.class).getString("CTL_NewSingle"),
                ModeImpl.SINGLE
            );
    static final SpecialItem NEW_MULTI_TAB_ITEM = new SpecialItem(
                NbBundle.getBundle(DockingAction.class).getString("CTL_NewMultiTab"),
                ModeImpl.MULTI_TAB
            );
    /** name of new multi tab mode when docking to it */
    static final String newMultiTabName =
        NbBundle.getBundle(DockingAction.class).getString("CTL_NewMultiTabName");
    /** asociation with window manager implementation */
    static WindowManagerImpl wm =
        (WindowManagerImpl)TopManager.getDefault().getWindowManager();

    /* Constructs a new docking action */
    static final long serialVersionUID =8679037289261745865L;
    public DockingAction() {
    }

    /* Returns resource string pointing to actions' icon.
    * @return resource string of actions' icon
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/actions/dock.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (DockingAction.class);
    }

    /* @return Returns localized name of this action */
    public String getName() {
        return NbBundle.getBundle(DockingAction.class).getString("Docking");
    }

    /* Returns a submenu that will presents this action in menu bar
    * @return the JMenuItem representation for this action (which is submenu)
    */
    public JMenuItem getMenuPresenter() {
        return new SpecialSubMenu(this, getModel(), false);
    }

    /* Returns a submneu that will present this action in a PopupMenu.
    * @return the JMenuItem representation for this action
    */
    public JMenuItem getPopupPresenter() {
        return new SpecialSubMenu(this, getModel(), true);
    }

    /* Actually performs the "docking" action by setting the mode
    * of how component will be displayed
    */
    public void performAction () {
        // all functionality is accomplished by
        // ActSubMenuModel.performActionAt(int)
    }

    /** Convenience method for obtaining submenu model */
    static ActSubMenuModel getModel () {
        if (model == null)
            model = new ActSubMenuModel();
        return model;
    }

    /** SubMenu of this action - notifies data model about
    * ancestor changes (addNotify, removeNotify)
    */
    private static final class SpecialSubMenu extends Actions.SubMenu {

        static final long serialVersionUID =-3808638464776612755L;
        SpecialSubMenu (SystemAction aAction, Actions.SubMenuModel model,
                        boolean popup) {
            super(aAction, model, popup);
        }

        /** Notifies data model in adition to normal behaviour */
        public void addNotify () {
            DockingAction.model.addNotify();
            super.addNotify();
        }

        /** Notifies data model in adition to normal behaviour */
        public void removeNotify () {
            DockingAction.model.removeNotify();
            super.removeNotify();
        }
    } // end of SpecialSubMenu

    /** Implementation of SubMenuModel.
    */
    static final class ActSubMenuModel implements Actions.SubMenuModel,
        Serializable {
        /** Caches a list of allowed modes for currently activated top comp 
         * @associates SpecialItem*/
        transient List modeList;
        /** Cached reference to the currently activated top component */
        transient TopComponent comp;


        static final long serialVersionUID =1811068881824983422L;
        public int getCount() {
            return modeList.size();
        }

        public String getLabel(int index) {
            Object found = modeList.get(index);
            if (found instanceof SpecialItem)
                return ((SpecialItem)found).name;
            if (found instanceof Mode)
                return ((Mode)found).getDisplayName();
            return null;
        }

        public HelpCtx getHelpCtx (int index) {
            return null;
        }

        public MenuShortcut getMenuShortcut(int index) {
            return null;
        }

        /** Actually performs the "docking" action by setting the mode
        * of how component will be displayed
        */
        public void performActionAt(int index) {
            //System.out.println ("Performing action..."); // NOI18N
            Object found = modeList.get(index);
            if (found instanceof Mode) {
                ((Mode)found).dockInto(comp);
                comp.requestFocus();
            }
            // special items
            if (found instanceof SpecialItem) {
                WorkspaceImpl curWorkspace =
                    (WorkspaceImpl)wm.getCurrentWorkspace();
                // dock into specially created modes
                Mode mode = null;
                String modeName = null;
                switch (((SpecialItem)found).type) {
                case ModeImpl.SINGLE:
                    modeName = wm.findUnusedModeName(comp.getName(),
                                                     curWorkspace);
                    mode = curWorkspace.createMode(
                               modeName, modeName, null,
                               ModeImpl.SINGLE, true
                           );
                    break;
                case ModeImpl.MULTI_TAB:
                    modeName = wm.findUnusedModeName(newMultiTabName,
                                                     curWorkspace);
                    mode = curWorkspace.createMode(
                               modeName, modeName, null, // PENDING - what to use ???
                               ModeImpl.MULTI_TAB, true
                           );
                    break;
                }
                mode.dockInto(comp);
            }
            // we can clear 'cause we know addNotify must be called
            // before successive performActionAt
            modeList = null;
            comp = null;
        }

        /** Adds change listener for changes of the model.
        */
        public void addChangeListener (ChangeListener l) {
        }

        /** Removes change listener for changes of the model.
        */
        public void removeChangeListener (ChangeListener l) {
        }

        /** Called when a menu becomes visible.
        * Loads the information about allowed modes of
        * currently active top component
        */
        void addNotify () {
            //System.out.println ("Add notify..."); // NOI18N
            comp = TopComponent.getRegistry ().getActivated ();
            if (comp == null) {
                modeList = new ArrayList(0);
                return;
            }
            Set modes = TopManager.getDefault().getWindowManager().
                        getCurrentWorkspace().getModes();
            int count = modes.size();
            // obtain mode active top component is in
            Workspace curWorkspace =
                TopManager.getDefault().getWindowManager().getCurrentWorkspace();
            Mode compMode = curWorkspace.findMode(comp);
            // create mode list
            modeList = new ArrayList(count + 3);
            ArrayList userDefined = new ArrayList(count);
            Mode curMode;
            for (Iterator iter = modes.iterator(); iter.hasNext(); ) {
                curMode = (Mode)iter.next();
                if (curMode.canDock(comp) && (!curMode.equals(compMode))) {
                    if (((ModeImpl)curMode).isUserDefined()) {
                        // exclude used defined orphans
                        if (!((ModeImpl)curMode).isOrphan())
                            userDefined.add(curMode);
                    } else {
                        modeList.add(curMode);
                    }
                }
            }
            // complete the modes and add separators (nulls)
            modeList.add(null);
            if (userDefined.size() > 0) {
                boolean someAdded = false;
                modeList.addAll(userDefined);
                modeList.add(null);
            }
            if (!((ModeImpl)compMode).isSingle()) {
                // allow docking to single only if in multi
                modeList.add(NEW_SINGLE_ITEM);
            }
            // new multi mode
            modeList.add(NEW_MULTI_TAB_ITEM);
        }

        /** Called when a presenter becomes invisible.
        * Clears cached data.
        */
        void removeNotify () {
            //System.out.println ("Remove notify..."); // NOI18N
        }

    } // end of ActSubMenuModel

    /** Simple structure to keep info about special items
    * in docking menu */
    static final class SpecialItem {
        public String name;
        public int type;

        SpecialItem (String name, int type) {
            this.name = name;
            this.type = type;
        }
    }

}

/*
* Log
*  8    Gandalf   1.7         1/13/00  Jaroslav Tulach I18N
*  7    Gandalf   1.6         1/12/00  Ales Novak      i18n
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  4    Gandalf   1.3         7/29/99  David Simonek   further ws serialization 
*       changes
*  3    Gandalf   1.2         7/28/99  David Simonek   workspace serialization 
*       bugfixes
*  2    Gandalf   1.1         7/21/99  David Simonek   window system updates...
*  1    Gandalf   1.0         7/20/99  David Simonek   
* $
*/
