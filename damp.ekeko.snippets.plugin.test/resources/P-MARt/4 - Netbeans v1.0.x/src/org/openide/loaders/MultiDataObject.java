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

package org.openide.loaders;

import java.lang.ref.WeakReference;
import java.io.*;
import java.util.*;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.enum.SequenceEnumeration;
import org.openide.util.enum.SingletonEnumeration;
import org.openide.util.WeakListener;
import org.openide.util.Mutex;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;

/** Provides support for handling of data objects with multiple files.
* One file is represented by one {@link Entry}. Each handler
* has one {@link #getPrimaryEntry primary} entry and zero or more secondary entries.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek
*/
public class MultiDataObject extends DataObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7750146802134210308L;

    /** getPrimaryEntry() is intended to have all inetligence for copy/move/... */
    private Entry primary;

    /** Map of secondary entries and its files. (FileObject, Entry) 
     * @associates Entry*/
    private HashMap secondary = new HashMap (11);

    /** array of cookies for this object */
    private CookieSet cookieSet;


    /** listener for changes in the cookie set */
    private EntryL cookieL = new EntryL ();

    /** listener to attach to file entries to listen if they are not removed */
    private FileChangeListener entryL = WeakListener.fileChange (cookieL, null);

    /** Create a handler.
    * @param fo the primary file object
    * @param loader loader of this data object
    */
    public MultiDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
    }

    /** Getter for the multi file loader that created this
    * object.
    *
    * @return the multi loader for the object
    */
    public final MultiFileLoader getMultiFileLoader () {
        return (MultiFileLoader)getLoader ();
    }

    /* Method to access all FileObjects used by this DataObject.
    * These file objects should have set the important flag to
    * allow the requester to distingush between important and
    * unimportant files.
    *
    * @return set of FileObjects
    */
    public Set files () {
        // enumeration of all files

        getMultiFileLoader ().checkFiles (this);

        HashSet s;
        synchronized (secondary) {
            s = new HashSet (secondary.keySet ());
            s.add (getPrimaryFile ());
        }
        return s;
    }

    /* Getter for delete action.
    * @return true if the object can be deleted
    */
    public boolean isDeleteAllowed() {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Getter for copy action.
    * @return true if the object can be copied
    */
    public boolean isCopyAllowed() {
        return true;
    }

    /* Getter for move action.
    * @return true if the object can be moved
    */
    public boolean isMoveAllowed() {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Getter for rename action.
    * @return true if the object can be renamed
    */
    public boolean isRenameAllowed () {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Provides node that should represent this data object.
    *
    * @return the node representation
    * @see DataNode
    */
    protected Node createNodeDelegate () {
        DataNode dataNode = (DataNode) super.createNodeDelegate ();
        return dataNode;
    }

    /** Add a new secondary entry to the list.
    * @param fe the entry to add
    */
    protected final void addSecondaryEntry (Entry fe) {
        synchronized (secondary) {
            secondary.put (fe.getFile (), fe);
            fe.getFile ().addFileChangeListener (entryL);
        }

        firePropertyChangeLater (PROP_FILES, null, null);
    }

    /** Remove a secondary entry from the list.
     * @param fe the entry to remove
    */
    protected final void removeSecondaryEntry (Entry fe) {
        synchronized (secondary) {
            secondary.remove (fe.getFile ());
        }

        firePropertyChangeLater (PROP_FILES, null, null);
    }

    /** All secondary entries are recognized. Called from multi file object.
    * @param recognized object to mark recognized file to
    */
    final void markSecondaryEntriesRecognized (DataLoader.RecognizedFiles recognized) {
        synchronized (secondary) {
            Iterator it = secondary.keySet ().iterator ();
            while (it.hasNext ()) {
                FileObject fo=(FileObject)it.next ();
                recognized.markRecognized (fo);
            }
        }
    }


    /** Tests whether this file is between entries and if not,
    * creates a secondary entry for it and adds it into set of
    * secondary entries.
    * <P>
    * This method should be used in constructor of MultiDataObject to
    * register all the important files, that could belong to this data object.
    * As example, our XMLDataObject, tries to locate its <CODE>xmlinfo</CODE>
    * file and then do register it
    *
    * @param fo the file to register (can be null, then the action is ignored)
    * @return the entry associated to this file object (returns primary entry if the fo is null)
    */
    protected final Entry registerEntry (FileObject fo) {
        synchronized (secondary) {
            if (fo == null) {
                // is it ok, to do this or somebody would like to see different behavour?
                return primary;
            }
            if (fo.equals (getPrimaryFile ())) {
                return primary;
            }

            Entry e = (Entry)secondary.get (fo);
            if (e != null) {
                return e;
            }

            // add it into set of entries
            e = getMultiFileLoader ().createSecondaryEntry (this, fo);
            addSecondaryEntry (e);

            return e;
        }
    }

    /** Removes the entry from the set of secondary entries.
    * Called from the EntryL listener.
    */
    final void removeFile (FileObject fo) {
        synchronized (secondary) {
            Entry e = (Entry)secondary.get (fo);
            if (e != null) {
                removeSecondaryEntry (e);
            }
        }
    }

    /** Get the primary entry.
    * @return the entry
    */
    public final Entry getPrimaryEntry () {
        synchronized (secondary) {
            if (primary == null) {
                primary = getMultiFileLoader ().createPrimaryEntry (this, getPrimaryFile ());
            }
            return primary;
        }
    }

    /** Get secondary entries.
    * @return immutable set of {@link Entry}s
    */
    public final Set secondaryEntries () {
        synchronized (this) {
            return new HashSet (secondary.values ());
        }
    }

    /** For a given file, find the associated secondary entry.
    * @param fo file object
    * @return the entry associated with the file object, or <code>null</code> if there is no
    *    such entry
    */
    public final Entry findSecondaryEntry (FileObject fo) {
        return (Entry)secondary.get (fo);
    }


    //methods overriding DataObjectHandler's abstract methods

    /* Obtains lock for primary file by asking getPrimaryEntry() entry.
    *
    * @return the lock for primary file
    * @exception IOException if it is not possible to set the template
    *   state.
    */
    protected FileLock takePrimaryFileLock () throws IOException {
        return getPrimaryEntry ().takeLock ();
    }

    // XXX does nothing of the sort --jglick
    /** Check if in specific folder exists fileobject with the same name.
    * If it exists user is asked for confirmation to rewrite, rename or cancel operation.
    * @param folder destination folder
    * @return the suffix which should be added to the name or null if operation is cancelled
    */
    private static String existInFolder(FileObject fo, FileObject folder) {
        String orig = fo.getName ();
        String name = FileUtil.findFreeFileName(
                          folder, orig, fo.getExt ()
                      );
        if (name.length () <= orig.length ()) {
            return ""; // NOI18N
        } else {
            return name.substring (orig.length ());
        }
    }

    /** Copies primary and secondary files to new folder.
     * May ask for user confirmation before overwriting.
     * @param df the new folder
     * @return data object for the new primary
     * @throws IOException if there was a problem copying
     * @throws UserCancelException if the user cancelled the copy
    */
    protected synchronized DataObject handleCopy (DataFolder df) throws IOException {
        FileObject fo;

        synchronized (secondary) {
            String suffix = existInFolder(
                                getPrimaryEntry().getFile(),
                                df.getPrimaryFile ()
                            );
            if (suffix == null)
                throw new org.openide.util.UserCancelException();

            fo = getPrimaryEntry ().copy (df.getPrimaryFile (), suffix);
            Iterator it = secondary.values ().iterator ();
            while (it.hasNext ()) {
                ((Entry)it.next()).copy (df.getPrimaryFile (), suffix);
            }
        }
        try {
            return getMultiFileLoader ().createMultiObject (fo);
        } catch (DataObjectExistsException ex) {
            return ex.getDataObject ();
        }
    }

    /* Deletes all secondary entries, removes them from the set of
    * secondary entries and then deletes the getPrimaryEntry() entry.
    */
    protected void handleDelete() throws IOException {
        synchronized (secondary) {
            Iterator it = secondary.entrySet ().iterator ();
            while (it.hasNext ()) {
                Map.Entry e = (Map.Entry)it.next ();
                ((Entry)e.getValue ()).delete ();
                it.remove ();
            }
            getPrimaryEntry().delete();
        }
    }

    /* Renames all entries and changes their files to new ones.
    */
    protected FileObject handleRename (String name) throws IOException {
        synchronized (secondary) {
            getPrimaryEntry ().file = getPrimaryEntry().rename (name);

            HashMap add = null;

            Iterator it = secondary.entrySet ().iterator ();
            while (it.hasNext ()) {
                Map.Entry e = (Map.Entry)it.next ();
                FileObject fo = ((Entry)e.getValue ()).rename (name);
                if (fo == null) {
                    // remove the entry
                    it.remove ();
                } else {
                    if (!fo.equals (e.getKey ())) {
                        // put the new one into change table
                        if (add == null) add = new HashMap ();
                        Entry entry = (Entry)e.getValue ();
                        add.put (fo, entry);
                        entry.file = fo;

                        // changed the file => remove the file
                        it.remove ();
                    }
                }
            }

            // if there has been a change in files, apply it
            if (add != null) {
                secondary.putAll (add);
                firePropertyChangeLater (PROP_FILES, null, null);
            }

            return getPrimaryEntry ().file;
        }
    }

    /** Moves primary and secondary files to a new folder.
     * May ask for user confirmation before overwriting.
     * @param df the new folder
     * @return the moved primary file object
     * @throws IOException if there was a problem moving
     * @throws UserCancelException if the user cancelled the move
    */
    protected FileObject handleMove (DataFolder df) throws IOException {
        synchronized (secondary) {
            String suffix = existInFolder(getPrimaryEntry().getFile(), df.getPrimaryFile ());
            if (suffix == null)
                throw new org.openide.util.UserCancelException();

            getPrimaryEntry ().file = getPrimaryEntry ().move (df.getPrimaryFile (), suffix);

            HashMap add = null;

            Iterator it = secondary.entrySet ().iterator ();
            while (it.hasNext ()) {
                Map.Entry e = (Map.Entry)it.next ();
                FileObject fo = ((Entry)e.getValue ()).move (df.getPrimaryFile (), suffix);
                if (fo == null) {
                    // remove the entry
                    it.remove ();
                } else {
                    if (!fo.equals (e.getKey ())) {
                        // put the new one into change table
                        if (add == null) add = new HashMap ();
                        Entry entry = (Entry)e.getValue ();
                        add.put (fo, entry);
                        entry.file = fo;

                        // changed the file => remove the file
                        it.remove ();
                    }
                }
            }

            // if there has been a change in files, apply it
            if (add != null) {
                secondary.putAll (add);
                firePropertyChangeLater (PROP_FILES, null, null);
            }

            return getPrimaryEntry ().file;
        }
    }

    /* Creates new object from template.
    * @exception IOException
    */
    protected DataObject handleCreateFromTemplate (
        DataFolder df, String name
    ) throws IOException {
        FileObject fo;

        synchronized (secondary) {

            if (name == null) {
                name = FileUtil.findFreeFileName(
                           df.getPrimaryFile (), getPrimaryFile ().getName (), getPrimaryFile ().getExt ()
                       );
            }

            fo = getPrimaryEntry().createFromTemplate (df.getPrimaryFile (), name);
            Iterator it = secondary.values ().iterator ();
            while (it.hasNext ()) {
                ((Entry)it.next()).createFromTemplate (df.getPrimaryFile (), name);
            }
        }
        try {
            return getMultiFileLoader ().createMultiObject (fo);
        } catch (DataObjectExistsException ex) {
            return ex.getDataObject ();
        }
    }

    // XXX:
    // Protected to be accessible
    // only from subclasses.
    /** Set the set of cookies.
     * To the provided cookie set a listener is attached,
    * and any change to the set is propagated by
    * firing a change on {@link #PROP_COOKIE}.
    *
    * @param s the cookie set to use
    */
    public synchronized void setCookieSet (CookieSet s) {
        if (cookieSet != null) {
            cookieSet.removeChangeListener (cookieL);
        }

        s.addChangeListener (cookieL);
        cookieSet = s;

        fireCookieChange ();
    }

    /** Get the set of cookies.
     * If the set had been
    * previously set by {@link #setCookieSet}, that set
    * is returned. Otherwise an empty set is
    * returned.
    *
    * @return the cookie set (never <code>null</code>)
    */
    public CookieSet getCookieSet () {
        CookieSet s = cookieSet;
        if (s != null) return s;
        synchronized (this) {
            if (cookieSet != null) return cookieSet;

            // sets empty sheet and adds a listener to it
            setCookieSet (new CookieSet ());
            return cookieSet;
        }
    }

    /** Look for a cookie in the current cookie set matching the requested class.
    *
    * @param type the class to look for
    * @return an instance of that class, or <code>null</code> if this class of cookie
    *    is not supported
    */
    public Node.Cookie getCookie (Class type) {
        CookieSet c = cookieSet;
        if (c != null) {
            Node.Cookie cookie = c.getCookie (type);
            if (cookie != null) return cookie;
        }
        return super.getCookie (type);
    }

    /** Fires cookie change.
    */
    final void fireCookieChange () {
        firePropertyChange (PROP_COOKIE, null, null);
    }

    /** Fires property change but in event thread.
    */
    private void firePropertyChangeLater (
        final String name, final Object oldV, final Object newV
    ) {
        Mutex.EVENT.readAccess (new Runnable () {
                                    public void run () {
                                        firePropertyChange (name, oldV, newV);
                                    }
                                });
    }

    /** Represents one file in a {@link MultiDataObject group data object}. */
    public abstract class Entry implements java.io.Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 6024795908818133571L;

        /** modified from MultiDataObject operations, that is why it is package
        * private
        */
        FileObject file;

        /** This factory is used for creating new clones of the holding lock for internal
        * use of this DataObject. It factory is null it means that the file entry is not
        */
        private transient WeakReference lock;

        protected Entry (FileObject file) {
            this.file = file;
        }

        /** Get the file this entry works with.
        */
        public final FileObject getFile () {
            return file;
        }

        /** Get the multi data object this entry is assigned to.
         * @return the data object
        */
        public final MultiDataObject getDataObject () {
            return MultiDataObject.this;
        }

        /** Called when the entry is to be copied.
        * Depending on the entry type, it should either copy the underlying <code>FileObject</code>,
        * or do nothing (if it cannot be copied).
        * @param f the folder to create this entry in
        * @param name the new name to use
        * @return the copied <code>FileObject</code> or <code>null</code> if it cannot be copied
        * @exception IOException when the operation fails
        */
        public abstract FileObject copy (FileObject f, String suffix) throws IOException;

        /** Called when the entry is to be renamed.
        * Depending on the entry type, it should either rename the underlying <code>FileObject</code>,
        * or delete it (if it cannot be renamed).
        * @param name the new name
        * @return the renamed <code>FileObject</code> or <code>null</code> if it has been deleted
        * @exception IOException when the operation fails
        */
        public abstract FileObject rename (String name) throws IOException;

        /** Called when the entry is to be moved.
        * Depending on the entry type, it should either move the underlying <code>FileObject</code>,
        * or delete it (if it cannot be moved).
        * @param f the folder to move this entry to
        * @param suffix the suffix to use
        * @return the moved <code>FileObject</code> or <code>null</code> if it has been deleted
        * @exception IOException when the operation fails
        */
        public abstract FileObject move (FileObject f, String suffix) throws IOException;

        /** Called when the entry is to be deleted.
        * @exception IOException when the operation fails
        */
        public abstract void delete () throws IOException;

        /** Called when the entry is to be created from a template.
        * Depending on the entry type, it should either copy the underlying <code>FileObject</code>,
        * or do nothing (if it cannot be copied).
        * @param f the folder to create this entry in
        * @param name the new name to use
        * @return the copied <code>FileObject</code> or <code>null</code> if it cannot be copied
        * @exception IOException when the operation fails
        */
        public abstract FileObject createFromTemplate (FileObject f, String name) throws IOException;

        /** Try to lock this file entry.
        * @return the lock if the operation was successful; otherwise <code>null</code>
        * @throws IOException if the lock could not be taken
        */
        public FileLock takeLock() throws IOException {
            FileLock l = lock == null ? null : (FileLock)lock.get ();
            if (l == null || !l.isValid ()){
                l = getFile ().lock ();
                lock = new WeakReference (l);
            }
            return l;
        }

        /** Tests whether the entry is locked.
         * @return <code>true</code> if so
         */
        public boolean isLocked() {
            FileLock l = lock == null ? null : (FileLock)lock.get ();
            return l != null && l.isValid ();
        }

        public boolean equals(Object o) {
            if (! (o instanceof Entry)) return false;
            return file.equals(((Entry) o).file);
        }

        public int hashCode() {
            return file.hashCode();
        }

        /** Make a Serialization replacement.
         * The entry is identified by the
        * file object is holds. When serialized, it stores the
        * file object and the data object. On deserialization
        * it finds the data object and creates the right entry
        * for it.
        */
        protected Object writeReplace () {
            return new EntryReplace (file);
        }
    }


    /** File change listener attached to entries that
    * removes the entry from the hash map if it is deleted.
    */
    private class EntryL extends FileChangeAdapter implements ChangeListener {
        /** Fired when a file has been deleted.
        * @param fe the event describing context where action has taken place
        */
        public void fileDeleted (FileEvent fe) {
            removeFile (fe.getFile ());
        }

        /** State changed */
        public void stateChanged (ChangeEvent ev) {
            fireCookieChange ();
        }
    }

    /** Entry replace.
    */
    private static final class EntryReplace extends Object implements java.io.Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -1498798537289529182L;

        /** file object of the entry */
        private FileObject file;
        /** entry to be used during read */
        private transient Entry entry;

        public EntryReplace (FileObject fo) {
            file = fo;
        }

        private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject ();
            try {
                DataObject obj = DataObject.find (file);
                if (obj instanceof MultiDataObject) {
                    MultiDataObject m = (MultiDataObject)obj;

                    if (file.equals (m.getPrimaryFile ())) {
                        // primary entry
                        entry = m.getPrimaryEntry ();
                    } else {
                        // secondary entry
                        Entry e = (Entry)m.findSecondaryEntry (file);
                        if (e == null) {
                            throw new InvalidObjectException (obj.toString ());
                        }
                        // remember the entry
                        entry = e;
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                throw new InvalidObjectException (ex.getMessage ());
            }
        }

        public Object readResolve () {
            return entry;
        }
    }
}

/*
 * Log
 *  31   Gandalf   1.30        1/12/00  Ian Formanek    NOI18N
 *  30   Gandalf   1.29        12/2/99  Jaroslav Tulach DataObject.files () 
 *       should return correct results for all MultiFileObject subclasses that 
 *       collects objects from one folder.
 *  29   Gandalf   1.28        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  28   Gandalf   1.27        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  27   Gandalf   1.26        9/6/99   Jaroslav Tulach 
 *  26   Gandalf   1.25        9/3/99   Jaroslav Tulach Different synch.
 *  25   Gandalf   1.24        7/21/99  Jaroslav Tulach MultiDataObject can mark
 *       easily mark secondary entries in constructor as belonging to the 
 *       object.
 *  24   Gandalf   1.23        7/15/99  Ian Formanek    Fixed bug 1903 - 
 *       Exception during renaming bookmark, which was added to package.
 *  23   Gandalf   1.22        6/24/99  Jaroslav Tulach Property 
 *       synchronization.
 *  22   Gandalf   1.21        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  21   Gandalf   1.20        5/17/99  Jaroslav Tulach Fix 1703  
 *  20   Gandalf   1.19        5/7/99   Michal Fadljevic 
 *  19   Gandalf   1.18        5/7/99   Michal Fadljevic 
 *  18   Gandalf   1.17        5/7/99   Michal Fadljevic registerFile() first 
 *       condition clarified
 *  17   Gandalf   1.16        4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  16   Gandalf   1.15        4/22/99  Jaroslav Tulach Does not garbage listner
 *       EntryL
 *  15   Gandalf   1.14        4/21/99  Jaroslav Tulach 
 *  14   Gandalf   1.13        3/17/99  Jaroslav Tulach 
 *  13   Gandalf   1.12        3/15/99  Jesse Glick     [JavaDoc]
 *  12   Gandalf   1.11        3/14/99  Jaroslav Tulach Change of 
 *       MultiDataObject.Entry.
 *  11   Gandalf   1.10        3/14/99  Jaroslav Tulach 
 *  10   Gandalf   1.9         3/10/99  Jesse Glick     [JavaDoc]
 *  9    Gandalf   1.8         3/9/99   Jaroslav Tulach Works even there is no 
 *       secondary entry.
 *  8    Gandalf   1.7         3/3/99   David Simonek   
 *  7    Gandalf   1.6         2/11/99  Jan Jancura     Support for icons.
 *  6    Gandalf   1.5         2/1/99   Jaroslav Tulach 
 *  5    Gandalf   1.4         2/1/99   Jaroslav Tulach Entry is replaceable.
 *  4    Gandalf   1.3         1/6/99   Ian Formanek    Property update.
 *  3    Gandalf   1.2         1/6/99   Ales Novak      
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    changed FileEntry.createFromTemplate - added <String name>parameter
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    added implementation in PrimaryEntrySupport.createFromTemplate as copy + rename
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    added implementation in MirroringEntry.createFromTemplate as copy + rename
 *  0    Tuborg    0.14        --/--/98 Ales Novak      overwritten to DataObject
 *  0    Tuborg    0.17        --/--/98 Jaroslav Tulach PrimaryEntrySupport has method createDataObject to allow subclasses to do more
 *  0    Tuborg    0.17        --/--/98 Jaroslav Tulach clever creation of data objects
 *  0    Tuborg    0.18        --/--/98 Jan Formanek    changes in FileEntry - copy, createFromTemplate now return FileObject
 *  0    Tuborg    0.18        --/--/98 Jan Formanek    added method createDataObject
 *  0    Tuborg    0.19        --/--/98 Ales Novak      NotImplementedException removed from numb entry - template
 *  0    Tuborg    0.20        --/--/98 Petr Hamernik   locking redesigned
 *  0    Tuborg    0.21        --/--/98 Petr Hamernik   Entries little redesigned
 *  0    Tuborg    0.22        --/--/98 Petr Hamernik   some improvements
 */
