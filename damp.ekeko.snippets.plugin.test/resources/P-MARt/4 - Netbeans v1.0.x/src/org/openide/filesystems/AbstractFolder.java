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

/** Implementation of the file that simplyfies common
* tasks with hierarchy of objects for AbstractFileObject and MultiFileObject.
*
* @author Jaroslav Tulach, 
*/
abstract class AbstractFolder extends FileObject {
    /** empty array */
    private static final AbstractFileObject[] EMPTY_ARRAY = new AbstractFileObject[0];


    /** default extension separator */
    private static final char EXT_SEP = '.';

    /** empty hash map to mark that we are initialized */
    private static final HashMap EMPTY = new HashMap (0);

    /** file system */
    private FileSystem system;

    /** name of the file (only name and extension) */
    protected String name;

    /** strong reference to parent (can be null for root) */
    protected final AbstractFolder parent;

    /** Stores the system name of the file system to test
    * validity later.
    */
    protected String systemName;

    /** list of children */
    private String[] children;

    /** map that assignes file object to names. (String, Reference (AbstractFileObject)) 
     * @associates WeakReference*/
    private HashMap map;

    /** listeners */
    private EventListenerList listeners;

    /** Reference to full name of the file */
    protected Reference fullName;

    /** Constructor. Takes reference to file system this file belongs to.
    *
    * @param fs the file system
    * @param parent the parent object (folder)
    * @param name name of the object (e.g. <code>filename.ext</code>)
    */
    public AbstractFolder(
        FileSystem fs, AbstractFolder parent, String name
    ) {
        this.system = fs;
        this.parent = parent;
        this.name = name;
        this.systemName = fs.getSystemName ();
    }

    /** To obtain real full name.
    */
    final String getNameExt () {
        // used in MultiFileSystem
        return name;
    }

    /* Get the name without extension of this file.
    *
    * @return name of the file (in its enclosing folder)
    */
    public final String getName () {
        int i = name.lastIndexOf ('.');
        return i == -1 || isFolder () ? name : name.substring (0, i);
    }

    /* Get the extension of this file.
    * This is the string after the last dot of the full name, if any.
    *
    * @return extension of the file (if any) or empty string if there is none
    */
    public final String getExt () {
        int i = name.lastIndexOf ('.') + 1;
        return i == 0 || i == name.length () ? "" : name.substring (i); // NOI18N
    }

    /** Get fully-qualified filename. Does so by walking through all folders
    * to the root of the file system. Separates files with provided <code>separatorChar</code>.
    * The extension, if present, is separated from the basename with <code>extSepChar</code>.
    *
    * @param separatorChar char to separate folders and files
    * @param extSepChar char to separate extension
    * @return the fully-qualified filename
    */
    public final String getPackageNameExt (char separatorChar, char extSepChar) {
        StringBuffer sb = new StringBuffer (50);

        constructName (sb, separatorChar, extSepChar);

        return sb.toString ();
    }

    /** Helper method to construct file name for a file system.
    * @param sb buffer to add text to
    * @param sepChar char to separate characters from
    * @param extChar char to use to separate extension
    * @return true if something has been added to the list
    */
    private boolean constructName (StringBuffer sb, char sepChar, char extChar) {
        if (parent == null) {
            return false;
        }
        if (parent.constructName (sb, sepChar, extChar)) {
            sb.append (sepChar);
        }
        sb.append (name.replace (EXT_SEP, extChar));
        return true;
    }

    /* Getter for the right file system */
    public final FileSystem getFileSystem () {
        return system;
    }

    //
    // Info
    //

    /* Test whether this object is the root folder.
    * The root should always be a folder.
    * @return true if the object is the root of a file system
    */
    public final boolean isRoot () {
        return parent == null;
    }


    /* Test whether the file is valid. The file can be invalid if it has been deserialized
    * and the file no longer exists on disk; or if the file has been deleted.
    *
    * @return true if the file object is valid
    */
    public final boolean isValid () {
        // valid
        if (parent == null) {
            return true;
        }

        // can use == because the system name uses intern () method of string
        return getFileSystem ().getSystemName () == systemName;
    }

    //
    // List
    //

    /* Get parent folder.
    * The returned object will satisfy {@link #isFolder}.
    *
    * @return the parent folder or <code>null</code> if this object {@link #isRoot}.
    */
    public final FileObject getParent () {
        return parent;
    }


    /* Get all children of this folder (files and subfolders). If the file does not have children
    * (does not exist or is not a folder) then an empty array should be returned. No particular order is assumed.
    *
    * @return array of direct children
    * @see #getChildren(boolean)
    * @see #getFolders
    * @see #getData
    */
    public final synchronized FileObject[] getChildren () {
        check ();

        if (children == null) {
            return new FileObject[0];
        }

        int size = children.length;
        FileObject[] arr = new FileObject[size];

        for (int i = 0; i < size; i++) {
            arr[i] = getChild (children[i]);
        }

        return arr;
    }

