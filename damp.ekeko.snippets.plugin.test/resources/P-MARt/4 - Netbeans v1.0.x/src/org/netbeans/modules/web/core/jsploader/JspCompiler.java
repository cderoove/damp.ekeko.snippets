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
import org.openide.compiler.CompilerJob;
import org.openide.compiler.CompilerTask;
import org.openide.compiler.CompilerGroup;
import org.openide.compiler.CompilerType;
import org.openide.compiler.ErrorEvent;
import org.openide.compiler.ProgressEvent;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.SaveCookie;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.CompilerSupport;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.util.NbBundle;

import com.sun.jsp.JspException;
import com.sun.jsp.compiler.ParseException;
import com.sun.jsp.compiler.JspReader;
import com.sun.jsp.compiler.Main;

import org.netbeans.modules.java.JCompilerSupport;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.JavaCompilerType;

/** JSP compiler
*
* @author Petr Jiricka
*/
public class JspCompiler extends Compiler {

    /** copy of type */
    private Class type;
    protected final JspDataObject jspdo;
    protected boolean errorPage;
    private Main.ClassFileData cfd;
    JavaCompilerType.IndirectCompiler servletCompiler;
    boolean successful = false;


    /** Constant for compilation. */
    public static final Class COMPILE = CompilerCookie.Compile.class;
    /** Constant for building. */
    public static final Class BUILD = CompilerCookie.Build.class;
    /** Constant for clean. */
    public static final Class CLEAN = CompilerCookie.Clean.class;

    public JspCompiler(JspDataObject jspdo, Class type) {
        super();
        if (type == CLEAN)
            this.type = BUILD;
        else
            this.type  = type;
        if ((this.type != COMPILE) && (this.type != BUILD))
            throw new IllegalArgumentException();
        this.jspdo = jspdo;
    }

