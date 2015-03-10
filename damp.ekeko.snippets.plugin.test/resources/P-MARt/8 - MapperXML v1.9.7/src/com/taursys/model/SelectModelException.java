/**
 * SelectModelException - ModelException caused in DefaultSelectModel.
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
 * General Exception for problems encountered in DefaultSelectModel objects.
 */
public class SelectModelException extends ModelException {
  public static final int REASON_OFFSET                       = 200;
  public static final int REASON_HOLDER_LIST_MISMATCH         = 0 + REASON_OFFSET;
  private static final String[] messages = new String[] {
    "Property names for valueHolder object and list object do not correspond",
  };
  private String displayPropertyName;
  private String[] listPropertyNames;
  private String[] propertyNames;
  private boolean nullAllowed;
  private String nullDisplay;

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
  public SelectModelException(int reason, String displayPropertyName,
      String[] listPropertyNames, String[] propertyNames, boolean nullAllowed,
      String nullDisplay) {
    super(getReasonMessage(reason), reason);
    this.displayPropertyName = displayPropertyName;
    this.listPropertyNames = listPropertyNames;
    this.propertyNames = propertyNames;
    this.nullAllowed = nullAllowed;
    this.nullDisplay = nullDisplay;
    setDiagnosticsInfo();
  }

  // ************************************************************************
  //                       Protected Constructors
  // ************************************************************************

  /**
   * Creates a ModelException with a reason code (which will display its message).
   */
  protected SelectModelException(String message, int reason) {
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
        "Display property Name=" + displayPropertyName +
        "\nlistPropertyNames=" + listPropertyNames +
        "\npropertyNames=" + propertyNames +
        "\nnullAllowed=" + nullAllowed +
        "\nnullDisplay=" + nullDisplay;
  }

  // ************************************************************************
  //                       Property Accessors
  // ************************************************************************

  /**
   * Returns the property name of the list object to display in the list.
   */
  public String getDisplayPropertyName() {
    return displayPropertyName;
  }

  /**
   * Returns array of property names in list object to copy to valueHolder object.
   */
  public String[] getListPropertyNames() {
    return listPropertyNames;
  }

  /**
   * Sets array of property names in valueHolder object that correspond to properties of list object.
   */
  public String[] getPropertyNames() {
    return propertyNames;
  }

  /**
   * Returns indicator that a null value is a valid selection.
   */
  public boolean isNullAllowed() {
    return nullAllowed;
  }

  /**
   * Returns value to display in list for a null value.
   */
  public String getNullDisplay() {
    return nullDisplay;
  }
}
