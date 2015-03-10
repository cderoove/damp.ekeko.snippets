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

import org.openide.util.enum.*;

/** The base for all filesystems that are build above a top of
* other ones. This system expects at most one filesystem it should write
* to and any number of filesystems to read from.
*
* If there is more versions of one file than the one from writable file system 
* is prefered or the read only systems are scanned in the given order.
*
* @author Jaroslav Tulach
*/
public class MultiFileSystem extends FileSystem {
    static final long serialVersionUID =-767493828111559560L;

    /** what extension to add to file that mask another ones */
    static final String MASK = "_hidden"; // NOI18N

    /** array of fs. the file system at position 0 can be null, because
    * it is writable file system. Others are only for read access
    */
    final FileSystem[] systems;

    /** root */
    private transient MultiFileObject root;

    /** index of the file system with write access */
    private static final int WRITE_SYSTEM_INDEX = 0;

    /** Creates new MultiFileSystem.
    * @param array of file systems (can contain nulls)
    */
    public MultiFileSystem (FileSystem[] fileSystems) {
        this.systems = fileSystems;
    }

    /** This file system is readonly if it has not writable system.
    */
    public boolean isReadOnly () {
        return systems[WRITE_SYSTEM_INDEX] == null;
    }

    /** The name of the file system.
    */
    public String getDisplayName () {
        return getString ("CTL_MultiFileSystem");
    }

    /** Root of the file system.
    */
    public FileObject getRoot () {
        return getMultiRoot ();
    }

    /** Root of the file system.
    */
    private MultiFileObject getMultiRoot () {
        if (root == null) {
            synchronized (this) {
                if (root == null) {
                    root = new MultiFileObject (this);
                }
            }
        }
        return root;
    }

