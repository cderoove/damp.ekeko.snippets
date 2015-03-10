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

import java.io.*;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.jar.*;
import java.util.zip.ZipEntry;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.RequestProcessor;

/** This class downloads modules and verifies the digital signatures
 * this class also copyies the downloaded modules.
 * @author  phrebejk
 */
class Downloader extends Object {

    /** Resource bundle */
    private static final ResourceBundle bundle = NbBundle.getBundle( Downloader.class );

    /** The collection of modules for update */
    private Updates updates;
    /** The update check progress panel */
    ProgressDialog progressDialog;
    /** Set to true if the download was canceled by the user */
    private volatile boolean downloadCanceled;
    /** Total size of the download */
    private int downloadSize;
    /** KBytes downloaded */
    private long totalDownloaded;
    /** Number of modules to downaload */
    private long modulesCount;
    /** Shoud internet download be performed */
    private boolean urlDownload;

    /** Extension of the distribution files */
    private static final String NBM_EXTENSION = "nbm"; // NOI18N

    /** Wizard validator, enables the Next button in wizard */
    private Wizard.Validator validator;


    /** Creates new Downloader */
    public Downloader( Updates updates, ProgressDialog progressDialog,
                       Wizard.Validator validator, boolean urlDownload ) {
        this.updates = updates;
        this.validator = validator;
        this.progressDialog = progressDialog;
        this.urlDownload = urlDownload;
    }

    void doDownload() {

        downloadCanceled = false;

        Runnable task = new Runnable () {
                            public void run() {

                                progressDialog.setLabelText( ProgressDialog.PARTIAL_LABEL,
                                                             bundle.getString( "CTL_PreparingDownload_Label" ) );

                                downloadSize = getTotalDownloadSize();

                                if ( downloadCanceled )
                                    return;

                                downloadAll();

                                if ( downloadCanceled )
                                    return;

                                // SignVerifier.verifyAll( updates );
                                validator.setValid( true );
                            }
                        };

        RequestProcessor.postRequest( task );
    }

    /** Total size of download in KBytes */
    int getTotalDownloadSize( ) {
        long result = 0L;
        modulesCount = 0;

        Iterator it = updates.getModules().iterator();

        while( it.hasNext() ) {
            ModuleUpdate mu = (ModuleUpdate)it.next();

            if ( mu.isSelected() && !mu.isDownloadOK() ) {
                result += mu.getDownloadSize();
                modulesCount++;
            }
        }
        return (int)(result / 1024);
    }


    /** Downloads the modules from web */
    private void downloadAll() {

        if ( downloadCanceled )
            return;

        progressDialog.setGaugeBounds( ProgressDialog.OVERALL_GAUGE, 0, downloadSize );
        progressDialog.setGaugeValue( ProgressDialog.OVERALL_GAUGE, 0 );
        progressDialog.setLabelText( ProgressDialog.OVERALL_LABEL, "" ); // NOI18N

        progressDialog.setGaugeValue( ProgressDialog.PARTIAL_GAUGE, 0 );

        int currentModule = 0;

        totalDownloaded = 0;

        Iterator it = updates.getModules().iterator();

        long start_time = System.currentTimeMillis();

        while( it.hasNext() ) {

            if ( downloadCanceled )
                return;

            ModuleUpdate mu = (ModuleUpdate)it.next();
            if ( mu.isSelected() && !mu.isDownloadOK() ) {
                progressDialog.setGaugeValue( ProgressDialog.PARTIAL_GAUGE, 0 );
                progressDialog.setLabelText( ProgressDialog.PARTIAL_LABEL,
                                             mu.getName() + " [" + (currentModule + 1) + "/" + modulesCount + "]" ); // NOI18N
                download( start_time, mu );
                currentModule++;
            }
        }

        progressDialog.setGaugeValue( ProgressDialog.OVERALL_GAUGE,  downloadSize );
        String mssgTotal = MessageFormat.format( bundle.getString( "FMT_DownloadedTotal" ),
                           new Object[] { new Integer( downloadSize ),
                                          new Integer( downloadSize ) } );
        progressDialog.setLabelText( ProgressDialog.OVERALL_LABEL, mssgTotal );

    }

