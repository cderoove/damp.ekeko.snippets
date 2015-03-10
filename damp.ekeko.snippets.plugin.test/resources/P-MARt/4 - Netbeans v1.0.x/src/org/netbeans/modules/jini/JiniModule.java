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
import java.awt.event.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import net.jini.lookup.*;

import org.openide.*;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.*;

import org.netbeans.modules.jini.admins.*;

/**
 * Jini module installer and registry of admins.
 *
 * @author Martin Ryzl, Petr Kuzel
 */
public class JiniModule extends ModuleInstall {

    private final static long serialVersionUID = 1;

    private Dialog dialog;

    /** True if JiniEnabled. (for example missing library ...)
    * It is ugly, but without proper manifest in jini-core.jar and jini-ext.jar ...
    */
    private static boolean enabled = true;

    /**
     * @associates AdminClass 
     */
    private static HashSet admins = new HashSet();


    // test occurence of jini 1.1alpha
    static {
        try {
            Class clazz = Class.forName("net.jini.core.lookup.ServiceRegistrar"); //jini-core.jar 1.0
            clazz = Class.forName("net.jini.lookup.ServiceItemFilter");  //jini-ext.jar 1.1
            clazz = Class.forName("com.sun.jini.admin.DestroyAdmin"); //sun-util.jar 1.0
        } catch (ClassNotFoundException ex) {
            enabled = false;
        }
    }

    static boolean isEnabled() {
        return enabled;
    }

    public void installed() {
        if (enabled) {
            restored();
        } else {
            final JButton ok = new JButton(Util.getString("PROP_OK")), url = new JButton(Util.getString("PROP_URL"));
            JOptionPane pane = new JOptionPane(Util.getString("MSG_MissingJini"), JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
            pane.setOptions(new Object[] {});
            DialogDescriptor desc = new DialogDescriptor(pane, Util.getString("MSG_MissingJiniTitle"),  false, DialogDescriptor.DEFAULT_OPTION, DialogDescriptor.OK_OPTION,
                                    new ActionListener() {
                                        public void actionPerformed(ActionEvent ev) {
                                            dialog.setVisible(false);
                                            dialog.dispose();
                                            dialog = null;
                                            if (ev.getSource() == url) {
                                                // display www browser
                                                try {
                                                    TopManager.getDefault().showUrl(new URL(Util.getString("PROP_JINI_URL")));
                                                } catch (MalformedURLException ex) {
                                                    // Bad URL means no browsing ...
                                                }
                                            }
                                        }
                                    }
                                                        );
            desc.setOptions(new Object[] {ok, url});
            dialog = TopManager.getDefault().createDialog(desc);
            dialog.show();
        }

        copyTemplates();
    }

    public synchronized void restored() {

        // register admins
        addAdmin(new AdminClass(DestroyAdminModel.class));

    }


    private void copyTemplates () {
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
                getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/jini/toinstall/templates.jar") //NOI18N
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }


    //
    // --  admins registry --
    //

    public static synchronized void addAdmin(AdminClass admin) {
        admins.add(admin);
    }

    public static synchronized void removeAdmin(AdminClass admin) {
        admins.remove(admin);
    }

    public static synchronized AdminClass[] admins() {
        AdminClass[] ret = new AdminClass[admins.size()];
        new Vector(admins).toArray(ret);
        return ret;
    }

}


/*
* <<Log>>
*  12   Gandalf   1.11        2/3/00   Petr Kuzel      Be smart and documented
*  11   Gandalf   1.10        2/2/00   Petr Kuzel      Jini module upon 1.1alpha
*  10   Gandalf   1.9         1/19/00  Petr Kuzel      Templates instalation 
*       added.
*  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  8    Gandalf   1.7         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
*        changed to class + some methods added
*  7    Gandalf   1.6         8/19/99  Martin Ryzl     
*  6    Gandalf   1.5         8/12/99  Martin Ryzl     Better dialog when 
*       required files are missing ... fixed BUG#3218
*  5    Gandalf   1.4         8/3/99   Martin Ryzl     
*  4    Gandalf   1.3         7/30/99  Martin Ryzl     group selection dialog
*  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         6/4/99   Martin Ryzl     jini v2
*  1    Gandalf   1.0         6/2/99   Martin Ryzl     
* $ 
*/ 

