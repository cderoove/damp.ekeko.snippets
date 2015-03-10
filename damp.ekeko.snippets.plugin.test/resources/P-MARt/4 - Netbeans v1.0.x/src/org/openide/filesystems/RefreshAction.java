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

package org.openide.filesystems;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.swing.Timer;

import org.openide.TopManager;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.Repository;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.enum.AlterEnumeration;
import org.openide.util.enum.FilterEnumeration;
import org.openide.util.enum.SequenceEnumeration;
import org.openide.util.enum.QueueEnumeration;

/** Action for refresh of file systm
*
* @author Jaroslav Tulach
*/
final class RefreshAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6022165630798612727L;

    /** @return DataFolder class */
    protected Class[] cookieClasses () {
        return new Class[] { DataFolder.class };
    }

    protected void performAction (Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            DataFolder df = (DataFolder)nodes[i].getCookie (DataFolder.class);
            if (df != null) {
                FileObject fo = df.getPrimaryFile ();
                fo.refresh ();
            }
        }
    }

    protected int mode () {
        return MODE_ALL;
    }

    public String getName () {
        return NbBundle.getBundle(RefreshAction.class).getString ("LAB_Refresh");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (RefreshAction.class);
    }

}

/*
 * Log
 *  5    src-jtulach1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    src-jtulach1.3         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  1    src-jtulach1.0         3/26/99  Jaroslav Tulach 
 * $
 */
