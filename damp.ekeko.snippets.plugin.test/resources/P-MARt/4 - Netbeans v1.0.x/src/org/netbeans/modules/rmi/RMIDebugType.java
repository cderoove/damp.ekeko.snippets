/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi;

import java.io.IOException;
import java.net.*;
import java.util.*;

import org.openide.*;
import org.openide.debugger.*;
import org.openide.execution.*;
import org.openide.util.*;

import org.netbeans.modules.debugger.delegator.*;

/**
 *
 * @author  mryzl
 */

public class RMIDebugType extends DefaultDebuggerType {

    /** Serial version UID. */
    static final long serialVersionUID = 9218416310627856112L;

    private int serialVer = 1;

    /** Creates new RMIDebugType. */
    public RMIDebugType() {
        init();
    }

    /** Init. */
    private void init() {
        setName(NbBundle.getBundle(RMIDebugType.class).getString("PROP_RMIDebugTypeName")); // NOI18N

        setDebuggerProcess (getDefaultProcess ());
    }

    private static NbProcessDescriptor getDefaultProcess () {
        return new NbProcessDescriptor(
                   "{java.home}{/}..{/}bin{/}java", // NOI18N
                   "{" + CLASSIC_SWITCH + "}" + // NOI18N
                   "{" + DEBUGGER_OPTIONS + "}" + // NOI18N
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
                   " -Djava.security.policy={" + RMIExecutorSettings.TAG_REPOSITORYIPURL + "}RMI/rmi.policy " + // NOI18N
                   "-Djava.rmi.server.codebase={" + RMIExecutorSettings.TAG_REPOSITORYIPURL + "} " + // NOI18N
                   "-Djava.rmi.server.hostname={" + RMIExecutorSettings.TAG_HOSTIP + "} " + // NOI18N
                   " {" + MAIN_SWITCH + "}" , // NOI18N
                   NbBundle.getBundle(RMIDebugType.class).getString("MSG_DebuggerHint") // NOI18N
               );
    }

    private void readObject (java.io.ObjectInputStream ois)
    throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        if (serialVer == 0) {
            setDebuggerProcess (getDefaultProcess ());
        }
    }

    /** Starts the debugger.
    */
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
        // prepare the map
        Map map = RMIExecutorSettings.getDefault().getSettings();
        if (info instanceof RMIExecInfo) {
            ((RMIExecInfo) info).addSettings(map);
        }

        // get new descriptor
        process = RMIExecutorSettings.format(map, process);
        super.startDebugger (
            className,
            arguments,
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

    /** setName was protected.
     * @param name - name
     */
    public void setName(String name) {
        super.setName(name);
    }

    // -- RMI specific properties. --

    /** Getter for host name.
     * @return host name
     */
    public String getHostname() {
        return RMIExecutorSettings.getDefault().getHostname();
    }

    /** Getter for host IP.
     * @return host name
     */
    public String getHostIP() {
        return RMIExecutorSettings.getDefault().getHostIP();
    }

    /** Getter for internal Http server port.
     * @return port
     */
    public int getInternalHttpPort() {
        return RMIExecutorSettings.getDefault().getInternalHttpPort();
    }

    /** Getter for repository URL.
     * @return url
     */
    public URL getRepositoryURL() {
        return RMIExecutorSettings.getDefault().getRepositoryURL();
    }

    /** Getter for repository URL with IP instead of hostname.
     * @return url with IP
     */
    public URL getRepositoryIPURL() {
        return RMIExecutorSettings.getDefault().getRepositoryIPURL();
    }
}

/*
* <<Log>>
*  10   Gandalf-post-FCS1.6.1.2     4/18/00  Jan Jancura     New "default" debugger 
*       type
*  9    Gandalf-post-FCS1.6.1.1     3/31/00  Martin Ryzl     updated to new the 
*       debugger  
*  8    Gandalf-post-FCS1.6.1.0     3/20/00  Martin Ryzl     localization
*  7    Gandalf   1.6         2/11/00  Martin Ryzl     repository -> filesystems
*  6    Gandalf   1.5         11/27/99 Patrik Knakal   
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         10/8/99  Martin Ryzl     some bugfixes
*  3    Gandalf   1.2         8/19/99  Martin Ryzl     dependence od 
*       classdataobject removed
*  2    Gandalf   1.1         8/17/99  Martin Ryzl     some bugfixes
*  1    Gandalf   1.0         8/17/99  Martin Ryzl     
* $ 
*/ 

