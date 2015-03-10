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

package org.openide.actions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/** Show help for the currently selected workspace element.
*
* @see HelpCtx#findHelp
* @author Jesse Glick
*/
public class HelpAction extends SystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4658008202517094416L;

    /** Component the mouse is currently over. */
    private static Component globallySelectedComp;

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("Help");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (HelpAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/showHelp.gif"; // NOI18N
    }

    // This ensures initialize() will be called ASAP, hence that
    // the AWT listener will actually be started quickly and there
    // will already have been interesting mouse-entered events
    // by the time F1 is first pressed. Otherwise only the second
    // F1 actually gets anything other than the main window help.
    static {
        ((HelpAction) SystemAction.get (HelpAction.class)).forceInit ();
    }
    private void forceInit () {
        getProperty ("whatever");
    }

    // Make sure this action works on dialogs, etc.--everywhere.
    protected void initialize () {
        //System.err.println("HelpAction.initialize");
        super.initialize ();
        putProperty ("OpenIDE-Transmodal-Action", Boolean.TRUE); // NOI18N
        // For live-tracking, we need to know what component the mouse is over:
        // Make sure it is not null:
        globallySelectedComp = TopManager.getDefault ().getWindowManager ().getMainWindow ();
        Toolkit.getDefaultToolkit ().addAWTEventListener (new AWTEventListener () {
                    public void eventDispatched (AWTEvent ev) {
                        if ((ev instanceof MouseEvent) && ev.getID () == MouseEvent.MOUSE_ENTERED)
                            globallySelectedComp = ((MouseEvent) ev).getComponent ();
                    }
                }, AWTEvent.MOUSE_EVENT_MASK);
    }

    public void actionPerformed (java.awt.event.ActionEvent ev) {
        HelpCtx help = HelpCtx.findHelp (globallySelectedComp);
        if ("full".equals (System.getProperty ("org.openide.actions.HelpAction.DEBUG")))
            System.err.println (help + " from " + globallySelectedComp);
        else if (Boolean.getBoolean ("org.openide.actions.HelpAction.DEBUG")) // NOI18N
            System.err.println (help);
        org.openide.TopManager.getDefault ().setStatusText (ActionConstants.BUNDLE.getString("CTL_OpeningHelp"));
        org.openide.TopManager.getDefault ().showHelp (help);
        org.openide.TopManager.getDefault ().setStatusText (""); // NOI18N
        // Copied from MainWindow:
        final MenuSelectionManager msm = MenuSelectionManager.defaultManager ();
        final MenuElement[] path = msm.getSelectedPath ();
        // post request that should after half of second clear the selected menu
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              SwingUtilities.invokeLater (new Runnable () {
                                                                              public void run () {
                                                                                  MenuElement[] newPath = msm.getSelectedPath ();
                                                                                  if (newPath.length != path.length) return;
                                                                                  for (int i = 0; i < newPath.length; i++) {
                                                                                      if (newPath[i] != path[i]) return;
                                                                                  }
                                                                                  msm.clearSelectedPath ();
                                                                              }
                                                                          });
                                          }
                                      }, 200);
    }
}

/*
 * Log
 *  21   src-jtulach1.20        2/4/00   Jesse Glick     #5452.
 *  20   src-jtulach1.19        1/12/00  Ian Formanek    NOI18N
 *  19   src-jtulach1.18        1/10/00  Jesse Glick     #5191 (first F1 after 
 *       restart does not work).
 *  18   src-jtulach1.17        11/8/99  Jesse Glick     Context help from 
 *       component under mouse, rather than selected component.
 *  17   src-jtulach1.16        11/5/99  Jesse Glick     Removed dead code, and 
 *       more debugging options.
 *  16   src-jtulach1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   src-jtulach1.14        7/28/99  Jesse Glick     Debugging hook for 
 *       Patrick.
 *  14   src-jtulach1.13        7/16/99  Jesse Glick     Taking advantage of 
 *       ActionEvent passed to it.
 *  13   src-jtulach1.12        7/16/99  Jesse Glick     Better handling of 
 *       menus--correct behavior when submenu is selected, but not any of the 
 *       items on it.
 *  12   src-jtulach1.11        7/16/99  Jesse Glick     Handling help from menu 
 *       items.
 *  11   src-jtulach1.10        7/11/99  David Simonek   window system change...
 *  10   src-jtulach1.9         6/24/99  Jesse Glick     Bugfix: works when only 
 *       Main Window is selected.
 *  9    src-jtulach1.8         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  8    src-jtulach1.7         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  7    src-jtulach1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    src-jtulach1.5         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  5    src-jtulach1.4         5/7/99   Jaroslav Tulach Help.
 *  4    src-jtulach1.3         5/2/99   Ian Formanek    Fixed last change
 *  3    src-jtulach1.2         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  2    src-jtulach1.1         3/26/99  Jesse Glick     [JavaDoc]
 *  1    src-jtulach1.0         3/16/99  Jaroslav Tulach 
 * $
 */
