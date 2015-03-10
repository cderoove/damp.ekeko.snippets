/**
 * ModelInvocationExceptionTargetException - Wrapper for InvocationTargetExceptions
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
import com.taursys.util.ChainedException;

/**
 * ModelPropertyAccessorException which is caused by InvocationTargetExceptions.
 */
public class ModelInvocationTargetException extends ModelPropertyAccessorException {
  public static final int REASON_OFFSET                       =
      ModelPropertyAccessorException.REASON_OFFSET + 10;
  public static final int REASON_INVOCATION_EXCEPTION         = 0 + REASON_OFFSET;
  private static final String[] messages = new String[] {
    "InvocationTargetException while accessing property value.",
  };

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
   * Constructs exception for given reason, VO class, propertyName, data type, target object, invoked method and cause.
   */
  public ModelInvocationTargetException(Class voClass,
      String propertyName, int javaDataType, Object target, Method method,
      Throwable cause) {
    super(getReasonMessage(REASON_INVOCATION_EXCEPTION), REASON_INVOCATION_EXCEPTION);
    setValueObjectClassName(voClass);
    this.propertyName = propertyName;
    this.cause = cause;
    setMethodName(method);
    setPropertyDataType(javaDataType);
    setGivenValueObjectClassName(target);
    setDiagnosticsInfo();
  }

  /**
   * Constructs exception for given reason, VO class, propertyName, data type, invoked method and cause.
   */
  public ModelInvocationTargetException(Class voClass,
      String propertyName, int javaDataType, Object target, Object value,
      Method method, Throwable cause) {
    super(getReasonMessage(REASON_INVOCATION_EXCEPTION), REASON_INVOCATION_EXCEPTION);
    setValueObjectClassName(voClass);
    this.propertyName = propertyName;
    this.cause = cause;
    setMethodName(method);
    setPropertyDataType(javaDataType);
    setGivenValueObjectClassName(target);
    setGivenValueDataType(value);
    setDiagnosticsInfo();
  }
}
