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

import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collection;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.ObjectInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.FileNotFoundException;
import java.io.File;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;

import javax.servlet.ServletContext;

import org.openide.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.actions.OpenAction;
import org.openide.actions.ViewAction;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeListener;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.CompilerType;

import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.JavaCompilerType;

import com.sun.jsp.compiler.Main;
import com.sun.jsp.compiler.ClassName;
import com.sun.jsp.compiler.JspReader;
import com.sun.jsp.JspException;

/** Object that provides main functionality for internet data loader.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Petr Jiricka
*/
public final class JspDataObject extends MultiDataObject {

    //  public static final String PROP_SERVLET_DATAOBJECT = "servlet_do"; // NOI18N
    public static final String PROP_COMPILATION_FINISHED = "jsp_compilation_finished"; // NOI18N
    public static final String PROP_SERVLET_GENERATED = "jsp_servlet_generated"; // NOI18N
    public static final String EA_JSP_ERRORPAGE = "jsp_errorpage"; // NOI18N

    transient protected Main.ClassFileData classFileData = null;
    transient protected JspServletEditor servletEdit;
    transient protected JspServletDataObject servletDataObject;

    public JspDataObject (FileObject pf, final UniFileLoader l) throws DataObjectExistsException {
        super (pf, l);
        initialize();
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        return new JspNode (this);
    }

    /** Creates a compiler and adds it into the job: for beans on which this JSP depends,
    *  generating servlet for this JSP, for compiling this JSP
    *  into a servlet, for other JSPs used by this JSP by &lt;jsp:include&gt; and &lt;jsp:forward&gt;.
    */                                             
    protected void createCompiler(CompilerJob job, java.lang.Class type, /*Compiler.Depth depth,*/
                                  boolean individual) {

        // clean compiler, if any
        Compiler cleanCompiler = null;
        // check if we should clean first
        if (type == JspCompiler.CLEAN || type == JspCompiler.BUILD) {
            // construct clean compiler
            cleanCompiler = new CleanCompiler(this);
            if (type == JspCompiler.CLEAN) {
                job.add(cleanCompiler);
                return;
            }
        }

        // the real type
        Class jspType = individual ? JspCompiler.BUILD : type;
        if (jspType == JspCompiler.CLEAN)
            jspType = JspCompiler.BUILD;

        try {
            // save first
            SaveCookie sc = (SaveCookie)getCookie(SaveCookie.class);
            if (sc != null)
                sc.save();

            // create the compiler for this page
            JspCompiler jspCompiler = new JspCompiler(this, jspType);
            if (cleanCompiler != null)
                jspCompiler.dependsOn(cleanCompiler);

            // check if this compiler has already been added
            Collection compilers = job.compilers();
            for (Iterator it = compilers.iterator(); it.hasNext(); ) {
                Object other = it.next();
                if (jspCompiler.equals(other)) {
                    JspCompiler otherJspComp = (JspCompiler)other;
                    // make the cookies consistent
                    if (jspCompiler.getType () != otherJspComp.getType()) {
                        otherJspComp.setType(JspCompiler.BUILD);
                    }
                    // make the dependencies consistent - it's enough to depend on the cleancompiler
                    otherJspComp.dependsOn(jspCompiler.dependsOn());
                    return;
                }
            }

            // get the JspCompilationInfo
            JspReader reader = JspReader.createJspReader(JspCompileUtil.getFileObjectFileName(
                                   getPrimaryFile()));
            JspInfo jspInfo = JspCompileUtil.analyzePage(reader, getPrimaryFile());
            JspCompilationInfo compInfo = new JspCompilationInfo(jspInfo, getPrimaryFile());

            // acquire compilers for the beans
            DataObject beans[] = compInfo.getBeans();
            CompilerJob beansJob = new CompilerJob(Compiler.DEPTH_ZERO);
            for (int i = 0; i < beans.length; i++) {
                CompilerCookie c = (CompilerCookie)beans[i].getCookie(CompilerCookie.Compile.class);
                if (c != null) {
                    c.addToJob(beansJob, Compiler.DEPTH_ZERO);
                }
                else {
                }
            }
            // now refresh the folders
            CompilerJob refreshBeansJob = new CompilerJob(Compiler.DEPTH_ZERO);
            RefreshCompiler rc;
            for (int i = 0; i < beans.length; i++) {
                rc = new RefreshCompiler(beans[i].getPrimaryFile().getParent());
                rc.dependsOn(beansJob);
                refreshBeansJob.add(rc);
            }


            // add it, add dependencies, error page info
            jspCompiler.setErrorPage(compInfo.isErrorPage());
            jspCompiler.dependsOn (refreshBeansJob);
            job.add(jspCompiler);

            // add the compiler for the generated servlet
            JavaCompilerType.IndirectCompiler servletComp = getServletCompiler(CompilerCookie.Build.class);
            if (servletComp == null) {
                Compiler error = new ErrorCompiler(getPrimaryFile(), new Exception(
                                                       NbBundle.getBundle(JspDataObject.class).getString("CTL_BadCompilerType")), true);
                job.add(error);
            }
            else {
                // compile the servlet
                servletComp.dependsOn(jspCompiler);
                jspCompiler.servletCompiler = servletComp;
                job.add(servletComp);
                // rename the class
                Compiler ren = new RenameCompiler(jspCompiler);
                ren.dependsOn(servletComp);
                job.add(ren);
                // refresh the folder
                Compiler refr = new RefreshCompiler(JspCompileUtil.getContextOutputRoot(getPrimaryFile()));
                refr.dependsOn(ren);
                job.add(refr);
            }

            // add compilers for referenced pages - jsp:include and jsp:forward
            JspDataObject usedPages[] = compInfo.getReferencedPages();
            for (int i = 0; i < usedPages.length; i++) {
                JspDataObject jspdo = usedPages[i];
                jspdo.createCompiler(job, CompilerCookie.Compile.class, /*depth,*/ false);
            }

            // add compilers for error pages
            JspDataObject errorPage[] = compInfo.getErrorPage();
            for (int i = 0; i < errorPage.length; i++) {
                JspDataObject jspdo = errorPage[i];
                jspdo.createCompiler(job, CompilerCookie.Compile.class, /*depth,*/ false);
            }
        }
        catch (JspException e) {
            Compiler error = new ErrorCompiler(getPrimaryFile(), e, false);
            job.add(error);
        }
        catch (FileStateInvalidException e) {
            Compiler error = new ErrorCompiler(getPrimaryFile(), e, true);
            job.add(error);
        }
        catch (IOException e) {
            Compiler error = new ErrorCompiler(getPrimaryFile(), e, false);
            job.add(error);
        }
    }

