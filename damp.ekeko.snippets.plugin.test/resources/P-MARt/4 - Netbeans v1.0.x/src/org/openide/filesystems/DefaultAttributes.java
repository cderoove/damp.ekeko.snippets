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
import java.lang.ref.*;
import java.util.*;

import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.enum.EmptyEnumeration;
import org.openide.util.io.NbMarshalledObject;

/** Implementation of <code>AbstractFileSystem.Attr</code> using a special file
* in each folder for holding attributes.
* It needs to hide
* the file from the rest of system, so it also implements
* <code>AbstractFileSystem.List</code> to exclude the file from the children list
* (it can then serve to filter a plain list implementation).
* 
* @author Jaroslav Tulach
*/
public class DefaultAttributes extends Object
    implements AbstractFileSystem.Attr, AbstractFileSystem.List {
    static final long serialVersionUID=-5801291358293736478L;

    /** File name of special file in each folder where attributes are saved. */
    public final static String ATTR_NAME = "filesystem"; // NOI18N

    /** Extension of special file in each folder where attributes are saved. */
    public final static String ATTR_EXT = "attributes"; // NOI18N

    /** Name with extension of special file in each folder where attributes are saved. */
    public final static String ATTR_NAME_EXT = ATTR_NAME + '.' + ATTR_EXT;

    /** description of the fs to work on - info about files */
    private AbstractFileSystem.Info info;
    /** description of the fs to work on - work with files */
    private AbstractFileSystem.Change change;

    /** description of the fs to work on - listing of files */
    private AbstractFileSystem.List list;


    /** Cache of attributes.
    * For name of folder gives map of maps of attibutes
    * (String, Reference (Table))
    */
    private transient Map cache;

    /** Constructor.
    * @param info file object information to use
    * @param change file change hooks to use
    * @param list list to filter (can be <code>null</code>, but then this object cannot work as a list)
    */
    public DefaultAttributes (
        AbstractFileSystem.Info info,
        AbstractFileSystem.Change change,
        AbstractFileSystem.List list
    ) {
        this.info = info;
        this.change = change;
        this.list = list;
    }


    /** Get the children list, filtering out the special attributes file.
    * You <em>must</em> have provided a non-<code>null</code> {@link AbstractFileSystem.List}
    * in the constructor for this to work. If you did not, the rest of the class will work
    * fine, but this method should not be called and this object should not be used
    * as a <code>List</code> implementation.
    *
    * @param f the folder, by name; e.g. <code>top/next/afterthat</code>
    * @return a list of children of the folder, as <code>file.ext</code> (no path)
    */
    public String[] children (String f) {
        String[] arr = list.children (f);

        if (arr == null) {
            return null;
        }

        int size = arr.length;
        for (int i = 0; i < size; i++) {
            if (ATTR_NAME_EXT.equals (arr[i])) {
                // exclude this index
                arr[i] = null;
                // there can be only one file with attributes
                break;
            }
        }
        return arr;
    }


    // JST: Description
    //
    //
    // The class should be written in such a way that the access to disk is
    // synchronized (this). But during the access nobody is allowed to
    // perform serialization and deserialization
    // of unknown objects, so all objects should be wrapped into NbMarshalledObject
    // serialized or in reverse target NbMarshalledObject should be deserialized
    // and then not holding the lock the object obtained from it by a call to
    // marshall.get ().
    //
    // JST: Got it?


    /* Get the file attribute with the specified name.
    * @param name the file
    * @param attrName name of the attribute
    * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
    */
    public Object readAttribute(String name, String attrName) {
        Table t;
        String[] arr = new String[2];
        split (name, arr);

        synchronized (this) {
            // synchronized so only one table for each folder
            // can exist
            t = loadTable (arr[0]);
        }
        // JST:
        // had to split the code to do getAttr out of synchronized block
        // because the attribute can be serialized FileObject and
        // so the code returns back to FileSystem (that is usually synchronized)
        //
        // this leads to deadlocks between FS & DefaultAttributes implementation
        //
        // I do not know if the table should not be somehow synchronized,
        // but it seems ok.
        return t.getAttr (arr[1], attrName);
    }

    /* Set the file attribute with the specified name.
    * @param name the file
    * @param attrName name of the attribute
    * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular file systems may or may not use serialization to store attribute values.
    * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
    */
    public void writeAttribute(String name, String attrName, Object value)
    throws IOException {
        // create object that should be serialized
        NbMarshalledObject marshall = new NbMarshalledObject (value);

        String[] arr = new String[2];
        split (name, arr);

        for (;;) {
            int version;
            Table t;
            synchronized (this) {
                t = loadTable (arr[0]);
                version = t.version;
            }

            // Tests if the attribute is changing
            Object prev = t.getAttr(arr[1], attrName);
            if (prev == value || (value != null && value.equals (prev))) {
                return;
            }

            synchronized (this) {
                Table t2 = loadTable (arr[0]);
                if (t == t2 && version == t2.version) {
                    // no modification between reading of the value =>
                    // save!
                    t.setMarshalledAttr (arr[1], attrName, marshall);
                    saveTable (arr[0], t);
                    // ok, saved
                    return;
                }
            }
            // otherwise try it again
        }
    }

    /* Get all file attribute names for the file.
    * @param name the file
    * @return enumeration of keys (as strings)
    */
    public synchronized Enumeration attributes(String name) {
        String[] arr = new String[2];
        split (name, arr);

        Table t = loadTable (arr[0]);
        return t.attrs (arr[1]);
    }

    /* Called when a file is renamed, to appropriatelly update its attributes.
    * <p>
    * @param oldName old name of the file
    * @param newName new name of the file
    */
    public synchronized void renameAttributes (String oldName, String newName) {
        try {
            String[] arr = new String[2];
            split (oldName, arr);

            Table t = loadTable (arr[0]);
            Map v = (Map) t.remove (arr[1]);

            //      System.out.println ("ARg[0] = " + arr[0] + " arr[1] = " + arr[1] + " value: " + v); // NOI18N

            if (v == null) {
                // no attrs no change
                return;
            }

            split (newName, arr);

            // Remove transient attributes:
            Iterator it = v.entrySet ().iterator ();
            while (it.hasNext ()) {
                Map.Entry pair = (Map.Entry) it.next ();
                if (FileUtil.transientAttributes.contains (pair.getKey ()))
                    it.remove ();
            }

            t.put (arr[1], v);

            //      System.out.println ("xyz[0] = " + arr[0] + " xyz[1] = " + arr[1] + " value: " + v); // NOI18N
            saveTable (arr[0], t);
        } catch (IOException e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    /* Called when a file is deleted to also delete its attributes.
    *
    * @param name name of the file
    */
    public synchronized void deleteAttributes (String name) {
        try {
            String[] arr = new String[2];
            split (name, arr);

            Table t = loadTable (arr[0]);
            if (t.remove (arr[1]) != null) {
                // if there is a change
                saveTable (arr[0], t);
            }
        } catch (IOException e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    /** Getter for the cache.
    */
    private Map getCache () {
        if (cache == null) {
            cache = new HashMap (31);
        }
        return cache;
    }

    /** Splits name of a file to name of folder and to name of the file.
    * @param name of file
    * @param arr arr[0] will hold name of folder and arr[1] name of the file
    */
    private static void split (String name, String[] arr) {
        int i = name.lastIndexOf ('/');
        if (i == -1) {
            arr[0] = ""; // NOI18N
            arr[1] = name;
            return;
        }

        // folder name
        arr[0] = name.substring (0, i);
        // increase the i to be beyond the length
        if (++i == name.length ()) {
            arr[1] = ""; // NOI18N
        } else {
            // split it
            arr[1] = name.substring (i);
        }
    }

    /** Save attributes.
    * @param name name of folder to save attributes for
    * @param map map to save
    */
    private void saveTable(String name, Table map) throws IOException {
        String fullName = (name.length()==0 ? "": (name + '/')) + ATTR_NAME_EXT; // NOI18N
        if (info.folder (fullName)) {
            if (map.size () == 0) {
                // ok no need to delete
                return;
            }

            // find parent
            change.createData (fullName);

            //      System.out.println ("Done            : " + fo); // NOI18N
        } else {
            if (map.size() == 0) {
                change.delete (fullName);
                return;
            }
        }

        BufferedOutputStream fos = new BufferedOutputStream(info.outputStream (fullName));
        try {
            ObjectOutputStream oos = new ObjectOutputStream (fos);
            oos.writeObject (map);
            oos.flush ();
            //    } catch (IOException e) {
            //      throw new IOException (FileSystem.getString("EXC_Cannot_modify", fullName));
        } finally {
            fos.close ();
        }
    }

    /** Load attributes from cache or
    * from disk.
    * @param name of folder to load data from
    */
    private Table loadTable(String name) { //throws IOException {
        Reference r = (Reference)getCache ().get (name);
        if (r != null) {
            Table m = (Table)r.get ();
            if (m != null) {
                return m;
            }
        }

        // have to load new table
        Table t = load (name);
        t.attach (name, this);

        getCache ().put (name, new SoftReference (t));
        return t;
    }


    /** Loads the table. Does no initialization.
    */
    private Table load (String name) {
        String fullName = (name.length()==0 ? "": (name + '/')) + ATTR_NAME_EXT; // NOI18N

        if (info.folder (fullName)){
            return new Table ();
        }

        InputStream fis = null;
        try {
            fis = info.inputStream (fullName);
            return loadTable (fis);
        } catch (FileNotFoundException ex) {
            return new Table ();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /** Remove from cache */
    synchronized void removeTable (String name) {
        getCache ().remove (name);
    }

    //
    // FileUtil.extractJar methods
    //

    /** Does the name seems like file with extended attributes?
    * @param name the name
    * @return true if so
    */
    static boolean acceptName (String name) {
        return name.endsWith (ATTR_NAME_EXT);
    }

    /** Loads the Table of extended attributes for a input stream.
    * @param is input stream
    * @return the attributes table for this input stream
    */
    static Table loadTable (InputStream is) {
        try {
            BufferedInputStream fis = new BufferedInputStream(is);
            ObjectInputStream ois = new org.openide.util.io.NbObjectInputStream (fis);
            Object o = ois.readObject();
            if (o instanceof Table) {
                return (Table)o;
            }
        } catch (IOException e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace();
        } catch (ClassNotFoundException e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace();
        }
        // create empty table, what else
        return new Table ();
    }

    /** Table that hold mapping between files and attributes.
    * Hold mapping of type (String, Map (String, Object))
    */
    final static class Table extends HashMap implements Externalizable {
        static final long serialVersionUID = 2353458763249746934L;

        /** name of folder we belong to */
        private transient String name;
        /** attributes to belong to */
        private transient DefaultAttributes attrs;

        /** version counting */
        private transient int version = 0;

        /** Constructor */
        public Table () {
            super (11);
        }

        /** Attaches to file in attributes */
        public void attach (String name, DefaultAttributes attrs) {
            this.name = name;
            this.attrs = attrs;
        }



        /** Remove itself from the cache if finalized.
        */
        protected void finalize () {
            //      System.out.println ("Finalizing table for: " + name); // NOI18N
            attrs.removeTable (name);
        }

        /** For given file finds requested attribute.
        * @param fileName name of the file
        * @param attrName name of the attribute
        */
        public Object getAttr (String fileName, String attrName) {
            Map m = (Map)get (fileName);
            if (m != null) {
                NbMarshalledObject mo = (NbMarshalledObject)m.get (attrName);
                try {
                    return mo == null ? null : mo.get ();
                } catch (IOException ex) {
                } catch (ClassNotFoundException ex) {
                }
            }
            return null;
        }

        /** Sets an marshaled attribute to the table.
        */
        final void setMarshalledAttr (
            String fileName, String attrName, NbMarshalledObject obj
        ) {
            Map m = (Map)get (fileName);
            if (m == null) {
                m = new HashMap (7);
                put (fileName, m);
            }
            m.put (attrName, obj);

            // increments the version
            version++;
        }

        /** Enum of attributes for one file.
        */
        public Enumeration attrs (String fileName) {
            Map m = (Map)get (fileName);
            if (m == null) {
                return EmptyEnumeration.EMPTY;
            } else {
                HashSet s = new HashSet (m.keySet ());
                return Collections.enumeration (s);
            }
        }

        /** Writes external.
        */
        public void writeExternal (ObjectOutput oo) throws IOException {
            // list of names
            Iterator it = keySet ().iterator ();
            while (it.hasNext ()) {
                String file = (String)it.next ();
                Map attr = (Map)get (file);
                if (attr != null && !attr.isEmpty ()) {
                    oo.writeObject (file);

                    Iterator entries = attr.entrySet ().iterator ();
                    while (entries.hasNext ()) {
                        Map.Entry entry = (Map.Entry)entries.next ();
                        String key = (String)entry.getKey ();
                        Object value = entry.getValue ();
                        if (key != null && value != null) {
                            oo.writeObject (key);
                            oo.writeObject (value);
                        }
                    }
                    oo.writeObject (null);
                }
            }
            oo.writeObject (null);
        }

        /** Reads external.
        */
        public void readExternal (ObjectInput oi) throws IOException, ClassNotFoundException {
            for (;;) {
                String file = (String)oi.readObject ();
                if (file == null) break;

                for (;;) {
                    String attr = (String)oi.readObject ();
                    if (attr == null) break;
                    Object o = oi.readObject ();

                    // backward compatibility
                    if (o instanceof java.rmi.MarshalledObject) {
                        o = ((java.rmi.MarshalledObject)o).get ();
                        o = new NbMarshalledObject (o);
                    }
                    // end of backward compatibility

                    if (o instanceof NbMarshalledObject) {
                        NbMarshalledObject value = (NbMarshalledObject)o;
                        setMarshalledAttr (file, attr, value);
                    }
                }
            }
        }

    }
}


/*
 * Log
 *  24   src-jtulach1.23        1/14/00  Jesse Glick     Transient file 
 *       attributes.
 *  23   src-jtulach1.22        1/13/00  Ian Formanek    NOI18N
 *  22   src-jtulach1.21        1/12/00  Ian Formanek    NOI18N
 *  21   src-jtulach1.20        11/25/99 Jaroslav Tulach List.children () can 
 *       return array that contains nulls
 *  20   src-jtulach1.19        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  19   src-jtulach1.18        8/6/99   Jaroslav Tulach Better synchronization.
 *  18   src-jtulach1.17        7/25/99  Ian Formanek    Exceptions printed to 
 *       console only on "netbeans.debug.exceptions" flag
 *  17   src-jtulach1.16        7/1/99   Jaroslav Tulach 
 *  16   src-jtulach1.15        6/25/99  Jaroslav Tulach Deadlock during 
 *       compilation.
 *  15   src-jtulach1.14        6/9/99   Jaroslav Tulach Executables can be in 
 *       menu & toolbars.
 *  14   src-jtulach1.13        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  13   src-jtulach1.12        6/8/99   Jaroslav Tulach extractJar
 *  12   src-jtulach1.11        6/4/99   Jaroslav Tulach Now it is backward 
 *       compatible.
 *  11   src-jtulach1.10        6/4/99   Jaroslav Tulach Extended attributes do 
 *       not need use of RMI
 *  10   src-jtulach1.9         6/2/99   Jaroslav Tulach Safe version of 
 *       serialization of attributes. Not backward compatible.
 *  9    src-jtulach1.8         5/6/99   Jaroslav Tulach Survives when root of FS
 *       is deleted.
 *  8    src-jtulach1.7         4/28/99  Petr Hamernik   read-only attributes 
 *       file is now readed
 *  7    src-jtulach1.6         4/27/99  Michal Fadljevic 
 *  6    src-jtulach1.5         4/23/99  Jaroslav Tulach 
 *  5    src-jtulach1.4         4/13/99  Ales Novak      NbObjectInputStream used
 *       - modules work now
 *  4    src-jtulach1.3         3/26/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/26/99  Jaroslav Tulach Refresh & Bundles
 *  2    src-jtulach1.1         3/26/99  Jaroslav Tulach 
 *  1    src-jtulach1.0         3/24/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach made package private
 */
