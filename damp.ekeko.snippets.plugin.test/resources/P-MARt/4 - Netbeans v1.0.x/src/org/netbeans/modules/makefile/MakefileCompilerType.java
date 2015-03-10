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

package org.netbeans.modules.makefile;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.cookies.CompilerCookie;
import org.openide.execution.NbProcessDescriptor;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Compiler which runs makefiles.
 * @author Jesse Glick
 */
public class MakefileCompilerType extends ExternalCompilerType {

    /* No special error expressions for now, probably not needed.
    static final ExternalCompiler.ErrorExpression[] ERROR_EXPRS = {
      new ExternalCompiler.ErrorExpression (NbBundle.getBundle (MakefileCompilerType.class).getString ("LBL_error_exp_1"),
                                            "(some|reg|exp|here)",
                                            1, 2, 3, 4),
};
    */

    /** Default process template.
     */
    private static final NbProcessDescriptor DEFAULT = new NbProcessDescriptor (
                // PROCESS NAME:
                "make",
                // LIST OF ARGUMENTS INCLUDING OPTIONS:
                "-C {" + MakefileCompilerGroup.Format.TAG_DIRECTORY + "} " +
                "-f {" + MakefileCompilerGroup.Format.TAG_MAKEFILE + "} " +
                "-s {" + MakefileCompilerGroup.Format.TAG_TARGET + "}",
                // DESCRIPTION FOR USER OF HOW TO MODIFY THE ARGUMENTS:
                NbBundle.getBundle (MakefileCompilerType.class).getString ("MSG_format_hint_MCT")
            );

    /** Current make target for regular makes.
     */
    private String target = ""; // default to use make's own default
    /** Current make clean target.
     */
    private String cleanTarget = "clean";
    /** Current make rebuild/force target.
     */
    private String forceTarget = ""; // default to not implement specially

    /** generated
     */
    private static final long serialVersionUID = 3900712857917300676L;

    /** Create a new compiler type.
     */
    public MakefileCompilerType () {
        setExternalCompiler (DEFAULT);
        // setErrorExpression (ERROR_EXPRS[0]);
    }

    /** Get the display name.
     * Workaround for 1.0 core bug; not needed in 1.1 core.
     * @return the name
     */
    protected String displayName () {
        try {
            return java.beans.Introspector.getBeanInfo (getClass ()).getBeanDescriptor ().getDisplayName ();
        } catch (Exception e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                e.printStackTrace ();
            return getClass ().getName ();
        }
    }

    /** Bean getter.
     * @return the normal target
     */
    public String getTarget () {
        return target;
    }

    /** Bean setter.
     * @param nue the normal target
     */
    public synchronized void setTarget (String nue) {
        String old = target;
        target = nue;
        firePropertyChange ("target", old, nue);
    }

    /** Bean getter.
     * @return the clean target
     */
    public String getCleanTarget () {
        return cleanTarget;
    }

    /** Bean setter.
     * @param nue the clean target
     */
    public synchronized void setCleanTarget (String nue) {
        String old = cleanTarget;
        cleanTarget = nue;
        firePropertyChange ("cleanTarget", old, nue);
    }

    /** Bean getter.
     * @return the force target
     */
    public String getForceTarget () {
        return forceTarget;
    }

    /** Bean setter.
     * @param nue the force target
     */
    public synchronized void setForceTarget (String nue) {
        String old = forceTarget;
        forceTarget = nue;
        firePropertyChange ("forceTarget", old, nue);
    }

    /** Get the help context.
     * @return help for the compiler
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.makefile.compiler");
    }

    /** Add a proper compiler to the job.
     * @param job the job
     * @param type the style of compilation, controlling the target
     * @param obj the makefile
     */
    public void prepareJob (CompilerJob job, Class type, DataObject obj) {
        if (type.equals (CompilerCookie.Compile.class))
            job.add (new MakefileCompiler (obj.getPrimaryFile (), getExternalCompiler (), getErrorExpression (), getTarget ()));
        else if (type.equals (CompilerCookie.Build.class))
            job.add (new MakefileCompiler (obj.getPrimaryFile (), getExternalCompiler (), getErrorExpression (), getForceTarget ()));
        else if (type.equals (CompilerCookie.Clean.class))
            job.add (new MakefileCompiler (obj.getPrimaryFile (), getExternalCompiler (), getErrorExpression (), getCleanTarget ()));
        else
            ; // do nothing
    }

}
