/**
 * DefaultSelectModel - Model which holds a list and the selected item
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

import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import com.taursys.util.DataTypes;
import com.taursys.util.UnsupportedDataTypeException;
import com.taursys.debug.Debug;

/**
 * Implementation of the <code>SelectModel</code> which holds a list of options
 * and the selected item. This model can be used in a variety of ways. It can
 * be used in an un-bound mode, where the current selection is maintained
 * internally. It can also be used in a bound mode where the current selection
 * is propagated to a value holder. When used in the bound mode, either a
 * single property, or multiple properties can be set in the value holder.
 * <p>
 * The following sections describe the required settings to make for each
 * of the modes.
 * <p>
 * <b>Un-bound Mode (uses internal VariantValueHolder)</b>
 * <p>
 * When used in this mode this component uses an internal
 * <code>VariantValueHolder</code> to hold the current selection. By default a
 * <code>VariantValueHolder</code> is created with a data type of
 * <code>String</code>. To set a different data type use the constructor which
 * takes a data type as a parameter. (example
 * <code>new DefaultSelectModel(DataTypes.TYPE_INT)</code>).
 * <p>
 * To use this component in the un-bound mode, you must set the following
 * properties:
 * <ul>
 * <li><code>list</code> - should be set to a type of
 * <code>CollectionHolder</code> which holds the list of options
 * (see "Setting the List").
 * </li>
 * <li><code>displayPropertyName</code> - the name of the property to display
 * (property of objects in list). Example: given a list of "Location" objects
 * with a property called "locationName", use
 * <code>setDisplayPropertyName("locationName")</code>
 * to display the zipCode. IMPORTANT - The displayed property choosen must
 * result in a unique list of values, otherwise the intended value may not be
 * selected/displayed.
 * </li>
 * <li><code>listPropertyNames</code> - set a single source property name in the
 * <code>list</code> objects. Example: Given a list containing "Location"
 * objects which has properties "zipCode", "cityName", "stateAbbr", and
 * "country", to make the zipcode the internal value use
 * <code>setListPropertyNames(new String[] {"zipCode"})</code>.
 * <p>
 * If the <code>list</code> is an <code>ObjectArrayValueHolder</code>, then
 * the property name should always be a single "value" (which is the default).
 * </li>
 * <li><code>propertyName</code> - must be "value" (which is the default).
 * </li>
 * <li><code>nullDisplay</code> - String to display in list for null selection.
 * Example: "--- Nothing Selected ---"
 * </li>
 * </ul>
 * <b>Bound Mode</b>
 * <p>
 * To use this component in the bound mode, use the same properties as
 * described in the Un-bound Mode, plus the following additional properties:
 * <ul>
 * <li><code>valueHolder</code> - should be set to the target
 * <code>ValueHolder</code> which contains the current selection and will be
 * updated if the selection is changed.
 * </li>
 * <li><code>propertyName</code> - set this to the first (or only)
 * <code>ValueHolder</code> object property name which will be bound to the
 * selection. Example: given a <code>ValueHolder</code> with an
 * "Address" object which has a "zipCode" property, use
 * <code>setPropertyName("zipCode")</code> to store the current selection in
 * the "Address.zipCode" property.
 * </li>
 * <li><code>propertyNames</code> - use this when you want to set more than
 * 1 property in the <code>ValueHolder</code> object. Example: assume you want
 * to set not only the "zipCode" property, but also the "city", "state" and
 * "country" properties, use
 * <code>setPropertyNames(new String[] {"zipCode","city","state","country"})</code>
 * </li>
 * <li><code>listPropertyNames</code> - set the to the source property name(s)
 * in the <code>list</code> objects. IMPORTANT - The name(s) of the
 * <code>listPropertyNames</code> properties MUST be
 * in the same ORDER as the <code>propertyName(s)</code>. The names in the
 * <code>listPropertNames</code> may be different than the names in the
 * <code>propertyNames</code> since they are associated
 * with in objects in the <code>list</code>, not the <code>valueHolder</code>.
 * Example: Given a <code>list</code>
 * containing "Location" objects which has properties "zipCode", "cityName",
 * "stateAbbr", and "country", for a single property use
 * <code>setListPropertyNames(new String[] {"zipCode"})</code>. For
 * multiple properties use
 * <code>setListPropertyNames(new String[] {"zipCode", "cityName", "stateAbbr",
 * "country"})</code>
 * <p>
 * If the <code>list</code> is an <code>ObjectArrayValueHolder</code>, then
 * the property name should always be a single "value" (which is the default).
 * </li>
 * </ul>
 * <b>Setting the List</b>
 * <p>
 * The <code>list</code> must be a type of <code>CollectionValueHolder</code>
 * (example: <code>VOCollectionValueHolder</code> or
 * <code>VOListValueHolder</code>).
 * The holder can contain any type of object (but they must all be instances
 * of the same class).
 * <p>
 * If the <code>list</code> is an <code>ObjectArrayValueHolder</code>, then the
 * <code>toString()</code> method is used as the display value (regardless of
 * the <code>displayPropertyName</code>). If used in the bound mode, the whole
 * object itself is stored in the target ValueHolder's object (regardless of
 * the property names listed in the <code>setListPropertyNames</code> method).
 * It is important to make sure that the <code>valueHolder</code> property
 * is the same type as the objects in the <code>ObjectArrayValueHolder</code>
 * or a <code>ModelException</code> will occur.
 * <p>
 * You can also preset the <code>list</code> in the constructor by passing it an
 * array of Objects to be used for the <code>list</code>. The resulting
 * <code>list</code> will be an <code>ObjectArrayValueHolder</code>.
 * <p>
 * <b>Other Important Information</b>
 * <p>
 * When a selection is made, the values are copied from the properties in the
 * <code>list</code> to the properties in the <code>valueHolder</code> object.
 * The property names in <code>propertyNames[]</code> and
 * <code>listPropertyNames[]</code> must appear in a corresponding order.
 * <p>
 * A "null" item will always be added to the <code>displayOptionList</code>.
 * When the "null" item is selected, a null will be assigned to the
 * <code>propertyNames[]</code> in the <code>valueHolder</code> object.
 * The actual "null" item to to display is defined by the
 * <code>nullDisplay</code> (default is "--none--").
 * <p>
 * The <code>format</code> and <code>formatPattern</code> govern the display
 * property in this component.  The <code>getText</code> method returns the
 * formatted display property, while the <code>setText</code>
 * method changes the current selection to one whose display matches the given
 * value. If you attempt to <code>setText</code> for an item that is not in
 * the list, a <code>NotInListException</code> will be thrown.
 */
