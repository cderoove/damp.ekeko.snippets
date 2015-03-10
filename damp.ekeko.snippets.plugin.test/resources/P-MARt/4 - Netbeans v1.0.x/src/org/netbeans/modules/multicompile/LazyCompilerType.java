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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.cookies.CompilerCookie;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Runs an external process to "compile" a type of file.
 *
 * @author jglick
 */
public class LazyCompilerType extends ExternalCompilerType {

    /** The default value for an unconfigured compiler.
     *@serial File extension for source files. */
    private String inExt ="java";
    /** The default value for an unconfigured compiler.
     *@serial File extension for output files. */
    private String outExt ="class";

    static final long serialVersionUID =-2616241687160254661L;

    public LazyCompilerType() {
    }

    public String getInExt () {
        return inExt;
    }

    public void setInExt (String nue) {
        String old = inExt;
        inExt = nue;
        firePropertyChange ("inExt", old, nue);
    }

    public String getOutExt () {
        return outExt;
    }

    public void setOutExt (String nue) {
        String old = outExt;
        outExt = nue;
        firePropertyChange ("outExt", old, nue);
    }

    /* [PENDING]
    public HelpCtx getHelpCtx () {
      return new HelpCtx (LazyCompilerType.class);
}
    */

    public void prepareJob (CompilerJob job, Class type, DataObject obj) {
        //System.err.println("LazyCompilerType.prepareJob; type=" + type.getName () + " obj=" + obj.getName () + " inExt=" + inExt + " outExt=" + outExt);
        try {
            if (type.equals (CompilerCookie.Compile.class)) {
                job.add (createLazyCompiler (obj, false));
            } else if (type.equals (CompilerCookie.Build.class)) {
                Compiler compile = createLazyCompiler (obj, true);
                compile.dependsOn (createLazyCleanCompiler (obj));
                job.add (compile);
            } else if (type.equals (CompilerCookie.Clean.class)) {
                job.add (createLazyCleanCompiler (obj));
            } else {
                // do not do anything--unrecognized compilation type
            }
        } catch (FileStateInvalidException fsie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                fsie.printStackTrace ();
        }
    }
    private Compiler createLazyCompiler (DataObject obj, boolean building) throws FileStateInvalidException {
        return new LazyCompiler (obj, building, getExternalCompiler (),
                                 getErrorExpression (), getInExt (), getOutExt ());
    }
    private Compiler createLazyCleanCompiler (DataObject obj) {
        return new LazyCleanCompiler (obj, getInExt (), getOutExt ());
    }

}
