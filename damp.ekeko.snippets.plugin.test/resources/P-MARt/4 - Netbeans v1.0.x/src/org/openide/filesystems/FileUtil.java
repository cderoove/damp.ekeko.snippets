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

package org.openide.filesystems;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/** Common utilities for handling files.
 * This is a dummy class; all methods are static.
*
* @author Petr Hamernik
* @version 0.12, May 12, 1998
*/
public final class FileUtil extends Object {
    private FileUtil() {}

    /** Copies stream of files.
    * @param is input stream
    * @param os output stream
    */
    public static void copy (InputStream is, OutputStream os) throws IOException {
        final byte[] BUFFER = new byte[4096];
        int len;

        for (;;) {
            len = is.read (BUFFER);
            if (len == -1) return;
            os.write (BUFFER, 0, len);
        }
    }

    /** Copies file to the selected folder.
     * This implementation simply copies the file by stream content.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @param newExt extension of destination file
    * @return the created file object in the destination folder
    * @exception IOException if <code>destFolder</code> is not a folder or does not exist; the destination file already exists; or
    *      another critical error occurs during copying
    */
    static FileObject copyFileImpl (
        FileObject source, FileObject destFolder, String newName, String newExt
    ) throws IOException {
        FileObject dest = destFolder.createData(newName, newExt);

        FileLock lock = null;
        InputStream bufIn = null;
        OutputStream bufOut = null;
        try {
            lock = dest.lock();
            bufIn = source.getInputStream();
            bufOut = dest.getOutputStream(lock);

            copy (bufIn, bufOut);
            copyAttributes (source, dest);

        }
        finally {
            if (bufIn != null)
                bufIn.close();
            if (bufOut != null)
                bufOut.close();

            if (lock != null)
                lock.releaseLock();
        }

        return dest;
    }


    //
    // public methods
    //


    /** Copies file to the selected folder.
    * This implementation simply copies the file by stream content.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @param newExt extension of destination file
    * @return the created file object in the destination folder
    * @exception IOException if <code>destFolder</code> is not a folder or does not exist; the destination file already exists; or
    *      another critical error occurs during copying
    */
    public static FileObject copyFile(FileObject source, FileObject destFolder,
                                      String newName, String newExt) throws IOException {
        return source.copy (destFolder, newName, newExt);
    }

    /** Copies file to the selected folder.
    * This implementation simply copies the file by stream content.
    * Uses the extension of the source file.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @return the created file object in the destination folder
    * @exception IOException if <code>destFolder</code> is not a folder or does not exist; the destination file already exists; or
    *      another critical error occurs during copying
    */
    public static FileObject copyFile(FileObject source, FileObject destFolder,
                                      String newName) throws IOException {
        return copyFile(source, destFolder, newName, source.getExt());
    }

    /** Moves file to the selected folder.
     * This implementation uses a copy-and-delete mechanism, and automatically uses the necessary lock.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @return new file object
    * @exception IOException if either the {@link #copyFile copy} or {@link FileObject#delete delete} failed
    */
    public static FileObject moveFile(FileObject source, FileObject destFolder,
                                      String newName) throws IOException {
        FileLock lock = null;
        try {
            lock = source.lock();
            return source.move (lock, destFolder, newName, source.getExt ());
        }
        finally {
            if (lock != null)
                lock.releaseLock();
        }
    }

    /** Creates a folder on given file system. The name of
    * folder can be composed as resource name (e. g. org/netbeans/myfolder)
    * and the method scans which of folders has already been created 
    * and which not. If successful the caller can be sure that the folder 
    * is there and receives a reference to it.
    *
    * @param folder to begin with creation at
    * @param name name of folder as a resource
    * @return the folder for given name
    * @exception IOException if the creation fails
    */
    public static FileObject createFolder (FileObject folder, String name)
    throws IOException {
        StringTokenizer st = new StringTokenizer (name, "/"); // NOI18N
        while (st.hasMoreElements ()) {
            name = st.nextToken ();
            if (name.length () > 0) {
                FileObject f = folder.getFileObject (name);
                if (f == null) {
                    f = folder.createFolder (name);
                }
                folder = f;
            }
        }
        return folder;
    }

    /** Creates a data file on given file system. The name of
    * data file can be composed as resource name (e. g. org/netbeans/myfolder/mydata )
    * and the method scans which of folders has already been created 
    * and which not. 
    *
    * @param folder to begin with creation at
    * @param name name of data file as a resource
    * @return the data file for given name
    * @exception IOException if the creation fails
    */
    public static FileObject createData (FileObject folder, String name)
    throws IOException {
        String foldername, dataname, fname, ext;
        int index = name.lastIndexOf('/');
        FileObject data;

        // names with '/' on the end are not valid
        if (index >= name.length()) throw new IOException("Wrong file name."); // NOI18N

        // if name contains '/', create necessary folder first
        if (index != -1) {
            foldername = name.substring(0, index);
            dataname = name.substring(index + 1);
            folder = createFolder(folder, foldername);
        } else {
            dataname = name;
        }

        // create data
        index = dataname.lastIndexOf('.');
        if (index != -1) {
            fname = dataname.substring(0, index);
            ext = dataname.substring(index + 1);
        } else {
            fname = dataname;
            ext = ""; // NOI18N
        }

        data = folder.getFileObject (fname, ext);
        if (data == null) {
            data = folder.createData(fname, ext);
        }
        return data;
    }

