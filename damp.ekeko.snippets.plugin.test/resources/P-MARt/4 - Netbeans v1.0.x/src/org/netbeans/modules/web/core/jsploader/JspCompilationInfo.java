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
import java.util.HashSet;

import com.sun.jsp.JspException;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

/** High-level information about a JSP file,
* used for compilation purposes. */
public class JspCompilationInfo {

    private FileObject jspFile;
    private ServletContextImpl context;

    private boolean isErrorPage;
    private DataObject[] includedFiles;
    private DataObject[] beans;
    private JspDataObject[] errorPage;
    private JspDataObject[] referencedPages;

    /** Creates a compilation info from a parser output.
    * Different error checking applies for different items.
    * If an error occurs, JspException is thrown.
    * <ul>
    * <li>getIncludedFiles() must convert succesfully all files, otherwise exception is thrown</li>
    * <li>getBeans() tries to convert beans, picks only java files, no exception is thrown</li>
    * <li>getErrorPage() tries to convert the error page, no exception is thrown</li>
    * <li>getReferencedPages() tries to convert the referenced pages, no exception is thrown</li>
    * </ul> */
    public JspCompilationInfo(JspInfo info, FileObject jspFile)
    throws JspException, FileStateInvalidException {
        this.jspFile = jspFile;
        context = new ServletContextImpl(jspFile.getFileSystem());

        // convert all data
        // error page
        isErrorPage = info.isErrorPage();
        Set dObjSet;

        // beans
        dObjSet = convertBeans(info.getBeans());
        beans = (DataObject[])dObjSet.toArray(new DataObject[dObjSet.size()]);

        // included files
        dObjSet = convertPages(info.getIncludedFiles(), DataFilter.ALL);
        includedFiles = (DataObject[])dObjSet.toArray(new DataObject[dObjSet.size()]);

        DataFilter jspFilter = new DataFilter() {
                                   public boolean acceptDataObject(DataObject obj) {
                                       return obj instanceof JspDataObject;
                                   }
                               };

        // error page
        dObjSet = convertPages(info.getErrorPage(), jspFilter);
        errorPage = (JspDataObject[])dObjSet.toArray(new JspDataObject[dObjSet.size()]);

        // referenced pages
        dObjSet = convertPages(info.getReferencedPages(), jspFilter);
        referencedPages = (JspDataObject[])dObjSet.toArray(new JspDataObject[dObjSet.size()]);
    }

    /** Assume that resource is a context-absolute URL
    * Returns null if the DataObject does not conform to the filter.
    */
    private DataObject resourceToDataObject(String resource, DataFilter filter) {
        FileObject fo = context.getResourceAsObject(resource);
        DataObject dObj = null;
        if (fo != null) {
            try {
                dObj = DataObject.find(fo);
                if ((filter != null) && (!filter.acceptDataObject(dObj)))
                    dObj = null;
            }
            catch (DataObjectNotFoundException e) {
                // do nothing
            }
        }
        return dObj;
    }

    /** Assume that clazz is a full class name
    * Returns null if neither *.java nor *.class was found.
    */
    private DataObject clazzToDataObject(String clazz) {
        String resource = clazz.replace('.', '/');
        DataObject dObj;
        dObj = resourceToDataObject(resource + ".java", DataFilter.ALL); // NOI18N
        if (dObj != null)
            return dObj;
        dObj = resourceToDataObject(resource + ".class", DataFilter.ALL); // NOI18N
        return dObj;
    }

    /** Converts URLs from urls[] to a set of DataObjects, applying the filter. */
    private Set convertPages(String urls[], DataFilter filter) {
        HashSet pages = new HashSet();
        for (int i = 0; i < urls.length; i++) {
            DataObject dObj = resourceToDataObject(urls[i], filter);
            if (dObj != null)
                pages.add(dObj);
        }
        return pages;
    }

    /** Converts class names from clazzes to a set of DataObjects. */
    private Set convertBeans(String clazzes[]) {
        HashSet pages = new HashSet();
        for (int i = 0; i < clazzes.length; i++) {
            DataObject dObj = clazzToDataObject(clazzes[i]);
            if (dObj != null)
                pages.add(dObj);
        }
        return pages;
    }

    public DataObject[] getIncludedFiles() {
        return includedFiles;
    }

    //  deferred until we have JSP 1.1
    /** TagLibaryInfo-s for used tag libraries. */
    //  public TagLibraryInfo[] getTagLibraries();

    public DataObject[] getBeans() {
        return beans;
    }

    /** File used as the error page,
    * resolved to absolute URL within the context. */
    public JspDataObject[] getErrorPage() {
        return errorPage;
    }

    /** Files referenced by include and forward actions,
    * resolved to absolute URL within the context. */
    public JspDataObject[] getReferencedPages() {
        return referencedPages;
    }

    /** Returns whether this page is an error page. */
    public boolean isErrorPage() {
        return isErrorPage;
    }

}