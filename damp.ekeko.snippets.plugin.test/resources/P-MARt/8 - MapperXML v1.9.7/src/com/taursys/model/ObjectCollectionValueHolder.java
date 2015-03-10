/**
 * ObjectCollectionValueHolder - ValueHolder which has a Collection of Objects
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

import java.util.*;

/**
 * This class is a ValueHolder which contains a Collection of Objects. All
 * get/setPropertyValue methods return the whole Object. It provides access to
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
 * @author Marty Phelan
 * @version 2.0
 */
public class ObjectCollectionValueHolder extends AbstractCollectionValueHolder {

  /**
   * Constructs an ObjectCollectionValueHolder. An ObjectValueHolder is created
   * to access the current item.
   */
  public ObjectCollectionValueHolder() {
    super(new ObjectValueHolder());
  }

  /**
   * Constructs an ObjectCollectionValueHolder with the given array of Objects
   * as its Collection. The Objects in the given array are stored in an
   * ArrayList.
   */
  public ObjectCollectionValueHolder(Object[] array) {
    super(new ObjectValueHolder(), new ArrayList(Arrays.asList(array)));
  }

  /**
   * Constructs a new ObjectCollectionValueHolder for the given collection.
   */
  public ObjectCollectionValueHolder(Collection collection) {
    super(new ObjectValueHolder(), collection);
  }
}
