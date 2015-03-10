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

package org.openide.explorer.propertysheet;

import java.awt.Component;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.explorer.propertysheet.editors.NodePropertyEditor;
import org.openide.nodes.Node;

/**
* Manage a set of node properties from different nodes (but all the same name and type).
* Provides
* information about the properties, such as reading the property value and so on.
* If an application is interested in knowing when the <code>PropertyDetails</code> changes the value of a property,
* it can register itself as a listener for property change events by calling
* {@link PropertyDisplayer#addPropertyChangeListener}.
*
* @author Jan Jancura
* @version 0.18, Jan 23, 1998
*/
final class PropertyDetails extends Object {


    // static ..................................................................................

    /** Normal read access, i.e. for simple properties. */
    public static final int                     NORMAL = 1;
    /** Indexed property read access. */
    public static final int                     INDEXED = 2;
    /** Normal and/or indexed property read access. */
    public static final int                     BOTH = 3;

    static java.util.ResourceBundle             bundle =
        org.openide.util.NbBundle.getBundle (
            PropertyDetails.class
        );


    // variables ...............................................................................

    /** Set of PropertyDescriptors of this property (in each bean this property has
        another PropertyDescriptor). 
     * @associates Property*/
    private Vector                              property = new Vector (3, 3);

    /** nodes this property display value for */
    private Node[] nodes;

    private Node.Property                       firstProperty;

    private PropertyEditor                      propertyEditor = null;

    private boolean                             propertyEditorReaded = false;

    private PropertyEditor                      indexedPropertyEditor = null;

    private boolean                             indexedPropertyEditorReaded = false;

    private Class                               type;
    private Class                               elementType;


    // init ...............................................................................

    /**
    * Create new property details object with one property in it.
    *
    * @param aProperty the property
    */
    public PropertyDetails (Node[] nodes, Node.Property aProperty) {
        firstProperty = aProperty;
        property.addElement (aProperty);
        this.nodes = nodes;
    }


    // other methods .......................................................................

    /**
    * Add a new property to this set.
    * Must have the same name and value type as all the rest.
    * @param aProperty the property to add
    * @return <CODE>true</CODE> if it was really added; <code>false</code> if it was not (because it did not match the others)
    */
    public boolean addProperty (Node.Property aProperty) {
        if ((aProperty == null) || !aProperty.equals (firstProperty)) return false;
        property.addElement (aProperty);
        return true;
    }

    /** Getter for nodes attached to this property.
    */
    public Node[] getNodes () {
        return nodes;
    }

    /**
    * Get the name of contained properties.
    *
    * @return the name
    */
    public String getName () {
        return firstProperty.getDisplayName ();
    }

    /**
    * Test whether the property value type is an array, or other indexed property.
    *
    * @return <code>true</code> if so
    */
    boolean isArray () {
        return (firstProperty instanceof Node.IndexedProperty) || (getValueType ().isArray ());
    }

    //*******************************************************************************

    /**
    * Get the value of the <em>first</em> property.
    * The property must have a getter.
    *
    * @return value of the property
    */
    public Object getPropertyValue () throws Exception {
        return firstProperty.getValue ();
    }

    /**
    * Get the values of all properties.
    * All the properties must have getters.
    *
    * @return values of all the properties (in order of addition)
    */
    public Object[] getPropertyValues () throws Exception {
        int i, k = property.size ();
        Object[] o = new Object [k];
        for (i = 0; i < k; i++)
            o [i] = ((Node.Property)property.elementAt (i)).getValue ();
        return o;
    }

    /**
    * Get the value of the <em>first</em> property at some index.
    * This first property must be an array or indexed.
    *
    * @param index index of property to get
    * @return value of the first property at that index
    */
    public Object getPropertyValue (int index) throws Exception {
        if ( (firstProperty instanceof Node.IndexedProperty) &&
                ((Node.IndexedProperty)firstProperty).canIndexedRead ()
           )
            return ((Node.IndexedProperty)firstProperty).getIndexedValue (index);
        Object[] array = (Object[]) getPropertyValue ();
        return array [index];
    }