    /** No special actions.
    */
    public org.openide.util.actions.SystemAction[] getActions () {
        return new org.openide.util.actions.SystemAction[0];
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
        return getMultiRoot ().find (en);
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
            return getMultiRoot ();
        } else {
            StringTokenizer tok = new StringTokenizer (name, "/"); // NOI18N
            return getMultiRoot ().find (tok);
        }
    }

    //
    // Helper methods for subclasses
    //

    /** For given file object finds the file system that the object is placed on.
    * The object must be created by this file system orherwise IllegalArgumentException
    * is thrown.
    *
    * @param fo file object
    * @return the file system (from the list we delegate to) the object has file on
    * @exception IllegalArgumentException if the file object is not represented in this file system
    */
    protected final FileSystem findSystem (FileObject fo) throws IllegalArgumentException {
        try {
            if (fo instanceof MultiFileObject) {
                MultiFileObject mfo = (MultiFileObject)fo;
                return mfo.getLeaderFileSystem ();
            }
        } catch (FileStateInvalidException ex) {
        }

        throw new IllegalArgumentException (fo.toString());
    }

    /** Marks a resource as hidden. It will not be listed in the list of files.
    * Uses createMaskOn method to determine on which file system to mark the file.
    *
    * @param res resource name of file to hide or show
    * @param hide true if we should hide the file/false otherwise
    * @exception IOException if it is not possible
    */
    protected final void hideResource (String res, boolean hide) throws IOException {
        if (hide) {
            // mask file
            maskFile (createWritableOn (res), res);
        } else {
            unmaskFile (createWritableOn (res), res);
        }
    }

    /** Finds all hidden files on given file system. The methods scans all files for
    * ones with hidden extension and returns enumeration of names of files
    * that are hidden.
    *
    * @param folder folder to start at
    * @param rec proceed recursivelly
    * @return enumeration of String with names of hidden files
    */
    protected static Enumeration hiddenFiles (FileObject folder, boolean rec) {
        Enumeration allFiles = folder.getChildren (rec);
        Enumeration allNull = new AlterEnumeration (allFiles) {
                                  public Object alter (Object fo) {
                                      String sf = ((FileObject)fo).getPackageNameExt ('/', '.');
                                      if (sf.endsWith (MASK)) {
                                          return sf.substring (0, sf.length () - MASK.length ());
                                      } else {
                                          return null;
                                      }
                                  }
                              };
        return new FilterEnumeration (allNull) {
                   public boolean accept (Object o) {
                       return o != null;
                   }
               };
    }

    //
    // methods for subclass customization
    //

    /** Finds the system to create writable version of the file on.
    *
    * @param name name of the file (full)
    * @return the first one
    * @exception IOException if the file system is readonly
    */
    protected FileSystem createWritableOn (String name) throws IOException {
        if (systems[WRITE_SYSTEM_INDEX] == null) {
            FSException.io ("EXC_FSisRO", getDisplayName ()); // NOI18N
        }
        return systems[WRITE_SYSTEM_INDEX];
    }

    /** Notification that a file has migrated from one file system
    * to another. Usually when somebody writes to file on readonly file
    * system and the file has to be copied to write one. 
    * <P>
    * This method allows subclasses to fire for example FileSystem.PROP_STATUS
    * change to notify that annotation of this file should change.
    *
    * @param fo file object that change its actual file system
    */
    protected void notifyMigration (FileObject fo) {

    }

    /** Notification that a file has been marked unimportant.
    * 
    *
    * @param fo file object that change its actual file system
    */
    protected void markUnimportant (FileObject fo) {
    }


    //
    // Private methods
    //

    /** Receives name of a resource and array of three elements and
    * splits the name into folder, name and extension.
    *
    * @param res resource name
    * @param store array to store data to
    */
    private static String[] split (String res, String[] store) {
        if (store == null) {
            store = new String[3];
        }

        int file = res.lastIndexOf ('/');
        int dot = res.lastIndexOf ('.');

        if (file == -1) {
            store[0] = ""; // NOI18N
        } else {
            store[0] = res.substring (0, file);
        }

        file++;

        if (dot == -1) {
            store[1] = res.substring (file);
            store[2] = ""; // NOI18N
        } else {
            store[1] = res.substring (file, dot);
            store[2] = res.substring (dot + 1);
        }

        return store;
    }


    /** Computes a list of FileObjects in the right order
    * that can represent this instance.
    *
    * @param name of resource to find
    * @return enumeration of FileObject
    */
    Enumeration delegates (final String name) {
        Enumeration en = new ArrayEnumeration (systems);

        Enumeration objsAndNulls = new AlterEnumeration (en) {
                                       public Object alter (Object o) {
                                           FileSystem fs = (FileSystem)o;
                                           if (fs == null) {
                                               return null;
                                           } else {
                                               return fs.findResource(name);
                                           }
                                       }
                                   };

        return new FilterEnumeration (objsAndNulls) {
                   public boolean accept (Object o) {
                       return o != null;
                   }
               };
    }

    /** Creates a file object that will mask the given file.
    * @param fs file system to work on
    * @param res resource name of the file
    * @exception IOException if it fails
    */
    static void maskFile (FileSystem fs, String res) throws IOException {
        FileObject fo = FileUtil.createData (fs.getRoot (), res + MASK);
    }

    /** Creates a file object that will mask the given file.
    * @param fs file system to work on
    * @param res resource name of the file
    * @exception IOException if it fails
    */
    static void unmaskFile (FileSystem fs, String res) throws IOException {
        FileObject fo = fs.findResource (res + MASK);

        if (fo != null) {
            FileLock lock = fo.lock ();
            try {
                fo.delete (lock);
            } finally {
                lock.releaseLock ();
            }
        }
    }
}

/*
* Log
*  11   Gandalf   1.10        1/12/00  Ian Formanek    NOI18N
*  10   Gandalf   1.9         12/30/99 Jaroslav Tulach New dialog for 
*       notification of exceptions.
*  9    Gandalf   1.8         11/3/99  Jaroslav Tulach Can create new files over
*       hidden ones.
*  8    Gandalf   1.7         10/29/99 Jaroslav Tulach MultiFileSystem + 
*       FileStatusEvent
*  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         6/1/99   Jaroslav Tulach delete works.
*  2    Gandalf   1.1         5/31/99  Jaroslav Tulach Write/rename/delete
*  1    Gandalf   1.0         5/20/99  Jaroslav Tulach 
* $
*/

