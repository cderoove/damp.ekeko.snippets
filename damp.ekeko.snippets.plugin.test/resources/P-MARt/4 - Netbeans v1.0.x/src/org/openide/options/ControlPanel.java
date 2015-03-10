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

package org.openide.options;

import java.io.*;
import java.util.Vector;
import java.util.Hashtable;

import org.openide.TopManager;
import org.openide.util.io.NbObjectInputStream;
import org.openide.util.io.NbObjectOutputStream;
import org.openide.util.io.SafeException;


/** Singleton pool with a list of options for the entire IDE.
* Provides "safe" serialization of options
* (i.e. failures are per-property, not per-option nor per-pool).
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public class ControlPanel extends Object implements java.io.Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6242888199364119241L;
    /** vector with options SystemOption 
     * @associates SystemOption*/
    private Vector options = new Vector ();
    /** hashtable that maps classname of option to the option itself (String, SystemOption) 
     * @associates SystemOption*/
    private Hashtable map = new Hashtable ();

    /** Default constructor, not to be called by user code.
    * @see TopManager#getControlPanel
    */
    public ControlPanel() {}

    /** Add a new option to the pool.
    * @param so the option to add
    * @return <code>true</code> if the object was added; <code>false</code> if it was already there
    */
    public synchronized boolean add (SystemOption so) {
        String name = so.getClass ().getName ();
        if (!map.containsKey (name)) {
            options.addElement (so);
            map.put (name, so);
            return true;
        }
        return false;
    }

    /** Remove an option from the pool.
    * @param so the option to remove
    * @return <code>true</code> if the option was removed; <code>false</code> if it was not there anyway
    */
    public synchronized boolean remove (SystemOption so) {
        if (map.remove (so.getClass ().getName ()) != null) {
            options.removeElement (so);
            return true;
        } else {
            return false;
        }

    }

    /** Get all options in the pool.
    * @return the options
    */
    public SystemOption[] getSystemOptions() {
        synchronized (options) {
            int size = options.size();
            SystemOption[] ret = new SystemOption[size];
            options.copyInto (ret);
            return ret;
        }
    }

    /* Stores content of the pool. Uses safe serialization for each options.
    */
    public void writeExternal (ObjectOutput oo) throws IOException {
        SystemOption[] arr = getSystemOptions ();
        oo.writeInt (arr.length);
        for (int i = 0; i < arr.length; i++) {
            oo.writeUTF (arr.getClass ().getName ());
            try {
                NbObjectOutputStream.writeSafely (oo, arr[i]);
            } catch (SafeException ex) {
                // print stack if debug exceptions enabled
                if (System.getProperty("netbeans.debug.exceptions") != null) {
                    ex.getException().printStackTrace();
                }
            }
        }
    }

    /* Reads content of the pool.
    */
    public void readExternal (ObjectInput oi) throws IOException, ClassNotFoundException {
        int len = oi.readInt ();
        while (len-- > 0) {
            String name = oi.readUTF ();
            try {
                add ((SystemOption)NbObjectInputStream.readSafely (oi));
            } catch (SafeException ex) {
                // print stack if debug exceptions enabled
                if (System.getProperty("netbeans.debug.exceptions") != null) {
                    ex.getException().printStackTrace();
                }
            }
        }
    }

    /* Creates replaces for this panel that stores and read data to the
    * default control panel
    */
    public final Object writeReplace () {
        return new Replace ();
    }

    /** Replace class */
    private static final class Replace extends Object
        implements java.io.Serializable {
        /** SUID */
        static final long serialVersionUID = -1956267184272888393L;

        private void writeObject (ObjectOutputStream oos) throws IOException {
            TopManager.getDefault ().getControlPanel ().writeExternal (oos);
        }

        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            TopManager.getDefault ().getControlPanel ().readExternal (ois);
        }

        /** @return the default pool */
        public Object readResolve () {
            return TopManager.getDefault ().getControlPanel ();
        }
    }
}

/*
* Log
*  7    Gandalf   1.6         1/15/00  Jaroslav Tulach SUID
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         9/8/99   David Simonek   exceptions printing on 
*       when netbeans.debug.exceptions enabled
*  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         3/22/99  Jesse Glick     [JavaDoc]
*  2    Gandalf   1.1         1/25/99  Jaroslav Tulach Saves filesystempool & 
*       control panel in the default project
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
