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
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.text.MessageFormat;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/** Class for verifying signs in NBM Files.
 * @author  Martin Ryzl, Petr Hrebejk
 * @version 
 */
class SignVerifier extends Object {

    /** Resource bundle */
    private static final ResourceBundle bundle = NbBundle.getBundle( SignVerifier.class );

    /** The password to the Forte keystore */
    private static final String KS_PSSWD = "open4all"; // NOI18N

    public static final int NOT_CHECKED = -1;
    public static final int BAD_DOWNLOAD = 0;
    public static final int CORRUPTED = 1;
    public static final int NOT_SIGNED = 2;
    public static final int SIGNED = 3;
    public static final int TRUSTED = 4;

    private static final String NEW_LINE = "\n"; // NOI18N
    private static final String SPACE = " "; // NOI18N
    private static final String TAB = "\t"; //NOI18N

    /** The collection of modules for update */
    private Updates updates;
    /** The update check progress panel */
    ProgressDialog progressDialog;
    /** Wizard validator, enables the Next button in wizard */
    private Wizard.Validator validator;
    /** Total size of the verify */
    private int verifySize;
    /** KBytes verified */
    private long totalVerified;
    /** Number of modules to verify */
    private long modulesCount;

    private boolean verifyCanceled = false;

    /** Creates new VeriSign */
    SignVerifier( Updates updates, ProgressDialog progressDialog,
                  Wizard.Validator validator ) {
        this.updates = updates;
        this.validator = validator;
        this.progressDialog = progressDialog;
    }


    /** Verifies all downloaded modules */
    void doVerify() {

        verifyCanceled = false;

        Runnable task = new Runnable () {
                            public void run() {

                                progressDialog.setLabelText( ProgressDialog.PARTIAL_LABEL,
                                                             bundle.getString( "CTL_PreparingVerify_Label" ) );

                                verifySize = getTotalVerifySize();

                                if ( verifyCanceled )
                                    return;

                                verifyAll();

                                if ( verifyCanceled )
                                    return;

                                validator.setValid( true );
                            }
                        };

        RequestProcessor.postRequest( task );
    }


    /** Total size for verify in KBytes */
    int getTotalVerifySize( ) {
        long result = 0L;
        modulesCount = 0;

        Iterator it = updates.getModules().iterator();

        while( it.hasNext() ) {
            ModuleUpdate mu = (ModuleUpdate)it.next();

            if ( mu.isSelected() && mu.isDownloadOK() && mu.getSecurity() == NOT_CHECKED ) {
                File file = Downloader.getNBM( mu );
                result += file.length();
                modulesCount++;
            }
        }
        return (int)(result / 1024);
    }


    void verifyAll() {

        progressDialog.setGaugeBounds( ProgressDialog.OVERALL_GAUGE, 0, verifySize );
        progressDialog.setGaugeValue( ProgressDialog.OVERALL_GAUGE, 0 );
        progressDialog.setLabelText( ProgressDialog.OVERALL_LABEL, "" ); // NOI18N

        progressDialog.setGaugeValue( ProgressDialog.PARTIAL_GAUGE, 0 );

        int currentModule = 0;
        totalVerified = 0;

        Iterator it = updates.getModules().iterator();

        while( it.hasNext() ) {

            if ( verifyCanceled )
                return;

            ModuleUpdate mu = (ModuleUpdate)it.next();

            if ( mu.isSelected() && mu.isDownloadOK() && mu.getSecurity() == NOT_CHECKED  ) {

                if ( verifyCanceled )
                    return;

                progressDialog.setGaugeValue( ProgressDialog.PARTIAL_GAUGE, 0 );
                progressDialog.setLabelText( ProgressDialog.PARTIAL_LABEL,
                                             mu.getName() + " [" + (currentModule + 1) + "/" + modulesCount + "]" ); //NOI18N

                File file = Downloader.getNBM( mu );
                try {
                    Collection certificates = verifyJar( file );
                    //showCollection(certificates);

                    if ( certificates == null ) {
                        mu.setSecurity( NOT_SIGNED );
                        mu.setInstallApproved( false );
                    }
                    else {
                        mu.setCerts( certificates );

                        if ( isTrusted( certificates ) ) {
                            mu.setSecurity( TRUSTED );
                            mu.setInstallApproved( true );
                        }
                        else {
                            mu.setSecurity( SIGNED );
                            mu.setInstallApproved( false );
                        }
                    }
                }
                catch( SecurityException e ) {
                    mu.setSecurity( CORRUPTED );
                    mu.setInstallApproved( false );
                }
                catch( IOException e ) {
                    mu.setSecurity( BAD_DOWNLOAD );
                    mu.setInstallApproved( false );
                    mu.setDownloadOK( false );
                }

                currentModule++;
            }
        }

        progressDialog.setGaugeValue( ProgressDialog.OVERALL_GAUGE,  verifySize );
        String mssgTotal = MessageFormat.format( bundle.getString( "FMT_VerifiedTotal" ),
                           new Object[] { new Integer( verifySize ),
                                          new Integer( verifySize ) } );
        progressDialog.setLabelText( ProgressDialog.OVERALL_LABEL, mssgTotal );
    }


