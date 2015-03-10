/**
 * DefaultCheckboxModel - a model which holds a selected/unselected state.
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
 * <p>The <code>setSelected</code> method simply invokes the <code>setText</code>
 * method with either the <code>selectedValue</code> or
 * <code>unselectedValue</code>. The <code>isSelected</code> method simply
 * invokes the <code>getText</code> method and returns true if the value equals
 * the <code>selectedValue</code>.
 * </p>
 * <p>The <code>set/getText</code> methods provide any required parsing or
 * formatting as they transform the value between its <code>String</code>
 * representation and actual internal representation (<code>int</code>,
 * <code>Date</code>, <code>boolean</code>, etc). There are 2 properties which
 * govern the parse/format process: <code>format</code> and
 * <code>formatPattern</code>. These use the standard
 * <code>java.text.Format</code> objects and patterns.
 * <p>
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
public class DefaultCheckboxModel extends DefaultTextModel implements CheckboxModel {
  private String selectedValue = "true";
  private String unselectedValue = "";

  // *************************************************************************
  //                            Constructors
  // *************************************************************************

  /**
   * Constructs a new DefaultCheckboxModel
   * The default valueHolder for this model is a String VariantValueHolder.
   */
  public DefaultCheckboxModel() {
  }

  /**
   * Constructs new DefaultCheckboxModel and sets valueHolder to a
   * VariantValueHolder for given data type.
   * @param the data type for the VariantValueHolder (DataType.TYPE_XXXX)
   * @throws UnsupportedDataTypeException if invalid javaDataType is given
   * @see com.taursys.util.DataTypes
   */
  public DefaultCheckboxModel(int javaDataType) throws UnsupportedDataTypeException {
    super(javaDataType);
  }

  // *************************************************************************
  //                              Public Methods
  // *************************************************************************

  /**
   * Returns text/display value of current state.  If the underlying valueHolder
   * value is null, the unselectedValue will be returned.
   * @throws UnknownStateValueException if the given value does not match the
   * selected or unselected values.
   * @throws ModelException if problem while matching properties of valueHolder
   * object to list object.
   */
  public String getText() throws UnknownStateValueException, ModelException {
    String v = super.getText();
    if (v == "" || v.equals(unselectedValue)) {
      return unselectedValue;
    } else if (v.equals(selectedValue)) {
      return selectedValue;
    } else {
      throw new UnknownStateValueException(
          UnknownStateValueException.REASON_UNKNOWN_VALUE, v);
    }
  }

  /**
   * Sets the current state by matching the given value to the selected or
   * unselected values.
   * @throws UnknownStateValueException if the given value does not match the
   * selected or unselected values.
   * @throws ModelException if problem while setting properties of object in
   * valueHolder.
   */
  public void setText(String value) throws UnknownStateValueException,
      ModelException {
    if (value == null || value.equals("") || value.equals(selectedValue)
        || value.equals(unselectedValue)) {
      super.setText(value);
    } else {
      throw new UnknownStateValueException(
          UnknownStateValueException.REASON_UNKNOWN_VALUE, value);
    }
  }

  /**
   * Sets the current state as selected(true) or unselected(false).
   * This method stores either the selectedValue or unselectedValue as the
   * text value depending on the given value.
   * @throws ModelException if problem while setting properties of object in
   * valueHolder.
   */
  public void setSelected(boolean newSelected) throws ModelException {
    if (newSelected) {
      setText(selectedValue);
    } else {
      setText(unselectedValue);
    }
  }

  /**
   * Gets the current state as selected(true) or unselected(false).
   * This method compares the selectedValue to the text value to determine
   * the state.
   * @throws UnknownStateValueException if the given value does not match the
   * selected or unselected values.
   * @throws ModelException if problem while setting properties of object in
   * valueHolder.
   */
  public boolean isSelected() throws UnknownStateValueException, ModelException {
    String v = super.getText();
    if (v == "" || v.equals(unselectedValue)) {
      return false;
    } else if (v.equals(selectedValue)) {
      return true;
    } else {
      throw new UnknownStateValueException(
          UnknownStateValueException.REASON_UNKNOWN_VALUE, v);
    }
  }

  // *************************************************************************
  //                      Property Accessor Methods
  // *************************************************************************

  /**
   * Set the value used to indicate a selected state.
   * Default value is "true".
   * @param newSelectedValue the value used to indicate a selected state.
   */
  public void setSelectedValue(String newSelectedValue) {
    selectedValue = newSelectedValue;
  }

  /**
   * Get the value used to indicate a selected state.
   * Default value is "true".
   * @return the value used to indicate a selected state.
   */
  public String getSelectedValue() {
    return selectedValue;
  }

  /**
   * Set the value used to indicate an unselected state.
   * Default value is "" (blank).
   * @param newUnselectedValue the value used to indicate a unselected state.
   */
  public void setUnselectedValue(String newUnselectedValue) {
    unselectedValue = newUnselectedValue;
  }

  /**
   * Get the value used to indicate an unselected state.
   * Default value is "" (blank).
   * @return the value used to indicate a unselected state.
   */
  public String getUnselectedValue() {
    return unselectedValue;
  }
}
