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

package org.netbeans.modules.multicompile;

import java.io.IOException;
import java.util.*;

import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.filesystems.*;
import org.openide.loaders.*;

/** Does the work of cleaning up after an external compiler.
 *
 * @author jglick
 */
public class LazyCleanCompilerGroup extends CompilerGroup {
    /**
     * @associates Compiler 
     */
    private Set compilers = new HashSet (); // Set<LazyCleanCompiler>

    public void add (Compiler c) throws IllegalArgumentException {
        if (! (c instanceof LazyCleanCompiler)) throw new IllegalArgumentException ();
        compilers.add (c);
    }

    public boolean start () {
        boolean ok = true;
        Iterator it = compilers.iterator ();
        while (it.hasNext ()) {
            LazyCleanCompiler c = (LazyCleanCompiler) it.next ();
            DataObject obj = c.getObject ();
            String outExt = c.getOutExt ();
            fireProgressEvent (new ProgressEvent (this, obj.getPrimaryFile (), ProgressEvent.TASK_WRITING));
            Iterator it2 = obj.files ().iterator ();
            while (it2.hasNext ()) {
                FileObject toKill = (FileObject) it2.next ();
                if (! toKill.hasExt (outExt)) continue;
                try {
                    FileLock lock = toKill.lock ();
                    try {
                        toKill.delete (lock);
                    } finally {
                        lock.releaseLock ();
                    }
                } catch (IOException ioe) {
                    fireErrorEvent (new ErrorEvent (this, toKill, 0, 0, ioe.toString (), null));
                    ok = false;
                }
            }
        }
        return ok;
    }

}