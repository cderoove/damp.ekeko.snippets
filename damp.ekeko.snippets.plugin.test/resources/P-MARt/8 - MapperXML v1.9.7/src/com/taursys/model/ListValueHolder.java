/**
 * ListValueHolder -
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

import javax.swing.event.ListSelectionListener;
import java.util.List;

/**
 * ListValueHolder is ...
 * @author Marty Phelan
 * @version 1.0
 */
public interface ListValueHolder extends CollectionValueHolder, List {

  /**
   * Returns the value of the given property in the valueObject of the given row.
   * Does not change the current position of the list.
   */
  public Object getPropertyValue(String propertyName, int row) throws ModelException;

  /**
   * Get the values for the given properties in the valueObject at the given index.
   */
  public Object[] getPropertyValues(String[] propertyNames, int index) throws ModelException;

  /**
   * Search through the items in the list for a match based on comparing the
   * given properties/values.
   * @param propertyNames which properties to compare for a match
   * @param values the values to match
   * @return the first item in the list which matches the criteria else -1
   */
  public int indexOf(String[] propertyNames, Object[] values)
      throws ModelException;

  /**
   * Indicates whether there is a prior Object in the List.
   */
  public boolean hasPrior();

  /**
   * Indicates if the List has any Objects.
   */
  public boolean hasAny();

  /**
   * Makes the given row number the current available object.  You should first
   * invoke getRowCount to ensure that you are not requesting a row out of range.
   */
  public void moveTo(int row);

  /**
   * Gets the current position in the list.  Returns -1 if position invalid.
   */
  public int getPosition();

  /**
   * Gets the current number of rows in the list.
   */
  public int getRowCount();

  /**
   * Makes the last object in the List available.  You should first invoke the
   * hasAny to ensure that there is an object in the List.
   * If the list is empty, the current object will be null.
   */
  public void first();

  /**
   * Makes the prior object in the List available. You should first invoke the
   * hasPrior to ensure that there is a prior object in the List.
   */
  public void prior();

  /**
   * Makes the last object in the List available.  You should first invoke the
   * hasAny to ensure that there is an object in the List.
   * If the list is empty, the current object will be null.
   */
  public void last();

  /**
   * Remove a listener from the list that's notified each time a change to the selection occurs.
   *
   * @param l the ListSelectionListener
   * @see #addListSelectionListener
   */
  public void removeListSelectionListener(ListSelectionListener l);

  /**
   * Add a listener to the list that's notified each time a change to the selection occurs.
   *
   * @param l the ListSelectionListener
   * @see #removeListSelectionListener
   */
  public void addListSelectionListener(ListSelectionListener l);
 }
