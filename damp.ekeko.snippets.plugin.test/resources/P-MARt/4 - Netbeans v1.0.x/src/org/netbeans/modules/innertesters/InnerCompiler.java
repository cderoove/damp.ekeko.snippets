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

import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.cookies.CompilerCookie;
import org.openide.filesystems.*;

/** Compiler implementation which moves inner test classes out of the way.
 *
 * @author Jesse Glick
 */
public class InnerCompiler extends Compiler {

    /** Primary file of data object in question.
     */
    private FileObject fo;
    /** Directory root for testing classfiles.
     */
    private File testDir;
    /** Expected name of inner class.
     */
    private String innerName;
    /** Compile vs. build vs. clean.
     */
    private Class type;

    /** Create the compiler; just store the required information.
     * @param fo primary source file
     * @param testDir testing directory root
     * @param innerName name of inner class
     * @param type type of compilation
     */
    public InnerCompiler (FileObject fo, File testDir, String innerName, Class type) {
        this.fo = fo;
        this.testDir = testDir;
        this.innerName = innerName;
        this.type = type;
    }

    /** <CODE>Object</CODE> method; must be implemented exactly for compilation engine
     * to work correctly.
     * @return <CODE>true</CODE> if equal
     * @param o object to compare to
     */
    public boolean equals (Object o) {
        if (! (o instanceof InnerCompiler)) return false;
        InnerCompiler other = (InnerCompiler) o;
        try {
            return fo.getFileSystem ().getSystemName ().equals (other.fo.getFileSystem ().getSystemName ()) &&
                   fo.getPackageNameExt ('/', '.').equals (other.fo.getPackageNameExt ('/', '.')) &&
                   testDir.equals (other.testDir) &&
                   type.equals (other.type);
        } catch (FileStateInvalidException fsie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                fsie.printStackTrace ();
            return false;
        }
    }

    /** <CODE>Object</CODE> method; desirable to implement correctly.
     * At least must return the same for the same object.
     * @return the hash code
     */
    public int hashCode () {
        return 1234 ^ fo.getPackageNameExt ('/', '.').hashCode ();
    }

    /** Get the proper kind of compiler group.
     * @return the class to instantiate
     */
    public Class compilerGroupClass () {
        return InnerCompilerGroup.class;
    }

    /** Test whether it is up-to-date. This will be true if:
     * <ol>
     * <li>Not building.
     * <li>Compiling, and inner test classfile is in right destination,
     * and also is not in source area.
     * <li>Cleaning, and there is no inner test classfile.
     * </ol>
     * @return <CODE>true</CODE> if up-to-date
     */
    public boolean isUpToDate () {
        if (type.equals (CompilerCookie.Compile.class)) {
            File df = getDestFile ();
            FileObject sf = getSourceFile ();
            return df.isFile () && sf == null;
        } else if (type.equals (CompilerCookie.Build.class)) {
            return false;
        } else if (type.equals (CompilerCookie.Clean.class)) {
            return ! getDestFile ().isFile ();
        } else {
            throw new InternalError ();
        }
    }

    /** For use by the compiler group.
     * @return the primary source file related to the compilation
     */
    public FileObject getFileObject () {
        return fo;
    }

    /** Get the <CODE>.class</CODE> test classfile in the position as created
     * by the regular compiler.
     * @return the classfile, or <CODE>null</CODE>
     */
    public FileObject getSourceFile () {
        FileObject foParent = fo.getParent ();
        foParent.refresh ();
        return foParent.getFileObject (fo.getName () + '$' + innerName, "class");
    }

    /** Get the destination location for the inner test classfile.
     * @return the destination location
     */
    public File getDestFile () {
        return new File (testDir, fo.getPackageName (File.separatorChar) + '$' + innerName + ".class");
    }

    /** For use by the compiler group.
     * @return the testing package root
     */
    public File getTestDir () {
        return testDir;
    }

    /** For use by the compiler group.
     * @return the inner class name
     */
    public String getInnerName () {
        return innerName;
    }

    /** For use by the compiler group.
     * @return the type of compilation
     */
    public Class getType () {
        return type;
    }

}
