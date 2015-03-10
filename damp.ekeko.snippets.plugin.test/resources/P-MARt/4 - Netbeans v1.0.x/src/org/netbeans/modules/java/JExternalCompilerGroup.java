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

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;

import org.openide.compiler.Compiler;
import org.openide.compiler.ExternalCompilerGroup;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileSystemCapability;


/**
*
* @author Ales Novak, Jaroslav Tulach
*/
public class JExternalCompilerGroup extends ExternalCompilerGroup {
    /**
     * @associates Compiler 
     */
    private final HashSet indirectCompilers;

    /**
     * @associates Compiler 
     */
    private final HashSet directCompilers;

    /** Create an external compiler group. */
    public JExternalCompilerGroup() {
        indirectCompilers = new HashSet(7);
        directCompilers = new HashSet(7);
    }

    /** Add a compiler */
    public void add(Compiler c) {
        if (c instanceof JavaCompilerType.IndirectCompiler) {
            indirectCompilers.add(c);
        } else {
            checkAndAdd(c);
        }
    }

    /** Checks for duplicity and adds the compiler if it has not been added yet. */
    private void checkAndAdd(Compiler c) {
        if (((JExternalCompiler)c).isUpToDate()) {
            return;
        }

        if (directCompilers.add(c)) {
            super.add(c);
        }
    }

    /** Start the group */
    public boolean start() {
        if (indirectCompilers.size() > 0) {
            JavaCompilerType.IndirectCompiler[] cs =
                new JavaCompilerType.IndirectCompiler[indirectCompilers.size()];
            cs = (JavaCompilerType.IndirectCompiler[]) indirectCompilers.toArray(cs);

            for (int i = 0; i < cs.length; i++) {
                Compiler compiler = cs[i].getCompiler();
                if (compiler != null) {
                    checkAndAdd(compiler);
                }
            }
        }
        return super.start();
    }

    /** Allows subclasses to provide their own format for parsing
    * the arguments of NbProcessDescriptor contained in the
    * ExternalCompiler.
    * <P>
    * This implementation creates new format Format with settings
    * from NbClassPath.createXXXX and executes them in the provided
    * process descriptor.
    *
    * @param desc description of program to start
    * @param files the argument to compiler list of files to compile (or reference
    *   to the file with @files)
    * @return format to use for changing the command line of the compiler
    * @exception IOException if exec fails
    */
    protected Process createProcess (
        NbProcessDescriptor desc, String[] files, Object compilerType
    ) throws IOException {
        return desc.exec (new JFormat(files, compilerType, desc));
    }

    /** Default format that can format tags related to compilation. These include settings of classpath
    * (can be composed from repository, class path, boot class path and libraries) and 
    * putting somewhere list of files to compile.
    */
    public static class JFormat extends Format {
        /** Tag replaced with -O */
        public static final String TAG_OPTIMIZE = "optimize"; // NOI18N
        /** Tag replaced with -g */
        public static final String TAG_DEBUGINFO = "debuginfo"; // NOI18N
        /** Tag replaced with -deprecation */
        public static final String TAG_DEPRECATION = "deprecation"; // NOI18N
        /** Tag replaced with -encoding */
        public static final String TAG_ENCODING = "encoding"; // NOI18N
        /** Tag replaced with netbeans.home property value */
        public static final String TAG_NBHOME = "netbeans.home"; // NOI18N
        /** Tag replaced with netbeans.home property value */
        public static final String TAG_FORTEHOME = "forte.home"; // NOI18N
        /** Tag replaced by "netbeans.home/lib/fastjavac.messages */
        public static final String TAG_MSGFILE = Util.getString("TAG_Msgfile"); // NOI18N

        static final long serialVersionUID =-8630048324703405233L;

        /** My NbProcessDescriptor */
        private NbProcessDescriptor descriptor;

