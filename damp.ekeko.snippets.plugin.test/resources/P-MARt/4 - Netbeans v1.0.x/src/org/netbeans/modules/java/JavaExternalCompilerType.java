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

package org.netbeans.modules.java;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.IOException;

import org.openide.loaders.DataObject;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerType;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.ExternalCompiler;
import org.openide.compiler.ExternalCompiler.ErrorExpression;
import org.openide.compiler.ExternalCompilerGroup;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.java.settings.ErrorDescriptions;
import org.netbeans.modules.java.settings.JavaSettings;

/**
*
* @author Ales Novak
*/
public class JavaExternalCompilerType extends JavaCompilerType {

    /** generated Serialized Version UID */
    static final long serialVersionUID = 2163925425759211535L;

    /** externalCompiler property name */
    public static final String PROP_EXTERNAL_COMPILER = "externalCompilerType"; // NOI18N
    /** errorDescriptions property name */
    public static final String PROP_ERROR_DESCRIPTIONS = "errorDescriptions"; // NOI18N

    /** serialzed user defined compilers */
    private ErrorExpression errorExpression = ExternalCompiler.JAVAC;

    /** The settings for external compiler's process (executable and classpath */
    protected NbProcessDescriptor externalCompiler;

    private String optimizeReplace = "-O"; // NOI18N
    private String debuginfoReplace = "-g"; // NOI18N
    private String deprecationReplace = "-deprecation"; // NOI18N
    private String encodingReplace = "-encoding"; // NOI18N

    private static NbProcessDescriptor JAVAC;

    static {
        String fileSeparator = java.io.File.separator;

        /*
           The external compiler's CLASSPATH for JDK 1.2 contains:
           - the REPOSITORY
        */

        JAVAC = new NbProcessDescriptor(
                    "{" + ExternalCompilerGroup.Format.TAG_JAVAHOME + "}{" + ExternalCompilerGroup.Format.TAG_SEPARATOR + "}..{" + // NOI18N
                    ExternalCompilerGroup.Format.TAG_SEPARATOR + "}bin{" + ExternalCompilerGroup.Format.TAG_SEPARATOR + "}javac", // NOI18N
                    "-classpath {" + ExternalCompilerGroup.Format.TAG_REPOSITORY + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}" + // NOI18N
                    "{" + ExternalCompilerGroup.Format.TAG_CLASSPATH + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}" + // NOI18N
                    "{" + ExternalCompilerGroup.Format.TAG_LIBRARY + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}" + // NOI18N
                    "{" + ExternalCompilerGroup.Format.TAG_BOOTCLASSPATH + "} " + // NOI18N
                    "{" + ExternalCompilerGroup.Format.TAG_FILES + "}", // NOI18N
                    NbBundle.getBundle (ExternalCompilerGroup.class).getString ("MSG_ExternalCompilerHint")
                );
    }

    public JavaExternalCompilerType() {
        externalCompiler = JAVAC;
    }

    /** human presentable name */
    public String displayName() {
        // Default only matters for regular external, which is
        // instantiated using Class.newInstance:
        return getString("CTL_ExternalCompilerType");
        // In the case of the FastJavac, the .ser is given the token
        // name; when initially loaded from manifest, readObject
        // gives it a localized name. Thereafter the localized name
        // is serialized in the project.
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(JavaExternalCompilerType.class);
    }

    public NbProcessDescriptor getExternalCompiler() {
        return externalCompiler;
    }

    public void setExternalCompiler(NbProcessDescriptor compiler) {
        externalCompiler = compiler;
        // [PENDING fire property change]
    }

    public ErrorExpression getErrorExpression() {
        return errorExpression;
    }

    public void setErrorExpression(ErrorExpression e) {
        if (e == null) {
            throw new IllegalArgumentException();
        }
        errorExpression = e;
        // [PENDING fire property change]
    }

    public void setOptimizeReplace(String x) {
        optimizeReplace = x;
    }
    public void setDebuginfoReplace(String x) {
        debuginfoReplace = x;
    }
    public void setDeprecationReplace(String x) {
        deprecationReplace = x;
    }
    public void setEncodingReplace(String x) {
        encodingReplace = x;
    }

    public String getOptimizeReplace() {
        return optimizeReplace;
    }
    public String getDebuginfoReplace() {
        return debuginfoReplace;
    }
    public String getDeprecationReplace() {
        return deprecationReplace;
    }
    public String getEncodingReplace() {
        return encodingReplace;
    }

    /** @return compiler group class */
    protected Class getCompilerGroupClass() {
        return JExternalCompilerGroup.class;
    }

    /** @return compiler for given classtype and file object */
    protected Compiler createCompiler(Class type, FileObject fo) {
        return new JExternalCompiler(fo, type, settingize(getExternalCompiler()),
                                     getErrorExpression(),
                                     this
                                    );
    }

    /**
    * @param desc is a NbProcessDescriptor into which the optimize - deprecation - ... tags are inserted.
    */
    private NbProcessDescriptor settingize(NbProcessDescriptor desc) {
        String params = desc.getArguments();
        if (getOptimize()) {
            params = checkOptimize(params);
        }
        if (getDebug()) {
            params = checkDebug(params);
        }
        if (getDeprecation()) {
            params = checkDeprecations(params);
        }
        if (getCharEncoding() != null) {
            params = checkEncoding(params);
        }
        if (desc.getArguments() != params) {
            desc = new NbProcessDescriptor(desc.getProcessName(), params, desc.getInfo());
        }

        return desc;
    }

    private static String checkOptimize(String params) {
        if (params.indexOf(JExternalCompilerGroup.JFormat.TAG_OPTIMIZE) < 0) {
            params = "{" + JExternalCompilerGroup.JFormat.TAG_OPTIMIZE + "} " + params; // NOI18N
        }
        return params;
    }
    private static String checkDebug(String params) {
        if (params.indexOf(JExternalCompilerGroup.JFormat.TAG_DEBUGINFO) < 0) {
            params = "{" + JExternalCompilerGroup.JFormat.TAG_DEBUGINFO + "} " + params; // NOI18N
        }
        return params;
    }
    private static String checkDeprecations(String params) {
        if (params.indexOf(JExternalCompilerGroup.JFormat.TAG_DEPRECATION) < 0) {
            params = "{" + JExternalCompilerGroup.JFormat.TAG_DEPRECATION + "} " + params; // NOI18N
        }
        return params;
    }
    private static String checkEncoding(String params) {
        if (params.indexOf(JExternalCompilerGroup.JFormat.TAG_ENCODING) < 0) {
            params = "{" + JExternalCompilerGroup.JFormat.TAG_ENCODING + "} " + params; // NOI18N
        }
        return params;
    }
}

