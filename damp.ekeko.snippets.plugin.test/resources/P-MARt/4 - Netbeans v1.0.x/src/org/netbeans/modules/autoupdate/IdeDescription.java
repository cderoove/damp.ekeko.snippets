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

package org.netbeans.modules.autoupdate;

import java.beans.Beans;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import org.openide.modules.ModuleDescription;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/** Singleton creates module description for the currently installed IDE
*/
public class IdeDescription extends Object  {



    private static ModuleDescription ideDescription = null;

    /** Is a singleton */
    private IdeDescription ( ) {
    }


    /** Gets the name of IDE e.g. IDE/1 */
    static String getName() {
        return System.getProperty ("org.openide.major.version", "IDE") ;
    }

    /** Returns the description of currently */
    static ModuleDescription getIdeDescription() {

        if ( ideDescription == null ) {
            Manifest mf = new Manifest();
            Attributes attrs = mf.getMainAttributes();

            attrs.put( ModuleDescription.TAG_MAGIC, System.getProperty ("org.openide.major.version", "IDE") );
            attrs.put( ModuleDescription.TAG_NAME, "IDE Core" ); // NOI18N
            attrs.put( ModuleDescription.TAG_SPEC_VERSION, System.getProperty ("org.openide.specification.version", "IDE") );
            attrs.put( ModuleDescription.TAG_IMPL_VERSION, System.getProperty ("org.openide.version", "<unknown>") );

            // Create IDE description from the temporary manifest
            try {
                ModuleDescription md = new ModuleDescription( "IDE", mf ); // NOI18N
                ideDescription = md;
            }
            catch ( org.openide.modules.IllegalModuleException e ) {
            }
        }
        return ideDescription;
    }
}
/*
 * Log
 *  6    Gandalf   1.5         1/12/00  Petr Hrebejk    i18n
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/11/99 Petr Hrebejk    Version before Beta 5
 *  3    Gandalf   1.2         10/8/99  Petr Hrebejk    Next development version
 *  2    Gandalf   1.1         10/8/99  Petr Hrebejk    Next Develop version
 *  1    Gandalf   1.0         10/7/99  Petr Hrebejk    
 * $
 */
