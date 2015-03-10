/**
 * ObjectArrayValueHolder - ValueHolder which stores an array of objects.
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

import java.util.Collection;
import java.util.Iterator;
import com.taursys.util.DataTypes;
import com.taursys.util.UnsupportedDataTypeException;
import com.taursys.model.ModelException;
import com.taursys.model.event.ContentChangeEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Vector;

/**
 * ValueHolder which stores an array of objects.  The get/setPropertyValue
 * methods simply return the object in the current position.
 */
public class ObjectArrayValueHolder extends AbstractValueHolder
    implements CollectionValueHolder {
  private Object[] array;
  private int position = -1;

  /**
   * Constructs a new ObjectArrayValueHolder.
   */
  public ObjectArrayValueHolder() {
  }

  /**
   * Constructs a new ObjectArrayValueHolder with the given array.
   */
  public ObjectArrayValueHolder(Object[] newArray) {
    array = newArray;
  }

  /**
   * Sets the given array as the internal array of objects.
   * @deprecated - use setArray - this method will be removed soon
   */
  public void setVariantArray(Object[] newArray) {
    array = newArray;
  }

  /**
   * Returns the current object array.
   * @deprecated - use setArray - this method will be removed soon
   */
  public Object[] getVariantArray() {
    return array;
  }

  /**
   * Sets the given array as the internal array of objects.
   */
  public void setArray(Object[] newArray) {
    array = newArray;
  }

  /**
   * Returns the current object array.
   */
  public Object[] getArray() {
    return array;
  }

  /**
   * Returns the current object in array (propertyName is ignored).
   */
  public Object getPropertyValue(String propertyName) throws ModelException {
    return array[position];
  }

  /**
   * Sets the current object in array (propertyName is ignored).
   */
  public void setPropertyValue(String propertyName, Object newValue)
      throws ModelException {
    array[position] = newValue;
    if (!isMultiplePropertiesChanging())
      fireStateChanged(new ContentChangeEvent(this, newValue == null));
  }

  /**
   * Always returns DataTypes.TYPE_UNDEFINED (propertyName is ignored)
   */
  public int getJavaDataType(String propertyName) throws ModelException {
    return DataTypes.TYPE_UNDEFINED;
  }

  /**
   * Indicates whether there is another (any) Objects in the array.
   * This method true if there is another element in the array.
   */
  public boolean hasNext() {
    return (position+1 < array.length);
  }

  /**
   * Increments the position in the array.  You should invoke the hasNext
   * method BEFORE invoking this method to ensure that there IS another object.
   * Fires a StateChanged event to any listeners.
   */
  public void next() {
    position++;
    fireStateChanged(new ChangeEvent(this));
  }

  /**
   * Resets the position in the array to -1.
   */
  public void reset() {
    position = -1;
  }

  /**
   * Returns the object in the current position.  You should ensure that the
   * current position is valid before invoking this method.
   */
  public Object getObject() {
    return array[position];
  }

  /**
   * Sets (replace/copy) the object in the current position.  You should ensure
   * that the current position is valid before invoking this method.  Depending
   * on the specific implementation, the given object may either replace the
   * current object in the list, or the property values of the given object may
   * be copied to the current object in the list.
   * Fires a StateChanged event to any listeners.
   */
  public void setObject(Object obj) {
    array[position] = obj;
    fireStateChanged(new ChangeEvent(this));
  }

  public int size() {
    if (array != null)
      return array.length;
    else
      return 0;
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public boolean contains(Object o) {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method contains() not yet implemented.");
  }
  public Iterator iterator() {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method iterator() not yet implemented.");
  }
  public Object[] toArray() {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method toArray() not yet implemented.");
  }
  public Object[] toArray(Object[] a) {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method toArray() not yet implemented.");
  }
  public boolean add(Object o) {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method add() not yet implemented.");
  }
  public boolean remove(Object o) {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method remove() not yet implemented.");
  }
  public boolean containsAll(Collection c) {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method containsAll() not yet implemented.");
  }
  public boolean addAll(Collection c) {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method addAll() not yet implemented.");
  }
  public boolean removeAll(Collection c) {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method removeAll() not yet implemented.");
  }
  public boolean retainAll(Collection c) {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method retainAll() not yet implemented.");
  }
  public void clear() {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method clear() not yet implemented.");
  }
  public boolean equals(Object o) {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method equals() not yet implemented.");
  }
  public void stateChanged(ChangeEvent e) {
    /**@todo: Implement this java.util.Collection method*/
    throw new java.lang.UnsupportedOperationException("Method equals() not yet implemented.");
  }
}
