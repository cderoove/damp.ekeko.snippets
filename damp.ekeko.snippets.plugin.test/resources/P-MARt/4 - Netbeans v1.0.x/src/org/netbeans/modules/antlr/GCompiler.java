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

package org.netbeans.modules.antlr;

import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;

/**
 *
 * @author  jleppanen
 * @version 
 */
class GCompiler extends Compiler {
    GDataObject obj;

    GCompiler(CompilerJob job,GDataObject obj) {
        super(job);
        this.obj = obj;
        System.out.println("GCompiler created");
    }

    public Class compilerGroupClass() {
        System.out.println("GCompiler::compilerGroupClass");
        return GCompilerGroup.class;
    }
    public boolean isUpToDate() {
        System.out.println("GCompiler::isUpToDate");
        // is importVocab is up-to-date

        return true;
    }

    // ** compile **
    public void compile() {
        System.out.println("GCompiler::compile");
        FileObject fo = obj.getPrimaryEntry().getFile();
        java.io.File file = org.openide.execution.NbClassPath.toFile(fo);
        System.out.println("EXEC: antlr.Tool -o "+file.getParent()+" "+file);

        // - Use thread executor -
        org.openide.execution.Executor executor = org.openide.execution.Executor.find(org.openide.execution.ThreadExecutor.class);
        org.openide.execution.ExecutorTask task = executor.execute("antlr.Tool", new String[] { "-o",file.getParent().toString(),file.toString()} );
        task.waitFinished();

        System.out.println("ANTLR Tool exited with exit status: "+ task.result());
        // --
        System.out.println("Continuing compilation");
        // - Should compile the generated Java files if no errors occurred -
        System.out.println("ANTLR module should compile "+obj.javaFiles);
    }

    static class Manager extends Compiler.Manager {
        public void prepareJob(CompilerJob job, Class type, DataObject obj) {
            System.out.println("GCompiler.Manager::prepareJob");
            if (type.isAssignableFrom(CompilerCookie.Compile.class)) {
                System.out.println("CompilerCookie.Compile");
                GCompiler compiler = new GCompiler(job,(GDataObject)obj);
                /*if (!compiler.isUpToDate()) {
                  compiler.compile();
            }*/
            }
            if (type.isAssignableFrom(CompilerCookie.Build.class)) {
                System.out.println("CompilerCookie.Build");
                GCompiler compiler = new GCompiler(job,(GDataObject)obj);
                //compiler.compile();
            }
            if (type.isAssignableFrom(CompilerCookie.Clean.class)) {
                System.out.println("CompilerCookie.Clean");
            }
            //job.start();
        }
    }

}