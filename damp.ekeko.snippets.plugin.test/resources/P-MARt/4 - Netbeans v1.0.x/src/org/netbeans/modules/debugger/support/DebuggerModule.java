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

package org.netbeans.modules.debugger.support;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.reflect.Method;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.Keymap;

import org.netbeans.modules.debugger.support.actions.AddBreakpointAction;
import org.netbeans.modules.debugger.support.actions.ConnectAction;
import org.netbeans.modules.debugger.support.actions.SuspendDebuggerAction;
import org.netbeans.modules.debugger.support.actions.ResumeDebuggerAction;
import org.netbeans.modules.debugger.support.actions.DebuggerViewAction;
import org.netbeans.modules.debugger.support.nodes.DebuggerView;
import org.netbeans.modules.debugger.support.nodes.DebuggerNode;
import org.netbeans.modules.debugger.support.nodes.BreakpointsRootNode;
import org.netbeans.modules.debugger.support.nodes.ThreadGroupNode;
import org.netbeans.modules.debugger.support.nodes.WatchesRootNode;

import org.openide.actions.StartDebuggerAction;
import org.openide.debugger.DebuggerException;
import org.openide.debugger.DebuggerType;
import org.openide.options.ContextSystemOption;
import org.openide.loaders.*;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;
import org.openide.windows.TopComponent;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.TopManager;

import org.openidex.util.Utilities2;

/**
* Module installation class for HtmlModule
*
* @author Jan Jancura
*/
public class DebuggerModule extends org.openide.modules.ModuleInstall {

    static final long serialVersionUID = 2700701939804772882L;


    // static ..............................................................................

    /** name of debugger mode */
    public static final String            MODE_NAME = "debugger"; // NOI18N
    /** name of debugger workspace */
    public static final String            WORKSPACE_NAME = ((StartDebuggerAction) StartDebuggerAction.get
            (StartDebuggerAction.class)).getWorkspace ();

    private static Keymap                 map;
    /** Node representing debugger breakpoints. */
    private static BreakpointsRootNode    breakpointsRootNode;
    /** Node representing debugger threads. */
    private static ThreadGroupNode        threadGroupsRootNode;
    /** Node representing debugger watches. */
    private static WatchesRootNode        watchesRootNode;
    /** Debugger view workspace */
    private static Workspace              workspace;
    /** Stores all views of debugger window. */
    private static LinkedList             views = new LinkedList ();
    /** Stores all nodes representing debugger in the explorer. */
    private static LinkedList             nodes = new LinkedList ();
    /** Number of starts counter. */
    private static int                    numberOfStarts = 0;
    protected static DebuggerModule       defaultModule;
    /** TopComponents to close when a main window is opened [PATCH]. */
    private static ArrayList              viewsToClose = new ArrayList ();
    private static boolean                patchInitialized = false;
    /** If true method installWorkspace () will be called when a main window is
    * opened [PATCH]. 
    */
    private static boolean                installWorkspaces = false;

    private transient PropertyChangeListener projectListener = null;

    // init ..................................................................................

    public DebuggerModule () {
        if (defaultModule == null) {
            defaultModule = this;
            java.awt.Window mainWindow = TopManager.getDefault ().
                                         getWindowManager ().getMainWindow ();
            mainWindow.addWindowListener (new Validator ());
        } else {
            if (defaultModule.getClass ().isAssignableFrom (getClass ()))
                defaultModule = this;
        }
    }

    public void readExternal (final java.io.ObjectInput objectInput)
    throws java.io.IOException, java.lang.ClassNotFoundException {
        super.readExternal (objectInput);
        numberOfStarts = objectInput.readInt ();
    }

    public void writeExternal (final java.io.ObjectOutput objectOutput)
    throws java.io.IOException {
        super.writeExternal (objectOutput);
        objectOutput.writeInt (numberOfStarts);
    }


    // ModuleInstall implementation ........................................................

