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

package org.netbeans.modules.debugger.jpda;

import org.openide.util.NbBundle;


/**
 *
 * @author  jjancura
 * @version 
 */
public class ExceptionBreakpointPanel extends javax.swing.JPanel {

    static final long serialVersionUID = -2304154922973564197L;
    private ExceptionBreakpoint event;

    /** Creates new form ExceptionBreakpointPanel */
    public ExceptionBreakpointPanel (ExceptionBreakpoint e) {
        initComponents ();
        event = e;
        jTextField1.setText (e.getClassName ());
        jComboBox2.addItem (NbBundle.getBundle (ExceptionBreakpointPanel.class).
                            getString ("CTL_Exception_catch"));
        jComboBox2.addItem (NbBundle.getBundle (ExceptionBreakpointPanel.class).
                            getString ("CTL_Exception_uncatch"));
        jComboBox2.addItem (NbBundle.getBundle (ExceptionBreakpointPanel.class).
                            getString ("CTL_Exception_both"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        jLabel4 = new javax.swing.JLabel ();
        jTextField1 = new javax.swing.JTextField ();
        jLabel5 = new javax.swing.JLabel ();
        jLabel6 = new javax.swing.JLabel ();
        jComboBox2 = new javax.swing.JComboBox ();
        jPanel4 = new javax.swing.JPanel ();
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        jLabel4.setText (NbBundle.getBundle (ExceptionBreakpointPanel.class).getString ("CTL_Exception_class_name"));


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add (jLabel4, gridBagConstraints1);

        jTextField1.setColumns (25);
        jTextField1.addFocusListener (new java.awt.event.FocusAdapter () {
                                          public void focusLost (java.awt.event.FocusEvent evt) {
                                              jTextField1FocusLost (evt);
                                          }
                                      }
                                     );


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 1.0;
        add (jTextField1, gridBagConstraints1);

        jLabel5.setText (NbBundle.getBundle (ExceptionBreakpointPanel.class).getString ("CTL_Exception_class_name_description"));


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add (jLabel5, gridBagConstraints1);

        jLabel6.setText (NbBundle.getBundle (ExceptionBreakpointPanel.class).getString ("CTL_Exception_stop_on"));


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add (jLabel6, gridBagConstraints1);

        jComboBox2.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              jComboBox1ActionPerformed (evt);
                                          }
                                      }
                                     );


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add (jComboBox2, gridBagConstraints1);



        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 3;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (jPanel4, gridBagConstraints1);

    }//GEN-END:initComponents

    private void jComboBox1ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // Add your handling code here:
        event.setCatchType (jComboBox2.getSelectedIndex () + 1);
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jTextField1FocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusLost
        // Add your handling code here:
        event.setClassName (jTextField1.getText ());
    }//GEN-LAST:event_jTextField1FocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables
}
/*
 * Log
 *  4    Gandalf   1.3         11/5/99  Jan Jancura     Add Breakpoint Dialog 
 *       design updated
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         9/28/99  Jan Jancura     
 *  1    Gandalf   1.0         9/3/99   Jan Jancura     
 * $
 */
