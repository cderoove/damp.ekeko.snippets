/**
 * Trigger - A Component which responds to TriggerEvents
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

import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import com.taursys.xml.event.TriggerEvent;

/**
 * A Component which responds to TriggerEvents.  By default, this component uses
 * a ButtonModel to hold its state. You can change this by overriding the
 * createDefaultModel method or explicitly setting the model property.
 */
public class Trigger extends Component {
  private ButtonModel model;
  private String parameter;
  private String text = null;
  private boolean defaultTrigger;

  /**
   * Creates a new Trigger and its default model.
   * The default model, a DefaultButtonModel, is created via the
   * createDefaultModel method.
   */
  public Trigger() {
    addEventType(TriggerEvent.class.getName());
    model = createDefaultModel();
  }

  /**
   * Creates the model used by this component. Override this method
   * to provide your own default ButtonModel for the component.
   */
  protected ButtonModel createDefaultModel() {
    return new DefaultButtonModel();
  }

  /**
   * Returns the current model used by this component
   */
  public ButtonModel getModel() {
    return model;
  }

  /**
   * Sets the current model used by this component
   */
  public void setModel(ButtonModel newModel) {
    model = newModel;
  }

  /**
   * Store value and fires parameter event if event has correct parameter name.
   */
  protected void processTriggerEvent(TriggerEvent e) throws Exception {
    model.setSelected(true);
    model.setPressed(true);
    fireActionPerformed(e);
  }

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

  /**
   * Sets the text value that will trigger this button.
   */
  public void setText(String newText) {
    text = newText;
  }

  /**
   * Returns the text value that will trigger this button.
   */
  public String getText() {
    return text;
  }

  /**
   * Set this whether to act as the defaultTrigger if no parameter is received.
   * If this property is true, it will be used during trigger dispatching
   * whenever the expected parameter is NOT present in the input.  In that
   * case, this component will respond as if its expected parameter value was
   * received.
   * @param newDefaultTrigger whether to act as the defaultTrigger.
   */
  public void setDefaultTrigger(boolean newDefaultTrigger) {
    defaultTrigger = newDefaultTrigger;
  }

  /**
   * Get whether this is to act as the defaultTrigger if no parameter is received.
   * If this property is true, it will be used during trigger dispatching
   * whenever the expected parameter is NOT present in the input.  In that
   * case, this component will respond as if its expected parameter value was
   * received.
   * @return whether to act as the defaultTrigger.
   */
  public boolean isDefaultTrigger() {
    return defaultTrigger;
  }
}
