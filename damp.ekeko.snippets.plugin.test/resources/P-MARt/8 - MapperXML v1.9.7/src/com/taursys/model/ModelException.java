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

import com.taursys.util.ChainedException;

/**
 * General Exception for problems encountered in Model objects.
 */
public class ModelException extends ChainedException {
  public static final int REASON_OFFSET                       = 1000;
  public static final int REASON_MODEL_IS_WRONG_CLASS         = 0 + REASON_OFFSET;
  public static final int REASON_OBJECTS_ARE_IMMUTABLE        = 1 + REASON_OFFSET;
  public static final int REASON_INTERNAL_ERROR               = 2 + REASON_OFFSET;
  public static final int REASON_PARSE_CONVERSION_ERROR       = 3 + REASON_OFFSET;
  public static final int REASON_MULTI_PROPERTY_MISMATCH      = 4 + REASON_OFFSET;
  private static final String[] messages = new String[] {
    "Model is wrong class.",
    "Objects in a Variant Collection Value Holder are immutable",
    "Internal error - see chained exception for cause",
    "UnsupportedConversionException while converting parsed value",
    "propertyNames[] and/or values[] are null or different sizes",
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
   * Creates a ModelException with a reason code (which will display its message).
   */
  public ModelException(int reason) {
    super(getReasonMessage(reason), reason);
  }

  /**
   * Creates a ModelException with a reason code (which will display its message).
   */
  public ModelException(int reason, Throwable cause) {
    super(getReasonMessage(reason), reason, cause);
  }

  /**
   * Creates a ModelException with a reason code (which will display its message).
   */
  public ModelException(int reason, String diagnosticInfo) {
    super(getReasonMessage(reason), reason);
  }

  // ************************************************************************
  //                       Protected Constructors
  // ************************************************************************

  /**
   * Creates a ChainedException with a message.
   * This constructor is only available to subclasses
   */
  protected ModelException(String message, int reason) {
    super(message, reason);
  }

  /**
   * Creates a ChainedException with a message.
   * This constructor is only available to subclasses.
   * This constructor appends the cause message to given message separated
   * by a ": ".  It then stores the message, reason code and cause.
   */
  protected ModelException(String message, int reason, Throwable cause) {
    super(message, reason, cause);
  }
}
