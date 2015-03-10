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

import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import javax.servlet.ServletContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.TopManager;

import com.sun.web.util.FilePathUtil;

/** Implementation of servlet context for use with standalone JSP compiler.
 *   Must be run internally.
 *   For Javadoc see javax.servlet.ServletContext.
 */

public class ServletContextImpl implements ServletContext {
    /**
     * @associates Object 
     */
    private Hashtable attributes = new Hashtable();
    private FileSystem baseFs;

    /** Constructs new implementation of ServletContext.
    *  @param fs filesystem for which to create the ServletContext. 
    *  If this is null ServletContext for the whole repository is created.
    */
    public ServletContextImpl(FileSystem fs) {
        attributes = new Hashtable();
        baseFs = fs;
    }

    /** Returns this context. */
    public ServletContext getContext(String uripath) {
        // pending : i might need other contexts for different schemes
        return this;
    }


    public int getMajorVersion() {
        return 2;
    }

    public int getMinorVersion() {
        return 1;
    }

    /** Implemented by FileUtil */
    public String getMimeType(String file) {
        // pending should look at some server config
        int i = file.lastIndexOf('.');
        if (i != -1)
            file = file.substring(i + 1);
        return FileUtil.getMIMEType(file);
    }

    /** Returns a nbfs: url */
    public URL getResource(String path) throws MalformedURLException {
        FileObject fo = getResourceAsObject(path);
        if (fo == null)
            return null;
        try {
            return getFileExternalURL(fo);
        }
        catch (FileStateInvalidException ex) {
            throw new MalformedURLException(ex.getClass().getName() + " : " + ex.toString()); // NOI18N
        }
    }

    /** Returns inputstream of a FileObject. */
    public InputStream getResourceAsStream(String path) {
        FileObject fo = getResourceAsObject(path);
        if (fo == null)
            return null;
        try {
            return fo.getInputStream();
        }
        catch (FileNotFoundException ex) {
            return null;
        }
    }

    /** Returns a file object for a given path, or <code>null</code> if not found. */
    public FileObject getResourceAsObject(String path) {
        if (baseFs == null)
            return TopManager.getDefault().getRepository().findResource(path);
        else
            return baseFs.findResource(path);
    }

    private URL getFileExternalURL(FileObject fo) throws FileStateInvalidException {
        File ff = NbClassPath.toFile(fo);
        if (ff != null)
            try {
                return ff.toURL();
            }
            catch (MalformedURLException e) {
                return fo.getURL();
            }
        return fo.getURL();
    }

    /** Not implemented, returns null */
    public RequestDispatcher getRequestDispatcher(String urlpath) {
        return null;
    }

    public Servlet getServlet(String name) throws ServletException {
        return null;
    }

    public Enumeration getServlets() {
        return new Enumeration() {

                   public boolean hasMoreElements() {
                       return false;
                   }

                   public Object nextElement() {
                       throw new NoSuchElementException();
                   }
               };
    }

    public Enumeration getServletNames() {
        return new Enumeration() {

                   public boolean hasMoreElements() {
                       return false;
                   }

                   public Object nextElement() {
                       throw new NoSuchElementException();
                   }
               };
    }

    /** Does nothing */
    public void log(String msg) {
    }

    /** Does nothing */
    public void log(Exception exception, String msg) {
    }

    /** Does nothing */
    public void log(String message, Throwable throwable) {
    }

    public String getRealPath(String path) {
        String realPath = null;
        try {
            URL url = getResource(path);
            if (url != null && url.getProtocol().equals("file")) // NOI18N
                realPath = FilePathUtil.patch(url.getFile());
        }
        catch(Exception ex) {
        }
        return realPath;
    }

    public String getServerInfo() {
        return org.openide.util.NbBundle.getBundle(ServletContextImpl.class).getString("CTL_ServerInfo");
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }


    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }
}



/*
 * Log
 *  9    Gandalf   1.8         1/12/00  Petr Jiricka    Fully I18n-ed
 *  8    Gandalf   1.7         1/12/00  Petr Jiricka    i18n phase 1
 *  7    Gandalf   1.6         1/4/00   Petr Jiricka    Bugfix - run JSP action 
 *       picks the same classes as compilation
 *  6    Gandalf   1.5         1/3/00   Petr Jiricka    Returns file: URL 
 *       instead of nbfs: ULR where possible
 *  5    Gandalf   1.4         12/20/99 Petr Jiricka    Checking in changes made
 *       in the U.S.
 *  4    Gandalf   1.3         11/30/99 Petr Jiricka    Initialize Hashtable of 
 *       attributes
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/12/99 Petr Jiricka    Removed debug messages
 *  1    Gandalf   1.0         9/22/99  Petr Jiricka    
 * $
 */
