/**
 * VariantValueHolder - ValueHolder which stores a given type of value internally
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

import com.taursys.util.DataTypes;
import com.taursys.util.UnsupportedDataTypeException;
import com.taursys.model.ModelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Vector;

/**
 * ValueHolder which stores a given type of value internally.  An instance
 * of this class is constructed by giving it the java data type.  After that,
 * the instance will only store/retrieve that data type.
 */
public class VariantValueHolder extends ObjectValueHolder {
  private int javaDataType = DataTypes.TYPE_UNDEFINED;

  /**
   * Constructs a new VariantValueHolder for the given data type.
   * @see com.taursys.util.DataTypes for list of supported data types
   * @throws UnsupportedDataTypeException if given an invalid data type
   */
  public VariantValueHolder(int javaDataType) throws UnsupportedDataTypeException {
    DataTypes.checkJavaDataType(javaDataType);
    this.javaDataType = javaDataType;
  }

  /**
   * Sets the current value (propertyName is ignored).
   * Fires a StateChanged event to any listeners.
   */
  public void setPropertyValue(String propertyName, Object newValue)
      throws ModelException {
    if (newValue != null) {
        Class expectedClass = DataTypes.getClassForType(javaDataType);
        if (!expectedClass.isInstance(newValue))
          throw new ClassCastException("Given "
              + newValue.getClass().getName() + " expected "
              + DataTypes.getJavaNameForType(javaDataType));
    }
    super.setPropertyValue(propertyName, newValue);
  }

  /**
   * Returns the java data type (propertyName is ignored)
   */
  public int getJavaDataType(String propertyName) throws ModelException {
    return javaDataType;
  }

}
