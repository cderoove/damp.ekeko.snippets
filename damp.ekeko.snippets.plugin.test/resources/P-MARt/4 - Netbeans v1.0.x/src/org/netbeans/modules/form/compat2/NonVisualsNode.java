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

/** The NonVisualsNode is a Node that holds all the nonvisual components of the Form.
*
* @author Ian Formanek
*/
public class NonVisualsNode implements java.io.Externalizable, RADContainer {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = 3159852954546091927L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    // -----------------------------------------------------------------------------
    // Private area

    public RADNodeArray nodeArray;

    public FormManager formManager;

    // FINALIZE DEBUG METHOD
    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    /** For externalization only */
    public NonVisualsNode () {
    }

    // ------------------------------------------------------
    // Serialization

    /** Fields to restore before calling this method:
    *
    *     formManager [FormManager]
    *     nodeArray [RADNodeArray]
    *
    */
    public void writeExternal (java.io.ObjectOutput oo)
    throws java.io.IOException {
        // store the version
        oo.writeObject (nbClassVersion);

        oo.writeObject(formManager);
        oo.writeObject(nodeArray);
    }

    /** Reads the object from stream.
    * @param ois input stream to read from
    * @exception IOException on error
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> NonVisualsNode: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

        formManager = (FormManager)oi.readObject ();
        Object o = oi.readObject ();
        nodeArray = (RADNodeArray) o;
        /*    Node[] children = nodeArray.toArray ();

            for (int i = 0; i < children.length; i++) 
              ((RADNode)children[i]).setParentNodeWithoutNotify (this);
            
            setDisplayName (FormEditor.getFormBundle ().getString ("CTL_NonVisualsNode")); */
        org.netbeans.modules.form.FormUtils.DEBUG("<< NonVisualsNode: readExternal: END"); // NOI18N
    }


}

/*
 * Log
 *  9    Gandalf   1.8         1/12/00  Ian Formanek    NOI18N
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         5/15/99  Ian Formanek    
 *  6    Gandalf   1.5         5/12/99  Ian Formanek    
 *  5    Gandalf   1.4         5/10/99  Ian Formanek    package change plus 
 *       other minor changes
 *  4    Gandalf   1.3         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  3    Gandalf   1.2         3/29/99  Ian Formanek    Uses FormUtils.DEBUG to 
 *       print messages
 *  2    Gandalf   1.1         3/28/99  Ian Formanek    
 *  1    Gandalf   1.0         3/28/99  Ian Formanek    
 * $
 */
