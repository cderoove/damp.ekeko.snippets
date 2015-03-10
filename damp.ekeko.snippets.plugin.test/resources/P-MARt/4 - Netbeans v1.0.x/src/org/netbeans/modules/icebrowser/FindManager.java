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

package org.netbeans.modules.icebrowser;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.JPanel;

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;


/**
* Support for Find Dialog for Ice browser.
*
* @author Jan Jancura
*/
class FindManager implements ActionListener, ActionPerformer {

    private static String         FIND_BUTT = NbBundle.getBundle (
                FindManager.class).getString ("CTL_Find");


    // variables .................................................................

    private Dialog                dialog;
    private int                   index = 0;
    private FindDialogPanel       panel;
    private IceBrowserImpl        browser;


    // init ......................................................................

    FindManager (IceBrowserImpl browser) {
        this.browser = browser;
    }


    // ActionPerformer implementation ............................................

    public void performAction (SystemAction action) {
        if (dialog == null)
            dialog = createDialog ();
        dialog.show ();
    }


    // ActionListener implementation ............................................

    /**
    * Called when some dialog button was pressed 
    */
    public void actionPerformed (ActionEvent evt) {
        if (FIND_BUTT.equals (evt.getActionCommand ())) {
            String text = ((String) panel.findWhat.getSelectedItem ()).trim ();
            put (text);
            int i = browser.getDocument ().search (
                        index,
                        text,
                        false
                    );
            if (i < 0) {
                /*        if ((index > 0) && panel.wrapSearch.isSelected ()) {
                          i = browser.getDocument ().search (
                            index, 
                            text, 
                            false
                          );
                          index = i;
                          return;
                        }*/
                index = 0;
            } else
                index = i;
            // in the current version of ICE the index do not works....
        } else {
            dialog.setVisible (false);
            index = 0;
        }
        // dialog.dispose ();
    }


    // other methods .....................................................................

    /**
    * Adds new string to find dialog's ComboBox.
    */
    private void put (String s) {
        int i, k = panel.findWhat.getItemCount ();
        for (i = 0; i < k; i++)
            if (panel.findWhat.getItemAt (i).equals (s))
                return;
        panel.findWhat.addItem (s);
    }

    /** Constructs managed dialog instance using TopManager.createDialog
    * and returnrs it */
    private Dialog createDialog () {
        ResourceBundle bundle = NbBundle.getBundle (FindManager.class);

        panel = new FindDialogPanel ();
        // create dialog descriptor, create & return the dialog
        DialogDescriptor descriptor = new DialogDescriptor (
                                          panel,
                                          bundle.getString ("CTL_Find_Dialog_Title"),
                                          true,                                      // modal
                                          new Object[] {
                                              bundle.getString ("CTL_Find"),
                                              DialogDescriptor.CANCEL_OPTION           // buttons
                                          },
                                          bundle.getString ("CTL_Find"),             // init. value
                                          DialogDescriptor.RIGHT_ALIGN,              // buttons align
                                          new HelpCtx (FindManager.class.getName () + ".dialog"), // NOI18N
                                          this                                       // action list.
                                      );
        Dialog dialog = TopManager.getDefault ().createDialog (descriptor);
        dialog.pack ();
        return dialog;
    }
}

/*
 * Log
 *  2    Gandalf   1.1         1/13/00  Ian Formanek    NOI18N
 *  1    Gandalf   1.0         12/23/99 Jan Jancura     
 * $
 * Beta Change History:
 */
