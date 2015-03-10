/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jini;

import java.awt.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;

import net.jini.admin.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.enum.*;
import org.openide.util.actions.*;


/**
 * Action that presents plugged in admins.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class AdminAction extends CookieAction implements PropertyChangeListener {

    private Dialog dlg;

    /**
    * The nodes must hold this data holder cookie.
    */
    public Class[] cookieClasses() {
        return new Class[] { AdminCookie.class };
    }

    public int mode() {
        return MODE_ALL;
    }

    /**
    * Enable only if some admins plugged in. 
    */
    public boolean isEnabled() {
        if (super.isEnabled())
            return JiniModule.admins().length > 0;
        return false;
    }

    /** Test whether owns cookie and exist any union admin. */
    /* Late enable check must be used
      public boolean enabled(Node[] nodes) {
        if (super.enabled(nodes)) {
          readyAdmins = createAdmins(nodes);
          if (readyAdmins.length > 0) return true
        } 
        
        return false;    
      }
    */

    /** provide initialized admins, just to show them */
    private Admin[] createAdmins(Node[] nodes) {
        AdminClass[] admins = JiniModule.admins();

        LinkedList list = new LinkedList();
        for (int i = 0; i< admins.length; i++ ) {
            Administrable[] objs = getAdministables(nodes);

            Admin admin = admins[i].newInstance();
            admin.addAdministrables(objs);
            if (admin.enabled()) {
                list.add(admin);
            }
        }

        Admin[] array = new Admin[list.size()];
        list.toArray(array);
        return array;
    }


    /**
    */
    private Administrable[] getAdministables(Node[] nodes) {

        LinkedList list = new LinkedList();
        for ( int i = 0; i<nodes.length; i++) {
            AdminCookie cake = (AdminCookie) nodes[i].getCookie(AdminCookie.class);
            Administrable obj = cake.getAdmin();
            //      System.err.println("getAdministables: " + obj);
            if (obj != null)
                list.add(obj);
        }

        Administrable[] array = new Administrable[list.size()];
        list.toArray(array);
        return array;
    }

    /**
    */
    private DialogDescriptor createUI(Enumeration admins) {

        JTabbedPane tabs = new JTabbedPane();
        JComponent comp = null;

        while (admins.hasMoreElements()) {
            Admin admin = (Admin) admins.nextElement();
            JComponent ui;
            if (admin.canBatch()) {
                ui = admin.getUI(Admin.RW);
            } else {
                ui = admin.getUI(Admin.WO);
            }

            if (ui != null) {
                comp = ui;
                tabs.add(ui);
            }
        }

        if (tabs.getComponentCount() > 0) {
            if (tabs.getComponentCount() > 1)
                comp = tabs;

            DialogDescriptor panel = new DialogDescriptor(
                                         comp, "Manage",
                                         false, null
                                     );
            panel.setOptions( new Object[] {DialogDescriptor.OK_OPTION} );
            return panel;
        }

        return null;
    }

    private void register(Enumeration admins) {
        while(admins.hasMoreElements()) {
            Admin admin = (Admin) admins.nextElement();
            admin.addPropertyChangeListener(this);
        }
    }

    /** Do remote call may spend a lot of time
    */
    protected void performAction(Node[] nodes) {

        if (nodes == null) return;

        Admin[] admins = createAdmins(nodes);
        DialogDescriptor panel = createUI(new ArrayEnumeration(admins));
        if (panel == null) return;
        register( new ArrayEnumeration(admins) );
        dlg = TopManager.getDefault().createDialog(panel);
        dlg.setVisible(true);

    }

    public String getName() {
        return "Manage...";
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    /** Listen for CLOSE_ALL */
    public void propertyChange(final java.beans.PropertyChangeEvent e) {

        if (e.getPropertyName().equals(Admin.EVENT_CLOSE_ALL)) {
            dlg.setVisible(false);
            dlg.dispose();
        }
    }

}


/*
* <<Log>>
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

