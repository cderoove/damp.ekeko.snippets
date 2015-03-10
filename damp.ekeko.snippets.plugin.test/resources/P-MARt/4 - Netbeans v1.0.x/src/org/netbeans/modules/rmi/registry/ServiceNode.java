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

import java.io.IOException;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

/**
 *
 * @author  mryzl
 */

public class ServiceNode extends AbstractNode implements Node.Cookie {

    /** Icon for service. */
    static final String SERVICE_ICON_BASE = "/org/netbeans/modules/rmi/resources/rmiService"; // NOI18N

    /** Resource bundle. */
    private static final ResourceBundle bundle = NbBundle.getBundle(RegistryItem.class);

    /** Creates new ServiceNode. */
    public ServiceNode(ServiceItem item) {
        this(item, Children.LEAF);
    }

    /** Creates new ServiceNode. */
    public ServiceNode(ServiceItem item, Children children) {
        super(children);
        setName(item.getName());
        CookieSet cookies = getCookieSet();
        cookies.add(item);
        cookies.add(this);
        cookies.add(new ServiceInstance());
        // cookies.add() - instance support

        // add class annotation property
        try {
            Sheet sheet = getSheet();
            Sheet.Set expert;
            if ((expert = sheet.get(Sheet.EXPERT)) == null) {
                expert = Sheet.createExpertSet();
                sheet.put(expert);
            }
            Node.Property p = new PropertySupport.Reflection(ServiceNode.this, String.class, "getClassAnnotation", null); // NOI18N
            p.setName("ClassAnnotation"); // NOI18N
            p.setDisplayName(bundle.getString("PROP_classAnnotation")); // NOI18N
            p.setShortDescription(bundle.getString("HINT_classAnnotation")); // NOI18N
            expert.put(p);
        } catch (NoSuchMethodException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
        }

        setIconBase(SERVICE_ICON_BASE);
        systemActions = new SystemAction[] {
                            SystemAction.get(org.openide.actions.CustomizeBeanAction.class),
                            null,
                            SystemAction.get(org.openide.actions.DeleteAction.class),
                            null,
                            SystemAction.get(org.openide.actions.ToolsAction.class),
                            SystemAction.get(org.openide.actions.PropertiesAction.class),
                        };
    }

    /** Returns the class annotation (representing the location for a class)
    * that RMI will use to annotate the call stream
    * when marshalling objects of the given class.
    * @return class annotation
    */
    public String getClassAnnotation() {
        try {
            ServiceItem sitem = (ServiceItem) getCookie(ServiceItem.class);
            return RMIClassLoader.getClassAnnotation(sitem.getServiceClass());
        } catch (NullPointerException ex) {
            // ex.printStackTrace();
            // if class is null, return null too
        }
        return null;
    }

    public void destroy() throws IOException {
        // call unbind
        RegistryItem item = (RegistryItem) getParentNode().getCookie(RegistryItem.class);
        try {
            Registry registry = item.getRegistry();
            ServiceItem sitem = (ServiceItem) getCookie(ServiceItem.class);
            registry.unbind(sitem.getName());
        } catch (AccessException ex) {
            // if this operation is not permitted (if originating from a non-local host, for example)
            TopManager.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } catch (RemoteException ex) {
            // Access can be encapsulated in RemoteException
            Throwable detail = ex.detail;
            if (detail instanceof AccessException) {
                TopManager.getDefault().notify(new NotifyDescriptor.Message(detail.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
            } else {
                throw ex;
            }
        } catch (NotBoundException ex) {
            // just refresh
        } finally {
            RMIRegistryPool.updateItem(item);
        }
    }

    public boolean canDestroy() {
        return true;
    }

    public class ServiceInstance implements InstanceCookie {

        public java.lang.Object instanceCreate() throws java.io.IOException, java.lang.ClassNotFoundException {
            Remote remote = null;
            try {
                String name = getItem().getName();
                Registry registry = getRegistry();
                remote = registry.lookup(name);
                return remote;
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IOException();
            }
        }

        public java.lang.String instanceName() {
            return getItem().toString();
        }

        public java.lang.Class instanceClass() throws java.io.IOException, java.lang.ClassNotFoundException {
            Class clazz = getItem().getServiceClass();
            if (clazz != null) return clazz;
            throw new ClassNotFoundException();
        }

        private ServiceItem getItem() {
            return (ServiceItem) getCookie(ServiceItem.class);
        }

        private Registry getRegistry() throws RemoteException {
            Node rnode = getParentNode();
            RegistryItem ritem = (RegistryItem) rnode.getCookie(RegistryItem.class);
            return ritem.getRegistry();
        }
    }
}


/*
* <<Log>>
*  6    Gandalf-post-FCS1.3.1.1     3/20/00  Martin Ryzl     localization
*  5    Gandalf-post-FCS1.3.1.0     3/2/00   Martin Ryzl     bugfix #4870
*  4    Gandalf   1.3         1/21/00  Martin Ryzl     
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         9/13/99  Martin Ryzl     varioous bug corrected  
*  1    Gandalf   1.0         8/27/99  Martin Ryzl     
* $ 
*/ 
