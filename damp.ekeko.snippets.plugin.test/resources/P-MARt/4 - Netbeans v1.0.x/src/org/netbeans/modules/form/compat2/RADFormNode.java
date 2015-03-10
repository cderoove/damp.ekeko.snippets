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

/** A Node that encapsulates the whole form (and its top-level container).
*
* @author Ian Formanek, Petr Hamernik
*/
final public class RADFormNode extends RADContainerNode {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = 846459318776993001L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);


    /** For externalization only. */
    public RADFormNode () {
    }

    // ------------------------------------------------------
    // Private area

    /** The Form represented by this RADFormNode */
    public DesignForm form;

    /** The context for nonvisual components */
    public NonVisualsNode nonVisualsNode;

    /** The current menu - name of the variable. */
    public String menu = null;

    // ------------------------------------------------------
    // serialization

    /** Fields to restore before calling this method:
    *
    *     form [DesignForm]
    *     nonVisualsNode [NonVisualsNode]
    *     menu [String] (null for no menu)
    *
    */
    protected void writeExternalImpl (java.io.ObjectOutput oo)
    throws java.io.IOException {
        super.writeExternalImpl (oo);

        // store the version
        oo.writeObject (nbClassVersion);

        oo.writeObject(form);
        oo.writeObject(nonVisualsNode);
        oo.writeObject(menu);
    }

    /** Reads the object from stream.
    * @param ois input stream to read from
    * @exception IOException on error
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    protected void readExternalImpl (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        super.readExternalImpl (oi);

        org.netbeans.modules.form.FormUtils.DEBUG(">> RADFormNode: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

        form = (DesignForm) oi.readObject ();
        nonVisualsNode = (NonVisualsNode) oi.readObject ();
        menu = (String) oi.readObject();
        org.netbeans.modules.form.FormUtils.DEBUG("<< RADFormNode: readExternal: END"); // NOI18N
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
