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

package org.netbeans.core;

import org.openide.TopManager;
import org.openide.modules.ModuleDescription;

/** This class contains all method and interfaces needed for support of
* autoupdate module
*
* @author Petr Hrebejk
*/
public class UpdateSupport extends Object {

    private static UpdateChecker updateChecker = null;

    private static IDESettings ideSettings = new IDESettings();

    /** This class is a singleton */
    private  UpdateSupport() {
    }

    /** Retruns array of module descriptions of installed modules
     */ 
    public static ModuleDescription[] getModuleDescriptions() {
        return ModuleInstaller.getModuleDescriptions( ModuleInstallerSupport.ENABLED_MODULE | ModuleInstallerSupport.DISABLED_MODULE );
    }

    /** Restarts the IDE in order to run the Updater programm
     */
    public static void restart() {
        ((NbTopManager)TopManager.getDefault()).restart();
    }

    /** Installs the automatic update checker (Implemented in autoupdate module )
     */
    public static void installUpdateChecker( UpdateChecker uc ) {
        updateChecker = uc;
    }

    /** Calls UpdateChecker to chceck for new updates  (called by main)
    */
    static void performUpdateCheck() {
        if ( updateChecker != null ) {
            updateChecker.check();
        }
    }

    /** Innerclass for UpdateChecker checks automatiacially for new
     * updates
     */ 
    public static interface UpdateChecker {

        /** Performs the automatic check for new updates */
        public void check();
    }

    /** Gets proxy usage */
    public static boolean isUseProxy() {
        return ideSettings.getUseProxy();
    }

    /** Gets Proxy Host */
    public static String getProxyHost() {
        return ideSettings.getProxyHost();
    }

    /** Gets Proxy Port */
    public static String getProxyPort() {
        return ideSettings.getProxyPort();
    }

    /** Sets the whole proxy configuration */
    public static void setProxyConfiguration( boolean use, String host, String port ) {
        ideSettings.setUseProxy( use );
        ideSettings.setProxyHost( host );
        ideSettings.setProxyPort( port );
    }

}

/*
 * Log
 *  5    Gandalf-post-FCS1.2.1.1     4/5/00   Ian Formanek    Last check-in fixed bug 
 *       5460 - AutoUpdate treats disabled moduled as they were never installed.
 *  4    Gandalf-post-FCS1.2.1.0     4/5/00   Ian Formanek    Bugfix for AU vs. 
 *       disabled module courtesy Hrebejk
 *  3    Gandalf   1.2         1/9/00   Petr Hrebejk    Support for proxy confug
 *       in Autoupdate added
 *  2    Gandalf   1.1         1/5/00   Petr Hrebejk    New module installer
 *  1    Gandalf   1.0         12/1/99  Petr Hrebejk    
 * $
 */
