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

import java.io.IOException;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.*;

import org.openide.execution.*;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.util.MapFormat;
import org.openide.util.Utilities;

/** A group holding several <code>ExternalCompiler</code>s.
* When they are compiled, all the filename arguments are collected and the process is run
* only once.
* @see ExternalCompiler
*
* @author Ales Novak, Jaroslav Tulach
*/
public class ExternalCompilerGroup extends CompilerGroup {

    /** The compilers to be used. 
     * @associates Compiler*/
    private Set compilers = new HashSet (7); // Set<ExternalCompiler>

    /** flag for indicating errors */
    private boolean dirty;

    /** dimension */
    //private static final int DIM = 7;

    /** Create an external compiler group. */
    public ExternalCompilerGroup() {
    }

    /* Consumes a compiler. Should absorb all information
    * contained in the compiler.
    *
    * @param c an instance of ExternalCompiler
    * @exception IllegalArgumentException if this compiler
    *   does not belong to this group (the group's class is not the
    *   same as the one returned from c.compilerGroupClass)
    */
    public void add (Compiler c) throws IllegalArgumentException {
        if (! (c instanceof ExternalCompiler)) {
            throw new IllegalArgumentException();
        }
        compilers.add (c);
        //System.err.println("ExternalCompilerGroup.add; c=" + c);
    }

    /** Allows subclasses to provide their own format for parsing
    * the arguments of NbProcessDescriptor contained in the
    * ExternalCompiler; assumes interesting content of "compiler type".
    * <P> By default, delegates to the variant that does not take a
    * "compiler type" argument, as this is deprecated usage.
    *
    * @param desc description of program to start
    * @param files the argument to compiler list of files to compile (or reference
    *   to the file with @files)
    * @param compilerType the type of compiler for all this files, 
    *   this is the compiler dependent object returned from method 
    *   ExternalCompiler.compilerType ()
    * @return format to use for changing the command line of the compiler
    * @exception IOException if exec fails
    *
    * @see ExternalCompiler#compilerType
    *
    * @deprecated Please instead directly override {@link #createProcess(NbProcessDescriptor,String[])}
    * as this version does not use the now-deprecated "compiler type" object.
    */
    protected Process createProcess (
        NbProcessDescriptor desc, String[] files, Object compilerType
    ) throws IOException {
        return createProcess (desc, files);
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
        NbProcessDescriptor desc, String[] files
    ) throws IOException {
        return desc.exec (new Format (files));
    }

    /** Creates human readable String used in status line - should contain
    * information what is compiled - Compiling MyClass.java
    *
    * @return String
    */
    protected String getStatusLineText() {
        String msg;

        if (compilers.size() == 1) {
            FileObject fo = getAllCompilers()[0].getFileObject();

            msg = java.text.MessageFormat.format(
                      getString("CTL_FMT_CompilingMessage"),
                      new Object[] { fo.getPackageName('.') });
        } else {
            msg = getString("FMT_GenericCompilingMessage");
        }
        return msg;
    }

    /**
    * @return an array containing all Compilers that were added to this 
    * CompilerGroup
    */
    protected final ExternalCompiler[] getAllCompilers() {
        return (ExternalCompiler[]) compilers.toArray(new ExternalCompiler[compilers.size()]);
    }

    /** access method for firing errors */
    void fireErrEvent(ErrorEvent ev) {
        if (ev.getFile() != null) {
            dirty = true;
        }
        fireErrorEvent(ev);
    }

