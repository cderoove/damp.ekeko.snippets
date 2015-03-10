/**
 * AbstractListValueHolder - A partial implementation of a ListValueHolder Interface
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

import javax.swing.event.*;
import java.util.*;
import com.taursys.model.event.*;
import com.taursys.swing.*;

/**
 * AbstractListValueHolder is a partial implementation of a ListValueHolder Interface.
 * @author Marty Phelan
 * @version 1.0
 */
public class AbstractListValueHolder extends AbstractCollectionValueHolder
    implements ListValueHolder {
  private int position = -1;
  private transient Vector listSelectionListeners;
  private boolean ignoreReset = false;
  private Comparator comparator;

  /**
   * Constructs a new AbstractListValueHolder
   */
  public AbstractListValueHolder(ObjectValueHolder holder) {
    super(holder);
  }

  /**
   * Constructs a new AbstractListValueHolder
   */
  public AbstractListValueHolder(ObjectValueHolder holder, List list) {
    super(holder, list);
  }

  /**
   * Indicates whether there is another (any) Objects in the List.
   */
  public boolean hasNext() {
    return (position+1) < size();
  }

  /**
   * Indicates whether there is a prior Object in the List.
   */
  public boolean hasPrior() {
    return position > 0;
  }

  /**
   * Indicates if the List has any Objects.
   */
  public boolean hasAny() {
    return size() > 0;
  }

  /**
   * Gets the current position in the list.  Returns -1 if position invalid.
   */
  public int getPosition() {
    return position;
  }

  /**
   * Gets the current number of rows in the list.
   */
  public int getRowCount() {
    return size();
  }

  /**
   * Makes the given row number the current available object.  You should first
   * invoke getRowCount to ensure that you are not requesting a row out of range.
   */
  public void moveTo(int row) {
    getObjectValueHolder().setObject(getList().get(row));
    position = row;
    fireValueChanged();
  }

  /**
   * Makes the last object in the List available.  You should first invoke the
   * hasAny to ensure that there is an object in the List.
   * If the list is empty, the current object will be null.
   */
  public void first() {
    if (hasAny()) {
      moveTo(0);
    } else {
      reset();
    }
  }

  /**
   * Makes the next object in the List available.  You should invoke
   * the hasNext method BEFORE invoking this method to ensure that there IS
   * a next object.
   */
  public void next() {
    position++;
    getObjectValueHolder().setObject(getList().get(position));
    fireValueChanged();
  }

  /**
   * Makes the prior object in the List available. You should first invoke the
   * hasPrior to ensure that there is a prior object in the List.
   */
  public void prior() {
    position--;
    getObjectValueHolder().setObject(getList().get(position));
    fireValueChanged();
  }

  /**
   * Makes the last object in the List available.  You should first invoke the
   * hasAny to ensure that there is an object in the List.
   * If the list is empty, the current object will be null.
   */
  public void last() {
    if (hasAny()) {
      moveTo(size() - 1);
    } else {
      reset();
    }
  }

  /**
   * Resets this holder so that you can iterate the List from the beginning.
   */
  public void reset() {
    if (!ignoreReset) {
      position = -1;
      getObjectValueHolder().setObject(null);
      fireValueChanged();
    }
  }

  /**
   * Search through the items in the list for a match based on comparing the given properties/values.
   * @param propertyNames which properties to compare for a match
   * @param values the values to match
   * @return the first item in the list which matches the criteria else -1
   */
  public int indexOf(String[] propertyNames, Object[] values)
      throws ModelException {
    if (propertyNames == null || values == null || propertyNames.length != values.length)
      throw new ModelException(ModelException.REASON_MULTI_PROPERTY_MISMATCH);
    for (int i = 0; i < size(); i++) {
      Object[] itemValues = getPropertyValues(propertyNames, i);
      if (isMatchAll(values, itemValues))
        return i;
    }
    return -1;
  }

  private boolean isMatchAll(Object[] values1, Object[] values2) {
    for (int i = 0; i < values1.length; i++) {
      if (!isMatch(values1[i], values2[i]))
        return false;
    }
    return true;
  }

  private boolean isMatch(Object value1, Object value2) {
    if (value1 == null && value2 == null)
      return true;
    if (value1 == null)
      return false;
    try {
      return ((Comparable)value1).compareTo(value2) == 0;
    } catch (ClassCastException ex) {
      com.taursys.debug.Debug.error(
          "Property in value object must be Comparable. Object=" + value1);
      return false;
    }
  }

  /**
   * Get the values for the given properties in the valueObject at the given index.
   */
  public Object[] getPropertyValues(String[] propertyNames, int index) throws ModelException {
    return getObjectValueHolder().getPropertyValues(
        propertyNames, get(index));
  }

  /**
   * Returns the value of the given property in the valueObject of the given index.
   */
  public Object getPropertyValue(String propertyName, int index) throws ModelException {
    return getObjectValueHolder().getPropertyValue(
        propertyName, getList().get(index));
  }

  /**
   * Set the values for the given properties in the valueObject.
   * Fires a StateChanged event to any listeners.
   */
  public void setPropertyValues(String[] propertyNames, Object[] values, int index)
      throws ModelException {
    getObjectValueHolder().setPropertyValues(
        propertyNames, values, get(index));
  }

  /**
   * Sets (replace/copy) the object in the current position.  You should ensure
   * that the current position is valid before invoking this method.  Depending
   * on the specific implementation, the given object may either replace the
   * current object in the list, or the property values of the given object may
   * be copied to the current object in the list.
   */
  public void setObject(Object obj) {
    getObjectValueHolder().setObject(obj);
    getList().set(position, obj);
  }

  /**
   * Sets the List that this ValueHolder will use and moves to first position (if any).
   * The list contains the set of Value Objects.
   * @param newCollection the List that this ValueHolder will use
   * @throws ClassCastException if given collection is not a List
   */
  public void setCollection(Collection newCollection) {
    setList((List)newCollection);
  }

  /**
   * Sets the List(collection) that this ValueHolder will use. Sorts the list
   * by the current comparator (if any), moves to first position (if any) and
   * broadcasts a ListContentChangeEvent and a ListSelectionEvent.
   * The list contains the set of ValueObjects.
   * @param newList the List that this ValueHolder will use
   */
  public void setList(java.util.List newList) {
    ignoreReset = true;
    super.setCollection(newList);
    ignoreReset = false;
    if (comparator != null)
      Collections.sort(getList(), comparator);
    listContentsPositionChange();
  }

  /**
   * Gets the List that this ValueHolder will use.  The list contains the set
   * of Value Objects.
   */
  public java.util.List getList() {
    return (List)getCollection();
  }

  /**
   * Set the Comparator that will be used to sort this List. The list is
   * sorted whenever you change the list (using setList) or invoke the
   * sort method.
   * @param comparator the Comparator used to sort this List.
   */
  public void setComparator(Comparator comparator) {
    this.comparator = comparator;
  }

  /**
   * Get the Comparator that will be used to sort this List. The list is
   * sorted whenever you change the list (using setList) or invoke the
   * sort method.
   * @return the Comparator used to sort this List.
   */
  public Comparator getComparator() {
    return comparator;
  }

  /**
   * Sort the list using the current comparator. After sorting, move the position
   * to the beginning of the list and broadcast a ListContentChangeEvent
   * and a ListSelectionEvent. No actions occur if the comparator is null.
   */
  public void sort() {
    if (comparator != null) {
      Collections.sort(getList(), comparator);
      listContentsPositionChange();
    }
  }

  /**
   * Move to first row in list, select object and broadcast a ListContentChangeEvent
   * and a ListSelectionEvent.
   */
  private void listContentsPositionChange() {
    Object vo = null;
    position = -1;
    if (getList() != null && size() > 0) {
      position = 0;
      vo = getList().get(position);
    }
    getObjectValueHolder().setObject(
        vo, new ListContentChangeEvent(this, vo==null));
    fireValueChanged(new ListSelectionEvent(this, position, position, false));
  }

  /**
   * Remove a listener from the list that's notified each time a change to the selection occurs.
   *
   * @param l the ListSelectionListener
   * @see #addListSelectionListener
   */
  public synchronized void removeListSelectionListener(ListSelectionListener l) {
    if (listSelectionListeners != null && listSelectionListeners.contains(l)) {
      Vector v = (Vector) listSelectionListeners.clone();
      v.removeElement(l);
      listSelectionListeners = v;
    }
  }

  /**
   * Add a listener to the list that's notified each time a change to the selection occurs.
   *
   * @param l the ListSelectionListener
   * @see #removeListSelectionListener
   */
  public synchronized void addListSelectionListener(ListSelectionListener l) {
    Vector v = listSelectionListeners == null ? new Vector(2) : (Vector) listSelectionListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      listSelectionListeners = v;
    }
  }

  /**
   * Notifies all ListSelectionListeners that the position has changed.
   * It generates a ListselectionEvent with this value holder as the source,
   * the current position as the first and last index, and false as ValueIsAdjusting.
   */
  protected void fireValueChanged() {
    fireValueChanged(new ListSelectionEvent(this, position, position, false));
  }

  /**
   * Notifies all ListSelectionListeners of the given ListSelectionEvent.
   */
  protected void fireValueChanged(ListSelectionEvent e) {
    if (listSelectionListeners != null) {
      Vector listeners = listSelectionListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ListSelectionListener) listeners.elementAt(i)).valueChanged(e);
      }
    }
  }

  // ============================================================

  /**
   * Makes the given row number the current available object.  You should first
   * invoke getRowCount to ensure that you are not requesting a row out of range.
   */
  private void reposition() {
    if (position >= getList().size())
      position = getList().size() - 1;
    Object vo = null;
    if (position >= 0)
      vo = getList().get(position);
    getObjectValueHolder().setObject(
        vo, new ListContentChangeEvent(this, vo==null));
    fireValueChanged(new ListSelectionEvent(this, position, position, false));
  }

  private void reposition(Object o) {
    position = getList().indexOf(o);
    reposition();
  }

  public Object remove() {
    Object o = getList().remove(position);
    reposition();
    return o;
  }
  public Object remove(int index) {
    Object o = getList().remove(index);
    reposition();
    return o;
  }
  public boolean remove(Object o) {
    boolean b = getList().remove(o);
    reposition();
    return b;
  }
  public boolean removeAll(Collection c) {
    boolean b = getList().removeAll(c);
    reposition();
    return b;
  }
  public boolean retainAll(Collection c) {
    boolean b = getList().retainAll(c);
    reposition();
    return b;
  }
  public void clear() {
    getList().clear();
    reposition();
  }
  public boolean add(Object o) {
    boolean b = getList().add(o);
    reposition(o);
    return b;
  }
  public void add(int index, Object element) {
    getList().add(index, element);
    reposition(element);
  }
  public boolean addAll(Collection c) {
    boolean b = getList().addAll(c);
    reposition();
    return b;
  }
  public boolean addAll(int index, Collection c) {
    boolean b = getList().addAll(index, c);
    reposition();
    return b;
  }
  public Object set(int index, Object element) {
    Object oldObj = getList().get(index);
    moveTo(index);
    setObject(element);
    return oldObj;
  }

  // =============================
  public Object get(int index) {
    return getList().get(index);
  }
  public int indexOf(Object o) {
    return getList().indexOf(o);
  }
  public int lastIndexOf(Object o) {
    return getList().lastIndexOf(o);
  }
  public ListIterator listIterator() {
    return getList().listIterator();
  }
  public ListIterator listIterator(int index) {
    return getList().listIterator(index);
  }
  public List subList(int fromIndex, int toIndex) {
    return getList().subList(fromIndex, toIndex);
  }
}
