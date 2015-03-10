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

package org.netbeans.modules.web.core;

import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.InetAddress;

import org.openide.cookies.ExecCookie;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.DebuggerCookie;
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
import org.openide.execution.ExecInfo;
import org.openide.execution.Executor;
import org.openide.execution.ProcessExecutor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.loaders.ExecSupport;
import org.openide.debugger.DebuggerException;
import org.openide.debugger.DebuggerType;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.ServiceType;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;

import org.netbeans.modules.web.core.jswdk.ServletJspExecutor;

/** Implementation of ExecCookie for internet objects
*
* @author Petr Jiricka
*/
public class WebExecSupport extends Object
    implements ExecCookie/*, DebuggerCookie, QueryStringCookie */{

    /** constant for class extension */
    private static final String CLASS_EXT = "class"; // NOI18N
    /** constant for java extension */
    private static final String JAVA_EXT = "class"; // NOI18N

    /** entry to be associated with */
    private MultiDataObject.Entry entry;

    public static final String EA_REQPARAMS = "NetBeansAttrReqParams"; // NOI18N

    public WebExecSupport(MultiDataObject.Entry entry) throws DataObjectExistsException {
        this.entry = entry;
    }

    /** This method allows subclasses to override the default
    * debugger type they want to use for debugging.
    *
    * @return current implementation returns DebuggerType.getDefault ()
    */
    public DebuggerType defaultDebuggerType () {
        return DebuggerType.getDefault ();
    }

    /** This method allows subclasses to override the default
    * executor they want to use for debugging.
    *
    * @return current implementation returns Executor.getDefault ()
    */
    public Executor defaultExecutor() {
        for (Enumeration en = Executor.executors(); en.hasMoreElements(); ) {
            Object exec = en.nextElement();
            if (exec instanceof ServletJspExecutor)
                return (Executor)exec;
        }
        return Executor.getDefault ();
    }

    /** only needs to be here because of a compiler bug */
    protected boolean startFailed (IOException ex) {
        Executor e = (Executor)choose (ExecSupport.getExecutor (entry), Executor.class, ex);
        if (e == null) {
            return false;
        } else {
            try {
                ExecSupport.setExecutor (entry, e);
                return true;
            } catch (IOException exc) {
                return false;
            }
        }
    }

    /** only needs to be here because of a compiler bug */
    protected boolean debugFailed (DebuggerException ex) {
        DebuggerType e = (DebuggerType)choose (ExecSupport.getDebuggerType (entry), DebuggerType.class, ex);
        if (e == null) {
            return false;
        } else {
            try {
                ExecSupport.setDebuggerType (entry, e);
                return true;
            } catch (IOException exc) {
                return false;
            }
        }
    }

    /** Opens dialog and asks for different executor or debugger.
    * @param current current value
    * @param clazz the class to use to locate property editor
    * @param ex the exception that caused the problem
    * @return the new value or null if choosing failed
    */
    private static ServiceType choose (ServiceType current, Class clazz, final Exception ex) {
        try {
            java.lang.reflect.Method chooser = ExecSupport.class.getDeclaredMethod("choose", new Class[]
                                               {ServiceType.class, Class.class, Exception.class});
            chooser.setAccessible(true);
            return (ServiceType)chooser.invoke(null, new Object[] {current, clazz, ex});
        }
        catch (NoSuchMethodException e) {
            return current;
        }
        catch (IllegalAccessException e) {
            return current;
        }
        catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException)
                throw (RuntimeException)e.getTargetException();
            return current;
        }
    }

    /* Starts the class.
    */
    public void start () {
        new Thread() {
            public void run() {
                // save the object first
                DataObject execDataObj = entry.getDataObject();
                SaveCookie sc = (SaveCookie)entry.getDataObject().getCookie(SaveCookie.class);
                if (sc != null)
                    try {
                        sc.save();
                    }
                    catch (IOException e) {}
                // now do what we should do
                Executor exec = ExecSupport.getExecutor (entry);
                if (exec == null) {
                    exec = defaultExecutor ();
                }

                try {
                    exec.execute(new WebExecInfo(entry.getFile().getPackageName ('.'), execDataObj)).result();
                } catch (final IOException ex) {
                    Mutex.EVENT.readAccess (new Runnable () {
                                                public void run () {
                                                    if (startFailed (ex)) {
                                                        // restart
                                                        WebExecSupport.this.start ();
                                                    }
                                                }
                                            });
                }
            }
        }.start();
    }

    /* Start debugging of associated object.
    * @param stopOnMain if <code>true</code>, debugger stops on the first line of debugged code
    * @exception DebuggerException if the session cannot be started
    */
    public void debug (final boolean stopOnMain) throws DebuggerException {
        // String[] params = getArguments (); - not needed
        DebuggerType t = ExecSupport.getDebuggerType (entry);
        if (t == null) {
            t = defaultDebuggerType ();
        }

        DataObject execDataObj = entry.getDataObject();
        SaveCookie sc = (SaveCookie)entry.getDataObject().getCookie(SaveCookie.class);
        if (sc != null)
            try {
                sc.save();
            }
            catch (IOException e) {}

        try {
            ExecInfo ei = new WebExecInfo (entry.getFile ().getPackageName ('.'), execDataObj);
            t.startDebugger (ei, stopOnMain);
            // ok, debugger started
            return;
        } catch (final DebuggerException ex) {
            try {
                Mutex.EVENT.readAccess (new Mutex.ExceptionAction () {
                                            public Object run () throws DebuggerException {
                                                if (debugFailed (ex)) {
                                                    // restart
                                                    debug (stopOnMain);
                                                }
                                                return null;
                                            }
                                        });
            } catch (org.openide.util.MutexException mx) {
                throw (DebuggerException)mx.getException ();
            }
        }
    }

    /** Helper method that creates default properties for execution of
    * a given support.
    * Includes properties to set the executor; debugger; and arguments.
    *
    * @param set sheet set to add properties to
    */
    public void addProperties (Sheet.Set set) {
        set.put(createExecutorProperty ());
    }

    /** Creates the executor property for entry.
    * @return the property
    */
    private PropertySupport createExecutorProperty () {
        return new PropertySupport.ReadWrite (
                   ExecSupport.PROP_EXECUTION,
                   Executor.class,
                   NbBundle.getBundle(ExecSupport.class).getString("PROP_execution"),
                   NbBundle.getBundle(ExecSupport.class).getString("HINT_execution")
               ) {
                   public Object getValue() {
                       Executor e = ExecSupport.getExecutor (entry);
                       if (e == null)
                           return defaultExecutor ();
                       else
                           return e;
                   }
                   public void setValue (Object val) throws java.lang.reflect.InvocationTargetException {
                       try {
                           ExecSupport.setExecutor(entry, (Executor) val);
                       } catch (IOException ex) {
                           throw new java.lang.reflect.InvocationTargetException (ex);
                       }
                   }
                   public boolean supportsDefaultValue () {
                       return true;
                   }

                   public void restoreDefaultValue () throws java.lang.reflect.InvocationTargetException {
                       setValue (null);
                   }
               };
    }


    public static File getLocalFile(FileObject fo) throws FileStateInvalidException {
        FileSystem fs = fo.getFileSystem();
        if (fs instanceof LocalFileSystem) {
            File root = ((LocalFileSystem)fs).getRootDirectory();
            String ap = root.getAbsolutePath();
            if (!ap.endsWith(File.separator))
                ap += File.separator;
            String relPath = fo.getPackageNameExt(File.separatorChar, '.');
            return new File(ap + relPath);
        }
        else
            return null;
    }

    public static boolean waitAndShowInBrowser(URL url, int timeout) {
        URL connectURL;
        try {
            connectURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/SOMENONSENSE_NONEXISTING_URL/xxxyyyzzz.nonexistingextension"); // NOI18N
        }
        catch (MalformedURLException e) {
            connectURL = url;
        }
        if (Util.getUtil().waitForURLConnection(connectURL, timeout, 1000)) {
            WebExecSupport.showInBrowser(url);
            return true;
        }
        else {
            //PENDING
            //TopManager.getDefault().notifyException(new IOException("Could not connect to the server")); // NOI18N
            return false;
        }
    }

    public static void showInBrowser(URL url) {
        ServletSettings set = ServletSettings.OPTIONS;
        if (set.getWebBrowser().equals(ServletSettings.INTERNAL_BROWSER))
            TopManager.getDefault().showUrl(url);
        else
            showInExternal(url);
    }

    private static void showInExternal(URL url) {
        ServletSettings set = ServletSettings.OPTIONS;

        NbProcessDescriptor proc = set.getExternalBrowser();
        try {
            proc.exec(new BrowserFormat(new ExecInfo(""), url.toString())); // NOI18N
        } catch (IOException ex) {
            TopManager.getDefault().notify(
                new NotifyDescriptor.Exception(ex,
                                               NbBundle.getBundle(WebExecSupport.class).getString("EXC_Invalid_Processor")
                                              )
            );
        }

    }


    /* Sets execution query string for the associated entry.
    * @param qStr the query string
    * @exception IOException if arguments cannot be set
    */
    public static void setQueryString(FileObject fo, String qStr) throws IOException {
        fo.setAttribute (EA_REQPARAMS, qStr);
    }

    /* Getter for query string associated with given file.
    * @return the query string or empty string if no quesy string associated
    */
    public static String getQueryString(FileObject fo) {
        try {
            String qStr = (String)fo.getAttribute (EA_REQPARAMS);
            if (qStr != null) {
                if ((qStr.length() > 0) && (!qStr.startsWith("?"))) // NOI18N
                    qStr = "?" + qStr; // NOI18N
                return qStr;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return ""; // NOI18N
    }

    /** Translates string name to a DataObject.
    * @param file
    * @return DataObject or null if not found 
    * @exception IOException if IO error occurs
    */
    public static DataObject classToDataObject(String className) throws IOException {
        String file = className.replace('.', '/');
        FileObject fo = TopManager.getDefault().getRepository().findResource(file + '.' + CLASS_EXT);
        if (fo == null) {
            /* class file may not be known to the filesystem yet if it has just been
            created by external compilation - in such a case look for the source */
            fo = TopManager.getDefault().getRepository().findResource(file + '.' + JAVA_EXT);
        }
        if (fo == null)
            return null;

        return TopManager.getDefault().getLoaderPool().findDataObject(fo);
    }


    /** Returns string for localhost */
    public static String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            return "localhost"; // NOI18N
        }
    }


    /** Default format that can format tags related to execution. Currently this is only the URL.
    */
    public static class BrowserFormat extends ProcessExecutor.Format {
        /** Tag replaced with the URL */

        static final long serialVersionUID =4315554797414856261L;
        /** @param info exec info about class to execute
        * @param classPath to substitute instead of CLASSPATH
        * @param bootClassPath boot class path
        * @param repository repository path
        * @param library library path
        */
        public BrowserFormat (ExecInfo info, String url) {
            super(info);
            java.util.Map map = getMap ();

            map.put (ServletSettings.TAG_URL, url);
        }

    }

}



