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

import java.util.ResourceBundle;
import java.io.File;

import org.openide.loaders.DataObject;
import org.openide.compiler.CompilerType;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.Compiler;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.cookies.CompilerCookie;

import sun.tools.java.Constants;

/**
*
* @author Jaroslav Tulach
*/
public abstract class JavaCompilerType extends CompilerType implements Constants {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1426544702124944362L;

    /** Constant for compilation. */
    public static final Object COMPILE = CompilerCookie.Compile.class;
    /** Constant for building. */
    public static final Object BUILD = CompilerCookie.Build.class;
    /** Constant for clean. */
    public static final Object CLEAN = CompilerCookie.Clean.class;

    /** bundle to obtain text information from */
    private static ResourceBundle bundle;

    /** compiler flags */
    private int flag = F_WARNINGS | F_DEBUG_VARS | F_DEBUG_LINES | F_DEBUG_SOURCE;

    /** character encoding */
    private String charEncoding;

    public int flag() {
        return flag;
    }

    private void orFlag(int f) {
        int oldflag = flag;
        flag |= f;
        firePropertyChange("flag", new Integer(oldflag), new Integer(flag)); // NOI18N
    }

    private void andFlag(int f) {
        int oldflag = flag;
        flag &= f;
        firePropertyChange("flag", new Integer(oldflag), new Integer(flag)); // NOI18N
    }

    public void setWarnings(boolean x) {
        boolean old = (flag & F_WARNINGS) != 0 ;
        if (x) {
            if (!old) orFlag(F_WARNINGS);
        }
        else if (old) andFlag(~F_WARNINGS);
        if (x != old)
            firePropertyChange("warnings", new Boolean(old), new Boolean(x)); // NOI18N
    }

    public boolean getWarnings() {
        return (flag & F_WARNINGS) != 0;
    }

    public void setDebug(boolean x) {
        boolean old = (flag & (F_DEBUG_VARS | F_DEBUG_LINES | F_DEBUG_SOURCE)) != 0;
        if (x) {
            if (!old) orFlag((F_DEBUG_VARS | F_DEBUG_LINES | F_DEBUG_SOURCE));
        }
        else if (old) andFlag(~(F_DEBUG_VARS | F_DEBUG_LINES | F_DEBUG_SOURCE));
        if (x != old)
            firePropertyChange("debug", new Boolean(old), new Boolean(x)); // NOI18N
    }

    public boolean  getDebug() {
        return (flag & (F_DEBUG_VARS | F_DEBUG_LINES | F_DEBUG_SOURCE)) != 0;
    }

    public void setOptimize(boolean x) {
        boolean old = (flag & (F_OPT | F_OPT_INTERCLASS)) != 0;
        if (x) {
            if (!old) orFlag((F_OPT | F_OPT_INTERCLASS));
        }
        else if (old) andFlag(~(F_OPT | F_OPT_INTERCLASS));
        if (x != old)
            firePropertyChange("optimize", new Boolean(old), new Boolean(x)); // NOI18N
    }

    public boolean getOptimize () {
        return (flag & (F_OPT | F_OPT_INTERCLASS)) != 0;
    }

    public void setDeprecation(boolean x) {
        boolean old = (flag & F_DEPRECATION) != 0;
        if (x) {
            if (!old) orFlag(F_DEPRECATION);
        }
        else if (old) andFlag(~F_DEPRECATION);
        if (x != old)
            firePropertyChange("deprecation", new Boolean(old), new Boolean(x)); // NOI18N
    }

    public boolean getDeprecation () {
        return  (flag & F_DEPRECATION) != 0;
    }

    public void setDependencies(boolean x) {
        boolean old = (flag & F_DEPENDENCIES) != 0;
        if (x) {
            if (!old) orFlag(F_DEPENDENCIES);
        }
        else if (old) andFlag(~F_DEPENDENCIES);
        if (x != old)
            firePropertyChange("dependencies", new Boolean(old), new Boolean(x)); // NOI18N
    }

    public boolean getDependencies () {
        return  (flag & F_DEPENDENCIES) != 0;
    }

    /** sets new character encoding
    * @param enc is a new encoding
    */
    public void setCharEncoding(String enc) {
        if (enc != null) {
            enc = enc.trim();
            if (enc.equals("")) {
                enc = null;
            }
        }
        String old = charEncoding;
        charEncoding = enc;
        firePropertyChange("encoding", old, charEncoding); // NOI18N
    }

    /** returns character encoding
    * @return encoding, null is possible encoding
    */
    public String getCharEncoding() {
        return charEncoding;
    }

    /** Prepare a data object for compilation.
    * Implementations should create an instance of a
    * suitable subclass of {@link Compiler}, passing
    * the compiler job to the constructor so that the job may
    * register the compiler.
    *
    * @param job compiler job to add compilers to
    * @param type the type of compilation task to manage
    * ({@link org.openide.cookies.CompilationCookie.Compile}, etc.)
    * @param obj data object to prepare for compilation
    */
    public void prepareJob(CompilerJob job, Class type, DataObject obj) {
        JavaDataObject jobj = (JavaDataObject) obj;
        if (type == CLEAN) {
            new CleanCompiler(job, jobj);
        } else {
            Compiler compiler = createCompiler(type, jobj.getPrimaryFile());
            if (type == BUILD) {
                compiler.dependsOn(new CleanCompiler(job, jobj));
            }
            job.add(compiler);
        }
    }