public class DefaultSelectModel extends DefaultTextModel implements SelectModel {
  private com.taursys.model.CollectionValueHolder list;
  private String displayPropertyName = "value";
  private String[] listPropertyNames = new String[] {"value"};
  private String[] propertyNames = new String[] {"value"};
  private String nullDisplay = "--none--";
  private boolean nullAllowed = true;

  // *************************************************************************
  //                            Constructors
  // *************************************************************************

  /**
   * Constructs a new DefaultSelectModel
   * The default valueHolder for this model is a String VariantValueHolder.
   * The default list for this model is an ObjectArrayValueHolder with an
   * empty Object array.
   */
  public DefaultSelectModel() {
    setList(createDefaultList());
  }

  /**
   * Constructs new DefaultSelectModel and sets valueHolder to a
   * VariantValueHolder for given data type.
   * The default list for this model is an ObjectArrayValueHolder with an
   * empty Object array.
   * @param the data type for the VariantValueHolder (DataType.TYPE_XXXX)
   * @throws UnsupportedDataTypeException if invalid javaDataType is given
   * @see com.taursys.util.DataTypes
   */
  public DefaultSelectModel(int javaDataType) throws UnsupportedDataTypeException {
    super(javaDataType);
    setList(createDefaultList());
  }

  // *************************************************************************
  //                            Default Creators
  // *************************************************************************