    /* Starts compilation. It should check which files realy needs to be
    * compiled and compile only those which really need to.
    * <P>
    * The compilation should fire info to status listeners and report
    * all errors to error listeners.
    *
    * @return true if successful, false otherwise
    */
    public boolean start () {
        //System.err.println("ExternalCompilerGroup.start; this=" + this + "; thread=" + Thread.currentThread ());
        dirty = false;

        if (compilers.isEmpty ()) return true; // if no compilers have been added -> succeed

        CompilerExecutor cexec = null;
        Set folders = new HashSet (7); // Set<FileObject>
        Iterator it = compilers.iterator ();

        while (it.hasNext ()) {
            ExternalCompiler c = (ExternalCompiler) it.next ();
            if (cexec == null)
                cexec = new CompilerExecutor (this,
                                              c.getCompilerDescriptor (),
                                              c.getErrorExpression (),
                                              c.compilerType ());
            String name = c.getFileName ();
            //System.err.println("\tname=" + name + "; this=" + this);
            if (name != null && ! "".equals (name)) cexec.addFile (name); // NOI18N
            FileObject folder = c.getFileObject ();
            if (folder != null) {
                if (folder.isData ()) {
                    folder = folder.getParent ();
                }
                folders.add (folder);
            }
        }

        try {
            //System.err.println("\twill execute; this=" + this);
            int result = cexec.execute(new ExecInfo("ExternalCompilerGroup")).result(); // NOI18N
            //System.err.println("\tresult=" + result + "; this=" + this);
            dirty |= (result != 0);

            // refresh folders
            if (! dirty) {
                it = folders.iterator ();
                while (it.hasNext ())
                    ((FileObject) it.next ()).refresh ();
            }

            if (result == CompilerSysProcess.INTERRUPTED) {
                ErrorEvent ev = new ErrorEvent(this, null, -1, -1, getString("CTL_Interrupted"), null);
                fireErrorEvent(ev);
            }
        } catch (IOException ioe) {
            StringWriter swriter = new StringWriter();
            PrintWriter pw = new PrintWriter(swriter);
            ioe.printStackTrace(pw);
            fireErrorEvent (new ErrorEvent (
                                this,
                                null, // wrong
                                0, 0,
                                swriter.toString(),
                                "" // NOI18N
                            ));
            dirty = true;
        }

        return !dirty; // dirty == false means success
    }

    private static java.util.ResourceBundle bundle;

    private static String getString(String x) {
        if (bundle == null) {
            bundle = org.openide.util.NbBundle.getBundle(ExternalCompilerType.class);
        }
        return bundle.getString(x);
    }

    /** Default format that can format tags related to compilation. These include settings of classpath
    * (can be composed from repository, class path, boot class path and libraries) and 
    * putting somewhere list of files to compile.
    */
    public static class Format extends MapFormat {
        /** Tag replaced with ProcessExecutors.getClassPath () */
        public static final String TAG_CLASSPATH = ProcessExecutor.Format.TAG_CLASSPATH;
        /** Tag replaced with ProcessExecutors.getBootClassPath () */
        public static final String TAG_BOOTCLASSPATH = ProcessExecutor.Format.TAG_BOOTCLASSPATH;
        /** Tag replaced with ProcessExecutors.getRepositoryPath () */
        public static final String TAG_REPOSITORY = ProcessExecutor.Format.TAG_REPOSITORY;
        /** Tag replaced with ProcessExecutors.getLibraryPath () */
        public static final String TAG_LIBRARY = ProcessExecutor.Format.TAG_LIBRARY;
        /** Tag replaced with arguments of the program */
        public static final String TAG_FILES = "files"; // NOI18N
        /** Tag replaced with install directory of JDK */
        public static final String TAG_JAVAHOME = ProcessExecutor.Format.TAG_JAVAHOME;
        /** Tag replaced with separator between filename components */
        public static final String TAG_SEPARATOR = ProcessExecutor.Format.TAG_SEPARATOR;
        /** Tag replaced with separator between path components */
        public static final String TAG_PATHSEPARATOR = ProcessExecutor.Format.TAG_PATHSEPARATOR;

        static final long serialVersionUID =-8630048144603405233L;

        /** All values for the paths takes from NbClassPath.createXXX methods.
        *
        * @param files files to compile
        */
        public Format (String[] files) {
            this (
                files,
                NbClassPath.createClassPath (),
                NbClassPath.createBootClassPath (),
                NbClassPath.createRepositoryPath (FileSystemCapability.COMPILE),
                NbClassPath.createLibraryPath ()
            );
        }

        /** @param files files to compile
        * @param classPath to substitute instead of CLASSPATH
        * @param bootClassPath boot class path
        * @param repository repository path
        * @param library library path
        */
        public Format (
            String[] files,
            NbClassPath classPath,
            NbClassPath bootClassPath,
            NbClassPath repository,
            NbClassPath library
        ) {
            super (createMap7 ());

            java.util.Map map = getMap ();

            map.put (TAG_CLASSPATH, classPath.getClassPath ());
            map.put (TAG_BOOTCLASSPATH, bootClassPath.getClassPath ());
            map.put (TAG_REPOSITORY, repository.getClassPath ());
            map.put (TAG_LIBRARY, library.getClassPath ());
            map.put (TAG_FILES, asParameterString (files));
            map.put (TAG_JAVAHOME, System.getProperty ("java.home"));
            map.put (TAG_SEPARATOR, File.separator);
            map.put (TAG_PATHSEPARATOR, File.pathSeparator);

        }

