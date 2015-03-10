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
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import org.openide.util.actions.SystemAction;
import org.openide.util.Queue;
import org.openide.util.enum.SequenceEnumeration;
import org.openide.util.enum.SingletonEnumeration;

/** Implementation of <code>FileSystem</code> that simplifies the most
* common tasks. Caches information about the filesystem in
* memory and periodically refreshes its content.
* Many other operations are performed in a safer manner so as to reuse
* known experience; should be substantially simpler to subclass.
*
* @author Jaroslav Tulach
*/
public abstract class AbstractFileSystem extends FileSystem {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -3345098214331282438L;

    /** system actions for this FS if it has refreshTime != 0 */
    private static final SystemAction[] SYSTEM_ACTIONS = new SystemAction[] {
                new RefreshAction ()
            };

    /** system actions for this FS */
    private static final SystemAction[] NO_SYSTEM_ACTIONS = new SystemAction[] {
            };

    /** root object for the file system */
    private transient AbstractFileObject root;

    /** refresher */
    private transient RefreshRequest refresher;

    /** Provider of hierarchy of files. */
    protected List list;

    /** Methods for modification of files. */
    protected Change change;

    /** Methods for moving of files. This field can be left null if the filesystem
    * does not require special handling handling of FileObject.move and is satified
    * with the default implementation.
    */
    protected Transfer transfer;

    /** Methods for obtaining information about files. */
    protected Info info;

    /** Handling of attributes for files. */
    protected Attr attr;

    /* Provides a name for the system that can be presented to the user.
    * @return user presentable name of the file system
    */
    public abstract String getDisplayName ();

    /* Getter for root folder in the filesystem.
    *
    * @return root folder of whole filesystem
    */
    public FileObject getRoot () {
        return getAbstractRoot ();
    }

    /* Finds file when its name is provided.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one want to obtain name of package and not file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    package and not file name
    *
    * @warning when one of name or ext is <CODE>null</CODE> then name and
    *    ext should be ignored and scan should look only for a package
    *
    * @return FileObject that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    public FileObject find (String aPackage, String name, String ext) {
        // create enumeration of name to look for
        StringTokenizer st = new StringTokenizer (aPackage, "."); // NOI18N
        Enumeration en;
        if (name == null || ext == null) {
            en = st;
        } else {
            en = new SequenceEnumeration (
                     st,
                     new SingletonEnumeration (name + '.' + ext)
                 );
        }
        // tries to find it (can return null)
        return getAbstractRoot ().find (en);
    }

    /* Finds file when its resource name is given.
    * The name has the usual format for the {@link ClassLoader#getResource(String)}
    * method. So it may consist of "package1/package2/filename.ext".
    * If there is no package, it may consist only of "filename.ext".
    *
    * @param name resource name
    *
    * @return FileObject that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    public FileObject findResource (String name) {
        if (name.length () == 0) {
            return getAbstractRoot ();
        } else {
            StringTokenizer tok = new StringTokenizer (name, "/"); // NOI18N
            return getAbstractRoot ().find (tok);
        }
    }

    /* Action for this filesystem.
    *
    * @return refresh action
    */
    public SystemAction[] getActions () {
        return refresher == null ? NO_SYSTEM_ACTIONS : SYSTEM_ACTIONS;
    }

    /** Set the number of milliseconds between automatic
    * refreshes of the directory structure.
    *
    * @param ms number of milliseconds between two refreshes; if <code><= 0</code> then refreshing is disabled
    */
    protected synchronized final void setRefreshTime (int ms) {
        if (refresher != null) {
            refresher.stop ();
        }


        if (ms <= 0 || System.getProperty ("netbeans.debug.heap") != null) {
            refresher = null;
        } else {
            refresher = new RefreshRequest (this, ms);
        }
    }

    /** Get the number of milliseconds between automatic
    * refreshes of the directory structure.
    * By default, automatic refreshing is disabled.
    * @return the number of milliseconds, or <code>0</code> if refreshing is disabled
    */
    protected final int getRefreshTime () {
        RefreshRequest r = refresher;
        return r == null ? 0 : r.getRefreshTime ();
    }

    /** Instruct the filesystem
    * that the root should change.
    * A fresh root is created. Subclasses that support root changes should use this.
    *
    * @return the new root
    */
    protected final synchronized AbstractFileObject refreshRoot () {
        root = createFileObject (null, ""); // NOI18N
        return root;
    }

