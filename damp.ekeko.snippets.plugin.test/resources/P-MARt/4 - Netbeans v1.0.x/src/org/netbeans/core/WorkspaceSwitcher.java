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

package org.netbeans.core;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

import java.awt.event.*;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import org.netbeans.core.awt.TabControl;
import org.openide.TopManager;
import org.openide.awt.MouseUtils;
import org.openide.awt.JPopupMenuPlus;
import org.openide.util.HelpCtx;
import org.openide.windows.Workspace;
import org.openide.windows.WindowManager;
import org.netbeans.core.windows.WindowManagerImpl;

/** Visual class for switching workspaces.
*
* @author Ales Novak
* @version 0.14, May 14, 1998
*/
public final class WorkspaceSwitcher {
    private WindowManagerImpl pool;
    private TabControl control;
    private ArrayList workspaces;

    /**
     * @associates PropertyChangeListener 
     */
    private HashMap listeners;
    /** helper flag to prevent changing current workspace
    * when changing all workspaces */
    boolean changeCurrentAllowed = true;

    /**
    * @param pool is a WorkspacePool its Workspaces we switch
    */
    public WorkspaceSwitcher (final WindowManager wpool) {
        this.pool = (WindowManagerImpl) wpool;
        control = new TabControl();
        HelpCtx.setHelpIDString (control, WorkspaceSwitcher.class.getName ());
        workspaces = new ArrayList(5);
        listeners = new HashMap(7);

        control.setDirection(false);
        // add workspaces from pool and select current workspace
        addNew(pool.getWorkspaces());
        Workspace cur = pool.getCurrentWorkspace();
        int ind = workspaces.indexOf(cur);
        if (ind >= 0) control.setSelectedIndex(ind);

        //listen for in/out workspaces
        PropertyChangeListener pcl1 = new PropertyChangeListener() {
                                          public void propertyChange(PropertyChangeEvent che) {
                                              if (che.getPropertyName().equals(WindowManager.PROP_WORKSPACES)) {
                                                  Workspace[] newDesks = (Workspace[]) che.getNewValue();
                                                  //Workspace[] oldDesks = (Workspace[]) che.getOldValue();
                                                  changeCurrentAllowed = false;
                                                  removeOld();
                                                  addNew(newDesks);
                                                  // restore current workspace selection
                                                  int i = workspaces.indexOf(pool.getCurrentWorkspace());
                                                  if (i >= 0) {
                                                      control.setSelectedIndex(i);
                                                  }
                                                  control.revalidate();
                                                  changeCurrentAllowed = true;
                                              }

                                              if (che.getPropertyName().equals(WindowManager.PROP_CURRENT_WORKSPACE)) {
                                                  Workspace newDesk = (Workspace) che.getNewValue();
                                                  int i = workspaces.indexOf(newDesk);
                                                  if (i < 0) {
                                                      addNew(new Workspace[] { newDesk });
                                                      control.revalidate();
                                                      return;
                                                  }
                                                  control.setSelectedIndex (i);
                                                  control.repaint ();
                                              }
                                          }
                                      };
        pool.addPropertyChangeListener(pcl1);

        //listen for events on control
        PropertyChangeListener pcl2 = new PropertyChangeListener() {
                                          public void propertyChange(PropertyChangeEvent che) {
                                              if (changeCurrentAllowed) {
                                                  int i = control.getSelectedIndex();
                                                  if ((i < 0) || (i >= workspaces.size ())) return;
                                                  pool.setCurrentWorkspace((Workspace) workspaces.get(i));
                                              }
                                          }
                                      };
        control.addIndexChangeListener(pcl2); //we will never remove it

        // mouse listener that opens explorer on the workspaces
        MouseListener mouseL = new MouseAdapter () {
                                   public void mousePressed (MouseEvent ev) {
                                       if (MouseUtils.isRightMouseButton (ev)) {
                                           showPopup (ev.getX (), ev.getY ());
                                           ev.consume ();
                                       }
                                   }
                               };
        control.addMouseListener(mouseL);
    }

    /** Subtracts two vectors, suppose that one is a subset of the other one
    * @param v1
    * @param v2
    * @return subtract of vectors
    */
    /*protected ArrayList subtractVec(ArrayList v1, ArrayList v2) {
      ArrayList rest = null;
      Iterator e = null;

      if (v1.size() < v2.size()) {
        e = v1.iterator();
        rest = (ArrayList) v2.clone();
      } else {
        e = v2.iterator();
        rest = (ArrayList) v1.clone();
      }

      while (e.hasNext()) {
        rest.remove(e.next());
      }

      return rest;
}

    protected void addDiffs(ArrayList v) {
      String name = null;
      Workspace d = null;
      for (Iterator e = v.iterator(); e.hasNext();) {
        d = (Workspace)e.next();
        name = d.getName();
        control.addTab(name);
        workspaces.add(d);
        PropertyChangeListener l = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getPropertyName().equals("name")) {
              Workspace source = (Workspace) ev.getSource();
              int i = workspaces.indexOf(source);
              if (i < 0) return;
              control.removeTabAt(i);
              control.addTabAt((String)ev.getNewValue(), i);
            }
          }
        }; //listener
        d.addPropertyChangeListener(l);
        listeners.put(d, l);
      }
}*/

