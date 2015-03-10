/**
 * VOListValueHolder - Holds a List of value objects and maintains an internal position.
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
 * VOListValueHolder holds a List of value objects and maintains an internal position.
 * You should add or remove objects from the list using methods on this class.
 * You can assign any List to this object.
 * @author Marty Phelan
 * @version 1.0
 */
public class VOListValueHolder extends AbstractListValueHolder
    implements ListValueHolder {

  /**
   * Constructs a new VOListValueHolder with an empty default size ArrayList.
   */
  public VOListValueHolder() {
    super(new VOValueHolder());
  }

  /**
   * Constructs a new VOListValueHolder with an empty given size ArrayList.
   */
  public VOListValueHolder(int size) {
    super(new VOValueHolder(), new ArrayList(size));
  }

  /**
   * Constructs a new VOListValueHolder with the given List.
   */
  public VOListValueHolder(List list) {
    super(new VOValueHolder(), list);
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

  /**
   * Set the Comparator that will be used to sort this List. The list is
   * sorted whenever you change the list (using setList) or invoke the
   * sort method. If the given comparator is an instance of VOComparator,
   * then it is linked to this VOListValueHolder and the previous
   * VOComparator (if any) is unlinked.
   * @param comparator the Comparator used to sort this List.
   */
  public void setComparator(Comparator comparator) {
    if (getComparator() != null && getComparator() instanceof VOComparator)
      ((VOComparator)getComparator()).setVOValueHolder(null);
    super.setComparator(comparator);
    if (comparator != null && comparator instanceof VOComparator)
      ((VOComparator)comparator).setVOValueHolder(
          (VOValueHolder)getObjectValueHolder());
  }
}
