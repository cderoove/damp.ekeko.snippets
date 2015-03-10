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

package org.openide.options;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

/** Base class for all system options.
* Provides methods for adding
* and working with property change and guarantees
* that all instances of the same class will share these listeners.
* <P>
* When a new option is created, it should subclass
* <CODE>SystemOption</CODE>, add <em>static</em> variables to it that will hold
* the values of properties, and write non-static setters/getters that will
* notify all listeners about property changes via
* {@link #firePropertyChange}.
* <p>JavaBeans introspection is used to find the properties,
* so it is possible to use {@link BeanInfo}.
*
* @author Jaroslav Tulach
*/
public abstract class SystemOption extends SharedClassObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 558589201969066966L;

    /** property to indicate that the option is currently loading its data */
    private static final Object PROP_LOADING = new Object ();
    /** property to indicate that the option is currently loading its data */
    private static final Object PROP_STORING = new Object ();

    /** Default constructor. */
    public SystemOption() {}

    /** Fire a property change event to all listeners. Delays
    * this loading when readExternal is active till it finishes.
    *
    * @param name the name of the property
    * @param oldValue the old value
    * @param newValue the new value
    */
    protected void firePropertyChange (
        String name, Object oldValue, Object newValue
    ) {
        if (getProperty (PROP_LOADING) != null) {
            // somebody is loading, assign any object different than
            // this to indicate that firing should occure
            putProperty (PROP_LOADING, PROP_LOADING);
            // but do not fire the change now
            return;
        }
        super.firePropertyChange (name, oldValue, newValue);
    }

    /** Write all properties of this object (or subclasses) to an object output.
    * @param out the output stream
    * @exception IOException on error
    */
    public void writeExternal (ObjectOutput out) throws IOException {
        try {
            // gets info about all properties that were added by subclass
            BeanInfo info = org.openide.util.Utilities.getBeanInfo (getClass (), SystemOption.class);
            PropertyDescriptor[] desc = info.getPropertyDescriptors ();

            putProperty (PROP_STORING, this);

            Object[] param = new Object[0];
            synchronized (getLock ()) {
                // write all properties that have getter to stream
                for (int i = 0; i < desc.length; i++) {
                    String propName = desc[i].getName();
                    Object value = getProperty(propName);
                    boolean fromRead;
                    if (value == null) {
                        fromRead = true;
                        Method read = desc[i].getReadMethod();
                        if (read != null) {
                            try {
                                value = read.invoke (this, param);
                            } catch (InvocationTargetException ex) {
                                // exception thrown
                                throw new IOException (new MessageFormat (NbBundle.getBundle (SystemOption.class).getString ("EXC_InGetter")).
                                                       format (new Object[] {getClass (), desc[i].getName ()})
                                                      );
                            } catch (IllegalAccessException ex) {
                                // exception thrown
                                throw new IOException (new MessageFormat (NbBundle.getBundle (SystemOption.class).getString ("EXC_InGetter")).
                                                       format (new Object[] {getClass (), desc[i].getName ()})
                                                      );
                            }
                        }
                    } else {
                        fromRead = false;
                    }
                    // writes name of the property
                    out.writeObject (propName);
                    // writes its value
                    out.writeObject (value);
                    // from getter or stored prop?
                    out.writeObject(fromRead ? Boolean.TRUE : Boolean.FALSE);
                }
            }
        } catch (IntrospectionException ex) {
            // if we cannot found any info about properties
        } finally {
            putProperty (PROP_STORING, null);
        }
        // write null to signal end of properties
        out.writeObject (null);
    }

    /** Read all properties of this object (or subclasses) from an object input.
    * If there is a problem setting the value of any property, that property will be ignored;
    * other properties should still be set.
    * @param in the input stream
    * @exception IOException on error
    * @exception ClassNotFound if a class used to restore the system option is not found
    */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        synchronized (getLock ()) {
            // hashtable that maps names of properties to setter methods
            HashMap map = new HashMap ();

            try {
                // indicate that we are loading files
                putProperty (PROP_LOADING, this);

                try {
                    // gets info about all properties that were added by subclass
                    BeanInfo info = org.openide.util.Utilities.getBeanInfo (getClass (), SystemOption.class);
                    PropertyDescriptor[] desc = info.getPropertyDescriptors ();

                    // write all properties that have getter to stream
                    for (int i = 0; i < desc.length; i++) {
                        Method m = desc[i].getWriteMethod ();
                        /*if (m == null) {
                          System.out.println ("HOW HOW HOW HOWHOWHOWHOWHWO: " + desc[i].getName() + " XXX " + getClass());
                          throw new IOException (new MessageFormat (NbBundle.getBundle (SystemOption.class).getString ("EXC_InSetter")).
                            format (new Object[] {getClass (), desc[i].getName ()})
                                                );
                    } */
                        map.put (desc[i].getName (), m );
                    }
                } catch (IntrospectionException ex) {
                    // if we cannot found any info about properties
                    // leave the hashtable empty and only read stream till null is found
                }

                String preread = null;
                do {
                    // read the name of property
                    String name;
                    if (preread != null) {
                        name = preread;
                        preread = null;
                    } else {
                        name = (String)in.readObject();
                    }

                    // break if the end of property stream is found
                    if (name == null) break;

                    // read the value of property
                    Object value = in.readObject ();

                    // read flag - use the setter method or store as property?
                    Object useMethodObject = in.readObject();
                    boolean useMethod;
                    boolean nullRead = false; // this should be last processed property?
                    if (useMethodObject == null) {
                        useMethod = true;
                        nullRead = true;
                    } else if (useMethodObject instanceof String) {
                        useMethod = true;
                        preread = (String) useMethodObject;
                    } else {
                        useMethod = ((Boolean) useMethodObject).booleanValue();
                    }

                    if (useMethod) {

                        // set the value
                        Method write = (Method)map.get (name);
                        if (write != null) {
                            // if you have where to set the value
                            try {
                                write.invoke (this, new Object[] { value });
                            } catch (InvocationTargetException ex) {
                            } catch (IllegalAccessException ex) {
                            } catch (IllegalArgumentException ex) {
                            }
                        }
                    } else {
                        putProperty(name, value, false);
                    }

                    if (nullRead) {
                        break;
                    }

                } while (true);
            } finally {
                // get current state
                if (this != getProperty (PROP_LOADING)) {
                    // some changes should be fired
                    // loading finished
                    putProperty (PROP_LOADING, null);
                    firePropertyChange (null, null, null);
                } else {
                    // loading finished
                    putProperty (PROP_LOADING, null);
                }
            }
        }
    }

    /**
    * Get the name of this system option.
    * The default implementation just uses the {@link #displayName display name}.
    * @return the name
    */
    public final String getName () {
        return displayName ();
    }

    /**
    * Get the display name of this system option.
    * @return the display name
    */
    public abstract String displayName ();

    /** Get context help for this system option.
    * @return context help
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (SystemOption.class);
    }

    /** Allows subclasses to test whether the change of a property
    * is invoked from readExternal method or by external change invoked
    * by any other program.
    *
    * @return true if the readExternal method is in progress
    */
    protected final boolean isReadExternal () {
        return getProperty (PROP_LOADING) != null;
    }

    /** Allows subclasses to test whether the getter of a property
    * is invoked from writeExternal method or by any other part of the program.
    *
    * @return true if the writeExternal method is in progress
    */
    protected final boolean isWriteExternal () {
        return getProperty (PROP_STORING) != null;
    }

}

