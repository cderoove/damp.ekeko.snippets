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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeSupport;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.netbeans.core.output.OutputTab;
import org.netbeans.core.actions.ExecutionViewAction;
import org.netbeans.core.actions.OpenExplorerAction;
import org.netbeans.core.NbMainExplorer;
import org.netbeans.core.NbNodeOperation;

import org.openide.windows.*;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.text.EditorSupport;
import org.openide.util.actions.SystemAction;

/** Creates default workspace pool.
* Creates default workspaces and default positions of windows
*
* @author Dafe Simonek
*/
final class DefaultCreator extends WindowAdapter
            implements Runnable, WellKnownModeNames,
    WellKnownWorkspaceNames {

    /** Window manager to initialize and fill */
    WindowManagerImpl wm;
    /** Default workspaces */
    Workspace editing;
    Workspace browsing;
    Workspace running;
    Workspace debugging;
    Workspace visual;
    Workspace[] workspaces;
    /** Bounds of the working space where windoes can be placed */
    Rectangle workingSpace;
    /** references to the modes - to prevent it from garbage collecting */
    HashSet explorerModes;
    HashSet executionModes;
    HashSet outputModes;
    HashSet editorModes;

    DefaultCreator (WindowManagerImpl wm) {
        this.wm = wm;
    }

    void start () {
        initWorkspaces();
        wm.setCreated(true);
        // listen to main window (and set initial positions
        // only after main window is opened)
        WindowManagerImpl.mainWindow().addWindowListener(this);
    }

    /** Initializes default workspace */
    void initWorkspaces () {
        // Initialize the default workspace pool
        ResourceBundle bundle = NbBundle.getBundle(DefaultCreator.class);
        editing = new WorkspaceImpl(EDITING, "CTL_Workspace_Editing", true);
        browsing = new WorkspaceImpl(BROWSING, "CTL_Workspace_Browsing", true);
        running = new WorkspaceImpl(RUNNING, "CTL_Workspace_Running", true);
        debugging = new WorkspaceImpl(DEBUGGING, "CTL_Workspace_Debugging", true);
        ((WorkspaceImpl)debugging).setToolbarConfigName(DEBUGGING);
        visual = new WorkspaceImpl(VISUAL, "CTL_Workspace_Visual", true);
        // modify the workspace pool
        workspaces = new Workspace[] {
                         editing, visual, browsing, running, debugging
                     };
        wm.setWorkspaces(new Workspace[0]);
        wm.setWorkspaces(workspaces);
        wm.setCurrentWorkspace(workspaces[0]);
        // pre-load needed classes
        //RequestProcessor.postRequest(this, 0, Thread.NORM_PRIORITY);
    }

    /** Realization of runnable interface, loads needed classes */
    public void run () {
        NbMainExplorer.getExplorer();
        OutputTab.getStdOutputTab();
        ExecutionViewAction.getExecutionView();
    }

    /** Set initial positions after main window is opened */
    public void windowOpened (WindowEvent e) {
        // notify me no more
        WindowManagerImpl.mainWindow().removeWindowListener(this);
        // set important positioning variables
        workingSpace = editing.getBounds();
        Window mainWindow = WindowManagerImpl.mainWindow();
        // cascade from different point on visual workspace
        ((WorkspaceImpl)visual).setCascadingOrigin(new Point(0, 50));
        // set default positions for frames
        placeFrames();
    }

    public void placeFrames () {
        // set default window positions (main window is already visible)
        Rectangle explorerBounds = placeExplorer();
        Rectangle outputBounds = placeOutput(explorerBounds);
        Rectangle debuggerBounds = null;
        Rectangle executionBounds = placeExecution(debuggerBounds, outputBounds);
        placePropertySheet(explorerBounds);
        placeEditor(explorerBounds, outputBounds,
                    debuggerBounds, executionBounds);
        // set current workspace
        editing.activate();
        // open main explorer and global property sheet
        // on current (editing) workspace
        NbMainExplorer.getExplorer().openRoots(editing);
        NbNodeOperation.Sheet.getDefault().open(editing);
    }

    /** Places output window */
    private Rectangle placeOutput (Rectangle explorerBounds) {
        outputModes = new HashSet(5);
        // compute right bounds
        Rectangle bounds = new Rectangle(
                               workingSpace.x,
                               workingSpace.y + workingSpace.height - OutputTab.DEFAULT_WINDOW_HEIGHT,
                               workingSpace.x + workingSpace.width,
                               OutputTab.DEFAULT_WINDOW_HEIGHT
                           );
        // create appropriate modes and set the bounds
        TopComponent outputTc = OutputTab.getStdOutputTab();
        String displayName = NbBundle.getBundle(DefaultCreator.class).
                             getString("CTL_OutputWindow");
        Mode curMode = null;
        for (int i = 0; i < workspaces.length; i++) {
            curMode = workspaces[i].createMode(
                          OUTPUT, displayName,
                          DefaultCreator.class.getResource(OutputTab.ICON_RESOURCE)
                      );
            if (workspaces[i].equals(editing)) {
                // special bounds for editing workspace
                curMode.setBounds(new Rectangle(
                                      bounds.x + explorerBounds.width, bounds.y,
                                      bounds.width - explorerBounds.width, bounds.height
                                  ));
            } else if (workspaces[i].equals(visual)) {
                // special bounds for visual workspace
                curMode.setBounds(new Rectangle(
                                      workingSpace.x + (workingSpace.width / 2), bounds.y,
                                      workingSpace.width / 2, bounds.height
                                  ));
            } else {
                curMode.setBounds(bounds);
            }
            curMode.dockInto(outputTc);
            outputModes.add(curMode);
        }
        // open on running and debugging workspaces
        outputTc.open(running);
        outputTc.open(debugging);
        return bounds;
    }

    /** Places explorer window */
    private Rectangle placeExplorer () {
        explorerModes = new HashSet(5);
        // compute right bounds
        int realHeight = workingSpace.height * 2 / 3;
        Rectangle bounds = new Rectangle(
                               workingSpace.x, workingSpace.y,
                               NbMainExplorer.DEFAULT_WIDTH,
                               (realHeight > NbMainExplorer.MIN_HEIGHT)
                               ? realHeight : workingSpace.height
                           );
        // create appropriate modes and set the bounds
        TopComponent explorerTc = NbMainExplorer.getExplorer();
        String displayName = NbBundle.getBundle(DefaultCreator.class).
                             getString("CTL_ExplorerWindow");
        Mode curMode = null;
        for (int i = 0; i < workspaces.length; i++) {
            curMode = workspaces[i].createMode(
                          EXPLORER, displayName,
                          DefaultCreator.class.getResource(
                              "/org/netbeans/core/resources/frames/explorer.gif" // NOI18N
                          )
                      );
            // special position for visual workspace
            if (workspaces[i].equals(visual)) {
                curMode.setBounds(new Rectangle(
                                      workingSpace.x, workingSpace.y,
                                      workingSpace.width * 3 / 10,
                                      workingSpace.height / 2
                                  ));
            } else {
                curMode.setBounds(bounds);
            }
            curMode.dockInto(explorerTc);
            explorerModes.add(curMode);
        }
        // open on visual workspace
        NbMainExplorer.getExplorer().openRoots(visual);
        return bounds;
    }

    private Rectangle placePropertySheet (Rectangle explorerBounds) {
        TopComponent propertiesTc = NbNodeOperation.Sheet.getDefault();
        // place and open on browsing workspace
        String displayName = NbBundle.getBundle(DefaultCreator.class).
                             getString("CTL_PropertiesWindow");
        int widthOfProps = workingSpace.width / 4;
        Rectangle bounds = new Rectangle(
                               workingSpace.x + (workingSpace.width - widthOfProps), workingSpace.y,
                               widthOfProps, workingSpace.height / 2
                           );
        Mode mode = browsing.createMode(PROPERTIES, displayName, null);
        mode.setBounds(bounds);
        mode.dockInto(propertiesTc);
        propertiesTc.open(browsing);
        // place on editing workspace
        mode = editing.createMode(PROPERTIES, displayName, null);
        bounds = new Rectangle(
                     workingSpace.x,
                     explorerBounds.y + explorerBounds.height,
                     explorerBounds.width,
                     (workingSpace.y + workingSpace.height) -
                     (explorerBounds.y + explorerBounds.height)
                 );
        mode.setBounds(bounds);
        mode.dockInto(propertiesTc);
        // place on visual workspace
        mode = visual.createMode(PROPERTIES, displayName, null);
        bounds = new Rectangle(
                     workingSpace.x + (workingSpace.width * 3 / 10), workingSpace.y,
                     workingSpace.width * 2 / 10, workingSpace.height / 2
                 );
        mode.setBounds(bounds);
        mode.dockInto(propertiesTc);

        return bounds;
    }

    /** Places execution window */
    private Rectangle placeExecution (Rectangle debuggerBounds,
                                      Rectangle outputBounds) {
        executionModes = new HashSet(5);
        Dimension prefSize =
            ExecutionViewAction.getExecutionView().getPreferredSize();
        Rectangle bounds = new Rectangle(
                               workingSpace.x + workingSpace.width - prefSize.width, workingSpace.y,
                               prefSize.width, prefSize.height
                           );
        // create appropriate modes and set the bounds
        String displayName = NbBundle.getBundle(DefaultCreator.class).
                             getString("CTL_ExecutionWindow");
        TopComponent executionTc = ExecutionViewAction.getExecutionView();
        // editing
        Mode curMode = editing.createMode(
                           EXECUTION, displayName,
                           DefaultCreator.class.getResource(
                               "/org/netbeans/core/resources/frames/execution.gif" // NOI18N
                           )
                       );
        curMode.setBounds(bounds);
        curMode.dockInto(executionTc);
        executionModes.add(curMode);
        // browsing
        curMode = browsing.createMode(
                      EXECUTION, displayName,
                      DefaultCreator.class.getResource(
                          "/org/netbeans/core/resources/frames/execution.gif" // NOI18N
                      )
                  );
        curMode.setBounds(bounds);
        curMode.dockInto(executionTc);
        executionModes.add(curMode);
        // running
        curMode = running.createMode(
                      EXECUTION, displayName,
                      DefaultCreator.class.getResource(
                          "/org/netbeans/core/resources/frames/execution.gif" // NOI18N
                      )
                  );
        curMode.setBounds(bounds);
        curMode.dockInto(executionTc);
        executionModes.add(curMode);
        // special position for debugging workspace
        Rectangle result = bounds;
        if (debuggerBounds != null) {
            int ourHeight =
                outputBounds.y - (debuggerBounds.y + debuggerBounds.height);
            if (ourHeight < ExecutionViewAction.MIN_HEIGHT)
                ourHeight = workingSpace.height -
                            (debuggerBounds.y + debuggerBounds.height);
            bounds = new Rectangle(
                         debuggerBounds.x, debuggerBounds.y + debuggerBounds.height,
                         debuggerBounds.width, ourHeight
                     );
        } else {
            bounds = new Rectangle(
                         workingSpace.x + workingSpace.width - prefSize.width,
                         outputBounds.y - prefSize.height,
                         prefSize.width, prefSize.height
                     );
        }
        curMode = debugging.createMode(
                      EXECUTION, displayName,
                      DefaultCreator.class.getResource(
                          "/org/netbeans/core/resources/frames/execution.gif" // NOI18N
                      )
                  );
        curMode.setBounds(bounds);
        curMode.dockInto(executionTc);
        executionModes.add(curMode);
        // open on running
        executionTc.open(running);
        return result;
    }

    /** Places editor window */
    private void placeEditor (Rectangle explorerBounds, Rectangle outputBounds,
                              Rectangle debuggerBounds, Rectangle executionBounds) {
        editorModes = new HashSet(5);
        int realHeight = outputBounds.y - workingSpace.y;
        if (realHeight < 200)
            realHeight = workingSpace.height;
        // create appropriate modes and set the bounds
        String displayName = NbBundle.getBundle(DefaultCreator.class).
                             getString("CTL_EditorWindow");
        URL editorIcon = getEditorIcon();
        // editing workspace
        Rectangle bounds = new Rectangle (
                               explorerBounds.x + explorerBounds.width, workingSpace.y,
                               workingSpace.width - (explorerBounds.x + explorerBounds.width),
                               realHeight
                           );
        Mode curMode =
            editing.createMode(EditorSupport.EDITOR_MODE, displayName, editorIcon);
        curMode.setBounds(bounds);
        editorModes.add(curMode);
        // visual workspace
        bounds = new Rectangle (
                     workingSpace.x + (workingSpace.width / 2), workingSpace.y,
                     workingSpace.width / 2, realHeight
                 );
        curMode = visual.createMode(
                      EditorSupport.EDITOR_MODE, displayName, editorIcon
                  );
        curMode.setBounds(bounds);
        editorModes.add(curMode);
        // browsing workspace
        bounds = new Rectangle (
                     workingSpace.x, workingSpace.y + (workingSpace.height / 2),
                     workingSpace.width, workingSpace.height / 2
                 );
        curMode = browsing.createMode(
                      EditorSupport.EDITOR_MODE, displayName, editorIcon
                  );
        curMode.setBounds(bounds);
        editorModes.add(curMode);
        // running workspace
        bounds = new Rectangle (
                     workingSpace.x, workingSpace.y, executionBounds.x, realHeight
                 );
        curMode = running.createMode(
                      EditorSupport.EDITOR_MODE, displayName, editorIcon
                  );
        curMode.setBounds(bounds);
        editorModes.add(curMode);
        // debugging workspace
        if (debuggerBounds != null) {
            bounds = new Rectangle (
                         workingSpace.x, workingSpace.y, debuggerBounds.x, realHeight
                     );
        }
        curMode = debugging.createMode(
                      EditorSupport.EDITOR_MODE, displayName, editorIcon
                  );
        curMode.setBounds(bounds);
        editorModes.add(curMode);
    }

    /** Utility helper method - editor icon */
    private static URL getEditorIcon () {
        return DefaultCreator.class.getResource(
                   "/org/netbeans/core/resources/frames/editor.gif" // NOI18N
               );
    }

}

