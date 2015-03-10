/**
 * UserSettingsDialog - Dialog for maintaining CodeGen Settings
 *
 * Copyright (c) 2002
 *      Marty Phelan, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package com.taursys.tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import com.taursys.debug.Debug;

/**
 * UserSettingsDialog is Dialog for maintaining CodeGen Settings
 * @author Marty Phelan
 * @version 1.0
 */
public class UserSettingsDialog extends javax.swing.JDialog {
  JPanel contentPanel = new JPanel();
  JPanel buttonPanel = new JPanel();
  JButton saveButton = new JButton();
  JButton cancelButton = new JButton();
  JTextField author = new JTextField();
  JTextField copyright = new JTextField();
  JTextField defaultProjectPath = new JTextField();
  JLabel authorLabel = new JLabel();
  JLabel copyrightLabel = new JLabel();
  JLabel defaultProjectPathLabel = new JLabel();
  JTextField templatesPath = new JTextField();
  JLabel templatesPathLabel = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  Settings settings;

  /**
   * Constructs a new UserSettingsDialog
   */
  public UserSettingsDialog(Frame parent, Settings settings) {
    super(parent, "Mapper Code Generator Settings", true);
    this.settings = settings;
    try {
      jbInit();
      author.setText(settings.getProperty(UserSettings.AUTHOR));
      copyright.setText(settings.getProperty(UserSettings.COPYRIGHT));
      defaultProjectPath.setText(settings.getProperty(UserSettings.DEFAULT_PROJECT_PATH));
      templatesPath.setText(settings.getProperty(UserSettings.TEMPLATES_PATH));
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    saveButton.setText("Save");
    saveButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveButton_actionPerformed(e);
      }
    });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    contentPanel.setLayout(gridBagLayout1);
    authorLabel.setText("Author:");
    copyrightLabel.setText("Copyright:");
    defaultProjectPathLabel.setText("Default project path:");

    templatesPathLabel.setText("Templates path:");
    this.setSize(new Dimension(450, 300));
    this.getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.add(author, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(35, 8, 0, 70), 159, 0));
    contentPanel.add(defaultProjectPath, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 8, 0, 70), 159, 0));
    contentPanel.add(authorLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(35, 113, 0, 0), 0, 0));
    contentPanel.add(copyrightLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 94, 0, 0), 0, 0));
    contentPanel.add(templatesPath, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(11, 8, 123, 70), 159, 0));
    contentPanel.add(templatesPathLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 59, 123, 0), 0, 0));
    contentPanel.add(copyright, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 8, 0, 70), 159, 0));
    contentPanel.add(defaultProjectPathLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 29, 0, 6), 0, 0));
    this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    buttonPanel.add(saveButton, null);
    buttonPanel.add(cancelButton, null);
  }

  void cancelButton_actionPerformed(ActionEvent e) {
    dispose();
  }

  void saveButton_actionPerformed(ActionEvent e) {
    settings.setProperty(UserSettings.AUTHOR, author.getText());
    settings.setProperty(UserSettings.COPYRIGHT, copyright.getText());
    settings.setProperty(UserSettings.DEFAULT_PROJECT_PATH,
        defaultProjectPath.getText());
    settings.setProperty(UserSettings.TEMPLATES_PATH, templatesPath.getText());
    try {
      settings.saveSettings();
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "Problem during saving settings: "
          + ex.getMessage(), "Mapper CodeGen Error", JOptionPane.ERROR_MESSAGE);
      Debug.error("Problem during saving settings", ex);
    }
    dispose();
  }
}
