/**
 * PropertyAccessor - Helper class which accesses value object properties
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

import java.text.Format;
import java.text.MessageFormat;
import com.taursys.util.DataTypes;
import com.taursys.util.UnsupportedDataTypeException;
import com.taursys.util.UnsupportedConversionException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import com.taursys.debug.Debug;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalAccessException;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.util.StringTokenizer;

/**
 * This is a helper class which provides access to value object properties.
 * This class is constructed by with the class of the target value object
 * and the name of the target property within the value object.  As part of
 * construction, introspection sets up the methods for reading and writing the
 * property.
 * <p>
 * This class provides two primary methods, getPropertyValue and setPropertyValue
 * which provide access to the property in the given value object.  The
 * valueObject passed to these methods must be the same class that was used to
 * create this property accessor.
 * <p>
 * The readMethods or writeMethod may be null if the valueObject does
 * not have a cooresponding get or set method. This method uses reflection
 * and draws information from the valueObject's BeanInfo and PropertyDescriptors.
 * <p>
 * The property name should follow JavaBean naming conventions.  If the
 * property you want to access is in a nested class, then separate the
 * property names by periods.
 * <p>
 * Example: propertyName="color": then it expects getColor and setColor
 * will be the method names in the valueObject.
 * <p>
 * Example: propertyName="address.city": Assumes your value object contains
 * an address property which in turn contains a city property.
 * Accessing the property will result in a call: getAddress().getCity() or
 * getAddress().setCity(...)
 */
public class PropertyAccessor {
  private String propertyName;
  private int javaDataType = DataTypes.TYPE_UNDEFINED;
  private java.lang.reflect.Method writeMethod;
  private java.lang.reflect.Method[] readMethods;
  private Class valueObjectClass;
  private boolean primative;

  public PropertyAccessor(Class valueObjectClass, String propertyName)
      throws ModelException {
    this.valueObjectClass = valueObjectClass;
    this.propertyName = propertyName;
    try {
      setAccessorMethods();
    } catch (ModelException ex) {
      Debug.error("Error accessing property. " + ex.getMessage(), ex);
      throw ex;
    }
  }

  // ***********************************************************************
  // *                     INITIALIZATION METHODS
  // ***********************************************************************

  /**
   * Sets the javaDataType, readMethods and writeMethod properties.
   * The valueObject and propertyName must be valid before invoking this
   * method.
   * The readMethods or writeMethod may be null if the valueObject does
   * not have a cooresponding get or set method. This method uses reflection
   * and draws information from the valueObject's BeanInfo and PropertyDescriptors.
   * @throws ModelException if valueObjectClass is null, propertyName is null/blank,
   * property not found in class, or an IntrospectionException occurs.
   */
  protected void setAccessorMethods() throws
      ModelException {
    if (valueObjectClass == null)
      throw new ModelPropertyAccessorException(
        ModelPropertyAccessorException.REASON_TARGET_CLASS_IS_NULL,
        valueObjectClass, propertyName);
    if (propertyName == null || propertyName.length() == 0)
      throw new ModelPropertyAccessorException(
        ModelPropertyAccessorException.REASON_PROPERTY_NAME_MISSING,
        valueObjectClass, propertyName);
    StringTokenizer tokenizer = new StringTokenizer(propertyName, ".");
    int count = tokenizer.countTokens() ;
    readMethods = new Method[count];
    PropertyDescriptor prop = null;
    Class targetClass = valueObjectClass;
    for (int i = 0; i < count ; i++) {
      String propName = tokenizer.nextToken();
      prop = getProperty(targetClass, propName);
      readMethods[i] = prop.getReadMethod();
      targetClass = prop.getPropertyType();
    }
    primative = targetClass.isPrimitive();
    javaDataType = DataTypes.getDataType(targetClass.getName());
    writeMethod = prop.getWriteMethod();
  }

  /**
   * Searches the given class for the given property name and returns the PropertyDescriptor.
   * @throws ModelPropertyAccessorException if property is not found, or if an
   * IntrospectionException occurs.
   */
  protected PropertyDescriptor getProperty(Class c, String propName)
      throws ModelPropertyAccessorException {
    BeanInfo info = null;
    try {
      info = Introspector.getBeanInfo(c);
    }
    catch (IntrospectionException ex) {
      throw new ModelPropertyAccessorException(
          ModelPropertyAccessorException.REASON_INTROSPECTION_EXCEPTION,
          c, propName);
    }
    PropertyDescriptor[] props = info.getPropertyDescriptors();
    for( int i=0 ; i < props.length ; i++ ) {
      if (props[i].getName().equals(propName)) {
        return props[i];
      }
    }
    throw new ModelPropertyAccessorException(
        ModelPropertyAccessorException.REASON_PROPERTY_NOT_FOUND,
        c, propName);
  }

  // ***********************************************************************
  // *               PROPERTY VALUE ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Returns a property value from the valueObject based on the propertyName.
   * @return Object obtained from invocation of getter method
   * @throws ModelException if property has no read method defined or if an
   * IllegalAccessException or IllegalArgumentException occurs.
   * @throws ModelInvocationTargetException if the invoked read method throws an
   * Exception.
   */
  public Object getPropertyValue(Object valueObject)
      throws ModelException {
    Object target = valueObject;
    try {
      for (int i = 0; target != null && i < readMethods.length; i++) {
        target = invokeReadMethod(i, target);
      }
      return target;
    } catch (ModelException ex) {
      Debug.error("Error accessing property. " + ex.getMessage(), ex);
      throw ex;
    }
  }

