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

import java.util.*;
import java.io.IOException;

import org.openide.TopManager;
import org.openide.cookies.CompilerCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.EnvironmentNotSupportedException;
import org.openide.filesystems.FileUtil;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.util.NbBundle;

/** Compiles (probably Java) sources via an external compiler process.
* The path to the executable, classpath, error-matching expressions, etc.
* are configurable.
*
* @author Ales Novak
*/
public class ExternalCompiler extends Compiler {

    /** locales */
    private static ResourceBundle bundle;

    /** constant denoting Sun's compiler */
    static final String SUN = getLocalizedString("CTL_Sun"); // NOI18N
    /** constant denoting MS compiler */
    static final String MICROSOFT = getLocalizedString("CTL_Microsoft"); // NOI18N
    /** constant denoting Jikes compiler */
    static final String IBM = getLocalizedString("CTL_Jikes"); // NOI18N

    /** regular expression descripting Sun's compiler error format */
    static final String SUN_ERROR_EXPR = "^([^ ][^\n]+):([0-9]+): (.*)"; // NOI18N
    /** regular expression descripting MS compiler error format */
    static final String MICROSOFT_ERROR_EXPR = "^([^\\(]+)\\(([0-9]+),([0-9]+)\\) : (.*)"; // NOI18N
    /** regexp of JIKES */
    static final String JIKES_ERROR_EXPR = "^([^ ]+):([0-9]+):([0-9]+):[0-9]+:[0-9]+:( |.*\n^)(.*)"; // NOI18N

    /** Error parsing for Sun's Javac. */
    public static final ErrorExpression JAVAC = new ErrorExpression(SUN, SUN_ERROR_EXPR, 1, 2, -1, 3);
    /** Error parsing for Microsoft's JVC. */
    public static final ErrorExpression JVC = new ErrorExpression(MICROSOFT, MICROSOFT_ERROR_EXPR, 1, 2, 3, 4);
    /** Error parsing for IBM's Jikes (with the <code>+E</code> switch). */
    public static final ErrorExpression JIKES = new ErrorExpression(IBM, JIKES_ERROR_EXPR, 1, 2, 3, 5);

    /** Constant for compilation. */
    public static final Object COMPILE = CompilerCookie.Compile.class;
    /** Constant for building. */
    public static final Object BUILD = CompilerCookie.Build.class;
    /** Constant for cleaning. */
    public static final Object CLEAN = CompilerCookie.Clean.class;


    /** File extension for class files. */
    protected static final String CLASS_EXTENSION = "class"; // NOI18N

    /** can create the name of the file to compile */
    private FNP fileNameProducer;
    /** external compiler description - which file to exec, its args... */
    private NbProcessDescriptor nbDescriptor;
    /** ErrorExpression valid for this instance of external compiler */
    private ErrorExpression errorExpression;
    /** Type of the compilation. */
    private Object type;

    /** Create an external compiler.
    * @param fo a file to compile
    * @param type the type of compilation ({@link #COMPILE}, {@link #BUILD}, or {@link #CLEAN})
    * @param nbDescriptor a description of an external compiler executable
    * @param err a regular expression to scan for compiler errors
    * @exception IllegalArgumentException if the file object is invalid
    */
    public ExternalCompiler(
        FileObject fo,
        Object type,
        NbProcessDescriptor nbDescriptor,
        ErrorExpression err
    ) {
        init (fo, type, nbDescriptor, err);
    }

    /** Create an external compiler.
    * @param job the compiler job to add to
    * @param fo a file to compile
    * @param type the type of compilation ({@link #COMPILE}, {@link #BUILD}, or {@link #CLEAN})
    * @param nbDescriptor a description of an external compiler executable
    * @param err a regular expression to scan for compiler errors
    * @exception IllegalArgumentException if the file object is invalid
    */
    public ExternalCompiler(CompilerJob job, FileObject fo, Object type, NbProcessDescriptor nbDescriptor, ErrorExpression err) {
        init (fo, type, nbDescriptor, err);

        job.add (this);
        registerInJob (job);
    }

