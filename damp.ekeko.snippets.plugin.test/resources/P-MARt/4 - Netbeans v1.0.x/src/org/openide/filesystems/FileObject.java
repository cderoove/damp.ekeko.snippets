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
import java.util.Enumeration;
import java.util.Hashtable;

import org.openide.execution.NbfsURLConnection;
import org.openide.util.enum.FilterEnumeration;
import org.openide.util.enum.QueueEnumeration;

/** This is the base for all implementations of file objects on a file system.
* Provides basic information about the object (its name, parent,
* whether it exists, etc.) and operations on it (move, delete, etc.).
*
* @author Jaroslav Tulach, Petr Hamernik, Ian Formanek
*/
public abstract class FileObject extends Object implements java.io.Serializable  {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 85305031923497718L;

    /** Get the name without extension of this file.
    *
    * @return name of the file (in its enclosing folder)
    */
    public abstract String getName ();

    /** Get the extension of this file.
    * This is the string after the last dot of the full name, if any.
    *
    * @return extension of the file (if any) or empty string if there is none
    */
    public abstract String getExt ();

    /** Renames this file (or folder).
    * Both the new basename and new extension should be specified.
    * <p>
    * Note that using this call, it is currently only possible to rename <em>within</em>
    * a parent folder, and not to do moves <em>across</em> folders.
    * Conversely, implementing file systems need only implement "simple" renames.
    * If you wish to move a file across folders, you should call {@link FileUtil#moveFile}.
    * @param lock File must be locked before renaming.
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    */
    public abstract void rename(FileLock lock, String name, String ext) throws IOException;

    /** Copies this file. This allows the filesystem to perform any additional
    * operation associated with the copy. But the default implementation is simple
    * copy of the file and its attributes
    * 
    * @param target target folder to move this file to
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    * @return the newly created file object representing the moved file
    */
    public FileObject copy (FileObject target, String name, String ext)
    throws IOException {
        FileObject dest = FileUtil.copyFileImpl (this, target, name, ext);
        return dest;
    }


    /** Moves this file. This allows the filesystem to perform any additional
    * operation associated with the move. But the default implementation is encapsulated
    * as copy and delete.
    * 
    * @param lock File must be locked before renaming.
    * @param target target folder to move this file to
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    * @return the newly created file object representing the moved file
    */
    public FileObject move (FileLock lock, FileObject target, String name, String ext)
    throws IOException {
        if (getParent ().equals (target)) {
            // it is possible to do only rename
            rename (lock, name, ext);
            return this;
        } else {
            // have to do copy
            FileObject dest = copy (target, name, ext);
            delete(lock);
            return dest;
        }
    }

    /** Get fully-qualified filename. Does so by walking through all folders
    * to the root of the file system. Separates files with provided <code>separatorChar</code>.
    * The extension, if present, is separated from the basename with <code>extSepChar</code>.
    *
    * @param separatorChar char to separate folders and files
    * @param extSepChar char to separate extension
    * @return the fully-qualified filename
    */
    public String getPackageNameExt (char separatorChar, char extSepChar) {
        String pn = getPackageName (separatorChar);
        String ext = getExt ();
        if (!ext.equals ("")) { // NOI18N
            // add extension
            return pn + extSepChar + ext;
        } else {
            // without extension
            return pn;
        }
    }

    /** Get fully-qualified filename, but without extension.
    * Like {@link #getPackageNameExt} but omits the extension.
    * @param separatorChar char to separate folders and files
    * @return the fully-qualified filename
    */
    public String getPackageName (char separatorChar) {
        StringBuffer sb = new StringBuffer ();
        constructName (sb, separatorChar);
        return sb.toString ();
    }

    /** Constructs path of file.
    * @param sb string buffer
    * @param sepChar separator character
    */
    private void constructName (StringBuffer sb, char sepChar) {
        FileObject parent = getParent ();
        if ((parent != null) && !parent.isRoot ()) {
            parent.constructName (sb, sepChar);
            sb.append (sepChar);
        }
        sb.append (getName ());
    }

