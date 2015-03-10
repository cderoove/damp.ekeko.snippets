/**
 * HTMLCheckBox - A peer component to an HTML Input type checkbox
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

import com.taursys.xml.render.CheckboxFieldRenderer;
import com.taursys.html.render.HTMLCheckboxFieldRenderer;
import com.taursys.xml.*;

/**
 * <p>HTMLCheckBox is a peer component to an HTML input type checkbox.
 * This field is for a stand-alone checkbox and maintains its checked/unchecked
 * state.
 * </p>
 * <p>This component uses an HTMLCheckboxFieldRenderer to render its value.
 * If the CheckboxModel isSelected returns true, then the renderer adds
 * the "checked" attribute to the input tag, otherwise it removed the attribute.
 * </p>
 * <p>The HTML checkbox has a unique behavior in that it only sends a value if it
 * is checked.  It does not send anything back if it is un-checked.  This
 * behavior requires the use of a defaultValue or uncheckedValue.
 * </p>
 * <p>The <code>unselectedValue</code> is used as the <code>defaultValue</code>
 * for this component. If the <code>unselectedValue</code> is not null it will
 * be used during parameter or input dispatching whenever the checkbox is NOT
 * checked (no parameter is sent). In that case, the <code>unselectedValue</code>
 * will be used as the value for the InputEvent.
 * </p>
 * <p><b>Limitation:</b> Since input processing will ALWAYS set the value to
 * either the <code>selectedValue</code> (if input present) or
 * <code>unselectedValue</code> (if no input is present), you will want to
 * disable input processing when simply preseting or displaying values. With an
 * HTML checkbox, there is no way to distinguish between an unchecked box or
 * no input submission. To disable input processing, either set the
 * <code>ServletForm's</code> <code>enableInput</code> property to
 * <code>false</code>, or temporarily set the <code>HTMLCheckBox's</code>
 * <code>parameter</code> property to null or blank.
 * </p>
 * <p>This component uses a <code>DefaultCheckboxModel</code> to manage the
 * state. There are only two states for this component: selected or
 * not-selected.</p>
 *
 * <p>To use this component you must first set the the following properties
 * as indicated:</p>
 * <ul>
 * <li><code>selectedValue</code> - the text value which indicates a
 * "selected" state. The default value is "true".</li>
 * <li><code>unselectedValue</code> - the text value which indicates an
 * "un-selected" state. The default value is "" (blank) which will result
 * in a <code>null</code> value being stored in the <code>valueHolder</code></li>
 * </ul>
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
 * <p>When used for input, this component receives its value from the
 * <code>InputDispatcher</code> AFTER the <code>openForm</code> method of the
 * <code>ServletForm</code> by default.  If you want this component to receive
 * its input earlier (at the same time as <code>Parameters</code>), set the
 * <code>earlyInputNotify</code> property to <code>true</code>.</p>
 *
 * <p>By default, this component uses a <code>DefaultCheckboxModel</code>.
 * You can change this by overriding the <code>createDefaultModel</code> method
 * or explicitly setting the <code>model</code> property.</p>
 *
 * <p>This component can be used in a variety of ways. It can be used in an
 * un-bound mode, where the current selected state is maintained internally.
 * It can also be used in a bound mode where the current selected state is
 * propagated to a value holder. When used in the bound mode a single property
 * can be set in the value holder. The following sections describe the required
 * settings to make for each of the modes.</p>
 *
 * <b>Un-bound Mode</b>
 *
 * <p>To use this component in the un-bound mode, you can set the following
 * properties:</p>
 * <ul>
 * <li><code>selectedValue</code> - the text value which indicates a
 * "selected" state. The default value is "true".</li>
 * <li><code>unselectedValue</code> - the text value which indicates an
 * "un-selected" state. The default value is "" (blank) which will result
 * in a <code>null</code> value being stored in the <code>valueHolder</code></li>
 * </ul>
 *
 * <b>Bound Mode</b>
 *
 * <p>To use this component in the bound mode, use the same properties as
 * described in the Un-bound Mode, plus the following additional properties:</p>
 * <ul>
 * <li><code>valueHolder</code> - should be bound to the
 * <code>ValueHolder</code> which contains the current selected state and is to
 * be updated if the state is changed.</li>
 *
 * <li><code>propertyName</code> - set this to the property
 * name (belonging to the object in the <code>ValueHolder</code>) which will
 * be bound to the selected state. Example: given a <code>ValueHolder</code>
 * with an "Address" object which has a "active" property, use
 * <code>setPropertyName("active")</code> to store the current selected state in
 * the "Address.active" property.</li>
 * </ul>
 * @author Marty Phelan
 * @version 1.0
 */
public class HTMLCheckBox extends CheckboxField {

  /**
   * Constructs a new HTMLCheckBox
   */
  public HTMLCheckBox() {
    setAttributeName("value");
  }

  /**
   * Constructs a new HTMLCheckBox for the given data type
   * @param javaDataType data type for component value
   * @see com.taursys.util.DataTypes
   */
  public HTMLCheckBox(int javaDataType) {
    super(javaDataType);
    setAttributeName("value");
  }

  /**
   * Creates the default CheckboxFieldRenderer for this component.
   * By default, this method returns a new HTMLCheckboxFieldRenderer.
   * Override this method to define your own CheckboxFieldRenderer.
   */
  protected CheckboxFieldRenderer createDefaultRenderer() {
    return new HTMLCheckboxFieldRenderer(this);
  }

  /**
   * Set the both the defaultValue and the unselectedValue for this component.
   * The defaultValue and unselectedValue are the same for this component.
   * If the defaultValue is not null it will be used during parameter
   * dispatching whenever the expected parameter is NOT present in the input.
   * In that case, the defaultValue will be used as the value for the InputEvent.
   * <p>
   * The HTML input type checkbox only sends a value if the box is checked,
   * and NO value is sent if it is unchecked.  Therfore the defaultValue
   * is used to provide the input for the "unchecked" state.
   * @param newDefaultValue to be used if no input is received.
   */
  public void setDefaultValue(String newDefaultValue) {
    super.setUnselectedValue( newDefaultValue);
  }

  /**
   * Get the both the defaultValue and the unselectedValue for this component.
   * The defaultValue and unselectedValue are the same for this component.
   * If the defaultValue is not null it will be used during parameter
   * dispatching whenever the expected parameter is NOT present in the input.
   * In that case, the defaultValue will be used as the value for the InputEvent.
   * <p>
   * The HTML input type checkbox only sends a value if the box is checked,
   * and NO value is sent if it is unchecked.  Therfore the defaultValue
   * is used to provide the input for the "unchecked" state.
   * @return default value to be used if no input is received.
   */
  public String getDefaultValue() {
    return getUnselectedValue();
  }
}
