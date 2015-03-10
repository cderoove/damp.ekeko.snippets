/**
 * ModelPropertyAccessorException - Exceptions encountered while accessing properties
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

import java.lang.reflect.Method;
import com.taursys.util.DataTypes;
import com.taursys.util.ChainedException;

/**
 * ModelExceptions encountered while accessing properties.  This exception
 * stores information about the reason and various other meaningful data
 * whenever an Exception occurs in a PropertyAccessor.
 */
public class ModelPropertyAccessorException extends ModelException {
  public static final int REASON_OFFSET                       =
      ModelException.REASON_OFFSET + 100;
  public static final int REASON_TARGET_CLASS_IS_NULL         = 0 + REASON_OFFSET;
  public static final int REASON_PROPERTY_NAME_MISSING        = 1 + REASON_OFFSET;
  public static final int REASON_PROPERTY_NOT_FOUND           = 2 + REASON_OFFSET;
  public static final int REASON_NO_READ_METHOD_FOR_PROPERTY  = 3 + REASON_OFFSET;
  public static final int REASON_NO_WRITE_METHOD_FOR_PROPERTY = 4 + REASON_OFFSET;
  public static final int REASON_INTROSPECTION_EXCEPTION      = 5 + REASON_OFFSET;
  public static final int REASON_ILLEGAL_ACCESS_EXCEPTION     = 6 + REASON_OFFSET;
  public static final int REASON_TARGET_IS_NULL               = 7 + REASON_OFFSET;
  public static final int REASON_ILLEGAL_ARGUMENT_EXCEPTION   = 8 + REASON_OFFSET;
  public static final int REASON_TARGET_AND_CLASS_ARE_NULL    = 9 + REASON_OFFSET;
  public static final int REASON_NULL_VALUE_FOR_PRIMATIVE     = 10 + REASON_OFFSET;
  protected static final String[] messages = new String[] {
    "ValueObject class is null.",
    "Property name is null or blank.",
    "Property not found in class.",
    "No read method defined for property.",
    "No write method defined for property.",
    "IntrospectionException occurred.",
    "IllegalAccessException occurred.",
    "Target object (or child) is null.",
    "IllegalArgumentException - Either ValueObject or value is wrong class.",
    "ValueObject and ValueObjectClass are both null.",
    "Value for primative cannot be set to null",
  };
  protected static final String[] friendlyMessages = new String[] {
    "A system problem has occurred and been logged.",
    "A system problem has occurred and been logged.",
    "A system problem has occurred and been logged.",
    "A system problem has occurred and been logged.",
    "A system problem has occurred and benn logged",
    "A system problem has occurred and benn logged",
    "A system problem has occurred and benn logged",
    "A system problem has occurred and benn logged",
    "A system problem has occurred and benn logged",
    "A system problem has occurred and benn logged",
    "You must enter or select a value.",
  };
  protected String propertyName = "n/a";
  protected String valueObjectClassName = "n/a";
  protected String methodName = "n/a";
  protected String givenValueObjectClassName = "n/a";
  protected String propertyDataType = "n/a";
  protected String givenValueDataType = "n/a";

  // ************************************************************************
  //                       Static Class Methods
  // ************************************************************************

  /**
   * Returns String for given reason code else String for REASON_INVALID_REASON_CODE.
   */
  public static String getReasonMessage(int reason) {
    if (reason >=  + REASON_OFFSET && reason < messages.length + REASON_OFFSET)
      return messages[reason - REASON_OFFSET];
    else
      return getInvalidMessage();
  }

  // ************************************************************************
  //                        Public Constructors
  // ************************************************************************

  /**
   * Constructs exception for given reason, VO class and propertyName.
   */
  public ModelPropertyAccessorException(int reason, Class voClass,
      String propertyName) {
    super(getReasonMessage(reason), reason);
    setValueObjectClassName(voClass);
    this.propertyName = propertyName;
    setDiagnosticsInfo();
  }

