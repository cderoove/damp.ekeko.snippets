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
import org.openide.TopManager;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.SaveCookie;
public abstract class ScoreCompilerSupport implements CompilerCookie {
    private ScoreDataObject obj;
    protected ScoreCompilerSupport (ScoreDataObject obj) {
        this.obj = obj;
    }
    protected boolean saveFirst () {
        if (obj.isModified ()) {
            SaveCookie save = (SaveCookie) obj.getCookie (SaveCookie.class);
            if (save != null) {
                try {
                    save.save ();
                } catch (IOException ioe) {
                    TopManager.getDefault ().notifyException (ioe);
                    return false;
                }
            }
        }
        return true;
    }
    public boolean isDepthSupported (Compiler.Depth depth) {
        return true;
    }
    public abstract void addToJob (CompilerJob job, Compiler.Depth depth);
    public static class Compile extends ScoreCompilerSupport implements CompilerCookie.Compile {
        public Compile (ScoreDataObject obj) {
            super (obj);
        }
        public void addToJob (CompilerJob job, Compiler.Depth depth) {
            if (this.saveFirst ())
                job.add (new SampledAudioCompiler (this.obj.getPrimaryFile (), false));
        }
    }
    public static class Build extends ScoreCompilerSupport implements CompilerCookie.Build {
        public Build (ScoreDataObject obj) {
            super (obj);
        }
        public void addToJob (CompilerJob job, Compiler.Depth depth) {
            if (this.saveFirst ())
                job.add (new SampledAudioCompiler (this.obj.getPrimaryFile (), true));
        }
    }
    public static class Clean extends ScoreCompilerSupport implements CompilerCookie.Clean {
        public Clean (ScoreDataObject obj) {
            super (obj);
        }
        public void addToJob (CompilerJob job, Compiler.Depth depth) {
            job.add (new SampledAudioCleaner (this.obj.getPrimaryFile ()));
        }
    }
}
