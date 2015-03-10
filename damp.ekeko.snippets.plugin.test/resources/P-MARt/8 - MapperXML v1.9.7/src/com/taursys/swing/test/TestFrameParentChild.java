/**
 * TestFrameParentChild -
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

import java.awt.*;
import javax.swing.*;
import com.taursys.swing.*;
import com.taursys.model.*;
import java.awt.event.*;

/**
 * TestFrameParentChild is ...
 * @author Marty Phelan
 * @version 1.0
 */
public class TestFrameParentChild extends javax.swing.JFrame {
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private MButton newInvoiceButton = new MButton();
  private MButton removeInvoiceButton = new MButton();
  private MButton newItemButton = new MButton();
  private MButton removeItemButton = new MButton();
  private MTextField invoiceNumberField = new MTextField();
  private MTextField issueDateField = new MTextField();
  private MTextField customerIDField = new MTextField();
  private MTextField termsField = new MTextField();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JLabel jLabel4 = new JLabel();
  private MTable invoiceTable = new MTable();
  private VOListValueHolder invoices = new VOListValueHolder();
  private VOListValueHolder items = new VOListValueHolder();
  private MTableColumn invoiceNumberCol = new MTableColumn();
  private MTableColumn issueDateCol = new MTableColumn();
  private MTableColumn customerIDCol = new MTableColumn();
  private MTableColumn termsCol = new MTableColumn();
  private MTextField itemNoField = new MTextField();
  private JLabel jLabel5 = new JLabel();
  private MTextField quantityField = new MTextField();
  private JLabel jLabel6 = new JLabel();
  private MTextField productIDField = new MTextField();
  private JLabel jLabel7 = new JLabel();
  private MTextField unitPriceField = new MTextField();
  private JLabel jLabel8 = new JLabel();
  private MTable itemTable = new MTable();
  private MTableColumn itemNoCol = new MTableColumn();
  private MTableColumn quantityCol = new MTableColumn();
  private MTableColumn productIDCol = new MTableColumn();
  private MTableColumn unitPriceCol = new MTableColumn();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * Constructs a new TestFrameParentChild
   */
  public TestFrameParentChild() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
/** @todo com.taursys.model.ModelPropertyAccessorException: ValueObject and ValueObjectClass are both null.
 *   must set VO class before binding */
    invoices.setValueObjectClass(com.taursys.swing.test.InvoiceVO.class);
    items.setValueObjectClass(com.taursys.swing.test.InvoiceItemVO.class);
    items.setParentValueHolder(invoices);
    items.setParentPropertyName("items");
    this.setSize(650,500);
    this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    this.setTitle("MTable Tests");
    this.getContentPane().setLayout(gridBagLayout1);
    newInvoiceButton.setText("New");
    newInvoiceButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newInvoiceButton_actionPerformed(e);
      }
    });
    removeInvoiceButton.setText("Remove");
    removeInvoiceButton.setValueHolder(invoices);
    removeInvoiceButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeInvoiceButton_actionPerformed(e);
      }
    });
    newItemButton.setText("New");
    newItemButton.setValueHolder(invoices);
    newItemButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newItemButton_actionPerformed(e);
      }
    });
    removeItemButton.setText("Remove");
    removeItemButton.setValueHolder(items);
    removeItemButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeItemButton_actionPerformed(e);
      }
    });
    issueDateField.setFormat(java.text.SimpleDateFormat.getInstance());
    issueDateField.setFormatPattern("MM/dd/yyyy");
    issueDateField.setPropertyName("issueDate");
    issueDateField.setValueHolder(invoices);
    issueDateField.setPreferredSize(new Dimension(100, 19));
    customerIDField.setPropertyName("customerID");
    customerIDField.setValueHolder(invoices);
    customerIDField.setPreferredSize(new Dimension(100, 19));
    termsField.setPropertyName("terms");
    termsField.setValueHolder(invoices);
    termsField.setPreferredSize(new Dimension(150, 19));
    jLabel1.setText("Inv no:");
    jLabel2.setText("Date:");
    jLabel3.setText("Cust ID:");
    jLabel4.setText("Terms:");
    invoiceNumberField.setPropertyName("invoiceNumber");
    invoiceNumberField.setValueHolder(invoices);
    invoiceNumberField.setPreferredSize(new Dimension(100, 19));
    invoiceNumberCol.setPropertyName("invoiceNumber");
    issueDateCol.setFormat(java.text.SimpleDateFormat.getInstance());
    issueDateCol.setFormatPattern("MM/dd/yyyy");
    issueDateCol.setPropertyName("issueDate");
    customerIDCol.setPropertyName("customerID");
    termsCol.setPropertyName("terms");
