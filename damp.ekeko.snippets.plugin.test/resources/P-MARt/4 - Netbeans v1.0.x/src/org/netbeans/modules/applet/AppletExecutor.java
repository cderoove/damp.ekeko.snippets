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

package org.netbeans.modules.applet;

import java.io.IOException;
import java.io.File;
import java.text.MessageFormat;

import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.execution.ProcessExecutor;
import org.openide.execution.ExecInfo;
import org.openide.execution.ExecutorTask;

/** Executes a class externally (in a separate process). Provides
* basic implementation that allows to specify the process to 
* execute, its parameters and also to substitute the content of repositorypath,
* classpath, bootclasspath and librarypath. This is done by inner class Format.
* <P>
* The behaviour described here can be overriden by subclasses to use different
* format (extend the set of recognized tags), execute the 
* process with additional environment properties, etc.
*
* @author Petr Jiricka, Ales Novak, Jaroslav Tulach
*/
public class AppletExecutor extends ProcessExecutor {

    private String url;

    /** default descriptor to use */
    private static final NbProcessDescriptor DEFAULT_APPLET_DESCRIPTOR = new NbProcessDescriptor (
                // /usr/local/bin/appletviewer
                "{" + Format.TAG_JAVAHOME + "}" + File.separatorChar + ".." + File.separatorChar + // NOI18N
                "bin" + File.separatorChar + "appletviewer", // NOI18N
                // {URL}
                " {" + AppletFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(AppletExecutor.class).getString("MSG_AppletExecutorHint")
            );

    static final long serialVersionUID =5682139532418769413L;
    /** Create a new executor.
    * The default Java launcher associated with this VM's installation will be used,
    * and the user repository entries will be used for the class path.
    */
    public AppletExecutor() {
        super();
        setExternalExecutor(DEFAULT_APPLET_DESCRIPTOR);
    }


    /* Default human-presentable name of the executor.
    * In the default implementation, just the class name.
    * @return initial value of the human-presentable name
    */
    public String displayName() {
        return NbBundle.getBundle(AppletExecutor.class).getString("CTL_Exec_Name");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (AppletExecutor.class);
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
        if (url == null)
            throw new IOException(NbBundle.getBundle(AppletExecutor.class).getString("EXC_NoHtmlPage"));
        return getExternalExecutor ().exec(new AppletFormat(info, url));
    }


    /* Executes given class by creating new process in underlting operating system.
    * @param ctx used to write to the Output Window
    * @param info information about the class to be executed
    */
    public ExecutorTask execute(ExecInfo info) throws IOException {
        url = null;
        try {
            String applet = info.getClassName();
            FileObject fo = AppletSupport.class2File(applet);
            if (fo == null) {
                throw new java.io.FileNotFoundException(NbBundle.getBundle(AppletExecutor.class).getString("EXC_BadExecutor"));
            }
            url = AppletSupport.generateHtmlFileURL(fo).toString();
        } catch (HttpServerNotFoundException ex) {
            AppletSupport.reportNoHttpServer();
        }
        return super.execute(info);
    }

    /** Default format that can format tags related to execution. Currently this is only the URL.
    */
    public static class AppletFormat extends Format {
        /** Tag replaced with the URL */
        public static final String TAG_URL = "URL"; // NOI18N

        static final long serialVersionUID =4315554797414856261L;
        /** @param info exec info about class to execute
        * @param classPath to substitute instead of CLASSPATH
        * @param bootClassPath boot class path
        * @param repository repository path
        * @param library library path
        */
        public AppletFormat (ExecInfo info, String url) {
            super(info);
            java.util.Map map = getMap ();

            map.put (TAG_URL, url);
        }

    }

}

/*
 * Log
 *  21   Gandalf   1.20        1/15/00  Petr Jiricka    Bugfix 5087
 *  20   Gandalf   1.19        1/12/00  Petr Jiricka    i18n
 *  19   Gandalf   1.18        11/27/99 Patrik Knakal   
 *  18   Gandalf   1.17        11/24/99 Ales Novak      #4700
 *  17   Gandalf   1.16        11/10/99 Ales Novak      #4654
 *  16   Gandalf   1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        10/5/99  Petr Jiricka    Extends ProcessExecutor 
 *       rather than Executor
 *  14   Gandalf   1.13        10/1/99  Ales Novak      new model of execution
 *  13   Gandalf   1.12        9/30/99  Petr Jiricka    Added notification when 
 *       the executor can not be used
 *  12   Gandalf   1.11        9/24/99  Petr Jiricka    Added arguments hint.
 *  11   Gandalf   1.10        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  10   Gandalf   1.9         7/15/99  Petr Jiricka    
 *  9    Gandalf   1.8         7/12/99  Petr Jiricka    Type of "External 
 *       Viewer" property changed to NbProcessDescriptor
 *  8    Gandalf   1.7         7/2/99   Jesse Glick     More help IDs.
 *  7    Gandalf   1.6         6/16/99  Petr Jiricka    Executes in the external
 *       viewer specified by the executor
 *  6    Gandalf   1.5         6/9/99   Petr Jiricka    
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/17/99  Petr Jiricka    
 *  3    Gandalf   1.2         5/11/99  Petr Jiricka    
 *  2    Gandalf   1.1         4/16/99  Ales Novak      
 *  1    Gandalf   1.0         4/13/99  Ales Novak      
 * $
 */
