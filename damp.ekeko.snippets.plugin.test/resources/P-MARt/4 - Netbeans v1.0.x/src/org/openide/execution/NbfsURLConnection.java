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

package org.openide.execution;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.ResourceBundle;

import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.TopManager;

/** Special URL connection directly accessing an internal file object.
*
* @author Ales Novak, Petr Hamernik, Jan Jancura, Jaroslav Tulach
*/
public final class NbfsURLConnection extends URLConnection {
    /** Protocol name for this type of URL. */
    public static final String PROTOCOL = "nbfs"; // NOI18N

    /** url separator */
    private static final char SEPARATOR = '/';

    /** FileObject that we want to connect to. */
    private FileObject fo;


    /**
    * Create a new connection to a {@link FileObject}.
    * @param u URL of the connection. Please use {@link #encodeFileObject(FileObject)} to create the URL.
    */
    public NbfsURLConnection (URL u) {
        super (u);
    }


    /** Provides a URL to access a file object.
    * @param fo the file object
    * @return a URL using the correct syntax and {@link #PROTOCOL protocol}
    * @exception FileStateInvalidException if the file object is not valid (typically, if its filesystem is inconsistent or no longer present)
    */
    public static URL encodeFileObject (FileObject fo) throws FileStateInvalidException {
        return encodeFileObject (fo.getFileSystem (), fo);
    }

    /** Encodes fileobject into URL.
    * @param fs file system the object is on
    * @param fo file object
    * @return URL
    */
    static URL encodeFileObject (FileSystem fs, FileObject fo) {
        try {
            String fsName = encodeFileSystemName (fs.getSystemName());
            String fileName = fo.getPackageNameExt('/', '.');
            String url = PROTOCOL + ":/" + fsName + SEPARATOR + fileName; // NOI18N
            return new java.net.URL (url);
        } catch (java.net.MalformedURLException e) {
            throw new InternalError();
        }

    }

    /** Retrieves the file object specified by an internal URL.
    * @param u the url to decode
    * @return the file object that is represented by the URL, or <code>null</code> if the URL is somehow invalid or the file does not exist
    */
    public static FileObject decodeURL (URL u) {
        if (!u.getProtocol ().equals (PROTOCOL)) return null;

        // resource name
        String resourceName = u.getFile ();
        if (resourceName.startsWith ("/")) resourceName = resourceName.substring (1); // NOI18N

        // first part is FS name
        int first = resourceName.indexOf ('/');
        if (first == -1) return null;

        String fileSystemName = decodeFileSystemName (resourceName.substring (0, first));
        resourceName = resourceName.substring (first);
        FileSystem fsys = TopManager.getDefault().getRepository ().findFileSystem(fileSystemName);
        return (fsys == null) ? null : fsys.findResource (resourceName);
    }

    /* A method for connecting to a FileObject.
    */
    public void connect() throws IOException {
        fo = decodeURL (url);
        if (fo == null) {
            throw new IOException(NbBundle.getBundle(NbfsURLConnection.class).getString(
                                      "EXC_UnreachableFileObject")); // NOI18N
        }
    }

    /*
    * @return InputStream or given FileObject.
    */
    public InputStream getInputStream()
    throws IOException, UnknownServiceException {
        connect ();
        try {
            if (fo.isFolder()) return new FolderInputStream(fo);
            return fo.getInputStream();
        } catch (FileNotFoundException e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace();
            throw e;
        }
    }

    /*
    * @return OutputStream for given FileObject.
    */
    public OutputStream getOutputStream()
    throws IOException, UnknownServiceException {
        connect();
        if (fo.isFolder()) throw new UnknownServiceException();
        org.openide.filesystems.FileLock flock = fo.lock();
        return new FileObjectOutputStream(fo.getOutputStream(flock), flock);
    }

    /*
    * @return length of FileObject.
    */
    public int getContentLength() {
        try {
            connect();
            return (int)fo.getSize();
        } catch (IOException ex) {
            return 0;
        }
    }


    /** Get a header field (currently, content type only).
    * @param name the header name. Only <code>content-type</code> is guaranteed to be present.
    * @return the value (i.e., MIME type)
    */
    public String getHeaderField(String name) {
        if (name.equals("content-type")) { // NOI18N
            try {
                connect();
                if (fo.isFolder())
                    return "text/html"; // NOI18N
                else
                    return fo.getMIMEType ();
            }
            catch (IOException e) {
            }
        }
        return super.getHeaderField(name);
    }

    /** Encodes filesystem name.
    * @param fs original filesystem name
    * @return new encoded name
    */
    static String encodeFileSystemName (String fs) {
        StringBuffer sb = new StringBuffer ();
        for (int i = 0; i < fs.length (); i++) {
            switch (fs.charAt (i)) {
            case 'Q':
                sb.append ("QQ"); // NOI18N
                break;
            case '/':
                sb.append ("QB"); // NOI18N
                break;
            case ':':
                sb.append ("QC"); // NOI18N
                break;
            case '\\':
                sb.append ("QD"); // NOI18N
                break;
            default:
                sb.append (fs.charAt (i));
                break;
            }
        }
        return sb.toString ();
    }

    /** Decodes name to FS one.
    * @param name encoded name
    * @return original name of the filesystem
    */
    static String decodeFileSystemName (String name) {
        StringBuffer sb = new StringBuffer ();
        int i = 0;
        int len = name.length ();
        while (i < len) {
            char ch = name.charAt (i++);
            if (ch == 'Q' && i < len) {
                switch (name.charAt (i++)) {
                case 'B':
                    sb.append ('/');
                    break;
                case 'C':
                    sb.append (':');
                    break;
                case 'D':
                    sb.append ('\\');
                    break;
                default:
                    sb.append ('Q');
                    break;
                }
            } else {
                // not Q
                sb.append (ch);
            }
        }
        return sb.toString ();
    }


}

/*
 * Log
 *  10   src-jtulach1.9         1/12/00  Ian Formanek    NOI18N
 *  9    src-jtulach1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    src-jtulach1.7         9/30/99  Jan Jancura     Bug 2433
 *  7    src-jtulach1.6         7/25/99  Ian Formanek    Exceptions printed to 
 *       console only on "netbeans.debug.exceptions" flag
 *  6    src-jtulach1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    src-jtulach1.4         5/26/99  Ian Formanek    changed incorrect usage 
 *       of getBundle
 *  4    src-jtulach1.3         5/14/99  Jaroslav Tulach 
 *  3    src-jtulach1.2         5/14/99  Jaroslav Tulach Bugfixes.
 *  2    src-jtulach1.1         4/28/99  Jaroslav Tulach Null pointer buxfix
 *  1    src-jtulach1.0         3/26/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Petr Hamernik   lock throws IOException
 *  0    Tuborg    0.13        --/--/98 Jan Jancura     Bugfix
 *  0    Tuborg    0.14        --/--/98 Petr Hamernik   rename
 *  0    Tuborg    0.15        --/--/98 Petr Hamernik   bugfix - getHeaderField added
 *  0    Tuborg    0.16        --/--/98 Jaroslav Tulach conversion to new characters
 */
