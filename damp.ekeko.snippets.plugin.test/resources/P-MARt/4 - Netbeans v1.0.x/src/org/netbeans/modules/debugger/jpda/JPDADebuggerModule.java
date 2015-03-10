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

package org.netbeans.modules.debugger.jpda;

import java.awt.Dialog;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;

import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.delegator.DelegatingDebugger;


/**
* Module installation class for JPDADebugger Module
*
* @author Jan Jancura
*/
public class JPDADebuggerModule extends org.openide.modules.ModuleInstall {

    transient private Dialog              dialog;
    transient private static boolean      previouslyInstalled;

    private static int                    counter;
    static boolean                        installed;

    static final long serialVersionUID = -2272025566936592771L;

    static {
        counter = 0;
        installed = false;
    }

    public JPDADebuggerModule () {
    }

    public JPDADebuggerModule (int counter, boolean installed) {
        this.counter = counter;
        this.installed = installed;
    }

    protected Object writeReplace () {
        return new JPDADebuggerModuleLoader (counter, installed);
    }

    /** Module installed for the first time. */
    public void installed () {
        restored ();
    }

    /** Module installed again. */
    public void restored () {
        previouslyInstalled = installed;
        try {
            Class.forName ("com.sun.jdi.VirtualMachineManager"); // NOI18N
            installed = true;
        } catch (ClassNotFoundException e) {
            installed = false;
        }
        if (installed) {
            try {
                DelegatingDebugger.registerDebugger (
                    new JPDADebugger (
                        ((DelegatingDebugger) TopManager.getDefault ().getDebugger ()).
                        isMultiSession (),
                        ((DelegatingDebugger) TopManager.getDefault ().getDebugger ()).
                        getValidator ()
                    )
                );
            } catch (Exception e) {
            }
        }
        else
            if (previouslyInstalled || (counter < 2))
                showWarning ();
        if (counter < 2)
            counter ++;
    }

    /** Module was uninstalled. */
    public void uninstalled () {
        if (installed)
            try {
                DelegatingDebugger.unregisterDebugger (
                    JPDADebugger.class
                );
            } catch (Exception e) {
            }
    }

    /**
    * Shows JPDA not installed warning.
    */
    private void showWarning () {
        final JButton ok = new JButton (
                               NbBundle.getBundle (JPDADebuggerModule.class).getString ("PROP_OK")
                           );
        final JButton url = new JButton (
                                NbBundle.getBundle (JPDADebuggerModule.class).getString ("PROP_URL")
                            );
        JOptionPane pane = new JOptionPane (
                               NbBundle.getBundle (JPDADebuggerModule.class).getString ("EXC_JPDA_not_found"),
                               JOptionPane.INFORMATION_MESSAGE,
                               JOptionPane.DEFAULT_OPTION
                           );
        pane.setOptions (new Object[] {});
        DialogDescriptor desc = new DialogDescriptor (
                                    pane,
                                    NbBundle.getBundle (JPDADebuggerModule.class).getString ("MSG_MissingJPDATitle"),
                                    false,
                                    DialogDescriptor.DEFAULT_OPTION,
                                    DialogDescriptor.OK_OPTION,
                                    new ActionListener () {
                                        public void actionPerformed (ActionEvent ev) {
                                            dialog.setVisible (false);
                                            dialog.dispose ();
                                            dialog = null;
                                            if (ev.getSource () == url) {
                                                // display www browser
                                                try {
                                                    TopManager.getDefault ().showUrl (new URL (
                                                                                          NbBundle.getBundle (JPDADebuggerModule.class).getString ("PROP_JPDA_URL")
                                                                                      ));
                                                } catch (MalformedURLException ex) {
                                                    // Bad URL means no browsing ...
                                                }
                                            }
                                        }
                                    }
                                );
        desc.setOptions (new Object[] {ok, url});
        dialog = TopManager.getDefault ().createDialog (desc);
        dialog.show ();
    }

}

// helper loader class ...............................................................

class JPDADebuggerModuleLoader implements Externalizable {

    private int                   counter;
    private boolean               installed;
    /** Current serialization version. */
    private static final int      SERIAL_VERSION = 1;

    static final long serialVersionUID = 4763887899005102648L;

    public JPDADebuggerModuleLoader () {
    }

    public JPDADebuggerModuleLoader (int counter, boolean installed) {
        this.counter = counter;
        this.installed = installed;
    }

    public void readExternal (ObjectInput oi) throws IOException, ClassNotFoundException {
        oi.readInt (); // SERIAL_VERSION
        counter = oi.readInt ();
        installed = oi.readBoolean ();
    }

    private Object readResolve () {
        return new JPDADebuggerModule (counter, installed);
    }

    public void writeExternal (ObjectOutput oo) throws IOException {
        oo.writeInt (SERIAL_VERSION);
        oo.writeInt (counter);
        oo.writeBoolean (installed);
    }

}

/*
* Log
*  18   Gandalf-post-FCS1.13.4.3    4/11/00  Daniel Prusa    bugfix for 
*       deserialization
*  17   Gandalf-post-FCS1.13.4.2    4/5/00   Jan Jancura     Default debugger Typwe 
*       patch
*  16   Gandalf-post-FCS1.13.4.1    3/29/00  Daniel Prusa    
*  15   Gandalf-post-FCS1.13.4.0    3/28/00  Daniel Prusa    
*  14   Gandalf   1.13        1/13/00  Daniel Prusa    NOI18N
*  13   Gandalf   1.12        11/15/99 Jan Jancura     Show "JPDA not installed"
*       dialogue properly.
*  12   Gandalf   1.11        11/8/99  Jan Jancura     Somma classes renamed
*  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  10   Gandalf   1.9         10/13/99 Jan Jancura     URL added to the "JPDA 
*       debugger not found" dialog.
*  9    Gandalf   1.8         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
*        changed to class + some methods added
*  8    Gandalf   1.7         9/28/99  Jan Jancura     
*  7    Gandalf   1.6         9/6/99   Jan Jancura     
*  6    Gandalf   1.5         9/6/99   Jan Jancura     
*  5    Gandalf   1.4         9/3/99   Jan Jancura     
*  4    Gandalf   1.3         9/2/99   Jan Jancura     
*  3    Gandalf   1.2         7/21/99  Jan Jancura     
*  2    Gandalf   1.1         7/13/99  Jan Jancura     
*  1    Gandalf   1.0         7/13/99  Jan Jancura     
* $
*/
