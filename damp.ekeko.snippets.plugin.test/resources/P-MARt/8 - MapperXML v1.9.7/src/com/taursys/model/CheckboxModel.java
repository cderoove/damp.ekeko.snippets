/**
 * CheckboxModel - a model which holds a selected/unselected state.
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

import com.taursys.util.UnsupportedDataTypeException;

/**
 * <p><code>CheckboxModel</code> is a model which maintains 2 states: selected
 * or unselected. The <code>CheckboxModel</code> provides access to the internal
 * "state" or value via the <code>setText</code>, <code>getText</code>,
 * <code>setSelected</code> and <code>isSelected</code> methods.</p>
 * <p>The <code>setSelected</code> should result in either the
 * <code>selectedValue</code> (true) or <code>unselectedValue</code> (false)
 * value being parsed and stored in the <code>valueHolder</code>. The
 * <code>isSelected</code> method should indicate whether or not the value
 * stored in the <code>valueHolder</code> equals (or represents) the
 * <code>selectedValue</code>.
 * </p>
 * <p>It is the responsibility of the <code>set/getText</code> methods to
 * provide any required parsing or formatting as they transform the value
 * between its <code>String</code> representation and actual internal
 * representation (<code>int</code>, <code>Date</code>, <code>boolean</code>,
 * etc). There are 2 properties which govern the parse/format process:
 * <code>format</code> and <code>formatPattern</code>. These use the standard
 * <code>java.text.Format</code> objects and patterns.
 * </p>
 * <p>If the <code>setText</code> method is invoked directly, the value passed
 * to it must be equal to either the <code>selectedValue</code> or
 * <code>unselectedValue</code>, otherwise an
 * <code>UnknownStateValueException</code> will occur.
 * </p>
 * <p>This model supports null as the "unselected" state. This is accomplished
 * by setting the <code>unselectedValue</code> to blank ("").  A initial null
 * value in the value holder is treated as unselected by the isSelected and
 * getText methods.
 * </p>
 * @author Marty Phelan
 * @version 1.0
 */
public interface CheckboxModel extends TextModel {

  // *************************************************************************
  //                              Public Methods
  // *************************************************************************

  /**
   * Returns text/display value of current state.  If the underlying valueHolder
   * value is null, the unselectedValue will be returned.
   * @throws UnknownStateValueException if the given value does not match a
   * known state text value.
   * @throws ModelException if problem while matching properties of valueHolder
   * object to list object.
   */
  public String getText() throws UnknownStateValueException, ModelException;

  /**
   * Sets the current state by matching the given value to the selected or unselected values.
   * @throws UnknownStateValueException if the given value does not match a
   * known state text value.
   * @throws ModelException if problem while setting properties of object in
   * valueHolder.
   */
  public void setText(String value) throws UnknownStateValueException,
      ModelException;

  /**
   * Sets the current state as selected(true) or unselected(false).
   * @throws ModelException if problem while setting properties of object in
   * valueHolder.
   */
  public void setSelected(boolean newSelected) throws ModelException;

  /**
   * Gets the current state as selected(true) or unselected(false).
   * @throws UnknownStateValueException if the internal value does not match a
   * known state text value.
   * @throws ModelException if problem while setting properties of object in
   * valueHolder.
   */
  public boolean isSelected() throws UnknownStateValueException, ModelException;

  // *************************************************************************
  //                      Property Accessor Methods
  // *************************************************************************

  /**
   * Set the value used to indicate a selected state.
   * @param newSelectedValue the value used to indicate a selected state.
   */
  public void setSelectedValue(String newSelectedValue);

  /**
   * Get the value used to indicate a selected state.
   * @return the value used to indicate a selected state.
   */
  public String getSelectedValue();

  /**
   * Set the value used to indicate an unselected state.
   * @param newUnselectedValue the value used to indicate a unselected state.
   */
  public void setUnselectedValue(String newUnselectedValue);

  /**
   * Get the value used to indicate an unselected state.
   * @return the value used to indicate a unselected state.
   */
  public String getUnselectedValue();
}
