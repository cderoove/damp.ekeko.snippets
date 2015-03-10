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
import java.io.*;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import javax.swing.JFileChooser;

import org.xml.sax.AttributeList;

import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInstall;
import org.openide.modules.ManifestSection;
import org.openide.modules.ModuleDescription;
import org.openide.modules.IllegalModuleException;
import org.openide.TopManager;
import org.openide.loaders.DataLoader;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard;

/**
* Item representing one module loaded in test mode i.e. with
* source code in repository.
*
* @author Petr Hrebejk
*/
public class TestModuleItem extends ModuleItem
    implements Node.Cookie {

    /** Last directory from which a manifest was choosen */
    private static File lastChosenDir = null;

    /** Stores the manifest file */
    private String manifestFileName;

    /** Createds new TestModuleItem for the specified manifestatic file
    */
    TestModuleItem ( String manifestFileName, boolean enabled ) {

        this.manifestFileName = manifestFileName;

        resetDescription();
    }


    /** Creates new module for JAR filesystem.
    * @param name path to the module jar
    * @param enabled enabled?
    * @exception IllegalModuleException if the jar is not module
    * @exception IOException if an error occures during reading
    */
    public TestModuleItem( Manifest manifest, String manifestFileName, boolean enabled )
    throws IllegalModuleException, IOException {
        this.manifestFileName = manifestFileName;
        init( manifest, "test.module", enabled ); // NOI18N
    }

    /** @param en true if enabled
    */
    public void setEnabled (boolean en) {
        if (enabled == en) return;

        enabled = en;
        //ModuleInstaller.changeEnabled (this);
    }

    /** @return URL to be used by class loaders */
    public URL getLoaderURL () {
        return null;
    }

    /** Test module is allways deletable */
    public boolean canDestroy() {
        return true;
    }

    /** Mehod called by ModuleInstaller to get the XML format of ModuleItem
     */
    String toXML() {
        StringBuffer moduleBuffer = new StringBuffer( 150 );

        // Module header
        moduleBuffer.append( ModuleTags.TAB ).append("<" ).append( ModuleTags.TEST_MODULE ); // NOI18N
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

        // URL
        moduleBuffer.append( ModuleTags.TABx2 ).append( ModuleTags.MANIFEST ).append( "=\"" ); // NOI18N
        moduleBuffer.append( manifestFileName );
        moduleBuffer.append( "\"" ); // NOI18N
        moduleBuffer.append( ModuleTags.NEW_LINE );

        // Enabled
        moduleBuffer.append( ModuleTags.TABx2 ).append( ModuleTags.ENABLED ).append( "=\"" ); // NOI18N
        moduleBuffer.append( new Boolean (isEnabled ()) ).append( "\"" ); // NOI18N
        moduleBuffer.append( ModuleTags.NEW_LINE );

        // Module footer
        moduleBuffer.append( "/>"); // NOI18N
        moduleBuffer.append( ModuleTags.NEW_LINE );

        return moduleBuffer.toString();
    }


    /** Restores the item from XML. Creates new instance of ModuleItem
     * represented by the attribute list. 
     * @return New ModuleItem or null if cannot be restored.
     */
    static TestModuleItem fromXML( AttributeList attr ) {

        String man = attr.getValue( ModuleTags.MANIFEST );
        String enabled = attr.getValue( ModuleTags.ENABLED );

        int relase;
        try {
            relase = Integer.parseInt( attr.getValue( ModuleTags.RELEASE ) );
        }
        catch ( NumberFormatException e ) {
            relase = -1;
        }

        String specVersion = attr.getValue( ModuleTags.SPECVERSION );

        if ( man != null ) {
            //try {
            TestModuleItem tmi = new TestModuleItem( man,
                                 enabled == null || Boolean.valueOf (enabled).booleanValue () );

            tmi.setOldRelease( relase );
            tmi.setOldSpecVersion( specVersion );
            return tmi;
            /*
        } catch (FileNotFoundException e) {
            // simply ignore (Ian's request)
        }
            */
            //catch (IOException e) {
            // TopManager.getDefault().notifyException(e);
            // }
        }

        return null;
    }


    /** Called from the popup menu action, the user wants to add a test module.
     * Asks user for manifest and then installs the module stored in repository.
     */
    public static void createNew() {

        final JFileChooser chooser = new JFileChooser ();
        if (lastChosenDir != null) chooser.setCurrentDirectory (lastChosenDir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setApproveButtonText (Main.getString ("CTL_ModuleSelect"));
        chooser.setApproveButtonToolTipText (Main.getString ("CTL_ModuleSelectToolTip"));
        chooser.setFileFilter (new javax.swing.filechooser.FileFilter () {
                                   public String getDescription () {
                                       return Main.getString ("CTL_TestModuleSelectFilter");
                                   }
                                   public boolean accept (File f) {
                                       return f.isDirectory () || ( f.getName().startsWith("mf-") && // NOI18N
                                                                    f.getName ().endsWith (".txt") ); // NOI18N
                                   }
                               });


        if (chooser.showOpenDialog (TopManager.getDefault().getWindowManager().getMainWindow ())
                == JFileChooser.APPROVE_OPTION) {

            TestModuleItem tmi = new TestModuleItem( chooser.getSelectedFile().getAbsolutePath(), true );

            try {
                // install the file
                ModuleInstaller.installTestModule( tmi );
            }
            catch ( IOException e ) {
                System.out.println(e );
            }
        }

        lastChosenDir = chooser.getCurrentDirectory ();
    }

    /** Resets the module description to succesfully reinstall the module
    */
    void resetDescription() {
        try {
            File manifestFile = new File( manifestFileName );
            Manifest manifest = new Manifest( new FileInputStream( manifestFile ) );
            init( manifest, manifestFile.getName(), enabled );
        }
        catch ( FileNotFoundException e ) {
            System.out.println(e);
        }
        catch ( IOException e ) {
            System.out.println(e);
        }
    }

    /** Uninstalls and again installs the ModuleItem to get new module instance to test.
     * Called from popup menu action
     */
    void reinstall() {

        try {
            ModuleInstaller.reinstallTestModules();
        }
        catch ( IOException e ) {
            TopManager.getDefault().notifyException( e );
        }
        /*
        ModuleInstaller.deleteModule( this );

        try {
          TestModuleItem tmi = new TestModuleItem( this.manifestFileName, isEnabled() );
          ModuleInstaller.installTestModule( tmi );
    }
        catch ( IOException e ) {
          System.out.println( e );
    }
        */

    }

    /** For use by the API Support module.
    * Deploys a test module, reinstalling an existing one if necessary.
    * @param manifestFileName name of the manifest to use, as a file
    */
    public static void deploy (String manifestFileName) {



        TestModuleItem tmi = new TestModuleItem (manifestFileName, true);
        if (tmi.getDescription () == null) {
            System.err.println ("Please validate your test module's manifest and try again...");
            return;
        }
        String name = tmi.getDescription ().getCodeNameBase ();


        ModuleItem exists = ModuleInstaller.getByName( name );
        if ( exists != null && exists instanceof TestModuleItem ) {
            System.err.println ("Redeploying test module...");
            tmi.reinstall();
        }
        else {
            System.err.println ("Installing a new test module...");
            try {
                ModuleInstaller.installTestModule (tmi);
            } catch (IOException e) {
                System.out.println (e);
            }
        }


        /*
        ModuleItem[] existing = ModuleInstaller.getModuleItems (
            ModuleInstallerSupport.ENABLED_MODULE | 
            ModuleInstallerSupport.DISABLED_MODULE | 
            ModuleInstallerSupport.TEST_MODULE);
        TestModuleItem exists = null;
        for (int i = 0; i < existing.length; i++) {
          if (existing[i].getDescription ().getCodeNameBase ().equals (name)) {
            if (existing[i] instanceof TestModuleItem) {
              exists = (TestModuleItem) existing[i];
              break;
            } else {
              System.err.println ("Module already existed, but not as a test module--please delete normal module and restart.");
              return;
            }
          }
    }

        if (exists != null) {
          System.err.println ("Redeploying test module...");
          ModuleInstaller.deleteModule (exists);
    } else {
          System.err.println ("Installing a new test module...");
    }
        try {
          ModuleInstaller.installTestModule (tmi);
    } catch (IOException e) {
          System.out.println (e);
    }
        */
    }

}


/*
* Log
*  5    Gandalf   1.4         1/13/00  Jaroslav Tulach I18N
*  4    Gandalf   1.3         1/5/00   Petr Hrebejk    New module installer
*  3    Gandalf   1.2         11/10/99 Jesse Glick     Added deployability for 
*       API Support.
*  2    Gandalf   1.1         10/29/99 Ian Formanek    Fixed filter name for 
*       Testing modules
*  1    Gandalf   1.0         10/27/99 Petr Hrebejk    
* $
*/