  /**
   * Sets a property of the valueObject to the given value based on the propertyName.
   * @throws ModelException if property has no write method defined or if an
   * IllegalAccessException or IllegalArgumentException occurs.
   * @throws ModelInvocationTargetException if the invoked read method throws an
   * Exception.
   */
  public void setPropertyValue(Object valueObject, Object value)
      throws ModelException {
    Object target = valueObject;
    try {
      int count = readMethods.length -1;
        for (int i = 0; target != null && i < count ; i++) {
          target = invokeReadMethod(i, target);
        }
      invokeWriteMethod(target, value);
    } catch (ModelException ex) {
      // Debug if this is not NULL for primative exception
      if (!(ex instanceof ModelPropertyAccessorException &&
          ((ModelPropertyAccessorException)ex).getReason() ==
          ModelPropertyAccessorException.REASON_NULL_VALUE_FOR_PRIMATIVE)) {
        Debug.error("Error accessing property. " + ex.getMessage(), ex);
      }
      throw ex;
    }
  }

  /**
   * Invokes the i(th) read method on the given target and returns results.
   * @return Object obtained from invocation of getter method
   * @throws ModelException if property has no read method defined or if an
   * IllegalAccessException occurs.
   * @throws ModelInvocationTargetException if the invoked read method throws an
   * Exception.
   */
  protected Object invokeReadMethod(int i, Object target) throws ModelException {
    if (target == null)
      return null;
    if (readMethods[i] == null)
      throw new ModelPropertyAccessorException(
          ModelPropertyAccessorException.REASON_NO_READ_METHOD_FOR_PROPERTY,
          target.getClass(), propertyName);
    try {
      return readMethods[i].invoke(target, new Object[] {});
    } catch (InvocationTargetException ex) {
      throw new ModelInvocationTargetException(
          valueObjectClass, propertyName, javaDataType, target,
          readMethods[i], ex);
    } catch (IllegalAccessException ex) {
      throw new ModelPropertyAccessorException(
          ModelPropertyAccessorException.REASON_ILLEGAL_ACCESS_EXCEPTION,
          valueObjectClass, propertyName, javaDataType, target,
          readMethods[i], ex);
    } catch (IllegalArgumentException ex) {
      throw new ModelPropertyAccessorException(
          ModelPropertyAccessorException.REASON_ILLEGAL_ARGUMENT_EXCEPTION,
          valueObjectClass, propertyName, javaDataType, target,
          readMethods[i], ex);
    }
  }

  /**
   * Invokes the write method on the given target.
   * @throws ModelException if property has no write method defined or if an
   * IllegalAccessException or IllegalArgumentException occurs.
   * @throws ModelInvocationTargetException if the invoked read method throws an
   * Exception.
   */
  protected void invokeWriteMethod(Object target, Object value)
      throws ModelException {
    if(target == null)
      throw new ModelPropertyAccessorException(
        ModelPropertyAccessorException.REASON_TARGET_IS_NULL,
           valueObjectClass, propertyName);
    if(writeMethod == null)
      throw new ModelPropertyAccessorException(
        ModelPropertyAccessorException.REASON_NO_WRITE_METHOD_FOR_PROPERTY,
           valueObjectClass, propertyName);
    if(value == null && isPrimative())
      throw new ModelPropertyAccessorException(
          ModelPropertyAccessorException.REASON_NULL_VALUE_FOR_PRIMATIVE,
          valueObjectClass, propertyName, javaDataType,
          target, writeMethod);
    try {
      writeMethod.invoke(target, new Object[] {value});
    } catch (InvocationTargetException ex) {
      throw new ModelInvocationTargetException(
          valueObjectClass, propertyName, javaDataType, target, value,
          writeMethod, ex);
    } catch (IllegalAccessException ex) {
      throw new ModelPropertyAccessorException(
          ModelPropertyAccessorException.REASON_ILLEGAL_ACCESS_EXCEPTION,
          valueObjectClass, propertyName, javaDataType, target, value,
          writeMethod, ex);
    } catch (IllegalArgumentException ex) {
      throw new ModelPropertyAccessorException(
          ModelPropertyAccessorException.REASON_ILLEGAL_ARGUMENT_EXCEPTION,
          valueObjectClass, propertyName, javaDataType, target, value,
          writeMethod, ex);
    }
  }

  // ***********************************************************************
  // *               PROPERTY ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Returns the JavaDataType of the property in the valueObject.
   */
  public int getJavaDateType() {
    return javaDataType;
  }

  /**
   * Returns the name of the property to access in the valueObjectClass.
   * This is the property that get/setPropertyValue methods will access.
   * The property name should follow JavaBean naming conventions.  If the
   * property you want to access is in a nested class, then separate the
   * property names by periods.
   * <p>
   * Example: propertyName="color": then it expects getColor and setColor
   * will be the method names in the valueObject.
   * <p>
   * Example: propertyName="address.city": Assumes your value object contains
   * an address property which in turn contains a city property.
   * Accessing the property will result in a call: getAddress().getCity() or
   * getAddress().setCity(...)
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Returns the Methods used to get/read the target property.
   * This value is set by setAccessorMethods which is invoked
   * automatically when this object is created.
   */
  protected java.lang.reflect.Method[] getReadMethods() {
    return readMethods;
  }

  /**
   * Returns the Method used to get/read the target property.
   * This value is set by setAccessorMethods which is invoked
   * automatically when this object is created.
   */
  protected java.lang.reflect.Method getWriteMethod() {
    return writeMethod;
  }

  /**
   * Returns the class of the ValueObject(or parent ValueObject) where the value resides.
   * This is the class that get/setPropertyValue methods will access.
   */
  public Class getValueObjectClass() {
    return valueObjectClass;
  }

  /**
   * Indicates that the property is a primative data type.
   * @return true if the property is a primative data type.
   */
  protected boolean isPrimative() {
    return primative;
  }
}
