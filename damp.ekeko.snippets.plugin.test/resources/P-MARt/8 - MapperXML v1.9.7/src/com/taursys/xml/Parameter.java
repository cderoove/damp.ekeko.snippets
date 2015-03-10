/**
 * Parameter - A Component which receives parameter values.
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

import java.text.ParseException;
import com.taursys.model.TextModel;
import com.taursys.model.DefaultTextModel;
import com.taursys.model.ModelException;
import com.taursys.util.UnsupportedDataTypeException;
import com.taursys.xml.event.ParameterEvent;
import com.taursys.xml.event.Dispatcher;

/**
 * <p>A <code>Component</code> which receives parameter values. A
 * <code>Parameter</code>, unlike a <code>TextField</code>,
 * is not a <code>DocumentComponent</code>. It cannot be bound to the XML
 * document to render its value. It is intended as a way to gather parameters
 * needed for opening a form (example accountNumber to open an account).</p>
 *
 * <p>A <code>Parameter</code> receives its values before a <code>TextField</code>.
 * Values are dispatched to <code>Parameters</code> immediately before the
 * <code>openForm</code> method of the <code>ServletForm</code> is invoked,
 * whereas values are dispatched to a <code>TextField</code> immediately
 * after the <code>openForm</code> method.</p>
 *
 * <p>This component has one required property. It is the <code>parameter</code>
 * property. This identifies the name of the value which it should listen for
 * and respond to in the request.</p>
 *
 * <p>By default, this component uses an internal <code>DefaultTextModel</code>
 * with a <code>VariantValueHolder</code> and a default data type of
 * <code>String</code>. You can specify a different data type when you invoke
 * the constructor of this component.
 *
 * <p>The following is an example of a <code>Parameter</code> which receives a
 * birthdate:</p>
 *
 * <pre>
 *   Parameter birthdate = new Parameter(DataTypes.TYPE_DATE);
 *   ...
 *   private void jbInit() throws Exception {
 *     birthdate.setParameter("birthdate");
 *     birthdate.setFormat(java.text.SimpleDateFormat.getInstance());
 *     birthdate.setFormatPattern("MM/dd/yyyy");
 *     ...
 *     this.add(birthdate);
 *   }
 *
 *   protected void openForm() throws Exception {
 *     // search for record by birthdate
 *     PersonVO person = delegate.findByBirthdate(
 *         (Date)birthdate.getValue());
 *   }
 * </pre>
 *
 * <p>This component can also be bound to an external <code>ValueHolder</code>.
 * An external <code>ValueHolder</code> can be shared by multiple components.
 * The <code>propertyName</code> specifies which property in the
 * <code>ValueHolder</code> will be bound to this component. To bind this
 * component, set the <code>valueHolder</code> and <code>propertyName</code>
 * properties. You do not need to specify a data type when you bind to a
 * <code>ValueHolder</code>. The following is an example of binding:</p>
 *
 * <pre>
 *   Parameter latitude = new Parameter();
 *   Parameter longitude = new Parameter();
 *   VOValueHolder holder = new VOValueHolder();
 *
 *   private void jbInit() throws Exception {
 *     holder.setValueObjectClass(LocationVO.class);
 *
 *     latitude.setParameter("latitude");
 *     latitude.setValueHolder(holder);
 *     latitude.setPropertyName("physicalLatitude");
 *
 *     longitude.setParameter("longitude");
 *     longitude.setValueHolder(holder);
 *     longitude.setPropertyName("physicalLongitude");
 *     ...
 *     this.add(latitude);
 *     this.add(longitude);
 *   }
 *
 *   void showOnMapButton_actionPerformed(TriggerEvent e) throws Exception {
 *     OutputStream map = delegate.getMap((LocationVO)holder.getValueObject());
 *     ...
 *   }
 * </pre>
 * @see com.taursys.model.ValueHolder
 * @see TextField
 */
public class Parameter extends Component {
  private String parameter;
  private TextModel model;
  private com.taursys.model.ValueHolder valueHolder;
  private String defaultValue;

  // *************************************************************************
  //                               Constructors
  // *************************************************************************

  /**
   * Constructs a new <code>Parameter</code> with a <code>DefaultTextModel</code>
   * and a <code>VariantValueHolder</code> for a <code>String</code> data type.
   * The default model, a <code>DefaultTextModel</code>, is created via the
   * <code>createDefaultModel</code> method.  By default, the
   * <code>DefaultTextModel</code> creates and uses a <code>VariantValueHolder</code>
   * of type <code>String</code>.
   */
  public Parameter() {
    addEventType(ParameterEvent.class.getName());
    model = createDefaultModel();
  }

  /**
   * Constructs a new <code>Parameter</code> with a <code>DefaultTextModel</code>
   * and a <code>VariantValueHolder</code> for the given data type.
   * To specify the data type, use one of the TYPE_xxx constants defined in
   * <code>DataTypes</code>.
   * @param javaDataType data type for new model
   * @see com.taursys.util.DataTypes.
   */
  public Parameter(int javaDataType) {
    addEventType(ParameterEvent.class.getName());
    model = createDefaultModel(javaDataType);
  }

  // ************************************************************************
  //                       Subcomponents Support
  // ************************************************************************

