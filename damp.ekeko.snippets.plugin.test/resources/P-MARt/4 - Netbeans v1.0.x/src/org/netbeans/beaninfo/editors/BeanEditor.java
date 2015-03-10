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

package org.netbeans.beaninfo.editors;

import java.beans.*;

import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

/** Property editor for beans and BeanContexts.
*
* @author Jan Jancura
* @version 0.11 Jan 08, 1998
*/
public class BeanEditor extends  PropertyEditorSupport {

    /**
    * Default constructor.
    */
    public BeanEditor () {
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public String getAsText () {
        return null;
    }

    public java.awt.Component getCustomEditor() {
        Node value = (Node)getValue ();
        if (value == null) return new java.awt.Label ("NULL"); // NOI18N
        PropertySheet propertySheet = new PropertySheet ();
        Node[] node = new Node [1];
        node [0] = value;
        propertySheet.setNodes (node);
        HelpCtx help = value.getHelpCtx ();
        if (help != null && ! help.equals (HelpCtx.DEFAULT_HELP) && help.getHelpID () != null)
            HelpCtx.setHelpIDString (propertySheet, help.getHelpID ());
        return propertySheet;
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Petr Jiricka    i18n
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/8/99   Jesse Glick     Context help.
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    small changes to reflect that propertySheet is no more a GUI component
 */
