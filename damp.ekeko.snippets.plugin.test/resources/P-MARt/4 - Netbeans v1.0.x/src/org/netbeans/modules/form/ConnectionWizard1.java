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

import java.beans.*;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/* Form Note:
* the source in guarded section has been changes without updating the .form file:
* - usage of advancedButton was commented out
*/

/** The ConnectionWizard is a dialog which allows to enter the data for connecting two
* components on a form.
*
* @author  Ian Formanek
*/
public class ConnectionWizard1 extends javax.swing.JDialog {

    public static final int CANCEL = 0;
    public static final int NEXT = 1;

    static final long serialVersionUID =7975448220626617288L;
    /** Initializes the Form */
    public ConnectionWizard1(RADComponent source) {
        super (TopManager.getDefault ().getWindowManager ().getMainWindow (), true);
        sourceComponent = source;

        initComponents ();

        eventNameCombo.addActionListener(new java.awt.event.ActionListener () {
                                             public void actionPerformed (java.awt.event.ActionEvent e) {
                                                 updateButtons ();
                                             }
                                         }
                                        );
        eventNameCombo.getEditor().addActionListener(new java.awt.event.ActionListener () {
                    public void actionPerformed (java.awt.event.ActionEvent e) {
                        updateButtons ();
                    }
                }
                                                    );

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

        sourceComponentName.setText (source.getName ());

        // populate event tree

        final Vector eventNodes = new Vector ();
        TreeNode rootNode = new TreeNode () {
                                public TreeNode getChildAt(int childIndex) {
                                    return (TreeNode) eventNodes.elementAt (childIndex);
                                }
                                public int getChildCount() {
                                    return eventNodes.size ();
                                }
                                public TreeNode getParent() {
                                    return null;
                                }
                                public int getIndex(TreeNode node) {
                                    return eventNodes.indexOf (node);
                                }
                                public boolean getAllowsChildren() {
                                    return true;
                                }
                                public boolean isLeaf() {
                                    return false;
                                }
                                public Enumeration children() {
                                    return eventNodes.elements ();
                                }
                            };

        EventsList.EventSet[] setHandlers = source.getEventsList ().getEventSets ();
        for (int i = 0; i < setHandlers.length; i++) {
            EventsList.Event[] events = setHandlers[i].getEvents();
            Vector eventsVector = new Vector ();
            EventSetNode esn = new EventSetNode (rootNode, setHandlers[i], eventsVector);
            for (int j = 0; j < events.length; j++) {
                eventsVector.addElement (new EventNode (esn, events[j]));
            }
            eventNodes.addElement (esn);
        }

        DefaultTreeSelectionModel treeSelectionModel = new DefaultTreeSelectionModel ();
        treeSelectionModel.addTreeSelectionListener (
            new TreeSelectionListener () {
                public void valueChanged (TreeSelectionEvent evt) {
                    TreePath[] paths = eventSelectTree.getSelectionPaths ();
                    if ((paths != null) && (paths.length == 1)) {
                        TreeNode node = (TreeNode) paths[0].getLastPathComponent ();
                        if ((node != null) && (node instanceof EventNode)) {
                            setSelectedEvent (((EventNode)node).getEvent ());
                            return;
                        }
                    }
                    setSelectedEvent (null);
                }
            }
        );
        treeSelectionModel.setSelectionMode (DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
        eventSelectTree.setModel (new DefaultTreeModel (rootNode));
        eventSelectTree.setSelectionModel (treeSelectionModel);

        // localization code
        setTitle (FormEditor.getFormBundle ().getString ("CTL_CW_Step1_Title"));                                               //"Connection Wizard - Step 1 of 3");
        insidePanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(8, 8, 3, 8)));
        buttonsPanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 5, 5, 5)));
        sourcePanel.setBorder (new javax.swing.border.CompoundBorder (
                                   new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 0, 8, 0)),
                                   new javax.swing.border.CompoundBorder (
                                       new javax.swing.border.TitledBorder (
                                           new javax.swing.border.EtchedBorder (), FormEditor.getFormBundle ().getString ("CTL_CW_ConnectionSource")),   // "Connection Source"),
                                       new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5)))));
        sourceNameLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_SourceComponent"));                            // "Source Component:");
        eventSelectLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_Event"));                                     // "Event:");
        eventHandlerPanel.setBorder (new javax.swing.border.CompoundBorder (
                                         new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 0, 8, 0)),
                                         new javax.swing.border.CompoundBorder (
                                             new javax.swing.border.TitledBorder (
                                                 new javax.swing.border.EtchedBorder (), FormEditor.getFormBundle ().getString ("CTL_CW_EventHandlerMethod")), //"Event Handler Method"),
                                             new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5)))));
        eventNameLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_MethodName"));                                  // "Method Name:");
        //    advancedButton.setText (FormEditor.getFormBundle ().getString ("CTL_CW_Advanced"));                                    // "Advanced");
        nextButton.setText (FormEditor.getFormBundle ().getString ("CTL_NEXT")); // "Next >"
        cancelButton.setText (FormEditor.getFormBundle ().getString ("CTL_CANCEL")); //"Cancel"

        updateButtons ();

        pack ();
        org.openidex.util.Utilities2.centerWindow (this);

        HelpCtx.setHelpIDString (getRootPane (), ConnectionWizard1.class.getName ());
    }

    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension pref = super.getPreferredSize ();
        return new java.awt.Dimension (Math.max (pref.width, 350), Math.max (pref.height, 500));
    }

    String getEventName () {
        return (String) eventNameCombo.getEditor().getItem();
    }

    EventsList.Event getSelectedEvent () {
        return selectedEvent;
    }

    int getReturnStatus () {
        return returnStatus;
    }

    // ----------------------------------------------------------------------------
    // private methods

    private void updateButtons () {
        // second enable/disable next buton according to whether all
        // required fields are filled
        String text = (String) eventNameCombo.getEditor ().getItem();
        if ((getSelectedEvent () != null) &&
                (
                    (
                        (!"".equals (text)) && // NOI18N
                        (org.openide.util.Utilities.isJavaIdentifier (text))
                    ) ||
                    (getSelectedEvent ().getHandlers ().size () > 0))) {
            nextButton.setEnabled (true);
        } else {
            nextButton.setEnabled (false);
        }
    }

    private void setSelectedEvent (EventsList.Event event) {
        selectedEvent = event;
        if (selectedEvent != null) {
            // restore non-modified if the input line is empty
            if ("".equals ((String) eventNameCombo.getEditor().getItem()) && eventNameModified) // NOI18N
                eventNameModified = false;

            if (selectedEvent.getHandlers ().size () > 0) {
                if (eventNameModified)
                    storedEventName = (String) eventNameCombo.getEditor().getItem();

                eventNameCombo.removeAllItems();
                for (java.util.Iterator iter = selectedEvent.getHandlers ().iterator (); iter.hasNext();) {
                    EventsManager.EventHandler eh = (EventsManager.EventHandler) iter.next();
                    eventNameCombo.addItem(eh.getName ());
                }
                eventNameCombo.setEnabled(true); // can add events
            } else {
                eventNameCombo.setEnabled(true);
                if (storedEventName != null) {
                    eventNameCombo.removeAllItems();
                    eventNameCombo.addItem (storedEventName);
                    storedEventName = null;
                    eventNameModified = true;
                } else {
                    if (!eventNameModified) {
                        eventNameCombo.removeAllItems();
                        eventNameCombo.addItem (FormUtils.getDefaultEventName (sourceComponent, selectedEvent.getListenerMethod ()));
                    }
                }
            }
        }
        updateButtons ();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        insidePanel = new javax.swing.JPanel ();
        sourcePanel = new javax.swing.JPanel ();
        sourceInfoPanel = new javax.swing.JPanel ();
        sourceNamePanel = new javax.swing.JPanel ();
        sourceNameLabel = new javax.swing.JLabel ();
        sourceComponentName = new javax.swing.JLabel ();
        eventSelectLabelPanel = new javax.swing.JPanel ();
        eventSelectLabel = new javax.swing.JLabel ();
        eventSelectScroll = new javax.swing.JScrollPane ();
        eventSelectTree = new javax.swing.JTree ();
        eventHandlerPanel = new javax.swing.JPanel ();
        eventNameLabel = new javax.swing.JLabel ();
        eventNameCombo = new javax.swing.JComboBox ();
        buttonsPanel = new javax.swing.JPanel ();
        rightButtonsPanel = new javax.swing.JPanel ();
        nextButton = new javax.swing.JButton ();
        cancelButton = new javax.swing.JButton ();

        insidePanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        sourcePanel.setLayout (new java.awt.BorderLayout ());

        sourceInfoPanel.setLayout (new java.awt.GridLayout (2, 1));

        sourceNamePanel.setLayout (new java.awt.FlowLayout (0, 5, 0));

        sourceNameLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_SourceComponent"));

        sourceNamePanel.add (sourceNameLabel);


        sourceNamePanel.add (sourceComponentName);

        sourceInfoPanel.add (sourceNamePanel);

        eventSelectLabelPanel.setLayout (new java.awt.FlowLayout (0, 5, 5));

        eventSelectLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_Event"));

        eventSelectLabelPanel.add (eventSelectLabel);

        sourceInfoPanel.add (eventSelectLabelPanel);

        sourcePanel.add (sourceInfoPanel, java.awt.BorderLayout.NORTH);

        eventSelectScroll.setMaximumSize (new java.awt.Dimension(32767, 100));

        eventSelectTree.setShowsRootHandles (true);
        eventSelectTree.setRootVisible (false);

        eventSelectScroll.setViewportView (eventSelectTree);

        sourcePanel.add (eventSelectScroll, java.awt.BorderLayout.CENTER);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 0.5;
        insidePanel.add (sourcePanel, gridBagConstraints1);

        eventHandlerPanel.setLayout (new java.awt.BorderLayout (8, 0));

        eventNameLabel.setText (FormEditor.getFormBundle ().getString ("CTL_CW_MethodName"));

        eventHandlerPanel.add (eventNameLabel, java.awt.BorderLayout.WEST);

        eventNameCombo.setEditable (true);

        eventHandlerPanel.add (eventNameCombo, java.awt.BorderLayout.CENTER);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        insidePanel.add (eventHandlerPanel, gridBagConstraints1);


        getContentPane ().add (insidePanel, java.awt.BorderLayout.CENTER);

        buttonsPanel.setLayout (new java.awt.BorderLayout ());

        rightButtonsPanel.setLayout (new java.awt.FlowLayout (2, 5, 5));

        nextButton.setText (FormEditor.getFormBundle ().getString ("CTL_NEXT"));
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
        if (getSelectedEvent ().getHandlers ().size () > 0) {
            if (TopManager.getDefault().notify(
                        new NotifyDescriptor.Confirmation(FormEditor.getFormBundle().getString("MSG_RewritingEvent"),
                                                          NotifyDescriptor.OK_CANCEL_OPTION,
                                                          NotifyDescriptor.WARNING_MESSAGE)
                    ) == NotifyDescriptor.CANCEL_OPTION)
                return;
        }
        returnStatus = NEXT;
        setVisible (false);
    }//GEN-LAST:event_nextButtonActionPerformed

    private void cancelButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelDialog ();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void eventNamePressed (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_eventNamePressed
        eventNameModified = true;
    }//GEN-LAST:event_eventNamePressed


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
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JPanel sourceInfoPanel;
    private javax.swing.JPanel sourceNamePanel;
    private javax.swing.JLabel sourceNameLabel;
    private javax.swing.JLabel sourceComponentName;
    private javax.swing.JPanel eventSelectLabelPanel;
    private javax.swing.JLabel eventSelectLabel;
    private javax.swing.JScrollPane eventSelectScroll;
    private javax.swing.JTree eventSelectTree;
    private javax.swing.JPanel eventHandlerPanel;
    private javax.swing.JLabel eventNameLabel;
    private javax.swing.JComboBox eventNameCombo;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel rightButtonsPanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton cancelButton;
    // End of variables declaration//GEN-END:variables

    // ----------------------------------------------------------------------------
    // Innerclasses

    class EventSetNode implements TreeNode {
        private TreeNode parent;
        private EventsList.EventSet eventSet;
        private Vector subNodes;

        public EventSetNode (TreeNode parent, EventsList.EventSet eventSet, Vector subNodes) {
            this.parent = parent;
            this.eventSet = eventSet;
            this.subNodes = subNodes;
        }

        public TreeNode getChildAt(int childIndex) {
            return (TreeNode) subNodes.elementAt (childIndex);
        }
        public int getChildCount() {
            return subNodes.size ();
        }
        public TreeNode getParent() {
            return null;
        }
        public int getIndex(TreeNode node) {
            return subNodes.indexOf (node);
        }
        public boolean getAllowsChildren() {
            return true;
        }
        public boolean isLeaf() {
            return false;
        }
        public Enumeration children() {
            return subNodes.elements ();
        }
        public String toString () {
            return eventSet.getName ();
        }
    }

    class EventNode implements TreeNode {
        private TreeNode parent;
        private EventsList.Event event;
        public EventNode (TreeNode parent, EventsList.Event event) {
            this.parent = parent;
            this.event = event;
        }
        public TreeNode getChildAt(int childIndex) {
            return null;
        }
        public int getChildCount() {
            return 0;
        }
        public TreeNode getParent() {
            return parent;
        }
        public int getIndex(TreeNode node) {
            return -1;
        }
        public boolean getAllowsChildren() {
            return false;
        }
        public boolean isLeaf() {
            return true;
        }
        public Enumeration children() {
            return null;
        }
        public String toString () {
            if (event.getHandlers ().size () == 0) return event.getName ();
            if (event.getHandlers ().size () == 1) return event.getName () + " ["+((EventsManager.EventHandler) event.getHandlers ().get (0)).getName ()+"]"; // NOI18N
            return event.getName () + " [...]"; // NOI18N
        }
        EventsList.Event getEvent () {
            return event;
        }
    }

    private String storedEventName = null; // used to preserve modified event name
    private boolean eventNameModified = false;
    private EventsList.Event selectedEvent = null;

    private RADComponent sourceComponent;
    private int returnStatus = CANCEL;
}

