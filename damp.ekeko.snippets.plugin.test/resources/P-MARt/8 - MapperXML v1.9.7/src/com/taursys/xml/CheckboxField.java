/**
 * CheckboxField - A component which input/renders selected state to a document.
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

import com.taursys.model.DefaultCheckboxModel;
import com.taursys.model.CheckboxModel;
import com.taursys.model.TextModel;
import com.taursys.model.ModelException;
import com.taursys.xml.render.CheckboxFieldRenderer;
import com.taursys.xml.event.RenderEvent;
import com.taursys.xml.event.RenderException;

/**
 * <p>This component is used to display and change a "selected indicator".
 * This component uses a <code>DefaultCheckboxModel</code> to manage the
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
 * <p>When used for output, the value is rendered in the XML document as a
 * text node by default. If you want the value to be rendered to
 * an attribute of the node instead, you must change the <code>renderer</code>
 * to an <code>AttributeTextFieldRenderer</code> and the the <code>attribute</code>
 * property to the name of the attribute. For rendering with an
 * HTML input type checkbox, use the <code>HTMLCheckBox</code> component.</p>
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
public class CheckboxField extends AbstractField {
  private CheckboxFieldRenderer renderer;

  // ************************************************************************
  //                       Constructors
  // ************************************************************************

  /**
   * Constructs a new CheckboxField with a default model and renderer.
   * The default model, a DefaultCheckboxModel, is created via the
   * createDefaultModel method in the Parameter superclass.
   * The default renderer, a CheckboxFieldRenderer, is created via the
   * createDefaultRenderer method.
   */
  public CheckboxField() {
    renderer = createDefaultRenderer();
  }

  /**
   * Constructs a new CheckboxField for the given datatype with a default model and renderer.
   * The default model, a DefaultCheckboxModel, is created via the
   * createDefaultModel method in the Parameter superclass.
   * The default renderer, a CheckboxFieldRenderer, is created via the
   * createDefaultRenderer method.
   * @param javaDataType data type for new model
   * @see com.taursys.util.DataTypes for defined data type constants TYPE_XXX.
   */
  public CheckboxField(int javaDataType) {
    super(javaDataType);
    renderer = createDefaultRenderer();
  }

  // ************************************************************************
  //                       Subcomponent Accessors
  // ************************************************************************

  /**
   * Creates the default TextModel used by this component
   * By default, this method returns a new DefaultCheckboxModel.
   * Override this method to define your own TextModel.
   */
  protected TextModel createDefaultModel() {
    return new DefaultCheckboxModel();
  }

  /**
   * Creates the default model of given data type used by this component
   * @param javaDataType data type for new model
   * @see com.taursys.util.DataTypes for defined data type constants TYPE_XXX.
   */
  protected TextModel createDefaultModel(int javaDataType) {
    return new DefaultCheckboxModel(javaDataType);
  }

  /**
   * Sets the Model for this component to the given CheckboxModel.
   * Although this component allows passing a TextModel, the given model
   * must be an instanceof CheckboxModel.
   * @throws IllegalArgumentException if newModel is not instance of CheckboxModel
   */
  public void setModel(TextModel newModel) throws IllegalArgumentException {
    if (!(newModel instanceof CheckboxModel))
      throw new IllegalArgumentException(
          "Invalid Model type passed - must be instance of CheckboxModel.");
    super.setModel( newModel);
  }

  /**
   * Returns the current model cast as a SelectModel
   */
  protected CheckboxModel getCheckboxModel() {
    return (CheckboxModel)getModel();
  }

  /**
   * Creates the default CheckboxFieldRenderer for this component.
   * By default, this method returns a new CheckboxFieldRenderer.
   * Override this method to define your own CheckboxFieldRenderer.
   */
  protected CheckboxFieldRenderer createDefaultRenderer() {
    return new CheckboxFieldRenderer(this);
  }

  /**
   * Sets the renderer subcomponent used to render the value to the Document.
   */
  public void setRenderer(CheckboxFieldRenderer newRenderer) {
    renderer = newRenderer;
  }

  /**
   * Returns the renderer subcomponent used to render the value to the Document.
   */
  public CheckboxFieldRenderer getRenderer() {
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
   * Gets the current selected state from the Model.  This method
   * simply invokes the model's isSelected method.
   * @throws UnknownStateValueException if the internal value does not match a
   * known state text value.
   * @throws ModelException if problem while setting properties of object in
   * valueHolder.
   */
  public boolean isSelected() throws ModelException {
    return getCheckboxModel().isSelected();
  }

  /**
   * Sets the current state as selected(true) or unselected(false) for the model.
   * @throws ModelException if problem while setting properties of object in
   * valueHolder.
   */
  public void setSelected(boolean newSelected) throws ModelException {
    getCheckboxModel().setSelected(newSelected);
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
  //                      Property Accessor Methods
  // *************************************************************************

  /**
   * Set the value used to indicate a selected state.
   * @param newSelectedValue the value used to indicate a selected state.
   */
  public void setSelectedValue(String newSelectedValue) {
    getCheckboxModel().setSelectedValue(newSelectedValue);
  }

  /**
   * Get the value used to indicate a selected state.
   * @return the value used to indicate a selected state.
   */
  public String getSelectedValue() {
    return getCheckboxModel().getSelectedValue();
  }

  /**
   * Set the value used to indicate an unselected state.
   * @param newUnselectedValue the value used to indicate a unselected state.
   */
  public void setUnselectedValue(String newUnselectedValue) {
    getCheckboxModel().setUnselectedValue(newUnselectedValue);
  }

  /**
   * Get the value used to indicate an unselected state.
   * @return the value used to indicate a unselected state.
   */
  public String getUnselectedValue() {
    return getCheckboxModel().getUnselectedValue();
  }
}
