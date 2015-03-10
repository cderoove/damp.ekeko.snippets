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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Arrays;
import java.awt.event.ActionListener;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openide.TopManager;
import org.openide.windows.Workspace;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/** Switch to a different workspace.
* @see Workspace#activate
* @author Ales Novak
*/
public class WorkspaceSwitchAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 8703562697574423965L;

    /** JMenu of the action */
    private transient JMenu menu;

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(WorkspaceSwitchAction.class).getString("WorkspacesItems");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (WorkspaceSwitchAction.class);
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a MenuBar.
    * @return the JMenuItem representation for the Action
    */
    public JMenuItem getMenuPresenter() {
        if (menu != null) return menu;
        menu = new org.openide.awt.JMenuPlus(getName());
        menu.setHorizontalTextPosition(JMenu.RIGHT);
        menu.setHorizontalAlignment(JMenu.LEFT);
        menu.setIcon (getIcon());
        HelpCtx.setHelpIDString (menu, WorkspaceSwitchAction.class.getName ());

        final WindowManager pool = TopManager.getDefault().getWindowManager ();
        Workspace processed;

        final Hashtable menu2Workspace = new Hashtable(10);
        // ^ maps listener on workspace
        final Hashtable workspace2Menu = new Hashtable(10);
        // ^ maps workspace to menuitem
        final Hashtable workspace2Listener = new Hashtable(10);
        // ^ maps workspace to action listener

        final Workspace[] currentDeskRef = new Workspace[1];
        currentDeskRef[0] = pool.getCurrentWorkspace();
        // attach all workspaces
        Workspace[] workspaces = pool.getWorkspaces();
        for (int i = 0; i < workspaces.length; i++) {
            attachWorkspace(workspaces[i], currentDeskRef, workspace2Menu,
                            menu2Workspace, workspace2Listener);
        }
        // check on currently active workspace
        JRadioButtonMenuItem curItem =
            (JRadioButtonMenuItem)workspace2Menu.get(currentDeskRef[0]);
        if (curItem != null)
            curItem.setSelected(true);
        // listen to the changes in workspaces
        pool.addPropertyChangeListener(
            getWorkspacePoolListener(workspace2Menu, menu2Workspace,
                                     workspace2Listener, currentDeskRef));
        return menu;
    }

    /** Not implemented. May only be used in a menu presenter, with the children performing the action. */
    public void performAction() {
        throw new UnsupportedOperationException();
    }

    /** creates new actionlistener for given menuitem */
    private java.awt.event.ActionListener createActionListener(final JRadioButtonMenuItem menuItem,
            final Workspace[] currentDeskRef,
            final Hashtable menu2Workspace,
            final Hashtable workspace2Menu) {
        return new java.awt.event.ActionListener() {
                   public void actionPerformed(java.awt.event.ActionEvent evt) {
                       Workspace desk = (Workspace) menu2Workspace.get(this);
                       if (desk == null) return;
                       if (workspace2Menu.get(desk) == null) return;
                       ((JRadioButtonMenuItem)workspace2Menu.get(desk)).setSelected(true);
                       if (desk == currentDeskRef[0]) return;
                       ((JRadioButtonMenuItem)workspace2Menu.get(currentDeskRef[0])).setSelected(false);
                       currentDeskRef[0] = desk;
                       desk.activate ();
                   }
               };
    }

    /** creates propertychangelistener that listens on current workspace */
    private PropertyChangeListener getWorkspacePoolListener(final Hashtable workspace2Menu,
            final Hashtable menu2Workspace,
            final Hashtable workspace2Listener,
            final Workspace[] currentDeskRef) {
        PropertyChangeListener pcl1 = new PropertyChangeListener() {
                                          public void propertyChange(PropertyChangeEvent che) {
                                              if (che.getPropertyName().equals(WindowManager.PROP_CURRENT_WORKSPACE)) {
                                                  Workspace newDesk = (Workspace) che.getNewValue();
                                                  if (currentDeskRef[0] == newDesk) return;
                                                  JRadioButtonMenuItem menu = ((JRadioButtonMenuItem)workspace2Menu.get(currentDeskRef[0]));
                                                  if (menu != null) {
                                                      menu.setSelected(false);
                                                  }
                                                  currentDeskRef[0] = newDesk;
                                                  menu = ((JRadioButtonMenuItem)workspace2Menu.get(newDesk));
                                                  if (menu != null) {
                                                      menu.setSelected(true);
                                                  }
                                              } else if (che.getPropertyName().equals(WindowManager.PROP_WORKSPACES)) {
                                                  Workspace[] newWorkspaces = (Workspace[]) che.getNewValue();
                                                  Workspace[] oldWorkspaces = (Workspace[]) che.getOldValue();
                                                  /*for (int i = 0; i < oldWorkspaces.length; i++) {
                                                    System.out.println ("Old Value["+i+"]= "+oldWorkspaces[i].getName());
                                              }
                                                  for (int i = 0; i < newWorkspaces.length; i++) {
                                                    System.out.println ("New Value["+i+"]= "+newWorkspaces[i].getName());
                                              }*/
                                                  List newList = Arrays.asList(newWorkspaces);
                                                  List oldList = Arrays.asList(oldWorkspaces);
                                                  // remove old
                                                  for (int i = 0; i < oldWorkspaces.length; i++) {
                                                      if (newList.indexOf(oldWorkspaces[i]) < 0) {
                                                          detachWorkspace(oldWorkspaces[i],
                                                                          workspace2Menu, menu2Workspace,
                                                                          workspace2Listener);
                                                      }
                                                  }
                                                  // attach new
                                                  for (int i = 0; i < newWorkspaces.length; i++) {
                                                      if (oldList.indexOf(newWorkspaces[i]) < 0) {
                                                          attachWorkspace(newWorkspaces[i], currentDeskRef,
                                                                          workspace2Menu, menu2Workspace,
                                                                          workspace2Listener);
                                                      }
                                                  }
                                              }
                                          }
                                      };
        return pcl1;
    }

    /** Initializes listeners atc to the given workspace */
    void attachWorkspace (Workspace workspace, Workspace[] currentDeskRef,
                          Hashtable workspace2Menu, Hashtable menu2Workspace,
                          Hashtable workspace2Listener) {
        // bugfix #6116 - change from getName() to getDisplayName()
        JRadioButtonMenuItem menuItem =
            new JRadioButtonMenuItem(workspace.getDisplayName());
        HelpCtx.setHelpIDString (menuItem, WorkspaceSwitchAction.class.getName());
        ActionListener listener =
            createActionListener(menuItem, currentDeskRef, menu2Workspace,
                                 workspace2Menu);
        menuItem.addActionListener(listener);
        menu2Workspace.put(listener, workspace);
        workspace2Listener.put(workspace, listener);
        workspace2Menu.put(workspace, menuItem);
        workspace.addPropertyChangeListener(createNameListener(menuItem));
        menu.add(menuItem);
    }

    /** Frees all listeners etc from given workspace. */
    void detachWorkspace (Workspace workspace, Hashtable workspace2Menu,
                          Hashtable menu2Workspace, Hashtable workspace2Listener) {
        JRadioButtonMenuItem menuItem =
            (JRadioButtonMenuItem) workspace2Menu.get(workspace);
        workspace2Menu.remove(workspace);
        menu2Workspace.remove(workspace2Listener.get(workspace));
        workspace2Listener.remove(workspace);
        menu.remove(menuItem);
    }

    /** creates new PropertyChangeListener that listens for "name" property... */ // NOI18N
    private PropertyChangeListener createNameListener(final JRadioButtonMenuItem item) {
        return new PropertyChangeListener() {
                   public void propertyChange(PropertyChangeEvent ev) {
                       if (ev.getPropertyName().equals("name")) {
                           item.setText((String) ev.getNewValue());
                       }
                   }
               };
    }

}

