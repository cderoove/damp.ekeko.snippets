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

import java.io.*;
import java.util.*;
import java.beans.*;

import net.jini.core.discovery.*;
import net.jini.core.lookup.*;
import net.jini.lookup.*;

import org.openide.nodes.*;
import org.openide.src.*;
import org.openide.src.nodes.*;
import org.openide.util.actions.*;

/**
 * Represents lookup results in specified domain. The domain 
 * is determined by BrowserModel. Contains three subnodes:
 * <p>
 * <li> Entries all available entry types in discovery domain
 * <li> Interfaces all available interfaces in discovery domain
 * <li> Services all available service instances in discovery domain
 *
 * @author  Petr Kuzel
 * @version 
 */
class LookupChildren extends Children.Array {

    BrowserModel browser;
    private boolean created = false;

    public LookupChildren(BrowserModel browser) {
        this.browser = browser;
    }

    public void addNotify() {
        if (created) return;

        add( new Node[] {
                 //new GroupsNode(loc),
                 //new ServiceTypesNode(loc),
                 new EventsNode(new EventsChildren(browser)),
                 new EntriesNode(new EntriesChildren(browser)),
                 new ServiceInterfacesNode(new ServiceInterfacesChildren(browser)),
                 new ServicesNode(new ServicesChildren(browser))
             });

        created = true;

    }


    class EventsNode extends DefaultNode {
        public EventsNode(EventsChildren kids) {
            super(kids);
            setName("Events");
            setIconBase(Util.getString("EVENTS_ICON_BASE"));

            systemActions = new SystemAction[] { };
        }
    }

    public class EventsChildren extends Children.Keys implements PropertyChangeListener {

        BrowserModel browser;

        public EventsChildren(BrowserModel browser) {
            this.browser = browser;
        }

        public void addNotify() {
            browser.addPropertyChangeListener(this);
            setKeys(browser.getEvents());
        }

        public void removeNotify() {
            browser.removePropertyChangeListener(this);
            setKeys(Collections.EMPTY_SET);
        }

        public void propertyChange(final PropertyChangeEvent e) {
            if (e.getPropertyName().equals("events")) {
                setKeys((List)e.getNewValue());
            }
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof BrowserEvent) {
                BrowserEvent e = (BrowserEvent) key;
                return new Node[] { new EventNode(e) };
            }

            return null;
        }

        public class EventNode extends DefaultNode {

            BrowserEvent e;

            public EventNode(BrowserEvent e) {
                super(Children.LEAF);

                this.e = e;

                setDisplayName(e.getName());
                setName(e.toString());

                setIconBase(Util.getString("EVENT_ICON_BASE"));
            }

            protected Sheet createSheet() {
                Sheet s = super.createSheet();
                Sheet.Set ss = s.get(Sheet.PROPERTIES);
                if (ss == null) {
                    ss = s.createPropertiesSet();
                    s.put(ss);
                }
                Node.Property p;

                try {

                    Object obj = this;

                    p = new PropertySupport.Reflection (obj, String.class, "getDetail", null);
                    p.setName("Detail");
                    //        p.setDisplayName(Util.getString("PROP_getHost"));
                    //        p.setShortDescription(Util.getString("HINT_getHost"));
                    ss.put(p);

                    p = new PropertySupport.Reflection (obj, String.class, "getTime", null);
                    p.setName("Time");
                    //        p.setDisplayName(Util.getString("PROP_getPort"));
                    //        p.setShortDescription(Util.getString("HINT_getPort"));
                    ss.put(p);


                } catch (Exception ex) {
                    throw new InternalError();
                }

                return s;
            }

            public String getDetail() {
                return e.getDetail();
            }

