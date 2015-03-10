/**
 * ValueObjectInfo -
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
package com.taursys.tools;

/**
 * ValueObjectInfo is ...
 * @author Marty Phelan
 * @version 1.0
 */
public class ValueObjectPropertyInfo {
  private String propertyName;
  private String propertyType;
  private int binding;
  private boolean getter;
  private boolean setter;
  private boolean expose;
  private String displayName;
  private String toolTip;
  private String getSetDescription;
  private String longDescription;

  /**
   * Constructs a new ValueObjectPropertyInfo
   */
  public ValueObjectPropertyInfo() {
  }

  public String getInstanceVarDeclSourceCode() {
    return "private " + propertyType + " " + propertyName;
  }

  public String getPropertyNameCapitalized() {
    return propertyName.substring(0,1).toUpperCase() + propertyName.substring(1);
  }
  public String getPropertyName() {
    return propertyName;
  }
  public void setPropertyName(String newPropertyName) {
    propertyName = newPropertyName;
  }
  public void setPropertyType(String newPropertyType) {
    propertyType = newPropertyType;
  }
  public String getPropertyType() {
    return propertyType;
  }
  public void setBinding(int newBinding) {
    binding = newBinding;
  }
  public int getBinding() {
    return binding;
  }
  public void setGetter(boolean newGetter) {
    getter = newGetter;
  }
  public boolean isGetter() {
    return getter;
  }
  public void setSetter(boolean newSetter) {
    setter = newSetter;
  }
  public boolean isSetter() {
    return setter;
  }
  public void setExpose(boolean newExpose) {
    expose = newExpose;
  }
  public boolean isExpose() {
    return expose;
  }
  public void setDisplayName(String newDisplayName) {
    displayName = newDisplayName;
  }
  public String getDisplayName() {
    return displayName;
  }
  public void setToolTip(String newToolTip) {
    toolTip = newToolTip;
  }
  public String getToolTip() {
    return toolTip;
  }
  public void setGetSetDescription(String newGetSetDescription) {
    getSetDescription = newGetSetDescription;
  }
  public String getGetSetDescription() {
    return getSetDescription;
  }
  public void setLongDescription(String newLongDescription) {
    longDescription = newLongDescription;
  }
  public String getLongDescription() {
    return longDescription;
  }
}
