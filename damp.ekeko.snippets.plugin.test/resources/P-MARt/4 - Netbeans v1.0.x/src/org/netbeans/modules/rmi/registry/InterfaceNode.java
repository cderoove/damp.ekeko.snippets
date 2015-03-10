/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi.registry;

import org.openide.nodes.*;
import org.openide.src.*;
import org.openide.src.nodes.*;
import org.openide.util.actions.*;

/**
 *
 * @author  mryzl
 */

public class InterfaceNode extends ClassElementNode implements Node.Cookie {

    private static final boolean DEBUG = false;

    Class cl;

    /** Creates new InterfaceNode. */
    public InterfaceNode(Class cl, ClassElement ce) {
        super(ce, new ClassChildren(ce), false);
        this.cl = cl;

        systemActions = new SystemAction[] {
                            SystemAction.get(org.netbeans.modules.rmi.registry.CreateClientAction.class),
                            SystemAction.get(org.netbeans.modules.rmi.registry.SaveInterfaceAction.class),
                            null,
                            SystemAction.get(org.openide.actions.ToolsAction.class),
                            SystemAction.get(org.openide.actions.PropertiesAction.class),
                        };
        getCookieSet().add(this);
    }

    public Class getInterface() {
        return cl;
    }

    public String getURLString() {
        // potrebuju registry item a service item
        try {
            Node snode = getParentNode(); // service item node
            if (DEBUG) System.err.println("InterfaceNode.getURLString(): snode = " + snode);
            Node rnode = snode.getParentNode(); // registry item node
            if (DEBUG) System.err.println("InterfaceNode.getURLString(): rnode = " + rnode);
            ServiceItem sitem = (ServiceItem) snode.getCookie(ServiceItem.class);
            if (DEBUG) System.err.println("InterfaceNode.getURLString(): sitem = " + sitem);
            RegistryItem ritem = (RegistryItem) rnode.getCookie(RegistryItem.class);
            if (DEBUG) System.err.println("InterfaceNode.getURLString(): ritem = " + ritem);
            return ritem.getURLString() + sitem.getName();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

/*
* <<Log>>
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         8/30/99  Martin Ryzl     saving corrected
*  1    Gandalf   1.0         8/27/99  Martin Ryzl     
* $ 
*/ 
