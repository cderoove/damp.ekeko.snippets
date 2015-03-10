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
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

/**
* xxx
*
* @author Slavek Psenicka
*/
public class ConnectDialog
{
    boolean result = false, remember = true;
    Dialog dialog = null;
    String user = null, pwd = null;
    JTextField userfield, pwdfield;
    JCheckBox rememberbox = null;

    public ConnectDialog(String loginname)
    {
        try {
            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");

            // Username field

            label = new JLabel(bundle.getString("ConnectDialogUserName"));
            con.anchor = GridBagConstraints.WEST;
            con.weightx = 0.0;
            con.fill = GridBagConstraints.NONE;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 2;
            layout.setConstraints(label, con);
            pane.add(label);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 2;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            userfield = new JTextField(35);
            userfield.setText(loginname);
            layout.setConstraints(userfield, con);
            pane.add(userfield);

            // Password field

            label = new JLabel(bundle.getString("ConnectDialogPassword"));
            con.anchor = GridBagConstraints.WEST;
            con.weightx = 0.0;
            con.fill = GridBagConstraints.NONE;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 3;
            layout.setConstraints(label, con);
            pane.add(label);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 3;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            pwdfield = new JPasswordField(35);
            layout.setConstraints(pwdfield, con);
            pane.add(pwdfield);

            // Remember password checkbox

            rememberbox = new JCheckBox(bundle.getString("ConnectDialogRememberPassword"));
            con.anchor = GridBagConstraints.WEST;
            con.weightx = 0.0;
            con.fill = GridBagConstraints.NONE;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 1;
            con.gridy = 4;
            layout.setConstraints(rememberbox, con);
            pane.add(rememberbox);

            ActionListener listener = new ActionListener() {
                                          public void actionPerformed(ActionEvent event) {
                                              boolean disres = true;
                                              if (event.getSource() == DialogDescriptor.OK_OPTION) {
                                                  result = true;
                                                  user = userfield.getText();
                                                  pwd = pwdfield.getText();
                                                  remember = rememberbox.isSelected();
                                              } else result = false;
                                              dialog.setVisible(false);
                                              dialog.dispose();
                                          }
                                      };

            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("ConnectDialogTitle"), true, listener);
            dialog = TopManager.getDefault().createDialog(descriptor);
            dialog.setResizable(false);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
    }

    public boolean run()
    {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    public String getUser()
    {
        return user;
    }

    public String getPassword()
    {
        return pwd;
    }

    public boolean rememberPassword()
    {
        return remember;
    }
}
/*
 * <<Log>>
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/8/99   Slavek Psenicka 
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
 * $
 */