/*
 * Log
 *  16   Gandalf   1.15        1/12/00  Pavel Buzek     I18N
 *  15   Gandalf   1.14        1/10/00  Ian Formanek    Finally removed Advanced
 *       button, form converted to XML
 *  14   Gandalf   1.13        1/5/00   Ian Formanek    NOI18N
 *  13   Gandalf   1.12        11/26/99 Pavel Buzek     
 *  12   Gandalf   1.11        11/25/99 Ian Formanek    Uses Utilities module
 *  11   Gandalf   1.10        11/25/99 Pavel Buzek     support for multiple 
 *       handlers for one event
 *  10   Gandalf   1.9         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         8/10/99  Ian Formanek    Generated Serial Version
 *       UID
 *  7    Gandalf   1.6         7/31/99  Ian Formanek    Fixed last change
 *  6    Gandalf   1.5         7/31/99  Ian Formanek    localization pendings
 *  5    Gandalf   1.4         7/11/99  Ian Formanek    Advanced button is not 
 *       displayed
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/1/99   Ian Formanek    Fixed removed event 
 *       handlers
 *  2    Gandalf   1.1         5/31/99  Ian Formanek    Updated to X2 form 
 *       format
 *  1    Gandalf   1.0         5/13/99  Ian Formanek    
 * $
 */

