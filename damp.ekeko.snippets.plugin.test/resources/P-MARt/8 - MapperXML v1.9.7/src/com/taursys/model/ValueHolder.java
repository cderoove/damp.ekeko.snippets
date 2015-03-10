/**
 * ValueHolder - Object which stores/retrieves a value by property name
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

import javax.swing.event.ChangeListener;

/**
 * <p>A <code>ValueHolder</code> is a foundation subcomponent for MapperXML which
 * is used by <code>TextModels</code> to store/retrieve the current "state"
 * or value. A <code>ValueHolder</code> can be used exclusively by a single
 * <code>TextModel</code> or shared by multiple <code>TextModels</code>. A
 * specific property of the <code>ValueHolder</code> host object can also be
 * targeted. The targeted property can be specified by setting the
 * <code>propertyName</code> property. (Note: the <code>ValueHolder</code>
 * implementation must be one that actually supports access to individual
 * properties for this feature to work, otherwise the <code>propertyName</code>
 * setting is ignored and the whole host object is accessed).</p>
 *
 * <p>The data type of the targeted host object property (or the host object
 * itself) is available via the <code>getJavaDataType</code> method.</p>
 *
 * <p>The <code>alias</code> property provides a way of naming this
 * <code>ValueHolder</code>. This name is required when using the
 * <code>ComponentFactory</code> to automatically create and bind
 * MapperXML components from an HTML/XML document.</p>
 *
 * <p>The <code>ValueHolder</code> also provides notification to
 * <code>ChangeListeners</code> whenever the "state" or value changes. Interested
 * listeners can register or unregister to receive notification of
 * <code>ChangeEvents</code> by using the <code>addChangeListener</code> or
 * <code>removeChangeListener</code> methods.</p>
 *
 * <p>Note: This interface is subject to change. You should extend one of the
 * existing classes rather than directly implementing this interface.</p>
 */
public interface ValueHolder {

  /**
   * Get the value of the given property in the valueObject.
   * @return the value of the given property in the valueObject.
   */
  public Object getPropertyValue(String propertyName) throws ModelException;

  /**
   * Get the values for the given property names.
   * This method returns an empty Object array if the given propertyNames
   * is null or empty.
   * @param propertyNames array of property names
   * @return the values for the given property names.
   */
  public Object[] getPropertyValues(String[] propertyNames)
      throws ModelException;

  /**
   * Set the value of the given property in the valueObject.
   * Fires a StateChanged event to any listeners.
   * @param propertyName the property name to set
   * @param value the value to set the property to
   */
  public void setPropertyValue(String propertyName, Object value)
      throws ModelException;

  /**
   * Set the values for the given properties in the valueObject.
   * Fires a StateChanged event to any listeners.
   * @param propertyNames the property names to set
   * @param values the values to set the properties to
   */
  public void setPropertyValues(String[] propertyNames, Object[] values)
      throws ModelException;

  /**
   * Get the java data type for the given property
   * @return the java data type for the given property
   */
  public int getJavaDataType(String propertyName) throws ModelException;

  /**
   * Get the alias name for this ValueHolder.  This property is used by the
   * ComponentFactory to bind Components to ValueHolders by matching it to the
   * first part of the Component's ID property.
   * @return the alias name for this ValueHolder
   */
  public String getAlias();

  /**
   * Removes the specified change listener so that it no longer receives change
   * events from this value holder. Change events are generated whenever the
   * contents of the value holder change.
   * @param l the change listener to remove
   */
  public void removeChangeListener(ChangeListener l);

  /**
   * Adds the specified change listener to receive change events from this
   * value holder. Change events are generated whenever the contents of the
   * value holder change.
   * @param l the change listener to add
   */
  public void addChangeListener(ChangeListener l);
}