    /**
    * @param args the command line arguments
    */
    /*
    public void check ( File file ) throws Exception {
      Collection certificates = verifyJar( file );
      
      showCollection(certificates);
      KeyStore keyStore = getKeyStore(KEYSTORE, "changeit", null);
      showCollection(getCertificates(keyStore));  
}
    */

    /** Verify jar file.
    * @return Collection of certificates
    */
    public Collection verifyJar( File jarName ) throws SecurityException, IOException {

        JarInputStream jis = null;
        boolean anySigned = false;
        boolean anyUnsigned = false;
        int moduleVerified = 0;

        int flen = (int) jarName.length();

        progressDialog.setGaugeBounds( ProgressDialog.PARTIAL_GAUGE, 0, flen / 1024 );

        List entries = new LinkedList();
        jis = new JarInputStream(new FileInputStream(jarName));

        ZipEntry entry;
        byte[] buffer = new byte[8192];

        while ((entry = jis.getNextEntry()) != null) {

            progressDialog.setGaugeValue( ProgressDialog.PARTIAL_GAUGE, moduleVerified );

            entries.add(entry);
            int len;

            try {

                while((len = jis.read(buffer)) != -1) {
                    // we just read. ( and report progress
                    // jis will throw a SecurityException
                    // if  a signature/digest check fails.
                    // moduleVerified += len;
                    // totalVerified += len;

                    //System.out.println("MV " + (moduleVerified /1024) + "/" + ( flen / 1024 ) + "  TV " + (totalVerified / 1024)); // NOI18N



                    if ( verifyCanceled )
                        return null;

                }
            }
            finally {
                totalVerified += entry.getCompressedSize();
                moduleVerified += entry.getCompressedSize();

                String mssgTotal = MessageFormat.format( bundle.getString( "FMT_VerifiedTotal" ),
                                   new Object[] { new Integer( (int)(totalVerified / 1024) ),
                                                  new Integer( verifySize ) } );
                progressDialog.setGaugeValue( ProgressDialog.OVERALL_GAUGE, (int)(totalVerified / 1024) > verifySize ?
                                              verifySize : (int)( totalVerified / 1024 ) );
                progressDialog.setLabelText( ProgressDialog.OVERALL_LABEL, mssgTotal );

                progressDialog.setGaugeValue( ProgressDialog.PARTIAL_GAUGE, moduleVerified / 1024 );
            }
        }


        jis.close();

        if ( verifyCanceled )
            return null;

        Manifest man = jis.getManifest();
        Set certificates = new HashSet();
        if (man != null) {
            Iterator e = entries.iterator();
            while (e.hasNext()) {
                JarEntry je = (JarEntry) e.next();
                String name = je.getName();
                Certificate[] certs = je.getCertificates();
                boolean isSigned = ((certs != null) && (certs.length > 0));
                anySigned |= isSigned;
                if (certs != null) {
                    for(int i = 0; i < certs.length; i++) {
                        certificates.add(certs[i]);
                        if ( verifyCanceled )
                            return null;
                    }
                }
                else { // The entry is not signed
                    if ( !je.isDirectory() && !name.toUpperCase().startsWith( "META-INF/" )  ) { // NOI18N
                        anyUnsigned = true;
                    }
                }
            }
        }

        if ( certificates.size() > 1 ) {
            throw new SecurityException( bundle.getString( "EXC_TooManySignatures" ) );
        }

        if ( anySigned && anyUnsigned ) {
            throw new SecurityException( bundle.getString( "EXC_NotSignedEntity" ) );
        }

        progressDialog.setGaugeValue( ProgressDialog.PARTIAL_GAUGE, flen / 1024 + 10);
        return anySigned ? certificates : null;
    }

