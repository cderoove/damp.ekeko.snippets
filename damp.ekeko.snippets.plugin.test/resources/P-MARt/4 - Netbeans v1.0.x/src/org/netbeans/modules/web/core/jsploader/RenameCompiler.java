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

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import java.io.File;

import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerGroup;
import org.openide.compiler.ErrorEvent;
import org.openide.util.NbBundle;
import org.openide.loaders.DataObject;

import com.sun.jsp.compiler.Main;


/** Compiler which renames a class created by servlet->class compiler to a name which is
* understood by the JSP classloader.
*
* @author Petr Jiricka
*/
public class RenameCompiler extends Compiler {

    protected final JspCompiler jspComp;
    protected boolean upToDate;

    public RenameCompiler(JspCompiler jspComp) {
        super();
        this.jspComp = jspComp;
        this.upToDate = jspComp.isUpToDate();
    }

    public JspDataObject getDataObject() {
        return jspComp.getDataObject();
    }

    /**
     */
    public Class compilerGroupClass() {
        return Group.class;
    }

    /** Checks if the class corresponding to this JSP is up to date
     */
    public boolean isUpToDate() {
        return false;
    }

    /** See {@link Compilable#equals(java.lang.Object)}
    */
    public boolean equals (Object other) {
        if (!(other instanceof RenameCompiler))
            return false;
        RenameCompiler comp2 = (RenameCompiler)other;
        return (comp2.jspComp.equals(jspComp));
    }

    public int hashCode() {
        return jspComp.hashCode();
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
     *
     * @return key to define type of the compiler (file object representing root of filesystem) 
     *         or null if there are any errors
     * @see ExternalCompilerGroup#createProcess
     */
    public Object compilerGroupKey () {
        return jspComp;
    }

    public String toString() {
        return "RenameCompiler for " + jspComp.toString(); // NOI18N
    }

    /** Compiler group for servlet code generation. */
    public static class Group extends CompilerGroup {

        public Group() {
            super();
        }

        private RenameCompiler comp;

        public void add(Compiler c) throws IllegalArgumentException {
            if (!(c instanceof RenameCompiler))
                throw new IllegalArgumentException();
            if (comp != null)
                throw new IllegalArgumentException();
            comp = ((RenameCompiler)c);
        }

        public boolean start() {
            if (!comp.jspComp.successful)
                // do not rename anything, just succeed
                return true;
            try {
                String outputDir = JspCompiler.Group.getOutputDir(comp.getDataObject());
                Main.ClassFileData cfd = comp.jspComp.getClassFileData();
                boolean renamedOk = JspCompileUtil.renameClass(comp.getDataObject().getPrimaryFile(),
                                    cfd, outputDir);
                if (!renamedOk)
                    return true; // the class should not be renamed in the first place
                comp.getDataObject().compilationFinished();
                return true;
            }
            catch (Exception e) {
                fireErrorEvent(new ErrorEvent(this, comp.getDataObject().getPrimaryFile(),
                                              -1, -1, NbBundle.getBundle(RenameCompiler.class).getString("CTL_ClassNotRenamed"), ""));
                return false;
            }
        }

    } // end of inner class Group

}

/*
 * Log
 *  5    Gandalf   1.4         1/17/00  Petr Jiricka    Debug outputs removed
 *  4    Gandalf   1.3         1/15/00  Petr Jiricka    Ensuring correct 
 *       compiler implementation - hashCode and equals
 *  3    Gandalf   1.2         1/12/00  Petr Jiricka    Fully I18n-ed
 *  2    Gandalf   1.1         1/12/00  Petr Jiricka    i18n phase 1
 *  1    Gandalf   1.0         1/10/00  Petr Jiricka    
 * $
 */
