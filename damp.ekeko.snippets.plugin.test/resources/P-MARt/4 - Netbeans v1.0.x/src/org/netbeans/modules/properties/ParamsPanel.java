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

package org.netbeans.modules.properties;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.BadLocationException;

import org.openide.util.HelpCtx;

/**
 *
 * @author  pjiricka
 * @version 
 */
public class ParamsPanel extends javax.swing.JPanel {

    static final int DEFAULT_WIDTH = 350;
    static final int DEFAULT_HEIGHT = 400;

    //  private String mainComment;
    // it is assured that comments and arguments contain the same number of elements
    //  private ArrayList comments = new ArrayList(); // of String


    /**
     * @associates String 
     */
    private ArrayList arguments = new ArrayList(); // of String

    private int editingRow = -1;

    private ParamsListModel model;

    static final long serialVersionUID =-3754019215574878093L;
    /** Creates new form ParamsPanel */
    public ParamsPanel() {
        initComponents ();
        paramsList.setModel(getListModel());
        paramsList.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (paramsList.getSelectedIndex() != -1)
                        updateEditor(paramsList.getSelectedIndex());
                    removeParamButton.setEnabled(paramsList.getSelectedIndex() != -1);
                }
            }
        );
        removeParamButton.setEnabled(paramsList.getSelectedIndex() != -1);
        HelpCtx.setHelpIDString (this, ParamsPanel.class.getName ());
    }

    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension inh = super.getPreferredSize ();
        return new java.awt.Dimension (Math.max (inh.width, DEFAULT_WIDTH), Math.max (inh.height, DEFAULT_HEIGHT));
    }

    public void setComment(String comment) {
        mainCommentTextArea.setText(comment);
    }

    public String getComment() {
        return mainCommentTextArea.getText();
    }

    /*  public void setComment(String comment) {
        parseComment(comment);
        equalize();
        if (getListModel().getSize() > 0)
          getListModel().fireIntervalAdded(0, getListModel().getSize() - 1);
      }
      
      public String getComment() { 
        commitChanges();
        return assembleComment();
      }*/

    public void setArguments(String[] args) {
        arguments.clear();
        for (int i = 0; i < args.length; i++) {
            arguments.add(args[i]);
        }
        //    equalize();
        if (getListModel().getSize() > 0)
            getListModel().fireIntervalAdded(0, getListModel().getSize() - 1);
        if (getListModel().getSize() > 0)
            editRow(0);
        else
            editRow(-1);
    }

    public String[] getArguments() {
        commitChanges();

        // j is the last non-empty index
        int j = -1;
        for (int i = 0; i < arguments.size(); i++)
            if (((String)arguments.get(i)).trim().length() > 0)
                j = i;

        String[] args = new String[j + 1];
        for (int i = 0; i <= j; i++)
            args[i] = (String)arguments.get(i);

        return args;
    }

    /*  private void equalize() {
        while (comments.size() < arguments.size())
          comments.add("");
        while (arguments.size() < comments.size())
          arguments.add("");
      }*/

    private void commitChanges() {
        //mainComment = mainCommentTextArea.getText();
        if (editingRow != -1) {
            //comments.set (editingRow, commentTextArea.getText());
            arguments.set(editingRow, codePane.getText());
            getListModel().fireContentsChanged(editingRow, editingRow);
        }
    }

    /** Sets the index of the row being edited to row or disables editing if row == -1.
    * Should only be called  with -1 if there is no data */
    private void editRow(int row) {
        if (row != -1)
            paramsList.setSelectedIndex(row);
        else
            paramsList.setSelectedIndices(new int[0]);
    }

    private void updateEditor(int row) {
        commitChanges();
        editingRow = row;
        if (row == -1) {
            //commentTextArea.setText("");
            codePane.setText("");
            //commentTextArea.setEnabled(false);
            codePane.setEnabled(false);
        }
        else {
            //commentTextArea.setText((String)comments.get(editingRow));
            codePane.setText((String)arguments.get(editingRow));
            //commentTextArea.setEnabled(true);
            codePane.setEnabled(true);
            codePane.requestFocus();
        }
    }

    /** Fills values arguments and mainComment with fragments of the parameter comment */
    /*  private void parseComment(String comment) {
        comments.clear();
        String part;
        int searchIndex = 0, lastSearchIndex = 0;
        int index = 0;
        while (searchIndex >= 0) {
          String iStr = "{" + index + "}";
          lastSearchIndex = searchIndex;
          searchIndex = comment.indexOf(iStr, searchIndex);
          if (searchIndex != -1) {
            part = comment.substring(lastSearchIndex, searchIndex).trim();
            putPart(part, index);
            searchIndex += iStr.length();
            int eqIndex = comment.indexOf("=", searchIndex);
            if (eqIndex != -1 && comment.substring(searchIndex, eqIndex).trim().length() == 0)
              searchIndex = eqIndex + "=".length();
          }
          index++;
        }
        part = comment.substring(lastSearchIndex).trim();
        putPart(part, index);
      }
      
      private void putPart(String part, int index) {
        if (index == 0) {
          mainComment = part;
        }
        else {
          comments.add(part);
        }
      }
      
      private String assembleComment() {
        StringBuffer comment = new StringBuffer(mainComment);
        if (!mainComment.endsWith("\n"))
          comment.append('\n');
        for (int i = 0; i < comments.size(); i++) {
          String x = "{" + i + "}";
          String cur = (String)comments.get(i);
          if (!cur.trim().startsWith(x))
            cur = x + " = " + cur;
          comment.append(cur);
          if (!cur.endsWith("\n"))
            comment.append('\n');
        }
        return comment.toString();
      }*/

    protected ParamsListModel getListModel() {
        if (model == null)
            model = new ParamsListModel ();
        return model;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(10, 10, 10, 10)));

        jPanel1 = new javax.swing.JPanel ();
        jPanel1.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints2;
        jPanel1.setBorder (new javax.swing.border.TitledBorder(
                               new javax.swing.border.EtchedBorder(), "Comment"));

        jScrollPane1 = new javax.swing.JScrollPane ();

        mainCommentTextArea = new javax.swing.JTextArea ();
        mainCommentTextArea.setRows (4);

        jScrollPane1.setViewportView (mainCommentTextArea);

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.gridheight = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.insets = new java.awt.Insets (0, 8, 8, 8);
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        jPanel1.add (jScrollPane1, gridBagConstraints2);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (jPanel1, gridBagConstraints1);

        jPanel2 = new javax.swing.JPanel ();
        jPanel2.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints3;
        jPanel2.setBorder (new javax.swing.border.TitledBorder(
                               new javax.swing.border.EtchedBorder(), "Parameters"));

        jScrollPane2 = new javax.swing.JScrollPane ();

        paramsList = new javax.swing.JList ();
        paramsList.setVisibleRowCount (3);
        paramsList.setSelectionMode (javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jScrollPane2.setViewportView (paramsList);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints3.insets = new java.awt.Insets (0, 8, 8, 8);
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        jPanel2.add (jScrollPane2, gridBagConstraints3);

        jPanel3 = new javax.swing.JPanel ();
        jPanel3.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints4;

        addParamButton = new javax.swing.JButton ();
        addParamButton.setText (org.openide.util.NbBundle.getBundle(ParamsPanel.class).getString("ParamsPanel.addParamButton.text"));
        addParamButton.addActionListener (new java.awt.event.ActionListener () {
                                              public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                  addParamButtonActionPerformed (evt);
                                              }
                                          }
                                         );

        gridBagConstraints4 = new java.awt.GridBagConstraints ();
        gridBagConstraints4.gridwidth = 0;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.insets = new java.awt.Insets (0, 8, 8, 8);
        gridBagConstraints4.weightx = 1.0;
        jPanel3.add (addParamButton, gridBagConstraints4);

        removeParamButton = new javax.swing.JButton ();
        removeParamButton.setText (org.openide.util.NbBundle.getBundle(ParamsPanel.class).getString("ParamsPanel.removeParamButton.text"));
        removeParamButton.addActionListener (new java.awt.event.ActionListener () {
                                                 public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                     removeParamButtonActionPerformed (evt);
                                                 }
                                             }
                                            );

        gridBagConstraints4 = new java.awt.GridBagConstraints ();
        gridBagConstraints4.gridwidth = 0;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.insets = new java.awt.Insets (0, 8, 8, 8);
        gridBagConstraints4.weightx = 1.0;
        jPanel3.add (removeParamButton, gridBagConstraints4);

        jPanel4 = new javax.swing.JPanel ();

        gridBagConstraints4 = new java.awt.GridBagConstraints ();
        gridBagConstraints4.gridwidth = 0;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints4.weightx = 1.0;
        gridBagConstraints4.weighty = 1.0;
        jPanel3.add (jPanel4, gridBagConstraints4);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints3.weighty = 1.0;
        jPanel2.add (jPanel3, gridBagConstraints3);

        jLabel2 = new javax.swing.JLabel ();
        jLabel2.setText (org.openide.util.NbBundle.getBundle(ParamsPanel.class).getString("ParamsPanel.jLabel2.text"));

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.insets = new java.awt.Insets (0, 8, 0, 8);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (jLabel2, gridBagConstraints3);

        jScrollPane3 = new javax.swing.JScrollPane ();

        codePane = new javax.swing.JEditorPane ();
        codePane.setContentType ("text/x-java");
        codePane.setFont (new java.awt.Font ("Courier New", 0, 11));
        codePane.addFocusListener (new java.awt.event.FocusAdapter () {
                                       public void focusLost (java.awt.event.FocusEvent evt) {
                                           codePaneFocusLost (evt);
                                       }
                                   }
                                  );

        jScrollPane3.setViewportView (codePane);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints3.insets = new java.awt.Insets (0, 8, 8, 8);
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        jPanel2.add (jScrollPane3, gridBagConstraints3);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (jPanel2, gridBagConstraints1);

    }//GEN-END:initComponents

    private void codePaneFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_codePaneFocusLost
        commitChanges();
    }//GEN-LAST:event_codePaneFocusLost

    private void removeParamButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeParamButtonActionPerformed
        int index = paramsList.getSelectedIndex();
        if (index == -1) return;
        arguments.remove(index);
        //comments.remove(index);
        getListModel().fireIntervalRemoved(index, index);
        if (index >= arguments.size()) index--;
        editingRow = -1; // so the row is not updated
        editRow(index);
    }//GEN-LAST:event_removeParamButtonActionPerformed

    private void addParamButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addParamButtonActionPerformed
        arguments.add("");
        //comments.add("");
        getListModel().fireIntervalAdded(getListModel().getSize() - 1, getListModel().getSize() - 1);
        editRow(getListModel().getSize() - 1);
    }//GEN-LAST:event_addParamButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea mainCommentTextArea;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList paramsList;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton addParamButton;
    private javax.swing.JButton removeParamButton;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JEditorPane codePane;
    // End of variables declaration//GEN-END:variables

    /** List model for the list of parameters */
    protected class ParamsListModel extends AbstractListModel {

        static final long serialVersionUID =6832148996617470334L;
        public ParamsListModel () {
        }

        public int getSize() {
            return arguments.size();
        }

        public Object getElementAt(int index) {
            return "{" + index + "}  " + (String)arguments.get(index);
        }

        public void fireContentsChanged(int index0, int index1) {
            super.fireContentsChanged(this, index0, index1);
        }

        public void fireIntervalAdded(int index0, int index1) {
            super.fireIntervalAdded(this, index0, index1);
        }

        public void fireIntervalRemoved(int index0, int index1) {
            super.fireIntervalRemoved(this, index0, index1);
        }
    }
}