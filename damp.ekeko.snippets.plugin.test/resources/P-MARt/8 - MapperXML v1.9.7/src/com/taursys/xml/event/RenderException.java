/**
 * RenderException - General Exception for problems encountered in Model objects.
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
package com.taursys.xml.event;

import com.taursys.util.ChainedException;

/**
 * General Exception for problems encountered in Model objects.
 */
public class RenderException extends ChainedException {
  public static final int REASON_OFFSET                       = 2000;
  public static final int REASON_MODEL_EXCEPTION              = 0 + REASON_OFFSET;
  public static final int REASON_PARENT_CONTAINER_NULL        = 1 + REASON_OFFSET;
  public static final int REASON_DOCUMENT_IS_NULL             = 2 + REASON_OFFSET;
  private static final String[] messages = new String[] {
    "ModelException occurred during rendering.",
    "Parent container is null",
    "Document and/or DocumentAdapter is null",
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
  public RenderException(int reason) {
    super(getReasonMessage(reason), reason);
  }

  /**
   * Creates a ModelException with a reason code (which will display its message) and cause.
   */
  public RenderException(int reason, Throwable cause) {
    super(getReasonMessage(reason), reason, cause);
  }
}
