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

import java.net.URL;
import java.net.URLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.xml.sax.AttributeList;

import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInstall;
import org.openide.modules.ManifestSection;
import org.openide.modules.ModuleDescription;
import org.openide.modules.IllegalModuleException;
import org.openide.TopManager;
import org.openide.loaders.DataLoader;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard;

/**
* Item representing one module.
*
* @author Jaroslav Tulach, Petr Hrebejk
*/
class ModuleItem extends Object {

    /** Constants for module bases */
    static final int BASE_USER = 1;
    static final int BASE_CENTRAL = 2;
    static final int BASE_JAR = 3;

    /** is enabled or not */
    boolean enabled;

    /** URL for the class loader */
    private transient URL loaderURL;

    /** Old release number of the module */
    private transient int oldRelease = -1;

    /** Old specification version of the module */
    private transient String oldSpecVersion = null;

    /** Description of the module */
    transient ModuleDescription descr;

    /** The module was deleted from autoload folder */
    private transient boolean deleted = false;

    /** Base of the module local|cenral|file */
    private int base = BASE_USER;

    /** The jar file the module is loaded from - used for modules
    * with base == BASE_CENTRAL || BASE_USER || BASE_JAR )
    */
    private File moduleFile = null;

    /** Whether or not, in this session, the $NBHOME/docs/ dir has been added
    * to the system classloader to help find help sets.
    */
    private static boolean addedDocsDir = false;

    /** Just to be able to have modules without jar file */
    protected ModuleItem() {
    }

    /** Creates new module for Jar file.
    * @param base Base of the module BASE_USER || BASE_CENTRAL || BASE_JAR
    * @param name path to the module jar
    * @param enabled enabled?
    * @exception IllegalModuleException if the jar is not module
    * @exception IOException if an error occures during reading
    */
    public ModuleItem (int base, String location, boolean enabled)
    throws IllegalModuleException, FileNotFoundException,
        MalformedURLException, IOException {

        this.enabled = enabled;
        this.base = base;

        File file = null;

        switch( base ) {
        case BASE_USER:
            file = new File( ModuleInstallerSupport.getUserModuleDirectory(), location );
            break;
        case BASE_CENTRAL:
            file = new File( ModuleInstallerSupport.getCentralModuleDirectory(), location );
            break;
        case BASE_JAR:
            file = new File( location );
            break;
        default:
            throw new InternalError( "Unknown module base " ); // NOI18N
        }

        if ( !file.canRead() ) {
            throw new FileNotFoundException( "Can't find module jarfile : "  + file.toString() ); // NOI18N
        }

        moduleFile = file;
        JarFile jarFile = new JarFile( file );
        Manifest manifest = jarFile.getManifest();
        jarFile.close();
        //loaderURL = new URL( "jar:" + file.toURL().toExternalForm() + "!/" ); // NOI18N
        loaderURL = file.toURL();

        init (manifest, file.getName(), enabled);
    }

    /** Initializes new module item according to given manifiest
    * @param manifest Manifest of the module
    * @param name Name to initialize the ModuleDescription
    * @param enabled enabled?
    * @exception IllegalModuleException if the jar is not module
    * @exception IOException if an error occures during reading
    */
    protected void init (Manifest manifest, String name, boolean enabled)
    throws IllegalModuleException {
        this.enabled = enabled;
        this.descr = new ModuleDescription ( name, manifest);
    }

    /** @param en true if enabled
    */
    public void setEnabled (boolean en) {
        if (enabled == en) return;

        enabled = en;
        ModuleInstaller.changeEnabled (this);
    }

    void setEnabledNoNotify( boolean en ) {
        enabled = en;
    }


    /** @return true if enabled
    */
    public boolean isEnabled () {
        return enabled;
    }

    /** @return Representation of ModuleItem location in XML file */
    public String getLocationForXML () {

        switch ( base ) {
        case BASE_CENTRAL:
        case BASE_USER:
            return moduleFile.getName();
        case BASE_JAR:
            return moduleFile.getPath();
        }

        return loaderURL.toString();
    }

