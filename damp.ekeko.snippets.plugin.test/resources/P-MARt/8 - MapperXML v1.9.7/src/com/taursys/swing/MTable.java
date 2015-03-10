/**
 * MTable - an extension of the JTable which binds to data in a ListValueHolder.
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

import javax.swing.table.*;
import javax.swing.*;
import javax.swing.JTable;
import com.taursys.model.*;
import javax.swing.event.*;
import java.util.List;

/**
 * MTable is an extension of the JTable which binds to data in a ListValueHolder.
 * When you create a new MTable, it creates all the required internal subcomponents
 * needed to be fully functional. You can change any of these subcomponents as
 * needed. Typically, you will create this MTable, set its listValueHolder
 * property, and indicate which properties to display by setting the columnNames
 * property.
 * <p>
 * There are two ways to setup the columns to display. The first is by simply
 * setting the columnNames property to a String array of property names. This
 * will use default formatting, widths and column titles.
 * Below is an example of this approach:
 * <pre>
 * private MTable holdersTable = new MTable();
 * private VOListValueHolder holder = new VOListValueHolder();
 * ...
 * private void jbInit() throws Exception {
 *   holder.setValueObjectClass(com.taursys.tools.ValueHolderInfo.class);
 *   holdersTable.setListValueHolder(holder);
 *   holdersTable.setColumnNames(new String[] {
 *       "holderName","holderAlias",});
 * ...
 * </pre>
 * The second method gives you much more control over presentation. In this
 * approach, you create an <code>MTableColumn</code> for each column and set
 * its properties. You then add this column to the <code>MTable</code> using
 * the <code>addColumn</code> method. Below is an example of this approach:
 * <pre>
 * private MTable holdersTable = new MTable();
 * private VOListValueHolder holder = new VOListValueHolder();
 * private MTableColumn holderNameColumn = new MTableColumn();
 * private MTableColumn holderAliasColumn = new MTableColumn();
 * ...
 * private void jbInit() throws Exception {
 *   holder.setValueObjectClass(com.taursys.tools.ValueHolderInfo.class);
 *   holdersTable.setListValueHolder(holder);
 *   holderNameColumn.setPreferredWidth(80);
 *   holderNameColumn.setDisplayHeading("Name");
 *   holderNameColumn.setPropertyName("holderName");
 *   holderNameColumn.setValueHolder(holder);
 *   holderNameColumn.setHeaderValue("Holder Name");
 *   holdersTable.addColumn(holderNameColumn);
 *   holderAliasColumn.setDisplayHeading("Alias");
 *   holderAliasColumn.setPropertyName("holderAlias");
 *   holdersTable.addColumn(holderAliasColumn);
 * ...
 * </pre>
 * The MTable will display the 2 listed properties for every object in the
 * holder. You can add or remove any object from the holder and the changes
 * will be displayed in the table.  Further, if the class which is contained
 * in the VOListValueHolder implements the BoundValueObject interface, any
 * changes to the values will be immediately displayed in the table.
 * <p>
 * The MTable uses a MTableModel which extends the of the AbstractTableModel.
 * The MTableModel uses a ListValueHolder to contain the actual data for this
 * table. The MTableModel uses a ListSelectionBinder to synchronize the
 * position in this table and the ListValueHolder.
 * @author Marty Phelan
 * @version 1.0
 */
public class MTable extends JTable {

