/**
 * DefaultTextModel - An Implementation of TextModel
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
import java.text.ChoiceFormat;
import com.taursys.util.DataTypes;
import com.taursys.util.UnsupportedConversionException;
import com.taursys.util.UnsupportedDataTypeException;
import java.text.ParseException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalAccessException;
import java.beans.IntrospectionException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Vector;

/**
 * This class implements the TextModel interface and is designed to work with Parameter and Fields.
 * This model stores its value in a ValueHolder object under a propertyName.
 * This model converts the value to/from text using a given format and formatPattern.
 * By default, this model uses a VariantValueHolder.
 */
public class DefaultTextModel implements TextModel ,ChangeListener {
  private com.taursys.model.ValueHolder valueHolder;
  private String propertyName;
  private java.text.Format format;
  private String formatPattern;
  private transient Vector changeListeners;

  /**
   * Constructs new DefaultTextModel and initializes valueHolder via createDefaultValueHolder
   */
  public DefaultTextModel() {
    valueHolder = createDefaultValueHolder();
  }

  /**
   * Constructs new DefaultTextModel and sets valueHolder to a VariantValueHolder for given data type.
   * @param the data type for the VariantValueHolder (DataType.TYPE_XXXX)
   * @throws UnsupportedDataTypeException if invalid javaDataType is given
   * @see com.taursys.util.DataTypes
   */
  public DefaultTextModel(int javaDataType) throws UnsupportedDataTypeException {
    valueHolder = new VariantValueHolder(javaDataType);
  }

  /**
   * Creates default valueHolder to be used by this model.
   * Override this method or use the setValueHolder method to change the valueHolder.
   * @return a new ValueHolder for this component to use.
   */
  protected ValueHolder createDefaultValueHolder() {
    return new VariantValueHolder(DataTypes.TYPE_STRING);
  }

  // ***********************************************************************
  // *               TEXT PARSE/FORMAT ROUTINES
  // ***********************************************************************

  /**
   * Returns the valueHolder's propertyValue as a String value using Format if defined.
   * If the valueHolder's propertyValue is null, it will return an empty
   * String ("").
   * @return the valueHolder's propertyValue as a formatted String
   * @throws ModelException if problem occurs when retrieving value from
   *    ValueHolder
   */
  public String getText()
      throws ModelException {
    Object value = getValueHolder().getPropertyValue(propertyName);
    int javaDataType = getValueHolder().getJavaDataType(propertyName);
    if (value == null)
      return "";
    if (format == null)
      return DataTypes.format(javaDataType, value);
    else if (format instanceof MessageFormat)
      return ((MessageFormat)format).format(new Object[] {value});
    else
      return format.format(value);
  }

  /**
   * Sets the valueHolder's propertyValue from the given String value to DataType using Format(if defined) to parse.
   * If the given String value is null or empty (""), the accessed valueHolder's
   * propertyValue is set to null.
   * @param the text value used to set the model value
   * @throws ModelException if problem parsing or storing value
   */
  public void setText(String value) throws ModelException {
    int javaDataType = -1;
    try {
      Object newValue = null;
      javaDataType = getValueHolder().getJavaDataType(propertyName);
      if (value == null || value.length() == 0) {
        newValue = null;
      } else if (format == null) {
        newValue = DataTypes.parse(javaDataType, value);
      } else if (format instanceof MessageFormat) {
        Object[] values = ((MessageFormat)format).parse(value);
        newValue = DataTypes.convert(javaDataType, values[0]);
      } else {
        newValue = DataTypes.convert(javaDataType, format.parseObject(value));
      }
      getValueHolder().setPropertyValue(propertyName, newValue);
    } catch (ParseException ex) {
      throw new ModelParseException(propertyName, javaDataType, format,
          formatPattern, value, ex);
    } catch (NumberFormatException ex) {
      throw new ModelParseException(propertyName, javaDataType, format,
          formatPattern, value, ex);
    } catch (UnsupportedConversionException ex) {
      throw new ModelException(ModelException.REASON_PARSE_CONVERSION_ERROR,
        "Property name: " + propertyName + "\n"
        + "Converting from output of Format type: "
        + format.getClass().getName() + "\n"
        + "to data type of value holder: "
        + DataTypes.getJavaNameForType(javaDataType));
    }
  }