    /** Downloads a .NBM file into download directory
    */
    private void download( long start_time, ModuleUpdate moduleUpdate ) {

        // throws java.io.IOException {

        int moduleDownloaded = 0;
        int flen = 0;

        while ( true ) {    // Retry loop

            try {

                URLConnection distrConnection = null;
                if ( urlDownload ) {
                    distrConnection = moduleUpdate.getDistribution().openConnection();
                    flen = distrConnection.getContentLength();
                }
                else {
                    flen = (int)moduleUpdate.getDistributionFile().length();
                }

                if ( downloadCanceled )
                    return;

                moduleDownloaded = 0;

                progressDialog.setGaugeBounds( ProgressDialog.PARTIAL_GAUGE, 0, flen / 1024 );

                /*
                File distrFile = new File( moduleUpdate.getDistribution().getFile() );
                File destFile = new File( Autoupdater.Support.getDownloadDirectory(), distrFile.getName() ); 
                */

                File destFile = getNBM( moduleUpdate );

                BufferedInputStream bsrc;
                if ( urlDownload ) {
                    bsrc = new BufferedInputStream( distrConnection.getInputStream() );
                }
                else {
                    bsrc = new BufferedInputStream( new FileInputStream( moduleUpdate.getDistributionFile() ) );
                }

                BufferedOutputStream bdest = new BufferedOutputStream( new FileOutputStream( destFile ) );

                int c;
                int i = 0;

                long time_old, time_new;
                time_old = System.currentTimeMillis();

                try {
                    while( ( c = bsrc.read() ) != -1 ) {
                        bdest.write( c );

                        moduleDownloaded++;
                        totalDownloaded++;

                        time_new = System.currentTimeMillis();

                        if ( downloadCanceled )
                            return;

                        if ( moduleDownloaded % 4096 == 0 /* && time_new - time_old > 999 */ ) {
                            time_new = time_old;


                            String mssgTotal = MessageFormat.format( bundle.getString( "FMT_DownloadedTotal" ),
                                               new Object[] { new Integer( (int)(totalDownloaded / 1024) ),
                                                              new Integer( downloadSize ) } );

                            progressDialog.setGaugeValue( ProgressDialog.OVERALL_GAUGE, (int)(totalDownloaded / 1024) > downloadSize ?
                                                          downloadSize : (int)( totalDownloaded / 1024 ) );
                            progressDialog.setLabelText( ProgressDialog.OVERALL_LABEL, mssgTotal );

                            /*
                            progressDialog.setLabelText( ProgressDialog.PARTIAL_LABEL, 
                              (int)(moduleDownloaded / 1024) + "KB of " + ( flen / 1024 ) + " downloaded."); 
                            */

                            progressDialog.setGaugeValue( ProgressDialog.PARTIAL_GAUGE, moduleDownloaded / 1024 );
                        }
                    }
                }
                finally {
                    bsrc.close();
                    bdest.close();
                }
            }
            catch ( IOException e ) {

                // Download failed

                String mssg = MessageFormat.format( bundle.getString( "FMT_DownloadFailed" ),
                                                    new Object[] { moduleUpdate.getName() } );
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg,
                                      bundle.getString( "CTL_DownloadFailed" ),
                                      NotifyDescriptor.YES_NO_CANCEL_OPTION );
                TopManager.getDefault().notify( nd );

                if ( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
                    totalDownloaded -= moduleDownloaded; // Dont't count lost bytes
                    continue; // Retry
                }
                else if ( nd.getValue().equals( NotifyDescriptor.CANCEL_OPTION ) ) {
                    downloadCanceled = true; // Stop whole download
                    validator.setValid( true );
                    return;
                }
                // User selected not to download current module
                totalDownloaded -= moduleDownloaded; // Dont't count lost bytes
                downloadSize -= flen * 1024;
                if ( getNBM( moduleUpdate ).exists() )
                    getNBM( moduleUpdate ).delete();
                return;
            }

            //progressDialog.setGaugeValue( ProgressDialog.PARTIAL_GAUGE, (int)(moduleUpdate.getDownloadSize() / 1024 ) );
            progressDialog.setGaugeValue( ProgressDialog.PARTIAL_GAUGE, flen / 1024 + 10);
            moduleUpdate.setDownloadOK( true );
            return; // The module downloaded O.K.
        }
    }

    void cancelDownload() {
        downloadCanceled = true;
    }


    static File getNBM( ModuleUpdate mu ) {
        //File distrFile = new File(mu.getDistributionFileName() );
        //File destFile = new File( Autoupdater.Support.getDownloadDirectory(), distrFile.getName() );
        File destFile = new File( Autoupdater.Support.getDownloadDirectory(), mu.getDistributionFilename() );
        return destFile;
    }


    // Deletes all files in download directory
    static void deleteDownload() {
        File[] nbms = getNBMFiles();

        for( int i = 0; i < nbms.length; i++ ) {
            nbms[i].delete();
        }
    }

    private static File[] getNBMFiles() {

        File dirList[] = Autoupdater.Support.getDownloadDirectory().listFiles( new FilenameFilter() {
                             public boolean accept( File dir, String name ) {
                                 return name.endsWith( NBM_EXTENSION );
                             }
                         });

        return dirList;
    }


}
/*
 * Log
 *  10   Gandalf   1.9         1/12/00  Petr Hrebejk    i18n
 *  9    Gandalf   1.8         12/1/99  Petr Hrebejk    Checkin signatures of 
 *       NBM files & automatic autoupdate check added
 *  8    Gandalf   1.7         11/12/99 Petr Hrebejk    Bug fixes: Texts, Not 
 *       NetBeans patches, unselecting modules
 *  7    Gandalf   1.6         11/11/99 Petr Hrebejk    Download bug fix
 *  6    Gandalf   1.5         11/8/99  Petr Hrebejk    Install of downloaded 
 *       modules added, Licenses in XML
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/11/99 Petr Hrebejk    Version before Beta 5
 *  3    Gandalf   1.2         10/10/99 Petr Hrebejk    AutoUpdate made to 
 *       wizard
 *  2    Gandalf   1.1         10/8/99  Petr Hrebejk    Next Develop version
 *  1    Gandalf   1.0         10/7/99  Petr Hrebejk    
 * $
 */
