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
import java.io.File;
import java.beans.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;
import java.text.MessageFormat;
import java.net.URL;
import java.net.MalformedURLException;

import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;
import org.openide.util.TaskListener;
import org.openide.util.Task;
import org.openide.loaders.DataObject;
import org.openide.execution.ProcessExecutor;
import org.openide.execution.NbClassPath;
import org.openide.execution.ExecInfo;
import org.openide.execution.ExecutorTask;

import org.netbeans.modules.web.core.WebExecSupport;

/** Executes a class externally (in a separate process). Provides
* basic implementation that allows to specify the process to 
* execute, its parameters and also to substitute the content of repositorypath,
* classpath, bootclasspath and librarypath. This is done by inner class Format.
* <P>
* The behaviour described here can be overriden by subclasses to use different
* format (extend the set of recognized tags), execute the 
* process with additional environment properties, etc.
* <p>
* By default the server is not started each time an object is executed, a running instance is used and the 
* object is reloaded to the server.
* However, the server is always restarted in these situations:
* <ul>
* <li>The user runs the object by "Execute (restart server)" action</li> // NOI18N
* <li>The object being run is a servlet</li>
* <li>The executor used for execution has changed since the last server start</li>
* <li>Any property of the executor has changed since the last server start</li>
* <li>The document root for the server is different from that of the running server instance</li>
* <li>When any JSP on the filesystem on which the instance is running is cleaned</li>
* </ul>
*
* @author Petr Jiricka, Ales Novak, Jaroslav Tulach
*/
public class ServletJspExecutor extends ProcessExecutor implements ServletParamsCookie {

    /** A pig wrote this code : serves to distinguish between "normal" run action
    *   and "restart server and run" action. */ // NOI18N
    private static boolean restartServerRunAction = false;

    public static final String PROP_PORT = "port"; // NOI18N
    public static final String PROP_DOCUMENTROOT = "documentRoot"; // NOI18N
    public static final String PROP_MIMETYPES = "MIMETypes"; // NOI18N
    public static final String PROP_WELCOMEFILES = "welcomeFiles"; // NOI18N
    public static final String PROP_INVOKER = "invoker"; // NOI18N

    /** DecRoot for the currently executing process. */
    private transient FileObject docRoot;

    private static ExecutorTask serverInstance;
    private static FileObject serverInstanceDocumentRoot;
    private static ServletJspExecutor serverInstanceExecutor;

    private static PropertyChangeListener pcl;

    public static void forceRestart() {
        //System.out.println("forcing restart");
        restartServerRunAction = true;
    }

    public static void killServerIfRunning() {
        if (serverInstance != null) {
            serverInstance.stop();
            serverInstance.getInputOutput().closeInputOutput();
            cleanServerInstance();
        }
    }

    private static void cleanServerInstance() {
        serverInstance = null;
        serverInstanceDocumentRoot = null;
        if ((serverInstanceExecutor != null) && (pcl != null))
            serverInstanceExecutor.removePropertyChangeListener(pcl);
        serverInstanceExecutor = null;
        pcl = null;
    }

    private int port = 8080;
    private FileSystem documentRoot;
    private Map mimeTypes;
    private String welcomeFiles = "index.jsp,index.html,index.htm"; // NOI18N
    private boolean invoker = true;

    static final long serialVersionUID =6058359069466864370L;

    /** Create a new executor.
    * The default Java launcher associated with this VM's installation will be used,
    * and the user repository entries will be used for the class path.
    */
    public ServletJspExecutor() {
        super();
        mimeTypes = EditServletParamsAction.getDefaultMimeMap();
        //setExternalExecutor(DEFAULT_APPLET_DESCRIPTOR);
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


    /* Default human-presentable name of the executor.
    * In the default implementation, just the class name.
    * @return initial value of the human-presentable name
    */
    public String displayName() {
        return NbBundle.getBundle(ServletJspExecutor.class).getString("CTL_Exec_Name");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ServletJspExecutor.class);
    }

    /** Called to create the java.lang.Process for given exec info.
    * Current implementation scans creates new Format with provided
    * exec info and asks the current executor to start with that
    * format.
    * <P>
    * Subclasses can override this to achive the right behaviour, add
    * system properties, own format, etc.
    * 
    * @param info exec info 
    * @return the executed process
    * @exception IOException if the action fails
    */
    protected Process createProcess (ExecInfo info) throws IOException {
        return getExternalExecutor ().exec (
                   new Format(new ExecInfo(ServletMain.class.getName(),
                                           new String[] {"" + getPort(), NbClassPath.toFile(docRoot).toURL().toString(), // NOI18N
                                                         EditServletParamsAction.getWorkDir(docRoot)}))
                   //new ServletJspFormat (getPort(), NbClassPath.toFile(docRoot).toURL().toString(), getWorkDir())
               );
    }


