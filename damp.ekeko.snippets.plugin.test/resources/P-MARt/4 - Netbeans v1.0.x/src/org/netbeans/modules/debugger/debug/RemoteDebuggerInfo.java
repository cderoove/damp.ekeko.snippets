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

package org.netbeans.modules.debugger.debug;

import org.openide.debugger.DebuggerInfo;
import org.netbeans.modules.debugger.delegator.SessionDebuggerInfo;

/**
* Contains information about a class to debug.
* Consists of these pieces of information:
* <UL>
* <LI>the class to run
* <LI>parameters for its main method
* <LI>a class name to stop execution in, if desired
* </UL>
*
* @author Jan Jancura
*/
public class RemoteDebuggerInfo extends DebuggerInfo implements SessionDebuggerInfo {

    private String hostName;
    private String password;

    /**
    * Construct a new <code>RemoteDebuggerInfo</code> with a host name and password.
    *
    * @param className name of debugged class
    * @param stopClassName name of class to stop in (may be <code>null</code>)
    * @param hostName name of remote computer
    * @param password password
    */
    public RemoteDebuggerInfo (
        String hostName,
        String password
    ) {
        super (null, new String [] {});
        this.hostName = hostName;
        this.password = password;
    }

    /**
    * Construct a new <code>RemoteDebuggerInfo</code> with the class to run, class to stop at, 
    * host name, and password.
    *
    * @param className name of debugged class
    * @param stopClassName name of class to stop in (may be <code>null</code>)
    * @param hostName name of remote computer
    * @param password password
    */
    public RemoteDebuggerInfo (
        String className,
        String stopClassName,
        String hostName,
        String password
    ) {
        super (className, new String [] {}, stopClassName);
        this.hostName = hostName;
        this.password = password;
    }

    /** Get hostname.
    *
    * @return repository path  or <code>null</code>
    */
    public String getHostName () {
        return hostName;
    }

    /** Get password.
    *
    * @return library path  or <code>null</code>
    */
    public String getPassword () {
        return password;
    }

    /**
    * Returns type of debugger.
    */
    public Class getDebuggerType () {
        return ToolsDebugger.class;
    }
}

/*
* Log
*  1    Jaga      1.0         2/25/00  Daniel Prusa    
* $
*/
