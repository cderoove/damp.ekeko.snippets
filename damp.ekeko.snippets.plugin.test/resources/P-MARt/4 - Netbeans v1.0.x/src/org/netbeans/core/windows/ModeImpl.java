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

import java.awt.Rectangle;
import java.awt.Image;
import java.awt.Frame;
import java.awt.Toolkit;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Set;
import java.util.ArrayList;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.EventObject;
import java.awt.event.ComponentEvent;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.openide.windows.*;
import org.openide.util.Mutex;
import org.openide.util.io.SafeException;
import org.openide.TopManager;

import org.netbeans.core.windows.util.*;

/** This class is an implementation of Mode interface.
* It's responsible for managing top component container.
* This implementation supports switching of various types of
* top component containers.
*
* @author Dafe Simonek
*/
public final class ModeImpl implements Mode, ContainerListener,
    DeferredPerformer.DeferredCommand {

    /** serial version UID */
    static final long serialVersionUID = 2721445375634234372L;

    /** Constant representing single type of the mode */
    public static final int SINGLE = 1;
    /** Constant representing multi tabbed type of the mode */
    public static final int MULTI_TAB = 2;
    /** For future use */
    public static final int SIDE_BY_SIDE = 4;
    /** For future use */
    public static final int INTERNAL_FRAME = 8;

    /** Top components changes property name */
    public static final String PROP_TOP_COMPONENTS = "topComponents"; // NOI18N

    /** Icon of the mode */
    URL icon;
    /** icon as an image, created from URL */
    Image iconImage;
    /** Programmatic unique name of the mode */
    String name;
    /** Human presentable name of the mode */
    String displayName;
    /** The bounds of the mode - should be kept synchronized with
    * top component container when container exists and visible */ 
    Rectangle bounds;
    /** Current container type asociated with this mode */
    int containerType;
    /** true if this mode was created by user, not system */
    boolean userDefined;
    /** The workspace which this mode belongs to */
    Workspace workspace;
    /** Name of workspace which this mode belongs to.
    * Used only during deserialization when delayed validation is needed */
    String workspaceName;

    /** Composited container for top components. Can be null if no top
    * components are currently in this mode */
    transient TopComponentContainer tcc;
    /** Set which holds weak references to the top components which
    * are docked in this mode but closed 
    * @associates WeakReference*/
    transient HashSet closedComponents;
    /** Asociation with window manager implementation */
    private transient WindowManagerImpl wm;
    /** asociated property change support for firing property changes */
    private transient PropertyChangeSupport changeSupport;
    /** helper variable, true when asociated top component container
    * is showing on the screen, false otherwise */
    private transient boolean showing;
    /** helper variable, true when there is some pending focus request */
    private transient boolean deferredFocusRequest;
    /** helper variable, holds top component as context of pending
    * focus request */
    private transient TopComponent compToReceiveFocus;
    /** Listener to the changes of the name of top component */
    private transient NameListener nameListener;
    /** Helper variable which holds  */
    private transient String oldDisplayName;


    /** manager of versioned serialization */
    private static VersionSerializator serializationManager;

    /** Construct new mode with given properties */
    public ModeImpl (String name, String displayName, URL icon,
                     int containerType, boolean userDefined,
                     Workspace workspace) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.workspace = workspace;
        this.containerType = containerType;
        this.userDefined = userDefined;
        initialize();
    }

    /** Creates new mode as a shallow copy of original mode on
    * given workspace. New mode will have the same characterists,
    * (displayname, icon, container type, bounds).
    * Contained components are NOT taken from original.
    * The name can differ (name uniquennes is ensured automatically).
    */
    public ModeImpl (Workspace workspace, ModeImpl original) {
        this(original.name, original.displayName, original.icon,
             original.containerType, original.userDefined, workspace);
        // set unigue name
        if (workspace.findMode(name) != null) {
            int i = 1;
            while (workspace.findMode(name = original.name + "_" + i++) != null); // NOI18N
        }
        // bounds
        this.bounds = (original.bounds == null) ? null
                      : new Rectangle(original.bounds);
    }

    /** Initialization, called from constructors and deserialization */
    private void initialize () {
        this.wm = (WindowManagerImpl)TopManager.getDefault().getWindowManager();
        changeSupport = new PropertyChangeSupport(this);
    }

    /** Get the programmatic name of the mode.
     * This name should be unique, as it is used to find modes etc.
     * @return programmatic name of the mode
     */
    public String getName () {
        return name;
    }

    /* @return true if given top component can be docked to this mode,
    * false otherwise */
    public boolean canDock (TopComponent tc) {
        return (tcc == null) ? true : tcc.canAdd(tc);
    }

    /** Attaches a component to a mode for this workspace.
    * If the component is in different mode on this desktop, it is 
    * removed from the original and moved to this one.
    *
    * @param c top component to dock into this mode
    * @return true if top component was succesfully docked to this
    * mode, false otherwise */
    public boolean dockInto (final TopComponent tc) {
        if (!canDock(tc))
            return false;
        final ModeImpl mi = (ModeImpl)workspace.findMode(tc);
        // find out if component is opened
        boolean opened = wm.findManager(tc).isOpened(workspace);
        // System.out.println("Tc in mode: " + ((mi == null) ? "null" : mi.getName())); // NOI18N
        if ((mi != null) && (!this.equals(mi))) {
            mi.release(tc);
        }
        if (opened) {
            // compute mode bounds if not known yet
            if (bounds == null) {
                ((WorkspaceImpl)workspace).placeMode(this, tc);
            }
            // remove top component in a case it was previously
            // docked in this mode in closed state
            removeClosedComponent(tc);
            if (tcc == null)
                createContainer();
            // synchronize opening with AWT Event queue
            Mutex.EVENT.readAccess(new Runnable () {
                                       public void run () {
                                           if (tcc.getState() == Frame.ICONIFIED) {
                                               tcc.setState(Frame.NORMAL);
                                           }
                                           // add only if not re-opened multiple times
                                           if (!this.equals(mi)) {
                                               tcc.addTopComponent(tc);
                                               updateNameListener();
                                           }
                                           // show container if we are on current workspace only
                                           if (workspace.equals(wm.getCurrentWorkspace()))
                                               setVisible(true);
                                           //System.out.println(tc.getName() + " docked to " + getName()); // NOI18N
                                           // notify
                                           changeSupport.firePropertyChange(
                                               PROP_TOP_COMPONENTS, null, null);
                                       }
                                   });
        } else {
            // component not opened
            addClosedComponent(tc);
            // notify
            changeSupport.firePropertyChange(PROP_TOP_COMPONENTS, null, null);
        }
        return true;
    }

    /** Releases given top component from current association
    * with this mode.
    */
    public void release (TopComponent tc) {
        // find out if component is opened
        if (wm.findManager(tc).isOpened(workspace)) {
            int left = tcc.removeTopComponent(tc);
            if (left <= 0) {
                destroyContainer();
            } else {
                updateNameListener();
            }
        } else {
            removeClosedComponent(tc);
        }
        // notify
        changeSupport.firePropertyChange(PROP_TOP_COMPONENTS, null, null);
    }

    /** Closes given top component.
    * Closing here means removing from top component container and
    * adding to the set of closed components docked in this mode.
    */
    public void close (TopComponent tc) {
        if (tcc.containsTopComponent(tc)) {
            int left = tcc.removeTopComponent(tc);
            if (left <= 0) {
                destroyContainer();
            } else {
                updateNameListener();
            }
            addClosedComponent(tc);
        }
        // notify
        changeSupport.firePropertyChange(PROP_TOP_COMPONENTS, null, null);
    }

    /** Closes this mode - it means, that opened top components
    * are closed and closed components docked to this mode are
    * removed 
    * @return true if mode was succesfully cleared and closed,
    * false if some top component refused to close
    */
    public boolean close () {
        // clear closed components
        if (closedComponents != null)
            closedComponents.clear();
        if (tcc == null)
            return true;
        // try to close all opened top components
        // selected top component will be closed at last
        // to prevent from focus transfering between components during closing
        TopComponent[] tcs = tcc.getTopComponents();
        TopComponent selected = tcc.getSelectedTopComponent();
        boolean result = true;
        for (int i = 0; i < tcs.length; i++) {
            if ((!tcs[i].equals(selected)) && (!tcs[i].close(workspace))) {
                result = false;
            }
        }
        // close selected top component, if possible
        if ((selected != null) && (!selected.close(workspace))) {
            result = false;
        }
        return result;
    }

    /** Sets the bounds of the mode.
     * @param s the bounds for the mode 
     */
    public void setBounds (Rectangle rect) {
        if ((bounds != null) && (bounds.equals(rect))) {
            return;
        }
        Rectangle old = bounds;
        bounds = rect;
        // notify top component container if possible
        if (tcc != null) {
            tcc.setBounds(bounds);
        }
        // notify others interested
        changeSupport.firePropertyChange(PROP_BOUNDS, old, bounds);
    }

    /** Getter for current bounds of the mode.
     * @return the bounds of the mode
     */
    public Rectangle getBounds () {
        return bounds;
    }

    /** Getter for asociated workspace.
     * @return The workspace instance to which is this mode asociated.
     */
    public Workspace getWorkspace () {
        return workspace;
    }

    /** @return array of top components which are currently
     * docked in this mode. May return empty array if no top component
     * is docked in this mode.
     */
    public TopComponent[] getTopComponents () {
        TopComponent[] containerTcs =
            (tcc == null) ? new TopComponent[0] : tcc.getTopComponents();
        if (closedComponents == null)
            return containerTcs;
        // merge container and closed tcs into one array
        TopComponent[] result = null;
        synchronized (this) {
            WeakReference[] closedRefs =
                (WeakReference[])closedComponents.toArray(new WeakReference[0]);
            HashSet closed = new HashSet(closedRefs.length);
            TopComponent curTc = null;
            for (int i = 0; i < closedRefs.length; i++) {
                curTc = (TopComponent)closedRefs[i].get();
                // remove from set if not valid reference
                if (curTc != null)
                    closed.add(curTc);
                else
                    closedComponents.remove(curTc);
            }
            // merge!
            result = new TopComponent[containerTcs.length + closed.size()];
            System.arraycopy(containerTcs, 0, result, 0, containerTcs.length);
            System.arraycopy(closed.toArray(new TopComponent[closed.size()]), 0,
                             result, containerTcs.length, closed.size());
        }
        return result;
    }

    /** @return an array of opened top components currently
    * docked in this mode.
    */
    public TopComponent[] getOpenedTopComponents () {
        return (tcc == null) ? new TopComponent[0] : tcc.getTopComponents();
    }

    /** The component requests focus. Request can be delayed
    * if top component containe ris not in consistent state.
    */
    public void requestFocus (TopComponent comp) {
        if (!showing) {
            deferredFocusRequest = true;
            compToReceiveFocus = comp;
        } else {
            tcc.requestFocus(comp);
        }
    }

    /** Requests focus for whole mode. Request can be delayed
    * if top component containe ris not in consistent state.
    */
    public void requestFocus () {
        if (!showing) {
            deferredFocusRequest = true;
        } else {
            tcc.requestFocus();
        }
    }

    /** @return TopComponentContainer - that is JInternalFram for instane.
    * Can return null if no container is asociated at present.
    * (it means that no opened top component is docked in this mode) */
    public TopComponentContainer getTopComponentContainer() {
        return tcc;
    }

    /** @return Human presentable name of this mode implementation */
    public String getDisplayName () {
        return displayName;
    }

    /** Sets new display name of this mode */
    public void setDisplayName (String s) {
        if (s == displayName) {
            return;
        }
        String old = displayName;
        displayName = s;
        changeSupport.firePropertyChange(PROP_DISPLAY_NAME, old, displayName);
    }

    /** @return icon of this mode */
    public Image getIcon () {
        if ((iconImage == null) && (icon != null)) {
            try {
                iconImage = Toolkit.getDefaultToolkit().getImage(icon);
            } catch (Exception exc) {
                // PENDING
                // in jdk 1.3, strange exceptions are thrown for some
                // reason that I couldn't find....ignore exceptions for now
            }
        }
        return iconImage;
    }

    public URL getIconURL () {
        return icon;
    }

    /** Updates UI of asociated top component container,
    * if possible */
    public void updateUI () {
        if (tcc != null)
            tcc.updateUI();
    }

    /** Sets the state of asociated top component container
    */
    public void setState (int state) {
        if (tcc != null)
            tcc.setState(state);
    }

    /** Shows or hides asociated top component container,
    * if possible (if container exists)
    */
    public void setVisible (boolean state) {
        if ((tcc != null) && (state != tcc.isVisible())) {
            if (!state || shouldShowTcc()) {
                tcc.setVisible(state);
            }
        }
    }

    /** @return true if it is ok to show top component container
    * false otherwise (all top components haven't been opened yet)
    * Called from setVisible.
    */
    private boolean shouldShowTcc () {
        TopComponent[] tcs = tcc.getTopComponents();
        for (int i = 0; i < tcs.length; i++) {
            if (WindowManagerImpl.findManager(tcs[i]).isOpened()) {
                return true;
            }
        }
        return false;
    }

    /** @return true if no component is docked to this mode
    * or all components docked to this mode are closed */
    public boolean isOrphan () {
        return (tcc == null) || (tcc.getTopComponents().length <= 0);
    }

    /** @return true if this mode is currently in the state where
    * it contains exactly one OPENED top component.
    * (it can contains any number of closed components but this method
    * will still return true if there is exatly one opened)
    */
    public boolean isSingle () {
        return (tcc != null) && (tcc.getTopComponents().length == 1);
    }

    /** @return true if mode is user defined, false otherwise
    * (defined programmatically) */
    public boolean isUserDefined () {
        return userDefined;
    }

    /** Fills this mode with top components contained in given source
    * mode.
    */
    public void fillTopComponents (ModeImpl source) {
        // copy references to closed top components
        if (source.closedComponents != null) {
            WeakReference curRef = null;
            TopComponent curTc = null;
            for (Iterator iter = source.closedComponents.iterator(); iter.hasNext(); ) {
                curRef = (WeakReference)iter.next();
                curTc = (TopComponent)curRef.get();
                if (curTc != null) {
                    dockInto(curTc);
                }
            }
        }
        // copy references to opened top components (and reopen them)
        TopComponent[] tcs = source.getOpenedTopComponents();
        for (int i = 0; i < tcs.length; i++) {
            dockInto(tcs[i]);
            tcs[i].open(workspace);
        }
    }

    /**** Implementation of container listener - we listen to the
    * events in container and react properly */

    /** When container is being deactivated */
    public void containerDeactivated (EventObject eo) {
    }

    /** When container was closed */
    public void containerClosed (EventObject eo) {
    }

    /** When container was brought bacjk from icon to normal state */
    public void containerDeiconified (EventObject eo) {
    }

    /** When container was opened - showed.
    * Checks if there are some pending focus requests and if so,
    * calls one of the requestFocus methods properly */
    public void containerOpened (EventObject eo) {
    }

    /** When container was iconified */
    public void containerIconified (EventObject eo) {
    }

    /** User attempted to close the container.
    * So try to close all contained top components, if they agree.
    */
    public void containerClosing (EventObject eo) {
        TopComponent[] tcs = tcc.getTopComponents();
        TopComponent selected = tcc.getSelectedTopComponent();
        boolean shouldClose = true;
        for (int i = 0; i < tcs.length; i++) {
            if ((!tcs[i].equals(selected)) && (!tcs[i].close())) {
                shouldClose = false;
            }
        }
        // close selected top component, if possible
        if ((selected != null) && (!selected.close())) {
            shouldClose = false;
        }
        // close container if nothing left
        if (shouldClose) {
            destroyContainer();
        }
    }

    /** When container was made active - receives focus */
    public void containerActivated (EventObject eo) {

    }

    /** When container was resized, its size was changed */
    public void containerResized (ComponentEvent ce) {
        setBounds(ce.getComponent().getBounds());
    }

    /** When container was moved, its position was changed */
    public void containerMoved (ComponentEvent ce) {
        setBounds(ce.getComponent().getBounds());
    }

    /** Called when container was shown. */
    public void containerShown (ComponentEvent ce) {
        showing = true;
        if (deferredFocusRequest) {
            deferredFocusRequest = false;
            if (compToReceiveFocus != null) {
                requestFocus(compToReceiveFocus);
                compToReceiveFocus = null;
            } else {
                requestFocus();
            }
        }
    }

    /** Called when container was hidden. */
    public void containerHidden (ComponentEvent ce) {
        showing = false;
    }

    /** Sets new container type and transfers content
    * of current container to the new one
    * @return true if new container was switched succesfully
    */
    public boolean setContainerType (int containerType) {
        this.containerType = containerType;
        // PENDING - to be done - create new container, transfer components
        // destroy old one if component container already exist
        return true;
    }

    /** @return current container type */
    public int getContainerType () {
        return containerType;
    }

    /** Add listener to the property changes */
    public void addPropertyChangeListener (PropertyChangeListener pchl) {
        changeSupport.addPropertyChangeListener(pchl);
    }

    /** Remove listener to the property changes */
    public void removePropertyChangeListener (PropertyChangeListener pchl) {
        changeSupport.removePropertyChangeListener(pchl);
    }

    /** Adds top component to the set of closed tcs docked
    * in this mode */ 
    void addClosedComponent (TopComponent tc) {
        if (closedComponents == null)
            closedComponents = new HashSet(10);
        closedComponents.add(new WeakReference(tc));
    }

    /** Removes top component from the set of closed tcs docked
    * in this mode */ 
    void removeClosedComponent (TopComponent tc) {
        if (closedComponents == null)
            return;
        TopComponent curTc = null;
        // remove found item plus all garbage collected
        WeakReference[] weakRefs =
            (WeakReference[])closedComponents.toArray(new WeakReference[0]);
        for (int i = 0; i < weakRefs.length; i++) {
            curTc = (TopComponent)weakRefs[i].get();
            if ((curTc == null) || (curTc.equals(tc)))
                closedComponents.remove(weakRefs[i]);
        }
    }

    /** @return Newly created top component container */
    synchronized void createContainer () {
        if (tcc != null)
            return;
        switch (containerType) {
        case SINGLE:
            tcc = new MultiTabContainer(this);
            ((MultiTabContainer)tcc).setMaxCount(1);
            break;
        case MULTI_TAB:
            tcc = new MultiTabContainer(this);
            break;
        case SIDE_BY_SIDE:
            // not implemented yet
            break;
        case INTERNAL_FRAME:
            // not implemented yet
            break;
        }
        tcc.addContainerListener(this);
    }

    /** Destroys asociated top component container */
    void destroyContainer () {
        if (tcc == null)
            return;

        updateNameListener ();
        tcc.removeContainerListener(this);
        tcc.dispose();
        tcc = null;
    }

    /** Asigns or removes listener to the name of the top component.
    * If mode is single, its display name should be the same as
    * the name of contained component. */
    private void updateNameListener () {
        if (isSingle()) {
            if (nameListener == null) {
                nameListener = new NameListener();
            }
            // save display name before listener activation
            oldDisplayName = displayName;
            nameListener.activate();
        } else {
            if (nameListener != null) {
                nameListener.passivate();
                if ((oldDisplayName != null) && !"".equals(oldDisplayName.trim())) { //NOI18N
                    setDisplayName(oldDisplayName);
                }
            }
        }
    }

    /** Just for testing...
    */
    protected void finalize () throws Throwable {
        // System.out.println ("Finalizing Mode: " + name); // NOI18N
        super.finalize();
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

    /** Let instance of properly parametrized DefaultReplacer to keep
    * persistent state of this workspace
    */
    private Object writeReplace ()
    throws ObjectStreamException {
        // provide version with data
        Version1 version =
            (Version1)serializationManager().getVersion(Version1.NAME);
        version.assignData(this);
        // use replacer
        return new DefaultReplacer(new VSAccess(serializationManager()));
    }

    /** Called when first phase of WS deserialization is done.
    * Validates asociation with its workspace.
    */
    void validateSelf (Workspace workspace) {
        this.workspace = workspace;
        /*if (workspace == null) {
          workspace = TopManager.getDefault().getWindowManager().
                      findWorkspace(workspaceName);
    }*/
    }

    /** Called when first phase of WS deserialization is done.
    * Validates its top component container, if possible
    */
    void validateData () {
        // validate tcc, if possible
        if (tcc != null) {
            tcc.validateData();
            if (tcc.getTopComponents().length > 0) {
                tcc.addContainerListener(this);
                // put a request for reopening all top components in
                // the container (actual opening is delayed and performed
                // after deserializaton)
                TopComponent[] tcs = tcc.getTopComponents();
                DeferredOpenContext openContext = null;
                for (int i = 0; i < tcs.length; i++) {
                    openContext = new DeferredOpenContext();
                    openContext.tc = tcs[i];
                    openContext.workspace = workspace;
                    WindowManagerImpl.deferredPerformer().putRequest(this, openContext);
                }
            } else {
                tcc = null;
            }
        }
    }

    /** Implementation of DeferredPerformer.DeferredCommand interface.
    * Actually opens managed top component. */
    public void performCommand (Object context) {
        DeferredOpenContext openContext = (DeferredOpenContext)context;
        openContext.tc.open(openContext.workspace);
    }

    /** Holds data context for delayed opening */
    private static final class DeferredOpenContext {
        TopComponent tc;
        Workspace workspace;
    }

    /** Basic version of persistence for mode implementation.
    * Method assignData(modeImpl) must be called prior to serialization */
    private static final class Version1
        implements DefaultReplacer.ResVersionable {

        /* identification string */
        public static final String NAME = "Version_1.0"; // NOI18N

        /** variables of persistent state of the mode implementation */
        String name;
        String displayName;
        Rectangle bounds;
        URL icon;
        int containerType;
        boolean userDefined;
        String workspaceName;
        TopComponentContainer tcc;

        /** asociation with mode implementation, used when writing */
        ModeImpl mode;

        /** Identification of the version */
        public String getName () {
            return "Version_1.0"; // NOI18N
        }

        /** Assigns data to be written. Must be called before writing */
        public void assignData (ModeImpl mode) {
            this.mode = mode;
        }

        /** read the data of the version from given input */
        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException {
            // read mode fields
            name = (String)in.readObject();
            displayName = (String)in.readObject();
            bounds = (Rectangle)in.readObject();
            icon = (URL)in.readObject();
            containerType = ((Integer)in.readObject()).intValue();
            userDefined = ((Boolean)in.readObject()).booleanValue();
            workspaceName = (String)in.readObject();
            tcc = (TopComponentContainer)in.readObject();
        }

        /** write the data of the version to given output */
        public void writeData (ObjectOutput out)
        throws IOException {
            // write mode fields
            out.writeObject(mode.name);
            out.writeObject(mode.displayName);
            out.writeObject(mode.bounds);
            out.writeObject(mode.icon);
            out.writeObject(new Integer(mode.containerType));
            out.writeObject(new Boolean(mode.userDefined));
            out.writeObject(mode.workspace.getName());
            out.writeObject(mode.tcc);
        }

        public Object resolveData ()
        throws ObjectStreamException {
            Workspace workspace =
                TopManager.getDefault().getWindowManager().findWorkspace(workspaceName);
            ModeImpl result =
                (workspace == null) ? null : (ModeImpl)workspace.findMode(name);
            if (result == null) {
                // mode don't exist, create new one and fill it
                result = new ModeImpl(name, displayName, icon, containerType,
                                      userDefined, workspace);
                result.bounds = bounds;
                result.tcc = tcc;
                // assign workspace if needed for later validation
                if (workspace == null) {
                    result.workspaceName = workspaceName;
                }
            }
            return result;
        }

    } // end of Version1 inner class

    /** Implementation of persistent access to our version serializator */
    private static final class VSAccess implements DefaultReplacer.Access {
        /** version serializator, used only during writing */
        transient VersionSerializator vs;

        /** serialVersionUID */
        private static final long serialVersionUID = -7577235918945664917L;

        public VSAccess (VersionSerializator vs) {
            this.vs = vs;
        }

        public VersionSerializator getVersionSerializator () {
            return (vs == null) ? createSerializationManager() : vs;
        }

    } // end of VSAccess inner class

    private final class NameListener implements PropertyChangeListener {

        WindowManagerImpl.TopComponentManager tcm;

        void activate () {
            if (tcm == null) {
                tcm = wm.findManager(tcc.getTopComponents()[0]);
                tcm.addPropertyChangeListener(this);
            }
        }

        void passivate () {
            if (tcm != null) {
                tcm.removePropertyChangeListener(this);
                tcm = null;
            }
        }

        public void propertyChange (PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (WindowManagerImpl.TopComponentManager.PROP_NAME.equals(propName)) {
                // name of top component changed, update display name of the mode
                setDisplayName(tcm.getComponent().getName());
            }
        }

    } // end of NameListener inner class

}