    /** Create an external compiler with dependencies.
    * @param dependencies an array of compilers that are to be invoked before this one.
    * @param fo a file to compile
    * @param type the type of compilation ({@link #COMPILE}, {@link #BUILD}, or {@link #CLEAN})
    * @param nbDescriptor a description of an external compiler executable
    * @param err a regular expression to scan for compiler errors
    * @exception IllegalArgumentException if the file object is invalid
    */
    public ExternalCompiler(Compiler[] dependencies, FileObject fo, Object type, NbProcessDescriptor nbDescriptor, ErrorExpression err) {
        init (fo, type, nbDescriptor, err);

        dependsOn (Arrays.asList (dependencies));
        registerInJob (dependencies[0]);
    }

    /** Create an external compiler for a given java.io.File.
    * @param file the file to compile
    * @param type the type of compilation ({@link #COMPILE}, {@link #BUILD}, or {@link #CLEAN})
    * @param nbDescriptor a description of an external compiler executable
    * @param err a regular expression to scan for compiler errors
    */
    public ExternalCompiler(
        final java.io.File file,
        Object type,
        NbProcessDescriptor nbDescriptor,
        ErrorExpression err
    ) {
        if (
            file == null ||
            type == null ||
            nbDescriptor == null ||
            err == null
        ) {
            throw new IllegalArgumentException();
        }

        this.fileNameProducer = new FNP () {
                                    public String getFileName () {
                                        return file.toString ();
                                    }
                                    public FileObject getFileObject () {
                                        return null;
                                    }
                                    public boolean equalTo (FNP fnp) {
                                        return fnp.equalToFile (file);
                                    }
                                    public boolean equalToFile (java.io.File f) {
                                        return file.equals (f);
                                    }
                                    public boolean equalToResource (String res) {
                                        return false;
                                    }
                                    public boolean equalToResourceAndFS (String res, FileSystem fs) {
                                        return false;
                                    }
                                    public boolean equalToFileObject (FileObject obj) {
                                        return false;
                                    }
                                };
        this.nbDescriptor = nbDescriptor;
        this.errorExpression = err;
        this.type = type;
    }

    /** Create an external compiler for an object in repository
    * that still does not exists.
    *
    * @param fs file system to look on for resource
    * @param resourceName the name of resource to look for
    *
    * @param type the type of compilation ({@link #COMPILE}, {@link #BUILD}, or {@link #CLEAN})
    * @param nbDescriptor a description of an external compiler executable
    * @param err a regular expression to scan for compiler errors
    */
    public ExternalCompiler(
        final FileSystem fs,
        final String resourceName,
        Object type,
        NbProcessDescriptor nbDescriptor,
        ErrorExpression err
    ) {
        if (
            fs == null ||
            resourceName == null ||
            type == null ||
            nbDescriptor == null ||
            err == null
        ) {
            throw new IllegalArgumentException();
        }

        this.fileNameProducer = new FNP () {
                                    public String getFileName () {
                                        FileObject fo = fs.findResource (resourceName);
                                        if (fo == null) {
                                            return ""; // NOI18N
                                        }
                                        return NbClassPath.toFile (fo).toString ();
                                    }
                                    public FileObject getFileObject () {
                                        return fs.findResource (resourceName);
                                    }

                                    public boolean equalTo (FNP fnp) {
                                        return fnp.equalToResourceAndFS (resourceName, fs);
                                    }
                                    public boolean equalToFile (java.io.File f) {
                                        return false;
                                    }
                                    public boolean equalToResource (String res) {
                                        return false;
                                    }
                                    public boolean equalToResourceAndFS (String res, FileSystem fileSystem) {
                                        return fs.equals (fileSystem) && res.equals (resourceName);
                                    }
                                    public boolean equalToFileObject (FileObject obj) {
                                        return false;
                                    }

                                };
        this.nbDescriptor = nbDescriptor;
        this.errorExpression = err;
        this.type = type;
    }