    public static String formatCerts(Collection collection) {
        StringBuffer sb = new StringBuffer( collection.size() * 300 );


        Iterator it = collection.iterator();
        while(it.hasNext()) {
            Certificate cert = (Certificate)it.next();

            if ( cert instanceof X509Certificate ) {
                try {
                    sb.append( "\n\n" ); // NOI18N
                    sb.append( X509CertToString( (X509Certificate) cert ) );
                }
                catch ( Exception e ) {
                    sb.append( cert.toString() );
                }
            }
            else {
                sb.append( cert.toString() );
            }
            sb.append( "\n\n" ); // NOI18N
        }

        return sb.toString();
    }

    /** Tests whether the cets are trusted
     */
    boolean isTrusted( Collection certs ) {

        Collection trustedCerts = getTrustedCerts();

        if ( trustedCerts.size() <= 0 || certs.size() <= 0 )
            return false;

        return trustedCerts.containsAll( certs );
    }

    /** Adds certificates into keystore
    */

    static void addCertificates( Collection certs )
    throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {

        KeyStore ks = getKeyStore( Autoupdater.Support.getUserKSFile(), KS_PSSWD, null);


        Iterator it = certs.iterator();

        while ( it.hasNext() ) {
            Certificate c = (Certificate) it.next();

            // don't add certificate twice
            if ( ks.getCertificateAlias( c ) != null )
                continue;

            // Find free alias name
            String alias = null;
            for ( int i = 0; i < 9999; i++ ) {
                alias = "genAlias" + i; // NOI18N
                if ( !ks.containsAlias( alias ) )
                    break;
            }
            if ( alias == null )
                throw new KeyStoreException( bundle.getString( "EXC_TooManyCertificates" ) );

            ks.setCertificateEntry( alias, c ) ;
        }


        saveKeyStore( ks, Autoupdater.Support.getUserKSFile(), KS_PSSWD, null);
    }

    /** Removes certificates from the keystore */
    static void removeCertificates( Collection certs )
    throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {

        KeyStore ks = getKeyStore( Autoupdater.Support.getUserKSFile(), KS_PSSWD, null);

        Iterator it = certs.iterator();

        while ( it.hasNext() ) {
            Certificate c = (Certificate) it.next();

            String alias = ks.getCertificateAlias( c );

            if ( alias != null )
                ks.deleteEntry( alias );

        }

        saveKeyStore( ks, Autoupdater.Support.getUserKSFile(), KS_PSSWD, null);

    }


    // Keystore utility methods --------------------------------------------------------


    /** Gets all trusted certificates */
    Collection getTrustedCerts() {

        Collection trustedCerts = new ArrayList( 10 );

        File cKS = Autoupdater.Support.getCentralKSFile();
        File uKS = Autoupdater.Support.getUserKSFile();

        try {
            if ( cKS.canRead() ) {
                KeyStore ks = getKeyStore( cKS, KS_PSSWD, null );
                trustedCerts.addAll ( getCertificates( ks ) );
            }
            if ( uKS.canRead() && !uKS.equals( cKS ) ) {
                KeyStore ks = getKeyStore( uKS, KS_PSSWD, null );
                trustedCerts.addAll ( getCertificates( ks ) );
            }

        }
        // In case of exception let the collection empty
        catch ( CertificateException  e ) {
        }
        catch ( KeyStoreException e ) {
        }
        catch ( IOException e ) {
        }
        catch ( NoSuchAlgorithmException e ) {
        }

        return trustedCerts;
    }