    /**
    * Set the property value (for all contained properties).
    *
    * @param value value to set
    */
    public void setPropertyValue (Object value) {
        int i, k = property.size ();
        Node.Property prop = null;
        for (i = 0; i < k; i++) {
            try {
                (prop = ((Node.Property)property.elementAt (i))).setValue (value);
            } catch (final Exception e) {
                notifyExceptionInSetter (e, prop.getDisplayName ());
            }
        }
    }

    /**
    * Set the property values (specifying individual values for each property).
    *
    * @param value values to set, in same order as properties were added
    */
    public void setPropertyValues (Object[] values) {
        int i, k = property.size ();
        Node.Property prop = null;
        for (i = 0; i < k; i++) {
            try {
                (prop = ((Node.Property)property.elementAt (i))).setValue (values [i]);
            } catch (Exception e) {
                notifyExceptionInSetter (e, prop.getDisplayName ());
            }
        }
    }

    /**
    * Set an indexed value (on all contained properties).
    * The properties must all have indexed setters.
    *
    * @param value the value to set
    * @param index the array index in each property to set the value at
    */
    public void setPropertyValue (Object value, int index) {
        int i, k = property.size ();
        Node.IndexedProperty prop = null;
        for (i = 0; i < k; i++) {
            try {
                (prop = ((Node.IndexedProperty)property.elementAt (i))).setIndexedValue (index, value);
            } catch (Exception e) {
                notifyExceptionInSetter (e, prop.getDisplayName ());
            }
        }
    }

    /**
    * Get the property editor (for the first property).
    *
    * @return the property editor, or <code>null</code> if it does not exist
    */
    public PropertyEditor getPropertyEditor () {
        if (propertyEditorReaded) return propertyEditor;
        propertyEditorReaded = true;
        return propertyEditor = getNewPropertyEditor ();
    }

    /**
    * Get the indexed property editor (for the first property).
    *
    * @return the property editor, or <code>null</code> if it does not exist
    */
    public PropertyEditor getIndexedPropertyEditor () {
        if (indexedPropertyEditorReaded) return indexedPropertyEditor;
        indexedPropertyEditorReaded = true;
        return indexedPropertyEditor = getNewIndexedPropertyEditor ();
    }

    /**
    * Get the property editor (for the first property), creating one afresh.
    *
    * @return the property editor, or <code>null</code> if it does not exist
    */
    public PropertyEditor getNewPropertyEditor () {
        PropertyEditor propertyEditor = firstProperty.getPropertyEditor ();
        if (propertyEditor instanceof NodePropertyEditor) {
            NodePropertyEditor np = (NodePropertyEditor)propertyEditor;
            np.attach (nodes);
        }

        if ((propertyEditor == null) && isArray ()) {
            PropertyEditor pe = getIndexedPropertyEditor ();
            if (pe == null) return null;
            return new IndexedPropertyEditor (this);
        }
        return propertyEditor;
    }

    /**
    * Get the indexed property editor (for the first property), creating one afresh.
    *
    * @return the property editor, or <code>null</code> if it does not exist
    */
    public PropertyEditor getNewIndexedPropertyEditor () {
        if (firstProperty instanceof Node.IndexedProperty)
            return ((Node.IndexedProperty)firstProperty).getIndexedPropertyEditor ();
        return findEditor (getIndexedValueType ());
    }

    /**
    * Test whether all properties are readable (normally or by index) and have the same value.
    * <p>Used to indicate whether it is reasonable to display a value representing all of them.
    *
    * @return <CODE>true</CODE> if so
    */
    public boolean canRead () throws Exception {
        return canRead (BOTH);
    }

    /**
    * Test whether all properties are readable and have the same value.
    *
    * @param type type of read access desired ({@link #NORMAL}, {@link #INDEXED}, or {@link #BOTH})
    * @return <CODE>true</CODE> if so
    */
    public boolean canRead (int type) throws Exception {
        if (getPropertyEditor () == null) return false;

        if (!canRead (firstProperty, type)) return false;
        int i, k = property.size ();
        if (k == 1) return true;
        Object value = firstProperty.getValue (), v;
        Node.Property prop;
        if (value == null)
            for (i = 1; i < k; i ++) {
                prop = (Node.Property)property.elementAt (i);
                if ((!canRead (prop, type)) || (prop.getValue () != null)) return false;
            }
        else
            for (i = 1; i < k; i ++) {
                prop = (Node.Property)property.elementAt (i);
                if ((!canRead (prop, type)) || ((v = prop.getValue ()) == null) ||
                        (!value.equals (v))) return false;
            }
        return true;
    }

