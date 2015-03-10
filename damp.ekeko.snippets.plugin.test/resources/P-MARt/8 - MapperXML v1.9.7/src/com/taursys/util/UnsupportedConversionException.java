/**
 * UnsupportedConversionException - Exception for conversions which are not supported
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
package com.taursys.util;

/**
 * Exception for unsupported conversions.
 */
public class UnsupportedConversionException extends Exception {

  /**
   * Creates new exception with no message
   */
  public UnsupportedConversionException() {
    super();
  }

  /**
   * Creates new exception with given message
   */
  public UnsupportedConversionException(String message) {
    super(message);
  }
}
