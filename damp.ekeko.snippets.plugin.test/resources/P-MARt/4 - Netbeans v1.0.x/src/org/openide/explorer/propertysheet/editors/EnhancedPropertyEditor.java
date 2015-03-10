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

import java.awt.Component;

/**
* Enhances standard property editor to support in-place custom editors and tagged values.
*
* @author Jan Jancura, Ian Formanek
*/
public interface EnhancedPropertyEditor extends java.beans.PropertyEditor {

    /** Get an in-place editor.
    * @return a custom property editor to be shown inside the property
    *         sheet
    */
    public Component getInPlaceCustomEditor ();

    /** Test for support of in-place custom editors.
    * @return <code>true</code> if supported
    */
    public boolean hasInPlaceCustomEditor ();

    /** Test for support of editing of tagged values.
    * Must also accept custom strings, otherwise you may may specify a standard property editor accepting only tagged values.
    * @return <code>true</code> if supported
    */
    public boolean supportsEditingTaggedValues ();

}


/*
 * Log
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         6/30/99  Ian Formanek    Moved to package 
 *       org.openide.explorer.propertysheet.editors
 *  4    Gandalf   1.3         6/30/99  Ian Formanek    Property update.
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/20/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
