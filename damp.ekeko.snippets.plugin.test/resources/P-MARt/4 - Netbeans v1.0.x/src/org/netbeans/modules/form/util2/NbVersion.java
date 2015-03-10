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

package org.netbeans.modules.form.util2;

/** A class that encapsulates a version information.
*
* @author Ian Formanek
* @version 0.10, Jul 12, 1998
* @deprecated Please use Java Versioning Specification.
*/
public class NbVersion extends Object implements java.io.Externalizable {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = 6160753574670641859L;

    public static final int VERSION_SAME = 0;
    public static final int VERSION_NEWER_COMPATIBLE = 1;
    public static final int VERSION_OLDER_COMPATIBLE = -1;
    public static final int VERSION_NEWER = 2;
    public static final int VERSION_OLDER = -2;

    /** Creates a new Version. */
    public NbVersion () {
    }

    /** Creates a new NbVersion.
    * @param major the major version number
    * @param minor the minor version number
    */
    public NbVersion (int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    /** Compares this version to the specified version.
    * Returns one of: <UL>
    * <LI>VERSION_SAME if the versions are the same
    * <LI>VERSION_NEWER_COMPATIBLE if this version is newer than the given one, but compatible
    * <LI>VERSION_OLDER_COMPATIBLE if this version is older than the given one, but compatible
    * <LI>VERSION_NEWER if this version is newer than the given one, but compatible
    * <LI>VERSION_OLDER if this version is older than the given one, but compatible
    * </UL>
    * @param v the version to compare to
    * @return one of version comparison constants defined in this class
    */   
    public int compareTo (NbVersion v) {
        if (major > v.major) return VERSION_NEWER;
        else if (major < v.major) return VERSION_OLDER;
        else if (minor > v.minor) return VERSION_NEWER_COMPATIBLE;
        else if (minor < v.minor) return VERSION_OLDER_COMPATIBLE;
        else return VERSION_SAME;
    }

    public boolean isCompatible (NbVersion v) {
        return v.major == major;
    }

    public boolean equals (Object o) {
        return ((o instanceof NbVersion) &&
                (((NbVersion)o).major == major) &&
                (((NbVersion)o).minor == minor));
    }

    public String toString () {
        return "NbVersion: "+major+"."+minor; // NOI18N
    }

    // -----------------------------------------------------------------------------
    // Serialization

    /** Writes the object to the stream.
    * @param oo output stream to write to
    * @exception IOException Includes any I/O exceptions that may occur
    */
    public void writeExternal (java.io.ObjectOutput oo)
    throws java.io.IOException {
        oo.writeInt (major);
        oo.writeInt (minor);
    }

    /** Reads the object from stream.
    * @param oi input stream to read from
    * @exception IOException Includes any I/O exceptions that may occur
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        major = oi.readInt ();
        minor = oi.readInt ();
    }

    // -----------------------------------------------------------------------------
    // Private Area

    /** The major version number - breaks backward compatibility */
    private int major = 1;
    /** The minor version number - keeps backward compatibility */
    private int minor = 0;
}

/*
 * Log
 *  3    Gandalf   1.2         1/13/00  Ian Formanek    NOI18N #2
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         5/15/99  Ian Formanek    
 * $
 */