    /** Tries to find a resource.
    * @param en enumeration of strings to scan
    * @return found object or null
    */
    final FileObject find (Enumeration en) {
        AbstractFolder fo = this;
        while (fo != null && en.hasMoreElements ()) {
            // try to go on
            // lock to provide safety for getChild
            synchronized (fo) {
                // JST: Better to call the check only here,
                // than in getChild, than it is not called
                // so often.
                fo.check ();

                fo = fo.getChild ((String)en.nextElement ());
            }
        }
        // no next requirements or not found
        return fo;
    }

    /** Tries to find a resource if it exists in memory.
    * @param en enumeration of strings to scan
    * @return found object or null
    */
    final FileObject findIfExists (Enumeration en) {
        AbstractFolder fo = this;
        while (fo != null && en.hasMoreElements ()) {
            if (fo.map == null) {
                // this object is not initialized yet
                return null;
            }

            // try to go on
            // lock to provide safety for getChild
            synchronized (fo) {
                // JST: Better to call the check only here,
                // than in getChild, than it is not called
                // so often.
                fo.check ();

                fo = fo.getChild ((String)en.nextElement ());
            }
        }
        // no next requirements or not found
        return fo;
    }

    /** Finds one child for given name .
    * @param name the name of the child
    * @return the file object or null if it does not exist
    */
    protected final AbstractFolder getChild (String name) {
        Reference r = (Reference)map.get (name);
        if (r == null) {
            return null;
        }

        AbstractFolder fo = (AbstractFolder)(r.get ());

        if (fo == null) {
            // object does not exist => have to recreate it
            fo = createFile (name);
            map.put (name, new WeakReference (fo));
        }

        return fo;
    }

    /** Obtains enumeration of all existing subfiles.
    */
    final synchronized AbstractFileObject[] subfiles () {
        if (map == null) {
            return EMPTY_ARRAY;
        }
        Iterator it = map.values ().iterator ();
        LinkedList ll = new LinkedList ();
        while (it.hasNext ()) {
            Reference r = (Reference)it.next ();
            if (r == null) {
                continue;
            }
            AbstractFolder fo = (AbstractFolder)r.get ();
            if (fo != null && (!fo.isFolder () || fo.map != null)) {
                // if the file object exists and either is not folder (then
                // we have to check the time) or it is folder and it has
                // some children
                // => use it
                ll.add (fo);
            }
        }

        return (AbstractFileObject[])ll.toArray (EMPTY_ARRAY);
    }

    /* Retrieve file contained in this folder by name.
    * <em>Note</em> that no file is created on disk.
    * @param name basename of the file (in this folder)
    * @param ext extension of the file; <CODE>null</CODE> or <code>""</code>
    *    if the file should have no extension
    * @return the object representing this file or <CODE>null</CODE> if the file
    *   does not exist
    * @exception IllegalArgumentException if <code>this</code> is not a folder
    */
    public final synchronized FileObject getFileObject (String name, String ext) {
        check ();

        if (ext == null || ext.equals ("")) { // NOI18N
            return getChild (name);
        } else {
            return getChild (name + EXT_SEP + ext);
        }
    }

    /* Refresh the contents of a folder. Rescans the list of children names.
    */
    public final void refresh(boolean expected) {
        refresh (null, null, true, expected);
    }


    //
    // Listeners section
    //

    /* Add new listener to this object.
    * @param l the listener
    */
    public final void addFileChangeListener (FileChangeListener fcl) {
        if (listeners == null) {
            synchronized (EMPTY_ARRAY) {
                if (listeners == null) {
                    listeners = new EventListenerList ();
                }
            }
        }
        listeners.add (FileChangeListener.class, fcl);
    }


    /* Remove listener from this object.
    * @param l the listener
    */
    public final void removeFileChangeListener (FileChangeListener fcl) {
        if (listeners != null) {
            listeners.remove (FileChangeListener.class, fcl);
        }
    }

    /** Fires event */
    protected final void fileDeleted0(FileEvent fileevent) {
        super.fireFileDeletedEvent(listeners (), fileevent);
        if(fileevent.getFile().equals(this) && parent != null) {
            FileEvent ev = new FileEvent(parent, fileevent.getFile());
            ev.setExpected (fileevent.isExpected ());
            parent.fileDeleted0(ev);
        }
    }

    /** Fires event */
    protected final void fileCreated0(FileEvent fileevent, boolean flag) {
        if(flag)
            super.fireFileDataCreatedEvent(listeners (), fileevent);
        else
            super.fireFileFolderCreatedEvent(listeners (), fileevent);

        if(fileevent.getFile().equals(this) && parent != null) {
            FileEvent ev = new FileEvent(parent, fileevent.getFile());
            ev.setExpected (fileevent.isExpected ());
            parent.fileCreated0 (ev, flag);
        }
    }

