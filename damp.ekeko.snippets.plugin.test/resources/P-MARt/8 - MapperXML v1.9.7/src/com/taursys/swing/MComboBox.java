/**
 * MComboBox - A bound version of the JComboBox
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
package com.taursys.swing;

import javax.swing.*;
import com.taursys.model.*;

/**
 * MComboBox is a bound version of the JComboBox.
 * @author Marty Phelan
 * @version 2.0
 */
public class MComboBox extends JComboBox implements EnableListener {

  // ************************************************************************
  //                       Constructors
  // ************************************************************************

  /**
   * Constructs a new MComboBox with an empty list and default properties.
   */
  public MComboBox() {
    super(new MComboBoxModel());
    setRenderer(new MListCellRenderer());
  }

  /**
   * Creates a <code>MComboBox</code> that contains the elements
   * in the specified array.  By default the first item in the array
   * (and therefore the data model) becomes selected.
   *
   * @param items  an array of objects to insert into the combo box
   * @see DefaultComboBoxModel
   */
  public MComboBox(final Object items[]) {
    super(new MComboBoxModel(items));
    setRenderer(new MListCellRenderer());
  }

  // ************************************************************************
  //                       Subcomponent Accessors
  // ************************************************************************

  /**
   * Get the renderer cast as an MListCellRenderer.
   * @return the renderer cast as an MListCellRenderer.
   * @throws ClassCastException if the current renderer is not a MListCellRenderer
   */
  public MListCellRenderer getMListCellRenderer() {
    return (MListCellRenderer)getRenderer();
  }

  /**
   * Binds this MComboBox to the given model. The given model should
   * be an instance of MComboBoxModel for this component to function properly.
   * This method registers this component as an EnableListener with the model.
   * @param model the ComboBoxModel for this MComboBox
   */
  public void setModel(ComboBoxModel model) {
    if (getModel() instanceof MComboBoxModel)
      ((MComboBoxModel)getModel()).removeEnableListener(this);
    super.setModel(model);
    if (model instanceof MComboBoxModel)
      ((MComboBoxModel)model).addEnableListener(this);
  }

  // =======================================================================
  //              Proxy Methods for MListCellRenderer Properties
  // =======================================================================

  /**
   * Sets the format for the renderer which is used to format the list display value.
   * @param newFormat the format for the renderer which is used to format the list display value.
   */
  public void setFormat(java.text.Format newFormat) {
    getMListCellRenderer().setFormat(newFormat);
  }

  /**
   * Sets the format for the renderer which is used to format the list display value.
   * @return the format for the renderer which is used to format the list display value.
   */
  public java.text.Format getFormat() {
    return getMListCellRenderer().getFormat();
  }

  /**
   * Sets the format pattern for the renderer which is used to format the list display value.
   * @param newPattern the format pattern for the renderer which is used to format the list display value.
   */
  public void setFormatPattern(String newPattern) {
    getMListCellRenderer().setFormatPattern(newPattern);
  }

  /**
   * Gets the format pattern for the renderer which is used to format the list display value.
   * @return the format pattern for the renderer which is used to format the list display value.
   */
  public String getFormatPattern() {
    return getMListCellRenderer().getFormatPattern();
  }

  /**
   * Set the property name of the list value to display. If the property name
   * is null or blank, then value itself will be displayed.
   * @param newDisplayPropertyName the property name of the list value to display.
   */
  public void setDisplayPropertyName(String newDisplayPropertyName) {
    getMListCellRenderer().setDisplayPropertyName(newDisplayPropertyName);
  }

  /**
   * Get the property name of the list value to display. If the property name
   * is null or blank, then value itself will be displayed.
   * @return the property name of the list value to display.
   */
  public String getDisplayPropertyName() {
    return getMListCellRenderer().getDisplayPropertyName();
  }

  // =======================================================================
  //              Proxy Methods for MComboBoxModel Properties
  //                      ValueHolder Properties
  // =======================================================================

  /**
   * Set the ValueHolder which will store the selection
   * @param valueHolder the ValueHolder which will store the selection
   */
  public void setValueHolder(ValueHolder valueHolder) {
    if (getModel() instanceof MComboBoxModel) {
      ((MComboBoxModel)getModel()).setValueHolder(valueHolder);
    }
  }

  /**
   * Get the ValueHolder which will store the selection
   * @return the ValueHolder which will store the selection
   */
  public ValueHolder getValueHolder() {
    if (getModel() instanceof MComboBoxModel) {
      return ((MComboBoxModel)getModel()).getValueHolder();
    } else {
      return null;
    }
  }

  /**
   * Set the property name in the ValueHolder which will store the selection.
   * This name must correspond with the optionListPropertyName.
   * @param propertyName the property name in the ValueHolder which will store the selection.
   */
  public void setPropertyName(String propertyName) {
    if (getModel() instanceof MComboBoxModel) {
      ((MComboBoxModel)getModel()).setPropertyName(propertyName);
    }
  }

  /**
   * Get the property name in the ValueHolder which will store the selection.
   * This name must correspond with the optionListPropertyName.
   * @return the property name in the ValueHolder which will store the selection.
   */
  public String getPropertyName() {
    if (getModel() instanceof MComboBoxModel) {
      return ((MComboBoxModel)getModel()).getPropertyName();
    } else {
      return null;
    }
  }

