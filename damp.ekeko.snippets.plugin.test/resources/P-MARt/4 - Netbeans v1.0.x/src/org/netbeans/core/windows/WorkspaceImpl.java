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

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.net.URL;
import javax.swing.SwingUtilities;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.openide.windows.*;
import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.awt.ToolbarPool;
import org.openide.util.Mutex;

import org.netbeans.core.output.OutputTab;
import org.netbeans.core.windows.util.*;

/** Implementation of the workspace interface.
*
* @author Dafe Simonek
*/
public final class WorkspaceImpl implements Workspace,
    DeferredPerformer.DeferredCommand {

    /** Name of property for the list of elements on this workspace. */
    public static final String PROP_MODES = "modes"; // NOI18N
    /** Name of property for the name of this workspace. */
    public static final String PROP_NAME = "name"; // NOI18N
    /** count number of workspaces */
    static int numberOfWorkspaces = 0;
    /** Dimensions of the screen  */
    static Dimension screenSize;
    /** Information message shown during loading */
    static MessageFormat loadingMessage;
    /** helper - last bounds of main window */
    static Rectangle lastMainBounds;
    /** a pool of toolbars */
    static ToolbarPool toolbarPool;

    /** cascading step */
    Dimension cascadeStep = new Dimension(20, 20);
    private int cascadeStepsCount;
    /** Current cascade point - to which cascaded windows are placed */
    Point cascadePoint;
    /** Origin of the cascading on this workspace */
    Point cascadingOrigin;
    /** programmatic name of the workspace */
    String name;
    /** display name of the workspace. actually, it serves two purposes.
    * if fromBundle helper variable is true, then displayName contains name
    * of the bundle string to take display name from. */
    String displayName;
    /** toolbar configuration name */
    String toolbarConfigName = "Standard"; // NOI18N
    /** A list of all modes on this workspace 
     * @associates ModeImpl*/
    HashSet modes;
    /** Currently activated mode */
    Mode current;
    /** Helper flag variable, true if display name is taken from the bundle */
    boolean fromBundle;

    /** holds previously active top component when
    * workspace was visible last time */
    transient TopComponent savedActive;
    /** Helper variable - holds information needed
    * to renew activated top component after deserialization. */
    WindowManagerImpl.TopComponentManager savedManager;

    /** true if this workspace is visible (current) */
    transient boolean visible;
    /** asociation with window manager implementation */
    transient WindowManagerImpl wm;
    /** property change support */
    private transient PropertyChangeSupport changeSupport;
    /** default workspace name */
    private static MessageFormat defaultWorkspaceName;
    /** manager of versioned serialization */
    private VersionSerializator serializationManager;

    /** Default height and width of modes */
    static final int DEFAULT_MODE_WIDTH = 500;
    static final int DEFAULT_MODE_HEIGHT = 350;

    static final long serialVersionUID =-1865300855714270375L;

    /** Creates new workspace implementation with generated default name
    * and display name */
    public WorkspaceImpl () {
        this((String)null);
    }

    /** Creates new workspace implementation with given name.
    * Given parameter is used both for name and display name.
    */
    public WorkspaceImpl (String name) {
        this(name, name, false);
    }

    /** Creates new workspace with given programmatic name and
    * given display name. */
    public WorkspaceImpl (String name, String displayName) {
        this(name, displayName, false);
    }

    /** Creates new workspace with given programmatic name and
    * given display name.
    * @param fromBundle true if displayName parameter contains bundle string
    * rather than real display name */
    public WorkspaceImpl (String name, String displayName, boolean fromBundle) {
        this.name = name;
        this.fromBundle = fromBundle;
        if (name == null) {
            // set default name
            Object[] arr = { new Integer (++numberOfWorkspaces) };
            if (defaultWorkspaceName == null) {
                ResourceBundle bundle = NbBundle.getBundle (WorkspaceImpl.class);
                defaultWorkspaceName =
                    new MessageFormat (bundle.getString ("WorkspaceNumber"));
            }
            this.name = defaultWorkspaceName.format (arr);
        }
        // display name
        this.displayName = (displayName == null) ? this.name : displayName;
        modes = new HashSet(10);
        initialize();
    }

    /** Copy constructor, creates new workspace as a copy of given workspace.
    * Created workspace will have a list of modes with the same names,
    * bounds etc as the original, and the same top components will
    * be opened on the clone as they are in the original.
    * The name of cloned workspace will be unique, based on the
    * name of the original.
    */
    public WorkspaceImpl (WorkspaceImpl original) {
        // usual initialization
        modes = new HashSet(10);
        initialize();
        // create a name
        int i = 1;
        while (wm.findWorkspace(name = original.name + "_" + i++) != null); // NOI18N
        // display name
        displayName = original.getDisplayName() + (i - 1);
        // copy content
        this.cascadeStep = new Dimension(original.cascadeStep);
        this.cascadeStepsCount = original.cascadeStepsCount;
        this.cascadePoint = (original.cascadePoint == null)
                            ? null : new Point(original.cascadePoint);
        this.toolbarConfigName = new String(original.toolbarConfigName);
        // copy modes
        ModeImpl curMode = null;
        ModeImpl newMode = null;
        for (Iterator iter = original.modes.iterator(); iter.hasNext(); ) {
            curMode = (ModeImpl)iter.next();
            newMode = new ModeImpl(this, curMode);
            if (curMode.equals(original.current)) {
                this.current = newMode;
            }
            modes.add(newMode);
            // copy reference to the contained tcs
            newMode.fillTopComponents(curMode);
        }
    }

    private void initialize () {
        if (screenSize == null) {
            screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        }
        changeSupport = new PropertyChangeSupport(this);
        wm = (WindowManagerImpl)TopManager.getDefault().getWindowManager();
        cascadingOrigin = new Point(0, 0);
    }

    /** Get the programmatic unique name of this workspace.
     * @return the programmatic name
     */
    public String getName () {
        return name;
    }

    /** Get human-presentable name of the workspace.
    * May be used, e.g., for display on the workspaces tab on the main window.
    * @return the diplay name of the workspace
    */
    public String getDisplayName () {
        return fromBundle
               ? NbBundle.getBundle(WorkspaceImpl.class).getString(displayName)
               : displayName;
    }

    /** Set human-presentable name of the workspace.
    * @param s the new display name
    */
    public void setDisplayName (String s) {
        String localDisplay = getDisplayName();
        if (((localDisplay != null) && localDisplay.equals(s)) ||
                (localDisplay == null) && (s == null)) {
            // no real change
            return;
        }
        // disable fromBundle feature, it user's choice now
        fromBundle = false;
        String old = displayName;
        displayName = s;
        changeSupport.firePropertyChange (PROP_DISPLAY_NAME, old, displayName);
    }

    /** Array of all modes on this workspace.
     */
    public Set getModes () {
        return modes;
    }

    /** Get bounds of the workspace.
    * Delegates to static getWorkingSpaceBounds().
    */
    public Rectangle getBounds () {
        return getWorkingSpaceBounds();
    }

    /** Activates this workspace to be current one.
     * This leads to change of current workspace of the WindowManager.
     */
    public void activate () {
        wm.setCurrentWorkspace(this);
    }

    /** Create a new mode.
     * @param name a unique programmatic name of the mode 
     * @param displayName a human presentable (probably localized) name
     *                    of the mode (may be used by
     *{@link org.openide.actions.DockingAction}, e.g.)
     * @param icon an icon to use for the mode (e.g. on a tab or window corner);
     *             may be <code>null</code>
     * @return the new mode
     */
    public Mode createMode (String name, String displayName, URL icon) {
        return createMode(name, displayName, icon, ModeImpl.MULTI_TAB, false);
    }

    /** Create new mode.
     * @param name a unique programmatic name of the mode 
     * @param displayName a human presentable (probably localized) name
     *                    of the mode (may be used by
     *{@link org.openide.actions.DockingAction}, e.g.)
     * @param icon an icon to use for the mode (e.g. on a tab or window corner);
     *             may be <code>null</code>
     * @return the new mode
    * @param containerType type specifies type of frame representing
    *        the mode
    * @param userDefined true if mode is creted by user
    */
    public Mode createMode (String name, String displayName, URL icon,
                            int containerType, boolean userDefined) {
        // throw exception when method is used wrongly
        if (name == null) {
            throw new IllegalArgumentException("Cannot create mode with null name!"); // NOI18N
        }
        Mode newMode = findMode(name);
        if (newMode == null) {
            newMode = new ModeImpl(name, displayName, icon, containerType,
                                   userDefined, this);
            addMode(newMode);
        }
        return newMode;
    }

    /** Search all modes on this workspace by name.
     * @param name the name of the mode to search for
     * @return the mode with that name, or <code>null</code> if no such mode
     *         can be found
     */
    public Mode findMode (String name) {
        if (name == null) {
            return null;
        }
        Mode[] modeArray = (Mode[])modes.toArray(new Mode[0]);
        for (int i = 0; i < modeArray.length; i++) {
            if (name.equals(modeArray[i].getName()))
                return modeArray[i];
        }
        return null;
    }

    /** Finds mode the component is in on this workspace.
     *
     * @param c component to find mode for
     * @return the mode or null if the component is not visible on this workspace
     */
    public Mode findMode (TopComponent c) {
        if (modes == null) {
            if (System.getProperty("netbeans.debug.exceptions") != null) {
                System.out.println("Inconsistent state!"); // NOI18N
                System.out.println("Modes null....."); // NOI18N
                System.out.println("Workspace: " + getName()); // NOI18N
                Thread.dumpStack();
            }
        }
        Mode[] modeArray = (Mode[])modes.toArray(new Mode[0]);
        for (int i = 0; i < modeArray.length; i++) {
            TopComponent[] tcs = modeArray[i].getTopComponents();
            for (int y = 0; y < tcs.length; y++) {
                if (c.equals(tcs[y])) {
                    // System.out.println("Found # " + y + " " + tcs[y].getName()); // NOI18N
                    return modeArray[i];
                }
            }
        }
        return null;
    }

    /** @return true if given top component is opened on this workspace,
    * false otherwise */
    public boolean isOpened (TopComponent tc) {
        return wm.findManager(tc).isOpened(this);
    }

    /** Clears this workspace and removee this workspace from
    * window manager, if possible */
    public void remove () {
        // clear the workspace
        if (!modes.isEmpty()) {
            // iterate through modes and try to close them
            ModeImpl[] modeArray = (ModeImpl[])modes.toArray(new ModeImpl[0]);
            for (int i = 0; i < modeArray.length; i++) {
                modeArray[i].close();
            }
            modes.clear();
            // fire changes
            changeSupport.firePropertyChange(PROP_MODES, null, null);
        }
        // remove this workspace
        Workspace[] wss = wm.getWorkspaces();
        int i = 0;
        for (i = 0; i < wss.length; i++) {
            if (this.equals(wss[i])) {
                break;
            }
        }
        if (i < wss.length) {
            Workspace[] nwss = new Workspace[wss.length-1];
            System.arraycopy(wss, 0, nwss, 0, i);
            System.arraycopy(wss, i + 1, nwss, i, wss.length - (i + 1));
            wm.setWorkspaces(nwss);
        }
    }

    /** Add a property change listener.
     * @param list the listener to add
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    /** Remove a property change listener.
     * @param list the listener to remove
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    /** Shows or hides all content of this workspace. */
    public synchronized void setVisible (boolean visible) {
        if (this.visible == visible)
            return;
        if (visible && (!wm.deferredPerformer().canImmediatelly())) {
            // show workspace later
            wm.deferredPerformer().putRequest(this, null);
            return;
        }
        this.visible = visible;
        // save active for later reactivation if hiding
        if (!visible) {
            savedActive = TopComponent.getRegistry().getActivated();
            if ((savedActive != null) && (findMode(savedActive) == null))
                savedActive = null;
            wm.activateComponent(null);
            if (savedActive != null) {
                //System.out.println("Saved: " + savedActive.getName()); // NOI18N
            }
        }
        // set visibility for all modes
        for (Iterator iter = modes.iterator(); iter.hasNext(); ) {
            ((ModeImpl)iter.next()).setVisible(visible);
        }
        // update toolbar configuration and active component
        if (visible) {
            activateConfiguration();
            if (savedActive != null) {
                // request focus later
                SwingUtilities.invokeLater(new Runnable () {
                                               public void run () {
                                                   // synchronized to prevent from bug #5539
                                                   synchronized (WorkspaceImpl.this) {
                                                       if (savedActive != null) {
                                                           savedActive.requestFocus();
                                                           savedActive = null;
                                                       }
                                                   }
                                               }
                                           });
            } else {
                // activation after deserialization
                reactivateSavedTc();
            }
        }
    }

    /** Implementation of DeferredPerformer.DeferredCommand interface.
    * Sets this workspace to be visible. */
    public void performCommand (Object context) {
        setVisible(true);
    }

    public boolean isVisible () {
        return visible;
    }

    /**
     * @return toolbar configuration name
     */
    public String getToolbarConfigName () {
        return toolbarConfigName;
    }

    /**
     * Sets toolbar configuration for workspace
     * @param name of toolbar configuration for this workspace
     */
    public void setToolbarConfigName (String name) {
        toolbarConfigName = name;
        if (this.equals(wm.getCurrentWorkspace()))
            activateConfiguration();
    }

    /**
     * Prepare toolbar configuration
     */
    void activateConfiguration () {
        if (toolbarPool == null)
            toolbarPool = ToolbarPool.getDefault();
        //System.out.println("Setting: " + toolbarConfigName); // NOI18N
        toolbarPool.setConfiguration (toolbarConfigName);
    }

    /** Adds given mode to this workspace. */
    public synchronized void addMode (Mode mode) {
        modes.add(mode);
        changeSupport.firePropertyChange(PROP_MODES, null, null);
    }

    /** Removes given mode from this workspace. */
    public synchronized void removeMode (Mode mode) {
        modes.remove(mode);
        changeSupport.firePropertyChange(PROP_MODES, null, null);
    }

    /** Sets new cascading origin for this workspace.
    * Cascading origin is specified using percentage and 
    * relative to left upper corner of IDE desktop
    * (main window's left bottom corner for SDI,
    * left bottom corner of main toolbar and workspace panel for MDI.<br>
    * So - setCascadingOrigin(0, 0) means upper left corner,
    * setCascadingOrigin(100, 100) means bottom right
    * corner of "IDE working space"
    */
    public void setCascadingOrigin (Point cascadingOrigin) {
        this.cascadingOrigin = cascadingOrigin;
        recomputeCascadePoint();
    }

    /* @return Current cascading origin for this workspace */
    public Point getCascadingOrigin () {
        return cascadingOrigin;
    }

    /** Utility method which activates top component that was active
    * before serialization. */
    private void reactivateSavedTc () {
        if (savedManager == null) {
            return;
        }
        final TopComponent toBeActivated = savedManager.getComponent();
        /*
        System.out.println("Saved manager: " + savedManager);
        System.out.println("Saved tc: " + savedManager.getComponent());
        */
        savedManager = null;
        // component can be resolved to null after bad serialization,
        // be prepared to such situation
        if (toBeActivated != null) {
            // delay focus transferring and pass it to the AWT Event queue
            SwingUtilities.invokeLater(new Runnable() {
                                           public void run () {
                                               toBeActivated.requestFocus();
                                           }
                                       });
        }
    }

    /** Corrects the bounds of given list of modes */
    private void correctBounds (Set modes) {
        // correct the size if the screen is different
        Dimension currentScreen = Toolkit.getDefaultToolkit().getScreenSize ();
        // do nothing if the same
        if (screenSize.equals (currentScreen))
            return;
        double dx = ((double)currentScreen.width) / ((double)screenSize.width);
        double dy = ((double)currentScreen.height) / ((double)screenSize.height);
        // correct the bounds of modes
        Mode curMode = null;
        for (Iterator iter = modes.iterator(); iter.hasNext(); ) {
            curMode = (Mode)iter.next();
            Rectangle r = (Rectangle)curMode.getBounds();
            if (r != null) {
                r.x = (int)(r.x * dx);
                r.y = (int)(r.y * dy);
                r.width = (int)(r.width * dx);
                r.height = (int)(r.height * dy);
                curMode.setBounds(r);
            }
        }
    }

    /** @return Current cascade point of this workspace. Cascade point
    * is a point where mode screen representation (window, internal
    * frame or whatever) will be placed when added to this workspace.
    */
    Point getCascadePoint () {
        Window mainWindow = wm.getMainWindow();
        Rectangle mainBounds = mainWindow.getBounds();
        // check variables
        if (lastMainBounds == null)
            lastMainBounds = mainBounds;
        if (cascadeStepsCount <= 0)
            cascadePoint = null;
        // recompute cascade point if needed
        if ((cascadePoint == null) || (!lastMainBounds.equals(mainBounds)) ||
                (((mainBounds.width == 0) && (mainBounds.height == 0)))) {
            recomputeCascadePoint();
            lastMainBounds = mainBounds;
        }
        return cascadePoint;
    }

    /** Recomputes location of the cascading point. Called when
    * no cascade point is computed yet or when dimensions of 
    * IDE working space (either in SDI or MDI) has changed. */
    private void recomputeCascadePoint () {
        // obtain IDE working space
        Rectangle workingSpace = getWorkingSpaceBounds();
        // compute new cascading point
        cascadePoint = new Point(
                           workingSpace.x + (cascadingOrigin.x * workingSpace.width / 100),
                           workingSpace.y + (cascadingOrigin.y * workingSpace.height / 100)
                       );
        // now compute how deep cascade could be
        int xSteps, ySteps;
        if (cascadeStep.width < 0) {
            xSteps = Math.abs(
                         (cascadingOrigin.x * workingSpace.width / 100) / cascadeStep.width
                     );
        } else {
            xSteps = Math.abs(
                         ((100 - cascadingOrigin.x) * workingSpace.width / 100) / cascadeStep.width
                     );
        }
        if (cascadeStep.height < 0) {
            ySteps = Math.abs(
                         (cascadingOrigin.y * workingSpace.height / 100) / cascadeStep.height
                     );
        } else {
            ySteps = Math.abs(
                         ((100 - cascadingOrigin.y) * workingSpace.height / 100) / cascadeStep.height
                     );
        }
        cascadeStepsCount = Math.min(xSteps, ySteps) / 2;
    }

    /** Returns bounds for working space of the IDE. called from
    * getBounds() method. */
    static Rectangle getWorkingSpaceBounds () {
        Rectangle mainBounds =
            TopManager.getDefault().getWindowManager().getMainWindow().getBounds();
        Rectangle workingSpace = null;
        // SDI case
        int bottomSpace = 0;
        if (org.openide.util.Utilities.isWindows()) {
            bottomSpace = OutputTab.TYPICAL_WINDOWS_TASKBAR_HEIGHT;
        }
        if (mainBounds.y < (screenSize.height / 2)) {
            workingSpace = new Rectangle(
                               0, mainBounds.y + mainBounds.height,
                               screenSize.width,
                               screenSize.height - bottomSpace - (mainBounds.y + mainBounds.height)
                           );
        } else {
            workingSpace = new Rectangle(
                               0, 0, screenSize.width, mainBounds.y
                           );
        }
        // MDI case - PENDING - TBD

        return workingSpace;
    }


    /** Compute the bounds for the mode according to the current
    * cascading point and preferred size of given top component.
    * Update (move) cascading point.
    * This methoid is used when no mode bounds are specified explixitly.
    */
    void placeMode (Mode mode, TopComponent tc) {
        // check
        if (mode.getBounds() != null)
            return;
        // compute
        Rectangle modeBounds = new Rectangle();
        // start in cascading point
        Point cascadePoint = getCascadePoint();
        modeBounds.x = cascadePoint.x;
        modeBounds.y = cascadePoint.y;
        // set size like top component wishes, if possible
        Dimension d = tc.getPreferredSize();
        if ((d == null) || ((d.width == 0) && (d.height == 0))) {
            // set default size
            modeBounds.width =
                Math.min(DEFAULT_MODE_WIDTH, screenSize.width - modeBounds.x);
            modeBounds.height =
                Math.min(DEFAULT_MODE_HEIGHT, screenSize.height - modeBounds.y);
        } else {
            // the bounds must not exceed screen bounds
            modeBounds.width = Math.min(d.width, screenSize.width - modeBounds.x);
            modeBounds.height = Math.min(d.height, screenSize.height - modeBounds.y);
        }
        // update cascading point
        cascadePoint.x += cascadeStep.height;
        cascadePoint.y += cascadeStep.width;
        cascadeStepsCount--;
        // finally place the mode
        mode.setBounds(modeBounds);
    }

    /** Getter for the formatted information message during loading */
    static MessageFormat getLoadingMessage () {
        if (loadingMessage == null) {
            loadingMessage =
                new MessageFormat(NbBundle.getBundle(Workspace.class).
                                  getString("FMT_LoadingWorkspace"));
        }
        return loadingMessage;
    }

    /** Utility method, removes modes which are user defined and
    * orphan from the given set of the modes */
    void excludeUserOrphans (Set modes) {
        ModeImpl curMode = null;
        // just for debugging
        if ((modes == null) &&
                (System.getProperty("netbeans.debug.exceptions") != null)) {
            System.out.println("Workspace in inconsistent state!"); // NOI18N
            System.out.println("Name: " + getName()); // NOI18N
            Thread.dumpStack();
        }
        ModeImpl[] modeArray = (ModeImpl[])modes.toArray(new ModeImpl[0]);
        for (int i = 0; i < modeArray.length; i++) {
            if (modeArray[i].isUserDefined() && modeArray[i].isOrphan()) {
                //System.out.println("Excluding " + modeArray[i].getName()); // NOI18N
                modes.remove(modeArray[i]);
            }
        }
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
        result.putVersion(new Version2());
        result.putVersion(new Version3());
        return result;
    }

    /** Let instance of properly parametrized DefaultReplacer to keep
    * persistent state of this workspace
    */
    private Object writeReplace ()
    throws ObjectStreamException {
        // exclude unwanted modes (user defined orphans)
        excludeUserOrphans(modes);
        // provide versions with data
        Version1 version =
            (Version1)serializationManager().getVersion(Version1.NAME);
        version.assignData(this);
        version =
            (Version1)serializationManager().getVersion(Version2.NAME2);
        version.assignData(this);
        version =
            (Version1)serializationManager().getVersion(Version3.NAME3);
        version.assignData(this);
        // use replacer
        return new DefaultReplacer(new VSAccess(serializationManager()));
    }

    /** Called when first phase of WS deserialization is done.
    * Ask all contained modes for self validation.
    */
    void validateSelf () {
        // turn to array to prevent concurrent modifications
        ModeImpl[] modeArray =
            (ModeImpl[])modes.toArray(new ModeImpl[modes.size()]);
        // validate modes themself
        for (int i = 0; i < modeArray.length; i++) {
            modeArray[i].validateSelf(this);
        }
    }

    /** Called after validateSelf() as second validation step.
    * Ask all contained modes data validation. */
    void validateData () {
        // turn to array to prevent concurrent modifications
        ModeImpl[] modeArray =
            (ModeImpl[])modes.toArray(new ModeImpl[modes.size()]);
        // validate data (top component containers and top components)
        for (int i = 0; i < modeArray.length; i++) {
            modeArray[i].validateData();
        }
    }


    /** Basic version of persistence for workspace implementation.
    * Method assignData(workspace) must be call before writing */
    private static class Version1
        implements DefaultReplacer.ResVersionable {

        /* identification string */
        public static final String NAME = "Version_1.0"; // NOI18N

        /** variables which form persistent state of the workspace */
        String name;
        String displayName;
        Dimension cascadeStep;
        int cascadeStepsCount;
        Point cascadePoint;
        String toolbarConfigName;
        HashSet modes;
        Mode current;
        /** asociation with workspace to be written, used during writing */
        WorkspaceImpl workspace;

        /** Identification of the version */
        public String getName () {
            return NAME;
        }

        /** Assigns data to be written. Must be called before writing */
        public synchronized void assignData (WorkspaceImpl workspace) {
            this.workspace = workspace;
        }

        /** read the data of the version from given input */
        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException {
            // read fields
            name = (String)in.readObject();
            displayName = (String)in.readObject();
            cascadeStep = (Dimension)in.readObject();
            cascadeStepsCount = ((Integer)in.readObject()).intValue();
            cascadePoint = (Point)in.readObject();
            toolbarConfigName = (String)in.readObject();
            modes = (HashSet)in.readObject();
            current = (Mode)in.readObject();
        }

        /** write the data of the version to given output */
        public synchronized void writeData (ObjectOutput out)
        throws IOException {
            // write fields
            out.writeObject(workspace.name);
            out.writeObject(workspace.displayName);
            out.writeObject(workspace.cascadeStep);
            out.writeObject(new Integer(workspace.cascadeStepsCount));
            out.writeObject(workspace.cascadePoint);
            out.writeObject(workspace.toolbarConfigName);
            out.writeObject(workspace.modes);
            out.writeObject(workspace.current);
        }

        /** First tries to resolve to existing workspace with the name
        * equal to deserialized name. If no such workspace can be found,
        * returns properly initialized newly created instance of workspace
        * implementation */
        public Object resolveData ()
        throws ObjectStreamException {
            WorkspaceImpl result = (WorkspaceImpl)
                                   TopManager.getDefault().getWindowManager().findWorkspace(name);
            if (result == null) {
                // workspace don't exist, create new one and fill it
                result = new WorkspaceImpl(name);
                result.displayName = displayName;
                result.cascadeStep = cascadeStep;
                result.cascadeStepsCount = cascadeStepsCount;
                result.cascadePoint = cascadePoint;
                result.toolbarConfigName = toolbarConfigName;
                result.modes = modes;
                result.current = current;
            } else {
                // try to add modes that don't exist yet in the workspace,
                // but leave other attributes untouched
                Mode curMode = null;
                for (Iterator iter = modes.iterator(); iter.hasNext(); ) {
                    curMode = (Mode)iter.next();
                    if (result.findMode(curMode.getName()) == null) {
                        result.addMode(curMode);
                    }
                }
            }
            return result;
        }

    } // end of Version1 inner class

    /** Enhanced serialization protocol - adds cascading origin,
    * identification of last activated top component */
    private static class Version2 extends Version1 {
        /* identification string */
        public static final String NAME2 = "Version_2.0"; // NOI18N

        Point cascadingOrigin;
        WindowManagerImpl.TopComponentManager savedManager;

        /** Identification of the version */
        public String getName () {
            return NAME2;
        }

        /** read the data of the version from given input */
        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException {
            super.readData(in);
            cascadingOrigin = (Point)in.readObject();
            savedManager = (WindowManagerImpl.TopComponentManager)in.readObject();
        }

        /** write the data of the version to given output */
        public synchronized void writeData (ObjectOutput out)
        throws IOException {
            // write fields
            super.writeData(out);
            out.writeObject(workspace.cascadingOrigin);
            TopComponent tc = (workspace.savedActive == null)
                              ? TopComponent.getRegistry().getActivated()
                              : workspace.savedActive;
            if ((tc != null) && (tc.isOpened(workspace))) {
                out.writeObject(WindowManagerImpl.findManager(tc));
            } else {
                out.writeObject(null);
            }
        }

        public Object resolveData ()
        throws ObjectStreamException {
            WorkspaceImpl result = (WorkspaceImpl)super.resolveData();
            WorkspaceImpl existing = (WorkspaceImpl)
                                     TopManager.getDefault().getWindowManager().findWorkspace(this.name);
            if (existing == null) {
                // workspace didn't exist, fill it
                result.cascadingOrigin = cascadingOrigin;
                result.savedManager = savedManager;
            }
            return result;
        }

    } // end of Version2 inner class

    /** Enhanced serialization protocol - adds fromBundle flag */
    private static class Version3 extends Version2 {
        /* identification string */
        public static final String NAME3 = "Version_3.0"; // NOI18N

        boolean fromBundle;

        /** Identification of the version */
        public String getName () {
            return NAME3;
        }

        /** read the data of the version from given input */
        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException {
            super.readData(in);
            fromBundle = ((Boolean)in.readObject()).booleanValue();
        }

        /** write the data of the version to given output */
        public synchronized void writeData (ObjectOutput out)
        throws IOException {
            // write fields
            super.writeData(out);
            out.writeObject(new Boolean(workspace.fromBundle));
        }

        public Object resolveData ()
        throws ObjectStreamException {
            WorkspaceImpl result = (WorkspaceImpl)super.resolveData();
            WorkspaceImpl existing = (WorkspaceImpl)
                                     TopManager.getDefault().getWindowManager().findWorkspace(this.name);
            if (existing == null) {
                // workspace didn't exist, fill it
                result.fromBundle = fromBundle;
            }
            return result;
        }

    } // end of Version3 inner class

    /** Implementation of persistent access to our version serializator */
    private static final class VSAccess implements DefaultReplacer.Access {
        /** serialVersionUID */
        private static final long serialVersionUID = 2367034636261092266L;

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
*  36   Gandalf-post-FCS1.32.1.2    4/20/00  David Simonek   
*  35   Gandalf-post-FCS1.32.1.1    4/20/00  David Simonek   
*  34   Gandalf-post-FCS1.32.1.0    4/20/00  David Simonek   from bundle added
*  33   Gandalf   1.32        2/25/00  David Simonek   #5859 bugfix
*  32   Gandalf   1.31        1/15/00  David Simonek   mutliwindow title bug 
*       fixed
*  31   Gandalf   1.30        1/13/00  David Simonek   i18n
*  30   Gandalf   1.29        1/12/00  Ian Formanek    NOI18N
*  29   Gandalf   1.28        1/10/00  David Simonek   minor changes connected 
*       with focus problematics
*  28   Gandalf   1.27        12/17/99 David Simonek   #1913, #2970
*  27   Gandalf   1.26        12/6/99  David Simonek   method getBounds() added
*  26   Gandalf   1.25        11/30/99 David Simonek   neccessary changes needed
*       to change main explorer to new UI style  (tabs are full top components 
*       now, visual workspace added, layout of editing workspace chnaged a bit)
*  25   Gandalf   1.24        11/10/99 David Simonek   debug comments removed
*  24   Gandalf   1.23        11/6/99  David Simonek   serialization bug fixing
*  23   Gandalf   1.22        11/4/99  David Simonek   ws serialization bugfixes
*  22   Gandalf   1.21        11/3/99  David Simonek   completely rewritten 
*       serialization of windowing system...
*  21   Gandalf   1.20        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  20   Gandalf   1.19        10/7/99  David Simonek   debug prints removed...
*  19   Gandalf   1.18        10/6/99  David Simonek   more robust serialization
*       of window system (especially editor TCs)
*  18   Gandalf   1.17        9/8/99   David Simonek   deferred opening and 
*       firing of selected nodes, state management
*  17   Gandalf   1.16        8/18/99  Ian Formanek    Generated serial version 
*       UID
*  16   Gandalf   1.15        8/17/99  David Simonek   persistent main window 
*       positioning issues
*  15   Gandalf   1.14        8/14/99  David Simonek   bugfixes, #3347, #3274 
*       etc.
*  14   Gandalf   1.13        8/9/99   David Simonek   
*  13   Gandalf   1.12        8/1/99   David Simonek   debug prints commented
*  12   Gandalf   1.11        8/1/99   David Simonek   
*  11   Gandalf   1.10        7/31/99  David Simonek   small additions 
*  10   Gandalf   1.9         7/30/99  David Simonek   serialization fixes
*  9    Gandalf   1.8         7/30/99  David Simonek   iconification bugfixes, 
*       focus bugfixes
*  8    Gandalf   1.7         7/29/99  David Simonek   further ws serialization 
*       changes
*  7    Gandalf   1.6         7/28/99  David Simonek   serialization of window 
*       system...first draft :-)
*  6    Gandalf   1.5         7/22/99  Libor Kramolis  
*  5    Gandalf   1.4         7/21/99  David Simonek   window system updates...
*  4    Gandalf   1.3         7/20/99  David Simonek   various window system 
*       updates
*  3    Gandalf   1.2         7/12/99  Jaroslav Tulach Fixed a 
*       NullPointerException
*  2    Gandalf   1.1         7/12/99  Jaroslav Tulach To be compilable.
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/
