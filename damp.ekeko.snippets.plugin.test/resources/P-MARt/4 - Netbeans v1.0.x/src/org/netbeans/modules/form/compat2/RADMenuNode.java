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
import org.netbeans.modules.form.util2.node.NodeArray;

/** The RADMenuNode is a Node that represents one component placed on the Form.
*
* @author Petr Hamernik, Ian Formanek
*/
public class RADMenuNode extends RADMenuItemNode implements RADContainer {

    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -6333847833552116544L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    /** array of subnodes representing menuitems */
    public RADNodeArray nodeArray;

    /** For externalization only. */
    public RADMenuNode() {
    }

    public Object[] getSubNodes () {
        if (nodeArray == null) return new RADNode [0];
        return nodeArray.array;
    }

    //-------------------------------------------------------------------------------
    // Menu Separator

    /** Node which represents the separator in the tree of menu components */
    public static class MenuSeparatorNode implements java.io.Externalizable {
        /** A JDK 1.1 serial version UID */
        static final long serialVersionUID = -6333847833552116000L;

        /** Netbeans class version of separator nodes. */
        public static final NbVersion nbClassVersionSeparator = new NbVersion (1, 0);

        /** For externalization only. */
        public MenuSeparatorNode() {
        }

        /** serializes object */
        public void writeExternal(java.io.ObjectOutput oo)
        throws java.io.IOException {
            // store the version
            oo.writeObject (nbClassVersionSeparator);
        }

        /** deserializes object */
        public void readExternal(java.io.ObjectInput oi)
        throws java.io.IOException, ClassNotFoundException {
            org.netbeans.modules.form.FormUtils.DEBUG(">> RADMenuNode.MenuSeparatorNode: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersionSeparator.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersionSeparator);
            org.netbeans.modules.form.FormUtils.DEBUG("<< RADMenuNode.MenuSeparatorNode: readExternal: END"); // NOI18N
        }
    }

    // ------------------------------------------------------
    // serialization

    protected void writeExternalImpl (java.io.ObjectOutput oo)
    throws java.io.IOException {
        super.writeExternalImpl (oo);

        // store the version
        oo.writeObject (nbClassVersion);

        oo.writeObject(nodeArray);
    }

    /** Reads the object from stream.
    * @param ois input stream to read from
    * @exception IOException on error
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    protected void readExternalImpl (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        super.readExternalImpl (oi);
        org.netbeans.modules.form.FormUtils.DEBUG(">> RADMenuNode: readExternal: START"); // NOI18N

        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

        nodeArray = (RADNodeArray) oi.readObject ();

        org.netbeans.modules.form.FormUtils.DEBUG("<< RADMenuNode: readExternal: END"); // NOI18N
    }
}

/*
 * Log
 *  12   Gandalf   1.11        1/12/00  Ian Formanek    NOI18N
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         9/10/99  Ian Formanek    Separators improved
 *  9    Gandalf   1.8         7/14/99  Ian Formanek    access to nodeArray 
 *       opened
 *  8    Gandalf   1.7         7/14/99  Ian Formanek    Provided access to 
 *       subnodes
 *  7    Gandalf   1.6         5/15/99  Ian Formanek    
 *  6    Gandalf   1.5         5/12/99  Ian Formanek    
 *  5    Gandalf   1.4         5/10/99  Ian Formanek    package change plus 
 *       other minor changes
 *  4    Gandalf   1.3         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  3    Gandalf   1.2         4/7/99   Ian Formanek    MenuSeparatorNode 
 *       persistence
 *  2    Gandalf   1.1         3/29/99  Ian Formanek    Uses FormUtils.DEBUG to 
 *       print messages
 *  1    Gandalf   1.0         3/28/99  Ian Formanek    
 * $
 */
