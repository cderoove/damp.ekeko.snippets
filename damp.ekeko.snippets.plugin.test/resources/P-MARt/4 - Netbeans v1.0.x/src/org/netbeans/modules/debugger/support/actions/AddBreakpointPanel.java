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

package org.netbeans.modules.debugger.support.actions;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.Component;
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.CoreBreakpoint.Action;
import org.netbeans.modules.debugger.support.CoreBreakpoint.Event;

/**
* Customizer panel of breakpoint.
*
* @author  Jan Jacura
*/
public class AddBreakpointPanel extends javax.swing.JPanel {

    private CoreBreakpoint breakpoint;
    private boolean doNotRefresh = false;

    /** Creates new form AddBreakpointPanel */
    public AddBreakpointPanel () {
        initComponents ();
        //    setPreferredSize (new Dimension (520, 400));
    }

    private void initComponents () {
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));

        jLabel1 = new javax.swing.JLabel ();
        jLabel1.setText (NbBundle.getBundle (AddBreakpointPanel.class).getString ("CTL_Breakpoint_type"));

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.insets = new java.awt.Insets (0, 10, 0, 0);
        add (jLabel1, gridBagConstraints1);

        cbEvents = new javax.swing.JComboBox ();
        cbEvents.addActionListener (new java.awt.event.ActionListener () {
                                        public void actionPerformed (java.awt.event.ActionEvent evt) {
                                            cbEventsActionPerformed (evt);
                                        }
                                    }
                                   );


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.insets = new java.awt.Insets (0, 10, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add (cbEvents, gridBagConstraints1);

        pEvent = new javax.swing.JPanel ();
        pEvent.setLayout (new java.awt.BorderLayout ());
        pEvent.setBorder (new javax.swing.border.CompoundBorder(
                              new javax.swing.border.TitledBorder(
                                  new javax.swing.border.EtchedBorder(), NbBundle.getBundle (AddBreakpointPanel.class).getString ("CTL_Settings")),
                              new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))));


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (pEvent, gridBagConstraints1);

        // ************************************

        try {
            AbstractDebugger d = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
            if (d.supportsExpressions ()) {

                jLabel2 = new javax.swing.JLabel ();
                jLabel2.setText (NbBundle.getBundle (AddBreakpointPanel.class).getString ("CTL_Condition_label"));

                gridBagConstraints1 = new java.awt.GridBagConstraints ();
                gridBagConstraints1.insets = new java.awt.Insets (10, 10, 0, 0);
                gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
                add (jLabel2, gridBagConstraints1);

                tfCondition = new javax.swing.JTextField ();
                tfCondition.addFocusListener (new java.awt.event.FocusAdapter () {
                                                  public void focusLost (java.awt.event.FocusEvent evt) {
                                                      breakpoint.setCondition (tfCondition.getText ());
                                                  }
                                              });

                gridBagConstraints1 = new java.awt.GridBagConstraints ();
                gridBagConstraints1.gridwidth = 0;
                gridBagConstraints1.insets = new java.awt.Insets (10, 0, 0, 10);
                gridBagConstraints1.weightx = 1.0;
                gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
                add (tfCondition, gridBagConstraints1);

                jLabel3 = new javax.swing.JLabel ();
                jLabel3.setText (NbBundle.getBundle (AddBreakpointPanel.class).getString ("CTL_Condition_hint"));

                gridBagConstraints1 = new java.awt.GridBagConstraints ();
                gridBagConstraints1.gridwidth = 0;
                gridBagConstraints1.insets = new java.awt.Insets (5, 10, 5, 0);
                gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
                add (jLabel3, gridBagConstraints1);
            }
        } catch (DebuggerNotFoundException ex) {
        }

        // ************************************

        pActionsOut = new javax.swing.JPanel ();
        pActionsOut.setLayout (new java.awt.BorderLayout ());
        pActionsOut.setBorder (new javax.swing.border.CompoundBorder(
                                   new javax.swing.border.TitledBorder(
                                       new javax.swing.border.EtchedBorder(), NbBundle.getBundle (AddBreakpointPanel.class).getString ("CTL_Actions")),
                                   new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))));

        pActions = new javax.swing.JPanel ();
        pActions.setLayout (new javax.swing.BoxLayout (pActions, 1));

        pActionsOut.add (pActions, "North"); // NOI18N


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.gridheight = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 3.0;
        add (pActionsOut, gridBagConstraints1);

    }

    private void cbEventsActionPerformed (java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        if (doNotRefresh) return;
        int j = cbEvents.getSelectedIndex ();
        if (j < 0) return;
        pEvent.removeAll ();
        pActions.removeAll ();
        setEvent (breakpoint.getBreakpointEvents () [j]);
    }

    void setInitialEvent (Event e) {
        Event[] ev = breakpoint.getBreakpointEvents ();
        int i, k = ev.length;
        for (i = 0; i < k; i++)
            if (ev [i] == e) {
                cbEvents.setSelectedItem (ev [i].getTypeDisplayName ());
            }
    }

    private void setEvent (Event e) {
        breakpoint.setEvent (e);
        try {
            AbstractDebugger d = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
            pEvent.add (breakpoint.getEvent (d).getCustomizer (), "Center"); // NOI18N
            Action[] a = breakpoint.getActions ();
            int i, k = a.length;
            for (i = 0; i < k; i++) {
                Component c = a [i].getCustomizer ();
                if (c != null) pActions.add (c);
            }
        } catch (DebuggerNotFoundException ex) {
        }
        revalidate ();
        Window w = SwingUtilities.windowForComponent (this);
        if (w == null) return;
        w.pack ();
    }

    public void setBreakpoint (CoreBreakpoint breakpoint) {
        this.breakpoint = breakpoint;
        if (tfCondition != null)
            tfCondition.setText (breakpoint.getCondition ());
        Event[] ev = breakpoint.getBreakpointEvents ();
        pEvent.removeAll ();
        pActions.removeAll ();
        if (cbEvents.getItemCount () > 0) cbEvents.removeAllItems ();
        int i, k = ev.length;
        doNotRefresh = true;
        for (i = 0; i < k; i++)
            cbEvents.addItem (ev [i].getTypeDisplayName ());
        doNotRefresh = false;
    }

    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox cbEvents;
    private javax.swing.JPanel pEvent;
    private javax.swing.JPanel pActionsOut;
    private javax.swing.JPanel pActions;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField tfCondition = null;

}
/*
 * Log
 *  11   Gandalf-post-FCS1.8.4.1     4/5/00   Daniel Prusa    bugfix for 
 *       NullPointerException
 *  10   Gandalf-post-FCS1.8.4.0     3/28/00  Daniel Prusa    
 *  9    Gandalf   1.8         1/13/00  Daniel Prusa    NOI18N
 *  8    Gandalf   1.7         11/29/99 Jan Jancura     
 *  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
 *  6    Gandalf   1.5         11/5/99  Jan Jancura     Add Breakpoint Dialog 
 *       design updated
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/18/99  Jan Jancura     Localization & Current 
 *       thread & Current session
 *  3    Gandalf   1.2         7/30/99  Jan Jancura     
 *  2    Gandalf   1.1         7/14/99  Jan Jancura     
 *  1    Gandalf   1.0         7/13/99  Jan Jancura     
 * $
 */
