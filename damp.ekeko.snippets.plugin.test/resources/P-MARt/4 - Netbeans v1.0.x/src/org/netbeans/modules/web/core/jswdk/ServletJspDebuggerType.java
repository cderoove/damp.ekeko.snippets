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

package org.netbeans.modules.web.core.jswdk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;


import org.openide.debugger.DebuggerType;
import org.openide.debugger.DebuggerInfo;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.debugger.DebuggerException;
import org.openide.execution.ExecInfo;
import org.openide.execution.NbClassPath;
import org.openide.execution.NbProcessDescriptor;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.web.core.HttpServerNotFoundException;
import org.netbeans.modules.web.core.WebExecSupport;
import org.netbeans.modules.web.core.Util;

import org.netbeans.modules.debugger.delegator.DefaultDebuggerType;


/** Debugger type for Servlets and JSPs.
*
* @author Petr Jiricka
* @version 0.10 May 22, 1998
*/
public class ServletJspDebuggerType extends DefaultDebuggerType
    implements ServletParamsCookie, Serializable {

    public static final String PROP_PORT         = ServletJspExecutor.PROP_PORT;
    public static final String PROP_DOCUMENTROOT = ServletJspExecutor.PROP_DOCUMENTROOT;
    public static final String PROP_MIMETYPES    = ServletJspExecutor.PROP_MIMETYPES;
    public static final String PROP_WELCOMEFILES = ServletJspExecutor.PROP_WELCOMEFILES;
    public static final String PROP_INVOKER      = ServletJspExecutor.PROP_INVOKER;

    private int port = 8080;
    private FileSystem documentRoot;
    private Map mimeTypes;
    private String welcomeFiles = "index.jsp,index.html,index.htm"; // NOI18N
    private boolean invoker = true;

    static final long serialVersionUID =-2476033812725787162L;
    public ServletJspDebuggerType() {
        super();
        mimeTypes = EditServletParamsAction.getDefaultMimeMap();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        firePropertyChange(PROP_PORT, null, new Integer(port));
    }

    public FileSystem getDocumentRoot() {
        return documentRoot;
    }

    public void setDocumentRoot(FileSystem documentRoot) {
        this.documentRoot = documentRoot;
        firePropertyChange(PROP_DOCUMENTROOT, null, documentRoot);
    }

    public Map getMIMETypes() {
        return mimeTypes;
    }

    public void setMIMETypes(Map mimeTypes) {
        this.mimeTypes = mimeTypes;
        firePropertyChange(PROP_MIMETYPES, null, mimeTypes);
    }

    public String getWelcomeFiles() {
        return welcomeFiles;
    }

    public void setWelcomeFiles(String welcomeFiles) {
        this.welcomeFiles = welcomeFiles;
        firePropertyChange(PROP_WELCOMEFILES, null, welcomeFiles);
    }

    public boolean isInvoker() {
        return invoker;
    }

    public void setInvoker(boolean invoker) {
        this.invoker = invoker;
        firePropertyChange(PROP_INVOKER, null, new Boolean(invoker));
    }


    /** Gets the display name for this debugger type. */
    public String displayName() {
        return NbBundle.getBundle(ServletJspDebuggerType.class).getString("CTL_Debug_Name");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ServletJspDebuggerType.class);
    }

    /** Starts the debugger. */
    protected void startDebugger (
        String className,
        String[] arguments,
        String stopClassName,
        NbProcessDescriptor process,
        String classPath,
        String bootClassPath,
        String repositoryPath,
        String libraryPath,
        boolean classic,
        ExecInfo info,
        boolean stopOnMain
    ) throws DebuggerException {
        try {
            // find out the docRoot
            FileObject docRoot = EditServletParamsAction.resolveDocRoot(info, getDocumentRoot());

            // show in browser
            final URL url =
                (EditServletParamsAction.isServlet(info)) ?
                EditServletParamsAction.constructServletURL(EditServletParamsAction.findDataObject(info), getPort(), isInvoker()) :
                EditServletParamsAction.constructJspURL(EditServletParamsAction.findDataObject(info), getPort());

            // stop the previously executed instance
            ServletJspExecutor.killServerIfRunning();

            // start the web browser
            Thread browser = new Thread(new Runnable() {
                                            public void run() {
                                                WebExecSupport.waitAndShowInBrowser(url, 20000);
                                            }
                                        });
            browser.start();

            // generate the properties
            EditServletParamsAction.deployWebAppDescriptor(
                docRoot, getMIMETypes(), getWelcomeFiles(), isInvoker());

            // create debugger info
            super.startDebugger (
                ServletMain.class.getName(),
                new String[] {"" + getPort(), NbClassPath.toFile(docRoot).toURL().toString(), // NOI18N
                              EditServletParamsAction.getWorkDir(docRoot)}, // parameters to servlet main
                stopOnMain ? getStopClassName(info) : null,
                process,
                classPath,
                bootClassPath,
                repositoryPath,
                libraryPath,
                classic,
                info,
                stopOnMain
            );
        }
        catch (IOException e) {
            throw new DebuggerException(e.getMessage());
        }
    }

    /** Gets the class name to stop at when debugging a servlet/JSP. For JSPs it should be
    * the most recent class version generated by compiler.
    */
    private String getStopClassName(ExecInfo info) {
        if (EditServletParamsAction.isServlet(info)) {
            return info.getClassName();
        }
        else
            // pending - on which class a JSP should stop ?
            throw new IllegalArgumentException();
    }

    /** Notify the user that the debugger is not present. */
    /*  private void debuggerNotPresent() {
        NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(ServletJspDebuggerType.class).
          getString("EXC_NoDebugger"), NotifyDescriptor.ERROR_MESSAGE);
        TopManager.getDefault().notify(message);
      }*/

}

/*
 * Log
 *  9    Gandalf-post-FCS1.7.1.0     4/18/00  Jan Jancura     New "default" debugger 
 *       type
 *  8    Gandalf   1.7         1/12/00  Petr Jiricka    i18n phase 1
 *  7    Gandalf   1.6         1/3/00   Petr Jiricka    Changed RootFileObject 
 *       to RootFileSystem
 *  6    Gandalf   1.5         12/21/99 Petr Jiricka    Type of DocumentRoot 
 *       property changed to FileSystem
 *  5    Gandalf   1.4         12/20/99 Petr Jiricka    Checking in changes made
 *       in the U.S.
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/12/99 Petr Jiricka    Implements 
 *       ServletParamsCookie
 *  1    Gandalf   1.0         10/9/99  Petr Jiricka    
 * $
 */
