/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager;

import java.io.*;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Component;
import java.text.MessageFormat;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.modules.jarpackager.options.JarPackagerOption;

/** Dialog for manipulating history of created archives.
 *
 * @author  Dafe Simonek
 */
public class HistoryPanel extends javax.swing.JPanel
    implements ActionListener {

    /** Creates new form HistoryPanel */
    public HistoryPanel (PackagingPanel packagingPanel) {
        this.packagingPanel = packagingPanel;
        initComponents();
        completeInitialization();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel ();
        removeButton = new javax.swing.JButton ();
        clearButton = new javax.swing.JButton ();
        jScrollPane1 = new javax.swing.JScrollPane ();
        historyList = new javax.swing.JList ();
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));

        jPanel1.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints2;

        removeButton.setToolTipText (java.util.ResourceBundle.getBundle("org/netbeans/modules/jarpackager/Bundle").getString("CTL_RemoveButtonTip"));
        removeButton.setText (java.util.ResourceBundle.getBundle("org/netbeans/modules/jarpackager/Bundle").getString("CTL_RemoveButton"));
        removeButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                removeButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (5, 0, 0, 8);
        jPanel1.add (removeButton, gridBagConstraints2);

        clearButton.setToolTipText (java.util.ResourceBundle.getBundle("org/netbeans/modules/jarpackager/Bundle").getString("CTL_ClearButtonTip"));
        clearButton.setText (java.util.ResourceBundle.getBundle("org/netbeans/modules/jarpackager/Bundle").getString("CTL_ClearButton"));
        clearButton.addActionListener (new java.awt.event.ActionListener () {
                                           public void actionPerformed (java.awt.event.ActionEvent evt) {
                                               clearButtonActionPerformed (evt);
                                           }
                                       }
                                      );

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.gridheight = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (8, 0, 8, 8);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel1.add (clearButton, gridBagConstraints2);


        historyList.addListSelectionListener (new javax.swing.event.ListSelectionListener () {
                                                  public void valueChanged (javax.swing.event.ListSelectionEvent evt) {
                                                      historyListValueChanged (evt);
                                                  }
                                              }
                                             );

        jScrollPane1.setViewportView (historyList);

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.gridheight = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.insets = new java.awt.Insets (5, 8, 8, 8);
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        jPanel1.add (jScrollPane1, gridBagConstraints2);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (jPanel1, gridBagConstraints1);

    }//GEN-END:initComponents

    /** Updates the state of buttons according to the current selection */
    private void historyListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_historyListValueChanged
        updateControlStates();
    }//GEN-LAST:event_historyListValueChanged

    /** clears all items from history data & history list */
    private void clearButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        historyData.clear();
    }//GEN-LAST:event_clearButtonActionPerformed

    /** removes currently selected items from the history */
    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        historyData.remove(historyList.getSelectedValues());
    }//GEN-LAST:event_removeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList historyList;
    // End of variables declaration//GEN-END:variables

    /** data model for the history management */
    private HistoryModel historyData;
    /** asociation with parent packaging panel */
    private PackagingPanel packagingPanel;

    /** Implementation of ActionListener interface. Called when
    * some action buttons (ok, cancel here) are pressed.
    */ 
    public void actionPerformed (ActionEvent evt) {
        if (NotifyDescriptor.OK_OPTION.equals(evt.getSource())) {
            boolean result = restoreArchive();
            if (!result) {
                return;
            }
        }
        packagingPanel.historyDialog.setVisible(false);
    }

    /** Initializes this dialog properly */
    private void completeInitialization () {
        // title of archives list
        jPanel1.setBorder (new javax.swing.border.TitledBorder(
                               new javax.swing.border.EtchedBorder(),
                               NbBundle.getBundle(HistoryModel.class).getString("CTL_Archives"))
                          );

        historyData = JarPackagerOption.singleton().historyData();
        historyList.setModel(historyData);
        updateControlStates();
        // testing
        JarContent jc = new JarContent();
        jc.filteredContent();
    }

    /** Updates the state of buttons according to the
    * current selection */
    private void updateControlStates () {
        int[] selItems = historyList.getSelectedIndices();
        removeButton.setEnabled(selItems.length > 0);
    }

    /** Restores currently selected archive */
    boolean restoreArchive () {
        String archivePath = (String)historyList.getSelectedValue();
        if (archivePath == null) {
            TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                               NbBundle.getBundle(HistoryPanel.class).getString("MSG_NoSelection"),
                                               NotifyDescriptor.ERROR_MESSAGE
                                           ));
            return false;
        }
        HistoryModel.HistoryEntry foundEntry =
            historyData.getEntry(archivePath);
        File contentFile = new File(foundEntry.contentPath);
        // return if content not found
        if (!contentFile.exists()) {
            TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                               MessageFormat.format(
                                                   NbBundle.getBundle(HistoryPanel.class).getString("FMT_NotExist"),
                                                   new Object[] { archivePath }
                                               ),
                                               NotifyDescriptor.ERROR_MESSAGE
                                           ));
            return false;
        }
        // read jar content, if it's possible
        JarContent jc = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(
                                        new BufferedInputStream(new FileInputStream(contentFile)));
            try {
                jc = new JarContent();
                jc.readContent(ois);
            } finally {
                ois.close();
            }
        } catch (IOException exc) {
            exc.printStackTrace();
            return false;
        } catch (ClassNotFoundException exc) {
            exc.printStackTrace();
            return false;
        }
        // set the content if everything goes well
        PackagingView.getPackagingView().setJarContent(jc);
        return true;
    }


}
/*
 * <<Log>>
 *  4    Gandalf   1.3         1/25/00  David Simonek   Various bugfixes and 
 *       i18n
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/13/99 David Simonek   various bugfixes 
 *       concerning primarily manifest
 *  1    Gandalf   1.0         10/4/99  David Simonek   
 * $
 */