    /** @return URL to be used by class loaders */
    public URL getLoaderURL () {
        return loaderURL;
    }

    /** @return Release of the module from previous start of the Ide or -1 */
    public int getOldRelease() {
        return oldRelease;
    }

    /** Old release is set only by ModuleInstaller */
    void setOldRelease( int oldRelease ) {
        this.oldRelease = oldRelease;
    }

    /** @return Specification version of the module from previous start
     * of the IDE or <CODE>null</CODE> */
    public String getOldSpecVersion() {
        return oldSpecVersion;
    }

    /** Old release is set only by ModuleInstaller */
    void setOldSpecVersion( String oldSpecVersion ) {
        this.oldSpecVersion = oldSpecVersion;
    }

    /** Test if the module was updated after last shutdown of IDE
    */
    boolean isUpdated() {
        if ( descr.getCodeNameRelease() > oldRelease ) {
            return true;
        }

        try {
            if ( !ModuleDescription.compatibleWith( descr.getSpecVersion(), oldSpecVersion ) ) {
                return true;
            }
        }
        catch ( org.openide.modules.IllegalModuleException e ) {
            TopManager.getDefault().notifyException( e );
        }

        return false;
    }

    /** Was the module deleted? */
    /*
    boolean isDeleted() {
      return deleted;
}
    */
    /** Sets the deleted value of the module item */
    /*
    void setDeleted( boolean deleted ) {
      this.deleted = deleted;
}
    */

    /** Getter for the module base */
    public int getBase() {
        return base;
    }

    /** Getter for description */
    public ModuleDescription getDescription() {
        return descr;
    }

    /** Is the deletion of this item allowed */
    public boolean canDestroy() {
        return base != BASE_CENTRAL && base != BASE_USER;
    }

    /** Get the actual URL to point to this module's help set.
    * If there is a valid URL from the JAR itself (i.e. according to
    * ModuleDescription), then that of course is used. If not, we also
    * look in the docs/ folder before giving up.
    * @return the URL to the help set file, or null if no help set was specified, or if it could not be found
    */
    private URL getRealHelpSet () {
        try {
            return descr.getDescription ();
        } catch (IllegalStateException ise) {
            synchronized (ModuleItem.class) {
                if (addedDocsDir) {
                    TopManager.getDefault ().notifyException (ise);
                    return null;
                }
                // Now try docs/.
                String nbhome = System.getProperty ("netbeans.home");
                if (nbhome != null) {
                    try {
                        // Java bug: if you do not canonicalize, it will make an absolute path,
                        // however file:/d:/nbdir/./docs/ is not treated as a valid URL for the
                        // URLClassLoader, for some reason.
                        URL url = new File (nbhome, "docs").getCanonicalFile ().toURL (); // NOI18N
                        if (! url.toString ().endsWith ("/")) // NOI18N
                            url = new URL (url.toString () + "/"); // NOI18N
                        ModuleClassLoader.add (url);
                    } catch (Exception e) {
                        if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                            e.printStackTrace ();
                    }
                }
                String nbuser = System.getProperty ("netbeans.user");
                if (nbuser != null && ! nbuser.equals (nbhome)) {
                    try {
                        URL url = new File (nbuser, "docs").getCanonicalFile ().toURL (); // NOI18N
                        if (! url.toString ().endsWith ("/")) // NOI18N
                            url = new URL (url.toString () + "/"); // NOI18N
                        ModuleClassLoader.add (url);
                    } catch (Exception e) {
                        if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                            e.printStackTrace ();
                    }
                }
                addedDocsDir = true;
            }
            try {
                return descr.getDescription ();
            } catch (IllegalStateException ise2) {
                TopManager.getDefault ().notifyException (ise2);
                return null;
            }
        }
    }

    /** Initializes defaults.
    */
    public void restoreDefault () {
        URL helpSet = getRealHelpSet ();
        if (helpSet != null) {
            Help.getDefault ().addHelpSet (helpSet, descr.getCodeName (), descr.getName ());
        }
    }

