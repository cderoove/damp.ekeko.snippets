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

package org.netbeans.modules.html;

import org.openide.util.NbBundle;

/**
* Module installation class for HtmlModule
*
* @author Ian Formanek
*/
public class HtmlModule extends org.openide.modules.ModuleInstall {

    static final long serialVersionUID =-4470431636351881949L;
    /** Module installed for the first time. */
    public void installed () {
        // -----------------------------------------------------------------------------
        // 1. copy Templates
        copyTemplates ();

        restored ();
    }

    // -----------------------------------------------------------------------------
    // Private methods

    private void copyTemplates () {
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
                NbBundle.getLocalizedFile ("org.netbeans.modules.html.templates", "jar").openStream () // NOI18N
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }

}

/*
 * Log
 *  6    Gandalf   1.5         1/16/00  Jesse Glick     Localized jars.
 *  5    Gandalf   1.4         1/13/00  Ian Formanek    NOI18N
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  1    Gandalf   1.0         6/10/99  Ian Formanek    
 * $
 */