    /* Executes given class by creating new process in underlying operating system.
    * @param ctx used to write to the Output Window
    * @param info information about the class to be executed
    */
    public ExecutorTask execute(ExecInfo info) throws IOException {
        try {
            // find out the docRoot
            docRoot = EditServletParamsAction.resolveDocRoot(info, getDocumentRoot());

            // force restart if docRoot differs
            if (docRoot != serverInstanceDocumentRoot)
                forceRestart();

            // force restart if this is a servlet
            if (EditServletParamsAction.isServlet(info))
                forceRestart();

            // force restart if the executor has changed
            if (serverInstanceExecutor != this)
                forceRestart();

            // construct the URL for the browser
            final URL url =
                (EditServletParamsAction.isServlet(info)) ?
                EditServletParamsAction.constructServletURL(EditServletParamsAction.findDataObject(info), getPort(), isInvoker()) :
                EditServletParamsAction.constructJspURL(EditServletParamsAction.findDataObject(info), getPort());

            // stop the previously executed instance
            if (restartServerRunAction) {
                killServerIfRunning();
            }

            // start the web browser
            Thread browser = new Thread(new Runnable() {
                                            public void run() {
                                                if (!WebExecSupport.waitAndShowInBrowser(url, 10000)) {
                                                    // consider server not running
                                                    cleanServerInstance();
                                                }
                                            }
                                        });
            browser.start();

            //System.out.println("(re)starting server " + (serverInstance == null));
            if (serverInstance == null) {
                // generate the properties
                EditServletParamsAction.deployWebAppDescriptor(
                    docRoot, getMIMETypes(), getWelcomeFiles(), isInvoker());

                // execute the process
                serverInstance = super.execute(new ExecInfo(
                                                   java.text.MessageFormat.format(NbBundle.getBundle(ServletJspExecutor.class).
                                                                                  getString("CTL_Servlet_Process"), new Object[] {new Integer(getPort())})
                                               ));

                serverInstanceDocumentRoot = docRoot;
                serverInstanceExecutor = this;
                pcl = new PropertyChangeListener() {
                          public void propertyChange(PropertyChangeEvent evt) {
                              forceRestart();
                          }
                      };
                serverInstanceExecutor.addPropertyChangeListener(pcl);

                serverInstance.addTaskListener(new TaskListener() {
                                                   public void taskFinished(Task task){
                                                       if (task == serverInstance)
                                                           cleanServerInstance();
                                                   }
                                               });
            }
            return serverInstance;
        }
        catch (IOException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new IOException();
        }
        finally {
            restartServerRunAction = false;
        }
    }


}

/*
 * Log
 *  21   Gandalf   1.20        2/4/00   Petr Jiricka    Restart the engine if 
 *       the execution parameters change - fixes bugs 5561, 5515, 5581, 5291, 
 *       5587  
 *  20   Gandalf   1.19        1/18/00  Petr Jiricka    Bugfix 2941
 *  19   Gandalf   1.18        1/17/00  Petr Jiricka    Debug outputs removed
 *  18   Gandalf   1.17        1/16/00  Petr Jiricka    Cleanup
 *  17   Gandalf   1.16        1/12/00  Petr Jiricka    i18n phase 1
 *  16   Gandalf   1.15        1/4/00   Petr Jiricka    More safe handling of 
 *       running server process
 *  15   Gandalf   1.14        1/3/00   Petr Jiricka    Changed RootFileObject 
 *       to RootFileSystem
 *  14   Gandalf   1.13        12/29/99 Petr Jiricka    Various execution fixes
 *  13   Gandalf   1.12        12/21/99 Petr Jiricka    Type of DocumentRoot 
 *       property changed to FileSystem
 *  12   Gandalf   1.11        12/20/99 Petr Jiricka    Checking in changes made
 *       in the U.S.
 *  11   Gandalf   1.10        11/27/99 Patrik Knakal   
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/12/99 Petr Jiricka    Removed debug messages
 *  8    Gandalf   1.7         10/9/99  Petr Jiricka    Cleanup
 *  7    Gandalf   1.6         10/8/99  Petr Jiricka    Various fixes
 *  6    Gandalf   1.5         10/8/99  Petr Jiricka    Show in browser in a 
 *       separate thread
 *  5    Gandalf   1.4         10/8/99  Petr Jiricka    Showing in the web 
 *       browser
 *  4    Gandalf   1.3         10/7/99  Petr Jiricka    
 *  3    Gandalf   1.2         10/7/99  Petr Jiricka    
 *  2    Gandalf   1.1         10/7/99  Petr Jiricka    
 *  1    Gandalf   1.0         10/7/99  Petr Jiricka    
 * $
 */
