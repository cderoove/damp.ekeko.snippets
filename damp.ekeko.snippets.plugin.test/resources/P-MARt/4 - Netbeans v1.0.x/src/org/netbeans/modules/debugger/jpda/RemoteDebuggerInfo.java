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

package org.netbeans.modules.debugger.jpda;

import org.openide.debugger.DebuggerInfo;
import java.util.Map;
import com.sun.jdi.connect.AttachingConnector;

import org.netbeans.modules.debugger.delegator.SessionDebuggerInfo;


/**
* Contains information for connectiong to JPDA debugger session.
*
* @author Jan Jancura
*/
public class RemoteDebuggerInfo extends DebuggerInfo
    implements SessionDebuggerInfo {

    private Map args;
    private AttachingConnector connector;

    /**
    * Construct a new <code>RemoteDebuggerInfo</code> with a host name and password.
    *
    * @param className name of debugged class
    * @param stopClassName name of class to stop in (may be <code>null</code>)
    * @param hostName name of remote computer
    * @param password password
    */
    public RemoteDebuggerInfo (
        AttachingConnector connector,
        Map args
    ) {
        super (null, new String [] {});
        this.args = args;
        this.connector = connector;
    }

    /**
    * Get attaching connector.
    *
    * @return attaching connector
    */
    public AttachingConnector getConnector () {
        return connector;
    }

    /**
    * Get arguments for connector.
    *
    * @return map of arguments for connector
    */
    public Map getArgs () {
        return args;
    }

    /**
    * Returns type of debugger.
    */
    public Class getDebuggerType () {
        return JPDADebugger.class;
    }
}

/*
* Log
*  1    Jaga      1.0         2/25/00  Daniel Prusa    
* $
*/
