/**
 * AbstractValueHolder - A partial implementation of a ValueHolder Interface
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

import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.taursys.util.DataTypes;
import com.taursys.util.UnsupportedDataTypeException;
import com.taursys.model.event.ContentValueChangeEvent;


/**
 * AbstractValueHolder is a partial implementation of the ValueHolder Interface.
 * @author Marty Phelan
 * @version 1.0
 */
public abstract class AbstractValueHolder implements ValueHolder {
  private String alias;
  private transient Vector changeListeners;
  private boolean multiplePropertiesChanging;

  /**
   * Constructs a new AbstractValueHolder
   */
  public AbstractValueHolder() {
  }

  // ***********************************************************************
  // *                    PROPERTY VALUE ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Get the java data type for the given propertyName
   * @return the java data type for the given propertyName
   */
  public abstract int getJavaDataType(String propertyName)
      throws ModelException;

  /**
   * Get the value for the given property name.
   * @return the value for the given property name.
   */
  public abstract Object getPropertyValue(String propertyName)
      throws ModelException;

  /**
   * Get the values for the given property names.
   * This method returns an empty Object array if the given propertyNames
   * is null or empty.
   * @param propertyNames array of property names
   * @return the values for the given property names.
   */
  public Object[] getPropertyValues(String[] propertyNames)
      throws ModelException {
    if (propertyNames != null && propertyNames.length > 0) {
      Object[] results = new Object[propertyNames.length];
      for (int i = 0; i < propertyNames.length; i++) {
        results[i] = getPropertyValue(propertyNames[i]);
      }
      return results;
    } else {
      return new Object[]{};
    }
  }

  /**
   * Set the named property's value to the given value.
   * Fires a StateChanged event to any listeners.
   * @param propertyName the property to update
   * @param the value to update the property with
   */
  public abstract void setPropertyValue(String propertyName, Object value)
      throws ModelException;

  /**
   * Set the the named property's values to the given values
   * This method also sets the multiplePropertiesChanging to true
   * while it is updating properties. Implementations of setPropertyValue
   * method should not generate events if this flag is true.
   * When complete, this method fires a StateChanged event to any listeners.
   * This method does nothing if either propertyNames or values are null, or
   * if they are different sizes.
   * @param propertyNames the names of the properties to update
   * @values the values to update the properties with
   */
  public void setPropertyValues(String[] propertyNames, Object[] values)
      throws ModelException {
    // Precheck for valid conditions
    checkArrays(propertyNames, values);
    setMultiplePropertiesChanging(true);
    try {
      for (int i = 0; i < propertyNames.length; i++) {
        setPropertyValue(propertyNames[i], values[i]);
      }
      fireStateChanged(new ContentValueChangeEvent(this, null, null, null));
    } finally {
      setMultiplePropertiesChanging(false);
    }
  }

  /**
   * Check to ensure arrays are not null and same size
   * @param propertyNames the list of property names
   * @param values the corresponding values
   * @throws ModelException.REASON_MULTI_PROPERTY_MISMATCH if either array
   * is null or they are not the same size.
   */
  protected void checkArrays(String[] propertyNames, Object[] values)
      throws ModelException {
    if (propertyNames == null || values == null
        || propertyNames.length != values.length)
      throw new ModelException(ModelException.REASON_MULTI_PROPERTY_MISMATCH);
  }

  // ***********************************************************************
  // *                         GENERAL PROPERTIES
  // ***********************************************************************

  /**
   * Sets the alias name for this VOValueHolder.  This property is used by the
   * ComponentFactory to bind Components to ValueHolders by matching it to the
   * first part of the Component's ID property.
   * @param newAlias the alias name for this VOValueHolder
   */
  public void setAlias(String newAlias) {
    alias = newAlias;
  }

  /**
   * Gets the alias name for this VOValueHolder.  This property is used by the
   * ComponentFactory to bind Components to ValueHolders by matching it to the
   * first part of the Component's ID property.
   * @return the alias name for this VOValueHolder
   */
  public String getAlias() {
    return alias;
  }

  /**
   * Set flag indicating that multiple properties are being changed.
   * This flag is intended to avoid multiple events from being fired for
   * a single batch update.
   * @param multiplePropertiesChanging flag indicating that multiple
   * properties are being changed.
   */
  protected void setMultiplePropertiesChanging(boolean multiplePropertiesChanging) {
    this.multiplePropertiesChanging = multiplePropertiesChanging;
  }

  /**
   * Get flag indicating that multiple properties are being changed.
   * This flag is intended to avoid multiple events from being fired for
   * a single batch update.
   * @return flag indicating that multiple properties are being changed.
   */
  protected boolean isMultiplePropertiesChanging() {
    return multiplePropertiesChanging;
  }

  // ***********************************************************************
  // *                    CHANGE LISTENER METHODS
  // ***********************************************************************

  /**
   * Removes the specified change listener so that it no longer receives
   * change events from this value holder.
   * Change events are generated whenever the contents of the value holder change.
   */
  public synchronized void removeChangeListener(ChangeListener l) {
    if (changeListeners != null && changeListeners.contains(l)) {
      Vector v = (Vector) changeListeners.clone();
      v.removeElement(l);
      changeListeners = v;
    }
  }

  /**
   * Adds the specified change listener to receive change events from this value holder.
   * Change events are generated whenever the contents of the value holder change.
   */
  public synchronized void addChangeListener(ChangeListener l) {
    Vector v = changeListeners == null ? new Vector(2) : (Vector) changeListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      changeListeners = v;
    }
  }

  /**
   * Reports a state change to all change listeners.
   */
  protected void fireStateChanged(ChangeEvent e) {
    if (changeListeners != null) {
      Vector listeners = changeListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ChangeListener) listeners.elementAt(i)).stateChanged(e);
      }
    }
  }
}
