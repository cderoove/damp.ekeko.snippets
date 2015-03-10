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

package org.netbeans.modules.debugger.support.actions;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.border.*;

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.debugger.DebuggerException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;

import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.DebuggerModule;
import org.netbeans.modules.debugger.support.DebuggerInfoProducer;


/**
* Connects debugger to some currently running VM.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class ConnectAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 287995876143889779L;

    public void performAction () {
        final Dialog[] d = new Dialog [1];
        try {
            final AbstractDebugger deb = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
            final JComponent c = deb.getConnectPanel ();
            DialogDescriptor descr = new DialogDescriptor (
                                         c,
                                         NbBundle.getBundle (ConnectAction.class).getString ("CTL_Connect_to_running_VM"),
                                         true, // modal
                                         new ActionListener () {
                                             public void actionPerformed (ActionEvent e) {
                                                 if (e.getSource ().equals (DialogDescriptor.OK_OPTION)) {
                                                     DebuggerModule.changeWorkspace ();
                                                     try {
                                                         deb.startDebugger (((DebuggerInfoProducer) c).getDebuggerInfo ());
                                                     } catch (DebuggerException ex) {
                                                         TopManager.getDefault ().notify (
                                                             new NotifyDescriptor.Exception (
                                                                 ex.getTargetException () == null ? ex : ex.getTargetException (),
                                                                 NbBundle.getBundle (ConnectAction.class).getString ("EXC_Debugger") +
                                                                 ": " + ex.getMessage ()) // NOI18N
                                                         );
                                                     }
                                                 }
                                                 d [0].setVisible (false);
                                                 d [0].dispose ();
                                             }
                                         }
                                     );
            descr.setHelpCtx (new HelpCtx (ConnectAction.class.getName () + ".dialog")); // NOI18N
            (d [0] = TopManager.getDefault ().createDialog (descr)).show ();
        } catch (DebuggerException ex) {
            TopManager.getDefault ().notify (
                new NotifyDescriptor.Exception (
                    ex.getTargetException () == null ? ex : ex.getTargetException (),
                    NbBundle.getBundle (ConnectAction.class).getString ("EXC_Debugger") +
                    ": " + ex.getMessage ()) // NOI18N
            );
        }
    }

    /** @return the action's name */
    public String getName () {
        return NbBundle.getBundle (ConnectAction.class).getString ("CTL_Connect");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ConnectAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/debugger/resources/connect.gif"; // NOI18N
    }
}

/*
* Log
*/