/*
* Log
*  23   Gandalf-post-FCS1.20.1.1    4/20/00  David Simonek   
*  22   Gandalf-post-FCS1.20.1.0    3/10/00  David Simonek   
*  21   Gandalf   1.20        2/18/00  David Simonek   #5708 bugfix
*  20   Gandalf   1.19        1/18/00  David Simonek   big editor icon fixed
*  19   Gandalf   1.18        1/12/00  Ian Formanek    NOI18N
*  18   Gandalf   1.17        1/9/00   David Simonek   modified initialization 
*       of the WindowManagerImpl
*  17   Gandalf   1.16        1/5/00   David Simonek   height of explorer on 
*       edditing workspace modified
*  16   Gandalf   1.15        12/21/99 David Simonek   repository now focused in
*       main explorer
*  15   Gandalf   1.14        12/6/99  David Simonek   property sheet now opens 
*       automatically on editing workspace, other positioning updated
*  14   Gandalf   1.13        11/30/99 David Simonek   neccessary changes needed
*       to change main explorer to new UI style  (tabs are full top components 
*       now, visual workspace added, layout of editing workspace chnaged a bit)
*  13   Gandalf   1.12        11/6/99  David Simonek   serialization bug fixing
*  12   Gandalf   1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  11   Gandalf   1.10        8/16/99  David Simonek   default position of 
*       property sheet on bowring workspace updated
*  10   Gandalf   1.9         8/9/99   David Simonek   
*  9    Gandalf   1.8         7/30/99  David Simonek   better initiasl 
*       positioning
*  8    Gandalf   1.7         7/29/99  David Simonek   further ws serialization 
*       changes
*  7    Gandalf   1.6         7/23/99  David Simonek   another fixes (closing a 
*       component)
*  6    Gandalf   1.5         7/21/99  David Simonek   window system updates...
*  5    Gandalf   1.4         7/20/99  David Simonek   various window system 
*       updates
*  4    Gandalf   1.3         7/16/99  Ian Formanek    renmaed to DefaultCreator
*  3    Gandalf   1.2         7/16/99  Ian Formanek    
*  2    Gandalf   1.1         7/14/99  Ales Novak      bugfixes  
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/