    /**
    * Test whether at least one contained property may be set (normally or by index).
    * <p>Indicates whether it is useful to provide a UI to set the property across all the nodes.
    * @return <CODE>true</CODE> if so
    */
    public boolean canWrite () {
        return canWrite (BOTH);
    }

    /**
    * Test whether at least one contained property may be set.
    * @param type type of write access desired ({@link #NORMAL}, {@link #INDEXED}, or {@link #BOTH})
    * @return <CODE>true</CODE> if so
    */
    public boolean canWrite (int type) {
        if (getPropertyEditor () == null) return false;
        int i, k = property.size ();
        for (i = 0; i < k; i++)
            if (canWrite ((Node.Property) property.elementAt (i), type)) return true;
        return false;
    }

    /**
    * Retruns true if property p has getter or indexed getter.
    */
    private static boolean canRead (Node.Property p, int type) {
        if (p instanceof Node.IndexedProperty) {
            Node.IndexedProperty ip = (Node.IndexedProperty)p;
            return (((type & NORMAL) != 0) && ip.canRead ()) ||
                   (((type & INDEXED) != 0) && ip.canIndexedRead ());
        }
        return ((type & NORMAL) != 0) && p.canRead ();
    }

    /**
    * Returns true if property has setter or indexed setter.
    */
    private static boolean canWrite (Node.Property p, int type) {
        if (p instanceof Node.IndexedProperty) {
            Node.IndexedProperty ip = (Node.IndexedProperty)p;
            return (((type & NORMAL) != 0) && ip.canWrite ()) ||
                   (((type & INDEXED) != 0) && ip.canIndexedWrite ());
        }
        return ((type & NORMAL) != 0) && p.canWrite ();
    }

    /**
    * Test whether the properties are editable.
    *
    * @return <CODE>true</CODE> if some properties are writable or if there is a custom editor
    */
    public boolean canEdit () {
        return canWrite () || hasCustomEditor ();
    }

    /**
    * Test whether all contained properties support a default value.
    * @return <code>true</code> if so
    */
    public boolean supportsDefaultValue () {
        int i, k = property.size ();
        for (i = 0; i < k; i++)
            if (!((Node.Property) property.elementAt (i)).supportsDefaultValue ()
               ) return false;
        return true;
    }

    /**
    * Restore the default value for all properties.
    */
    public void restoreDefaultValue () {
        int i, k = property.size ();
        Node.Property prop = null;
        for (i = 0; i < k; i++) {
            try {
                (prop = (Node.Property) property.elementAt (i)).restoreDefaultValue ();
            } catch (Exception e) {
                notifyExceptionInSetter (e, prop.getDisplayName ());
            }
        }
    }

    /**
    * Test whether all contained properties are expert.
    *
    * @return true if so
    */
    public boolean isExpert () {
        //    int i, k = property.size ();
        //    for (i = 0; i < k; i++)
        //      if (!((Node.Property) property.elementAt (i)).isExpert ())
        //        return false;
        return false;
    }

    /**
    * Get the short description of the contained properties.
    *
    * @return the short description for all properties if they have one and they all match
    */
    public String getShortDescription () {
        String s = firstProperty.getShortDescription ();
        int i, k = property.size ();
        for (i = 1; i < k; i++)
            if (!((Node.Property) property.elementAt (i)).getShortDescription ().equals (s))
                return null;
        return s;
    }

    /**
    * Test whether the first property has a custom editor.
    *
    * @return <CODE>true</CODE> if so
    */
    public boolean hasCustomEditor () {
        PropertyEditor propertyEditor = getPropertyEditor ();
        if (propertyEditor == null) return false;
        return propertyEditor.supportsCustomEditor ();
    }

    /**
    * Get the custom property editor for the first property.
    *
    * @return the customizer or <code>null</code> if none
    */
    public Component getPropertyCustomEditor () {
        PropertyEditor propertyEditor = getPropertyEditor ();
        if (propertyEditor == null) return null;
        if (!propertyEditor.supportsCustomEditor ()) return null;
        return propertyEditor.getCustomEditor();
    }

