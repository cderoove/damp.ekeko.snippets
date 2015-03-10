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
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


import org.openide.debugger.DebuggerType;
import org.openide.debugger.DebuggerInfo;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.debugger.DebuggerException;
import org.openide.execution.ExecInfo;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

import org.netbeans.modules.debugger.delegator.DefaultDebuggerType;

/** Debug type for JavaDataObjects representing applets
*
* @author Petr Jiricka
* @version 0.10 May 22, 1998
*/
public class AppletDebuggerType extends DefaultDebuggerType implements Serializable {


    static final long serialVersionUID =-2850943063947022741L;

    /** The default debugger process and CLASSPATH */
    public static final NbProcessDescriptor DEFAULT_APPLET_DEBUGGER_PROCESS;

    private static final String DEBUGGING_POLICY_RESOURCE = "applet/appletDebugging.policy"; // NOI18N
    private static final String DEBUGGING_MAIN_CLASS = "sun.applet.AppletViewer"; // NOI18N

    static {
        // initialize DEFAULT_APPLET_DEBUGGER_PROCESS

        // helper variables
        String fileSeparator = System.getProperty ("file.separator");
        String pathSeparator = System.getProperty ("path.separator");
        String javaRoot = System.getProperty ("java.home") + fileSeparator;
        String netbeansHome = System.getProperty ("netbeans.home");
        /*String javaRoot1 = javaRoot;
        if (javaRoot.toLowerCase ().endsWith (fileSeparator + "jre" + fileSeparator)) {
          javaRoot1 = javaRoot.substring (0, javaRoot1.length () - 3 - fileSeparator.length ());
    }*/

        FileSystem defaultFs = TopManager.getDefault().getRepository().getDefaultFileSystem();
        FileObject policyFile = defaultFs.findResource(DEBUGGING_POLICY_RESOURCE);
        String securityFile;
        if (policyFile == null)
            securityFile = NbClassPath.toFile(defaultFs.getRoot()).getAbsolutePath() +
                           fileSeparator + DEBUGGING_POLICY_RESOURCE.replace('/', java.io.File.separatorChar);
        else
            securityFile = NbClassPath.toFile(policyFile).getAbsolutePath();

        DEFAULT_APPLET_DEBUGGER_PROCESS = new NbProcessDescriptor (
                                              "{java.home}{/}..{/}bin{/}java", // NOI18N
                                              "{" + CLASSIC_SWITCH + "}" + // NOI18N
                                              "{" + DEBUGGER_OPTIONS + "}" + // NOI18N
                                              " -Djava.security.policy=" + securityFile + // NOI18N
                                              " -Djava.compiler=NONE " + // NOI18N
                                              "{" + QUOTE_SWITCH + "}" + // NOI18N
                                              "{" + BOOT_CLASS_PATH_SWITCH_SWITCH + "}" + // NOI18N
                                              "{" + BOOT_CLASS_PATH_SWITCH + "}" + // NOI18N
                                              "{" + QUOTE_SWITCH + "}" + // NOI18N
                                              " -classpath " +
                                              "{" + QUOTE_SWITCH + "}" + // NOI18N
                                              "{" + REPOSITORY_SWITCH + "}" + // NOI18N
                                              "{" + LIBRARY_SWITCH + "}" + // NOI18N
                                              "{" + CLASS_PATH_SWITCH + "}" + // NOI18N
                                              "{" + QUOTE_SWITCH + "}" + // NOI18N
                                              " {" + MAIN_SWITCH + "}", // NOI18N
                                              NbBundle.getBundle (AppletDebuggerType.class).getString ("MSG_AppletDebuggerHint")
                                          );
    }

    private int serialVer = 1;

    public AppletDebuggerType() {
        super();
        setDebuggerProcess(DEFAULT_APPLET_DEBUGGER_PROCESS);
    }

    private void readObject (java.io.ObjectInputStream ois)
    throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        if (serialVer == 0) {
            setDebuggerProcess (DEFAULT_APPLET_DEBUGGER_PROCESS);
        }
    }

    /** Gets the display name for this debugger type. */
    public String displayName() {
        return NbBundle.getBundle(AppletDebuggerType.class).getString("CTL_Debug_Name");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (AppletDebuggerType.class);
    }

    /* Starts the debugger. */
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
        FileObject classFile = AppletSupport.class2File(info.getClassName());
        if (classFile == null) {
            throw new DebuggerException(NbBundle.getBundle(AppletDebuggerType.class).
                                        getString("EXC_BadDebuggerType"));
        }
        try {
            super.startDebugger (
                DEBUGGING_MAIN_CLASS,
                getParameters (classFile), // parameters to the appletviewer
                stopClassName,
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
        catch (HttpServerNotFoundException e) {
            throw new DebuggerException(NbBundle.getBundle(AppletDebuggerType.class).
                                        getString("EXC_NoHttpServer"));
        }
    }


    /**
    * @param fo is a FileObject for that parameters are to be constructed
    * @return parameters
    */
    private static String[] getParameters(FileObject fo) throws HttpServerNotFoundException, DebuggerException {
        URL url = AppletSupport.generateHtmlFileURL(fo);
        return new String[] {url.toString()};
    }

}

/*
 * Log
 *  25   Gandalf-post-FCS1.23.1.0    4/18/00  Jan Jancura     New "default" debugger 
 *       type
 *  24   Gandalf   1.23        1/18/00  Daniel Prusa    Arguments Key desription
 *  23   Gandalf   1.22        1/18/00  Daniel Prusa    {java.home} switch
 *  22   Gandalf   1.21        1/12/00  Petr Jiricka    i18n
 *  21   Gandalf   1.20        1/6/00   Daniel Prusa    Quote character switch 
 *       added
 *  20   Gandalf   1.19        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  19   Gandalf   1.18        10/9/99  Petr Jiricka    Removed setName method, 
 *       as super is public now.
 *  18   Gandalf   1.17        10/9/99  Petr Jiricka    Removed debug println()
 *  17   Gandalf   1.16        10/8/99  Petr Jiricka    Fixed applet debugging 
 *       in Java 1.3
 *  16   Gandalf   1.15        10/5/99  Petr Jiricka    Reflecting method move 
 *       from AppletExecutor to AppletSupport
 *  15   Gandalf   1.14        10/4/99  Petr Jiricka    Removed error 
 *       notifications, instead, an exception is thrown.
 *  14   Gandalf   1.13        9/27/99  Petr Jiricka    Fixed 
 *       NullPointerException #4026
 *  13   Gandalf   1.12        8/13/99  Petr Jiricka    Property name changed to
 *        read/write
 *  12   Gandalf   1.11        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  11   Gandalf   1.10        8/9/99   Petr Jiricka    Change of debugger API -
 *       dependence on debugger module
 *  10   Gandalf   1.9         8/2/99   Jan Jancura     Do not stop on Go 
 *       action.
 *  9    Gandalf   1.8         7/15/99  Petr Jiricka    
 *  8    Gandalf   1.7         7/2/99   Jesse Glick     More help IDs.
 *  7    Gandalf   1.6         6/28/99  Petr Jiricka    Modified to reflect 
 *       changes in debugger API
 *  6    Gandalf   1.5         6/25/99  Petr Jiricka    Removed debug prints
 *  5    Gandalf   1.4         6/10/99  Petr Jiricka    
 *  4    Gandalf   1.3         6/9/99   Petr Jiricka    
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/31/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/17/99  Petr Jiricka    
 * $
 */
