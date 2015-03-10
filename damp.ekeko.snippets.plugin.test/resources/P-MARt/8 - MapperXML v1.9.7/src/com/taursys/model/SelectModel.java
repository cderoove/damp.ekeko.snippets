/**
 * SelectModel - Model interface which holds a list and the selected item
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

import java.util.Collection;

/**
 * Model interface which holds a list of options and the selected item.
 * This model can be used in a variety of ways. It can be used in an
 * un-bound mode, where the current selection is maintained internally. It can
 * also be used in a bound mode where the current selection is propagated to a
 * value holder. When used in the bound mode, either a single property, or
 * multiple properties can be set in the value holder.
 * <p>
 * The following sections describe the required settings to make for each
 * of the modes.
 * <p>
 * <b>Un-bound Mode</b>
 * <p>
 * To use this component in the un-bound mode, you can set the following
 * properties:
 * <ul>
 * <li>list - should be set to a CollectionHolder which holds the list
 * of options. (see Setting the List below for more information)
 * </li>
 * <li>displayPropertyName - the name of the property to display (property of
 * objects in list). Example: given a list of "Location" objects with a property
 * called "locationName", use <code>setDisplayPropertyName("locationName")</code>
 * to display the zipCode. IMPORTANT - The displayed property choosen must
 * result in a unique list of values, otherwise the intended value may not be
 * selected/displayed.
 * </li>
 * <li>nullDisplay - String to display in list for null selection. Example:
 * "--- Nothing Selected ---"
 * </li>
 * </ul>
 * <b>Bound Mode</b>
 * <p>
 * To use this component in the bound mode, use the same properties as
 * described in the Un-bound Mode, plus the following additional properties:
 * <ul>
 * <li>valueHolder - should be set to the target ValueHolder which contains
 * the current selection and will be updated if the selection is changed.
 * </li>
 * <li>propertyName - set this to the first (or only) ValueHolder object property
 * name which will be bound to the selection. Example: given a ValueHolder with an
 * "Address" object which has a "zipCode" property, use
 * <code>setPropertyName("zipCode")</code> to store the current selection in
 * the "Address.zipCode" property.
 * </li>
 * <li>propertyNames - use this when you want to set more than 1 property in the
 * ValueHolder object. Example: assume you want to set not only the "zipCode"
 * property, but also the "city", "state" and "country" properties, use
 * <code>setPropertyNames(new String[] {"zipCode","city","state","country"})</code>
 * </li>
 * <li>listPropertyNames - set the to the source property name(s) in the List
 * objects. IMPORTANT - The name(s) of the listPropertyNames properties MUST be
 * in the same ORDER as the propertyName(s). The names in the listPropertNames
 * may be different than the names in the propertyNames since they are associated
 * with in objects in the List, not the ValueHolder. Example: Given a List
 * containing "Location" objects which has properties "zipCode", "cityName",
 * "stateAbbr", and "country", for a single property use
 * <code>setListPropertyNames(new String[] {"zipCode"})</code>. For
 * multiple properties use
 * <code>setListPropertyNames(new String[] {"zipCode", "cityName", "stateAbbr",
 * "country"})</code>
 * </li>
 * </ul>
 * <b>Setting the List</b>
 * <p>
 * The List must be a CollectionValueHolder. The holder can contain any type
 * of object (but they must all be instances of the same class).
 * <p>
 * The List can be any type of CollectionValueHolder such as VOCollectionValueHolder
 * or VOListValueHolder.
 * <p>
 * If the List is an ObjectArrayValueHolderList, then the <code>toString()</code>
 * method is used as the display value (regardless of the displayPropertyName).
 * If used in the bound mode, the whole object itself is stored in the target
 * ValueHolder's object (regardless of the properties names listed in the
 * <code>setListPropertyNames</code> method).
 * <p>
 * You can also preset the list in the constructor by passing it an array
 * of Objects to be used for the List. The resulting List will be an
 * ObjectArrayValueHolderList.
 * <p>
 * <b>Other Important Information</b>
 * <p>
 * When a selection is made, the values are copied from the properties in the
 * list to the properties in the valueHolder object.  The property names in
 * propertyNames[] and listPropertyNames[] must appear in a corresponding order.
 * <p>
 * A "null" item will always be added to the listDisplay.  When the "null" item
 * is selected, a null will be assigned to the propertyNames[] in the
 * valueHolder object.  The actual "null" item to to display is defined by
 * the nullDisplay (default is "--none--").
 * <p>
 * The format and pattern govern the display property in this component.  The
 * getText method returns the formatted display property, while the setText
 * method changes the current selection to one whose display matches the given
 * value. If you attempt to setText for an item that is not in the list, a
 * NotInListException will be thrown.
 */
public interface SelectModel extends TextModel {

  // *************************************************************************
  //                              Public Methods
  // *************************************************************************

  /**
   * Returns a Collection of the SelectModelOptions.  The SelectModelOption
   * has 2 properties: optionText which is the value to display in the list
   * and selected, which indicates whether or not the item is selected.
   * <p>
   * The nullDisplay value will be first in the collection.
   * @throws ModelException if problem while matching properties of valueHolder
   * object to list object.
   */
  public Collection getDisplayOptionList() throws ModelException;

  /**
   * Returns the currently selected item from the list.
   * @throws NotInListException if current valueHolder object values do not
   * match any item in list.
   * @throws ModelException if problem while matching properties of valueHolder
   * object to list object.
   */
  public Object getSelectedItem() throws com.taursys.model.ModelException;

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
  public void setSelectedItem(Object value) throws ModelException;

  // *************************************************************************
  //                      Property Accessor Methods
  // *************************************************************************

  /**
   * Sets collectionValueHolder which holds the collection of possible selections.
   */
  public void setList(com.taursys.model.CollectionValueHolder newList);

  /**
   * Returns collectionValueHolder which holds the collection of possible selections.
   */
  public com.taursys.model.CollectionValueHolder getList();

  /**
   * Sets the property name of the list object to display in the list.
   * Default is "value".
   */
  public void setDisplayPropertyName(String newDisplayPropertyName);

  /**
   * Returns the property name of the list object to display in the list.
   * Default is "value".
   */
  public String getDisplayPropertyName();

  /**
   * Sets array of property names in list object to copy to valueHolder object.
   * Default is {"value"}.
   */
  public void setListPropertyNames(String[] newListPropertyNames);

  /**
   * Returns array of property names in list object to copy to valueHolder object.
   * Default is {"value"}.
   */
  public String[] getListPropertyNames();

  /**
   * Sets array of property names in valueHolder object that correspond to properties of list object.
   * Default is {"value"}.
   */
  public void setPropertyNames(String[] newPropertyNames);

  /**
   * Sets array of property names in valueHolder object that correspond to properties of list object.
   * Default is {"value"}.
   */
  public String[] getPropertyNames();

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
  public void setNullAllowed(boolean newNullAllowed);

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
  public boolean isNullAllowed();

  /**
   * Sets value to display in list for a null value.
   */
  public void setNullDisplay(String newNullDisplay);

  /**
   * Returns value to display in list for a null value.
   */
  public String getNullDisplay();
}