    /**
    * Get the custom property editor from any regular property editor.
    *
    * @param propertyEditor editor which will be asked for custom editor
    * @return the custom property editor, or <CODE>null</CODE> if that is not supported
    */
    public static Component getPropertyCustomEditor (PropertyEditor propertyEditor) {
        if (!propertyEditor.supportsCustomEditor ()) return null;
        return propertyEditor.getCustomEditor ();
    }

    /**
    * Get the property type for all properties.
    *
    * @return the type
    */
    public Class getValueType () {
        if (type != null) return type;

        // Indexed properties without nonindexed access return null on getPropertyType
        // this patch creates array type or them
        type = firstProperty.getValueType ();
        try {
            if (type == null) {
                // obtains array type [getIndexValueType ()]
                type = java.lang.reflect.Array.newInstance (getIndexedValueType (), 0).getClass ();
            }
        } catch (Exception e) {
            return type = Object[].class;
        }
        return type;
    }

    /**
    * Get the element type for all properties.
    *
    * @return the element type, or <code>null</code> if the properties are not arrays or indexed
    */
    public Class getIndexedValueType () {
        if (elementType != null) return elementType;
        return elementType = (firstProperty instanceof Node.IndexedProperty) ?
                             ((Node.IndexedProperty) firstProperty).getElementType () :
                             getValueType ().getComponentType ();
    }

    /** Finds editor for given class. Registers the property editor
    * if necessary.
    *
    * @param clazz the class
    * @return the property editor or null
    */
    private PropertyEditor findEditor (Class clazz) {
        PropertyEditor p = PropertyEditorManager.findEditor (clazz);
        if (p instanceof NodePropertyEditor) {
            NodePropertyEditor np = (NodePropertyEditor)p;
            np.attach (nodes);
        }
        return p;
    }

    void notifyExceptionInSetter (Exception e, String propertyName) {
        TopManager.getDefault ().notifyException (
            new ExceptionHack (e, propertyName)
        );
    }


    // innerclasses ...........................................................................

    /**
    * Hack for using Exception dialog with Details button.
    */
    class ExceptionHack extends Exception {

        /** Original exception. */
        private Throwable t;
        /** Localized text. */
        private String text;

        ExceptionHack (Throwable t, String propertyName) {
            super (""); // NOI18N
            this.t = t;

            // JST: this can be improved in future... // HANZ: See NotifyException
            boolean isLocalized = false;
            if ( (t.getLocalizedMessage () != null) &&
                    (!t.getLocalizedMessage ().equals (t.getMessage ()))
               )
                isLocalized = true;

            if (isLocalized)
                text = new MessageFormat (
                           bundle.getString ("EXC_Setter_localized")
                       ).format (new Object[] {
                                     propertyName,
                                     t.getLocalizedMessage ()
                                 });
            else
                text = new MessageFormat (
                           bundle.getString ("EXC_Setter")
                       ).format (new Object[] {
                                     propertyName
                                 });
        }

        public String getLocalizedMessage () {
            return text;
        }

        public void printStackTrace (java.io.PrintStream s) {
            t.printStackTrace (s);
        }

        public void printStackTrace (java.io.PrintWriter s) {
            t.printStackTrace (s);
        }
    }
}

/*
 * Log
 *  15   Gandalf   1.14        1/12/00  Ian Formanek    NOI18N
 *  14   Gandalf   1.13        1/10/00  Jan Jancura     Bug getLocMessage == 
 *       null
 *  13   Gandalf   1.12        1/5/00   Jan Jancura     NotifyException (with 
 *       Details button)  used for exceptions in setter (Yarda!).
 *  12   Gandalf   1.11        12/10/99 Jan Jancura     Localisation improved
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         9/15/99  Jaroslav Tulach More private things & 
 *       support for default property.
 *  9    Gandalf   1.8         6/30/99  Ian Formanek    Reflecting package 
 *       change of NodePropertYEditor
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         6/3/99   Jaroslav Tulach NodePropertyEditor & 
 *       NodeCustomizer
 *  6    Gandalf   1.5         3/26/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         3/20/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/20/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/4/99   Jan Jancura     Localization moved
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
