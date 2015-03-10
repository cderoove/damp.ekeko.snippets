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
* Enhances standard custom property editor with the possibility to return the customized value.
* I.e. the custom property editor does not need to fire property changes upon
* modifications; the {@link PropertyDialogManager} instead sets the acquired value after the custom editor is closed.
*
* @author  Ian Formanek
*/
public interface EnhancedCustomPropertyEditor {

    /** Get the customized property value.
    * @return the property value
    * @exception InvalidStateException when the custom property editor does not contain a valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue () throws IllegalStateException;
}


/*
 * Log
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/30/99  Ian Formanek    Moved to package 
 *       org.openide.explorer.propertysheet.editors
 *  5    Gandalf   1.4         6/30/99  Ian Formanek    Property update.
 *  4    Gandalf   1.3         6/30/99  Ian Formanek    Property update.
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/20/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