    public boolean isUpToDate() {
        if (getServletDataObject() == null)
            return false;
        FileObject clazz = getCorrespondingClass(true);
        if (clazz == null)
            return false;
        if (clazz.lastModified().compareTo(getPrimaryFile().lastModified()) < 0)
            return false;
        return true;
    }

    /** Return the compiler for the resulting servlet.*/
    private JavaCompilerType.IndirectCompiler getServletCompiler(Class type) throws IOException {
        CompilerType ct = CompilerSupport.getCompilerType (getPrimaryEntry());
        if (ct == null) {
            JspCompilerSupport ccookie = (JspCompilerSupport)getCookie(JspCompilerSupport.Compile.class);
            if (ccookie == null)
                return null;
            ct = ccookie.defaultCompilerType();
        }

        if (ct instanceof JavaCompilerType) {
            JavaCompilerType.IndirectCompiler comp = ((JavaCompilerType)ct).prepareIndirectCompiler(type, null);
            return comp;
        }
        else {
            return null;
        }
    }

    /** Searches for the class file corresponding to this JSP.
    * Then checks whether the name of the actual class in the file matches this file's name.<br>
    * @return corresponding class file, if found, otherwise <code>null</code><br>*/
    public FileObject getCorrespondingClass(boolean checkClassName) {
        FileObject classFo = getClassFile();
        if (classFo == null || (!checkClassName))
            return classFo;
        // make sure that the actual class in the class file, which may be different from
        // the class file name, matches this servlet's name.
        try {
            String classFileName = JspCompileUtil.getFileObjectFileName(classFo);
            String className = ClassName.getClassName(classFileName);
            if (className.equals(getClassFileData().getClassName()))
                return classFo;
            else
                return null;
        }
        catch (FileStateInvalidException e) {
            return null;
        }
        catch (JspException e) {
            return null;
        }
    }