    /** Create an external compiler for an object in repository
    * need not exist in present. Looks on all filesystems.
    *
    * @param resourceName the name of resource to look for
    *
    * @param type the type of compilation ({@link #COMPILE}, {@link #BUILD}, or {@link #CLEAN})
    * @param nbDescriptor a description of an external compiler executable
    * @param err a regular expression to scan for compiler errors
    */
    public ExternalCompiler(
        final String resourceName,
        Object type,
        NbProcessDescriptor nbDescriptor,
        ErrorExpression err
    ) {
        if (
            resourceName == null ||
            type == null ||
            nbDescriptor == null ||
            err == null
        ) {
            throw new IllegalArgumentException();
        }

        this.fileNameProducer = new FNP () {
                                    public String getFileName () {
                                        FileObject fo = FileSystemCapability.ALL.findResource (resourceName);
                                        if (fo == null) {
                                            return ""; // NOI18N
                                        }
                                        return NbClassPath.toFile (fo).toString ();
                                    }
                                    public FileObject getFileObject () {
                                        return FileSystemCapability.ALL.findResource (resourceName);
                                    }

                                    public boolean equalTo (FNP fnp) {
                                        return fnp.equalToResource (resourceName);
                                    }
                                    public boolean equalToFile (java.io.File f) {
                                        return false;
                                    }
                                    public boolean equalToResource (String res) {
                                        return res.equals (resourceName);
                                    }
                                    public boolean equalToResourceAndFS (String res, FileSystem fs) {
                                        return false;
                                    }
                                    public boolean equalToFileObject (FileObject obj) {
                                        return false;
                                    }

                                };
        this.nbDescriptor = nbDescriptor;
        this.errorExpression = err;
        this.type = type;
    }

    /** Initialization of basic parameters.
    */
    private void init (
        final FileObject fo,
        Object type,
        NbProcessDescriptor nbDescriptor,
        ErrorExpression err
    ) {
        if (
            fo == null ||
            type == null ||
            nbDescriptor == null ||
            err == null) {
            throw new IllegalArgumentException();
        }

        this.fileNameProducer = new FNP () {
                                    public String getFileName () {
                                        return org.openide.execution.NbClassPath.toFile (fo).toString ();
                                    }
                                    public FileObject getFileObject () {
                                        return fo;
                                    }

                                    public boolean equalTo (FNP fnp) {
                                        return fnp.equalToFileObject (fo);
                                    }
                                    public boolean equalToFile (java.io.File f) {
                                        return false;
                                    }
                                    public boolean equalToResource (String res) {
                                        return false;
                                    }
                                    public boolean equalToResourceAndFS (String res, FileSystem fs) {
                                        return false;
                                    }
                                    public boolean equalToFileObject (FileObject obj) {
                                        return obj.equals (fo);
                                    }
                                };
        this.nbDescriptor = nbDescriptor;
        this.errorExpression = err;
        this.type = type;
    }

    /** Get the description of the compiler executable.
     * @return the descriptor */
    public final NbProcessDescriptor getCompilerDescriptor() {
        return nbDescriptor;
    }

    /** Get the error expression used to parse error output from this compiler.
     * @return the error expression
     */
    public final ErrorExpression getErrorExpression() {
        return errorExpression;
    }

    /** Get the file object to be compiled
    * @return the file object (can be null if constructor without 
    *   file object argument has been used and the file object does not exists)
    */
    protected final FileObject getFileObject() {
        return fileNameProducer.getFileObject ();
    }

    /** Get the name of the file to be compiled.
    * @return the file name
    */
    public String getFileName() {
        return fileNameProducer.getFileName ();
    }

    /** @retrun <tt>true</tt> iff the fo is up to date */
    private static boolean isUpToDate(FileObject fo) {
        FileObject clsfo;

        // find all entries if the fo belongs to a MultiDataObject
        try {
            org.openide.loaders.DataObject dataobj = org.openide.loaders.DataObject.find(fo);
            java.util.Set files = dataobj.files();
            java.util.Iterator iter = files.iterator();
            int classfilescount = 0;

            while (iter.hasNext()) {
                clsfo = (FileObject) iter.next();
                if (clsfo.getExt().equals(CLASS_EXTENSION)) {
                    classfilescount++;
                    if (clsfo.lastModified().compareTo(fo.lastModified ()) < 0) {
                        // class fo does not exists or is older then the java source
                        // => compile the file
                        return false;
                    }
                }
            }
            if (classfilescount == 0) {
                return false;
            } else {
                return true;
            }
        } catch (org.openide.loaders.DataObjectNotFoundException e) {
            return false;
        }
    }

    /* inherited */
    public boolean isUpToDate() {
        // if (type == CLEAN) { error!!! }
        if (type == BUILD) {
            return false;
        } else {
            FileObject fo = getFileObject ();
            return fo == null || isUpToDate(fo);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getName());
        sb.append(" for "); // NOI18N
        sb.append(getFileName());
        sb.append(" "); // NOI18N
        if (type == COMPILE) sb.append("COMPILE");  // NOI18N
        if (type == BUILD) sb.append("BUILD");  // NOI18N
        if (type == CLEAN) sb.append("CLEAN");  // NOI18N
        return sb.toString();
    }