    /** Module installed for the first time. */
    public void installed () {
        installActions ();
        installWorkspaces = true;

        TopManager.getDefault ().addPropertyChangeListener (projectListener = new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (evt.getPropertyName ().equals (TopManager.PROP_PLACES)) {
                            updateDefaultDebuggerType ();
                        }
                    }
                });

    }

    /**
    * Called when the module is loaded and the version is higher than
    * by the previous load
    * The default implementation calls restored().
    * @release The major release number of the <B>old</B> module code name or -1 if not specified.
    * @specVersion The specification version of the this <B>old</B> module.
    */
    public void updated ( int release, String specVersion ) {
        installed ();
    }

    /** Module installed again. */
    public void restored () {

        numberOfStarts ++;

        // initiate nodes.
        getNodes ();

        // Assign the Alt+5 to DebuggerViewAction

        /*
        try {
          map = TopManager.getDefault ().getGlobalKeymap ();
          assign ("A-5", "org.netbeans.modules.debugger.support.actions.DebuggerViewAction");
    } catch (ClassNotFoundException e) {
          if (System.getProperty ("netbeans.debug.exceptions") != null) {
            e.printStackTrace ();
          }
          // ignore failure to install
    }
        */

        TopManager.getDefault ().addPropertyChangeListener (projectListener = new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (evt.getPropertyName ().equals (TopManager.PROP_PLACES)) {
                            updateDefaultDebuggerType ();
                        }
                    }
                });

    }

    /** Module was uninstalled. */
    public void uninstalled () {
        uninstallActions ();
        uninstallWorkspace ();
        map = null;
        breakpointsRootNode = null;
        threadGroupsRootNode = null;
        watchesRootNode = null;
        workspace = null;
        views = new LinkedList ();
        nodes = new LinkedList ();

        if (projectListener != null) {
            TopManager.getDefault ().removePropertyChangeListener (projectListener);
            projectListener = null;
        }

    }

    /** Module is being closed. */
    public boolean closing () {
        try {
            TopManager.getDefault ().getDebugger ().finishDebugger ();
        } catch (Throwable e) {
        }
        return true; // agree to close
    }


    // main public methods ...................................................................

    /**
    * Adds a new debugger view.
    *
    * @param replace If true, previous view of the same type is replaced, otherwise not.
    */
    public static void addView (TopComponent view, boolean replace) {
        TopComponent dview;
        if (view instanceof DebuggerView)
            dview = getViewFor (
                        ((DebuggerView) view).getExplorerManager ().getRootContext ()
                    );
        else
            dview = getView (view.getClass ());

        if (dview == null) {
            views.add (view);
        } else
            if (replace) {
                closeView (dview);
                views.set (views.indexOf (dview), view);
            }
    }

    /**
    * Closes given debugger view.
    */
    public static void closeView (final TopComponent view) {
        if (patchInitialized) {
            RequestProcessor.postRequest (new Runnable () {
                                              public void run () {
                                                  SwingUtilities.invokeLater (new Runnable () {
                                                                                  public void run () {
                                                                                      if (view.isOpened ()) {
                                                                                          view.setCloseOperation (DebuggerView.CLOSE_EACH);
                                                                                          view.close (null);
                                                                                      }
                                                                                  }
                                                                              });
                                              }
                                          }, 1000);
        } else
            viewsToClose.add (view);
    }

    /**
    * Returns debugger view of given type.
    */
    public static DebuggerView getViewFor (Node rn) {
        Iterator it = views.iterator ();
        while (it.hasNext ()) {
            TopComponent viewObj = (TopComponent) it.next ();
            if (!(viewObj instanceof DebuggerView)) continue;
            DebuggerView view = (DebuggerView) viewObj;
            if (view.getExplorerManager ().getRootContext ().equals (rn))
                return view;
        }
        return null;
    }

    /**
    * Returns debugger view of given type.
    */
    public static TopComponent getView (Class cls) {
        Iterator it = views.iterator ();
        while (it.hasNext ()) {
            TopComponent view = (TopComponent) it.next ();
            if (view.getClass ().equals (cls))
                return view;
        }
        return null;
    }

    /**
    * Returns node of given type.
    */
    public static Node getNode (Class cls) {
        Iterator it = nodes.iterator ();
        while (it.hasNext ()) {
            Object node = it.next ();
            if (node.getClass ().equals (cls))
                return (Node) node;
        }
        return null;
    }

    /**
    * Returns all debugger views of debugger vindow.
    */
    public static Collection getViews () {
        return Collections.unmodifiableList (views);
    }

    /**
    * Adds custom node to the debugger root node in the explorer
    * hierarchy.
    */  
    public static void addNode (Node node) {
        nodes.add (node);
        Node e = TopManager.getDefault ().getPlaces ().nodes ().environment ();
        Node[] ee = e.getChildren ().getNodes ();
        int i, k = ee.length;
        for (i = 0; i < k; i++)
            if (ee [i] instanceof DebuggerNode)
                break;
        if (i == k) return;
        ee [i].getChildren ().add (new Node[] {
                                       node
                                   });
    }

    /**
    * Returns all debugger views of debugger vindow.
    */
    public static Collection getNodes () {
        if (defaultModule != null)
            defaultModule.installNodes ();
        return Collections.unmodifiableList (nodes);
    }

    /**
    * Return Node representing debugger breakpoints.
    */  
    public static BreakpointsRootNode getBreakpointsRootNode () {
        if (breakpointsRootNode == null) {
            breakpointsRootNode = new BreakpointsRootNode ();
            addNode (breakpointsRootNode);
        }
        return breakpointsRootNode;
    }

    /**
    * Return Node representing debugger threads.
    */  
    public static ThreadGroupNode getThreadGroupsRootNode () {
        if (threadGroupsRootNode == null) {
            // obtain thread group
            try {
                AbstractThreadGroup threadGroup = ((AbstractDebugger) TopManager.getDefault ().getDebugger ()).getThreadGroupRoot ();
                threadGroupsRootNode = new ThreadGroupNode (getWatchesRootNode (), threadGroup);
                addNode (threadGroupsRootNode);
            } catch (DebuggerException e) {
                throw new InternalError ("Debugger not initialized"); // NOI18N
            }
        }
        return threadGroupsRootNode;
    }

    /**
    * Return Node representing debugger watches.
    */  
    public static WatchesRootNode getWatchesRootNode () {
        if (watchesRootNode == null) {
            watchesRootNode = new WatchesRootNode ();
            addNode (watchesRootNode);
        }
        return watchesRootNode;
    }

    /**
    * Returns Debugger workspace.
    */
    public static Workspace getWorkspace () {
        WindowManager wm = TopManager.getDefault ().getWindowManager ();
        workspace = wm.findWorkspace (WORKSPACE_NAME);
        if (workspace == null)
            workspace = wm.createWorkspace (WORKSPACE_NAME);
        return workspace;
    }

    /**
    * Switches to running workspace 
    */
    public static void changeWorkspace () {
        WindowManager dp = TopManager.getDefault ().getWindowManager ();
        Workspace d = dp.findWorkspace (org.openide.actions.StartDebuggerAction.getWorkspace());
        if (d != null) d.activate ();
    }

    /**
    * Installation of debugger actions.
    */
    protected void installActions () {
        try {

            DataFolder dfDebugMenu = DataFolder.create (org.openide.TopManager.getDefault ().
                                     getPlaces ().folders().menus (), "Debug"); // NOI18N
            DataFolder dfViewMenu = DataFolder.create (org.openide.TopManager.getDefault ().
                                    getPlaces ().folders().menus (), "View"); // NOI18N
            DataFolder dfDebugToolbar = DataFolder.create (org.openide.TopManager.getDefault ().
                                        getPlaces ().folders().toolbars (), "Debug"); // NOI18N
            DataFolder dfDebugFullToolbar = DataFolder.create (org.openide.TopManager.getDefault ().
                                            getPlaces ().folders().toolbars (), "DebugFull"); // NOI18N
            DataFolder dfViewToolbar = DataFolder.create (org.openide.TopManager.getDefault ().
                                       getPlaces ().folders().toolbars (), "View"); // NOI18N
            DataFolder dfDebugActions = DataFolder.create (org.openide.TopManager.getDefault ().
                                        getPlaces ().folders ().actions (), "Debug"); // NOI18N


            // menu actions ...

            Utilities2.createAction (
                AddBreakpointAction.class,
                dfDebugMenu,
                "ToggleBreakpoint", true, false, false, false // NOI18N
            );

            Utilities2.createAction (
                ConnectAction.class,
                dfDebugMenu,
                "Go", true, false, false, false // NOI18N
            );

            Utilities2.createAction (
                SuspendDebuggerAction.class,
                dfDebugMenu,
                "FinishDebugger", true, true, false, false // NOI18N
            );

            Utilities2.createAction (
                ResumeDebuggerAction.class,
                dfDebugMenu,
                "Suspend", true, false, false, true // NOI18N
            );

            Utilities2.createAction (
                DebuggerViewAction.class,
                dfDebugMenu,
                "AddWatch", true, true, true, false // NOI18N
            );

            Utilities2.createAction (
                DebuggerViewAction.class,
                dfViewMenu,
                "Output", true, false, false, false // NOI18N
            );

            // toolbars-debug actions ...

            Utilities2.createAction (
                AddBreakpointAction.class,
                dfDebugToolbar,
                "ToggleBreakpoint", true, false, false, false // NOI18N
            );

            Utilities2.createAction (
                ConnectAction.class,
                dfDebugToolbar,
                "Go", true, false, false, false // NOI18N
            );

            // toolbars-debug-full actions ...

            Utilities2.createAction (
                AddBreakpointAction.class,
                dfDebugFullToolbar,
                "ToggleBreakpoint", true, false, false, false // NOI18N
            );

            Utilities2.createAction (
                ConnectAction.class,
                dfDebugFullToolbar,
                "Go", true, false, false, false // NOI18N
            );

            Utilities2.createAction (
                SuspendDebuggerAction.class,
                dfDebugFullToolbar,
                "FinishDebugger", true, true, false, false // NOI18N
            );

            Utilities2.createAction (
                ResumeDebuggerAction.class,
                dfDebugFullToolbar,
                "Suspend", true, false, false, true // NOI18N
            );

            Utilities2.createAction (
                DebuggerViewAction.class,
                dfViewToolbar,
                "Execution", true, false, false, false // NOI18N
            );

            // install into actions pool

            Utilities2.createAction (
                AddBreakpointAction.class,
                dfDebugActions
            );

            Utilities2.createAction (
                ConnectAction.class,
                dfDebugActions
            );

            Utilities2.createAction (
                SuspendDebuggerAction.class,
                dfDebugActions
            );

            Utilities2.createAction (
                ResumeDebuggerAction.class,
                dfDebugActions
            );

            Utilities2.createAction (
                DebuggerViewAction.class,
                dfDebugActions
            );

        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                e.printStackTrace ();
            }
            // ignore failure to install
        }
    }

    /**
    * Remove of debugger actions.
    */
    protected void uninstallActions () {
        try {
            DataFolder dfDebugMenu = DataFolder.create (org.openide.TopManager.getDefault ().
                                     getPlaces ().folders().menus (), "Debug"); // NOI18N
            DataFolder dfViewMenu = DataFolder.create (org.openide.TopManager.getDefault ().
                                    getPlaces ().folders().menus (), "View"); // NOI18N
            DataFolder dfDebugToolbar = DataFolder.create (org.openide.TopManager.getDefault ().
                                        getPlaces ().folders().toolbars (), "Debug"); // NOI18N
            DataFolder dfDebugFullToolbar = DataFolder.create (org.openide.TopManager.getDefault ().
                                            getPlaces ().folders().toolbars (), "DebugFull"); // NOI18N
            DataFolder dfViewToolbar = DataFolder.create (org.openide.TopManager.getDefault ().
                                       getPlaces ().folders().toolbars (), "View"); // NOI18N
            DataFolder dfDebugActions = DataFolder.create (org.openide.TopManager.getDefault ().
                                        getPlaces ().folders ().actions (), "Debug"); // NOI18N


            // menu actions ...

            Utilities2.removeAction (
                AddBreakpointAction.class,
                dfDebugMenu
            );

            Utilities2.removeAction (
                ConnectAction.class,
                dfDebugMenu
            );

            Utilities2.removeAction (
                SuspendDebuggerAction.class,
                dfDebugMenu
            );

            Utilities2.removeAction (
                ResumeDebuggerAction.class,
                dfDebugMenu
            );

            Utilities2.removeAction (
                DebuggerViewAction.class,
                dfDebugMenu
            );

            Utilities2.removeAction (
                DebuggerViewAction.class,
                dfViewMenu
            );

            // toolbars-debug actions ...

            Utilities2.removeAction (
                AddBreakpointAction.class,
                dfDebugToolbar
            );

            Utilities2.removeAction (
                ConnectAction.class,
                dfDebugToolbar
            );

            // toolbars-debug-full actions ...

            Utilities2.removeAction (
                AddBreakpointAction.class,
                dfDebugFullToolbar
            );

            Utilities2.removeAction (
                ConnectAction.class,
                dfDebugFullToolbar
            );

            Utilities2.removeAction (
                SuspendDebuggerAction.class,
                dfDebugFullToolbar
            );

            Utilities2.removeAction (
                ResumeDebuggerAction.class,
                dfDebugFullToolbar
            );

            Utilities2.removeAction (
                DebuggerViewAction.class,
                dfViewToolbar
            );

            // install into actions pool

            Utilities2.removeAction (
                AddBreakpointAction.class,
                dfDebugActions
            );

            Utilities2.removeAction (
                ConnectAction.class,
                dfDebugActions
            );

            Utilities2.removeAction (
                SuspendDebuggerAction.class,
                dfDebugActions
            );

            Utilities2.removeAction (
                ResumeDebuggerAction.class,
                dfDebugActions
            );

            Utilities2.removeAction (
                DebuggerViewAction.class,
                dfDebugActions
            );

        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                e.printStackTrace ();
            }
            // ignore failure to uninstall
        }
    }

    /**
    * Install all workspaces.
    */
    public static void installWorkspaces () {
        installWorkspaces = true;
    }

    /**
    * Installation of debugger workspace and open DebuggerWindow.
    */
    protected void installWorkspace () {
        WindowManager wm = TopManager.getDefault ().getWindowManager ();
        Workspace workspace = getWorkspace ();
        defaultModule.installViews ();

        // Opens DebuggerWindow for BuildMakera... ;-)
        ((DebuggerViewAction) SystemAction.get (DebuggerViewAction.class)).
        showDebuggerView (workspace);
    }

    /**
    * Installation of debugger views.
    */
    protected void installViews () {
        addView (new DebuggerView (true, getBreakpointsRootNode ()), false);
        addView (new DebuggerView (true, getThreadGroupsRootNode ()), false);
        addView (new DebuggerView (true, getWatchesRootNode ()), false);
    }

    /**
    * Installation of debugger nodes.
    */
    protected void installNodes () {
        getBreakpointsRootNode ();
        getThreadGroupsRootNode ();
        getWatchesRootNode ();
    }

    /**
    * Remove debgger workspace.
    */
    protected void uninstallWorkspace () {
        int i, k = views.size ();
        for (i = 0; i < k; i++) {
            TopComponent t = (TopComponent) views.get (i);
            if (t.isOpened ()) {
                t.setCloseOperation (TopComponent.CLOSE_EACH);
                t.close (null);
            }
        }
    }


    // support methods .......................................................................

    /** Assigns a key to an action
    * @param key key name
    * @param action name of the action
    */
    protected static void assign (String key, String action) throws ClassNotFoundException {
        KeyStroke str = Utilities.stringToKey (key);
        if (str == null) {
            System.err.println ("Not a valid key: " + key);
            // go on
            return;
        }

        Class actionClass = Class.forName (action);

        // create instance of the action
        SystemAction a = SystemAction.get (actionClass);

        map.addActionForKeyStroke (str, a);
    }

    /**
    * Creates debugger mode on given workspace if debugger mode
    * doesn't exist on given worksspace 
    */
    public static Mode createMode (Workspace workspace, TopComponent dv) {
        Mode m = workspace.createMode (
                     MODE_NAME,
                     NbBundle.getBundle (DebuggerModule.class).
                     getString ("CTL_Debugger_view"),
                     DebuggerModule.class.getResource (
                         "/org/netbeans/core/resources/debuggerView.gif" // NOI18N
                     )
                 );
        Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
        Window mainWindow = TopManager.getDefault ().getWindowManager ().getMainWindow ();
        int lowPoint = mainWindow.getPreferredSize ().height +
                       mainWindow.getLocation ().y;
        // compute the bounds for debugger
        Dimension prefSize = dv.getPreferredSize ();
        Rectangle bounds = new Rectangle (
                               screenSize.width - prefSize.width,
                               lowPoint,
                               prefSize.width,
                               prefSize.height
                           );
        m.setBounds (bounds);
        return m;
    }

    /** Try to set Default debugger type in JavaSettings if no Default debugger type is set. */
    private void updateDefaultDebuggerType () {
        try {
            Class debuggerClass = Class.forName ("org.netbeans.modules.debugger.delegator.DefaultDebuggerType"); // NOI18N
            if (!debuggerClass.isInstance (DebuggerType.getDefault ())) {
                Enumeration enum = TopManager.getDefault ().getServices ().services ();
                ArrayList list = new ArrayList ();
                boolean installed = false;
                while (enum.hasMoreElements ()) {
                    Object o = enum.nextElement ();
                    if (debuggerClass.isAssignableFrom (o.getClass ())) {
                        list.add (0, o);
                        installed = true;
                    } else
                        list.add (o);
                }
                /*if (!installed) {
                  Object ni = debuggerClass.getConstructor (new Class [0]).
                    newInstance (new Object [0]);
                  if ( (ni != null) && 
                       debuggerClass.isAssignableFrom (ni.getClass ())
                  ) list.add (0, ni);
            } do not works. */
                // instances of DefaultDebuggerType are placed at the beginning of the services list
                TopManager.getDefault ().getServices ().setServiceTypes (list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class jsetClass = Class.forName ("org.netbeans.modules.java.settings.JavaSettings"); // NOI18N
            Method getMethod = jsetClass.getMethod ("getDebugger", new Class [] {}); // NOI18N
            Method setMethod = jsetClass.getMethod ("setDebugger", new Class [] { DebuggerType.class }); // NOI18N
            Object jset = ContextSystemOption.findObject (jsetClass, true);
            if (getMethod.invoke (jset, new Object [] {}) == null)
                setMethod.invoke (jset, new Object [] { DebuggerType.getDefault () });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // innerclasses ..............................................................

    class Validator extends WindowAdapter {
        public void windowOpened (java.awt.event.WindowEvent ev) {
            // notify me no more
            java.awt.Window mainWindow = TopManager.getDefault ().
                                         getWindowManager ().getMainWindow ();
            mainWindow.removeWindowListener (this);

            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {

                                                if ( ((numberOfStarts == 1) && (views.size () < 1)) ||
                                                        installWorkspaces
                                                   ) {
                                                    installWorkspace ();
                                                } else
                                                    defaultModule.installViews ();

                                                // close waiting views [PATCH]
                                                int i, k = viewsToClose.size ();
                                                for (i = 0; i < k; i++) {
                                                    TopComponent tc = (TopComponent) viewsToClose.get (i);
                                                    if (tc.isOpened ()) {
                                                        tc.setCloseOperation (DebuggerView.CLOSE_EACH);
                                                        tc.close (null);
                                                    }
                                                } //for
                                                viewsToClose = new ArrayList ();
                                                patchInitialized = true;
                                            } //run
                                        });
        }
    }
}

/*
* Log
*  39   Gandalf-post-FCS1.31.4.6    4/5/00   Jan Jancura     Default debugger Typwe 
*       patch
*  38   Gandalf-post-FCS1.31.4.5    4/5/00   Daniel Prusa    Improvement of default 
*       debugger type setting
*  37   Gandalf-post-FCS1.31.4.4    4/5/00   Daniel Prusa    Setting of default 
*       debugger
*  36   Gandalf-post-FCS1.31.4.3    3/30/00  Jan Jancura     New serialization
*  35   Gandalf-post-FCS1.31.4.2    3/29/00  Jan Jancura     Serialization of debugger
*       improved
*  34   Gandalf-post-FCS1.31.4.1    3/28/00  Daniel Prusa    
*  33   Gandalf-post-FCS1.31.4.0    3/13/00  David Simonek   process of creating 
*       module's own workspace modified to be functional even in japanese and 
*       other localised versions
*  32   Gandalf   1.31        1/18/00  Daniel Prusa    StartDebugger action
*  31   Gandalf   1.30        1/15/00  Jesse Glick     Nicer actions pool 
*       installation.
*  30   Gandalf   1.29        1/13/00  Daniel Prusa    NOI18N
*  29   Gandalf   1.28        1/6/00   Daniel Prusa    Assigning of Alt+5 
*       shortcut removed (it is in Forte4J.keys now)
*  28   Gandalf   1.27        12/28/99 Ian Formanek    Debug | Debugger View 
*       menu item added
*  27   Gandalf   1.26        12/15/99 Jan Jancura     
*  26   Gandalf   1.25        12/10/99 Jan Jancura     Serialization of views 
*       and initialization redesigned.
*  25   Gandalf   1.24        11/10/99 Jan Jancura     Create new views on Open 
*       debugger view action
*  24   Gandalf   1.23        11/8/99  Jan Jancura     Somma classes renamed
*  23   Gandalf   1.22        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  22   Gandalf   1.21        10/15/99 Jan Jancura     Bug in serialization of 
*       Debugger Window 2
*  21   Gandalf   1.20        10/15/99 Jan Jancura     Bug in deserialization of
*       Debugger window
*  20   Gandalf   1.19        10/14/99 Jan Jancura     Bug in installing 
*       Debugger window
*  19   Gandalf   1.18        10/13/99 Jan Jancura     Destroy action  bug in 
*       deleting watches  deserializing of main window
*  18   Gandalf   1.17        10/5/99  Jan Jancura     Bug 4194
*  17   Gandalf   1.16        10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
*        changed to class + some methods added
*  16   Gandalf   1.15        9/3/99   Jan Jancura     
*  15   Gandalf   1.14        9/2/99   Jan Jancura     
*  14   Gandalf   1.13        8/19/99  Jan Jancura     Close debuger views on 
*       module uninstall
*  13   Gandalf   1.12        8/18/99  Jan Jancura     Localization & Current 
*       thread & Current session
*  12   Gandalf   1.11        8/17/99  Jan Jancura     Debugger nodes renamed 
*       and some moved to enterprise
*  11   Gandalf   1.10        8/10/99  Jan Jancura     
*  10   Gandalf   1.9         8/9/99   Jan Jancura     Functionality of modes 
*       moved to Module
*  9    Gandalf   1.8         8/2/99   Jan Jancura     A lot of bugs...
*  8    Gandalf   1.7         7/29/99  David Simonek   changes concerning window
*       system
*  7    Gandalf   1.6         7/29/99  David Simonek   opening on all workspaces
*       together in debugger window
*  6    Gandalf   1.5         7/23/99  David Simonek   workspace initialization 
*       fixed
*  5    Gandalf   1.4         7/22/99  David Simonek   workspace initialization
*  4    Gandalf   1.3         7/21/99  Jan Jancura     
*  3    Gandalf   1.2         7/16/99  Jan Jancura     
*  2    Gandalf   1.1         7/14/99  Jan Jancura     
*  1    Gandalf   1.0         7/13/99  Jan Jancura     
* $
*/


