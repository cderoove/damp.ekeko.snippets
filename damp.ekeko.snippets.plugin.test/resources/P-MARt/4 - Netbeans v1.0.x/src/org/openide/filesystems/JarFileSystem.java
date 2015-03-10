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

import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.actions.SystemAction;
import org.openide.util.enum.EmptyEnumeration;
import org.openide.util.NbBundle;
import org.openide.util.Task;

/** A virtual file system based on a JAR archive.
*
* @author Jan Jancura, Jaroslav Tulach, Petr Hamernik
*/
public class JarFileSystem extends AbstractFileSystem
            implements AbstractFileSystem.List, AbstractFileSystem.Info,
    AbstractFileSystem.Change, AbstractFileSystem.Attr {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -98124752801761145L;

    /** File system name prefix */
    private static final String JAR_FS = "JAR_FS "; // NOI18N

    /** a set of all folders in the archive (String, EntryInfo) */
    private transient Map allFolders;

    /**
    * Opened zip file of this file system is stored here or null.
    */
    private transient JarFile jar;

    /** Manifest file for jar
    */
    private transient Manifest manifest;

    /** Archive file.
    */
    private File root = new File ("."); // NOI18N

    /** Task that scans the content of jar.
    */
    private transient Task scanning;

    /** Watches modification on root file */
    private transient ModifiedWatcher modifiedWatcher;

    /** False, if ZipEntry last modification date/time reliability has not been yet tested.
    */
    private static boolean entryDatesTested = false;

    /** True, if ZipEntry date/time support is reliable. False if not.
    */
    private static boolean entryDatesOK = true;

    /**
    * Default constructor.
    */
    public JarFileSystem () {
        this.list = this;
        this.info = this;
        this.change = this;
        this.attr = this;
        // refreshing
        setRefreshTime(5000);
    }

    /**
    * Constructor that can provide own capability for the file system.
    * @param cap the capability
    */
    public JarFileSystem (FileSystemCapability cap) {
        this ();
        setCapability (cap);
    }

    /** Getter for entry.
    * @param file filename to scan for
    * @return entry for given file or null if the info does not exists
    */
    private final EntryInfo getEntryInfo (String file) {
        return (EntryInfo)getFolders ().get (file);
    }

    /** Getter for entry.
    */
    private final JarEntry getEntry (String file) {
        JarFile j = jar;
        JarEntry je = j == null ? null : j.getJarEntry (file);
        if (je == null) {
            return new JarEntry (file);
        } else {
            return je;
        }

    }

    /** Getter for all folders in the file.
    * @return set of names of all folders
    */
    private final Map getFolders () {
        if (modifiedWatcher != null) {
            //System.out.println("Rescnning from getFolders()"); // NOI18N
            modifiedWatcher.rescanIfNeeded();
        }

        getScanningTask ().waitFinished ();

        return allFolders;
    }

    /** Getter for the scanning task. If not running,
    * new task is started.
    */
    private Task getScanningTask () {
        if (scanning == null) {
            synchronized (this) {
                if (scanning == null) {
                    rescan ();
                }
            }
        }
        return scanning;
    }

    /** Rescans content of the JAR file.
    */
    private void rescan () {
        manifest = null;

        scanning = new Task (new Runnable () {
                                 public void run () {
                                     HashMap s = new HashMap (23);

                                     if (jar == null) {
                                         // ok, that is everything
                                         allFolders = s;
                                         return;
                                     }
                                     try {
                                         Enumeration en = jar.entries ();
                                         while (en.hasMoreElements ()) {
                                             JarEntry je = (JarEntry)en.nextElement ();
                                             String name = je.getName ();
                                             addFile (s, name);
                                         }
                                     } catch (IllegalStateException ex) {
                                         // if the jar file is closed
                                     }
                                     allFolders = s;
                                 }
                             });
        new Thread (scanning, "Parsing JAR: " + root).start (); // NOI18N
    }

    /** Adds a file together with all subdirectories to the hashtable
    * @param hash hashtable
    * @param name name of the file to add
    */
    private static void addFile (Map hash, String name) {
        // if the name ends with slash takes it away
        if (name.endsWith ("/")) { // NOI18N
            name = name.substring (0, name.length () - 1);
        }

        // work only with slashes
        name = name.replace ('\\', '/');

        while (!"".equals (name)) { // NOI18N
            int prev = name.lastIndexOf ('/');

            String parentName = prev < 0 ? "" : name.substring (0, prev);; // NOI18N


            // info for parent
            EntryInfo ei = (EntryInfo)hash.get (parentName);
            if (ei == null) {
                // if info is not registered, register new
                ei = new EntryInfo ();
                hash.put (parentName, ei);
            }

            // add parent
            ei.addChild (name.substring (prev + 1));

            // proceed to parent
            name = parentName;
        }

    }

    /** Get the JAR manifest.
    * It will be lazily initialized.
    * @return parsed manifest file for this archive
    */
    public Manifest getManifest() {
        if (manifest == null) {
            try {
                JarFile j = jar;
                manifest = j == null ? null : j.getManifest ();
            } catch (IOException ex) {
            }
            if (manifest == null) {
                manifest = new Manifest ();
            }
        }

        return manifest;
    }

    /**
    * Set name of the ZIP/JAR file.
    * @param aRoot path to new ZIP or JAR file
    * @throws IOException if the file is not valid
    */
    public synchronized void setJarFile (File aRoot) throws IOException, PropertyVetoException {
        //    System.out.println("setJarFile to:"+aRoot); // NOI18N
        if (!aRoot.exists ())
            FSException.io ("EXC_FileNotExists", aRoot.toString ()); // NOI18N
        if (!aRoot.canRead ())
            FSException.io ("EXC_CanntRead", aRoot.toString ()); // NOI18N
        if (!aRoot.isFile ())
            FSException.io ("EXC_NotValidFile", aRoot.toString ()); // NOI18N

        //    System.out.println("setJarFile #2"); // NOI18N
        String s;
        try {
            s = aRoot.getCanonicalPath ();
        } catch (IOException e) {
            FSException.io ("EXC_NotValidFile", aRoot.toString ()); // NOI18N
            s = null;
        }

        setSystemName (s);
        //    System.out.println("setJarFile, systemName:"+JAR_FS + s); // NOI18N

        try {
            jar = new JarFile (s);
        } catch (ZipException e) {
            FSException.io ("EXC_NotValidJarFile"); // NOI18N
        }
        //    System.out.println("setJarFile #3"); // NOI18N
        root = new File (s);

        rescan ();

        //    System.out.println("setJarFile #4:"+root); // NOI18N
        firePropertyChange ("root", null, refreshRoot ()); // NOI18N
        // watch the modifications of root file
        if (modifiedWatcher == null) {
            modifiedWatcher = new ModifiedWatcher();
            //new Thread(modifiedWatcher).start();
        }
    }

    /** Get the file path for the ZIP or JAR file.
    * @return the file path
    */
    public File getJarFile () { // JST
        return root;
    }

    /*
    * Provides name of the system that can be presented to the user.
    * @return user presentable name of the file system
    */
    public String getDisplayName () {
        if (root == null) return getString ("JAR_NotValidJarFileSystem");
        return root.getName();
    }

    /** This file system is read-only.
    * @return <code>true</code>
    */
    public boolean isReadOnly () {
        return true;
    }

    /** Prepare environment for external compilation or execution.
    * <P>
    * Adds name of the ZIP/JAR file, if it has been set, to the class path.
    */
    public void prepareEnvironment (Environment env) {
        if (root != null) {
            env.addClassPath (root.toString ());
        }
    }

    /** Initializes the root of FS.
    */
    private void readObject (ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        try {
            setJarFile (root);
        } catch (PropertyVetoException ex) {
            throw new IOException (ex.getMessage ());
        }
    }

    //
    // List
    //

    private static final String[] EMPTY_ARRAY = {};

    /* Scans children for given name
    */
    public String[] children (String name) {
        EntryInfo ei = getEntryInfo (name);
        if (ei == null) {
            return EMPTY_ARRAY;
        }
        Collection l = ei.getChildren ();
        return l == null ? EMPTY_ARRAY : (String[])l.toArray (EMPTY_ARRAY);
    }

    //
    // Change
    //

    /* Creates new folder named name.
    * @param name name of folder
    * @throws IOException if operation fails
    */
    public void createFolder (String name) throws java.io.IOException {
        throw new IOException ();
    }

    /* Create new data file.
    *
    * @param name name of the file
    *
    * @return the new data file object
    * @exception IOException if the file cannot be created (e.g. already exists)
    */
    public void createData (String name) throws IOException {
        throw new IOException ();
    }

    /* Renames a file.
    *
    * @param oldName old name of the file
    * @param newName new name of the file
    */
    public void rename(String oldName, String newName) throws IOException {
        throw new IOException ();
    }

    /* Delete the file.
    *
    * @param name name of file
    * @exception IOException if the file could not be deleted
    */
    public void delete (String name) throws IOException {
        throw new IOException ();
    }

    //
    // Info
    //

    /*
    * Get last modification time.
    * @param name the file to test
    * @return the date
    */
    public java.util.Date lastModified(String name) {
        if (!entryDatesTested) {
            JarEntry je = getEntry(name);
            // JDK 1.3 for WinNT bug test: Jar/ZipEntries have date set to the autumn of 1979.
            entryDatesOK = new java.util.Date(je.getTime()).getYear() >= 1980;
            entryDatesTested = true;
        }
        if (entryDatesOK) {
            return new java.util.Date (getEntry (name).getTime ());
        } else {
            return new java.util.Date(this.root.lastModified());
        }
    }

    /* Test if the file is folder or contains data.
    * @param name name of the file
    * @return true if the file is folder, false otherwise
    */
    public boolean folder (String name) {
        return "".equals (name) || getEntryInfo (name) != null; // NOI18N
    }

    /* Test whether this file can be written to or not.
    * @param name the file to test
    * @return <CODE>true</CODE> if file is read-only
    */
    public boolean readOnly (String name) {
        return true;
    }

    /** Get the MIME type of the file.
    * Uses {@link FileUtil#getMIMEType}.
    *
    * @param name the file to test
    * @return the MIME type textual representation, e.g. <code>"text/plain"</code>
    */
    public String mimeType (String name) {
        int i = name.lastIndexOf ('.');
        String s;
        try {
            s = FileUtil.getMIMEType (name.substring (i + 1));
        } catch (IndexOutOfBoundsException e) {
            s = null;
        }

        return s == null ? "content/unknown" : s; // NOI18N
    }

    /* Get the size of the file.
    *
    * @param name the file to test
    * @return the size of the file in bytes or zero if the file does not contain data (does not
    *  exist or is a folder).
    */
    public long size (String name) {
        return getEntry (name).getSize ();
    }

    /* Get input stream.
    *
    * @param name the file to test
    * @return an input stream to read the contents of this file
    * @exception FileNotFoundException if the file does not exists or is invalid
    */
    public InputStream inputStream (String name) throws java.io.FileNotFoundException {
        InputStream is = null;
        try {
            JarFile j = jar;
            if (j != null) {
                JarEntry je = j.getJarEntry (name);
                if (je != null) {
                    is = j.getInputStream (je);
                }
            }
        } catch (java.io.FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new java.io.FileNotFoundException (e.getMessage ());
        }

        if (is == null) {
            throw new java.io.FileNotFoundException (name);
        }

        return is;
    }

    /* Get output stream.
    *
    * @param name the file to test
    * @return output stream to overwrite the contents of this file
    * @exception IOException if an error occures (the file is invalid, etc.)
    */
    public OutputStream outputStream (String name) throws java.io.IOException {
        throw new IOException ();
    }

    /* Does nothing.
    *
    * @param name name of the file
    */
    public void lock (String name) throws IOException {
        FSException.io ("EXC_CannotLock", name, getDisplayName (), name); // NOI18N
    }

    /* Unlock the file. Does nothing.
    *
    * @param name name of the file
    */
    public void unlock (String name) {
    }

    /* Does nothing.
    *
    * @param name the file to mark
    */
    public void markUnimportant (String name) {
    }

    /* Get the file attribute with the specified name.
    * @param name the file
    * @param attrName name of the attribute
    * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
    */
    public Object readAttribute(String name, String attrName) {
        Attributes attr = getManifest ().getAttributes (name);
        return attr == null ? null : attr.get (attrName);
    }

    /* Set the file attribute with the specified name.
    * @param name the file
    * @param attrName name of the attribute
    * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular file systems may or may not use serialization to store attribute values.
    * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
    */
    public void writeAttribute(String name, String attrName, Object value) throws IOException {
        throw new IOException ();
    }

    /* Get all file attribute names for the file.
    * @param name the file
    * @return enumeration of keys (as strings)
    */
    public Enumeration attributes(String name) {
        Attributes attr = getManifest ().getAttributes (name);
        if (attr != null) {
            return Collections.enumeration (attr.keySet ());
        } else {
            return EmptyEnumeration.EMPTY;
        }
    }

    /* Called when a file is renamed, to appropriatelly update its attributes.
    * <p>
    * @param oldName old name of the file
    * @param newName new name of the file
    */
    public void renameAttributes (String oldName, String newName) {
    }

    /* Called when a file is deleted to also delete its attributes.
    *
    * @param name name of the file
    */
    public void deleteAttributes (String name) {
    }

    /** Close the jar file when we go away...*/
    protected void finalize () throws Throwable {
        super.finalize();
        if (jar != null)
            jar.close();
    }

    /** Info about one entry. Can be tested to be folder and if so the
    * array of children names can be obtained.
    */
    private static class EntryInfo extends Object {
        /** vector with children names (String) 
         * @associates String*/
        private Collection children;

        /** Adds new child.
        * @param name name of child to add
        */
        void addChild (String name) {
            if (children == null) {
                children = new HashSet ();
            }
            children.add (name);
        }

        /** Test if the entry represents a folder (has children)
        * @return true if so
        */
        public boolean isFolder () {
            return children != null;
        }

        /** Return vector with children names.
        * @return vector of Strings
        */
        public Collection getChildren () {
            return children;
        }
    } // end of EntryInfo inner class

    /** Periodically tests the root jar file for modifications
    * and runs rescanning task if modifications are discovered.
    */ 
    private final class ModifiedWatcher implements Runnable {
        /** Date of last modification */
        long lastModification = 0;

        ModifiedWatcher () {
            synchronized (JarFileSystem.this) {
                File rootJar = getJarFile();
                if (rootJar != null)
                    lastModification = rootJar.lastModified();
            }
        }

        public synchronized void run () {
            while (true) {
                try {
                    wait(10000);
                } catch (InterruptedException exc) {
                    continue;
                }
                rescanIfNeeded();
            }
        }

        /** Starts rerscanning task if root file modified */
        void rescanIfNeeded () {
            synchronized (JarFileSystem.this) {
                File rootJar = getJarFile();
                if (rootJar == null)
                    return;
                long curModif = rootJar.lastModified();
                if (curModif != lastModification) {
                    lastModification = curModif;
                    JarFileSystem.this.scanning = null;
                    try {
                        //System.out.println("Refreshing from rescanIfNeeded..."); // NOI18N
                        if (jar != null)
                            jar.close();
                        jar = new JarFile(root);
                        getScanningTask().waitFinished();
                    } catch (Exception exc) {
                        if (System.getProperty ("netbeans.debug.exceptions") != null) exc.printStackTrace();
                    }
                    /*JarFileSystem.this.firePropertyChange(
                      "root", JarFileSystem.this.root, refreshRoot ());*/ // NOI18N
                }
            }
        }
    } // end of ModifiedWatcher


    /*
      public static void main (String[] args) throws Exception {
        JarFileSystem fs = new JarFileSystem ();
        fs.setJarFile (new File (args[0]));
        FileObject fo = fs.getRoot ();
        FileObject[] arr = fo.getChildren ();
        for (int i = 0; i < arr.length; i++) {
          System.out.println ("  " + arr[i]);
        }

        //    cycle (fo);
        //    createData (fo, args[1], args[2]);
        //   createFolder (fo, args[1]);
        // delete (fs.findResource (args[1]));
      }
      */

}


