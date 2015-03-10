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

package org.netbeans.modules.autoupdate;

import java.util.ResourceBundle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.border.TitledBorder;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.NbBundle;


/** Panel for displaying progress of update checking.
 * @author Petr Hrebejk
 */
public class UpdatePanel extends javax.swing.JPanel {

    private static final String EMPTY_STRING = ""; // NOI18N
    private static final ResourceBundle bundle = NbBundle.getBundle( UpdatePanel.class );

    private static final java.awt.Dimension PREFERRED_SIZE = new java.awt.Dimension( 600, 500 );

    private static final String SPACE = " "; //NOI18N

    /** Explorer tree */
    private ExplorerView explorerView;
    /** List model for listBox with selected modules */
    private DefaultListModel selectedListModel;
    /** Summation of lengths of all selected modules */
    private long totalSize;

    /** The collection of module updates */
    private Updates updates;

    private Wizard.Validator validator;

    static final long serialVersionUID =897622109141801200L;
    /** Creates new form UpdatePanel */
    public UpdatePanel( Wizard.Validator validator, int wizardType ) {
        initComponents ();

        this.validator = validator;

        // Add Explorer view

        explorerView = new ExplorerView();
        explorerView.requestFocus();

        explorerView.getExplorerManager().addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange( PropertyChangeEvent evt ) {
                    if ( evt.getPropertyName() == ExplorerManager.PROP_SELECTED_NODES ) {
                        selectionChange();
                    }
                }
            } );

        java.awt.GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        gridBagConstraints1.insets = new java.awt.Insets (0, 3, 3, 3);
        availablePanel.add (explorerView, gridBagConstraints1);

        // Customize selectedList

        selectedList.setCellRenderer( new SelectedListCellRenderer() );
        selectedListModel = new DefaultListModel();
        selectedList.setModel( selectedListModel );
        selectedList.getSelectionModel().setSelectionMode( javax.swing.ListSelectionModel.SINGLE_SELECTION );
        selectedList.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged( javax.swing.event.ListSelectionEvent evt ) {
                    selectionChange();
                }
            }
        );

        // i18n

        ((TitledBorder)availablePanel.getBorder()).setTitle( bundle.getString( "CTL_UpdatePanel_availablePanel" ) );
        ((TitledBorder)selectedPanel.getBorder()).setTitle( bundle.getString( "CTL_UpdatePanel_selectedPanel" ) );
        ((TitledBorder)detailPanel.getBorder()).setTitle( bundle.getString( "CTL_UpdatePanel_detailPanel" ) );

        availableLabel.setText( bundle.getString( "CTL_UpdatePanel_availableLabel" ) );
        localLabel.setText( bundle.getString( "CTL_UpdatePanel_localLabel" ) );
        moduleSizeLabel.setText( bundle.getString( "CTL_UpdatePanel_downloadSizeLabel" ) );
        totalSizeLabel.setText( bundle.getString( "CTL_UpdatePanel_totalSizeLabel" ) );
        descriptionLabel.setText( bundle.getString( "CTL_UpdatePanel_descriptionLabel" ) );

        addButton.setText( bundle.getString( "CTL_UpdatePanel_addButton" ) );
        removeButton.setText( bundle.getString( "CTL_UpdatePanel_removeButton" ) );
        addAllButton.setText( bundle.getString( "CTL_UpdatePanel_addAllButton" ) );
        removeAllButton.setText( bundle.getString( "CTL_UpdatePanel_removeAllButton" ) );

        homePageButton.setText( bundle.getString( "CTL_UpdatePanel_homePageButton" ) );

        if ( wizardType == 1 ) { // We don't need this fieds for downloaded modules
            moduleSizeLabel.setVisible( false );
            moduleSizeField.setVisible( false );
            totalSizeLabel.setVisible( false );
            totalSizeField.setVisible( false );
            homePageButton.setVisible( false );
        }

    }

    /** Returns preferred size of the panel */
    public java.awt.Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        availablePanel = new javax.swing.JPanel ();
        buttonPanel = new javax.swing.JPanel ();
        addButton = new javax.swing.JButton ();
        removeButton = new javax.swing.JButton ();
        addAllButton = new javax.swing.JButton ();
        removeAllButton = new javax.swing.JButton ();
        jPanel5 = new javax.swing.JPanel ();
        selectedPanel = new javax.swing.JPanel ();
        jScrollPane1 = new javax.swing.JScrollPane ();
        selectedList = new javax.swing.JList ();
        detailPanel = new javax.swing.JPanel ();
        availableLabel = new javax.swing.JLabel ();
        availableVersionField = new javax.swing.JTextField ();
        moduleSizeLabel = new javax.swing.JLabel ();
        moduleSizeField = new javax.swing.JTextField ();
        localLabel = new javax.swing.JLabel ();
        localVersionField = new javax.swing.JTextField ();
        totalSizeLabel = new javax.swing.JLabel ();
        totalSizeField = new javax.swing.JTextField ();
        descriptionLabel = new javax.swing.JLabel ();
        homePageButton = new javax.swing.JButton ();
        jScrollPane2 = new javax.swing.JScrollPane ();
        descriptionTextArea = new javax.swing.JTextArea ();
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));

        availablePanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints2;
        availablePanel.setBorder (new javax.swing.border.TitledBorder(
                                      new javax.swing.border.EtchedBorder(), "b1")); // NOI18N


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (availablePanel, gridBagConstraints1);

        buttonPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints3;
        buttonPanel.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));

        addButton.setMargin (new java.awt.Insets(2, 2, 2, 2));
        addButton.setText ("jButton1"); // NOI18N
        addButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             addButtonActionPerformed (evt);
                                         }
                                     }
                                    );

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTH;
        buttonPanel.add (addButton, gridBagConstraints3);

        removeButton.setMargin (new java.awt.Insets(2, 2, 2, 2));
        removeButton.setText ("jButton2"); // NOI18N
        removeButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                removeButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets (0, 0, 5, 0);
        buttonPanel.add (removeButton, gridBagConstraints3);

        addAllButton.setMargin (new java.awt.Insets(2, 2, 2, 2));
        addAllButton.setText ("jButton3"); // NOI18N
        addAllButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                addAllButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add (addAllButton, gridBagConstraints3);

        removeAllButton.setMargin (new java.awt.Insets(2, 2, 2, 2));
        removeAllButton.setText ("jButton4"); // NOI18N
        removeAllButton.addActionListener (new java.awt.event.ActionListener () {
                                               public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                   removeAllButtonActionPerformed (evt);
                                               }
                                           }
                                          );

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add (removeAllButton, gridBagConstraints3);


        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridheight = 0;
        gridBagConstraints3.weighty = 1.0;
        buttonPanel.add (jPanel5, gridBagConstraints3);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints1.weighty = 1.0;
        add (buttonPanel, gridBagConstraints1);

        selectedPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints4;
        selectedPanel.setBorder (new javax.swing.border.TitledBorder(
                                     new javax.swing.border.EtchedBorder(), "b2")); // NOI18N


        selectedList.addMouseListener (new java.awt.event.MouseAdapter () {
                                           public void mouseClicked (java.awt.event.MouseEvent evt) {
                                               selectedListMouseClicked (evt);
                                           }
                                       }
                                      );

        jScrollPane1.setViewportView (selectedList);

        gridBagConstraints4 = new java.awt.GridBagConstraints ();
        gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints4.insets = new java.awt.Insets (0, 3, 3, 3);
        gridBagConstraints4.weightx = 1.0;
        gridBagConstraints4.weighty = 1.0;
        selectedPanel.add (jScrollPane1, gridBagConstraints4);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (selectedPanel, gridBagConstraints1);

        detailPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints5;
        detailPanel.setBorder (new javax.swing.border.TitledBorder(
                                   new javax.swing.border.EtchedBorder(), "b3")); // NOI18N

        availableLabel.setText ("jLabel1"); // NOI18N

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.insets = new java.awt.Insets (0, 5, 3, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        detailPanel.add (availableLabel, gridBagConstraints5);

        availableVersionField.setEditable (false);

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.insets = new java.awt.Insets (0, 5, 3, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints5.weightx = 1.0;
        detailPanel.add (availableVersionField, gridBagConstraints5);

        moduleSizeLabel.setText ("jLabel2"); // NOI18N

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.insets = new java.awt.Insets (0, 15, 3, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        detailPanel.add (moduleSizeLabel, gridBagConstraints5);

        moduleSizeField.setEditable (false);

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.gridwidth = 0;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.insets = new java.awt.Insets (0, 5, 3, 3);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints5.weightx = 1.0;
        detailPanel.add (moduleSizeField, gridBagConstraints5);

        localLabel.setText ("JLabel2"); // NOI18N

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.insets = new java.awt.Insets (0, 5, 3, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        detailPanel.add (localLabel, gridBagConstraints5);

        localVersionField.setEditable (false);

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.insets = new java.awt.Insets (0, 5, 3, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints5.weightx = 1.0;
        detailPanel.add (localVersionField, gridBagConstraints5);

        totalSizeLabel.setText ("jLabel4"); // NOI18N

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.insets = new java.awt.Insets (0, 15, 3, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        detailPanel.add (totalSizeLabel, gridBagConstraints5);

        totalSizeField.setEditable (false);

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.gridwidth = 0;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints5.insets = new java.awt.Insets (0, 5, 3, 3);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints5.weightx = 1.0;
        detailPanel.add (totalSizeField, gridBagConstraints5);

        descriptionLabel.setText ("jLabel5"); // NOI18N

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.insets = new java.awt.Insets (0, 5, 3, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        detailPanel.add (descriptionLabel, gridBagConstraints5);

        homePageButton.setText ("jButton5"); // NOI18N
        homePageButton.addActionListener (new java.awt.event.ActionListener () {
                                              public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                  homePageButtonActionPerformed (evt);
                                              }
                                          }
                                         );

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.gridwidth = 0;
        gridBagConstraints5.insets = new java.awt.Insets (0, 0, 3, 3);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.EAST;
        detailPanel.add (homePageButton, gridBagConstraints5);


        descriptionTextArea.setMinimumSize (new java.awt.Dimension(200, 200));
        descriptionTextArea.setEditable (false);

        jScrollPane2.setViewportView (descriptionTextArea);

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.gridwidth = 0;
        gridBagConstraints5.gridheight = 0;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints5.insets = new java.awt.Insets (0, 3, 3, 3);
        gridBagConstraints5.weighty = 1.0;
        detailPanel.add (jScrollPane2, gridBagConstraints5);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (5, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (detailPanel, gridBagConstraints1);

    }//GEN-END:initComponents

    private void selectedListMouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedListMouseClicked
        if ( evt.getClickCount() == 2 ) {
            int index = selectedList.locationToIndex( evt.getPoint() );

            if ( index >= 0 && index < selectedListModel.size() ) {
                removeModule( (ModuleUpdate)selectedListModel.get( index ) );
                validator.setValid( selectedListModel.size() > 0 );
            }

        }
    }//GEN-LAST:event_selectedListMouseClicked

    private void addAllButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllButtonActionPerformed
        Collection modules = updates.getModules();

        Iterator it = modules.iterator();

        while ( it.hasNext() ) {
            if ( !addModule( (ModuleUpdate)it.next() ) ) {
                // Some licence rejected
                removeAllButtonActionPerformed(evt);
                return;
            }
        }

    }//GEN-LAST:event_addAllButtonActionPerformed

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int index = selectedList.getMinSelectionIndex();

        if ( index < 0 )
            return;

        removeModule( (ModuleUpdate)selectedListModel.get( index ) );
        validator.setValid( selectedListModel.size() > 0 );
    }//GEN-LAST:event_removeButtonActionPerformed

    private void removeAllButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed

        selectedListModel.clear();
        showSize();
        validator.setValid( selectedListModel.size() > 0 );
        /*
        for ( int i = selectedListModel.size(); i  > 0; i-- ) {
          removeModule( (ModuleUpdate)selectedListModel.get( i - 1 ) );
    } */
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Node[] nodes = explorerView.getSelectedNodes();

        for ( int i = 0; i < nodes.length; i ++ ) {
            ModuleUpdate mu = (ModuleUpdate)nodes[i].getCookie( ModuleUpdate.class );
            if ( mu != null ) {
                if ( !addModule( mu ) ) {
                    return;
                }
            }
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void homePageButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homePageButtonActionPerformed
        ModuleUpdate mu = getSingleSelection();

        if ( mu != null && mu.getHomePage() != null ) {
            TopManager.getDefault().showUrl( mu.getHomePage() );
        }
    }//GEN-LAST:event_homePageButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel availablePanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton addButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton addAllButton;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel selectedPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList selectedList;
    private javax.swing.JPanel detailPanel;
    private javax.swing.JLabel availableLabel;
    private javax.swing.JTextField availableVersionField;
    private javax.swing.JLabel moduleSizeLabel;
    private javax.swing.JTextField moduleSizeField;
    private javax.swing.JLabel localLabel;
    private javax.swing.JTextField localVersionField;
    private javax.swing.JLabel totalSizeLabel;
    private javax.swing.JTextField totalSizeField;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JButton homePageButton;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea descriptionTextArea;
    // End of variables declaration//GEN-END:variables


    void setUpdates( Updates updates ) {

        this.updates = updates;

        selectedListModel.clear();
        totalSize = 0;
        Iterator it = updates.getModules().iterator();

        while( it.hasNext() ) {
            ModuleUpdate mu = (ModuleUpdate)it.next();

            if ( mu.isSelected() ) {
                selectedListModel.addElement( mu );
                totalSize += mu.getDownloadSize();
            }
        }
        validator.setValid( selectedListModel.size() > 0 );

        totalSizeField.setText( ( totalSize / 1024 ) + SPACE + bundle.getString("CTL_UpdatePanel_KB") );

        explorerView.setContext( new UpdateNode.Group( updates.getRootGroup()) );

        explorerView.expandAll();

        showSize();
    }

    // INNERCLAS ------------------------------------------------------
    static class ExplorerView extends ExplorerPanel  {

        private static ExplorerManager em;
        private BeanTreeView btv;

        static final long serialVersionUID =-5811911993587966912L;
        ExplorerView( ) {
            Node waitNode = new UpdateNode.Wait();
            createContent( waitNode );
        }

        private void createContent ( Node node ) {

            btv = new BeanTreeView ();
            btv.setPopupAllowed( false );
            btv.setDefaultActionAllowed( true );
            em = getExplorerManager ();

            //sp.add (new org.openide.explorer.view.ListView (), SplittedPanel.ADD_LEFT);
            setLayout (new java.awt.BorderLayout());
            add ( java.awt.BorderLayout.CENTER, btv );

            em.setRootContext ( node );
            em.setExploredContext( node );


            btv.setDefaultActionAllowed( true );
            btv.setRootVisible( false );
        }

        public java.awt.Dimension getPreferredSize () {
            java.awt.Dimension sup = super.getPreferredSize ();
            return new java.awt.Dimension ( Math.max (sup.width, 450), Math.max (sup.height, 300 ));
        }

        void expandAll() {
            btv.expandAll();
        }

        static Node[] getSelectedNodes() {
            return em.getSelectedNodes();
        }

        void setContext( Node node ) {
            em.setRootContext ( node );
            em.setExploredContext( node );
        }

    }

    private ModuleUpdate getSingleSelection() {
        Node[] selectedNodes = explorerView.getSelectedNodes();
        if ( selectedNodes.length != 1 ) {
            return null;
        }
        else {
            return (ModuleUpdate)selectedNodes[0].getCookie( ModuleUpdate.class );
        }
    }


    /** Called when the selection in selectedList or explorerView changes */
    private void selectionChange() {
        ModuleUpdate mu = getSingleSelection();

        if ( mu == null ) {
            localVersionField.setText( EMPTY_STRING );
            availableVersionField.setText( EMPTY_STRING );
            moduleSizeField.setText( EMPTY_STRING );
            descriptionTextArea.setText( EMPTY_STRING );
        }
        else {
            localVersionField.setText( mu.getLocalModule() == null ?
                                       bundle.getString( "CTL_UpdatePanel_ModuleNotInstalled" ) :
                                       mu.getLocalModule().getSpecVersion() );
            availableVersionField.setText( mu.getRemoteModule().getSpecVersion() );
            moduleSizeField.setText( ( mu.getDownloadSize() / 1024 ) + SPACE + bundle.getString("CTL_UpdatePanel_KB") );
            descriptionTextArea.setText( mu.getDescription() );
        }
    }

    /** Adds module to selected list. Shows LicenceAgreement if needed.
     * Increases total size of download. Checks dependencies and adds 
     * all modules needed to satisfy all dependencies.
     *@return False if the licence was rejected
     */
    private boolean addModule( final ModuleUpdate mu ) {

        if ( selectedListModel.contains( mu ) )
            return true; // Already in download list

        if ( mu.getLicenceText() != null && mu.getLicenceID() != null ) {

            // Test if the licence was already accepted
            boolean accepted = false;

            for( int i = 0; mu.getLicenceID() != null && i < selectedListModel.size(); i++ ) {

                if( ((ModuleUpdate)selectedListModel.get(i)).getLicenceID() != null &&
                        ((ModuleUpdate)selectedListModel.get(i)).getLicenceID().equals( mu.getLicenceID() ) ) {
                    accepted = true;
                }
            }

            if ( !accepted && !LicenceDialog.acceptLicence( mu.getLicenceText() ) ) {
                return false;
            }
        }

        // Module not selected yet and licence accepted

        selectedListModel.add( findPosition( mu ), mu );

        DependencyChecker dc = new DependencyChecker( updates );
        Collection modulesToAdd = dc.modulesToAdd( mu );
        Iterator it = modulesToAdd.iterator();
        while( it.hasNext() ) {
            ModuleUpdate addMu = (ModuleUpdate)it.next();
            if ( !addModule( addMu ) ) {
                selectedListModel.removeElement( mu );
                totalSize += mu.getDownloadSize();
                return false;
            }
        }

        showSize();
        validator.setValid( selectedListModel.size() > 0 );
        selectedList.revalidate();
        return true;
    }

    /** Finds the right position of the module in the modules list */
    private int findPosition( ModuleUpdate mu ) {
        int pos = 0;

        Collection modules = updates.getModules();
        Iterator it = modules.iterator();
        while ( it.hasNext() ) {

            ModuleUpdate imu = ((ModuleUpdate)it.next());

            if ( mu == imu ) {
                return pos;
            }
            if ( selectedListModel.contains( imu ) ) {
                pos++;
            }
        }

        return pos;
    }

    /** Removes module at position <CODE>index</CODE>. Decreases total
     * size of download. Checks all dependencies and removes all dependent
     * modules.
     */   
    private void removeModule( final ModuleUpdate mu ) {

        if ( !selectedListModel.contains( mu ) )
            return; // is not selected

        final int index = selectedListModel.indexOf ( mu );
        totalSize -= mu.getDownloadSize();

        selectedListModel.remove( index );
        showSize();

        DependencyChecker dc = new DependencyChecker( updates );
        Collection modulesToRemove = dc.modulesToRemove( mu );
        Iterator it = modulesToRemove.iterator();
        while( it.hasNext() ) {
            ModuleUpdate removeMu = (ModuleUpdate)it.next();
            removeModule( removeMu );
        }

        validator.setValid( selectedListModel.size() > 0 );

    }

    void showSize() {
        totalSize = 0;

        for( int i = 0; i < selectedListModel.size(); i++ ) {
            if ( !((ModuleUpdate)selectedListModel.get(i)).isDownloadOK() ) {
                totalSize += ((ModuleUpdate)selectedListModel.get(i)).getDownloadSize();
            }
        }

        totalSizeField.setText( ( totalSize / 1024 ) + SPACE + bundle.getString("CTL_UpdatePanel_KB") );
    }

    int modulesToDownload() {
        int result = 0;

        for( int i = 0; i < selectedListModel.size(); i++ ) {
            if ( !((ModuleUpdate)selectedListModel.get(i)).isDownloadOK() ) {
                result++;
            }
        }

        return result;
    }


    void markSelectedModules() {

        // We have firts to unmark all modules
        Collection modules = updates.getModules();
        Iterator it = modules.iterator();
        while ( it.hasNext() ) {
            ((ModuleUpdate)it.next()).setSelected( false );
        }

        // Then mark the selected

        for( int i = 0; i < selectedListModel.size(); i++ ) {
            ((ModuleUpdate)selectedListModel.get(i)).setSelected( true );
        }
    }


}
/*
 * Log
 *  27   Gandalf   1.26        1/12/00  Petr Hrebejk    i18n mk2
 *  26   Gandalf   1.25        1/12/00  Petr Hrebejk    i18n
 *  25   Gandalf   1.24        1/9/00   Petr Hrebejk    Proxy Config and 
 *       Registration number added
 *  24   Gandalf   1.23        1/3/00   Petr Hrebejk    Various bug fixes - 
 *       5097, 5098, 5110, 5099, 5108
 *  23   Gandalf   1.22        12/22/99 Petr Hrebejk    Various bugfixes
 *  22   Gandalf   1.21        12/16/99 Petr Hrebejk    Sign checking added
 *  21   Gandalf   1.20        12/1/99  Petr Hrebejk    Checkin signatures of 
 *       NBM files & automatic autoupdate check added
 *  20   Gandalf   1.19        11/27/99 Patrik Knakal   
 *  19   Gandalf   1.18        11/12/99 Petr Hrebejk    Bug fixes: Texts, Not 
 *       NetBeans patches, unselecting modules
 *  18   Gandalf   1.17        11/8/99  Petr Hrebejk    Install of downloaded 
 *       modules added, Licenses in XML
 *  17   Gandalf   1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        10/11/99 Petr Hrebejk    Last minute fixes
 *  15   Gandalf   1.14        10/11/99 Petr Hrebejk    Version before Beta 5
 *  14   Gandalf   1.13        10/10/99 Petr Hrebejk    AutoUpdate made to 
 *       wizard
 *  13   Gandalf   1.12        10/8/99  Petr Hrebejk    Next Develop version
 *  12   Gandalf   1.11        10/7/99  Petr Hrebejk    Next development version
 *  11   Gandalf   1.10        10/6/99  Petr Hrebejk    New Autoupdate
 *  10   Gandalf   1.9         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  9    Gandalf   1.8         8/9/99   Petr Hrebejk    Dependency check added. 
 *       Update-Location tag removed
 *  8    Gandalf   1.7         8/1/99   Petr Hrebejk    Action install & 
 *       multiuser install fix
 *  7    Gandalf   1.6         7/28/99  Petr Hrebejk    Check single URL added
 *  6    Gandalf   1.5         6/10/99  Petr Hrebejk    
 *  5    Gandalf   1.4         6/10/99  Petr Hrebejk    
 *  4    Gandalf   1.3         6/10/99  Petr Hrebejk    
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/7/99   Petr Hrebejk    
 *  1    Gandalf   1.0         6/7/99   Petr Hrebejk    
 * $
 */