/*
 * Log
 *  26   Gandalf-post-FCS1.24.1.0    4/4/00   David Simonek   bugfix #6116
 *  25   Gandalf   1.24        1/12/00  Ian Formanek    NOI18N
 *  24   Gandalf   1.23        12/21/99 David Simonek   #3731
 *  23   Gandalf   1.22        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  22   Gandalf   1.21        7/30/99  David Simonek   Null pointer bugfix
 *  21   Gandalf   1.20        7/29/99  David Simonek   further ws serialization
 *       changes
 *  20   Gandalf   1.19        7/21/99  David Simonek   window system updates...
 *  19   Gandalf   1.18        7/20/99  David Simonek   various window system 
 *       updates
 *  18   Gandalf   1.17        7/19/99  Jesse Glick     Context help.
 *  17   Gandalf   1.16        7/19/99  Ian Formanek    Temporarily commented 
 *       out to allow startup
 *  16   Gandalf   1.15        7/11/99  David Simonek   window system change...
 *  15   Gandalf   1.14        6/28/99  Ian Formanek    NbJMenu renamed to 
 *       JMenuPlus
 *  14   Gandalf   1.13        6/28/99  Ian Formanek    Fixed bug 2043 - It is 
 *       virtually impossible to choose lower items of New From Template  from 
 *       popup menu on 1024x768
 *  13   Gandalf   1.12        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  12   Gandalf   1.11        6/10/99  Jaroslav Tulach Commented println
 *  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   Gandalf   1.9         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  9    Gandalf   1.8         5/11/99  David Simonek   changes to made window 
 *       system correctly serializable
 *  8    Gandalf   1.7         5/2/99   Ian Formanek    Fixed last change
 *  7    Gandalf   1.6         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  6    Gandalf   1.5         3/30/99  Jesse Glick     Using WorkspacePool 
 *       property name constants.
 *  5    Gandalf   1.4         3/26/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  3    Gandalf   1.2         3/6/99   David Simonek   
 *  2    Gandalf   1.1         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  1    Gandalf   1.0         2/12/99  Ian Formanek    
 * $
 */
