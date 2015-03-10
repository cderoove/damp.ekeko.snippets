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

package org.openide.windows;

import java.awt.Image;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Set;

import org.openide.nodes.Node;

/** Manager of windows in the IDE.
* Handles the work with workspaces, allows to listen to
* workspace changes.
*
* @author Jaroslav Tulach
*/
public abstract class WindowManager extends Object implements Serializable {
    /** property change of workspaces */
    public static final String PROP_WORKSPACES = "workspaces"; // NOI18N
    /** property change of current workspace */
    public static final String PROP_CURRENT_WORKSPACE = "currentWorkspace"; // NOI18N
    /** The top component which is currently active */
    private TopComponent activeComponent;

    static final long serialVersionUID =-4133918059009277602L;
    /** Get the Main Window of the IDE.
    * This should ONLY be used for:
    * <UL>
    *   <LI>using the Main Window as the parent for dialogs</LI>
    *   <LI>using the Main Window's position for preplacement of windows</LI>
    * </UL>
    * @return the Main Window of the IDE
    */
    public abstract java.awt.Frame getMainWindow ();

    /** Called after a Look&amp;Feel change to update the IDE's UI.
    * Should call {@link javax.swing.JComponent#updateUI} on all opened windows.
    */
    public abstract void updateUI ();

    /** Create a component manager for the given top component.
    * @param c the component
    * @return the manager to handle opening, closing and selecting the component
    */
    protected abstract WindowManager.Component createTopComponentManager (TopComponent c);

    /** Access method for registry of all components in the system.
    * @return the registry 
    */
    protected abstract TopComponent.Registry componentRegistry ();

    /** Creates new workspace.
    * @deprecated please use method createWorkspace(String name, String displayName) instead
    * @param name the name of the workspace
    * @return new workspace
    */
    public final Workspace createWorkspace (String name) {
        return createWorkspace(name, name);
    }

    /** Creates new workspace.
    * @param codeName the code name (used for lookup)
    * @param displayName the display name
    */
    public abstract Workspace createWorkspace (String name, String displayName);

    /** Finds workspace given its name.
    * @param name the name of workspace to find
    * @return workspace or null if not found
    */
    public abstract Workspace findWorkspace (String name);

    /** List of all workspaces.
    */
    public abstract Workspace[] getWorkspaces ();

    /** Sets new array of workspaces.
    * @param workspaces An array consisting of new workspaces.
    */
    public abstract void setWorkspaces (Workspace[] workspaces);

    /** Current workspace. Can be changed by calling Workspace.activate ()
    */
    public abstract Workspace getCurrentWorkspace ();

    //
    // You can add implementation to this class (+firePropertyChange), or implement it in subclass
    // Do as you want.
    //

    /** Attaches listener for changes in workspaces
    */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /** Removes listener.
    */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);

    /** Finds top component manager for given top component.
    * @param tc top component to find manager for.
    * @return component manager for given top component.
    */
    protected static final Component findComponentManager (TopComponent tc) {
        return tc.getManager();
    }

    /** Activate a component. The top component containers should inform
    * the top component that it is active via a call to this method through
    * derived window manager implementation.
    * @param comp the top component to activate;
    * or <code>null</code> to deactivate all top components
    */
    protected void activateComponent (TopComponent tc) {
        // check
        if (activeComponent == tc) return;
        // deactivate old if possible
        if (activeComponent != null)
            activeComponent.componentDeactivated();
        activeComponent = tc;
        if (activeComponent != null)
            activeComponent.componentActivated();
    }

    /** A manager that handles operations on top components.
    * It is always attached to a {@link TopComponent}.
    */
    protected interface Component extends java.io.Serializable {

        static final long serialVersionUID =6319441499266039128L;
        /** Open the component on current workspace */
        public void open ();

        /** Open the component on given workspace in the right mode.
        */
        public void open (Workspace workspace);

        /** Close the component on given workspace.
        */
        public void close (Workspace workspace);

        /** Called when the component requests focus. Moves it to be visible.
        */
        public void requestFocus ();

        /** Get the set of activated nodes.
        * @return currently activated nodes for this component
        */
        public Node[] getActivatedNodes ();

        /** Set the set of activated nodes for this component.
        * @param nodes new set of activated nodes
        */
        public void setActivatedNodes (Node[] nodes);

        /** Called when the name of the top component changes.
        */
        public void nameChanged ();

        /** Set the icon of the top component.
        * @param icon the new icon
        */
        public void setIcon (final Image icon);

        /** @return the icon of the top component */
        public Image getIcon ();

        /** @return the set of workspaces where managed component is open */
        public Set whereOpened ();

    }
}

/*
* Log
*  19   Gandalf   1.18        1/15/00  David Simonek   createWorkspace modified
*  18   Gandalf   1.17        1/13/00  David Simonek   i18n
*  17   Gandalf   1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  16   Gandalf   1.15        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  15   Gandalf   1.14        8/9/99   David Simonek   
*  14   Gandalf   1.13        7/28/99  David Simonek   
*  13   Gandalf   1.12        7/21/99  David Simonek   window system updates...
*  12   Gandalf   1.11        7/11/99  David Simonek   window system change...
*  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  10   Gandalf   1.9         4/7/99   David Simonek   
*  9    Gandalf   1.8         3/29/99  Jesse Glick     [JavaDoc]
*  8    Gandalf   1.7         3/29/99  Jesse Glick     [JavaDoc]
*  7    Gandalf   1.6         3/25/99  David Simonek   changes in window system,
*       initial positions, bugfixes
*  6    Gandalf   1.5         3/22/99  David Simonek   
*  5    Gandalf   1.4         3/19/99  David Simonek   
*  4    Gandalf   1.3         3/17/99  David Simonek   slightly changed window 
*       system
*  3    Gandalf   1.2         2/17/99  David Simonek   setRequestedSize method 
*       added to the window system  getDefaultMode added to the TopComponent
*  2    Gandalf   1.1         2/12/99  Ian Formanek    Reflected renaming 
*       Desktop -> Workspace
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
