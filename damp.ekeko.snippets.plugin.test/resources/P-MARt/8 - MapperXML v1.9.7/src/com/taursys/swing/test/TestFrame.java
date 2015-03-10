/**
 * TestFrame - Frame for design/testing of various com.taursys.swing.Mx components
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
package com.taursys.swing.test;

import com.taursys.model.*;
import com.taursys.model.test.*;
import javax.swing.*;
import java.awt.*;
import com.taursys.swing.*;
import java.awt.event.*;
import java.util.*;
import java.math.BigDecimal;

/**
 * TestFrame is a Frame for design/testing of various com.taursys.swing.Mx components.
 * @author Marty Phelan
 * @version 1.0
 */
public class TestFrame extends javax.swing.JFrame {
  private VOComparator sorter = new VOComparator();
  private VOListValueHolder listHolder = new VOListValueHolder();
  private MTextField fullNameField = new MTextField();
  private JLabel jLabel1 = new JLabel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private MTable mTable = new MTable();
  private MTableColumn fullNameColumn = new MTableColumn();
  private MButton addRowButton = new MButton();
  private MButton deleteRowButton = new MButton();
  private MButton retrieveRowsButton = new MButton();
  private PersonVO[] people = new PersonVO[] {
    new BeverlyCrusherVO(),
    new WilliamTRikerVO(),
    new JeanLucPicardVO()
  };
  private MComboBox supervisorCombo = new MComboBox();
  private JLabel jLabel2 = new JLabel();
  private MTableColumn supervisorColumn = new MTableColumn();
  private MTextField firstNameField = new MTextField();
  private MTextField lastNameField = new MTextField();
  private JLabel jLabel3 = new JLabel();
  private JLabel jLabel4 = new JLabel();
  private MComboBox mComboBox1 = new MComboBox(new String[] {
    "red",
    "yellow",
    "green",
  });
  private JLabel jLabel5 = new JLabel();
  private MTextField unboundField = new MTextField();
  private JLabel jLabel6 = new JLabel();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private MTextArea mTextArea1 = new MTextArea();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * Constructs a new TestFrame
   */
  public TestFrame() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setSize(600,500);
    this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    this.setTitle("MTable Tests");
    this.getContentPane().setLayout(gridBagLayout1);
    sorter.setPropertyName("fullName");
    jLabel1.setText("Full name:");
    listHolder.setValueObjectClass(PersonVO.class);
    listHolder.setComparator(sorter);
    mTable.setListValueHolder(listHolder);
    fullNameField.setPropertyName("fullName");
    fullNameField.setValueHolder(listHolder);
    fullNameColumn.setDisplayHeading("Full Name");
    fullNameColumn.setPropertyName("fullName");
    fullNameColumn.setValueHolder(listHolder);
    mTable.addColumn(fullNameColumn);
    addRowButton.setText("Add Row");
    addRowButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addRowButton_actionPerformed(e);
      }
    });
    deleteRowButton.setText("Delete Row");
    deleteRowButton.setValueHolder(listHolder);
    deleteRowButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteRowButton_actionPerformed(e);
      }
    });
    retrieveRowsButton.setText("Retrieve Rows");
    retrieveRowsButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        retrieveRowsButton_actionPerformed(e);
      }
    });
    jLabel2.setText("Supervisor:");
    supervisorCombo.setListValueHolder(listHolder);
    supervisorCombo.setDisplayPropertyName("firstName");
    supervisorCombo.setValueHolder(listHolder);
    supervisorCombo.setPropertyName("supervisorID");
    supervisorCombo.setListPropertyName("personID");
    supervisorCombo.setNullAllowed(true);
    supervisorCombo.setNullDisplay("--No Supervisor--");
    supervisorColumn.setDisplayHeading("Supervisor ID");
    supervisorColumn.setPropertyName("supervisorID");
    supervisorColumn.setValueHolder(listHolder);
    mTable.addColumn(supervisorColumn);
    firstNameField.setPropertyName("firstName");
    firstNameField.setValueHolder(listHolder);
    jLabel3.setText("First name:");
    jLabel4.setText("Last name:");
    lastNameField.setPropertyName("lastName");
    lastNameField.setValueHolder(listHolder);
    jLabel5.setText("Unbound Combo");
    unboundField.setText("Unbound Field");
    jLabel6.setText("Unbound field:");
    mTextArea1.setValueHolder(listHolder);
    mTextArea1.setPropertyName("notes");
    this.getContentPane().add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 49, 0, 9), 0, 0));
    this.getContentPane().add(fullNameField, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(11, 10, 0, 40), 195, 0));
    this.getContentPane().add(supervisorCombo, new GridBagConstraints(1, 4, 2, 2, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 37, 20), 68, 0));
    this.getContentPane().add(jLabel2, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 45, 0, 8), 0, 1));
    this.getContentPane().add(firstNameField, new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(17, 8, 0, 42), 195, 0));
    this.getContentPane().add(lastNameField, new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(9, 10, 0, 43), 192, 0));
    this.getContentPane().add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 48, 0, 0), 0, 0));
    this.getContentPane().add(jLabel4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 46, 0, 9), 0, 0));
    this.getContentPane().add(mComboBox1, new GridBagConstraints(1, 5, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(18, 9, 0, 23), 145, 0));
    this.getContentPane().add(jLabel5, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(26, 16, 0, 0), 0, 0));
    this.getContentPane().add(unboundField, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(18, 10, 0, 40), 112, 0));
    this.getContentPane().add(jLabel6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(18, 26, 0, 0), 0, 0));
    this.getContentPane().add(addRowButton, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(19, 16, 0, 13), 0, 0));
    this.getContentPane().add(deleteRowButton, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(17, 11, 0, 0), 0, 0));
    this.getContentPane().add(retrieveRowsButton, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(17, 22, 0, 0), -10, 0));
    this.getContentPane().add(jScrollPane1, new GridBagConstraints(0, 7, 4, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(16, 16, 58, 24), 107, -299));
    this.getContentPane().add(jScrollPane2, new GridBagConstraints(3, 0, 1, 7, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(18, 20, 0, 24), 110, 204));
    jScrollPane2.getViewport().add(mTextArea1, null);
    jScrollPane1.getViewport().add(mTable, null);
  }

  /**
   * For testing/designing only
   */
  static public void main(String[] args) {
    try {
      TestFrame frame = new TestFrame();
      frame.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void addRowButton_actionPerformed(ActionEvent e) {
    listHolder.add(new PersonVO());
  }

  void deleteRowButton_actionPerformed(ActionEvent e) {
    listHolder.remove();
  }

  void retrieveRowsButton_actionPerformed(ActionEvent e) {
    listHolder.setList(new ArrayList(Arrays.asList(people)));
  }
}
