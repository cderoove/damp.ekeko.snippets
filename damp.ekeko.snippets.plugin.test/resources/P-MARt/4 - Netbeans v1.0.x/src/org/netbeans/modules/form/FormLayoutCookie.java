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

package org.netbeans.modules.form;

import org.openide.nodes.Node;

/** Cookie for Form Editor Layouts.
*
* @author Ian Formanek
* @version 1.00, Jul 19, 1998
*/
public interface FormLayoutCookie extends Node.Cookie {

    /** Provides access to layout node */
    public RADLayoutNode getLayoutNode ();

}

/*
 * Log
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/20/99  Ian Formanek    
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  1    Gandalf   1.0         3/17/99  Ian Formanek    
 * $
 */
