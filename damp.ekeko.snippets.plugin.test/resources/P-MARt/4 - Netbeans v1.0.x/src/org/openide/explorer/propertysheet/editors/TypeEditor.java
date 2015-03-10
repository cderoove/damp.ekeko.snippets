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

package org.openide.explorer.propertysheet.editors;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;

import org.openide.src.Type;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

/** Property editor for the org.netbeans.src.Type
*
* @author Petr Hamernik
*/
public class TypeEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1423443523462351952L;

    /** Current value */
    private Type type;

    /** Creates new editor */
    public TypeEditor () {
        type = null;
    }

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText () {
        return (type == null) ? "" : type.toString(); // NOI18N
    }

    /**
    * Set the property value by parsing a given String.
    * @param text  The string to be parsed.
    */
    public void setAsText (String string) throws IllegalArgumentException {
        type = Type.parse(string);
    }

    /**
    * @param v new value
    */
    public void setValue(Object v) {
        if (!(v instanceof Type))
            throw new IllegalArgumentException();
        type = (Type) v;
    }

    /**
    * @return value
    */
    public Object getValue() {
        return type;
    }

    /**
    * @return A fragment of Java code representing an initializer for the
    * current value.
    */
    public String getJavaInitializationString () {
        return (type == null) ? "" : type.toString(); // NOI18N
    }

    /**
    * @return The tag values for this property.
    */
    public String[] getTags () {
        return Type.getTypesNames();
    }

    /**
    * @return Returns custom property editor to be showen inside the property
    *         sheet.
    */
    public Component getInPlaceCustomEditor () {
        return null;
    }

    /**
    * @return true if this PropertyEditor provides a enhanced in-place custom
    *              property editor, false otherwise
    */
    public boolean hasInPlaceCustomEditor () {
        return false;
    }

    /**
    * @return true if this property editor provides tagged values and
    * a custom strings in the choice should be accepted too, false otherwise
    */
    public boolean supportsEditingTaggedValues () {
        return true;
    }
}

/*
* Log
*  6    Gandalf   1.5         1/12/00  Ian Formanek    NOI18N
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         6/30/99  Ian Formanek    Fixed last change
*  3    Gandalf   1.2         6/30/99  Ian Formanek    Moved to package 
*       org.openide.explorer.propertysheet.editors
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         4/30/99  Petr Hamernik   
* $
*/