    private FileObject getClassFile() {
        try {
            FileSystem outputFs = JspCompileUtil.getContextOutputRoot(getPrimaryFile()).getFileSystem();
            if (getClassFileData() == null)
                return null;
            String cfn = getClassFileData().getClassFileName();
            int index = cfn.lastIndexOf(File.separatorChar);
            if (index != -1)
                cfn = cfn.substring(index + 1);
            FileObject cl = outputFs.findResource(cfn);
            return cl;
        }
        catch (FileStateInvalidException e) {
            return null;
        }
        catch (IOException e) {
            return null;
        }
    }


    public Main.ClassFileData getClassFileData() {
        return classFileData;
    }

    public JspServletDataObject getServletDataObject() {
        return servletDataObject;
    }

    void compilationFinished() {
        firePropertyChange(PROP_COMPILATION_FINISHED, null, null);
    }

    void servletGenerated() {
        firePropertyChange(PROP_SERVLET_GENERATED, null, null);
    }


    private void initialize() {
        addPropertyChangeListener(new PropertyChangeListener() {
                                      public void propertyChange(PropertyChangeEvent evt) {
                                          if (PROP_COMPILATION_FINISHED.equals(evt.getPropertyName()))
                                              updateClassFileData();
                                          if (PROP_SERVLET_GENERATED.equals(evt.getPropertyName()))
                                              servletChanged();
                                      }
                                  });
        updateClassFileData();
        servletChanged();
    }


    /** Updates ClassFileData */
    void updateClassFileData() {
        try {
            ServletContext context = new ServletContextImpl(getPrimaryFile().getFileSystem());
            String realPath = context.getRealPath(getPrimaryFile().getPackageNameExt('/','.'));
            if (realPath == null)
                realPath = getPrimaryFile().getPackageNameExt('/','.');

            // classfiledata
            classFileData = Main.getClassFileData(
                                realPath,
                                //JspCompileUtil.getFileObjectFileName(getPrimaryFile()),
                                JspCompileUtil.getFileObjectFileName(JspCompileUtil.getContextOutputRoot(getPrimaryFile())),
                                context.getRealPath("") // NOI18N
                                //JspCompileUtil.getFileObjectFileName(JspCompileUtil.getContextRoot(getPrimaryFile()))
                            );
        } catch (Exception e) {
            classFileData = null;
        }
    }


    /** Updates classFileData, servletDataObject, servletEdit */
    private void servletChanged() {

        // dataobject
        try {
            FileObject servletFileObject = updateServletFileObject();
            if (servletFileObject != null) {
                // pending - maybe this slows down the compilation
                servletFileObject.refresh(true);
                DataObject dObj= TopManager.getDefault().getLoaderPool().findDataObject(servletFileObject);
                if (dObj instanceof JspServletDataObject) {
                    servletDataObject = (JspServletDataObject)dObj;
                    servletDataObject.setSourceJspPage(this);
                }
            }
            else
                servletDataObject = null;
        }
        catch (IOException e) {
            servletDataObject = null;
        }

        // editor
        RequestProcessor.postRequest(new Runnable() {
                                         public void run() {
                                             if (servletEdit != null) {
                                                 if (servletDataObject == null) {
                                                     removeServletEdit();
                                                 }
                                                 else {
                                                     boolean openAgain =  servletEdit.isOpen();
                                                     removeServletEdit();
                                                     addServletEdit();
                                                     if (openAgain) servletEdit.open();
                                                 }
                                             }
                                             else {
                                                 if (servletDataObject != null) {
                                                     addServletEdit();
                                                 }
                                             }
                                         }
                                     });

    }

    private void addServletEdit() {
        if (servletEdit == null) {
            servletEdit = (JspServletEditor)servletDataObject.getCookie(EditorCookie.class);
            getCookieSet().add(new ServletOpenCookie() {
                                   public void open() {
                                       servletEdit.open();
                                   }
                               });
        }
    }

    private void removeServletEdit() {
        if (servletEdit != null) {
            servletEdit.close();
            servletEdit = null;
            Node.Cookie open = getCookieSet().getCookie(ServletOpenCookie.class);
            if (open != null)
                getCookieSet().remove(open);
        }
    }

