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

import java.awt.*;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.ObjectInputValidation;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.windows.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Mutex;
import org.openide.util.io.NbMarshalledObject;
import org.openide.NotifyDescriptor;

import org.netbeans.core.MainWindow;

import org.netbeans.core.windows.WeakHash.Value;
import org.netbeans.core.windows.util.*;

/** Implementation of manager of windows in the IDE.
* Handles work with workspaces, serialization of all window system,
* allows to listen to workspace events.
* This class is final only for performance reasons. Can be freely
* unfinaled if desired.
*
* @author Jaroslav Tulach, Dafe Simonek
*/
public final class WindowManagerImpl extends WindowManager {
    /** The Corona IDE's main window */
    static MainWindow mainWindow;
    /** The only instance of the window manager implementation
    * in the system */
    static WindowManagerImpl defaultInstance;
    /** State machine support */
    static StateManager stateManager;
    /** Support for deferred opening of top components */
    static DeferredPerformer deferredPerformer;

    /** array of workspaces */
    Workspace[] workspaces;
    /** Current active workspace */
    Workspace current;
    /** properties support */
    transient PropertyChangeSupport changeSupport;
    /** the set of listeners which listen to the changes of
    * top component set in system 
    * @associates TopComponentListener*/
    transient HashSet tcListeners;

    /** The history of activated components mapped to their classes
    * (mapping class - top component) */
    private static Hashtable lastActivatedComponents;
    /** initialized already? */
    transient private boolean isCreated;
    /** map of workspace::Value (activated nodes) */
    transient private WeakHash workspace2Nodes;
    /** helper flag */
    transient private boolean serializationInProgress = false;
    /** true if main window was already positioned
    * during deserialization */ 
    transient private boolean mainPositioned = false;
    /** Helper temporary variable, holds TC manager currently
    * being validated, used in createTopComponentManager() method */
    transient private TopComponentManager validatedManager;
    /** Standard creator for proper initialixation of the window system */
    private static DefaultCreator dc;

    static final long serialVersionUID =680725949680433701L;

    /** default base name for noname modes */
    private static final String DEFAULT_NAME = "untitled_mode"; // NOI18

    /** Default constructor. Don't use directly, use getDefault()
    * instead.
    */
    private WindowManagerImpl () {
        initialize();
    }

    /** Needed initialization, called from constrcutor and
    * during deserialization */
    private void initialize () {
        changeSupport = new PropertyChangeSupport(this);
        workspace2Nodes = new WeakHash();
    }

