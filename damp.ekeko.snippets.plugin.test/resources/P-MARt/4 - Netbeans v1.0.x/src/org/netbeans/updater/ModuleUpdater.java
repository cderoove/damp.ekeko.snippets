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

package org.netbeans.updater;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** Class used by autoupdate module for the work with module files and
 * for installing / uninstalling modules
 *
 * @author  Petr Hrebejk
 * @version 
 */
public class ModuleUpdater extends Thread {

    ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/updater/Bundle"); // NOI18N

    /** Platform dependent file name separator */
    private static final String FILE_SEPARATOR = System.getProperty ("file.separator");

    /** Relative name of directory where the .NBM files are downloaded */
    private static final String DOWNLOAD_DIR = "update" + FILE_SEPARATOR + "download"; // NOI18N

    /** Relative name of backup directory */
    private static final String BACKUP_DIR = "update" + FILE_SEPARATOR + "backup"; // NOI18N

    /** Relative name of netbeans/lib directory */
    private static final String NB_LIB_DIR = "lib"; // NOI18N

    /** Name of extension directory */
    private static final String EXT_DIR = "ext"; // NOI18N

    /** Relative name of java/lib/ext directory */
    private static final String JAVA_LIB_EXT_DIR = "lib" +  FILE_SEPARATOR + EXT_DIR; // NOI18N

    /** The name of zip entry containing netbeans files */
    public static final String UPDATE_NETBEANS_DIR = "netbeans"; // NOI18N

    /** The name of zip entry containing java_extension files */
    public static final String UPDATE_JAVA_EXT_DIR = "java_ext"; // NOI18N


    /** Extension of the distribution files */
    private static final String NBM_EXTENSION = "nbm"; // NOI18N


    private static File downloadDirectory = null;
    private static File backupDirectory = null;
    private static File netbeansDirectory = null;
    private static File nbLibDirectory = null;
    private static File javaLibExtDirectory = null;

    /** All distribution files in the download directory */
    private File[] nbmFiles = null;

    /** Should the thread stop */
    private volatile boolean stop = false;

    private volatile boolean suspend = false;

    /** Total length of unpacked files */
    private long totalLength;

    /** Creates new ModuleUpdater */
    public void run() {


        getDownloadDirectory();

        checkStop();

        if ( downloadDirectory == null ) {
            endRun();
        }

        checkStop();
        nbmFiles = getNBMFiles();

        if ( nbmFiles == null || nbmFiles.length == 0 ) {
            endRun();
        }

        checkStop();

        totalLength();

        checkStop();

        unpack();

        UpdaterFrame.unpackingFinished();
    }

    /** ends the run of update */
    void endRun() {
        stop = true;
    }

    /** checks wheter ends the run of update */
    private void checkStop() {

        if ( suspend )
            while ( suspend );

        if ( stop ) {
            System.exit( 0 );
        }
    }

    /** Creates array of all .NBM files in the update/download directory
    */
    static File[] getNBMFiles() {


        File dirList[] = downloadDirectory.listFiles( new FilenameFilter() {
                             public boolean accept( File dir, String name ) {
                                 return name.endsWith( NBM_EXTENSION );
                             }
                         });

        return dirList;
    }


    /** Determines size of unpacked modules */
    private void totalLength() {
        totalLength = 0L;

        UpdaterFrame.setLabel( bundle.getString( "CTL_PreparingUnpack" ) );
        UpdaterFrame.setProgressRange( 0, nbmFiles.length );

        for( int i = 0; i < nbmFiles.length; i++ ) {

            JarFile jarFile = null;

            try {
                UpdaterFrame.setProgressValue( i + 1 );

                jarFile = new JarFile( nbmFiles[i] );
                Enumeration entries = jarFile.entries();
                while( entries.hasMoreElements() ) {
                    JarEntry entry = (JarEntry) entries.nextElement();

                    checkStop();

                    if ( ( entry.getName().startsWith( UPDATE_NETBEANS_DIR ) ||
                            entry.getName().startsWith( ModuleUpdater.UPDATE_JAVA_EXT_DIR ) ) &&
                            !entry.isDirectory() ) {
                        totalLength += entry.getSize();
                    }
                }
            }
            catch ( java.io.IOException e ) {
                // Ignore non readable files
            }
            finally {
                try {
                    if ( jarFile != null )
                        jarFile.close();
                }
                catch ( java.io.IOException e ) {
                    // We can't close the file do nothing
                    // System.out.println( "Cant close : " + e ); // NOI18N
                }
            }
        }
    }


