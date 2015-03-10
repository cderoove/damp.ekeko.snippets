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

package org.openide.explorer.propertysheet.editors;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Custom editor for ErrorExpression property of external compilers.
 *
 * @author Jan Formanek
 */
final class ErrorExpressionPanel extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {

    /** Reference to ErrorDescriptionPropertyEditor
    */
    private ExternalCompiler.ErrorExpressionEditor editor;

    static final long serialVersionUID =-2763818133650482979L;

    /** Initializes the Form
    * @param ed an ErrorDescriptionPropertyEditor instance
    */
    public ErrorExpressionPanel(ExternalCompiler.ErrorExpressionEditor ed) {
        editor = ed;
        // make a copy of the current value
        descriptions = (org.openide.compiler.ExternalCompiler.ErrorExpression)ed.getValue ();

        initComponents ();

        // localize components
        setBorder (new javax.swing.border.CompoundBorder (
                       new javax.swing.border.EmptyBorder (new java.awt.Insets(2, 2, 2, 2)),
                       new javax.swing.border.TitledBorder (
                           new javax.swing.border.EtchedBorder (), getString("CTL_Error_description_name"))));

        addButton.setText(getString("CTL_Add"));
        addButton.setToolTipText(getString("CTL_Add_tip"));
        removeButton.setText(getString("CTL_Remove"));
        removeButton.setToolTipText(getString("CTL_Remove_tip"));
        setButton.setText(getString("CTL_Set"));
        setButton.setToolTipText(getString("CTL_Set_tip"));
        presetNameLabel.setText(getString("CTL_Preset_label"));
        errorDescriptionLabel.setText(getString("CTL_Error_label"));
        filePositionLabel.setText(getString("CTL_File_label"));
        linePositionLabel.setText(getString("CTL_Line_label"));
        columnPositionLabel.setText(getString("CTL_Column_label"));
        descriptionPositionLabel.setText(getString("CTL_Description_label"));

        ListSelectionListener l = new ListSelectionListener() {
                                      public void valueChanged(ListSelectionEvent ev) {
                                          if (internalListChange) return;
                                          int sel = errorDescriptions.getSelectedIndex();
                                          if (sel < 0) return;
                                          descriptions = editor.getExpressions ()[sel];
                                          updateFields ();
                                          updateButtons ();
                                      }
                                  };
        errorDescriptions.addListSelectionListener(l);
        updateList ();
        updateFields ();
        updateButtons ();

        HelpCtx.setHelpIDString (this, ErrorExpressionPanel.class.getName ());
    }

