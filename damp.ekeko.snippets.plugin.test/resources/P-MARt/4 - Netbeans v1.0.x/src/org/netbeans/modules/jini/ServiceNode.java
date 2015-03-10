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

import net.jini.admin.*;
import net.jini.core.lookup.*;
import net.jini.core.entry.*;
import net.jini.lookup.entry.*;

import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.src.*;
import org.openide.src.nodes.*;

/**
 * Node representing one instance of discovered service. The instance subnodes
 * includes implemented interfaces and attached attributes. The service can be
 * optinally administrable.
 *
 * @author  Petr Kuzel, Martin Ryzl
 * @version
 */
public class ServiceNode extends AbstractNode {

    private ServiceItemExt service;

    /** Creates new serviceNode */
    public ServiceNode(ServiceItemExt key) {
        super(new ServiceChildren(key));
        service = key;

        getCookieSet().add(
            new ServiceTemplateCookie.Default(key, ServiceTemplateCookie.SERVICE_ITEM)
        );


        if (key.service instanceof net.jini.admin.Administrable) {
            getCookieSet().add( new AdminCookie((net.jini.admin.Administrable) key.service) );
            //      System.err.println("Administrable: " + key.serviceID);
        }

        systemActions = new SystemAction[] {
                            SystemAction.get(AdminAction.class)
                        };

        //<--

        if (key.service != null) {
            setIconBase(Util.getString("SERVICE_ITEM_ICON_BASE"));
            setName(key.service.getClass().getName());
        }
        else {
            setIconBase(Util.getString("INVALID_SERVICE_ITEM_ICON_BASE"));
            setName(Util.getString("PROP_UnknownService"));
        }

        // properties ServiceID, Service class,
        try {
            Sheet sheet = getSheet();
            Sheet.Set prop;
            if ((prop = sheet.get(Sheet.PROPERTIES)) == null) {
                prop = Sheet.createExpertSet();
                sheet.put(prop);
            }
            Node.Property p = new PropertySupport.Reflection(this, String.class, "getServiceID", null);
            p.setName("ServiceID");
            p.setDisplayName(Util.getString("PROP_ServiceID"));
            p.setShortDescription(Util.getString("HINT_ServiceID"));
            prop.put(p);
        } catch (NoSuchMethodException ex) {
            // Problem, no property. That's life ...
            // ex.printStackTrace();
        }
    }

    /** Test equallity by SID. */
    public boolean equals(Object obj) {
        if (obj instanceof ServiceNode)
            return service.equals(((ServiceNode)obj).service);
        return false;
    }

    public String getServiceID() {
        return service.serviceID.toString();
    }


}


/*
* <<Log>>
*  3    Gandalf   1.2         2/7/00   Petr Kuzel      More service details
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

