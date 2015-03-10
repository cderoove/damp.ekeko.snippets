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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.openide.TopManager;
import org.openide.ServiceType;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.cookies.CompilerCookie;
import org.openide.execution.NbProcessDescriptor;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** User-visible management of inner-test-class compilation.
 *
 * @author Jesse Glick
 */
public class InnerCompilerType extends CompilerType {

    /** The testing package root, with a default value.
     */
    private File testDir = new File (System.getProperty ("java.io.tmpdir"), "innertst");
    /** A handle for the regular Java compiler type.
     * Note that service types themselves should not be serialized
     * except by the services registry itself; other code should only
     * retain references via the service handle.
     */
    private ServiceType.Handle mainCompiler = null;
    /** The expected inner testing class name.
     */
    private String innerName = "TEST";

    private static final long serialVersionUID =-568513364525971057L;
    /** Bean getter.
     * @return the testing package root
     */
    public File getTestDir () {
        return testDir;
    }

    /** Bean setter.
     * @param nue the testing package root
     */
    public synchronized void setTestDir (File nue) {
        File old = testDir;
        testDir = nue;
        firePropertyChange ("testDir", old, nue);
    }

    /** Bean getter.
     * @return the regular Java compiler
     */
    public CompilerType getMainCompiler () {
        return mainCompiler == null ? null : ((CompilerType) mainCompiler.getServiceType ());
    }

    /** Bean setter.
     * @param nue the regular Java compiler
     */
    public synchronized void setMainCompiler (CompilerType nue) {
        CompilerType old = getMainCompiler ();
        mainCompiler = new ServiceType.Handle (nue);
        firePropertyChange ("mainCompiler", old, nue);
    }

    /** Bean getter.
     * @return the expected inner class name
     */
    public String getInnerName () {
        return innerName;
    }

    /** Bean setter.
     * @param nue the expected inner class name
     */
    public synchronized void setInnerName (String nue) {
        String old = innerName;
        innerName = nue;
        firePropertyChange ("innerName", old, nue);
    }

    /** Get context help for the compiler type.
     * @return the help context
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (InnerCompilerType.class);
    }

    /** Prepare the job to be run.
     * Always lets the regular Java compiler type add its own compiler(s) to the job.
     * Also adds its own special compiler to shuffle the inner test classes
     * around; these may need to be run after the regular compilers (thus use
     * compiler dependencies), or for cleaning it may be run in parallel.
     * @param job the job to prepare
     * @param type the type of compilation
     * @param obj the Java source object to be compiled
     */
    public void prepareJob (CompilerJob job, Class type, DataObject obj) {
        CompilerType main = getMainCompiler ();
        if (main == null) {
            TopManager.getDefault ().getIO (org.openide.util.NbBundle.getBundle(InnerCompilerType.class).
                                            getString ("LBL_io_tab_Compiler"), false).getOut ().println
            (MessageFormat.format (NbBundle.getBundle (InnerCompilerType.class).getString ("ERR_no_regular_compiler"),
                                   new Object[] { getName () }));
            return;
        }
        if (type.equals (CompilerCookie.Clean.class)) {
            // Clean cookie: clean normal classfiles and $TEST.class in parallel.
            job.add (new InnerCompiler (obj.getPrimaryFile (), testDir, innerName, type));
            main.prepareJob (job, type, obj);
        } else if (type.equals (CompilerCookie.Compile.class) ||
                   type.equals (CompilerCookie.Build.class)) {
            // Compile/clean: first run normal compiler, then move $TEST.class if needed.
            CompilerJob mainJob = new CompilerJob (job.getInitialDepth ());
            mainJob.setDisplayName (MessageFormat.format (NbBundle.getBundle (InnerCompilerType.class).
                                    getString ("LBL_main_job_for"),
                                    new Object[] { job.getDisplayName () }));
            main.prepareJob (mainJob, type, obj);
            Compiler compiler = new InnerCompiler (obj.getPrimaryFile (), testDir, innerName, type);
            job.add (compiler);
            compiler.dependsOn (mainJob);
        } else {
            // Something else: just use the normal compiler.
            main.prepareJob (job, type, obj);
        }
    }

}
