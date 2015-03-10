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
import java.lang.ref.*;

import javax.swing.event.EventListenerList;

import org.openide.TopManager;
import org.openide.util.WeakListener;
import org.openide.util.Utilities;
import org.openide.util.enum.*;

/** Implementation of the file object for multi file system.
*
* @author Jaroslav Tulach, 
*/
final class MultiFileObject extends AbstractFolder
    implements FileChangeListener {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2343651324897646809L;

    /** default extension separator */
    private static final char EXT_SEP = '.';

    /** default path separator */
    private static final char PATH_SEP = '/';

    /** list of objects that we delegate to and that already
    * has been created.
    */
    private Set delegates;

    /** current delegate (the first object to delegate to), never null */
    private FileObject leader;

    /** Reference to lock or null */
    private Reference lock;

    /** listener */
    private FileChangeListener weakL;

    /** Constructor. Takes reference to file system this file belongs to.
    *
    * @param fs the file system
    * @param parent the parent object (folder)
    * @param name name of the object (e.g. <code>filename.ext</code>)
    */
    public MultiFileObject(MultiFileSystem fs, MultiFileObject parent, String name) {
        super (fs, parent, name);

        weakL = WeakListener.fileChange (this, null);

        update ();
    }

    /** Constructor for root.
    *
    * @param fs the file system
    */
    public MultiFileObject (MultiFileSystem fs) {
        this (fs, null, ""); // NOI18N
    }

    /** File system.
    */
    public FileSystem getLeaderFileSystem () throws FileStateInvalidException {
        return leader.getFileSystem();
    }

    /** Updates list of all references.
    */
    private void update () {
        FileSystem[] arr = getMultiFileSystem ().systems;

        Set now = delegates == null ? Collections.EMPTY_SET : delegates;
        HashSet del = new HashSet (arr.length * 2);
        FileObject led = null;

        String name = toString ();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                FileObject fo = arr[i].findResource (name);
                if (fo != null) {
                    del.add (fo);

                    if (!now.remove (fo)) {
                        // now there yet
                        fo.addFileChangeListener(weakL);
                    }


                    if (led == null) {
                        led = fo;
                    }
                }
            }
        }

        Iterator it = now.iterator ();
        while (it.hasNext()) {
            FileObject fo = (FileObject)it.next ();
            fo.removeFileChangeListener (weakL);
        }


        if (led != null) {
            // otherwise leave the leader to be last file that represented
            // this one
            if (led != this.leader && this.leader != null) {
                getMultiFileSystem ().notifyMigration (this);
            }
            this.leader = led;
        }
        this.delegates = del;
    }

    /** Getter for the right file system */
    private MultiFileSystem getMultiFileSystem () {
        return (MultiFileSystem)getFileSystem ();
    }

    /** Getter for one of children.
    */
    private MultiFileObject getMultiChild (String name) {
        return (MultiFileObject)getChild (name);
    }

    /** Converts the file to be writable.
    * The file has to be locked!
    * 
    * @return file object (new leader) that is writable
    * @exception IOException if the object cannot be writable
    */
    private FileObject writable () throws IOException {
        MultiFileSystem fs = getMultiFileSystem ();
        FileSystem single = fs.createWritableOn (toString ());

        if (single != leader.getFileSystem()) {
            // if writing to a file that is not on writable fs =>
            // copy it

            if (leader.isFolder()) {
                leader = FileUtil.createFolder (single.getRoot (), toString ());
            } else {
                FileObject folder = FileUtil.createFolder(single.getRoot (), getParent ().toString ());
                leader = leader.copy (folder, leader.getName (), leader.getExt ());
            }

            MfLock l = (MfLock)(lock == null ? null : lock.get ());
            if (l != null) {
                // update the lock
                FileLock prev = l.lock;
                l.lock = leader.lock ();
                if (prev != null) {
                    // can be null if we lock file on readonly system
                    prev.releaseLock ();
                }
            }
        }

        return leader;
    }

    /** All objects that are beyond this one.
    * @return enumeration of FileObject
    */
    private Enumeration delegates () {
        return getMultiFileSystem ().delegates (toString ());
    }

    /** Method that goes upon list of folders and updates its locks. This is used when
    * an object is masked which may lead to creation of folders on a disk.
    *
    * @param fo folder to check
    * @exception IOException if something locks cannot be updated
    */
    private static void updateFoldersLock (FileObject fo) throws IOException {
        while (fo != null) {
            MultiFileObject mfo = (MultiFileObject)fo;

            MfLock l = (MfLock)(mfo.lock == null ? null : mfo.lock.get ());
            if (l != null) {
                // the file has been locked => update the lock
                mfo.writable ();
            }

            fo = fo.getParent ();
        }
    }

    //
    // List
    //

    /** Method that allows subclasses to return its children.
    *
    * @return names (name . ext) of subfiles
    */
    protected final String[] list () {
        LinkedList list = new LinkedList ();
        HashSet mask = new HashSet (37);

        Iterator it = delegates.iterator();

        while (it.hasNext()) {
            FileObject folder = (FileObject)it.next ();
            if (folder == null || !folder.isFolder ()) continue;

            FileObject[] add = folder.getChildren ();

            for (int j = 0; j < add.length; j++) {
                String e = add[j].getExt ();
                String name = add[j].getName ();
                if (e != null && e.length () != 0) {
                    name = name + '.' + e;
                }
                if (name.endsWith (MultiFileSystem.MASK)) {
                    name = name.substring (0, name.length () - MultiFileSystem.MASK.length ());
                    mask.add (name);
                } else {
                    list.add (name);
                }
            }
        }

        if (!mask.isEmpty ()) {
            list.removeAll (mask);
        }

        String[] arr = (String[])list.toArray (new String[list.size()]);
        return arr;
    }

    /** When refreshing, also update the state of delegates.
    */
    protected synchronized void refresh (
        String add, String remove, boolean fire, boolean expected
    ) {
        update ();
        super.refresh (add, remove, fire, expected);
    }

    /** Method to create a file object for given subfile.
    * @param name of the subfile
    * @return the file object
    */
    protected final AbstractFolder createFile (String name) {
        return new MultiFileObject (getMultiFileSystem (), this, name);
    }

    //
    // Info
    //

    /* Test whether this object is a folder.
    * @return true if the file object is a folder (i.e., can have children)
    */
    public boolean isFolder () {
        return leader.isFolder ();
    }

    /*
    * Get last modification time.
    * @return the date
    */
    public java.util.Date lastModified() {
        return leader.lastModified ();
    }

    /* Test whether this object is a data object.
    * This is exclusive with {@link #isFolder}.
    * @return true if the file object represents data (i.e., can be read and written)
    */
    public boolean isData () {
        return leader.isData ();
    }

    /* Test whether this file can be written to or not.
    * @return <CODE>true</CODE> if file is read-only
    */
    public boolean isReadOnly () {
        MultiFileSystem fs = getMultiFileSystem ();

        if (fs.isReadOnly ()) {
            return true;
        }

        if (leader.isReadOnly ()) {
            // if we can make it writable then nothing
            try {
                FileSystem simple = fs.createWritableOn (toString ());
                return simple == leader.getFileSystem ();
            } catch (IOException e) {
                return true;
            }
        }

        return false;
    }

    /* Get the MIME type of this file.
    * The MIME type identifies the type of the file's contents and should be used in the same way as in the <B>Java
    * Activation Framework</B> or in the {@link java.awt.datatransfer} package.
    * <P>
    * The default implementation calls {@link FileUtil#getMIMEType}.
    *
    * @return the MIME type textual representation, e.g. <code>"text/plain"</code>
    */
    public String getMIMEType () {
        return leader.getMIMEType ();
    }

    /* Get the size of the file.
    * @return the size of the file in bytes or zero if the file does not contain data (does not
    *  exist or is a folder).
    */
    public long getSize () {
        return leader.getSize ();
    }

    /* Get input stream.
    * @return an input stream to read the contents of this file
    * @exception FileNotFoundException if the file does not exists or is invalid
    */
    public InputStream getInputStream () throws java.io.FileNotFoundException {
        return leader.getInputStream ();
    }

    /* Get output stream.
    * @param lock the lock that belongs to this file (obtained by a call to
    *   {@link #lock})
    * @return output stream to overwrite the contents of this file
    * @exception IOException if an error occures (the file is invalid, etc.)
    */
    public synchronized OutputStream getOutputStream (FileLock lock)
    throws java.io.IOException {
        testLock (lock);
        MfLock l = (MfLock)lock;

        // this can also change lock in l.lock
        FileObject fo = writable ();

        return fo.getOutputStream (l.lock);
    }

    /* Lock this file.
    * @return lock that can be used to perform various modifications on the file
    * @throws FileAlreadyLockedException if the file is already locked
    */
    public synchronized FileLock lock () throws IOException {
        if (lock != null) {
            FileLock f = (FileLock)lock.get ();
            if (f != null) {
                //        System.out.println ("Already locked: " + this); // NOI18N
                throw new FileAlreadyLockedException();
            }
        }


        FileLock l;

        // create the lock
        FileSystem single = getMultiFileSystem ().createWritableOn (toString ());
        if (single == leader.getFileSystem()) {
            // lock exactly the leader's file
            l = new MfLock (leader.lock ());
        } else {
            // do not lock the leader
            l = new MfLock ();
        }


        lock = new WeakReference (l);
        //    Thread.dumpStack ();
        //    System.out.println ("Locking file: " + this); // NOI18N

        return l;
    }

    /** Tests the lock if it is valid, if not throws exception.
    * @param l lock to test
    * @return the lock to use on leader
    */
    private FileLock testLock (FileLock l) throws java.io.IOException {
        if (lock == null || lock.get () != l) {
            FSException.io ("EXC_InvalidLock", toString (), getMultiFileSystem ().getDisplayName ()); // NOI18N
        }

        return ((MfLock)l).lock;
    }

    // [???] Implicit file state is important.
    /* Indicate whether this file is important from a user perspective.
    * This method allows a file system to distingush between important and
    * unimportant files when this distinction is possible.
    * <P>
    * <em>For example:</em> Java sources have important <code>.java</code> files and
    * unimportant <code>.class</code> files. If the file system provides
    * an "archive" feature it should archive only <code>.java</code> files.
    * @param b true if the file should be considered important
    */
    public void setImportant (boolean b) {
        Enumeration en = delegates ();
        while (en.hasMoreElements ()) {
            FileObject fo = (FileObject)en.nextElement ();
            fo.setImportant (b);
        }
        if (!b) {
            getMultiFileSystem ().markUnimportant (this);
        }
    }



    /* Get the file attribute with the specified name.
    * @param attrName name of the attribute
    * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
    */
    public Object getAttribute(String attrName) {
        Enumeration en = delegates ();
        while (en.hasMoreElements ()) {
            FileObject fo = (FileObject)en.nextElement ();
            Object obj = fo.getAttribute (attrName);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    /* Set the file attribute with the specified name.
    * @param attrName name of the attribute
    * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular file systems may or may not use serialization to store attribute values.
    * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
    */
    public void setAttribute(String attrName, Object value) throws IOException {
        writable ().setAttribute (attrName, value);
    }

    /* Get all file attribute names for this file.
    * @return enumeration of keys (as strings)
    */
    public Enumeration getAttributes() {
        return leader.getAttributes ();
    }


    /* Create a new folder below this one with the specified name. Fires
    * <code>fileCreated</code> event.
    *
    * @param name the name of folder to create (without extension)
    * @return the new folder
    * @exception IOException if the folder cannot be created (e.g. already exists)
    */
    public synchronized FileObject createFolder (String name) throws IOException {
        MultiFileSystem fs = getMultiFileSystem ();
        if (fs.isReadOnly()) {
            FSException.io ("EXC_FSisRO", fs.getDisplayName ()); // NOI18N
        }
        if (isReadOnly()) {
            FSException.io ("EXC_FisRO", name, fs.getDisplayName ()); // NOI18N
        }

        String fullName = toString () + PATH_SEP + name;
        FileSystem simple = fs.createWritableOn (fullName);

        // create
        FileUtil.createFolder (simple.getRoot (), fullName);
        // try to unmask if necessary
        MultiFileSystem.unmaskFile (simple, fullName);

        refresh (name, null);

        MultiFileObject fo = getMultiChild (name);

        if (fo == null) {
            // system error
            throw new FileStateInvalidException (FileSystem.getString ("EXC_ApplicationCreateError", toString (), name));
        }

        if (hasListeners ()) {
            fileCreated0(new FileEvent(fo), false);
        }

        return fo;
    }

    /* Create new data file in this folder with the specified name. Fires
    * <code>fileCreated</code> event.
    *
    * @param name the name of data object to create (should not contain a period)
    * @param ext the extension of the file (or <code>null</code> or <code>""</code>)
    *
    * @return the new data file object
    * @exception IOException if the file cannot be created (e.g. already exists)
    */
    public synchronized FileObject createData (String name, String ext) throws IOException {
        MultiFileSystem fs = getMultiFileSystem ();
        if (fs.isReadOnly()) {
            FSException.io ("EXC_FSisRO", fs.getDisplayName ()); // NOI18N
        }
        if (isReadOnly()) {
            FSException.io ("EXC_FisRO", name, fs.getDisplayName ()); // NOI18N
        }

        String n = "".equals (ext) ? name : name + EXT_SEP + ext; // NOI18N
        String fullName = toString () + PATH_SEP + n;

        FileSystem simple = fs.createWritableOn (fullName);

        // create
        FileUtil.createData (simple.getRoot (), fullName);

        // try to unmask if necessary
        MultiFileSystem.unmaskFile (simple, fullName);

        refresh (n, null);

        MultiFileObject fo = getMultiChild (n);

        if (fo == null) {
            // system error
            throw new FileStateInvalidException (FileSystem.getString ("EXC_ApplicationCreateError", toString (), n));
        }

        if (hasListeners ()) {
            fileCreated0(new FileEvent(fo), true);
        }

        return fo;
    }

    /* Renames this file (or folder).
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
    public void rename(FileLock lock, String name, String ext) throws IOException {
        MultiFileSystem fs = getMultiFileSystem ();

        if (parent == null) {
            FSException.io ("EXC_CannotRenameRoot", fs.getDisplayName ()); // NOI18N
        }

        synchronized (parent) {
            // synchronize on your folder
            testLock (lock);
            MfLock l = (MfLock)lock;


            String newFullName =  parent.toString () + PATH_SEP + name;
            if (isData ()) {
                newFullName += EXT_SEP + ext;
            }
            String oldFullName = toString ();

            if (isReadOnly ()) {
                FSException.io ("EXC_CannotRename", toString (), getMultiFileSystem ().getDisplayName (), newFullName); // NOI18N
            }
            if (getFileSystem ().isReadOnly()) {
                FSException.io ("EXC_FSisRO", getMultiFileSystem ().getDisplayName ()); // NOI18N
            }

            String on = getName ();
            String oe = getExt ();

            //!!!      getMultiFileSystem ().change.rename (oldFullName, newFullName);
            FileSystem single = fs.createWritableOn (newFullName);

            if (single == leader.getFileSystem ()) {
                // delete the file if we can on the selected
                // system
                leader.rename (l.lock, name, ext);
            } else {
                // rename file that is on different file system
                // means to copy it

                if (isData ()) {
                    // data
                    FileObject folder = FileUtil.createFolder(single.getRoot (), getParent ().toString ());
                    leader = leader.copy (folder, name, ext);
                } else {
                    // folder
                    FileObject fo = FileUtil.createFolder (single.getRoot (), newFullName);
                    copyContent (this, fo);

                    leader = fo;
                    update ();
                }

                if (l.lock != null) {
                    l.lock.releaseLock ();
                }
                l.lock = leader.lock ();
            }

            if (getMultiFileSystem ().delegates (oldFullName).hasMoreElements ()) {
                // if there is older version of the file
                // then we have to mask it
                MultiFileSystem.maskFile (single, oldFullName);
                updateFoldersLock (getParent ());
            }

            String oldName = this.name;
            this.name = name;
            // clear cached full name
            this.fullName = null;

            /*
                  System.out.println ("Resulting file is: " + toString ());
                  System.out.println ("Bedw      file is: " + newFullName);
                  System.out.println ("Name: " + name);
                  System.out.println ("Old : " + oldName);
            */

            parent.refresh (name, oldName);

            //!!!      getMultiFileSystem ().attr.renameAttributes (oldFullName, newFullName);

            if (hasAtLeastOneListeners ()) {
                fileRenamed0 (new FileRenameEvent(this, on, oe));
            }
        }
    }

    /* Delete this file. If the file is a folder and it is not empty then
    * all of its contents are also recursively deleted.
    *
    * @param lock the lock obtained by a call to {@link #lock}
    * @exception IOException if the file could not be deleted
    */
    public void delete (FileLock lock) throws IOException {
        if (parent == null) {
            FSException.io (
                "EXC_CannotDeleteRoot", getMultiFileSystem ().getDisplayName () // NOI18N
            );
        }

        MultiFileSystem fs = getMultiFileSystem ();

        synchronized (parent) {
            testLock (lock);
            MfLock l = (MfLock)lock;

            String fullName = toString ();
            //!!!      getMultiFileSystem ().change.delete (fullName);

            FileSystem single = fs.createWritableOn (fullName);

            if (single == leader.getFileSystem ()) {
                // delete the file if we can on the selected
                // system
                leader.delete (l.lock);
            }


            if (getMultiFileSystem ().delegates (fullName).hasMoreElements ()) {
                // if there is older version of the file
                // then we have to mask it
                MultiFileSystem.maskFile (single, fullName);
                updateFoldersLock (getParent ());
            }


            String n = name;
            // if deleted set systemName to null, that indicates that
            // the object is not valid
            systemName = null;

            parent.refresh (null, n);

            //!!!      getMultiFileSystem ().attr.deleteAttributes (fullName);

            if (hasAtLeastOneListeners ()) {
                fileDeleted0 (new FileEvent(this));
            }
        }
    }

    //
    // Transfer
    //

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
        return leader.copy (target, name, ext);
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
        MultiFileSystem fs = getMultiFileSystem ();

        if (parent == null) {
            FSException.io (
                "EXC_CannotDeleteRoot", fs.getDisplayName () // NOI18N
            );
        }

        FileLock l = testLock (lock);

        if (fs.isReadOnly() || l == null) {
            FSException.io ("EXC_FSisRO", fs.getDisplayName ()); // NOI18N
        }

        return leader.move (l, target, name, ext);
    }

    //
    // Listeners
    //

    /** Fired when a new folder is created. This action can only be
     * listened to in folders containing the created folder up to the root of
     * file system.
     *
     * @param fe the event describing context where action has taken place
     */
    public void fileFolderCreated(FileEvent fe) {
        refresh ();
    }

    /** Fired when a new file is created. This action can only be
     * listened in folders containing the created file up to the root of
     * file system.
     *
     * @param fe the event describing context where action has taken place
     */
    public void fileDataCreated(FileEvent fe) {
        refresh ();
    }

    /** Fired when a file is changed.
     * @param fe the event describing context where action has taken place
     */
    public void fileChanged(FileEvent fe) {
        if (fe.getFile() == leader && hasAtLeastOneListeners ()) {
            fileChanged0 (new FileEvent (this));
        }
    }

    /** Fired when a file is deleted.
     * @param fe the event describing context where action has taken place
     */
    public void fileDeleted(FileEvent fe) {
        refresh ();
    }

    /** Fired when a file is renamed.
     * @param fe the event describing context where action has taken place
     *           and the original name and extension.
     */
    public void fileRenamed(FileRenameEvent fe) {
        refresh ();
    }

    /** Fired when a file attribute is changed.
     * @param fe the event describing context where action has taken place,
     *           the name of attribute and the old and new values.
     */
    public void fileAttributeChanged(FileAttributeEvent fe) {
        if (fe.getFile() == leader && hasAtLeastOneListeners ()) {
            fileAttributeChanged0 (new FileAttributeEvent (
                                       this, fe.getName(), fe.getOldValue(), fe.getNewValue()
                                   ));
        }
    }

    /** Copies content of one folder into another.
    * @param source source folder
    * @param target target folder
    * @exception IOException if it fails
    */
    private static void copyContent (FileObject source, FileObject target) throws IOException {
        FileObject[] srcArr = source.getChildren ();

        for (int i = 0; i < srcArr.length; i++) {
            FileObject child = srcArr[i];

            if (child.isData ()) {
                FileUtil.copyFile (child, target, child.getName (), child.getExt ());
            } else {
                FileObject targetChild = target.createFolder (child.getName ());
                copyContent (child, targetChild);
            }
        }
    }


    /** Implementation of lock for abstract files.
    */
    private class MfLock extends FileLock {
        private FileLock lock;

        public MfLock () {
        }

        public MfLock (FileLock l) {
            lock = l;
        }

        public void releaseLock () {
            if (this.isValid()) {
                super.releaseLock();
                if  (lock != null) {
                    lock.releaseLock();
                }
                MultiFileObject.this.lock = null;
            }
        }
    } // MfLock

}

