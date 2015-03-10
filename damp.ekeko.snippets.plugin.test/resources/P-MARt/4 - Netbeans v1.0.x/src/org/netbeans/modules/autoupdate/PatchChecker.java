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


import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.openide.modules.ModuleDescription;

/** This class finds all patches in the system
 * 
 * @author  Petr Hrebejk
 */
class PatchChecker extends Object {

    private static final String JAR_EXTENSION = ".JAR"; // NOI18N
    private static final String ZIP_EXTENSION = ".ZIP"; // NOI18N

    private static ModuleDescription[] patchArray = null;

    /** The class is singleton */
    private PatchChecker() {
    }

    static ModuleDescription[] getPatches() {

        if ( patchArray == null ) {

            File userDirectory = Autoupdater.Support.getUserPatchDirectory();
            File centralDirectory = Autoupdater.Support.getCentralPatchDirectory();

            Collection patches = new ArrayList();

            addPatches( userDirectory, patches );

            if ( !userDirectory.equals( centralDirectory ) ) {
                addPatches( centralDirectory, patches );
            }

            patchArray = new ModuleDescription[ patches.size() ];

            patches.toArray( patchArray );
        }

        return patchArray;
    }

    private static void addPatches( File directory, Collection result ) {

        File dirList[] = directory.listFiles( new FilenameFilter() {
                                                  public boolean accept( File dir, String name ) {
                                                      return name.toUpperCase().endsWith( JAR_EXTENSION ) ||
                                                             name.toUpperCase().endsWith( ZIP_EXTENSION );
                                                  }
                                              });

        for ( int i = 0; i < dirList.length; i++ ) {


            try {
                JarFile jarFile = new JarFile( dirList[i] );
                Manifest manifest = jarFile.getManifest();


                if ( manifest == null )
                    continue; // This is not a standard NetBeans patch

                ModuleDescription md = new ModuleDescription( "temp", manifest ); // NOI18N

                Iterator it = result.iterator();
                boolean found = false;
                while( it.hasNext() ) {
                    ModuleDescription td = (ModuleDescription)it.next();

                    if ( md.getCodeNameBase().equals( td.getCodeNameBase() ) ) {
                        found = true;
                        break;
                    }

                }

                if ( !found )
                    result.add( md );
            }
            catch ( java.io.IOException e ) {
            }

        }
    }
}
/*
 * Log
 *  4    Gandalf   1.3         1/12/00  Petr Hrebejk    i18n
 *  3    Gandalf   1.2         11/12/99 Petr Hrebejk    Bug fixes: Texts, Not 
 *       NetBeans patches, unselecting modules
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/8/99  Petr Hrebejk    
 * $
 */