  /**
   * Constructs a default list for this model.  The default list is an
   * ObjectArrayValueHolder with an empty Object array.
   */
  protected CollectionValueHolder createDefaultList() {
    return new ObjectArrayValueHolder(new Object[]{});
  }

  // *************************************************************************
  //                              Public Methods
  // *************************************************************************

  /**
   * Returns display value of current selection.
   * @throws NotInListException if current valueHolder object values do not
   * match any item in list.
   * @throws ModelException if problem while matching properties of valueHolder
   * object to list object.
   */
  public String getText() throws com.taursys.model.ModelException {
    if (positionListToCurrentSelection())
      return getCurrentDisplayValue();
    else
      return nullDisplay;
  }

  /**
   * Sets the current selection by matching the given value to the list's display values.
   * This method also copies the corresponding values from the selected list
   * object to the valueHolder object.  If the given value is null or a value
   * matching the nullDisplay, this model will set the propertyNames[] in the
   * valueHolder object to null.
   * @throws NotInListException if the given value does not match any item in list.
   * @throws ModelException if problem while setting properties of object in
   * valueHolder or fetching display values from the list.
   */
  public void setText(String value) throws ModelException {
    if (value == null || value.equals(nullDisplay)) {
      if (nullAllowed) {
        setNullValues();
        return;
      } else {
        throw new NotInListException(
            NotInListException.GIVEN_VALUE_NOT_IN_LIST, value);
      }
    }
    list.reset();
    while (list.hasNext()) {
      list.next();
      if (value.equals(getCurrentDisplayValue())) {
        copyValues();
        return;
      }
    }
    throw new NotInListException(NotInListException.GIVEN_VALUE_NOT_IN_LIST, value);
  }

  /**
   * Returns the currently selected item from the list.
   * @throws NotInListException if current valueHolder object values do not
   * match any item in list.
   * @throws ModelException if problem while matching properties of valueHolder
   * object to list object.
   */
  public Object getSelectedItem() throws com.taursys.model.ModelException {
    if (positionListToCurrentSelection())
      return list.getObject();
    else
      return null;
  }

  /**
   * Sets the current selection by searching through the list for the given object.
   * This method also copies the corresponding values from the selected list
   * object to the valueHolder object.  If the given value is null or a value
   * matching the nullDisplay, this model will set the propertyNames[] in the
   * valueHolder object to null.
   * @throws NotInListException if the given value does not match any item in list.
   * @throws ModelException if problem while setting properties of object in
   * valueHolder or fetching display values from the list.
   */
  public void setSelectedItem(Object value) throws ModelException {
    if (value == null) {
      setNullValues();
      return;
    }
    list.reset();
    while (list.hasNext()) {
      list.next();
      if (value.equals(list.getObject())) {
        copyValues();
        return;
      }
    }
    throw new NotInListException(NotInListException.GIVEN_VALUE_NOT_IN_LIST,
      value.toString());
  }

  /**
   * Returns a Collection of the SelectModelOptions.  The SelectModelOption
   * has 2 properties: optionText which is the value to display in the list
   * and selected, which indicates whether or not the item is selected.
   * <p>
   * The nullDisplay value will be first in the collection.
   * @throws ModelException if problem while matching properties of valueHolder
   * object to list object.
   */
  public Collection getDisplayOptionList() throws ModelException {
    ArrayList arrayList = new ArrayList();
    boolean found = false;
    if (nullAllowed) {
      found = isValueNull();
      arrayList.add(new SelectModelOption(nullDisplay, found));
    }
    list.reset();
    while (list.hasNext()) {
      list.next();
      String displayValue = getCurrentDisplayValue();
      if (isCurrentMatch()) {
        arrayList.add(new SelectModelOption(displayValue, true));
        found = true;
      } else {
        arrayList.add(new SelectModelOption(displayValue, false));
      }
    }
    if (!found)
      Debug.warn("Current selection not in list. "
          + getCurrentPropertyValues());
    return arrayList;
  }

