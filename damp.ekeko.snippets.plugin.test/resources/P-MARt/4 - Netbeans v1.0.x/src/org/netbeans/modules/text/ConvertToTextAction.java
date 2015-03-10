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

package org.netbeans.modules.text;

import java.beans.PropertyVetoException;
import java.io.IOException;

import org.openide.TopManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

/** Action which converts DefaultDataObjects to TXTDataObjects.
 *
 * @author Jesse Glick
 */
public class ConvertToTextAction extends CookieAction {

    private static final long serialVersionUID = 3495147047528675606L;

    protected void initialize () {
        super.initialize ();
        try {
            putProperty ("ddoclazz", Class.forName ("org.openide.loaders.DefaultDataObject")); // NOI18N
        } catch (ClassNotFoundException cnfe) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                cnfe.printStackTrace ();
        }
    }

    private Class getDDOClazz () {
        return (Class) getProperty ("ddoclazz"); // NOI18N
    }

    protected void addNotify () {
        if (getDDOClazz () != null)
            super.addNotify ();
        else
            setEnabled (false);
    }

    protected Class[] cookieClasses () {
        return new Class[] { getDDOClazz () };
    }

    protected int mode () {
        return MODE_ALL;
    }

    protected void performAction (Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            DataObject ddo = (DataObject) nodes[i].getCookie (getDDOClazz ());
            if (ddo == null) continue;
            try {
                ddo.getPrimaryFile ().setAttribute (TXTDataLoader.ATTR_IS_TEXT_FILE, Boolean.TRUE);
                ddo.setValid (false);
            } catch (IOException ioe) {
                TopManager.getDefault ().notifyException (ioe);
            } catch (PropertyVetoException pve) {
                TopManager.getDefault ().notifyException (pve);
            }
        }
    }

    public String getName () {
        return NbBundle.getBundle (ConvertToTextAction.class).getString ("ConvertToText");
    }

    protected String iconResource () {
        return "txtObject.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ConvertToTextAction.class);
    }

}

/*
 * Log
 *  1    Gandalf-post-FCS1.0         3/24/00  Jesse Glick     
 * $
 */
