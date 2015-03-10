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

package org.netbeans.examples.modules.globalactions;

import java.awt.*;
import javax.swing.JLabel;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/** Simple action. Opens up a new window and displays a message. */
public class TestAction extends CallableSystemAction {
    static final long serialVersionUID =6101321673293585672L;
    public String getName () {
        return "Test Action";
    }
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
    public void performAction () {
        TopComponent comp = new TopComponent ();
        comp.setName ("Test Window");
        comp.setLayout (new BorderLayout ());
        JLabel label = new JLabel ("Put something here...");
        label.setFont (new Font ("Serif", Font.ITALIC | Font.BOLD, 24));
        comp.add (label, BorderLayout.CENTER);
        comp.open ();
        comp.requestFocus ();
    }
    protected String iconResource () {
        return "/org/netbeans/examples/modules/globalactions/testAction.gif";
    }
    public static void main (String[] ignore) {
        new TestAction ().performAction ();
    }
}
