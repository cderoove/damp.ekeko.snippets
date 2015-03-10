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
import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.net.*;
import java.net.UnknownHostException;
import java.text.*;
import java.util.*;

import org.openide.*;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;

/**  Class representing one registry, i.e. pair hostname:port.
 *
 * @author Martin Ryzl
 */
public class RegistryItem implements Node.Cookie, java.io.Serializable {

    /** Serial version UID. */
    static final long serialVersionUID = -4780961588255414026L;

    /** Bundle. */
    transient private static final ResourceBundle bundle = NbBundle.getBundle(RegistryItem.class);

    /** Name of the property services*/
    public static final String PROP_SERVICES = "services"; // NOI18N

    /** Icon for server. */
    static final String SERVER_ICON_BASE = "/org/netbeans/modules/rmi/resources/rmiServer"; // NOI18N
    /** Icon for interface. */
    static final String INTERFACE_ICON_BASE = "/org/openide/resources/src/interface"; // NOI18N

    /** Message format for valid node. */
    static final MessageFormat FMT_URL = new MessageFormat(bundle.getString("FMT_ItemURL")); // NOI18N
    /** Message format for invalid node. */
    static final MessageFormat FMT_REGISTRY_ITEM = new MessageFormat(bundle.getString("FMT_RegistryItem")); // NOI18N

    /** Localhost name. */
    public static final String LOCALHOST = "localhost"; // NOI18N

    /** Address. */
    protected InetAddress address;
    /** Port. */
    protected int port;

    /** Holds value of property services. */
    transient private Collection services;
    /** Property change support. */
    transient private PropertyChangeSupport support;

    /** Creates a new RegistryItem with localhost and the default port.
     */
    public RegistryItem() throws RemoteException, UnknownHostException {
        this(LOCALHOST, Registry.REGISTRY_PORT);
    }

    /** Creates a new RegistryItem with given host and the port.
     * @param hostname Hostname.
     * @param port Port.
     */
    public RegistryItem(String hostname, int port) throws RemoteException, UnknownHostException {
        this(InetAddress.getByName(hostname), port);
    }

    /** Creates a new RegistryItem with given host and the port.
     * @param address Address.
     * @param port Port.
     */
    public RegistryItem(InetAddress address, int port) throws RemoteException {
        this.address = address;
        this.port = port;
    }

    /** Equals.
     * @return true if the objects is a RegistryItem and the adress and port are
     * equal.
     */
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o != null) && getClass().isAssignableFrom(o.getClass())) {
            RegistryItem ri = (RegistryItem) o;
            if (    ( ri.getAddress().equals( getAddress() ) )
                    && ( ri.getPort() == getPort() )
               ) return true;
        }
        return false;
    }

    /** Hash code.
    * @return has code of address xored with hash code of port.
    */
    public int hashCode() {
        return getAddress().hashCode() ^ getPort();
    }

    /**
    */
    public String toString() {
        return FMT_REGISTRY_ITEM.format(getItemObjects());
    }

    /** Get the registry for the RegitryItem.
     * @return Registry reference.
     */
    public Registry getRegistry() throws RemoteException {
        return LocateRegistry.getRegistry(getHostName(), getPort());
    }

    /** Creates an URL for the RegistryItem.
     * @return URL of registry.
     */
    public String getURLString() {
        return FMT_URL.format(getItemObjects());
    }

    /** Getter for the address.
    * @return address
    */
    public InetAddress getAddress() {
        return address;
    }

    /** Returns the host name.
    * @return host
    */
    public String getHostName() {
        return getAddress().getHostName();
    }

    /** Getter for the port.
    * @return host
    */
    public int getPort() {
        return port;
    }

    /** Return item as array of objects. Used for formating texts.
    * @return an array of Objects. [0] = hostname, [1] = port.
    */
    protected Object[] getItemObjects() {
        return new Object[] {
                   getHostName(),
                   new Integer(getPort())
               };
    }

    /** Getter for property services.
     * @return Value of property services.
     */
    public Collection getServices() {
        return services;
    }

    /** Setter for property services.
     * @param services New value of property services.
     */
    public void setServices(Collection services) {
        Object old = this.services;
        this.services = services;
        firePropertyChange(PROP_SERVICES, old, services);
    }

    /** Update services. This operation could be VERY time consuming and should
    * be called from dedicated thread.
    */
    public void updateServices() {
        try {
            Registry registry = getRegistry();
            String[] services = registry.list();
            Set set = new TreeSet();
            for(int i = 0; i < services.length; i++) {
                Class clazz = null;
                try {
                    Remote remote = registry.lookup(services[i]);
                    clazz = remote.getClass();
                } catch (Exception ex) {
                    // no clazz
                }
                set.add(new ServiceItem(services[i], clazz));
            }
            setServices(set);
        } catch (Exception ex) {
            setServices(null);
        }
    }

    /** Adds property listener.
    */
    public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
        if (support == null) {
            synchronized (this) {
                // new test under synchronized block
                if (support == null) {
                    support = new PropertyChangeSupport (this);
                }
            }
        }
        support.addPropertyChangeListener (l);
    }

    /** Removes property listener.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        if (support != null) {
            support.removePropertyChangeListener (l);
        }
    }

    /** Fires property change event.
    * @param name property name
    * @param o old value
    * @param n new value
    */
    protected final void firePropertyChange(String name, Object o, Object n) {
        if (support != null) {
            support.firePropertyChange (name, o, n);
        }
    }

    /** Finalize object. It sets the list of services to null.
    */
    protected void finalize() throws Throwable  {
        setServices(null);
        super.finalize();
    }
}

/*
 * <<Log>>
 *  8    Gandalf-post-FCS1.5.1.1     3/20/00  Martin Ryzl     localization
 *  7    Gandalf-post-FCS1.5.1.0     3/2/00   Martin Ryzl     local registry control 
 *       added
 *  6    Gandalf   1.5         11/27/99 Patrik Knakal   
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/12/99 Martin Ryzl     Beta5 bugfixes
 *  3    Gandalf   1.2         10/7/99  Martin Ryzl     
 *  2    Gandalf   1.1         9/13/99  Martin Ryzl     varioous bug corrected  
 *  1    Gandalf   1.0         8/27/99  Martin Ryzl     
 * $
 */














