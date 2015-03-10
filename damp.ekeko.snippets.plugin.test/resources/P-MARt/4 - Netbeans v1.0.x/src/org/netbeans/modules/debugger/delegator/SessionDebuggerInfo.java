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

package org.netbeans.modules.debugger.delegator;

import org.openide.debugger.DebuggerInfo;

/**
* This interface must be implemented by debuggerInfo, which wants
* to speciy type of debugger.
* @author Jan Jancura
*/
public interface SessionDebuggerInfo {

    /**
    * Returns type of debugger.
    */
    public Class getDebuggerType ();
}

/*
* Log
*  1    Gandalf   1.0         11/9/99  Jan Jancura     
* $
*/
