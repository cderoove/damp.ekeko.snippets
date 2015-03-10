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

import java.beans.*;
import java.util.*;
import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.net.*;

import net.jini.core.discovery.*;
import net.jini.core.lookup.*;

import net.jini.discovery.*;
import net.jini.lookup.*;

import org.openide.util.*;

/**
 * An cached implementation of Jini services browser.
 * Contains simple constructors for three basic discovery scenarios:
 * <li> all services in <groups> in multicast domain
 * <li> all services at <located registrars>
 * <li> <templetd services> at all groups at multicast domain
 * 
 * <p>User can register some listeners:
 * <li> addListener(ServiceDiscoveryListener l) new service
 * <li> addDiscoveryListener(DiscoveryListener l) new LUS
 * <li> addPropertyChangeListener(PropertyChangeListener l);
 * <P>Fires:
 * <li> "services" a new service discovered, ...
 * <li> "entries" ...
 * <li> "events" a new djinn event occured
 *
 * @author  Petr Kuzel
 * @version 
 */
public class BrowserModel extends Object implements DiscoveryListener, DiscoveryManagement, ServiceDiscoveryListener {

    // -- caching --

    // cache of discovered services
    private LookupCache cache;

    // defenes WHERE to search
    private LookupDiscoveryManager dmgr;

    // chache provider
    private ClientLookupManager lmgr;

    private final static ServiceItemFilter ALL = new NoFilter();

    // default instance caching LUSes
    private static BrowserModel model;

    /** Discovered LUS SID -> String[] of groups 
     * @associates String*/
    private HashMap groups = new HashMap();

    /** Discovered LUS SID -> LookupLocators */
    private HashMap locators = new HashMap();

    /** DiscoveryListener -> DiscoveryListenerWrapper */
    private HashMap wrappers = new HashMap();

    /** Entry class -> list of SIDs */
    private HashMap entries = new HashMap();

    /** Service class -> list of SIDs */
    private HashMap services = new HashMap();

    /** Holds value of property events. 
     * @associates BrowserEvent*/
    private LinkedList events = new LinkedList();

    /** Utility field used by bound properties. */
    private PropertyChangeSupport propertyChangeSupport
    = new PropertyChangeSupport(this);

    /** All registered discovery listeners. */
    private HashSet discoveryListeners = new HashSet();

    /**
    * Creates model caching all services registered at LUSes reachable throught
    * multicasting and being members of any group passed.
    */
    public BrowserModel(String[] groups) {
        this(groups, null, null);
    }

    public BrowserModel(String grp) {
        this(new String[] {grp});
    }

    public BrowserModel(Groups groups) {
        this(groups.getGroups());
    }

    /**
    * Creates model caching all services registered at LUSes reachable throught
    * passed locators.
    */  
    public BrowserModel(LookupLocator[] locators) {
        this(LookupDiscovery.NO_GROUPS, locators, null);
    }

    public BrowserModel(LookupLocator loc) {
        this(new LookupLocator[] {loc});
    }

    /**
    * Creates model caching templated services registered at LUSes reachable throught
    * multicasting (i.e. ALL_GROUPS).
    */    
    public BrowserModel(ServiceTemplate template) {
        this(LookupDiscovery.ALL_GROUPS, null, template);
    }

    /** Creates new LookupModel that caches Jini services.
    * @param groups to discover (multicast)
    * @param locators lookup these LUSes (unicast)
    * @param template services that should be included
    */
    public BrowserModel(String[] groups, LookupLocator[] locators, ServiceTemplate template) {
        annotateClasses();
        createCache(groups, locators, template);

    }

    /** Discover all services in all groups */
    public BrowserModel() {
        this(new ServiceTemplate(null, null, null));
    }

    /** Shared BrowserModel instance caching local LUSes
    * can be obtained by call to this method. 
    */
    public static synchronized BrowserModel getDefault() {
        ServiceTemplate template = new ServiceTemplate(null, new Class[] {ServiceRegistrar.class}, null);
        if (model == null)
            model = new BrowserModel(template);

        return model;
    }