    /** Fires event */
    protected final void fileChanged0 (FileEvent fileevent) {
        super.fireFileChangedEvent(listeners (), fileevent);
        if(fileevent.getFile().equals(this) && parent != null) {
            FileEvent ev = new FileEvent(parent, fileevent.getFile());
            ev.setExpected (fileevent.isExpected ());
            parent.fileChanged0 (ev);
        }
    }

    /** Fires event */
    protected final void fileRenamed0 (FileRenameEvent filerenameevent) {
        super.fireFileRenamedEvent(listeners (), filerenameevent);
        if(filerenameevent.getFile().equals(this) && parent != null) {
            FileRenameEvent ev = new FileRenameEvent(
                                     parent,
                                     filerenameevent.getFile(),
                                     filerenameevent.getName(),
                                     filerenameevent.getExt()
                                 );
            ev.setExpected (filerenameevent.isExpected ());
            parent.fileRenamed0 (ev);
        }
    }

    /** Fires event */
    protected final void fileAttributeChanged0 (FileAttributeEvent fileattributeevent) {
        super.fireFileAttributeChangedEvent(listeners (), fileattributeevent);
        if(fileattributeevent.getFile().equals(this) && parent != null) {
            FileAttributeEvent ev = new FileAttributeEvent(
                                        parent,
                                        fileattributeevent.getFile(),
                                        fileattributeevent.getName(),
                                        fileattributeevent.getOldValue(),
                                        fileattributeevent.getNewValue()
                                    );
            ev.setExpected (fileattributeevent.isExpected ());
            parent.fileAttributeChanged0 (ev);
        }
    }

    /** @return true if there is a listener
    */
    protected final boolean hasListeners () {
        return listeners != null && listeners.getListenerList ().length != 0;
    }

    /** @return true if this folder or its parent have listeners
    */
    protected final boolean hasAtLeastOneListeners () {
        return hasListeners () || (parent != null && parent.hasListeners ());
    }

    /** @return enumeration of all listeners.
    */
    private final Enumeration listeners () {
        if (listeners == null) {
            return EmptyEnumeration.EMPTY;
        } else {
            return new FilterEnumeration (new ArrayEnumeration (listeners.getListenerList ())) {
                       public boolean accept (Object o) {
                           return o != FileChangeListener.class;
                       }
                   };
        }
    }

    //
    // Refreshing the state of the object
    //

    /** Test if the file has been checked and if not, refreshes its
    * content.
    */
    private final void check () {
        if (map == null) {
            refresh (null, null, false, false);
            if (map == null) {
                // create empty map to mark that we are initialized
                map = EMPTY;
            }
        }
    }

    /** Refresh the content of file. Ignores changes to the files provided,
    * instead returns its file object.
    * @param added do not notify addition of this file
    * @param removed do not notify removing of this file
    */
    protected final void refresh (String added, String removed) {
        refresh (added, removed, true, false);
    }

    /** Method that allows subclasses to return its children.
    *
    * @return names (name . ext) of subfiles
    */
    protected abstract String[] list ();

    /** Method to create a file object for given subfile.
    * @param name of the subfile
    * @return the file object
    */
    protected abstract AbstractFolder createFile (String name);