  /**
   * Returns the model for this component
   */
  public TextModel getModel() {
    return model;
  }

  /**
   * Sets the model used by this component.
   * If the given model does not have a defined format, the format
   * and pattern are copied from the old model.
   * @param newModel to be used by this component.
   */
  public void setModel(TextModel newModel) {
    if (newModel.getFormat() == null) {
      newModel.setFormat(model.getFormat());
      newModel.setFormatPattern(model.getFormatPattern());
    }
    model = newModel;
  }

  /**
   * Creates the default model used by this component
   */
  protected TextModel createDefaultModel() {
    return new DefaultTextModel();
  }

  /**
   * Creates the default model of given data type used by this component
   * @param javaDataType data type for new model
   * @see com.taursys.util.DataTypes for defined data type constants TYPE_XXX.
   */
  protected TextModel createDefaultModel(int javaDataType) {
    return new DefaultTextModel(javaDataType);
  }

  // ************************************************************************
  //                 Value Methods Proxied to Subcomponents
  // ************************************************************************

  /**
   * Returns the model value as a String (using Format if defined).  This
   * method simply calls the getText method in the model.
   */
  public String getText() throws ModelException {
    return model.getText();
  }

  /**
   * Sets the model value from the given String (using Format if defined).  This
   * method simply calls the setText method in the model.
   */
  public void setText(String text) throws ModelException {
    model.setText(text);
  }

  /**
   * Returns the value of within the ValueHolder of the Model.  This method
   * simply invokes the getPropertyValue of the model's valueHolder.
   */
  public Object getValue() throws ModelException {
    return model.getValueHolder().getPropertyValue(model.getPropertyName());
  }

  /**
   * Sets the value within the ValueHolder of the Model.  This method simply
   * invokes the setPropertyValue of the model's valueHolder.
   */
  public void setValue(Object value) throws ModelException {
    model.getValueHolder().setPropertyValue(model.getPropertyName(), value);
  }

  // ************************************************************************
  //                       Event Support Methods
  // ************************************************************************

  /**
   * Store value and fires parameter event if event has correct parameter name.
   */
  protected void processParameterEvent(ParameterEvent e) throws Exception {
    if (parameter != null && parameter.equals(e.getName())) {
      model.setText(e.getValue());
      fireParameterReceived(e);
    }
  }

  // ************************************************************************
  //                       Property Accessors
  // ************************************************************************

  /**
   * Sets the name of the parameter this components listens for.
   */
  public void setParameter(String newParameter) {
    parameter = newParameter;
  }

  /**
   * Returns the name of the parameter this components listens for
   */
  public String getParameter() {
    return parameter;
  }

  // ************************************************************************
  //                  Property Accessors for Subcomponents
  // ************************************************************************

  /**
   * Sets the Format of the TextModel.
   */
  public void setFormat(java.text.Format format) {
    model.setFormat(format);
  }

  /**
   * Returns the Format of the TextModel.
   */
  public java.text.Format getFormat() {
    return model.getFormat();
  }

  /**
   * Sets the Format patten of the TextModel.
   */
  public void setFormatPattern(String newPattern) {
    model.setFormatPattern(newPattern);
  }

  /**
   * Returns the Format pattern of the TextModel.
   */
  public String getFormatPattern() {
    return model.getFormatPattern();
  }

  /**
   * Sets the valueHolder for the model.  The valueHolder is the object
   * which holds the Object where the model stores the value.  The
   * default valueHolder is a VariantValueHolder with a javaDataType of String.
   */
  public void setValueHolder(com.taursys.model.ValueHolder newValueHolder) {
    model.setValueHolder(newValueHolder);
  }

  /**
   * Returns the valueHolder for the model.  The valueHolder is the object
   * which holds the Object where the model stores the value.  The
   * default valueHolder is a VariantValueHolder with a javaDataType of String.
   */
  public com.taursys.model.ValueHolder getValueHolder() {
    return model.getValueHolder();
  }

  /**
   * Sets the propertyName in the valueHolder where the model stores the value.
   * This name is ignored if you are using the default model (A DefaultTextModel
   * with a VariantValueHolder).
   */
  public void setPropertyName(String newPropertyName) {
    model.setPropertyName(newPropertyName);
  }

  /**
   * Returns the propertyName in the valueHolder where the model stores the value.
   * This name is ignored if you are using the default model (A DefaultTextModel
   * with a VariantValueHolder).
   */
  public String getPropertyName() {
    return model.getPropertyName();
  }

  /**
   * Set the defaultValue to be used if no input is received.  If this property
   * is not null, it will be used during parameter dispatching whenever the
   * expected parameter is NOT present in the input.  In that case, it will be
   * used as the value for the InputEvent.
   * @param newDefaultValue to be used if no input is received.
   */
  public void setDefaultValue(String newDefaultValue) {
    defaultValue = newDefaultValue;
  }

  /**
   * Get the defaultValue to be used if no input is received.  If this property
   * is not null, it will be used during parameter dispatching whenever the
   * expected parameter is NOT present in the input.  In that case, it will be
   * used as the value for the InputEvent.
   * @return default value to be used if no input is received.
   */
  public String getDefaultValue() {
    return defaultValue;
  }
}
