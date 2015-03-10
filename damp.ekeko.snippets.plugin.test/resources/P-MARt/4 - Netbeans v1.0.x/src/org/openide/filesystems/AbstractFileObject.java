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
import org.openide.util.Utilities;
import org.openide.util.enum.*;

/** Implementation of the file object for abstract file system.
*
* @author Jaroslav Tulach, 
*/
final class AbstractFileObject extends AbstractFolder {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2343651324897646809L;

    /** default extension separator */
    private static final char EXT_SEP = '.';

    /** default path separator */
    private static final char PATH_SEP = '/';

    /** Reference to lock or null */
    private Reference lock;

    /** cache to remember if this object is folder or not */
    private Boolean folder;

    /** the time of last modification */
    private java.util.Date lastModified;

    /** Constructor. Takes reference to file system this file belongs to.
    *
    * @param fs the file system
    * @param parent the parent object (folder)
    * @param name name of the object (e.g. <code>filename.ext</code>)
    */
    public AbstractFileObject (AbstractFileSystem fs, AbstractFileObject parent, String name) {
        super (fs, parent, name);
    }

    /** Getter for the right file system */
    private AbstractFileSystem getAbstractFileSystem () {
        return (AbstractFileSystem)getFileSystem ();
    }

    /** Getter for one of children.
    */
    private AbstractFileObject getAbstractChild (String name) {
        return (AbstractFileObject)getChild (name);
    }

    //
    // List
    //
    /** Method that allows subclasses to return its children.
    *
    * @return names (name . ext) of subfiles
    */
    protected final String[] list () {
        return getAbstractFileSystem ().list.children (toString ());
    }

    /** Method to create a file object for given subfile.
    * @param name of the subfile
    * @return the file object
    */
    protected final AbstractFolder createFile (String name) {
        return getAbstractFileSystem ().createFileObject (this, name);
    }

    //
    // Info
    //

    /* Test whether this object is a folder.
    * @return true if the file object is a folder (i.e., can have children)
    */
    public boolean isFolder () {
        if (folder == null) {
            if (parent == null || getAbstractFileSystem ().info.folder (toString ())) {
                folder = Boolean.TRUE;
                return true;
            } else {
                folder = Boolean.FALSE;
                return false;
            }
        } else {
            return folder.booleanValue ();
        }
    }

    /* Test whether this object is a data object.
    * This is exclusive with {@link #isFolder}.
    * @return true if the file object represents data (i.e., can be read and written)
    */
    public final boolean isData () {
        return !isFolder ();
    }

    /*
    * Get last modification time.
    * @return the date
    */
    public java.util.Date lastModified() {
        if (lastModified == null) {
            lastModified = getAbstractFileSystem ().info.lastModified (toString ());
        }

        return lastModified;
    }

    /* Test whether this file can be written to or not.
    * @return <CODE>true</CODE> if file is read-only
    */
    public boolean isReadOnly () {
        AbstractFileSystem fs = getAbstractFileSystem ();
        return fs.isReadOnly () || fs.info.readOnly (toString ());
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
        return getAbstractFileSystem ().info.mimeType (toString ());
    }

    /* Get the size of the file.
    * @return the size of the file in bytes or zero if the file does not contain data (does not
    *  exist or is a folder).
    */
    public long getSize () {
        return getAbstractFileSystem ().info.size (toString ());
    }

    /* Get input stream.
    * @return an input stream to read the contents of this file
    * @exception FileNotFoundException if the file does not exists or is invalid
    */
    public InputStream getInputStream () throws java.io.FileNotFoundException {
        return getAbstractFileSystem ().info.inputStream (toString ());
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
        return new NotifyOutputStream (getAbstractFileSystem ().info.outputStream (toString ()));
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

        getAbstractFileSystem ().info.lock (toString ());

        FileLock l = new AfLock ();
        lock = new WeakReference (l);
        //    Thread.dumpStack ();
        //    System.out.println ("Locking file: " + this); // NOI18N

        return l;
    }

    /** Unlocks the file. Notifies the underlaying impl.
    */
    synchronized void unlock () {
        getAbstractFileSystem ().info.unlock (toString ());
        lastModified = getAbstractFileSystem ().info.lastModified (toString ());
        // clear my lock
        lock = null;
    }

