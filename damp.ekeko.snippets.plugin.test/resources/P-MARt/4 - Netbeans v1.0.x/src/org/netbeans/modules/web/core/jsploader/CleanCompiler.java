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
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerGroup;
import org.openide.compiler.ProgressEvent;
import org.openide.compiler.ErrorEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

import com.sun.jsp.compiler.Main;

import org.netbeans.modules.web.core.jswdk.ServletJspExecutor;

/**
*
* @author Petr Jiricka
*/
final class CleanCompiler extends Compiler {

    JspDataObject jspdo;

    /**
    * @param job a compiler job
    * @param fo file object to compile (represents .jsp source)
    */
    public CleanCompiler(JspDataObject jspdo) {
        super();
        this.jspdo = jspdo;
    }

    /** See {@link Compilable#equals(java.lang.Object)}
    */
    public boolean equals (Object other) {
        if (!(other instanceof CleanCompiler))
            return false;
        return (((CleanCompiler)other).jspdo == jspdo);
    }

    public int hashCode() {
        return jspdo.hashCode();
    }

    /** inherited */
    public boolean isUpToDate() {
        return false;
    }

    /** inherited */
    public Class compilerGroupClass() {
        return Group.class;
    }

    public Object compilerGroupKey() {
        return jspdo;
    }

    public String toString() {
        return "CleanCompiler for " + jspdo.getPrimaryFile().getPackageNameExt('/','.');
    }


    public static class Group extends CompilerGroup {

        private CleanCompiler comp;

        /** new CleanCompiler Group */
        public Group() {
            super();
        }

        /** inherited */
        public void add(Compiler compiler) {
            if (!(compiler instanceof CleanCompiler)) {
                throw new IllegalArgumentException();
            }
            if (comp != null)
                throw new IllegalArgumentException();
            comp = (CleanCompiler)compiler;
        }

        /** inherited */
        public boolean start() {
            try {
                // this should be done in another way, but I don't really have a choice
                ServletJspExecutor.forceRestart();

                deleteServlets(comp.jspdo);
                return true;
            }
            catch (IOException e) {
                fireErrorEvent(ErrorCompiler.Group.constructError(this,
                               e, comp.jspdo.getPrimaryFile(), false));
                return false;
            }
            finally {
                comp.jspdo.servletGenerated();
            }
        }

        /** Deletes associated servlet and class files for a JSP file. */
        private void deleteServlets(JspDataObject dobj) throws IOException {
            // seek and destroy the class object
            FileObject clazz = dobj.getCorrespondingClass(false);
            if (clazz != null) {
                try {
                    DataObject clazzDo = DataObject.find(clazz);
                    clazzDo.delete();
                    fireProgressEvent(new ProgressEvent(this, clazz, ProgressEvent.TASK_CLEANING));
                }
                catch (DataObjectNotFoundException e) {
                    throw e;
                }
                catch (IOException e) {
                    fireErrorEvent(ErrorCompiler.Group.constructError(this,
                                   e, clazz, true));
                }
            }

            // find class name for the servlets
            Main.ClassFileData cfd = dobj.getClassFileData();
            if (cfd == null) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    new Exception(NbBundle.getBundle(CleanCompiler.class).
                                  getString("EXC_BadClassFileData")).printStackTrace();
                }
                return;
            }
            String baseClassName = cfd.getClassNameSansNumber();
            String className, servletRes;
            FileSystem servletFs = JspCompileUtil.getContextOutputRoot(dobj.getPrimaryFile()).getFileSystem();
            int lastNumber = cfd.getNumber();
            int number;
            for (number = 1; number <= lastNumber; number++) {
                className = baseClassName + "_jsp_" + number; // NOI18N
                servletRes = className.replace('.', '/') + ".java"; // NOI18N
                FileObject servlet = servletFs.findResource(servletRes);
                if (servlet != null) {
                    try {
                        DataObject servletDo = DataObject.find(servlet);
                        servletDo.delete();
                        fireProgressEvent(new ProgressEvent(this, servlet, ProgressEvent.TASK_CLEANING));
                    }
                    catch (DataObjectNotFoundException e) {
                        throw e;
                    }
                    catch (IOException e) {
                        fireErrorEvent(ErrorCompiler.Group.constructError(this,
                                       e, servlet, true));
                    }
                }
            }
        }

    }


}


/*
 * Log
 */
