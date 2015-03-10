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

package org.netbeans.core;

import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.lang.ref.*;

import org.openide.TopManager;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.*;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataLoaderPool;
import org.openide.nodes.FilterNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.Children.SortedMap;
import org.openide.nodes.NodeAdapter;
import org.openide.util.WeakListener;
import org.openide.util.RequestProcessor;
import org.openide.util.enum.AlterEnumeration;
import org.openide.util.enum.EmptyEnumeration;
import org.openide.util.enum.SingletonEnumeration;
import org.openide.util.enum.SequenceEnumeration;

/**
* Produces list of packages.
*
* @author   Jan Jancura, Jaroslav Tulach
*/
final class Packages extends Object
            implements RepositoryListener, Comparator, FileChangeListener,
    PropertyChangeListener {
    /** values for notification of changes when parsing large number
    * of packages 
    */
    private static final int MIN_LIMIT = 8;
    private static final int MUL_LIMIT = 2;

    /** priority for parsing
    */
    private static final int PRIORITY_WRITE = 3;
    /** priority for reading results from the
    * queue
    */
    // is bigger to go before parsing
    private static final int PRIORITY_READ = 4;


    /** static instance */
    private static Reference ref = new WeakReference (null);


    /** property names. list of all files, fired when list of all files
    * is changed
    */
    public static final String PROP_LIST = "list"; // NOI18N

    /** property names. fired when a name of a file is changed. than the
    * new value contains the file object that chanded the name
    */
    public static final String PROP_NAME = "name"; // NOI18N


    /** weak listener to changes in files */
    private FileChangeListener weakFCL = WeakListener.fileChange (this, null);
    /** weak listener to changes in hidden property of file systems */
    private PropertyChangeListener weakPCL = WeakListener.propertyChange (
                this, null
            );

    /** Set of all folders in the system. (FileObject)
    */
    private TreeSet fileFolders;

    /** Supporting value for reorder of filesystems. Maps
    * (FileSystem, Integer) where int is the index of the filesystem
    * in repository
    * @associates Integer
    */
    private Map indexOfFileSystem;

    /** request processor for processing of all packages.
    */
    private RequestProcessor PROCESSOR = new RequestProcessor (
                                             "All packages processor" // NOI18N
                                         );

    /** property change support.
    */
    private PropertyChangeSupport pcs = new PropertyChangeSupport (this);

    private Packages () {
    }

    /** When finalized, stop the request processor
    */
    protected void finalize () {
        PROCESSOR.stop();
    }


    /** Starts the initialization.
    */
    private void initialize () {
        if (fileFolders == null) {
            synchronized (this) {
                if (fileFolders == null) {
                    fileFolders = new TreeSet (this);

                    Repository rep = TopManager.getDefault ().getRepository ();
                    rep.addRepositoryListener (WeakListener.repository (this, rep));
                    // read all packages in the repository
                    putPackages (null);

                    // attach listener to hidden property change
                    Enumeration en = rep.fileSystems ();
                    while (en.hasMoreElements()) {
                        FileSystem fs = (FileSystem)en.nextElement();
                        fs.addPropertyChangeListener (weakPCL);
                    }
                }
            }

        }
    }

    //
    // The only public methods
    //

    /** Getter for default instance.
    */
    public static Packages getDefault () {
        Packages p = (Packages)ref.get ();
        if (p == null) {
            synchronized (Packages.class) {
                if (p == null) {
                    p = new Packages ();
                    ref = new WeakReference (p);
                }
            }
        }
        return p;
    }



    public void addPropertyChangeListener (PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener (pcl);
    }

    public void removePropertyChangeListener (PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener (pcl);
    }

    /** Called when a PackageChildren object wants to update
    * its state
    */
    public void update (final PackageChildren ch) {
        initialize ();

        PROCESSOR.post(new Runnable () {
                           public void run () {
                               ch.updatePackages (fileFolders);
                           }
                       }, 0, PRIORITY_READ);
    }

    //
    // end of public methods
    //




    /** Method for sending working tasks into the queue.
    */
    private void postTask (Runnable r) {
        PROCESSOR.post (r, 0, PRIORITY_WRITE);
    }

    // FileChangeListener support

    /** Fired when a new folder is created. This action can only be
     * listened to in folders containing the created folder up to the root of
     * file system.
     *
     * @param fe the event describing context where action has taken place
     */
    public void fileFolderCreated(FileEvent fe) {
        putPackages (fe.getFile ());
    }

    /** Fired when a new file is created. This action can only be
     * listened in folders containing the created file up to the root of
     * file system.
     *
     * @param fe the event describing context where action has taken place
     */
    public void fileDataCreated(FileEvent fe) {
    }

    /** Fired when a file is changed.
     * @param fe the event describing context where action has taken place
     */
    public void fileChanged(FileEvent fe) {
    }

    /** Fired when a file is deleted.
     * @param fe the event describing context where action has taken place
     */
    public void fileDeleted(final FileEvent fe) {
        if (fe.getFile () != fe.getSource ()) return;

        postTask (new Runnable () {
                      public void run () {
                          fileFolders.remove (fe.getFile ());
                          pcs.firePropertyChange (PROP_LIST, null, null);
                      }
                  });
    }

    /** Fired when a file is renamed.
     * @param fe the event describing context where action has taken place
     *           and the original name and extension.
     */
    public void fileRenamed (final FileRenameEvent fe) {
        if (fe.getFile () != fe.getSource ()) return;

        postTask (new Runnable () {
                      public void run () {
                          pcs.firePropertyChange (PROP_NAME, null, fe.getFile ());
                      }
                  });
    }

    /** Fired when a file attribute is changed.
     * @param fe the event describing context where action has taken place,
     *           the name of attribute and the old and new values.
     */
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    // RepositoryListener support .........................................................

    /**
    * Adds packages for given FS.
    */
    public void fileSystemAdded (final RepositoryEvent ev) {
        FileSystem fs = ev.getFileSystem ();
        if (fs.isHidden ()) return;


        putPackages (fs.getRoot());
        fs.addPropertyChangeListener(weakPCL);
    }

    /**
    * Removes packages of given FS.
    */
    public void fileSystemRemoved (final RepositoryEvent ev) {
        FileSystem fs = ev.getFileSystem ();
        removeFileSystemPackages (fs);
        fs.addPropertyChangeListener(weakPCL);
    }

    /**
    * Does nothing.
    */
    public void fileSystemPoolReordered (RepositoryReorderedEvent ev) {
        //    task = reinitialize based on original fileFolders
        postTask (new Runnable () {
                      public void run () {
                          LinkedList ll = new LinkedList (fileFolders);

                          // this changes the comparator
                          indexOfFileSystem = null;

                          // so we have to reinsert new values
                          fileFolders.clear ();
                          fileFolders.addAll (ll);
                      }
                  });

    }

    public void propertyChange(final java.beans.PropertyChangeEvent p1) {
        if (FileSystem.PROP_HIDDEN.equals(p1.getPropertyName())) {
            FileSystem fs = (FileSystem)p1.getSource();
            if (fs.isHidden()) {
                removeFileSystemPackages (fs);
            } else {
                putPackages (fs.getRoot());
            }
        }
    }

    // main methods ......................................................................

    /** Create enumeration of all subfiles under given file.
    * @param fo file object
    * @return enum of FileObjects
    */
    private static Enumeration createSubFolders (FileObject fo) {
        return new SequenceEnumeration (
                   new SingletonEnumeration (fo),
                   fo.getFolders (true)
               );
    }

    /** Create enumeration of all folders on all file systems.
    * @return enum of FileObjects
    */
    private static Enumeration createFolders () {
        return new SequenceEnumeration (
                   new AlterEnumeration (
                       TopManager.getDefault ().getRepository ().getFileSystems ()
                   ) {
                       protected Object alter (Object o) {
                           FileSystem fs = (FileSystem)o;
                           if (fs.isHidden () || !fs.isValid ()) {
                               return EmptyEnumeration.EMPTY;
                           }
                           return createSubFolders (fs.getRoot ());
                       }
                   }
               );
    }

    /**
    * Returns TreeMap of file => nodes contained in given FileObject. For null
    * returns map of all files.
    */
    private void putPackages (
        FileObject source
    ) {
        Enumeration en = source == null ?
                         createFolders () : createSubFolders (source);

        postTask (processPackages (en, MIN_LIMIT));
    }

    /** Processes packages in the enumeration.
    * @param en the enumeration of files to process
    * @param cnt max count of packages to process in this task
    */
    private Runnable processPackages (
        final Enumeration en,
        final int cnt
    ) {
        return new Runnable () {
                   private int limit = cnt;

                   public void run () {
                       if (!en.hasMoreElements ()) return;

                       int i = 0;
                       while (en.hasMoreElements () && i++ < limit) {
                           FileObject fo = (FileObject) en.nextElement ();
                           fo.addFileChangeListener (weakFCL);
                           fileFolders.add (fo);
                       }
                       pcs.firePropertyChange (PROP_LIST, null, null);

                       // post this runnable again till the enumeration
                       // is empty
                       limit *= MUL_LIMIT;
                       postTask (this);
                   }
               };
    }

    /** Removes all packages from given file system.
    */
    private void removeFileSystemPackages (final FileSystem fs) {
        postTask (new Runnable () {
                      public void run () {
                          Iterator it = fileFolders.iterator ();
                          while (it.hasNext()) {
                              FileObject fo = (FileObject)it.next ();
                              try {
                                  if (fo.getFileSystem ().equals (fs)) {
                                      // file is on deleted file system
                                      it.remove ();
                                  }
                              } catch (FileStateInvalidException e) {
                                  // remove too
                                  it.remove ();
                              }
                          }
                          pcs.firePropertyChange (PROP_LIST, null, null);
                      }
                  });
    }

    /** Getter for index of file system.
    */
    private int indexOf (FileSystem fs) {
        Map ifs = indexOfFileSystem;

        if (ifs == null) {
            synchronized (this) {
                ifs = indexOfFileSystem;
                if (ifs == null) {
                    Repository rep = TopManager.getDefault ().getRepository ();
                    FileSystem[] arr = rep.toArray ();

                    ifs = indexOfFileSystem = new HashMap ((int) (arr.length * 1.3));
                    for (int i = 0; i < arr.length; i++) {
                        indexOfFileSystem.put (arr[i], new Integer (i));
                    }
                }
            }
        }

        Integer i = (Integer)ifs.get (fs);
        return i == null ? -1 : i.intValue ();
    }

    /** Comparator for two FileObjects representing folders.
    */
    public int compare (Object o1, Object o2) {
        try {
            FileObject fo1 = (FileObject)o1;
            FileObject fo2 = (FileObject)o2;

            FileSystem fs1 = fo1.getFileSystem ();
            FileSystem fs2 = fo2.getFileSystem ();

            if (fs1.equals (fs2)) {
                return fo1.getPackageName ('.').compareTo (fo2.getPackageName ('.'));
            } else {
                return indexOf (fs1) - indexOf (fs2);
            }
        } catch (FileStateInvalidException ee) {
            return 0;
        }
    }

}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Jaroslav Tulach I18N
 *  4    Gandalf   1.3         1/9/00   Jaroslav Tulach Renaming works better.
 *  3    Gandalf   1.2         1/7/00   Jaroslav Tulach #5156
 *  2    Gandalf   1.1         11/29/99 Jaroslav Tulach Updates while parsing.  
 *  1    Gandalf   1.0         11/29/99 Jaroslav Tulach 
 * $
 */
