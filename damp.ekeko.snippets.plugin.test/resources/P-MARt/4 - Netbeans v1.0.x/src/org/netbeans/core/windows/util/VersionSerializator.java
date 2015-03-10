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

package org.netbeans.core.windows.util;

import java.util.HashMap;
import java.io.*;

import org.openide.util.io.SafeException;

/** A class that represents support for serialization
* of objects with versioning.
* Usage:<p>
* 1) Initialization:<br>  
* VersionSerializator versionSafe = new VersionSerializator();<br>
* versionSafe.putVersion("0.9", version09Data);<br>
* versionSafe.putVersion("1.0", version10Data);<p>
* 2) Writing data:<br>
* ObjectOutputStream oos = ....<br>
* versionSafe.writeData(oos, "1.0"); or versionSafe.writeData(oos); <p>
* 3) Reading data:<br>
* ObjectOutputStream ois = ....<br>
* Object data = versionSafe.readData(ois);<br>
* String version = versionSafe.lastReadVersion();
* ....
*
* @author  Dafe Simonek
*/
public class VersionSerializator extends Object {

    // Attributes

    /** mapping between versions and their serializators 
     * @associates Versionable*/
    HashMap versionMap;
    /** version that was read from stream */
    String lastReadVersion;
    /** last version that was registered via putVersion(..) */
    String lastRegisteredVersion;

    // Operations

    /** Default constructor, performs initialization. */
    public VersionSerializator () {
        this(10);
    }

    /** Performs initialization.
    * @param capacity the capacity of the underlying map holding
    * versions. */
    public VersionSerializator (int capacity) {
        // initialize
        versionMap = new HashMap(capacity);
    }

    /** An operation that puts new entry into versions table.
    * @param version to put in
    * @param data serializable object which will be asked
    * to write and read the data if appropriate verion is requested.
    */
    public void putVersion(Versionable data) {
        String version = data.getName();
        versionMap.put(version, data);
        lastRegisteredVersion = version;
    }

    /** An operation that removes given version from
    * versions table.
    * @param version Version to remove
    */
    public void removeVersion(String version) {
        versionMap.remove(version);
        if (version.equals(lastRegisteredVersion)) {
            lastRegisteredVersion = null;
        }
    }

    /** @return version Versionable which belongs to the given version string
    * or null if no such Versionable can be found.
    */
    public Versionable getVersion (String version) {
        return (Versionable)versionMap.get(version);
    }

    /** @return A verion which was read by the readData method.
    */
    public String lastReadVersion () {
        return lastReadVersion;
    }

    /** Reads the data from given input stream and returns
    * the Versionable instance that was used to read the data.
    * Read version can be obtained via lastReadVersion call.
    * @param ois stream to read from.
    * @return read data
    */
    public Versionable readVersion (ObjectInput in)
    throws IOException, ClassNotFoundException {
        String version = (String)in.readObject();
        Versionable dataManager = (Versionable)versionMap.get(version);
        if (dataManager == null) {
            throw new IOException("Unknown version"); // NOI18N
        }
        try {
            dataManager.readData(in);
        } catch (Exception exc) {
            // excapsulate thrown exception with safe exception
            if (System.getProperty("netbeans.debug.exceptions") != null) {
                exc.printStackTrace();
            }
            throw new SafeException(exc);
        }
        lastReadVersion = version;
        return dataManager;
    }

    /** Writes the data of specified version to given stream.
    * Data are written using appropriate serializable registered
    * previously using putVersion(....) method.
    * @param oos output stream to write to
    * @param param firstParamName  a description of this parameter
    */
    public void writeVersion (ObjectOutput out, String version)
    throws IOException {
        Versionable dataManager = (Versionable)versionMap.get(version);
        if (dataManager == null) {
            throw new IOException("Unknown version"); // NOI18N
        }
        out.writeObject(version);
        try {
            dataManager.writeData(out);
        } catch (Exception exc) {
            // excapsulate thrown exception with safe exception
            if (System.getProperty("netbeans.debug.exceptions") != null) {
                exc.printStackTrace();
            }
            throw new SafeException(exc);
        }
    }

    /** Writes the data of last registered version to given stream.
    */
    public void writeLastVersion (ObjectOutput oos)
    throws IOException {
        writeVersion(oos, lastRegisteredVersion);
    }

    /** Interface for versioned serialization. Classes that want to be
    * serialized through VersionSerializator will need to implement this
    * interface. (It is actually a replacement for Serializable)
    */
    public interface Versionable {

        /** Identification of the version */
        public String getName ();

        /** read the data of the version from given input */
        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException;

        /** write the data of the version to given output */
        public void writeData (ObjectOutput out)
        throws IOException;

    } // end of inner interface Versionable


} /* end class VersionSerializator */

/*
* Log
*  2    Gandalf   1.1         1/12/00  Ian Formanek    NOI18N
*  1    Gandalf   1.0         11/3/99  David Simonek   
* $ 
*/ 
