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

package org.netbeans.examples.modules.minicomposer;
import java.io.IOException;
import java.util.*;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.filesystems.*;
public class SampledAudioCleaner extends Compiler {
    private FileObject scoreFile;
    public SampledAudioCleaner (FileObject scoreFile) {
        this.scoreFile = scoreFile;
    }
    public boolean equals (Object o) {
        if (o == null || ! (o instanceof SampledAudioCleaner)) return false;
        return scoreFile.equals (((SampledAudioCleaner) o).scoreFile);
    }
    public int hashCode () {
        return SampledAudioCleaner.class.hashCode () ^ scoreFile.hashCode ();
    }
    public Class compilerGroupClass () {
        return Group.class;
    }
    protected boolean isUpToDate () {
        return FileUtil.findBrother (scoreFile, ScoreDataLoader.SECONDARY_EXT) == null;
    }
    public FileObject getScoreFile () {
        return scoreFile;
    }
    public static class Group extends CompilerGroup {
        /**
         * @associates SampledAudioCleaner 
         */
        private Set compilers = new HashSet ();
        public void add (Compiler c) {
            compilers.add ((SampledAudioCleaner) c);
        }
        public boolean start () {
            boolean ok = true;
            Iterator it = compilers.iterator ();
            while (it.hasNext ()) {
                SampledAudioCleaner c = (SampledAudioCleaner) it.next ();
                FileObject fo = c.getScoreFile ();
                FileObject toClean = FileUtil.findBrother (fo, ScoreDataLoader.SECONDARY_EXT);
                if (toClean != null) {
                    try {
                        FileLock lock = toClean.lock ();
                        try {
                            fireProgressEvent (new ProgressEvent (this, toClean, ProgressEvent.TASK_CLEANING));
                            toClean.delete (lock);
                        } finally {
                            lock.releaseLock ();
                        }
                    } catch (IOException ioe) {
                        fireErrorEvent (new ErrorEvent (this, toClean, 0, 0, ioe.toString (), ""));
                        ok = false;
                    }
                }
            }
            return ok;
        }
    }
}
