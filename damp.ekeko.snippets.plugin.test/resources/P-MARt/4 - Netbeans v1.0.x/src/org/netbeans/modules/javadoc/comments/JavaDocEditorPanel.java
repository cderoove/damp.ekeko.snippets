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

package org.netbeans.modules.javadoc.comments;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.TitledBorder;

import org.openide.src.MemberElement;
import org.openide.src.JavaDoc;
import org.openide.src.JavaDocTag;
import org.openide.src.SourceException;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.HelpCtx;

/**
 *
 * @author  phrebejk
 * @version
 */
public class JavaDocEditorPanel extends javax.swing.JPanel
    implements EnhancedCustomPropertyEditor {

    private JavaDoc javaDoc;
    private DefaultListModel listModel;


    private EmptyTagPanel emptyTagPanel;
    private StandardTagPanel standardTagPanel;
    private SeeTagPanel seeTagPanel;
    private ParamTagPanel paramTagPanel;
    private ThrowsTagPanel throwsTagPanel;
    private SerialFieldTagPanel serialFieldTagPanel;

    private NewTagDialog newTagDialog;

    private MemberElement element;

    private MnemonicsDistributor mnemonicsDistributor;

    static final long serialVersionUID =7005703844831686911L;
    /** Creates new form JavaDocEditorPanel */
    public JavaDocEditorPanel( JavaDoc javaDoc, MemberElement element ) {
        initComponents ();

        this.element = element;
        this.javaDoc = javaDoc;

        // Set the text of comment into text area

        // Buttons mnemonics
        boldButton.setMnemonic( 'B' );
        italicButton.setMnemonic( 'I' );
        underlineButton.setMnemonic( 'U' );
        codeButton.setMnemonic( 'C' );
        preButton.setMnemonic( 'P' );
        linkButton.setMnemonic( 'L' );

        enableButtons( false );

        mnemonicsDistributor = new MnemonicsDistributor();

        commentTextArea.setContentType( "text/html"); // NOI18N
        mnemonicsDistributor.registerComponent( commentTextArea );

        if ( javaDoc != null ) {
            this.javaDoc = javaDoc;
            commentTextArea.setText( javaDoc.getText()  );
            //commentTextArea.setText( removeWhiteSpaces( javaDoc.getText() ) );
        }


        // Make the list to select only one line and listen to selections

        tagList.setVisibleRowCount(4);
        tagList.getSelectionModel().setSelectionMode( javax.swing.ListSelectionModel.SINGLE_SELECTION );
        tagList.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged( ListSelectionEvent evt ) {
                    tagSelection( evt );
                }
            } );


        // Put the tags into listbox

        listModel = new DefaultListModel();
        tagList.setModel( listModel );

        if ( javaDoc != null ) {
            JavaDocTag tags[] = javaDoc.getTags();
            for( int i = 0; i < tags.length; i++ ) {
                listModel.addElement( tags[i] );
            }

            if ( listModel.getSize() < 0 ) {
                tagList.setSelectedIndex( 0 );
            }
        }

        // i18n

        textPanel.setBorder (new javax.swing.border.TitledBorder(
                                 new javax.swing.border.EtchedBorder(),
                                 org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.textPanel.title")));


        tagPanel.setBorder (new javax.swing.border.TitledBorder(
                                new javax.swing.border.EtchedBorder(),
                                org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.tagPanel.title")));


        // Add panels for different tag types

        emptyTagPanel = new EmptyTagPanel( element, this );
        tagParamPanel.add( emptyTagPanel, emptyTagPanel.getCardName()  );

        standardTagPanel = new StandardTagPanel( element, this );
        tagParamPanel.add( standardTagPanel, standardTagPanel.getCardName() );

        seeTagPanel = new SeeTagPanel( element, this );
        tagParamPanel.add( seeTagPanel, seeTagPanel.getCardName() );


        paramTagPanel = new ParamTagPanel( element, this );
        tagParamPanel.add( paramTagPanel, paramTagPanel.getCardName() );

        throwsTagPanel = new ThrowsTagPanel( element, this );
        tagParamPanel.add( throwsTagPanel, throwsTagPanel.getCardName() );

        serialFieldTagPanel = new SerialFieldTagPanel( element, this );
        tagParamPanel.add( serialFieldTagPanel, serialFieldTagPanel.getCardName() );

        HelpCtx.setHelpIDString (this, JavaDocEditorPanel.class.getName ());
    }

    public java.awt.Dimension getPreferredSize() {
        return new Dimension( 600, 520 );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        textPanel = new javax.swing.JPanel ();
        commentScrollPane = new javax.swing.JScrollPane ();
        commentTextArea = new javax.swing.JEditorPane ();
        tagPanel = new javax.swing.JPanel ();
        jPanel2 = new javax.swing.JPanel ();
        tagScrollPane = new javax.swing.JScrollPane ();
        tagList = new javax.swing.JList ();
        oneTagPanel = new javax.swing.JPanel ();
        tagParamPanel = new javax.swing.JPanel ();
        jPanel3 = new javax.swing.JPanel ();
        newButton = new javax.swing.JButton ();
        deleteButton = new javax.swing.JButton ();
        jSeparator2 = new javax.swing.JSeparator ();
        moveUpButton = new javax.swing.JButton ();
        moveDownButton = new javax.swing.JButton ();
        jPanel4 = new javax.swing.JPanel ();
        htmlToolBar = new javax.swing.JPanel ();
        boldButton = new javax.swing.JButton ();
        italicButton = new javax.swing.JButton ();
        underlineButton = new javax.swing.JButton ();
        codeButton = new javax.swing.JButton ();
        preButton = new javax.swing.JButton ();
        linkButton = new javax.swing.JButton ();
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        setPreferredSize (new java.awt.Dimension(459, 300));
        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        setMinimumSize (new java.awt.Dimension(241, 300));

        textPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints2;


        commentTextArea.setPreferredSize (new java.awt.Dimension(200, 150));
        commentTextArea.setContentType ("text/html"); // NOI18N
        commentTextArea.addFocusListener (new java.awt.event.FocusAdapter () {
                                              public void focusGained (java.awt.event.FocusEvent evt) {
                                                  commentTextAreaFocusGained (evt);
                                              }
                                              public void focusLost (java.awt.event.FocusEvent evt) {
                                                  commentTextAreaFocusLost (evt);
                                              }
                                          }
                                         );

        commentScrollPane.setViewportView (commentTextArea);

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.insets = new java.awt.Insets (0, 5, 5, 5);
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        textPanel.add (commentScrollPane, gridBagConstraints2);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weighty = 1.0;
        add (textPanel, gridBagConstraints1);

        tagPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints3;

        jPanel2.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints4;

        tagScrollPane.setMinimumSize (new java.awt.Dimension(24, 100));


        tagScrollPane.setViewportView (tagList);

        gridBagConstraints4 = new java.awt.GridBagConstraints ();
        gridBagConstraints4.gridwidth = 0;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints4.weightx = 1.0;
        gridBagConstraints4.weighty = 1.0;
        jPanel2.add (tagScrollPane, gridBagConstraints4);

        oneTagPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints5;

        tagParamPanel.setLayout (new java.awt.CardLayout ());

        gridBagConstraints5 = new java.awt.GridBagConstraints ();
        gridBagConstraints5.gridwidth = 0;
        gridBagConstraints5.gridheight = 0;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints5.weightx = 1.0;
        gridBagConstraints5.weighty = 0.4;
        oneTagPanel.add (tagParamPanel, gridBagConstraints5);

        gridBagConstraints4 = new java.awt.GridBagConstraints ();
        gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints4.weightx = 1.0;
        gridBagConstraints4.weighty = 1.0;
        jPanel2.add (oneTagPanel, gridBagConstraints4);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints3.insets = new java.awt.Insets (0, 5, 0, 0);
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        tagPanel.add (jPanel2, gridBagConstraints3);

        jPanel3.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints6;

        newButton.setText (org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.newButton.text"));
        newButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             newTagButtonActionPerformed (evt);
                                         }
                                     }
                                    );

        gridBagConstraints6 = new java.awt.GridBagConstraints ();
        gridBagConstraints6.gridwidth = 0;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add (newButton, gridBagConstraints6);

        deleteButton.setText (org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.deleteButton.text"));
        deleteButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                delTagButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        gridBagConstraints6 = new java.awt.GridBagConstraints ();
        gridBagConstraints6.gridwidth = 0;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add (deleteButton, gridBagConstraints6);

        jSeparator2.setMinimumSize (new java.awt.Dimension(1, 2));

        gridBagConstraints6 = new java.awt.GridBagConstraints ();
        gridBagConstraints6.gridwidth = 0;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints6.insets = new java.awt.Insets (0, 2, 0, 2);
        jPanel3.add (jSeparator2, gridBagConstraints6);

        moveUpButton.setText (org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.moveUpButton.text"));
        moveUpButton.setActionCommand ("UP"); // NOI18N
        moveUpButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                moveTagButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        gridBagConstraints6 = new java.awt.GridBagConstraints ();
        gridBagConstraints6.gridwidth = 0;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints6.insets = new java.awt.Insets (5, 0, 0, 0);
        jPanel3.add (moveUpButton, gridBagConstraints6);

        moveDownButton.setText (org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.moveDownButton.text"));
        moveDownButton.setActionCommand ("DOWN"); // NOI18N
        moveDownButton.addActionListener (new java.awt.event.ActionListener () {
                                              public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                  moveTagButtonActionPerformed (evt);
                                              }
                                          }
                                         );

        gridBagConstraints6 = new java.awt.GridBagConstraints ();
        gridBagConstraints6.gridwidth = 0;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add (moveDownButton, gridBagConstraints6);


        gridBagConstraints6 = new java.awt.GridBagConstraints ();
        gridBagConstraints6.gridwidth = 0;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints6.weighty = 1.0;
        jPanel3.add (jPanel4, gridBagConstraints6);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints3.insets = new java.awt.Insets (0, 5, 0, 5);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTH;
        tagPanel.add (jPanel3, gridBagConstraints3);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weighty = 1.0;
        add (tagPanel, gridBagConstraints1);

        htmlToolBar.setLayout (new java.awt.GridLayout (1, 6));

        boldButton.setHorizontalTextPosition (javax.swing.SwingConstants.CENTER);
        boldButton.setMaximumSize (new java.awt.Dimension(59, 27));
        boldButton.setMinimumSize (new java.awt.Dimension(32, 27));
        boldButton.setText (org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.boldButton.text"));
        boldButton.setActionCommand ("B"); // NOI18N
        boldButton.setRequestFocusEnabled (false);
        boldButton.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              formatButtonActionPerformed (evt);
                                          }
                                      }
                                     );

        htmlToolBar.add (boldButton);

        italicButton.setHorizontalTextPosition (javax.swing.SwingConstants.CENTER);
        italicButton.setMaximumSize (new java.awt.Dimension(57, 27));
        italicButton.setMinimumSize (new java.awt.Dimension(32, 27));
        italicButton.setText (org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.italicButton.text"));
        italicButton.setActionCommand ("I"); // NOI18N
        italicButton.setRequestFocusEnabled (false);
        italicButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                formatButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        htmlToolBar.add (italicButton);

        underlineButton.setHorizontalTextPosition (javax.swing.SwingConstants.CENTER);
        underlineButton.setMaximumSize (new java.awt.Dimension(61, 27));
        underlineButton.setMinimumSize (new java.awt.Dimension(32, 27));
        underlineButton.setText (org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.underlineButton.text"));
        underlineButton.setActionCommand ("U"); // NOI18N
        underlineButton.setRequestFocusEnabled (false);
        underlineButton.addActionListener (new java.awt.event.ActionListener () {
                                               public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                   formatButtonActionPerformed (evt);
                                               }
                                           }
                                          );

        htmlToolBar.add (underlineButton);

        codeButton.setHorizontalTextPosition (javax.swing.SwingConstants.CENTER);
        codeButton.setMaximumSize (new java.awt.Dimension(83, 27));
        codeButton.setMinimumSize (new java.awt.Dimension(32, 27));
        codeButton.setText (org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.codeButton.text"));
        codeButton.setActionCommand ("CODE"); // NOI18N
        codeButton.setRequestFocusEnabled (false);
        codeButton.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              formatButtonActionPerformed (evt);
                                          }
                                      }
                                     );

        htmlToolBar.add (codeButton);

        preButton.setHorizontalTextPosition (javax.swing.SwingConstants.CENTER);
        preButton.setMaximumSize (new java.awt.Dimension(73, 27));
        preButton.setMinimumSize (new java.awt.Dimension(32, 27));
        preButton.setText (org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.preButton.text"));
        preButton.setActionCommand ("PRE"); // NOI18N
        preButton.setRequestFocusEnabled (false);
        preButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             formatButtonActionPerformed (evt);
                                         }
                                     }
                                    );

        htmlToolBar.add (preButton);

        linkButton.setHorizontalTextPosition (javax.swing.SwingConstants.CENTER);
        linkButton.setMaximumSize (new java.awt.Dimension(77, 27));
        linkButton.setMinimumSize (new java.awt.Dimension(32, 27));
        linkButton.setText (org.openide.util.NbBundle.getBundle(JavaDocEditorPanel.class).getString("CTL_JavaDocEditorPanel.linkButton.text"));
        linkButton.setActionCommand ("link"); // NOI18N
        linkButton.setRequestFocusEnabled (false);
        linkButton.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              formatButtonActionPerformed (evt);
                                          }
                                      }
                                     );

        htmlToolBar.add (linkButton);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (5, 5, 0, 5);
        gridBagConstraints1.weightx = 1.0;
        add (htmlToolBar, gridBagConstraints1);

    }//GEN-END:initComponents

    private void commentTextAreaFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commentTextAreaFocusLost
        enableButtons( false );
    }//GEN-LAST:event_commentTextAreaFocusLost

    private void commentTextAreaFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commentTextAreaFocusGained
        enableButtons( true );
    }//GEN-LAST:event_commentTextAreaFocusGained

    private void newTagButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTagButtonActionPerformed
        if ( newTagDialog == null )
            newTagDialog = new NewTagDialog( new java.awt.Frame (), true, element);

        newTagDialog.show();
        JavaDocTag tag = newTagDialog.getResult();

        if ( tag != null ) {
            listModel.addElement( tag );
            tagList.ensureIndexIsVisible( listModel.getSize() );
            tagList.setSelectedIndex( listModel.getSize() - 1 );
            tagList.grabFocus();
            /*
            tagScrollPane.revalidate();
            getPanelForTag( tag ).grabFirstFocus();
            */
        }

    }//GEN-LAST:event_newTagButtonActionPerformed
    /** Deletes the actual row */
    private void delTagButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delTagButtonActionPerformed
        int sel = tagList.getMinSelectionIndex();

        if ( sel != -1 )
            listModel.removeElementAt( sel );

        if ( listModel.getSize() > 0 )
            tagList.setSelectedIndex( sel == listModel.getSize() ? sel - 1 : sel );
        else {
            CardLayout layout = (CardLayout)tagParamPanel.getLayout();
            layout.show( tagParamPanel, emptyTagPanel.getCardName() );
        }
    }//GEN-LAST:event_delTagButtonActionPerformed

    private void chgTagButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chgTagButtonActionPerformed
        commitTagChange();
    }//GEN-LAST:event_chgTagButtonActionPerformed


    private void moveTagButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveTagButtonActionPerformed
        // Add your handling code here:
        if ( evt.getActionCommand().equals( "UP" ) ) { // NOI18N
            int selIndex = tagList.getMinSelectionIndex();
            if ( selIndex > 0 ) {
                Object tag = listModel.get( selIndex );
                listModel.removeElementAt( selIndex );
                listModel.insertElementAt( tag, selIndex - 1 );
                tagList.setSelectedIndex( selIndex - 1 );
            }
        }
        else if ( evt.getActionCommand().equals( "DOWN" ) ) { // NOI18N
            int selIndex = tagList.getMinSelectionIndex();
            if ( selIndex < listModel.getSize() - 1 ) {
                Object tag = listModel.get( selIndex );
                listModel.removeElementAt( selIndex );
                listModel.insertElementAt( tag, selIndex + 1 );
                tagList.setSelectedIndex( selIndex + 1 );
            }
        }
    }//GEN-LAST:event_moveTagButtonActionPerformed

    private void formatButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatButtonActionPerformed
        String begTag;
        String endTag;
        String command = evt.getActionCommand();

        if ( command.equals( "link" ) ) { // NOI18N
            begTag = "{@link "; // NOI18N
            endTag = "}"; // NOI18N
        }
        else {
            begTag = "<" + command + ">"; // NOI18N
            endTag = "</" + command + ">"; // NOI18N
        }

        if ( commentTextArea.hasFocus() ) {
            int caretPosition = commentTextArea.getCaretPosition();
            /*
            StringBuffer sb = new StringBuffer( commentTextArea.getText() );
            sb.insert( commentTextArea.getSelectionStart(), begTag );
            sb.insert( commentTextArea.getSelectionEnd(), endTag  );
            commentTextArea.setText( sb.toString() ); 
            */
            try {
                commentTextArea.getDocument().insertString( commentTextArea.getSelectionStart(), begTag, null );
                commentTextArea.getDocument().insertString( commentTextArea.getSelectionEnd(), endTag, null );
                commentTextArea.setCaretPosition( caretPosition + 2 + evt.getActionCommand().length() );
            }
            catch ( javax.swing.text.BadLocationException e ) {
                //System.out.println(e );
            }
        }
        else {
            JavaDocTag tag = (JavaDocTag)listModel.get( tagList.getMinSelectionIndex() ) ;
            TagPanel tagPanel = getPanelForTag( tag );
            tagPanel.handleFormatButton( begTag, endTag );
        }
    }//GEN-LAST:event_formatButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel textPanel;
    private javax.swing.JScrollPane commentScrollPane;
    private javax.swing.JEditorPane commentTextArea;
    private javax.swing.JPanel tagPanel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane tagScrollPane;
    private javax.swing.JList tagList;
    private javax.swing.JPanel oneTagPanel;
    private javax.swing.JPanel tagParamPanel;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton newButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel htmlToolBar;
    private javax.swing.JButton boldButton;
    private javax.swing.JButton italicButton;
    private javax.swing.JButton underlineButton;
    private javax.swing.JButton codeButton;
    private javax.swing.JButton preButton;
    private javax.swing.JButton linkButton;
    // End of variables declaration//GEN-END:variables


    /** gets the text of comment */
    String getRawText() {
        StringBuffer sb = new StringBuffer( 1000 );

        try {
            sb.append( commentTextArea.getDocument().getText( 0, commentTextArea.getDocument().getLength() ) );
        }
        catch ( javax.swing.text.BadLocationException ex ) {
            System.err.println( ex );
        }
        sb.append( '\n' );

        for ( int i = 0; i < listModel.getSize(); i++ ) {
            JavaDocTag tag = (( JavaDocTag )listModel.get( i ));
            sb.append( " " + tag.name() + " " + tag.text() ); // NOI18N
            sb.append( '\n' );
        }

        return sb.toString();
    }


    /**
    * @return Returns the property value that is result of the CustomPropertyEditor.
    * @exception InvalidStateException when the custom property editor does not represent valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue () throws IllegalStateException {

        try {
            javaDoc.setRawText( getRawText() );
        }
        catch ( SourceException ex ) {
            throw new IllegalStateException();

        }

        return javaDoc;
    }

    /** Called when new tag is selected */
    private void tagSelection( ListSelectionEvent evt ) {

        TagPanel tagPanel;
        int sel = tagList.getMinSelectionIndex();

        if ( sel < 0 ) {
            tagPanel = emptyTagPanel;
        }
        else {
            JavaDocTag tag = (JavaDocTag)listModel.get( tagList.getMinSelectionIndex() ) ;

            tagPanel = getPanelForTag( tag );
            tagPanel.setData( tag );
        }
        CardLayout layout = (CardLayout)tagParamPanel.getLayout();
        layout.show( tagParamPanel, tagPanel.getCardName() );
    }


    TagPanel getPanelForTag( JavaDocTag tag ) {

        if ( tag instanceof JavaDocTag.Param )
            return paramTagPanel;
        else if ( tag instanceof JavaDocTag.Throws )
            return throwsTagPanel;
        else if ( tag instanceof JavaDocTag.SerialField )
            return serialFieldTagPanel;
        else if ( tag instanceof JavaDocTag.See )
            return seeTagPanel;
        else
            return standardTagPanel;

    }

    /** Removes the whitespaces after new line characters */

    private String removeWhiteSpaces( String text ) {
        StringBuffer sb = new StringBuffer( text );
        StringBuffer newSb = new StringBuffer( text.length() );


        boolean inWhite = false;
        for( int i = 0; i < sb.length(); i++ ) {

            if ( inWhite ) {
                if ( sb.charAt(i) == '\n' || !Character.isWhitespace( sb.charAt( i ) ) ) {
                    //newSb.append( sb.charAt( i ) );
                    inWhite = false;
                }
                else {
                    continue;
                }
            }

            newSb.append( sb.charAt( i ) );
            if ( sb.charAt( i ) == '\n' ) {
                inWhite = true;
            }
        }

        return newSb.toString();
    }

    /** Changes the tag in the tag list */
    void commitTagChange() {

        TagPanel tagPanel;
        int sel = tagList.getMinSelectionIndex();

        if ( sel < 0 ) {
            return;
        }
        else {
            JavaDocTag tag = (JavaDocTag)listModel.get( tagList.getMinSelectionIndex() );
            tagPanel = getPanelForTag( tag );
            JavaDocTag newTag = tagPanel.getTag( tag.name() );
            listModel.removeElementAt( sel );
            listModel.insertElementAt( newTag, sel );
            tagList.setSelectedIndex( sel );
        }
    }

    void enableButtons( boolean enable ) {
        boldButton.setEnabled( enable );
        italicButton.setEnabled( enable );
        underlineButton.setEnabled( enable );
        codeButton.setEnabled( enable );
        preButton.setEnabled( enable );
        linkButton.setEnabled( enable );
    }

    void registerComponent( java.awt.Component component) {
        mnemonicsDistributor.registerComponent( component );
    }


    /** This innerclass serves as workaround for handling alt key mnemonics
     */
    class MnemonicsDistributor extends java.awt.event.KeyAdapter {

        MnemonicsDistributor() {
        }


        public void keyPressed( java.awt.event.KeyEvent e ) {

            javax.swing.KeyStroke ks = javax.swing.KeyStroke.getKeyStrokeForEvent( e );

            if ( ( ks.getModifiers() & java.awt.event.InputEvent.ALT_MASK ) != 0 ) {

                switch ( ks.getKeyCode() ) {
                case  KeyEvent.VK_B:
                    boldButton.doClick();
                    e.consume();
                    break;
                case  KeyEvent.VK_I:
                    italicButton.doClick();
                    e.consume();
                    break;
                case  KeyEvent.VK_U:
                    underlineButton.doClick();
                    e.consume();
                    break;
                case  KeyEvent.VK_C:
                    codeButton.doClick();
                    e.consume();
                    break;
                case  KeyEvent.VK_P:
                    preButton.doClick();
                    e.consume();
                    break;
                case  KeyEvent.VK_L:
                    linkButton.doClick();
                    e.consume();
                    break;
                }
            }
        }

        void registerComponent( java.awt.Component component ) {
            component.addKeyListener(this);
        }

    }

}

/*
 * Log
 *  12   Gandalf   1.11        1/12/00  Petr Hrebejk    i18n mk2
 *  11   Gandalf   1.10        1/12/00  Petr Hrebejk    i18n
 *  10   Gandalf   1.9         1/4/00   Petr Hrebejk    Bug fix 5007
 *  9    Gandalf   1.8         11/27/99 Patrik Knakal   
 *  8    Gandalf   1.7         11/10/99 Petr Hrebejk    Workaround for catching 
 *       the mnemonics of HTML tag buttons
 *  7    Gandalf   1.6         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  4    Gandalf   1.3         9/16/99  Petr Hrebejk    Tag descriptions editing
 *       in HTML editor + localization
 *  3    Gandalf   1.2         8/19/99  Miloslav Metelka html mime type
 *  2    Gandalf   1.1         8/13/99  Petr Hrebejk    Window serialization 
 *       added & Tag change button in Jdoc editor removed 
 *  1    Gandalf   1.0         7/9/99   Petr Hrebejk    
 * $
 */
