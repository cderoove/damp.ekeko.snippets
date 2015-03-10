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


import java.awt.Component;
import java.awt.Image;
import java.awt.Font;
import java.util.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.DefaultListCellRenderer;

import org.openide.util.NbBundle;

/** Paints the list items in the ResultsListBox
 * @author  phrebejk
 * @version 
 */
public class ResultListCellRenderer extends javax.swing.JPanel
    implements javax.swing.ListCellRenderer{

    static final long serialVersionUID =-3181200208271061471L;

    private static final ResourceBundle bundle = NbBundle.getBundle( ResultListCellRenderer.class );

    private static final String SPACE = " "; //NOI18N

    private static final String BAD_DOWNLOAD = SPACE + bundle.getString("CTL_BAD_DOWNLOAD") + SPACE;
    private static final String CORRUPTED = SPACE + bundle.getString("CTL_CORRUPTED") + SPACE;
    private static final String NOT_SIGNED = SPACE + bundle.getString("CTL_NOT_SIGNED") + SPACE;
    private static final String SIGNED = SPACE + bundle.getString("CTL_SIGNED") + SPACE;
    private static final String TRUSTED = SPACE + bundle.getString("CTL_TRUSTED") + SPACE;


    private static ImageIcon okIco = new ImageIcon (SelectedListCellRenderer.class.getResource ("/org/netbeans/modules/autoupdate/resources/ok.gif")); // NOI18N
    private static ImageIcon badIco = new ImageIcon (SelectedListCellRenderer.class.getResource ("/org/netbeans/modules/autoupdate/resources/failed.gif")); // NOI18N
    private static ImageIcon signedIco = new ImageIcon (SelectedListCellRenderer.class.getResource ("/org/netbeans/modules/autoupdate/resources/signed.gif")); // NOI18N
    private static ImageIcon unsignedIco = new ImageIcon (SelectedListCellRenderer.class.getResource ("/org/netbeans/modules/autoupdate/resources/unsigned.gif")); // NOI18N


    /** The one instalnce of the renderer in the system */
    private static ResultListCellRenderer renderer = null;


    public Component getListCellRendererComponent( JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        if ( renderer == null ) {
            renderer = new ResultListCellRenderer();
        }

        if ( ((ModuleUpdate)value).isInstallApproved() )
            renderer.iconLabel.setIcon( okIco );
        else {
            switch ( ((ModuleUpdate)value).getSecurity() ) {
            case SignVerifier.BAD_DOWNLOAD:
            case SignVerifier.CORRUPTED:
                renderer.iconLabel.setIcon( badIco );
                break;
            case SignVerifier.NOT_SIGNED:
                renderer.iconLabel.setIcon( unsignedIco );
                break;
            case SignVerifier.SIGNED:
                renderer.iconLabel.setIcon( signedIco );
                break;
            default:
                renderer.iconLabel.setIcon( badIco );
                break;
            }
        }

        String sec = ""; // NOI18N
        switch ( ((ModuleUpdate)value).getSecurity() ) {
        case SignVerifier.BAD_DOWNLOAD:
            sec = BAD_DOWNLOAD;
            break;
        case SignVerifier.CORRUPTED:
            sec = CORRUPTED;
            break;
        case SignVerifier.NOT_SIGNED:
            sec = NOT_SIGNED;
            break;
        case SignVerifier.SIGNED:
            StringBuffer sb = new StringBuffer( SIGNED );
            sb.append( SPACE + bundle.getString("CTL_By"));
            Collection certs = ((ModuleUpdate)value).getCerts();
            Iterator it = certs.iterator();
            while ( it.hasNext() ) {
                Certificate cert = (Certificate)it.next();
                if ( cert instanceof X509Certificate ) {
                    sb.append( " " ).append( ((X509Certificate)cert).getSubjectDN().getName() ); //NOI18N
                }
            }

            sec = sb.toString();
            break;
        case SignVerifier.TRUSTED:
            sec = TRUSTED;
            break;
        }

        renderer.nameLabel.setText( ((ModuleUpdate)value).getName() +
                                    "  " + bundle.getString( "CTL_Version" ) +
                                    ((ModuleUpdate)value).getRemoteModule().getSpecVersion());
        renderer.securityLabel.setText( sec );

        if ( isSelected ) {
            renderer.setBackground ((java.awt.Color) javax.swing.UIManager.getDefaults ().get ("List.selectionBackground")); // NOI18N
            renderer.nameLabel.setForeground((java.awt.Color) javax.swing.UIManager.getDefaults ().get ("List.selectionForeground")); // NOI18N
            renderer.securityLabel.setForeground((java.awt.Color) javax.swing.UIManager.getDefaults ().get ("List.selectionForeground")); // NOI18N
        }
        else {
            renderer.setBackground ((java.awt.Color) javax.swing.UIManager.getDefaults ().get ("List.background")); // NOI18N
            renderer.nameLabel.setForeground((java.awt.Color) javax.swing.UIManager.getDefaults ().get ("List.Foreground")); // NOI18N
            renderer.securityLabel.setForeground((java.awt.Color) javax.swing.UIManager.getDefaults ().get ("List.Foreground")); // NOI18N
        }

        renderer.invalidate();

        return renderer;

    }


    /** Creates new form ResultListCellRenderer */
    public ResultListCellRenderer() {
        initComponents ();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        iconLabel = new javax.swing.JLabel ();
        nameLabel = new javax.swing.JLabel ();
        securityLabel = new javax.swing.JLabel ();
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        setBackground ((java.awt.Color) javax.swing.UIManager.getDefaults ().get ("List.background")); // NOI18N



        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        add (iconLabel, gridBagConstraints1);

        nameLabel.setFont (nameLabel.getFont().deriveFont( Font.BOLD ));


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (0, 6, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (nameLabel, gridBagConstraints1);



        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (0, 6, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (securityLabel, gridBagConstraints1);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel iconLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel securityLabel;
    // End of variables declaration//GEN-END:variables

}