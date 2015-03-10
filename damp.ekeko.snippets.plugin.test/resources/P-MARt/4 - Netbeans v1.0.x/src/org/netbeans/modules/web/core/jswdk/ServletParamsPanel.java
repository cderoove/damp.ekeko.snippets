/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jswdk;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;

import org.openide.util.NbBundle;
/**
 *
 * @author  pjiricka
 * @version 
 */
public class ServletParamsPanel extends javax.swing.JPanel {

    private ServletExecParams params;

    static final long serialVersionUID =6133301587971190835L;
    /** Creates new form ServletParamsPanel */
    public ServletParamsPanel(ServletExecParams params) {
        initComponents ();
        this.params = params;
        nameField.setText(params.getName());
        mappingField.setText(params.getMapping());
        queryStringField.setText(params.getQueryString());
        Vector names = new Vector();
        names.add(NbBundle.getBundle(ServletExecParams.class).getString("LBL_InitParamName"));
        names.add(NbBundle.getBundle(ServletExecParams.class).getString("LBL_InitParamValue"));
        DefaultTableModel model = new DefaultTableModel(params.getInitParams(), names);
        initParamsTable.setModel(model);
        initParamsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public ServletExecParams getServletExecParams() {
        params.setName(nameField.getText());
        params.setMapping(mappingField.getText());
        params.setQueryString(queryStringField.getText());
        DefaultTableModel model = (DefaultTableModel)initParamsTable.getModel();
        for (int i=0; i<model.getRowCount(); i++) {
            String par = (String)model.getValueAt(i, 0);
            String val = (String)model.getValueAt(i, 1);
            if ("".equals(par) || par == null) { // NOI18N
                model.removeRow(i);
                i--;
            }
            else
                if (val == null) model.setValueAt("", i, 1); // NOI18N
        }
        params.setInitParams(model.getDataVector());
        return params;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        jLabel1 = new javax.swing.JLabel ();
        nameField = new javax.swing.JTextField ();
        jLabel2 = new javax.swing.JLabel ();
        mappingField = new javax.swing.JTextField ();
        jLabel3 = new javax.swing.JLabel ();
        jScrollPane1 = new javax.swing.JScrollPane ();
        initParamsTable = new javax.swing.JTable ();
        addParamButton = new javax.swing.JButton ();
        removeParamButton = new javax.swing.JButton ();
        jLabel4 = new javax.swing.JLabel ();
        queryStringField = new javax.swing.JTextField ();
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        jLabel1.setText (org.openide.util.NbBundle.getBundle(ServletParamsPanel.class).getString("ServletParamsPanel.jLabel1.text"));


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (0, 0, 0, 8);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add (jLabel1, gridBagConstraints1);



        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.weightx = 1.0;
        add (nameField, gridBagConstraints1);

        jLabel2.setText (org.openide.util.NbBundle.getBundle(ServletParamsPanel.class).getString("ServletParamsPanel.jLabel2.text"));


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add (jLabel2, gridBagConstraints1);



        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        add (mappingField, gridBagConstraints1);

        jLabel3.setText (org.openide.util.NbBundle.getBundle(ServletParamsPanel.class).getString("ServletParamsPanel.jLabel3.text"));


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 8);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add (jLabel3, gridBagConstraints1);



        jScrollPane1.setViewportView (initParamsTable);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (jScrollPane1, gridBagConstraints1);

        addParamButton.setText (org.openide.util.NbBundle.getBundle(ServletParamsPanel.class).getString("ServletParamsPanel.addParamButton.text"));
        addParamButton.addActionListener (new java.awt.event.ActionListener () {
                                              public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                  addParamButtonActionPerformed (evt);
                                              }
                                          }
                                         );


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 8);
        gridBagConstraints1.weightx = 1.0;
        add (addParamButton, gridBagConstraints1);

        removeParamButton.setText (org.openide.util.NbBundle.getBundle(ServletParamsPanel.class).getString("ServletParamsPanel.removeParamButton.text"));
        removeParamButton.addActionListener (new java.awt.event.ActionListener () {
                                                 public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                     removeParamButtonActionPerformed (evt);
                                                 }
                                             }
                                            );


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        add (removeParamButton, gridBagConstraints1);

        jLabel4.setText (org.openide.util.NbBundle.getBundle(ServletParamsPanel.class).getString("ServletParamsPanel.jLabel4.text"));


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add (jLabel4, gridBagConstraints1);



        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 0);
        add (queryStringField, gridBagConstraints1);

    }//GEN-END:initComponents

    private void removeParamButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeParamButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel)initParamsTable.getModel();
        model.removeRow(initParamsTable.getSelectionModel().getMinSelectionIndex());
        // Add your handling code here:
    }//GEN-LAST:event_removeParamButtonActionPerformed

    private void addParamButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addParamButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel)initParamsTable.getModel();
        model.addRow(new String[2]);
    }//GEN-LAST:event_addParamButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField mappingField;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable initParamsTable;
    private javax.swing.JButton addParamButton;
    private javax.swing.JButton removeParamButton;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField queryStringField;
    // End of variables declaration//GEN-END:variables

}
/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Petr Jiricka    More i18n
 *  5    Gandalf   1.4         1/12/00  Petr Jiricka    i18n phase 1
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/7/99  Petr Jiricka    
 *  1    Gandalf   1.0         10/7/99  Petr Jiricka    
 * $
 */