    /** @return ExternalCompilerGroup
    */
    public Class compilerGroupClass() {
        return ExternalCompilerGroup.class;
    }

    /** Identifier for type of compiler. This method allows subclasses to specify
    * the type this compiler belongs to. Compilers that belong to the same class
    * will be compiled together by one external process.
    * <P>
    * It is necessary for all compilers of the same type to have same process
    * descriptor and error expression.
    * <P>
    * This implementation returns the process descriptor, so all compilers
    * with the same descriptor will be compiled at once.
    * <p><em>Note</em> that this method has no relation to {@link CompilerType}s; the name is incidental.
    * @return key to define type of the compiler
    * @see ExternalCompilerGroup#createProcess
    * @deprecated While subclassing this method and specifying a type will still work,
    * it is no longer recommended. Instead, please use {@link Compiler#compilerGroupKey}
    * and make all compiler-specific state available to the compiler group via other means
    * (such as getter methods).
    */
    protected Object compilerType () {
        return nbDescriptor;
    }

    /** Produce a refined key for external compilers.
    * Groups not only by the compiler group class, but by the result of {@link #compilerType} as well,
    * and also by the error description.
    * Specialized external compilers are encouraged to override this method directly according to the
    * contract specified in {@link Compiler#compilerGroupKey}.
    * @return a composite key
    */
    public Object compilerGroupKey () {
        List l = new ArrayList (3);
        l.add (super.compilerGroupKey ());
        l.add (compilerType ());
        l.add (errorExpression);
        return l;
    }

    /** Two external compilers are the same if they have been constructed with
    * the same arguments. If one has been constructed with the 
    * java.io.File argument and second with resource name then they are different.
    *
    */
    public boolean equals (Object o) {
        if (o instanceof ExternalCompiler) {
            ExternalCompiler c = (ExternalCompiler)o;

            return
                fileNameProducer.equalTo (c.fileNameProducer) &&
                compilerGroupKey ().equals (c.compilerGroupKey ());
        }
        return false;
    }

    public int hashCode() {
        String fn = getFileName();
        return (fn == null) ? 0 : fn.hashCode();
    }

    /** @return localized String */
    static String getLocalizedString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ExternalCompiler.class);
        }
        return bundle.getString(s);
    }

    /** Encapsulates several properties needed for processing
    * the error output of an external compiler.
    */
    public static class ErrorExpression implements java.io.Serializable, Cloneable {
        /** name of the compiler */
        private String name;
        /** a regular expression */
        private String errordesc;
        /** position of the file with error inside the expression */
        private int filepos;
        /** position of the line */
        private int linepos;
        /** positiom of the column */
        private int columnpos;
        /** position of the description */
        private int descpos;

        static final long serialVersionUID =-2647801563993403964L;
        /** Create an error expression.
        * @param name display name
        * @param errordesc Perl5-style regular expression which should match lines containing error output
        * @param filepos index of backreference containing the filename of the error, or <code>-1</code> if none
        * @param linepos index of backreference containing the line number of the error, or <code>-1</code> if none
        * @param columnpos index of backreference containing the column number of the error, or <code>-1</code> if none
        * @param descpos index of backreference containing the description of the error, or <code>-1</code> if none
        */
        public ErrorExpression (String name, String errordesc, int filepos, int linepos, int columnpos, int descpos) {
            this.name = name;
            this.errordesc = errordesc;
            this.filepos = filepos;
            this.linepos = linepos;
            this.columnpos = columnpos;
            this.descpos = descpos;
        }

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                // cannot happen
                throw new InternalError ();
            }
        }

        /** Get the display name.
        * @return the display name
        */
        public String getName () {
            return name;
        }
        /** Set the display name.
        * @param d the new name
        */
        public void setName(String d) {
            name = d;
        }

        /** Get the error regexp.
        * @return the error regexp
        */
        public String getErrorExpression () {
            return errordesc;
        }
        /** Set the error regexp.
        * @param d the new regexp
        */
        public void setErrorExpression(String s) {
            errordesc = s;
        }

        /** Get the filename backreference.
        * @return the index, or <code>-1</code> for none
        */
        public int getFilePos () {
            return filepos;
        }
        /** Set the filename backreference.
        * @param d the new index (<code>-1</code> to disable)
        */
        public void setFilePos(int i) {
            filepos = i;
        }

        /** Get the line-number backreference.
        * @return the index, or <code>-1</code> for none
        */
        public int getLinePos () {
            return linepos;
        }
        /** Set the line-number backreference.
        * @param d the new index (<code>-1</code> to disable)
        */
        public void setLinePos(int i) {
            linepos = i;
        }

        /** Get the column-number backreference.
        * @return the index, or <code>-1</code> for none
        */
        public int getColumnPos () {
            return columnpos;
        }
        /** Set the column-number backreference.
        * @param d the new index (<code>-1</code> to disable)
        */
        public void setColumnPos(int i) {
            columnpos = i;
        }

        /** Get the description backreference.
        * @return the index, or <code>-1</code> for none
        */
        public int getDescriptionPos () {
            return descpos;
        }
        /** Set the description backreference.
        * @param d the new index (<code>-1</code> to disable)
        */
        public void setDescriptionPos(int i) {
            descpos = i;
        }

        public boolean equals (Object o) {
            if ((o == null) || (!(o instanceof ErrorExpression))) return false;
            ErrorExpression him = (ErrorExpression) o;
            return name.equals(him.name) &&
                   errordesc.equals(him.errordesc) &&
                   filepos == him.filepos &&
                   linepos == him.linepos &&
                   columnpos == him.columnpos &&
                   descpos == him.descpos;
        }

        public int hashCode () {
            return name.hashCode();
        }
    }

    /** Internal interface to provide different methods for locating of
    * file object and file name.
    */
    interface FNP {
        public String getFileName ();
        public FileObject getFileObject ();

        /** JST: Do you know how SmallTalk compares numbers? Let's try
        * the same comparing here....
        *
        * So three different subclasses => four comparing methods
        *
        * But as usual there is a room for improvement.
        */
        public boolean equalTo (FNP fnp);
        public boolean equalToFile (java.io.File f);
        public boolean equalToResource (String res);
        public boolean equalToResourceAndFS (String res, FileSystem fs);
        public boolean equalToFileObject (FileObject obj);
    }
}

