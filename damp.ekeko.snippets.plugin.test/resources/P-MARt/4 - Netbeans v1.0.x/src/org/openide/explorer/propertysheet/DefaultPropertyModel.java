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

import java.beans.*;
import java.lang.reflect.*;

import org.openide.util.WeakListener;

/** The default implementation of PropertyModel interface.
* It takes the bean instance and the property name which should
* be accessed through PropertyModel methods.
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public class DefaultPropertyModel extends Object implements PropertyModel, PropertyChangeListener {
    /** The Java Bean */
    private Object bean;

    /** Name of the property of the bean. */
    private String propertyName;

    /** support for the properties changes. */
    private PropertyChangeSupport support;

    /** Beaninfo for the bean */
    private BeanInfo beanInfo;

    /** Property descriptor for the bean. */
    private PropertyDescriptor prop;

    /** Read method if exists one. */
    private Method readMethod;

    /** Write method if exists one. */
    private Method writeMethod;

    /** Type of the property. */
    private Class propertyTypeClass;

    /** Class of the PropertyEditor. */
    private Class editorClass;

    /** Creates new DefaultPropertyModel.
    * @param bean the java bean to be introspected
    * @param propertyName name of the property
    * 
    * @exception IllegalArgumentException if there is any problem
    *      with the parameters (introspection of bean,...)
    */
    public DefaultPropertyModel(Object bean, String propertyName) throws IllegalArgumentException {
        this.bean = bean;
        this.propertyName = propertyName;
        support = new PropertyChangeSupport(this);

        try {
            Method addList = bean.getClass().getMethod("addPropertyChangeListener", new Class[] { PropertyChangeListener.class }); // NOI18N

            addList.invoke(bean, new Object[] {
                               WeakListener.propertyChange(this, bean)
                           });

            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] descr = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < descr.length; i++) {
                if (descr[i].getName().equals(propertyName)) {
                    prop = descr[i];
                    propertyTypeClass = prop.getPropertyType();
                    readMethod = prop.getReadMethod();
                    writeMethod = prop.getWriteMethod();
                    editorClass = prop.getPropertyEditorClass();
                    break;
                }
            }
        }
        catch (Exception e) {
            if (Boolean.getBoolean("org.netbeans.exceptions")) // NOI18N
                e.printStackTrace();
        }

        if (prop == null)
            throw new IllegalArgumentException();
    }

    /**
     * @return the class of the property.
     */
    public Class getPropertyType() {
        return propertyTypeClass;
    }

    /** Getter for current value of a property.
     */
    public Object getValue() throws InvocationTargetException {
        try {
            return (readMethod == null) ?
                   null :
                   readMethod.invoke(bean, new Object[] {});
        }
        catch (IllegalAccessException e) {
            if (Boolean.getBoolean("org.netbeans.exceptions")) // NOI18N
                e.printStackTrace();
            throw new InvocationTargetException(e);
        }
    }

    /** Setter for a value of a property.
     * @param v the value
     * @exeception InvocationTargetException
     */
    public void setValue(Object v) throws InvocationTargetException {
        try {
            if (writeMethod != null)
                writeMethod.invoke(bean, new Object[] {v});
        }
        catch (IllegalAccessException e) {
            if (Boolean.getBoolean("org.netbeans.exceptions")) // NOI18N
                e.printStackTrace();
            throw new InvocationTargetException(e);
        }
    }

    /** Adds listener to change of the value.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    /** Removes listener to change of the value.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    /** Implementation of PropertyChangeListener method */
    public void propertyChange(PropertyChangeEvent evt) {
        if (propertyName.equals(evt.getPropertyName())) {
            support.firePropertyChange(PROP_VALUE, evt.getOldValue(), evt.getNewValue());
        }
    }

    /** The class of the property editor or <CODE>null</CODE>
     * if default property editor should be used.
     */
    public Class getPropertyEditorClass() {
        return editorClass;
    }
}

/*
* Log
*  2    Gandalf   1.1         1/12/00  Ian Formanek    NOI18N
*  1    Gandalf   1.0         11/25/99 Petr Hamernik   
* $
*/

