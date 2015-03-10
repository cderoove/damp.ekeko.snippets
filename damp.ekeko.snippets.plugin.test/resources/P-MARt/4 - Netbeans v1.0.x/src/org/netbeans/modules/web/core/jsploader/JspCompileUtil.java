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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.text.MessageFormat;
import javax.servlet.ServletContext;

import com.sun.jsp.Constants;
import com.sun.jsp.JspException;
import com.sun.jsp.compiler.Main;
import com.sun.jsp.compiler.EscapeUnicodeWriter;
import com.sun.jsp.compiler.Jsp1_0ParseEventListener;
import com.sun.jsp.compiler.JspReader;
import com.sun.jsp.compiler.ServletWriter;
import com.sun.jsp.compiler.Parser;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.compiler.Compiler;
import org.openide.execution.NbClassPath;
import org.openide.util.NbBundle;

/** JSP compilation utilities
*
* @author Petr Jiricka
*/
public class JspCompileUtil {

    /** Generates a servlet from a JSP page in this thread.
     * @param fo JSP file to compile
     * @outputdir directory for the resulting servlet
     * @classfiledata classfiledata for the page
     * @isErrorPage <code>true</code> if this is an error page
     */ 
    public static final void generate(JspReader reader, FileObject fo, String outputDir, Main.ClassFileData classfiledata, boolean isErrorPage)
    throws FileStateInvalidException, IOException, JspException {
        generate(reader, TopManager.getDefault().currentClassLoader(),
                 new ServletContextImpl(fo.getFileSystem()),
                 fo, classfiledata, outputDir, isErrorPage,
                 getFileObjectFileName(getContextRoot(fo)));
    }

    public static void invalidateJspInRunningJasper(JspDataObject jspdo) {
        // pending
    }

    /** Finds a fileobject for an absolute file name or null if not found */
    public static final FileObject findFileObjectForFile(String fileName) {
        String canName = ""; // NOI18N
        String canName2 = ""; // NOI18N
        try {
            canName = new File(fileName).getCanonicalPath();
        }
        catch (IOException e) {
            return null;
        }
        String resFriendly = canName.replace(File.separatorChar, '/');
        Repository rep = TopManager.getDefault().getRepository();
        int sepFound = -1;
        while (true) {
            sepFound = resFriendly.indexOf('/', sepFound + 1);
            if (sepFound == -1)
                return null;
            String resName = resFriendly.substring(sepFound);
            FileObject fo = rep.findResource(resName);
            if (fo == null) continue;
            File ff = NbClassPath.toFile(fo);
            if (ff == null) continue;
            // verify that they're the same
            try {
                canName2 = ff.getCanonicalPath();
            }
            catch (IOException e) {
                continue;
            }
            if (!(canName2.equals(canName)))
                continue;
            return fo;
        }
    }

    /** Returns true if the dir exists when we finish */
    private static boolean myMkdirs(File f) {
        if (f.exists()) return true;
        if (!f.isAbsolute())
            f = f.getAbsoluteFile();
        String par = f.getParent();
        if (par == null) return false;
        if (!myMkdirs(new File(par))) return false;
        f.mkdir();
        return f.exists();
    }

    public static String getClassNameSansNumberSansPackage(FileObject jspObj) throws FileStateInvalidException {
        String jspFile = getFileObjectFileName(jspObj);
        File jsp = new File(jspFile);
        String pkgName = Main.getPackageName(jsp);
        ServletContext context = new ServletContextImpl(jspObj.getFileSystem());
        String prefix = Main.getPrefix(jsp.getPath(), context.getRealPath(""));
        return prefix + Main.getBaseClassName(jsp) + "_jsp_";
    }


    public static final String getFileObjectFileName(FileObject fo) throws FileStateInvalidException {
        File ff = NbClassPath.toFile(fo);
        if (ff == null)
            throw new FileStateInvalidException(NbBundle.getBundle(JspCompileUtil.class).getString("CTL_NotLocalFile"));
        return ff.getAbsolutePath();
    }

    // @deprecated context root is always the filesystem root

    /** Gets the folder which is at the root of the context into which fo belongs
    */
    public static final FileObject getContextRoot(FileObject fo) throws FileStateInvalidException {
        // pending
        return fo.getFileSystem().getRoot();
    }

    /** Gets the folder which is the root output folder for the context into which fo belongs */
    public static final FileObject getContextOutputRoot(FileObject fo) throws IOException {
        File serverRoot = getOutputRootFolder();
        FileSystem fs = fo.getFileSystem();
        File contextRoot = new File(serverRoot, URLEncoder.encode(fs.getSystemName()));
        return getAsRootOfFileSystem(contextRoot);
    }


    /** Gets the root OUTPUT folder for all contexts in NB systm FileSystem. */
    private static File getOutputRootFolder() {
        String path = System.getProperty("netbeans.user");
        if (path == null || path.length() == 0) {
            FileObject sysRoot = TopManager.getDefault().getRepository().getDefaultFileSystem().getRoot();
            path = NbClassPath.toFile(sysRoot).getAbsoluteFile().getParent();
            if (path == null)
                throw new InternalError();
        }
        if (!path.endsWith(File.separator))
            path = path + File.separator;
        path = path + "temp" + File.separator + "jspwork"; // NOI18N
        File myRoot = (new File(path)).getAbsoluteFile();
        return myRoot;
    }

    /** Does the following:
    * <ul>
    * <li>creates a hidden LocalFileSystem (with compile, execute and debug capabilities)
    * with root in <code>intendedRoot</code>, if it does not exist yet</li>
    * <li>returns the root of this filesystem
    * </ul> */
    private static FileObject getAsRootOfFileSystem(File intendedRoot) {
        // try to find it among current filesystems
        for (Enumeration en = TopManager.getDefault().getRepository().getFileSystems(); en.hasMoreElements(); ) {
            FileSystem fs = (FileSystem)en.nextElement();
            File root = NbClassPath.toFile(fs.getRoot());
            if (root != null) {
                if (root.equals(intendedRoot))
                    return fs.getRoot();
            }
        }

        // does not exist in repository
        if (!intendedRoot.exists()) {
            boolean success = myMkdirs(intendedRoot);
        }

        FileSystemCapability.Bean cap = new FileSystemCapability.Bean();
        cap.setCompile(true);
        cap.setExecute(true);
        cap.setDebug(true);
        cap.setDoc(false);
        LocalFileSystem newFs = new LocalFileSystem(cap);
        try {
            newFs.setRootDirectory(intendedRoot);
        }
        catch (Exception e) {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                                                   MessageFormat.format(NbBundle.getBundle(JspCompileUtil.class).
                                                                        getString("EXC_JspFSNotCreated"),
                                                                        new Object[] {intendedRoot.getAbsolutePath()}), NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(message);
            return null;
        }
        newFs.setHidden(true);
        TopManager.getDefault().getRepository().addFileSystem(newFs);
        return newFs.getRoot();
    }

    /** Returns an absolute context URL (starting with '/') for a relative URL and base URL.
    *  @param relativeTo url to which the relative URL is related. Treated as directory iff
    *    ends with '/'
    *  @param url the relative URL by RFC 2396
    *  @exception IllegalArgumentException if url is not absolute and relativeTo 
    * can not be related to, or if url is intended to be a directory
    */
    public static String resolveRelativeURL(String relativeTo, String url) {
        String result;
        if (url.startsWith("/")) { // NOI18N
            result = "/"; // NOI18N
            url = url.substring(1);
        }
        else {
            // canonize relativeTo
            if (!relativeTo.startsWith("/")) // NOI18N
                throw new IllegalArgumentException();
            relativeTo = resolveRelativeURL(null, relativeTo);
            int lastSlash = relativeTo.lastIndexOf('/');
            if (lastSlash == -1)
                throw new IllegalArgumentException();
            result = relativeTo.substring(0, lastSlash + 1);
        }

        // now url does not start with '/' and result starts with '/' and ends with '/'
        StringTokenizer st = new StringTokenizer(url, "/", true); // NOI18N
        while(st.hasMoreTokens()) {
            String tok = st.nextToken();
            //System.out.println("token : \"" + tok + "\""); // NOI18N
            if (tok.equals("/")) { // NOI18N
                if (!result.endsWith("/")) // NOI18N
                    result = result + "/"; // NOI18N
            }
            else
                if (tok.equals("")) // NOI18N
                    ;  // do nohing
                else
                    if (tok.equals(".")) // NOI18N
                        ;  // do nohing
                    else
                        if (tok.equals("..")) { // NOI18N
                            String withoutSlash = result.substring(0, result.length() - 1);
                            int ls = withoutSlash.lastIndexOf("/"); // NOI18N
                            if (ls != -1)
                                result = withoutSlash.substring(0, ls + 1);
                        }
                        else {
                            // some file
                            result = result + tok;
                        }
            //System.out.println("result : " + result); // NOI18N
        }
        return result;
    }


    /** Returns a FileObject whose URL is relative to a given fileObject. Doesn't allow to backtrack any
    * higher in the hierarchy than <code>rootFile</code>. Returns <code>null</code> if fileobject not found.
    * @param relativeTo FileObject relative to which the URL shoud be resolved. May be null if url is absolute
    * @param url URL to resolve. May start with a '/', in such a case <code>relativeTo</code> param is ignored
    * @param rootFile root of the hierarchy. <br>
    * Pending: if <code>null</code>, the repository root should be the root.
    */
    public static FileObject resolveRelativeURL(FileObject relativeTo, String url, FileObject rootFile) {
        if (rootFile == null)  //pending
            throw new IllegalArgumentException();
        else {
            // find the origin
            FileObject origin;
            if (url.startsWith("/")) { // NOI18N
                origin = rootFile;
            }
            else {
                if (relativeTo == null)
                    return null;
                origin = relativeTo.isFolder() ? relativeTo : relativeTo.getParent();
            }

            // now process the URL
            StringTokenizer st = new StringTokenizer(url, "/"); // NOI18N
            while(st.hasMoreTokens()) {
                String tok = st.nextToken();
                if (tok.equals("")) // NOI18N
                    ;  // do nohing
                else
                    if (tok.equals(".")) // NOI18N
                        ;  // do nohing
                if (tok.equals("..")) { // NOI18N
                    if (!rootFile.equals(origin))
                        origin = origin.getParent();
                }
                else {
                    // some file
                    FileObject newFile = origin.getFileObject(tok);
                    if (newFile == null) {
                        int lastDot = tok.lastIndexOf('.');
                        if (lastDot == -1)
                            return null;
                        newFile = origin.getFileObject(tok.substring(0, lastDot), tok.substring(lastDot + 1));
                        if (newFile == null)
                            return null;
                        origin = newFile;
                    }
                }
            }
            return origin;
        }
    }

    /** Generates servlet source from a JSP.
    * @param reader reader form which to read the JSP
    * @param classloader  classloader for loading beans
    * @param context servletcontext
    * @param s file name of the source JSP
    * @param classfiledata classfiledata for the JSP
    * @param outputDir output directory
    * @param isErrorPage whether this is an error page
    * @param s2 don't know
    */
    public static final void generate(JspReader reader, ClassLoader classloader, ServletContext context,
                                      FileObject jspObj, Main.ClassFileData classfiledata, String outputDir, boolean isErrorPage, String s2)
    throws JspException, IOException {
        String s = getFileObjectFileName(jspObj);
        File file = new File(s);
        file.getPath();
        String s3 = Main.getPackageName(file);
        String s4 = getClassNameSansNumberSansPackage(jspObj) + classfiledata.getNumber(); // NOI18N
        String servletName = Main.getJavaFileName(s4, outputDir);
        File servletDir = new File(servletName).getAbsoluteFile().getParentFile();
        myMkdirs(servletDir);
        ServletWriter servletwriter = new ServletWriter(new PrintWriter(
                                          new EscapeUnicodeWriter(new FileOutputStream(servletName))));
        try {
            Jsp1_0ParseEventListener jsp1_0parseeventlistener = instantiateJsp1_0ParseEventListener(
                        context, classloader, reader, servletwriter, s, s4, s3, isErrorPage, s2, outputDir);
            Parser parser = new Parser(reader, jsp1_0parseeventlistener);
            jsp1_0parseeventlistener.beginPageProcessing();
            parser.parse();
            jsp1_0parseeventlistener.endPageProcessing();
        }
        finally {
            servletwriter.close();
        }
    }

    public static JspInfo analyzePage(JspReader reader, FileObject jsp)
    throws JspException, FileStateInvalidException {
        AnalyzerParseEventListener listener =
            new AnalyzerParseEventListener(reader, jsp.getPackageNameExt('/', '.'),
                                           new ServletContextImpl(jsp.getFileSystem()));
        Parser parser = new Parser(reader, listener);
        listener.beginPageProcessing();
        parser.parse();
        listener.endPageProcessing();
        return listener.getJspInfo();
    }


    /** Renames the resulting class.
    * @param jspFile JSP file object
    * @param classfiledata classfiledata for the JSP
    * @param outputDir output directory
    * @exception JspException if the file could not be renamed
    * @return true if the file to be renamed existed
    */
    public static final boolean renameClass(FileObject jspFile, Main.ClassFileData classfiledata, String outputDir)
    throws JspException, IOException {
        File file = new File(getFileObjectFileName(jspFile));
        file.getPath();
        String s4 = getClassNameSansNumberSansPackage(jspFile) + classfiledata.getNumber(); // NOI18N
        String s8 = outputDir + File.separatorChar;
        s8 = s8 + s4 + ".class"; // NOI18N
        File file2 = new File(s8);
        // check that the class file exists
        if (!file2.exists())
            return false;
        File file3 = new File(classfiledata.getClassFileName());
        if (file3.exists()) {
            file3.delete();
        }
        if (!file2.renameTo(file3)) {
            throw new JspException(java.text.MessageFormat.format(
                                       org.openide.util.NbBundle.getBundle(JspCompileUtil.class).getString("CTL_NotRenamed"),
                                       new Object[] {file2, file3}));
        }
        else
            return true;
    }

    /*public static final void compileSource(String s, Main.ClassFileData classfiledata, String outputDir, boolean keepGenerated)
    throws JspException, IOException {
      // pending - may be wrong
      File file = new File(s);
      file.getPath();
      String s3 = Main.getPackageName(file);
      String s4 = Main.getBaseClassName(file) + "_jsp_" + classfiledata.getNumber();
      String s5 = Main.getJavaFileName(s4, outputDir);
      ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(256);
      sun.tools.javac.Main main1 = new sun.tools.javac.Main(bytearrayoutputstream, "javac");
      String s6 = System.getProperty("path.separator");
      String as[] = {
        s5, "-classpath", System.getProperty("java.class.path") + s6 + System.getProperty("jsp.class.path", ".") + s6 + outputDir, "-d", outputDir
      };
      boolean flag2 = main1.compile(as);
      if(!keepGenerated) {
        File file1 = new File(s5);
        file1.delete();
      }
      if(!flag2) {
        String s7 = bytearrayoutputstream.toString();
        throw new JspException("Compilation failed:" + s7);
      }
      String s8 = outputDir + File.separatorChar;
      if (s3 != null && !s3.equals(""))
        s8 = s8 + s3.replace('.', File.separatorChar) + File.separatorChar;
      s8 = s8 + s4 + ".class";
      File file2 = new File(s8);
      File file3 = new File(classfiledata.getClassFileName());
      if (file3.exists())
        file3.delete();
      if (!file2.renameTo(file3))
        throw new JspException("Unable to rename class file " + file2 + " to " + file3);
      else
        return;
}*/

    private static Jsp1_0ParseEventListener instantiateJsp1_0ParseEventListener(
        ServletContext context, ClassLoader classloader, JspReader jspreader, ServletWriter servletwriter,
        String s, String s4, String s3, boolean isErrorPage, String s2, String outputDir) {

        try {
            Constructor evListConst = Jsp1_0ParseEventListener.class.getDeclaredConstructor(new Class[] {
                                          ServletContext.class, ClassLoader.class, JspReader.class, ServletWriter.class,
                                          String.class, String.class, String.class, Boolean.TYPE, String.class, String.class});
            evListConst.setAccessible(true);
            return (Jsp1_0ParseEventListener)evListConst.newInstance(new Object[] {
                        context, classloader, jspreader, servletwriter, s, s4, s3, new Boolean(isErrorPage), s2, outputDir});
        }
        catch (NoSuchMethodException e) {
            throw new InternalError();
        }
        catch (SecurityException e) {
            throw new InternalError();
        }
        catch (ClassCastException e) {
            throw new InternalError();
        }
        catch (IllegalAccessException e) {
            throw new InternalError();
        }
        catch (InvocationTargetException e) {
            throw new InternalError();
        }
        catch (InstantiationException e) {
            throw new InternalError();
        }
    }


    /** Clones a Main.ClassFileData object */
    public static Main.ClassFileData cloneClassFileData(Main.ClassFileData toClone) {
        return new Main.ClassFileData(toClone.isOutDated(), toClone.getClassFileName(), toClone.getClassName());
    }

    /*  public static final void compile(ClassLoader classloader, ServletContext context,
      String s, Main.ClassFileData classfiledata, String outputDir, boolean keepGenerated, boolean isErrorPage, String s2) 
      throws JspException, IOException {
        File file = new File(s);
        file.getPath();
        String s3 = Main.getPackageName(file);
        String s4 = Main.getBaseClassName(file) + "_jsp_" + classfiledata.getNumber();
        String s5 = Main.getJavaFileName(s4, outputDir);
        JspReader jspreader = JspReader.createJspReader(s);
        ServletWriter servletwriter = new ServletWriter(new PrintWriter(new EscapeUnicodeWriter(new FileOutputStream(s5))));
        Jsp1_0ParseEventListener jsp1_0parseeventlistener = new Jsp1_0ParseEventListener2(context, classloader, jspreader, servletwriter, s, s4, s3, isErrorPage, s2, outputDir);
        Parser parser = new Parser(jspreader, jsp1_0parseeventlistener);
        jsp1_0parseeventlistener.beginPageProcessing();
        parser.parse();
        jsp1_0parseeventlistener.endPageProcessing();
        servletwriter.close();
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(256);
        sun.tools.javac.Main main1 = new sun.tools.javac.Main(bytearrayoutputstream, "javac");
        String s6 = System.getProperty("path.separator");
        String as[] = {
          s5, "-classpath", System.getProperty("java.class.path") + s6 + System.getProperty("jsp.class.path", ".") + s6 + outputDir, "-d", outputDir
        };
        boolean flag2 = main1.compile(as);
        if(!keepGenerated) {
          File file1 = new File(s5);
          file1.delete();
        }
        if(!flag2) {
          String s7 = bytearrayoutputstream.toString();
          throw new JspException("Compilation failed:" + s7);
        }
        String s8 = outputDir + File.separatorChar;
        if (s3 != null && !s3.equals(""))
          s8 = s8 + s3.replace('.', File.separatorChar) + File.separatorChar;
        s8 = s8 + s4 + ".class";
        File file2 = new File(s8);
        File file3 = new File(classfiledata.getClassFileName());
        if (file3.exists())
          file3.delete();
        if (!file2.renameTo(file3))
          throw new JspException("Unable to rename class file " + file2 + " to " + file3);
        else
          return;
      }*/

}



/*
 * Log
 *  19   Gandalf   1.18        1/27/00  Petr Jiricka    Changes in generating 
 *       names of the servlet
 *  18   Gandalf   1.17        1/13/00  Petr Jiricka    More i18n
 *  17   Gandalf   1.16        1/12/00  Petr Jiricka    Fully I18n-ed
 *  16   Gandalf   1.15        1/12/00  Petr Jiricka    i18n phase 1
 *  15   Gandalf   1.14        1/10/00  Petr Jiricka    Significant compilation 
 *       change - prepare compilers for Java beforehand.
 *  14   Gandalf   1.13        1/7/00   Petr Jiricka    Safe File.mkdirs() used
 *  13   Gandalf   1.12        1/6/00   Petr Jiricka    Cleanup
 *  12   Gandalf   1.11        1/4/00   Petr Jiricka    
 *  11   Gandalf   1.10        1/3/00   Petr Jiricka    Always close the writer.
 *  10   Gandalf   1.9         12/28/99 Petr Jiricka    getContextRoot 
 *       un-deprecated
 *  9    Gandalf   1.8         12/20/99 Petr Jiricka    Checking in changes made
 *       in the U.S.
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/12/99 Petr Jiricka    Removed debug messages
 *  6    Gandalf   1.5         10/10/99 Petr Jiricka    Changes relaetd to 
 *       servlet package move
 *  5    Gandalf   1.4         10/10/99 Petr Jiricka    outputDir does not throw
 *       an exception
 *  4    Gandalf   1.3         9/29/99  Petr Jiricka    cloneClassFileData() 
 *       utility method
 *  3    Gandalf   1.2         9/27/99  Petr Jiricka    
 *  2    Gandalf   1.1         9/22/99  Petr Jiricka    Added File -> FileObject
 *       conversion
 *  1    Gandalf   1.0         9/22/99  Petr Jiricka    
 * $
 */
