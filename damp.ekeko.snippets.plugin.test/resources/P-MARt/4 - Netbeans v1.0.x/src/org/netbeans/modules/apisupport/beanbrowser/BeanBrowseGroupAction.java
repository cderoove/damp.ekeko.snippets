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

package org.netbeans.modules.apisupport.beanbrowser;

import java.awt.event.ActionEvent;
import java.awt.Component;
import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

public class BeanBrowseGroupAction extends SystemAction implements Presenter.Menu, Presenter.Popup, Presenter.Toolbar {

    private static final long serialVersionUID =2334296231179086973L;
    public void actionPerformed (ActionEvent ev) {
        // do nothing; should not be called
    }

    public String getName () {
        return "Bean Browse...";
    }

    protected String iconResource () {
        return "/org/netbeans/modules/apisupport/resources/BeanBrowserIcon.gif";
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
    }

    private static final SystemAction[] grouped = new SystemAction[] {
                SystemAction.get (BeanBrowseAction.class),
                SystemAction.get (BeanBrowseMasterAction.class),
                SystemAction.get (BeanBrowseWindowAction.class),
            };

    public JMenuItem getMenuPresenter () {
        JMenu menu = new JMenu (getName ());
        menu.setIcon (getIcon ());
        for (int i = 0; i < grouped.length; i++) {
            SystemAction action = grouped[i];
            if (action == null) {
                menu.addSeparator ();
            } else if (action instanceof Presenter.Menu) {
                menu.add (((Presenter.Menu) action).getMenuPresenter ());
            }
        }
        return menu;
    }

    public JMenuItem getPopupPresenter () {
        JMenu menu = new JMenu (getName ());
        for (int i = 0; i < grouped.length; i++) {
            SystemAction action = grouped[i];
            if (action == null) {
                menu.addSeparator ();
            } else if (action instanceof Presenter.Popup) {
                menu.add (((Presenter.Popup) action).getPopupPresenter ());
            }
        }
        return menu;
    }

    public Component getToolbarPresenter () {
        JToolBar toolbar = new JToolBar (/* In JDK 1.3 you may add: getName () */);
        for (int i = 0; i < grouped.length; i++) {
            SystemAction action = grouped[i];
            if (action == null) {
                toolbar.addSeparator ();
            } else if (action instanceof Presenter.Toolbar) {
                toolbar.add (((Presenter.Toolbar) action).getToolbarPresenter ());
            }
        }
        return toolbar;
    }

}

/*
 * Log
 *  2    Gandalf-post-FCS1.0.1.0     3/28/00  Jesse Glick     SVUIDs.
 *  1    Gandalf   1.0         12/23/99 Jesse Glick     
 * $
 */
