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

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

import java.beans.*;
import java.util.*;
import javax.swing.event.*;

/** The ConnectionWizard is a dialog which allows to enter the data for connecting two
* components on a form.
*
* @author  Ian Formanek
* @version 1.00, Aug 29, 1998
*/
public class ConnectionWizard2 extends javax.swing.JDialog {

    public static final int CANCEL = 0;
    public static final int NEXT = 1;
    public static final int PREVIOUS = 2;

    public static final int METHOD_TYPE = 0;
    public static final int PROPERTY_TYPE = 1;
    public static final int CODE_TYPE = 2;

    static final long serialVersionUID =6347152949164963416L;
    /** Initializes the Form */
    public ConnectionWizard2 (RADComponent target) {
        super (TopManager.getDefault ().getWindowManager ().getMainWindow (), true);
        targetComponent = target;

        initComponents ();

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

        javax.swing.ButtonGroup gr = new javax.swing.ButtonGroup ();
        gr.add (propertyButton);
        gr.add (methodButton);
        gr.add (codeButton);

        targetComponentName.setText (target.getName ());

        // populate property list
        actionList.setSelectionMode (javax.swing.ListSelectionModel.SINGLE_SELECTION);
        actionList.addListSelectionListener (new ListSelectionListener () {
                                                 public void valueChanged (ListSelectionEvent evt) {
                                                     updateButtons ();
                                                 }
                                             }
                                            );
        updateActionList ();

        // localization code
        targetPanel.setBorder (new javax.swing.border.CompoundBorder (
                                   new javax.swing.border.TitledBorder (
                                       new javax.swing.border.EtchedBorder (), FormEditor.getFormBundle ().getString ("CTL_CW_ConnectionTarget")), // "Connection Target"
                                   new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));
        targetNameLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_TargetComponent"));                          // "Target Component:"
        propertyButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_SetProperty"));                               // "Set Property"
        methodButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_MethodCall"));                                  // "Method Call"
        codeButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_UserCode"));                                      // "User Code"

        previousButton.setText (FormEditor.getFormBundle ().getString ("CTL_PREVIOUS")); // "< Previous"
        cancelButton.setText (FormEditor.getFormBundle ().getString ("CTL_CANCEL")); //"Cancel"

        insidePanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(8, 8, 3, 8)));
        targetNamePanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 0, 5, 0)));
        buttonsPanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 5, 5, 5)));

        updateButtons ();

        pack ();
        org.openidex.util.Utilities2.centerWindow (this);

        HelpCtx.setHelpIDString (getRootPane (), ConnectionWizard2.class.getName ());
    }

    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension pref = super.getPreferredSize ();
        return new java.awt.Dimension (Math.max (pref.width, 350), Math.max (pref.height, 500));
    }

    int getReturnStatus () {
        return returnStatus;
    }

    int getActionType () {
        if (methodButton.isSelected ())
            return METHOD_TYPE;
        else if (propertyButton.isSelected ())
            return PROPERTY_TYPE;
        else
            return CODE_TYPE;
    }

    MethodDescriptor getSelectedMethod () {
        if (!methodButton.isSelected ()) return null;
        if (actionList.getSelectedIndex () == -1) return null;
        return methodDescriptors [actionList.getSelectedIndex ()];
    }

    PropertyDescriptor getSelectedProperty () {
        if (!propertyButton.isSelected ()) return null;
        if (actionList.getSelectedIndex () == -1) return null;
        return propDescriptors [actionList.getSelectedIndex ()];
    }

    // ----------------------------------------------------------------------------
    // private methods

    private void updateButtons () {
        // second enable/disable next buton according to whether all
        // required fields are filled
        if (codeButton.isSelected () || (actionList.getSelectedIndex () != -1)) {
            nextButton.setEnabled (true);

            if (propertyButton.isSelected () ||
                    (methodButton.isSelected () &&
                     (getSelectedMethod () != null) &&
                     (getSelectedMethod ().getMethod ().getParameterTypes ().length > 0))) {
                setTitle (FormEditor.getFormBundle ().getString ("CTL_CW_Step2_Title"));   // "Connection Wizard - Step 2 of 3"
                nextButton.setText (FormEditor.getFormBundle ().getString ("CTL_NEXT")); // "Next >"
            } else {
                setTitle (FormEditor.getFormBundle ().getString ("CTL_CW_Step2b_Title"));  // "Connection Wizard - Step 2 of 2"
                nextButton.setText (FormEditor.getFormBundle ().getString ("CTL_FINISH")); // "Finish"
            }
        } else {
            nextButton.setEnabled (false);
            nextButton.setText (FormEditor.getFormBundle ().getString ("CTL_NEXT")); // "Next >"
            setTitle (FormEditor.getFormBundle ().getString ("CTL_CW_Step2_Title"));     // "Connection Wizard - Step 2 of 3"
        }

    }

    private void updateActionList () {
        if (codeButton.isSelected ()) {
            actionList.setListData (new String [] {
                                        FormEditor.getFormBundle ().getString ("CTL_CW_UserCodeText1"),    // "After clicking Finish, go to the editor to enter the",
                                        FormEditor.getFormBundle ().getString ("CTL_CW_UserCodeText2") }); // "custom code for handling the event.";
            actionList.setEnabled (false);
        } else if (propertyButton.isSelected ()) {
            // properties list
            actionList.setEnabled (true);
            if (propertyListData == null) {
                BeanInfo targetBeanInfo = targetComponent.getBeanInfo ();
                PropertyDescriptor[] descs = targetBeanInfo.getPropertyDescriptors ();

                // filter out read-only properties // [FUTURE: provide also indexed properties]
                ArrayList list = new ArrayList ();
                for (int i = 0; i < descs.length; i++) {
                    if (descs[i].getWriteMethod () != null) {
                        list.add (descs[i]);
                    }
                }

                // sort the properties by name
                Collections.sort (list, new Comparator () {
                                      public int compare(Object o1, Object o2) {
                                          return ((PropertyDescriptor)o1).getName ().compareTo (((PropertyDescriptor)o2).getName ());
                                      }
                                  }
                                 );

                propDescriptors = new PropertyDescriptor [list.size ()];
                list.toArray (propDescriptors);

                propertyListData = new String [propDescriptors.length];
                for (int i = 0; i < propDescriptors.length; i++) {
                    propertyListData [i] = propDescriptors [i].getName ();
                }
            }
            actionList.setListData (propertyListData);
        } else {
            // methods list
            actionList.setEnabled (true);
            if (methodListData == null) {
                BeanInfo targetBeanInfo = targetComponent.getBeanInfo ();
                methodDescriptors = targetBeanInfo.getMethodDescriptors ();
                ArrayList list = new ArrayList ();
                for (int i = 0; i < methodDescriptors.length; i++) {
                    list.add (methodDescriptors[i]);
                }

                // sort the methods by name
                Collections.sort (list, new Comparator () {
                                      public int compare(Object o1, Object o2) {
                                          return ((MethodDescriptor)o1).getName ().compareTo (((MethodDescriptor)o2).getName ());
                                      }
                                  }
                                 );
                // copy it back to the array as it is used later
                list.toArray (methodDescriptors);

                methodListData = new String [list.size ()];
                int i = 0;
                for (Iterator it = list.iterator (); it.hasNext (); ) {
                    methodListData [i++] = getMethodName ((MethodDescriptor)it.next ());
                }
            }
            actionList.setListData (methodListData);
        }
        actionList.revalidate ();
        actionList.repaint ();
    }

    private static String getMethodName (MethodDescriptor desc) {
        StringBuffer sb = new StringBuffer (desc.getName ());
        Class[] params = desc.getMethod ().getParameterTypes ();
        if ((params == null) || (params.length == 0)) {
            sb.append (" ()"); // NOI18N
        } else {
            for (int i = 0; i < params.length; i++) {
                if (i == 0) sb.append (" ("); // NOI18N
                else sb.append (", "); // NOI18N
                sb.append (Utilities.getShortClassName (params[i]));
            }
            sb.append (")"); // NOI18N
        }

        return sb.toString ();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        insidePanel = new javax.swing.JPanel ();
        targetPanel = new javax.swing.JPanel ();
        targerInfoPanel = new javax.swing.JPanel ();
        targetNamePanel = new javax.swing.JPanel ();
        targetNameLabel = new javax.swing.JLabel ();
        targetComponentName = new javax.swing.JLabel ();
        actionTypePanel = new javax.swing.JPanel ();
        propertyButton = new javax.swing.JRadioButton ();
        methodButton = new javax.swing.JRadioButton ();
        codeButton = new javax.swing.JRadioButton ();
        actionPanel = new javax.swing.JScrollPane ();
        actionList = new javax.swing.JList ();
        buttonsPanel = new javax.swing.JPanel ();
        leftButtonsPanel = new javax.swing.JPanel ();
        rightButtonsPanel = new javax.swing.JPanel ();
        previousButton = new javax.swing.JButton ();
        nextButton = new javax.swing.JButton ();
        cancelButton = new javax.swing.JButton ();

        insidePanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        targetPanel.setLayout (new java.awt.BorderLayout ());

        targerInfoPanel.setLayout (new java.awt.GridLayout (2, 1));

        targetNamePanel.setLayout (new java.awt.FlowLayout (0, 5, 0));

        targetNameLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_TargetComponent"));

        targetNamePanel.add (targetNameLabel);


        targetNamePanel.add (targetComponentName);

        targerInfoPanel.add (targetNamePanel);

        actionTypePanel.setLayout (new java.awt.FlowLayout (0, 8, 0));

        propertyButton.setSelected (true);
        propertyButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_SetProperty"));
        propertyButton.addActionListener (new java.awt.event.ActionListener () {
                                              public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                  actionTypeButtonPressed (evt);
                                              }
                                          }
                                         );

        actionTypePanel.add (propertyButton);

        methodButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_MethodCall"));
        methodButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                actionTypeButtonPressed (evt);
                                            }
                                        }
                                       );

        actionTypePanel.add (methodButton);

        codeButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_UserCode"));
        codeButton.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              actionTypeButtonPressed (evt);
                                          }
                                      }
                                     );

        actionTypePanel.add (codeButton);

        targerInfoPanel.add (actionTypePanel);

        targetPanel.add (targerInfoPanel, java.awt.BorderLayout.NORTH);



        actionPanel.setViewportView (actionList);

        targetPanel.add (actionPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 0.5;
        insidePanel.add (targetPanel, gridBagConstraints1);


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

    private void actionTypeButtonPressed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionTypeButtonPressed
        updateActionList ();
        updateButtons ();
    }//GEN-LAST:event_actionTypeButtonPressed


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
    private javax.swing.JPanel targetPanel;
    private javax.swing.JPanel targerInfoPanel;
    private javax.swing.JPanel targetNamePanel;
    private javax.swing.JLabel targetNameLabel;
    private javax.swing.JLabel targetComponentName;
    private javax.swing.JPanel actionTypePanel;
    private javax.swing.JRadioButton propertyButton;
    private javax.swing.JRadioButton methodButton;
    private javax.swing.JRadioButton codeButton;
    private javax.swing.JScrollPane actionPanel;
    private javax.swing.JList actionList;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel leftButtonsPanel;
    private javax.swing.JPanel rightButtonsPanel;
    private javax.swing.JButton previousButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton cancelButton;
    // End of variables declaration//GEN-END:variables

    private Object[] propertyListData;
    private Object[] methodListData;
    private MethodDescriptor[] methodDescriptors;
    private PropertyDescriptor[] propDescriptors;

    private RADComponent targetComponent;
    private int returnStatus = CANCEL;
}