    /** Deinitializes them.
    */
    public void unrestoreDefault () {
        URL helpSet = getRealHelpSet ();
        if (helpSet != null) {
            Help.getDefault ().removeHelpSet (helpSet);
        }
    }

    /** Called when the module is installed for the first time
    */
    public void installSection () {
        ModuleActions.attachTo (this);
        descr.forEachSection (INSTALL);
        ModuleActions.attachTo (null);
    }

    /** Called when the module is restored on startup
    */
    public void restoreSection () {
        ModuleActions.attachTo (this);
        descr.forEachSection (RESTORE);
        ModuleActions.attachTo (null);
    }

    /** Called when the module should be uninstalled.
    */
    public void uninstallSection () {
        ModuleActions.attachTo (this);
        descr.forEachSection (REMOVE);
        ModuleActions.attachTo (null);
    }

    /** Called when the module is installed for the first time
    */
    public void installCode () {
        descr.getModule ().installed ();
    }

    /** Called when the module is restored on startup
    */
    public void restoreCode () {
        descr.getModule ().restored ();
    }

    /** Called when the module should be uninstalled.
    */
    public void uninstallCode () {
        descr.getModule ().uninstalled ();
    }

    /** Called when the module was updated to higher version.
    */ 
    public void updatedCode() {
        descr.getModule ().updated( oldRelease, oldSpecVersion );
    }

    /** Called when the IDE is closing
    * @return false if the IDE should not close
    */
    public boolean closing () {
        try {
            return descr.getModule ().closing ();
        } catch (RuntimeException ex) {
            // if there is an error, suppose the module is completelly bad.
            return true;
        }
    }

    /** Called when the IDE is finallty closing. All modules accepted the close
    * of the IDE
    */
    public void closeCode() {
        descr.getModule ().close();
    }


    /** Mehod called by ModuleInstaller to get the XML format of ModuleItem
     */
    String toXML() {

        StringBuffer moduleBuffer = new StringBuffer( 150 );

        /*
        if ( isDeleted() && !( base == BASE_CENTRAL || base == BASE_USER ) )
          return moduleBuffer.toString();
        */

        // Module header
        moduleBuffer.append( ModuleTags.TAB ).append("<" ).append( ModuleTags.MODULE ); // NOI18N
        moduleBuffer.append( ModuleTags.NEW_LINE );

        // CodeBaseName
        moduleBuffer.append( ModuleTags.TABx2 ).append( ModuleTags.CODENAMEBASE ).append( "=\"" ); // NOI18N
        moduleBuffer.append( getDescription().getCodeNameBase() ).append( "\"" ); // NOI18N
        moduleBuffer.append( ModuleTags.NEW_LINE );

        // Version
        moduleBuffer.append( ModuleTags.TABx2 ).append( ModuleTags.RELEASE ).append( "=\"" ); // NOI18N
        moduleBuffer.append( getDescription().getCodeNameRelease() ).append( "\"" ); // NOI18N
        moduleBuffer.append( ModuleTags.TAB ).append( ModuleTags.SPECVERSION ).append( "=\"" ); // NOI18N
        moduleBuffer.append( getDescription().getSpecVersion() ).append( "\"" ); // NOI18N
        moduleBuffer.append( ModuleTags.NEW_LINE );

        // Base
        moduleBuffer.append( ModuleTags.TABx2 ).append( ModuleTags.BASE ).append( "=\"" ); // NOI18N
        switch ( base ) {
        case BASE_USER:
            moduleBuffer.append( ModuleTags.BASE_USER );
            break;
        case BASE_CENTRAL:
            moduleBuffer.append( ModuleTags.BASE_CENTRAL );
            break;
        case BASE_JAR:
            moduleBuffer.append( ModuleTags.BASE_JAR );
            break;
        }
        moduleBuffer.append( "\"" ).append( ModuleTags.NEW_LINE ); // NOI18N

        // URL
        moduleBuffer.append( ModuleTags.TABx2 ).append( ModuleTags.URL ).append( "=\"" ); // NOI18N
        moduleBuffer.append( getLocationForXML() );
        moduleBuffer.append( "\"" ); // NOI18N
        moduleBuffer.append( ModuleTags.NEW_LINE );

        // Enabled
        moduleBuffer.append( ModuleTags.TABx2 ).append( ModuleTags.ENABLED ).append( "=\"" ); // NOI18N
        moduleBuffer.append( new Boolean (isEnabled ()) ).append( "\"" ); // NOI18N
        moduleBuffer.append( ModuleTags.NEW_LINE );

        // Deleted
        /*
        if ( isDeleted() ) {
          moduleBuffer.append( ModuleTags.TABx2 ).append( ModuleTags.DELETED ).append( "=\"" );
          moduleBuffer.append( new Boolean ( isDeleted ()) ).append( "\"" );
          moduleBuffer.append( ModuleTags.NEW_LINE );
    }
        */

        // Module footer
        moduleBuffer.append( "/>"); // NOI18N
        moduleBuffer.append( ModuleTags.NEW_LINE );

        return moduleBuffer.toString();
    }


    /** Restores the item from XML. Creates new instance of ModuleItem
     * represented by the attribute list. 
     * @return New ModuleItem or null if cannot be restored.
     */
    static ModuleItem fromXML( AttributeList attr, URL base ) {

        // Read main flags

        String url = attr.getValue( ModuleTags.URL );
        String enabled = attr.getValue( ModuleTags.ENABLED );
        // String deleted = attr.getValue( ModuleTags.DELETED );
        String baseAttr = attr.getValue( ModuleTags.BASE );

        // Read old version of module

        int relase;
        try {
            relase = Integer.parseInt( attr.getValue( ModuleTags.RELEASE ) );
        }
        catch ( NumberFormatException e ) {
            relase = -1;
        }

        String specVersion = attr.getValue( ModuleTags.SPECVERSION );

        // Restore the URL of module jar file

        if ( url != null ) {
            try {

                int baseCode = BASE_CENTRAL;

                if (  ModuleTags.BASE_USER.equals( baseAttr ) ) {
                    baseCode = BASE_USER;
                }
                else if ( ModuleTags.BASE_JAR.equals( baseAttr ) ) {
                    baseCode = BASE_JAR;
                }

                ModuleItem mi = new ModuleItem( baseCode, url,
                                                enabled == null || Boolean.valueOf (enabled).booleanValue () );

                /*
                if ( deleted != null ) {
                  mi.setDeleted( Boolean.valueOf (deleted).booleanValue () );
            }
                */

                mi.setOldRelease( relase );
                mi.setOldSpecVersion( specVersion );
                return mi;
            } catch (FileNotFoundException e) {
                // simply ignore (Ian's request)
            } catch (IOException e) {
                TopManager.getDefault().notifyException(e);
            }
        }

        return null;
    }


    /** Installs everything.
    */
    private static class InstallIterator extends Object implements ManifestSection.Iterator {

        /** Process action section.
         */
        public void processAction (ManifestSection.ActionSection as) throws InstantiationException {
            ModuleActions.add (as);
        }

        /** Processes the option section.
         */
        public void processOption (ManifestSection.OptionSection os) throws InstantiationException {
            NbControlPanel pool = NbControlPanel.getDefault ();
            pool.add (os.getOption ());
        }

        /** Processes the loader section.
        */
        public void processLoader (ManifestSection.LoaderSection ls) throws InstantiationException {
            LoaderPoolNode.add (ls);
        }


        /** Processes debugger section
        */
        public void processDebugger (ManifestSection.DebuggerSection ds) throws InstantiationException {
            NbTopManager.setDebugger (ds.getDebugger ());
        }

        /** Processes executor section
        */
        public void processService (ManifestSection.ServiceSection es) throws InstantiationException {
            Services.addService (es);
        }

        /** Processes filesystem section
        */
        public void processFileSystem (ManifestSection.FileSystemSection fs) throws InstantiationException {
            ModuleFSSection.install (fs);
        }

        /** Processes environment node section.
        * @exception Exception if there is an error and the section should be removed
        */
        public void processNode (ManifestSection.NodeSection es) throws InstantiationException {
            if (ManifestSection.NodeSection.TYPE_ROOTS.equalsIgnoreCase (es.getType ())) {
                NbPlaces.addRoot (es.getNode ());
            } else if (ManifestSection.NodeSection.TYPE_SESSION.equalsIgnoreCase (es.getType ())) {
                NbPlaces.addSession (es.getNode ());
            } else {
                EnvironmentNode.addNode (es.getNode ());
            }
        }

        /** Processes clipboard convertor section.
        * @exception Exception if there is an error and the section should be removed
        */
        public void processClipboardConvertor (ManifestSection.ClipboardConvertorSection es) throws InstantiationException {
            CoronaClipboard.addConvertor (es.getConvertor ());
        }

    }

    /** Installs new modules. */
    private static final ManifestSection.Iterator INSTALL = new InstallIterator ();

    /** Restores existing modules.
    * Not private because ModuleInstaller uses it for the IDE manifest.
    */
    static final ManifestSection.Iterator RESTORE = INSTALL;

    /** Deinstalator of sections.
    */
    private static final ManifestSection.Iterator REMOVE = new ManifestSection.Iterator () {
                /** Process action section.
                */
                public void processAction (ManifestSection.ActionSection as) throws InstantiationException {
                    ModuleActions.remove (as);
                }

                /** Processes the option section.
                */
                public void processOption (ManifestSection.OptionSection os) throws InstantiationException {
                    NbControlPanel pool = NbControlPanel.getDefault ();
                    pool.remove (os.getOption ());
                }

                /** Processes the loader section.
                */
                public void processLoader (ManifestSection.LoaderSection ls) throws InstantiationException {
                    DataLoader loader = ls.getLoader ();

                    LoaderPoolNode.remove (loader);
                }


                /** Processes debugger section
                */
                public void processDebugger (ManifestSection.DebuggerSection ds) throws InstantiationException {
                    NbTopManager.setDebugger (null);
                }

                /** Processes executor section
                */
                public void processService (ManifestSection.ServiceSection es) throws InstantiationException {
                    Services.removeService (es);
                }

                /** Processes filesystem section
                */
                public void processFileSystem (ManifestSection.FileSystemSection fs) throws InstantiationException {
                    ModuleFSSection.uninstall (fs);
                }

                /** Processes environment node section.
                * @exception Exception if there is an error and the section should be removed
                */
                public void processNode (ManifestSection.NodeSection es) throws InstantiationException {
                    if (ManifestSection.NodeSection.TYPE_ROOTS.equalsIgnoreCase (es.getType ())) {
                        NbPlaces.removeRoot (es.getNode ());
                    } else if (ManifestSection.NodeSection.TYPE_SESSION.equalsIgnoreCase (es.getType ())) {
                        NbPlaces.removeSession (es.getNode ());
                    } else {
                        EnvironmentNode.removeNode (es.getNode ());
                    }
                }

                /** Processes clipboard convertor section.
                * @exception Exception if there is an error and the section should be removed
                */
                public void processClipboardConvertor (ManifestSection.ClipboardConvertorSection es) throws InstantiationException {
                    CoronaClipboard.removeConvertor (es.getConvertor ());
                }

            };
}

/*
* Log
*  45   Gandalf   1.44        1/16/00  Ian Formanek    Removed semicolons after 
*       methods body to prevent fastjavac from complaining
*  44   Gandalf   1.43        1/13/00  Petr Hrebejk    i18n
*  43   Gandalf   1.42        1/10/00  Jesse Glick     #5252 -- making lack of 
*       helpset for module a nonfatal error.
*  42   Gandalf   1.41        1/5/00   Petr Hrebejk    New module installer
*  41   Gandalf   1.40        12/2/99  Jesse Glick     Loaders cannot be removed
*       from pool, either intentionally or accidentally (e.g. after failed 
*       deserialize).
*  40   Gandalf   1.39        11/25/99 Jesse Glick     Rewrite of 
*       LoaderPoolNode, specifically the management of loader ordering. Now 
*       permits multiple -before and -after dependencies, and should be more 
*       robust. Also made LoaderPoolItemNode's properly deletable and fixed a 
*       timing-related NullPointerException when uninstalling modules.
*  39   Gandalf   1.38        11/6/99  Jesse Glick     You may now unwrap 
*       documentation (for OpenIDE-Module-Description) to $NBHOME/docs/ 
*       preserving classpath (or $NBUSER/docs/).
*  38   Gandalf   1.37        10/27/99 Petr Hrebejk    Testing of modules added
*  37   Gandalf   1.36        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  36   Gandalf   1.35        10/7/99  Petr Hrebejk    
*  35   Gandalf   1.34        9/10/99  Jaroslav Tulach Services API.
*  34   Gandalf   1.33        9/3/99   Jaroslav Tulach Catches exception in 
*       closing.
*  33   Gandalf   1.32        8/1/99   Petr Hrebejk    Multiuser install fixed
*  32   Gandalf   1.31        7/28/99  Jaroslav Tulach Additional manifest & 
*       separation of actions by modules
*  31   Gandalf   1.30        7/9/99   Jesse Glick     ModuleHelpAction 
*       implemented.
*  30   Gandalf   1.29        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  29   Gandalf   1.28        5/27/99  Jaroslav Tulach Executors rearanged.
*  28   Gandalf   1.27        5/16/99  Jaroslav Tulach Throws FileNotFound 
*       instead of Zip exception
*  27   Gandalf   1.26        5/13/99  Jaroslav Tulach Services.
*  26   Gandalf   1.25        5/7/99   Jaroslav Tulach Help.
*  25   Gandalf   1.24        5/6/99   Jaroslav Tulach Should load also 
*       extensions.
*  24   Gandalf   1.23        5/4/99   Jaroslav Tulach Relative URL for modules.
*  23   Gandalf   1.22        5/4/99   Jaroslav Tulach Correct processing of 
*       executors.
*  22   Gandalf   1.21        4/28/99  Jaroslav Tulach XML storage for modules.
*  21   Gandalf   1.20        4/16/99  Libor Martinek  
*  20   Gandalf   1.19        4/7/99   Ian Formanek    Rename 
*       Section->ManifestSection
*  19   Gandalf   1.18        4/7/99   Ian Formanek    Rename 
*       Description->ModuleDescription
*  18   Gandalf   1.17        3/30/99  Jaroslav Tulach Form loader before Java 
*       loaderem.
*  17   Gandalf   1.16        3/29/99  Jaroslav Tulach places ().nodes 
*       ().session ()
*  16   Gandalf   1.15        3/26/99  Jaroslav Tulach 
*  15   Gandalf   1.14        3/26/99  Ian Formanek    Fixed use of obsoleted 
*       NbBundle.getBundle (this)
*  14   Gandalf   1.13        3/25/99  Ian Formanek    
*  13   Gandalf   1.12        3/25/99  Jaroslav Tulach Loader pool order fixed.
*  12   Gandalf   1.11        3/13/99  Jaroslav Tulach Places.roots ()
*  11   Gandalf   1.10        3/10/99  Jaroslav Tulach Modules from 
*       autoloadfolder are restored not initialized
*  10   Gandalf   1.9         3/8/99   Jesse Glick     For clarity: Module -> 
*       ModuleInstall; NetBeans-Module-Main -> NetBeans-Module-Install.
*  9    Gandalf   1.8         2/25/99  Jaroslav Tulach Change of clipboard 
*       management  
*  8    Gandalf   1.7         2/18/99  David Simonek   initializer manifest 
*       added
*  7    Gandalf   1.6         1/27/99  Jaroslav Tulach 
*  6    Gandalf   1.5         1/12/99  Jaroslav Tulach 
*  5    Gandalf   1.4         1/12/99  Jaroslav Tulach Modules are loaded by 
*       URLClassLoader
*  4    Gandalf   1.3         1/11/99  Ian Formanek    
*  3    Gandalf   1.2         1/6/99   Jaroslav Tulach Debugger is now module.
*  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting change in 
*       datasystem package
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/

