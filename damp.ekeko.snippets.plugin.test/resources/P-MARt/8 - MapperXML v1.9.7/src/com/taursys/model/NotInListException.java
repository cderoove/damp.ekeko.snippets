/**
 * ModelException - General Exception for problems encountered in Model objects.
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
 * Exception generated when item is not in list.  Typically used with SelectModel.
 */
public class NotInListException extends SelectModelException {
  public static final int REASON_OFFSET                       =
      SelectModelException.REASON_OFFSET + 10;
  public static final int CURRENT_VALUE_NOT_IN_LIST           = 0 + REASON_OFFSET;
  public static final int GIVEN_VALUE_NOT_IN_LIST             = 1 + REASON_OFFSET;
  private static String[] messages = new String[] {
    "Current value not found in list.",
    "Given value not found in list."
  };
  private String value;

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
   * Constructs a new exception for given reason
   */
  public NotInListException(int reason) {
    super(getReasonMessage(reason), reason);
  }

  /**
   * Constructs a new exception for given reason with given diagnostic value
   */
  public NotInListException(int reason, String value) {
    super(getReasonMessage(reason), reason);
    this.value = value;
    diagnosticInfo = "Given value=" + value;
  }

  // ************************************************************************
  //                       Property Accessors
  // ************************************************************************

  public String getValue() {
    return value;
  }
}