    /** Get the file system containing this file.
    * <p>
    * Note that it may be possible for a stale file object to exist which refers to a now-defunct file system.
    * If this is the case, this method will throw an exception.
    * @return the file system
    * @exception FileStateInvalidException if the reference to the file
    *   system has been lost (e.g., if the file system was deleted)
    */
    public abstract FileSystem getFileSystem () throws FileStateInvalidException;

    /** Get parent folder.
    * The returned object will satisfy {@link #isFolder}.
    *
    * @return the parent folder or <code>null</code> if this object {@link #isRoot}.
    */
    public abstract FileObject getParent ();

    /** Test whether this object is a folder.
    * @return true if the file object is a folder (i.e., can have children)
    */
    public abstract boolean isFolder ();

    /**
    * Get last modification time.
    * @return the date
    */
    public abstract java.util.Date lastModified();

    /** Test whether this object is the root folder.
    * The root should always be a folder.
    * @return true if the object is the root of a file system
    */
    public abstract boolean isRoot ();

    /** Test whether this object is a data object.
    * This is exclusive with {@link #isFolder}.
    * @return true if the file object represents data (i.e., can be read and written)
    */
    public abstract boolean isData ();

    /** Test whether the file is valid.
    *
    * @see FileSystem#isValid
    *
    * @return true if the file object is valid
    */
    public abstract boolean isValid ();

    /** Test whether there is a file with the same basename and only a changed extension in the same folder.
    * The default implementation asks this file's parent using {@link #getFileObject(String name, String ext)}.
    *
    * @param ext the alternate extension
    * @return true if there is such a file
    */
    public boolean existsExt (String ext) {
        FileObject parent = getParent ();
        return parent != null && parent.getFileObject (getName (), ext) != null;
    }

    /** Delete this file. If the file is a folder and it is not empty then
    * all of its contents are also recursively deleted.
    *
    * @param lock the lock obtained by a call to {@link #lock}
    * @exception IOException if the file could not be deleted
    */
    public abstract void delete (FileLock lock) throws IOException;

    /** Get the file attribute with the specified name.
    * @param attrName name of the attribute
    * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
    */
    abstract public Object getAttribute(String attrName);

    /** Set the file attribute with the specified name.
    * @param attrName name of the attribute
    * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular file systems may or may not use serialization to store attribute values.
    * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
    */
    abstract public void setAttribute(String attrName, Object value) throws IOException;

    /** Get all file attribute names for this file.
    * @return enumeration of keys (as strings)
    */
    abstract public Enumeration getAttributes();

    /** Test whether this file has the specified extension.
    * @param ext the extension the file should have
    * @return true if the text after the last period (<code>.</code>) is equal to the given extension
    */
    public final boolean hasExt (String ext) {
        return getExt ().equals (ext);
    }

    /** Add new listener to this object.
    * @param l the listener
    */
    public abstract void addFileChangeListener (FileChangeListener fcl);


    /** Remove listener from this object.
    * @param l the listener
    */
    public abstract void removeFileChangeListener (FileChangeListener fcl);

