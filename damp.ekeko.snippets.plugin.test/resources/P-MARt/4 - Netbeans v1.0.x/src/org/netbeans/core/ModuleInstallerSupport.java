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

import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.openide.TopManager;
import org.openide.modules.*;

/** ModuleInstallerSupport provides supporting methods for ModuleInstaller. It
 * cares about writing/reading modules to/from XML. Maintains list of modules etc.
 * check dependencies
 *
 * @author  Administrator
 * @version 
 */
class ModuleInstallerSupport extends Object {

    /** Extensions for modules in the folders */
    private static final String JAR_EXT = ".jar"; // NOI18N

    /** Installed enabled modules */
    static final int ENABLED_MODULE = 1;
    /** Installed disabled modules */
    static final int DISABLED_MODULE = 2;
    /** Deleted modules */
    // static final int DELETED_MODULE = 4;
    /** Modules for testing */
    static final int TEST_MODULE = 8;
    /** New modules added in autoload directory since last start */
    static final int AUTOLOAD_CENTRAL_MODULE = 16;
    /** New modules found in users autoload directory */
    static final int AUTOLOAD_USER_MODULE = 32;
    /** Patches - not used yet */
    static final int PATCH = 64;

    /** Central directory for modules */
    private static File centralModuleDirectory = null;

    /** User directory for modules */
    private static File userModuleDirectory = null;

    /** Name of file to contain stored modules */
    private static final String INSTALLED_MODULES = "installedModules.xml"; // NOI18N

    /** Map between module names and ModuleItems 
     * @associates ModuleItem*/
    private HashMap nameModuleMap = new HashMap( 77 );

    /** Enabled and disabled - installed modules 
     * @associates ModuleItem*/
    private LinkedList installedModules = new LinkedList();

    /** Modules for testing 
     * @associates ModuleItem*/
    private LinkedList testModules = new LinkedList();

    /** Modules deleted */
    // private LinkedList deletedModules = new LinkedList();

    /** Modules found in autoload director(ies) 
     * @associates ModuleItem*/
    private LinkedList autoloadCentralModules = new LinkedList();

    /** Modules found in local autoload directory 
     * @associates ModuleItem*/
    private LinkedList autoloadUserModules = new LinkedList();

    /** Creates new ModuleRegistry
    */
    public ModuleInstallerSupport() {
        try {
            readRegistry();
        }
        catch ( java.io.IOException e ) {
            // Do nothing - leave the registry empty
        }

        // System.out.println("Looking for new modules" );


        findNewModules();
    }

    /** Gets the central directory of modules
    */
    static File getCentralModuleDirectory() {
        if ( centralModuleDirectory == null ) {
            centralModuleDirectory = new File (Main.homeDir + File.separator + Main.DIR_MODULES);
        }
        return centralModuleDirectory;
    }

    /** Gets the user modules directory
    */
    static File getUserModuleDirectory() {
        if ( userModuleDirectory == null ) {
            userModuleDirectory = new File (Main.userDir == null ? Main.homeDir : Main.userDir
                                            + File.separator + Main.DIR_MODULES);
        }
        return userModuleDirectory;
    }

    /** Gets all installed parts of given kind
    */
    Collection get( int kind ) {
        ArrayList result = new ArrayList();

        // Standard modules
        Iterator it = installedModules.iterator();
        while( it.hasNext() ) {
            ModuleItem mi = (ModuleItem) it.next();

            if ( ( kind & ENABLED_MODULE ) > 0 && mi.isEnabled() ) {
                result.add( mi );
            }
            else if ( ( kind & DISABLED_MODULE ) > 0 && !mi.isEnabled() ) {
                result.add( mi );
            }

        }

        // Test Modules
        if ( ( kind & TEST_MODULE ) > 0 ) {
            result.addAll( testModules );
        }

        // Deleted Modules
        /*
        if ( ( kind & DELETED_MODULE ) > 0 ) {
          result.addAll( deletedModules );
    }
        */

        // Autoload Modules
        if ( ( kind & AUTOLOAD_CENTRAL_MODULE ) > 0 ) {
            result.addAll( autoloadCentralModules );
        }

        // AutloadLocal Modules
        if ( ( kind & AUTOLOAD_USER_MODULE ) > 0 ) {
            result.addAll( autoloadUserModules );
        }

        return result;
    }

