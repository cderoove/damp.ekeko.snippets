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

/**
 *
 * @author  jjancura
 * @version 
 */
public interface SessionsListener {

    /**
    * New session created.
    */
    public void sessionCreated (Session s);

    /**
    * Session death.
    */
    public void sessionDeath (Session s);
}
/*
 * Log
 *  1    Jaga      1.0         2/25/00  Daniel Prusa    
 * $
 */