  /**
   * Constructs exception for given reason, VO class, propertyName, data type, invoked method and cause.
   */
  public ModelPropertyAccessorException(int reason, Class voClass,
      String propertyName, int javaDataType, Object target, Object value,
      Method method, Throwable cause) {
    super(getReasonMessage(reason), reason);
    setValueObjectClassName(voClass);
    this.propertyName = propertyName;
    this.cause = cause;
    setMethodName(method);
    setPropertyDataType(javaDataType);
    setGivenValueObjectClassName(target);
    setGivenValueDataType(value);
    setDiagnosticsInfo();
  }

  /**
   * Constructs exception for given reason, VO class, propertyName, data type, target object, and invoked method.
   */
  public ModelPropertyAccessorException(int reason, Class voClass,
      String propertyName, int javaDataType, Object target, Method method) {
    super(getReasonMessage(reason), reason);
    setValueObjectClassName(voClass);
    this.propertyName = propertyName;
    setMethodName(method);
    setPropertyDataType(javaDataType);
    setGivenValueObjectClassName(target);
    setDiagnosticsInfo();
  }

  /**
   * Constructs exception for given reason, VO class, propertyName, data type, target object, invoked method and cause.
   */
  public ModelPropertyAccessorException(int reason, Class voClass,
      String propertyName, int javaDataType, Object target, Method method,
      Throwable cause) {
    super(getReasonMessage(reason), reason);
    setValueObjectClassName(voClass);
    this.propertyName = propertyName;
    this.cause = cause;
    setMethodName(method);
    setPropertyDataType(javaDataType);
    setGivenValueObjectClassName(target);
    setDiagnosticsInfo();
  }

  // ************************************************************************
  //                       Protected Constructors
  // ************************************************************************

  /**
   * Creates a ChainedException with a message.
   * This constructor is only available to subclasses
   */
  protected ModelPropertyAccessorException(String message, int reason) {
    super(message, reason);
  }

  // ************************************************************************
  //                   Prepare Diagnostics Info Methods
  // ************************************************************************

  /**
   * Builds the diagnosticsInfo String from the current property values.
   */
  protected void setDiagnosticsInfo() {
    diagnosticInfo =
        "Property Name=" + propertyName +
        "(" + propertyDataType + ")" +
        "\nValueObject Class=" + valueObjectClassName +
        "\nInvoked Method Name=" + methodName +
        "\nGiven ValueObject Class=" + givenValueObjectClassName +
        "\nGiven Value data type=" + givenValueDataType;
    if (reason >=  + REASON_OFFSET && reason < messages.length + REASON_OFFSET)
      userFriendlyMessage = friendlyMessages[reason - REASON_OFFSET];
    else
      userFriendlyMessage = "System problem has occurred (unknown reason code).";
    userFriendlyMessage += " (" + propertyName + ")";
  }

  // ************************************************************************
  //                       Property Accessors
  // ************************************************************************

  /**
   * Sets the property data type for the given type code
   */
  protected void setPropertyDataType(int javaDataType) {
    propertyDataType = DataTypes.getJavaNameForType(javaDataType);
  }

  /**
   * Returns the data type of the property
   */
  public String getPropertyDataType() {
    return propertyDataType;
  }

  /**
   * Returns property name involved in exception
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Sets the valueObject class name for the given class else "null" if null.
   */
  protected void setValueObjectClassName(Class voClass) {
    valueObjectClassName = voClass==null?"null":voClass.getName();
  }

  /**
   * Returns class name involved in exception
   */
  public String getValueObjectClassName() {
    return valueObjectClassName;
  }

  /**
   * Sets the method name for the given method.
   */
  protected void setMethodName(Method method) {
    methodName = method.getName();
  }

  /**
   * Returns method name involved in exception
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Sets the class name for the given object else "null" if null.
   */
  protected void setGivenValueObjectClassName(Object given) {
    givenValueObjectClassName = given==null?"null":given.getClass().getName();
  }

  /**
   * Returns the class name of the given value object for the operation
   */
  public String getGivenValueObjectClassName() {
    return givenValueObjectClassName;
  }

  /**
   * Sets the class name of the given value else "null" if null.
   */
  protected void setGivenValueDataType(Object value) {
    givenValueDataType = value==null?"null":value.getClass().getName();
  }

  /**
   * Returns the class name of the given value
   */
  public String getGivenValueDataType() {
    return givenValueDataType;
  }
}
