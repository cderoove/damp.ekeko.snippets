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

import java.io.*;
import java.util.*;
import java.beans.*;
import java.net.*;
import java.util.jar.Manifest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.openide.TopManager;
import org.openide.execution.*;
import org.openide.filesystems.*;
import org.openide.modules.*;
import org.openide.util.io.*;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;

/** This class implementats the main functions which are needed for
* installing of new module into the system and storage of them.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
public final class ModuleInstaller extends Object {

    /** Tes ResourceBundle */
    private static final ResourceBundle bundle = NbBundle.getBundle( ModuleInstaller.class );

    /** resource string for initializer module manifest file */
    private static final String MF_INITIALIZER =
        "/org/netbeans/core/resources/mf-initializer.txt"; // NOI18N

    /** name of file to contain stored externalizable modules */
    private static final String EXTERNALIZED_MODULES = "externalizedModules.ser"; // NOI18N

    /** Instance of ModuleInstaller support */
    private static ModuleInstallerSupport miSupport = null;

    /** Boolean property to remember if some modules were updated or disabled
     * to write new installedModules.xml during installing modules 
     */
    private static boolean writeRegistry = true;

    /** The only instance of the installer */
    private static final PropertyChangeSupport INSTANCE = new PropertyChangeSupport (new ModuleInstaller ());

    /** Private constructor */
    private ModuleInstaller () {
    }


    // PUBLIC METHODS ------------------------------------------------------------

    /** Adds listener on the nodes.
    */
    public static void addPropertyChangeListener (PropertyChangeListener l) {
        INSTANCE.addPropertyChangeListener (l);
    }

    /** Removes listener on the nodes.
    */
    public static void removePropertyChangeListener (PropertyChangeListener l) {
        INSTANCE.removePropertyChangeListener (l);
    }

    /** Getter for array of modules of given kind
    * @return array of modules
    */
    public static ModuleItem[] getModuleItems ( int kind ) {
        Collection modules = miSupport.get( kind );
        return (ModuleItem[])modules.toArray (new ModuleItem[ modules.size ()]);
    }

    /** Retruns array of module descriptions of installed modules */
    public static ModuleDescription[] getModuleDescriptions( int kind ) {
        Collection modules = miSupport.get( kind );
        ModuleItem[] items = (ModuleItem[])modules.toArray (new ModuleItem[ modules.size ()]);
        ModuleDescription[] result = new ModuleDescription[ items.length ];

        for( int i = 0; i < items.length; i++ ) {
            result[i] = items[i].getDescription();
        }
        return result;
    }

    /** Returns module item for name */
    static ModuleItem getByName( String codeNameBase ) {
        return miSupport.get( codeNameBase );
    }


    /** Initializes the modules loads the previously installed modules
     */
    public synchronized static void initialize() {

        // System.out.println("LOADING MODULES ------------------------------- " ); //NOI18N

        miSupport = new ModuleInstallerSupport();  // Reads the miSupport
        // System.out.println( miSupport );

        // Initalize actions pool ...
        ModuleActions.initialize();

        Collection m2i = miSupport.get( miSupport.ENABLED_MODULE );
        Collection orderedModules = null;

        // Sort enabled modules
        try {
            orderedModules = resolveOrdering( m2i );
        }
        catch ( IllegalModuleException e ) {
            TopManager.getDefault().notifyException( e );
        }
        if ( orderedModules == null ) {
            orderedModules = new ArrayList();
        }

        // Look wther some modules have to be disabled
        Collection dm = miSupport.get( miSupport.ENABLED_MODULE );
        dm.removeAll( orderedModules );

        if ( !dm.isEmpty() ) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                                      bundle.getString ("MSG_Some_Missed") + "\n" +
                                      getMissed( new ArrayList(), m2i ) );
            TopManager.getDefault().notify( nd );
        }

        // Print info
        if ( orderedModules.size() > 0 )
            System.err.println (Main.getString ( "INFO_LoadedModules",
                                                 moduleItemsDescr (m2i)));


        // Initialize module class loader
        initializeClassLoader( orderedModules );

        // Instal IDE
        installIDEManifest();

        // Add disabled modules to ordered modules
        orderedModules.addAll( dm );

        // Install default sections of all modules
        {
            Iterator it = orderedModules.iterator ();
            while (it.hasNext ()) {
                ModuleItem mi = (ModuleItem)it.next ();
                TopManager.getDefault ().setStatusText (Main.getString ( "MSG_Install_Section",
                                                        mi.getDescription ().getName ()));
                if ( dm.contains( mi ) ) {
                    writeRegistry = true;
                    mi.setEnabledNoNotify( false );
                }
                else {
                    mi.restoreSection ();
                    mi.restoreDefault ();
                }
            }
        }

        // Tell loader pool node the installation is finished
        // No longer necessary: LoaderPoolNode.finishInstallation();

        // Try to deexternalize modules
        try {
            deExternalizeModules();
        }
        catch ( IOException e ) {
            TopManager.getDefault().notifyException( e );
        }

        // Restored or Updated code of all modules
        {
            boolean someUpdated = false;

            Iterator it = orderedModules.iterator ();
            while (it.hasNext ()) {
                ModuleItem mi = (ModuleItem)it.next ();

                if (mi.isEnabled ()) {

                    TopManager.getDefault ().setStatusText (
                        Main.getString (  "MSG_Install_Module",  mi.getDescription ().getName ()));

                    if ( mi.isUpdated() ) {
                        mi.updatedCode();
                    }
                    else {
                        mi.restoreCode ();
                        someUpdated = true;
                    }
                }
            }

            if ( !writeRegistry )
                writeRegistry = someUpdated;
        }

        // Fire module change
        INSTANCE.firePropertyChange (null, null, null);

        // If multiuser let's restore the new central modules
        if ( miSupport.isMultiuser() ) {
            //System.out.println("Loading mu -- restored" ); // NOI18N
            autoLoadModules( miSupport.AUTOLOAD_CENTRAL_MODULE );
        }
    }

    /** Autoloads all modules into the system. This is called from Main and
    * installs new modules. In multiuser environment only the modules in USER directory
    * are installed. The modules from central directory are already restored
    *
    * This method also reads the modules for testing and installs them. The repository 
    * is already restored so we can load the classes.
    */
    public static synchronized void autoLoadModules () {
        if ( miSupport.isMultiuser() ) {
            autoLoadModules( miSupport.AUTOLOAD_USER_MODULE );
        }
        else {
            //System.out.println(" loading standard - no multiuser " ); // NOI18N
            autoLoadModules( miSupport.AUTOLOAD_CENTRAL_MODULE | miSupport.AUTOLOAD_USER_MODULE );
        }
    }

    public static synchronized void autoLoadModules ( int kind ) {

        // resort new modules
        Collection m2i = miSupport.get( kind );
        Collection newModules = null;

        try {
            newModules = resolveOrdering ( m2i, miSupport.get( miSupport.ENABLED_MODULE | miSupport.DISABLED_MODULE ) );
        } catch (IllegalModuleException e) {
            TopManager.getDefault ().notifyException (e);
        }

        Collection dm = miSupport.get( kind );
        dm.removeAll( newModules );

        if ( !dm.isEmpty() ) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                                      bundle.getString ("MSG_Some_New_Missed") + "\n" +
                                      getMissed( miSupport.get( miSupport.ENABLED_MODULE ), m2i ) );
            TopManager.getDefault().notify( nd );
        }


        if ( newModules == null )
            newModules = new ArrayList();


        Collection modules4Check = miSupport.get( kind );
        modules4Check.removeAll( dm );
        Collection enModules = miSupport.checkDependenciesOnDisabled ( modules4Check );
        if ( enModules != null && enModules.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append( bundle.getString( "MSG_EnableToInstall") );
            Iterator it = enModules.iterator();
            while( it.hasNext() ) {
                sb.append ( ((ModuleItem)it.next() ).getDescription().getCodeName() ).append( "\n" ); // NOI18N
            }
            NotifyDescriptor nd = new NotifyDescriptor.Message( sb.toString() );
            TopManager.getDefault().notify( nd );

            it = enModules.iterator();
            while( it.hasNext() ) {
                ((ModuleItem)it.next() ).setEnabled( true );
            }
        }

        // Print information about installed modules ...
        if ( newModules.size () > 0)
            System.err.println (Main.getString ("INFO_InstalledModules", moduleItemsDescr ( newModules )));


        // Add disabled modules to ordered modules
        newModules.addAll( dm );


        try {
            {
                Iterator it = newModules.iterator ();
                while (it.hasNext ()) {
                    ModuleItem mi = (ModuleItem)it.next ();

                    TopManager.getDefault ().setStatusText (Main.getString ("MSG_Install_Section", mi.getDescription ().getName ()));

                    if ( dm.contains( mi ) ) {
                        mi.setEnabledNoNotify( false );
                        continue;
                    }

                    ModuleClassLoader.add ( mi.getLoaderURL() );

                    if ( miSupport.isMultiuser() && miSupport.getKind( mi ) == miSupport.AUTOLOAD_CENTRAL_MODULE )
                        mi.restoreSection();
                    else
                        mi.installSection();

                    mi.restoreDefault();
                }
            }

            // Install code of all modules ...
            {
                Iterator it = newModules.iterator ();
                while (it.hasNext ()) {
                    ModuleItem mi = (ModuleItem)it.next ();


                    TopManager.getDefault ().setStatusText (Main.getString ("MSG_Install_Module", mi.getDescription ().getName ()));

                    if ( mi.isEnabled() ) {
                        if ( miSupport.isMultiuser() && miSupport.getKind( mi ) == miSupport.AUTOLOAD_CENTRAL_MODULE )
                            mi.restoreCode();
                        else
                            mi.installCode();
                    }

                    // add the module to list of installed modules
                    miSupport.remove( mi, miSupport.getKind( mi ) );
                    miSupport.add ( mi, miSupport.ENABLED_MODULE );
                }
            }

            if ( !newModules.isEmpty() ) {

                TopManager.getDefault().setStatusText( Main.getString (
                                                           "MSG_Install_Complete", // NOI18N
                                                           new Integer( newModules.size ()),
                                                           ((ModuleItem)newModules.iterator().next()).getDescription().getName()
                                                       ));

                INSTANCE.firePropertyChange (null, null, null);
            }
        }
        finally {
        }

        if ( !newModules.isEmpty () || writeRegistry ) {
            miSupport.writeRegistry();
        }

        // Now is the time to install all test modules

        ClassLoaderSupport.resetLoader();
        try {
            //addAllTestModules();

            if ( !miSupport.get( miSupport.TEST_MODULE ).isEmpty() ) {

                Collection testModules = resolveOrdering (
                                             miSupport.get( miSupport.TEST_MODULE ), miSupport.ENABLED_MODULE );

                if ( testModules.isEmpty() ) {
                    //System.out.println(" can't reorder modules " ); // NOI18N
                    throw new IOException ();
                }

                Iterator it = testModules.iterator();
                while( it.hasNext() ) {
                    ModuleItem mi = (ModuleItem)it.next();
                    mi.restoreSection ();
                    mi.restoreDefault ();
                    mi.restoreCode();
                    //doAddModule( ((ModuleItem)it.next() ) );
                }
            }
        }
        catch ( IOException e ) {
            TopManager.getDefault().notifyException( e );
        }

        TopManager.getDefault().setStatusText( "" ); // NOI18N
        INSTANCE.firePropertyChange (null, null, null);

    }

    /** Instructs all modules to exit.
    * @return false if the exit should not continue
    */ 
    public static boolean exit () {

        Collection enabledModules = miSupport.get( miSupport.ENABLED_MODULE );

        Iterator it = enabledModules.iterator ();
        while (it.hasNext ()) {
            ModuleItem mi = (ModuleItem)it.next ();
            if (!mi.closing ()) {
                return false;
            }
        }

        try {
            externalizeModules ();
        } catch (IOException ex) {
            TopManager.getDefault ().notifyException (ex);
        }

        // No of the modules voted against exit notify it to modules
        it = enabledModules.iterator ();
        while (it.hasNext ()) {
            ModuleItem mi = (ModuleItem)it.next ();
            mi.closeCode ();
        }

        return true;
    }

    /** Installs module from file */
    public synchronized static ModuleItem installFromFile (File file) throws IOException {

        int base = ModuleItem.BASE_JAR;

        if ( file.getParentFile().equals( miSupport.getCentralModuleDirectory() ) ) {
            base = ModuleItem.BASE_CENTRAL;
        }
        else if ( file.getParentFile().equals( miSupport.getUserModuleDirectory() ) ) {
            base = ModuleItem.BASE_USER;
        }

        ModuleItem mi = null;

        if ( base == ModuleItem.BASE_JAR )
            mi = new ModuleItem( base, file.getPath(), true );
        else
            mi = new ModuleItem( base, file.getName(), true );

        ModuleItem exMi = miSupport.get( mi.getDescription().getCodeNameBase() );

        if ( exMi != null ) {
            switch ( miSupport.getKind( exMi ) ) {
            case ModuleInstallerSupport.ENABLED_MODULE:
            case ModuleInstallerSupport.DISABLED_MODULE:
            case ModuleInstallerSupport.TEST_MODULE:
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                                          bundle.getString( "MSG_Already_Installed" ) );
                TopManager.getDefault().notify( nd );
                return null;
                /*
                case ModuleInstallerSupport.DELETED_MODULE:
                  miSupport.remove( exMi, miSupport.DELETED_MODULE );
                */
            }

        }

        if (resolveOrdering ( Collections.nCopies (1, mi),
                              miSupport.ENABLED_MODULE | miSupport.DISABLED_MODULE ).isEmpty () ) {

            NotifyDescriptor.Message nd = new NotifyDescriptor.Message(
                                              bundle.getString( "MSG_Module_Missed" ) + "\n" +
                                              getMissed( miSupport.get( miSupport.ENABLED_MODULE | miSupport.DISABLED_MODULE ), Collections.nCopies (1, mi) ) );
            TopManager.getDefault().notify( nd );
            return null;
            //throw new IOException ();
        }

        Collection enModules = checkDependenciesOnDisabled ( mi.getDescription() );

        if ( enModules != null ) {
            Iterator it = enModules.iterator();
            while( it.hasNext() ) {
                ((ModuleItem)it.next() ).setEnabled( true );
            }
        }
        else {
            return null;
        }

        doAddModule( mi );
        miSupport.add( mi, miSupport.ENABLED_MODULE );

        INSTANCE.firePropertyChange (null, null, null);
        miSupport.writeRegistry();

        return mi;
    }


    static String getMissed( int kind, Collection installed ) {
        return getMissed( miSupport.get( kind ), installed );
    }

    private static String getMissed( Collection restored, Collection installed ) {
        // List of messages for modules which are *not* to be installed.
        StringBuffer sb = new StringBuffer();
        List missed = new ArrayList (); // List<String>
        // Set of modules which will actually be installed.
        Set actual = new HashSet (); // Set<ModuleDescription>
        actual.addAll (installed);
        int misscount;              // this time around
        do {
            misscount = 0;
            Iterator it = actual.iterator ();
            while (it.hasNext ()) {
                ModuleDescription test = ((ModuleItem) it.next ()).getDescription();
                Set whatCanIStillUse = new HashSet (); // Set<ModuleDescription>

                //whatCanIStillUse.addAll (restored);
                Iterator it2 = restored.iterator();
                while (it2.hasNext ())
                    whatCanIStillUse.add( ((ModuleItem) it2.next ()).getDescription() );
                // whatCanIStillUse.addAll (actual);
                it2 = actual.iterator();
                while (it2.hasNext ())
                    whatCanIStillUse.add( ((ModuleItem) it2.next ()).getDescription() );

                try {
                    String miss = test.reasonWhyUnsatisfied ((ModuleDescription[]) whatCanIStillUse.toArray (new ModuleDescription[0]));
                    if (miss != null) {
                        misscount++;
                        it.remove ();
                        missed.add (miss);
                        sb.append( miss ).append( "\n" ); // NOI18N
                    }
                }
                catch ( org.openide.modules.IllegalModuleException e ) {}
            }
        } while (misscount > 0);

        if (missed.size () > 0) {
            return sb.toString();
            /*
            System.out.println( missed );
            return java.text.MessageFormat.format (bundle.getString ("MSG_Some_Missed") + "\n", 
              new Object[] { (String[]) missed.toArray (new String[missed.size()] ) } );
            */
        }

        return null;
    }

    /** Deletes a module with given by ModuleItem parameter.
    * @return true if the module was removed, false if it was not installed
    */
    public synchronized static boolean deleteModule( ModuleItem item )  {

        if (item != null ) {

            int kind = miSupport.getKind( item );

            if ( kind != miSupport.ENABLED_MODULE &&
                    kind != miSupport.DISABLED_MODULE &&
                    kind != miSupport.TEST_MODULE )
                return false;


            if ( item.canDestroy() ) {
                doRemoveModule (item);
                miSupport.remove( item, kind );
                // item.setDeleted( true );
                // miSupport.add( item, miSupport.DELETED_MODULE );
                INSTANCE.firePropertyChange (null, null, null);
                miSupport.writeRegistry();
            }
            else {
                item.setEnabled( false );
            }

            return true;
        } else {
            return false;
        }
    }

    /** Changes the enabled status
    */
    static synchronized void changeEnabled (ModuleItem item) {

        if (item.isEnabled ()) {
            doAddModule (item);
        } else {
            doRemoveModule (item);
        }

        miSupport.writeRegistry();
    }

    /** Checks for dependencies on disabled modules. If the module depends on
    * some disabled modules then the method returns collection of modules which 
    * should be enabled. If there are some other unsatisfied dependencies it returns 
    * null. If there are no problems it returns empty Collection.
    */
    static Collection checkDependenciesOnDisabled( ModuleDescription md ) {
        return askAndOrderToEnable( miSupport.checkDependenciesOnDisabled( md ) );
    }

    static Collection checkDependenciesOnDisabled( Collection moduleItems ) {
        return askAndOrderToEnable( miSupport.checkDependenciesOnDisabled( moduleItems ) );
    }

    // PRIVATE METHODS -----------------------------------------------------------

    /** Initializes ModuleClassLoader with the given set of modules
    */
    private static void initializeClassLoader( Collection modules ) {
        // Initialize module patches:
        List patches = new LinkedList (); // List<URL>
        FilenameFilter filter = new FilenameFilter () {
                                    public boolean accept (File dir, String name) {
                                        return name.toLowerCase ().endsWith ("jar") || name.toLowerCase ().endsWith ("zip"); // NOI18N
                                    }
                                };
        boolean debug = Boolean.getBoolean ("netbeans.debug.exceptions"); // NOI18N
        try {
            String nbUser = System.getProperty ("netbeans.user");
            if (nbUser != null) {
                File[] files = new File (nbUser, "modules" + File.separatorChar + "patches").listFiles (filter); // NOI18N
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        patches.add (files[i].toURL ());
                        if (debug) System.err.println("Modules patch: " + files[i]);
                    }
                }
            }
            String nbHome = System.getProperty ("netbeans.home");
            if (nbHome != null && ! nbHome.equals (nbUser)) {
                File[] files = new File (nbHome, "modules" + File.separatorChar + "patches").listFiles (filter); // NOI18N
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        patches.add (files[i].toURL ());
                        if (debug) System.err.println("Modules patch: " + files[i]);
                    }
                }
            }
        } catch (MalformedURLException mfurle) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                mfurle.printStackTrace ();
        }

        URL[] names = new URL[patches.size () + modules.size ()];
        patches.toArray (names);
        Iterator it = modules.iterator ();
        int i = patches.size ();
        while (it.hasNext ()) {
            ModuleItem mi = (ModuleItem)it.next ();
            names[i++] = mi.getLoaderURL ();
        }
        ModuleClassLoader.initialize (names);

    }

    /** Installs the IDE Manifest
    */
    private static void installIDEManifest() {
        ModuleDescription ideDescr;

        try {
            ideDescr = new ModuleDescription(MF_INITIALIZER,
                                             new Manifest(ModuleInstaller.class.getResourceAsStream(MF_INITIALIZER))
                                            );
        } catch (Exception ex) {
            ex.printStackTrace ();
            throw new InternalError("Cannot install initializer manifest: " + ex); // NOI18N
        }
        ideDescr.forEachSection (ModuleItem.RESTORE);
    }


    /** Get a brief localized string describing a ModuleItem.
    */ 
    private static String moduleItemDescr (ModuleItem item) {

        String spec = item.getDescription ().getSpecVersion ();
        String impl = item.getDescription ().getImplVersion ();

        java.text.MessageFormat mf = new java.text.MessageFormat(Main.getString ("INFO_ModuleSummary"));


        return mf.format (new Object[] {
                              item.getDescription ().getCodeName (),
                              spec == null ? Main.getString ("INFO_NullSpec") : spec,
                              impl == null ? Main.getString ("INFO_NullImpl") : impl
                          });

        /*
        return Main.getString ("INFO_ModuleSummary",
                               item.getDescription ().getCodeName (),
                               spec == null ? Main.getString ("INFO_NullSpec") : spec);
        */
    }

    /** Get a brief localized string describing a list of ModuleItem's. */

    private static String moduleItemsDescr (Collection items) {

        StringBuffer buf = new StringBuffer ();
        Iterator it = items.iterator ();
        boolean first = true;
        while (it.hasNext ()) {
            if (first)
                first = false;
            else
                buf.append (Main.getString ("INFO_ModuleSummarySeparator"));
            buf.append (moduleItemDescr ((ModuleItem) it.next ()));
        }
        return buf.toString ();

    }

    /** @exception if something fails
    */
    private static void doAddModule (ModuleItem item) {

        ModuleClassLoader.add (item.getLoaderURL ());

        TopManager.getDefault ().setStatusText (Main.getString ("MSG_Install_Section", item.getDescription ().getName ()));
        item.installSection ();
        item.restoreDefault ();
        TopManager.getDefault ().setStatusText (Main.getString ("MSG_Install_Module", item.getDescription ().getName ()));
        item.installCode ();
        TopManager.getDefault ().setStatusText (Main.getString ("MSG_Install_Complete", new Integer (1), item.getDescription ().getName ()));

        System.err.println (Main.getString ("INFO_ModuleInstalled", moduleItemDescr (item)));

    }

    /** Removes a module.
    */

    private static void doRemoveModule (ModuleItem item) {
        TopManager.getDefault ().setStatusText (Main.getString ("MSG_Uninstall_Module", item.getDescription ().getName ()));
        item.uninstallCode ();
        TopManager.getDefault ().setStatusText (Main.getString ("MSG_Uninstall_Section", item.getDescription ().getName ()));
        item.uninstallSection ();
        item.unrestoreDefault ();

        ModuleClassLoader.remove (item.getLoaderURL ());

        TopManager.getDefault ().setStatusText (Main.getString ("MSG_Uninstall_Complete", new Integer (1), item.getDescription ().getName ()));

        System.err.println (Main.getString ("INFO_ModuleUninstalled", moduleItemDescr (item)));
    }

    /** Externalize modules.
    */
    private static void externalizeModules () throws IOException {

        ObjectOutputStream os = new ObjectOutputStream (
                                    new BufferedOutputStream (
                                        new FileOutputStream (
                                            new File ( miSupport.getUserModuleDirectory(), EXTERNALIZED_MODULES)))
                                );

        Iterator it = miSupport.get( miSupport.ENABLED_MODULE ).iterator();

        while (it.hasNext ()) {
            ModuleItem mi = (ModuleItem)it.next ();

            try {
                NbObjectOutputStream.writeSafely (os, mi.getDescription ().getModule ());
            }
            catch (SafeException ex) {
                if (System.getProperty ("netbeans.debug.exceptions") != null) {
                    ex.getException ().printStackTrace();
                }
            }
        }
        NbObjectOutputStream.writeSafely (os, null);

        NbObjectOutputStream.writeSafely (os, TopManager.getDefault ().getLoaderPool ());

        os.close ();
    }

    /** Deexternalize modules.
    */
    private static void deExternalizeModules () throws IOException {

        File f = new File ( miSupport.getUserModuleDirectory(), EXTERNALIZED_MODULES);
        if (!f.exists ()) {
            return;
        }

        ObjectInputStream is = new ObjectInputStream (
                                   new BufferedInputStream (new FileInputStream (f))
                               );

        for (;;) {
            try {
                if (NbObjectInputStream.readSafely (is) == null) {
                    break;
                };
            } catch (SafeException ex) {
                if (System.getProperty ("netbeans.debug.exceptions") != null) {
                    ex.getException ().printStackTrace();
                }
            }
        }

        try {
            // TopManager.getDefault ().getLoaderPool ()
            NbObjectInputStream.readSafely (is);
        } catch (EOFException ex) {
            // this object has not been saved before => ignore
        } catch (SafeException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                ex.getException ().printStackTrace();
            }
        }

        is.close ();
    }


    static Collection resolveOrdering( Collection in, int kind ) {
        return resolveOrdering( in, kind, false );
    }

    static Collection resolveOrdering( Collection in, int kind, boolean containsIn ) {

        Collection installed = miSupport.get( kind );

        if ( containsIn ) {
            installed.removeAll( in );
        }

        try {
            Collection result = resolveOrdering( in, installed );
            return result;
        }
        catch ( IllegalModuleException e ) {
            TopManager.getDefault().notifyException( e );
            return new ArrayList();
        }
    }


    /** Reordres module to folow the dependencies.
    *
    * @param in collection of ModuleItems to install
    * @return list of module items
    */
    private static List resolveOrdering (Collection in) throws IllegalModuleException {
        return resolveOrdering (in, new ArrayList() );
    }


    /** Reordres module to folow the dependencies.
    *
    * @param in collection of ModuleItems to install
    * @param current current modules
    * @return list of module items
    * @exception IllegalModuleException if the modules are not ok.
    */
    private static List resolveOrdering (Collection in, Collection current)
    throws IllegalModuleException {
        HashMap map = new HashMap (in.size ()); // Map (ModuleDescription, ModuleItem)
        Iterator it = in.iterator ();
        while (it.hasNext ()) {
            ModuleItem mi = (ModuleItem)it.next ();
            map.put (mi.getDescription (), mi);
        }

        List descr;

        if (current == null) {
            descr = ModuleDescription.resolveOrdering (map.keySet ());
        } else {
            it = current.iterator ();
            HashSet s = new HashSet (); // set of descriptions
            while (it.hasNext ()) {
                ModuleItem mi = (ModuleItem)it.next ();
                s.add (mi.getDescription ());
            }
            descr = ModuleDescription.resolveOrderingForRealInstall (s, map.keySet ());
        }

        List out = new ArrayList (in.size ()); // List (ModuleItem)

        it = descr.iterator ();
        while (it.hasNext ()) {
            out.add (map.get (it.next ()));
        }
        return out;
    }


    private static Collection askAndOrderToEnable( Collection modules ) {

        if ( modules == null ) {
            return null;
        }

        if ( modules.size() == 0 )
            return modules;

        StringBuffer sb = new StringBuffer( 200 );

        sb.append( bundle.getString( "MSG_EnableOthers" ) ).append( "\n" );

        Iterator it = modules.iterator();
        while( it.hasNext() ) {
            sb.append( ((ModuleItem)it.next()).getDescription().getCodeNameBase() );
            sb.append( "\n" ); // NOI18N
        }
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation( sb.toString(), NotifyDescriptor.OK_CANCEL_OPTION );
        TopManager.getDefault().notify( nd );
        if ( nd.getValue() == DialogDescriptor.CANCEL_OPTION )
            return null;
        else {
            Collection orderedModules = null;

            // Order the modules to enable
            try {
                orderedModules = resolveOrdering(
                                     modules,
                                     miSupport.get( ModuleInstallerSupport.ENABLED_MODULE ) );
                return orderedModules;
            }
            catch ( IllegalModuleException e ) {
                TopManager.getDefault().notifyException( e );
                return null;
            }
        }
    }

    // ---------------- Methods for modules testing

    /** Installs new module for testing */
    static synchronized void installTestModule( ModuleItem mi ) throws IOException {

        boolean needToRestart = false;

        // Check wether the module already exists and is enabled
        ModuleItem exMi = miSupport.get( mi.getDescription().getCodeNameBase() );
        if ( exMi != null ) {
            switch ( miSupport.getKind(exMi) ) {
            case ModuleInstallerSupport.ENABLED_MODULE:
            case ModuleInstallerSupport.DISABLED_MODULE:
                NotifyDescriptor nc = new NotifyDescriptor.Confirmation(
                                          bundle.getString( "MSG_TestModuleExists" ), DialogDescriptor.YES_NO_OPTION );
                TopManager.getDefault().notify( nc );
                if ( nc.getValue() == DialogDescriptor.NO_OPTION ) {
                    return;
                }
                else {
                    doRemoveModule ( exMi );
                    miSupport.remove( exMi, miSupport.getKind(exMi) );
                    INSTANCE.firePropertyChange (null, null, null);
                    miSupport.writeRegistry();
                    needToRestart = true;
                }
                break;
            case ModuleInstallerSupport.TEST_MODULE:
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                                          bundle.getString( "MSG_Test_Already_Installed" ) );
                TopManager.getDefault().notify( nd );
                return;
            }
        }

        // CheckDependencies
        if ( resolveOrdering (Collections.nCopies (1, mi),
                              miSupport.ENABLED_MODULE | miSupport.TEST_MODULE ).isEmpty ()) {
            NotifyDescriptor.Message nd = new NotifyDescriptor.Message(
                                              bundle.getString( "MSG_Module_Missed" ) + "\n" +
                                              getMissed( miSupport.get( miSupport.ENABLED_MODULE | miSupport.DISABLED_MODULE ), Collections.nCopies (1, mi) ) );
            TopManager.getDefault().notify( nd );
            return;
            //throw new IOException ();
        }

        // Check disabled dependencies
        Collection enModules = checkDependenciesOnDisabled ( mi.getDescription() );

        if ( enModules != null ) {
            Iterator it = enModules.iterator();
            while( it.hasNext() ) {
                ((ModuleItem)it.next() ).setEnabled( true );
            }
        }
        else {
            return;
        }


        removeAllTestModules();
        ClassLoaderSupport.resetLoader ();
        miSupport.add( mi, miSupport.TEST_MODULE );
        addAllTestModules();
        //doAddModule( mi );
        //miSupport.add( mi, miSupport.TEST_MODULE );

        INSTANCE.firePropertyChange (null, null, null);
        miSupport.writeRegistry();

        if ( needToRestart ) {
            NotifyDescriptor nd = new NotifyDescriptor.Message( bundle.getString( "MSG_Need_To_Restart" ) );
            TopManager.getDefault().notify( nd );
        }

        return;
    }

    /** Removes all test modules */
    private static void removeAllTestModules() throws IOException {

        if ( miSupport.get( miSupport.TEST_MODULE ).isEmpty() )
            return;

        Collection testModules = resolveOrdering (
                                     miSupport.get( miSupport.TEST_MODULE ), miSupport.ENABLED_MODULE );

        if ( testModules.isEmpty() ) {
            throw new IOException ( "Can't reorder modules" ); //NOI18N
        }

        Iterator it = testModules.iterator();
        while( it.hasNext() ) {
            TestModuleItem tmi = ((TestModuleItem)it.next() );
            doRemoveModule( tmi );
            tmi.resetDescription();
        }
    }

    /** Adds all test modules */
    private static void addAllTestModules() throws IOException {
        if ( miSupport.get( miSupport.TEST_MODULE ).isEmpty() )
            return;

        Collection testModules = resolveOrdering (
                                     miSupport.get( miSupport.TEST_MODULE ), miSupport.ENABLED_MODULE );

        if ( testModules.isEmpty() ) {
            throw new IOException ( "can't reorder modules" ); //NOI18N
        }

        Iterator it = testModules.iterator();
        while( it.hasNext() ) {
            doAddModule( ((ModuleItem)it.next() ) );
        }
        TopManager.getDefault().setStatusText( "" ); // NOI18N
    }


    /** Reinstalls all test modules */
    static synchronized void reinstallTestModules() throws IOException {

        removeAllTestModules();
        ClassLoaderSupport.resetLoader ();
        addAllTestModules();

        INSTANCE.firePropertyChange (null, null, null);
    }

    /*
    static synchronized boolean removeTestModule( ModuleItem mi ) {
     
      ClassLoaderSupport.resetLoader ();  
      ModuleItem item = findModule ( mi.getDescription().getCodeNameBase() );

      if (item != null && modules.remove (item)) {
        doRemoveModule (item);
        
        INSTANCE.firePropertyChange (null, null, null);

        try {
          writeInstalledModules ();
        } 
        catch (IOException e) {
        }
        
        return true;
      } 
      else {
        return false;
      }
      
      return true;
}
    */

    /* Utility method gets all modules dependent on the module
    */
    static Collection getDependentModules( ModuleItem item ) {
        return miSupport.getDependentModules( item.getDescription(), miSupport.ENABLED_MODULE );
    }

}

/*
 * Log
 *  70   Gandalf   1.69        3/9/00   Petr Hrebejk    The implementation 
 *       version of installed/restored modules is now written to the console
 *  69   Gandalf   1.68        1/19/00  Petr Nejedly    Commented out debug 
 *       messages
 *  68   Gandalf   1.67        1/16/00  Jaroslav Tulach Faster startup.
 *  67   Gandalf   1.66        1/16/00  Ian Formanek    NOI18N
 *  66   Gandalf   1.65        1/15/00  Petr Hrebejk    Restoring of modules 
 *       from cetral directory in multiuser moved before opening of  MainWindow 
 *       - mk1
 *  65   Gandalf   1.64        1/13/00  Petr Hrebejk    i18n
 *  64   Gandalf   1.63        1/13/00  Jesse Glick     Modules patch printlns.
 *  63   Gandalf   1.62        1/13/00  Jesse Glick     Module patch dirs.
 *  62   Gandalf   1.61        1/7/00   Petr Hrebejk    Messages added to 
 *       enabling modules and installing from file
 *  61   Gandalf   1.60        1/6/00   Petr Hrebejk    Bugfix
 *  60   Gandalf   1.59        1/5/00   Petr Hrebejk    New module installer
 *  59   Gandalf   1.58        11/16/99 Petr Hrebejk    Autoload works now even 
 *       with a test module installed
 *  58   Gandalf   1.57        11/10/99 Petr Hrebejk    Unistalling/Disabling of
 *       module now Uninstalls/Disables all dependent modules
 *  57   Gandalf   1.56        11/9/99  Petr Hrebejk    Remove of last fix
 *  56   Gandalf   1.55        11/5/99  Petr Hrebejk    More than one module can
 *       be tested.
 *  55   Gandalf   1.54        11/1/99  Petr Hrebejk    Multiuser installation 
 *       with MultiLayer filesystem first step
 *  54   Gandalf   1.53        10/29/99 Ian Formanek    Removed obsoleted 
 *       imports
 *  53   Gandalf   1.52        10/29/99 Jesse Glick     System.out -> System.err
 *       for module info, so netbeans.log will get it.
 *  52   Gandalf   1.51        10/27/99 Petr Hrebejk    Testing of modules added
 *  51   Gandalf   1.50        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  50   Gandalf   1.49        10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  49   Gandalf   1.48        10/7/99  Petr Hrebejk    Versioning of modules in
 *       installedModules.xml
 *  48   Gandalf   1.47        10/7/99  Petr Hrebejk    
 *  47   Gandalf   1.46        10/5/99  Jaroslav Tulach Serializes differently 
 *       because it uses SharedClassObject serialization.
 *  46   Gandalf   1.45        9/30/99  Jesse Glick     Brief log messages re. 
 *       module installation and uninstallation, for tech support purposes. Also
 *       improved file chooser for New from File.
 *  45   Gandalf   1.44        9/30/99  Jaroslav Tulach DataLoader is now 
 *       serializable.
 *  44   Gandalf   1.43        8/27/99  Jesse Glick     Better error message.
 *  43   Gandalf   1.42        8/20/99  Ian Formanek    Uncommented debug 
 *       println on -Dnetbeans.manifest
 *  42   Gandalf   1.41        8/17/99  Petr Jiricka    Installs loaders before 
 *       deexternalizing modules
 *  41   Gandalf   1.40        8/10/99  Ian Formanek    removed debug printlns
 *  40   Gandalf   1.39        8/4/99   Petr Hrebejk    One more multiuser 
 *       install fix
 *  39   Gandalf   1.38        8/3/99   Petr Hrebejk    Multiuser update fix
 *  38   Gandalf   1.37        7/28/99  Jesse Glick     -Dnetbeans.manifest=/foo/bar.mf:/baz/quux.mf
 *         Can now use a colon to separate >1 manifest file, to test >1 module 
 *       at a time from Repository. Also ugly hack to make help set installation
 *       work in this case.
 *  37   Gandalf   1.36        7/28/99  Jaroslav Tulach Additional manifest & 
 *       separation of actions by modules
 *  36   Gandalf   1.35        7/9/99   Jaroslav Tulach Add module from file 
 *       works better
 *  35   Gandalf   1.34        7/2/99   Jaroslav Tulach Enabled add module from 
 *       file
 *  34   Gandalf   1.33        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  33   Gandalf   1.32        6/3/99   Jaroslav Tulach State of ModuleInstall 
 *       is stored if the object is also Externalizable.
 *  32   Gandalf   1.31        5/24/99  Jaroslav Tulach XML data object in Open 
 *       API
 *  31   Gandalf   1.30        5/16/99  Jaroslav Tulach Does not notify if 
 *       modules is not found
 *  30   Gandalf   1.29        5/13/99  Jaroslav Tulach Services.
 *  29   Gandalf   1.28        5/10/99  Jesse Glick     Module versioning--IDE 
 *       version numbers refined, made into system properties.
 *  28   Gandalf   1.27        5/7/99   Jaroslav Tulach Help.
 *  27   Gandalf   1.26        5/6/99   Jaroslav Tulach Should load also 
 *       extensions.
 *  26   Gandalf   1.25        5/4/99   Jaroslav Tulach Checks dependencies.
 *  25   Gandalf   1.24        5/4/99   Jaroslav Tulach 
 *  24   Gandalf   1.23        5/4/99   Jaroslav Tulach Relative URL for 
 *       modules.
 *  23   Gandalf   1.22        4/28/99  Jaroslav Tulach Checks for dependencies 
 *       between modules.
 *  22   Gandalf   1.21        4/28/99  Jaroslav Tulach XML storage for modules.
 *  21   Gandalf   1.20        4/19/99  Jaroslav Tulach Updating of modules  
 *  20   Gandalf   1.19        4/8/99   Ian Formanek    Changed Object.class -> 
 *       getClass ()
 *  19   Gandalf   1.18        4/7/99   Ian Formanek    Rename 
 *       Section->ManifestSection
 *  18   Gandalf   1.17        4/7/99   Ian Formanek    Fixed last change
 *  17   Gandalf   1.16        4/7/99   Ian Formanek    Updates Modules node 
 *       when modules change
 *  16   Gandalf   1.15        4/7/99   Ian Formanek    Rename 
 *       Description->ModuleDescription
 *  15   Gandalf   1.14        3/30/99  Jaroslav Tulach Form loader before Java 
 *       loaderem.
 *  14   Gandalf   1.13        3/30/99  Jaroslav Tulach 
 *  13   Gandalf   1.12        3/29/99  Ian Formanek    Removed obsoleted 
 *       imports of ButtonBar
 *  12   Gandalf   1.11        3/29/99  Ian Formanek    Modules are not loaded 
 *       into Hashtable to keep alphabetical order of installation
 *  11   Gandalf   1.10        3/26/99  Jaroslav Tulach 
 *  10   Gandalf   1.9         3/26/99  Jaroslav Tulach 
 *  9    Gandalf   1.8         3/24/99  Ian Formanek    
 *  8    Gandalf   1.7         3/10/99  Jaroslav Tulach Modules from 
 *       autoloadfolder are restored not initialized
 *  7    Gandalf   1.6         3/9/99   Jaroslav Tulach ButtonBar  
 *  6    Gandalf   1.5         2/18/99  David Simonek   initializer manifest 
 *       added
 *  5    Gandalf   1.4         1/27/99  Jaroslav Tulach 
 *  4    Gandalf   1.3         1/12/99  Jaroslav Tulach Modules are loaded by 
 *       URLClassLoader
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
