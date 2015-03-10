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

package org.netbeans.modules.editor.options;

import java.util.Vector;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.KeyStroke;

import org.openide.util.NbBundle;

/**
 * This class could be used as input of sequence of KeyStrokes.
 * {@link #getKeySequence}
 * One instance could be reused.
 * {@link #clear}
 * Knows how to typeout the key sequence too.
 * {@link #keySequenceToString}
 * {@link #keyStrokeToString}
 * When actual keySequence changes, it fires PropertyChangeEvent
 * of property {@link #PROP_KEYSEQUENCE}.
 * There is additional label on the bottom, which could be set
 * with {@link #setInfoText} to pass some information to user.
 *
 * @author  Petr Nejedly
 */

public class KeySequenceInputPanel extends javax.swing.JPanel {

    public static String PROP_KEYSEQUENCE = "keySequence"; // NOI18N


    /**
     * @associates KeyStroke 
     */
    private Vector strokes = new Vector();
    private StringBuffer text = new StringBuffer();

    /** Creates new form KeySequenceInputPanel with empty sequence*/
    public KeySequenceInputPanel() {
        initComponents ();
    }

    /**
     * Clears actual sequence of KeyStrokes
     */
    public void clear() {
        strokes.clear();
        text.setLength( 0 );
        keySequenceInputField.setText( text.toString() );
        firePropertyChange( PROP_KEYSEQUENCE, null, null );
    }

    /*
     * Sets the text of JLabel locaten on the bottom of this panel
     */
    public void setInfoText( String s ) {
        collisionLabel.setText( s + ' ' ); // NOI18N
    }

    /**
     * Returns sequence of completed KeyStrokes as KeyStroke[]
     */
    public KeyStroke[] getKeySequence() {
        return (KeyStroke[])strokes.toArray( new KeyStroke[0] );
    }

    /**
     * Makes it trying to be bigger
     */
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        // if we are too small, make width equals about 40 chars in FixedSize font
        if( dim.width < 15*dim.height) dim.width = 15*dim.height;
        return dim;
    }

    /**
     * We're redirecting our focus to proper component.
     */
    public void requestFocus() {
        keySequenceInputField.requestFocus();
    }

    /**
     * Creates nice textual description of key sequence.
     * Single strokes are separated by spaces.
     */
    public static String keySequenceToString( KeyStroke[] seq ) {
        StringBuffer sb = new StringBuffer();
        for( int i=0; i<seq.length; i++ ) {
            if( i>0 ) sb.append( ' ' );  // NOI18N
            sb.append( keyStrokeToString( seq[i] ) );
        }
        return sb.toString();
    }

    /**
     * Creates nice textual description of KeyStroke.
     * Modifiers and an actual key label are concated by pluses
     */
    public static String keyStrokeToString( KeyStroke stroke ) {
        String modifText = KeyEvent.getKeyModifiersText( stroke.getModifiers() );
        String keyText = KeyEvent.getKeyText( stroke.getKeyCode() );
        if( modifText.length() > 0 ) return modifText + '+' + keyText; // NOI18N
        else return keyText;
    }

    /**
     * Visual part and event handling:
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        keySequenceLabel = new javax.swing.JLabel ();
        keySequenceInputField = new javax.swing.JTextField ();
        collisionLabel = new javax.swing.JLabel ();
        setLayout (new java.awt.BorderLayout ());
        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));

        keySequenceLabel.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 8)));
        keySequenceLabel.setText (NbBundle.getBundle( KeySequenceInputPanel.class ).getString( "LBL_KSIP_Sequence" ));


        add (keySequenceLabel, java.awt.BorderLayout.WEST);

        keySequenceInputField.addKeyListener (new java.awt.event.KeyAdapter () {
                                                  public void keyTyped (java.awt.event.KeyEvent evt) {
                                                      keySequenceInputFieldKeyTyped (evt);
                                                  }
                                                  public void keyPressed (java.awt.event.KeyEvent evt) {
                                                      keySequenceInputFieldKeyPressed (evt);
                                                  }
                                                  public void keyReleased (java.awt.event.KeyEvent evt) {
                                                      keySequenceInputFieldKeyReleased (evt);
                                                  }
                                              }
                                             );


        add (keySequenceInputField, java.awt.BorderLayout.CENTER);

        collisionLabel.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 0, 0, 0)));
        collisionLabel.setText (" ");
        collisionLabel.setForeground (java.awt.Color.red);


        add (collisionLabel, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void keySequenceInputFieldKeyTyped (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keySequenceInputFieldKeyTyped
        evt.consume();
    }//GEN-LAST:event_keySequenceInputFieldKeyTyped

    private void keySequenceInputFieldKeyReleased (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keySequenceInputFieldKeyReleased
        evt.consume();
        keySequenceInputField.setText( text.toString() );
    }//GEN-LAST:event_keySequenceInputFieldKeyReleased

    private void keySequenceInputFieldKeyPressed (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keySequenceInputFieldKeyPressed
        evt.consume();

        String modif = KeyEvent.getKeyModifiersText( evt.getModifiers() );
        if( isModifier( evt.getKeyCode() ) ) {
            keySequenceInputField.setText( text.toString() + modif + '+' ); //NOI18N
        } else {
            KeyStroke stroke = KeyStroke.getKeyStrokeForEvent( evt );
            strokes.add( stroke );
            text.append( keyStrokeToString( stroke ) );
            text.append( ' ' );
            keySequenceInputField.setText( text.toString() );
            firePropertyChange( PROP_KEYSEQUENCE, null, null );
        }
    }//GEN-LAST:event_keySequenceInputFieldKeyPressed

    private boolean isModifier( int keyCode ) {
        return (keyCode == KeyEvent.VK_ALT) ||
               (keyCode == KeyEvent.VK_ALT_GRAPH) ||
               (keyCode == KeyEvent.VK_CONTROL) ||
               (keyCode == KeyEvent.VK_SHIFT) ||
               (keyCode == KeyEvent.VK_META);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel keySequenceLabel;
    private javax.swing.JTextField keySequenceInputField;
    private javax.swing.JLabel collisionLabel;
    // End of variables declaration//GEN-END:variables

}

/*
 * Log
 *  1    Gandalf-post-FCS1.0         2/28/00  Petr Nejedly    initial revision
 * $
 */