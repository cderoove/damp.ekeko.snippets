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

package org.netbeans.modules.java;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.ProgressEvent;
import org.openide.compiler.ErrorEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.cookies.CompilerCookie;
/**
*
* @author Ales Novak
*/
final class CleanCompiler extends Compiler {

    JavaDataObject jdo;
    CleanCompilerGroup group;

    /**
    * @param job a compiler job
    * @param fo file object to compile (represents .java source)
    * @param constraint - COMPILE, BUILD, ...
    */
    public CleanCompiler(CompilerJob job, JavaDataObject jdo) {
        super(job);
        this.jdo = jdo;
    }


    /** inherited */
    public boolean isUpToDate() {
        return (jdo.files().size() == 1); // heuristic but fast
    }

    /** Deletes class file for given java file. */
    private void deleteClass(JavaDataObject dobj) throws IOException {
        java.util.Set files = dobj.secondaryEntries();
        java.util.Iterator iter = files.iterator();
        FileObject fo;
        MultiDataObject.Entry entry;
        while (iter.hasNext()) {
            entry = (MultiDataObject.Entry) iter.next();
            fo = entry.getFile();
            if (fo.getExt().equals(JavaDataLoader.CLASS_EXTENSION)) {
                if (fo.isReadOnly()) {
                    continue;
                }
                FileLock lock = fo.lock();
                group.ireProgressEvent(new ProgressEvent(group, fo, ProgressEvent.TASK_CLEANING));
                try {
                    fo.delete(lock);
                    dobj.removeSecondaryEntryAccess(entry);
                } finally {
                    lock.releaseLock();
                }
            }
        }
    }

    public boolean equals (Object other) {
        if (!(other instanceof CleanCompiler))
            return false;
        return (jdo == ((CleanCompiler)other).jdo);
    }

    public int hashCode () {
        return ((jdo == null) ? 0 : jdo.hashCode());
    }

    /**
    */
    boolean compile()  {
        try {
            deleteClass(jdo);
            return true;
        } catch (IOException ioe) {
            group.printThrowable(ioe);
            return false;
        }
    }//compile

    /** @return false used in JavaCompilerGroup */
    public final boolean getClearEnv() {
        return false;
    }

    /** inherited */
    public Class compilerGroupClass() {
        return CleanCompilerGroup.class;
    }

    public Object compilerGroupKey() {
        List l = new ArrayList(2);
        try {
            l.add(super.compilerGroupKey ());
            l.add(jdo.getPrimaryFile().getFileSystem());
        } catch (FileStateInvalidException e) { // could not happen
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }
        return l;
    }
}


/*
 * Log
 */
