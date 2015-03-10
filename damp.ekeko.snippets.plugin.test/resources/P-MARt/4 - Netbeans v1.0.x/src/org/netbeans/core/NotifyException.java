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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import javax.swing.*;

import org.openide.windows.*;
import org.openide.*;
import org.openide.util.enum.*;
import org.openide.util.NbBundle;

import org.netbeans.core.output.OutPane;

/**
 * Notifies exceptions.
 *
 * @author  Jaroslav Tulach
 */
final class NotifyException extends JScrollPane implements ActionListener {
    static final long serialVersionUID =3680397500573480127L;


    /** the instance */
    private static NotifyException INSTANCE = new NotifyException ();

    /** max text of exception */
    private static final int MAXIMUM_TEXT_WIDTH = 40;



    /** enuration of Throwables to notify */
    private QueueEnumeration exceptions;
    /** current exception */
    private Throwable current;

    /** dialog descriptor */
    private DialogDescriptor descriptor;
    /** dialog that displayes the exceptions */
    private java.awt.Dialog dialog;
    /** button to show next exceptions */
    private JButton next;
    /** details button */
    private JButton details;
    /** details window */
    private OutPane output;


    /** boolean to show/hide details */
    private boolean showDetails;

    /** Constructor.
    */
    private NotifyException () {
        super ();

        next = new JButton (org.openide.util.NbBundle.getBundle(NotifyException.class).getString("CTL_NextException"));
        details = new JButton ();

        output = new OutPane ();
        setViewportView (output);


        descriptor = new DialogDescriptor ("", ""); // NOI18N
        descriptor.setModal (false);
        descriptor.setMessageType (DialogDescriptor.ERROR_MESSAGE);
        descriptor.setOptions (new Object[] {
                                   DialogDescriptor.OK_OPTION,
                                   next
                               });
        descriptor.setAdditionalOptions (new Object[] {
                                             details
                                         });
        descriptor.setClosingOptions (new Object[0]);
        descriptor.setButtonListener (this);

        dialog = TopManager.getDefault ().createDialog (descriptor);
    }



    /** Adds new exception into the queue.
    */
    public static void notify (final Throwable t) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            INSTANCE.updateState (t);

                                            /** If netbeans.debug.exceptions is set, print the exception to console */
                                            if (System.getProperty ("netbeans.debug.exceptions") != null) { // NOI18N
                                                t.printStackTrace ();
                                            }

                                            PrintStream ps = TopLogging.getLogOutputStream ();
                                            if (t instanceof InvocationTargetException) {
                                                ps.println ("InvocationTargetException:"); // NOI18N
                                                InvocationTargetException detail = (InvocationTargetException)t;
                                                detail.getTargetException ().printStackTrace (ps);
                                            } else {
                                                t.printStackTrace(ps);
                                            }
                                        }
                                    });
    }


    /** updates the state of the dialog. called only in AWT thread.
    */
    private void updateState (Throwable t) {
        if (exceptions == null) {
            // the dialog is not shown
            exceptions = new QueueEnumeration ();
            current = t;
            update ();
            dialog.show ();
        } else {
            // add the exception to the queue
            exceptions.put (t);
            next.setVisible (true);
        }
    }

    /** Updates the visual state of the dialog.
    */
    private void update () {
        // JST: this can be improved in future...
        String lm = current.getLocalizedMessage ();
        String nm = current.getMessage ();
        boolean isLocalized = lm != null && !lm.equals (nm);

        next.setVisible (exceptions.hasMoreElements ());
        details.setText (
            showDetails
            ?
            org.openide.util.NbBundle.getBundle(NotifyException.class).getString("CTL_Exception_Hide_Details")
            :
            org.openide.util.NbBundle.getBundle(NotifyException.class).getString("CTL_Exception_Show_Details")
        );


        if (current instanceof InvocationTargetException) {
            // go in
            current = ((InvocationTargetException)current).getTargetException ();
        }

        //    setText (current.getLocalizedMessage ());
        String title = org.openide.util.NbBundle.getBundle(NotifyException.class).getString("CTL_Title_Exception");

        if (showDetails) {
            descriptor.setMessage (createDetails ());
        } else {
            if (isLocalized) {
                String msg = current.getLocalizedMessage ();
                if (msg == null || "".equals(msg)) {  // NOI18N
                    msg = org.openide.util.Utilities.wrapString (
                              msg, MAXIMUM_TEXT_WIDTH, false, false
                          );
                }
                descriptor.setMessage (msg);
            } else {
                // emphasize user-non-friendly exceptions
                //      if (this.getMessage() == null || "".equals(this.getMessage())) { // NOI18N
                descriptor.setMessage (
                    java.text.MessageFormat.format(
                        NbBundle.getBundle (NotifyDescriptor.class).getString("NTF_ExceptionalException"),
                        new Object[] {
                            current.getClass().getName()
                        }
                    )
                );

                title = NbBundle.getBundle (NotifyDescriptor.class).getString(
                            "NTF_ExceptionalExceptionTitle" // NOI18N
                        );
            }
        }

        descriptor.setTitle (title);
    }

    /** Create details.
    */
    private JComponent createDetails () {
        try {
            output.getOut ().reset ();
            current.printStackTrace (output.getOut ());
            output.setSelectedIndex (0);
            output.requestFocus ();
        } catch (java.io.IOException ex) {
        }

        return this;
    }

    //
    // Handlers
    //

    public void actionPerformed(final java.awt.event.ActionEvent ev) {
        if (ev.getSource () == next) {
            current = (Throwable)exceptions.nextElement ();
            update ();
            return;
        }

        if (ev.getSource () == details) {
            showDetails = !showDetails;
            update ();
            return;
        }

        if (ev.getSource () == DialogDescriptor.OK_OPTION) {
            exceptions = null;
            dialog.setVisible (false);
            return;
        }
    }

    //
    // Listeners
    //

    /**
     * Shows or hides this component depending on the value of parameter.
     * @param show if true, shows this component; otherwise, hides it.
     *
    public void setVisible(boolean visible) {
        if (visible == true) {
            // print details to log file
            if (descriptor != null) {
                PrintStream ps = TopLogging.getLogOutputStream ();
                Object detail = descriptor.getDetail();
                if (detail instanceof Throwable) {
                    if (detail instanceof InvocationTargetException) {
                        ps.println ("InvocationTargetException:");
                        ((InvocationTargetException)detail).getTargetException ().printStackTrace (ps);
                    } else {
                        ((Throwable)detail).printStackTrace(ps);
                    }
                } else {
                    ps.println(detail);
                }
            }
        }
        super.setVisible(visible);
}
    */
}

/*
 * Log
 *  6    Gandalf   1.5         1/15/00  Jaroslav Tulach Next is visible when 
 *       more exceptions arrives after displaying the first
 *  5    Gandalf   1.4         1/13/00  Jaroslav Tulach I18N
 *  4    Gandalf   1.3         1/9/00   Jaroslav Tulach #5148
 *  3    Gandalf   1.2         1/9/00   Jaroslav Tulach getLocalizedMessage can 
 *       also return null.
 *  2    Gandalf   1.1         1/5/00   Jaroslav Tulach Next is not visible when
 *       no next exception is there.
 *  1    Gandalf   1.0         12/30/99 Jaroslav Tulach 
 * $
 */

