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

package org.netbeans.modules.objectbrowser;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Window;

import org.openide.TopManager;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.TopManager;

import org.openidex.util.Utilities2;

/**
* Module installation class for Object browser.
*
* @author Jan Jancura
*/
public class ObjectBrowserModule extends org.openide.modules.ModuleInstall {

    static final long serialVersionUID = 3225613414350004303L;

    public static final String  BROWSING_WKS_NAME = "Browsing";
    public static final String  BROWSING_WKS_DISPLAY_NAME = NbBundle.getBundle (
                ObjectBrowserModule.class
            ).getString ("CTL_Browsing_workspace_name");


    // ModuleInstall implementation .........................................................................

    /** Module installed for the first time. */
    public void installed () {
        installActions ();
        installWorkspace ();
    }

    /** Module was uninstalled. */
    public void uninstalled () {
        uninstallActions ();
        uninstallWorkspace ();
    }

    /** Module installed again. */
    public void restored () {
        final java.awt.Window mainWindow =
            TopManager.getDefault ().getWindowManager ().getMainWindow ();
        mainWindow.addWindowListener (
            new java.awt.event.WindowAdapter () {
                public void windowOpened (java.awt.event.WindowEvent ev) {
                    // notify me no more
                    mainWindow.removeWindowListener (this);
                    installWorkspace ();
                }
            }
        );
    }


    // other methods .........................................................................

    private void installActions () {
        try {

            // menu actions ...

            Utilities2.createAction (
                ShowBrowserAction.class,
                DataFolder.create (
                    TopManager.getDefault ().getPlaces ().folders ().menus (),
                    "File" // NOI18N
                ),
                "OpenExplorer", true, false, false, false // NOI18N
            );

            // toolbars actions ...

            Utilities2.createAction (
                ShowBrowserAction.class,
                DataFolder.create (
                    TopManager.getDefault ().getPlaces ().folders ().toolbars (),
                    "System" // NOI18N
                ),
                "OpenExplorer", true, false, false, false // NOI18N
            );

            // install into actions pool

            Utilities2.createAction (
                ShowBrowserAction.class,
                DataFolder.create (
                    TopManager.getDefault ().getPlaces ().folders ().actions (),
                    "View" // NOI18N
                )
            );

        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                e.printStackTrace ();
            }
            // ignore failure to install
        }
    }

    private void uninstallActions () {
        try {

            // menu actions ...

            Utilities2.removeAction (
                ShowBrowserAction.class,
                DataFolder.create (
                    TopManager.getDefault ().getPlaces ().folders ().menus (),
                    "File" // NOI18N
                )
            );

            // toolbars actions ...

            Utilities2.removeAction (
                ShowBrowserAction.class,
                DataFolder.create (
                    TopManager.getDefault ().getPlaces ().folders ().toolbars (),
                    "System" // NOI18N
                )
            );

            // install into actions pool

            Utilities2.removeAction (
                ShowBrowserAction.class,
                DataFolder.create (
                    TopManager.getDefault ().getPlaces ().folders ().actions (),
                    "View" // NOI18N
                )
            );

        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                e.printStackTrace ();
            }
            // ignore failure to uninstall
        }
    }

    /** Install and initialize object browser's workspace. */
    private void installWorkspace () {
        WindowManager wm = TopManager.getDefault ().getWindowManager ();
        Workspace browsing = wm.findWorkspace (BROWSING_WKS_NAME);
        // create the workspace if not found
        if (browsing == null) {
            browsing = wm.createWorkspace (BROWSING_WKS_NAME, BROWSING_WKS_DISPLAY_NAME);
            // add browsing workspace after editing workspace
            Workspace[] curWorkspaces = wm.getWorkspaces();
            Workspace[] newWorkspaces = new Workspace[curWorkspaces.length + 1];
            boolean found = false;
            int y = 0;
            for (int i = 0; i < curWorkspaces.length; i++, y++) {
                if ("Visual".equals(curWorkspaces[i].getName())) { // NOI18N
                    found = true;
                    newWorkspaces[y++] = curWorkspaces[i];
                    newWorkspaces[y] = browsing;
                } else {
                    newWorkspaces[y] = curWorkspaces[i];
                }
            }
            // add to the end if editing was not found
            if (!found) {
                newWorkspaces [y] = browsing;
            }
            wm.setWorkspaces (newWorkspaces);
        } else {
            Mode browserMode = browsing.findMode (
                                   ObjectBrowser.MODE_NAME
                               );
            if (browserMode != null) {
                // mode already inicialized...
                return;
            }
        }

        // install mode and open object browser on browsing workspace
        createOBMode (browsing);
        ((ShowBrowserAction) SystemAction.get (ShowBrowserAction.class)).
        getObjectBrowser ().open (browsing);
    }

    /** Create and place browser mode */
    static Mode createOBMode (Workspace workspace) {

        // create browser mode and place it
        Mode browserMode = workspace.createMode (
                               ObjectBrowser.MODE_NAME,
                               NbBundle.getBundle (ObjectBrowserModule.class).
                               getString ("CTL_Browser_window_name"),
                               ObjectBrowserModule.class.getResource (
                                   "/org/netbeans/modules/objectbrowser/resources/browser.gif" // NOI18N
                               )
                           );
        // compute the bounds for browser
        Rectangle workingSpace = workspace.getBounds();
        Rectangle bounds = new Rectangle (
                               workingSpace.x, workingSpace.y,
                               3 * workingSpace.width / 4,
                               workingSpace.height / 2
                           );
        browserMode.setBounds (bounds);
        return browserMode;
    }

    /** Remove object browser's workspace, as it is no longer needed. */
    void uninstallWorkspace () {
        TopComponent t = (TopComponent) ((ShowBrowserAction) SystemAction.get (
                                             ShowBrowserAction.class)
                                        ).getObjectBrowser ();
        t.setCloseOperation (TopComponent.CLOSE_EACH);
        t.close (null);

        WindowManager wm = TopManager.getDefault ().getWindowManager ();
        Workspace browsing = wm.findWorkspace (BROWSING_WKS_NAME);
        if (browsing == null) return;

        // remove browsing workspace if found
        Workspace[] curWorkspaces = wm.getWorkspaces ();
        Workspace[] newWorkspaces = new Workspace [curWorkspaces.length - 1];
        int i, ii = curWorkspaces.length, y = 0;
        for (i = 0; i < ii; i++)
            if (!BROWSING_WKS_NAME.equals (curWorkspaces [i].getName ()))
                newWorkspaces [y ++] = curWorkspaces [i];
        if (y == newWorkspaces.length)
            wm.setWorkspaces (newWorkspaces);
    }
}

