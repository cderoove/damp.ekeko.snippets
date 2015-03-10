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

import org.openide.compiler.ExternalCompiler;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;

/** Compiler for compilation of makefiles.
*
* @author Jaroslav Tulach, Jesse Glick
*/
public class MakefileCompiler extends ExternalCompiler {

    /** Target to use for make.
     */
    private String target;

    /** Create a new compiler.
     * @param fo the makefile
     * @param proc the command template
     * @param err how to recognize errors
     * @param target the make target to run
     */
    public MakefileCompiler (FileObject fo, NbProcessDescriptor proc, ExternalCompiler.ErrorExpression err, String target) {
        // Always treat as Build cookie, since the target actually decides all this:
        super (fo, BUILD, proc, err);
        this.target = target;
    }

    /** Get the make target to use.
     * @return the target
     */
    public String getTarget () {
        return target;
    }

    /** Get the compiler group class to use.
     * @return the class
     */
    public Class compilerGroupClass () {
        return MakefileCompilerGroup.class;
    }

    /** Whether the compiler is up-to-date.
     * Could use make -q but why waste time? If it is up to date, make
     * will not rebuild it anyway, we do not need to care.
     * @return always <CODE>false</CODE>
     */
    public boolean isUpToDate () {
        return false;
    }

    /** One key per compiler.
     */
    private Object key = new Object ();
    /** Get the grouping key.
     * In this case, constant per compiler,
     * since these should never be grouped together.
     * @return the constant key
     */
    public Object compilerGroupKey () {
        // Note: do not use 'this' as that causes a cycle in equals()
        return key;
    }

    /** Test for compiler equality.
     * @param o compiler to test against
     * @return <CODE>true</CODE> if equivalent
     */
    public boolean equals (Object o) {
        if (! (o instanceof MakefileCompiler)) return false;
        if (! super.equals (o)) return false;
        return target.equals (((MakefileCompiler) o).target);
    }

    /** Get the compiler hash code.
     * @return a computed hash
     */
    public int hashCode () {
        return super.hashCode () ^ target.hashCode ();
    }

}