    /** Refresh the content of file. Ignores changes to the files provided,
    * instead returns its file object.
    * @param added do not notify addition of this file
    * @param removed do not notify removing of this file
    * @param fire true if we should fire changes
    * @param expected true if the change has been expected by the user
    */
    protected synchronized void refresh (
        String added, String removed, boolean fire, boolean expected
    ) {
        if (isFolder ()) {
            // refresh of folder checks children
            String[] newChildren = list ();

            if (newChildren == null && parent == null) {
                // if root => we have to have children
                newChildren = new String[0];
            }

            if (children == null && newChildren == null) {
                // no change and we are still date file
                return;
            }

            //    System.out.println ("Refresh: " + this + " fire: " + fire); // NOI18N
            //    Thread.dumpStack ();

            // new map (String, AbstractFileObject)
            HashMap m;
            // set of added files (String)
            Set add;
            // moreover map will contain only such files that disappeared
            int newChildrenContainNull = 0;

            if (newChildren != null) {
                int size = newChildren.length;

                m = new HashMap (size);
                add = new HashSet (size);

  
                Object replaceInSteadOfRemoved = map != null ? map.get (removed) : null;


                for (int i = 0; i < size; i++) {
                    String ch = newChildren[i];

                    if (ch == null) {
                        // ignore this and
                        newChildrenContainNull++;
                        continue;
                    }

                    Reference old = map == null ? null : (Reference)map.remove (ch);
                    if (old == null) {
                        // create new empty reference
                        old = new WeakReference (null);
                        add.add (ch);
                    }
                    m.put (ch, old);
                }

                if (added != null && replaceInSteadOfRemoved != null) {
                  m.put (added, replaceInSteadOfRemoved);
                }

            } else {
                m = new HashMap (0);
                add = Collections.EMPTY_SET;
            }

            // maps (String, AbstractFileObject) between objects that has diappeared
            HashMap disappeared = null;

            if (fire) {
                // a set of files to notify that has been added
                if (added != null) {
                    add.remove (added);
                }

                if (map != null) {
                    //
                    // MAP IS CHANGING TO (String, AbstractFileObject)
                    // this should be fine cause map is replaced immediatelly after
                    // this loop
                    //

                    disappeared = map;
                    Iterator it = map.entrySet ().iterator ();
                    while (it.hasNext ()) {
                        Map.Entry entry = (Map.Entry)it.next ();
                        // uses the value and replaces it immediatelly
                        entry.setValue (getChild ((String)entry.getKey ()));
                    }

                    // remove the removed
                    if (removed != null) {
                        disappeared.remove (removed);
                    }

                    if (disappeared.isEmpty ()) {
                        disappeared = null;
                    }
                }
            }

            // use the new map
            // Map now contains the right content
            map = m;

            if (newChildrenContainNull != 0) {
                // exclude nulls from the array
                String[] arr = new String[newChildren.length - newChildrenContainNull];

                int j = 0;
                for (int i = 0; i < newChildren.length; i++) {
                    if (newChildren[i] != null) {
                        arr[j++] = newChildren[i];
                    }
                }
                children = arr;
            } else {
                // ok, no nulls in the array
                children = newChildren;
            }


            if (fire && !add.isEmpty () && hasAtLeastOneListeners ()) {
                // fire these files has been added
                for (Iterator it = add.iterator (); it.hasNext (); ) {
                    String name = (String)it.next ();

                    AbstractFolder fo = getChild (name);
                    FileEvent ev = new FileEvent (this, fo);
                    ev.setExpected (expected);
                    if (fo.isFolder ()) {
                        fileCreated0 (ev, false);
                    } else {
                        fileCreated0 (ev, true);
                    }
                    // a change in children
                    //        changed = true;
                }
            }

            if (fire && disappeared != null && hasAtLeastOneListeners ()) {
                // fire these files has been removed
                for (Iterator it = disappeared.values ().iterator (); it.hasNext (); ) {
                    AbstractFolder fo = (AbstractFolder)it.next ();

                    FileEvent ev = new FileEvent (this, fo);
                    ev.setExpected (expected);
                    fo.fileDeleted0 (ev);
                    // a change happened
                    //        changed = true;
                }
            }
        }

        //    System.out.println ("Refresh of " + this + " ended"); // NOI18N
        return;
    }

    /** Constructs the full name of the object.
    * @return the name, e.g. <code>path/from/root.ext</code>
    */
    public final String toString () {
        return getPackageNameExt ('/', '.');
    }

    //
    // Serialization
    //

    public final Object writeReplace () {
        return new AbstractFileObject.Replace (getFileSystem ().getSystemName (), toString ());
    }


    /** Output stream that notifies about change of the file when it is closed.
    */
    final class NotifyOutputStream extends FilterOutputStream {
        private boolean closed;

        public NotifyOutputStream (OutputStream os) {
            super (os);
        }

        /** Faster implementation of writing than is implemented in
        * the filter output stream.
        */
        public void write (byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        public void close () throws IOException {
            if (!closed) {
                closed = true;
                try {
                    super.close ();
                } finally {
                    fileChanged0 (new FileEvent (AbstractFolder.this));
                }
            }
        }
    } // end of NotifyOutputStream
}

/*
 * Log
 *  9    Gandalf-post-FCS1.7.3.0     4/4/00   Jaroslav Tulach Faster writing to notify
 *       output stream. Backported from Jaga.
 *  8    Gandalf   1.7         1/14/00  Jaroslav Tulach refresh (expected)
 *  7    Gandalf   1.6         1/13/00  Ian Formanek    NOI18N
 *  6    Gandalf   1.5         1/12/00  Ian Formanek    NOI18N
 *  5    Gandalf   1.4         1/9/00   Jaroslav Tulach When a new file is 
 *       created a fileDataCreated is fired and not fileDataChanged.
 *  4    Gandalf   1.3         1/5/00   Jaroslav Tulach AbstractFileSystem.refreshResource
 *        modifies lastModified time
 *  3    Gandalf   1.2         11/25/99 Jaroslav Tulach List.children () can 
 *       return array that contains nulls
 *  2    Gandalf   1.1         11/24/99 Jaroslav Tulach FileEvent can be 
 *       expected + fired by AbstractFileSystem
 *  1    Gandalf   1.0         10/29/99 Jaroslav Tulach 
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
