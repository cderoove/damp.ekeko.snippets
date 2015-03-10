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

import net.jini.core.lookup.*;

/**
 * Extends ServiceItem by equals() mathod.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class ServiceItemExt extends ServiceItem {

    /** Creates new ServiceItemExt */
    public ServiceItemExt(ServiceItem item) {
        super(item.serviceID, item.service, item.attributeSets);
    }

    public boolean equals(Object obj) {

        //    System.err.print("? " + obj + "=" + this);
        if (obj instanceof ServiceItem) {
            if ( ((ServiceItem)obj).serviceID.equals(serviceID) ) {
                //        System.err.println(" true");
                return true;
            }
        }

        //    System.err.println(" false");
        //    Thread.dumpStack();
        return false;
    }

    public String toString() {
        return "ServiceItemExt[" + service.getClass() + ", " + serviceID + "]";
    }

}


/*
* <<Log>>
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

