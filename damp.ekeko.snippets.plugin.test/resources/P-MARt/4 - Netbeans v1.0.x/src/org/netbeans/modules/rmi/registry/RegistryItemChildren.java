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

import java.beans.*;
import java.rmi.*;
import java.util.*;

import org.openide.nodes.*;
import org.openide.util.WeakListener;

import org.netbeans.modules.rmi.*;

/**
 * [PENDING] removing listener from item needs improving
 * it should be rather called when node is discarded.
 * @author  mryzl
 */

public class RegistryItemChildren extends Children.Keys {

    RegistryItem item;

    /** Keep this reference because it is weak listener. */
    PropertyChangeListener listener;

    /** Creates new RegistryItemChildren. */
    public RegistryItemChildren(RegistryItem item) {
        this.item = item;

        // weak listener, it is not necessary to unregister
        listener = new ChildrenListener();
        item.addPropertyChangeListener(new WeakListener.PropertyChange(listener));
        setKeys(item.getServices());
    }

    /** Proxy to setKeys.
    */
    protected void setKeys2(Collection keysSet) {
        super.setKeys(keysSet);
    }

    protected Node[] createNodes(Object key) {
        Class clazz = ((ServiceItem) key).getServiceClass();
        Node node;
        if (clazz != null) {
            node = new ServiceNode((ServiceItem) key, new ServiceChildren(clazz.getInterfaces()));
        } else {
            node = new ServiceNode((ServiceItem) key);
        }
        return new Node[] { node };
    }

    private class ChildrenListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            // huh, zmena
            // jestli je nova hodnota null, tak se odregistruj, jinak se updatni
            Collection services;
            if ((services = item.getServices()) != null) setKeys2(services);
            else {
                RMIRegistryPool.getDefault().getChildren().refresh(item);
            }
        }
        protected void finalize() throws Throwable {
            item.removePropertyChangeListener(this);
            super.finalize();
        }
    }
}

/*
* <<Log>>
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         8/27/99  Martin Ryzl     
* $ 
*/ 
