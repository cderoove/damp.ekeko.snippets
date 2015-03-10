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

package org.openide.compiler;

import java.io.File;

import org.openide.execution.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/** Compiler type that compiles using ExternalCompiler.
*
* @author Jaroslav Tulach
*/
public class ExternalCompilerType extends CompilerType {

    private static final long serialVersionUID = -4934327582488427081L;

    /** property identifying the external compiler */
    public static final String PROP_EXTERNAL_COMPILER = "externalCompiler"; // NOI18N
    /** property identifying external compiler output format */
    public static final String PROP_ERROR_EXPRESSION = "errorExpression"; // NOI18N

    private static final NbProcessDescriptor DEFAULT_DESCRIPTOR = new NbProcessDescriptor(
                "{" + ExternalCompilerGroup.Format.TAG_JAVAHOME + "}{" + ExternalCompilerGroup.Format.TAG_SEPARATOR + "}..{" + // NOI18N
                ExternalCompilerGroup.Format.TAG_SEPARATOR + "}bin{" + ExternalCompilerGroup.Format.TAG_SEPARATOR + "}javac", // NOI18N
                "-classpath {" + ExternalCompilerGroup.Format.TAG_REPOSITORY + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}" + // NOI18N
                "{" + ExternalCompilerGroup.Format.TAG_CLASSPATH + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}" + // NOI18N
                "{" + ExternalCompilerGroup.Format.TAG_LIBRARY + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}" + // NOI18N
                "{" + ExternalCompilerGroup.Format.TAG_BOOTCLASSPATH + "} " + // NOI18N
                "{" + ExternalCompilerGroup.Format.TAG_FILES + "}", // NOI18N
                NbBundle.getBundle (ExternalCompilerGroup.class).getString ("MSG_ExternalCompilerHint")
            );

    /** the descriptor to use for execution */
    private NbProcessDescriptor nbDescriptor = DEFAULT_DESCRIPTOR;
    /** error expression parser */
    private ExternalCompiler.ErrorExpression err = ExternalCompiler.JAVAC;

    /** Constructor */
    public ExternalCompilerType () {
    }

    protected String displayName () {
        return NbBundle.getBundle (ExternalCompilerType.class).getString ("LBL_ExternalCompilerType_name");
    }

    /** Getter for process descriptor describing the compiler.
    */
    public NbProcessDescriptor getExternalCompiler () {
        return nbDescriptor;
    }

    /** Setter for process descriptor describing the compiler.
    */
    public void setExternalCompiler (NbProcessDescriptor nb) {
        NbProcessDescriptor old = nbDescriptor;
        nbDescriptor = nb;
        firePropertyChange (PROP_EXTERNAL_COMPILER, old, nb);
    }

    /** Changes error expression.
    */
    public void setErrorExpression (ExternalCompiler.ErrorExpression ee) {
        ExternalCompiler.ErrorExpression old = err;
        err = ee;
        firePropertyChange (PROP_ERROR_EXPRESSION, old, ee);
    }

    /** Getter for current error expression.
    */
    public ExternalCompiler.ErrorExpression getErrorExpression () {
        return err;
    }

    /** Starts the compilation.
    */
    public void prepareJob(CompilerJob job, Class type, DataObject obj) {
        FileObject fo = obj.getPrimaryFile ();

        // adds the compiler to the job
        ExternalCompiler c = new ExternalCompiler(
                                 job, fo, type, nbDescriptor, err
                             );
    }

}

/*
* Log
*  5    Gandalf   1.4         1/12/00  Ian Formanek    NOI18N
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         10/1/99  Jesse Glick     ExternalCompilerGroup has
*       process format consistent with ProcessExecutor.
*  2    Gandalf   1.1         9/14/99  Jaroslav Tulach Error expressions.
*  1    Gandalf   1.0         9/10/99  Jaroslav Tulach 
* $
*/