    /** Adds module into right into specified collection */
    void add( ModuleItem mi, int kind ) {
        switch ( kind ) {
        case ENABLED_MODULE:
        case DISABLED_MODULE:
            installedModules.add( mi );
            break;
        case TEST_MODULE:
            testModules.add( mi );
            break;
            /*
            case DELETED_MODULE: 
             deletedModules.add( mi );
             break;
             */
        case AUTOLOAD_CENTRAL_MODULE:
            autoloadCentralModules.add( mi );
            break;
        case AUTOLOAD_USER_MODULE:
            autoloadUserModules.add( mi );
            break;
        default:
            return;
        }

        nameModuleMap.put( mi.getDescription().getCodeNameBase(), mi );
    }


    /** Removes module from given collection */
    void remove( ModuleItem mi, int kind ) {
        switch ( kind ) {
        case ENABLED_MODULE:
        case DISABLED_MODULE:
            installedModules.remove( mi );
            break;
        case TEST_MODULE:
            testModules.remove( mi );
            break;
            /*
            case DELETED_MODULE: 
            deletedModules.remove( mi );
            break;
            */
        case AUTOLOAD_CENTRAL_MODULE:
            autoloadCentralModules.remove( mi );
            break;
        case AUTOLOAD_USER_MODULE:
            autoloadUserModules.remove( mi );
            break;
        default:
            return;
        }

        nameModuleMap.remove( mi.getDescription().getCodeNameBase() );
    }

    /** Gets the kind of given module */
    int getKind( ModuleItem mi ) {
        ArrayList result = new ArrayList();

        // Standard modules
        if ( installedModules.contains( mi ) ) {
            if ( mi.isEnabled() )
                return ENABLED_MODULE;
            else
                return DISABLED_MODULE;
        }

        // Test Modules
        if ( testModules.contains( mi ) ) {
            return TEST_MODULE;
        }

        // Deleted Modules
        /*
        if ( deletedModules.contains( mi ) ) {
          return DELETED_MODULE;
    }
        */

        // Autoload Central Modules
        if ( autoloadCentralModules.contains( mi ) ) {
            return AUTOLOAD_CENTRAL_MODULE;
        }

        // Autload Local Modules
        if ( autoloadUserModules.contains( mi ) ) {
            return AUTOLOAD_USER_MODULE;
        }

        return -1;
    }

    /** Gets installed part identified by it's key
    */
    ModuleItem get( String key ) {
        return (ModuleItem)nameModuleMap.get( key );
    }

    /** Writes the whole registry to XML file
    */
    void writeRegistry() {

        OutputStream os = null;

        try {
            os = new BufferedOutputStream (
                     new FileOutputStream ( new File ( getUserModuleDirectory(), INSTALLED_MODULES )));
            new XML().write ( os );
        }
        catch ( IOException e ) {
            TopManager.getDefault().notifyException( e );
        }
        finally {
            if ( os != null )
                try {
                    os.close ();
                }
                catch( IOException e ) {}
        }
    }

    /** Reads the whole registry. The read of registry is not restricted to
    * to reading of XML file, it also checks the module directory for newly
    * installed modules (Autoload feature)
    */ 
    void readRegistry() throws java.io.IOException {

        File f = new File ( getUserModuleDirectory(), INSTALLED_MODULES);
        InputStream is = new BufferedInputStream (new FileInputStream (f));
        try {
            new XML().read( f.toURL (), is );
        }
        finally {
            is.close ();
        }
    }

