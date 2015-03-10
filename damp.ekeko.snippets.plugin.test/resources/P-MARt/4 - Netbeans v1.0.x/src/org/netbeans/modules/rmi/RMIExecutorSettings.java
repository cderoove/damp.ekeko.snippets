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

import java.net.*;
import java.util.*;

import org.openide.execution.*;
import org.openide.util.*;

/**
 *
 * @author  mryzl
 */

public class RMIExecutorSettings extends Object {

    static final RMIExecutorSettings DEFAULT = new RMIExecutorSettings();

    /** Tag for hostname (getHostname()). */
    public static final String TAG_HOSTNAME = "hostname"; // NOI18N

    /** Tag for hostip (getHostIP()). */
    public static final String TAG_HOSTIP = "hostip"; // NOI18N

    /** Tag for internal Http port (getInternalHttpPort()). */
    public static final String TAG_HTTPPORT = "internalHttpPort"; // NOI18N

    /** Tag for repository URL (getRepositoryURL()). */
    public static final String TAG_REPOSITORYURL = "filesystemsURL"; // NOI18N

    /** Tag for repository URL with IP (getRepositoryIPURL()). */
    public static final String TAG_REPOSITORYIPURL = "filesystemsIPURL"; // NOI18N

    /** Tag for port from ExecInfo */
    public static final String TAG_EXPORT_PORT = "port"; // NOI18N

    /** Tag for service from ExecInfo */
    public static final String TAG_EXPORT_SERVICE = "service"; // NOI18N

    /** Creates new RMIExecutorSettings. */
    public RMIExecutorSettings() {
    }

    /** Getter for host name.
     * @return host name
     */
    public String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            return "localhost"; // NOI18N
        }
    }

    /** Getter for host IP.
     * @return host name
     */
    public String getHostIP() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            return "127.0.0.1"; // NOI18N
        }
    }

    /** Getter for internal Http server port.
     * @return port
     */
    public int getInternalHttpPort() {
        try {
            URL url = HttpServer.getRepositoryRoot();
            return url.getPort();
        } catch (Exception ex) {
            return 8082;
        }
    }

    /** Getter for repository URL.
     * @return url
     */
    public URL getRepositoryURL() {
        try {
            return HttpServer.getRepositoryRoot();
        } catch (Exception ex) {
            return null;
        }
    }

    /** Getter for repository URL with IP instead of hostname.
     * @return url with IP
     */
    public URL getRepositoryIPURL() {
        try {
            URL url = HttpServer.getRepositoryRoot();
            return  new URL(url.getProtocol(), getHostIP(), url.getPort(), url.getFile());
        } catch (Exception ex) {
            return null;
        }
    }

    /** Returns default settings.
    */
    public static RMIExecutorSettings getDefault() {
        return DEFAULT;
    }

    /** Add settings to the map.
    * @param map map
    * @return map with new settings (the same instance)
    */
    public Map addSettings(Map map) {
        map.put (TAG_HOSTNAME, getHostname ());
        map.put (TAG_HOSTIP, getHostIP ());
        map.put (TAG_HTTPPORT, String.valueOf(getInternalHttpPort ()));
        map.put (TAG_REPOSITORYURL, getRepositoryURL ());
        map.put (TAG_REPOSITORYIPURL, getRepositoryIPURL ());
        return map;
    }

    /** Get settings as a map.
    * @return map with new settings
    */
    public Map getSettings() {
        return addSettings(new HashMap());
    }

    /** Format NbProcessDescriptor.
    * @param descriptor 
    * @param map map for MapFormat
    * @return formated NbProcessDescriptor (new instance)
    */
    public static NbProcessDescriptor format(Map map, NbProcessDescriptor descriptor) {
        MapFormat format = new MapFormat(map);
        String process = descriptor.getProcessName();
        String arguments = descriptor.getArguments();
        String info = descriptor.getInfo();
        return new NbProcessDescriptor(
                   format.format(process),
                   format.format(arguments),
                   info
               );
    }
}

/*
* <<Log>>
*  4    Gandalf-post-FCS1.2.1.0     3/20/00  Martin Ryzl     localization
*  3    Gandalf   1.2         1/21/00  Martin Ryzl     repository changed to 
*       filesystems
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         8/17/99  Martin Ryzl     
* $ 
*/ 
