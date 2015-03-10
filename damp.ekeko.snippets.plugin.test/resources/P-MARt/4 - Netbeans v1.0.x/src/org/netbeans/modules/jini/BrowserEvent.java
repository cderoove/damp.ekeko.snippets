/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jini;

import net.jini.core.discovery.*;
import net.jini.core.lookup.*;

import net.jini.discovery.*;
import net.jini.lookup.*;

/**
 * One event that was handled by browser (a dynamic view of djinn).
 * Service discovery, LUS discovery, ...
 *
 * @author  Petr Kuzel
 * @version 
 */
public class BrowserEvent {

    String detail;
    String name;
    long time;

    /** Creates new BrowserEvent */
    public BrowserEvent(String detail) {
        time = System.currentTimeMillis();
        this.detail = detail;
    }

    private BrowserEvent() {
        time = System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }

    public String getDetail() {
        return detail;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return detail + " at " + time;
    }

    public static class DiscoveredLUS extends BrowserEvent {
        public DiscoveredLUS(LookupLocator loc) {
            name = "New LUS";
            detail = "New LUS " + loc.getHost() + ":" + loc.getPort();
        }
    }

    public static class DiscardedLUS extends BrowserEvent {
        public DiscardedLUS(LookupLocator loc) {
            name = "Old LUS";
            detail = "Old LUS " + loc.getHost() + ":" + loc.getPort();
        }
    }

    public static class DiscoveredService extends BrowserEvent {
        public DiscoveredService(ServiceItem item) {
            name = "New service";
            detail = "New " + item.service;
        }
    }

    public static class DiscardedService extends BrowserEvent {
        public DiscardedService(ServiceItem item) {
            name = "Old service";
            detail = "Old " + item.service;
        }
    }
}


/*
* <<Log>>
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