    /** Unpack the distribution files into update directory */

    void unpack ()  {

        long bytesRead = 0L;

        // System.out.println("Total lengtg " + totalLength ); // NOI18N

        UpdaterFrame.setLabel( "" ); // NOI18N
        UpdaterFrame.setProgressRange( 0, totalLength );

        for( int i = 0; i < nbmFiles.length; i++ ) {

            UpdaterFrame.setLabel( bundle.getString("CTL_UnpackingFile") + "  " + nbmFiles[i].getName() ); //NOI18N
            UpdaterFrame.setProgressValue( bytesRead );
            JarFile jarFile = null;

            try {
                jarFile = new JarFile( nbmFiles[i] );
                Enumeration entries = jarFile.entries();
                while( entries.hasMoreElements() ) {
                    JarEntry entry = (JarEntry) entries.nextElement();

                    checkStop();

                    if ( entry.getName().startsWith( UPDATE_NETBEANS_DIR ) ) {
                        // Copy files into netbeans directory
                        if ( entry.isDirectory() ) {
                            File newDir = new File( getNetbeansDirectory(), entry.getName().substring( UPDATE_NETBEANS_DIR.length() ) );
                            if ( !newDir.isDirectory() )
                                newDir.mkdirs();
                            File newBckDir = new File( getBackupDirectory(), entry.getName() );
                            if ( !newBckDir.isDirectory() )
                                newBckDir.mkdirs();
                        }
                        else {
                            File destFile = new File( getNetbeansDirectory(), entry.getName().substring( UPDATE_NETBEANS_DIR.length() ) );
                            if ( destFile.exists() ) {
                                File bckFile = new File( getBackupDirectory(), entry.getName() );
                                // System.out.println("Backing up" ); // NOI18N
                                copyStreams( new FileInputStream( destFile ), new FileOutputStream( bckFile ) );
                            }
                            copyStreams( jarFile.getInputStream( entry ), new FileOutputStream( destFile ) );
                            bytesRead += entry.getSize();
                            UpdaterFrame.setProgressValue( bytesRead );

                        }
                    }
                    else if ( entry.getName().startsWith( ModuleUpdater.UPDATE_JAVA_EXT_DIR ) &&
                              !entry.isDirectory() ) {
                        // Copy files into java/lib/ext directory
                        File destFile = new File( getJavaLibExtDirectory(), entry.getName().substring( UPDATE_JAVA_EXT_DIR.length() ) );
                        if ( destFile.exists() ) {
                            File bckFile = new File( getBackupDirectory(), ModuleUpdater.UPDATE_JAVA_EXT_DIR + FILE_SEPARATOR + entry.getName().substring( UPDATE_NETBEANS_DIR.length() ) );
                            copyStreams( new FileInputStream( destFile ), new FileOutputStream( bckFile ) );
                        }
                        copyStreams( jarFile.getInputStream( entry ), new FileOutputStream( destFile ) );
                        bytesRead += entry.getSize();
                        UpdaterFrame.setProgressValue( bytesRead );
                    }
                }
            }
            catch ( java.io.IOException e ) {
                // Ignore non readable files
            }
            finally {
                try {
                    if ( jarFile != null )
                        jarFile.close();
                }
                catch ( java.io.IOException e ) {
                    // We can't close the file do nothing
                    // System.out.println("Can't close : " + e ); // NOI18N
                }
                //System.out.println("Dleting :" + nbmFiles[i].getName() + ":" + nbmFiles[i].delete() ); // NOI18N

                nbmFiles[i].delete();

                //nbmFiles[i].deleteOnExit();

            }
        }
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


    /** The directory where to backup old versions of modules */
    public static File getBackupDirectory() {
        if ( backupDirectory == null ) {

            if ( isMultiuser() )
                backupDirectory = new File (System.getProperty ("netbeans.user") + FILE_SEPARATOR + BACKUP_DIR );
            else
                backupDirectory = new File (System.getProperty ("netbeans.home") + FILE_SEPARATOR + BACKUP_DIR );

            if ( !backupDirectory.isDirectory() )
                backupDirectory.mkdirs();
        }

        return backupDirectory;
    }


    /** Gets the netbeans directory */
    public static File getNetbeansDirectory() {
        if ( netbeansDirectory == null ) {
            if ( isMultiuser() )
                netbeansDirectory = new File (System.getProperty ("netbeans.user") );
            else
                netbeansDirectory = new File (System.getProperty ("netbeans.home") );
        }
        return netbeansDirectory;
    }

    /** The directory of libraries that are added to CLASSPATH on startup */

    public static File getNbLibDirectory() {
        if ( nbLibDirectory == null ) {

            if ( isMultiuser() )
                nbLibDirectory = new File (System.getProperty ("netbeans.user") + FILE_SEPARATOR + NB_LIB_DIR );
            else
                nbLibDirectory = new File (System.getProperty ("netbeans.home") + FILE_SEPARATOR + NB_LIB_DIR );
        }

        File nbLibExt = new File( nbLibDirectory, EXT_DIR );
        if ( !nbLibExt.isDirectory() )
            nbLibExt.mkdirs();

        return nbLibDirectory;
    }


    /** The directory lib/ext directory of JDK */
    public static File getJavaLibExtDirectory() {
        if ( javaLibExtDirectory == null ) {
            javaLibExtDirectory = new File (System.getProperty ("java.home") + FILE_SEPARATOR + JAVA_LIB_EXT_DIR );
        }

        if ( canWrite( javaLibExtDirectory, true ) )
            return javaLibExtDirectory;
        else
            return getNbLibDirectory();
    }

    private static boolean isMultiuser() {

        if ( System.getProperty ("netbeans.user") == null ||
                System.getProperty ("netbeans.home").equals( System.getProperty ("netbeans.user") ) )
            return false;
        else
            return true;
    }


    /*
    private static Manifest getManifest( File file ) throws java.io.IOException {
      JarFile jar = new JarFile( file );
      Manifest man = jar.getManifest();
      jar.close();
      return man; 
}
    */



    /** Copies two streams */
    private void copyStreams( InputStream src, OutputStream dest ) throws java.io.IOException {

        BufferedInputStream bsrc = new BufferedInputStream( src );
        BufferedOutputStream bdest = new BufferedOutputStream( dest );

        int count = 0;

        int c;

        try {
            while( ( c = bsrc.read() ) != -1 ) {
                bdest.write( c );
                count++;
                if ( count > 8500 ) {
                    count = 0;
                    checkStop();
                }
            }
        }
        finally {
            bsrc.close();
            bdest.close();
        }
    }

    /** Test whether the user has rights to write into directory */

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


    /*
    void cancel() {
      suspend = true;
      
      StopWarning stopWarning = new StopWarning();
      stopWarning.show();
     
      stop = stopWarning.isStop();
      suspend = false; 
}
    */

}

/*
 * Log
 *  9    Gandalf   1.8         1/24/00  Petr Hrebejk    Space in unpacking files
 *       label adde
 *  8    Gandalf   1.7         1/13/00  Petr Hrebejk    i18n mk2
 *  7    Gandalf   1.6         1/13/00  Petr Hrebejk    i18n
 *  6    Gandalf   1.5         1/10/00  Petr Hrebejk    Multiuser bug fixed
 *  5    Gandalf   1.4         1/9/00   Petr Hrebejk    Multiuser recognition 
 *       changed
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/12/99 Petr Hrebejk    Backs up the replaced 
 *       files
 *  2    Gandalf   1.1         10/7/99  Petr Hrebejk    
 *  1    Gandalf   1.0         10/6/99  Petr Hrebejk    
 * $
 */
