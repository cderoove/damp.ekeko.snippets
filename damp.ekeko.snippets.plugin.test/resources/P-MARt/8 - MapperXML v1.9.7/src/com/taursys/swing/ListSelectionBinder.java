/**
 * ListSelectionBinder - Synchronizes the position between a ListSelectionModel and a ListValueHolder.
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

import javax.swing.event.*;
import com.taursys.model.*;
import javax.swing.*;

/**
 * ListSelectionBinder synchronizes the position between a ListSelectionModel and a ListValueHolder.
 * It is typically used by Tables and Lists.
 * @author Marty Phelan
 * @version 1.0
 */
public class ListSelectionBinder implements ListSelectionListener {
  private com.taursys.model.ListValueHolder listValueHolder;
  private javax.swing.ListSelectionModel listSelectionModel;
  private boolean synchronizingPositions = false;

  /**
   * Constructs a new ListSelectionBinder
   */
  public ListSelectionBinder() {
  }

  /**
   * Get the ListValueHolder to synchronize positions with.
   * @return the ListValueHolder to synchronize positions with.
   */
  public ListValueHolder getListValueHolder() {
    return listValueHolder;
  }

  /**
   * Set the ListValueHolder to synchronize positions with.
   * This method also registers this ListSelectionBinder as a ListSelectionListener
   * with the given ListValueHolder.
   * @param newListValueHolder the ListValueHolder to synchronize positions with.
   */
  public void setListValueHolder(ListValueHolder newListValueHolder) {
    if (listValueHolder != null)
      listValueHolder.removeListSelectionListener(this);
    listValueHolder = newListValueHolder;
    listValueHolder.addListSelectionListener(this);
  }

  /**
   * Set the ListSelectionModel to synchronize positions with.
   * This method also registers this ListSelectionBinder as a ListSelectionListener
   * with the given ListSelectionModel.
   * @param newListSelectionModel the ListSelectionModel to synchronize positions with.
   */
  public void setListSelectionModel(ListSelectionModel newListSelectionModel) {
    if (listSelectionModel != null)
      listSelectionModel.removeListSelectionListener(this);
    listSelectionModel = newListSelectionModel;
    listSelectionModel.addListSelectionListener(this);
  }

  /**
   * Get the ListSelectionModel to synchronize positions with.
   * @return the ListSelectionModel to synchronize positions with.
   */
  public javax.swing.ListSelectionModel getListSelectionModel() {
    return listSelectionModel;
  }

  /**
   * Notification method to sync positions between ListValueHolder and ListSelectionModel.
   * This method is required by the ListSelectionListener interface.
   * This ListSelectionBinder, registers itself with both the ListValueHolder
   * and the ListSelectionModel to be notified of selection/position changes
   * initiated by either source.
   */
  public void valueChanged(ListSelectionEvent e) {
    if (!synchronizingPositions
        && listValueHolder != null && listSelectionModel != null) {
      // ListValueHolder changing position
      if (e.getSource() == listValueHolder) {
        synchronizingPositions = true;
        int row = ((ListValueHolder)e.getSource()).getPosition();
        listSelectionModel.setSelectionInterval(row, row);
        synchronizingPositions = false;
      // ListSelectionModel changing position
      } else if (e.getSource() == listSelectionModel
          && !e.getValueIsAdjusting()
          && listSelectionModel.getMinSelectionIndex() > -1) {
        synchronizingPositions = true;
        listValueHolder.moveTo(listSelectionModel.getLeadSelectionIndex());
        synchronizingPositions = false;
      }
    }
  }
}