  /**
   * Set the property names in the ValueHolder which will store the selection.
   * The selection can be associated with multiple properties in the value holder.
   * These property names must correspond to the optionListPropertyNames.
   * @param propertyNames the property names in the ValueHolder which will store the selection.
   */
  public void setPropertyNames(String[] propertyNames) {
    if (getModel() instanceof MComboBoxModel) {
      ((MComboBoxModel)getModel()).setPropertyNames(propertyNames);
    }
  }

  /**
   * Get the property names in the ValueHolder which will store the selection.
   * The selection can be associated with multiple properties in the value holder.
   * These property names must correspond to the optionListPropertyNames.
   * @return the property names in the ValueHolder which will store the selection.
   */
  public String[] getPropertyNames() {
    if (getModel() instanceof MComboBoxModel) {
      return ((MComboBoxModel)getModel()).getPropertyNames();
    } else {
      return null;
    }
  }

  // =======================================================================
  //              Proxy Methods for MComboBoxModel Properties
  //                     ListValueHolder Properties
  // =======================================================================

  /**
   * Set the valueHolder with the list of options for this component.
   * @param newListValueHolder the valueHolder with the list of options for
   * this component.
   */
  public void setListValueHolder(ListValueHolder holder) {
    if (getModel() instanceof MComboBoxModel) {
      ((MComboBoxModel)getModel()).setListValueHolder(holder);
    }
  }

  /**
   * Get the valueHolder with the list of options for this component.
   * @return the valueHolder with the list of options for this component.
   */
  public ListValueHolder getListValueHolder() {
    if (getModel() instanceof MComboBoxModel) {
      return ((MComboBoxModel)getModel()).getListValueHolder();
    } else {
      return null;
    }
  }

  /**
   * Set the property name in the ListValueHolder which will provide the
   * selection value. This name must correspond with the propertyName for the
   * valueHolder.
   * @param listPropertyName the property name in the ListValueHolder which will
   * provide the selection value.
   */
  public void setListPropertyName(String listPropertyName) {
    if (getModel() instanceof MComboBoxModel) {
      ((MComboBoxModel)getModel()).setListPropertyName(listPropertyName);
    }
  }

  /**
   * Get the property name in the ListValueHolder which will provide the
   * selection value. This name must correspond with the propertyName for the
   * valueHolder.
   * @return the property name in the ListValueHolder which will provide the
   * selection value.
   */
  public String getListPropertyName() {
    if (getModel() instanceof MComboBoxModel) {
      return ((MComboBoxModel)getModel()).getListPropertyName();
    } else
      return null;
  }

  /**
   * Set the property names in the ListValueHolder which will provide the
   * selection values. These names must correspond with the propertyNames for
   * the valueHolder.
   * @param listPropertyNames the property names in the ListValueHolder which
   * will provide the selection values.
   */
  public void setListPropertyNames(String[] listPropertyNames) {
    if (getModel() instanceof MComboBoxModel) {
      ((MComboBoxModel)getModel()).setListPropertyNames(listPropertyNames);
    }
  }

  /**
   * Get the property names in the ListValueHolder which will provide the
   * selection values. These names must correspond with the propertyNames for
   * the valueHolder.
   * @return the property names in the ListValueHolder which will provide the
   * selection values.
   */
  public String[] getListPropertyNames() {
    if (getModel() instanceof MComboBoxModel) {
      return ((MComboBoxModel)getModel()).getListPropertyNames();
    } else
      return null;
  }

  // =======================================================================
  //              Proxy Methods for MComboBoxModel Properties
  //                        Null Value Properties
  // =======================================================================

  /**
   * Sets text to display in list for a null value.
   */
  public void setNullDisplay(String text) {
    if (getModel() instanceof MComboBoxModel)
      ((MComboBoxModel)getModel()).setNullDisplay(text);
  }

  /**
   * Returns value to display in list for a null value.
   */
  public String getNullDisplay() {
    if (getModel() instanceof MComboBoxModel)
      return ((MComboBoxModel)getModel()).getNullDisplay();
    else
      return null;
  }

  /**
   * Sets indicator that a null value is a valid selection.
   * Default is false.
   */
  public void setNullAllowed(boolean nullAllowed) {
    if (getModel() instanceof MComboBoxModel) {
      ((MComboBoxModel)getModel()).setNullAllowed(nullAllowed);
    }
  }

  /**
   * Returns indicator that a null value is a valid selection.
   * Default is false.
   */
  public boolean isNullAllowed() {
    if (getModel() instanceof MComboBoxModel) {
      return ((MComboBoxModel)getModel()).isNullAllowed();
    } else {
      return false;
    }
  }

  // ************************************************************************
  //                       Event Support Methods
  // ************************************************************************

  /**
   * Invoked whenever an EnableChange event is generated by the model.
   * The model will issue the EnableChange event to indicate whether or
   * not this control should allow edits. The enabled property
   * is set based on this.
   */
  public void enableChange(EnableEvent e) {
    setEnabled(e.isEnable());
  }

}
