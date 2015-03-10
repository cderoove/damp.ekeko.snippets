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

package org.netbeans.modules.innertesters;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.cookies.CompilerCookie;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;

/** Compiler group to move inner test classes out of the way.
 *
 * @author Jesse Glick
 */
public class InnerCompilerGroup extends CompilerGroup {

    /** The compilers already in this group.
     * @associates Compiler
     */
    private Set compilers = new HashSet (); // Set<InnerCompiler>

    /** Keep track of a compiler added to the group.
     * @param c the compiler being added
     * @throws IllegalArgumentException if it is of the wrong type
     */
    public void add (Compiler c) throws IllegalArgumentException {
        if (! (c instanceof InnerCompiler)) throw new IllegalArgumentException ();
        compilers.add (c);
    }

    /** Run the compilation.
     * Deletes existing classfiles from test dir (when cleaning or building).
     * Moves inner classfile to the test dir from source dir
     * (when building or compiling).
     * @return <CODE>true</CODE> for success
     */
    public boolean start () {
        // Only stays true if all are successful:
        boolean ok = true;
        Iterator it = compilers.iterator ();
        // First, go through all the compilers one-by-one.
        // This group has no need to consider a number of compilers at a time,
        // so whether they are grouped together or not does not really matter.
        while (it.hasNext ()) {
            InnerCompiler c = (InnerCompiler) it.next ();
            // Don't need to do anything in this case:
            if (c.isUpToDate ()) continue;
            Class type = c.getType ();
            try {
                File destfile = c.getDestFile ();
                // Always kill existing destfile, just in case (note that we already
                // know the compiler is *not* up-to-date):
                if (destfile.exists ()) destfile.delete ();
                if (! type.equals (CompilerCookie.Clean.class)) {
                    FileObject sourcefile = c.getSourceFile ();
                    if (sourcefile != null) {
                        // Auto-creates any package structure:
                        destfile.getParentFile ().mkdirs ();
                        // Copy using streams, since destfile is not generally a FileObject,
                        // and sourcefile is not generally a File:
                        OutputStream os = new FileOutputStream (destfile);
                        try {
                            InputStream is = sourcefile.getInputStream ();
                            try {
                                // May show up in status bar etc.:
                                fireProgressEvent (new ProgressEvent (this, sourcefile, ProgressEvent.TASK_WRITING));
                                byte[] buf = new byte[4096];
                                int count;
                                while ((count = is.read (buf)) != -1)
                                    os.write (buf, 0, count);
                            } finally {
                                is.close ();
                            }
                        } finally {
                            os.close ();
                        }
                        FileLock lock = sourcefile.lock ();
                        try {
                            sourcefile.delete (lock);
                        } finally {
                            lock.releaseLock ();
                        }
                    } else { // sourceFile == null
                        fireErrorEvent (new ErrorEvent (this, c.getFileObject (), 0, 0,
                                                        MessageFormat.format (NbBundle.getBundle (InnerCompilerGroup.class)
                                                                              .getString ("ERR_no_inner_testing_class"),
                                                                              new Object[] { c.getInnerName () }),
                                                        ""));
                        ok = false;
                    }
                }
            } catch (IOException ioe) {
                // Should appear in Compiler output tab:
                FileObject fo = c.getSourceFile ();
                if (fo == null) fo = c.getFileObject ();
                fireErrorEvent (new ErrorEvent (this, fo, 0, 0, ioe.toString (), ""));
                ok = false;
            }
        }
        return ok;
    }

}
