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
import java.awt.event.*;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.SwingConstants;

import org.openide.loaders.DataObject;
import org.openide.awt.UndoRedo;
import org.openide.awt.MouseUtils;
import org.openide.awt.JPopupMenuPlus;
import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.windows.*;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.Task;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.SafeException;
import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeAdapter;

import org.netbeans.core.actions.DockingAction;
import org.netbeans.core.NbNodeOperation;
import org.netbeans.core.windows.util.*;

/** Implementation of multi-tabbed top component container.
* The window should be invisible if there is no component in it.
*/
final class MultiTabContainer extends JFrame
            implements TopComponentContainer,
            ChangeListener, ActionPerformer, PropertyChangeListener,
    WindowListener {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5803032916590052467L;

    /** Clone action */
    static CloneViewAction clone =
        (CloneViewAction)SystemAction.get(CloneViewAction.class);
    /** Undock action */
    static UndockAction undock =
        (UndockAction)SystemAction.get(UndockAction.class);
    /** Nexttab action */
    static NextTabAction nextTab =
        (NextTabAction)SystemAction.get(NextTabAction.class);
    /** Previoustab action */
    static PreviousTabAction prevTab =
        (PreviousTabAction)SystemAction.get(PreviousTabAction.class);
    /** Close view action */
    static CloseViewAction closeView =
        (CloseViewAction)SystemAction.get(CloseViewAction.class);
    /** Docking action */
    static DockingAction docking =
        (DockingAction)SystemAction.get(DockingAction.class);

    /** default workspace name */
    static MessageFormat windowTitle;
    /** default image icon for multi tabbed frame */
    static Image defaultIcon;
    /** Default name for the components with no name */
    static String untitledComponent;
    /** Time delta between two successive calls to name change method */
    static final int NAME_CHANGE_DELTA = 1000;
    /** Component listener that watches if the component is shown and
    * if so, it sets it as the selected one */
    transient ComponentListener compL;
    /** Asociation with window manager implementation */
    transient WindowManagerImpl wm;
    /** This flag represents the internal state of visibility.
    * It can be ignored if MultiObjectFrame has no TopPanel inside */
    transient boolean visibility = false;
    /** Is true if this Frame has focus. */
    transient boolean hasFocus = false;
    /** Task for renaming */
    transient RequestProcessor.Task nameTask;
    /** Rename performer */
    transient NameChanger nameChanger;
    /** The time of last call to name change method */
    transient long nameChangeTime = -1;
    /** JTabbedPane for this frame */
    transient JTabbedPane tabc;
    /** helper variable - stores selected tab in tabbed pane */
    transient TopComponent wasSelected;
    /** listener to the mode which owns this container */
    transient PropertyChangeListener weakModeL;

    /** Asociation with the mode which we represent */
    ModeImpl mode;
    /** Tab placement of the tabs */
    int tabPlacement;
    /** Maximal allowed count of contained top components */
    int maxCount;
    /** Mapping between container listeners and MultiTabListenerImpls. 
     * @associates MultiTabListenerImpl*/
    HashMap clMap;
    /** List of all contained top components */
    ArrayList topComps;
    /** Currently selected top component in this container */
    TopComponent current;

    /** Internal list of deserialized tc managers. This list is used
    * in validateData() method, where list of deserialized top components
    * is finnaly obtained */
    private ArrayList tcManagers;
    /** Holds the manager of selected top component,
    * used only during deserialization */
    private WindowManagerImpl.TopComponentManager currentTcm;
    /** Holds mode and workspace name during deserialization.
    * Used to restore association with the mode after deserialization */
    private String modeName;
    private String workspaceName;
    /** manager of versioned serialization */
    private static VersionSerializator serializationManager;

    /** Internal status in which currently this container resides */
    private int innerStatus;
    /** inner status constants */
    private static final int EMPTY = 0;
    private static final int SINGLE = 1;
    private static final int MULTI = 2;

    /** Creates new multiFrame with no mode implementation asociated.
    * Used during deserialization.
    */
    public MultiTabContainer () {
        this(null);
    }

    /** Creates new multi tab top component container with given
    * asociated mode implementatio
    */
    public MultiTabContainer (ModeImpl mode) {
        super();
        this.mode = mode;
        innerStatus = EMPTY;
        topComps = new ArrayList(10);
        maxCount = 0;
        wm = (WindowManagerImpl)TopManager.getDefault().getWindowManager();
        initGui();
        initListeners();
    }

    /** initializes the gui of this container */
    private void initGui () {
        tabc = new JTabbedPane();
        tabc.setTabPlacement (SwingConstants.BOTTOM);
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // set default image icon
        setIconImage(null);
    }

    /* performs needed initialization */
    private void initListeners () {
        // listen to self window events
        addWindowListener(this);
        // listen to various things
        tabc.addChangeListener(this);
        tabc.addMouseListener(new PopupMouseImpl());
    }

    /** @return an array containing all top components which are currently
    * managed by this container */
    public synchronized TopComponent[] getTopComponents () {
        int count = topComps.size();
        return (TopComponent[])topComps.toArray(new TopComponent[count]);
    }

    /** Adds given top component to this container.
    * Beware, the code must be written to ensure that top component
    * will receive addNotify() before componentActivated(). 
    * @param tc top component to add
    * @return false if top component already exixts in container or 
    * cannot be added due to container-dependent restrictions,
    * true otherwise
    */
    public boolean addTopComponent (TopComponent tc) {
        // check if we can add or not
        if (!canAdd(tc))
            return false;
        // select only if top component is already contained
        if (topComps.contains(tc)) {
            selectView(tc);
            return true;
        }
        topComps.add(tc);
        Image icon = tc.getIcon();
        // perform adding
        synchronized (this) {
            switch (innerStatus) {
                // first component added -> act as single
            case EMPTY:
                innerStatus = SINGLE;
                current = tc;
                if (isVisible())
                    getContentPane().add(current);
                setIconImage(icon);
                break;
                // second added -> turn to multi
            case SINGLE:
                innerStatus = MULTI;
                // deactivate first
                if (current.equals(TopComponent.getRegistry().getActivated()))
                    doActivateComponent(null);
                getContentPane().remove(current);
                getContentPane().add(tabc, BorderLayout.CENTER);
                setIconImage(mode.getIcon());
                // add first top component
                Image currIcon = current.getIcon();
                if (isVisible()) {
                    tabc.addTab(current.getName(),
                                currIcon == null ? null : new ImageIcon(currIcon),
                                current, current.getToolTipText());
                }
                // BEWARE, WITHOUT BREAK!
            case MULTI:
                if (isVisible()) {
                    tabc.addTab(tc.getName(),
                                icon == null ? null : new ImageIcon(icon),
                                tc, tc.getToolTipText());
                }
                break;
            }
            // PENDING
            // updateUndoRedo();
            refreshAfterAdding(tc);
        }
        // start to listen to top component's property changes
        wm.findManager(tc).addPropertyChangeListener(this);
        tc.addComponentListener(getCompListener());
        return true;
    }

    /** @return True if count of contained top components is
    * smaller than maxCount property */
    public boolean canAdd (TopComponent tc) {
        return (maxCount <= 0) || (topComps.size() < maxCount);
    }

    /** Sets new maximal count of top components that can be
    * contained in this container */
    public void setMaxCount (int maxCount) {
        this.maxCount = maxCount;
    }

    /** @return Maximal allowed count of contained top components */
    public int getMaxCount () {
        return maxCount;
    }

    /** Removes specified top component from this container.
    * Beware! The code in this method must be written to ensure
    * that each top component will receive componentDeactivated() before
    * removeNotify().
    * @param tc top component to remove
    */
    public int removeTopComponent (TopComponent tc) {
        if (!topComps.contains(tc))
            return topComps.size();
        // stop to listen to the component
        wm.findManager(tc).removePropertyChangeListener(this);
        tc.removeComponentListener(getCompListener());
        int count = -1;
        synchronized (this) {
            topComps.remove(tc);
            count = topComps.size();
            // update current if needed
            if (tc.equals(current)) {
                current = (count == 0) ? null : (TopComponent)topComps.get(0);
            }
            switch (innerStatus) {
                // the only component removed -> turn into EMPTY
            case SINGLE:
                innerStatus = EMPTY;
                doActivateComponent(null);
                getContentPane().remove(tc);
                checkVisible();
                break;
            case MULTI:
                // deactivate first
                if (tc.equals(TopComponent.getRegistry().getActivated()))
                    doActivateComponent(null);
                if (count == 1) {
                    // only one component left -> turn to single
                    innerStatus = SINGLE;
                    // remove using removeTabAt to ensure correct visility reset
                    for (int i = tabc.getTabCount() - 1; i >= 0; i--) {
                        tabc.removeTabAt(i);
                    }
                    // switch pane content
                    tabc.invalidate();
                    tabc.validate();
                    getContentPane().remove(tabc);
                    setIconImage(current.getIcon());
                    if (isVisible()) {
                        current.setVisible(true);
                        getContentPane().add(current);
                    }
                } else {
                    int index = tabc.indexOfComponent(tc);
                    if (index != -1) {
                        tabc.removeTabAt(index);
                        // synchronize current variable with tab selection
                        current = (TopComponent)tabc.getSelectedComponent();
                    }
                    // invoke relayout of multi tab
                    tabc.invalidate ();
                    tabc.validate ();
                    tabc.repaint();
                }
                // reactivate
                if (hasFocus) {
                    doActivateComponent(current);
                    if (innerStatus == SINGLE) {
                        doFrameActivated();
                    }
                }
                updateMainTitle();

                // PENDING
                // getWorkspaceElement().setUndoRedo (comp.getUndoRedo ());
                break;
            }
        }
        return count;
    }

    /** @return true if container contains specified top component,
    * false otherwise
    */
    public boolean containsTopComponent (TopComponent tc) {
        return topComps.contains(tc);
    }

    /** @return Currently selected top component or null if no top
     * component is selected at a time */
    public TopComponent getSelectedTopComponent() {
        return current;
    }

    /** The component requests focus. Move it to be visible.
    * When in iconified state, deiconify first.
    */
    public void requestFocus (TopComponent tc) {
        requestFocusBase();
        selectView(tc);
    }

    /** The whole mode requests focus. Move it to be visible.
    * When in iconified state, deiconify first. */
    public void requestFocus () {
        requestFocusBase();
        if (current != null) {
            current.requestFocus();
        }
    }

    private void requestFocusBase () {
        if (getState() == Frame.ICONIFIED)
            setState(Frame.NORMAL);
        toFront();
        super.requestFocus();
    }

    /** Sets visibility status for container
    * @param visible true if container should be made visible,
    * false otherwise
    */
    public void setVisible (boolean visible) {
        visible = (innerStatus != EMPTY) ? visible : false;
        if (visibility == visible)
            return;
        visibility = visible;
        if (visibility) {
            if (weakModeL == null) {
                // listen to our mode
                weakModeL = WeakListener.propertyChange(this, mode);
                mode.addPropertyChangeListener(weakModeL);
            }
            // attach our top components to the AWT's hierarchy
            synchronized (this) {
                if (innerStatus == MULTI) {
                    if (getContentPane().getComponentCount() <= 0) {
                        getContentPane().add(tabc);
                    }
                    // add all contained top components
                    TopComponent curComp = null;
                    for (Iterator iter = topComps.iterator(); iter.hasNext(); ) {
                        curComp = (TopComponent)iter.next();
                        Image icon = curComp.getIcon();
                        tabc.addTab(
                            curComp.getName(),
                            (icon == null) ? null : new ImageIcon(icon),
                            curComp, curComp.getToolTipText()
                        );
                    }
                } else if (innerStatus == SINGLE) {
                    if (getContentPane().getComponentCount() <= 0) {
                        getContentPane().add(current);
                        // ensure that component is visible
                        if (!current.isVisible())
                            current.setVisible(true);
                    }
                }
            }
        } else {
            // free our top components from AWT's component hierarchy
            // to be available for other workspaces
            if (innerStatus == MULTI) {
                wasSelected = (TopComponent)tabc.getSelectedComponent();
                // remove using removeTabAt to reset visibility flag
                // properly (bug in JTabbedPane)
                for (int i = tabc.getTabCount() - 1; i >= 0; i--) {
                    tabc.removeTabAt(i);
                    /*if (!curComp.isVisible())
                      System.out.println("Component " + curComp.getName() + 
                                         " not visible.");*/ // NOI18N
                }
            } else if (innerStatus == SINGLE) {
                getContentPane().remove(current);
            }
        }
        super.setVisible(visible);
        if (visible) {
            if ((innerStatus == MULTI) && (wasSelected != null)) {
                // restore selected tab and ensure that it is visible
                final TopComponent toBeSelected = wasSelected;
                // select the component a little bit later,
                // when tabs are all added and initialized
                SwingUtilities.invokeLater(new Runnable() {
                                               public void run () {
                                                   selectView(toBeSelected);
                                                   if (!toBeSelected.isVisible())
                                                       toBeSelected.setVisible(true);
                                               }
                                           }
                                          );
            }
        }
    }

    /**
    * @return true if frame is visible or false if it isn't.
    */
    public boolean isVisible () {
        return visibility;
    }

    /** Request for UI update (when changing L&F) */
    public void updateUI () {
        SwingUtilities.updateComponentTreeUI(this);
    }

    /** Adds given container listener for listening to the container
    * events */
    public synchronized void addContainerListener (ContainerListener cl) {
        if (clMap == null)
            clMap = new HashMap(5);
        MultiTabListenerImpl mtli = new MultiTabListenerImpl(cl);
        addComponentListener(mtli);
        addWindowListener(mtli);
        clMap.put(cl, mtli);
    }

    /** Removes given container listener */
    public synchronized void removeContainerListener (ContainerListener cl) {
        if (clMap == null)
            return;
        MultiTabListenerImpl mtli = (MultiTabListenerImpl)clMap.get(cl);
        if (mtli != null) {
            removeComponentListener(mtli);
            removeWindowListener(mtli);
        }
        clMap.remove(cl);
    }

    /** Safe getter for component listener */
    ComponentListener getCompListener () {
        if (compL == null) {
            compL = new ComponentAdapter () {
                        public void componentShown (ComponentEvent ev) {
                            Component comp = ev.getComponent();
                            /*if ((tabc.indexOfComponent(comp) >= 0) && comp.isVisible()) {
                              tabc.setSelectedComponent(comp);
                              comp.repaint();
                        }*/
                        }
                    };
        }
        return compL;
    }

    /** Overrides superclass' version to allow null icon parameter.
    * @param icon New icon of the window.
    * If icon equals to null, default icon is used.
    */
    public void setIconImage (Image icon) {
        if (defaultIcon == null) {
            defaultIcon = Toolkit.getDefaultToolkit().getImage(
                              getClass().getResource(
                                  "/org/netbeans/core/resources/frames/default.gif" // NOI18N
                              )
                          );
        }
        if ((icon == null) && (mode != null))
            icon = mode.getIcon();
        if (icon != null)
            super.setIconImage(icon);
        else if (getIconImage() == null)
            super.setIconImage(defaultIcon);
    }

    /** Reactions to the property changes of contained top components
    * and property changes of the mode.
    */
    public void propertyChange (PropertyChangeEvent ev) {
        String propName = ev.getPropertyName();
        Object source = ev.getSource();
        if (source instanceof WindowManagerImpl.TopComponentManager) {
            // notification from top component manager
            TopComponent tc = ((WindowManagerImpl.TopComponentManager)source).getComponent();
            if (WindowManagerImpl.TopComponentManager.PROP_NAME.equals(propName)) {
                componentNameChanged(tc);
            } else
                if (WindowManagerImpl.TopComponentManager.PROP_ICON.equals(propName)) {
                    setComponentIcon(tc, (Image)ev.getNewValue());
                } else
                    if (WindowManagerImpl.TopComponentManager.PROP_ACTIVATED_NODES.
                            equals(propName)) {
                        // PENDING... should we do anything?
                    }
        } else {
            // notification from the mode implementation
            if (ModeImpl.PROP_DISPLAY_NAME.equals(propName)) {
                updateMainTitle();
            }
        }
    }

    /** Updates title of multi frame. Title is constructed from
    * the multi frame name and the name of selected view
    */
    private void updateMainTitle () {
        if (windowTitle == null) {
            ResourceBundle bundle = NbBundle.getBundle(MultiTabContainer.class);
            windowTitle = new MessageFormat(bundle.getString("CTL_MultiTabTitle"));
            if (untitledComponent == null)
                untitledComponent = bundle.getString("CTL_UntitledComponent");
        }
        String selectedName = (current == null) ? "?" : current.getName(); // NOI18N
        if (selectedName == null)
            selectedName = untitledComponent;
        switch (innerStatus) {
        case SINGLE:
            setTitle(selectedName);
            break;
        case MULTI:
            setTitle(windowTitle.format(new Object[] {
                                            mode == null ? NbBundle.getBundle(MultiTabContainer.class).
                                            getString("CTL_UntitledMultiTab")
                                            : mode.getDisplayName(),
                                            selectedName })
                    );
            break;
        }
    }

    /** Called when the name of some component in this multi frame has changed */
    void componentNameChanged (TopComponent component) {
        if (!topComps.contains(component))
            return;  // not added yet
        long now = System.currentTimeMillis();
        if ((nameChangeTime < 0) ||
                ((now - nameChangeTime) > NAME_CHANGE_DELTA)) {
            SwingUtilities.invokeLater(new NameChanger(this, component));
        } else {
            // run the request for the change of the name of tab
            if ((nameTask == null) || (nameChanger.component != component)) {
                nameTask = RequestProcessor.createRequest(
                               nameChanger = new NameChanger(this, component));
            }
            nameTask.schedule(1500);
        }
        nameChangeTime = now;
    }

    /** Called when icon of some component in this multi frame has changed */
    void setComponentIcon (TopComponent component, Image icon) {
        if (!topComps.contains(component))
            return;  // not added yet
        if (innerStatus == SINGLE) {
            setIconImage(icon);
        } else {
            int compIndex = tabc.indexOfComponent(component);
            if (compIndex < 0)
                return;
            tabc.setIconAt(compIndex, new ImageIcon(icon));
            if (isVisible()) {
                tabc.invalidate ();
                tabc.validate ();
                tabc.repaint();
            }
        }
    }

    /** Initialization when frame becomes active */
    void doFrameActivated () {
        registerPerformers();
    }

    /** Activates component and updates selected nodes.
    * If param is null, clears both active top component and
    * selected nodes.
    */
    void doActivateComponent (TopComponent tc) {
        wm.activateComponent(tc);
        // selected nodes
        // don't change sel nodes if activated top component
        // is property sheet
        if (!(tc instanceof NbNodeOperation.Sheet)) {
            RegistryImpl rimpl = (RegistryImpl) TopComponent.getRegistry();
            Node[] nodes = (tc == null) ? new Node[0] : tc.getActivatedNodes();
            rimpl.selectedNodesChanged(
                new SelectedNodesChangedEvent(this, tc, nodes));
        }
    }

    /** Post-initialization after adding - called from addTopComponent method */
    void refreshAfterAdding (TopComponent comp) {
        if (innerStatus == SINGLE) {
            // first component added, perform needed initialization
            // retrieve and set initial bounds, if possible
            Rectangle modeBounds = mode.getBounds();
            if (modeBounds != null) {
                setBounds(modeBounds);
            } else {
                pack();
            }
            updateMainTitle();
        }
        // adds listener for closing the component
        comp.addComponentListener (getCompListener());
        if (isVisible() && innerStatus == MULTI) {
            // workaround for JTabbedPane bug, make visible if needed
            selectView(comp);
            if (!comp.isVisible())
                comp.setVisible(true);
            tabc.invalidate ();
            tabc.validate ();
            tabc.repaint();
        }
    }

    /** Helper method, called from mode managers during
    * deserialization. Updates undo / redo management */
    // PENDING
    /*void updateUndoRedo () {
      TopComponent comp = (TopComponent)tabc.getSelectedComponent();
      if (comp != null)
        getWorkspaceElement().setUndoRedo(comp.getUndoRedo());
}*/

    /** Selects specified top component
    */
    void selectView (TopComponent comp) {
        if ((innerStatus == MULTI) && (tabc.indexOfComponent(comp) >= 0)) {
            // select
            tabc.setSelectedComponent(comp);
            // ensure that component is visible
            /*if (!comp.isVisible())
              comp.setVisible(true);*/
        }
    }

    /** Register as performer for actions when get focus.
    */
    void registerPerformers () {
        // next and previous actions
        updateNextAndPreviousState();
        if (innerStatus == EMPTY)
            return;
        // close action
        closeView.setActionPerformer(this);
        // undock action
        if (current != null) {
            // obtain mode active top component is in
            Workspace curWorkspace = wm.getCurrentWorkspace();
            Mode compMode = curWorkspace.findMode(current);
            // activate undock action if component is in multi
            if ((compMode != null) && (!((ModeImpl)compMode).isSingle()))
                undock.setActionPerformer(this);
        }
        // clone action
        if (current instanceof TopComponent.Cloneable)
            clone.setActionPerformer(this);
    }

    /** Enable or disable next and previous actions depending on
    * current inner state */
    void updateNextAndPreviousState () {
        if (innerStatus == MULTI) {
            nextTab.setActionPerformer(this);
            prevTab.setActionPerformer(this);
        } else {
            nextTab.setActionPerformer(null);
            prevTab.setActionPerformer(null);
        }
    }

    /** @return context help for the frame. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(MultiTabContainer.class);
    }

    /** Makes the window invisible if in EMPTY state, make it visible
    * in all other cases.
    */
    void checkVisible () {
        super.setVisible(innerStatus != EMPTY);
    }

    /************* Reactions to window events ************/

    /** performers registering, and component activation. */
    public void windowActivated (WindowEvent e) {
        hasFocus = true;
        doActivateComponent(current);
        doFrameActivated();
    }

    /** performers registering */
    public void windowOpened (WindowEvent ev) {
        registerPerformers();
    }

    /** deactivating... */
    public void windowDeactivated (WindowEvent ev) {
        hasFocus = false;
    }

    public void windowClosing (WindowEvent ev) {
    }

    public void windowIconified (WindowEvent ev) {
    }

    public void windowDeiconified (WindowEvent ev) {
    }

    public void windowClosed (WindowEvent ev) {
    }

    /** Implementation of the ChangeListener interface
    * Called when some tab is the tabbed pane is activated.
    */
    public void stateChanged (ChangeEvent e) {
        if ((!isVisible()) || (tabc.getTabCount() <= 0) ||
                (innerStatus != MULTI))
            return;
        int index = tabc.getSelectedIndex();
        current = (index < 0) ? null : (TopComponent)tabc.getSelectedComponent();
        updateMainTitle();
        if (hasFocus) {
            doActivateComponent(current);
            doFrameActivated();
            // request focus if needed
            if (current != null) {
                current.requestFocus();
            }
        }
    }

    /** Sets the placement of the tabs.
    * @param placement SwingConstant.XXX constant representing new
    * placement of the tabs (TOP, LEFT...)
    */
    public void setTabPlacement (int tabPlacement) {
        if (this.tabPlacement == tabPlacement)
            return;
        this.tabPlacement = tabPlacement;
        tabc.setTabPlacement(tabPlacement);
        // PENDING - need to repaint?
    }

    /** @return current placement of the tab */
    public int getTabPlacement () {
        return tabPlacement;
    }

    /** Implementation of the ActionPerformer interface
    * Performs differently depending on given action.
    */
    public void performAction (SystemAction action) {
        if (current == null)
            return;
        if (action instanceof CloseViewAction)
            // close view action performing
            current.close();
        else if (action instanceof CloneViewAction ||
                 action instanceof UndockAction) {
            // prepare new single mode
            WorkspaceImpl curWorkspace = (WorkspaceImpl)wm.getCurrentWorkspace();
            String modeName = wm.findUnusedModeName(current.getName(), curWorkspace);
            Mode singleMode = curWorkspace.createMode(
                                  modeName, modeName, null, ModeImpl.SINGLE, true
                              );
            if (action instanceof CloneViewAction) {
                // clone the view and dock into new single mode
                TopComponent newComp =
                    (TopComponent)((TopComponent.Cloneable)current).cloneComponent();
                newComp.setIcon(current.getIcon());
                singleMode.dockInto(newComp);
                newComp.open();
            } else {
                // undock to the single mode
                singleMode.dockInto(current);
            }
        } else if (action instanceof NextTabAction ||
                   action instanceof PreviousTabAction) {
            // select previous or next component
            int count = tabc.getTabCount();
            int index = tabc.getSelectedIndex();
            if ((count <= 1) || (index == -1))
                return;
            int delta = (action instanceof NextTabAction) ? 1 : -1;
            int newIndex = (index + delta + count) % count;
            tabc.setSelectedIndex(newIndex);
        }
    }

    /** Called when whole deserialization is done,
    * assign listeners to the top compoennts' property changes
    */
    public void validateObject() throws java.io.InvalidObjectException {
    }

    /** Called when first phase of WS deserialization is done.
    * Finish deserialization of all contained top components
    * and attach listeners properly.
    */
    public void validateData () {
        // restore connection with our mode
        mode = (ModeImpl)TopManager.getDefault().getWindowManager().
               findWorkspace(workspaceName).findMode(modeName);
        workspaceName = null;
        modeName = null;
        // obtain top component instances from
        // instances of top component managers
        topComps = new ArrayList(tcManagers.size());
        WindowManagerImpl.TopComponentManager cur = null;
        for (Iterator iter = tcManagers.iterator(); iter.hasNext(); ) {
            cur = (WindowManagerImpl.TopComponentManager)iter.next();
            if (cur.validateData()) {
                topComps.add(cur.getComponent());
                // set current if possible
                if (cur.equals(currentTcm)) {
                    current = cur.getComponent();
                }
            }
        }
        // make temporary variables gc'able
        tcManagers = null;
        currentTcm = null;
        // update current if needed
        int count = topComps.size();
        if ((current == null) && (count > 0)) {
            current = (TopComponent)topComps.get(0);
        }
        // update inner status
        if (count <= 0)
            innerStatus = EMPTY;
        else if (count == 1)
            innerStatus = SINGLE;
        // another validation
        wasSelected = current;
        updateMainTitle();
        // listen to top components
        TopComponent curTc = null;
        for (Iterator iter = topComps.iterator(); iter.hasNext(); ) {
            curTc = (TopComponent)iter.next();
            wm.findManager(curTc).addPropertyChangeListener(this);
            curTc.addComponentListener(getCompListener());
        }
        // update icon
        if ((innerStatus == SINGLE) && (current != null))
            setIconImage(current.getIcon());
        if (innerStatus == MULTI)
            setIconImage(mode.getIcon());
    }

    /** Let instance of properly parametrized DefaultReplacer to keep
    * persistent state of this multi tab container */
    private Object writeReplace ()
    throws ObjectStreamException {
        // provide version with data
        Version1 version =
            (Version1)serializationManager().getVersion(Version1.NAME);
        version.assignData(this);
        // use replacer
        return new DefaultReplacer(new VSAccess(serializationManager()));
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

    /** Only accessor for inner class */
    private void fireNameChange (final String newName) {
        firePropertyChange("name", null, newName); // NOI18N
    }

    /** Popup menu reaction implementation */
    final class PopupMouseImpl extends MouseUtils.PopupMouseAdapter {
        /** Called when the seqeunce of mouse events should lead to actual
        * showing of the popup menu. */
        protected void showPopup (MouseEvent e) {
            TabbedPaneUI tabUI = tabc.getUI();
            int clickTab = tabUI.tabForCoordinate(tabc, e.getX(), e.getY());
            TopComponent selected = current;
            if ((selected == null) ||
                    (!selected.equals(TopComponent.getRegistry().getActivated()))) {
                return;
            }
            JPopupMenu popup = new JPopupMenuPlus();
            // constructs pop-up menu from actions of selected component
            SystemAction[] compActions = selected.getSystemActions();
            for (int i = 0; i < compActions.length; i++) {
                if (compActions[i] == null)
                    popup.addSeparator();
                else if (compActions[i] instanceof CallableSystemAction)
                    popup.add(((CallableSystemAction)compActions[i]).
                              getPopupPresenter());
            }
            if (compActions.length != 0)
                popup.addSeparator();
            // and add our docking action
            popup.add(docking.getPopupPresenter());

            Point p = e.getPoint ();
            SwingUtilities.convertPointToScreen (p, MultiTabContainer.this);
            Dimension popupSize = popup.getPreferredSize ();
            Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
            int yCorrection = 0;
            if (org.openide.util.Utilities.isWindows ()) {
                yCorrection = org.netbeans.core.output.OutputTab.TYPICAL_WINDOWS_TASKBAR_HEIGHT;
            }
            if (p.x + popupSize.width > screenSize.width) p.x = screenSize.width - popupSize.width;
            if (p.y + popupSize.height > screenSize.height - yCorrection) p.y = screenSize.height - popupSize.height - yCorrection;

            SwingUtilities.convertPointFromScreen (p, MultiTabContainer.this);

            popup.show(tabc, p.x, p.y);
        }
    } // end of PopupMouseImpl

    /** Takes care of lazy renaming of tabs in tabbed panel.
    * Waits until the name stays the same for some period of time,
    * and only then calls rename of the tab.
    */
    private static final class NameChanger extends Object implements Runnable {
        /** Outer class */
        private MultiTabContainer outer;
        /** The component whose name has changed */
        TopComponent component;

        NameChanger (MultiTabContainer outer, TopComponent component) {
            this.outer = outer;
            this.component = component;
        }

        public synchronized void run () {
            // update main title
            outer.updateMainTitle();
            String newName = component.getName();
            if (outer.innerStatus == MULTI) {
                // update tabbed pane
                JTabbedPane tabc = outer.tabc;
                int compIndex = tabc.indexOfComponent(component);
                if (compIndex < 0)
                    return;  // removed or somewhat..
                tabc.setTitleAt(compIndex, newName);
                tabc.invalidate();
                tabc.validate();
                tabc.repaint();
            }
            // notify name change listeners
            outer.fireNameChange(newName);
        }

    } // end of NameChanger inner class

    /** Basic version of persistence for mode implementation.
    * Method assignData(modeImpl) must be called prior to serialization */
    private static final class Version1
        implements DefaultReplacer.ResVersionable {

        /* identification string */
        public static final String NAME = "Version_1.0"; // NOI18N

        /** variables of persistent state of the mode implementation */
        int innerStatus;
        String workspaceName;
        String modeName;
        Rectangle bounds;

        /**
         * @associates TopComponentManager 
         */
        ArrayList tcManagers;
        WindowManagerImpl.TopComponentManager currentTcm;

        /** asociation with outerclass, used when writing */
        MultiTabContainer mtc;

        /** Identification of the version */
        public String getName () {
            return "Version_1.0"; // NOI18N
        }

        /** Assigns data to be written. Must be called before writing */
        public void assignData (MultiTabContainer mtc) {
            this.mtc = mtc;
        }

        /** read the data of the version from given input */
        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException {
            // read the fields
            innerStatus = ((Integer)in.readObject()).intValue();
            workspaceName = (String)in.readObject();
            modeName = (String)in.readObject();
            bounds = (Rectangle)in.readObject();
            // read managers of top components (but leave top components
            // in byte streams, not fully deserialized).
            // Deserialization of tcs will be finished
            // in validateData() of MultiTabContainer
            tcManagers = (ArrayList)in.readObject();
            // remove tcManagers deserialized to null, because
            // it signalizes that there was a problem during
            // serialization of top component
            WindowManagerImpl.TopComponentManager[] tcmArray =
                (WindowManagerImpl.TopComponentManager[])
                tcManagers.toArray(new WindowManagerImpl.TopComponentManager[tcManagers.size()]);
            for (int i = tcmArray.length - 1; i >= 0; i--) {
                if (tcmArray[i] == null) {
                    tcManagers.remove(i);
                }
            }
            // obtain manager of selected tc
            Object temp = in.readObject();
            //System.out.println("Class: " + temp.getClass().getName()); // NOI18N
            currentTcm = (WindowManagerImpl.TopComponentManager)temp;
        }

        /** write the data of the version to given output */
        public void writeData (ObjectOutput out)
        throws IOException {
            // write fields
            out.writeObject(new Integer(mtc.innerStatus));
            out.writeObject(mtc.mode.getWorkspace().getName());
            out.writeObject(mtc.mode.getName());
            out.writeObject(mtc.mode.getBounds());
            // create and write list of tc managers
            tcManagers = new ArrayList();
            TopComponent[] tcs = mtc.getTopComponents();
            for (int i = 0; i < tcs.length; i++) {
                tcManagers.add(WindowManagerImpl.findManager(tcs[i]));
            }
            out.writeObject(tcManagers);
            // write manager of current tc
            currentTcm = (mtc.current == null)
                         ? null : WindowManagerImpl.findManager(mtc.current);
            out.writeObject(currentTcm);
        }

        public Object resolveData ()
        throws ObjectStreamException {
            // create and fill mtc
            MultiTabContainer result = new MultiTabContainer();
            result.innerStatus = innerStatus;
            result.workspaceName = workspaceName;
            result.modeName = modeName;
            result.setBounds(bounds);
            result.tcManagers = tcManagers;
            result.currentTcm = currentTcm;
            return result;
        }

    } // end of Version1 inner class

    /** Implementation of persistent access to our version serializator */
    private static final class VSAccess implements DefaultReplacer.Access {
        /** serialVersionUID */
        private static final long serialVersionUID = -5417180019495806786L;

        /** version serializator, used only during writing */
        transient VersionSerializator vs;

        public VSAccess (VersionSerializator vs) {
            this.vs = vs;
        }

        public VersionSerializator getVersionSerializator () {
            return (vs == null) ? createSerializationManager() : vs;
        }

    } // end of VSAccess inner class


}

/*
 * Log
 *  30   Gandalf   1.29        3/11/00  Martin Ryzl     menufix [by E.Adams, 
 *       I.Formanek]
 *  29   Gandalf   1.28        1/15/00  David Simonek   mutliwindow title bug 
 *       fixed
 *  28   Gandalf   1.27        1/13/00  David Simonek   i18n
 *  27   Gandalf   1.26        1/12/00  Ian Formanek    NOI18N
 *  26   Gandalf   1.25        1/12/00  Ales Novak      setVisible added
 *  25   Gandalf   1.24        1/12/00  David Simonek   
 *  24   Gandalf   1.23        1/10/00  David Simonek   minor changes connected 
 *       with focus problematics
 *  23   Gandalf   1.22        12/23/99 David Simonek   
 *  22   Gandalf   1.21        12/17/99 David Simonek   #1913, #2970
 *  21   Gandalf   1.20        11/30/99 David Simonek   neccessary changes 
 *       needed to change main explorer to new UI style  (tabs are full top 
 *       components now, visual workspace added, layout of editing workspace 
 *       chnaged a bit)
 *  20   Gandalf   1.19        11/10/99 David Simonek   debug comments removed
 *  19   Gandalf   1.18        11/4/99  David Simonek   ws serialization 
 *       bugfixes
 *  18   Gandalf   1.17        11/3/99  David Simonek   completely rewritten 
 *       serialization of windowing system...
 *  17   Gandalf   1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        10/7/99  David Simonek   request focus related 
 *       bugs repaired
 *  15   Gandalf   1.14        10/6/99  David Simonek   more robust 
 *       serialization of window system (especially editor TCs)
 *  14   Gandalf   1.13        9/13/99  David Simonek   request focus for the 
 *       mode added
 *  13   Gandalf   1.12        8/18/99  David Simonek   visibility bugfix
 *  12   Gandalf   1.11        8/14/99  David Simonek   bugfixes, #3347, #3274 
 *       etc.
 *  11   Gandalf   1.10        8/9/99   David Simonek   
 *  10   Gandalf   1.9         8/2/99   Ian Formanek    popup menu positioning 
 *       improved
 *  9    Gandalf   1.8         8/2/99   Ian Formanek    Fixed startup problem 
 *       with deserialization of workspaces (IndexOutOfBoundsException thrown)
 *  8    Gandalf   1.7         7/30/99  David Simonek   window icons, comments 
 *       removed
 *  7    Gandalf   1.6         7/30/99  David Simonek   serialization fixes
 *  6    Gandalf   1.5         7/30/99  David Simonek   iconification bugfixes, 
 *       focus bugfixes
 *  5    Gandalf   1.4         7/29/99  David Simonek   further ws serialization
 *       changes
 *  4    Gandalf   1.3         7/28/99  David Simonek   serialization of window 
 *       system...first draft :-)
 *  3    Gandalf   1.2         7/21/99  David Simonek   window system updates...
 *  2    Gandalf   1.1         7/20/99  David Simonek   various window system 
 *       updates
 *  1    Gandalf   1.0         7/11/99  David Simonek   
 * $
 * Beta Change History:
 *  0    Tuborg    0.30        --/--/98 Jaroslav Tulach Added lock and all synchronization is done against it.
 *  0    Tuborg    0.30        --/--/98 Jaroslav Tulach Deadlock warning: Do not synchronize against the frame!!!!
 *  0    Tuborg    0.31        --/--/98 Jan Formanek    bugfix (lock was null after serialization)
 *  0    Tuborg    0.32        --/--/98 Petr Hamernik   resizing changed
 *  0    Tuborg    0.33        --/--/98 Petr Hamernik   context popup menu at the tabc
 *  0    Tuborg    0.34        --/--/98 Petr Hamernik   position is set by CoronaTopManager
 *  0    Tuborg    0.35        --/--/98 Petr Hamernik   bug fix
 *  0    Tuborg    0.36        --/--/98 Jaroslav Tulach undoable edit
 *  0    Tuborg    0.37        --/--/98 Jan Formanek    improved popup menu invocation
 *  0    Tuborg    0.38        --/--/98 Jan Jancura     componentActivated / Deactivated support
 *  0    Tuborg    0.39        --/--/98 Petr Hamernik   modified flag
 *  0    Tuborg    0.43        --/--/98 Petr Hamernik   bugfix
 *  0    Tuborg    0.44        --/--/98 Petr Hamernik   positioning improvements
 *  0    Tuborg    0.45        --/--/98 Miloslav Metelk patched deadlock for closeLast()
 *  0    Tuborg    0.47        --/--/98 Petr Hamernik   switching tabs - actions
 *  0    Tuborg    0.48        --/--/98 Petr Hamernik   bugfixes
 *  0    Tuborg    0.50        --/--/98 Jan Formanek    HelpContext
 *  0    Tuborg    0.51        --/--/98 Jan Formanek    repaint is setVisible ();
 */