    /** Test's wether IDE is runs under multiuser installation */
    static boolean isMultiuser() {
        return !getUserModuleDirectory().equals( getCentralModuleDirectory() );
    }

    /** Loadsthe new modules from modules directory.
    */
    void findNewModules() {

        if ( isMultiuser() ) {
            // In multiuser install first scan users directory
            // System.out.println("Looking in USER" );

            findModulesInFolder( getUserModuleDirectory(), AUTOLOAD_USER_MODULE );
        }

        // System.out.println("LOOKING iN CENTRAL" );

        findModulesInFolder( getCentralModuleDirectory(), AUTOLOAD_CENTRAL_MODULE );
    }

    /** Finds all jars in given folder and loads them into collection specified by
    * moduleType
    * @param folder Folder to search
    * @param moduleType Type of the module ( AUTOLOAD_CENTRAL_MODULE || AUTOLOAD_USER_MODULE )
    */
    private void findModulesInFolder( File folder, int moduleType ) {

        // System.out.println("Looking in : " + folder ); // NOI18N

        if ( moduleType != AUTOLOAD_CENTRAL_MODULE && moduleType != AUTOLOAD_USER_MODULE ) {
            throw new InternalError();
        }

        // Find all jarfiles in the folder

        final String[] list = folder.list ( new FilenameFilter() {
                                                public boolean accept( File dir, String name ) {
                                                    return name.toLowerCase().endsWith( JAR_EXT );
                                                }
                                            } );

        if ( list == null || list.length == 0 )
            return;



        // Go through the list and add ModuleItems into the collection
        for ( int i = 0; i < list.length; i++ ) {

            try {
                ModuleItem mi = new ModuleItem (
                                    moduleType == AUTOLOAD_CENTRAL_MODULE ? ModuleItem.BASE_CENTRAL : ModuleItem.BASE_USER,
                                    list[i],
                                    true);

                ModuleItem existing = get( mi.getDescription().getCodeNameBase() );

                if ( existing != null ) { // The module already exists
                    int kind = getKind( existing );
                    switch ( kind ) {
                    case ENABLED_MODULE:
                    case DISABLED_MODULE:

                        // System.out.print(" Module mismatch " + mi.getDescription().getCodeNameBase() );
                        // System.out.println(" : " +  existing.getBase()  +  "  : "  + mi.getBase() );


                        //case DELETED_MODULE:
                        if ( ( existing.getBase() != ModuleItem.BASE_CENTRAL &&
                                existing.getBase() != ModuleItem.BASE_USER ) ||
                                ( existing.getBase() == ModuleItem.BASE_CENTRAL &&
                                  mi.getBase() == ModuleItem.BASE_USER ) ||
                                ( existing.getBase() == ModuleItem.BASE_USER &&
                                  mi.getBase() == ModuleItem.BASE_CENTRAL )) {

                            // If there is a new module update the new module.
                            // System.out.println(" Module mismatch " + mi.getDescription().getCodeNameBase() );

                            mi.setOldRelease( existing.getOldRelease() );
                            mi.setOldSpecVersion( existing.getOldSpecVersion() );

                            if ( mi.isUpdated() ) {
                                remove( existing, kind );
                                add( mi, kind );
                            }

                        }
                        continue; // Standard behavior the module is already loaded from XML
                    case TEST_MODULE:
                        // Do nothing the test module goes first
                        continue;
                    case AUTOLOAD_USER_MODULE:
                    case AUTOLOAD_CENTRAL_MODULE:
                        // [ PENDING Show it to user ]
                        // System.out.println("AUTOLOAD Modules colision" ); // NOI18N
                        continue;
                    }
                }
                else { // If the module does not exists add it into new modules collection
                    add( mi, moduleType );
                }
            }
            catch (IOException e) {
                // wrong module
                TopManager.getDefault ().notifyException (e);
            }
        }
    }

    /** Returns all modules of given kind depending on module
    */
    Collection getDependentModules( ModuleDescription md, int kind ) {
        Collection result = new ArrayList();
        getDependentModules( md, kind, result );
        return result;
    }