/*
 * Log
 *  21   Gandalf-post-FCS1.19.2.0    3/29/00  Svatopluk Dedic Workaround  for JDK's 
 *       ZipEntry's bug
 *  20   src-jtulach1.19        1/13/00  Ian Formanek    NOI18N
 *  19   src-jtulach1.18        1/12/00  Ian Formanek    NOI18N
 *  18   src-jtulach1.17        1/9/00   Jaroslav Tulach #5059
 *  17   src-jtulach1.16        12/30/99 Jaroslav Tulach New dialog for 
 *       notification of exceptions.
 *  16   src-jtulach1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   src-jtulach1.14        10/7/99  Jaroslav Tulach #4316
 *  14   src-jtulach1.13        9/24/99  Jaroslav Tulach #3735
 *  13   src-jtulach1.12        7/25/99  Ian Formanek    Exceptions printed to 
 *       console only on "netbeans.debug.exceptions" flag
 *  12   src-jtulach1.11        6/10/99  Jaroslav Tulach Capabilities can be 
 *       passed to constructor.
 *  11   src-jtulach1.10        6/10/99  David Simonek   closing jar on 
 *       finalize..
 *  10   src-jtulach1.9         6/10/99  David Simonek   refreshing now ok
 *  9    src-jtulach1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    src-jtulach1.7         6/8/99   David Simonek   bugfixes....
 *  7    src-jtulach1.6         5/24/99  Jaroslav Tulach Survives backslashes in 
 *       the zip.
 *  6    src-jtulach1.5         5/14/99  Jaroslav Tulach Serialization works.
 *  5    src-jtulach1.4         3/26/99  Jesse Glick     [JavaDoc]
 *  4    src-jtulach1.3         3/26/99  Jaroslav Tulach 
 *  3    src-jtulach1.2         3/26/99  Jaroslav Tulach Refresh & Bundles
 *  2    src-jtulach1.1         3/26/99  Jaroslav Tulach 
 *  1    src-jtulach1.0         3/26/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.13        --/--/98 Jaroslav Tulach fire root change
 *  0    Tuborg    0.14        --/--/98 Jaroslav Tulach environment support
 *  0    Tuborg    0.15        --/--/98 Petr Hamernik   attributes implementation (manifest file)
 *  0    Tuborg    0.16        --/--/98 Jaroslav Tulach findResource added
 *  0    Tuborg    0.20        --/--/98 Jaroslav Tulach speed up
 *  0    Tuborg    0.21        --/--/98 Jan Jancura     system name changed
 *  0    Tuborg    0.22        --/--/98 Petr Hamernik   finding manifest file improved.
 */