  /**
   * Construct a new MTable.
   */
  public MTable() {
    setAutoCreateColumnsFromModel(true);
    getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  /**
   * Create a TableModel to be used by this MTable. By default, this method
   * returns a MTableModel. It also sets the MTableModel's listSelectionModel
   * to this MTable's listSelectionModel.
   * @return a TableModel to be used by this MTable.
   */
  protected TableModel createDefaultDataModel() {
    MTableModel m = new MTableModel();
    m.setListSelectionModel(getSelectionModel());
    return m;
  }

  /**
   * Set the class of the value object.  Only needed if the valueObject
   * itself can be null.  If set, this takes presidence over the actual
   * class of the valueObject. This property is only effective if the internal
   * ListValueHolder is an instanceof VOListValueHolder (which is the default).
   */
  public void setValueObjectClass(Class newValueObjectClass) {
    if (getModel() != null && getModel() instanceof MTableModel) {
      getMTableModel().setValueObjectClass(newValueObjectClass);
    }
  }

  /**
   * Get the class of the value object.  Only needed if the valueObject
   * itself can be null.  If set, this takes presidence over the actual
   * class of the valueObject. This property is only effective if the internal
   * ListValueHolder is an instanceof VOListValueHolder (which is the default).
   */
  public Class getValueObjectClass() {
    if (getModel() != null && getModel() instanceof MTableModel) {
      return getMTableModel().getValueObjectClass();
    } else {
      return null;
    }
  }

  /**
   * Set the List that the internal ListValueHolder will use. This property is
   * only effective if the internal ListValueHolder is an instanceof
   * VOListValueHolder (which is the default).
   */
  public void setList(List newList) {
    if (getModel() != null && getModel() instanceof MTableModel) {
      getMTableModel().setList(newList);
    }
  }

  /**
   * Gets the List that the internal ListValueHolder will use. This property is
   * only effective if the internal ListValueHolder is an instanceof
   * VOListValueHolder (which is the default).
   */
  public List getList() {
    if (getModel() != null && getModel() instanceof MTableModel) {
      return getMTableModel().getList();
    } else {
      return null;
    }
  }

  /**
   * Get the current MTableModel for this MTable.
   * @return the current MTableModel for this MTable.
   * @throws ClassCastException if the internal model does not subclass MTableModel.
   */
  protected MTableModel getMTableModel() {
    return (MTableModel)getModel();
  }

  /**
   * Get the ListValueHolder used by the MTableModel
   * @return the ListValueHolder used by the MTableModel
   */
  public ListValueHolder getListValueHolder() {
    return getMTableModel().getListValueHolder();
  }

  /**
   * Set the ListValueHolder used by the MTableModel.
   * This method also binds the
   * @param newListValueHolder the ListValueHolder used by the MTableModel
   */
  public void setListValueHolder(ListValueHolder newListValueHolder) {
    getMTableModel().setListValueHolder(newListValueHolder);
  }

  /**
   * Set which columnNames (property names) to display in this MTable.
   * @param newColumnNames a String array of which columnNames (property names) to display in this MTable.
   */
  public void setColumnNames(String[] newColumnNames) {
    getMTableModel().setColumnNames(newColumnNames);
  }

  /**
   * Get which columnNames (property names) to display in this MTable.
   * @return a String array of which columnNames (property names) to display in this MTable.
   */
  public String[] getColumnNames() {
    return getMTableModel().getColumnNames();
  }

  /**
   * Add column to ColumnModel for this table (and possibly to MTableModel).
   * If the given column is a MTableColumn, additional operations are performed.
   * It will try to set the modelIndex of the MTableColumn based on the
   * MTableColumn's propertyName property. If the propertyName is null or
   * blank, no action will occur. It will attempt to determine the modelIndex
   * by looking up the propertyName in the MTableModel. If not found, it will add
   * the propertyName to the MTableModel. It will then set the MTableColumn's
   * modelIndex to the index in the MTableModel. It will also set the
   * MTableColumn's valueHolder to the MTableModel's valueHolder.
   */
  public void addColumn(TableColumn newColumn) {
    if (newColumn != null && newColumn instanceof MTableColumn) {
      MTableColumn col = (MTableColumn)newColumn;
      String propName = col.getPropertyName();
      if (propName != null && propName.length() > 0) {
        int index = getMTableModel().findColumn(propName);
        if (index == -1) {
          getMTableModel().addColumn(propName);
          index = getMTableModel().findColumn(propName);
        }
        col.setModelIndex(index);
        col.setValueHolder(getMTableModel().getListValueHolder());
        super.addColumn(newColumn);
      } else {
        com.taursys.debug.Debug.error("Attempt to add a MTableColumn without a propertyName");
      }
    } else {
      super.addColumn(newColumn);
    }
  }
}
