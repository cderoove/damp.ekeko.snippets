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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;

import org.w3c.dom.*;

import org.openide.loaders.XMLDataObject;
import org.openide.TopManager;
import org.openide.modules.ModuleDescription;
import org.openide.util.*;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;


/** Serves for building an UpdateCache from XML Document
 * @author  Petr Hrebejk
 */
public class Updates extends Object {

    /** The ResourceBundle */
    private static final ResourceBundle bundle = NbBundle.getBundle( Updates.class );

    /** XML Element tag names */
    private static final String TAG_MODULE_UPDATES = "module_updates"; // NOI18N
    private static final String TAG_MODULE = "module"; // NOI18N
    private static final String TAG_MODULE_GROUP = "module_group"; // NOI18N
    private static final String TAG_NOTIFICATION = "notification"; // NOI18N
    private static final String ATTR_NOTIFICATION_URL = "url"; // NOI18N

    /** The URL of the document */
    private URL xmlURL;

    /** The list of files in case of installing downloaded modules */
    private File[] files;

    /** The XML Document */
    private Document document = null;

    /** List of all modules 
     * @associates ModuleUpdate*/
    private ArrayList modules;

    /** The tree structure of Modules and Groups */
    private ModuleGroup rootGroup;

    /** All installed modules */
    static private ModuleDescription[] installedModules;

    /** Stops checking for new updates */
    private boolean checkCanceled;

    /** The update check progress panel */
    ProgressDialog progressDialog;

    /** Number of modules to check */
    private int moduleCount;

    private boolean pError = false;

    /** The timeStamp of downloaded xml */
    private Date timeStamp = null;

    /** Text of the notification if any */
    private String notificationText = null;

    /** URL of the notification if any */
    private URL notificationURL = null;

    /** Creates new Updates
     */
    Updates(URL xmlURL) {
        this.xmlURL = xmlURL;
        installedModules = Autoupdater.Support.getModuleDescriptions();
    }

    /** Create new Updates for files list - used
     * for installing downloaded modules
     */
    Updates( File[] files ) {
        this.files = files;
        installedModules = Autoupdater.Support.getModuleDescriptions();
    }

    /** Checks for updates in separate thread. Displays progress in a dialog
     */
    void checkUpdates( ProgressDialog progressDialog, final Wizard.Validator validator ) {

        this.progressDialog = progressDialog;

        pError = false;
        checkCanceled = false;

        Runnable task = new Runnable () {
                            public void run() {
                                parseDocument();
                                Settings.getShared().setLastCheck( new Date() );
                                if (!pError && document != null )
                                    buildStructures();
                                else
                                    pError = true;
                                //Settings.getShared().setLastStamp( getTimeStamp() );
                                validator.setValid( true );
                            }
                        };
        RequestProcessor.postRequest( task );

    }

    /** Builds structures for downloaded modules, the structures are onbly
     * linear 
     */
    void checkDownloadedModules() {
        modules = new ArrayList();
        rootGroup = new ModuleGroup();

        for ( int i = 0; i < files.length; i++ ) {
            ModuleUpdate update = new ModuleUpdate( files[i] );

            if ( update.createFromDistribution() ) {

                if ( update.isUpdateAvailable() ) {
                    modules.add( update );
                    rootGroup.addItem( update );
                }
            }
        }
    }

    /** Calls static parsing method in XMLDataObject to parse the
     * document
     */ 
    private void parseDocument() {

        if ( checkCanceled )
            return;

        try {
            document = XMLDataObject.parse( xmlURL, new ErrorCatcher() );
        }
        catch ( org.xml.sax.SAXException e ) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) { // NOI18N
                System.out.println("URL : " + xmlURL ); // NOI18N
                e.printStackTrace ();
            }
            pError = true;
            //TopManager.getDefault().notifyException( e );
        }
        catch ( java.io.IOException e ) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) { // NOI18N
                System.out.println("URL : " + xmlURL ); // NOI18N
                e.printStackTrace ();
            }
            pError = true;
            // TopManager.getDefault().notifyException( e );
        }
    }

    /** Builds the linear and the tree structure of module updates.
     */
    private void buildStructures() {

        if ( checkCanceled )
            return;

        if ( document.getDocumentElement() == null ) {
            // System.out.println( "WARNING <MODULE> is not element tag" ); // NOI18N
        }
        else {
            modules = new ArrayList();
            rootGroup = new ModuleGroup();

            NodeList allModules = document.getElementsByTagName( TAG_MODULE );
            moduleCount = allModules.getLength();
            progressDialog.setGaugeBounds( ProgressDialog.OVERALL_GAUGE, 0, moduleCount );
            progressDialog.setGaugeValue( ProgressDialog.OVERALL_GAUGE, 0 );
            progressDialog.setLabelText( ProgressDialog.OVERALL_LABEL, "" ); // NOI18N

            processElement( document.getDocumentElement(), rootGroup );

            // Try to read timestamp
            Node attr = document.getDocumentElement().getAttributes().getNamedItem( "timestamp" ); // NOI18N
            if ( attr != null ) {
                String timeString = attr.getNodeValue();
                SimpleDateFormat formatter = new SimpleDateFormat( "ss/mm/HH/dd/MM/yyyy" ); // NOI18N
                ParsePosition pos = new ParsePosition(0);
                timeStamp = formatter.parse(timeString, pos);
            }
        }
    }

    /** Finds module and module_group elements in the node's children and
     * process them
     *@param element The DOM Element node to be read.
     */
    private void processElement( Element element, ModuleGroup moduleGroup ) {

        NodeList nodeList = element.getChildNodes();
        for( int i = 0; i < nodeList.getLength(); i++ ) {

            if ( checkCanceled )
                return;

            Node node = nodeList.item( i );

            if ( node.getNodeType() != Node.ELEMENT_NODE ) {
                continue;
            }

            if ( ((Element)node).getTagName().equals( TAG_MODULE ) ) {
                ModuleUpdate update = new ModuleUpdate( xmlURL, node, document.getDocumentElement() );

                if ( update.readModuleUpdate() ) {
                    int currentModule = progressDialog.getGaugeValue( ProgressDialog.OVERALL_GAUGE);

                    progressDialog.setLabelText( ProgressDialog.OVERALL_LABEL,
                                                 update.getName() + " [" + (currentModule + 1) + "/" + moduleCount + "]" ); // NOI18N
                    progressDialog.setGaugeValue( ProgressDialog.OVERALL_GAUGE, currentModule + 1 );

                    if ( update.isUpdateAvailable() ) {
                        modules.add( update );
                        moduleGroup.addItem( update );
                    }
                }
            }
            else if ( ((Element)node).getTagName().equals( TAG_MODULE_GROUP ) ) {
                ModuleGroup group = new ModuleGroup( node );
                moduleGroup.addItem( group );
                processElement( (Element)node, group );
            }
            else if ( ((Element)node).getTagName().equals( TAG_NOTIFICATION ) ) {
                readNotification( node );
            }
        }
    }

    void cancelCheck() {
        checkCanceled = true;
    }


    /** Gets the root of the module/module group tree
     * @return The group in the root of the tree.
     */
    ModuleGroup getRootGroup() {
        return rootGroup;
    }


    /** Gets the linear structure of all module updates i.e. Collection
     */
    Collection getModules() {
        return modules;
    }

    /** Gets the state of pError the file was not parsed */
    boolean isError() {
        return pError;
    }

    /** Gets array of currently installed modules */
    static ModuleDescription[] getInstalledModules() {
        return installedModules;
    }


    /** Gets Collection of patches installed in the system */
    static ModuleDescription[] getInstalledPatches() {
        return PatchChecker.getPatches();
    }

    /** Returns the time stamp of the downloaded XML file */
    Date getTimeStamp() {
        return timeStamp;
    }

    /** Returns notification text if specified otherwise null */
    String getNotificationText() {
        return notificationText;
    }

    /** Returns notification URL if specified otherwise null */
    URL getNotificationURL() {
        return notificationURL;
    }

    /** Reads the notification */
    private void readNotification( Node node ) {

        if ( getNotificationText() != null ) {
            return;
        }

        try {
            Node attr = node.getAttributes().getNamedItem( ATTR_NOTIFICATION_URL );
            String textURL = attr == null ? null : attr.getNodeValue();

            if ( textURL != null )
                notificationURL = new URL( textURL );
        }
        catch ( java.net.MalformedURLException e ) {
            // TopManager.getDefault().notifyException( e );
            // let homepage set to null
        }

        StringBuffer sb = new StringBuffer();

        NodeList innerList = node.getChildNodes();

        for( int i = 0; i < innerList.getLength(); i++ ) {
            if ( innerList.item( i ).getNodeType() == Node.TEXT_NODE )  {
                sb.append( innerList.item( i ).getNodeValue() );
            }
        }

        if ( sb.length() > 0 )
            notificationText = sb.toString();
        else
            notificationText = null;

    }


    class ErrorCatcher implements org.xml.sax.ErrorHandler {
        private void message (String level, org.xml.sax.SAXParseException e) {
            pError = true;
        }

        public void error (org.xml.sax.SAXParseException e) {
            // normally a validity error
            pError = true;
        }

        public void warning (org.xml.sax.SAXParseException e) {
            //parseFailed = true;
        }

        public void fatalError (org.xml.sax.SAXParseException e) {
            pError = true;
        }
    } //end of inner class ErrorPrinter




}
/*
 * Log
 *  17   Gandalf   1.16        2/23/00  Petr Hrebejk    Notifications added into
 *       autoupdate
 *  16   Gandalf   1.15        1/13/00  Petr Hrebejk    i18 mk3
 *  15   Gandalf   1.14        1/12/00  Petr Hrebejk    i18n mk2
 *  14   Gandalf   1.13        1/12/00  Petr Hrebejk    i18n
 *  13   Gandalf   1.12        1/10/00  Petr Hrebejk    Bug in setting last 
 *       stamp fixed
 *  12   Gandalf   1.11        1/3/00   Petr Hrebejk    Various bug fixes - 
 *       5097, 5098, 5110, 5099, 5108
 *  11   Gandalf   1.10        12/22/99 Petr Hrebejk    Various bugfixes
 *  10   Gandalf   1.9         12/20/99 Petr Hrebejk    Autocheck & security 
 *       finished
 *  9    Gandalf   1.8         12/1/99  Petr Hrebejk    Checkin signatures of 
 *       NBM files & automatic autoupdate check added
 *  8    Gandalf   1.7         11/8/99  Petr Hrebejk    Install of downloaded 
 *       modules added, Licenses in XML
 *  7    Gandalf   1.6         11/1/99  Petr Hrebejk    Remove of 
 *       org.netbeans.core.ModuleUpdater fixed
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/11/99 Petr Hrebejk    Version before Beta 5
 *  4    Gandalf   1.3         10/10/99 Petr Hrebejk    AutoUpdate made to 
 *       wizard
 *  3    Gandalf   1.2         10/8/99  Petr Hrebejk    Next development version
 *  2    Gandalf   1.1         10/7/99  Petr Hrebejk    Next development version
 *  1    Gandalf   1.0         10/7/99  Petr Hrebejk    
 * $
 */
