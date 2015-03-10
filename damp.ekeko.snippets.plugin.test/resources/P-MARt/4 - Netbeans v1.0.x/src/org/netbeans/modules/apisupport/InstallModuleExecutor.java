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

import java.io.*;
import java.util.Enumeration;
import java.util.jar.*;

import org.openide.TopManager;
import org.openide.execution.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.util.*;
import org.openide.windows.InputOutput;

import org.netbeans.core.TestModuleItem;

public class InstallModuleExecutor extends Executor {

    private static final long serialVersionUID =3672055366671872774L;
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.modules");
    }

    public ExecutorTask execute (final ExecInfo info) throws IOException {
        String name = info.getClassName ();
        String packageName, baseName;
        int idx = name.lastIndexOf ((int) '.');
        if (idx == -1) {
            packageName = "";
            baseName = name;
        } else {
            packageName = name.substring (0, idx);
            baseName = name.substring (idx + 1);
        }
        Enumeration folders = TopManager.getDefault ().getRepository ().findAll (packageName, null, null);
        if (! folders.hasMoreElements ()) throw new IOException ("No such folder: " + packageName);
        ManifestProvider p = null;
SEARCHING:
        while (folders.hasMoreElements ()) {
            FileObject folder = (FileObject) folders.nextElement ();
            FileObject[] children = folder.getChildren ();
            for (int i = 0; i < children.length; i++) {
                FileObject fo = children[i];
                if (fo.getName ().equals (baseName)) {
                    try {
                        DataObject dob = DataObject.find (fo);
                        p = (ManifestProvider) dob.getCookie (ManifestProvider.class);
                        if (p != null) break SEARCHING;
                    } catch (DataObjectNotFoundException donfe) {
                        donfe.printStackTrace ();
                    }
                }
            }
        }
        if (p == null) throw new IOException ("No such manifest provider: " + name);
        File f = p.getManifestAsFile ();
        if (f == null) {
            f = File.createTempFile ("manif", ".mf");
            OutputStream os = new FileOutputStream (f);
            try {
                p.getManifest ().write (os);
            } finally {
                os.close ();
            }
        }
        final String toDeploy = f.getAbsolutePath ();
        return TopManager.getDefault ().getExecutionEngine ().execute ("Installing module", new Runnable () {
                    public void run () {
                        TestModuleItem.deploy (toDeploy);
                    }
                }, null);
    }

}

/*
 * Log
 *  18   Gandalf-post-FCS1.14.1.2    3/28/00  Jesse Glick     Bugfix: deployment did 
 *       not work from e.g. default package (whenever folder was ambiguous).
 *  17   Gandalf-post-FCS1.14.1.1    3/28/00  Jesse Glick     More robust module 
 *       install executor.
 *  16   Gandalf-post-FCS1.14.1.0    3/9/00   Jesse Glick     Bugfix: deployment of 
 *       manifests with unusual extensions did not work.
 *  15   Gandalf   1.14        2/4/00   Jesse Glick     Better names for temp 
 *       manifests.
 *  14   Gandalf   1.13        1/26/00  Jesse Glick     Executor display names 
 *       can just be taken from bean descriptor.
 *  13   Gandalf   1.12        1/22/00  Jesse Glick     Manifest files can now 
 *       be recognized, not just JARs.
 *  12   Gandalf   1.11        12/22/99 Jesse Glick     Template descriptions 
 *       for all API templates.
 *  11   Gandalf   1.10        11/10/99 Jesse Glick     Install JAR as Module 
 *       now uses Petr Hr's test module feature. Nice!
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/7/99  Jesse Glick     Service name changes.
 *  8    Gandalf   1.7         10/6/99  Jesse Glick     Added table of contents,
 *       anchored context help.
 *  7    Gandalf   1.6         10/5/99  Jesse Glick     Sundry API changes 
 *       affecting me.
 *  6    Gandalf   1.5         9/30/99  Jesse Glick     Package rename and misc.
 *  5    Gandalf   1.4         9/17/99  Jesse Glick     Making a special loader 
 *       for modules.
 *  4    Gandalf   1.3         9/17/99  Jesse Glick     
 *  3    Gandalf   1.2         9/16/99  Jesse Glick     Assumes JarDataObject.
 *  2    Gandalf   1.1         9/14/99  Jesse Glick     Context help.
 *  1    Gandalf   1.0         9/12/99  Jesse Glick     
 * $
 */
