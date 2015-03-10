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


import org.openide.compiler.ExternalCompiler;
import org.openide.compiler.CompilerJob;
import org.openide.filesystems.FileObject;
import org.openide.execution.NbProcessDescriptor;

/**
*
* @author Ales Novak
*/
public class JExternalCompiler extends ExternalCompiler {

    /** Value of my compiler type */
    private JavaExternalCompilerType myType;
    /** Type of compilation */
    private Object type;

    private JCompilerType jtype;

    /** Create an external compiler.
    * @param job the compiler job to add to
    * @param fo a file to compile
    * @param type the type of compilation ({@link #COMPILE}, {@link #BUILD}, or {@link #CLEAN})
    * @param nbDescriptor a description of an external compiler executable
    * @param err a regular expression to scan for compiler errors
    * @exception IllegalArgumentException if the file object is invalid
    */
    public JExternalCompiler(CompilerJob job, FileObject fo, Object type, NbProcessDescriptor nbDescriptor, ErrorExpression err, JavaExternalCompilerType ctype) {
        super (job, fo, type, nbDescriptor, err);
        this.myType = ctype;
        this.type = type;
    }

    /** Create an external compiler.
    * @param fo a file to compile
    * @param type the type of compilation ({@link #COMPILE}, {@link #BUILD}, or {@link #CLEAN})
    * @param nbDescriptor a description of an external compiler executable
    * @param err a regular expression to scan for compiler errors
    * @exception IllegalArgumentException if the file object is invalid
    */
    public JExternalCompiler(FileObject fo, Object type, NbProcessDescriptor nbDescriptor, ErrorExpression err, JavaExternalCompilerType ctype) {
        super (fo, type, nbDescriptor, err);
        this.myType = ctype;
        this.type = type;
    }

    /** Create an external compiler with dependencies.
    * @param dependencies an array of compilers that are to be invoked before this one.
    * @param fo a file to compile
    * @param type the type of compilation ({@link #COMPILE}, {@link #BUILD}, or {@link #CLEAN})
    * @param nbDescriptor a description of an external compiler executable
    * @param err a regular expression to scan for compiler errors
    * @exception IllegalArgumentException if the file object is invalid
    */
    public JExternalCompiler(org.openide.compiler.Compiler[] dependencies, FileObject fo, Object type, NbProcessDescriptor nbDescriptor, ErrorExpression err, JavaExternalCompilerType ctype) {
        super (dependencies, fo, type, nbDescriptor, err);
        this.myType = ctype;
        this.type = type;
    }

    /** @return ExternalCompilerGroup
    */
    public Class compilerGroupClass() {
        return JExternalCompilerGroup.class;
    }

    /** @return <tt>true</tt> if the compilers are equal */
    public boolean equals(Object o) {
        if (o instanceof JExternalCompiler) {
            JExternalCompiler him = (JExternalCompiler) o;
            return (super.equals(o) &&
                    (him.getFileObject() == getFileObject()) &&
                    (him.myType == myType));
        } else {
            return false;
        }
    }

    public int hashCode() {
        FileObject fo = getFileObject();
        return ((fo == null) ? 0 : fo.hashCode()) ^ ((myType == null) ? 0 : myType.hashCode());
    }

    protected Object compilerType() {
        if (jtype == null) {
            jtype = new JCompilerType(getCompilerDescriptor(), myType);
        }
        return jtype;
    }

    /* inherited */
    public boolean isUpToDate() {
        // if (type == CLEAN) { error!!! }
        if (type == BUILD) {
            return false;
        } else {
            FileObject fo = getFileObject();
            return fo == null || org.netbeans.modules.java.gj.JavaCompiler.isUpToDate(fo);
        }
    }

    static class JCompilerType {

        NbProcessDescriptor desc;
        JavaExternalCompilerType jtype;

        JCompilerType(NbProcessDescriptor desc, JavaExternalCompilerType jtype) {
            this.desc = desc;
            this.jtype = jtype;
        }

        public boolean equals(Object o) {
            if (o instanceof JCompilerType) {
                JCompilerType him = (JCompilerType) o;
                return (him.desc.equals(desc) && him.jtype == jtype);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return desc.hashCode() ^ jtype.hashCode();
        }
    }
}
// Generated from substitution template 'Class' Blocks:Class:13,15,29,15; Package:8,2,24,2;

/*
 * Log
 *  6    Gandalf   1.5         1/14/00  Petr Jiricka    Duplicate compilers 
 *       fixed.
 *  5    Gandalf   1.4         1/10/00  Ales Novak      new compiler API 
 *       deployed
 *  4    Gandalf   1.3         1/5/00   Ales Novak      isUpToDate improved for 
 *       JExternalCompiler
 *  3    Gandalf   1.2         1/5/00   Ales Novak      equals methods
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/29/99  Ales Novak      
 * $
 */
