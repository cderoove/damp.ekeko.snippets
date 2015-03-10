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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.ImageIcon;
import javax.swing.*;

import org.openide.TopManager;
import org.openide.awt.MenuBar;
import org.openide.awt.SplittedPanel;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderInstance;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.*;
import org.openide.windows.Workspace;
import org.openide.awt.ToolbarPool;
import org.netbeans.core.windows.WindowManagerImpl;


/** The MainWindow of Corona IDE.
* This class is final only for performance reasons, can be unfinaled
* if desired.
*
* @author   Ian Formanek, Petr Hamernik
*/
public final class MainWindow extends JFrame {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1160791973145645501L;

    private static HelpCtx mainWindowHelp;

    /** The MainWindow's menu */
    private static MenuBar menuBar;

    /** The MainWindow's toolbar */
    transient private Component toolbar;

    /** Construct a new MainWindow */
    public MainWindow () {
        super();
    }

    /** Takes care of the MainWindow size
    * the height is always equal to preferred height, width can be anything between min and max 
    */
    public void validate() {
        super.validate ();
        if (getSize ().height != getPreferredSize ().height) {
            setSize (getSize ().width, getPreferredSize ().height);
            validate ();
        }
    }

    /**
     * Initializes menu and toolbar systems.
     */
    public static void initialize() {
        menuBar = new MenuBar(null);

        WindowManagerImpl.mainWindow().init();
    }
    /*
      private static ToolbarPanel.Configuration createDefaultToolbar () {
        return new ToolbarPanel.Configuration () {
          public ToolbarPanel activate () {
            return new ToolbarPanel ();
            // [PENDING - [Ian] just a no-op implementation - it is not correct]
          }

          public String getName () {
            return "Default";
          }
        };
      }
    */

    /**
     * Shows the main window
     */
    public static void showWindow() {
        MainWindow mainWindow = WindowManagerImpl.mainWindow ();

        if (menuBar != null) {
            // menu initialization
            menuBar.waitFinished();
            mainWindow.setJMenuBar(menuBar);
        }

        ToolbarPool tp = ToolbarPool.getDefault();
        tp.waitFinished ();

        tp.setConfiguration ("Standard"); // NOI18N

        mainWindow.getContentPane().add(tp);

        // workspaces and status line
        /*    SplittedPanel southLine = new SplittedPanel();
            southLine.setSplitType(SplittedPanel.HORIZONTAL);
            southLine.setSplitTypeChangeEnabled(false);
            WorkspaceSwitcher ws =
              new WorkspaceSwitcher(TopManager.getDefault().getWindowManager());
            southLine.add(ws.getComponent(), SplittedPanel.ADD_FIRST);
            southLine.add(StatusLine.createLabel(), SplittedPanel.ADD_SECOND);
            southLine.setSplitPosition(SplittedPanel.FIRST_PREFERRED);
            mainWindow.getContentPane().add(southLine, BorderLayout.SOUTH); */

        JPanel southPanel = new JPanel();
        southPanel.setLayout (new BorderLayout ());
        WorkspaceSwitcher ws =
            new WorkspaceSwitcher(TopManager.getDefault().getWindowManager());
        southPanel.add(ws.getComponent(), BorderLayout.WEST);
        southPanel.add(StatusLine.createLabel(), BorderLayout.CENTER);
        mainWindow.getContentPane().add(southPanel, BorderLayout.SOUTH);

        WindowManagerImpl wm =
            (WindowManagerImpl)TopManager.getDefault().getWindowManager();
        if (!wm.isMainPositioned()) {
            mainWindow.pack();
        }
        mainWindow.show();
        mainWindow.getRootPane().requestDefaultFocus();
    }

    public void init() {

        // initialize frame
        javax.swing.ImageIcon ideIcon =
            new javax.swing.ImageIcon (
                Toolkit.getDefaultToolkit ().getImage (
                    getClass ().getResource (org.openide.util.Utilities.isLargeFrameIcons() ?
                                             "/org/netbeans/core/resources/frames/ide32.gif" : // NOI18N
                                             "/org/netbeans/core/resources/frames/ide.gif"))); // NOI18N

        setIconImage (ideIcon.getImage ());
        updateTitle ();
        setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);


        Container content = getContentPane();
        //    content.setLayout(new BorderLayout());

        // status line initialization
        /* [PENDING]
        WorkspaceSwitcher ds = new WorkspaceSwitcher(TopManager.getDefault().getWorkspacePool());
        */

