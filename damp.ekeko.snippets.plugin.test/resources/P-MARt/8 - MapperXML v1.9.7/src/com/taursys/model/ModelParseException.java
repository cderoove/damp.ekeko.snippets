/**
 * ModelParseException - Exceptions encountered while parsing text value
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
import java.text.Format;

/**
 * ModelExceptions encountered while parsing text value.  This exception
 * stores information about the reason and various other meaningful data
 * whenever an Exception occurs during a Parse operation.
 */
public class ModelParseException extends ModelException {
  public static final int REASON_OFFSET                       =
      ModelException.REASON_OFFSET + 300;
  public static final int REASON_PARSE_ERROR                  = 0 + REASON_OFFSET;
  protected static final String[] messages = new String[] {
    "Problem parsing given value.",
  };
  private String propertyName;
  private String propertyDataType;
  private String givenValue;
  private String formatClass;
  private String formatPattern;

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
   * Constructs exception for given propertyName, data type, etc.
   */
  public ModelParseException(String propertyName, int javaDataType,
    Format format, String pattern, String givenValue, Throwable cause) {
    super(getReasonMessage(REASON_PARSE_ERROR), REASON_PARSE_ERROR, cause);
    this.propertyName = propertyName;
    this.propertyDataType = DataTypes.getJavaNameForType(javaDataType);
    if (format == null)
      this.formatClass = "null";
    else
      this.formatClass = format.getClass().getName();
    this.formatPattern = pattern;
    this.givenValue = givenValue;
    this.userFriendlyMessage = "The value you entered, \""
        + givenValue + "\" is not valid. Please try again.";
    setDiagnosticsInfo();
  }

  // ************************************************************************
  //                   Prepare Diagnostics Info Methods
  // ************************************************************************

/** @todo Prepare the User Friendly message for this Exception */

  /**
   * Builds the diagnosticsInfo String from the current property values.
   */
  protected void setDiagnosticsInfo() {
    diagnosticInfo =
        "Property Name=" + propertyName +
        "(" + propertyDataType + ")" +
        "\nFormat Class=" + formatClass +
        "\nFormat Pattern=" + formatPattern +
        "\nGiven Value=" + givenValue;
  }

  // ************************************************************************
  //                       Property Accessors
  // ************************************************************************

  /**
   * Gets the data type of the property where the parsed value would be stored
   */
  public String getPropertyDataType() {
    return propertyDataType;
  }

  /**
   * Gets property name where the parsed value would be stored
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Gets the given value which was being parsed
   */
  public String getGivenValue() {
    return givenValue;
  }

  /**
   * Gets the Format object which was trying to parse given value
   */
  public String getFormatClass() {
    return formatClass;
  }

  /**
   * Gets the Pattern used by the Format object to parse the given value
   */
  public String getFormatPattern() {
    return formatPattern;
  }
}
