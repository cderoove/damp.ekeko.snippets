/**
 * ValueObjectPropertyInfoPanel - A wizard panel to gather info about value holders
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
 * ValueObjectPropertyInfoPanel is a wizard panel to gather info about value holders.
 * @author Marty Phelan
 * @version 1.0
 */
public class ValueObjectPropertyInfoPanel extends WizardPanel {
  // Non GUI instance variables
  public static final String PROPERTY_LIST = "propertyList";
  // end Non-GUI
  private JScrollPane holdersScrollPane = new JScrollPane();
  private MTable propertiesTable = new MTable();
  private VOListValueHolder holder = new VOListValueHolder(new Vector());
  private JLabel nameLabel = new JLabel();
  private JLabel typeLabel = new JLabel();
  private JLabel classLabel = new JLabel();
  private MTextField nameField = new MTextField();
  private MTextField typeField = new MTextField();
  private MTextField getSetDescriptionField = new MTextField();
  private JButton addButton = new JButton();
  private MButton removeButton = new MButton();
  //
  private DefaultTableCellRenderer cellRenderer = null;
  private MTableColumn propertyNameColumn = new MTableColumn();
  private MTableColumn propertyTypeColumn = new MTableColumn();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * Constructs a new ValueObjectPropertyInfoPanel
   */
  public ValueObjectPropertyInfoPanel() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    holder.setValueObjectClass(com.taursys.tools.ValueObjectPropertyInfo.class);
    propertiesTable.setListValueHolder(holder);
    this.setLayout(gridBagLayout1);
    nameLabel.setText("Name:");
    typeLabel.setText("Type:");
    classLabel.setText("Description:");
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
    nameField.setPropertyName("propertyName");
    nameField.setValueHolder(holder);
    nameField.setToolTipText("the name of this property");
    typeField.setPropertyName("propertyType");
    typeField.setValueHolder(holder);
    typeField.setToolTipText("the primative type or Object type of this property");
    getSetDescriptionField.setPropertyName("getSetDescription");
    getSetDescriptionField.setValueHolder(holder);
    getSetDescriptionField.setToolTipText("Description for get, set, param, and return JavaDoc");
    propertyNameColumn.setPreferredWidth(80);
    propertyNameColumn.setDisplayHeading("Name");
    propertyNameColumn.setPropertyName("propertyName");
    propertyNameColumn.setValueHolder(holder);
    propertiesTable.addColumn(propertyNameColumn);
    propertyTypeColumn.setDisplayHeading("Type");
    propertyTypeColumn.setPropertyName("propertyType");
    propertiesTable.addColumn(propertyTypeColumn);
    this.add(holdersScrollPane, new GridBagConstraints(0, 4, 4, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(8, 17, 17, 22), -92, -273));
    this.add(addButton, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 0, 0, 66), 8, 0));
    this.add(removeButton, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(13, 0, 0, 94), 0, 0));
    this.add(typeField, new GridBagConstraints(2, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(15, 9, 0, 49), 246, 0));
    this.add(getSetDescriptionField, new GridBagConstraints(2, 2, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(15, 8, 0, 49), 247, 0));
    this.add(classLabel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(18, 17, 0, 0), 0, 0));
    this.add(nameField, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(19, 10, 0, 0), 117, 0));
    this.add(typeLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(17, 53, 0, 0), 0, 0));
    this.add(nameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(19, 52, 0, 0), 0, 0));
    holdersScrollPane.getViewport().add(propertiesTable, null);
  }

  void addButton_actionPerformed(ActionEvent e) {
    holder.add(new ValueObjectPropertyInfo());
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
    putContext(PROPERTY_LIST, holder.getList());
  }

  /**
   * For testing/designing only
   */
  static public void main(String[] args) {
    try {
      JFrame frame = new JFrame();
      ValueObjectPropertyInfoPanel panel = new ValueObjectPropertyInfoPanel();
      frame.setSize(400,400);
      frame.getContentPane().add(panel);
      frame.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