    /** transient attributes which should not be copied
    * of type Set<String>
    * @associates String
    */
    static final Set transientAttributes = new HashSet ();
    static {
        transientAttributes.add ("templateWizardURL"); // NOI18N
        transientAttributes.add ("templateWizardIterator"); // NOI18N
        transientAttributes.add ("templateWizardDescResource"); // NOI18N
        transientAttributes.add ("SystemFileSystem.localizingBundle"); // NOI18N
    }
    /** Copies attributes from one file to another.
    * Note: several special attributes will not be copied, as they should
    * semantically be transient. These include attributes used by the
    * template wizard (but not the template atttribute itself).
    * @param source source file object
    * @param dest destination file object
    * @exception IOException if the copying failed
    */
    public static void copyAttributes (FileObject source, FileObject dest) throws IOException {
        Enumeration attrKeys = source.getAttributes();
        while (attrKeys.hasMoreElements()) {
            String key = (String) attrKeys.nextElement();
            if (transientAttributes.contains (key)) continue;
            Object value = source.getAttribute(key);
            if (value != null) {
                dest.setAttribute(key, value);
            }
        }
    }

    /** Extract jar file into folder represented by file object. If the JAR contains
    * files with name filesystem.attributes, it is assumed that these files 
    * has been created by DefaultAttributes implementation and the content
    * of these files is treated as attributes and added to extracted files.
    * <p><code>META-INF/</code> directories are skipped over.
    *
    * @param fo file object of destination folder
    * @param is input stream of jar file
    * @exception IOException if the extraction fails
    */
    public static void extractJar (FileObject fo, InputStream is) throws IOException {
        JarInputStream jis;
        JarEntry je;

        // files with extended attributes (name, DefaultAttributes.Table)
        HashMap attributes = new HashMap (7);

        jis = new JarInputStream(is);

        while ((je = jis.getNextJarEntry()) != null) {
            String name = je.getName();
            if (name.toLowerCase ().startsWith ("meta-inf/")) continue; // NOI18N

            if (je.isDirectory ()) {
                createFolder (fo, name);
                continue;
            }

            if (DefaultAttributes.acceptName (name)) {
                // file with extended attributes
                DefaultAttributes.Table table = DefaultAttributes.loadTable (jis);
                attributes.put (name, table);
            } else {
                // copy the file
                FileObject fd = createData(fo, name);
                FileLock lock = fd.lock ();
                try {
                    OutputStream os = fd.getOutputStream (lock);
                    try {
                        copy (jis, os);
                    } finally {
                        os.close ();
                    }
                } finally {
                    lock.releaseLock ();
                }
            }
        }

        //
        // apply all extended attributes
        //

        Iterator it = attributes.entrySet ().iterator ();
        while (it.hasNext ()) {
            Map.Entry entry = (Map.Entry)it.next ();

            String fileName = (String)entry.getKey ();
            int last = fileName.lastIndexOf ('/');
            String dirName;
            if (last != -1)
                dirName = fileName.substring (0, last + 1);
            else
                dirName = ""; // NOI18N
            String prefix = fo.isRoot () ? dirName : fo.getPackageName ('/') + '/' + dirName;

            DefaultAttributes.Table t = (DefaultAttributes.Table)entry.getValue ();
            Iterator files = t.keySet ().iterator ();
            while (files.hasNext ()) {
                String orig = (String)files.next ();
                String fn = prefix + orig;
                FileObject obj = fo.getFileSystem ().findResource (fn);

                if (obj == null) {
                    continue;
                }

                Enumeration attrEnum = t.attrs (orig);
                while (attrEnum.hasMoreElements ()) {
                    // iterate thru all arguments
                    String attrName = (String)attrEnum.nextElement ();
                    // Note: even transient attributes set here!
                    Object value = t.getAttr (orig, attrName);
                    if (value != null) {
                        obj.setAttribute (attrName, value);
                    }
                }
            }
        }

    } // extractJar


    /** Gets the extension of a specified file name. The extension is
    * everything after the last dot.
    *
    * @param fileName name of the file
    * @return extension of the file (or <code>""</code> if it had none)
    */
    public static String getExtension(String fileName) {
        int index = fileName.lastIndexOf("."); // NOI18N
        if (index == -1)
            return ""; // NOI18N
        else
            return fileName.substring(index + 1);
    }

