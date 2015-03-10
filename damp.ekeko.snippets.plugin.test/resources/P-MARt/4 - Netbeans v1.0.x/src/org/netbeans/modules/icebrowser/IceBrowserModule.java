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

package org.netbeans.modules.icebrowser;

import org.openide.awt.HtmlBrowser;
import org.openide.modules.ModuleInstall;
import java.util.Properties;

/**
* Module installation class for Ice Browser
*
* @author Jan Jancura
*/
public class IceBrowserModule extends ModuleInstall {

    static final long serialVersionUID =1906033893126812896L;
    /** Module installed for the first time. */
    public void installed () {
        installIceBrowser ();
    }

    /** Module installed again. */
    public void restored () {
        installIceBrowser ();
    }

    private void installIceBrowser () {
        Properties p = System.getProperties ();
        p.put ("ice.iblite.installSecurityManager", "false"); // NOI18N
        p.put ("ice.iblite.verbose", "false"); // NOI18N
        String ver = (String) p.get ("java.version"); // NOI18N
        p.put ("java.version", "1.1"); // NOI18N
        System.setProperties (p);

        try {
            Class c = Class.forName ("ice.iblite.Document"); // NOI18N
        } catch (ClassNotFoundException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
        p = System.getProperties ();
        p.put ("java.version", ver); // NOI18N
        System.setProperties (p);
        HtmlBrowser.setFactory (new HtmlBrowser.Factory () {
                                    /**
                                    * Returns a new instance of BrowserImpl implementation.
                                    */
                                    public HtmlBrowser.Impl createHtmlBrowserImpl () {
                                        return new IceBrowserImpl ();
                                    }
                                });
    }

}

/*
 * Log
 *  15   src-jtulach1.14        1/13/00  Ian Formanek    NOI18N
 *  14   src-jtulach1.13        12/23/99 Jan Jancura     New version of Ice 
 *       Browser support
 *  13   src-jtulach1.12        11/27/99 Patrik Knakal   
 *  12   src-jtulach1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   src-jtulach1.10        10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  10   src-jtulach1.9         6/11/99  Jaroslav Tulach System.out commented
 *  9    src-jtulach1.8         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    src-jtulach1.7         6/7/99   Jan Jancura     Starting messages 
 *       hidden.
 *  7    src-jtulach1.6         4/8/99   Ian Formanek    Fixed bug 1461 - During 
 *       first startup, the IceBrowser module is not correctly installed. 
 *       Subsequent startups of Gandalf work OK.
 *  6    src-jtulach1.5         3/21/99  Jan Jancura     
 *  5    src-jtulach1.4         3/9/99   Ian Formanek    Fixed last change
 *  4    src-jtulach1.3         3/9/99   Ian Formanek    Removed obsoleted import
 *  3    src-jtulach1.2         3/8/99   Jesse Glick     For clarity: Module -> 
 *       ModuleInstall; NetBeans-Module-Main -> NetBeans-Module-Install.
 *  2    src-jtulach1.1         3/2/99   Jan Jancura     BrowserFactory & 
 *       BrowserImpl moved to HtmlBrowser
 *  1    src-jtulach1.0         2/17/99  Jan Jancura     
 * $
 */