    /** Fire data creation event.
    * @param en enumeration of {@link FileChangeListener}s that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileDataCreatedEvent (
        final Enumeration en, final FileEvent fe
    ) {
        putEventDispatcher (new FileSystem.EventDispatcher () {
                                public void dispatch () {
                                    while (en.hasMoreElements ()) {
                                        FileChangeListener fcl = (FileChangeListener)en.nextElement ();
                                        fcl.fileDataCreated (fe);
                                    }
                                }
                            });
    }

    /** Fire folder creation event.
    * @param en enumeration of {@link FileChangeListener}s that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileFolderCreatedEvent (
        final Enumeration en, final FileEvent fe
    ) {
        putEventDispatcher (new FileSystem.EventDispatcher () {
                                public void dispatch () {
                                    while (en.hasMoreElements ()) {
                                        FileChangeListener fcl = (FileChangeListener)en.nextElement ();
                                        fcl.fileFolderCreated (fe);
                                    }
                                }
                            });
    }

    /** Fire file change event.
    * @param en enumeration of {@link FileChangeListener}s that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileChangedEvent (
        final Enumeration en, final FileEvent fe
    ) {
        putEventDispatcher (new FileSystem.EventDispatcher () {
                                public void dispatch () {
                                    while (en.hasMoreElements ()) {
                                        FileChangeListener fcl = (FileChangeListener)en.nextElement ();
                                        fcl.fileChanged (fe);
                                    }
                                }
                            });
    }

    /** Fire file deletion event.
    * @param en enumeration of {@link FileChangeListener}s that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileDeletedEvent (
        final Enumeration en, final FileEvent fe
    ) {
        putEventDispatcher (new FileSystem.EventDispatcher () {
                                public void dispatch () {
                                    while (en.hasMoreElements ()) {
                                        FileChangeListener fcl = (FileChangeListener)en.nextElement ();
                                        fcl.fileDeleted (fe);
                                    }
                                }
                            });
    }

    /** Fire file attribute change event.
    * @param en enumeration of {@link FileChangeListener}s that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileAttributeChangedEvent (
        final Enumeration en, final FileAttributeEvent fe
    ) {
        putEventDispatcher (new FileSystem.EventDispatcher () {
                                public void dispatch () {
                                    while (en.hasMoreElements ()) {
                                        FileChangeListener fcl = (FileChangeListener)en.nextElement ();
                                        fcl.fileAttributeChanged (fe);
                                    }
                                }
                            });
    }

    /** Fire file rename event.
    * @param en enumeration of {@link FileChangeListener}s that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileRenamedEvent (
        final Enumeration en, final FileRenameEvent fe
    ) {
        putEventDispatcher (new FileSystem.EventDispatcher () {
                                public void dispatch () {
                                    while (en.hasMoreElements ()) {
                                        FileChangeListener fcl = (FileChangeListener)en.nextElement ();
                                        fcl.fileRenamed (fe);
                                    }
                                }
                            });
    }

    /** Puts the dispatch event into the file system.
    */
    private final void putEventDispatcher (FileSystem.EventDispatcher d) {
        try {
            FileSystem fs = getFileSystem ();
            fs.putEventDispatcher (d);
        } catch (FileStateInvalidException ex) {
            // no file system, no notification
        }
    }

    /** Get the MIME type of this file.
    * The MIME type identifies the type of the file's contents and should be used in the same way as in the <B>Java
    * Activation Framework</B> or in the {@link java.awt.datatransfer} package.
    * <P>
    * The default implementation calls {@link FileUtil#getMIMEType}.
    * (As a fallback return value, <code>content/unknown</code> is used.)
    * @return the MIME type textual representation, e.g. <code>"text/plain"</code>; never <code>null</code>
    */
    public String getMIMEType () {
        String type = FileUtil.getMIMEType (getExt ());
        return type == null ? "content/unknown" : type; // NOI18N
    }

    /** Get the size of the file.
    * @return the size of the file in bytes or zero if the file does not contain data (does not
    *  exist or is a folder).
    */
    public abstract long getSize ();

    /** Get input stream.
    * @return an input stream to read the contents of this file
    * @exception FileNotFoundException if the file does not exists or is invalid
    */
    public abstract InputStream getInputStream () throws java.io.FileNotFoundException;

    /** Get output stream.
    * @param lock the lock that belongs to this file (obtained by a call to
    *   {@link #lock})
    * @return output stream to overwrite the contents of this file
    * @exception IOException if an error occures (the file is invalid, etc.)
    */
    public abstract OutputStream getOutputStream (FileLock lock)
    throws java.io.IOException;

    /** Lock this file.
    * @return lock that can be used to perform various modifications on the file
    * @throws FileAlreadyLockedException if the file is already locked
    */
    public abstract FileLock lock () throws IOException;

    // [???] Implicit file state is important.
    /** Indicate whether this file is important from a user perspective.
    * This method allows a file system to distingush between important and
    * unimportant files when this distinction is possible.
    * <P>
    * <em>For example:</em> Java sources have important <code>.java</code> files and
    * unimportant <code>.class</code> files. If the file system provides
    * an "archive" feature it should archive only <code>.java</code> files.
    * @param b true if the file should be considered important
    */
    public abstract void setImportant (boolean b);

