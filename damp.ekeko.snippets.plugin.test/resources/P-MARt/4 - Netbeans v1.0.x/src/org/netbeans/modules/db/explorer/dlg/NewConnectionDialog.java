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
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.netbeans.lib.ddl.*;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.netbeans.modules.db.explorer.*;

/**
* xxx
*
* @author Slavek Psenicka
*/
public class NewConnectionDialog
{
    DatabaseConnection con;
    boolean result = false;
    Dialog dialog = null;
    JComboBox drvfield;
    JTextField dbfield, userfield;
    JPasswordField pwdfield;
    JCheckBox rememberbox;

    public NewConnectionDialog(Vector drivervec, String driver, String database, String loginname)
    {
        this(drivervec, new DatabaseConnection(driver, database, loginname, null));
    }

    public NewConnectionDialog(Vector drivervec, DatabaseConnection xcon)
    {
        con = (DatabaseConnection)xcon;
        try {
            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constr = new GridBagConstraints ();
            pane.setLayout (layout);
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");

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
            drvfield = new JComboBox(drivervec);
            drvfield.setEditable(false);
            drvfield.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               JComboBox combo = (JComboBox)e.getSource();
                                               Object drv = combo.getSelectedItem();
                                               String dbprefix = null;
                                               if (drv != null && drv instanceof DatabaseDriver) dbprefix = ((DatabaseDriver)drv).getDatabasePrefix();
                                               if (dbprefix!=null) dbfield.setText(dbprefix);
                                           }
                                       });

            layout.setConstraints(drvfield, constr);
            pane.add(drvfield);

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

            // Setup driver if found

            String drv = xcon.getDriver();
            String drvname = xcon.getDriverName();
            if (drv != null && drvname != null) {

                for (int i = 0; i < drivervec.size(); i++) {
                    DatabaseDriver dbdrv = (DatabaseDriver)drivervec.elementAt(i);
                    if (dbdrv.getURL().equals(drv) && dbdrv.getName().equals(drvname)) {
                        drvfield.setSelectedIndex(i);
                    }
                }
            }

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

            // Remember password checkbox

            rememberbox = new JCheckBox(bundle.getString("NewConnectionRememberPassword"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 1;
            constr.gridy = 4;
            layout.setConstraints(rememberbox, constr);
            pane.add(rememberbox);

            ActionListener listener = new ActionListener() {
                                          public void actionPerformed(ActionEvent event) {
                                              if (event.getSource() == DialogDescriptor.OK_OPTION) {
                                                  result = true;
                                                  try {
                                                      String drvval, pwd;
                                                      int idx = drvfield.getSelectedIndex();
                                                      if (idx != -1) {
                                                          drvval = ((DatabaseDriver)drvfield.getItemAt(idx)).getURL();
                                                      } else drvval = (String)drvfield.getSelectedItem();
                                                      con.setDriver(drvval);
                                                      con.setDatabase(dbfield.getText());
                                                      con.setUser(userfield.getText());
                                                      String tmppwd = new String(pwdfield.getPassword());
                                                      if (tmppwd.length() > 0) pwd = tmppwd;
                                                      else pwd = null;
                                                      con.setPassword(pwd);
                                                      con.setRememberPassword(rememberbox.isSelected());
                                                  } catch (Exception e) {
                                                      e.printStackTrace();
                                                  }
                                              } else result = false;

                                              dialog.setVisible(false);
                                              dialog.dispose();
                                          }
                                      };

            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("NewConnectionDialogTitle"), true, listener);
            dialog = TopManager.getDefault().createDialog(descriptor);
            dialog.setResizable(false);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    public void setSelectedDriver(DatabaseDriver anObject)
    {
        drvfield.setSelectedItem(anObject);
    }

    public boolean run() throws ClassNotFoundException
    {
        if (dialog != null) dialog.setVisible(true);
        if (result) {
            ClassLoader syscl = TopManager.getDefault().currentClassLoader();
            syscl.loadClass(con.getDriver());
        }

        return result;
    }

    public DBConnection getConnection()
    {
        return con;
    }

    public String getDriver()
    {
        return con.getDriver();
    }

    public String getDatabase()
    {
        return con.getDatabase();
    }

    public String getUser()
    {
        return con.getUser();
    }

    public String getPassword()
    {
        return con.getPassword();
    }

    public boolean rememberPassword()
    {
        return con.rememberPassword();
    }
}
/*
 * <<Log>>
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/8/99   Slavek Psenicka adaptor changes
 *  5    Gandalf   1.4         7/21/99  Slavek Psenicka 
 *  4    Gandalf   1.3         6/15/99  Slavek Psenicka debug prints
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
