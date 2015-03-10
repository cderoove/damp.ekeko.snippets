/**
 * VOCollectionValueHolder - ValueHolder which has a Collection of ValueObjects
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
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Collections;
import com.taursys.debug.Debug;

/**
 * ValueHolder which has a Collection of ValueObjects.  It provides access to
 * a current object via an internal iterator.  The next and reset
 * methods control the position in the collection.  The hasNext indicates
 * whether there is another object in the collection (used before invoking next).
 * <p>
 * You must invoke the next() method before invoking any method which acts on
 * the valueObject (getValueObject, getPropertyValue, setPropertyValue).  The
 * next() method will retrieve a ValueObject from the collection and hold it
 * for access and modification.
 * <p>
 * This ValueHolder also support connection to a parentValueHolder to provide
 * its Collection. This is used for hierarchical data representations
 * (master/detail). To make use of this feature, set the parentValueHolder
 * and the property name which holds the Collection in the parent via
 * the parentPropertyName property. This object will then be notified of
 * changes in the parent and will retrieve its new Collection.
 */
public class VOCollectionValueHolder extends AbstractCollectionValueHolder {

  /**
   * Constructs a new VOCollectionValueHolder
   */
  public VOCollectionValueHolder() {
    super(new VOValueHolder());
  }

  /**
   * Constructs a new VOCollectionValueHolder for the given collection.
   */
  public VOCollectionValueHolder(Collection collection) {
    super(new VOValueHolder(), collection);
  }

  /**
   * Constructs a new VOCollectionValueHolder with an empty given size ArrayList.
   */
  public VOCollectionValueHolder(int size) {
    super(new VOValueHolder(), new ArrayList(size));
  }

  // ====================================================================
  //            PROXY METHODS TO INTERNAL OBJECT VALUE HOLDER
  //                   ValueHolder Interface Methods
  // ====================================================================

  /**
   * Sets the class of the value object.  Only needed if the obj
   * itself can be null.  If set, this takes presidence over the actual
   * class of the obj.
   * @param valueObjectClass the class of the value object.
   */
  public void setValueObjectClass(Class clazz) {
    ((VOValueHolder)getObjectValueHolder()).setValueObjectClass(clazz);
  }

  /**
   * Get the class of the value object.  Only needed if the obj
   * itself can be null.  If set, this takes presidence over the actual
   * class of the obj.
   * @return the Class of the value object
   */
  public Class getValueObjectClass() {
    return ((VOValueHolder)getObjectValueHolder()).getValueObjectClass();
  }

  /**
   * @deprecated - use <code>getObject</code>() instead
   */
  public Object getValueObject() {
    return getObject();
  }

  /**
   * @deprecated - use <code>setObject</code> instead
   */
  public void setValueObject(Object obj) {
    setObject(obj);
  }
}