            public String getTime() {
                return new Date(e.getTime()).toString();
            }
        }
    }

    /** Hode that contains all  services.
    */
    class ServicesNode extends DefaultNode {
        public ServicesNode(ServicesChildren kids) {
            super(kids);
            setName("Services");
            setIconBase(Util.getString("SERVICES_ICON_BASE"));
        }
    }


    class ServicesChildren extends Children.Keys implements ServiceDiscoveryListener {
        /**
         * @associates ServiceItemExt 
         */
        private TreeSet keys;
        private boolean created = false;
        private BrowserModel browser;

        public ServicesChildren(BrowserModel browser) {
            keys = new TreeSet(new Util.ClassCollator());
            this.browser = browser;
        }

        protected void addNotify() {
            if (!created) {
                browser.addListener(this);
            }
            created = true;
        }

        protected void removeNotify() {
            // do nothing
            // let keys stays up to date even if closed
        }

        void destroy() {
            if (!created) return;
            browser.removeListener(this);
            browser.terminate();
            browser = null;
            keys = null;
            created = false;
        }

        protected Node[] createNodes(Object key) {
            return new Node[] { new ServiceNode((ServiceItemExt)key) };
        }

        public void serviceAdded(ServiceDiscoveryEvent event) {
            keys.add(new ServiceItemExt(event.getPostEventServiceItem()));
            setKeys(keys);
        }

        public void serviceRemoved(ServiceDiscoveryEvent event) {
            keys.remove(new ServiceItemExt(event.getPreEventServiceItem()));
            setKeys(keys);
        }

        public void serviceChanged(ServiceDiscoveryEvent event) {
            refreshKey(new ServiceItemExt(event.getPostEventServiceItem()));
        }

    }

    /** Contains async keys */
    class EntriesNode extends DefaultNode {
        EntriesNode(EntriesChildren kids) {
            super(kids);
            setName("Entries");
            setIconBase(Util.getString("ENTRY_CLASSES_ICON_BASE"));
        }

    }

    class EntriesChildren extends Children.Keys implements PropertyChangeListener {

        BrowserModel browser;
        private boolean created = false;

        public EntriesChildren(BrowserModel browser) {
            this.browser = browser;
        }

        public void addNotify() {
            if (created) return;
            setKeys(browser.getEntryClasses());
            browser.addPropertyChangeListener(this);
            created = true;
        }

        /** Notify that the source brovser has changed state. */
        public void propertyChange(final PropertyChangeEvent e) {
            if ("entries".equals(e.getPropertyName()) ) {
                if (this.browser == browser) {
                    setKeys(browser.getEntryClasses());
                }
            }
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof Class) {
                Class clzz = (Class) key;
                ClassElement ce = ClassElement.forClass(clzz);
                ClassElementFilter filter = new ClassElementFilter();
                filter.setModifiers(filter.PUBLIC | filter.FIELD);
                ClassChildren kids = new ClassChildren(ce);
                kids.setFilter(filter);
                return new Node[] {
                           new EntryClassNode(clzz, ce, kids, false)
                       };
            }

            return null;
        }

        private class EntryClassNode extends ClassElementNode {
            public EntryClassNode(Class cl, ClassElement el, ClassChildren kids, boolean flag) {
                super(el, kids, flag);
                setDisplayName(cl.getName());
            }

        }
    }


    /** Node entry for all looked up interfaces in domain.
    */
    class ServiceInterfacesNode extends DefaultNode {

        /** Creates new ServiceInterfacesNode */
        public ServiceInterfacesNode(ServiceInterfacesChildren kids) {
            super(kids);
            setName("Interfaces");
            setIconBase(Util.getString("INTERFACES_ICON_BASE"));
            systemActions = new SystemAction[] { };
        }
    }

    class ServiceInterfacesChildren extends Children.Keys implements PropertyChangeListener {

        BrowserModel browser;
        private boolean created = false;

        public ServiceInterfacesChildren(BrowserModel browser) {
            this.browser = browser;
        }

        public void addNotify() {
            if (created) return;
            setKeys(browser.getServiceInterfaces());
            browser.addPropertyChangeListener(this);
            created = true;
        }

        /** Notify that the source brovser has changed state. */
        public void propertyChange(final PropertyChangeEvent e) {
            if ("services".equals(e.getPropertyName()) ) {
                if (this.browser == browser) {
                    setKeys(browser.getServiceInterfaces());
                }
            }
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof Class) {
                Class clzz = (Class) key;
                ClassElement ce = ClassElement.forClass(clzz);
                return new Node[] {
                           new InterfaceNode(clzz, ce)
                       };
            }

            return null;
        }
    }


    //
    // TODO, very low priority
    //

    /** Contains async keys */
    class ServiceTypesNode extends DefaultNode {
        ServiceTypesNode(LookupLocator loc) {
            super(Children.LEAF);
            setName("Service Types");
        }

    }

    /** Contains async keys */
    class GroupsNode extends DefaultNode {
        GroupsNode(LookupLocator loc) {
            super(Children.LEAF);
            setName("Groups");
        }

    }

}


/*
* <<Log>>
*  3    Gandalf   1.2         2/7/00   Petr Kuzel      More service details
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