    /** Shows popup with actions for customization */
    void showPopup (int x, int y) {
        JPopupMenu m = new JPopupMenuPlus ();

        final int index = control.pointToIndex (x);
        final Workspace workspace = TopManager.getDefault ().getWindowManager ().getWorkspaces ()[index];
        final String workspaceName = workspace.getName ();
        boolean current = index == control.getSelectedIndex ();

        {
            JMenuItem mi = new JMenuItem (Main.getString ("CTL_SwitchToWorkspace", workspaceName));
            HelpCtx.setHelpIDString (mi, WorkspaceSwitcher.class.getName ());
            mi.addActionListener (new ActionListener () {
                                      public void actionPerformed (ActionEvent ev) {
                                          control.setSelectedIndex (index);
                                      }
                                  });
            mi.setEnabled (!current);
            m.add (mi);
        }

        m.addSeparator ();


        {
            JMenuItem mi = new JMenuItem (Main.getString ("CTL_DeleteWorkspace", workspaceName));
            HelpCtx.setHelpIDString (mi, WorkspaceSwitcher.class.getName ());
            mi.addActionListener (new ActionListener () {
                                      public void actionPerformed (ActionEvent ev) {
                                          // delete the index
                                          workspace.remove ();
                                      }
                                  });
            mi.setEnabled (!current);
            m.add (mi);
            m.addSeparator ();
        }


        {
            JMenuItem mi = new JMenuItem (Main.getString ("CTL_CustomizeWorkspaces"));
            HelpCtx.setHelpIDString (mi, WorkspaceSwitcher.class.getName ());
            mi.addActionListener (new ActionListener () {
                                      public void actionPerformed (ActionEvent ev) {
                                          TopManager top = NbTopManager.getDefault ();
                                          top.getNodeOperation ().explore (top.getPlaces ().nodes ().workspaces ());
                                      }
                                  });
            m.add (mi);
        }

        m.show (control, x, y);
    }

    /** Add new workspaces to the internal structures, assign listeners */
    protected void addNew (Workspace[] newWorkspaces) {
        String name = null;
        PropertyChangeListener l = null;
        for (int i = 0; i < newWorkspaces.length; i++) {
            name = newWorkspaces[i].getDisplayName();
            control.addTab(name);
            workspaces.add(newWorkspaces[i]);
            l = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent ev) {
                        if (ev.getPropertyName().equals(Workspace.PROP_DISPLAY_NAME)) {
                            Workspace source = (Workspace) ev.getSource();
                            int y = workspaces.indexOf(source);
                            if (y < 0) return;
                            control.removeTabAt(y);
                            control.addTabAt((String)ev.getNewValue(), y);
                            control.revalidate();
                        }
                    }
                }; //listener
            newWorkspaces[i].addPropertyChangeListener(l);
            listeners.put(newWorkspaces[i], l);
        }
    }


    /** Remove old workspaces from internal structures,
    * remove their listeners */
    protected void removeOld () {
        Workspace[] toRemove = (Workspace[])workspaces.toArray(new Workspace[0]);
        PropertyChangeListener l = null;
        for (int i = 0; i < toRemove.length; i++) {
            l = (PropertyChangeListener)listeners.remove(toRemove[i]);
            if (l != null) toRemove[i].removePropertyChangeListener(l);
        } //for
        workspaces.clear();
        control.removeAllTabs();
    }

    /**
    * @return TabControl
    */
    public Component getComponent() {
        return control;
    }

}

/*
 * Log
 *  18   Gandalf   1.17        3/11/00  Martin Ryzl     menufix [by E.Adams, 
 *       I.Formanek]
 *  17   Gandalf   1.16        12/17/99 David Simonek   #3496
 *  16   Gandalf   1.15        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        7/31/99  David Simonek   changing current 
 *       workspace bugfix
 *  13   Gandalf   1.12        7/30/99  David Simonek   window icons, comments 
 *       removed
 *  12   Gandalf   1.11        7/28/99  David Simonek   destroy method updated
 *  11   Gandalf   1.10        7/28/99  Jaroslav Tulach Popup menu for 
 *       workspaces.
 *  10   Gandalf   1.9         7/22/99  David Simonek   repaint fix
 *  9    Gandalf   1.8         7/22/99  David Simonek   now reflects workspace 
 *       order correctly
 *  8    Gandalf   1.7         7/21/99  David Simonek   window system updates...
 *  7    Gandalf   1.6         7/15/99  Ian Formanek    Fixed bug #1951 - Every 
 *       right-click on workspace tab open new Explorer[Workspaces] window.
 *  6    Gandalf   1.5         7/11/99  David Simonek   window system change...
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         3/31/99  David Simonek   
 *  3    Gandalf   1.2         3/30/99  Jesse Glick     Using WorkspacePool 
 *       property name constants.
 *  2    Gandalf   1.1         3/9/99   Jaroslav Tulach ButtonBar  
 *  1    Gandalf   1.0         2/12/99  Ian Formanek    
 * $
 */
