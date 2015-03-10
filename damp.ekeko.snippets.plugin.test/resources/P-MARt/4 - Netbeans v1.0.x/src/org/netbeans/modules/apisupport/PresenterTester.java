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

package org.netbeans.modules.apisupport;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.Presenter;
import org.openide.util.io.FoldingIOException;

public class PresenterTester extends Tester {

    private static final long serialVersionUID =-7652741600072204927L;
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.testers.Presenter");
    }

    protected void checkClass (Class clazz) throws IOException {
        try {
            Object action = clazz.newInstance ();
            if (! (action instanceof Presenter.Menu) && ! (action instanceof Presenter.Toolbar))
                throw new IOException ("Must implement menu or toolbar presenter (or both)!");
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            throw new FoldingIOException (t);
        }
    }

    protected void executeClass (Class clazz, String[] args) {
        try {
            Object action = clazz.newInstance ();
            System.err.println ("Will show " + clazz.getName () + " ...");
            JMenuItem menuitem = null;
            if (action instanceof Presenter.Menu)
                menuitem = ((Presenter.Menu) action).getMenuPresenter ();
            Component toolbar = null;
            if (action instanceof Presenter.Toolbar)
                toolbar = ((Presenter.Toolbar) action).getToolbarPresenter ();
            JFrame frame = new JFrame ("Testing " + clazz.getName ());
            if (menuitem != null) {
                JMenu menu = new JMenu ("Menu...");
                menu.add (menuitem);
                JMenuBar menubar = new JMenuBar ();
                menubar.add (menu);
                frame.setJMenuBar (menubar);
            }
            if (toolbar != null) {
                frame.getContentPane ().add (toolbar, BorderLayout.CENTER);
            }
            final Object pause = new Object ();
            frame.addWindowListener (new WindowAdapter () {
                                         public void windowClosing (WindowEvent ev) {
                                             synchronized (pause) {
                                                 pause.notify ();
                                             }
                                         }
                                     });
            frame.pack ();
            frame.show ();
            synchronized (pause) {
                pause.wait ();
            }
            System.err.println ("Presenter closed.");
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            t.printStackTrace ();
        }
    }

}

/*
 * Log
 *  10   Gandalf-post-FCS1.8.1.0     3/28/00  Jesse Glick     SVUIDs.
 *  9    Gandalf   1.8         1/26/00  Jesse Glick     Executor display names 
 *       can just be taken from bean descriptor.
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  6    Gandalf   1.5         10/7/99  Jesse Glick     Service name changes.
 *  5    Gandalf   1.4         10/6/99  Jesse Glick     Added table of contents,
 *       anchored context help.
 *  4    Gandalf   1.3         10/5/99  Jesse Glick     Sundry API changes 
 *       affecting me.
 *  3    Gandalf   1.2         9/30/99  Jesse Glick     Package rename and misc.
 *  2    Gandalf   1.1         9/20/99  Jesse Glick     Fixed output from 
 *       testers; now has correct classloader.
 *  1    Gandalf   1.0         9/12/99  Jesse Glick     
 * $
 */