    /** Private/recursive method for retrieving dependencies
    */
    private void getDependentModules( ModuleDescription md, int kind, Collection result ) {
        Collection modules = get( kind );

        //ModuleItem[] ims = getModuleItems( miSupport.ENABLED_MODULE );

        Iterator it = modules.iterator();
        //for( int j = 0; j < ims.length; j ++ ) {
        while ( it.hasNext() ) {
            //ModuleDescription imd = ims[j].getDescription();
            ModuleItem mi = (ModuleItem)it.next();
            ModuleDescription imd = mi.getDescription();

            if ( result.contains( mi ) || md == imd  ) {
                continue;
            }

            if ( md.getCodeNameBase().equals( imd.getCodeNameBase() ) ) {
                continue;
            }

            ModuleDescription.Dependency[] deps = imd.getDependencies();

            for( int k = 0; k < deps.length; k++ ) {
                if ( deps[k].getName().equals ( md.getCodeName ()) ) {
                    if ( !result.contains( mi ) ) {
                        result.add( mi );
                        getDependentModules( imd, kind, result );
                    }
                }
            }
        }
    }

    /** Checks for dependencies on disabled modules. If the module depends on
    * some disabled modules then the method returns collection of modules which 
    * should be enabled. If there are some other unsatisfied dependencies or
    * no problems it returns null.
    */
    Collection checkDependenciesOnDisabled( ModuleDescription md ) {
        Collection result = new ArrayList( 10 );
        return checkDependenciesOnDisabled( md, result ) ? result : null;
    }

    Collection checkDependenciesOnDisabled( Collection moduleItems ) {
        Collection result = new ArrayList( 10 );

        Iterator it = moduleItems.iterator();
        while( it.hasNext() ) {
            ModuleDescription md = ((ModuleItem)it.next()).getDescription();
            if ( !checkDependenciesOnDisabled( md, result ) )
                return null;
        }

        return result;
    }

    private boolean checkDependenciesOnDisabled( ModuleDescription md, Collection result ) {
        ModuleDescription[] otherModules = ModuleInstaller.getModuleDescriptions(
                                               ENABLED_MODULE | DISABLED_MODULE );
        ModuleDescription.Dependency[] deps = md.getDependencies();

        for ( int i = 0; i < deps.length; i++ )  {

            String miss = null;

            try {
                miss = deps[i].checkForMiss (otherModules);
            }
            catch ( IllegalModuleException e ) {
                return false;
            }
            if ( miss != null) {
                return false; // Some dependencies are not satisfied
            }
            else if ( deps[i].getType() != ModuleDescription.Dependency.TYPE_MODULE ) {
                continue;
            }
            else {
                // Dependency is satisfied lets look if the module is enabled
                String moduleName = deps[i].getName();
                int index = moduleName.lastIndexOf( '/' );

                if ( index == -1 ) {
                    index = moduleName.length();
                }

                ModuleItem dm = get( moduleName.substring( 0, index ) );

                if ( getKind( dm ) == DISABLED_MODULE && !result.contains( dm ) ) {
                    result.add( dm );
                    if ( !checkDependenciesOnDisabled( dm.getDescription(), result ) )
                        return false;
                }
            }
        }
        return true;
    }


    // InnerClasses -----------------------------------------------------------


    /** XML writes and reads all modules to / from XML.
    */
    private class XML extends HandlerBase {

        /** base URL */
        private URL base;

