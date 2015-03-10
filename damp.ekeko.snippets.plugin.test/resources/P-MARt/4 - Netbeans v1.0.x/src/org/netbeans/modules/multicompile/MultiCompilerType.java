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

import org.openide.ServiceType;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.cookies.CompilerCookie;
import org.openide.execution.NbProcessDescriptor;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Runs an external process to "compile" a type of file.
 *
 * @author jglick
 */
public class MultiCompilerType extends CompilerType {

    /** Handle for first-stage compiler.
     *@serial First stage compiler. */
    private ServiceType.Handle firstCompiler =null;
    /** Handle for second-stage compiler.
     *@serial Second stage compiler. */
    private ServiceType.Handle secondCompiler =null;

    static final long serialVersionUID =5008299979443901129L;
    public MultiCompilerType() {
    }

    public CompilerType getFirstCompiler () {
        if (firstCompiler != null)
            return (CompilerType) firstCompiler.getServiceType ();
        else
            return null;
    }

    public synchronized void setFirstCompiler (CompilerType nue) {
        CompilerType old = getFirstCompiler ();
        firstCompiler = new ServiceType.Handle (nue);
        firePropertyChange ("firstCompiler", old, nue);
    }

    public CompilerType getSecondCompiler () {
        if (secondCompiler != null)
            return (CompilerType) secondCompiler.getServiceType ();
        else
            return null;
    }

    public synchronized void setSecondCompiler (CompilerType nue) {
        CompilerType old = getSecondCompiler ();
        secondCompiler = new ServiceType.Handle (nue);
        firePropertyChange ("secondCompiler", old, nue);
    }

    /* [PENDING]
    public HelpCtx getHelpCtx () {
      return new HelpCtx (MultiCompilerType.class);
}
    */

    public void prepareJob (CompilerJob job, Class type, DataObject obj) {
        if (firstCompiler == null || secondCompiler == null) return;
        CompilerType first = (CompilerType) firstCompiler.getServiceType ();
        CompilerJob firstJob = new CompilerJob (job.getInitialDepth ());
        firstJob.setDisplayName (first.getName ());
        first.prepareJob (firstJob, type, obj);
        CompilerType second = (CompilerType) secondCompiler.getServiceType ();
        CompilerJob secondJob = new CompilerJob (job.getInitialDepth ());
        secondJob.setDisplayName (second.getName ());
        second.prepareJob (secondJob, type, obj);

        if (type.equals (CompilerCookie.Compile.class) || type.equals (CompilerCookie.Build.class)) {
            // Progress in the forward direction.
            secondJob.dependsOn (firstJob);
            job.dependsOn (secondJob);
        } else if (type.equals (CompilerCookie.Clean.class)) {
            // Perform in reverse.
            firstJob.dependsOn (secondJob);
            job.dependsOn (firstJob);
        } else {
            // do not do anything--unrecognized compilation type
        }
    }

}