    /** Allows subclasses to fire that a change occured in a
    * file or folder. The change can be "expected" when it is 
    * a result of an user action and the user knows that such
    * change should occur. 
    *
    * @param name resource name of the file where the change occured
    * @param expected true if the user initiated change and expects it
    */
    protected final void refreshResource (String name, boolean expected) {
        AbstractFileObject fo = (AbstractFileObject)findResourceIfExists (name);
        if (fo != null) {
            // refresh and behave like the changes is expected
            fo.refresh (null, null, true, true);
        }
    }

    /* Finds file when its resource name is given.
    * The name has the usual format for the {@link ClassLoader#getResource(String)}
    * method. So it may consist of "package1/package2/filename.ext".
    * If there is no package, it may consist only of "filename.ext".
    *
    * @param name resource name
    *
    * @return FileObject that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    private FileObject findResourceIfExists (String name) {
        if (name.length () == 0) {
            return getAbstractRoot ();
        } else {
            StringTokenizer tok = new StringTokenizer (name, "/"); // NOI18N
            return getAbstractRoot ().findIfExists (tok);
        }
    }

    /** Hooking method to allow MultiFileSystem to be informed when a new
    * file object is created. This is the only method that creates AbstractFileObjects.
    * 
    * @param parent parent object
    * @param name of the object
    */
    AbstractFileObject createFileObject (AbstractFileObject parent, String name) {
        return new AbstractFileObject (this, parent, name);
    }

    /** Creates root object for the fs.
    */
    final AbstractFileObject getAbstractRoot () {
        if (root == null) {
            synchronized (this) {
                if (root == null) {
                    return refreshRoot ();
                }
            }
        }
        return root;
    }

    /** Writes the common fields and the state of refresher.
    */
    private void writeObject (ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject ();
        oos.writeInt (getRefreshTime ());
    }

