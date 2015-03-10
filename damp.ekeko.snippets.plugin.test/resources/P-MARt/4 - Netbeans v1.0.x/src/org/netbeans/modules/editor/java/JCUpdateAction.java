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

package org.netbeans.modules.editor.java;

import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.cookies.SourceCookie;
import org.openide.util.HelpCtx;

/**
* Java completion refresh action
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JCUpdateAction extends NodeAction {

    private static final String BUNDLE_NAME = "update_action"; // NOI18N

    private static java.util.ResourceBundle bundle;

    static final long serialVersionUID =-651028649715574174L;

    static String getBundleString(String s) {
        if (bundle == null) {
            bundle = org.openide.util.NbBundle.getBundle(JCProviderPanel.class);
        }
        return bundle.getString(s);
    }

    public String getName() {
        return getBundleString(BUNDLE_NAME);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(JCUpdateAction.class);
    }

    public void performAction(final Node[] activatedNodes) {
        new JCUpdater(activatedNodes).start();
    }

    protected boolean enable(Node[] activatedNodes) {
        boolean ok = true;
        for (int i = 0; i < activatedNodes.length; i++) {
            if (activatedNodes[i].getCookie(DataFolder.class) == null) {
                ok = false;
                break;
            }
        }
        return ok;
    }

    protected String iconResource () {
        return "/org/netbeans/modules/editor/resources/jcUpdate.gif"; // NOI18N
    }

}

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Miloslav Metelka Localization
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/10/99  Miloslav Metelka 
 *  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

