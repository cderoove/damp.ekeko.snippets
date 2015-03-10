/**
 * ValueHolderInfo - Information about a ValueHolder for a new class
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

import java.util.Vector;

/**
 * ValueHolderInfo - Information about a ValueHolder for a new class
 * @author Marty Phelan
 * @version 1.0
 */
public class ValueHolderInfo {
  private String holderType;
  private String containedClassName;
  private String holderName;
  private String holderAlias;

  /**
   * Constructs a new ValueHolderInfo with all properties null.
   */
  public ValueHolderInfo() {
  }

  /**
   * Constructs a new ValueHolderInfo with the given property values.
   * @param holderType fully qualified class name for type of holder
   * @param newContainedClassName the class name of the object contained by the holder.
   * @param newHolderName the holder's instance variable name within the code.
   * @param newHolderAlias the alias used in html/xml document ID's for auto-binding to the holder.
   */
  public ValueHolderInfo(
      String holderType,
      String containedClassName,
      String holderName,
      String holderAlias
      ) {
    this.holderType = holderType;
    this.containedClassName = containedClassName;
    this.holderName = holderName;
    this.holderAlias = holderAlias;
  }

  /**
   * Get the instance variable declaration source code.
   * Given current property values of this object:
   * holderType="com.taursys.model.VOValueHolder", containedClassName=
   * "com.taursys.examples.simpleweb.Person", holderName="personHolder", and
   * holderAlias="PERSON", the following value would be returned:
   * <pre>
   * private com.taursys.model.VOValueHolder personHolder =
   * new com.taursys.model.VOValueHolder()
   * </pre>
   */
  public String getInstanceVarDeclSourceCode() {
    return "private " + holderType + " " + holderName
        + " = new " + holderType + "()";
  }

  /**
   * Get a vector of the source code lines to be placed in jbInit method for this holder.
   */
  public Vector getJbInitSourceCode() {
    Vector source = new Vector();
    if (containedClassName != null && containedClassName.length() > 0)
      source.add(holderName + ".setValueObjectClass(" + containedClassName + ".class)");
    if (holderAlias != null && holderAlias.length() > 0)
      source.add(holderName + ".setAlias(\"" + holderAlias + "\")");
    return source;
  }

  /**
   * Get fully qualified class name for type of holder
   * @return fully qualified class name for type of holder
   */
  public String getHolderType() {
    return holderType;
  }

  /**
   * Set fully qualified class name for type of holder
   * @param holderType fully qualified class name for type of holder
   */
  public void setHolderType(String holderType) {
    this.holderType = holderType;
  }

  /**
   * Set the class name of the object contained by the holder.
   * @param newContainedClassName the class name of the object contained by the holder.
   */
  public void setContainedClassName(String newContainedClassName) {
    containedClassName = newContainedClassName;
  }

  /**
   * Get the class name of the object contained by the holder.
   * @return the class name of the object contained by the holder.
   */
  public String getContainedClassName() {
    return containedClassName;
  }

  /**
   * Set the holder's instance variable name within the code.
   * @param newHolderName the holder's instance variable name within the code.
   */
  public void setHolderName(String newHolderName) {
    holderName = newHolderName;
  }

  /**
   * Get the holder's instance variable name within the code.
   * @return the holder's instance variable name within the code.
   */
  public String getHolderName() {
    return holderName;
  }

  /**
   * Set the alias used in html/xml document ID's for auto-binding to the holder.
   * @param newHolderAlias the alias used in html/xml document ID's for auto-binding to the holder.
   */
  public void setHolderAlias(String newHolderAlias) {
    holderAlias = newHolderAlias;
  }

  /**
   * Get the alias used in html/xml document ID's for auto-binding to the holder.
   * @return the alias used in html/xml document ID's for auto-binding to the holder.
   */
  public String getHolderAlias() {
    return holderAlias;
  }
}
