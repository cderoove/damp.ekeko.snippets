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
import java.util.Hashtable;
import java.util.Vector;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ArrayList;

import org.openide.TopManager;
import org.openide.util.io.NbMarshalledObject;

/** This singleton object contains all {@link FileSystem}s in the IDE.
* <P>
* At any given time, no two file systems in the pool may share the same {@link FileSystem#getSystemName name}
* (unless all but one are {@link FileSystem#isValid invalid}).
*
* <p>Use {@link TopManager#getRepository} to retrieve the singleton.
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public final class Repository extends Object implements org.openide.nodes.Node.Cookie, java.io.Serializable {
    /** list of file systems (FileSystem) 
     * @associates FileSystem*/
    private ArrayList fileSystems;

    /** the system file system */
    private FileSystem system;

    /** hashtable that maps system names to FileSystems 
     * @associates FileSystem*/
    private Hashtable names;

    /** hashtable for listeners on changes in the file system.
    * Its elements are of type (RepositoryListener, RepositoryListener)
    * @associates RepositoryListener
    */
    private Hashtable listeners = new Hashtable ();

    /** default Repository implementation */
    private static Repository defaultPool;

    /** vetoable listener on systemName property of file system */
    private VetoableChangeListener vetoListener = new VetoableChangeListener () {
                /** @param ev event with changes */
                public void vetoableChange (PropertyChangeEvent ev)
                throws PropertyVetoException {
                    if (ev.getPropertyName ().equals ("systemName")) {
                        String nv = (String)ev.getNewValue ();
                        if (names.get (nv) != null) {
                            // changing systemName to name which is already there
                            String msg = ""; // TBD // NOI18N
                            throw new PropertyVetoException (msg, ev);
                        }
                    }
                }
            };

    /** property listener on systemName property of file system */
    private PropertyChangeListener propListener = new PropertyChangeListener () {
                /** @param ev event with changes */
                public void propertyChange (PropertyChangeEvent ev) {
                    if (ev.getPropertyName ().equals ("systemName")) {
                        // assign the property to new name
                        String ov = (String)ev.getOldValue ();
                        String nv = (String)ev.getNewValue ();
                        FileSystem fs = (FileSystem)ev.getSource ();
                        if (fs.isValid ()) {
                            // when a file system is valid then it is attached to a name
                            names.remove (ov);
                        }
                        // register name of the file system
                        names.put (nv, fs);
                        // the file system becomes valid
                        fs.setValid (true);
                    }
                }
            };

    static final long serialVersionUID =-6344768369160069704L;
    /** Creates new instance of file system pool and
    * registers it as the default one. Also registers the default file system.
    *
    * @param def the default filesystem
    */
    public Repository (FileSystem def) {
        this.system = def;
        java.net.URL.setURLStreamHandlerFactory(
            new org.openide.execution.NbfsStreamHandlerFactory());
        init ();
    }

    /** Initialazes the pool.
    * @exception SecurityException if CoronaStreamHandlerFactory is already registered.
    */
    private void init () {
        // empties the pool
        fileSystems = new ArrayList ();
        names = new Hashtable ();
        addFileSystem (system);
    }

    /** Gets the default filesystem of the IDE.
    * @return the default filesystem
    */
    public FileSystem getDefaultFileSystem () {
        return system;
    }

    /** Adds new file system to the pool.
    * <em>Note</em> that a file system cannot be assigned to more than one file
    *   system pool at one time (though currently there is only one pool anyway).
    * @param fs file system to add
    */
    public final void addFileSystem (FileSystem fs) {
        synchronized (Repository.class) {
            // if the file system is not assigned yet
            if (!fs.assigned && !fileSystems.contains(fs)) {
                // new file system
                fileSystems.add(fs);
                String systemName = fs.getSystemName ();

                boolean isReg = names.get (systemName) == null;
                if (isReg && !systemName.equals ("")) { // NOI18N
                    // file system with the same name is not there => then it is valid
                    names.put (systemName, fs);
                    fs.setValid (true);
                } else {
                    // there is another file system with the same name => it is invalid
                    fs.setValid (false);
                }
                // mark the file system as being assigned
                fs.assigned = true;
                // mark as a listener on changes in the file system
                fs.addPropertyChangeListener (propListener);
                fs.addVetoableChangeListener (vetoListener);

                // fire info about new file system
                fireFileSystem (fs, true);
            }
        }
    }

    /** Removes a file system from the pool.
    * @param fs file system to remove
    */
    public final void removeFileSystem (FileSystem fs) {
        synchronized (Repository.class) {
            if (fs.isDefault()) return;
            if (fileSystems.remove(fs)) {
                // the file system realy was here
                if (fs.isValid ()) {
                    // if the file system is valid then is in names hashtable
                    names.remove (fs.getSystemName ());
                }
                // in all cases remove it from listeners
                fs.removePropertyChangeListener (propListener);
                fs.removeVetoableChangeListener (vetoListener);

                fireFileSystem (fs, false);
            }
            // unassign the file system
            fs.assigned = false;
        }
    }

    /** Reorders {@link FileSystem}s by given permutation.
     * For example, if there are three file systems, <code>new int[] {2, 0, 1}</code> cycles the file systems forwards.
    * @param perm an array of integers
    * @throws IllegalArgumentException if the array is not a permutation, or is not the same length as the current number of file systems in the pool
    */
    public final void reorder(int[] perm) {
        synchronized (getClass()) {
            if (perm == null || perm.length != fileSystems.size() || !isPermutation(perm))
                throw new IllegalArgumentException();

            ArrayList newList = new ArrayList (fileSystems.size ());
            int len = perm.length;
            for (int i = 0; i < len; i++) {
                newList.add (fileSystems.get (perm[i]));
            }
            fileSystems = newList;
            fireFileSystemReordered(perm);
        }
    }


    /** @return true if the parameter describes a permutation */
    private static boolean isPermutation(int[] perm) {
        final int len = perm.length;
        boolean[] bool = new boolean[len];
        try {
            for (int i = 0; i < len; i++) {
                if (bool[perm[i]]) return false;
                else bool[perm[i]] = true;
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    /** Returns enumeration of all file systems.
    * @return enumeration of type {@link FileSystem}
    */
    public final Enumeration getFileSystems () {
        return java.util.Collections.enumeration ((java.util.Collection)fileSystems.clone());
    }

    /** Returns enumeration of all file systems.
    * @return enumeration of type {@link FileSystem}
    */
    public final Enumeration fileSystems () {
        return getFileSystems ();
    }


    /** Returns a sorted array of file systems. */
    public final FileSystem[] toArray() {
        synchronized (getClass()) {
            FileSystem[] fss = new FileSystem[fileSystems.size()];
            fileSystems.toArray(fss);
            return fss;
        }
    }

    /** Finds file system when only its system name is known.
    * @param systemName {@link FileSystem#getSystemName name} of the file system
    * @return the file system or <CODE>null</CODE> if there is no such
    *   file system
    */
    public final FileSystem findFileSystem (String systemName) {
        FileSystem fs = (FileSystem)names.get (systemName);
        return fs;
    }

    /** Saves pool to stream by saving all file systems.
    * The default (system) file system, or any non-persistent file systems, are skipped.
    *
    * @param oos object output stream
    * @exception IOException if an error occures
    */
    public void writeExternal (ObjectOutput oos) throws IOException {
        synchronized (getClass()) {
            Iterator iter = fileSystems.iterator();
            while (iter.hasNext()) {
                FileSystem fs = (FileSystem)iter.next();

                if (!fs.isDefault () && !fs.isPersistent ()) {
                    oos.writeObject (new NbMarshalledObject (fs));
                }
            }
            oos.writeObject (null);
        }
    }

    /** Reads object from stream.
    * Reads all filesystems. Persistent and system file systems are untouched; all others are removed and possibly reread.
    * @param oos object output stream
    * @exception IOException if an error occures
    * @exception ClassNotFoundException if read class is not found
    */
    public void readExternal (ObjectInput ois)
    throws IOException, ClassNotFoundException {
        ArrayList temp = new ArrayList(10);
        synchronized (Repository.class) {

            for (;;) {
                Object obj = ois.readObject ();
                if (obj == null) {
                    // all system has been read in
                    break;
                }
                FileSystem fs;
                if (obj instanceof FileSystem) {
                    fs = (FileSystem)obj;
                } else {
                    try {
                        NbMarshalledObject mar = (NbMarshalledObject)obj;
                        fs = (FileSystem)mar.get ();
                    } catch (IOException ex) {
                        TopManager.getDefault().notifyException(ex);
                        fs = null;
                    } catch (ClassNotFoundException ex) {
                        TopManager.getDefault().notifyException(ex);
                        fs = null;
                    }
                }
                if (fs != null) {
                    // add the new file system
                    temp.add(fs);
                }
            }

            Enumeration ee = getFileSystems();
            FileSystem fs;
            while (ee.hasMoreElements()) {
                fs = (FileSystem) ee.nextElement();
                if (!fs.isPersistent ()) {
                    removeFileSystem (fs);
                }
            }
            // in init assigned is checked and we force 'system' to be added again
            system.assigned = false;
            init ();

            // all is successfuly read
            for (Iterator iter = temp.iterator(); iter.hasNext();)
                addFileSystem ((FileSystem) iter.next());
        }
    }



    /** Finds file when its name is provided. It scans in the list of
    * file systems and asks them for the specified file by a call to
    * {@link FileSystem#find find}. The first object that is found is returned or <CODE>null</CODE>
    * if none of the file systems contain such a file.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain the name of a package and not a file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    a package and not a file name
    *
    * @return {@link FileObject} that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    public final FileObject find (String aPackage, String name, String ext) {
        Enumeration en = getFileSystems ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            FileObject fo = fs.find (aPackage, name, ext);
            if (fo != null) {
                // object found
                return fo;
            }
        }
        return null;
    }


    /** Searches for the given resource among all file systems.
    * @see FileSystem#findResource
    * @param name a name of the resource
    * @return file object or <code>null</code> if the resource can not be found
    */
    public FileObject findResource(String name) {
        Enumeration en = getFileSystems ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            FileObject fo = fs.findResource(name);
            if (fo != null) {
                // object found
                return fo;
            }
        }
        return null;
    }

    /** Searches for the given resource among all file systems, returning all matches.
    * @param name name of the resource
    * @return enumeration of {@link FileObject}s
    */
    public Enumeration findAllResources(String name) {
        Vector v = new Vector(8);
        Enumeration en = getFileSystems ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            FileObject fo = fs.findResource(name);
            if (fo != null) {
                v.addElement(fo);
            }
        }
        return v.elements();
    }

    /** Finds all files among all file systems matching a given name, returning all matches.
    * All file systems are queried with {@link FileSystem#find}.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain the name of a package and not a file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    a package and not a file name
    *
    * @return enumeration of {@link FileObject}s
    */
    public final Enumeration findAll (String aPackage, String name, String ext) {
        Enumeration en = getFileSystems ();
        Vector ret = new Vector();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            FileObject fo = fs.find (aPackage, name, ext);
            if (fo != null) {
                ret.addElement(fo);
            }
        }
        return ret.elements();
    }

    /** Fire info about changes in the file system pool.
    * @param fs file system
    * @param add <CODE>true</CODE> if the file system is added,
    *   <CODE>false</CODE> if it is removed
    */
    private void fireFileSystem (FileSystem fs, boolean add) {
        Enumeration en = ((Hashtable)listeners.clone ()).elements ();
        RepositoryEvent ev = new RepositoryEvent (this, fs, add);
        while (en.hasMoreElements ()) {
            RepositoryListener list = (RepositoryListener)en.nextElement ();
            if (add) {
                list.fileSystemAdded (ev);
            } else {
                list.fileSystemRemoved (ev);
            }
        }
    }

    /** Fires info about reodering
    * @param perm
    */
    private void fireFileSystemReordered(int[] perm) {
        Enumeration en = ((Hashtable)listeners.clone ()).elements ();
        RepositoryReorderedEvent ev = new RepositoryReorderedEvent(this, perm);
        while (en.hasMoreElements ()) {
            RepositoryListener list = (RepositoryListener)en.nextElement ();
            list.fileSystemPoolReordered(ev);
        }
    }

    /** Adds new listener.
    * @param list the listener
    */
    public void addRepositoryListener (RepositoryListener list) {
        listeners.put (list, list);
    }

    /** Removes listener.
    * @param list the listener
    */
    public void removeRepositoryListener (RepositoryListener list) {
        listeners.remove (list);
    }

    /** Writes the object to the stream.
    */
    private Object writeReplace () {
        return new java.io.Serializable () {
                   /** serial version UID */
                   static final long serialVersionUID=-3874531277726540941L;

                   private void writeObject (ObjectOutputStream oos) throws IOException {
                       TopManager.getDefault ().getRepository ().writeExternal (oos);
                   }

                   private void readObject (ObjectInputStream ois)
                   throws IOException, ClassNotFoundException {
                       TopManager.getDefault ().getRepository ().readExternal (ois);
                   }

                   /** @return the default pool */
                   public Object readResolve () {
                       return TopManager.getDefault ().getRepository ();
                   }
               };
    }
}


/*
 * Log
 *  13   Gandalf   1.12        1/13/00  Ian Formanek    NOI18N
 *  12   Gandalf   1.11        1/12/00  Ian Formanek    NOI18N
 *  11   Gandalf   1.10        11/29/99 Petr Kuzel      Repository node tagged 
 *       by Repository cookie.
 *  10   Gandalf   1.9         11/25/99 Jaroslav Tulach Safer serialization.
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         6/7/99   Jaroslav Tulach FS capabilities.
 *  5    Gandalf   1.4         3/26/99  Jaroslav Tulach 
 *  4    Gandalf   1.3         3/21/99  Jaroslav Tulach Repository displayed ok.
 *  3    Gandalf   1.2         3/19/99  Jaroslav Tulach Serial version UID
 *  2    Gandalf   1.1         3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  1    Gandalf   1.0         2/11/99  Ian Formanek    
 * $
 * Beta Change History:
 */