    /** Prepare a data object for compilation.
    * Implementations should create an instance of a
    * suitable subclass of {@link Compiler}, passing
    * the compiler job to the constructor so that the job may
    * register the compiler.
    *
    * @param job compiler job to add compilers to
    * @param type the type of compilation task to manage
    * ({@link org.openide.cookies.CompilationCookie.Compile}, etc.)
    * @param fo file object to prepare for compilation
    */
    public void prepareJobForFileObject(CompilerJob job, Class type, FileObject fo) {
        if (type == CLEAN) {
            throw new IllegalArgumentException();
        } else {
            Compiler c = createCompiler(type, fo);
            job.add(c);
        }
    }

    /** Prepare a data object for compilation.
    * Implementations should create an instance of a
    * suitable subclass of {@link Compiler}, passing
    * the compiler job to the constructor so that the job may
    * register the compiler.
    *
    * @param type the type of compilation task to manage
    * ({@link org.openide.cookies.CompilationCookie.Compile}, etc.)
    * @param fo file in wich to find the resource
    * @param resource file to compile
    */
    public IndirectCompiler prepareIndirectCompiler(Class type, FileSystem fs, String resource) {
        return new IndirectCompiler(type, fs, resource);
    }

    /** Prepare a data object for compilation.
    * Implementations should create an instance of a
    * suitable subclass of {@link Compiler}, passing
    * the compiler job to the constructor so that the job may
    * register the compiler.
    *
    * @param type the type of compilation task to manage
    * ({@link org.openide.cookies.CompilationCookie.Compile}, etc.)
    * @param fo file in wich to find the resource
    * @param resource file to compile
    */
    public IndirectCompiler prepareIndirectCompiler(Class type, String resource) {
        return new IndirectCompiler(type, null, resource);
    }

    /** @return compiler group class */
    protected abstract Class getCompilerGroupClass();

    /** @return compiler for given classtype and file object */
    protected abstract Compiler createCompiler(Class type, FileObject fo);

    /** @return localized name */
    static String getString(String name) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(JavaCompilerType.class);
        }
        return bundle.getString(name);
    }

    public final class IndirectCompiler extends Compiler {

        private final FileSystem dir;
        private final String res;
        private final Class type;

        private FileObject resolvedResource;

        public IndirectCompiler(Class type, FileSystem dir, String res) {
            this.dir = dir;
            this.type = type;
            if ((res != null) && (File.separatorChar != '/')) {
                res = res.replace(File.separatorChar, '/');
            }
            this.res = res;
        }

        public boolean isUpToDate() {
            return false;
        }

        public Class compilerGroupClass() {
            return getCompilerGroupClass();
        }

        public boolean equals(Object other) {
            return (this == other);
        }

        public int hashCode() {
            return (res == null) ? 4371234 /* any number */ : res.hashCode();
        }

        public Compiler getCompiler() {
            FileObject fo = getResolved();
            if (fo == null) {
                return null;
            } else {
                return createCompiler(type, fo);
            }
        }

        public FileObject getResolved() {
            if (resolvedResource == null) {
                if (res != null) {
                    if (dir == null) {
                        resolvedResource = FileSystemCapability.COMPILE.findResource(res);
                    } else {
                        resolvedResource = dir.findResource(res);
                    }
                }
            }
            return resolvedResource;
        }

        public void setResolved(FileObject fo) {
            resolvedResource = fo;
        }
    }
}

/*
* Log
*  12   Gandalf   1.11        2/16/00  Ales Novak      #5788
*  11   Gandalf   1.10        1/15/00  Petr Jiricka    Last changes rolled back 
*       + reimplementation of equals() and hashCode()
*  10   Gandalf   1.9         1/15/00  Petr Jiricka    One more small check in 
*       equals()
*  9    Gandalf   1.8         1/15/00  Petr Jiricka    equals() and hashCode() 
*       added to IndirectCompiler
*  8    Gandalf   1.7         1/12/00  Petr Hamernik   i18n: perl script used ( 
*       //NOI18N comments added )
*  7    Gandalf   1.6         1/10/00  Petr Jiricka    Fixed 
*       NullPointerException when not setting resource name.
*  6    Gandalf   1.5         1/10/00  Ales Novak      new compiler API deployed
*  5    Gandalf   1.4         11/30/99 Ales Novak      cleaning is 
*       FileSystem.AtomicAction   processing of javac errors moved into 
*       JavaCompilerGroup
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         10/6/99  Ales Novak      the 
*       prepareJobForFileObject method added
*  2    Gandalf   1.1         9/29/99  Ales Novak      CompilerType used
*  1    Gandalf   1.0         9/10/99  Jaroslav Tulach 
* $
*/