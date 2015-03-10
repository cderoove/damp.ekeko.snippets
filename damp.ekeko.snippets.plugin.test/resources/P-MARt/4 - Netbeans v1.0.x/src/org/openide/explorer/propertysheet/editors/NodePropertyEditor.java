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

import java.beans.PropertyEditor;
import org.openide.nodes.Node;

/** Special interface for property editors to allow
* connection between the editor and node which property
* is displayed by this editor.
*
* @author Jaroslav Tulach
*/
public interface NodePropertyEditor extends PropertyEditor {
    /** Informs the editor that the property that it
    * is displaying belongs to following nodes.
    *
    * @param nodes array of nodes having the property
    */
    public void attach (Node[] nodes);
}

/*
* Log
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/30/99  Ian Formanek    Moved to package 
*       org.openide.explorer.propertysheet.editors
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         6/3/99   Jaroslav Tulach 
* $
*/