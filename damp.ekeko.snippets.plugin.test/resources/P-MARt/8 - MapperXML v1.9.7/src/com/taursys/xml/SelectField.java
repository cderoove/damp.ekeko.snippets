/**
 * SelectField - A Component which receives InputEvents and/or renders selection to a Document
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
package com.taursys.xml;

import com.taursys.model.DefaultSelectModel;
import com.taursys.model.SelectModel;
import com.taursys.model.TextModel;
import com.taursys.model.ObjectArrayValueHolder;
import com.taursys.model.ModelException;
import com.taursys.xml.render.SelectFieldRenderer;
import com.taursys.xml.event.RenderEvent;
import com.taursys.xml.event.RenderException;

/**
 * <p>This component is used to display and change a selection.
 * This component, (using a <code>DefaultSelectModel</code>), can display a
 * selection from a list of objects. It can also change the selection from
 * user input.</p>
 *
 * <p>This component can be used for a variety of purposes. It can provide
 * a way to display a "description" rather than a "code" for a coded value.
 * It can also serve to scope user input to a subset of values.</p>
 *
 * <p>The selection is made and displayed using the property indicated by
 * the <code>displayPropertyName</code>. The <code>displayPropertyName</code>
 * is effectively the "selection key". If the <code>displayPropertyName</code>
 * is null or blank, then the <code>toString</code> method is used instead.
 * For example, given a list of Address objects, and a
 * <code>displayPropertyName</code> of "zipCode", the display value for
 * "Juneau, AK 99801 USA" would be "99801". To change the selection, you would
 * supply a different zipCode from the list.</p>
 *
 * <p>This component can function in three different ways, depending on the
 * properties you set:</p>
 *
 * <ul>
 *  <li>input only - set the <code>parameter</code> property.</li>
 *  <li>output only - set the <code>id</code> property.</li>
 *  <li>input and output - set both the <code>parameter</code> and
 *  <code>id</code> properties.</li>
 * </ul>
 *
 * <p>When used for output, the value is rendered in the XML document  as a
 * text node by default. If you want the value to be rendered to
 * an attribute of the node instead, you must change the <code>renderer</code>
 * to an <code>AttributeTextFieldRenderer</code> and the the <code>attribute</code>
 * property to the name of the attribute. For rendering with an
 * HTML SELECT/OPTION, use the <code>HTMLSelect</code> component.</p>
 *
 * <p>When used for input, this component receives its value from the
 * <code>InputDispatcher</code> AFTER the <code>openForm</code> method of the
 * <code>ServletForm</code> by default.  If you want this component to receive
 * its input earlier (at the same time as <code>Parameters</code>), set the
 * <code>earlyInputNotify</code> property to <code>true</code>.</p>
 *
 * <p>By default, this component uses a <code>DefaultSelectModel</code>.
 * You can change this by overriding the <code>createDefaultModel</code> method
 * or explicitly setting the <code>model</code> property.</p>
 *
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
 * The following is an example of this usage:</p>
 *
 * <pre>
 *   SelectField color = new SelectField(new String[] {
 *       "Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet",
 *       });
 *   ...
 *   private void jbInit() throws Exception {
 *     ...
 *     color.setParameter("color");
 *     color.setId("color");
 *     ...
 *     this.add(color);
 *   }
 * </pre>
 *
 * <p>This component can be used in a variety of ways. It can be used in an
 * un-bound mode, where the current selection is maintained internally. It can
 * also be used in a bound mode where the current selection is propagated to a
 * value holder. When used in the bound mode, either a single property, or
 * multiple properties can be set in the value holder. The following sections
 * describe the required settings to make for each of the modes.</p>
 *
 * <b>Un-bound Mode (uses internal VariantValueHolder)</b>
 * <p>
 * When used in this mode this component uses an internal
 * <code>VariantValueHolder</code> to hold the current selection. By default a
 * <code>VariantValueHolder</code> is created with a data type of
 * <code>String</code>. To set a different data type use the constructor which
 * takes a data type as a parameter. (example
 * <code>new SelectField(DataTypes.TYPE_INT)</code>).
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
 *
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
public class SelectField extends AbstractField {
  private SelectFieldRenderer renderer;

  // ************************************************************************
  //                       Constructors
  // ************************************************************************

  /**
   * Constructs a new SelectField.
   */
  public SelectField() {
    renderer = createDefaultRenderer();
  }

  /**
   * Creates a new SelectField of the given data type.
   * @param javaDataType the data type for the VariantValueHolder
   * @see com.taursys.util.DataTypes for defined data type constants TYPE_XXXXXX.
   */
  public SelectField(int javaDataType) {
    super(javaDataType);
    renderer = createDefaultRenderer();
  }

  /**
   * Constructs a new SelectField with the given list of options.
   */
  public SelectField(Object[] array) {
    renderer = createDefaultRenderer();
    setList(new ObjectArrayValueHolder(array));
  }

  /**
   * Constructs a new SelectField of the given data type with the given list of
   * options.
   * @param javaDataType the data type for the VariantValueHolder
   * @param array the array of options
   * @see com.taursys.util.DataTypes for defined data type constants TYPE_XXXXXX.
   */
  public SelectField(int javaDataType, Object[] array) {
    super(javaDataType);
    renderer = createDefaultRenderer();
    setList(new ObjectArrayValueHolder(array));
  }

  // ************************************************************************
  //                       Subcomponent Accessors
  // ************************************************************************

  /**
   * Creates the default TextModel used by this component
   * By default, this method returns a new DefaultSelectModel.
   * Override this method to define your own TextModel.
   */
  protected TextModel createDefaultModel() {
    return new DefaultSelectModel();
  }

  /**
   * Creates the default model of given data type used by this component
   * @param javaDataType data type for new model
   * @see com.taursys.util.DataTypes for defined data type constants TYPE_XXX.
   */
  protected TextModel createDefaultModel(int javaDataType) {
    return new DefaultSelectModel(javaDataType);
  }

  /**
   * Sets the Model for this component to the given SelectModel.
   * Although this component allows passing a TextModel, the given model
   * must be an instanceof SelectModel.
   * @throws IllegalArgumentException if newModel is not instance of SelectModel
   */
  public void setModel(TextModel newModel) throws IllegalArgumentException {
    if (!(newModel instanceof SelectModel))
      throw new IllegalArgumentException(
          "Invalid Model type passed - must be instance of SelectModel.");
    super.setModel( newModel);
  }

  /**
   * Returns the current model cast as a SelectModel
   */
  protected SelectModel getSelectModel() {
    return (SelectModel)getModel();
  }

  /**
   * Creates the default SelectFieldRenderer for this component.
   * By default, this method returns a new SelectFieldRenderer.
   * Override this method to define your own SelectFieldRenderer.
   */
  protected SelectFieldRenderer createDefaultRenderer() {
    return new SelectFieldRenderer(this);
  }

  /**
   * Sets the renderer subcomponent used to render the value to the Document.
   */
  public void setRenderer(SelectFieldRenderer newRenderer) {
    renderer = newRenderer;
  }

  /**
   * Returns the renderer subcomponent used to render the value to the Document.
   */
  public SelectFieldRenderer getRenderer() {
    return renderer;
  }

  // ************************************************************************
  //                 Value Methods Proxied to Subcomponents
  // ************************************************************************

  /**
   * Returns the model value as a String (using Format if defined).  This
   * method simply calls the getText method in the model.
   */
  public String getText() throws ModelException {
    return getModel().getText();
  }

  /**
   * Sets the model value from the given String (using Format if defined).  This
   * method simply calls the setText method in the model.
   */
  public void setText(String text) throws ModelException {
    getModel().setText(text);
  }

  /**
   * Gets the current selection from the Model.  This method
   * simply invokes the model's getSelectedItem method.
   */
  public Object getSelection() throws ModelException {
    return getSelectModel().getSelectedItem();
  }

  /**
   * Sets the current selection from the Model.  This method simply
   * invokes the model's setSelectedItem method.
   */
  public void setSelection(Object value) throws ModelException {
    getSelectModel().setSelectedItem(value);
  }

  // ************************************************************************
  //                             Event Support
  // ************************************************************************

  /**
   * Responds to a render event for this component.  This uses the renderer
   * subcomponent to actually render the value. It first notifies any
   * RenderListeners of the event. It then invokes the renderer subcomponent
   * to render the value to the document.
   * @param e the current render event message
   * @throws RenderException if problem rendering value to document
   */
  public void processRenderEvent(RenderEvent e) throws RenderException {
    fireRender(e);
    renderer.render();
  }

  // *************************************************************************
  //                      Property Accessors for Model
  // *************************************************************************

  /**
   * Sets collectionValueHolder which holds the collection of possible selections.
   */
  public void setList(com.taursys.model.CollectionValueHolder newList) {
    getSelectModel().setList(newList);
  }

  /**
   * Returns collectionValueHolder which holds the collection of possible selections.
   */
  public com.taursys.model.CollectionValueHolder getList() {
    return getSelectModel().getList();
  }

  /**
   * Sets the property name of the list object to display in the list.
   * Default is "value".
   */
  public void setDisplayPropertyName(String newDisplayPropertyName) {
    getSelectModel().setDisplayPropertyName(newDisplayPropertyName);
  }

  /**
   * Returns the property name of the list object to display in the list.
   * Default is "value".
   */
  public String getDisplayPropertyName() {
    return getSelectModel().getDisplayPropertyName();
  }

  /**
   * Sets array of property names in list object to copy to valueHolder object.
   * Default is {"value"}.
   */
  public void setListPropertyNames(String[] newListPropertyNames) {
    getSelectModel().setListPropertyNames(newListPropertyNames);
  }

  /**
   * Returns array of property names in list object to copy to valueHolder object.
   * Default is {"value"}.
   */
  public String[] getListPropertyNames() {
    return getSelectModel().getListPropertyNames();
  }

  /**
   * Sets array of property names in valueHolder object that correspond to properties of list object.
   * Default is {"value"}.
   */
  public void setPropertyNames(String[] newPropertyNames) {
    getSelectModel().setPropertyNames(newPropertyNames);
  }

  /**
   * Sets array of property names in valueHolder object that correspond to properties of list object.
   * Default is {"value"}.
   */
  public String[] getPropertyNames() {
    return getSelectModel().getPropertyNames();
  }

  /**
   * Sets the property name in valueHolder that will hold the value from the list.
   * If you have also used the setPropertyNames method, then this will change the
   * first name in that array.
   */
  public void setPropertyName(String newPropertyName) {
    getSelectModel().setPropertyName(newPropertyName);
  }

  /**
   * Gets the property name in valueHolder that will hold the value from the list.
   * If you have also used the setPropertyNames method, then this will return the
   * first name in that array.
   */
  public String getPropertyName() {
    return getSelectModel().getPropertyName();
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
    getSelectModel().setNullAllowed(nullAllowed);
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
    return getSelectModel().isNullAllowed();
  }

  /**
   * Sets value to display in list for a null value.
   */
  public void setNullDisplay(String newNullDisplay) {
    getSelectModel().setNullDisplay(newNullDisplay);
  }

  /**
   * Returns value to display in list for a null value.
   */
  public String getNullDisplay() {
    return getSelectModel().getNullDisplay();
  }
}