    /** Reads common fields and state of refresher.
    */
    private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        setRefreshTime (ois.readInt ());
    }


    /** Provides access to the hierarchy of resources.
    */
    public interface List extends java.io.Serializable {
        static final long serialVersionUID =-6242105832891012528L;

        /** Get a list of children files for a given folder.
        *
        * @param f the folder, by name; e.g. <code>top/next/afterthat</code>
        * @return a list of children of the folder, as <code>file.ext</code> (no path)
        *   the array can contain <code>null</code> values that will be ignored
        */
        public String[] children (String f);
    }

    /** Controls modification of files.
    */
    public interface Change extends java.io.Serializable {
        static final long serialVersionUID =-5841597109944924596L;

        /** Create new folder.
        * @param name full name of new folder, e.g. <code>topfolder/newfolder</code>
        * @throws IOException if the operation fails
        */
        public void createFolder (String name) throws java.io.IOException;

        /** Create new data file.
        *
        * @param name full name of the file, e.g. <code>path/from/root/filename.ext</code>
        *
        * @exception IOException if the file cannot be created (e.g. already exists)
        */
        public void createData (String name) throws IOException;

        /** Rename a file.
        *
        * @param oldName old name of the file; fully qualified
        * @param newName new name of the file; fully qualified
        * @throws IOException if it could not be renamed
        */
        public void rename(String oldName, String newName) throws IOException;

        /** Delete a file.
        *
        * @param name name of file; fully qualified
        * @exception IOException if the file could not be deleted
        */
        public void delete (String name) throws IOException;
    }

    /** Controls on moving of files. This is additional interface to
    * allow file system that require special handling of move to implement 
    * it in different way then is the default one.
    */
    public interface Transfer extends java.io.Serializable {
        static final long serialVersionUID =-8945397853892302838L;

        /** Move a file.
        *
        * @param name of the file on current file system
        * @param target move implementation
        * @param targetName of target file
        * @exception IOException if the move fails
        * @return false if the method is not able to handle the request and
        *    default implementation should be used instead
        */
        public boolean move (String name, Transfer target, String targetName) throws IOException;

        /** Copy a file.
        *
        * @param name of the file on current file system
        * @param target target transfer implementation
        * @param targetName name of target file
        * @exception IOException if the copy fails
        * @return false if the method is not able to handle the request and
        *    default implementation should be used instead
        */
        public boolean copy (String name, Transfer target, String targetName) throws IOException;
    }


    /** Information about files.
    */
    public interface Info extends java.io.Serializable {
        static final long serialVersionUID =-2438286177948307985L;

        /**
        * Get last modification time.
        * @param name the file to test
        * @return the date of last modification
        */
        public java.util.Date lastModified(String name);

        /** Test if the file is a folder or contains data.
        * @param name name of the file
        * @return <code>true</code> if the file is folder, <code>false</code> if it is data
        */
        public boolean folder (String name);

        /** Test whether this file can be written to or not.
        * @param name the file to test
        * @return <CODE>true</CODE> if the file is read-only
        */
        public boolean readOnly (String name);

        /** Get the MIME type of the file.
        *
        * @param name the file to test
        * @return the MIME type textual representation, e.g. <code>"text/plain"</code>
        */
        public String mimeType (String name);

        /** Get the size of the file.
        *
        * @param name the file to test
        * @return the size of the file in bytes, or zero if the file does not contain data (does not
        *  exist or is a folder).
        */
        public long size (String name);

        /** Get input stream.
        *
        * @param name the file to test
        * @return an input stream to read the contents of this file
        * @exception FileNotFoundException if the file does not exist or is invalid
        */
        public InputStream inputStream (String name) throws java.io.FileNotFoundException;

        /** Get output stream.
        *
        * @param name the file to test
        * @return output stream to overwrite the contents of this file
        * @exception IOException if an error occurs (the file is invalid, etc.)
        */
        public OutputStream outputStream (String name) throws java.io.IOException;

        /** Lock the file.
        * May do nothing if the underlying storage does not support locking.
        * This does not affect locking using {@link FileLock} within the IDE, however.
        * @param name name of the file
        * @throws FileAlreadyLockedException if the file is already locked
        */
        public void lock (String name) throws IOException;

        /** Unlock the file.
        * @param name name of the file
        */
        public void unlock (String name);

        /** Mark the file as being unimportant.
        * If not called, the file is assumed to be important.
        *
        * @param name the file to mark
        */
        public void markUnimportant (String name);
    }

    /** Handle attributes of files.
    */
    public interface Attr extends java.io.Serializable {
        static final long serialVersionUID =5978845941846736946L;
        /** Get the file attribute with the specified name.
        * @param name the file
        * @param attrName name of the attribute
        * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
        */
        public Object readAttribute(String name, String attrName);

        /** Set the file attribute with the specified name.
        * @param name the file
        * @param attrName name of the attribute
        * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular file systems may or may not use serialization to store attribute values.
        * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
        */
        public void writeAttribute(String name, String attrName, Object value) throws IOException;

        /** Get all file attribute names for the file.
        * @param name the file
        * @return enumeration of keys (as strings)
        */
        public Enumeration attributes(String name);

        /** Called when a file is renamed, to appropriately update its attributes.
        * @param oldName old name of the file
        * @param newName new name of the file
        */
        public void renameAttributes (String oldName, String newName);

        /** Called when a file is deleted, to also delete its attributes.
        *
        * @param name name of the file
        */
        public void deleteAttributes (String name);
    }

}

/*
* Log
*  16   src-jtulach1.15        1/12/00  Ian Formanek    NOI18N
*  15   src-jtulach1.14        1/5/00   Jaroslav Tulach AbstractFileSystem.refreshResource
*        modifies lastModified time
*  14   src-jtulach1.13        11/25/99 Jaroslav Tulach List.children () can 
*       return array that contains nulls
*  13   src-jtulach1.12        11/24/99 Jaroslav Tulach FileEvent can be expected
*       + fired by AbstractFileSystem
*  12   src-jtulach1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  11   src-jtulach1.10        10/1/99  Jaroslav Tulach FileObject.move & 
*       FileObject.copy
*  10   src-jtulach1.9         9/16/99  Petr Hrebejk    -D netbeans.debug.heap 
*       for no filesystem refresh added
*  9    src-jtulach1.8         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  8    src-jtulach1.7         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  7    src-jtulach1.6         6/3/99   Jaroslav Tulach Refresh of only opened 
*       files.
*  6    src-jtulach1.5         5/24/99  Jaroslav Tulach 
*  5    src-jtulach1.4         3/26/99  Jesse Glick     [JavaDoc]
*  4    src-jtulach1.3         3/26/99  Jaroslav Tulach Refresh & Bundles
*  3    src-jtulach1.2         3/26/99  Jaroslav Tulach 
*  2    src-jtulach1.1         3/24/99  Jesse Glick     [JavaDoc] partial.
*  1    src-jtulach1.0         3/24/99  Jaroslav Tulach 
* $
*/