        /** All values for the paths takes from NbClassPath.createXXX methods.
        *
        * @param files files to compile
        */
        public JFormat (String[] files, Object compilerType, NbProcessDescriptor descriptor) throws IOException {
            super (
                files,
                NbClassPath.createClassPath(),
                NbClassPath.createBootClassPath(),
                NbClassPath.createRepositoryPath(FileSystemCapability.COMPILE),
                NbClassPath.createLibraryPath()
            );

            this.descriptor = descriptor;

            java.util.Map map = getMap();

            if (compilerType instanceof JExternalCompiler.JCompilerType) {
                JExternalCompiler.JCompilerType ctype = (JExternalCompiler.JCompilerType) compilerType;
                JavaExternalCompilerType jext = ctype.jtype;
                map.put(TAG_OPTIMIZE, jext.getOptimizeReplace());
                map.put(TAG_DEBUGINFO, jext.getDebuginfoReplace());
                map.put(TAG_DEPRECATION, jext.getDeprecationReplace());
                map.put(TAG_ENCODING, createEncodingReplace(jext));
                String nb = System.getProperty(TAG_NBHOME);
                nb = new java.io.File(nb).getCanonicalPath();
                map.put(TAG_FORTEHOME, nb);
                map.put(TAG_NBHOME, nb);
                map.put(TAG_JAVAHOME, getJavaHome());
                map.put(TAG_MSGFILE, getMsgFile(nb));
            } else {
                map.put(TAG_OPTIMIZE, ""); // NOI18N
                map.put(TAG_DEBUGINFO, ""); // NOI18N
                map.put(TAG_DEPRECATION, ""); // NOI18N
                map.put(TAG_ENCODING, ""); // NOI18N
            }
        }

        /** Creates -encoding <spec enc> */
        static String createEncodingReplace(JavaExternalCompilerType jext) {
            return jext.getEncodingReplace() + " " + jext.getCharEncoding();
        }

        /** @return java.home property - if the String contains one or more spaces
        * then the String is returned inside ""
        */
        static String getJavaHome() {
            String ret = System.getProperty("java.home"); // NOI18N
            int idx = ret.indexOf(' ');
            if (idx >= 0) {
                StringBuffer sb = new StringBuffer(ret);
                sb.insert(0, '"');
                sb.append('"');
                ret = sb.toString();
            }
            return ret;
        }
    }

    /** @return location of mmsgfile fo rfastjavac */
    static String getMsgFile(String nbhome) {
        StringBuffer sb = new StringBuffer(nbhome);
        sb.append(java.io.File.separatorChar);
        sb.append("lib"); // NOI18N
        sb.append(java.io.File.separatorChar);
        sb.append("fastjavac.messages"); // NOI18N
        String ret = sb.toString();
        if (ret.indexOf(' ') >= 0) {
            sb.insert(0, '"');
            sb.append('"');
            ret = sb.toString();
        }
        return ret;
    }
}

/*
 * Log
 *  13   Gandalf   1.12        2/16/00  Ales Novak      #5788
 *  12   Gandalf   1.11        1/24/00  Ales Novak      #5523
 *  11   Gandalf   1.10        1/16/00  Petr Jiricka    Fixed problem with equal
 *       compilers with different cookies (Build/Compile)
 *  10   Gandalf   1.9         1/14/00  Petr Jiricka    Duplicate compilers 
 *       fixed.
 *  9    Gandalf   1.8         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  8    Gandalf   1.7         1/10/00  Ales Novak      new compiler API 
 *       deployed
 *  7    Gandalf   1.6         1/6/00   Ales Novak      again previous
 *  6    Gandalf   1.5         1/6/00   Ales Novak      FastJavac accepts spaces
 *       in java.home
 *  5    Gandalf   1.4         12/22/99 Ales Novak      fastjavac -msgfile 
 *       option is used
 *  4    Gandalf   1.3         11/10/99 Ales Novak      fastjavac
 *  3    Gandalf   1.2         11/9/99  Ales Novak      parameters of an 
 *       external compiler passed through @
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/29/99  Ales Novak      
 * $
 */