    public JspDataObject getDataObject() {
        return jspdo;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public void setErrorPage(boolean errorPage) {
        this.errorPage = errorPage;
    }

    public boolean isErrorPage() {
        return errorPage;
    }
    /**
     */
    public Class compilerGroupClass() {
        return Group.class;
    }

    /** Checks if the class corresponding to this JSP is up to date
     */
    public boolean isUpToDate() {
        if (type == BUILD) return false;
        return jspdo.isUpToDate();
    }

    /** See {@link Compilable#equals(java.lang.Object)}
    */
    public boolean equals (Object other) {
        if (!(other instanceof JspCompiler))
            return false;
        JspCompiler comp2 = (JspCompiler)other;
        boolean eq = (comp2.jspdo == jspdo); // && (comp2.errorPage == errorPage));
        return eq;
    }

    public int hashCode () {
        return ((jspdo == null) ? 0 : jspdo.hashCode());
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
        return jspdo;
    }

    void setClassFileData(Main.ClassFileData cfd) {
        this.cfd = cfd;
    }

    Main.ClassFileData getClassFileData() {
        return cfd;
    }

    public String toString() {
        String result = "JspCompiler: " + jspdo.getPrimaryFile().getPackageNameExt('/','.') + // NOI18N
                        ", errorPage: " + errorPage + " "; // NOI18N
        if (type == BUILD)
            result = result + "BUILD";
        if (type == COMPILE)
            result = result + "COMPILE";
        return result;
    }

    void setToServletCompiler(FileObject fo) {
        if (servletCompiler == null) return;
        servletCompiler.setResolved(fo);
    }

    /** Compiler group for servlet code generation. */
    public static class Group extends CompilerGroup {

        public Group() {
            super();
        }

        private JspCompiler comp;

        public void add(Compiler c) throws IllegalArgumentException {
            if (!(c instanceof JspCompiler))
                throw new IllegalArgumentException();
            if (comp != null)
                throw new IllegalArgumentException();
            comp = ((JspCompiler)c);
        }

        public boolean start() {
            try {

                // prepare the file and class names
                comp.getDataObject().updateClassFileData();
                Main.ClassFileData cfd = JspCompileUtil.cloneClassFileData(
                                             comp.getDataObject().getClassFileData());
                cfd.incrementNumber();
                comp.setClassFileData(cfd);

                String outputDir = getOutputDir(comp.getDataObject());
                JspReader reader = JspReader.createJspReader(JspCompileUtil.getFileObjectFileName(
                                       comp.getDataObject().getPrimaryFile()));

                try {
                    // generate the servlet source
                    fireProgressEvent(new ProgressEvent(this, comp.getDataObject().getPrimaryFile(),
                                                        ProgressEvent.TASK_GENERATING));
                    JspCompileUtil.generate(reader, comp.getDataObject().getPrimaryFile(),
                                            outputDir, cfd, comp.isErrorPage()
                                           );

                    comp.getDataObject().servletGenerated();
                    JspServletDataObject servlet = comp.getDataObject().getServletDataObject();
                    if (servlet == null) {
                        fireErrorEvent(new ErrorEvent(this, comp.getDataObject().getPrimaryFile(),
                                                      -1, -1, NbBundle.getBundle(JspCompiler.class).getString("CTL_ServletNotFound"), ""));
                        return false;
                    }

                    // compile the servlet
                    fireProgressEvent(new ProgressEvent(this, servlet.getPrimaryFile(),
                                                        ProgressEvent.TASK_GENERATING));
                    comp.setToServletCompiler(servlet.getPrimaryFile());
                    comp.successful = true;
                    return true;
                }
                catch (JspException e) {
                    // thrown in the first phase - delete the generated erroneous servlet
                    deleteErroneousServletCatch(cfd);
                    comp.getDataObject().servletGenerated();
                    fireErrorEvent(ErrorCompiler.Group.constructError(
                                       this, e, comp.getDataObject().getPrimaryFile(), false));
                    return false;
                }
                catch (NoClassDefFoundError e) {
                    // thrown in the first phase - delete the generated erroneous servlet
                    deleteErroneousServletCatch(cfd);
                    comp.getDataObject().servletGenerated();
                    fireErrorEvent(new ErrorEvent(this, comp.getDataObject().getPrimaryFile(),
                                                  -1, -1, e.getMessage(), ""));
                    return false;
                }
            }
            catch (ThreadDeath e) {
                throw e;
            }
            catch (Throwable e) {
                fireErrorEvent(ErrorCompiler.Group.constructError(
                                   this, e, comp.getDataObject().getPrimaryFile(), true));
                return false;
            }
        }

        /** Returns the actual output directory for servlets generated from a given JSP page
        *   (not a root of the hierarchy). */                           
        static String getOutputDir(DataObject jspdo) throws IOException {
            File jspFile = new File(JspCompileUtil.getFileObjectFileName(jspdo.getPrimaryFile()));
            String folderName = Main.getPackageName(jspFile).replace('.','/');
            FileObject fsRoot = JspCompileUtil.getContextOutputRoot(jspdo.getPrimaryFile());
            FileObject packageFolder = fsRoot.getFileSystem().find(folderName, null, null);
            if (packageFolder == null) {
                packageFolder = FileUtil.createFolder(fsRoot, folderName);
            }
            return JspCompileUtil.getFileObjectFileName(packageFolder);
        }


        private void deleteErroneousServletCatch(Main.ClassFileData cfd) {
            try {
                deleteErroneousServlet(cfd);
            }
            catch (IOException ioe) {
                // not a bad error
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ioe.printStackTrace();
            }
        }

        private void deleteErroneousServlet(Main.ClassFileData cfd) throws IOException {
            File jspFile = new File(JspCompileUtil.getFileObjectFileName(comp.getDataObject().getPrimaryFile()));
            jspFile.getPath();
            FileObject contextOutputRoot = JspCompileUtil.getContextOutputRoot(comp.getDataObject().getPrimaryFile());
            String pkg = Main.getPackageName(jspFile);
            FileObject fileDir = contextOutputRoot.getFileSystem().find(pkg, null, null);
            fileDir.refresh();
            FileObject servletFo;
            String almostFileName = JspCompileUtil.getClassNameSansNumberSansPackage(
                                        comp.getDataObject().getPrimaryFile());
            servletFo = fileDir.getFileObject(almostFileName + cfd.getNumber(), "java"); // NOI18N

            /*      File ff = NbClassPath.toFile(servletFo);
                  if (ff != null)
                    if (ff.exists()) {
                      if (!ff.delete())
                        throw new IOException();
                    }*/

            if (servletFo != null) {
                FileLock lock = servletFo.lock();
                try {
                    servletFo.delete(lock);
                }
                finally {
                    lock.releaseLock();
                }
            }
        }

    } // end of inner class Group

}

/*
 * Log
 *  32   Gandalf   1.31        1/27/00  Petr Jiricka    Changes in generating 
 *       names of the servlet
 *  31   Gandalf   1.30        1/17/00  Petr Jiricka    Debug outputs removed
 *  30   Gandalf   1.29        1/16/00  Petr Jiricka    Duplicate checks etc.
 *  29   Gandalf   1.28        1/15/00  Petr Jiricka    More fixes.
 *  28   Gandalf   1.27        1/15/00  Petr Jiricka    Ensuring correct 
 *       compiler implementation - hashCode and equals
 *  27   Gandalf   1.26        1/14/00  Petr Jiricka    Compilation fixes
 *  26   Gandalf   1.25        1/13/00  Petr Jiricka    Bugfix 5331
 *  25   Gandalf   1.24        1/12/00  Petr Jiricka    Fully I18n-ed
 *  24   Gandalf   1.23        1/12/00  Petr Jiricka    i18n phase 1
 *  23   Gandalf   1.22        1/10/00  Petr Jiricka    Significant compilation 
 *       change - prepare compilers for Java beforehand.
 *  22   Gandalf   1.21        1/7/00   Petr Jiricka    Cleanup
 *  21   Gandalf   1.20        1/7/00   Petr Jiricka    
 *  20   Gandalf   1.19        1/6/00   Petr Jiricka    Cleanup
 *  19   Gandalf   1.18        1/4/00   Petr Jiricka    Fixed minor bug
 *  18   Gandalf   1.17        1/4/00   Petr Jiricka    
 *  17   Gandalf   1.16        1/4/00   Petr Jiricka    More bugfixes
 *  16   Gandalf   1.15        1/3/00   Petr Jiricka    Changes in deleting the 
 *       servlet
 *  15   Gandalf   1.14        1/3/00   Petr Jiricka    Bugfixes
 *  14   Gandalf   1.13        12/29/99 Petr Jiricka    Various compilation 
 *       fixes
 *  13   Gandalf   1.12        12/28/99 Petr Jiricka    Reflectiong compilation 
 *       API changes
 *  12   Gandalf   1.11        12/20/99 Petr Jiricka    Checking in changes made
 *       in the U.S.
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         10/12/99 Petr Jiricka    Removed debug messages
 *  9    Gandalf   1.8         10/12/99 Petr Jiricka    defaultCompilerType 
 *       fixed
 *  8    Gandalf   1.7         10/12/99 Petr Jiricka    Fixed bug with default 
 *       compiler type
 *  7    Gandalf   1.6         10/10/99 Petr Jiricka    Servlet package change
 *  6    Gandalf   1.5         10/10/99 Petr Jiricka    Changed compilation 
 *       style EternalCompilerSettings -> CompilerType
 *  5    Gandalf   1.4         10/4/99  Petr Jiricka    
 *  4    Gandalf   1.3         9/29/99  Petr Jiricka    Compilation fixes - 
 *       consecutive names
 *  3    Gandalf   1.2         9/27/99  Petr Jiricka    Added compilation of the
 *       resulting servlet
 *  2    Gandalf   1.1         9/22/99  Petr Jiricka    Improved compilation 
 *       error reporting.
 *  1    Gandalf   1.0         9/22/99  Petr Jiricka    
 * $
 */
