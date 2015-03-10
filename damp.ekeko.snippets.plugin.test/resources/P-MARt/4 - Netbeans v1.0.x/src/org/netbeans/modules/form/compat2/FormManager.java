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

/** The FormManager manages the design-time behaviour of one form.
*
* @author   Ian Formanek
*/
final public class FormManager extends Object
    implements java.io.Externalizable {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -3613005287076359375L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    // -----------------------------------------------------------------------------
    // Constructor

    /** For externalization only. WARNING: Do not use for creating a new instance */
    public FormManager () {
    }

    // -----------------------------------------------------------------------------
    // Serialized fields

    /** The RADNode for the Form itself */
    public RADFormNode rootNode;

    /** The DesignForm that is managed by this FormManager */
    public DesignForm designForm;

    // ------------------------------------------------------
    // Serialization

    /** Fields to restore before calling this method:
    *
    *     rootNode [RADFormNode]
    *     designForm [DesignForm]
    *
    */
    public void writeExternal (java.io.ObjectOutput oo) throws java.io.IOException {
        // store the version
        oo.writeObject (nbClassVersion);

        oo.writeObject (rootNode);
        oo.writeObject (designForm);
    }

    /** Reads this object from the specified stream */
    public void readExternal (java.io.ObjectInput oi) throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> FormManager: readExternal: START"); // NOI18N
        org.netbeans.modules.form.FormUtils.DEBUG("?? FormManager: readExternal: expecting NbVersion"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
        org.netbeans.modules.form.FormUtils.DEBUG("** FormManager: readExternal: loaded: "+classVersion); // NOI18N

        org.netbeans.modules.form.FormUtils.DEBUG("?? FormManager: readExternal: expecting RADFormNode"); // NOI18N
        Object o = oi.readObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("** FormManager: readExternal: loaded: "+o); // NOI18N
        rootNode = (RADFormNode) o;
        org.netbeans.modules.form.FormUtils.DEBUG("?? FormManager: readExternal: expecting DesignForm"); // NOI18N
        Object o2 = oi.readObject ();
        org.netbeans.modules.form.FormUtils.DEBUG("** FormManager: readExternal: loaded: "+o2); // NOI18N
        designForm = (DesignForm) o2;
        org.netbeans.modules.form.FormUtils.DEBUG("<< FormManager: readExternal: END"); // NOI18N
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
