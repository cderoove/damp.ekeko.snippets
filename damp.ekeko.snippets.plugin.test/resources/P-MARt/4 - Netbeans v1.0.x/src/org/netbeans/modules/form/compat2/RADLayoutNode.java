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

package org.netbeans.modules.form.compat2;

import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;

import org.netbeans.modules.form.compat2.layouts.*;

/** A Node that represents a design layout of a container.
*
* @author Ian Formanek
*/
public final class RADLayoutNode implements java.io.Externalizable {
    /** JDK 1.1 serialVersionUID */
    static final long serialVersionUID = -8514927130374508287L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    // -----------------------------------------------------------------------------
    // Private area

    /** The DesignLayout instance represented by this RADNode */
    public DesignLayout designLayout;

    /** The display name of the layout node */
    public String layoutName = ""; // NOI18N

    // -----------------------------------------------------------------------------
    // Constructor

    /** For externalization only */
    public RADLayoutNode () {
    }

    // -----------------------------------------------------------------------------
    // Serialization

    /** Fields to restore before calling this method:
    *
    *     designLayout [DesignLayout]
    *     layoutName [String] (can be "" ???)
    *
    */
    public final void writeExternal (java.io.ObjectOutput oo)
    throws java.io.IOException {
        // store the version
        oo.writeObject (nbClassVersion);

        oo.writeObject (designLayout);
        oo.writeObject (layoutName);
    }

    /** Reads the object from stream.
    * @param oi input stream to read from
    * @exception IOException Includes any I/O exceptions that may occur
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    public final void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> RADLayoutNode: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion)) {
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
        }

        designLayout = (DesignLayout) oi.readObject ();
        layoutName = (String) oi.readObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("<< RADLayoutNode: readExternal: END"); // NOI18N
    }

}

/*
 * Log
 *  8    Gandalf   1.7         1/12/00  Ian Formanek    NOI18N
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         5/15/99  Ian Formanek    
 *  5    Gandalf   1.4         5/12/99  Ian Formanek    
 *  4    Gandalf   1.3         5/10/99  Ian Formanek    package change plus 
 *       other minor changes
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  2    Gandalf   1.1         3/29/99  Ian Formanek    Uses FormUtils.DEBUG to 
 *       print messages
 *  1    Gandalf   1.0         3/28/99  Ian Formanek    
 * $
 */
