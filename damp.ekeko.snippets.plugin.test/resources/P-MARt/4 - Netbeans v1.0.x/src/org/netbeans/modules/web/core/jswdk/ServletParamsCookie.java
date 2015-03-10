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

package org.netbeans.modules.web.core.jswdk;

import org.openide.nodes.Node;

/** Provides ability to edit serlet execution parameters, such as name, mapping,
* initialization parameters, request parameters or request method (in the future).<br>
* Empty marker interface, the real functionality is somewhere else.<br>
* Can be implemented by a DataObject, Executor or a DebuggerType.
* @author  Petr Jiricka
* @version 1.00, Jun 03, 1999
*/
public interface ServletParamsCookie extends Node.Cookie {

}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/7/99  Petr Jiricka    
 * $
 */