/*
 * Log
 *  14   Gandalf   1.13        1/12/00  Pavel Buzek     I18N
 *  13   Gandalf   1.12        1/5/00   Ian Formanek    NOI18N
 *  12   Gandalf   1.11        11/27/99 Patrik Knakal   
 *  11   Gandalf   1.10        11/25/99 Ian Formanek    Uses Utilities module
 *  10   Gandalf   1.9         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         7/31/99  Ian Formanek    changed comment
 *  7    Gandalf   1.6         7/31/99  Ian Formanek    localization pendings
 *  6    Gandalf   1.5         7/27/99  Ian Formanek    Fixed bug 2179 - 
 *       Problems in Connection wizard - step 2 during setting connection 
 *       target.
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         6/1/99   Ian Formanek    Fixed removed event 
 *       handlers
 *  3    Gandalf   1.2         5/31/99  Ian Formanek    Updated to X2 form 
 *       format
 *  2    Gandalf   1.1         5/17/99  Ian Formanek    Fixed bug 1812 - 
 *       Connection Wizard - Step 2: if you click on User code radio button (and
 *       then click on e.g. Method Call), from now all items are disabled 
 *       (gray).  Fixed bug 1810 - Connection Wizard: the items in list should 
 *       be alphabetically sorted.
 *  1    Gandalf   1.0         5/13/99  Ian Formanek    
 * $
 */