/*
* Log
*  20   Gandalf-post-FCS1.18.1.0    3/13/00  David Simonek   process of creating 
*       module's own workspace modified to be functional even in japanese and 
*       other localised versions
*  19   Gandalf   1.18        1/16/00  Ian Formanek    Tweaked comments
*  18   Gandalf   1.17        1/15/00  Jesse Glick     Nicer actions pool 
*       installation.
*  17   Gandalf   1.16        1/13/00  Radko Najman    I18N
*  16   Gandalf   1.15        1/5/00   David Simonek   computing of size of 
*       browser's window modified
*  15   Gandalf   1.14        12/20/99 Jan Jancura     Bug 5032
*  14   Gandalf   1.13        12/15/99 Jan Jancura     Bug 4593 - uninstall OB +
*       utils modul
*  13   Gandalf   1.12        11/27/99 Patrik Knakal   
*  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems copyright in file comment
*  11   Gandalf   1.10        10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
*        changed to class + some methods added
*  10   Gandalf   1.9         8/18/99  Jan Jancura     Localization
*  9    Gandalf   1.8         7/29/99  David Simonek   changes concerning window
*       system
*  8    Gandalf   1.7         7/27/99  Jan Jancura     
*  7    Gandalf   1.6         7/24/99  Ian Formanek    Fixed installation of 
*       actions
*  6    Gandalf   1.5         7/22/99  David Simonek   further workspace init 
*  5    Gandalf   1.4         7/21/99  David Simonek   
*  4    Gandalf   1.3         7/21/99  David Simonek   installing browsing 
*       workspace and mode..
*  3    Gandalf   1.2         7/21/99  Jan Jancura     
*  2    Gandalf   1.1         7/16/99  Jan Jancura     
*  1    Gandalf   1.0         7/14/99  Jan Jancura     
* $
*/