/*
* Log
*  12   Gandalf   1.11        1/20/00  Jaroslav Tulach Menu on multiuser 
*       instalation can be renamed.
*  11   Gandalf   1.10        1/15/00  Jaroslav Tulach rename + delete of 
*       folders and its content works.
*  10   Gandalf   1.9         1/13/00  Ian Formanek    NOI18N
*  9    Gandalf   1.8         1/12/00  Ian Formanek    NOI18N
*  8    Gandalf   1.7         1/5/00   Jaroslav Tulach AbstractFileSystem.refreshResource
*        modifies lastModified time
*  7    Gandalf   1.6         12/30/99 Jaroslav Tulach New dialog for 
*       notification of exceptions.
*  6    Gandalf   1.5         11/17/99 Jaroslav Tulach Works in multi 
*       instalation.
*  5    Gandalf   1.4         11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  4    Gandalf   1.3         11/3/99  Jaroslav Tulach Can create new files over
*       hidden ones.
*  3    Gandalf   1.2         11/1/99  Jaroslav Tulach Folder is made "writable"
*       correctly
*  2    Gandalf   1.1         11/1/99  Jaroslav Tulach Can lock files on 
*       readonly fs too.
*  1    Gandalf   1.0         10/29/99 Jaroslav Tulach 
* $
*/
