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

package org.openide.explorer.propertysheet;

import java.awt.event.ActionEvent;

/**
* SheetButtonListener interface.
*
* @author Jan Jancura
* @version 0.11, Nov 14, 1997
*/
interface SheetButtonListener {

    /**
    * Invoked when the mouse enters a component.
    */
    public void sheetButtonEntered (ActionEvent e);

    /**
    * Invoked when the mouse exits a component.
    */
    public void sheetButtonExited (ActionEvent e);

    /**
    * Invoked when the mouse has been clicked on a component.
    */
    public void sheetButtonClicked (ActionEvent e);
}


/*
 * Log
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach Changed not to be public
 */
