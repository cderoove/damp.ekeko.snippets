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

package org.netbeans.examples.scripts;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.windows.InputOutput;
public class ListFiles {
    public static void main (String[] args) throws Exception {
        String pattern;
        if (args.length == 1)
            pattern = args[0];
        else
            throw new Exception ("Arguments: one substring to match");
        InputOutput io = TopManager.getDefault ().getIO ("Matches for " + pattern);
        io.select ();
        PrintWriter pw = io.getOut ();
        pw.println ("Scanning for `" + pattern + "'...");
        Enumeration fss = TopManager.getDefault ().getRepository ().getFileSystems ();
        while (fss.hasMoreElements ()) {
            FileSystem fs = (FileSystem) fss.nextElement ();
            if (! fs.isHidden ()) {
                FileObject fo = fs.getRoot ();
                scan (fo, pattern, pw);
            }
        }
        pw.println ("Done.");
    }
    private static void scan (FileObject fo, String pattern, PrintWriter pw)
    throws IOException {
        if ((fo.getName () + '.' + fo.getExt ()).indexOf (pattern) != -1)
            pw.println (fo.getPackageNameExt ('/', '.'));
        if (fo.isFolder ()) {
            FileObject[] children = fo.getChildren ();
            for (int i = 0; i < children.length; i++)
                scan (children[i], pattern, pw);
        }
    }
}
