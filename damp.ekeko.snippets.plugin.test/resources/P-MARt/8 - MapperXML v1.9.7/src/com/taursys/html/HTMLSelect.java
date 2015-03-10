/**
 * HTMLSelect - a peer component for the HTML Select form component
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
package com.taursys.html;

import com.taursys.html.render.HTMLSelectFieldRenderer;
import com.taursys.model.ObjectArrayValueHolder;

/**
 * <p>This components is a peer component for the &lt;Select&gt; tag in an HTML
 * form. This component is a specialization of the <code>SelectField</code>.
 * Like the <code>SelectField</code>, this component (using a
 * <code>DefaultSelectModel</code>), can display a selection from a
 * list of objects. It can also change the selection from user input.</p>
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
 * <p>The HTMLSelect uses a specialized renderer which will create a list
 * of html &lt;Option&gt; tags with the displayPropertyName value for each
 * item in the list. Using the same example as above, after rendering the
 * document would contain:</p>
 *
 * <pre>
 * &lt;select name="zip" id="zip"&gt;
 *   &lt;option&gt;99801&lt;/option&gt;
 *   &lt;option&gt;99824&lt;/option&gt;
 *   &lt;option&gt;99827&lt;/option&gt;
 * &lt;/select&gt;
 * </pre>
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
 * property to the name of the attribute.</p>
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
public class HTMLSelect extends com.taursys.xml.SelectField {

  /**
   * Constructs a new HTMLSelect component.
   */
  public HTMLSelect() {
    setRenderer(new HTMLSelectFieldRenderer(this));
  }

  /**
   * Creates a new HTMLSelect of the given data type.
   * @param javaDataType the data type for the VariantValueHolder
   * @see com.taursys.util.DataTypes for defined data type constants TYPE_XXXXXX.
   */
  public HTMLSelect(int javaDataType) {
    super(javaDataType);
    setRenderer(new HTMLSelectFieldRenderer(this));
  }

  /**
   * Constructs a new HTMLSelect with the given list of options.
   */
  public HTMLSelect(Object[] array) {
    setRenderer(new HTMLSelectFieldRenderer(this));
    setList(new ObjectArrayValueHolder(array));
  }

  /**
   * Constructs a new HTMLSelect of the given data type with the given list of
   * options.
   * @param javaDataType the data type for the VariantValueHolder
   * @param array the array of options
   * @see com.taursys.util.DataTypes for defined data type constants TYPE_XXXXXX.
   */
  public HTMLSelect(int javaDataType, Object[] array) {
    super(javaDataType);
    setRenderer(new HTMLSelectFieldRenderer(this));
    setList(new ObjectArrayValueHolder(array));
  }

}