    /**
    * @return Returns the property value that is result of the CustomPropertyEditor.
    * @exception InvalidStateException when the custom property editor does not represent valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue () throws IllegalStateException {
        return descriptions;
    }

    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension d = super.getPreferredSize ();
        if (d.width < 400) d.width = 400;
        return d;
    }

    private void updateList () {
        org.openide.compiler.ExternalCompiler.ErrorExpression[] exprs = editor.getExpressions ();
        org.openide.compiler.ExternalCompiler.ErrorExpression sel = descriptions;

        String[] strings = new String [exprs.length];
        int selIndex = -1;
        for (int i = 0; i < exprs.length; i++) {
            strings[i] = exprs[i].getName ();
            if (exprs[i].getName ().equals (sel.getName ())) selIndex = i;
        }

        internalListChange = true;
        errorDescriptions.setListData(strings);
        if (selIndex != -1)
            errorDescriptions.setSelectedIndex (selIndex);
        internalListChange = false;
    }

    private void updateFields () {
        org.openide.compiler.ExternalCompiler.ErrorExpression current = descriptions;
        presetNameField.setText(current.getName ());
        errorDescriptionField.setText(current.getErrorExpression());
        filePositionField.setText(String.valueOf(current.getFilePos()));
        linePositionField.setText(String.valueOf(current.getLinePos()));
        columnPositionField.setText(String.valueOf(current.getColumnPos()));
        descriptionPositionField.setText(String.valueOf(current.getDescriptionPos()));
    }

    private void updateButtons () {
        removeButton.setEnabled (errorDescriptions.getSelectedIndex () != -1);
        setButton.setEnabled (errorDescriptions.getSelectedIndex () != -1);
        addButton.setEnabled (!"".equals (presetNameField.getText ())); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 0)));
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        descriptionPanel = new javax.swing.JPanel ();
        descriptionPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints2;

        presetNameLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add (presetNameLabel, gridBagConstraints2);

        presetNameField = new javax.swing.JTextField ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        descriptionPanel.add (presetNameField, gridBagConstraints2);

        errorDescriptionLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weighty = 0.2;
        descriptionPanel.add (errorDescriptionLabel, gridBagConstraints2);

        errorDescriptionField = new javax.swing.JTextField ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.ipadx = 20;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 1.0;
        descriptionPanel.add (errorDescriptionField, gridBagConstraints2);

        filePositionLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weighty = 0.2;
        descriptionPanel.add (filePositionLabel, gridBagConstraints2);

        filePositionField = new javax.swing.JTextField ();
        filePositionField.setHorizontalAlignment (javax.swing.SwingConstants.RIGHT);
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.ipadx = 40;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 20);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add (filePositionField, gridBagConstraints2);

        linePositionLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weighty = 0.2;
        descriptionPanel.add (linePositionLabel, gridBagConstraints2);

        linePositionField = new javax.swing.JTextField ();
        linePositionField.setHorizontalAlignment (javax.swing.SwingConstants.RIGHT);
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.ipadx = 40;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 20);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add (linePositionField, gridBagConstraints2);

        columnPositionLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weighty = 0.2;
        descriptionPanel.add (columnPositionLabel, gridBagConstraints2);

        columnPositionField = new javax.swing.JTextField ();
        columnPositionField.setHorizontalAlignment (javax.swing.SwingConstants.RIGHT);
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.ipadx = 40;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 20);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add (columnPositionField, gridBagConstraints2);

        descriptionPositionLabel = new javax.swing.JLabel ();
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weighty = 0.2;
        descriptionPanel.add (descriptionPositionLabel, gridBagConstraints2);

        descriptionPositionField = new javax.swing.JTextField ();
        descriptionPositionField.setHorizontalAlignment (javax.swing.SwingConstants.RIGHT);
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.ipadx = 40;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 20);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add (descriptionPositionField, gridBagConstraints2);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        add (descriptionPanel, gridBagConstraints1);

        buttonsPanel = new javax.swing.JPanel ();
        buttonsPanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 8, 0, 0)));
        buttonsPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints3;

        addButton = new javax.swing.JButton ();
        addButton.setEnabled (false);
        addButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             addButtonActionPerformed (evt);
                                         }
                                     }
                                    );
        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets (2, 2, 2, 5);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.SOUTH;
        buttonsPanel.add (addButton, gridBagConstraints3);

        removeButton = new javax.swing.JButton ();
        removeButton.setEnabled (false);
        removeButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                removeButtonActionPerformed (evt);
                                            }
                                        }
                                       );
        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 2;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets (2, 2, 2, 5);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints3.weighty = 1.0;
        buttonsPanel.add (removeButton, gridBagConstraints3);

        setButton = new javax.swing.JButton ();
        setButton.setEnabled (false);
        setButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             setButtonActionPerformed (evt);
                                         }
                                     }
                                    );
        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 1;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets (2, 2, 2, 5);
        buttonsPanel.add (setButton, gridBagConstraints3);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        add (buttonsPanel, gridBagConstraints1);

        presetsScroll = new javax.swing.JScrollPane ();

        errorDescriptions = new javax.swing.JList ();
        errorDescriptions.addMouseListener (new java.awt.event.MouseAdapter () {
                                                public void mouseClicked (java.awt.event.MouseEvent evt) {
                                                    errorDescriptionsMouseClicked (evt);
                                                }
                                            }
                                           );

        presetsScroll.setViewportView (errorDescriptions);
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (presetsScroll, gridBagConstraints1);

    }//GEN-END:initComponents

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        synchronized (editor) {//GEN-HEADEREND:event_removeButtonActionPerformed
            internalListChange = true;
            java.util.Collection exprs = editor.getExpressionsVector ();
            int pos = errorDescriptions.getSelectedIndex ();
            Object value = errorDescriptions.getSelectedValue ();
            exprs.remove (value);
            if (pos >= exprs.size ()) pos = exprs.size () - 1;
            updateList ();
            if (pos >= 0)
                errorDescriptions.setSelectedIndex (pos);
            internalListChange = false;
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void setButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButtonActionPerformed
        if ("".equals (presetNameField.getText ())) // NOI18N
            return;
        org.openide.compiler.ExternalCompiler.ErrorExpression expr = descriptions;
        int fPos = 0;
        int lPos = 0;
        int cPos = 0;
        int dPos = 0;
        try {
            fPos = Integer.parseInt (filePositionField.getText ());
            lPos = Integer.parseInt (linePositionField.getText ());
            cPos = Integer.parseInt (columnPositionField.getText ());
            dPos = Integer.parseInt (descriptionPositionField.getText ());
        } catch (NumberFormatException ex) { // ignored
            return; // [PENDING - notify user]
        }
        expr.setName(presetNameField.getText());
        expr.setErrorExpression(errorDescriptionField.getText());
        expr.setFilePos(fPos);
        expr.setLinePos(lPos);
        expr.setColumnPos(cPos);
        expr.setDescriptionPos(dPos);
        updateList ();
    }//GEN-LAST:event_setButtonActionPerformed

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        if ("".equals (presetNameField.getText ())) // NOI18N
            return;
        org.openide.compiler.ExternalCompiler.ErrorExpression[] exprsAr = editor.getExpressions ();
        for (int i = 0; i < exprsAr.length; i++)
            if (exprsAr[i].getName ().equals (presetNameField.getText ()))
                return;

        org.openide.compiler.ExternalCompiler.ErrorExpression expr = null;
        try {
            expr = new org.openide.compiler.ExternalCompiler.ErrorExpression (
                       presetNameField.getText (),
                       errorDescriptionField.getText (),
                       Integer.parseInt (filePositionField.getText ()),
                       Integer.parseInt (linePositionField.getText ()),
                       Integer.parseInt (columnPositionField.getText ()),
                       Integer.parseInt (descriptionPositionField.getText ())
                   );
        } catch (NumberFormatException ex) { // ignored
            return; // [PENDING - notify user]
        }
        synchronized (editor) {
            internalListChange = true;
            java.util.Collection exprs = editor.getExpressionsVector ();
            int pos = errorDescriptions.getSelectedIndex ();
            exprs.add (expr);
            descriptions = expr;
            pos = exprs.size () - 1;
            updateList ();
            errorDescriptions.setSelectedIndex (pos);
            internalListChange = false;
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void errorDescriptionsMouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_errorDescriptionsMouseClicked
        // Add your handling code here:
        //    int index = errorDescriptions.locationToIndex(evt.getPoint());
        //    setSelected(index);
    }//GEN-LAST:event_errorDescriptionsMouseClicked



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JScrollPane presetsScroll;
    private javax.swing.JLabel presetNameLabel;
    private javax.swing.JTextField presetNameField;
    private javax.swing.JLabel errorDescriptionLabel;
    private javax.swing.JTextField errorDescriptionField;
    private javax.swing.JLabel filePositionLabel;
    private javax.swing.JTextField filePositionField;
    private javax.swing.JLabel linePositionLabel;
    private javax.swing.JTextField linePositionField;
    private javax.swing.JLabel columnPositionLabel;
    private javax.swing.JTextField columnPositionField;
    private javax.swing.JLabel descriptionPositionLabel;
    private javax.swing.JTextField descriptionPositionField;
    private javax.swing.JButton addButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton setButton;
    private javax.swing.JList errorDescriptions;
    // End of variables declaration//GEN-END:variables

    private org.openide.compiler.ExternalCompiler.ErrorExpression descriptions;
    private boolean internalListChange = false;

    /** Getter for resource string.
    */
    private static String getString (String res) {
        return NbBundle.getBundle (ErrorExpressionPanel.class).getString (res);
    }
}

/*
* Log
*/
