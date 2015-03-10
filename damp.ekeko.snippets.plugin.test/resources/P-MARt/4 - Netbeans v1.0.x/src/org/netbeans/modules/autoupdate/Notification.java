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
import java.net.URL;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.DialogDescriptor;

/** This class performs the notification if found in the XML file
 *
 * @author  Petr Hrebejk
 */
public class Notification extends Object {

    /** Resource bundle to be used */
    private static final ResourceBundle bundle = NbBundle.getBundle( Notification.class );

    /** This class is a singleton */
    private Notification() {
    }

    /** Tests whether XML file contains notification tag. If so opens modal
    * dialog with the notification.
    * @return True if there was a notification, false if not.
    */

    static boolean performNotification( Updates updates ) {

        final String text = updates.getNotificationText();
        final URL url = updates.getNotificationURL();

        if ( text == null ) {
            return false;
        }

        final JButton closeButton = new JButton (
                                        NbBundle.getBundle (Notification.class).getString ("CTL_Notification_Close")
                                    );
        final JButton urlButton = new JButton (
                                      NbBundle.getBundle (Notification.class).getString ("CTL_Notification_URL")
                                  );

        JOptionPane pane = new JOptionPane (
                               text,
                               JOptionPane.INFORMATION_MESSAGE,
                               JOptionPane.DEFAULT_OPTION
                           );

        pane.setOptions (new Object[] {});

        DialogDescriptor dd = new DialogDescriptor (
                                  pane,
                                  bundle.getString( "CTL_Notification_Title" ),
                                  true,
                                  DialogDescriptor.DEFAULT_OPTION,
                                  DialogDescriptor.OK_OPTION,
                                  new ActionListener () {
                                      public void actionPerformed (ActionEvent ev) {
                                          /*
                                          dialog.setVisible (false);
                                          dialog.dispose ();
                                          dialog = null;
                                          */
                                          if (ev.getSource () == urlButton ) {
                                              // display www browser
                                              if ( url != null ) {
                                                  javax.swing.SwingUtilities.invokeLater( new Runnable() {
                                                                                              public void run() {
                                                                                                  TopManager.getDefault ().showUrl ( url );
                                                                                              }
                                                                                          } );
                                              }
                                          }
                                      }
                                  }
                              );

        dd.setOptions( url != null ? new Object[] {closeButton, urlButton} :
                       new Object[] {closeButton} );
        dd.setClosingOptions( null );
        java.awt.Dialog dialog = TopManager.getDefault().createDialog( dd );
        dialog.show ();
        return true;
    }




    /*

    NotifyDescriptor nd = new NotifyDescriptor( 
      text,
      bundle.getString( "CTL_Notification_Title" ),
      NotifyDescriptor.DEFAULT_OPTION,
      NotifyDescriptor.INFORMATION_MESSAGE,
      null,
      null
      );


    TopManager.getDefault().notify( nd );

    return true;

}
    */
}