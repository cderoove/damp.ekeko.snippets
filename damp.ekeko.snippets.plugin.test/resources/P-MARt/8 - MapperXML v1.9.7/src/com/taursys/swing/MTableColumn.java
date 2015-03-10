/**
 * MTableColumn - a TableColumn for use with an MTable
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
import com.taursys.model.ValueHolder;

/**
 * MTableColumn is a TableColumn for use with an MTable. This column contains
 * additional information for binding and formatting. The following special
 * actions take place when adding this MTableColumn to an MTable:
 * <ul>
 * <li>with the propertyName property set, the column is also added to the
 * underlying MTableModel (unless already present).
 * </li>
 * <li>the valueHolder property is set to reference the same ValueHolder
 * as the MTableModel.
 * </li>
 * <li>Finally, the modelIndex is set to point to the corresponding property
 * in the underlying MTableModel.
 * </li>
 * </ul>
 * You can also control the format, alignment, and size of the column by setting
 * the appropriate properties of this object.
 * @author Marty Phelan
 * @version 1.0
 */
public class MTableColumn extends TableColumn {
  private String propertyName;
  private com.taursys.model.ValueHolder valueHolder;

  /**
   * Constructs a new MTableColumn
   */
  public MTableColumn() {
    super(0, 75, new MTableCellRenderer(), null);
  }

  /**
   * Get the CellRenderer and cast it as an MTableCellRenderer (convience method).
   * @return the CellRenderer and cast it as an MTableCellRenderer
   */
  public MTableCellRenderer getMTableCellRenderer() {
    return (MTableCellRenderer)getCellRenderer();
  }

  /**
   * Set the Format used for rendering this column. This must be a subclass of
   * <code>java.text.Format</code>. The format is passed to the underlying
   * MTableCellRenderer to be used during rendition.
   * @param format the Format used for rendering this column
   */
  public void setFormat(java.text.Format format) {
    getMTableCellRenderer().setFormat(format);
  }

  /**
   * Get the Format used for rendering this column. This must be a subclass of
   * <code>java.text.Format</code>. The format is passed to the underlying
   * MTableCellRenderer to be used during rendition.
   * @return the Format used for rendering this column
   */
  public java.text.Format getFormat() {
    return getMTableCellRenderer().getFormat();
  }

  /**
   * Set the Format pattern used for rendering this column. This must be a valid
   * format String for the current <code>Format</code>. The format pattern
   * is passed to the underlying MTableCellRenderer to be used during rendition.
   * @param newPattern the Format pattern used for rendering this column
   */
  public void setFormatPattern(String newPattern) {
    getMTableCellRenderer().setFormatPattern(newPattern);
  }

  /**
   * Get the Format pattern used for rendering this column. This must be a valid
   * format String for the current <code>Format</code>. The format pattern
   * is passed to the underlying MTableCellRenderer to be used during rendition.
   * @return the Format pattern used for rendering this column
   */
  public String getFormatPattern() {
    return getMTableCellRenderer().getFormatPattern();
  }

  /**
   * Set the horizontal alignment of the icon and text for the cell.
   * @param alignment - One of the following constants defined in
   * SwingConstants: LEFT, CENTER (the default for image-only labels),
   * RIGHT, LEADING (the default for text-only labels) or TRAILING.
   */
  public void setHorizontalAlignment(int alignment) {
    getMTableCellRenderer().setHorizontalAlignment(alignment);
  }

  /**
   * Get the horizontal alignment of the icon and text for the cell.
   * @return alignment - One of the following constants defined in
   * SwingConstants: LEFT, CENTER (the default for image-only labels),
   * RIGHT, LEADING (the default for text-only labels) or TRAILING.
   */
  public int getHorizontalAlignment() {
    return getMTableCellRenderer().getHorizontalAlignment();
  }

  /**
   * Set the propertyName for the corresponding columnName in the MTableModel.
   * This propertyName must be a valid propertyName for the object contained
   * in the ListValueHolder of the MTableModel. When this MTableColumn is added
   * to an MTable, this propertyName is used to set the modelIndex by finding
   * the column in the MTableModel. If it is not found, this property is added
   * to the MTableModel's list of columns.
   * @param newPropertyName the propertyName for the corresponding columnName in the MTableModel.
   */
  public void setPropertyName(String newPropertyName) {
    propertyName = newPropertyName;
  }

  /**
   * Get the propertyName for the corresponding columnName in the MTableModel.
   * This propertyName must be a valid propertyName for the object contained
   * in the ListValueHolder of the MTableModel. When this MTableColumn is added
   * to an MTable, this propertyName is used to set the modelIndex by finding
   * the column in the MTableModel. If it is not found, this property is added
   * to the MTableModel's list of columns.
   * @return the propertyName for the corresponding columnName in the MTableModel.
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Set the valueHolder for this MTableColumn. This is usually set automatically
   * when this MTableColumn is added to the MTable.
   * @param newValueHolder the valueHolder for this MTableColumn.
   */
  public void setValueHolder(ValueHolder newValueHolder) {
    valueHolder = newValueHolder;
  }

  /**
   * Get the valueHolder for this MTableColumn. This is usually set automatically
   * when this MTableColumn is added to the MTable.
   * @return the valueHolder for this MTableColumn.
   */
  public com.taursys.model.ValueHolder getValueHolder() {
    return valueHolder;
  }

  /**
   * Set the Heading to display for this MTableColumn. This simply calls the
   * <code>setHeaderValue</code> with a String value rather than Object value.
   * @param newDisplayHeading the Heading to display for this MTableColumn
   */
  public void setDisplayHeading(String newDisplayHeading) {
    setHeaderValue(newDisplayHeading);
  }

  /**
   * Get the Heading to display for this MTableColumn. This simply calls the
   * <code>getHeaderValue</code> returning a String value rather than Object value.
   * @return the Heading to display for this MTableColumn
   */
  public String getDisplayHeading() {
    if (getHeaderValue() == null)
      return null;
    else
      return getHeaderValue().toString();
  }
}