  // *************************************************************************
  //                          Internal Methods
  // *************************************************************************

  /**
   * Positions the list to the current selection as indicated by the valueHolder.
   * If the current selection is null then the list is put in the "reset"
   * position and this method returns false.
   * @returns true if valid position
   * @throws NotInListException if the current selection is not in the list.
   * @throws ModelException if problem while matching properties of valueHolder
   * object to list object.
   */
  private boolean positionListToCurrentSelection() throws ModelException {
    list.reset();
    if (isValueNull())
      return false;
    while (list.hasNext()) {
      list.next();
      if (isCurrentMatch())
        return true;
    }
    throw new NotInListException(NotInListException.CURRENT_VALUE_NOT_IN_LIST);
  }

  /**
   * Returns the Display value of the current position in the option list.
   * This is not necessarily the currently selected item.
   * @throws ModelException if problem occurs while accessing value
   */
  private String getCurrentDisplayValue() throws ModelException {
    Object value = getList().getPropertyValue(displayPropertyName);
    int javaDataType = getList().getJavaDataType(displayPropertyName);
    if (value == null)
      return "";
    Format form = getFormat();
    if (form == null)
      return DataTypes.format(javaDataType, value);
    else if (form instanceof MessageFormat)
      return ((MessageFormat)form).format(new Object[] {value});
    else
      return form.format(value);
  }

  protected String getCurrentPropertyValues() throws ModelException {
    String values = "";
    for (int i = 0; i < propertyNames.length; i++)
      values += propertyNames[i] + "=" +
          getValueHolder().getPropertyValue(propertyNames[i]);
    return values;
  }

