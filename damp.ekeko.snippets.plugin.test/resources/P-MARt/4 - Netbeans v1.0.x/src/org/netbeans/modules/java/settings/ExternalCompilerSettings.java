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

package org.netbeans.modules.java.settings;

import java.io.File;
import java.io.IOException;

import org.openide.options.SystemOption;
import org.openide.compiler.ExternalCompilerGroup;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.HelpCtx;

/** A settings for external compiler.
*
* @author  Ales Novak
*/
public class ExternalCompilerSettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 2163925025759211535L;

    /** externalCompiler property name */
    public static final String PROP_EXTERNAL_COMPILER = "externalCompiler"; // NOI18N
    /** errorDescriptions property name */
    public static final String PROP_ERROR_DESCRIPTIONS = "errorDescriptions"; // NOI18N

    /** serialzed user defined compilers */
    private static ErrorDescriptions defs = new ErrorDescriptions ();

    /** The settings for external compiler's process (executable and classpath */
    private static NbProcessDescriptor externalCompiler;

    static {
        String fileSeparator = java.io.File.separator;
        String javaRoot = System.getProperty("java.home") + fileSeparator + ".." + fileSeparator;

        /*
           The external compiler's CLASSPATH for JDK 1.2 contains:
           - the REPOSITORY
        */
        externalCompiler = new NbProcessDescriptor(
                               javaRoot + "bin" + fileSeparator + "javac", // NOI18N
                               "-classpath {" + ExternalCompilerGroup.Format.TAG_REPOSITORY + "}" + File.pathSeparatorChar + // NOI18N
                               "{" + ExternalCompilerGroup.Format.TAG_CLASSPATH + "}" + File.pathSeparatorChar + // NOI18N
                               "{" + ExternalCompilerGroup.Format.TAG_LIBRARY + "}" + File.pathSeparatorChar + // NOI18N
                               "{" + ExternalCompilerGroup.Format.TAG_BOOTCLASSPATH + "} " + // NOI18N
                               "{" + ExternalCompilerGroup.Format.TAG_FILES + "}" // NOI18N
                           );

    }

    /** human presentable name */
    public String displayName() {
        return null;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ExternalCompilerSettings.class);
    }

    public NbProcessDescriptor getExternalCompiler() {
        return externalCompiler;
    }

    public void setExternalCompiler(NbProcessDescriptor compiler) {
        externalCompiler = compiler;
        // [PENDING fire property change]
    }

    public ErrorDescriptions getErrorDescriptions() {
        return defs;
    }

    public void setErrorDescriptions(ErrorDescriptions s) {
        defs = s;
        // [PENDING fire property change]
    }
}

/*
 * Log
 *  12   src-jtulach1.11        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  11   src-jtulach1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   src-jtulach1.9         9/29/99  Ales Novak      CompilerType used
 *  9    src-jtulach1.8         7/2/99   Jesse Glick     More help IDs.
 *  8    src-jtulach1.7         6/11/99  Ales Novak      library item added
 *  7    src-jtulach1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    src-jtulach1.5         5/31/99  Jaroslav Tulach 
 *  5    src-jtulach1.4         5/17/99  Petr Hamernik   fixed bug #1638
 *  4    src-jtulach1.3         5/7/99   Ales Novak      getAllLibraries moved 
 *  3    src-jtulach1.2         4/23/99  Ales Novak      redesigned adding of 
 *       libs & modules
 *  2    src-jtulach1.1         4/21/99  Ales Novak      lib dir read for zip and
 *       jar files
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */


