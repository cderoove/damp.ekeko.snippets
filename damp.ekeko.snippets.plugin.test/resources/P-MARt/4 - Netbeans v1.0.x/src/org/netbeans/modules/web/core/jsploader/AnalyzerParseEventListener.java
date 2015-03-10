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

import com.sun.jsp.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ArrayList;

import com.sun.jsp.compiler.ParseEventListener;
import com.sun.jsp.compiler.JspReader;
import com.sun.jsp.compiler.Mark;
import com.sun.jsp.compiler.ParseException;
import com.sun.jsp.compiler.JspUtil;
import com.sun.jsp.compiler.CycleCheck;
import com.sun.jsp.JspException;
import com.sun.jsp.Constants;

import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/** Analyzer for JSP files. Looks for beans, included files,
* include and forward directives, error pages. Included files 
* are processed recursively. */
public class AnalyzerParseEventListener implements ParseEventListener {

    protected JspReader reader;
    protected ServletContextImpl context;
    protected String pageContextURL;  // the JSP page URL within the context

    private String[] includedFiles;
    private String[] beans;
    private String[] errorPage;
    private String[] referencedPages;
    private boolean isErrorPage;

    private boolean iserrorpageDir;
    private boolean errorpageDir;

    /**
     * @associates String 
     */
    private ArrayList includedFileList;

    /**
     * @associates String 
     */
    private ArrayList beanList;

    /**
     * @associates String 
     */
    private ArrayList errorPageList;

    /**
     * @associates String 
     */
    private ArrayList referencedPageList;

    public AnalyzerParseEventListener(JspReader jspreader, String pageContextURL,
                                      ServletContextImpl servletContext) {
        reader = jspreader;
        context = servletContext;
        this.pageContextURL = pageContextURL;
        /*    int lastSlash = pageContextURL.lastIndexOf('/');
            if (lastSlash == -1)
              throw new IllegalArgumentException();
            pageDirURL = pageContextURL.substring(0, lastSlash + 1);  */
    }

    public void beginPageProcessing() throws JspException {
        includedFileList = new ArrayList();
        beanList = new ArrayList();
        errorPageList = new ArrayList();
        referencedPageList = new ArrayList();
        isErrorPage = false;
        // duplicate attribute guards
        iserrorpageDir = false;
        errorpageDir = false;
    }

    public void endPageProcessing() throws JspException {
        includedFiles = (String[])includedFileList.toArray(new String[includedFileList.size()]);
        beans = (String[])beanList.toArray(new String[beanList.size()]);
        errorPage = (String[])errorPageList.toArray(new String[errorPageList.size()]);
        referencedPages = (String[])referencedPageList.toArray(new String[referencedPageList.size()]);
    }

    public void handleComment(Mark start, Mark stop) throws JspException {
    }

    public void handleDirective(String directive, Mark start, Mark stop, Hashtable attrs)
    throws JspException {
        if (directive.equals("page")) { // NOI18N
            // process errorPage and isErrorPage
            Enumeration e = attrs.keys();
            String attr, attrValue;
            while (e.hasMoreElements()) {
                attr = (String) e.nextElement();
                attrValue = (String)attrs.get(attr);

                // isErrorPage
                if (attr.equals("isErrorPage")) { // NOI18N
                    if (iserrorpageDir)
                        throw new ParseException(start, Constants.getString("jsp.error.page.multiple.iserrorpage"));
                    iserrorpageDir = true;
                    if (attrValue == null)
                        throw new ParseException(start, Constants.getString("jsp.error.page.invalid.iserrorpage"));
                    if(attrValue.equalsIgnoreCase("true")) { // NOI18N
                        isErrorPage = true;
                    }
                    else
                        if (attrValue.equalsIgnoreCase("false")) { // NOI18N
                            isErrorPage = false;
                        }
                        else {
                            throw new ParseException(start, Constants.getString("jsp.error.page.invalid.iserrorpage"));
                        }
                } // end isErrorPage

                // errorPage
                if (attr.equals("errorPage")) { // NOI18N
                    if (errorpageDir)
                        throw new ParseException(start, Constants.getString("jsp.error.page.multiple.iserrorpage"));
                    errorpageDir = true;
                    if (attrValue != null) {
                        String absolute = JspCompileUtil.resolveRelativeURL("/" + pageContextURL, attrValue); // NOI18N
                        errorPageList.add(absolute);
                    }
                } // end errorPage


            }
        }



        if (directive.equals("taglib")) { // NOI18N
            /* not interesting for now
            String uri = (String) attrs.get("uri");
            String prefix = (String) attrs.get("prefix");
            try {
              CachedTagLibraryInfo tli = createTagLibInfo(uri, prefix);
              libraries.addTagLibrary(prefix, tli);
        } catch (Exception ex) {
              ex.printStackTrace();
              Object[] args = new Object[] { uri, ex.getMessage() };
              throw new JasperException(Constants.getString("jsp.error.badtaglib", args));
        }*/
        }

        /* For JAKARTA
        if (directive.equals("include")) {
          String file = (String) attrs.get("file");
          String encoding = (String) attrs.get("encoding");
         
          if (file == null)
            throw new ParseException(start, 
              NbBundle.getBundle(AnalyzerParseEventListener.class).
              getString("EXC_ParseNoFile"));
                
          // jsp.error.include.bad.file needs taking care of here??
          try {
            reader.pushFile(file, encoding);
          } catch (FileNotFoundException fnfe) {
            throw new ParseException(start, 
              NbBundle.getBundle(AnalyzerParseEventListener.class).
              getString("EXC_ParseFileNotFound"));
          }
    }*/

        // for JSWDK
        if (directive.equals("include")) { // NOI18N
            String file = (String) attrs.get("file"); // NOI18N
            String encoding = (String) attrs.get("encoding"); // NOI18N
            if (file == null)
                throw new ParseException(start,
                                         NbBundle.getBundle(AnalyzerParseEventListener.class).
                                         getString("EXC_ParseNoFile"));
            includedFileList.add(file);
            file = JspCompileUtil.resolveRelativeURL("/" + pageContextURL, file); // NOI18N
            if (file.charAt(0) == '/') {
                URL url = null;
                try {
                    url = context.getResource(file);
                    if (url == null)
                        throw new ParseException(start,
                                                 NbBundle.getBundle(AnalyzerParseEventListener.class).
                                                 getString("EXC_ParseFileNotFound"));

                    // find the corresponding DO and save it if it needs saving
                    FileObject fo = context.getResourceAsObject(file);
                    DataObject dobj = DataObject.find(fo);
                    SaveCookie sc = (SaveCookie) dobj.getCookie (SaveCookie.class);
                    if (sc != null)
                        sc.save();
                }
                catch(MalformedURLException exception) {
                    throw new ParseException(start,
                                             NbBundle.getBundle(AnalyzerParseEventListener.class).
                                             getString("EXC_ParseFileNotFound"));
                }
                catch(DataObjectNotFoundException exception) {
                    throw new ParseException(start,
                                             NbBundle.getBundle(AnalyzerParseEventListener.class).
                                             getString("EXC_ParseFileNotFound"));
                }
                catch(IOException exception) {
                    throw new ParseException(start,
                                             NbBundle.getBundle(AnalyzerParseEventListener.class).
                                             getString("EXC_ParseFileNotFound"));
                }
                if (!url.getProtocol().equalsIgnoreCase("file")) // NOI18N
                    throw new ParseException(start,
                                             NbBundle.getBundle(AnalyzerParseEventListener.class).
                                             getString("EXC_ParseBadFile"));
                file = url.getFile();
            }
            reader.pushFile(file, encoding);

            // cycle check
            if (CycleCheck.isReaderCycled(reader))
                throw new ParseException(start,
                                         NbBundle.getBundle(AnalyzerParseEventListener.class).
                                         getString("EXC_ParseCyclicInclude"));
        }

    }

    public void handleDeclaration(Mark mark, Mark mark1) throws JspException {
    }

    public void handleScriptlet(Mark mark, Mark mark1) throws JspException {
    }

    public void handleExpression(Mark mark, Mark mark1) throws JspException {
    }

    public void handleBean(Mark start, Mark stop, Hashtable attrs) throws JspException {
        if (attrs == null)
            throw new ParseException(start,
                                     NbBundle.getBundle(AnalyzerParseEventListener.class).
                                     getString("EXC_ClassTypeMissing"));
        String clazz = (String)attrs.get("class"); // NOI18N
        String type  = (String)attrs.get("type"); // NOI18N
        if(clazz == null && type == null) {
            throw new ParseException(start,
                                     NbBundle.getBundle(AnalyzerParseEventListener.class).
                                     getString("EXC_ClassTypeMissing"));
        }
        if (clazz == null)
            clazz = type;
        beanList.add(clazz);
    }

    public void handleBeanEnd(Mark mark, Mark mark1, Hashtable hashtable)
    throws JspException {
    }

    public void handleGetProperty(Mark mark, Mark mark1, Hashtable hashtable)
    throws JspException {
    }

    public void handleSetProperty(Mark mark, Mark mark1, Hashtable hashtable)
    throws JspException {
    }

    public void handlePlugin(Mark mark, Mark mark1, Hashtable hashtable, Hashtable hashtable1, String s)
    throws JspException {
    }

    public void handleCharData(char[] ac) throws JspException {
    }

    public void handleForward(Mark mark, Mark mark1, Hashtable attrs)
    throws JspException {
        handleIncludeForward(mark, mark1, attrs);
    }

    public void handleInclude(Mark mark, Mark mark1, Hashtable attrs)
    throws JspException {
        handleIncludeForward(mark, mark1, attrs);
    }

    private void handleIncludeForward(Mark start, Mark stop, Hashtable attrs)
    throws JspException {
        boolean isExpression = false;
        String page = (String)attrs.get("page"); // NOI18N
        if (page == null)
            throw new ParseException(start, Constants.getString("jsp.error.include.tag"));
        isExpression = JspUtil.isExpression(page);
        if (!isExpression) {
            page = JspCompileUtil.resolveRelativeURL("/" + pageContextURL, page); // NOI18N
            referencedPageList.add(page);
        }
        else {
            referencedPageList.add(page);
        }
    }

    /** Gets the JspInfo retrieved from the parsed text */
    public JspInfo getJspInfo() {
        return new JspInfo() {

                   public String[] getIncludedFiles() {
                       return includedFiles;
                   }

                   //  deferred until we have JSP 1.1
                   //  public TagLibraryInfo[] getTagLibraries();

                   public String[] getBeans() {
                       return beans;
                   }

                   public String[] getErrorPage() {
                       return errorPage;
                   }

                   public String[] getReferencedPages() {
                       return referencedPages;
                   }

                   public boolean isErrorPage() {
                       return isErrorPage;
                   }

               };
    }


}