  /**
   * Tries to applies the formatPattern to the format.  This only occurs if
   * the format and formatPattern are not null, and the format is an instance of
   * MessageFormat, SimpleDateFormat, ChoiceFormat, or DecimalFormat.
   */
  protected void setupFormat() {
    // Now apply the format formatPattern if possible
    if (format != null && formatPattern != null) {
      if (format instanceof MessageFormat)
        ((MessageFormat)format).applyPattern(formatPattern);
      if (format instanceof SimpleDateFormat)
        ((SimpleDateFormat)format).applyPattern(formatPattern);
      if (format instanceof DecimalFormat)
        ((DecimalFormat)format).applyPattern(formatPattern);
      if (format instanceof ChoiceFormat)
        ((ChoiceFormat)format).applyPattern(formatPattern);
    }
  }

  // ***********************************************************************
  // *                   PROPERTY ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Sets the valueHolder for this model.  The valueHolder is the object
   * which holds the Object where this model stores the value.  The
   * default valueHolder is a VariantValueHolder with a javaDataType of String.
   * Removes this DefaultTextModel as a change listener from the current
   * ValueHolder (if any) and adds this as a change listener to the
   * given ValueHolder.
   * @param the ValueHolder for this model to use to hold the actual value
   */
  public void setValueHolder(com.taursys.model.ValueHolder newValueHolder) {
    if (valueHolder != null)
      valueHolder.removeChangeListener(this);
    valueHolder = newValueHolder;
    valueHolder.addChangeListener(this);
  }

  /**
   * Returns the valueHolder for this model.  The valueHolder is the object
   * which holds the Object where this model stores the value.  The
   * default valueHolder is a VariantValueHolder with a javaDataType of String.
   * @return the ValueHolder used by this model to hold the actual value
   */
  public com.taursys.model.ValueHolder getValueHolder() {
    return valueHolder;
  }

  /**
   * Sets the propertyName in the valueHolder where this model stores the value.
   * This name is ignored if you are using the VariantValueHolder.
   * @param the name of the property in the ValueHolder which holds the actual
   *    value.
   */
  public void setPropertyName(String newPropertyName) {
    propertyName = newPropertyName;
  }

  /**
   * Returns the propertyName in the valueHolder where this model stores the value.
   * This name is ignored if you are using the VariantValueHolder.
   * @return the name of the property in the ValueHolder which holds the actual
   *    value.
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Sets the format for this model which is used by set/getText to parse/display values.
   * @param the Format object used to format and parse the text value
   */
  public void setFormat(java.text.Format newFormat) {
    format = newFormat;
    setupFormat();
  }

  /**
   * Returns the format for this model which is used by set/getText to parse/display values.
   * @return the Format object used to format and parse the text value
   */
  public java.text.Format getFormat() {
    return format;
  }

  /**
   * Sets the format formatPattern for this model which is used by set/getText to parse/display values.
   * @param the pattern to be used by the Format object to format and parse the
   *    text value.
   */
  public void setFormatPattern(String newPattern) {
    formatPattern = newPattern;
    setupFormat();
  }

  /**
   * Returns the format formatPattern for this model which is used by set/getText to parse/display values.
   * @return the pattern to be used by the Format object to format and parse the
   *    text value.
   */
  public String getFormatPattern() {
    return formatPattern;
  }

  // ***********************************************************************
  // *                    CHANGE LISTENER METHODS
  // ***********************************************************************


  /**
   * Invoked when the target ValueHolder of the listener has changed its state.
   * This message will simply be propagated to listeners of this component.
   */
  public void stateChanged(ChangeEvent e) {
    fireStateChanged(e);
  }

  /**
   * Removes the specified change listener so that it no longer receives change events from this model.
   * Change events are generated whenever the contents of the value holder change.
   */
  public synchronized void removeChangeListener(ChangeListener l) {
    if (changeListeners != null && changeListeners.contains(l)) {
      Vector v = (Vector) changeListeners.clone();
      v.removeElement(l);
      changeListeners = v;
    }
  }

  /**
   * Adds the specified change listener to receive change events from this model.
   * Change events are generated whenever the contents of the value holder change.
   */
  public synchronized void addChangeListener(ChangeListener l) {
    Vector v = changeListeners == null ? new Vector(2) : (Vector) changeListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      changeListeners = v;
    }
  }

  /**
   * Reports a state change to all change listeners.
   */
  protected void fireStateChanged(ChangeEvent e) {
    if (changeListeners != null) {
      Vector listeners = changeListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ChangeListener) listeners.elementAt(i)).stateChanged(e);
      }
    }
  }
}
