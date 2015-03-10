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

import java.net.URL;
import java.io.File;
import java.io.BufferedInputStream;
import java.util.*;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import org.w3c.dom.*;

import org.openide.loaders.XMLDataObject;
import org.openide.modules.ModuleDescription;
import org.openide.TopManager;


/** This class represents one module update available on the web
 *
 * @author  phrebejk
 * @version 
 */
class ModuleUpdate extends Object
    implements org.openide.nodes.Node.Cookie {

    // Constants
    private static final String NO = "NO"; // NOI18N
    private static final String FALSE = "FALSE"; // NOI18N

    private static final String ATTR_HOMEPAGE = "homepage"; // NOI18N
    private static final String ATTR_MANIFEST = "manifest"; // NOI18N
    private static final String ATTR_DISTRIBUTION = "distribution"; // NOI18N
    private static final String ATTR_MANUFACTURER = "manufacturer"; // NOI18N
    private static final String ATTR_DOWNLOAD_SIZE = "downloadsize"; // NOI18N
    private static final String ATTR_UNPACKED_SIZE = "unpacksize"; // NOI18N
    private static final String ATTR_LICENCE = "licence"; // NOI18N

    private static final String ELEMENT_DESCRIPRION = "description"; // NOI18N


    //private static LicenceCache licenceCache = new LicenceCache();

    /** The base url of XML Document used to create URL from relative paths */
    private URL xmlURL;
    /** Node in the DOM the ModuleUpdate comes from */
    private Node node;
    /** Holds the document of the XML */
    private Element documentElement;

    /** Used for downloaded files */
    private File nbmFile;


    /** Holds value of property distribution. */
    private URL distribution = null;
    /** Holds value of property homepage. */
    private URL homepage = null;
    /** Holds value of property manifest. */
    private URL manifest = null;
    /** Holds value of property manufacturer. */
    private String manufacturer = null;
    /** Holds value of property downloadSize. */
    private long downloadSize = -1;
    /** Holds value of property unpackedSize. */
    private long unpackedSize = -1;
    /** Holds value of property downlaodOK. */
    private boolean downloadOK = false;
    /** Holds value of property description. */
    private String description = null;
    /** Holds value of property licence */
    private String licenceID = null;
    /** Holds value text of the licence */
    private String licenceText = null;
    /** Holds value of property selected */
    private boolean selected = false;
    /** Holds value of property secutiry */
    private int security = SignVerifier.NOT_CHECKED;
    /** Holds value of property certificates */
    private Collection certs = null;
    /** Holds value of property installApproved */
    private boolean installApproved = false;

    private boolean pError = false;

    // Associations
    private ModuleDescription localModule = null;

    private ModuleDescription remoteModule = null;

    //private Updates linearStructure;

    // CONSTRUCTORS -------------------------------------------------------------

    /** Creates new ModuleUpdate */
    ModuleUpdate( URL xmlURL, Node node, Element documentElement ) {
        this.xmlURL = xmlURL;
        this.node = node;
        this.documentElement = documentElement;
    }

    /** Creates new ModuleUpdate for downloaded .nbm file */
    ModuleUpdate( File nbmFile ) {
        this.nbmFile = nbmFile;
    }

    // METHODS ------------------------------------------------------------------

    /** Reads the module update from DOM and if the module is already loaded
     * finds the description 
     * @return True if the read-operation was O.K.
     */
    boolean readModuleUpdate( ) {

        // Read module update information

        try {
            String textURL = getAttribute( ATTR_HOMEPAGE );
            if ( textURL != null )
                homepage = new URL( xmlURL, textURL );
        }
        catch ( java.net.MalformedURLException e ) {
            TopManager.getDefault().notifyException( e );
            // let homepage set to null
        }


        setManufacturer( getAttribute( ATTR_MANUFACTURER ) );

        try {
            setDownloadSize( Long.parseLong( getAttribute( ATTR_DOWNLOAD_SIZE ) ) );
        }
        catch ( NumberFormatException e ) {
            // Let the value set to -1
        }

        try {
            setUnpackedSize( Long.parseLong( getAttribute( ATTR_UNPACKED_SIZE ) ) );
        }
        catch ( NumberFormatException e ) {
            // Let the value set to -1
        }

        setDescription( getTextOfElement( ELEMENT_DESCRIPRION ) );

        try {
            String textURL = getAttribute( ATTR_DISTRIBUTION );
            if ( textURL != null )
                distribution = new URL ( xmlURL, textURL );
        }
        catch ( java.net.MalformedURLException e ) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace ();

            //TopManager.getDefault().notifyException( e );
            // let distibution URL set to null
        }

        // Read the manifest from XML file and create
        // the module description
        try {
            Manifest mf = manifestFromXML( );
            remoteModule = new ModuleDescription( "temp", mf ); // NOI18N
        }
        catch ( org.openide.modules.IllegalModuleException e ) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace ();
            // let manifest URL set to null
        }

        // Read the licence from the XML
        licenceID = getAttribute( ATTR_LICENCE );
        licenceText = licenseFromXML( licenceID, documentElement );


        /*
        try { 
          String textURL = getAttribute( ATTR_LICENCE );
          if ( textURL != null ) {
            licenceURL = new URL( xmlURL, textURL );
            
            String cachedText = licenceCache.getLicence( licenceURL );
            if ( cachedText != null ) {
              licenceText = cachedText; // licence already loaded
            }
            else {
              BufferedInputStream bis = new BufferedInputStream( licenceURL.openStream() );
              StringBuffer sb = new StringBuffer( 2000 );

              int ch;
              while( ( ch = bis.read() ) != -1 ) {
                sb.append( (char)ch );
              }
            
              licenceText = sb.toString();
              
              licenceCache.addLicence( licenceURL, licenceText );
            }
          }
    }
        catch ( java.net.MalformedURLException e ) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) 
            e.printStackTrace ();
          //TopManager.getDefault().notifyException( e );
          licenceURL = null;
          licenceText = null;
    }
        catch ( java.io.IOException e ) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) 
            e.printStackTrace ();
          //TopManager.getDefault().notifyException( e );
          licenceURL = null;
          licenceText = null;
    }
        */

        // Try to find installed module
        ModuleDescription[] installedModules = Updates.getInstalledModules();
        ModuleDescription[] installedPatches = Updates.getInstalledPatches();

        if ( remoteModule != null ) {

            // Try if the module describes the Core IDE
            if ( remoteModule.getCodeName().equals( IdeDescription.getName() ) )  {
                localModule = IdeDescription.getIdeDescription();
            }

            // Try other modules
            for ( int i = 0; localModule == null && i < installedModules.length; i++ ) {
                if ( installedModules[i].getCodeNameBase().equals( remoteModule.getCodeNameBase() ) ) {
                    localModule = installedModules[i];
                    break;
                }
            }

            // Try wether the module is a installed patch
            for ( int i = 0; localModule == null && i < installedPatches.length; i++ ) {
                if ( installedPatches[i].getCodeNameBase().equals( remoteModule.getCodeNameBase() ) ) {
                    localModule = installedPatches[i];
                    break;
                }
            }

        }

        return remoteModule != null;
    }



    /** Creates module from downloaded .nbm file */
    boolean createFromDistribution() {

        Document document = null;
        URL infoURL = null;

        // Get the URL of the info file
        try {
            infoURL = new URL ( "jar:" + nbmFile.toURL().toString() + "!/Info/info.xml" ); // NOI18N
        }
        catch ( java.net.MalformedURLException e ) {
            System.out.println( e );
        }

        // Try to parse the info file
        try {
            document = XMLDataObject.parse( infoURL, new ErrorCatcher() );
            documentElement = document.getDocumentElement();
            node = documentElement;
        }
        catch ( org.xml.sax.SAXException e ) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) { // NOI18N
                System.out.println("Bad info : " + infoURL ); // NOI18N
                //e.printStackTrace ();
            }
            return false;
            //TopManager.getDefault().notifyException( e );
        }
        catch ( java.io.IOException e ) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) { // NOI18N
                System.out.println("Missing info : " + infoURL ); // NOI18N
                //e.printStackTrace ();
            }
            return false;
        }



        // Read module update information


        try {
            String textURL = getAttribute( ATTR_HOMEPAGE );
            if ( textURL != null )
                homepage = new URL( textURL );
        }
        catch ( java.net.MalformedURLException e ) {
            TopManager.getDefault().notifyException( e );
            // let homepage set to null
        }

        setManufacturer( getAttribute( ATTR_MANUFACTURER ) );

        setDownloadSize( nbmFile.length() );


        /*
        try {
          setDownloadSize( Long.parseLong( getAttribute( ATTR_DOWNLOAD_SIZE ) ) );
    }
        catch ( NumberFormatException e ) {
          // Let the value set to -1
    }

        try {
          setUnpackedSize( Long.parseLong( getAttribute( ATTR_UNPACKED_SIZE ) ) );
    }
        catch ( NumberFormatException e ) {
          // Let the value set to -1
    }
        */

        setDescription( getTextOfElement( ELEMENT_DESCRIPRION ) );

        // Read the manifest from XML file and create
        // the module description
        // Get the manifest file for the module

        try {
            Manifest mf = manifestFromXML( );
            remoteModule = new ModuleDescription( "temp", mf ); // NOI18N
        }
        catch ( org.openide.modules.IllegalModuleException e ) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace ();
            // let manifest URL set to null
        }

        // Read the licence from the XML
        licenceID = getAttribute( ATTR_LICENCE );
        licenceText = licenseFromXML( licenceID, documentElement );


        // Try to find installed module
        ModuleDescription[] installedModules = Updates.getInstalledModules();
        ModuleDescription[] installedPatches = Updates.getInstalledPatches();

        if ( remoteModule != null ) {

            // Try if the module describes the Core IDE
            if ( remoteModule.getCodeName().equals( IdeDescription.getName() ) )  {
                localModule = IdeDescription.getIdeDescription();
            }

            // Try other modules
            for ( int i = 0; localModule == null && i < installedModules.length; i++ ) {
                if ( installedModules[i].getCodeNameBase().equals( remoteModule.getCodeNameBase() ) ) {
                    localModule = installedModules[i];
                    break;
                }
            }

            // Try wether the module is a installed patch
            for ( int i = 0; localModule == null && i < installedPatches.length; i++ ) {
                if ( installedPatches[i].getCodeNameBase().equals( remoteModule.getCodeNameBase() ) ) {
                    localModule = installedPatches[i];
                    break;
                }
            }

        }

        return remoteModule != null;

    }

    /** Finds the module in the Colloection of installed modules */
    void resolveInstalledModule( Collection installedModules ) {

    }

    // GETTERS AND SETTERS ------------------------------------------------------

    /** Getter for property codeNameBase.
     *@return Value of property codeNameBase.
     */
    String getCodeNameBase() {
        return remoteModule.getCodeNameBase();
    }

    /** Getter for property name.
     *@return Value of property name.
     */
    String getName() {
        return remoteModule.getName();
    }

    /** Getter for property distribution.
     *@return Value of property distribution.
     */
    URL getDistribution() {
        return distribution;
    }

    /** Getter for property distributionFilename.
     *@return Name of the distribution file.
     */
    String getDistributionFilename() {
        if ( nbmFile != null ) {
            return nbmFile.getName();
        }
        else {
            return new File( getDistribution().getFile() ).getName();
        }
    }

    /** Getter for property licenceID.
     *@return Value of property licenceID.
     */
    String getLicenceID() {
        return licenceID;
    }

    /** Getter for property manufacturer.
     *@return Value of property manufacturer.
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /** Setter for property manufacturer.
     *@param manufacturer New value of property manufacturer.
     */
    void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /** Getter for property downloadSize.
     *@return Value of property downloadSize.
     */
    long getDownloadSize() {
        return downloadSize;
    }

    /** Setter for property downloadSize.
     *@param downloadSize New value of property downloadSize.
     */
    void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    /** Getter for property unpackedSize.
     *@return Value of property unpackedSize.
     */
    long getUnpackedSize() {
        return unpackedSize;
    }

    /** Setter for property unpackedSize.
     *@param unpackedSize New value of property unpackedSize.
     */
    void setUnpackedSize(long unpackedSize) {
        this.unpackedSize = unpackedSize;
    }

    /** Getter for property description.
     *@return Value of property description.
     */
    String getDescription() {
        return description;
    }

    /** Setter for property description.
     *@param description New value of property description.
     */
    void setDescription(String description) {
        this.description = description;
    }

    /** Getter for property selected.
     *@return Value of property selected.
     */
    boolean isSelected() {
        return selected;
    }

    /** Setter for property selected.
     *@param description New value of property selected.
     */
    void setSelected( boolean selected ) {
        this.selected = selected;
    }


    /** Getter for property new.
     *@return True if such module is not installed in netbeans
     */
    boolean isNew() {
        return localModule == null;
    }

    /** Getter for property homePage.
     *@return Value of property homePage.
     */
    URL getHomePage() {
        return homepage;
    }

    /** Getter for property licenceText.
     *@return Value of property licenceText.
     */
    String getLicenceText() {
        return licenceText;
    }

    /** Getter for property remoteModule.
     *@return Value of property remoteModule.
     */
    ModuleDescription getRemoteModule() {
        return remoteModule;
    }

    /** Getter for property localModule.
     *@return Value of property localModule.
     */
    ModuleDescription getLocalModule() {
        return localModule;
    }

    /** Tests if there is an update available */
    boolean isUpdateAvailable() {
        if ( getLocalModule() == null )
            return true;

        if ( getRemoteModule().getCodeNameRelease() > getLocalModule().getCodeNameRelease() ) {
            return true;
        }

        try {
            if ( !ModuleDescription.compatibleWith( getRemoteModule().getSpecVersion(), getLocalModule().getSpecVersion() ) ) {
                return true;
            }
        }
        catch ( org.openide.modules.IllegalModuleException e ) {
            TopManager.getDefault().notifyException( e );
        }

        return false;

    }

    /** Getter for property downloadOK.
     *@return Value of property downloadOK.
     */
    boolean isDownloadOK() {
        return downloadOK;
    }

    /** Setter for property downloadOK
     *@param downloadOK New value of property downloadOK.
     */
    void setDownloadOK( boolean downloadOK ) {
        this.downloadOK = downloadOK;
    }


    /** Getter for property security.
     *@return Value of property security.
     */
    int getSecurity() {
        return security;
    }

    /** Setter for property security
     *@param downloadOK New value of property security.
     */
    void setSecurity( int security ) {
        this.security = security;
    }

    /** Getter for property certificates.
     *@return Value of property certificates.
     */
    Collection getCerts() {
        return certs;
    }

    /** Setter for property certificates
     *@param downloadOK New value of property certificates.
     */
    void setCerts( Collection certs ) {
        this.certs = certs;
    }


    /** Getter for property nbmFile.
     *@return The manually downloaded nbm file.
     */
    File getDistributionFile() {
        return nbmFile;
    }

    /** Getter for property installApproved.
     *@return Value of property installApproved.
     */
    boolean isInstallApproved() {
        return installApproved;
    }

    /** Setter for property installApproved.
     *@param description New value of property installApproved.
     */
    void setInstallApproved( boolean installApproved ) {
        this.installApproved = installApproved;
    }

    // UTILITY METHODS ----------------------------------------------------------

    /** Utility method gets the atribute of node
     *@param attribute Name of the desired attribute
     */
    private String getAttribute(String attribute) {
        Node attr = node.getAttributes().getNamedItem( attribute );
        return attr == null ? null : attr.getNodeValue();
    }

    /** Utility method gets text of subelement. Used for getting
     * description text.
     *@param name Name of the desired subelement
     */
    private String getTextOfElement( String name ) {

        if ( node.getNodeType() != Node.ELEMENT_NODE ||
                !( node instanceof Element ) ) {
            return null;
        }

        NodeList nodeList = ((Element)node).getElementsByTagName( name );
        StringBuffer sb = new StringBuffer();

        for( int i = 0; i < nodeList.getLength(); i++ ) {

            if ( nodeList.item( i ).getNodeType() != Node.ELEMENT_NODE ||
                    !( nodeList.item( i ) instanceof Element ) ) {
                break;
            }

            // ((Element)nodeList.item( i )).normalize();
            NodeList innerList = nodeList.item( i ).getChildNodes();

            for( int j = 0; j < innerList.getLength(); j++ ) {
                if ( innerList.item( j ).getNodeType() == Node.TEXT_NODE )  {
                    sb.append( innerList.item( j ).getNodeValue() );
                }
            }
        }
        return sb.toString();
    }


    /** Utility methods finds the manifest tag, reads all it's attributes and
     * creates the manifest */
    private Manifest manifestFromXML() {
        if ( node.getNodeType() != Node.ELEMENT_NODE ||
                !( node instanceof Element ) ) {
            return null;
        }

        NodeList nodeList = ((Element)node).getElementsByTagName( "manifest" ); // NOI18N

        for( int i = 0; i < nodeList.getLength(); i++ ) {

            if ( nodeList.item( i ).getNodeType() != Node.ELEMENT_NODE ||
                    !( nodeList.item( i ) instanceof Element ) ) {
                break;
            }

            // ((Element)nodeList.item( i )).normalize();
            NamedNodeMap attrList = nodeList.item( i ).getAttributes();
            Manifest mf = new Manifest();
            Attributes mfAttrs = mf.getMainAttributes();


            for( int j = 0; j < attrList.getLength(); j++ ) {
                Attr attr = (Attr) attrList.item( j );
                mfAttrs.put( new Attributes.Name( attr.getName() ), attr.getValue() );
            }

            return mf;
        }
        return null;
    }

    /** Gets the licence from XML file
    */
    private String licenseFromXML( String name, Element docElement ) {
        NodeList nodeList = docElement.getElementsByTagName( "license" ); // NOI18N

        for( int i = 0; i < nodeList.getLength(); i++ ) {

            Node nameAttr = nodeList.item(i).getAttributes().getNamedItem( "name" ); // NOI18N

            if ( nameAttr == null )
                continue;

            if ( nameAttr.getNodeValue().equals( name ) ) { // licence found
                StringBuffer sb = new StringBuffer();

                // ((Element)nodeList.item( i )).normalize();
                NodeList innerList = nodeList.item( i ).getChildNodes();

                for( int j = 0; j < innerList.getLength(); j++ ) {
                    if ( innerList.item( j ).getNodeType() == Node.TEXT_NODE )  {
                        sb.append( innerList.item( j ).getNodeValue() );
                    }
                }
                return sb.toString();
            }
        }

        // licence not found
        return null;
    }

    /* This innerclass represents licence cache
    */
    /*
    private static class LicenceCache extends Object {
      
      private HashMap map;
      
      
      LicenceCache() {
        map = new HashMap();
      }
      
      String getLicence ( URL url ) {      
        Set keys = map.keySet();
        Iterator it = keys.iterator();
        while( it.hasNext() ) {
          URL keyUrl = (URL)it.next();
          if ( keyUrl.equals( url ) ) {
            return (String)map.get( keyUrl );
          }
        }
        
        return null;
      }
      
      void addLicence ( URL url, String text ) {
        map.put( url, text );
      }
}
    */
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
 *  16   Gandalf   1.15        1/13/00  Petr Hrebejk    Multiuser bugfix
 *  15   Gandalf   1.14        1/12/00  Petr Hrebejk    i18n
 *  14   Gandalf   1.13        12/20/99 Petr Hrebejk    Autocheck & security 
 *       finished
 *  13   Gandalf   1.12        12/16/99 Petr Hrebejk    Sign checking added
 *  12   Gandalf   1.11        12/1/99  Petr Hrebejk    Checkin signatures of 
 *       NBM files & automatic autoupdate check added
 *  11   Gandalf   1.10        11/11/99 Petr Hrebejk    Download bug fix
 *  10   Gandalf   1.9         11/9/99  Petr Hrebejk    Better selection of nbm 
 *       files
 *  9    Gandalf   1.8         11/8/99  Petr Hrebejk    Install of downloaded 
 *       modules added, Licenses in XML
 *  8    Gandalf   1.7         11/2/99  Petr Hrebejk    Update made quicker. 
 *       Manifests in XML file, Licence caching
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/11/99 Petr Hrebejk    Version before Beta 5
 *  5    Gandalf   1.4         10/10/99 Petr Hrebejk    AutoUpdate made to 
 *       wizard
 *  4    Gandalf   1.3         10/8/99  Petr Hrebejk    Next development version
 *  3    Gandalf   1.2         10/8/99  Petr Hrebejk    Next Develop version
 *  2    Gandalf   1.1         10/7/99  Petr Hrebejk    Next development version
 *  1    Gandalf   1.0         10/7/99  Petr Hrebejk    
 * $
 */
