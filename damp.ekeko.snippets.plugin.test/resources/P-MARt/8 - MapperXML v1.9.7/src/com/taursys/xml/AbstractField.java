/**
 * AbstractField - A Component which receives InputEvents and/or renders value in a Document
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

import com.taursys.xml.event.RenderException;
import com.taursys.xml.event.ParameterEvent;
import com.taursys.xml.event.InputEvent;
import com.taursys.xml.event.RecycleEvent;
import com.taursys.xml.event.RenderEvent;
import com.taursys.xml.event.Dispatcher;

/**
 * A Component which receives input and/or renders value to a Document. It
 * responds to InputEvents and holds the given value.  By default, this
 * component uses a VariantTextModel. You can change this by overriding the
 * createDefaultModel method or explicitly setting the model property.
 * <p>
 * This component can render its value to an xml Document (thru the
 * DocumentAdapter). The id property indicates which element to render in
 * the Document.  The attributeName property is only used with an
 * AttributeTextFieldRenderer to indicate which attribute to store the value
 * in.  Subclasses must override the processRenderEvent method
 * and should delegate the work to a rendering subcomponent.
 */
public abstract class AbstractField extends Parameter
    implements DocumentComponent {
  private String id;
  private String attributeName = "value";
  private boolean earlyInputNotify;

  // *************************************************************************
  //                               Constructors
  // *************************************************************************

  /**
   * Constructs a new AbstractField with a default model.
   * The default model, a VariantTextModel, is created via the
   * createDefaultModel method in the Parameter superclass.
   */
  public AbstractField() {
    super();
    removeEventType(ParameterEvent.class.getName());
    addEventType(InputEvent.class.getName());
    addEventType(RenderEvent.class.getName());
    addEventType(RecycleEvent.class.getName());
  }

  /**
   * Creates a new AbstractField with a DefaultTextModel and VariantValueHolder of the given type.
   * See com.taursys.util.DataTypes for defined data type constants TYPE_XXXXXX.
   */
  public AbstractField(int javaDataType) {
    super(javaDataType);
    removeEventType(ParameterEvent.class.getName());
    addEventType(InputEvent.class.getName());
    addEventType(RenderEvent.class.getName());
    addEventType(RecycleEvent.class.getName());
  }

  // ************************************************************************
  //                       Event Support Methods
  // ************************************************************************

  /**
   * Store value and fires input event if event has correct input name.
   */
  protected void processInputEvent(InputEvent e) throws Exception {
    if (getParameter() != null && getParameter().equals(e.getName())) {
      getModel().setText(e.getValue());
      fireInputReceived(e);
    }
  }

  /**
   * Responds to a render event for this component. Subclasses must override
   * the processRenderEvent method and should delegate the work to a
   * rendering subcomponent.
   */
  public abstract void processRenderEvent(RenderEvent e) throws RenderException;

  /**
   * Set flag for early input notification.  Normally this field registers to
   * receive InputEvents.  If earlyInputNofity is true, it will register to
   * receive ParameterEvents instead.  It will then be notified of input at the
   * same time as Parameter components. This method will also force new
   * registration if isNotifySet is true.
   * @param earlyInputNotify flag for early input notification.
   */
  public void setEarlyInputNotify(boolean earlyInputNotify) {
    boolean b = isNotifySet(); // save old state
    if (isNotifySet())
      removeNotify();
    this.earlyInputNotify = earlyInputNotify;
    if (earlyInputNotify) {
      removeEventType(InputEvent.class.getName());
      addEventType(ParameterEvent.class.getName());
    } else {
      removeEventType(ParameterEvent.class.getName());
      addEventType(InputEvent.class.getName());
    }
    if (b)
      addNotify(); // only if was originally set
  }

  /**
   * Get flag for early input notification.  Normally this field registers to
   * receive InputEvents.  If earlyInputNofity is true, it will register to
   * receive ParameterEvents instead.  It will then be notified of input at the
   * same time as Parameter components. This method will also force new
   * registration if isNotifySet is true.
   * @return earlyInputNotify flag for early input notification.
   */
  public boolean isEarlyInputNotify() {
    return earlyInputNotify;
  }

  // ************************************************************************
  //                       Property Accessors
  // ************************************************************************

  /**
   * Sets the id of the Element this component is bound to.  This is the Element
   * where the component will render its value.
   */
  public void setId(String newId) {
    id = newId;
  }

  /**
   * Returns the id of the Element this component is bound to.  This is the Element
   * where the component will render its value.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the name of the Element's attribute where the value should be rendered.
   * This property is only used when a AttributeTextFieldRenderer is used.  The
   * default is "value".
   */
  public void setAttributeName(String newAttributeName) {
    attributeName = newAttributeName;
  }

  /**
   * Returns the name of the Element's attribute where the value should be rendered.
   * This property is only used when a AttributeTextFieldRenderer is used.  The
   * default is "value".
   */
  public String getAttributeName() {
    return attributeName;
  }
}