    /** Finds an unused file name similar to that requested in the same folder.
     * The specified file name is used if that does not yet exist.
     * Otherwise, the first available name of the form <code>basename_nnn.ext</code> (counting from one) is used.
     *
     * <p><em>Caution:</em> this method does not lock the parent folder
     * to prevent race conditions: i.e. it is possible (though unlikely)
     * that the resulting name will have been created by another thread
     * just as you were about to create the file yourself (if you are,
     * in fact, intending to create it just after this call). Since you
     * cannot currently lock a folder against child creation actions,
     * the safe approach is to use a loop in which a free name is
     * retrieved; an attempt is made to {@link FileObject#createData create}
     * that file; and upon an <code>IOException</code> during
     * creation, retry the loop up to a few times before giving up.
     *
    * @param df parent folder
    * @param name preferred base name of file
    * @param ext extension to use
    * @return a free file name */
    public static String findFreeFileName (
        FileObject folder, String name, String ext
    ) {
        if (folder.getFileObject (name, ext) == null) {
            return name;
        }
        for (int i = 1;;i++) {
            String destName = name + "_"+i; // NOI18N
            if (folder.getFileObject (destName, ext) == null) {
                return destName;
            }
        }
    }

    /** Finds an unused folder name similar to that requested in the same parent folder.
     * <p>See caveat for <code>findFreeFileName</code>.
     * @see #findFreeFileName findFreeFileName
    * @param df parent folder
    * @param name preferred folder name
    * @return a free folder name
    */
    public static String findFreeFolderName (
        FileObject folder, String name
    ) {
        if (folder.getFileObject (name) == null) {
            return name;
        }
        for (int i = 1;;i++) {
            String destName = name + "_"+i; // NOI18N
            if (folder.getFileObject (destName) == null) {
                return destName;
            }
        }
    }

    // note: "sister" is preferred in English, please don't ask me why --jglick // NOI18N
    /** Finds brother file with same base name but different extension.
    * @param fo the file to find the brother for or <CODE>null</CODE>
    * @param ext extension for the brother file
    * @return the brother file (the one with requested extension) or
    *   <CODE>null</CODE> if the brother file does not exists or the original file was <CODE>null</CODE>
    */
    public static FileObject findBrother (FileObject fo, String ext) {
        if (fo == null) return null;
        FileObject parent = fo.getParent ();
        if (parent == null) return null;

        return parent.getFileObject (fo.getName (), ext);
    }

    /** Obtain MIME type for a well-known extension.
    * @param ext the extension: <code>"jar"</code>, <code>"zip"</code>, etc. Case is unimportant.
    * @return the MIME type for the extension, or <code>null</code> if the extension is unrecognized
    */
    public static String getMIMEType (String ext) {
        return (String)map.get (ext.toLowerCase());
    }

    /* mapping of file extensions to content-types */
    private static java.util.Dictionary map = new java.util.Hashtable();

    /**
     * Register MIME type for a new extension.
     * @param ext the file extension (case is unimportant)
     * @param mimeType the new MIME type
     * @throws IllegalArgumentException if this extension was already registered with a <em>different</em> MIME type
     * @see #getMIMEType
     */
    public static void setMIMEType(String ext, String mimeType) {
        String kk=ext.toLowerCase();
        synchronized (map) {
            String old=(String)map.get(kk);
            if (old == null) {
                map.put(kk, mimeType);
            } else {
                if (!old.equals(mimeType))
                    throw new IllegalArgumentException
                    ("Cannot overwrite existing MIME type mapping for extension `" + // NOI18N
                     kk + "' with " + mimeType + " (was " + old + ")"); // NOI18N
                // else do nothing
            }
        }
    }

