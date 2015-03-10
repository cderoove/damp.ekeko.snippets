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

/** The RADMenuNode is a Node that represents one component placed on the Form.
*
* @author Petr Hamernik, Ian Formanek
*/
public class RADMenuItemNode extends RADNode {

    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -6333847833552116543L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    /** Type of container */
    public int type;

    /** Possible constants for type variable */
    public static final int T_MENUBAR             = 0x1110;
    public static final int T_MENUITEM            = 0x0011;
    public static final int T_CHECKBOXMENUITEM    = 0x0012;
    public static final int T_MENU                = 0x0113;
    public static final int T_POPUPMENU           = 0x1114;

    public static final int T_JPOPUPMENU          = 0x1125;
    public static final int T_JMENUBAR            = 0x1126;
    public static final int T_JMENUITEM           = 0x0027;
    public static final int T_JCHECKBOXMENUITEM   = 0x0028;
    public static final int T_JMENU               = 0x0129;
    public static final int T_JRADIOBUTTONMENUITEM= 0x002A;

    /** Masks for the T_XXX constants */
    public static final int MASK_AWT              = 0x0010;
    public static final int MASK_SWING            = 0x0020;
    public static final int MASK_CONTAINER        = 0x0100;
    public static final int MASK_ROOT             = 0x1000;

    /** For externalization only. */
    public RADMenuItemNode() {
    }

    // ------------------------------------------------------
    // serialization

    protected void writeExternalImpl (java.io.ObjectOutput oo)
    throws java.io.IOException {
        super.writeExternalImpl (oo);

        // store the version
        oo.writeObject (nbClassVersion);

        oo.writeInt(type);
    }

    /** Reads the object from stream.
    * @param ois input stream to read from
    * @exception IOException on error
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    protected void readExternalImpl (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        super.readExternalImpl (oi);
        org.netbeans.modules.form.FormUtils.DEBUG(">> RADMenuItemNode: readExternal: START"); // NOI18N

        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);

        type = oi.readInt();
        org.netbeans.modules.form.FormUtils.DEBUG("<< RADMenuItemNode: readExternal: END"); // NOI18N
    }
}

/*
 * Log
 *  9    Gandalf   1.8         1/12/00  Ian Formanek    NOI18N
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/10/99  Ian Formanek    Separators improved
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