    /** Get all children of this folder (files and subfolders). If the file does not have children
    * (does not exist or is not a folder) then an empty array should be returned. No particular order is assumed.
    *
    * @return array of direct children
    * @see #getChildren(boolean)
    * @see #getFolders
    * @see #getData
    */
    public abstract FileObject[] getChildren ();

    /** Enumerate all children of this folder. If the children should be enumerated
    * recursively, first all direct children are listed; then children of direct subfolders; and so on.
    *
    * @param rec whether to enumerate recursively
    * @return enumeration of type <code>FileObject</code>
    */
    public Enumeration getChildren (final boolean rec) {
        QueueEnumeration en = new QueueEnumeration () {
                                  /** @param o processes object by adding its children to the queue */
                                  public void process (Object o) {
                                      FileObject fo = (FileObject)o;
                                      if (rec && fo.isFolder ()) {
                                          addChildrenToEnum (this, fo.getChildren ());
                                      }
                                  }
                              };
        addChildrenToEnum (en, getChildren ());
        return en;
    }

    /** Puts children into QueueEnumeration.
    * @param en the queue enumeration to add children to
    * @param list array of file objects
    */
    static void addChildrenToEnum (QueueEnumeration en, FileObject[] list) {
        for (int i = 0; i < list.length; i++) {
            en.put (list[i]);
        }
    }

    /** Enumerate the subfolders of this folder.
    * @param rec whether to recursively list subfolders
    * @return enumeration of type <code>FileObject</code> (satisfying {@link #isFolder})
    */
    public Enumeration getFolders (boolean rec) {
        return new org.openide.util.enum.FilterEnumeration (getChildren (rec)) {
                   /** @return true if the object is of type FileFolder */
                   protected boolean accept (Object o) {
                       return ((FileObject)o).isFolder ();
                   }
               };
    }

    /** Enumerate all data files in this folder.
    * @param rec whether to recursively search subfolders
    * @return enumeration of type <code>FileObject</code> (satisfying {@link #isData})
    */
    public Enumeration getData (boolean rec) {
        return new org.openide.util.enum.FilterEnumeration (getChildren (rec)) {
                   /** @return true if the object is of type FileFolder */
                   protected boolean accept (Object o) {
                       return ((FileObject)o).isData ();
                   }
               };
    }

    /** Retrieve file contained in this folder by name.
    * <em>Note</em> that no file is created on disk.
    * @param name basename of the file (in this folder)
    * @param ext extension of the file; <CODE>null</CODE> or <code>""</code>
    *    if the file should have no extension
    * @return the object representing this file or <CODE>null</CODE> if the file
    *   does not exist
    * @exception IllegalArgumentException if <code>this</code> is not a folder
    */
    public abstract FileObject getFileObject (String name, String ext);

    /** Retrieve file contained in this folder by name (no extension).
    * <em>Note</em> that no file is created on disk.
    * @param name basename of the file (in this folder)
    * @return the object representing this file or <CODE>null</CODE> if the file
    *   does not exist
    * @exception IllegalArgumentException if <code>this</code> is not a folder
    */
    public final FileObject getFileObject (String name) {
        return getFileObject (name, null);
    }

    /** Create a new folder below this one with the specified name. Fires
    * <code>fileCreated</code> event.
    *
    * @param name the name of folder to create (without extension)
    * @return the new folder
    * @exception IOException if the folder cannot be created (e.g. already exists)
    */
    public abstract FileObject createFolder (String name) throws IOException;

    /** Create new data file in this folder with the specified name. Fires
    * <code>fileCreated</code> event.
    *
    * @param name the name of data object to create (should not contain a period)
    * @param ext the extension of the file (or <code>null</code> or <code>""</code>)
    *
    * @return the new data file object
    * @exception IOException if the file cannot be created (e.g. already exists)
    */
    public abstract FileObject createData (String name, String ext) throws IOException;

    /** Test whether this file can be written to or not.
    * @return <CODE>true</CODE> if file is read-only
    */
    public abstract boolean isReadOnly ();

    /** Should check for external modifications. For folders it should reread
    * the content of disk, for data file it should check for the last 
    * time the file has been modified.
    * 
    * @param expected should the file events be marked as expected change or not?
    * @see FileEvent#isExpected
    */
    public void refresh (boolean expected) {
    }

