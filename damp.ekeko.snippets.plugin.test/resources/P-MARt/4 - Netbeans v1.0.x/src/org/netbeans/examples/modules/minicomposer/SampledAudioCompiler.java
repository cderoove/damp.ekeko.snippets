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
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.filesystems.*;
public class SampledAudioCompiler extends Compiler {
    private FileObject scoreFile;
    private boolean build;
    public SampledAudioCompiler (FileObject scoreFile, boolean build) {
        this.scoreFile = scoreFile;
        this.build = build;
    }
    public boolean equals (Object o) {
        if (o == null || ! (o instanceof SampledAudioCompiler)) return false;
        return scoreFile.equals (((SampledAudioCompiler) o).scoreFile) &&
               build == ((SampledAudioCompiler) o).build;
    }
    public int hashCode () {
        return SampledAudioCleaner.class.hashCode () ^
               scoreFile.hashCode () ^
               (build ? 23 : 111);
    }
    public Class compilerGroupClass () {
        return SampledAudioCompilerGroup.class;
    }
    protected boolean isUpToDate () {
        if (build) return false;
        FileObject au = FileUtil.findBrother (scoreFile, ScoreDataLoader.SECONDARY_EXT);
        if (au == null) return false;
        return au.lastModified ().compareTo (scoreFile.lastModified ()) > 0;
    }
    public FileObject getScoreFile () {
        return scoreFile;
    }
}