/** @todo Attempt to add a MTableColumn without a propertyName - adds must follow col define */
    invoiceTable.setListValueHolder(invoices);
    invoiceTable.addColumn(invoiceNumberCol);
    invoiceTable.addColumn(customerIDCol);
    invoiceTable.addColumn(issueDateCol);
    invoiceTable.addColumn(termsCol);
    jLabel5.setText("Item no:");
    itemNoField.setPropertyName("itemNo");
    itemNoField.setValueHolder(items);
    quantityField.setPropertyName("quantity");
    quantityField.setValueHolder(items);
    jLabel6.setText("Quantity:");
    productIDField.setPropertyName("productID");
    productIDField.setValueHolder(items);
    jLabel7.setText("Product ID:");
    unitPriceField.setFormat(java.text.DecimalFormat.getInstance());
    unitPriceField.setFormatPattern("###,##0.00");
    unitPriceField.setPropertyName("unitPrice");
    unitPriceField.setValueHolder(items);
    jLabel8.setText("Unit price:");
    itemNoCol.setPropertyName("itemNo");
    quantityCol.setPropertyName("quantity");
    productIDCol.setPropertyName("productID");
    unitPriceCol.setFormat(java.text.DecimalFormat.getInstance());
    unitPriceCol.setFormatPattern("###,##0.00");
    unitPriceCol.setPropertyName("unitPrice");
    // ====
    itemTable.setListValueHolder(items);
    itemTable.addColumn(itemNoCol);
    itemTable.addColumn(quantityCol);
    itemTable.addColumn(productIDCol);
    itemTable.addColumn(unitPriceCol);
    this.getContentPane().add(jScrollPane2, new GridBagConstraints(0, 8, 5, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 24, 25, 23), 124, -332));
    jScrollPane2.getViewport().add(itemTable, null);
    this.getContentPane().add(jScrollPane1, new GridBagConstraints(0, 3, 5, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 18, 0, 23), 130, -308));
    jScrollPane1.getViewport().add(invoiceTable, null);
    this.getContentPane().add(newInvoiceButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(17, 25, 0, 6), 0, 0));
    this.getContentPane().add(removeInvoiceButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(18, 47, 0, 0), 0, 0));
    this.getContentPane().add(newItemButton, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(11, 31, 0, 0), 0, 0));
    this.getContentPane().add(removeItemButton, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 49, 0, 0), -1, 0));
    this.getContentPane().add(issueDateField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 0, 0, 29), 1, 0));
    this.getContentPane().add(customerIDField, new GridBagConstraints(3, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(13, 6, 0, 246), 3, 0));
    this.getContentPane().add(termsField, new GridBagConstraints(3, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 6, 0, 207), -8, 0));
    this.getContentPane().add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(11, 52, 0, 8), 0, 0));
    this.getContentPane().add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 44, 0, 6), 0, 0));
    this.getContentPane().add(invoiceNumberField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(13, 0, 0, 26), 4, 0));
    this.getContentPane().add(itemNoField, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(23, 0, 0, 17), 109, 0));
    this.getContentPane().add(jLabel5, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(26, 27, 0, 14), 0, 0));
    this.getContentPane().add(productIDField, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(13, 0, 0, 16), 110, 0));
    this.getContentPane().add(jLabel7, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 18, 0, 6), 0, 0));
    this.getContentPane().add(jLabel6, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 28, 0, 7), 0, 0));
    this.getContentPane().add(quantityField, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(13, 0, 0, 16), 110, 0));
    this.getContentPane().add(unitPriceField, new GridBagConstraints(4, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(24, 18, 0, 174), 106, 0));
    this.getContentPane().add(jLabel8, new GridBagConstraints(2, 4, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(26, 37, 0, 0), 0, 0));
    this.getContentPane().add(jLabel4, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 0, 0, 0), 0, 0));
    this.getContentPane().add(jLabel3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 0, 0, 0), 0, 0));
  }

  /**
   * For testing/designing only
   */
  static public void main(String[] args) {
    try {
      TestFrameParentChild frame = new TestFrameParentChild();
      frame.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void newInvoiceButton_actionPerformed(ActionEvent e) {
    invoices.add(new InvoiceVO());
  }

  void removeInvoiceButton_actionPerformed(ActionEvent e) {
    invoices.remove();
  }

  void newItemButton_actionPerformed(ActionEvent e) {
    items.add(new InvoiceItemVO());
  }

  void removeItemButton_actionPerformed(ActionEvent e) {
    items.remove();
  }
}
