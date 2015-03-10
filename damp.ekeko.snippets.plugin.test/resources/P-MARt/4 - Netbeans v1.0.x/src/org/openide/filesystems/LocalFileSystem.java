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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.enum.SingletonEnumeration;
import org.openide.util.enum.SequenceEnumeration;


/** Local file system. Provides access to files on local disk.
*/
public class LocalFileSystem extends AbstractFileSystem
            implements AbstractFileSystem.List, AbstractFileSystem.Info,
    AbstractFileSystem.Change {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5355566113542272442L;
    /** refresh time (30s) */
    private static final int REFRESH_TIME = 15000;

    /** root file */
    private File rootFile = new File ("."); // NOI18N

    /** is read only */
    private boolean readOnly;

    /** Constructor.
    */
    public LocalFileSystem () {
        info = this;
        change = this;
        DefaultAttributes a = new DefaultAttributes (info, change, this);
        attr = a;
        list = a;
        setRefreshTime (REFRESH_TIME);
    }

    /** Constructor. Allows user to provide own capabilities
    * for this file system.
    * @param cap capabilities for this file system
    */
    public LocalFileSystem (FileSystemCapability cap) {
        this ();
        setCapability (cap);
    }

    /* Human presentable name */
    public String getDisplayName() {
        if(!isValid())
            return getString("LAB_FileSystemInvalid", rootFile.toString ());
        else
            return getString ("LAB_FileSystemValid", rootFile.toString ());
    }

    /** Set the root directory of the file system.
    * @param r file to set root to
    * @exception PropertyVetoException if the value if vetoed by someone else (usually
    *    by the {@link org.openide.filesystems.Repository Repository})
    * @exception IOException if the root does not exists or some other error occured
    */
    public synchronized void setRootDirectory (File r) throws PropertyVetoException, IOException {
        if (!r.exists() || r.isFile ()) {
            FSException.io ("EXC_RootNotExist", r.toString ()); // NOI18N
        }

        setSystemName(computeSystemName (r));

        rootFile = r;

        firePropertyChange("root", null, refreshRoot ()); // NOI18N
    }

    /** Get the root directory of the file system.
     * @return root directory
    */
    public File getRootDirectory () {
        return rootFile;
    }

    /** Set whether the file system should be read only.
     * @param flag <code>true</code> if it should
    */
    public void setReadOnly(boolean flag) {
        if (flag != readOnly) {
            readOnly = flag;
            firePropertyChange (PROP_READ_ONLY, new Boolean (!flag), new Boolean (flag));
        }
    }

    /* Test whether file system is read only.
     * @return <true> if file system is read only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /** Prepare environment by adding the root directory of the file system to the class path.
    * @param environment the environment to add to
    */
    public void prepareEnvironment(FileSystem.Environment environment) {
        environment.addClassPath(rootFile.toString ());
    }

    /** Compute the system name of this file system for a given root directory.
    * <P>
    * The default implementation simply returns the filename separated by slashes.
    * @see FileSystem#setSystemName
    * @param rootFile root directory for the filesystem
    * @return system name for the filesystem
    */
    protected String computeSystemName (File rootFile) {
        return rootFile.toString ().replace(File.separatorChar, '/');
    }

    /** Creates file for given string name.
    * @param name the name
    * @return the file
    */
    private File getFile (String name) {
        return new File (rootFile, name);
    }

    //
    // List
    //

    /* Scans children for given name
    */
    public String[] children (String name) {
        File f = getFile (name);
        if (f.isDirectory ()) {
            return f.list ();
        } else {
            return null;
        }
    }

    //
    // Change
    //

    /* Creates new folder named name.
    * @param name name of folder
    * @throws IOException if operation fails
    */
    public void createFolder (String name) throws java.io.IOException {
        File f = getFile (name);
        Object[] errorParams = new Object[] {
                                   f.getName (),
                                   getDisplayName (),
                                   f.toString ()
                               };

        if (name.equals ("")) { // NOI18N
            FSException.io ("EXC_CannotCreateF", errorParams); // NOI18N
        }

        if (f.exists()) {
            FSException.io ("EXC_FolderAlreadyExist", errorParams); // NOI18N
        }

        boolean b = createRecursiveFolder(f);
        if (!b) {
            FSException.io ("EXC_CannotCreateF", errorParams); // NOI18N
        }
    }


    /** Creates new folder and all necessary subfolders
    *  @param f folder to create
    *  @return <code>true</code> if the file exists when returning from this method
    */
    private static boolean createRecursiveFolder(File f) {
        if (f.exists()) return true;
        if (!f.isAbsolute())
            f = f.getAbsoluteFile();
        String par = f.getParent();
        if (par == null) return false;
        if (!createRecursiveFolder(new File(par))) return false;
        f.mkdir();
        return f.exists();
    }


    /* Create new data file.
    *
    * @param name name of the file
    *
    * @return the new data file object
    * @exception IOException if the file cannot be created (e.g. already exists)
    */
    public void createData (String name) throws IOException {
        File f = getFile (name);
        Object[] errorParams = new Object[] {
                                   f.getName (),
                                   getDisplayName (),
                                   f.toString (),
                               };

        if (!f.createNewFile ()) {
            FSException.io ("EXC_DataAlreadyExist", errorParams); // NOI18N
        }
        /* JST: Maybe handled by createNewFile, but probably
            if (!tmp.exists())
              throw new IOException(MessageFormat.format (LocalFileSystem.getString("EXC_CannotCreateD"), errorParams));
        */
    }

    /* Renames a file.
    *
    * @param oldName old name of the file
    * @param newName new name of the file
    */
    public void rename(String oldName, String newName) throws IOException {
        File of = getFile (oldName);
        File nf = getFile (newName);

        if (nf.exists() || !of.renameTo (nf)) {
            FSException.io ("EXC_CannotRename", oldName, getDisplayName (), newName); // NOI18N
        }
    }

    /* Delete the file.
    *
    * @param name name of file
    * @exception IOException if the file could not be deleted
    */
    public void delete (String name) throws IOException {
        File file = getFile (name);
        if (file.exists()) {
            if (!deleteFile (file)) {
                FSException.io ("EXC_CannotDelete", name, getDisplayName (), file.toString ()); // NOI18N
            }
        }
    }

    /** Method that recursivelly deletes all files in a folder.
    * @return true if successful
    */
    private static boolean deleteFile (File file) {
        if (file.isDirectory()) {
            // first of all delete whole content
            File[] arr = file.listFiles();
            for (int i = 0; i < arr.length; i++) {
                if (!deleteFile (arr[i])) {
                    return false;
                }
            }
        }
        // delete the file itself
        return file.delete();
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
        return new java.util.Date (getFile (name).lastModified ());
    }

    /* Test if the file is folder or contains data.
    * @param name name of the file
    * @return true if the file is folder, false otherwise
    */
    public boolean folder (String name) {
        return getFile (name).isDirectory ();
    }

    /* Test whether this file can be written to or not.
    * @param name the file to test
    * @return <CODE>true</CODE> if file is read-only
    */
    public boolean readOnly (String name) {
        File f = getFile (name);
        return !f.canWrite () && f.exists ();
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

    /** Get the size of the file.
    *
    * @param name the file to test
    * @return the size of the file in bytes or zero if the file does not contain data (does not
    *  exist or is a folder).
    */
    public long size (String name) {
        return getFile (name).length ();
    }

    // ===============================================================================
    //  This part of code could be used for monitoring of closing file streams.
    /*  public static java.util.HashMap openedIS = new java.util.HashMap();
      public static java.util.HashMap openedOS = new java.util.HashMap();
      
      static class DebugIS extends FileInputStream {
        public DebugIS(File f) throws java.io.FileNotFoundException { super(f); }
        public void close() throws IOException { openedIS.remove(this); super.close(); }
      };
      
      static class DebugOS extends FileOutputStream {
        public DebugOS(File f) throws java.io.IOException { super(f); }
        public void close() throws IOException { openedOS.remove(this); super.close(); }
      };

      public InputStream inputStream (String name) throws java.io.FileNotFoundException {
        DebugIS is = new DebugIS(getFile(name));
        openedIS.put(is, new Exception());
        return is;
      }

      public OutputStream outputStream (String name) throws java.io.IOException {
        DebugOS os = new DebugOS(getFile(name));
        openedOS.put(os, new Exception());
        return os;
      }*/
    //  End of the debug part
    // ============================================================================
    //  Begin of the original part

    /** Get input stream.
    *
    * @param name the file to test
    * @return an input stream to read the contents of this file
    * @exception FileNotFoundException if the file does not exists or is invalid
    */
    public InputStream inputStream (String name) throws java.io.FileNotFoundException {
        return new FileInputStream(getFile(name));
    }

    /** Get output stream.
    *
    * @param name the file to test
    * @return output stream to overwrite the contents of this file
    * @exception IOException if an error occures (the file is invalid, etc.)
    */
    public OutputStream outputStream (String name) throws java.io.IOException {
        return new FileOutputStream (getFile (name));
    }

    //  End of the original part
    // ============================================================================

    /** Does nothing to lock the file.
    *
    * @param name name of the file
    */
    public void lock (String name) throws IOException {
        File file = getFile (name);
        if (file.exists () && !file.canWrite ()) {
            FSException.io ("EXC_CannotLock", name, getDisplayName (), file.toString ()); // NOI18N
        }
    }

    /** Does nothing to unlock the file.
    *
    * @param name name of the file
    */
    public void unlock (String name) {
    }

    /** Does nothing to mark the file as unimportant.
    *
    * @param name the file to mark
    */
    public void markUnimportant (String name) {
    }

    /* JST: Commented out, only testing examples for Q&A
      //
      // Testing routines
      //
      private static void out (FileObject fo, boolean ref, boolean rec) {
        if (ref) fo.refresh ();
        
        Enumeration en = fo.getChildren (rec);

        while (en.hasMoreElements ()) {
          FileObject f = (FileObject)en.nextElement ();
          if (ref) f.refresh ();
          System.out.println (f + " size: " + f.getSize ());
        }
      }

      // Cyclic test for external modifications
      private static void cycle (FileObject fo) throws Exception {
        int x = '\n';
        boolean ref = false;
        for (;;) {
          if (x == '\n') out (fo, ref, true);
          x = System.in.read ();
          ref = true;
        }
      }

      // Create new file test
      private static void createData (FileObject fo, String name, String ext) throws Exception {
        out (fo, false, false);
        System.out.println ("----------");

        FileObject nf = fo.createData (name, ext);

        out (fo, false, false);

        System.out.println ("---------- the object: " + nf);

        fo.refresh ();

        out (fo, false, false);
        System.out.println ("---------- end");
      }
      
      // Create new folder test
      private static void createFolder (FileObject fo, String name) throws Exception {
        out (fo, false, false);
        System.out.println ("----------");

        FileObject nf = fo.createFolder (name);

        out (fo, false, false);

        System.out.println ("---------- the object: " + nf);

        fo.refresh ();

        out (fo, false, false);
        System.out.println ("---------- end");
      }
      
      // Delete
      private static void delete (FileObject fo) throws Exception {
        out (fo.getParent (), false, false);
        System.out.println ("----------");

        FileLock l = fo.lock ();
        fo.delete (l);
        l.releaseLock ();

        out (fo.getParent (), false, false);

        System.out.println ("---------- the object: " + fo + " is valid: " + fo.isValid ());

        fo.getParent ().refresh ();

        out (fo.getParent (), false, false);
        System.out.println ("---------- end");
      }

      public static void main (String[] args) throws Exception {
        LocalFileSystem fs = new LocalFileSystem ();
        fs.setRootDirectory (new File (args[0]));
        FileObject fo = fs.getRoot ();

        //    cycle (fo);
        //    createData (fo, args[1], args[2]);
        //   createFolder (fo, args[1]);
        delete (fs.findResource (args[1]));
      }
    */

}

/*
 * Log
 *  18   Gandalf-post-FCS1.16.1.0    3/24/00  Svatopluk Dedic Workaround for bug in 
 *       JDK's File.renameTo; existing destination was overwritten on UNIXes, 
 *       preserved on Windows.
 *  17   src-jtulach1.16        1/12/00  Ian Formanek    NOI18N
 *  16   src-jtulach1.15        1/4/00   Petr Jiricka    Fix for a subtle bug: 
 *       when the root directory of the filesystem does not exist, 
 *       createFolder() did not work.
 *  15   src-jtulach1.14        12/30/99 Jaroslav Tulach New dialog for 
 *       notification of exceptions.
 *  14   src-jtulach1.13        12/6/99  Jaroslav Tulach #2313
 *  13   src-jtulach1.12        10/29/99 Jaroslav Tulach MultiFileSystem + 
 *       FileStatusEvent
 *  12   src-jtulach1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   src-jtulach1.10        10/8/99  Petr Hamernik   debug methods for 
 *       closing streams was added and commented (could be useful in the future)
 *  10   src-jtulach1.9         10/6/99  Jaroslav Tulach Cannot lock readonly 
 *       files.
 *  9    src-jtulach1.8         10/6/99  Ales Novak      bugfix #4076 and #3617
 *  8    src-jtulach1.7         6/10/99  Jaroslav Tulach Capabilities can be 
 *       passed to constructor.
 *  7    src-jtulach1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    src-jtulach1.5         4/6/99   Ian Formanek    Imporved error messages
 *  5    src-jtulach1.4         3/31/99  Ian Formanek    Fixed order of 
 *       parameters for error message DataAlreadyExists
 *  4    src-jtulach1.3         3/26/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/26/99  Jaroslav Tulach Refresh & Bundles
 *  2    src-jtulach1.1         3/26/99  Jaroslav Tulach 
 *  1    src-jtulach1.0         3/24/99  Jaroslav Tulach 
 * $
 */
