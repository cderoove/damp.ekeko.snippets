/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.PrintWriter;
import java.util.*;
import java.sql.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.openide.*;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.explorer.*;

/**
* xxx
*
* @author Slavek Psenicka
*/
public class TestDriverDialog
{
    DatabaseConnection con;
    boolean result = false;
    Dialog dialog = null;
    JTextField dbfield, userfield;
    JPasswordField pwdfield;
    static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");

    public TestDriverDialog(String driver, String database, String loginname)
    {
        this(new DatabaseConnection(driver, database, loginname, null));
    }

    public TestDriverDialog(DatabaseConnection xcon)
    {
        con = (DatabaseConnection)xcon;
        try {
            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constr = new GridBagConstraints ();
            pane.setLayout (layout);

            // Driver field

            label = new JLabel(bundle.getString("NewConnectionDriverURL"));
            constr.anchor = GridBagConstraints.WEST;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 0;
            layout.setConstraints(label, constr);
            pane.add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.gridx = 1;
            constr.gridy = 0;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            label = new JLabel(con.getDriver());
            layout.setConstraints(label, constr);
            pane.add(label);

            // Database field

            label = new JLabel(bundle.getString("NewConnectionDatabaseURL"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 1;
            layout.setConstraints(label, constr);
            pane.add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.gridx = 1;
            constr.gridy = 1;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            dbfield = new JTextField(35);
            dbfield.setText(xcon.getDatabase());
            layout.setConstraints(dbfield, constr);
            pane.add(dbfield);

            // Username field

            label = new JLabel(bundle.getString("NewConnectionUserName"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 2;
            layout.setConstraints(label, constr);
            pane.add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.gridx = 1;
            constr.gridy = 2;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            userfield = new JTextField(35);
            userfield.setText(xcon.getUser());
            layout.setConstraints(userfield, constr);
            pane.add(userfield);

            // Password field

            label = new JLabel(bundle.getString("NewConnectionPassword"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 3;
            layout.setConstraints(label, constr);
            pane.add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.gridx = 1;
            constr.gridy = 3;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            pwdfield = new JPasswordField(35);
            layout.setConstraints(pwdfield, constr);
            pane.add(pwdfield);

            ActionListener listener = new ActionListener() {
                                          public void actionPerformed(ActionEvent event) {
                                              if (event.getSource() == DialogDescriptor.OK_OPTION) {
                                                  ok();
                                              } else cancel();
                                              dialog.setVisible(false);
                                              dialog.dispose();
                                          }
                                      };

            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("TestDriverDialogTitle"), true, listener);
            dialog = TopManager.getDefault().createDialog(descriptor);
            dialog.setResizable(false);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    protected void ok()
    {
        try {
            String drvval = con.getDriver(), pwd;
            ClassLoader syscl = TopManager.getDefault().currentClassLoader();
            Class.forName(con.getDriver());
            syscl.loadClass(con.getDriver());
            con.setDriver(drvval);
            con.setDatabase(dbfield.getText());
            con.setUser(userfield.getText());
            String tmppwd = new String(pwdfield.getPassword());
            if (tmppwd.length() > 0) pwd = tmppwd;
            else pwd = null;
            con.setPassword(pwd);
            result = true;
        } catch (ClassNotFoundException ex) {
            result = false;
            //			ex.printStackTrace();
            TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("TestDriverNoDriverClass"), NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    protected void cancel()
    {
        result = false;
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(con.getDatabase(), con.getUser(), con.getPassword());
    }

    public boolean run()
    {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }
}
/*
 * <<Log>>
 *  4    Gandalf-post-FCS1.2.1.0     4/10/00  Radko Najman    
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         9/8/99   Slavek Psenicka adaptor changes
 *  1    Gandalf   1.0         9/2/99   Slavek Psenicka 
 * $
 */
