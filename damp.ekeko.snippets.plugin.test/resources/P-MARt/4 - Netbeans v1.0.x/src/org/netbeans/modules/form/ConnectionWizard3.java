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

package org.netbeans.modules.form;

import java.lang.reflect.Method;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/** The ConnectionWizard is a dialog which allows to enter the data for connecting two
* components on a form.
*
* @author  Ian Formanek
* @version 1.00, Aug 29, 1998
*/
public class ConnectionWizard3 extends javax.swing.JDialog {

    public static final int CANCEL = 0;
    public static final int NEXT = 1;
    public static final int PREVIOUS = 2;

    static final long serialVersionUID =1673915932424583702L;
    /** Initializes the Form */
    public ConnectionWizard3 (FormManager2 manager, Method m, RADComponent sourceComponent) {
        super (TopManager.getDefault ().getWindowManager ().getMainWindow (), true);

        this.manager = manager;
        this.sourceComponent = sourceComponent;

        setDefaultCloseOperation (javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener (new java.awt.event.WindowAdapter () {
                               public void windowClosing (java.awt.event.WindowEvent evt) {
                                   cancelDialog ();
                               }
                           }
                          );

        // attach cancel also to Escape key
        getRootPane().registerKeyboardAction(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancelDialog ();
                }
            },
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, true),
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        parameters = m.getParameterTypes ();
        pickers = new ParametersPicker [parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            pickers[i] = new ParametersPicker (manager, sourceComponent, parameters[i]);
            pickers[i].addChangeListener (new ChangeListener () {
                                              public void stateChanged (ChangeEvent evt) {
                                                  updateButtons ();
                                              }
                                          }
                                         );
        }

        initComponents ();

        for (int i = 0; i < parameters.length; i++) {
            parameterTabs.addTab (
                Utilities.getShortClassName (parameters[i]),
                null,
                pickers[i],
                parameters[i].getName ()
            );
        }

        setTitle (FormEditor.getFormBundle ().getString ("CTL_CW_Step3_Title"));         // "Connection Wizard - Step 3 of 3"
        previewLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_Preview")); // "Preview"
        previewLabel.setBorder (new javax.swing.border.CompoundBorder (
                                    new javax.swing.border.TitledBorder (
                                        new javax.swing.border.EtchedBorder (), " " + FormEditor.getFormBundle ().getString ("CTL_CW_GeneratedPreview") +" "), // "Generated Parameters Preview:"
                                    new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));
        previousButton.setText (FormEditor.getFormBundle ().getString ("CTL_PREVIOUS")); // "< Previous"
        nextButton.setText (FormEditor.getFormBundle ().getString ("CTL_FINISH")); // "Finish"
        cancelButton.setText (FormEditor.getFormBundle ().getString ("CTL_CANCEL")); //"Cancel"

        insidePanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(8, 8, 3, 8)));
        buttonsPanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 5, 5, 5)));

        updateButtons ();

        pack ();
        org.openidex.util.Utilities2.centerWindow (this);

        HelpCtx.setHelpIDString (getRootPane (), ConnectionWizard3.class.getName ());
    }

    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension pref = super.getPreferredSize ();
        return new java.awt.Dimension (Math.max (pref.width, 350), Math.max (pref.height, 500));
    }

    int getReturnStatus () {
        return returnStatus;
    }

    String getParametersText () {
        StringBuffer buf = new StringBuffer ();
        for (int i = 0; i < pickers.length; i++) {
            buf.append (pickers[i].getText ());
            if (i != pickers.length - 1)
                buf.append (", "); // NOI18N
        }
        return buf.toString ();
    }

    Object[] getParameters () {
        try {
            Object values[] = new Object [pickers.length];
            for (int i = 0; i < pickers.length; i++) {
                values[i] = pickers[i].getPropertyValue ();
            }
            return values;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ----------------------------------------------------------------------------
    // private methods

    private String getPreviewText () {
        StringBuffer buf = new StringBuffer ();
        for (int i = 0; i < pickers.length; i++) {
            buf.append (pickers[i].getPreviewText ());
            if (i != pickers.length - 1)
                buf.append (", "); // NOI18N
        }
        return buf.toString ();
    }

    private void updateButtons () {
        boolean flag = true;
        for (int i = 0; i < pickers.length; i++)
            flag = flag && pickers[i].isFilled ();
        nextButton.setEnabled (flag);
        previewLabel.setText (getPreviewText ());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        insidePanel = new javax.swing.JPanel ();
        parameterTabs = new javax.swing.JTabbedPane ();
        previewLabel = new javax.swing.JLabel ();
        buttonsPanel = new javax.swing.JPanel ();
        leftButtonsPanel = new javax.swing.JPanel ();
        rightButtonsPanel = new javax.swing.JPanel ();
        previousButton = new javax.swing.JButton ();
        nextButton = new javax.swing.JButton ();
        cancelButton = new javax.swing.JButton ();

        insidePanel.setLayout (new java.awt.BorderLayout (0, 5));

        parameterTabs.addChangeListener (new javax.swing.event.ChangeListener () {
                                             public void stateChanged (javax.swing.event.ChangeEvent evt) {
                                                 updatePreview (evt);
                                             }
                                         }
                                        );

        insidePanel.add (parameterTabs, java.awt.BorderLayout.CENTER);

        previewLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_Preview"));

        insidePanel.add (previewLabel, java.awt.BorderLayout.SOUTH);


        getContentPane ().add (insidePanel, java.awt.BorderLayout.CENTER);

        buttonsPanel.setLayout (new java.awt.BorderLayout ());

        leftButtonsPanel.setLayout (new java.awt.FlowLayout (0, 5, 5));

        buttonsPanel.add (leftButtonsPanel, java.awt.BorderLayout.WEST);

        rightButtonsPanel.setLayout (new java.awt.FlowLayout (2, 5, 5));

        previousButton.setText (FormEditor.getFormBundle ().getString ("CTL_PREVIOUS"));
        previousButton.addActionListener (new java.awt.event.ActionListener () {
                                              public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                  previousButtonActionPerformed (evt);
                                              }
                                          }
                                         );

        rightButtonsPanel.add (previousButton);

        nextButton.setText (FormEditor.getFormBundle ().getString ("CTL_FINISH"));
        nextButton.setEnabled (false);
        nextButton.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              nextButtonActionPerformed (evt);
                                          }
                                      }
                                     );

        rightButtonsPanel.add (nextButton);

        cancelButton.setText (FormEditor.getFormBundle ().getString ("CTL_CANCEL"));
        cancelButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                cancelButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        rightButtonsPanel.add (cancelButton);

        buttonsPanel.add (rightButtonsPanel, java.awt.BorderLayout.EAST);


        getContentPane ().add (buttonsPanel, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void updatePreview (javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_updatePreview
        updateButtons ();
    }//GEN-LAST:event_updatePreview


    private void nextButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        returnStatus = NEXT;
        setVisible (false);
    }//GEN-LAST:event_nextButtonActionPerformed

    private void previousButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        returnStatus = PREVIOUS;
        setVisible (false);
    }//GEN-LAST:event_previousButtonActionPerformed

    private void cancelButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelDialog ();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:closeDialog
        cancelDialog ();
    }//GEN-LAST:closeDialog

    private void cancelDialog () {
        returnStatus = CANCEL;
        setVisible (false);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel insidePanel;
    private javax.swing.JTabbedPane parameterTabs;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel leftButtonsPanel;
    private javax.swing.JPanel rightButtonsPanel;
    private javax.swing.JButton previousButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton cancelButton;
    // End of variables declaration//GEN-END:variables

    private Class[] parameters;
    private ParametersPicker[] pickers;
    private FormManager2 manager;
    private RADComponent sourceComponent;
    private int returnStatus = CANCEL;
}

/*
 * Log
 *  13   Gandalf   1.12        1/12/00  Pavel Buzek     I18N
 *  12   Gandalf   1.11        1/8/00   Pavel Buzek     #2574
 *  11   Gandalf   1.10        1/5/00   Ian Formanek    NOI18N
 *  10   Gandalf   1.9         11/27/99 Patrik Knakal   
 *  9    Gandalf   1.8         11/25/99 Ian Formanek    Uses Utilities module
 *  8    Gandalf   1.7         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         7/31/99  Ian Formanek    localization pendings
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         6/1/99   Ian Formanek    Fixed removed event 
 *       handlers
 *  3    Gandalf   1.2         5/31/99  Ian Formanek    Updated to X2 form 
 *       format
 *  2    Gandalf   1.1         5/15/99  Ian Formanek    
 *  1    Gandalf   1.0         5/13/99  Ian Formanek    
 * $
 */