    /** Creates keystore and loads data from file.
    * @param filename - name of the keystore
    * @param storetype - type of the keystore
    * @param password
    */
    private static KeyStore getKeyStore(File file, String password, String storetype)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {

        InputStream is = null;

        try {
            is = new FileInputStream( file );
        }
        catch ( IOException e ) {
            // Do nothing leaving is null creates empty key store
        }

        KeyStore keyStore = null;



        if ( storetype == null ) {
            keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
            keyStore.load( is, password.toCharArray() );
            if ( is != null )
                is.close();
        }

        return keyStore;
    }

    /** Creates keystore and loads data from file.
    * @param keyStore - keystore
    * @param filename - name of the keystore
    * @param storetype - type of the keystore
    * @param password
    */
    public static void saveKeyStore(KeyStore keyStore, File file, String password, String storetype)
    throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        OutputStream os = new FileOutputStream( file );
        keyStore.store(os, password.toCharArray( ) );
        os.close();
    }

    /** Return all certificates in system ( Form central and user's directory )
    */
    public static Collection getCertificates(KeyStore keyStore) throws KeyStoreException {

        List certificates = new ArrayList( 10 );

        Enumeration en = keyStore.aliases();
        while (en.hasMoreElements()) {
            String alias = (String)en.nextElement();
            Certificate cert = keyStore.getCertificate(alias);
            certificates.add(cert);
        }
        return certificates;
    }

    void cancelVerify() {
        verifyCanceled = true;
    }


    /** Prints a X509 certificate in a human readable format.
    */
    private static String X509CertToString(X509Certificate cert )
    throws Exception
    {
        return bundle.getString("MSG_Owner") + SPACE + (cert.getSubjectDN()) + NEW_LINE +
               bundle.getString("MSG_Issuer") + SPACE + (cert.getIssuerDN()) + NEW_LINE +
               bundle.getString("MSG_SerNumber") + SPACE + cert.getSerialNumber().toString(16) + NEW_LINE +
               bundle.getString("MSG_Valid") + SPACE + cert.getNotBefore().toString() +
               SPACE + bundle.getString("MSG_Until") + SPACE + cert.getNotAfter().toString() + NEW_LINE +
               bundle.getString("MSG_CertFinger") + NEW_LINE +
               SPACE + TAB + bundle.getString("MSG_MD5") + SPACE + SPACE + getCertFingerPrint("MD5", cert) + NEW_LINE +
               SPACE + TAB + bundle.getString("MSG_SHA1") + SPACE + getCertFingerPrint("SHA1", cert);
    }

    /** Gets the requested finger print of the certificate.
    */
    private static String getCertFingerPrint(String mdAlg, Certificate cert)
    throws Exception
    {
        byte[] encCertInfo = cert.getEncoded();
        MessageDigest md = MessageDigest.getInstance(mdAlg);
        byte[] digest = md.digest(encCertInfo);
        return toHexString(digest);
    }

    /**Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":"); // NOI18N
            }
        }
        return buf.toString();
    }


    /** Converts a byte to hex digit and writes to the supplied buffer
    */
    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

}
/*
 * Log
 *  8    Gandalf   1.7         1/18/00  Petr Hrebejk    Keystore for multiuser 
 *       fixed
 *  7    Gandalf   1.6         1/13/00  Petr Hrebejk    i18 mk3
 *  6    Gandalf   1.5         1/12/00  Petr Hrebejk    i18n
 *  5    Gandalf   1.4         12/20/99 Petr Hrebejk    Autocheck & security 
 *       finished
 *  4    Gandalf   1.3         12/16/99 Petr Hrebejk    Sign checking added
 *  3    Gandalf   1.2         12/1/99  Petr Hrebejk    Checkin signatures of 
 *       NBM files & automatic autoupdate check added
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/10/99 Petr Hrebejk    
 * $
 */
