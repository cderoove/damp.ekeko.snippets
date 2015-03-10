/**
 * NullObject - a placeholder for a null value
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
package com.taursys.swing;

import javax.swing.Icon;

/**
 * NullObject is a placeholder for a null value. It has a two properties
 * which are intended for display purposes.
 * @author Marty Phelan
 * @version 1.0
 */
public class NullObject {
  private String text = "-- none --";
  private javax.swing.Icon icon = null;

  /**
   * Constructs a new NullObject
   */
  public NullObject() {
  }

  /**
   * Set the text value for this NullObject.
   * This is typically used as a display property.
   * @param text the text value for this NullObject.
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Get the text value for this NullObject.
   * This is typically used as a display property.
   * @return the text value for this NullObject.
   */
  public String getText() {
    return text;
  }

  /**
   * Set the icon value for this NullObject.
   * This is typically used as a display property.
   * @param icon the icon value for this NullObject.
   */
  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  /**
   * Get the icon value for this NullObject.
   * This is typically used as a display property.
   * @return the icon value for this NullObject.
   */
  public Icon getIcon() {
    return icon;
  }

  /**
   * Get the String that represents this object (default is text).
   * @return the String that represents this object (default is text).
   */
  public String toString() {
    return text;
  }
}
