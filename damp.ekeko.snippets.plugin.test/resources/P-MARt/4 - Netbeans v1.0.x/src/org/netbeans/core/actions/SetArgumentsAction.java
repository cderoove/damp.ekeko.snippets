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

package org.netbeans.core.actions;

import java.io.IOException;
import java.util.StringTokenizer;

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.ArgumentsCookie;
import org.openide.execution.ExecInfo;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;

/** SetArguments action. Is enabled if the activated node implements ArgumentsCookie.
*
* @see ArgumentsCookie
*
* This class is final only for performance reasons.
* Can be happily unfinaled if desired.
*
* @author   Ian Formanek
*/
public final class SetArgumentsAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6015899396428466799L;

    /** Manages enable / disable logic of this action */
    protected boolean enable (Node[] activatedNodes) {
        if (!super.enable (activatedNodes)) return false;

        // This action is enabled if the ArgumentsCookie returns true from getArgumentsSupported ()
        ArgumentsCookie rightCookie =
            (ArgumentsCookie)(activatedNodes[0].getCookie(ArgumentsCookie.class));
        return rightCookie != null;
    }

    /** Actually performs SetArguments action */
    protected void performAction (final Node[] activatedNodes) {
        ArgumentsCookie rightCookie =
            (ArgumentsCookie)(activatedNodes[0].getCookie(ArgumentsCookie.class));
        if (rightCookie != null)
            displayArgumentsDialog (rightCookie);
    }

    protected int mode () {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    protected Class[] cookieClasses () {
        return new Class[] { ArgumentsCookie.class };
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(SetArgumentsAction.class).getString("SetArguments");
    }

    /** Help context where to find more about the acion.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (SetArgumentsAction.class);
    }

    /** Resource name for the icon.
    * @return resource name
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/actions/setArguments.gif"; // NOI18N
    }

    private static void displayArgumentsDialog (ArgumentsCookie cookie) {
        String[] args = cookie.getArguments ();
        StringBuffer sb = new StringBuffer ();
        for (int i = 0; i < args.length; i++) {
            sb.append (args[i]);
            if (i != args.length - 1) sb.append (" "); // NOI18N
        }

        ArgumentsPanel ap = new ArgumentsPanel (sb.toString ());
        DialogDescriptor dd = new DialogDescriptor (ap, NbBundle.getBundle(SetArgumentsAction.class).getString("CTL_SetArgumentsTitle"));
        TopManager.getDefault ().createDialog (dd).show ();

        if (dd.getValue () == DialogDescriptor.OK_OPTION) {
            StringTokenizer st = new StringTokenizer (ap.getArguments ());
            args = new String[st.countTokens ()];
            int i = 0;
            while (st.hasMoreTokens ()) {
                args[i++] = st.nextToken ();
            }
            try {
                cookie.setArguments (args);
            } catch (IOException ex) {
                TopManager.getDefault().notify(new NotifyDescriptor.Exception(ex));
            }
        }
    }

    static class ArgumentsPanel extends javax.swing.JPanel {

        private javax.swing.JLabel jLabel1;
        private javax.swing.JTextField argField;

        /** Initializes the Form */
        public ArgumentsPanel (String args) {

            setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5)));
            setLayout (new java.awt.GridBagLayout ());
            java.awt.GridBagConstraints gridBagConstraints1;

            jLabel1 = new javax.swing.JLabel ();
            jLabel1.setText (NbBundle.getBundle(SetArgumentsAction.class).getString("CTL_SetArguments"));
            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.insets = new java.awt.Insets (0, 0, 0, 8);
            add (jLabel1, gridBagConstraints1);

            argField = new javax.swing.JTextField ();
            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.weightx = 1.0;
            add (argField, gridBagConstraints1);

            argField.setText (args);
            argField.unregisterKeyboardAction(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0));

            setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(12, 12, 12, 12)));
            argField.requestFocus ();
        }

        public java.awt.Dimension getPreferredSize () {
            return new java.awt.Dimension (300, 50);
        }

        String getArguments () {
            return argField.getText ();
        }
    }
}

/*
 * Log
 *  12   Gandalf   1.11        1/12/00  Ales Novak      i18n
 *  11   Gandalf   1.10        12/3/99  Ian Formanek    I18Nzed
 *  10   Gandalf   1.9         12/3/99  Ian Formanek    Uses 
 *       DialogDescriptor/createDialog for Arguments Dialog, fixes bug 1464 - 
 *       "Set Arguments" dialog OK button is marked active, but pressing  Enter 
 *       key does not work.
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  7    Gandalf   1.6         6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  4    Gandalf   1.3         3/10/99  Ian Formanek    Made more safe in 
 *       performAction
 *  3    Gandalf   1.2         1/21/99  David Simonek   Removed references to 
 *       "Actions" class
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
