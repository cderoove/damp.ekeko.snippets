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

import org.netbeans.modules.form.TuborgPersistenceManager;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.FormEditor;

/** The RADVisualNode is a Node that represents one visual component placed on the Form.
*
* @author Ian Formanek
*/
public class RADVisualNode extends RADNode {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = 7921660223260849569L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    // ------------------------------------------------------
    // Private area

    /** The mapping <Layout Class -> Object> where this component's constraints
    * for different layout managers might be stored.
    * @associates Object
    */
    public java.util.HashMap constraints;

    // ------------------------------------------------------
    // Constructor

    /** For externalization only. */
    public RADVisualNode () {
    }


    // ------------------------------------------------------
    // serialization

    /** Fields to restore before calling this method:
    *
    *     constraints [HashMap]
    *
    */
    protected void writeExternalImpl (java.io.ObjectOutput oo)
    throws java.io.IOException {
        super.writeExternalImpl (oo);

        // store the version
        oo.writeObject (nbClassVersion);

        int len = constraints.size ();
        oo.writeInt (len);
        for (java.util.Iterator keys = constraints.keySet ().iterator (); keys.hasNext (); ) {
            Object next = keys.next ();
            oo.writeObject (next);
            TuborgPersistenceManager.writeSafely (oo, constraints.get (next));
        }
        //    oo.writeObject(constraints);
    }

    /** Reads the object from stream.
    * @param ois input stream to read from
    * @exception IOException on error
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    protected void readExternalImpl (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        super.readExternalImpl (oi);
        org.netbeans.modules.form.FormUtils.DEBUG(">> RADVisualNode: readExternal: START"); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("?? RADVisualNode: readExternal: expecting NbVersion"); // NOI18N

        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

        org.netbeans.modules.form.FormUtils.DEBUG("** RADVisualNode: readExternal: loaded: "+classVersion); // NOI18N

        constraints = new java.util.HashMap ();
        org.netbeans.modules.form.FormUtils.DEBUG("?? RADVisualNode: readExternal: expecting int (length of constraints array)"); // NOI18N
        int len = oi.readInt ();
        org.netbeans.modules.form.FormUtils.DEBUG("** RADVisualNode: readExternal: loaded: "+len); // NOI18N
        for (int i = 0; i < len; i++) {
            org.netbeans.modules.form.FormUtils.DEBUG("?? RADVisualNode: readExternal: [] expecting Object (layout class name)"); // NOI18N
            Object layoutClassName = oi.readObject ();
            org.netbeans.modules.form.FormUtils.DEBUG("** RADVisualNode: readExternal: loaded: "+layoutClassName); // NOI18N
            try {
                org.netbeans.modules.form.FormUtils.DEBUG("?? RADVisualNode: readExternal: [] expecting Object (constraints)"); // NOI18N
                Object constr = TuborgPersistenceManager.readSafely (oi);
                org.netbeans.modules.form.FormUtils.DEBUG("** RADVisualNode: readExternal: loaded: "+constr); // NOI18N
                constraints.put (layoutClassName, constr);
            } catch (ClassNotFoundException e) {
                FormEditor.fileWarning (java.text.MessageFormat.format (
                                            FormEditor.getFormBundle ().getString ("FMT_ERR_ConstraintsNotFound"),
                                            new Object [] {
                                                e.getMessage (),
                                                e.getClass ().getName (),
                                            }
                                        ), e);
            }
            org.netbeans.modules.form.FormUtils.DEBUG("<< RADVisualNode: readExternal: END"); // NOI18N
        }

    }

}

/*
 * Log
 *  12   Gandalf   1.11        1/12/00  Ian Formanek    NOI18N
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         5/16/99  Ian Formanek    
 *  9    Gandalf   1.8         5/15/99  Ian Formanek    
 *  8    Gandalf   1.7         5/12/99  Ian Formanek    
 *  7    Gandalf   1.6         5/11/99  Ian Formanek    Build 318 version
 *  6    Gandalf   1.5         5/10/99  Ian Formanek    package change plus 
 *       other minor changes
 *  5    Gandalf   1.4         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  4    Gandalf   1.3         4/7/99   Ian Formanek    Backward-compatible 
 *       deserialization finalized for Gandalf beta
 *  3    Gandalf   1.2         3/29/99  Ian Formanek    Uses FormUtils.DEBUG to 
 *       print messages
 *  2    Gandalf   1.1         3/28/99  Ian Formanek    
 *  1    Gandalf   1.0         3/28/99  Ian Formanek    
 * $
 */