/*
* Log
*  20   Gandalf-post-FCS1.18.1.0    3/24/00  Ales Novak      FastJavacCompilerType 
*       cloned
*  19   Gandalf   1.18        2/16/00  Ales Novak      #5788
*  18   Gandalf   1.17        1/24/00  Ales Novak      #5523
*  17   Gandalf   1.16        1/18/00  Jesse Glick     Various BeanInfo and 
*       localization fixes for Java compiler types.
*  16   Gandalf   1.15        1/12/00  Petr Hamernik   i18n: perl script used ( 
*       //NOI18N comments added )
*  15   Gandalf   1.14        1/10/00  Ales Novak      new compiler API deployed
*  14   Gandalf   1.13        1/4/00   Ales Novak      FastJavac - default 
*       compiler
*  13   Gandalf   1.12        12/22/99 Ales Novak      fastjavac for LINUX
*  12   Gandalf   1.11        12/22/99 Ales Novak      
*  11   Gandalf   1.10        12/22/99 Ales Novak      fastjavac -msgfile option
*       is used
*  10   Gandalf   1.9         12/16/99 Ales Novak      Linux for x86
*  9    Gandalf   1.8         11/30/99 Ales Novak      cleaning is 
*       FileSystem.AtomicAction   processing of javac errors moved into 
*       JavaCompilerGroup
*  8    Gandalf   1.7         11/15/99 Petr Hamernik   fastjavac hack disabled 
*       (internal compiler is used instead)
*  7    Gandalf   1.6         11/12/99 Ales Novak      fastjavac name
*  6    Gandalf   1.5         11/10/99 Ales Novak      fastjavac
*  5    Gandalf   1.4         11/9/99  Ales Novak      bugfix
*  4    Gandalf   1.3         10/26/99 Ales Novak      #4491
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems copyright in file comment
*  2    Gandalf   1.1         10/6/99  Ales Novak      the 
*       prepareJobForFileObject method added
*  1    Gandalf   1.0         9/29/99  Ales Novak      
* $
*/