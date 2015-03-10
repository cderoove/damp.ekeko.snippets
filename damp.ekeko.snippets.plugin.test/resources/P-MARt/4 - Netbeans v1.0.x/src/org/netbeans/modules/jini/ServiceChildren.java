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

import java.rmi.*;
import java.beans.*;
import java.util.*;

import net.jini.admin.*;
import net.jini.core.lookup.*;
import net.jini.core.entry.*;
import net.jini.lookup.entry.*;

import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.src.*;
import org.openide.src.nodes.*;

/** Children for entries and interfaces.
*/
class ServiceChildren extends Children.Array {

    boolean created = false;
    ServiceItem item;

    public ServiceChildren(ServiceItem item) {
        this.item = item;
    }

    public void addNotify() {
        if (created) return;

        if (item.service == null) return;

        if (item.service instanceof Administrable) {
            add( new Node[] {
                     new AdminsNode(new AdminsChildren(new AdminsGetter(item)))
                 });
        }

        add( new Node[] {
                 new EntriesNode(new EntriesChildren(item)),
                 new InterfacesNode(new InterfacesChildren(item)),
             });

        created = true;

    }


    class AdminsGetter implements Runnable {

        Object admin;
        Runnable user;
        ServiceItem item;

        public AdminsGetter(ServiceItem item) {
            this.item = item;
            new Thread(this, "Async-admin-getter").start();
        }

        public void run() {
            if (item.service != null && item.service instanceof Administrable) {
                try {

                    Object admin2 = ((Administrable)item.service).getAdmin();

                    synchronized (this) {
                        admin = admin2;
                        if (user != null) user.run();
                    }

                }  catch (RemoteException ex) {
                    // no kids
                }
            }
        }

        public synchronized void addUser(Runnable user) {
            this.user = user;
            if (admin != null)
                user.run();
        }

    }

    class AdminsNode extends DefaultNode {

        Object admin;

        public AdminsNode(AdminsChildren kids) {
            super(kids);

            setName("Admins");
            setIconBase(Util.getString("ADMINS_ICON_BASE"));

        }


        private void notifyAdmin(Object admin) {
            this.admin = admin;
            firePropertyChange(Node.PROP_PROPERTY_SETS, null, null);
        }

        protected Sheet createSheet() {

            Sheet s = super.createSheet();
            Sheet.Set ss = s.get(Sheet.PROPERTIES);
            if (ss == null) {
                ss = s.createPropertiesSet();
                s.put(ss);
            }


            BeanNode bn = null;

            try {
                if (admin != null)
                    bn = new BeanNode(admin);

                if (bn != null) {
                    Node.PropertySet ps[] = bn.getPropertySets();
                    for (int i = 0; i<ps.length; i++) {
                        Node.Property prop[] = ps[i].getProperties();
                        for (int j = 0; j<prop.length; j++)
                            ss.put(prop[j]);
                    }
                }

            } catch (IntrospectionException ex) {
                // nothig to do
                System.err.println(ex);
            }

            return s;
        }

    }

    class AdminsChildren extends Children.Keys implements Runnable {

        AdminsGetter getter;

        public AdminsChildren(AdminsGetter getter) {

            this.getter = getter;
            getter.addUser(this);

        }

        /** Callback from Admins getter. */
        public void run() {
            ((AdminsNode)getNode()).notifyAdmin(getter.admin);

            TreeSet set = new TreeSet(new Util.ClassCollator());
            Class ifs[] = getter.admin.getClass().getInterfaces();
            for (int i=0; i<ifs.length; i++) {
                addIf(ifs[i], set);
            }

            setKeys(set);
        }

        /** Recussivelly add all super interfaces */
        private void addIf(Class sui, Set set) {
            if (sui == null) return;
            set.add(sui);
            Class[] ifs = sui.getInterfaces();
            if (ifs == null) return;

            for (int i=0; i<ifs.length; i++) {
                if (! ifs[i].equals(sui))
                    addIf(ifs[i], set);
            }
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof Class) {
                // interfaces
                ClassElement ce = ClassElement.forClass((Class)key);
                InterfaceNode inode = new InterfaceNode((Class) key, ce);
                return new Node[] { inode };
            }
            return null;
        }
    }





    class EntriesNode extends DefaultNode {
        public EntriesNode(EntriesChildren kids) {
            super(kids);
            setName("Entries");
            setIconBase(Util.getString("ENTRY_CLASSES_ICON_BASE"));
        }
    }

    class EntriesChildren extends Children.Keys {

        ServiceItem item;

        public EntriesChildren(ServiceItem item) {
            this.item = item;
        }

        public void addNotify() {
            net.jini.core.entry.Entry[] entries = item.attributeSets;
            if (entries != null) setKeys(entries);
        }

        protected Node[] createNodes(Object key) {

            if (key instanceof net.jini.core.entry.Entry) {

                // entries
                EntryBean eb = null;
                try {
                    eb = EntryBeans.createBean((net.jini.core.entry.Entry)key);
                    //          System.err.println("jini.RI: eb = " + eb);
                } catch (Exception ex) {
                    // dont worry, try key
                    // ex.printStackTrace();  //BUG 5536
                }

                try {
                    BeanNode bn = (eb != null) ? new BeanNode(eb): new BeanNode(key);
                    Node nodes[] = new Node[1];

                    bn.setName(key.getClass().getName());
                    bn.getCookieSet().add(
                        new ServiceTemplateCookie.Default(key, ServiceTemplateCookie.ENTRY_OBJECT_ITEM)
                    );
                    nodes[0] = bn;
                    return nodes;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return new Node[] {};
        }

    }





    class InterfacesNode extends DefaultNode {
        public InterfacesNode(InterfacesChildren kids) {
            super(kids);
            setName("Interfaces");
            setIconBase(Util.getString("INTERFACES_ICON_BASE"));
        }
    }

    class InterfacesChildren extends Children.Keys {

        ServiceItem item;

        public InterfacesChildren(ServiceItem item) {

            this.item = item;

        }

        public void addNotify() {
            Object service = item.service;
            if (service != null) {
                Class[] interfaces = service.getClass().getInterfaces();
                if (interfaces != null) setKeys(interfaces);
            }
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof Class) {
                // interfaces
                ClassElement ce = ClassElement.forClass((Class)key);
                InterfaceNode inode = new InterfaceNode((Class) key, ce);
                return new Node[] { inode };
            }

            return new Node[] {};
        }

    }


}