        /** Helper method to allows conversion of list of files to compile to
        * one string that can be passed as parameter to external process.
        * On non Windows machines the method simply concatenates the strings
        * into one. On Windows, if the file count it greater then ten, it
        * creates temporary file, writes the strings into it and returns
        * "@filename" witch is accepted by common programmers instead of the
        * list of files.
        *
        * @param files array of files to compile
        * @return the string representing the files to compile or null if it
        *   cannot be created (like the temporary file cannot be created)
        */
        public static String asParameterString (String[] files) {
            if (files.length > 10 && Utilities.isWindows()) {
                File f = constructFile(files);
                if (f == null) return null;

                return "@" + f; // NOI18N
            } else {
                return constructString(files);
            }
        }

        /** Creates default size hash map.
        */
        private static java.util.HashMap createMap7 () {
            return new java.util.HashMap (7);
        }

        /** prefix for a tmp file */
        private static final String PREFIX = "compilerparams"; // NOI18N
        /** suffix for a tmp file */
        private static final String SUFFIX = "pms"; // NOI18N

        /** @return File containing all files to compile. */
        private static File constructFile(String[] files) {
            try {
                File f = File.createTempFile(PREFIX, SUFFIX);
                f.deleteOnExit();
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f)));

                Iterator iter = Arrays.asList (files).iterator();
                while (iter.hasNext()) {
                    pw.println((String) iter.next());
                }
                pw.close();

                return f;
            } catch (IOException e) {
                return null;
            }
        }

        /** @return StringBuffer containing all files to compile. */
        private static String constructString(String[] files) {
            StringBuffer sb = new StringBuffer ();
            String add = ""; // NOI18N

            for (int i = 0; i < files.length; i++) {
                sb.append (add);
                if (files[i].indexOf(' ') >= 0) {
                    sb.append("\""); // NOI18N
                    sb.append(files[i]);
                    sb.append("\""); // NOI18N
                } else {
                    sb.append (files[i]);
                }
                add = " "; // NOI18N
            }

            return sb.toString ();
        }

    }
}

/*
 * Log
 *  25   src-jtulach1.24        2/4/00   Ales Novak      #5556
 *  24   src-jtulach1.23        2/4/00   Jesse Glick     Fix to permit 
 *       ExternalCompiler's with new-style constructors (taking resource paths 
 *       or file names) to work in an ExternalCompilerGroup.
 *  23   src-jtulach1.22        1/24/00  Ales Novak      #5180
 *  22   src-jtulach1.21        1/12/00  Ian Formanek    NOI18N
 *  21   src-jtulach1.20        1/10/00  Ales Novak      stopCompile action
 *  20   src-jtulach1.19        1/10/00  Ales Novak      #5180
 *  19   src-jtulach1.18        1/8/00   Petr Jiricka    Fixed 
 *       NullPointerException (thrown when no compilers have been added to the 
 *       group)
 *  18   src-jtulach1.17        11/10/99 Ales Novak      last change is rolled 
 *       back
 *  17   src-jtulach1.16        11/9/99  Ales Novak      @ parameter using moved 
 *       into java DO
 *  16   src-jtulach1.15        11/8/99  Ales Novak      better notification of 
 *       exceptions
 *  15   src-jtulach1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   src-jtulach1.13        10/1/99  Jesse Glick     ExternalCompilerGroup 
 *       has process format consistent with ProcessExecutor.
 *  13   src-jtulach1.12        9/29/99  Ales Novak      isUpToDate check moved 
 *       into CompilationEngine
 *  12   src-jtulach1.11        9/10/99  Jesse Glick     Small API change: 
 *       ExternalCompiler.compilerType -> Compiler.compilerGroupKey.
 *  11   src-jtulach1.10        9/10/99  Jaroslav Tulach compiles with jikes.
 *  10   src-jtulach1.9         8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  9    src-jtulach1.8         8/11/99  Ales Novak      file names containing 
 *       space bug
 *  8    src-jtulach1.7         6/22/99  Jesse Glick     Made start() nonfinal.
 *  7    src-jtulach1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    src-jtulach1.5         6/2/99   Jaroslav Tulach createProcess receives 
 *       also compiler type.
 *  5    src-jtulach1.4         6/2/99   Jaroslav Tulach ExternalCompiler has 
 *       method for specifying its type.
 *  4    src-jtulach1.3         5/31/99  Jaroslav Tulach External Execution & 
 *       Compilation
 *  3    src-jtulach1.2         4/1/99   Ales Novak      
 *  2    src-jtulach1.1         3/29/99  Jesse Glick     [JavaDoc]
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */
