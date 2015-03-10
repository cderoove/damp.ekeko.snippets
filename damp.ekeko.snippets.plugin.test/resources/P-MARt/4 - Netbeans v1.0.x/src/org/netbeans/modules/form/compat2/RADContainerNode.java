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

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.compat2.layouts.*;

/** A Node that represents an AWT container.
*
* @author Ian Formanek, Petr Hamernik
*/
public class RADContainerNode extends RADVisualNode implements RADContainer {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = 230341275584484194L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    // -----------------------------------------------------------------------------
    // Serialized fields

    /** node array */
    public RADNodeArray nodeArray;

    /** The DesignLayout of this container */
    public DesignLayout designLayout;

    /** The Node that represents the layout */
    public RADLayoutNode radLayoutNode;

    // -----------------------------------------------------------------------------
    // Constructor

    /** For externalization only. */
    public RADContainerNode () {
    }

    public RADNode[] getSubNodes () {
        if (nodeArray == null) return new RADVisualNode [0];
        Object[] subs = nodeArray.array;
        RADNode[] nodes = new RADNode[subs.length];
        System.arraycopy (subs, 0, nodes, 0, subs.length);
        return nodes;
    }

    // ------------------------------------------------------
    // serialization

    /** Fields to restore before calling this method:
    *
    *     nodeArray [RADNodeArray]
    *     designLayout [DesignLayout]
    *     radLayoutNode [RADLayoutNode]
    *
    */
    protected void writeExternalImpl (java.io.ObjectOutput oo)
    throws java.io.IOException {
        super.writeExternalImpl (oo);

        // store the version
        oo.writeObject (nbClassVersion);

        oo.writeObject(nodeArray);
        oo.writeObject(designLayout);
        oo.writeObject(radLayoutNode);
    }

    /** Reads the object from stream.
    * @param ois input stream to read from
    * @exception IOException on error
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    protected void readExternalImpl (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        super.readExternalImpl (oi);
        org.netbeans.modules.form.FormUtils.DEBUG(">> RADContainerNode: readExternal: START"); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("?? RADContainerNode: readExternal: expecting NbVersion"); // NOI18N

        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion)) {
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
        }
        org.netbeans.modules.form.FormUtils.DEBUG("** RADContainerNode: readExternal: loaded: "+classVersion); // NOI18N


        org.netbeans.modules.form.FormUtils.DEBUG("?? RADContainerNode: readExternal: expecting RADNodeArray"); // NOI18N
        nodeArray = (RADNodeArray) oi.readObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("** RADContainerNode: readExternal: loaded: "+nodeArray); // NOI18N

        org.netbeans.modules.form.FormUtils.DEBUG("?? RADContainerNode: readExternal: expecting DesignLayout"); // NOI18N
        designLayout = (DesignLayout) oi.readObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("** RADContainerNode: readExternal: loaded: "+designLayout); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("?? RADContainerNode: readExternal: expecting RADLayoutNode"); // NOI18N
        radLayoutNode = (RADLayoutNode) oi.readObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("** RADContainerNode: readExternal: loaded: "+radLayoutNode); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("<< RADContainerNode: readExternal: END"); // NOI18N
    }


}

/*
 * Log
 *  10   Gandalf   1.9         1/12/00  Ian Formanek    NOI18N
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         5/15/99  Ian Formanek    
 *  7    Gandalf   1.6         5/12/99  Ian Formanek    
 *  6    Gandalf   1.5         5/10/99  Ian Formanek    package change plus 
 *       other minor changes
 *  5    Gandalf   1.4         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  4    Gandalf   1.3         4/29/99  Ian Formanek    
 *  3    Gandalf   1.2         3/29/99  Ian Formanek    Uses FormUtils.DEBUG to 
 *       print messages
 *  2    Gandalf   1.1         3/28/99  Ian Formanek    
 *  1    Gandalf   1.0         3/28/99  Ian Formanek    
 * $
 */