    /** Is anybody interested in asynchronous browsing? */
    public void addListener(ServiceDiscoveryListener l) {
        cache.addListener(l);
    }

    /** Indicate no more interest. */
    public void removeListener(ServiceDiscoveryListener l) {
        cache.removeListener(l);
    }

    public void addDiscoveryListener(DiscoveryListener l) {
        // wrap it to be always the first who knows
        discoveryListeners.add(l);
    }

    public void removeDiscoveryListener(DiscoveryListener l) {
        discoveryListeners.remove(l);
    }

    /** Add a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener (l);
    }

    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener (l);
    }



    public ServiceRegistrar[] getRegistrars() {
        return dmgr.getRegistrars();
    }

    public void discard(ServiceRegistrar proxy) {
        dmgr.discard(proxy);
    }


    public void addLocators(LookupLocator[] locs) {
        dmgr.addLocators(locs);
    }

    public void removeLocators(LookupLocator[] locs) {
        dmgr.removeLocators(locs);
    }

    public void setLocators(LookupLocator[] locs) {
        dmgr.setLocators(locs);
    }

    public void addGroups(String[] grps) throws IOException {
        dmgr.addGroups(grps);
    }

    public void removeGroups(String[] grps) throws IOException {
        dmgr.removeGroups(grps);
    }

    public void setGroups(String[] grps) throws IOException {
        dmgr.setGroups(grps);
    }

    /** End all underlayng threads. */
    public void terminate() {
        dmgr.terminate();
        lmgr.terminate();
        cache.terminate();
    }

    /**
    * Annotate classes by URL that can serve internal http server. 
    * Using introspection cange codebase field.
    * <p> Warning: depends on SUn's implementation of RMI.
    */
    private void annotateClasses() {

        try {
            java.lang.reflect.Field f = Class.forName("sun.rmi.server.LoaderHandler").getDeclaredField("codebase");
            f.setAccessible(true);
            String codebase = HttpServer.getResourceRoot().toExternalForm();
            f.set(null, codebase);
        } catch (Exception ex) {
            System.err.println("can not set codebase " + ex);
        }

    }

    /** Create a new cache of discovered services */
    private void createCache(String[] groups, LookupLocator[] locators, ServiceTemplate template) {

        template = template == null ? new ServiceTemplate(null, null, null) : template;

        try {
            dmgr = new LookupDiscoveryManager(groups, locators, this);
            lmgr = new ClientLookupManager(dmgr, null);
            cache = lmgr.createLookupCache(template, ALL, this);
        } catch (RemoteException ex) {
            // cache cannot create remote listener for LUS
            System.err.println(ex);
        } catch (IOException ex) {
            // cannot do multicast discovery - can not allocate socket.
            System.err.println(ex);
        }

        //    System.err.println("Cache created.");
    }

    /** Obtain names of up to current time available LUS groups.
     * @return array (may be empty) of strings (group names). 
     */
    public String[] getDiscoveredGroups() {
        TreeSet set = new TreeSet(java.text.Collator.getInstance());
        Iterator it = groups.values().iterator();

        while (it.hasNext()) {
            String[] grps = (String[]) it.next();
            set.addAll(Arrays.asList(grps));
        }

        if (set.remove(""))
            set.add("public");

        String[] ret = new String[set.size()];
        new Vector(set).toArray(ret);
        return ret;
    }

    /** Obtain names of up to current time available LUS locators.
     * @return array of locators (may be empty). 
    */  
    public LookupLocator[] getDiscoveredLocators() {

        HashSet set = new HashSet(locators.values());
        LookupLocator[] ret = new LookupLocator[set.size()];
        new Vector(set).toArray(ret);
        return ret;
    }


    /** Getter for property events.
     * @return unmodifiable list of recent events.
     */
    public List getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
    * Add one event, as side effect canremove last one.
    */
    protected void logEvent(BrowserEvent e) {
        while (events.size() > org.netbeans.modules.jini.settings.JiniSettings.DEFAULT.getEventLimit()) {
            events.removeLast();
        }
        events.addFirst(e);
        setEvents(events);
    }

    /** Setter for property events.
     * @param events New value of property events.
     */
    protected void setEvents(LinkedList events) {
        LinkedList oldEvents = this.events;
        this.events = events;
        propertyChangeSupport.firePropertyChange ("events", null, events);
    }

    //
    // Discovery stage routines
    //


    /** Called when one or more lookup service registrars has been discovered.
     * The method should return quickly; e.g., it should not make remote
     * calls.
     *
     * @param e the event that describes the discovered registrars
     */
    public void discovered(DiscoveryEvent e) {

        // update groups and allow access from that address
        ServiceRegistrar[] regs = e.getRegistrars();

        for (int i=0; i<regs.length; i++) {
            ServiceRegistrar reg = regs[i];
            try {
                String _groups[] = reg.getGroups();
                ServiceID sid = reg.getServiceID();
                LookupLocator loc = reg.getLocator();

                logEvent(new BrowserEvent.DiscoveredLUS(loc));

                InetAddress adr = InetAddress.getByName(loc.getHost());

                groups.put(sid, _groups);
                locators.put(sid, loc);
                HttpServer.allowAccess(adr);

            } catch (java.net.UnknownHostException ex) {
                System.err.println(ex);

            } catch (RemoteException ex) {
                //TODO discard it
                System.err.println(ex);
            }
        }

        // forvard event

        Enumeration en = Collections.enumeration(discoveryListeners);
        while (en.hasMoreElements()) {
            DiscoveryListener lis = (DiscoveryListener) en.nextElement();
            lis.discovered(e);
        }
    }

    /** Called when one or more lookup service registrars has been discarded.
     * The method should return quickly; e.g., it should not make remote
     * calls.
     *
     * @param e the event that describes the discarded registrars
     */
    public void discarded(DiscoveryEvent e) {

        // update groups

        ServiceRegistrar[] regs = e.getRegistrars();

        for (int i=0; i<regs.length; i++) {
            ServiceRegistrar reg = regs[i];
            ServiceID sid = reg.getServiceID();

            logEvent(new BrowserEvent.DiscardedLUS((LookupLocator)locators.get(sid)));

            groups.remove(sid);
            locators.remove(sid);
        }

        // forward event

        Enumeration en = Collections.enumeration(discoveryListeners);
        while (en.hasMoreElements()) {
            DiscoveryListener lis = (DiscoveryListener) en.nextElement();
            lis.discarded(e);
        }

    }


    //
    // Lookup stage routines.
    //
    // Monitor all discovered services' entries and interfaces
    //
    // Implementation of BrowserListener background routines
    //


    public void serviceAdded(ServiceDiscoveryEvent event) {
        ServiceItem item = event.getPostEventServiceItem();

        logEvent(new BrowserEvent.DiscoveredService(item));

        addServiceClass(item.service.getClass(), item.serviceID);

        for (int i = 0; i<item.attributeSets.length; i++) {
            addEntryClass(item.attributeSets[i].getClass(), item.serviceID);
        }
    }

    public void serviceRemoved(ServiceDiscoveryEvent event) {
        ServiceItem item = event.getPreEventServiceItem();

        logEvent(new BrowserEvent.DiscardedService(item));

        removeServiceClass(item.service.getClass(), item.serviceID);

        for (int i = 0; i<item.attributeSets.length; i++) {
            removeEntryClass(item.attributeSets[i].getClass(), item.serviceID);
        }

    }

    public void serviceChanged(ServiceDiscoveryEvent event) {
        serviceRemoved(event);
        serviceAdded(event);
    }

    /** */
    private void addEntryClass(Class clzz, ServiceID id) {
        if (addClass(entries, clzz, id))
            fireUpdateBrowser("entries");
    }