    static {
        setMIMEType("", "content/unknown"); // NOI18N
        setMIMEType("uu", "application/octet-stream"); // NOI18N
        setMIMEType("exe", "application/octet-stream"); // NOI18N
        setMIMEType("ps", "application/postscript"); // NOI18N
        setMIMEType("zip", "application/zip"); // NOI18N
        setMIMEType("class", "application/octet-stream"); // Sun uses application/java-vm // NOI18N
        setMIMEType("jar", "application/x-jar"); // NOI18N
        setMIMEType("sh", "application/x-shar"); // NOI18N
        setMIMEType("tar", "application/x-tar"); // NOI18N
        setMIMEType("snd", "audio/basic"); // NOI18N
        setMIMEType("au", "audio/basic"); // NOI18N
        setMIMEType("wav", "audio/x-wav"); // NOI18N
        setMIMEType("gif", "image/gif"); // NOI18N
        setMIMEType("jpg", "image/jpeg"); // NOI18N
        setMIMEType("jpeg", "image/jpeg"); // NOI18N
        setMIMEType("htm", "text/html"); // NOI18N
        setMIMEType("html", "text/html"); // NOI18N
        setMIMEType("xml", "text/xml"); // NOI18N
        setMIMEType("xsl", "text/xml"); // NOI18N
        setMIMEType("dtd", "text/x-dtd"); // NOI18N
        setMIMEType("text", "text/plain"); // NOI18N
        setMIMEType("c", "text/plain"); // NOI18N
        setMIMEType("cc", "text/plain"); // NOI18N
        setMIMEType("c++", "text/plain"); // NOI18N
        setMIMEType("h", "text/plain"); // NOI18N
        setMIMEType("pl", "text/plain"); // NOI18N
        setMIMEType("txt", "text/plain"); // NOI18N
        setMIMEType("properties", "text/plain"); // NOI18N
        setMIMEType("java", "text/x-java"); // NOI18N
        // mime types from Jetty web server
        setMIMEType("ra", "audio/x-pn-realaudio"); // NOI18N
        setMIMEType("ram", "audio/x-pn-realaudio"); // NOI18N
        setMIMEType("rm", "audio/x-pn-realaudio"); // NOI18N
        setMIMEType("rpm", "audio/x-pn-realaudio"); // NOI18N
        setMIMEType("mov", "video/quicktime"); // NOI18N
        setMIMEType("jsp", "text/plain"); // NOI18N
    }

}


/*
 * Log
 *  33   Gandalf   1.32        1/20/00  Jesse Glick     Bugfix: after the first 
 *       file entry in a filesystem.attributes which does not exist, the 
 *       remainder of the attributes set is skipped even for files which do 
 *       exist.
 *  32   Gandalf   1.31        1/14/00  Jesse Glick     Transient file 
 *       attributes.
 *  31   Gandalf   1.30        1/13/00  Ian Formanek    NOI18N
 *  30   Gandalf   1.29        1/12/00  Ian Formanek    NOI18N
 *  29   Gandalf   1.28        1/6/00   Jesse Glick     #5148: extractJar should
 *       not create meta-inf/ dirs.
 *  28   Gandalf   1.27        12/22/99 Jesse Glick     Bugfix: now possible to 
 *       set attributes on folders/files at top level of the JAR (null-package 
 *       filesystem.attributes).
 *  27   Gandalf   1.26        12/9/99  Jaroslav Tulach #4347
 *  26   Gandalf   1.25        11/25/99 Jaroslav Tulach FileUtil.copy is public.
 *  25   Gandalf   1.24        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  24   Gandalf   1.23        10/1/99  Jaroslav Tulach FileObject.move & 
 *       FileObject.copy
 *  23   Gandalf   1.22        7/28/99  Libor Kramolis  
 *  22   Gandalf   1.21        7/21/99  Libor Kramolis  
 *  21   Gandalf   1.20        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  20   Gandalf   1.19        6/8/99   Jaroslav Tulach extractJar
 *  19   Gandalf   1.18        6/1/99   Petr Jiricka    Added MIME type for 
 *       properties files
 *  18   Gandalf   1.17        5/26/99  Martin Ryzl     createData added
 *  17   Gandalf   1.16        5/25/99  Libor Kramolis  
 *  16   Gandalf   1.15        5/20/99  Jaroslav Tulach First version of 
 *       MultiFileSystem.
 *  15   Gandalf   1.14        5/17/99  Miloslav Metelka copyFile with extension
 *  14   Gandalf   1.13        5/11/99  Jesse Glick     Removed obsoleted 
 *       comment.
 *  13   Gandalf   1.12        5/6/99   Petr Jiricka    Added MIME types used by
 *       Jetty web server
 *  12   Gandalf   1.11        5/5/99   Petr Jiricka    Added MIME type for 
 *       class files - application/octet-stream
 *  11   Gandalf   1.10        3/31/99  Jaroslav Tulach 
 *  10   Gandalf   1.9         3/15/99  Jesse Glick     Utility classes ought 
 *       not have public constructors.
 *  9    Gandalf   1.8         3/11/99  Jaroslav Tulach Works with plain 
 *       document.
 *  8    Gandalf   1.7         2/18/99  Jesse Glick     Looking over list of 
 *       MIME types...
 *  7    Gandalf   1.6         2/12/99  Jesse Glick     Made setMIMEType() 
 *       idempotent: can call several times w/ same args as long as there is no 
 *       conflict.
 *  6    Gandalf   1.5         2/12/99  Jesse Glick     Made setSuffix() into 
 *       public setMIMEType().
 *  5    Gandalf   1.4         2/8/99   Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         2/5/99   Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/4/99   Petr Hamernik   setting of extended file
 *       attributes doesn't require FileLock
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach findFreeFolderName added
 *  0    Tuborg    0.12        --/--/98 Jaroslav Tulach getMimeType
 */
