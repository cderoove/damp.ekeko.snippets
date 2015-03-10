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

import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ResourceBundle;
import java.util.Properties;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.TopManager;
import org.openide.execution.NbProcessDescriptor;

/** Options for servlet support
*
* @author Petr Jiricka
* @version 0.01, May 18, 1999
*/
public class ServletSettings extends SystemOption {

    /** serialVersionUID */
    private static final long serialVersionUID = 4086443781681509635L;

    /** generated Serialized Version UID */
    //static final long serialVersionUID = -2930037136839837001L;
    public static final String TAG_URL = "URL"; // NOI18N

    /** constant for internal browser */
    public static final String INTERNAL_BROWSER = "internal"; // NOI18N
    /** constant for external browser */
    public static final String EXTERNAL_BROWSER = "external"; // NOI18N

    private static NbProcessDescriptor DEFAULT_EXTERNAL_BROWSER = new NbProcessDescriptor(
                // empty string for process
                "", // NOI18N
                // {URL}
                " {" + TAG_URL + "}", // NOI18N
                NbBundle.getBundle(ServletSettings.class).getString("MSG_BrowserExecutorHint")
            );

    private static NbProcessDescriptor externalBrowser = DEFAULT_EXTERNAL_BROWSER;

    private static String webBrowser = INTERNAL_BROWSER;

    /** bundle to obtain text information from */
    private static ResourceBundle bundle = NbBundle.getBundle(ServletSettings.class);

    /** jswdk settings */
    public final static ServletSettings OPTIONS = new ServletSettings();


    public ServletSettings() {
    }

    /** This is a project option. */
    private boolean isGlobal() {
        return false;
    }

    /** human presentable name */
    public String displayName() {
        return bundle.getString("CTL_servlet_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ServletSettings.class);
    }


    /** Setter for the external browser */
    public void setExternalBrowser(NbProcessDescriptor externalBrowser) {
        this.externalBrowser = externalBrowser;
        firePropertyChange0("externalBrowser", null, externalBrowser); // NOI18N
    }

    /** Getter for the external browser */
    public NbProcessDescriptor getExternalBrowser() {
        return externalBrowser;
    }

    /** Setter for the web browser */
    public void setWebBrowser(String webBrowser) {
        if (webBrowser.equals(INTERNAL_BROWSER) || webBrowser.equals(EXTERNAL_BROWSER)) {
            this.webBrowser = webBrowser;
            firePropertyChange0("webBrowser", null, webBrowser); // NOI18N
        }
    }

    /** Getter for the web browser */
    public String getWebBrowser() {
        return webBrowser;
    }



    /* Access the firePropertyChange from HTTPServer (which holds the enabled prop). */
    void firePropertyChange0 (String name, Object oldVal, Object newVal) {
        firePropertyChange (name, oldVal, newVal);
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
}

/*
 * Log
 *  6    Gandalf   1.5         1/18/00  Jesse Glick     Context help.
 *  5    Gandalf   1.4         1/12/00  Petr Jiricka    i18n phase 1
 *  4    Gandalf   1.3         1/4/00   Petr Jiricka    Added to project options
 *  3    Gandalf   1.2         12/21/99 Petr Jiricka    External browser changed
 *       to NbProcessDescriptor
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         8/3/99   Petr Jiricka    
 * $
 */
