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

import java.io.*;

/** This utility class serves as common serialization replacer
* which can be used in writeReplace method.<br>
* To use DefaultReplacer properly, do following:
* 1) Define persistent access to your VersionSerializator
*    by implementing DefaultReplacer.Access interface
* 2) Fill your VersionSerializator with implementations of ResVersionable,
*    instead of Versionable.
* 3) Implement writeReplace() method to return newly created instance
*    of DefaultReplacer accompanioed with implementation of Access.
*
* @author Dafe Simonek
*/
public class DefaultReplacer implements Serializable {

    /** persistent access to the version serializator instance */
    private Access vsAccess;
    /** reference to the versionable which supports replacing. */
    private ResVersionable rv;

    static final long serialVersionUID =8676891589987251425L;
    /** Creates new DefaultReplacer with given persistent access to the
    * version serializator */
    public DefaultReplacer (Access vsAccess) {
        this.vsAccess = vsAccess;
    }

    /** Reads access to the version serializator and reads the
    * data using obtained version serializator */
    private void readObject (ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        vsAccess = (Access)ois.readObject();
        rv = (ResVersionable)vsAccess.getVersionSerializator().readVersion(ois);
    }

    /** Writes access to the version serializator and then
    * actual data using version serializator */
    private void writeObject (ObjectOutputStream oos)
    throws IOException {
        oos.writeObject(vsAccess);
        vsAccess.getVersionSerializator().writeLastVersion(oos);
    }

    /** Resolves this instance to the instance returned from
    * ResVersionable.resolveData() */
    private Object readResolve ()
    throws java.io.ObjectStreamException {
        return rv.resolveData();
    }

    /** Interface for specialized versioned serialization using
    * DefaultReplacer class for replacing mechanism.
    */
    public interface ResVersionable extends VersionSerializator.Versionable {

        /** Resolves read data to some object.
        * @return instance of object to which the data should be resolved */
        public Object resolveData ()
        throws ObjectStreamException;

    } // end of inner interface ResVersionable

    /** Specialized interface which helps DefaultReplacer to
    * keep the access to VersionSerializator after deserialization.<p>
    *
    * Implementors will usually implement this interface as private 
    * static inner class of the object whose state needs to be persistent
    * and method getVersionSerializator() will just return some static
    * field holding VersionSerializator instance filled with map of available
    * versions and their persistence managers (Versionables).
    */
    public interface Access extends Serializable {

        /** @return instance of VersionSerializator which manages
        * versioned persistence for the object we want to be replaceable
        * with using DefaultReplacer utility class */
        public VersionSerializator getVersionSerializator ();

    } // end of Access inner interface

}

/*
* Log
*  2    Gandalf   1.1         11/26/99 Patrik Knakal   
*  1    Gandalf   1.0         11/3/99  David Simonek   
* $ 
*/ 