    /** Gets the current fileobject of the servlet corresponding to this JSP or null if may not exist.
    * Note that the file still doesn't need to exist, even if it's not null. */
    private FileObject updateServletFileObject() throws IOException {
        if (classFileData == null)
            return null;
        File jspFile = new File(JspCompileUtil.getFileObjectFileName(getPrimaryFile()));
        jspFile.getPath();
        FileObject contextRoot = JspCompileUtil.getContextOutputRoot(getPrimaryFile());
        String pName = Main.getPackageName(jspFile);
        FileObject myFolder = contextRoot.getFileSystem().find(pName, null, null);
        if (myFolder == null)
            return null;
        myFolder.refresh();
        FileObject servletFo;
        String s4 = JspCompileUtil.getClassNameSansNumberSansPackage(getPrimaryFile()); // NOI18N

        // first try the higher (new) number
        Main.ClassFileData newClassFileData = JspCompileUtil.cloneClassFileData(classFileData);
        newClassFileData.incrementNumber();
        servletFo = myFolder.getFileObject(s4 + newClassFileData.getNumber(), "java"); // NOI18N
        if (servletFo != null) {
            return servletFo;
        }

        // then try the lower (old) number
        servletFo = myFolder.getFileObject(s4 + classFileData.getNumber(), "java"); // NOI18N
        return servletFo;
    }

    public interface ServletOpenCookie extends Node.Cookie {
        public void open();
    }

}



/*
 * Log
 *  30   Gandalf   1.29        2/4/00   Petr Jiricka    Fixed exception on 
 *       compilation for JSPs in jar files - bugs 5501, 5502  
 *  29   Gandalf   1.28        1/27/00  Petr Jiricka    Changes in generating 
 *       names of the servlet
 *  28   Gandalf   1.27        1/17/00  Petr Jiricka    Debug outputs removed
 *  27   Gandalf   1.26        1/17/00  Petr Jiricka    
 *  26   Gandalf   1.25        1/16/00  Petr Jiricka    Correct compilation 
 *       cookies.
 *  25   Gandalf   1.24        1/16/00  Petr Jiricka    Duplicate checks etc.
 *  24   Gandalf   1.23        1/15/00  Petr Jiricka    More fixes.
 *  23   Gandalf   1.22        1/15/00  Petr Jiricka    Ensuring correct 
 *       compiler implementation - hashCode and equals
 *  22   Gandalf   1.21        1/14/00  Petr Jiricka    Compilation fixes
 *  21   Gandalf   1.20        1/14/00  Petr Jiricka    Generated servlet 
 *       refreshed after it has changed.
 *  20   Gandalf   1.19        1/13/00  Petr Jiricka    More i18n
 *  19   Gandalf   1.18        1/12/00  Petr Jiricka    i18n phase 1
 *  18   Gandalf   1.17        1/10/00  Petr Jiricka    Significant compilation 
 *       change - prepare compilers for Java beforehand.
 *  17   Gandalf   1.16        1/7/00   Petr Jiricka    Bugfix - infinite loop 
 *       prevention for interdependent jsp:include-s
 *  16   Gandalf   1.15        1/6/00   Petr Jiricka    Cleanup
 *  15   Gandalf   1.14        1/4/00   Petr Jiricka    More bugfixes
 *  14   Gandalf   1.13        1/3/00   Petr Jiricka    Added method for 
 *       acquiring generated class
 *  13   Gandalf   1.12        12/29/99 Petr Jiricka    Added registration of 
 *       this page with the generated servlet
 *  12   Gandalf   1.11        12/29/99 Petr Jiricka    Various compilation 
 *       fixes
 *  11   Gandalf   1.10        12/28/99 Petr Jiricka    Compilation changes
 *  10   Gandalf   1.9         12/23/99 Jaroslav Tulach mergeInto deleted from 
 *       the CompilerJob.
 *  9    Gandalf   1.8         12/20/99 Petr Jiricka    Checking in changes made
 *       in the U.S.
 *  8    Gandalf   1.7         12/9/99  Petr Kuzel      Overseas changes due 
 *       Tomcat 1.0.1
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/12/99 Petr Jiricka    Removed debug messages
 *  5    Gandalf   1.4         10/10/99 Petr Jiricka    Compiler creation 
 *       changes
 *  4    Gandalf   1.3         10/4/99  Petr Jiricka    
 *  3    Gandalf   1.2         9/29/99  Petr Jiricka    Compilation fixes
 *  2    Gandalf   1.1         9/27/99  Petr Jiricka    Fixed creation of 
 *       ClassFileData
 *  1    Gandalf   1.0         9/22/99  Petr Jiricka    
 * $
 */
