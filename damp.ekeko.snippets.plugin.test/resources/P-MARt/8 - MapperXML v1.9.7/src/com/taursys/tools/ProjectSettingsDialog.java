/**
 * ProjectSettingsDialog - Sets the properties for the current project
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
 * ProjectSettingsDialog is ...
 * @author Marty Phelan
 * @version 1.0
 */
public class ProjectSettingsDialog extends JDialog {
  JPanel contentPanel = new JPanel();
  JPanel buttonPanel = new JPanel();
  JButton saveButton = new JButton();
  JButton cancelButton = new JButton();
  JTextField sourcePath = new JTextField();
  JLabel sourcePathLabel = new JLabel();
  Settings settings = null;
  JTextField projectPath = new JTextField();
  JLabel jLabel1 = new JLabel();
  private int exitState = JOptionPane.CANCEL_OPTION;
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * Constructs a new ProjectSettingsDialog
   */
  public ProjectSettingsDialog(Frame parent, Settings settings) {
    super(parent, "Mapper Code Generator Project Settings", true);
    this.settings = settings;
    try {
      jbInit();
      sourcePath.setText(settings.getProperty(ProjectSettings.SOURCE_PATH));
      projectPath.setText(settings.getProperty(ProjectSettings.PROJECT_PATH));
      projectPath.setInputVerifier(new InputVerifier() {
        public boolean verify(JComponent input) {
          if(sourcePath.getText().length() == 0)
            sourcePath.setText(projectPath.getText() + "/src");
          return true;
        }
      });
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
    sourcePathLabel.setText("Source path:");

    this.setSize(new Dimension(450, 300));
    jLabel1.setText("Project path:");
    this.getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.add(projectPath, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(42, 9, 0, 70), 209, 0));
    contentPanel.add(sourcePath, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(11, 8, 174, 70), 209, 0));
    contentPanel.add(sourcePathLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 80, 174, 0), 0, 0));
    contentPanel.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(42, 80, 0, 0), 0, 0));
    this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    buttonPanel.add(saveButton, null);
    buttonPanel.add(cancelButton, null);
  }

  public int showDialog() {
    show();
    return exitState;
  }

  void cancelButton_actionPerformed(ActionEvent e) {
    exitState = JOptionPane.CANCEL_OPTION;
    dispose();
  }

  void saveButton_actionPerformed(ActionEvent e) {
    settings.setProperty(ProjectSettings.SOURCE_PATH, sourcePath.getText());
    settings.setProperty(ProjectSettings.PROJECT_PATH, projectPath.getText());
    try {
      settings.saveSettings();
      exitState = JOptionPane.OK_OPTION;
      dispose();
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "Problem during saving settings: "
          + ex.getMessage(), "Mapper CodeGen Error", JOptionPane.ERROR_MESSAGE);
      Debug.error("Problem during saving settings", ex);
    }
  }
}
