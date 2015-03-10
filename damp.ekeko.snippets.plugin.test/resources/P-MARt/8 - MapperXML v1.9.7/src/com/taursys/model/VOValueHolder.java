/**
 * VOValueHolder - ValueHolder which stores value in a ValueObject(JavaBean)
 *
 * Copyright (c) 2002
 *      Marty Phelan, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package com.taursys.model;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import com.taursys.model.event.ContentValueChangeEvent;
import com.taursys.model.event.ContentChangeEvent;
import com.taursys.model.event.StructureChangeEvent;
import java.beans.*;
import javax.swing.event.*;

/**
 * This is an implementation of ValueHolder which stores value in a ValueObject(JavaBean).
 * This ValueHolder can be shared by multiple models and can access all/any
 * properties of the ValueObject/JavaBean.  For each property accessed, this
 * object creates and uses a PropertyAccessor to manage the access process.
 * For performance benefits, this class caches the PropertyAccessors after
 * they have been created and reuses them for subsequent invocations.
 */
public class VOValueHolder extends ObjectValueHolder
    implements PropertyChangeListener {
  private HashMap properties = new HashMap();
  private Class valueObjectClass;
  private boolean changingPropertyValue = false;

  /**
   * Constructs a new VOValueHolder.
   */
  public VOValueHolder() {
  }

  // ***********************************************************************
  // *               PROPERTY VALUE ACCESSOR METHODS (PUBLIC)
  // ***********************************************************************

  /**
   * Returns the value of the given property in the obj.
   */
  public Object getPropertyValue(String propertyName) throws ModelException {
    return getPropertyAccessor(propertyName).getPropertyValue(obj);
  }

  /**
   * Set the value for the given property in the obj.
   * Fires a StateChanged event to any listeners.
   */
  public void setPropertyValue(String propertyName, Object value) throws ModelException {
    changingPropertyValue = true;
    try {
      getPropertyAccessor(propertyName).setPropertyValue(obj, value);
    } finally {
      changingPropertyValue = false;
    }
    if (!isMultiplePropertiesChanging())
      fireStateChanged(
          new ContentValueChangeEvent(this, propertyName, null, value));
  }

  // ***********************************************************************
  // *               MISC ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Returns the java data type for the given property
   */
  public int getJavaDataType(String propertyName) throws ModelException {
    return getPropertyAccessor(propertyName).getJavaDateType();
  }

  // ***********************************************************************
  // *                    VALUE OBJECT ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Set the internal object for this holder.  Also registers
   * this ValueHolder as a bound property change listener if the internal
   * object implements the BoundValueObject interface. Finally it fires a
   * ContentChangeEvent to all ChangeListeners.
   * @param obj the internal object for this holder.
   */
  public void setObject(Object obj) {
    super.setObject(obj);
  }

  /**
   * Set the internal object for this holder.  Also registers
   * this ValueHolder as a bound property change listener if the internal
   * object implements the BoundValueObject interface. Finally it fires the
   * given ChangeEvent to all ChangeListeners (if it is not null).
   * @param obj the internal object for this holder.
   * @param e ChangeEvent to fire to all ChangeListeners (if not null)
   */
  public void setObject(Object obj, ChangeEvent e) {
    if (this.obj != null && this.obj instanceof BoundValueObject)
      ((BoundValueObject)this.obj).removePropertyChangeListener(this);
    this.obj = obj;
    if (obj != null && obj instanceof BoundValueObject)
      ((BoundValueObject)obj).addPropertyChangeListener(this);
    if (e != null)
      fireStateChanged(e);
  }

  /**
   * Set the internal object for this holder.  Also registers
   * this ValueHolder as a bound property change listener if the internal
   * object implements the BoundValueObject interface. Finally it fires a
   * ContentChangeEvent to all ChangeListeners.
   * @param obj the internal object for this holder.
   */
  public void setValueObject(Object obj) {
    setObject(obj);
  }

  /**
   * Set the internal object for this holder.  Also registers
   * this ValueHolder as a bound property change listener if the internal
   * object implements the BoundValueObject interface. Finally it fires the
   * given ChangeEvent to all ChangeListeners (if it is not null).
   * @param obj the internal object for this holder.
   * @param e ChangeEvent to fire to all ChangeListeners (if not null)
   */
  public void setValueObject(Object obj, ChangeEvent e) {
    setObject(obj, e);
  }

  /**
   * Get the internal object for this holder.
   * @return the internal object for this holder.
   */
  public Object getValueObject() {
    return obj;
  }

  /**
   * Sets the class of the value object.  Only needed if the obj
   * itself can be null.  If set, this takes presidence over the actual
   * class of the obj.
   */
  public void setValueObjectClass(Class newValueObjectClass) {
    valueObjectClass = newValueObjectClass;
    fireStateChanged(new StructureChangeEvent(this));
  }

  /**
   * Returns the class of the value object.  Only needed if the obj
   * itself can be null.  If set, this takes presidence over the actual
   * class of the obj.
   */
  public Class getValueObjectClass() {
    return valueObjectClass;
  }

  // ***********************************************************************
  // *                   PROTECTED ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Get the value for the given property in the given obj.
   */
  protected Object getPropertyValue(String propertyName, Object vo)
      throws ModelException {
    return getPropertyAccessor(propertyName).getPropertyValue(vo);
  }

  /**
   * Get the values for the given properties in the given obj.
   */
  protected Object[] getPropertyValues(String[] propertyNames, Object vo)
      throws ModelException {
    // do pre-check
    if (propertyNames == null)
      throw new ModelException(ModelException.REASON_MULTI_PROPERTY_MISMATCH);
    // begin retrieval
    Object[] results = new Object[propertyNames.length];
    for (int i = 0; i < propertyNames.length; i++) {
      results[i] =
          getPropertyAccessor(propertyNames[i])
              .getPropertyValue(vo);
    }
    return results;
  }

  /**
   * Set the values for the given properties in the given obj.
   * Fires a StateChanged event to any listeners.
   */
  protected void setPropertyValues(String propertyName, Object value, Object vo)
      throws ModelException {
    getPropertyAccessor(propertyName).setPropertyValue(vo, value);
    fireStateChanged(new ContentValueChangeEvent(this, propertyName, null, value));
  }

  /**
   * Set the values for the given properties in the given obj.
   * Fires a StateChanged event to any listeners.
   */
  protected void setPropertyValues(String[] propertyNames, Object[] values, Object vo)
      throws ModelException {
    // do pre-check
    checkArrays(propertyNames, values);
    // begin changes
    changingPropertyValue = true;
    try {
      for (int i = 0; i < propertyNames.length; i++) {
        getPropertyAccessor(propertyNames[i]).setPropertyValue(vo, values[i]);
      }
    } finally {
      changingPropertyValue = false;
    }
    fireStateChanged(new ContentValueChangeEvent(this, null, null, null));
  }

  /**
   * Returns existing PropertyAccessor for given propertyName else creates new one.
   */
  protected PropertyAccessor getPropertyAccessor(String propertyName)
      throws ModelException {
    PropertyAccessor pa = (PropertyAccessor)properties.get(propertyName);
    if (pa == null) {
      // Get or set voClass
      if (valueObjectClass == null) {
        if (obj == null)
          throw new ModelPropertyAccessorException(
            ModelPropertyAccessorException.REASON_TARGET_AND_CLASS_ARE_NULL,
            valueObjectClass, propertyName);
        setValueObjectClass(obj.getClass());
      }
      pa = new PropertyAccessor(valueObjectClass, propertyName);
      properties.put(propertyName, pa);
    }
    return pa;
  }

  /**
   * Returns the map of PropertyAccessors which have been created so far.
   */
  protected Map getPropertyAccessors() {
    return properties;
  }

  // ***********************************************************************
  // *                PROPERTY CHANGE LISTENER METHODS
  // ***********************************************************************

  /**
   * This method gets called when a bound property is changed.
   * Notifies this value holder that a property of the value object has changed.
   * This component will, in turn, notify all of its ChangeListeners with a
   * ContentValueChangeEvent.  The components will NOT be notified twice if the
   * change was initiated through this component's setPropertyValue method.
   */
  public void propertyChange(PropertyChangeEvent e) {
    if (!changingPropertyValue)
      fireStateChanged(new ContentValueChangeEvent(
          this, e.getPropertyName(), e.getOldValue(), e.getNewValue()));
  }
}