/*
* Log
*  30   Gandalf   1.29        1/17/00  David Simonek   topcomponent.open upon 
*       deserialization now delayed automatically
*  29   Gandalf   1.28        1/16/00  Jaroslav Tulach Memory Leak Fix.
*  28   Gandalf   1.27        1/15/00  David Simonek   mutliwindow title bug 
*       fixed
*  27   Gandalf   1.26        1/13/00  David Simonek   i18n
*  26   Gandalf   1.25        1/12/00  Ian Formanek    NOI18N
*  25   Gandalf   1.24        12/17/99 David Simonek   #1913, #2970
*  24   Gandalf   1.23        11/30/99 David Simonek   neccessary changes needed
*       to change main explorer to new UI style  (tabs are full top components 
*       now, visual workspace added, layout of editing workspace chnaged a bit)
*  23   Gandalf   1.22        11/6/99  David Simonek   serialization bug fixing
*  22   Gandalf   1.21        11/4/99  David Simonek   ws serialization bugfixes
*  21   Gandalf   1.20        11/3/99  David Simonek   completely rewritten 
*       serialization of windowing system...
*  20   Gandalf   1.19        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  19   Gandalf   1.18        10/6/99  David Simonek   more robust serialization
*       of window system (especially editor TCs)
*  18   Gandalf   1.17        9/13/99  David Simonek   request focus for the 
*       mode added
*  17   Gandalf   1.16        8/15/99  David Simonek   initialization in 
*       readObject() returned back (how could it dissapear??)
*  16   Gandalf   1.15        8/14/99  David Simonek   bugfixes, #3347, #3274 
*       etc.
*  15   Gandalf   1.14        8/10/99  Ian Formanek    removed debug printlns
*  14   Gandalf   1.13        8/9/99   David Simonek   
*  13   Gandalf   1.12        7/31/99  David Simonek   small additions 
*  12   Gandalf   1.11        7/30/99  David Simonek   multiple serialization 
*       bugfix
*  11   Gandalf   1.10        7/30/99  David Simonek   serialization fixes
*  10   Gandalf   1.9         7/29/99  David Simonek   further ws serialization 
*       changes
*  9    Gandalf   1.8         7/28/99  David Simonek   workspace serialization 
*       bugfixes
*  8    Gandalf   1.7         7/28/99  David Simonek   serialization of window 
*       system...first draft :-)
*  7    Gandalf   1.6         7/23/99  David Simonek   another fixes (closing a 
*       component)
*  6    Gandalf   1.5         7/21/99  David Simonek   
*  5    Gandalf   1.4         7/21/99  David Simonek   window system updates...
*  4    Gandalf   1.3         7/20/99  David Simonek   various window system 
*       updates
*  3    Gandalf   1.2         7/14/99  Ales Novak      bugfixes  
*  2    Gandalf   1.1         7/12/99  Jaroslav Tulach To be compilable.
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/