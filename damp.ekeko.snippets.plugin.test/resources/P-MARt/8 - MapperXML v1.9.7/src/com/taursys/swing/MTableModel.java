/**
 * MTableModel - Table model for use with the MTable
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
import com.taursys.model.*;
import com.taursys.util.*;
import javax.swing.event.*;
import com.taursys.model.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * MTableModel is a table model for use with the MTable.  It provides binding to
 * data in a ListValueHolder.  You must set the listValueHolder and columnNames
 * properties to use this model.
 * @author Marty Phelan
 * @version 1.0
 */
public class MTableModel extends AbstractTableModel implements ChangeListener {
  private ListSelectionBinder listSelectionBinder = new ListSelectionBinder();
  private ListValueHolder listValueHolder;
  private ArrayList columns = new ArrayList();
  private javax.swing.ListSelectionModel listSelectionModel;

  /**
   * Constructs a new MTableModel
   */
  public MTableModel() {
    listValueHolder = createDefaultListValueHolder();

  }

  protected ListValueHolder createDefaultListValueHolder() {
    return new VOListValueHolder();
  }

  /**
   * Sets the class of the value object.  Only needed if the valueObject
   * itself can be null.  If set, this takes presidence over the actual
   * class of the valueObject. This property is only effective if the internal
   * ListValueHolder is an instanceof VOListValueHolder (which is the default).
   */
  public void setValueObjectClass(Class newValueObjectClass) {
    if (listValueHolder != null && listValueHolder instanceof VOListValueHolder) {
      ((VOListValueHolder)listValueHolder).setValueObjectClass(newValueObjectClass);
    }
  }

  /**
   * Returns the class of the value object.  Only needed if the valueObject
   * itself can be null.  If set, this takes presidence over the actual
   * class of the valueObject. This property is only effective if the internal
   * ListValueHolder is an instanceof VOListValueHolder (which is the default).
   */
  public Class getValueObjectClass() {
    if (listValueHolder != null && listValueHolder instanceof VOListValueHolder) {
      return ((VOListValueHolder)listValueHolder).getValueObjectClass();
    } else {
      return null;
    }
  }

  /**
   * Sets the List that the internal ListValueHolder will use. This property is
   * only effective if the internal ListValueHolder is an instanceof
   * VOListValueHolder (which is the default).
   */
  public void setList(List newList) {
    if (listValueHolder != null && listValueHolder instanceof VOListValueHolder) {
      ((VOListValueHolder)listValueHolder).setList(newList);
      listValueHolder.first();
    }
  }

  /**
   * Gets the List that the internal ListValueHolder will use. This property is
   * only effective if the internal ListValueHolder is an instanceof
   * VOListValueHolder (which is the default).
   */
  public List getList() {
    if (listValueHolder != null && listValueHolder instanceof VOListValueHolder) {
      return ((VOListValueHolder)listValueHolder).getList();
    } else {
      return null;
    }
  }


  public boolean isCellEditable(int parm1, int parm2) {
    return false;
  }

  public int getColumnCount() {
    return columns.size();
  }

  public String getColumnName(int col) {
    return (String)columns.get(col);
  }

  public int findColumn(String columnName) {
    return columns.indexOf(columnName);
  }

  public Object getValueAt(int row, int col) {
    try {
      return listValueHolder.getPropertyValue((String)columns.get(col), row);
    } catch (ModelException ex) {
      return ex.getMessage();
    }
  }

  public int getRowCount() {
    if (listValueHolder == null)
      return 0;
    return listValueHolder.getRowCount();
  }

  public Class getColumnClass(int col) {
    try {
      return DataTypes.getClassForType(listValueHolder.getJavaDataType((String)columns.get(col)));
    } catch (ModelException ex) {
      return String.class;
    }
  }

  public void setListValueHolder(com.taursys.model.ListValueHolder newListValueHolder) {
    listSelectionBinder.setListValueHolder(newListValueHolder);
    if (listValueHolder != null)
      listValueHolder.removeChangeListener(this);
    listValueHolder = newListValueHolder;
    listValueHolder.addChangeListener(this);
    fireTableStructureChanged();
  }

  public com.taursys.model.ListValueHolder getListValueHolder() {
    return listValueHolder;
  }

  public void setListSelectionModel(javax.swing.ListSelectionModel newListSelectionModel) {
    listSelectionModel = newListSelectionModel;
    listSelectionBinder.setListSelectionModel(listSelectionModel);
  }

  public javax.swing.ListSelectionModel getListSelectionModel() {
    return listSelectionModel;
  }

  public void setColumnNames(String[] newColumnNames) {
    columns.clear();
    columns.addAll(java.util.Arrays.asList(newColumnNames));
    fireTableStructureChanged();
  }

  public String[] getColumnNames() {
    return (String[])columns.toArray(new String[]{});
  }

  public int getColumnIndex(String columnName) {
    return columns.indexOf(columnName);
  }

  public void addColumn(String columnName) {
    columns.add(columnName);
  }

  public void removeColumn(String columnName) {
    columns.remove(columnName);
  }

  public void stateChanged(ChangeEvent e) {
    if (e instanceof ContentValueChangeEvent) {
      fireTableCellUpdated(listValueHolder.getPosition(),
          getColumnIndex(((ContentValueChangeEvent)e).getPropertyName()));
    } else if (e instanceof ListContentChangeEvent) {
      fireTableDataChanged();
    } else if (e instanceof ContentChangeEvent) {
      fireTableRowsUpdated(listValueHolder.getPosition(),listValueHolder.getPosition());
    } else if (e instanceof StructureChangeEvent) {
      fireTableStructureChanged();
    }
  }
}
