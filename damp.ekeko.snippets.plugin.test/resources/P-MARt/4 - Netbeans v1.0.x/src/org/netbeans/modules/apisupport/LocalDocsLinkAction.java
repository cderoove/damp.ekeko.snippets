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

package org.netbeans.modules.apisupport;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.openide.TopManager;
import org.openide.execution.NbfsURLConnection;
import org.openide.filesystems.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

public class LocalDocsLinkAction extends CallableSystemAction {

    private static URL index;

    protected void initialize () {
        super.initialize ();
        File jar = APIModule.findAPIDocs ();
        if (jar == null) {
            setEnabled (false);
            return;
        }
        Enumeration fss = TopManager.getDefault ().getRepository ().getFileSystems ();
        while (fss.hasMoreElements ()) {
            FileSystem fs = (FileSystem) fss.nextElement ();
            if (! (fs instanceof JarFileSystem)) continue;
            if (! jar.equals (((JarFileSystem) fs).getJarFile ())) continue;
            FileObject fo = fs.findResource ("index.html");
            if (fo == null) continue;
            try {
                index = NbfsURLConnection.encodeFileObject (fo);
                return;
            } catch (FileStateInvalidException fsie) {
                fsie.printStackTrace ();
            }
        }
        System.err.println("NOTE: could not find API docs mounted");
        setEnabled (false);
    }

    public void performAction () {
        if (index != null) TopManager.getDefault ().showUrl (index);
    }

    public String getName () {
        return "Local Open APIs Documentation";
    }

    protected String iconResource () {
        return "resources/webLink.gif";
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.apis");
    }

}

/*
 * Log
 *  1    Gandalf-post-FCS1.0         3/30/00  Jesse Glick     
 * $
 */
