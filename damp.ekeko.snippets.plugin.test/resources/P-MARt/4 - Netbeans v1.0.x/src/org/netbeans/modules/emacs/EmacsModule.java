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

package org.netbeans.modules.emacs;

import javax.swing.JEditorPane;

import org.openide.modules.ModuleInstall;

public class EmacsModule extends ModuleInstall {

    // XXX does anything need to be done to reload
    // mime type settings??
    // if so, implement installed ()

    public void uninstalled () {
        EmacsSettings def = EmacsSettings.DEFAULT;
        String[] types = def.getMimeTypes ();
        for (int i = 0; i < types.length; i++)
            JEditorPane.registerEditorKitForContentType (types[i], "javax.swing.text.DefaultEditorKit");
        close ();
    }

    public void close () {
        EmacsSettings def = EmacsSettings.DEFAULT;
        if (def.isPassive ())
            Connection.stopServer (def.getPort ());
        EmacsProxier.closeAll ();
        Connection.disconnectAll ();
    }

}