    /** Tests the lock if it is valid, if not throws exception.
    * @param l lock to test
    */
    private void testLock (FileLock l) throws java.io.IOException {
        if (lock == null || lock.get () != l) {
            FSException.io ("EXC_InvalidLock", toString (), getAbstractFileSystem ().getDisplayName ()); // NOI18N
        }
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
        if (!b) {
            getAbstractFileSystem ().info.markUnimportant (toString ());
        }
    }



    /* Get the file attribute with the specified name.
    * @param attrName name of the attribute
    * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
    */
    public Object getAttribute(String attrName) {
        return getAbstractFileSystem ().attr.readAttribute (toString (), attrName);
    }

    /* Set the file attribute with the specified name.
    * @param attrName name of the attribute
    * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular file systems may or may not use serialization to store attribute values.
    * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
    */
    public void setAttribute(String attrName, Object value) throws IOException {
        getAbstractFileSystem ().attr.writeAttribute (toString (), attrName, value);
    }

    /* Get all file attribute names for this file.
    * @return enumeration of keys (as strings)
    */
    public Enumeration getAttributes() {
        return getAbstractFileSystem ().attr.attributes (toString ());
    }


    /* Create a new folder below this one with the specified name. Fires
    * <code>fileCreated</code> event.
    *
    * @param name the name of folder to create (without extension)
    * @return the new folder
    * @exception IOException if the folder cannot be created (e.g. already exists)
    */
    public synchronized FileObject createFolder (String name) throws IOException {
        AbstractFileSystem fs = getAbstractFileSystem ();
        if (fs.isReadOnly()) {
            FSException.io("EXC_FSisRO", fs.getDisplayName ()); // NOI18N
        }
        if (isReadOnly()) {
            FSException.io("EXC_FisRO", name, fs.getDisplayName ()); // NOI18N
        }

        getAbstractFileSystem ().change.createFolder (toString () + PATH_SEP + name);

        refresh (name, null);

        AbstractFileObject fo = getAbstractChild (name);

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
        AbstractFileSystem fs = getAbstractFileSystem ();
        if (fs.isReadOnly()) {
            FSException.io("EXC_FSisRO", fs.getDisplayName ()); // NOI18N
        }
        if (isReadOnly()) {
            FSException.io("EXC_FisRO", name, fs.getDisplayName ()); // NOI18N
        }

        String n = "".equals (ext) ? name : name + EXT_SEP + ext; // NOI18N

        getAbstractFileSystem ().change.createData (toString () + PATH_SEP + n);
        refresh (n, null);

        AbstractFileObject fo = getAbstractChild (n);

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
        if (parent == null) {
            FSException.io ("EXC_CannotRenameRoot", getAbstractFileSystem ().getDisplayName ()); // NOI18N
        }

        if (name == null) {
            throw new NullPointerException();
        }

        synchronized (parent) {
            // synchronize on your folder
            testLock (lock);

            if (isData ()) {
                if (ext == null) {
                    throw new NullPointerException();
                }
                name = name + EXT_SEP + ext;
            }
            String newFullName =  parent.toString () + PATH_SEP + name;
            String oldFullName = toString ();

            if (isReadOnly ()) {
                FSException.io ("EXC_CannotRename", toString (), getAbstractFileSystem ().getDisplayName (), newFullName); // NOI18N
            }
            if (getFileSystem ().isReadOnly()) {
                FSException.io ("EXC_FSisRO", getAbstractFileSystem ().getDisplayName ()); // NOI18N
            }

            String on = getName ();
            String oe = getExt ();

            getAbstractFileSystem ().change.rename (oldFullName, newFullName);

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

            getAbstractFileSystem ().attr.renameAttributes (oldFullName, newFullName);

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
            FSException.io ("EXC_CannotDeleteRoot", getAbstractFileSystem ().getDisplayName ()); // NOI18N
        }

        synchronized (parent) {
            testLock (lock);

            String fullName = toString ();
            getAbstractFileSystem ().change.delete (fullName);

            String n = name;
            // if deleted set systemName to null, that indicates that
            // the object is not valid
            systemName = null;

            parent.refresh (null, n);

            getAbstractFileSystem ().attr.deleteAttributes (fullName);

            if (hasAtLeastOneListeners ()) {
                fileDeleted0(new FileEvent(this));
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
        AbstractFileSystem fs = getAbstractFileSystem ();
        AbstractFileSystem.Transfer from = getAbstractFileSystem ().transfer;

        if (from == null || !(target instanceof AbstractFileObject)) {
            return super.copy (target, name, ext);
        }

        AbstractFileObject abstractTarget = (AbstractFileObject)target;
        AbstractFileSystem abstractFS = abstractTarget.getAbstractFileSystem ();
        AbstractFileSystem.Transfer to = abstractFS.transfer;

        if (to != null) synchronized (abstractTarget) {
                // try copying thru the transfer
                if (abstractFS.isReadOnly()) {
                    FSException.io ("EXC_FSisRO", abstractFS.getDisplayName ()); // NOI18N
                }

                if (target.isReadOnly()) {
                    FSException.io ("EXC_FisRO", target.toString (), abstractFS.getDisplayName ()); // NOI18N
                }

                String n = "".equals (ext) ? name : name + EXT_SEP + ext; // NOI18N

                if (from.copy (toString (), to, target.toString () + PATH_SEP + n)) {
                    // the transfer implementation thinks that the copy succeeded
                    abstractTarget.refresh (n, null);

                    AbstractFileObject fo = abstractTarget.getAbstractChild (n);

                    if (fo == null) {
                        // system error
                        throw new FileStateInvalidException (FileSystem.getString ("EXC_ApplicationCreateError", abstractTarget.toString (), n));
                    }

                    if (abstractTarget.hasListeners ()) {
                        abstractTarget.fileCreated0(new FileEvent(fo), true);
                    }

                    return fo;
                }
            }
        return super.copy (target, name, ext);
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
        AbstractFileSystem fs = getAbstractFileSystem ();

        if (parent == null) {
            FSException.io ("EXC_CannotDeleteRoot", fs.getDisplayName ()); // NOI18N
        }

        AbstractFileSystem.Transfer from = getAbstractFileSystem ().transfer;

        if (from == null || !(target instanceof AbstractFileObject)) {
            return super.move (lock, target, name, ext);
        }

        AbstractFileObject abstractTarget = (AbstractFileObject)target;
        AbstractFileSystem abstractFS = abstractTarget.getAbstractFileSystem ();
        AbstractFileSystem.Transfer to = abstractFS.transfer;

        if (to != null) synchronized (parent) {
                testLock (lock);

                if (abstractFS.isReadOnly()) {
                    FSException.io ("EXC_FSisRO", abstractFS.getDisplayName ()); // NOI18N
                }

                if (target.isReadOnly()) {
                    FSException.io ("EXC_FisRO", target.toString (), abstractFS.getDisplayName ()); // NOI18N
                }

                String n = "".equals (ext) ? name : name + EXT_SEP + ext; // NOI18N
                String fullName = toString ();

                if (from.move (fullName, to, target.toString () + PATH_SEP + n)) {
                    // the transfer implementation thinks that the move succeeded
                    String oldN = name;
                    // if deleted set systemName to null, that indicates that
                    // the object is not valid
                    systemName = null;

                    // refresh the parent because this file has been deleted
                    parent.refresh (null, oldN);

                    // deletes all attributes asssociated with the moved file
                    // JST: I am not sure if this is the right behaviour, maybe this
                    //      should be the reposibility of from.move?
                    // fs.attr.deleteAttributes (fullName);

                    // refresh the target so new file appears there
                    abstractTarget.refresh (n, null);

                    AbstractFileObject fo = abstractTarget.getAbstractChild (n);

                    if (fo == null) {
                        // system error
                        throw new FileStateInvalidException (FileSystem.getString ("EXC_ApplicationCreateError", abstractTarget.toString (), n));
                    }

                    if (hasAtLeastOneListeners ()) {
                        fileDeleted0(new FileEvent(this));
                    }

                    if (abstractTarget.hasListeners ()) {
                        abstractTarget.fileCreated0(new FileEvent(fo), true);
                    }

                    return fo;
                }
            }
        return super.move (lock, target, name, ext);
    }

    /** Refresh the content of file. Ignores changes to the files provided,
    * instead returns its file object.
    * @param added do not notify addition of this file
    * @param removed do not notify removing of this file
    * @param fire true if we should fire changes
    */
    protected synchronized void refresh (
        String added, String removed, boolean fire, boolean expected
    ) {
        if (isFolder ()) {
            super.refresh (added, removed, fire, expected);
        } else {
            // check the time of a file last modification
            if (fire) {
                java.util.Date l = getAbstractFileSystem ().info.lastModified (toString ());

                if (lastModified == null) {
                    lastModified = l;
                    return;
                }
                // JST: Seems like the lastModified () time can vary a bit on NT (up to 500ms)
                //        if (!l.equals (lastModified)) {
                //
                if (Math.abs(lastModified.getTime() - l.getTime ()) >= 5000) {
                    /*
                    System.out.println("file     : " + toString ());          
                    System.out.println("prev date: " + lastModified.getTime ());
                    System.out.println("now  date: " + l.getTime());
                    System.out.println("diff     : " + (lastModified.getTime () - l.getTime()));
                    */

                    lastModified = l;

                    if (hasAtLeastOneListeners ()) {
                        FileEvent ev = new FileEvent (this, this);
                        ev.setExpected (expected);
                        fileChanged0 (ev);
                    }
                }
            }
        }

        //    System.out.println ("Refresh of " + this + " ended"); // NOI18N
        return;
    }

    /** Implementation of lock for abstract files.
    */
    private class AfLock extends FileLock {
        public void releaseLock () {
            if (this.isValid()) {
                super.releaseLock();
                unlock ();
            }
        }
    }

    //
    // Invalid object that can be created after deserialization
    //

    static final class Invalid extends FileObject {
        static final long serialVersionUID =-4558997829579415276L;

        /** name */
        private String name;
        private String fullName;

        /** special instance that represent root */
        private static final Invalid ROOT = new Invalid (""); // NOI18N

        /** Constructor. Takes reference to file system this file belongs to.
        *
        * @param fs file system
        * @param parent the parent object
        * @param name name of the object
        */
        public Invalid (String name) {
            fullName = name;
            int i = name.lastIndexOf ('/') + 1;
            this.name = i == 0 || i == name.length () ? name : name.substring (i);
        }

        /** Get the name without extension of this file.
        *
        * @return name of the file (in its enclosing folder)
        */
        public String getName () {
            int i = name.lastIndexOf ('.');
            return i == -1 ? name : name.substring (0, i);
        }

        /** Get the extension of this file.
        * This is the string after the last dot of the full name, if any.
        *
        * @return extension of the file (if any) or empty string if there is none
        */
        public String getExt () {
            int i = name.lastIndexOf ('.') + 1;
            return i == 0 || i == name.length () ? "" : name.substring (i); // NOI18N
        }

        /** @exception FileStateInvalidException always
        */
        public FileSystem getFileSystem () throws FileStateInvalidException {
            throw new FileStateInvalidException ();
        }

        //
        // Info
        //

        /** Test whether this object is the root folder.
        * The root should always be a folder.
        * @return true if the object is the root of a file system
        */
        public boolean isRoot () {
            return this == ROOT;
        }

        /** Test whether this object is a folder.
        * @return true if the file object is a folder (i.e., can have children)
        */
        public boolean isFolder () {
            return this == ROOT;
        }

        /**
        * Get last modification time.
        * @return the date
        */
        public java.util.Date lastModified() {
            return new java.util.Date ();
        }

        /** Test whether this object is a data object.
        * This is exclusive with {@link #isFolder}.
        * @return true if the file object represents data (i.e., can be read and written)
        */
        public boolean isData () {
            return false;
        }

        /** Test whether this file can be written to or not.
        * @return <CODE>true</CODE> if file is read-only
        */
        public boolean isReadOnly () {
            return false;
        }

        /** Test whether the file is valid. The file can be invalid if it has been deserialized
        * and the file no longer exists on disk; or if the file has been deleted.
        *
        * @return true if the file object is valid
        */
        public boolean isValid () {
            return false;
        }


        /** Get the MIME type of this file.
        * The MIME type identifies the type of the file's contents and should be used in the same way as in the <B>Java
        * Activation Framework</B> or in the {@link java.awt.datatransfer} package.
        * <P>
        * The default implementation calls {@link FileUtil#getMIMEType}.
        *
        * @return the MIME type textual representation, e.g. <code>"text/plain"</code>
        */
        public String getMIMEType () {
            return "content/unknown"; // NOI18N
        }

        /** Get the size of the file.
        * @return the size of the file in bytes or zero if the file does not contain data (does not
        *  exist or is a folder).
        */
        public long getSize () {
            return 0;
        }

        /** Get input stream.
        * @return an input stream to read the contents of this file
        * @exception FileNotFoundException if the file does not exists or is invalid
        */
        public InputStream getInputStream () throws java.io.FileNotFoundException {
            throw new java.io.FileNotFoundException ();
        }

        /** Get output stream.
        * @param lock the lock that belongs to this file (obtained by a call to
        *   {@link #lock})
        * @return output stream to overwrite the contents of this file
        * @exception IOException if an error occures (the file is invalid, etc.)
        */
        public synchronized OutputStream getOutputStream (FileLock lock)
        throws java.io.IOException {
            throw new java.io.IOException ();
        }

        /** Lock this file.
        * @return lock that can be used to perform various modifications on the file
        * @throws FileAlreadyLockedException if the file is already locked
        */
        public synchronized FileLock lock () throws IOException {
            throw new java.io.IOException ();
        }

        /** Indicate whether this file is important from a user perspective.
        * This method allows a file system to distingush between important and
        * unimportant files when this distinction is possible.
        * <P>
        * <em>For example:</em> Java sources have important <code>.java</code> files and
        * unimportant <code>.class</code> files. If the file system provides
        * an "archive" feature it should archive only <code>.java</code> files.
        * @param b true if the file should be considered important
        */
        public void setImportant (boolean b) {
        }



        /** Get the file attribute with the specified name.
        * @param attrName name of the attribute
        * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
        */
        public Object getAttribute(String attrName) {
            return null;
        }

        /** Set the file attribute with the specified name.
        * @param attrName name of the attribute
        * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular file systems may or may not use serialization to store attribute values.
        * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
        */
        public void setAttribute(String attrName, Object value) throws IOException {
            throw new java.io.IOException ();
        }

        /** Get all file attribute names for this file.
        * @return enumeration of keys (as strings)
        */
        public Enumeration getAttributes() {
            return EmptyEnumeration.EMPTY;
        }


        /** Create a new folder below this one with the specified name. Fires
        * <code>fileCreated</code> event.
        *
        * @param name the name of folder to create (without extension)
        * @return the new folder
        * @exception IOException if the folder cannot be created (e.g. already exists)
        */
        public synchronized FileObject createFolder (String name) throws IOException {
            throw new java.io.IOException ();
        }

        /** Create new data file in this folder with the specified name. Fires
        * <code>fileCreated</code> event.
        *
        * @param name the name of data object to create (should not contain a period)
        * @param ext the extension of the file (or <code>null</code> or <code>""</code>)
        *
        * @return the new data file object
        * @exception IOException if the file cannot be created (e.g. already exists)
        */
        public synchronized FileObject createData (String name, String ext) throws IOException {
            throw new java.io.IOException ();
        }

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
        public void rename(FileLock lock, String name, String ext) throws IOException {
            throw new java.io.IOException ();
        }

        /** Delete this file. If the file is a folder and it is not empty then
        * all of its contents are also recursively deleted.
        *
        * @param lock the lock obtained by a call to {@link #lock}
        * @exception IOException if the file could not be deleted
        */
        public void delete (FileLock lock) throws IOException {
            throw new java.io.IOException ();
        }

        //
        // List
        //

        /** Get parent folder.
        * The returned object will satisfy {@link #isFolder}.
        *
        * @return common root for all invalid objects
        */
        public FileObject getParent () {
            return this == ROOT ? null : ROOT;
        }


        /** Get all children of this folder (files and subfolders). If the file does not have children
        * (does not exist or is not a folder) then an empty array should be returned. No particular order is assumed.
        *
        * @return array of direct children
        * @see #getChildren(boolean)
        * @see #getFolders
        * @see #getData
        */
        public synchronized FileObject[] getChildren () {
            return new FileObject[0];
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
        public synchronized FileObject getFileObject (String name, String ext) {
            return null;
        }

        /** Refresh the contents of a folder. Rescans the list of children names.
        */
        public void refresh() {
        }


        //
        // Listeners section
        //

        /** Add new listener to this object.
        * @param l the listener
        */
        public void addFileChangeListener (FileChangeListener fcl) {
        }


        /** Remove listener from this object.
        * @param l the listener
        */
        public void removeFileChangeListener (FileChangeListener fcl) {
        }
    } // end of Invalid

    /** Replace that stores name of fs and file.
    */
    static final class Replace extends Object implements java.io.Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -8543432135435542113L;

        private String fsName;
        private String fileName;

        /** Constructor
        */
        public Replace (String fsName, String fileName) {
            this.fsName = fsName;
            this.fileName = fileName;
        }

        /** Finds the right file.
        */
        public Object readResolve () {
            Repository rep = TopManager.getDefault ().getRepository ();
            FileSystem fs = rep.findFileSystem (fsName);
            FileObject fo = null;
            if (fs != null) {
                // scan desired system
                fo = fs.findResource (fileName);
            }

            if (fo == null) {
                // scan all systems
                fo = rep.findResource (fileName);
            }

            if (fo == null) {
                // create invalid file instead
                return new Invalid (fileName);
            }

            return fo;
        }

    } // end of Replace

}

/*
 * Log
 *  35   Gandalf-post-FCS1.33.3.0    3/29/00  Svatopluk Dedic Now throws 
 *       NullPointerException if name or ext (in case of files) is null instead 
 *       of renaming to whatever.null
 *  34   src-jtulach1.33        1/13/00  Ian Formanek    NOI18N
 *  33   src-jtulach1.32        1/12/00  Ian Formanek    NOI18N
 *  32   src-jtulach1.31        1/5/00   Jaroslav Tulach AbstractFileSystem.refreshResource
 *        modifies lastModified time
 *  31   src-jtulach1.30        12/30/99 Jaroslav Tulach New dialog for 
 *       notification of exceptions.
 *  30   src-jtulach1.29        11/3/99  Jaroslav Tulach File is modified if the 
 *       difference is more than 5s.
 *  29   src-jtulach1.28        11/2/99  Jaroslav Tulach Added comments to see 
 *       why a file is modified.
 *  28   src-jtulach1.27        10/29/99 Jaroslav Tulach MultiFileSystem + 
 *       FileStatusEvent
 *  27   src-jtulach1.26        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  26   src-jtulach1.25        10/5/99  Miloslav Metelka close() fires only once
 *  25   src-jtulach1.24        10/1/99  Jaroslav Tulach FileObject.move & 
 *       FileObject.copy
 *  24   src-jtulach1.23        9/27/99  Miloslav Metelka patched releaseLock()
 *  23   src-jtulach1.22        9/25/99  Jaroslav Tulach Works copying of files 
 *       without extension.
 *  22   src-jtulach1.21        9/17/99  Miloslav Metelka super.releaseLock() in 
 *       lock()
 *  21   src-jtulach1.20        9/3/99   Jaroslav Tulach #3320
 *  20   src-jtulach1.19        8/31/99  Pavel Buzek     
 *  19   src-jtulach1.18        8/18/99  Jaroslav Tulach Handles folders with 
 *       dots.
 *  18   src-jtulach1.17        8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  17   src-jtulach1.16        8/2/99   Jaroslav Tulach Invalid files after 
 *       serialization also have root.
 *  16   src-jtulach1.15        7/20/99  Jesse Glick     Filenames with multiple 
 *       dots should use last one for extension.
 *  15   src-jtulach1.14        7/20/99  Jaroslav Tulach Not valid file system 
 *       has not children.
 *  14   src-jtulach1.13        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  13   src-jtulach1.12        6/3/99   Jaroslav Tulach Refresh of only opened 
 *       files.
 *  12   src-jtulach1.11        5/24/99  Jaroslav Tulach 
 *  11   src-jtulach1.10        5/17/99  Jaroslav Tulach Even after delete the 
 *       name is valid.
 *  10   src-jtulach1.9         5/6/99   Jaroslav Tulach Survives when root of FS
 *       is deleted.
 *  9    src-jtulach1.8         4/9/99   Jaroslav Tulach 
 *  8    src-jtulach1.7         4/7/99   Petr Hamernik   Find works (once more) -
 *       bugfix
 *  7    src-jtulach1.6         3/27/99  Jaroslav Tulach Find work.
 *  6    src-jtulach1.5         3/26/99  Jesse Glick     [JavaDoc]
 *  5    src-jtulach1.4         3/26/99  Jaroslav Tulach 
 *  4    src-jtulach1.3         3/26/99  Jaroslav Tulach Refresh & Bundles
 *  3    src-jtulach1.2         3/26/99  Jaroslav Tulach 
 *  2    src-jtulach1.1         3/26/99  Jaroslav Tulach 
 *  1    src-jtulach1.0         3/24/99  Jaroslav Tulach 
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