    public synchronized static WindowManagerImpl getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new WindowManagerImpl();
            // initialize window manager with default settings
            // we are not doing this in constructor to prevent from cycling
            if (!"full".equals (System.getProperty ("netbeans.full.hack"))) {
                dc = new DefaultCreator(defaultInstance);
                dc.start();
            }
        }
        return defaultInstance;
    }

    /** @return The only instance of window manager's state support
    * in the system */
    public static StateManager stateManager () {
        if (stateManager == null) {
            stateManager = new StateManager();
        }
        return stateManager;
    }

    /** @return The only instance of support for deferred opening.
    */
    public static DeferredPerformer deferredPerformer () {
        if (deferredPerformer == null) {
            deferredPerformer = new DeferredPerformer();
        }
        return deferredPerformer;
    }


    /** Helper method - create workspaces from scratch by
    * adding standard workspaces and setting initial positions */
    public static void createFromScratch () {
        if (!"full".equals (System.getProperty ("netbeans.full.hack"))) {
            // ensure that initialization is done
            getDefault();
        }
    }

    /** Provides access to the MainWindow of the IDE.
    * This should ONLY be used for:
    * <UL>
    *   <LI>Using the MainWindow as the parent for dialogs</LI>
    *   <LI>Using the MainWindow's positions for positioning of windows
    *       that need to be prepositioned </LI>
    * </UL>
    * @return the MainWindow of the IDE.
    */
    public java.awt.Frame getMainWindow () {
        return mainWindow ();
    }

    /** Called after a current LookAndFeel change to update the IDE's UI
    * Should call updateUI on all opened windows */
    public void updateUI () {
        // update main window first
        SwingUtilities.updateComponentTreeUI(mainWindow);
        mainWindow.pack();
        // update all other opened windows on workspaces
        Workspace[] wArray = getWorkspaces();
        for (int i = 0; i < wArray.length; i++) {
            for (Iterator iter = wArray[i].getModes().iterator(); iter.hasNext(); ) {
                ((ModeImpl)iter.next()).updateUI();
            }
        }
    }

    /** Returns newly created and properly
    * initialized top component registry.
    * 
    * @return the registry 
    */
    protected TopComponent.Registry componentRegistry () {
        RegistryImpl result = new RegistryImpl();
        // force registry to listen to top components' events
        addTopComponentListener(result);
        return result;
    }

    /** Creates new workspace with given name.
    * @param name the name of the workspace
    * @return new workspace
    */
    public Workspace createWorkspace (String name, String displayName) {
        return new WorkspaceImpl(name, displayName);
    }

    /** Finds workspace given its name.
    * @param name the name of workspace to find
    * @return workspace or null if not found
    */
    public Workspace findWorkspace (String name) {
        if (workspaces == null)
            return null;
        for (int i = 0; i < workspaces.length; i++) {
            if (name.equals(workspaces[i].getName()))
                return workspaces[i];
        }
        return null;
    }

    /** List of all currenty available workspaces.
    */
    public Workspace[] getWorkspaces () {
        return workspaces;
    }

    /** Sets new workspaces.
    * @param workspaces array of new workspaces
    */
    public void setWorkspaces (Workspace[] workspaces) {
        if (Arrays.equals(this.workspaces, workspaces))
            return;
        Workspace[] old = this.workspaces;
        this.workspaces = workspaces;
        changeSupport.firePropertyChange(
            PROP_WORKSPACES, old, this.workspaces
        );
    }

    /** @return Current workspace.
    * Can be changed by calling Workspace.activate ()
    */
    public Workspace getCurrentWorkspace () {
        return current;
    }

    /** Sets current workspace.
    * @return true if succeded, false otherwise (when workspace is not
    * known to this window manager...)
    */
    public boolean setCurrentWorkspace (Workspace workspace) {
        if ((current != null) && (current.equals(workspace))) {
            return true;
        }
        // check if present
        boolean found = false;
        for (int i = 0; i < workspaces.length; i++) {
            if (workspaces[i].equals(workspace)) {
                found = true;
                break;
            }
        }
        if (!found)
            return false;
        // perform the change
        StateManager stateMan = stateManager();
        if ((stateMan.getState() & StateManager.READY) != 0) {
            stateMan.setMainState(StateManager.SWITCHING);
        }
        // close all menus before workspace switch
        // (opened popup menus caused some serious problems)
        javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
        Workspace old = current;
        current = workspace;
        if (old != null) {
            workspace2Nodes.put(old, getSelectedNodes());
            ((WorkspaceImpl)old).setVisible(false);
        }
        ((WorkspaceImpl)current).setVisible(true);
        // notify others
        changeSupport.firePropertyChange(
            PROP_CURRENT_WORKSPACE, old, current
        );
        setSelectedNodes(workspace2Nodes.get(current));
        if ((stateMan.getState() & StateManager.SWITCHING) != 0) {
            stateMan.setMainState(StateManager.READY);
        }
        return true;
    }


    /** switches to next workspace as current */
    public synchronized void nextWorkspace() {
        Workspace current = getCurrentWorkspace();
        int len = workspaces.length - 1;
        for (int i = len; i >= 0; i--) {
            if (workspaces[i] == current) {
                if (i == len) i = 0; // cycle it
                else ++i;
                // i will be current
                current = (Workspace) workspaces[i];
                setCurrentWorkspace(current);
                return;
            }
        }
    }

    /** switche to previous workspace */
    public synchronized void previousWorkspace() {
        Workspace current = getCurrentWorkspace();
        int len = workspaces.length - 1;
        for (int i = len; i >= 0; i--) {
            if (workspaces[i] == current) {
                if (i == 0) i = len; // cycle it
                else --i;
                // i will be current
                current = (Workspace) workspaces[i];
                setCurrentWorkspace(current);
                return;
            }
        }
    }

    /** @return selected nodes at 0 and activated at 1 */
    static Value getSelectedNodes() {
        Node[][] ns = new Node[2][];
        ns[0] = TopComponent.getRegistry().getCurrentNodes();
        ns[1] = TopComponent.getRegistry().getActivatedNodes();
        Value ret = new Value();
        ret.activatedNodes = ns;
        ret.activatedTC = TopComponent.getRegistry().getActivated();
        return ret;
    }

    void setSelectedNodes(Value active) {
        Node[][] nodes = (active == null ? null : active.activatedNodes);
        if (nodes == null) {
            nodes = new Node[2][];
            nodes[0] = nodes[1] = new Node[] {};
        }
        RegistryImpl rimpl = (RegistryImpl) TopComponent.getRegistry();
        SelectedNodesChangedEvent ev = new SelectedNodesChangedEvent(nodes, null, nodes[1]);
        rimpl.selectedNodesChanged(ev);
        ev = new SelectedNodesChangedEvent(nodes, null, nodes[0]);
        rimpl.selectedNodesChanged(ev);

        if (active != null) {
            //      activateComponent(active.activatedTC);
            /*
            TopComponentChangedEvent tcev = new TopComponentChangedEvent(nodes,
                                                                       active.activatedTC,
                                                                       null,
                                                                       1
                                                                      );
                                                                      rimpl.topComponentActivated(tcev);
                                                                      */
        }
    }

    //
    // You can add implementation to this class (+firePropertyChange), or implement it in subclass
    // Do as you want.
    //

    /** Attaches listener for changes in workspaces
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    /** Removes listener.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    /** Adds top component listener for listening to the changes of
    * the set of top components in the system */
    public synchronized void addTopComponentListener (TopComponentListener tcl) {
        if (tcListeners == null)
            tcListeners = new HashSet(5);
        tcListeners.add(tcl);
    }

    /** Removes top component listener */
    public synchronized void removeTopComponentListener (TopComponentListener tcl) {
        if (tcListeners == null)
            return;
        tcListeners.remove(tcl);
    }

    /** @return True if workspace pool was created from scratch or
    * deserialized already, false if not initialized yet.
    */
    public boolean isCreated () {
        return isCreated;
    }

    /** Sets created flag. Accessible only for class in this
    * package */
    void setCreated (boolean isCreated) {
        this.isCreated = isCreated;
    }

    /** @return true if main window was already positioned */
    public boolean isMainPositioned () {
        return mainPositioned;
    }

    /** Helper method.
    * @return set of listeners, which is prepared for firing.
    * Can return null if no listeners are attached */
    Set getTcListenersForFiring () {
        if (tcListeners == null)
            return null;
        Set cloned = null;
        synchronized (this) {
            cloned = (Set)tcListeners.clone();
        }
        return cloned;
    }

    /** Utility method, reactivates last activated top component
    * of specified class.
    * @param componentClass the class of the top component which should
    *        be reactivated
    * @return true if some component was reactivated, false if not
    * (no component of specified class was activated before)
    */
    public static boolean reactivateComponent (Class componentClass) {
        TopComponent last = lastActivated(componentClass);
        if (last != null) {
            last.open();
            last.requestFocus();
        }
        return last != null;
    }

    /** Returns the top component of specified class which was activated
    * in the past and no other top component of this class was
    * activated later.
    * @param componentClass the class of the top component
    * @return requested top component or null if no such top component
    * can be found.
    */
    public static TopComponent lastActivated (Class componentClass) {
        if (lastActivatedComponents == null)
            return null;
        return (TopComponent)lastActivatedComponents.get(componentClass);
    }

    /** Utility method, finds unused name of the mode on given workspace
    * based on given string.
    * @param base Base name of the mode. Can be null - in this cas
    * some default string constant will be used
    * @return string representing mode name which is not used yet
    * on current workspace */
    public static String findUnusedModeName (String base, Workspace workspace) {
        // be prepared when base is null
        if (base == null) {
            base = DEFAULT_NAME;
        }
        if (workspace.findMode(base) == null)
            return base;
        // add numbers to the name
        String result = null;
        int modeNumber = 1;
        while (workspace.findMode(result = base + " " + modeNumber) != null) { // NOI18N
            modeNumber++;
        }
        return result;
    }


    /** Enable classes from this package to call this method
    * and registers component into history of activated top components.
    */
    protected void activateComponent (TopComponent tc) {
        /*System.out.println("Activating: " +
                           ((tc == null) ? "NONE" : tc.getName()));*/ // NOI18N
        if (tc != null) {
            // add to activated history
            if (lastActivatedComponents == null)
                lastActivatedComponents = new Hashtable();
            //System.out.println("Registering: " + tc.getClass().getName()); // NOI18N
            registerComponent(tc.getClass(), tc);
        }
        // fire info that activated component changed
        Set listeners = getTcListenersForFiring();
        if (listeners != null) {
            for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
                ((TopComponentListener)iter.next()).topComponentActivated(
                    new TopComponentChangedEvent(this, tc, getCurrentWorkspace(),
                                                 TopComponentChangedEvent.ACTIVATED));
            }
        }
        super.activateComponent(tc);
    }

    /** Adds given pair class - top component to the activated history */
    static void registerComponent (Class c, TopComponent comp) {
        if (c == null) return;
        Object orig = lastActivatedComponents.put (c, comp);
        // recurse for super class and super interfaces
        registerComponent(c.getSuperclass (), comp);
        Class[] inter = c.getInterfaces ();
        for (int i = 0; i < inter.length; i++) {
            registerComponent(inter[i], comp);
        }
    }

    /** Removes given pair class - top component from activated history */
    static void unregisterComponent (Class c, TopComponent comp) {
        if (c == null) return;
        // if different comp is attached to class c stop removing
        if (comp != lastActivatedComponents.get(c))
            return;
        lastActivatedComponents.remove (c);
        // recurse for super class and super interfaces
        unregisterComponent(c.getSuperclass (), comp);
        Class[] inter = c.getInterfaces ();
        for (int i = 0; i < inter.length; i++) {
            unregisterComponent(inter[i], comp);
        }
    }

    /** Creates a component manager for given top component.
    * @param c the component
    * @return the manager that handles opening, closing and
    * selecting a component
    */
    protected synchronized org.openide.windows.WindowManager.Component createTopComponentManager (TopComponent c) {
        /*if (validatedManager != null) {
          System.out.println("Validating for " + validatedManager.componentName);
          System.out.println("TC class: " + c.getClass().getName());
    }*/
        // create new manager or returng manager being validated
        org.openide.windows.WindowManager.Component result =
            (validatedManager == null) ? new TopComponentManager(c) : validatedManager;
        // clear after each usage
        validatedManager = null;
        return result;
    }

    /** Helper; used from TopComponentManager to identify itself
    * during deserialization to achieve asociation with its top component */
    private void setValidatedManager (TopComponentManager tcm) {
        validatedManager = tcm;
    }

    private TopComponentManager getValidatedManager () {
        return validatedManager;
    }

    /** Finds top component manager for given top component.
    * It's here just to rovide access for classes in this package.
    */
    public static TopComponentManager findManager (TopComponent tc) {
        return (TopComponentManager) /*WindowManager. not compilable by JIKES */findComponentManager(tc);
    }

    /** Convenience static method for simpler obtaining mainWindow
    * reference
    */
    public static final MainWindow mainWindow () {
        if (mainWindow == null) {
            synchronized (WindowManagerImpl.class) {
                if (mainWindow == null) {
                    mainWindow = new MainWindow ();
                    mainWindow.addWindowListener(new IconifyManager());
                }
            }
        }
        return mainWindow;
    }

    /** Delegates serialization manager instance to be serialized
    * instead of window manager impl */
    private Object writeReplace ()
    throws ObjectStreamException {
        return new SerializationReplacer();
    }

    /** Instance of this class is serialized instead of WindowManagerImpl.
    * It saves all needed information and deserializesback  to the signleton
    * instance of WindowManagerImpl */
    private static final class SerializationReplacer implements Serializable {
        static final long serialVersionUID =-8212722893309295268L;
        // deserialized workspaces
        transient Workspace[] workspaces;
        // deserialized current workspaces
        transient Workspace current;
        // flag for recognizing first startup
        transient boolean isStartup = true;

        /** Deserialization of the workspace */
        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            // obtain WindowManagerImpl whose serialization we manage
            WindowManagerImpl wm = WindowManagerImpl.getDefault();
            StateManager stateMan = wm.stateManager();
            stateMan.setMainState(StateManager.DESERIALIZING);
            try {
                // remove old workspaces, (but don't do this on the
                // first startup, as modules can have their own
                // workspaces already installed)
                if (!isStartup) {
                    wm.setWorkspaces(new Workspace[0]);
                }
                // read workspaces - first phase
                //System.out.println("First phase - reading..."); // NOI18N
                int count = ois.readInt();
                workspaces = new Workspace[count];
                for (int i = 0; i < count; i++) {
                    workspaces[i] = (Workspace)ois.readObject();
                }
                // read current workspace
                current = (Workspace)ois.readObject();
                // read bounds of main window
                wm.mainWindow().setBounds((Rectangle)ois.readObject());
                //System.out.println("Workspaces read suceesfully!"); // NOI18N
                // testing
                //throw new IOException();
            } finally {
                isStartup = false;
                stateMan.setMainState(StateManager.READY);
                //System.out.println("Workspace reading finished."); // NOI18N
            }
        }

        /** Resolves deserialized SerializationReplacer to the singleton
        * instance of WindowManagerImpl */
        private synchronized Object readResolve ()
        throws ObjectStreamException {
            // obtain WindowManagerImpl whose serialization we manage
            WindowManagerImpl wm = WindowManagerImpl.getDefault();
            StateManager stateMan = wm.stateManager();
            stateMan.setMainState(StateManager.DESERIALIZING);
            try {
                wm.mainPositioned = true;
                // validating workspaces and modes
                //System.out.println("Second phase - validate workspaces and modes..."); // NOI18N
                for (int i = 0; i < workspaces.length; i++) {
                    ((WorkspaceImpl)workspaces[i]).validateSelf();
                }
                // set new workspaces
                wm.setWorkspaces(workspaces);
                // validate top components
                //System.out.println("Third phase - validate top components..."); // NOI18N
                for (int i = 0; i < workspaces.length; i++) {
                    ((WorkspaceImpl)workspaces[i]).validateData();
                }
                wm.setCurrentWorkspace(current);
                //System.out.println("WS validated succesfully."); // NOI18N
            } finally {
                stateMan.setMainState(StateManager.READY);
            }
            // reactivate
            return wm;
        }

        /** Serialization of all workspaces */
        private void writeObject (ObjectOutputStream oos)
        throws IOException {
            //System.out.println("Writing workapaces..."); // NOI18N
            // obtain WindowManagerImpl whose serialization we manage
            WindowManagerImpl wm = WindowManagerImpl.getDefault();
            StateManager stateMan = wm.stateManager();
            stateMan.setMainState(StateManager.SERIALIZING);
            try {
                // write workspaces
                Workspace[] workspaces = wm.workspaces;
                if (workspaces == null) {
                    oos.writeInt(0);
                } else {
                    oos.writeInt(workspaces.length);
                    for (int i = 0; i < workspaces.length; i++) {
                        oos.writeObject(workspaces[i]);
                    }
                }
                // write current workspace
                oos.writeObject(wm.current);
                // write bounds of main window
                oos.writeObject(wm.mainWindow().getBounds());
            } finally {
                stateMan.setMainState(StateManager.READY);
            }
            //System.out.println("Workspaces written."); // NOI18N
        }

    } // end of inner class SerializationReplacer

    /** Manager for iconifying/deiconifying main window */
    static final class IconifyManager extends WindowAdapter {
        /** The workspace to be shown on deiconification */
        private WorkspaceImpl workspace;

        public void windowIconified (WindowEvent evt) {
            RequestProcessor.postRequest (new Runnable () {
                                              public void run () {
                                                  iconify ();
                                              }
                                          }, 200);
        }

        public void windowDeiconified (WindowEvent evt) {
            if (workspace != null) {
                workspace.setVisible(true);
            }
        }

        /** Iconify all modes on current workspace */
        private void iconify () {
            workspace = (WorkspaceImpl)TopManager.getDefault().
                        getWindowManager().getCurrentWorkspace();
            workspace.setVisible(false);
        }
    } // end of IconifyManager

    /** The manager that handles operations on a top component.
    * It is always attached to a TopComponent via one-to-one
    * relationship.
    *
    * @author Dafe Simonek
    */
    static final class TopComponentManager extends ComponentAdapter
                implements WindowManager.Component,
        DeferredPerformer.DeferredCommand {
        /** The constants for properties of managed top component */
        public static final String PROP_ACTIVATED_NODES = "activatedNodes"; // NOI18N
        public static final String PROP_NAME = "name"; // NOI18N
        public static final String PROP_ICON = "icon"; // NOI18N

        /** Set of workspaces where top component is opened. */
        transient HashSet whereOpened;
        /** Agregation of the top component which we're trying to manage */
        TopComponent component;
        /** top component we manage in hlaf-way deserialized form,
        * used only during deserialization */
        private NbMarshalledObject marshalledComponent;
        /** helper variable, holds top component's name, used when
        * reporting failure of deserialization of the top copmponent */
        String componentName;

        /** Icon of the component we manage */
        transient Image icon;
        /** Activated nodes of the component we manage */
        transient Node[] nodes;
        /** Nodes which will be set as activated when the component becomes non-null */
        transient Node[] tempNodes;
        /** asociation with the window manager */
        transient WindowManagerImpl wm;
        /** Support for property changes */
        transient PropertyChangeSupport changeSupport;
        /** if true, the body of open method is performed even if managed
        * top component is already opened */ 
        transient boolean forceOpen = false;
        /** asociation with support for deferred opening */
        transient DeferredPerformer deferredPerformer;
        /** helper flag, holds current state of validation during
        * deserialization */
        private transient int innerState = READY;
        /** constant for all-right state */
        private static final int READY = 1;
        /** constant for state when validation was not performed yet */
        private static final int INVALID = 2;
        /** constant for state when deserialization of tc completely failed */
        private static final int FAILED = 4;

        /** manager of versioned serialization */
        private static VersionSerializator serializationManager;

        static final long serialVersionUID =5669852754182098300L;
        /** Constructs new top component manager. Allow only classes in package
        * to construct us
        * @param component Managed top component.
        */
        TopComponentManager (TopComponent component) {
            this.component = component;
            whereOpened = new HashSet(5);
            initialize();
        }

        /** Initialization of this manager, called also when deserializaing */
        private void initialize () {
            this.wm =
                (WindowManagerImpl)TopManager.getDefault().getWindowManager();
            changeSupport = new PropertyChangeSupport(this);
            deferredPerformer = deferredPerformer();
        }

        /** Opens a component on current workspace. If main window is
        * not visible, open action is delayed until it's open.
        */
        public void open () {
            open(null);
        }

        /** Opens a component on given workspace. If window system is
        * in inconsistent state (main window is not visible, serializing etc..)
        8 open action is delayed.
        * @workspace the workspace where to open managed top component
        */
        public void open (Workspace workspace) {
            if (deferredPerformer.canImmediatelly()) {
                // immediate open
                doOpen((workspace == null) ? wm.getCurrentWorkspace() : workspace);
            } else {
                // deferred open
                deferredPerformer.putRequest(this, workspace);
            }
        }

        /** Implementation of DeferredPerformer.DeferredCommand interface.
        * Actually opens managed top component. */
        public void performCommand (Object context) {
            /*if (component == null) {
              System.out.println("ASOCIATED COMPONENT IS NULL!!!!!");
              Thread.dumpStack();
        }*/
            component.open((Workspace)context);
        }

        /** Opens a component on given workspace.
        * If given workspace differs from current, component will
        * be visible only after switching to given workspace.
        * @workspace the workspace where to open managed top component
        */
        private void doOpen (Workspace workspace) {
            /*System.out.println("Opening " + component.getName() + " on " +
                               workspace.getName());*/
            // PENDING -> editor TC open on all workspaces at once
            if ((!whereOpened.add(workspace)) && (!forceOpen)) {
                return;
            };
            Mode mode = workspace.findMode(component);
            if (mode == null) {
                // create new mode for given tc
                // important bugfix - create properly named mode even
                // for top components with null name
                String modeName = wm.findUnusedModeName(component.getName(), workspace);
                mode = ((WorkspaceImpl)workspace).createMode(
                           modeName, modeName, null,
                           ModeImpl.MULTI_TAB, true
                       );
            }
            mode.dockInto(component);
            // let others know that top component was opened...
            Set listeners = wm.getTcListenersForFiring();
            if (listeners != null) {
                for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
                    ((TopComponentListener)iter.next()).topComponentOpened(
                        new TopComponentChangedEvent(this, component, workspace,
                                                     TopComponentChangedEvent.OPENED));
                }
            }
        }

        /** @return true if managed top component is currently opened
        * on some workspace, false otherwise */
        public boolean isOpened () {
            return whereOpened.size() > 0;
        }

        /** @return true if managed top component is currently opened
        * on given workspace, false otherwise */
        public boolean isOpened (Workspace workspace) {
            return whereOpened.contains(workspace);
        }

        /** Closes the component on given workspace.
        * @param workspace the workspace where managed top component
        * should be closed.
        */
        public void close (Workspace workspace) {
            switch (component.getCloseOperation()) {
            case TopComponent.CLOSE_LAST:
                if (isOpened(workspace))
                    doClose(workspace);
                break;
            case TopComponent.CLOSE_EACH:
                // special mode for editor etc...
                // close on all workspaces
                Workspace[] workspaces = wm.getWorkspaces();
                for (int i = 0; i < workspaces.length; i++) {
                    if (isOpened(workspaces[i]))
                        doClose(workspaces[i]);
                }
                break;
            }
        }

        /** The component requests focus. Works only on opened component.
        * When the request comes too early, implemetation waits
        * till component is shown.
        */
        public void requestFocus () {
            if (!isOpened())
                return;
            if (component.isDisplayable())
                Mutex.EVENT.readAccess(new Runnable() {
                                       public void run () {
                                           ((ModeImpl)getMode()).requestFocus(component);
                                       }
                                   });
            else {
                // component not yet shown, we must wait
                // until the component is shown
                component.addComponentListener(this);
            }
        }

        /** @return the mode which belongs to managed top component on
        * CURRENT workspace.
        */
        Mode getMode () {
            return wm.getCurrentWorkspace().findMode(component);
        }

        /** Actually perform the work of closing ther managed component on
        * given workspace, without checking. */
        void doClose (Workspace workspace) {
            ModeImpl mode = (ModeImpl)workspace.findMode(component);
            whereOpened.remove(workspace);
            if (mode != null) {
                mode.close(component);
            }
            // remove from activated history if conditions satisfied
            if (WindowManagerImpl.lastActivatedComponents != null) {
                WindowManagerImpl.unregisterComponent(component.getClass(),
                                                      component);
                /*System.out.println ("Removed from act history... " +
                                    component.getClass());*/
            }
            // let others know that top component was closed...
            Set listeners = wm.getTcListenersForFiring();
            if (listeners != null) {
                for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
                    ((TopComponentListener)iter.next()).topComponentClosed(
                        new TopComponentChangedEvent(this, component, workspace,
                                                     TopComponentChangedEvent.CLOSED));
                }
            }
        }

        /** Getter for set of activated nodes
        * @return activated nodes for the component
        */
        public Node[] getActivatedNodes () {
            //System.out.println ("Getting nodes, size: " + result.length); // NOI18N
            return nodes;
        }

        /** Setter for set of activated nodes for the component
        * @param nodes activated nodes for this component
        */
        public void setActivatedNodes (Node[] nodes) {
            if (component == null) {
                // store the nodes to a temporary variable and set them properly
                // when a non-null value of the component is set
                tempNodes = nodes;
                return;
            }
            //System.out.println ("Setting nodes, size: " + nodes.length); // NOI18N
            //System.out.println ("Opened?: " + opened); // NOI18N
            if (Arrays.equals(this.nodes, nodes))
                return;
            Node[] old = this.nodes;
            this.nodes = nodes;
            // notify all that are interested...
            changeSupport.firePropertyChange(PROP_ACTIVATED_NODES, old, nodes);
            Set listeners = wm.getTcListenersForFiring();
            if (listeners != null) {
                for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
                    ((TopComponentListener)iter.next()).selectedNodesChanged(
                        new SelectedNodesChangedEvent(this, component, nodes));
                }
            }
        }

        /** Notify about name change.
        */
        public void nameChanged () {
            // component can be null for a while during deserialization
            if (component != null) {
                changeSupport.firePropertyChange(PROP_NAME, null, component.getName());
            }
        }

        /** Sets the icon of the top component which will be used for
        * component representaion on the screen.
        * @param icon New components' icon.
        */
        public void setIcon (final Image icon) {
            if (((this.icon != null) && this.icon.equals(icon)) ||
                    ((this.icon == null) && (icon == null)))
                return;
            Image old = this.icon;
            this.icon = icon;
            changeSupport.firePropertyChange(PROP_ICON, old, this.icon);
        }

        public Image getIcon () {
            return icon;
        }

        /** Getter for managed component
        * @return managed component
        */
        public TopComponent getComponent () {
            return component;
        }

        /** @return the set of workspaces where managed component is open */
        public Set whereOpened () {
            return whereOpened;
        }

        /** Adds listener for listening to top component property changes.
        */
        public void addPropertyChangeListener (PropertyChangeListener pchl) {
            changeSupport.addPropertyChangeListener(pchl);
        }

        /** Removes property change listener.
        */
        public void removePropertyChangeListener (PropertyChangeListener pchl) {
            changeSupport.removePropertyChangeListener(pchl);
        }

        /** Called when managed top component becomes
        * visible - just for the requestFocus, when
        * it comes too early.
        */
        public void componentShown (ComponentEvent ev) {
            // notify me no more
            component.removeComponentListener(this);
            // now request a focus
            SwingUtilities.invokeLater(new Runnable() {
                                           public void run () {
                                               component.requestFocus();
                                           }
                                       });
        }

        /** Accessor to the versioned serialization manager */
        private VersionSerializator serializationManager () {
            if (serializationManager == null) {
                serializationManager = createSerializationManager();
            }
            return serializationManager;
        }

        /** Creates new serialization manager filled with our versions */
        private static VersionSerializator createSerializationManager () {
            VersionSerializator result = new VersionSerializator();
            result.putVersion(new Version1());
            return result;
        }

        /** Serialization */
        private Object writeReplace ()
        throws ObjectStreamException {
            // provide version with data
            Version1 version =
                (Version1)serializationManager().getVersion(Version1.NAME);
            try {
                version.assignData(this, new NbMarshalledObject(component));
            } catch (Exception exc) {
                notifyPersistenceError(exc, component.getName(), false);
                // write null as a marker that there was serialization problem
                return null;
            }
            // use replacer
            return new DefaultReplacer(new VSAccess(serializationManager()));
        }

        /** Called when first phase of WS deserialization is done.
        * Deserialization of managed TC is finished here.
        */
        public boolean validateData () {
            // deserialize only once, then only return succes or failure
            // status
            if (innerState != INVALID) {
                return innerState == READY;
            }
            // component written badly, consider it a failure
            if (marshalledComponent == null) {
                innerState = FAILED;
                return false;
            }
            // Actually deserializes top component.
            // Uses ugly hack - redundant TopComponent<init>.getManager() call
            // to enforce wm.createTopComponentManager() to be called,
            // in which we can re-asociate deserialized top component
            // with this manager.
            // This is UGLY implementation, based on the fact that there's
            // no way to set new manager for top component and we want to
            // keep the Open API unchanged if possible
            try {
                wm.setValidatedManager(this);
                component = (TopComponent)marshalledComponent.get();
                // component can resolve itself to null,
                // be prepared to such situation
                /*if (component != null) {
                  component.getActivatedNodes();
            }*/
                innerState = (component == null) ? FAILED : READY;
                if ((component != null) && (tempNodes != null)) {
                    setActivatedNodes(tempNodes);
                    tempNodes = null;
                }
            } catch (Exception exc) {
                // do catch all exceptions, because exceptions
                // during deserialization of one top component
                // shoudn't break whole window system
                innerState = FAILED;
                notifyPersistenceError(exc, componentName, true);
                return false;
            } finally {
                // ensure that manager is not set anymore
                wm.setValidatedManager(null);
            }
            return innerState == READY;
        }

        /** Notify user about exception during top component
        * serialization or deserialization */
        private void notifyPersistenceError (final Exception exc,
                                             final String tcName,
                                             final boolean reading) {
            Runnable performer = new Runnable () {
                                     public void run () {
                                         if (System.getProperty("netbeans.debug.exceptions") != null) {
                                             exc.printStackTrace();
                                         }
                                         String message = null;
                                         if (reading) {
                                             message = MessageFormat.format(
                                                           NbBundle.getBundle(WindowManagerImpl.class).getString("FMT_TCReadError"),
                                                           new Object[] { tcName }
                                                       );
                                         } else {
                                             message = MessageFormat.format(
                                                           NbBundle.getBundle(WindowManagerImpl.class).getString("FMT_TCWriteError"),
                                                           new Object[] { tcName }
                                                       );
                                         }
                                         TopManager.getDefault().notify(
                                             new NotifyDescriptor.Exception(exc, message)
                                         );
                                     }
                                 };
            if (reading) {
                SwingUtilities.invokeLater(performer);
            } else {
                performer.run();
            }
        }

        /** Basic version of persistence for mode implementation.
        * Method assignData(modeImpl) must be called prior to serialization */
        private static final class Version1
            implements DefaultReplacer.ResVersionable {

            /* identification string */
            public static final String NAME = "Version_1.0"; // NOI18N

            /** variables of persistent state of the tc manager */
            String componentName;
            NbMarshalledObject marshalledComponent;
            /** asociation with outerclass, used when writing */
            TopComponentManager tcm;
            /** set of workspaces where managed tc is opened */
            transient HashSet whereOpened;

            /** Identification of the version */
            public String getName () {
                return "Version_1.0"; // NOI18N
            }

            /** Assigns data to be written. Must be called before writing */
            public void assignData (TopComponentManager tcm,
                                    NbMarshalledObject marshalledComponent) {
                this.tcm = tcm;
                this.marshalledComponent = marshalledComponent;
            }

            /** read the data of the version from given input */
            public void readData (ObjectInput in)
            throws IOException, ClassNotFoundException {
                // read the fields
                componentName = (String)in.readObject();
                marshalledComponent = (NbMarshalledObject)in.readObject();
            }

            /** write the data of the version to given output */
            public void writeData (ObjectOutput out)
            throws IOException {
                out.writeObject(tcm.getComponent().getName());
                // write top component itself using marshalled object
                out.writeObject(marshalledComponent);
            }

            /** Resolves this object to top component manager instance again */
            public Object resolveData ()
            throws ObjectStreamException {
                TopComponentManager result = new TopComponentManager(null);
                result.componentName = componentName;
                result.marshalledComponent = marshalledComponent;
                result.innerState = INVALID;
                return result;
            }

        } // end of Version1 inner class

        /** Implementation of persistent access to our version serializator */
        private static final class VSAccess implements DefaultReplacer.Access {

            /** serialVersionUID */
            private static final long serialVersionUID = -6484558550904999459L;
            
            /** version serializator, used only during writing */
            transient VersionSerializator vs;

            public VSAccess (VersionSerializator vs) {
                this.vs = vs;
            }

            public VersionSerializator getVersionSerializator () {
                return (vs == null) ? createSerializationManager() : vs;
            }

        } // end of VSAccess inner class


    } // end of TopComponentManager inner class

}

