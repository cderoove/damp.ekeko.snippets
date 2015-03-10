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

package org.netbeans.modules.beans;

import java.awt.Dialog;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import javax.swing.border.TitledBorder;

import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.src.Type;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

/** Customizer for new Property Pattern
 *
 * @author Petr Hrebejk
 */
public class PropertyPatternPanel extends javax.swing.JPanel
    implements java.awt.event.ActionListener {

    /** The resource bundle */
    private static final ResourceBundle bundle = NbBundle.getBundle( PropertyPatternPanel.class );
    /** Dialog for displaiyng this panel */
    private Dialog dialog = null;
    /** Group node under which the new pattern will below */
    private PatternGroupNode groupNode;
    /** Geneartion for interface/class */
    private boolean forInterface = false;
    /** Default types */
    private final String[] types = new String[] {
                                       "boolean", "char", "byte", "short", "int", // NOI18N
                                       "long", "float", "double", "String" // NOI18N
                                   };
    /** Human readable values of modes */
    private final String[] modes = new String[] {
                                       bundle.getString( "LAB_ReadWriteMODE" ),
                                       bundle.getString( "LAB_ReadOnlyMODE" ),
                                       bundle.getString( "LAB_WriteOnlyMODE" )
                                   };

    /** Generated UID */
    static final long serialVersionUID =4959196907494713555L;

    /** Initializes the Form */
    public PropertyPatternPanel() {
        initComponents ();


        // Customize type checkbox
        for ( int i = 0; i < types.length; i++ ) {
            typeComboBox.addItem( types[i] );
        }
        typeComboBox.setSelectedItem( "" ); // NOI18N

        // Customize mode checkbox
        for ( int i = 0; i < modes.length; i++ ) {
            modeComboBox.addItem( modes[i] );
        }
        modeComboBox.setSelectedItem( modes[0] );

        // i18n

        ((TitledBorder)propertyPanel.getBorder()).setTitle(
            bundle.getString( "CTL_PropertyPanel_propertyPanel" ) );
        ((TitledBorder)optionsPanel.getBorder()).setTitle(
            bundle.getString( "CTL_PropertyPanel_optionsPanel" ) );
        nameLabel.setText( bundle.getString( "CTL_PropertyPanel_nameLabel" ) );
        typeLabel.setText( bundle.getString( "CTL_PropertyPanel_typeLabel" ) );
        modeLabel.setText( bundle.getString( "CTL_PropertyPanel_modeLabel" ) );
        boundCheckBox.setText( bundle.getString( "CTL_PropertyPanel_boundCheckBox" ) );
        constrainedCheckBox.setText( bundle.getString( "CTL_PropertyPanel_constrainedCheckBox" ) );
        fieldCheckBox.setText( bundle.getString( "CTL_PropertyPanel_fieldCheckBox" ) );
        returnCheckBox.setText( bundle.getString( "CTL_PropertyPanel_returnCheckBox" ) );
        setCheckBox.setText( bundle.getString( "CTL_PropertyPanel_setCheckBox" ) );
        supportCheckBox.setText( bundle.getString( "CTL_PropertyPanel_supportCheckBox" ) );

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        mainPanel = new javax.swing.JPanel ();
        propertyPanel = new javax.swing.JPanel ();
        nameLabel = new javax.swing.JLabel ();
        nameTextField = new javax.swing.JTextField ();
        typeLabel = new javax.swing.JLabel ();
        typeComboBox = new javax.swing.JComboBox ();
        modeLabel = new javax.swing.JLabel ();
        modeComboBox = new javax.swing.JComboBox ();
        jPanel3 = new javax.swing.JPanel ();
        boundCheckBox = new javax.swing.JCheckBox ();
        jPanel4 = new javax.swing.JPanel ();
        constrainedCheckBox = new javax.swing.JCheckBox ();
        optionsPanel = new javax.swing.JPanel ();
        fieldCheckBox = new javax.swing.JCheckBox ();
        returnCheckBox = new javax.swing.JCheckBox ();
        setCheckBox = new javax.swing.JCheckBox ();
        supportCheckBox = new javax.swing.JCheckBox ();
        setLayout (new java.awt.BorderLayout ());

        mainPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        mainPanel.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));

        propertyPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints2;
        propertyPanel.setBorder (new javax.swing.border.TitledBorder(
                                     new javax.swing.border.EtchedBorder(java.awt.Color.white, new java.awt.Color (149, 142, 130)),
                                     "propertyPanel", 1, 2, new java.awt.Font ("Dialog", 0, 11), java.awt.Color.black)); // NOI18N

        nameLabel.setText ("nameLabel"); // NOI18N

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.insets = new java.awt.Insets (2, 6, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints2.weighty = 1.0;
        propertyPanel.add (nameLabel, gridBagConstraints2);


        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        propertyPanel.add (nameTextField, gridBagConstraints2);

        typeLabel.setText ("typeLabel"); // NOI18N

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.insets = new java.awt.Insets (2, 6, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints2.weighty = 1.0;
        propertyPanel.add (typeLabel, gridBagConstraints2);

        typeComboBox.setEditable (true);

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        propertyPanel.add (typeComboBox, gridBagConstraints2);

        modeLabel.setText ("modeLabel"); // NOI18N

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.insets = new java.awt.Insets (2, 6, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints2.weighty = 1.0;
        propertyPanel.add (modeLabel, gridBagConstraints2);

        modeComboBox.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                modeComboBoxActionPerformed (evt);
                                            }
                                        }
                                       );

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        propertyPanel.add (modeComboBox, gridBagConstraints2);


        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        propertyPanel.add (jPanel3, gridBagConstraints2);

        boundCheckBox.setText ("boundCheckBox"); // NOI18N
        boundCheckBox.addActionListener (new java.awt.event.ActionListener () {
                                             public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                 boundCheckBoxActionPerformed (evt);
                                             }
                                         }
                                        );

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        propertyPanel.add (boundCheckBox, gridBagConstraints2);


        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        propertyPanel.add (jPanel4, gridBagConstraints2);

        constrainedCheckBox.setText ("constrainedCheckBox"); // NOI18N
        constrainedCheckBox.addActionListener (new java.awt.event.ActionListener () {
                                                   public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                       constrainedCheckBoxActionPerformed (evt);
                                                   }
                                               }
                                              );

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        propertyPanel.add (constrainedCheckBox, gridBagConstraints2);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        mainPanel.add (propertyPanel, gridBagConstraints1);

        optionsPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints3;
        optionsPanel.setBorder (new javax.swing.border.TitledBorder(
                                    new javax.swing.border.EtchedBorder(java.awt.Color.white, new java.awt.Color (149, 142, 130)),
                                    "optionsPanel", 1, 2, new java.awt.Font ("Dialog", 0, 11), java.awt.Color.black)); // NOI18N

        fieldCheckBox.setText ("fieldCheckBox"); // NOI18N
        fieldCheckBox.addActionListener (new java.awt.event.ActionListener () {
                                             public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                 fieldCheckBoxActionPerformed (evt);
                                             }
                                         }
                                        );

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.insets = new java.awt.Insets (2, 4, 2, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        optionsPanel.add (fieldCheckBox, gridBagConstraints3);

        returnCheckBox.setText ("returnCheckBox"); // NOI18N
        returnCheckBox.setEnabled (false);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.insets = new java.awt.Insets (2, 4, 2, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        optionsPanel.add (returnCheckBox, gridBagConstraints3);

        setCheckBox.setText ("setCheckBox"); // NOI18N
        setCheckBox.setEnabled (false);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.insets = new java.awt.Insets (2, 4, 2, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        optionsPanel.add (setCheckBox, gridBagConstraints3);

        supportCheckBox.setText ("supportCheckBox"); // NOI18N
        supportCheckBox.setEnabled (false);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.insets = new java.awt.Insets (2, 4, 2, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        optionsPanel.add (supportCheckBox, gridBagConstraints3);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        mainPanel.add (optionsPanel, gridBagConstraints1);


        add (mainPanel, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void fieldCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldCheckBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_fieldCheckBoxActionPerformed

    private void constrainedCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_constrainedCheckBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_constrainedCheckBoxActionPerformed

    private void boundCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boundCheckBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_boundCheckBoxActionPerformed

    private void modeComboBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modeComboBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_modeComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel propertyPanel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel modeLabel;
    private javax.swing.JComboBox modeComboBox;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JCheckBox boundCheckBox;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JCheckBox constrainedCheckBox;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JCheckBox fieldCheckBox;
    private javax.swing.JCheckBox returnCheckBox;
    private javax.swing.JCheckBox setCheckBox;
    private javax.swing.JCheckBox supportCheckBox;
    // End of variables declaration//GEN-END:variables


    class Result {
        String name;
        String type;
        int mode = PropertyPattern.READ_WRITE;
        boolean bound = false;
        boolean constrained = false;
        boolean withField = false;
        boolean withReturn = false;
        boolean withSet = false;
        boolean withSupport = false;
    }

    PropertyPatternPanel.Result getResult( ) {
        Result result = new Result();

        result.name = nameTextField.getText();
        result.type = typeComboBox.getEditor().getItem().toString();
        if ( modeComboBox.getSelectedItem().toString().equals( modes[1] ) )
            result.mode = PropertyPattern.READ_ONLY;
        else if ( modeComboBox.getSelectedItem().toString().equals( modes[2] ) )
            result.mode = PropertyPattern.WRITE_ONLY;
        else
            result.mode = PropertyPattern.READ_WRITE;

        if ( boundCheckBox.isSelected() )
            result.bound = true;

        if ( constrainedCheckBox.isSelected() )
            result.constrained = true;

        if ( fieldCheckBox.isSelected() )
            result.withField = true;

        if ( returnCheckBox.isSelected() )
            result.withReturn = true;

        if ( setCheckBox.isSelected() )
            result.withSet = true;

        if ( supportCheckBox.isSelected() )
            result.withSupport = true;

        return result;
    }

    /** This method is called when ocuures the possibilty that any
    * xontrol should be enabled or disabled.
    */
    private void protectControls() {
        Result result = getResult();


        fieldCheckBox.setEnabled( !forInterface );

        returnCheckBox.setEnabled(
                  ( result.mode == PropertyPattern.READ_WRITE ||
                    result.mode == PropertyPattern.READ_ONLY ) &&
                  result.withField && !forInterface );

        setCheckBox.setEnabled(
            ( result.mode == PropertyPattern.READ_WRITE ||
              result.mode == PropertyPattern.WRITE_ONLY ) &&
            result.withField && !forInterface );

        supportCheckBox.setEnabled( (result.bound || result.constrained) && !forInterface );
    }



    void setDialog( Dialog dialog ) {
        this.dialog = dialog;
    }

    void setForInterface( boolean forInterface ) {
        this.forInterface = forInterface;
        protectControls();
    }

    void setGroupNode( PatternGroupNode groupNode ) {
        this.groupNode = groupNode;
    }

    public void actionPerformed( java.awt.event.ActionEvent e ) {
        if ( dialog != null ) {
            //System.out.println( e );

            //if ( e.getActionCommand().equals( "OK" ) ) { // NOI18N

            if ( e.getSource() == org.openide.DialogDescriptor.OK_OPTION ) {

                //Test wether the string is empty
                if ( typeComboBox.getEditor().getItem().toString().trim().length() <= 0) {
                    TopManager.getDefault().notify(
                        new NotifyDescriptor.Message(
                            bundle.getString("MSG_Not_Valid_Type"),
                            NotifyDescriptor.ERROR_MESSAGE) );
                    typeComboBox.requestFocus();
                    return;
                }


                if ( !Utilities.isJavaIdentifier( nameTextField.getText() ) ) {
                    TopManager.getDefault().notify(
                        new NotifyDescriptor.Message(
                            bundle.getString("MSG_Not_Valid_Identifier"),
                            NotifyDescriptor.ERROR_MESSAGE) );
                    nameTextField.requestFocus();
                    return;
                }

                // Test wheter property with this name already exists
                if ( groupNode.propertyExists( nameTextField.getText() ) ) {
                    String msg = MessageFormat.format( bundle.getString("MSG_Property_Exists"),
                                                       new Object[] { nameTextField.getText() } );
                    TopManager.getDefault().notify(
                        new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE) );

                    nameTextField.requestFocus();
                    return;
                }

                try {
                    Type.parse( typeComboBox.getEditor().getItem().toString() );
                }
                catch ( IllegalArgumentException ex ) {
                    TopManager.getDefault().notify(
                        new NotifyDescriptor.Message(
                            bundle.getString("MSG_Not_Valid_Type"),
                            NotifyDescriptor.ERROR_MESSAGE) );
                    typeComboBox.requestFocus();
                    return;
                }
                dialog.setVisible( false );
                dialog.dispose();
            }
            else if ( e.getSource() == org.openide.DialogDescriptor.CANCEL_OPTION ) {
                dialog.setVisible( false );
                dialog.dispose();
            }

        }
    }

}

/*
 * Log
 *  11   Gandalf   1.10        1/13/00  Petr Hrebejk    i18n mk3
 *  10   Gandalf   1.9         1/12/00  Petr Hrebejk    i18n  
 *  9    Gandalf   1.8         1/4/00   Petr Hrebejk    Various bugfixes - 5036,
 *       5044, 5045
 *  8    Gandalf   1.7         11/10/99 Petr Hrebejk    Canged to work with 
 *       DialogDescriptor.setClosingOptions()
 *  7    Gandalf   1.6         11/10/99 Petr Hrebejk    Resize behavior in 
 *       property panel fixed
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/13/99  Petr Hrebejk    Creating multiple 
 *       Properties/EventSet with the same name vorbiden. Forms made i18n
 *  4    Gandalf   1.3         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  3    Gandalf   1.2         7/21/99  Petr Hrebejk    Bug fixes interface 
 *       bodies, is for boolean etc
 *  2    Gandalf   1.1         7/9/99   Petr Hrebejk    Factory chaining fix
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $
 */