/*
 * Log
 *  16   Gandalf   1.15        12/23/99 Ales Novak      #5107
 *  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        10/4/99  Jaroslav Tulach Has also 
 *       isWriteExternal.
 *  13   Gandalf   1.12        9/30/99  Jaroslav Tulach DataLoader is now 
 *       serializable.
 *  12   Gandalf   1.11        6/30/99  Jesse Glick     Context help.
 *  11   Gandalf   1.10        6/22/99  Jaroslav Tulach Does not fire change 
 *       when loading properties.
 *  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         5/12/99  Ales Novak      options can be RD_ONLY
 *  8    Gandalf   1.7         4/20/99  Jesse Glick     Made firePropertyChange 
 *       final again--there should be no reason to override it, that would 
 *       probably just be a confusing option.
 *  7    Gandalf   1.6         4/20/99  Jesse Glick     SystemOption.firePropertyChange
 *        is now protected and unfinal.
 *  6    Gandalf   1.5         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  5    Gandalf   1.4         3/22/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/8/99   Jaroslav Tulach Bundles.
 *  3    Gandalf   1.2         3/4/99   Jan Jancura     
 *  2    Gandalf   1.1         3/4/99   Petr Hamernik   
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Jancura     getter for mane of options.
 *  0    Tuborg    0.12        --/--/98 Jaroslav Tulach changed fired exception
 */
