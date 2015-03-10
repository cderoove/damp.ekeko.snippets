/**
 * MTableCellRenderer - used by the MTableColumn to render a table cell with
 * specific formatting
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
import javax.swing.table.*;
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
 * MTableCellRenderer is used by the MTableColumn to render a table cell with
 * specific formatting. The MTableCellRenderer provides the ability to use
 * Formatting by setting its <code>format</code> and <code>formatPattern</code>
 * properties.
 * @author Marty Phelan
 * @version 1.0
 */
public class MTableCellRenderer extends DefaultTableCellRenderer {
  private java.text.Format format = null;
  private String formatPattern = null;
  private int javaDataType = DataTypes.TYPE_UNDEFINED;

  /**
   * Constructs a new MTableCellRenderer
   */
  public MTableCellRenderer() {
    super();
  }

  // ***********************************************************************
  // *               TEXT PARSE/FORMAT ROUTINES
  // ***********************************************************************

  /**
   * Sets the underlying JLabel's text property using Format if defined.
   * If the value is null, it will set it to an empty String ("").
   * @param value to convert/format to String and store in the JLabel
   */
  protected void setValue(Object value) {
    if (value == null) {
      setText("");
    } else if (format == null) {
      if (javaDataType == DataTypes.TYPE_UNDEFINED) {
        javaDataType = DataTypes.getDataType(value.getClass().getName());
      }
      setText(DataTypes.format(javaDataType, value));
    } else if (format instanceof MessageFormat) {
      setText(((MessageFormat)format).format(new Object[] {value}));
    } else {
      setText(format.format(value));
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
   * Sets the format for this renderer which is used by setValue to format values.
   * @param newFormat the format for this renderer which is used by setValue to format values.
   */
  public void setFormat(java.text.Format newFormat) {
    format = newFormat;
    setupFormat();
  }

  /**
   * Sets the format for this renderer which is used by setValue to format values.
   * @return the format for this renderer which is used by setValue to format values.
   */
  public java.text.Format getFormat() {
    return format;
  }

  /**
   * Sets the format pattern for this renderer which is used by setValue to format values.
   * @param newPattern the format pattern for this renderer which is used by setValue to format values.
   */
  public void setFormatPattern(String newPattern) {
    formatPattern = newPattern;
    setupFormat();
  }

  /**
   * Gets the format pattern for this renderer which is used by setValue to format values.
   * @return the format pattern for this renderer which is used by setValue to format values.
   */
  public String getFormatPattern() {
    return formatPattern;
  }
}