        /** Write the document to stream.
        * @param os output stream
        * @param list of ModuleItems
        * @exception IOException if I/O error occured
        */
        void write( OutputStream os ) throws IOException {

            Collection list = get( ENABLED_MODULE | DISABLED_MODULE /* | DELETED_MODULE */ | TEST_MODULE );

            PrintStream p = new PrintStream( os );

            p.print( ModuleTags.XML_HEADER );
            p.print( ModuleTags.NEW_LINE + ModuleTags.NEW_LINE );
            p.print( ModuleTags.MODULES_HEADER );
            p.print( ModuleTags.NEW_LINE );

            Iterator it = list.iterator ();
            while (it.hasNext ()) {
                ModuleItem mi = (ModuleItem)it.next ();

                p.print( mi.toXML() );
                p.print( ModuleTags.NEW_LINE );
            }

            p.print( ModuleTags.MODULES_FOOTER );
            p.print( ModuleTags.NEW_LINE );

            p.close ();
        }

        /** Parse the input stream.
        * @param url the base URL to referer all relative to
        * @return list of ModuleItems
        * @exception IOException if I/O error occured
        */
        void read( URL url, InputStream is ) throws IOException {
            base = url;

            Parser p = org.openide.loaders.XMLDataObject.createParser ();

            p.setDocumentHandler ( this );
            try {
                p.parse (new InputSource (is));
            } catch (SAXException e) {
                throw new IOException (e.getMessage ());
            }
            return;
        }

        /** Creates new XML parser/saver for given list of ModuleItems.
         */
        XML () {
        }

        /** Creates the right type of module item and adds it to list of modules.
         */
        public void startElement (String name, AttributeList attr) {

            if ( name.equals( ModuleTags.MODULE )  ) {
                ModuleItem mi = ModuleItem.fromXML( attr, base );
                if ( mi != null ) {
                    /* if ( mi.isDeleted() )
                      add( mi, ModuleInstallerSupport.DELETED_MODULE );
                    else */ if ( mi.isEnabled() )
                        add( mi, ModuleInstallerSupport.ENABLED_MODULE );
                    else
                        add( mi, ModuleInstallerSupport.DISABLED_MODULE );
                }
            }
            else if ( name.equals( ModuleTags.TEST_MODULE ) ) {
                TestModuleItem tmi = TestModuleItem.fromXML( attr );
                if ( tmi != null ) {
                    add( tmi, ModuleInstallerSupport.TEST_MODULE );
                }
            }
        }

    }

    public String toString() {
        StringBuffer sb = new StringBuffer( 500 );


        sb.append("ENABLED --------------------------\n" ); // NOI18N
        sb.append( modulesToString( get( ModuleInstallerSupport.ENABLED_MODULE ) ) );
        sb.append( "\n" ); // NOI18N
        sb.append("DISABLED --------------------------\n" ); // NOI18N
        sb.append( modulesToString( get( ModuleInstallerSupport.DISABLED_MODULE ) ) );
        sb.append( "\n" ); // NOI18N
        /*
        sb.append("DELETED --------------------------\n" ); // NOI18N
        sb.append( modulesToString( get( ModuleInstallerSupport.DELETED_MODULE ) ) );
        */
        sb.append( "\n" ); // NOI18N
        sb.append("TEST --------------------------\n" ); // NOI18N
        sb.append( modulesToString( get( ModuleInstallerSupport.TEST_MODULE ) ) );
        sb.append( "\n" ); // NOI18N
        sb.append("CENTRAL --------------------------\n" ); // NOI18N
        sb.append( modulesToString( get( ModuleInstallerSupport.AUTOLOAD_CENTRAL_MODULE ) ) );
        sb.append( "\n" ); // NOI18N
        sb.append("USER --------------------------\n" ); // NOI18N
        sb.append( modulesToString( get( ModuleInstallerSupport.AUTOLOAD_USER_MODULE ) ) );
        sb.append( "\n" ); // NOI18N

        return sb.toString();
    }

    String modulesToString( Collection modules ) {
        StringBuffer sb = new StringBuffer( 100 );
        Iterator it = modules.iterator();
        while( it.hasNext() ) {
            ModuleItem mi = (ModuleItem)it.next();
            sb.append( " " + mi.getDescription().getName() + mi.getLoaderURL() ); // NOI18N
            sb.append( "\n" ); // NOI18N
        }

        return sb.toString();
    }

}

/*
 * Log
 */