/*
 * Log
 *  25   Gandalf   1.24        1/17/00  Petr Jiricka    WebExecSupport - related
 *       changes.
 *  24   Gandalf   1.23        1/16/00  Petr Jiricka    Cleanup
 *  23   Gandalf   1.22        1/16/00  Petr Jiricka    DebuggerCookie removed
 *  22   Gandalf   1.21        1/13/00  Petr Jiricka    More i18n
 *  21   Gandalf   1.20        1/12/00  Petr Jiricka    i18n phase 1
 *  20   Gandalf   1.19        1/6/00   Petr Jiricka    Cleanup
 *  19   Gandalf   1.18        1/4/00   Petr Jiricka    
 *  18   Gandalf   1.17        12/21/99 Petr Jiricka    Changes in executing the
 *       external browser
 *  17   Gandalf   1.16        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        10/9/99  Petr Jiricka    Changes in 
 *       waitAndShowInBrowser
 *  15   Gandalf   1.14        10/8/99  Petr Jiricka    Patches for compiler 
 *       errors
 *  14   Gandalf   1.13        10/8/99  Petr Jiricka    Debugging, fixes 
 *       executor + debugger choice after unsuccesful action
 *  13   Gandalf   1.12        10/8/99  Petr Jiricka    ClassName -> DataObject 
 *       - improvements
 *  12   Gandalf   1.11        10/4/99  Petr Jiricka    Reflecting execution API
 *       changes
 *  11   Gandalf   1.10        9/14/99  Petr Jiricka    Reflected change in 
 *       semantics of ExecSupport.getExecutor()
 *  10   Gandalf   1.9         9/10/99  Petr Jiricka    
 *  9    Gandalf   1.8         9/10/99  Jaroslav Tulach Changes to services.
 *  8    Gandalf   1.7         8/3/99   Petr Jiricka    Now uses new class 
 *       ServletSettings
 *  7    Gandalf   1.6         7/27/99  Petr Jiricka    
 *  6    Gandalf   1.5         7/24/99  Petr Jiricka    
 *  5    Gandalf   1.4         7/20/99  Petr Jiricka    
 *  4    Gandalf   1.3         7/20/99  Petr Jiricka    
 *  3    Gandalf   1.2         7/20/99  Petr Jiricka    Now uses executors
 *  2    Gandalf   1.1         7/16/99  Petr Jiricka    
 *  1    Gandalf   1.0         7/3/99   Petr Jiricka    
 * $
 */