    /** Should check for external modifications. For folders it should reread
    * the content of disk, for data file it should check for the last 
    * time the file has been modified.
    * <P>
    * The file events are marked as unexpected.
    */
    public void refresh () {
        refresh (false);
    }

    /** Get URL that can be used to access this file.
    * The URL is only usable within the IDE as it uses a special protocol handler.
    * {@link org.openide.execution.NbClassLoader} must be installed (this is done automatically in the {@link Repository} constructor).
    * @return URL of this file object
    * @exception FileStateInvalidException if the file is not valid
    */
    public final java.net.URL getURL() throws FileStateInvalidException {
        return NbfsURLConnection.encodeFileObject (this);
    }

}

/*
* Log
*  24   Gandalf   1.23        1/14/00  Jaroslav Tulach refresh (expected)
*  23   Gandalf   1.22        1/12/00  Ian Formanek    NOI18N
*  22   Gandalf   1.21        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  21   Gandalf   1.20        10/1/99  Jaroslav Tulach FileObject.move & 
*       FileObject.copy
*  20   Gandalf   1.19        8/30/99  Jesse Glick     [JavaDoc]
*  19   Gandalf   1.18        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  18   Gandalf   1.17        6/1/99   Jaroslav Tulach Synch on atomic actions
*  17   Gandalf   1.16        3/26/99  Jesse Glick     Same javadoc as before, 
*       only without the huge block of ASCII nuls this time, ^%*%$*^&.
*  16   Gandalf   1.15        3/26/99  Jesse Glick     StarTeam bullshit, don't 
*       ask.
*  15   Gandalf   1.14        3/26/99  Jesse Glick     [JavaDoc]
*  14   Gandalf   1.13        3/26/99  Jaroslav Tulach 
*  13   Gandalf   1.12        3/13/99  Jaroslav Tulach FileSystem.Status & 
*       lastModified
*  12   Gandalf   1.11        3/11/99  Jesse Glick     [JavaDoc]
*  11   Gandalf   1.10        3/1/99   Jesse Glick     [JavaDoc]
*  10   Gandalf   1.9         2/11/99  Ian Formanek    Renamed FileSystemPool ->
*       Repository
*  9    Gandalf   1.8         2/4/99   Petr Hamernik   setting of extended file 
*       attributes doesn't require FileLock
*  8    Gandalf   1.7         2/2/99   Jesse Glick     [JavaDoc]
*  7    Gandalf   1.6         2/1/99   Jesse Glick     [JavaDoc]
*  6    Gandalf   1.5         2/1/99   Jesse Glick     [JavaDoc]
*  5    Gandalf   1.4         1/12/99  Jaroslav Tulach 
*  4    Gandalf   1.3         1/11/99  Jaroslav Tulach NbClassLoader extends 
*       URLClassLoader
*  3    Gandalf   1.2         1/6/99   Jaroslav Tulach Change of package of 
*       DataObject
*  2    Gandalf   1.1         1/6/99   Ales Novak      
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
* Beta Change History:
*  0    Tuborg    0.32        --/--/98 Petr Hamernik   isReadOnly, rename methods added
*  0    Tuborg    0.33        --/--/98 Petr Hamernik   getURL added
*  0    Tuborg    0.34        --/--/98 Jaroslav Tulach getURL made final
*  0    Tuborg    0.35        --/--/98 Petr Hamernik   lock throws IOException
*  0    Tuborg    0.36        --/--/98 Jaroslav Tulach comments extended
*  0    Tuborg    0.38        --/--/98 Petr Hamernik   file attributes
*  0    Tuborg    0.39        --/--/98 Petr Hamernik   comments improved
*  0    Tuborg    0.40        --/--/98 Jan Formanek    equals() and hashCode() added
*  0    Tuborg    0.41        --/--/98 Jaroslav Tulach late fireXYZ methods, only adds to the FS fire queue
*  0    Tuborg    0.42        --/--/98 Petr Hamernik   URL protocol
*  0    Tuborg    0.43        --/--/98 Ales Novak      NbfsURLConstants
*/
