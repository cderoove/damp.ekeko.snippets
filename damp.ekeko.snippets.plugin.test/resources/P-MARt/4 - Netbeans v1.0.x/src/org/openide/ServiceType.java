/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.openide;
import java.beans.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.openide.util.HelpCtx;

/** This class represents an abstract subclass for services
* (compilation, execution, debugging, etc.) that can be registered in
* the system.
*
* @author Jaroslav Tulach
*/
public abstract class ServiceType extends Object implements java.io.Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7573598174423654252L;

    /** Name of property for the name of the service type. */
    public static final String PROP_NAME = "name"; // NOI18N

    /** name of the service type */
    private String name;

    /** listeners support */
    private transient PropertyChangeSupport supp;

    /** Default human-presentable name of the service type.
    * In the default implementation, taken from the bean descriptor.
    * @return initial value of the human-presentable name
    * @see FeatureDescriptor#getDisplayName
    */
    protected String displayName () {
        try {
            return Introspector.getBeanInfo (getClass ()).getBeanDescriptor ().getDisplayName ();
        } catch (Exception e) {
            // Catching IntrospectionException, but also maybe NullPointerException...?
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace ();
            return getClass ().getName ();
        }
    }

    /** Set the name of the service type.
    * Usually it suffices to override {@link #displayName},
    * or just to provide a {@link BeanDescriptor} for the class.
    * @param name the new human-presentable name
    */
    public void setName (String name) {
        String old = this.name;
        this.name = name;
        if (supp != null) {
            supp.firePropertyChange (PROP_NAME, old, name);
        }
    }

    /** Get the name of the service type.
    * The default value is given by {@link #displayName}.
    * @return a human-presentable name for the service type
    */
    public String getName () {
        return name == null ? displayName () : name;
    }

    /** Get context help for this executor type.
    * @return context help
    */
    public abstract HelpCtx getHelpCtx ();

    /** Add a property change listener.
    * @param l the listener to add
    */
    public final synchronized void addPropertyChangeListener (PropertyChangeListener l) {
        if (supp == null) supp = new PropertyChangeSupport (this);
        supp.addPropertyChangeListener (l);
    }

    /** Remove a property change listener.
    * @param l the listener to remove
    */
    public final void removePropertyChangeListener (PropertyChangeListener l) {
        if (supp != null) supp.removePropertyChangeListener (l);
    }

    /** Fire information about change of a property in the executor.
    * @param name name of the property
    * @param o old value
    * @param n new value
    */
    protected final void firePropertyChange (String name, Object o, Object n) {
        if (supp != null) {
            supp.firePropertyChange (name, o, n);
        }
    }

    /** The registry of all services. This class is provided by the implementation
    * of the IDE and should hold all of the services registered to the system.
    * <P>
    * This class can be serialized to securely save settings of all
    * services in the system.
    */
    public static abstract class Registry implements java.io.Serializable {
        /** suid */
        final static long serialVersionUID = 8721000770371416481L;

        /** Get all available services managed by the engine.
        * @return an enumeration of {@link ServiceType}s
        */
        public abstract Enumeration services ();

        /** Get all available services that are subclass of given class
        * @param clazz the class that all services should be subclass of
        * @return an enumeration of {@link ServiceType}s that are subclasses of
        *    given class
        */
        public Enumeration services (final Class clazz) {
            return new org.openide.util.enum.FilterEnumeration (services ()) {
                       public boolean accept (Object o) {
                           return clazz.isInstance (o);
                       }
                   };
        }

        /** Getter for list of all services types.
        * @return list of ServiceType
        */
        public abstract java.util.List getServiceTypes ();

        /** Setter for list of services types. This allows to change
        * instaces of the objects but only of the types that are already registered
        * to the system by manifest sections. If instance of any other type
        * is in the arr list it is ignored.
        *
        * @param arr list of ServiceTypes 
        */
        public abstract void setServiceTypes (java.util.List arr);

        /** Find the
        * executor implemented as a given class, among the executors registered to the
        * execution engine.
        * <P>
        * This should be used during (de-)serialization
        * of the specific executor for a data object: only store its class name
        * and then try to find the executor implemented by that class later.
        *
        * @param clazz the class of the executor looked for
        * @return the desired executor or <code>null</code> if it does not exist
        */
        public ServiceType find (Class clazz) {
            Enumeration en = services ();
            while (en.hasMoreElements ()) {
                Object o = en.nextElement ();
                if (o.getClass () == clazz) {
                    return (ServiceType)o;
                }
            }
            return null;
        }

        /** Find the
        * executor with requested name, among the executors registered to the
        * execution engine.
        * <P>
        * This should be used during (de-)serialization
        * of the specific executor for a data object: only store its name
        * and then try to find the executor later.
        *
        * @param name (display) name of executor to find
        * @return the desired executor or <code>null</code> if it does not exist
        */
        public ServiceType find (String name) {
            Enumeration en = services ();
            while (en.hasMoreElements ()) {
                ServiceType o = (ServiceType)en.nextElement ();
                if (name.equals (o.getName ())) {
                    return o;
                }
            }
            return null;
        }
    }


    /** Handle for an executor. This is a serializable class that should be used
    * to store executors and to recreate them after deserialization.
    */
    public static final class Handle extends Object
        implements java.io.Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 7233109534462148872L;

        /** name executor */
        private String name;
        /** name of class of the executor */
        private String className;
        /** kept ServiceType may be <tt>null</tt> after deserialization */
        private transient ServiceType serviceType;

        /** Create a new handle for an service.
        * @param ex the service to store a handle for
        */
        public Handle (ServiceType ex) {
            name = ex.getName ();
            className = ex.getClass ().getName ();
            serviceType = ex;
        }

        /** Find the service for this handle.
        * @return the reconstituted executor
        */
        public ServiceType getServiceType () {
            if (serviceType == null) {

                // try to find the executor by name
                TopManager tm = TopManager.getDefault ();
                ServiceType.Registry r = tm.getServices ();
                serviceType = r.find (name);
                if (serviceType != null) {
                    return serviceType;
                }

                // try to find it by class
                try {
                    serviceType = r.find (
                                      Class.forName (className, true, tm.systemClassLoader ())
                                  );
                } catch (ClassNotFoundException ex) {
                }
            }
            return serviceType;
        }

        /** Old compatibility version.
        */
        private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
            name = (String)ois.readObject ();
            className = (String)ois.readObject ();
        }

        /** Has also save the object.
        */
        private void writeObject (ObjectOutputStream oos) throws IOException {
            oos.writeObject (name);
            oos.writeObject (className);
        }
    }

}

/*
* Log
*  8    Gandalf   1.7         1/13/00  Ian Formanek    NOI18N
*  7    Gandalf   1.6         12/21/99 Jaroslav Tulach suid
*  6    Gandalf   1.5         12/21/99 Jaroslav Tulach serviceTypes r/w property
*  5    Gandalf   1.4         11/3/99  Jesse Glick     ServiceType.displayName 
*       <- BeanDescriptor.displayName.
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         10/1/99  Jesse Glick     Cleanup of service type 
*       name presentation.
*  2    Gandalf   1.1         10/1/99  Ales Novak      Handle keeps transient 
*       reference to its ServiceType
*  1    Gandalf   1.0         9/10/99  Jaroslav Tulach 
* $
*/