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

import java.io.IOException;
import java.util.ResourceBundle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dialog;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.openide.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class NewPropertyDialog {

    private JTextField tfComment;
    private JTextField tfKey;
    private JTextField tfValue;
    /** true if ok was pressed */
    private boolean okPressed;
    private Dialog dialog;

    /** Consturcts the dialog if it does not exist and returns the instance */
    Dialog getDialog () {
        if (dialog == null) {
            dialog = getNewPropertyDialog();
            dialog.pack ();
        }
        return dialog;
    }

    public String getCommentText () {
        return tfComment.getText ();
    }

    public void setCommentText (String text) {
        tfComment.setText (text);
    }

    public String getKeyText () {
        return tfKey.getText ();
    }

    public void setKeyText (String text) {
        tfKey.setText (text);
    }

    public String getValueText () {
        return tfValue.getText ();
    }

    public void setValueText (String text) {
        tfValue.setText (text);
    }

    /** @return true if OK button was pressed in dialog,
    * false otherwise. */
    public boolean getOKPressed () {
        return okPressed;
    }

    public void focusKey() {
        tfKey.requestFocus ();
    }

    /** Constructs managed dialog instance using TopManager.createDialog
    * and returnrs it */
    private Dialog getNewPropertyDialog() {
        ResourceBundle bundle = NbBundle.getBundle(NewPropertyDialog.class);

        JLabel textLabel;
        JPanel p = new JPanel ();
        GridBagLayout gridBag;
        GridBagConstraints c = new GridBagConstraints ();
        p.setLayout (gridBag = new GridBagLayout ());
        p.setBorder (new EmptyBorder (5, 5, 2, 5));

        c.anchor = GridBagConstraints.WEST;
        c.insets = new java.awt.Insets (0, 0, 3, 0);
        textLabel = new JLabel (bundle.getString ("CTL_PropertyComment"));
        gridBag.setConstraints (textLabel, c);
        p.add (textLabel);
        textLabel.setBorder (new EmptyBorder (0, 0, 0, 10));

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        //      tfComment = new JTextArea ("", 2, 25);
        tfComment = new JTextField (25);
        gridBag.setConstraints (tfComment, c);
        p.add (tfComment);

        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridwidth = GridBagConstraints.RELATIVE;
        textLabel = new JLabel (bundle.getString ("CTL_PropertyKey"));
        gridBag.setConstraints (textLabel, c);
        p.add (textLabel);
        textLabel.setBorder (new EmptyBorder (0, 0, 0, 10));

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        tfKey = new JTextField (25);
        gridBag.setConstraints (tfKey, c);
        p.add (tfKey);
        tfComment.setBorder(tfKey.getBorder());

        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridwidth = GridBagConstraints.RELATIVE;
        textLabel = new JLabel (bundle.getString ("CTL_PropertyValue"));
        gridBag.setConstraints (textLabel, c);
        p.add (textLabel);
        textLabel.setBorder (new EmptyBorder (0, 0, 0, 10));

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        tfValue = new JTextField (25);
        gridBag.setConstraints (tfValue, c);
        p.add (tfValue);


        tfKey.requestFocus ();

        // create dialog descriptor, create & return the dialog
        DialogDescriptor descriptor =
            new DialogDescriptor(p, bundle.getString("CTL_NewPropertyTitle"), true,
                                 new ActionListener(){
                                     /** Called when some dialog button was pressed */
                                     public void actionPerformed (ActionEvent evt) {
                                         okPressed = DialogDescriptor.OK_OPTION.equals(evt.getSource ());
                                         if (okPressed && (getKeyText().trim().length() == 0)) {
                                             TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                                                                NbBundle.getBundle(NewPropertyDialog.class).getString ("ERR_PropertyEmpty"),
                                                                                NotifyDescriptor.ERROR_MESSAGE));
                                             focusKey();
                                         }
                                         else
                                             dialog.setVisible (false);
                                         // dialog.dispose ();
                                     }
                                 }
                                );
        descriptor.setHelpCtx (new HelpCtx (NewPropertyDialog.class.getName () + ".dialog"));
        return TopManager.getDefault().createDialog(descriptor);
    }

}

/*
 * <<Log>>
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         7/8/99   Jesse Glick     Context help.
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/8/99   Petr Jiricka    
 *  2    Gandalf   1.1         6/8/99   Petr Jiricka    
 *  1    Gandalf   1.0         6/6/99   Petr Jiricka    
 * $
 */