    private void removeEntryClass(Class clzz, ServiceID id) {
        if (removeClass(entries, clzz, id))
            fireUpdateBrowser("entries");
    }

    private void addServiceClass(Class clzz, ServiceID id) {
        if (addClass(services, clzz, id))
            fireUpdateBrowser("services");
    }

    private void removeServiceClass(Class clzz, ServiceID id) {
        if (removeClass(services, clzz, id))
            fireUpdateBrowser("services");
    }

    /** @return true if first added. */
    private boolean  addClass(Map map, Class clzz, ServiceID id) {
        synchronized (map) {

            //      System.err.println("adding " + clzz + " " + id + " at " + map);
            boolean first = false;

            Set ids = (Set) map.get(clzz);
            if (ids == null) {
                first = true;
                ids = new HashSet();
                ids.add(id);
            } else {
                ids.add(id);
            }
            map.put(clzz, ids);

            return first;
        }
    }

    /** @return true if last removed. */
    private boolean removeClass(Map map, Class clzz, ServiceID id) {
        synchronized (map) {
            boolean last = false;

            Set ids = (Set) map.get(clzz);
            if (ids != null) {
                ids.remove(id);
                if (ids.size() == 0) {
                    map.remove(clzz);
                    last = true;
                }
            }

            return last;
        }
    }


    /** @return collated set of classes representing all discovered interfaces. */
    public Set getServiceInterfaces() {
        synchronized (services) {
            TreeSet ret = new TreeSet(new Util.ClassCollator());
            Enumeration en = Collections.enumeration(services.keySet());

            while (en.hasMoreElements()) {
                Class clzz = (Class) en.nextElement();

                Class[] ifs = clzz.getInterfaces();
                ret.addAll(Arrays.asList(ifs));
            }

            return ret;
        }
    }

    /** @return collated set classes representing all entries of discovered services. */
    public Set getEntryClasses() {
        synchronized (entries) {
            TreeSet ret = new TreeSet(new Util.ClassCollator());
            ret.addAll(entries.keySet());
            return ret;
        }
    }

    /** @return collated set of classes representying all discovered services. */
    public Set getServiceClassses() {
        synchronized (services) {
            TreeSet ret = new TreeSet(new Util.ClassCollator());
            ret.addAll(services.keySet());
            return ret;
        }
    }

    private synchronized void fireUpdateBrowser(String prop) {
        if ("entries".equals(prop))
            propertyChangeSupport.firePropertyChange("entries", null, null);
        else if ("services".equals(prop))
            propertyChangeSupport.firePropertyChange("services", null, null);
    }

    /** Filter without any filter ability. */
    private static class NoFilter implements ServiceItemFilter {
        public boolean check(ServiceItem item) {
            return true;
        }
    }


    //
    // Basic self test.
    //

    /** Test this class
    * @param args the command line arguments
    */
    public static void main (String[] args) throws Exception {

        //!!!
        System.setSecurityManager(new RMISecurityManager());

        BrowserModel me = BrowserModel.getDefault();
        Test test = new Test();
        me.addListener(test);
        Thread.currentThread().join(10000);

        System.err.println("Groups: ");
        String groups[] = me.getDiscoveredGroups();
        for (int i=0; i<groups.length; i++) {
            System.err.print(groups[i] + ", ");
        }
        System.err.println("");

        me.terminate();
        System.err.println("Self-test done.");
    }

    /** For self-testing purposes. */
    private static class Test implements ServiceDiscoveryListener {

        public void serviceAdded(ServiceDiscoveryEvent event) {
            System.err.println("Added:  " + event.getPostEventServiceItem());
        }

        public void serviceRemoved(ServiceDiscoveryEvent event) {
            System.err.println("Removed: " + event.getPreEventServiceItem());
        }

        public void serviceChanged(ServiceDiscoveryEvent event) {
            System.err.println("Changed: " + event.getPostEventServiceItem());
        }
    }

}


/*
* <<Log>>
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

