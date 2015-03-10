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

import java.awt.*;
import java.util.Vector;
import java.util.Set;
import java.util.HashMap;
import javax.swing.*;
import java.beans.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.openide.TopManager;
import org.openide.windows.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.awt.JMenuPlus;

import org.netbeans.core.windows.WorkspaceImpl;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;

/** This class represents a list of all opened windows in the Corona system
* as a menu and as a PanContext.
*
* @author   Jan Jancura
*/
public final class OpenedWindowsAction extends SystemAction
    implements Presenter.Menu {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -145456518535036783L;

    /** Human presentable name */
    private String name;

    private static JMenu mainItem;

    /** listener for main windows menu */
    private static MainItemListener mainItemListener;
    /** listener for workspace menus */
    private static MenuListener workspaceMenuListener;
    /** listener for mode items */
    private static ActionListener modeItemListener;
    /** currently selected workspace in workspace submenu */
    private static Workspace chosenWorkspace;
    /** default icon for mode menu items */
    private static Image defaultModeIcon;

    /** Creates with default name */
    public OpenedWindowsAction () {
        this (null);
    }

    /** Creates with specified name */
    public OpenedWindowsAction (final String aName) {
        name = aName;
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        if (name == null)
            name = NbBundle.getBundle(OpenedWindowsAction.class).getString("OpenedWindows");
        return name;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (OpenedWindowsAction.class);
    }

    /**
    * Returns menu item which representates this submenu.
    *
    * @return <CODE>JMenuItem</CODE> Submenu representated with this context.
    */
    public JMenuItem getMenuPresenter () {
        if (mainItem != null) {
            return mainItem;
        }
        mainItem = new JMenuPlus(getName());
        mainItem.setIcon (SystemAction.get (OpenedWindowsAction.class).getIcon ());
        HelpCtx.setHelpIDString (mainItem, OpenedWindowsAction.class.getName ());
        mainItem.addMenuListener(mainItemListener());
        return mainItem;
    }

    /** Do nothing. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {}

    /** accessor for mode item listener */
    private static ActionListener modeItemListener () {
        if (modeItemListener == null) {
            modeItemListener = new ModeActionListener();
        }
        return modeItemListener;
    }

    /** accessor for workspace menu listener */
    private static MenuListener workspaceMenuListener () {
        if (workspaceMenuListener == null) {
            workspaceMenuListener = new WorkspaceMenuListener();
        }
        return workspaceMenuListener;
    }

    /** accessor for main 'windows' menu item listener */
    private static MainItemListener mainItemListener () {
        if (mainItemListener == null) {
            mainItemListener = new MainItemListener();
        }
        return mainItemListener;
    }

    /** accessor for default icon of the mode */
    private static Image defaultModeIcon () {
        if (defaultModeIcon == null) {
            defaultModeIcon = Toolkit.getDefaultToolkit().getImage(
                                  OpenedWindowsAction.class.getResource(
                                      "/org/netbeans/core/resources/frames/default.gif" // NOI18N
                                  )
                              );
        }
        return defaultModeIcon;
    }

    // innerclasses .......................................................

    /** Listens to selecting of main item and expands it to the
    * submenu of workspaces */
    private static final class MainItemListener implements MenuListener {

        /** Mapping between menu instances and
        * programmatic names of the workspaces */
        HashMap menus2names;

        public void menuCanceled (MenuEvent e) {
            //      System.out.println("main canceled..."); // NOI18N
        }

        public void menuDeselected (MenuEvent e) {
            JMenu menu = (JMenu)e.getSource();
            menu.removeAll();
        }

        public void menuSelected (MenuEvent e) {
            JMenu menu = (JMenu)e.getSource();
            JMenu curMenu = null;
            // obtain workspaces and create a submenu containing them
            Workspace[] workspaces =
                TopManager.getDefault().getWindowManager().getWorkspaces();
            menus2names = new HashMap(workspaces.length * 2);
            for (int i = 0; i < workspaces.length; i++) {
                // bugfix #6116 - change from getName() to getDisplayName()
                curMenu = new JMenuPlus(workspaces[i].getDisplayName());
                // store mapping between menu item and the name of the workspace
                menus2names.put(curMenu, workspaces[i].getName());
                HelpCtx.setHelpIDString (curMenu, OpenedWindowsAction.class.getName ());
                curMenu.addMenuListener(workspaceMenuListener());
                menu.add(curMenu);
            }
        }

    } // end of MainItemListener inner class

    /** Listens to selecting of workspace items and expands to the
    * submenu of modes of selected workspace. */
    private static final class WorkspaceMenuListener implements MenuListener {

        public void menuCanceled (MenuEvent e) {
            //      System.out.println("workspace cancelled..."); // NOI18N
        }

        public void menuDeselected (MenuEvent e) {
            JMenu menu = (JMenu)e.getSource();
            menu.removeAll();
        }

        public void menuSelected (MenuEvent e) {
            JMenu menu = (JMenu)e.getSource();
            String workspaceName = (String)mainItemListener().menus2names.get(menu);
            chosenWorkspace = TopManager.getDefault().getWindowManager().
                              findWorkspace(workspaceName);
            JMenuItem curItem = null;
            Image curIcon = null;
            // obtain modes and create a submenu containing them
            ModeImpl[] modes =
                (ModeImpl[])chosenWorkspace.getModes().toArray(new ModeImpl[0]);
            for (int i = 0; i < modes.length; i++) {
                if (!modes[i].isOrphan()) {
                    curItem = new JMenuItem(modes[i].getDisplayName());
                    HelpCtx.setHelpIDString (curItem, OpenedWindowsAction.class.getName ());
                    curItem.setName(modes[i].getName());
                    curIcon = modes[i].getIcon();
                    curItem.setIcon(
                        new ImageIcon(curIcon == null ? defaultModeIcon() : curIcon)
                    );
                    curItem.addActionListener(modeItemListener());
                    menu.add(curItem);
                }
            }
        }

    } // end of MainItemListener inner class

    /** Listens to actions on mode menu items and opens a mode on current
    * workspace on action performed */
    private static final class ModeActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JMenuItem item = (JMenuItem)evt.getSource();
            WindowManagerImpl wm =
                (WindowManagerImpl)TopManager.getDefault().getWindowManager();
            WorkspaceImpl curWs = (WorkspaceImpl)wm.getCurrentWorkspace();
            ModeImpl curMode = (ModeImpl)curWs.findMode(item.getName());
            if (!chosenWorkspace.equals(curWs)) {
                // copy the mode if doesn't exist on current workspace
                Mode origMode = chosenWorkspace.findMode(item.getName());
                if (curMode == null) {
                    curMode = new ModeImpl(curWs, (ModeImpl)origMode);
                }
                curWs.addMode(curMode);
                // dock and open all as in chosen workspace
                TopComponent[] tcs = origMode.getTopComponents();
                for (int i = 0; i < tcs.length; i++) {
                    curMode.dockInto(tcs[i]);
                    if (((WorkspaceImpl)chosenWorkspace).isOpened(tcs[i])) {
                        tcs[i].open(curWs);
                    }
                }
            }
            curMode.requestFocus();
        }
    } // end of ModeActionListener

}

