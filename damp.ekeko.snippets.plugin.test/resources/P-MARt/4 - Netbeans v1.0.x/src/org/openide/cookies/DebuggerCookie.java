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

package org.openide.cookies;

import org.openide.debugger.DebuggerInfo;
import org.openide.debugger.DebuggerException;
import org.openide.nodes.Node;

/** Cookie for debugger. Any data object or node that supports
* debugging can implement this cookie.
*
* @author Jan Jancura
*/
public interface DebuggerCookie extends Node.Cookie {
    /** Start debugging of associated object.
    * @param stopOnMain if <code>true</code>, debugger stops on the first line of debugged code
    * @exception DebuggerException if the session cannot be started
    */
    public void debug (boolean stopOnMain) throws DebuggerException;
}

/*
 * Log
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         3/11/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/11/99  Jan Jancura     
 *  2    Gandalf   1.1         2/26/99  Jaroslav Tulach Open API
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    moved to org.openide.cookies
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    extends Node.Cookie
 *  0    Tuborg    0.14        --/--/98 Jan Jancura     isDebugAllowed added
 */
