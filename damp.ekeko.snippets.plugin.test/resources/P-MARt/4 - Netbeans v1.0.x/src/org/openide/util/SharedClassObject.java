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

package org.openide.util;

import java.lang.ref.WeakReference;
import java.beans.*;
import java.io.*;

import java.util.Map;
import java.util.HashMap;

/** Shared object that allows different instances of the same class
* to share common data.
* <p>The data are shared only between instances of the same class (not subclasses).
* Thus, such "variables" have neither instance nor static behavior.
*
* @author Ian Formanek, Jaroslav Tulach
*/
public abstract class SharedClassObject extends Object
    implements Externalizable {
    /** serialVersionUID */
    static final long serialVersionUID = 4527891234589143259L;

    /** property change support (PropertyChangeSupport) */
    private static final Object PROP_SUPPORT = new Object ();

    /** Map (Class, DataEntry) that maps Classes to maps of any objects 
     * @associates DataEntry*/
    private static HashMap values = new HashMap (4);

    /** data entry for this class */
    private DataEntry dataEntry;

    /** hard reference to primary instance of this class
    * This is here not to allow the finalization till at least
    * one object exists
    */
    private SharedClassObject first;

    /** Create a shared object.
    * Typically shared-class constructors should not take parameters, since there
    * will conventionally be no instance variables.
    */
    protected SharedClassObject () {
        dataEntry = getDataEntry (this);
    }

    /* Calls a referenceLost to decrease the counter on the shared data.
    * This method is final so no descendant can override it, but
    * it calls the method unreferenced() that can be overriden to perform any
    * additional tasks on finalizing.
    */
    protected final void finalize() throws Throwable {
        referenceLost ();
    }

    /** Indicate whether the shared data of the last existing instance of this class
    * should be cleared when that instance is finalized.
    *
    * Subclasses may perform additional tasks
    * on finalization if desired. This method should be overridden
    * in lieu of {@link #finalize}.
    * <p>The default implementation returns <code>true</code>.
    * Classes which have precious shared data may want to return <code>false</code>, so that
    * all instances may be finalized, after which new instances will pick up the same shared variables
    * without requiring a recalculation.
    *
    * @return <code>true</code> if all shared data should be cleared,
    *   <code>false</code> if it should stay in memory
    */
    protected boolean clearSharedData () {
        return true;
    }

    /** Test whether the classes of the compared objects are the same.
    * @param obj the object to compare to
    * @return <code>true</code> if the classes are equal
    */
    public final boolean equals (Object obj) {
        return ((obj instanceof SharedClassObject) && (getClass().equals(obj.getClass())));
    }

    /** Get a hashcode of the shared class.
    * @return the hash code
    */
    public final int hashCode () {
        return getClass().hashCode();
    }

    /** Obtain lock for synchronization on manipulation with this
    * class.
    * Can be used by subclasses when performing nonatomic writes, e.g.
    * @return an arbitrary synchronizable lock object
    */
    protected final Object getLock () {
        return getClass ().toString ().intern ();
    }

    /** Obtains data entry for this class.
    * @param obj the requestor
    * @return the data entry object
    */
    private DataEntry getDataEntry (SharedClassObject obj) {
        synchronized (getLock ()) {
            DataEntry de = (DataEntry)values.get (getClass ());
            if (de == null) {
                de = new DataEntry ();
                values.put (getClass (), de);
            }
            de.increase();
            // finds reference for the first object of the class
            obj.first = de.first (obj);
            return de;
        }
    }

    /** Should be called from within a finalize method to manage references
    * to the shared data (when the last reference is lost, the object is
    * removed)
    */
    private void referenceLost() {
        /*System.out.println ("Lock: " + getLock());
        System.out.println ("  DataEntry: " + dataEntry);
        System.out.println ("Values: " + (values==null));*/ // NOI18N
        synchronized (getLock ()) {
            if (dataEntry == null || dataEntry.decrease() == 0) {
                if (clearSharedData ()) {
                    // clears the data
                    values.remove (getClass());
                }
            }
        }
    }

    /** Set a shared variable.
    * Automatically {@link #getLock locks}.
    * @param key name of the property
    * @param value value for that property (may be null)
    * @return the previous value assigned to the property, or <code>null</code> if none
    */
    protected final Object putProperty (Object key, Object value) {
        synchronized (getLock ()) {
            return dataEntry.getMap (this).put (key, value);
        }
    }

    /** Set a shared variable available only for string names.
    * Automatically {@link #getLock locks}. 
    * @param key name of the property
    * @param value value for that property (may be null)
    * @param notify should all listeners be notified about property change?
    * @return the previous value assigned to the property, or <code>null</code> if none
    */
    protected final Object putProperty (String key, Object value, boolean notify) {
        Object previous = putProperty (key, value);

        if (notify) {
            firePropertyChange (key, previous, value);
        }

        return previous;
    }

    /** Get a shared variable.
    * Automatically {@link #getLock locks}.
    * @param key name of the property
    * @return value of the property, or <code>null</code> if none
    */
    protected final Object getProperty (Object key) {
        synchronized (getLock ()) {
            return dataEntry.getMap (this).get (key);
        }
    }


    /** Initialize shared state.
    * Should use {@link #putProperty} to set up variables.
    * Subclasses should always call the super method.
    * <p>This method need <em>not</em> be called explicitly; it will be called once
    * the first time a given shared class is used (not for each instance!).
    */
    protected void initialize () {
    }


    /* Adds the specified property change listener to receive property
     * change events from this action.
     * @param         l the property change listener.
     * @see           java.beans.PropertyChangeListener
     * @see           #removePropertyChangeListener
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (getLock ()) {
            //      System.out.println ("added listener: " + l + " to: " + getClass ()); // NOI18N
            PropertyChangeSupport supp = (PropertyChangeSupport)getProperty (PROP_SUPPORT);
            if (supp == null) {
                //        System.out.println ("Creating support"); // NOI18N
                putProperty (PROP_SUPPORT, supp = new PropertyChangeSupport (this));
            }
            boolean noListener = !supp.hasListeners (null);
            supp.addPropertyChangeListener(l);
            if (noListener) {
                // added first listener
                //        Thread.dumpStack ();
                //        System.out.println ("added first listener to: " + getClass ()); // NOI18N
                addNotify ();
            }
        }
    }

    /*
     * Removes the specified property change listener so that it
     * no longer receives property change events from this action.
     * @param         l     the property change listener.
     * @see           java.beans.PropertyChangeListener
     * @see           #addPropertyChangeListener
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        synchronized (getLock ()) {
            PropertyChangeSupport supp = (PropertyChangeSupport)getProperty (PROP_SUPPORT);
            if (supp == null) return;

            boolean hasListener = supp.hasListeners (null);
            supp.removePropertyChangeListener(l);
            if (hasListener && !supp.hasListeners (null)) {
                //      System.out.println ("removed all listeners to: " + getClass ()); // NOI18N
                removeNotify ();
            }
        }
    }

    /** Notify subclasses that the first listener has been added to this action.
    * The default implementation does nothing.
    */
    protected void addNotify () {
    }

    /** Notify subclasses that the last listener has been removed from this action.
    * The default implementation does nothing.
    */
    protected void removeNotify () {
    }

    /** Fire a property change event to all listeners.
    * @param name the name of the property
    * @param oldValue the old value
    * @param newValue the new value
    */
    protected void firePropertyChange (
        String name, Object oldValue, Object newValue
    ) {
        PropertyChangeSupport supp = (PropertyChangeSupport)getProperty (PROP_SUPPORT);
        if (supp != null)
            supp.firePropertyChange (name, oldValue, newValue);
    }

    /** Writes nothing to the stream.
    * @param oo ignored
    */
    public void writeExternal (ObjectOutput oo) throws IOException {
    }

    /** Reads nothing from the stream.
    * @param oi ignored
    */
    public void readExternal (ObjectInput oi)
    throws IOException, ClassNotFoundException {
    }

    /** This method provides correct handling of serialization and deserialization.
    * When serialized the method writeExternal is used to store the state.
    * When deserialized first an instance is located by a call to findObject (clazz, true)
    * and then a method readExternal is called to read its state from stream.
    * <P>
    * This allows to have only one instance of the class in the system and work
    * only with it.
    *
    * @return write replace object that handles the described serialization/deserialization process
    */
    protected Object writeReplace () {
        return new WriteReplace (this);
    }


    /** Obtain an instance of the desired class, if there is one.
    * @param clazz the shared class to look for
    * @return the instance, or <code>null</code> if such does not exists
    */
    public static SharedClassObject findObject (Class clazz) {
        return findObject (clazz, false);
    }

    /** Find an existing object, possibly creating a new one as needed.
    * To create a new instance the class must be public and have a public
    * default constructor.
    *
    * @param clazz the class of the object to find (must extend <code>SharedClassObject</code>)
    * @param create <code>true</code> if the object should be created if it does not yet exist
    * @return an instance, or <code>null</code> if there was none and <code>create</code> was <code>false</code>
    * @exception IllegalArgumentException if a new instance could not be created for some reason
    */
    public static SharedClassObject findObject (Class clazz, boolean create) {
        DataEntry de = (DataEntry)values.get (clazz);
        // either null or the object
        SharedClassObject obj = de == null ? null : de.get ();

        if (obj == null && create) {
            // try to create new instance
            try {
                obj = (SharedClassObject) clazz.newInstance ();
            } catch (Exception ex) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace ();
                // cannot create the instance or it is not SharedClassObject
                throw new IllegalArgumentException (ex.getMessage ());
            }
        }
        return obj;
    }

    /** Class that is used as default write replace.
    */
    static final class WriteReplace extends Object implements Serializable {
        /** serialVersionUID */
        static final long serialVersionUID = 1327893248974327640L;

        /** the class  */
        private Class clazz;
        /** shared instance */
        private transient SharedClassObject object;

        /** Constructor.
        * @param the instance
        */
        public WriteReplace (SharedClassObject object) {
            this.object = object;
            this.clazz = object.getClass ();
        }

        /** Write object.
        */
        private void writeObject (ObjectOutputStream oos) throws IOException {
            oos.defaultWriteObject ();

            object.writeExternal (oos);
        }

        /** Read object.
        */
        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            ois.defaultReadObject ();
            object = findObject (clazz, true);

            object.readExternal (ois);
        }

        /** Read resolve to the read object.
        */
        private Object readResolve () {
            return object;
        }
    }

    /** The inner class that encapsulates the shared data together with
    * a reference counter
    */
    static final class DataEntry extends Object {
        /** The data */
        private HashMap map;
        /** The reference counter */
        private int count = 0;
        /** weak reference to an object of this class */
        private WeakReference ref = new WeakReference (null);

        /** Returns the data
        * @param obj the requestor object
        * @return the data
        */
        Map getMap (SharedClassObject obj) {
            if (map == null) {
                // to signal invalid state
                map = new HashMap ();

                // no data for this class yet
                obj.initialize ();
            }
            return map;
        }


        /** Increases the counter (thread safe)
        * @return new counter value
        */
        int increase () {
            return ++count;
        }

        /** Dereases the counter (thread safe)
        * @return new counter value
        */
        int decrease () {
            return --count;
        }

        /** Request for first object. If there is none, use the requestor
        * @param obj requestor
        * @return the an object of this type
        */
        SharedClassObject first (SharedClassObject obj) {
            SharedClassObject s = (SharedClassObject)ref.get ();
            if (s == null) {
                ref = new WeakReference (obj);
                return obj;
            } else {
                return s;
            }
        }

        /** @return shared object or null
        */
        public SharedClassObject get () {
            return (SharedClassObject)ref.get ();
        }
    }
}


/*
 * Log
 *  11   Gandalf   1.10        1/13/00  Ian Formanek    NOI18N
 *  10   Gandalf   1.9         1/12/00  Pavel Buzek     I18N
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/4/99  Jesse Glick     Better exception 
 *       reporting.
 *  7    Gandalf   1.6         9/30/99  Jaroslav Tulach DataLoader is now 
 *       serializable.
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         4/26/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/4/99   Jaroslav Tulach API cleaning
 *  3    Gandalf   1.2         2/19/99  David Simonek   menu related changes...
 *  2    Gandalf   1.1         2/8/99   Jaroslav Tulach HashMap with smaller 
 *       size
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.33        --/--/98 Jaroslav Tulach minimal class
 */
