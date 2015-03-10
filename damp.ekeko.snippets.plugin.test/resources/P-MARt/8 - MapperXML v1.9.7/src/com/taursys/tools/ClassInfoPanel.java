/**
 * ClassInfoPanel - a wizard panel to gather basic class information
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
import javax.swing.border.*;
import org.apache.velocity.VelocityContext;

/**
 * ClassWizardPanel is a wizard panel to gather basic class information
 * @author Marty Phelan
 * @version 1.0
 */
public class ClassInfoPanel extends WizardPanel {
  JPanel jPanel7 = new JPanel();
  JLabel jLabel1 = new JLabel();
  JTextField jtfClassName = new JTextField();
  JLabel jLabel2 = new JLabel();
  Border border1;
  TitledBorder titledBorder1;
  JButton jbSelectPackage = new JButton();
  JTextField jtfPackageName = new JTextField();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  // Non GUI instance variables
//  private VelocityContext context;
  public static final String CLASS_NAME = "className";
  public static final String PACKAGE_NAME = "packageName";

  /**
   * Constructs a new ClassWizardPanel
   */
  public ClassInfoPanel() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(Color.white,1);
    titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(Color.white,1),"Class Information:");
    jtfPackageName.setPreferredSize(new Dimension(350, 22));
    jtfPackageName.setToolTipText("Package name");
    jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel1.setText("Package:");
    jtfClassName.setPreferredSize(new Dimension(140, 22));
    jtfClassName.setToolTipText("Class name");
    jLabel2.setPreferredSize(new Dimension(113, 18));
    jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel2.setText("Class name:");
    jPanel7.setLayout(gridBagLayout1);
    jPanel7.setBorder(titledBorder1);
    jPanel7.setMinimumSize(new Dimension(480, 150));
    jPanel7.setPreferredSize(new Dimension(480, 150));
    jbSelectPackage.setEnabled(false);
    jbSelectPackage.setToolTipText("Select Package");
    jbSelectPackage.setText("Select Package");
//    jbSelectPackage.addActionListener(new java.awt.event.ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        jbSelectPackage_actionPerformed(e);
//      }
//    });
    this.add(jPanel7, null);
    jPanel7.add(jtfPackageName, new GridBagConstraints(1, 0, 1, 2, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 10, 39, 0), -28, 2));
    jPanel7.add(jbSelectPackage, new GridBagConstraints(2, 0, 1, 2, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 12, 39, 16), -103, -1));
    jPanel7.add(jtfClassName, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(20, 10, 0, 178), 4, 2));
    jPanel7.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(22, 2, 0, 0), -27, 0));
    jPanel7.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 2, 0, 0), 33, 0));
  }

  // Accessor methods for data
  private String getPackageName() {
    String pkgName = jtfPackageName.getText();
    return pkgName;
  }

  /**
   * Return a message for any errors encountered else an empty string if all is OK.
   */
  private String checkForErrors() {
    String errorMessages = "";
//    if (!JotNames.isValidClassName(getPackageName()))
//      errorMessages += "Invalid package name\n";
//    if (!JotNames.isValidClassName(jtfClassName.getText()))
//      errorMessages += "Invalid class name\n";
    return errorMessages;
  }

  /**
   * Check page for errors and stores values in context.
   * @throws Exception if problems found
   */
  public void checkPage() throws Exception {
/** @todo Change this to a custom execption */
    String errorMessages = checkForErrors();
    if (errorMessages.length()!=0) {
      JOptionPane.showMessageDialog(null, /** @todo this should be parent for dialog */
        errorMessages,
        "Errors Encountered",
        JOptionPane.ERROR_MESSAGE,
        null);
      throw new Exception();
    }
    putContext(PACKAGE_NAME, getPackageName());
    putContext(CLASS_NAME, jtfClassName.getText());
  }

//  /**
//   * Opens a package browser tree for the user to select the package
//   */
//  void jbSelectPackage_actionPerformed(ActionEvent e) {
//    PackageBrowserTree tree =
//      new PackageBrowserTree(wizardHost.getBrowser().getActiveProject());
//    if (PackageBrowserDialog.showPackageBrowserDialog(this, "Select Package", tree))
//      jtfPackageName.setText(tree.getSelectedPath());
//  }
//
}
