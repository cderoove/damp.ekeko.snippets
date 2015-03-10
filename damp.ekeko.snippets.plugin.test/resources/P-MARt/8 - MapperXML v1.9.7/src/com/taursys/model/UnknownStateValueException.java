/**
 * UnknownStateValueException - ModelException caused in CheckboxModel
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

/**
 * UnknownStateValueException is caused within Checkbox model by unknown state values.
 * @author Marty Phelan
 * @version 1.0
 */
public class UnknownStateValueException extends com.taursys.model.ModelException {
  public static final int REASON_OFFSET                       = 400;
  public static final int REASON_UNKNOWN_VALUE                = 0 + REASON_OFFSET;
  private static final String[] messages = new String[] {
    "Given value does not match selectedValue or unselectedValue.",
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
   * Creates an UnknownStateValueException with a reason code (which will display its message).
   */
  public UnknownStateValueException(int reason, String givenValue) {
    super(getReasonMessage(reason), reason);
    diagnosticInfo = "Given value=" + givenValue;
  }
}
