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

package org.netbeans.modules.debugger.jpda.util;

import java.util.LinkedList;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import org.netbeans.modules.debugger.jpda.JPDADebugger;


/**
*
* @author   Jan Jancura
*/
public class Requestor {

    private LinkedList                    requests = new LinkedList ();
    private EventRequestManager           requestManager;


    // init ......................................................................

    public Requestor (EventRequestManager requestManager) {
        this.requestManager = requestManager;
    }


    // main methods ..............................................................

    /**
    * @return number of requests
    */
    public int size () {
        return requests.size ();
    }

    /**
    * Adds new request.
    */
    public void add (EventRequest r) {
        requests.add (r);
    }

    /**
    * Removes all requests.
    */
    public void removeRequests () {  //S ystem.out.println ("LineBreakpoint.removeRequests " + this); // NOI18N
        if (requests.size () == 0) return;
        int i, k = requests.size ();
        try {
            for (i = 0; i < k; i++)
                requestManager.deleteEventRequest ((EventRequest) requests.get (i));
        } catch (VMDisconnectedException e) {
        }
        requests = new LinkedList ();
    }
}

/*
* Log
*  1    Jaga      1.0         2/25/00  Daniel Prusa    
* $
*/
