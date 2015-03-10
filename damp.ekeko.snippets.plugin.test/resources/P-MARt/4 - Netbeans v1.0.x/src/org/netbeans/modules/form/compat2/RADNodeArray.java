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

import org.netbeans.modules.form.TuborgPersistenceManager;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.FormEditor;

/** An array of nodes for RAD containers.
*
* @author Ian Formanek
*/
public class RADNodeArray extends NodeArray implements java.io.Externalizable {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -1248723445781519939L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    // FINALIZE DEBUG METHOD
    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    // -----------------------------------------------------------------------------
    // Private area

    public RADContainer containerNode;

    transient public Object[] array;

    // -----------------------------------------------------------------------------
    // Constructor

    /** For externalization only */
    public RADNodeArray () {
    }

    // -----------------------------------------------------------------------------
    // Serialization

    /** Fields to restore before calling this method:
    *
    *     array [Object[] ]
    *     containerNode [RADContainer]
    *
    */
    final public void writeExternal (java.io.ObjectOutput oo)
    throws java.io.IOException {
        // store the version
        oo.writeObject (nbClassVersion);

        oo.writeObject (containerNode);
        oo.writeInt (array.length);
        for (int i = 0; i < array.length; i++)
            TuborgPersistenceManager.writeSafely (oo, array[i]);
    }

    /** Reads the object from stream.
    * @param oi input stream to read from
    * @exception IOException Includes any I/O exceptions that may occur
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    final public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> RADNodeArray: readExternal: START"); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("?? RADNodeArray: readExternal: expecting NbVersion"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
        org.netbeans.modules.form.FormUtils.DEBUG("** RADNodeArray: readExternal: loaded: "+classVersion); // NOI18N

        org.netbeans.modules.form.FormUtils.DEBUG("?? RADNodeArray: readExternal: expecting RADContainer"); // NOI18N
        Object o = oi.readObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("** RADNodeArray: readExternal: loaded: "+o); // NOI18N
        containerNode = (RADContainer)o;
        org.netbeans.modules.form.FormUtils.DEBUG("?? RADNodeArray: readExternal: expecting int (length of subnodes)"); // NOI18N
        int len = oi.readInt ();
        org.netbeans.modules.form.FormUtils.DEBUG("** RADNodeArray: readExternal: loaded: "+len); // NOI18N
        java.util.Vector nodes = new java.util.Vector ();
        for (int i = 0; i < len; i++) {
            try {
                org.netbeans.modules.form.FormUtils.DEBUG("?? RADNodeArray: readExternal: [] expecting RADNode"); // NOI18N
                Object obj = TuborgPersistenceManager.readSafely (oi);
                org.netbeans.modules.form.FormUtils.DEBUG("** RADNodeArray: readExternal: loaded: "+obj); // NOI18N
                if (obj instanceof RADNode) {
                    RADNode n = (RADNode)obj;
                    if (!n.invalidClass) {
                        nodes.addElement (n);
                    }
                } else if (obj instanceof RADMenuNode.MenuSeparatorNode) {
                    nodes.addElement (obj);
                }
            } catch (ClassNotFoundException e) {
                org.netbeans.modules.form.FormUtils.DEBUG("XX RADNodeArray: readExternal: [] exception:" + e.getMessage ()); // NOI18N
                // file the error into log
                FormEditor.fileError (java.text.MessageFormat.format (
                                          FormEditor.getFormBundle ().getString ("FMT_ERR_ClassNotFound"),
                                          new Object [] {
                                              e.getMessage (),
                                              e.getClass ().getName (),
                                          }
                                      ), e);
            } catch (java.io.InvalidClassException e) {
                org.netbeans.modules.form.FormUtils.DEBUG("XX RADNodeArray: readExternal: [] exception #2:" + e.getMessage ()); // NOI18N
                // file the error into log
                FormEditor.fileError (java.text.MessageFormat.format (
                                          FormEditor.getFormBundle ().getString ("FMT_ERR_ClassNotFound"),
                                          new Object [] {
                                              e.getMessage (),
                                              e.getClass ().getName (),
                                          }
                                      ), e);
            }
        }

        try {
            array = new Object [nodes.size ()];
            nodes.copyInto (array);
        } catch (Throwable t) {
            t.printStackTrace ();
        }
        org.netbeans.modules.form.FormUtils.DEBUG("<< RADNodeArray: readExternal: END"); // NOI18N
    }

}

/*
 * Log
 *  15   Gandalf   1.14        1/12/00  Ian Formanek    NOI18N
 *  14   Gandalf   1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        9/10/99  Ian Formanek    Separators improved
 *  12   Gandalf   1.11        5/17/99  Ian Formanek    Fixed problem with 
 *       loading forms with Menu Separators
 *  11   Gandalf   1.10        5/16/99  Ian Formanek    Persistence 
 *       failure-proofness improved
 *  10   Gandalf   1.9         5/16/99  Ian Formanek    
 *  9    Gandalf   1.8         5/15/99  Ian Formanek    
 *  8    Gandalf   1.7         5/12/99  Ian Formanek    
 *  7    Gandalf   1.6         5/10/99  Ian Formanek    package change plus 
 *       other minor changes
 *  6    Gandalf   1.5         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  5    Gandalf   1.4         4/29/99  Ian Formanek    
 *  4    Gandalf   1.3         4/7/99   Ian Formanek    Backward-compatible 
 *       deserialization finalized for Gandalf beta
 *  3    Gandalf   1.2         3/29/99  Ian Formanek    Uses FormUtils.DEBUG to 
 *       print messages
 *  2    Gandalf   1.1         3/28/99  Ian Formanek    
 *  1    Gandalf   1.0         3/28/99  Ian Formanek    
 * $
 */
