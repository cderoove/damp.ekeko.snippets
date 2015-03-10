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

import java.awt.Dialog;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import java.util.ResourceBundle;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.debugger.DebuggerNotFoundException;

import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.util.Utils;


/**
* AddBreakpoint action.
*
* @author   Jan Jancura
*/
public class AddBreakpointAction extends NodeAction {

    /** generated Serialized Version UID */
    static final long              serialVersionUID = -8487176709797303658L;

    private static AddBreakpointDialogManager abdm;

    static CoreBreakpoint.Event findEvent (CoreBreakpoint.Event[] events, String name) {
        int i, k = events.length;
        for (i = 0; i < k; i++)
            if (events [i].getTypeName ().equalsIgnoreCase (name))
                return events [i];
        return null;
    }


    /** @return the action's name */
    public String getName () {
        return NbBundle.getBundle (AddBreakpointAction.class).
               getString ("CTL_AddBreakpoint");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (AddBreakpointAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/addBreakpoint.gif"; // NOI18N
    }

    protected boolean enable (Node[] activatedNodes) {
        return true;
    }

    public void performAction (Node[] activatedNodes) {
        try {
            AbstractDebugger debugger = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
            CoreBreakpoint breakpoint = (CoreBreakpoint) debugger.createBreakpoint (true);
            if (abdm == null)
                abdm = new AddBreakpointDialogManager ();
            abdm.setBreakpoint (breakpoint);
            abdm.getDialog ().setVisible (true);
            if (!abdm.getOKPressed ())
                breakpoint.remove ();
            else
                breakpoint.setHidden (false);
        } catch (DebuggerNotFoundException ex) {
            return;
        }
    }

    public String getCurrentClassName () {
        return Utils.getCurrentClassName (getActivatedNodes ());
    }

    public String getCurrentMethodName () {
        return Utils.getCurrentMethodName (getActivatedNodes ());
    }

    public String getCurrentFieldName () {
        return Utils.getCurrentFieldName (getActivatedNodes ());
    }

    public int getCurrentLineNumber () {
        return Utils.getCurrentLineNumber (getActivatedNodes ());
    }

    public String getCurrentIdentifier () {
        return Utils.getCurrentIdentifier (getActivatedNodes ());
    }


    // innerclasses .........................................................................

    /**
    * Dialog manager for adding breakpoints.
    * This class is final only for performance reasons,
    * can be happily unfinaled if desired.
    */
    static final class AddBreakpointDialogManager extends Object
        implements ActionListener {

        /** true if ok was pressed */
        private boolean okPressed;
        private Dialog dialog;
        private CoreBreakpoint breakpoint;
        private AddBreakpointPanel panel;

        /** Accessor for managed dialog instance */
        Dialog getDialog () {
            if (dialog == null)
                dialog = createDialog ();
            panel.setBreakpoint (breakpoint);
            setInitialEvent ();
            okPressed = false;
            return dialog;
        }

        /** Constructs managed dialog instance using TopManager.createDialog
        * and returnrs it */
        private Dialog createDialog () {
            ResourceBundle bundle = NbBundle.getBundle (AddBreakpointAction.class);

            panel = new AddBreakpointPanel ();
            // create dialog descriptor, create & return the dialog
            DialogDescriptor descriptor = new DialogDescriptor (
                                              panel,
                                              bundle.getString ("CTL_Breakpoint_Title"),
                                              true,
                                              this
                                          );
            descriptor.setHelpCtx (
                new HelpCtx (AddBreakpointAction.class.getName () + ".dialog") // NOI18N
            );
            Dialog d = TopManager.getDefault().createDialog (descriptor);
            d.pack ();
            return d;
        }

        /** Called when some dialog button was pressed */
        public void actionPerformed (ActionEvent evt) {
            okPressed = DialogDescriptor.OK_OPTION.equals(evt.getSource ());
            dialog.setVisible (false);
            // dialog.dispose ();
        }

        void setBreakpoint (CoreBreakpoint b) {
            breakpoint = b;
        }

        /** @return true if OK button was pressed in dialog,
        * false otherwise. */
        public boolean getOKPressed () {
            return okPressed;
        }

        /**
        * Sets initial event dependently on currently selected nodes.
        */
        private void setInitialEvent () {
            AddBreakpointAction aba = (AddBreakpointAction) SystemAction.
                                      get (AddBreakpointAction.class);
            CoreBreakpoint.Event[] ev = breakpoint.getBreakpointEvents ();
            if (aba.getCurrentFieldName ().length () > 0) {
                CoreBreakpoint.Event e = AddBreakpointAction.findEvent (ev, "Variable");
                if (e != null) {
                    panel.setInitialEvent (e);
                    return;
                }
            }
            if (aba.getCurrentIdentifier ().length () > 0) {
                CoreBreakpoint.Event e = AddBreakpointAction.findEvent (ev, "Exception");
                if (e != null) {
                    panel.setInitialEvent (e);
                    return;
                }
            }
            CoreBreakpoint.Event e = AddBreakpointAction.findEvent (ev, "Line");
            if (e != null) {
                panel.setInitialEvent (e);
                return;
            }
            panel.setInitialEvent (ev [0]);
        }
    }
}

/*
 * Log
 *  15   Gandalf-post-FCS1.13.3.0    3/28/00  Daniel Prusa    
 *  14   Gandalf   1.13        1/13/00  Daniel Prusa    NOI18N
 *  13   Gandalf   1.12        11/8/99  Jan Jancura     Somma classes renamed
 *  12   Gandalf   1.11        11/5/99  Jan Jancura     Add Breakpoint Dialog 
 *       design updated
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         9/28/99  Jan Jancura     
 *  9    Gandalf   1.8         9/2/99   Jan Jancura     
 *  8    Gandalf   1.7         8/2/99   Jan Jancura     A lot of bugs...
 *  7    Gandalf   1.6         7/14/99  Jan Jancura     
 *  6    Gandalf   1.5         7/8/99   Jesse Glick     Context help.
 *  5    Gandalf   1.4         7/2/99   Jan Jancura     
 *  4    Gandalf   1.3         6/25/99  Ian Formanek    Fixed HelpCtx
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
