/**
 * ValueHolderInfoPanel - A wizard panel to gather info about value holders
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
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import org.apache.velocity.VelocityContext;
import com.taursys.swing.*;
import com.taursys.model.*;
import java.util.*;

/**
 * ValueHolderInfoPanel is a wizard panel to gather info about value holders.
 * @author Marty Phelan
 * @version 1.0
 */
public class ValueHolderInfoPanel extends WizardPanel {
  // Non GUI instance variables
  public static final String HOLDER_LIST = "holderList";
  // end Non-GUI
  private JScrollPane holdersScrollPane = new JScrollPane();
  private MTable holdersTable = new MTable();
  private VOListValueHolder holder = new VOListValueHolder(new Vector());
  private JLabel nameLabel = new JLabel();
  private JLabel aliasLabel = new JLabel();
  private JLabel typeLabel = new JLabel();
  private JLabel classLabel = new JLabel();
  private MTextField nameField = new MTextField();
  private MTextField alialField = new MTextField();
  private MTextField typeField = new MTextField();
  private MTextField classField = new MTextField();
  private JButton addButton = new JButton();
  private MButton removeButton = new MButton();
  //
  private DefaultTableCellRenderer cellRenderer = null;
  private MTableColumn holderNameColumn = new MTableColumn();
  private MTableColumn holderAliasColumn = new MTableColumn();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * Constructs a new ValueHolderInfoPanel
   */
  public ValueHolderInfoPanel() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    holder.setValueObjectClass(com.taursys.tools.ValueHolderInfo.class);
    holdersTable.setListValueHolder(holder);
    this.setLayout(gridBagLayout1);
    nameLabel.setText("Name:");
    aliasLabel.setText("Alias:");
    typeLabel.setText("Type:");
    classLabel.setText("Class:");
    addButton.setText("Add");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    removeButton.setText("Remove");
    removeButton.setValueHolder(holder);
    removeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeButton_actionPerformed(e);
      }
    });
    nameField.setPropertyName("holderName");
    nameField.setValueHolder(holder);
    nameField.setToolTipText("Instance variable name for holder in code");
    alialField.setPropertyName("holderAlias");
    alialField.setValueHolder(holder);
    alialField.setToolTipText("Name used in html/xml document ID\'s for auto-binding.");
    typeField.setPropertyName("holderType");
    typeField.setValueHolder(holder);
    typeField.setToolTipText("Class of holder. Must subclass com.taursys.model.ValueHolder");
    classField.setPropertyName("containedClassName");
    classField.setValueHolder(holder);
    classField.setToolTipText("Class contained by holder(needed by VOValueHolder\'s)");
    holderNameColumn.setPreferredWidth(80);
    holderNameColumn.setDisplayHeading("Name");
    holderNameColumn.setPropertyName("holderName");
    holderNameColumn.setValueHolder(holder);
    holderNameColumn.setHeaderValue("Holder Name");
    holdersTable.addColumn(holderNameColumn);
    holderAliasColumn.setDisplayHeading("Alias");
    holderAliasColumn.setPropertyName("holderAlias");
    holdersTable.addColumn(holderAliasColumn);
    this.add(holdersScrollPane, new GridBagConstraints(0, 4, 4, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(8, 17, 17, 22), -85, -271));
    this.add(addButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 26, 0, 52), 0, 0));
    this.add(removeButton, new GridBagConstraints(2, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(13, 37, 0, 94), 0, 0));
    this.add(typeLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 23, 0, 0), 0, 0));
    this.add(typeField, new GridBagConstraints(1, 1, 3, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 8, 0, 49), 290, 0));
    this.add(classField, new GridBagConstraints(1, 2, 3, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(15, 8, 0, 49), 290, 0));
    this.add(classLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(17, 17, 0, 0), 0, 0));
    this.add(nameField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(22, 8, 0, 0), 125, 0));
    this.add(nameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(22, 17, 0, 0), 0, 0));
    this.add(alialField, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(22, 7, 0, 22), 125, 0));
    this.add(aliasLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(22, 21, 0, 0), 0, 0));
    holdersScrollPane.getViewport().add(holdersTable, null);
  }

  void addButton_actionPerformed(ActionEvent e) {
    holder.add(new ValueHolderInfo("com.taursys.model.VOValueHolder",
         null, "holder"+holder.size(), null));
  }

  void removeButton_actionPerformed(ActionEvent e) {
    holder.remove();
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
    String errorMessages = checkForErrors();
    if (errorMessages.length()!=0) {
      JOptionPane.showMessageDialog(null, /** @todo this should be parent for dialog */
        errorMessages,
        "Errors Encountered",
        JOptionPane.ERROR_MESSAGE,
        null);
      throw new Exception();
    }
    putContext(HOLDER_LIST, holder.getList());
  }

  /**
   * For testing/designing only
   */
  static public void main(String[] args) {
    try {
      JFrame frame = new JFrame();
      ValueHolderInfoPanel panel = new ValueHolderInfoPanel();
      frame.setSize(400,400);
      frame.getContentPane().add(panel);
      frame.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
