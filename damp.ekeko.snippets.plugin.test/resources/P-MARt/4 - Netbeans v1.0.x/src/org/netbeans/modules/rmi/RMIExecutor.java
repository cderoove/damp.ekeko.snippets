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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.openide.util.*;
import org.openide.execution.ProcessExecutor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.ExecInfo;

/** The node representation of RMIDataObject for Java sources.
*
* @author Martin Ryzl
*/
public class RMIExecutor extends ProcessExecutor {

    /** Serial version UID. */
    static final long serialVersionUID = -1327375421897348602L;

    /** default descriptor to use */
    private static final NbProcessDescriptor DEFAULT = new NbProcessDescriptor (
                "{java.home}{/}bin{/}java",   // NOI18N
                "-cp {" + Format.TAG_REPOSITORY + "}" +  // NOI18N
                File.pathSeparatorChar + "{" + Format.TAG_CLASSPATH + "} " + // NOI18N
                "-Djava.security.policy={" + RMIExecutorSettings.TAG_REPOSITORYIPURL + "}RMI/rmi.policy " + // NOI18N
                "-Djava.rmi.server.codebase={" + RMIExecutorSettings.TAG_REPOSITORYIPURL + "} " + // NOI18N
                "-Djava.rmi.server.hostname={" + RMIExecutorSettings.TAG_HOSTIP + "} " + // NOI18N
                '{' + Format.TAG_CLASSNAME + '}' +
                " {" + Format.TAG_ARGUMENTS + '}', // NOI18N
                NbBundle.getBundle(RMIExecutor.class).getString ("MSG_ExecutorHint") // NOI18N
            );

    /** Constructor.
     */
    public RMIExecutor() {
        super();
        setExternalExecutor(DEFAULT);
        setName(org.openide.util.NbBundle.getBundle(RMIExecutor.class).getString ("PROP_RMIExecutorName")); // NOI18N
    }

    /**
    */
    public RMIExecutor(String name, String process, String args) {
        super();
        setExternalExecutor(new NbProcessDescriptor(
                                process,
                                args,
                                NbBundle.getBundle(RMIExecutor.class).getString ("MSG_ExecutorHint") // NOI18N
                            ));
        setName(name); // NOI18N
    }

    /**
     */
    public static class RMIFormat extends Format {

        /** Serial version UID. */
        static final long serialVersionUID = 745095811977999930L;

        /** All values for the paths takes from NbClassPath.createXXX methods.
         *
         * @param info exec info about class to execute 
         */
        public RMIFormat (ExecInfo info) {
            super(info);

            java.util.Map map = getMap();
            RMIExecutorSettings.getDefault().addSettings(map);

            if (info instanceof RMIExecInfo) {
                ((RMIExecInfo)info).addSettings(map);
            }
        }
    }  // class RMIFormat

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
        return getExternalExecutor ().exec (new RMIFormat (info));
    }

}

/*
 * <<Log>>
 *  19   Gandalf-post-FCS1.16.1.1    4/18/00  Martin Ryzl     constructor that is used
 *       by rmie added
 *  18   Gandalf-post-FCS1.16.1.0    3/20/00  Martin Ryzl     localization
 *  17   Gandalf   1.16        11/27/99 Patrik Knakal   
 *  16   Gandalf   1.15        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        8/18/99  Martin Ryzl     corrected localization
 *  14   Gandalf   1.13        8/17/99  Martin Ryzl     some bugfixes
 *  13   Gandalf   1.12        8/17/99  Martin Ryzl     debugger support
 *  12   Gandalf   1.11        8/16/99  Martin Ryzl     
 *  11   Gandalf   1.10        8/16/99  Martin Ryzl     debug prints were 
 *       removed
 *  10   Gandalf   1.9         8/12/99  Martin Ryzl     hints on executors and 
 *       compiler, debug executors
 *  9    Gandalf   1.8         7/20/99  Martin Ryzl     
 *  8    Gandalf   1.7         7/12/99  Martin Ryzl     large changes  
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         6/2/99   Martin Ryzl     new executor scheme
 *  5    Gandalf   1.4         5/27/99  Martin Ryzl     many fixes
 *  4    Gandalf   1.3         5/19/99  Martin Ryzl     
 *  3    Gandalf   1.2         5/19/99  Martin Ryzl     some bugfixing
 *  2    Gandalf   1.1         5/4/99   Martin Ryzl     
 *  1    Gandalf   1.0         5/4/99   Martin Ryzl     
 * $
 */














