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
import java.io.*;
import java.util.*;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.filesystems.*;
import javax.sound.sampled.*;
public class SampledAudioCompilerGroup extends CompilerGroup {
    /**
     * @associates SampledAudioCompiler 
     */
    private Set compilers = new HashSet ();
    public void add (Compiler c) {
        compilers.add ((SampledAudioCompiler) c);
    }
    public boolean start () {
        boolean ok = true;
        Iterator it = compilers.iterator ();
        while (it.hasNext ()) {
            SampledAudioCompiler c = (SampledAudioCompiler) it.next ();
            FileObject score = c.getScoreFile ();
            FileObject au = FileUtil.findBrother (score, ScoreDataLoader.SECONDARY_EXT);
            try {
                if (au == null)
                    au = score.getParent ().createData (score.getName (), ScoreDataLoader.SECONDARY_EXT);
            } catch (IOException ioe) {
                fireErrorEvent (new ErrorEvent (this, score, 0, 0, ioe.toString (), ""));
                ok = false;
                continue;
            }
            try {
                InputStream is = score.getInputStream ();
                try {
                    FileLock lock = au.lock ();
                    try {
                        OutputStream os = au.getOutputStream (lock);
                        try {
                            fireProgressEvent (new ProgressEvent (this, au, ProgressEvent.TASK_WRITING));
                            AudioSystem.write (LineInFromScore.makeStream (is),
                                               AudioFileFormat.Type.AU,
                                               os);
                        } finally {
                            os.close ();
                        }
                    } finally {
                        lock.releaseLock ();
                    }
                } finally {
                    is.close ();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                fireErrorEvent (new ErrorEvent (this, au, 0, 0, ioe.toString (), ""));
                ok = false;
            }
        }
        return ok;
    }
}