/*
 * Log
 *  24   src-jtulach1.23        2/7/00   Ales Novak      #5656
 *  23   src-jtulach1.22        1/18/00  Jaroslav Tulach External Compiler is 
 *       initialized first and than its dependencies are handled (caused 
 *       problems in the hashCode) method.
 *  22   src-jtulach1.21        1/15/00  Petr Jiricka    hashCode() added.
 *  21   src-jtulach1.20        1/14/00  Petr Jiricka    toString() method added.
 *  20   src-jtulach1.19        1/12/00  Ian Formanek    NOI18N
 *  19   src-jtulach1.18        12/23/99 Jaroslav Tulach Enhancing compiler API 
 *       to makefile capabilities
 *  18   src-jtulach1.17        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  17   src-jtulach1.16        10/5/99  Ales Novak      isUpToDate method bugfix
 *  16   src-jtulach1.15        10/1/99  Jaroslav Tulach FileObject.move & 
 *       FileObject.copy
 *  15   src-jtulach1.14        9/29/99  Ales Novak      isUpToDate does not 
 *       delete files if CLEAN is specified
 *  14   src-jtulach1.13        9/10/99  Jesse Glick     Small API change: 
 *       ExternalCompiler.compilerType -> Compiler.compilerGroupKey.
 *  13   src-jtulach1.12        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  12   src-jtulach1.11        8/5/99   Ales Novak      BUILD actions remove 
 *       possibly generated classes before compilation
 *  11   src-jtulach1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   src-jtulach1.9         6/2/99   Jaroslav Tulach ExternalCompiler has 
 *       method for specifying its type.
 *  9    src-jtulach1.8         4/21/99  Jesse Glick     [JavaDoc]
 *  8    src-jtulach1.7         4/16/99  Martin Ryzl     getFileObject() added
 *  7    src-jtulach1.6         4/2/99   Jesse Glick     [JavaDoc]
 *  6    src-jtulach1.5         4/2/99   Ales Novak      
 *  5    src-jtulach1.4         4/1/99   Ales Novak      
 *  4    src-jtulach1.3         3/29/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/29/99  Jaroslav Tulach ErrorExpression.clone 
 *       does not throw error
 *  2    src-jtulach1.1         3/28/99  Ales Novak      
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */
