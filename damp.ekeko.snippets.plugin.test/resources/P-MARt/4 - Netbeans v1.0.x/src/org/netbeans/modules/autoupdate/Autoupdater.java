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
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.modules.ModuleDescription;
import org.openide.util.NbBundle;

import org.netbeans.core.UpdateSupport;


/** This singleton class is a skeleton for running autoupdate, it also
 * contains all communication with core of IDE implementation. This
 * communication is handled by org.netbeans.core.UpdateSupport
 * on the side of IDE Core implementation.
 *
 * @author  Petr Hrebejk
 */
public class Autoupdater extends Object {

    /** Resource bundle */
    static ResourceBundle bundle = NbBundle.getBundle(Autoupdater.class);

    /** Is the autoupdate running ? */
    private static boolean isRunning = false;

    /** Getter for static property isRunning */
    static boolean isRunning() {
        return isRunning;
    }

    /** Setter for static property isRunning */
    static void setRunning( boolean isRunning ) {
        Autoupdater.isRunning = isRunning;
    }

    /** Restarts IDE in order to run Updater */
    static void restart() {
        UpdateSupport.restart();
    }

    /** Installs autoupdate checker */
    static void installUpdateChecker( UpdateSupport.UpdateChecker updateChecker ) {
        UpdateSupport.installUpdateChecker( updateChecker );
    }

    /** Gets proxy usage */
    static boolean isUseProxy() {
        return UpdateSupport.isUseProxy();
    }

    /** Gets Proxy Host */
    static String getProxyHost() {
        return UpdateSupport.getProxyHost();
    }

    /** Gets Proxy Port */
    static String getProxyPort() {
        return UpdateSupport.getProxyPort();
    }

    /** Sets the whole proxy configuration */
    static void setProxyConfiguration( boolean use, String host, String port ) {
        UpdateSupport.setProxyConfiguration( use, host, port );
    }

    /** Singleton support class for getting directories */
    static class Support extends Object {

        /** Important system properies */
        private static final String SYSPROP_REGNUM = "netbeans.autoupdate.regnum"; // NOI18N
        private static final String SYSPROP_COUNTRY = "netbeans.autoupdate.country"; // NOI18N
        private static final String SYSPROP_LANGUAGE = "netbeans.autoupdate.language"; // NOI18N
        private static final String SYSPROP_VARIANT = "netbeans.autoupdate.variant"; // NOI18N

        /** System property holding autoupdate version */
        private static final String UPDATE_VERSION_PROP = "netbeans.autoupdate.version"; // NOI18N

        /** Current version of autoupdate */
        private static final String UPDATE_VERSION = "1.1"; // NOI18N

        /** Platform dependent file name separator */
        private static final String FILE_SEPARATOR = System.getProperty ("file.separator");

        /** Relative name of directory where the .NBM files are downloaded */
        private static final String DOWNLOAD_DIR = "update" + FILE_SEPARATOR + "download"; // NOI18N

        /** Relative name of directory where the patch files are stored */
        private static final String PATCH_DIR = "lib" + FILE_SEPARATOR + "patches"; // NOI18N

        /** Relative name of key store file */
        private static final String KS_FILE = "lib" + FILE_SEPARATOR + "f4j.ks"; // NOI18N

        /** File representing the download directory */
        private static File downloadDirectory = null;

        /** File representing the central patch directory */
        private static File centralPatchDirectory = null;

        /** File representing the users patch directory */
        private static File userPatchDirectory = null;

        /** The URL where to find  updates*/
        private static URL updateURL = null;

        /** The file with cenral KeySotre */
        private static File centralKSFile = null;

        /** The file with users KeySotre */
        private static File userKSFile = null;

        /** Disable instantiation */
        private Support() {}

        /** Retruns array of module descriptions of installed modules */
        public static ModuleDescription[] getModuleDescriptions() {
            return UpdateSupport.getModuleDescriptions();
        }

        /** Gets the update URL */
        public static URL getUpdateURL () {


            if ( System.getProperty( UPDATE_VERSION_PROP ) == null ) {
                System.setProperty( UPDATE_VERSION_PROP, UPDATE_VERSION );
            }

            try {
                updateURL = new URL( replace( bundle.getString( "URL_Default" ) ) );
            }
            catch (java.net.MalformedURLException e) {
                TopManager.getDefault ().notifyException (e);
            };

            //System.out.println( updateURL );

            return updateURL;

        }


        /** The directory where to download the distribution files of modules */
        public static File getDownloadDirectory() {
            if ( downloadDirectory == null ) {

                if ( isMultiuser() )
                    downloadDirectory = new File (System.getProperty ("netbeans.user") + FILE_SEPARATOR + DOWNLOAD_DIR );
                else
                    downloadDirectory = new File (System.getProperty ("netbeans.home") + FILE_SEPARATOR + DOWNLOAD_DIR );

                if ( !downloadDirectory.isDirectory() )
                    downloadDirectory.mkdirs();
            }

            return downloadDirectory;
        }

        /** Decides about running multiuser */
        private static boolean isMultiuser() {

            if ( System.getProperty ("netbeans.user") == null ||
                    System.getProperty ("netbeans.home").equals( System.getProperty ("netbeans.user") ) )
                return false;
            else
                return true;
        }

        /** Gets the central directory of patches */
        public static File getCentralPatchDirectory() {
            if ( centralPatchDirectory == null ) {
                centralPatchDirectory = new File (System.getProperty ("netbeans.home") + FILE_SEPARATOR + PATCH_DIR );
            }
            return centralPatchDirectory;
        }


        /** Gets the users directory of patches */
        public static File getUserPatchDirectory() {
            if ( userPatchDirectory == null ) {
                userPatchDirectory = new File (System.getProperty ("netbeans.user") + FILE_SEPARATOR + PATCH_DIR );
            }
            return userPatchDirectory;
        }


        /** Gets the central keystore */
        static File getCentralKSFile() {
            if ( centralKSFile == null ) {
                centralKSFile = new File (System.getProperty ("netbeans.home") + FILE_SEPARATOR + KS_FILE );
            }

            return centralKSFile;
        }

        /** Gets the users keystore */
        static File getUserKSFile() {
            if ( userKSFile == null ) {
                userKSFile = new File (System.getProperty ("netbeans.user") + FILE_SEPARATOR + KS_FILE );
            }

            return  userKSFile;
        }

        /** Test whether the user has rights to write into directory */
        /*
        private static boolean canWrite( File dir, boolean create ) {
          if ( !dir.exists() && create ) 

            dir.mkdirs();

          if ( !dir.isDirectory() || !dir.canWrite() )
            return false;



          File tmp = null;

          try {
            tmp = File.createTempFile( "test", "access", dir ); // NOI18N
          }
          catch ( java.io.IOException e ) {
            return false;
          }

          if ( tmp == null )
            return false;

          boolean cw = tmp.canWrite();
          if (cw)
            tmp.delete();

          return cw;
    }
        */

        /** Utility method for replacing {$xxx} with value of system property xxx
        */
        private static String replace( String string ) {

            // First of all set our system properties
            setSystemProperties();

            if ( string == null )
                return null;

            StringBuffer sb = new StringBuffer();

            int index, prevIndex;
            index = prevIndex = 0;
            while( ( index = string.indexOf( "{", index )) != -1 && index < string.length() - 1) { // NOI18N

                if ( string.charAt( index + 1 ) == '{' || string.charAt( index + 1 ) != '$'  ) {
                    ++index;
                    continue;
                }

                sb.append( string.substring( prevIndex, index ) );
                int endBracketIndex = string.indexOf( "}", index ); // NOI18N
                if ( endBracketIndex != -1 ) {
                    String whatToReplace = string.substring( index + 2, endBracketIndex );
                    sb.append( System.getProperty( whatToReplace, ""  ) );
                }
                prevIndex = endBracketIndex == -1 ? index + 2 : endBracketIndex + 1;
                ++index;
            }

            if ( prevIndex < string.length() - 1 )
                sb.append( string.substring( prevIndex ) );

            return sb.toString();
        }

        private static void setSystemProperties() {

            System.setProperty( SYSPROP_REGNUM, Settings.getShared().getRegistrationNumber() );

            if ( System.getProperty( SYSPROP_COUNTRY, null ) == null ) {
                System.setProperty( SYSPROP_COUNTRY, java.util.Locale.getDefault().getCountry() );
            }
            if ( System.getProperty( SYSPROP_LANGUAGE, null ) == null ) {
                System.setProperty( SYSPROP_LANGUAGE, java.util.Locale.getDefault().getLanguage() );
            }
            if ( System.getProperty( SYSPROP_VARIANT, null ) == null ) {
                System.setProperty( SYSPROP_VARIANT, java.util.Locale.getDefault().getVariant() );
            }
        }


    }


}

/*
 * Log
 *  19   Gandalf   1.18        3/20/00  Milan Kubec     new version of 
 *       autoupdate 1.0
 *  18   Gandalf   1.17        3/8/00   Petr Hrebejk    URL version raised
 *  17   Gandalf   1.16        2/23/00  Petr Hrebejk    Notifications added into
 *       autoupdate
 *  16   Gandalf   1.15        1/13/00  Petr Hrebejk    Multiuser bugfix
 *  15   Gandalf   1.14        1/12/00  Petr Hrebejk    i18n
 *  14   Gandalf   1.13        1/12/00  Petr Hrebejk    Regnum can be changed at
 *       any time
 *  13   Gandalf   1.12        1/10/00  Petr Hrebejk    Version for finding XML 
 *       raised
 *  12   Gandalf   1.11        1/9/00   Petr Hrebejk    Support for i18n added
 *  11   Gandalf   1.10        1/9/00   Petr Hrebejk    Proxy Config and 
 *       Registration number added
 *  10   Gandalf   1.9         12/16/99 Petr Hrebejk    Sign checking added
 *  9    Gandalf   1.8         12/1/99  Petr Hrebejk    Checkin signatures of 
 *       NBM files & automatic autoupdate check added
 *  8    Gandalf   1.7         11/9/99  Petr Hrebejk    Better selection of nbm 
 *       files
 *  7    Gandalf   1.6         11/1/99  Petr Hrebejk    Remove of 
 *       org.netbeans.core.ModuleUpdater fixed
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/11/99 Petr Hrebejk    Last minute fixes
 *  4    Gandalf   1.3         10/11/99 Petr Hrebejk    Version before Beta 5
 *  3    Gandalf   1.2         10/10/99 Petr Hrebejk    AutoUpdate made to 
 *       wizard
 *  2    Gandalf   1.1         10/8/99  Petr Hrebejk    Next development version
 *  1    Gandalf   1.0         10/7/99  Petr Hrebejk    
 * $
 */
