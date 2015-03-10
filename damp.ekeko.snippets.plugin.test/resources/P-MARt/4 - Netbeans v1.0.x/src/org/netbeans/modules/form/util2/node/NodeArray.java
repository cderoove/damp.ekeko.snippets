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

package org.netbeans.modules.form.util2.node;

import java.io.*;

/** Class that simplifies work with nodes. They can be simply added
* to it and after that retrived in the array.
*
* @author Petr Hamernik, Jaroslav Tulach, Jan Jancura
*/
public class NodeArray extends Object implements java.io.Serializable {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = 949809410200662854L;

    /** flag indicating whether serialize children or not */
    public boolean serialize;

    /** Constructs NodeArray. */
    public NodeArray () {
    }

    /** writes the object into serialization stream */
    private void writeObject(ObjectOutputStream os)
    throws IOException {
        os.defaultWriteObject ();
    }

    /** reads the object from the serialization stream */
    private void readObject (ObjectInputStream is)
    throws IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> NodeArray: readExternal: START"); // NOI18N
        is.defaultReadObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("<< NodeArray: readExternal: END"); // NOI18N
    }
}

/*
 * Log
 *  3    Gandalf   1.2         1/13/00  Ian Formanek    NOI18N #2
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         5/15/99  Ian Formanek    
 * $
 */
