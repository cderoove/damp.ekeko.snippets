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

import org.openide.src.ElementFormat;

/** Property editor for ElementFormat.
* TODO: - there should be also custom editor (for visual editing the format)
*
* @author   Petr Hamernik
*/
public class ElementFormatEditor extends java.beans.PropertyEditorSupport {

    /** Constructs new property editor. */
    public ElementFormatEditor() {
    }

    /**
    * @return The property value as a human editable string.
    */
    public String getAsText() {
        return ((ElementFormat)getValue()).getPattern();
    }

    /** Set the property value by parsing a given String.
    *
    * @param text The string to be parsed.
    * @exception IllegalArgumentException if the String is badly formatted.
    */
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(new ElementFormat(text));
    }
}


/*
* Log
*  5    src-jtulach1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    src-jtulach1.3         6/30/99  Ian Formanek    Moved to package 
*       org.openide.explorer.propertysheet.editors
*  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    src-jtulach1.1         3/22/99  Petr Hamernik   
*  1    src-jtulach1.0         3/12/99  Petr Hamernik   
* $
*/