        addWindowListener (new WindowAdapter() {
                               public void windowClosing(WindowEvent evt) {
                                   (new org.netbeans.core.actions.SystemExit()).performAction();
                               }

                               public void windowActivated (WindowEvent evt) {
                                   if (mainWindowHelp == null) {
                                       mainWindowHelp = new HelpCtx(MainWindow.class);
                                       HelpCtx.setHelpIDString (getRootPane (), mainWindowHelp.getHelpID ());
                                   }
                               }

                               /** When deactivated closes all popup menus
                               */
/****** XXX(-tdt) on JDK 1.3, Linux at least, windowDeactivated() is called
******* whenever a menu popup is shown, this code would make the menu disappear
******* immediately after it is displayed
                               
                               public void windowDeactivated (WindowEvent e) {
                                   final MenuSelectionManager msm = MenuSelectionManager.defaultManager ();
                                   final MenuElement[] path = msm.getSelectedPath ();

                                   // post request that should after half of second clear the selected menu
                                   RequestProcessor.postRequest (new Runnable () {
                                                                     private boolean secondTime;

                                                                     public void run () {
                                                                         if (!secondTime) {
                                                                             // we have to be sure we run in AWT thread
                                                                             secondTime = true;
                                                                             SwingUtilities.invokeLater (this);
                                                                             return;
                                                                         }

                                                                         MenuElement[] newPath = msm.getSelectedPath ();
                                                                         if (newPath.length != path.length) return;
                                                                         for (int i = 0; i < newPath.length; i++) {
                                                                             if (newPath[i] != path[i]) return;
                                                                         }
                                                                         msm.clearSelectedPath ();
                                                                     }
                                                                 }, 200);

                               }
**********************/
                               }
                          );
    }


    /** Updates the MainWindow's title */
    void updateTitle () {
        String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
        setTitle (java.text.MessageFormat.format (NbBundle.getBundle(MainWindow.class).getString ("CTL_MainWindow_Title"),
                  new Object[] { buildNumber }));
    }

    /** Returns preferredSize as the preferred height and the widht of the screen */
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        Dimension minimumSize = getMinimumSize();
        Dimension maximumSize = getMaximumSize();
        size.width = maximumSize.width;

        if (size.height < minimumSize.height) size.height = minimumSize.height;
        if (size.height > maximumSize.height) size.height = maximumSize.height;

        return size;
    }

    public Dimension getMinimumSize() {
        return new Dimension(320, 32);
    }

    public Dimension getMaximumSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

}

/*
 * Log
 *  47   Gandalf   1.46        1/24/00  Jaroslav Tulach MenuSelectionManager is 
 *       modified in AWT thread.
 *  46   Gandalf   1.45        1/14/00  Jesse Glick     Moving versioning info 
 *       out of localizable range.
 *  45   Gandalf   1.44        1/13/00  Jaroslav Tulach I18N
 *  44   Gandalf   1.43        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  43   Gandalf   1.42        10/7/99  David Simonek   request focus related 
 *       bugs repaired
 *  42   Gandalf   1.41        8/17/99  David Simonek     
 *  41   Gandalf   1.40        8/17/99  David Simonek   persistent main window 
 *       positioning issues
 *  40   Gandalf   1.39        7/23/99  Ian Formanek    Removed splitted panel 
 *       between workspace switch and status line
 *  39   Gandalf   1.38        7/12/99  Ian Formanek    removed registration 
 *       code
 *  38   Gandalf   1.37        7/11/99  David Simonek   window system change...
 *  37   Gandalf   1.36        6/24/99  Jesse Glick     Bugfix: context help for
 *       Main Window.
 *  36   Gandalf   1.35        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  35   Gandalf   1.34        6/10/99  Ian Formanek    Beta3 in title
 *  34   Gandalf   1.33        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  33   Gandalf   1.32        6/4/99   Libor Kramolis  
 *  32   Gandalf   1.31        5/25/99  Jaroslav Tulach Fix #1889
 *  31   Gandalf   1.30        5/15/99  Jaroslav Tulach starts faster if 
 *       netbeans.full.hack=full
 *  30   Gandalf   1.29        5/11/99  Jaroslav Tulach ToolbarPool changed to 
 *       look better in Open API
 *  29   Gandalf   1.28        5/8/99   Ian Formanek    Further cleanup
 *  28   Gandalf   1.27        5/8/99   Ian Formanek    cleaned up 
 *       comments/commented code
 *  27   Gandalf   1.26        5/7/99   Libor Kramolis  
 *  26   Gandalf   1.25        5/2/99   Ian Formanek    Fixed width of the main 
 *       window, default toolbars creation is no more dependent on XML module
 *  25   Gandalf   1.24        4/9/99   Ian Formanek    Removed debug printlns
 *  24   Gandalf   1.23        4/8/99   Ian Formanek    Changed Object.class -> 
 *       getClass ()
 *  23   Gandalf   1.22        4/7/99   Ian Formanek    Handles resizing - the 
 *       height is always fixed to the preferred height
 *  22   Gandalf   1.21        4/5/99   Ian Formanek    Changed toolbars init to
 *       compile
 *  21   Gandalf   1.20        4/2/99   Libor Kramolis  
 *  20   Gandalf   1.19        4/2/99   Libor Kramolis  
 *  19   Gandalf   1.18        3/31/99  David Simonek   status line & workspace 
 *       switcher added
 *  18   Gandalf   1.17        3/27/99  Ian Formanek    Fixed creation of 
 *       default toolbar
 *  17   Gandalf   1.16        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  16   Gandalf   1.15        3/16/99  Jaroslav Tulach Help commented out
 *  15   Gandalf   1.14        3/16/99  Jaroslav Tulach JavaHelp like help 
 *       system
 *  14   Gandalf   1.13        3/6/99   David Simonek   
 *  13   Gandalf   1.12        2/26/99  David Simonek   
 *  12   Gandalf   1.11        2/19/99  David Simonek   menu related changes...
 *  11   Gandalf   1.10        2/17/99  Ian Formanek    
 *  10   Gandalf   1.9         2/17/99  Ian Formanek    Updated for new toolbar 
 *       location
 *  9    Gandalf   1.8         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  8    Gandalf   1.7         2/11/99  Jaroslav Tulach StatusLine separated 
 *       from DesktopSwitcher
 *  7    Gandalf   1.6         2/11/99  Jaroslav Tulach SystemAction is 
 *       javax.swing.Action
 *  6    Gandalf   1.5         1/25/99  David Peroutka  
 *  5    Gandalf   1.4         1/20/99  David Peroutka  
 *  4    Gandalf   1.3         1/6/99   Jaroslav Tulach 
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