/*
 * Log
 *  27   Gandalf-post-FCS1.24.1.1    4/6/00   David Simonek   bugfix #6129
 *  26   Gandalf-post-FCS1.24.1.0    4/4/00   David Simonek   bugfix #6116
 *  25   Gandalf   1.24        1/13/00  Jaroslav Tulach I18N
 *  24   Gandalf   1.23        1/12/00  Ales Novak      i18n
 *  23   Gandalf   1.22        12/20/99 Ian Formanek    Fixed icon in menu
 *  22   Gandalf   1.21        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  21   Gandalf   1.20        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  20   Gandalf   1.19        10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  19   Gandalf   1.18        9/13/99  David Simonek   action totally changed, 
 *       now supports workspaces submenu 
 *  18   Gandalf   1.17        7/31/99  David Simonek   now react to the click 
 *       as expected
 *  17   Gandalf   1.16        7/11/99  David Simonek   window system change...
 *  16   Gandalf   1.15        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  15   Gandalf   1.14        6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  14   Gandalf   1.13        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  13   Gandalf   1.12        5/15/99  David Simonek   improving serialization 
 *       to allow component to resolve to null
 *  12   Gandalf   1.11        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  11   Gandalf   1.10        3/26/99  Jesse Glick     SystemAction.actionPerformed(ActionEvent)
 *        is now abstract; you must explicitly provide an empty body if that is 
 *       desired.
 *  10   Gandalf   1.9         3/14/99  David Simonek   
 *  9    Gandalf   1.8         3/12/99  David Simonek   
 *  8    Gandalf   1.7         3/2/99   David Simonek   
 *  7    Gandalf   1.6         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  6    Gandalf   1.5         2/11/99  Ian Formanek    Last change undone
 *  5    Gandalf   1.4         2/11/99  Ian Formanek    getXXXPresenter -> 
 *       createXXXPresenter (XXX={Menu, Toolbar})
 *  4    Gandalf   1.3         1/21/99  David Simonek   Removed references to 
 *       "Actions" class
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    action name localization
 *  0    Tuborg    0.17        --/--/98 Jan Formanek    WARNING: Repaired to compile with Swing 1.0
 */
