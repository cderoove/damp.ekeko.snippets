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
import org.netbeans.modules.form.FormDataObject;

/** The DesignForm is an abstract superclass of all different
* Form types supported by the FormEditor.
*
* @author  Ian Formanek
*/
public abstract class DesignForm extends Object implements java.io.Externalizable {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = 6521541306176410181L; // fixed number to provide backward compatibility

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    // --------------------------------------------------------------------------------------
    // private area

    /** The manager that manages a design-time behaviour of this form */
    public FormManager formManager;

    // -----------------------------------------------------------------------------
    // FINALIZE DEBUG METHOD

    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    }

    // --------------------------------------------------------------------------------------
    // Serialization

    /** Fields to restore before calling this method:
    *
    *     formManager [FormManager]
    *
    */
    public void writeExternal (java.io.ObjectOutput oo) throws java.io.IOException {
        // store the version
        oo.writeObject (nbClassVersion);

        oo.writeObject (formManager);
    }

    /** Reads this object from the specified stream */
    public void readExternal (java.io.ObjectInput oi) throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> Design Form: readExternal: START"); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("?? Design Form: readExternal: expecting NbVersion"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
        org.netbeans.modules.form.FormUtils.DEBUG("** Design Form: readExternal: loaded: "+classVersion); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("?? Design Form: readExternal: expecting FormManager"); // NOI18N
        formManager = (FormManager) oi.readObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("** Design Form: readExternal: loaded: "+formManager); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("<< Design Form: readExternal: END"); // NOI18N
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
