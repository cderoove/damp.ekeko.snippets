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

package org.netbeans.core.windows;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.MenuElement;

import org.openide.TopManager;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.nodes.Node;
import org.openide.util.WeakSet;

/** Implementstion of registry of top components. This implementation
* receives information about top component changes from the window
* manager implementation, to which is listening to.
*
* @author Dafe Simonek
*/
final class RegistryImpl extends Object implements TopComponent.Registry,
            TopComponentListener,
    StateManager.StateListener {

    // fields
    /** Activated top component */
    private TopComponent activatedTopComponent;
    /** Set of opened TopComponents */
    private HashSet openSet;
    /** Currently selected nodes. */
    private Node[] current;
    /** Last non-null value of current nodes */
    private Node[] activated;
    /** PropertyChange support */
    private PropertyChangeSupport support;
    /** state support for window manager */
    private StateManager stateManager;

    /** Creates new RegistryImpl */
    public RegistryImpl () {
        support = new PropertyChangeSupport(this);
        openSet = new HashSet(30);
        activated = new Node[0];
    }

    /** Get all opened componets in the system.
    *
    * @return immutable set of {@link TopComponent}s
    */
    public synchronized Set getOpened() {
        return java.util.Collections.unmodifiableSet(openSet);
    }

    /** Get the currently selected element.
    * @return the selected top component, or <CODE>null</CODE> if there is none
    */
    public TopComponent getActivated () {
        return activatedTopComponent;
    }

    /** Getter for the currently selected nodes.
    * @return array of nodes or null if no component activated or it returns
    *   null from getActivatedNodes ().
    */
    public Node[] getCurrentNodes () {
        return current;
    }

    /** Getter for the lastly activated nodes. Comparing
    * to previous method it always remembers the selected nodes
    * of the last component that had ones.
    *
    * @return array of nodes (not null)
    */
    public Node[] getActivatedNodes () {
        return activated;
    }

    /** Add a property change listener.
    * @param l the listener to add
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    /** Remove a property change listener.
    * @param l the listener to remove
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    /** Called when a TopComponent is activated.
    *
    * @param ev TopComponentChangedEvent
    */
    public void topComponentActivated(TopComponentChangedEvent ev) {
        TopComponent old = activatedTopComponent;
        activatedTopComponent = ev.topComponent;

        java.awt.Window w = ev.topComponent == null ? 
            null : SwingUtilities.windowForComponent(ev.topComponent);
    
        cancelMenu (w);
        
        if (old == activatedTopComponent) {
            return;
        }

        support.firePropertyChange(PROP_ACTIVATED, old, activatedTopComponent);
    }

    /** Called when a TopComponent is opened.
    *
    * @param ev TopComponentChangedEvent
    */
    public synchronized void topComponentOpened(TopComponentChangedEvent ev) {
        if (openSet.contains(ev.topComponent)) {
            return;
        }
        Set old = (Set) openSet.clone();
        openSet.add(ev.topComponent);
        support.firePropertyChange(PROP_OPENED, old, openSet);
    }

    /** Called when a TopComponent is closed.
    *
    * @param ev TopComponentChangedEvent
    */
    public synchronized void topComponentClosed(TopComponentChangedEvent ev) {
        if (! openSet.contains(ev.topComponent)) {
            return;
        }
        // we should remove it from the set only if it is closed
        // on all workspaces
        WindowManagerImpl wm =
            (WindowManagerImpl)TopManager.getDefault().getWindowManager();
        Workspace[] workspaces = wm.getWorkspaces();
        for (int i = 0; i < workspaces.length; i++) {
            if (wm.findManager(ev.topComponent).isOpened(workspaces[i]))
                return;
        }
        // conditions satisfied, now remove...
        Set old = (Set) openSet.clone();
        openSet.remove(ev.topComponent);
        support.firePropertyChange(PROP_OPENED, old, openSet);
    }

    /** Called when selected nodes change..
    *
    * @param ev TopComponentChangedEvent
    */
    public void selectedNodesChanged(SelectedNodesChangedEvent ev) {
        Node[] old = current;
        Node[] c = ev.getSelectedNodes();
        if (Arrays.equals(old, c)) {
            return;
        }
        if (stateManager == null) {
            // initialize and start to listen to state changes
            stateManager = ((WindowManagerImpl)TopManager.getDefault().
                            getWindowManager()).stateManager();
            stateManager.addStateListener(this);
        }
        current = c == null ? null : (Node[])c.clone ();
        // fire immediatelly only if window manager in proper state
        tryFireChanges(stateManager.getState(), old, current);
    }

    /** called when state of window manager changes */
    public void stateChanged (int state) {
        tryFireChanges(state, null, current);
    }

    /** If window manager in proper state, fire selected and
    * activated node changes */
    private void tryFireChanges (int state, Node[] oldNodes, Node[] newNodes) {
        if (state == (StateManager.READY | StateManager.VISIBLE)) {
            support.firePropertyChange(PROP_CURRENT_NODES, oldNodes, newNodes);
            if (newNodes != null) {
                oldNodes = activated;
                activated = newNodes;
                support.firePropertyChange(PROP_ACTIVATED_NODES, oldNodes, activated);
            }
        } else {
            // defer firing, do nothing now
            //System.out.println("Deferred firing..."); // NOI18N
        }
    }

  /**
   * Cancels the menu if it is not assigned to specified window.
   * @param window window that the menu should be checked against (if this
   * window contains the menu, then the menu will not be closed)
   */
  /** Closes popup menu.
   */
  private static void cancelMenu (java.awt.Window window) {
    MenuSelectionManager msm = MenuSelectionManager.defaultManager();
    MenuElement[] path = msm.getSelectedPath ();

    for (int i = 0; i < path.length; i++) {
//      if (newPath[i] != path[i]) return;
        java.awt.Window w = SwingUtilities.windowForComponent(
            path[i].getComponent()
            );
                
        if (w == window || w.getOwner() == window) {
            // ok, this menu can stay
            return;
        }

    }
    
    msm.clearSelectedPath ();
  }
    
}

/*
* Log
*  10   Gandalf   1.9         1/13/00  David Simonek   i18n
*  9    Gandalf   1.8         12/17/99 David Simonek   #1913, #2970
*  8    Gandalf   1.7         12/3/99  Jaroslav Tulach Activated/Current works 
*       better.
*  7    Gandalf   1.6         11/24/99 Jaroslav Tulach Does not keep values of 
*       old selected nodes. Which protected them from garbage collection.  
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         9/8/99   David Simonek   deferred opening and 
*       firing of selected nodes, state management
*  4    Gandalf   1.3         7/28/99  David Simonek   workspace serialization 
*       bugfixes
*  3    Gandalf   1.2         7/21/99  David Simonek   window system updates...
*  2    Gandalf   1.1         7/20/99  David Simonek   various window system 
*       updates
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/
