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

package org.netbeans.core;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.KeyStroke;
import javax.swing.event.*;
import javax.swing.text.Keymap;
import javax.swing.tree.*;

import org.openide.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
*
* @author Ian Formanek
*/
public class ActionsPanel extends javax.swing.JPanel {
    private static final java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(ActionsPanel.class);

    private ShortcutsEditor shortcutsEditor;

    private boolean canChangeKey;

    /**
     * @associates TreeNode 
     */
    private HashMap actionToNode = new HashMap (41);
    private DefaultTreeModel model;
    private DefaultTreeSelectionModel treeSelectionModel;
    private SystemAction selectedAction;
    private DefaultListModel shortcutsModel;

    /**
     * @associates KeyStroke 
     */
    private HashMap nameToStroke = new HashMap (11);

    private int currentKeyCode;
    private int currentModifiers;

    /** Creates new form ActionsPanel */
    public ActionsPanel(boolean canChangeKey, ShortcutsEditor shortcutsEditor) {
        this.shortcutsEditor = shortcutsEditor;
        this.canChangeKey = canChangeKey;
        initComponents ();

        actionsPanel.setBorder (new javax.swing.border.CompoundBorder(
                                    new javax.swing.border.TitledBorder(
                                        new javax.swing.border.EtchedBorder(), bundle.getString ("ActionsPanel.ActionsTitle")),
                                    new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8))));

        shortcutPanel.setBorder (new javax.swing.border.CompoundBorder(
                                     new javax.swing.border.TitledBorder(
                                         new javax.swing.border.EtchedBorder(), bundle.getString ("ActionsPanel.ShortcutsTitle")),
                                     new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8))));

        shortcutsList.setModel(shortcutsModel = new DefaultListModel ());
        treeSelectionModel = new DefaultTreeSelectionModel ();
        treeSelectionModel.addTreeSelectionListener (
            new TreeSelectionListener () {
                public void valueChanged (TreeSelectionEvent evt) {
                    TreePath[] paths = actionsTree.getSelectionPaths ();
                    if ((paths != null) && (paths.length == 1)) {
                        TreeNode node = (TreeNode) paths[0].getLastPathComponent ();
                        if ((node != null) && (node instanceof ActionNode)) {
                            updateSelectedAction (((ActionNode)node).getAction ());
                            return;
                        }
                    }
                    updateSelectedAction (null);
                }
            }
        );
        treeSelectionModel.setSelectionMode (DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
        actionsTree.setModel (model = new DefaultTreeModel (createActionsRootNode ()));
        actionsTree.setSelectionModel (treeSelectionModel);

        // expand whole actionsTree
        int i = 0, j, k = actionsTree.getRowCount ();
        do {
            do {
                j = actionsTree.getRowCount ();
                actionsTree.expandRow (i);
            } while (j != actionsTree.getRowCount ());
            i++;
        } while (i < actionsTree.getRowCount ());

        actionsTree.setShowsRootHandles(true);
        actionsTree.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N
        updateButtons ();

        HelpCtx.setHelpIDString (this, ActionsPanel.class.getName ());
    }

    void setAction (SystemAction action) {
        TreeNode tn = (TreeNode)actionToNode.get (action.getClass ());
        if (tn != null) {
            TreePath tp = new TreePath (model.getPathToRoot (tn));
            treeSelectionModel.setSelectionPath(tp);
            actionsTree.scrollPathToVisible(tp);
            updateSelectedAction (action);
        }
    }

    private void updateSelectedAction (SystemAction action) {
        selectedAction = action;
        Keymap map = TopManager.getDefault ().getGlobalKeymap ();

        Object[] acts = map.getBoundActions();

        KeyStroke[] strokes = map.getKeyStrokesForAction(action);
        shortcutsModel.removeAllElements ();
        nameToStroke.clear ();
        for (int i = 0; i < strokes.length; i++) {
            String keyName = ShortcutsEditor.getKeyText (strokes[i]);
            nameToStroke.put (keyName, strokes[i]);
            shortcutsModel.addElement (keyName);
        }
        updateButtons ();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        actionsPanel = new javax.swing.JPanel ();
        actionsScrollPane = new javax.swing.JScrollPane ();
        actionsTree = new javax.swing.JTree ();
        shortcutPanel = new javax.swing.JPanel ();
        shortcutsScrollPane = new javax.swing.JScrollPane ();
        shortcutsList = new javax.swing.JList ();
        shortcutsButtonsPanel = new javax.swing.JPanel ();
        shortcutAddButton = new javax.swing.JButton ();
        shortcutRemoveButton = new javax.swing.JButton ();
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        actionsPanel.setLayout (new java.awt.BorderLayout ());


        actionsTree.setRootVisible (false);

        actionsScrollPane.setViewportView (actionsTree);

        actionsPanel.add (actionsScrollPane, java.awt.BorderLayout.CENTER);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (actionsPanel, gridBagConstraints1);

        shortcutPanel.setLayout (new java.awt.BorderLayout (8, 0));


        shortcutsList.addListSelectionListener (new javax.swing.event.ListSelectionListener () {
                                                    public void valueChanged (javax.swing.event.ListSelectionEvent evt) {
                                                        shortcutsListValueChanged (evt);
                                                    }
                                                }
                                               );

        shortcutsScrollPane.setViewportView (shortcutsList);

        shortcutPanel.add (shortcutsScrollPane, java.awt.BorderLayout.CENTER);

        shortcutsButtonsPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints2;

        shortcutAddButton.setText (org.openide.util.NbBundle.getBundle(ActionsPanel.class).getString("ActionsPanel.shortcutAddButton.text"));
        shortcutAddButton.addActionListener (new java.awt.event.ActionListener () {
                                                 public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                     shortcutAddButtonActionPerformed (evt);
                                                 }
                                             }
                                            );

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (0, 0, 8, 0);
        shortcutsButtonsPanel.add (shortcutAddButton, gridBagConstraints2);

        shortcutRemoveButton.setText (org.openide.util.NbBundle.getBundle(ActionsPanel.class).getString("ActionsPanel.shortcutRemoveButton.text"));
        shortcutRemoveButton.addActionListener (new java.awt.event.ActionListener () {
                                                    public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                        shortcutRemoveButtonActionPerformed (evt);
                                                    }
                                                }
                                               );

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (0, 0, 8, 0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints2.weighty = 1.0;
        shortcutsButtonsPanel.add (shortcutRemoveButton, gridBagConstraints2);

        shortcutPanel.add (shortcutsButtonsPanel, java.awt.BorderLayout.EAST);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (8, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 0.2;
        add (shortcutPanel, gridBagConstraints1);

    }//GEN-END:initComponents

    private void shortcutRemoveButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shortcutRemoveButtonActionPerformed
        Object[] selectedItems = shortcutsList.getSelectedValues();
        Keymap map = TopManager.getDefault ().getGlobalKeymap ();
        for (int i = 0; i < selectedItems.length; i++) {
            KeyStroke ks = (KeyStroke)nameToStroke.get (selectedItems[i]);
            if (ks != null) {
                map.removeKeyStrokeBinding(ks);
            }
        }
        updateSelectedAction (selectedAction);
        shortcutsEditor.setModified (true);
        updateTree ();
    }//GEN-LAST:event_shortcutRemoveButtonActionPerformed

    private void shortcutAddButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shortcutAddButtonActionPerformed
        KeyStroke existingStroke = null;
        boolean shortcutAccepted = false;
        Keymap map = TopManager.getDefault ().getGlobalKeymap ();

        while (true) {
            ShortcutEnterPanel sep = new ShortcutEnterPanel (existingStroke);
            DialogDescriptor dd = new DialogDescriptor (sep, bundle.getString ("ActionsPanel.AddShortcut"));
            TopManager.getDefault().createDialog(dd).show ();
            if (dd.getValue().equals (DialogDescriptor.OK_OPTION)) {
                existingStroke = sep.getShortcut ();
                Action a = map.getAction (existingStroke);
                if (a != null) {
                    String text = java.text.MessageFormat.format (
                                      bundle.getString ("FMT_ActionsPanel.AlreadyBound"),
                                      new Object[] {
                                          a.getValue(Action.NAME)
                                      }
                                  );
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation (text);
                    Object result = TopManager.getDefault ().notify (nd);
                    if (result.equals (NotifyDescriptor.YES_OPTION)) {
                        shortcutAccepted = true; // yes, will replace eisting shortcut with new one
                    } else if (result.equals (NotifyDescriptor.NO_OPTION)) {
                        return; // no, will keep the old shortcut
                    } // else open the shortcut selector again
                } else {
                    shortcutAccepted = true;
                }
            } else {
                return; // adding cancelled
            }
            if (shortcutAccepted) {
                map.addActionForKeyStroke(existingStroke, selectedAction);
                updateSelectedAction (selectedAction);
                shortcutsEditor.setModified (true);
                updateTree ();
                return;
            }
        }
    }//GEN-LAST:event_shortcutAddButtonActionPerformed

    private void shortcutsListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_shortcutsListValueChanged
        updateButtons ();
    }//GEN-LAST:event_shortcutsListValueChanged

    private void updateButtons () {
        shortcutAddButton.setEnabled (selectedAction != null);
        shortcutRemoveButton.setEnabled ((selectedAction != null) && (shortcutsList.getSelectedIndices().length > 0));
    }

    void updateTree () {
        // next two lines are hack to force the Tree to invalidate sizes of renderes
        actionsTree.setShowsRootHandles(false);
        actionsTree.setShowsRootHandles(true);

        actionsTree.repaint ();
        actionsTree.revalidate ();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionsPanel;
    private javax.swing.JScrollPane actionsScrollPane;
    private javax.swing.JTree actionsTree;
    private javax.swing.JPanel shortcutPanel;
    private javax.swing.JScrollPane shortcutsScrollPane;
    private javax.swing.JList shortcutsList;
    private javax.swing.JPanel shortcutsButtonsPanel;
    private javax.swing.JButton shortcutAddButton;
    private javax.swing.JButton shortcutRemoveButton;
    // End of variables declaration//GEN-END:variables


    // -----------------------------------------------------------------------------
    // TreeNode for actions tree

    TreeNode createActionsRootNode () {
        final ArrayList actionGroups = new ArrayList ();
        DataFolder actionsFolder = TopManager.getDefault ().getPlaces ().folders ().actions ();
        DataObject[] actionsChildren = actionsFolder.getChildren ();
        for (int i = 0; i < actionsChildren.length; i++) {
            if (actionsChildren[i] instanceof DataFolder) actionGroups.add (new ActionsGroupNode ((DataFolder)actionsChildren[i], null));
        }

        return new TreeNode () {
                   public TreeNode getChildAt(int childIndex) { return (TreeNode) actionGroups.get(childIndex); }
                   public int getChildCount() { return actionGroups.size (); }
                   public TreeNode getParent() { return null; }
                   public int getIndex(TreeNode node) { return actionGroups.indexOf (node); }
                   public boolean getAllowsChildren() { return true; }
                   public boolean isLeaf() { return false; }
                   public java.util.Enumeration children() { return Collections.enumeration (actionGroups); }
               };
    }

    class ActionsGroupNode implements TreeNode {
        private DataFolder folder;

        /**
         * @associates TreeNode 
         */
        private ArrayList actions;
        private TreeNode parent;

        ActionsGroupNode (DataFolder folder, TreeNode parent) {
            this.folder = folder;
            this.parent = parent;
            DataObject[] children = folder.getChildren ();
            actions = new ArrayList (children.length);
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof InstanceDataObject) {
                    try {
                        Class instClass = ((InstanceDataObject)children[i]).instanceClass ();
                        if (SystemAction.class.isAssignableFrom (instClass)) {
                            TreeNode tn = new ActionNode (SystemAction.get (instClass), ActionsGroupNode.this);
                            actionToNode.put (instClass, tn);
                            actions.add (tn);
                        }
                    } catch (Throwable t) {
                        if (t instanceof ThreadDeath) throw (ThreadDeath)t;
                        // ignore problematic items
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) t.printStackTrace(); // NOI18N
                    }
                }
            }
        }

    public TreeNode getChildAt(int childIndex) { return  (TreeNode) actions.get(childIndex); }
        public int getChildCount() { return actions.size (); }
        public TreeNode getParent() { return parent; }
        public int getIndex(TreeNode node) { return actions.indexOf (node); }
        public boolean getAllowsChildren() { return true; }
        public boolean isLeaf() { return false; }
        public java.util.Enumeration children() { return Collections.enumeration (actions); }
        public String toString () { return folder.getName (); }
    }

    class ActionNode implements TreeNode {
        private SystemAction action;
        private TreeNode parent;

        ActionNode (SystemAction action, TreeNode parent) {
            this.action = action;
            this.parent = parent;
        }

        SystemAction getAction () { return action; }

        public TreeNode getChildAt(int childIndex) { return null; }
        public int getChildCount() { return 0; }
        public TreeNode getParent() { return parent; }
        public int getIndex(TreeNode node) { return -1; }
        public boolean getAllowsChildren() { return false; }
        public boolean isLeaf() { return true; }
        public java.util.Enumeration children() { return null; }
        public String toString () { return ShortcutsEditor.getActionName (action); }
    }

    class ShortcutEnterPanel extends javax.swing.JPanel {
        private javax.swing.JLabel shortcutLabel;
        private javax.swing.JTextField shortcutField;

        private KeyStroke defaultStroke;
        private int currentKeyCode;
        private int currentModifiers;

        ShortcutEnterPanel (KeyStroke stroke) {
            defaultStroke = stroke;

            shortcutLabel = new javax.swing.JLabel (bundle.getString ("ActionsPanel.ShortcutLabel"));
            shortcutField = new javax.swing.JTextField ();
            setLayout (new java.awt.GridBagLayout ());
            java.awt.GridBagConstraints gridBagConstraints1;
            setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));

            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.insets = new java.awt.Insets (0, 0, 0, 8);
            add (shortcutLabel, gridBagConstraints1);

            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.weightx = 1.0;
            add (shortcutField, gridBagConstraints1);

            shortcutField.addKeyListener (new java.awt.event.KeyAdapter () {
                                              public void keyPressed (java.awt.event.KeyEvent evt) {
                                                  shortcutFieldKeyPressed (evt);
                                              }
                                              public void keyReleased (java.awt.event.KeyEvent evt) {
                                                  shortcutFieldKeyReleased (evt);
                                              }
                                              public void keyTyped (java.awt.event.KeyEvent evt) {
                                                  shortcutFieldKeyTyped (evt);
                                              }
                                          }
                                         );

            if (defaultStroke != null) {
                shortcutField.setText(ShortcutsEditor.getKeyText (defaultStroke));
                currentKeyCode = defaultStroke.getKeyCode();
                currentModifiers = defaultStroke.getModifiers();
            }
            else shortcutField.setText (""); // NOI18N
        }

        public java.awt.Dimension getPreferredSize() {
            return new java.awt.Dimension (300, 50);
        }

        KeyStroke getShortcut () {
            return KeyStroke.getKeyStroke (currentKeyCode, currentModifiers);
        }

        private void shortcutFieldKeyReleased (java.awt.event.KeyEvent evt) {
            evt.consume();
            if ((currentKeyCode == KeyEvent.VK_ALT) || (currentKeyCode == KeyEvent.VK_ALT_GRAPH) || (currentKeyCode == KeyEvent.VK_CONTROL) || (currentKeyCode == KeyEvent.VK_SHIFT)) {
                // Not finished entering key
                if (defaultStroke != null) {
                    shortcutField.setText(ShortcutsEditor.getKeyText (defaultStroke));
                } else {
                    shortcutField.setText(""); // NOI18N
                }
            }
        }

        private void shortcutFieldKeyTyped (java.awt.event.KeyEvent evt) {
            evt.consume();
        }

        private void shortcutFieldKeyPressed (java.awt.event.KeyEvent evt) {
            evt.consume();
            currentKeyCode = evt.getKeyCode();
            currentModifiers = evt.getModifiers();
            shortcutField.setText(ShortcutsEditor.getKeyText (currentKeyCode, currentModifiers));
        }

    }
}

/*
 * Log
 *  11   Gandalf   1.10        1/16/00  Jesse Glick     Context help.
 *  10   Gandalf   1.9         1/13/00  Ian Formanek    I18N
 *  9    Gandalf   1.8         1/9/00   Ian Formanek    Removed debug printlns
 *  8    Gandalf   1.7         1/9/00   Ian Formanek    Improved loading and 
 *       adding shortcuts
 *  7    Gandalf   1.6         1/6/00   Ian Formanek    Cleaned, correct 
 *       updating of tree after changes
 *  6    Gandalf   1.5         12/1/99  Ian Formanek    
 *  5    Gandalf   1.4         11/30/99 Ian Formanek    
 *  4    Gandalf   1.3         11/29/99 Ian Formanek    
 *  3    Gandalf   1.2         11/26/99 Patrik Knakal   
 *  2    Gandalf   1.1         11/25/99 Ian Formanek    
 *  1    Gandalf   1.0         11/25/99 Ian Formanek    
 * $
 */