/*
* Log
*  47   Gandalf   1.46        3/8/00   David Simonek   bugfix - exception in 
*       explore from here operation
*  46   Gandalf   1.45        2/25/00  David Simonek   #5859 bugfix
*  45   Gandalf   1.44        2/25/00  Petr Jiricka    Bugfix 5603, see this for
*       evaluation and description of fix.
*  44   Gandalf   1.43        2/18/00  David Simonek   #5708 bugfix
*  43   Gandalf   1.42        2/15/00  David Simonek   bug after unmount of 
*       opened editor files fixed (hopefully..:-)
*  42   Gandalf   1.41        1/17/00  David Simonek   SerializationReplacer 
*       serialization UID added 
*  41   Gandalf   1.40        1/17/00  Jesse Glick     No more 
*       TemplatesExplorer.
*  40   Gandalf   1.39        1/15/00  David Simonek   popup menu *stuck* bug 
*       fixed, mutliwindow title bug fixed
*  39   Gandalf   1.38        1/13/00  David Simonek   i18n
*  38   Gandalf   1.37        1/12/00  Ian Formanek    NOI18N
*  37   Gandalf   1.36        1/9/00   David Simonek   modified initialization 
*       of the WindowManagerImpl
*  36   Gandalf   1.35        12/23/99 David Simonek   last activated 
*       registratiron modified
*  35   Gandalf   1.34        12/17/99 David Simonek   #1913, #2970
*  34   Gandalf   1.33        12/6/99  David Simonek   property sheet now opens 
*       automatically on editing workspace, other positioning updated
*  33   Gandalf   1.32        11/10/99 David Simonek   debug comments removed
*  32   Gandalf   1.31        11/6/99  David Simonek   serialization bug fixing
*  31   Gandalf   1.30        11/4/99  David Simonek   ws serialization bugfixes
*  30   Gandalf   1.29        11/3/99  David Simonek   ws now respects resolving
*       of tc to null again
*  29   Gandalf   1.28        11/3/99  David Simonek   completely rewritten 
*       serialization of windowing system...
*  28   Gandalf   1.27        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  27   Gandalf   1.26        10/8/99  David Simonek   serialization of window 
*       manager impl is now done in SerializationReplacer  (solves problems in 
*       projects module)
*  26   Gandalf   1.25        10/7/99  David Simonek   debug prints removed...
*  25   Gandalf   1.24        10/6/99  David Simonek   more robust serialization
*       of window system (especially editor TCs)
*  24   Gandalf   1.23        9/8/99   David Simonek   deferred opening and 
*       firing of selected nodes, state management
*  23   Gandalf   1.22        8/19/99  David Simonek   unfinaled parameters
*  22   Gandalf   1.21        8/17/99  David Simonek   persistent main window 
*       positioning issues
*  21   Gandalf   1.20        8/14/99  David Simonek   bugfixes, #3347, #3274 
*       etc.
*  20   Gandalf   1.19        8/9/99   David Simonek   
*  19   Gandalf   1.18        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  18   Gandalf   1.17        8/9/99   Miloslav Metelka Paste action enabling in 
*       editor
*  17   Gandalf   1.16        8/1/99   David Simonek   debug prints commented
*  16   Gandalf   1.15        8/1/99   David Simonek   
*  15   Gandalf   1.14        7/31/99  David Simonek   small additions 
*  14   Gandalf   1.13        7/30/99  David Simonek   serialization fixes
*  13   Gandalf   1.12        7/30/99  David Simonek   iconification bugfixes, 
*       focus bugfixes
*  12   Gandalf   1.11        7/29/99  David Simonek   further ws serialization 
*       changes
*  11   Gandalf   1.10        7/28/99  David Simonek   workspace serialization 
*       bugfixes
*  10   Gandalf   1.9         7/28/99  David Simonek   serialization of window 
*       system...first draft :-)
*  9    Gandalf   1.8         7/23/99  David Simonek   another fixes (closing a 
*       component)
*  8    Gandalf   1.7         7/22/99  Libor Kramolis  
*  7    Gandalf   1.6         7/21/99  David Simonek   window system updates...
*  6    Gandalf   1.5         7/20/99  David Simonek   various window system 
*       updates
*  5    Gandalf   1.4         7/16/99  Ales Novak      bugfix
*  4    Gandalf   1.3         7/16/99  Ales Novak      windows placing
*  3    Gandalf   1.2         7/15/99  Ales Novak      nodes actions 
*       enable/disable
*  2    Gandalf   1.1         7/13/99  Ales Novak      optimized
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/