  /**
   * Compares the corresponding property values of the current list with valueHolder object.
   * @returns true if values match
   * @throws ModelException if problem while accessing properties of valueHolder
   * object or list object.
   */
  protected boolean isCurrentMatch() throws ModelException {
    validateProperties();
    for (int i = 0; i < propertyNames.length; i++) {
      Object value = getValueHolder().getPropertyValue(propertyNames[i]);
      Object listValue = getList().getPropertyValue(listPropertyNames[i]);
      if ( value == null || !value.equals(listValue)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if all of the properties (propertyNames[]) are null.
   * @throws ModelException if problem accessing properties of valueHolder object.
   */
  protected boolean isValueNull() throws ModelException {
    validateProperties();
    for (int i = 0; i < propertyNames.length; i++) {
      Object value = getValueHolder().getPropertyValue(propertyNames[i]);
      if ( value != null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks integrity of propertyNames and listPropertyNames arrays. are not
   * @throws ModelException if either are null/empty or if different sizes.
   */
  private void validateProperties() throws ModelException {
    if (propertyNames == null || listPropertyNames == null
        || propertyNames.length != listPropertyNames.length) {
      throw new SelectModelException(
          SelectModelException.REASON_HOLDER_LIST_MISMATCH,
          displayPropertyName, listPropertyNames, propertyNames, true,
          nullDisplay);
    }
  }

  /**
   * Copies the corresponding values from the current listObject to the valueHolder object.
   * @throws ModelException if problem copying values
   */
  protected void copyValues() throws ModelException {
    validateProperties();
    for (int i = 0; i < propertyNames.length; i++) {
      Object listValue = getList().getPropertyValue(listPropertyNames[i]);
      getValueHolder().setPropertyValue(propertyNames[i], listValue);
    }
  }

  /**
   * Sets the propertyNames[] in the valueHolder object to null.
   * @throws ModelException if problem occurs
   */
  protected void setNullValues() throws ModelException {
    validateProperties();
    for (int i = 0; i < propertyNames.length; i++) {
      getValueHolder().setPropertyValue(propertyNames[i], null);
    }
  }

  /**
   * Creates an ObjectValueHolder as the default ValueHolder for model.
   */
  protected ValueHolder createDefaultValueHolder() {
    return new ObjectValueHolder();
  }

  // *************************************************************************
  //                      Property Accessor Methods
  // *************************************************************************

  /**
   * Sets collectionValueHolder which holds the collection of possible selections.
   */
  public void setList(com.taursys.model.CollectionValueHolder newList) {
    list = newList;
  }

  /**
   * Returns collectionValueHolder which holds the collection of possible selections.
   */
  public com.taursys.model.CollectionValueHolder getList() {
    return list;
  }

  /**
   * Sets the property name of the list object to display in the list.
   * Default is "value".
   */
  public void setDisplayPropertyName(String newDisplayPropertyName) {
    displayPropertyName = newDisplayPropertyName;
  }

  /**
   * Returns the property name of the list object to display in the list.
   * Default is "value".
   */
  public String getDisplayPropertyName() {
    return displayPropertyName;
  }

  /**
   * Sets array of property names in list object to copy to valueHolder object.
   * Default is {"value"}.
   */
  public void setListPropertyNames(String[] newListPropertyNames) {
    listPropertyNames = newListPropertyNames;
  }

  /**
   * Returns array of property names in list object to copy to valueHolder object.
   * Default is {"value"}.
   */
  public String[] getListPropertyNames() {
    return listPropertyNames;
  }

  /**
   * Sets array of property names in valueHolder object that correspond to properties of list object.
   * Default is {"value"}.
   */
  public void setPropertyNames(String[] newPropertyNames) {
    propertyNames = newPropertyNames;
  }

  /**
   * Gets array of property names in valueHolder object that correspond to properties of list object.
   * Default is {"value"}.
   */
  public String[] getPropertyNames() {
    return propertyNames;
  }

  /**
   * Sets the property name in valueHolder that will hold the value from the list.
   * If you have also used the setPropertyNames method, then this will change the
   * first name in that array.
   */
  public void setPropertyName(String newPropertyName) {
    propertyNames[0] = newPropertyName;
  }

  /**
   * Gets the property name in valueHolder that will hold the value from the list.
   * If you have also used the setPropertyNames method, then this will return the
   * first name in that array.
   */
  public String getPropertyName() {
    return propertyNames[0];
  }

  /**
   * Sets indicator that a null value is a valid selection. If true, the
   * <code>nullDisplay</code> value will appear in the list of options
   * generated by <code>getDisplayOptionList</code> and <code>setText</code>
   * will accept null or the <code>nullDisplay</code> value. If false, the
   * <code>setText</code> method will throw a <code>NotInListException</code>
   * if null or the <code>nullDisplay</code> value is set, and the
   * <code>nullDisplay</code> will not appear in the <code>displayOptionList</code>.
   * Default is true.
   */
  public void setNullAllowed(boolean nullAllowed) {
    this.nullAllowed = nullAllowed;
  }

  /**
   * Returns indicator that a null value is a valid selection. If true, the
   * <code>nullDisplay</code> value will appear in the list of options
   * generated by <code>getDisplayOptionList</code> and <code>setText</code>
   * will accept null or the <code>nullDisplay</code> value. If false, the
   * <code>setText</code> method will throw a <code>NotInListException</code>
   * if null or the <code>nullDisplay</code> value is set, and the
   * <code>nullDisplay</code> will not appear in the <code>displayOptionList</code>.
   * Default is true.
   */
  public boolean isNullAllowed() {
    return nullAllowed;
  }

  /**
   * Sets value to display in list for a null value.
   */
  public void setNullDisplay(String newNullDisplay) {
    nullDisplay = newNullDisplay;
  }

  /**
   * Returns value to display in list for a null value.
   */
  public String getNullDisplay() {
    return nullDisplay;
  }
}
