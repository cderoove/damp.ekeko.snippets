/**
 * MComboBoxModel - A ComboBoxModel for use with an MComboBox
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

import javax.swing.ComboBoxModel;
import javax.swing.AbstractListModel;
import javax.swing.event.*;
import java.util.*;
import com.taursys.model.*;
import com.taursys.model.event.*;

/**
 * MComboBoxModel is a ComboBoxModel for use with an MComboBox.
 * @author Marty Phelan
 * @version 2.0
 */
public class MComboBoxModel extends AbstractListModel
    implements ComboBoxModel {
  private com.taursys.model.ListValueHolder listValueHolder;
  transient private Vector listDataListeners;
  private com.taursys.model.ValueHolder valueHolder;
  private String[] propertyNames = new String[] {"value"};
  private String[] listPropertyNames = new String[] {"value"};
  private ListChangeListener listChangeListener =
      new ListChangeListener();
  private ValueHolderChangeListener valueHolderChangeListener =
      new ValueHolderChangeListener();
  private boolean settingSelection = false;
  private boolean nullAllowed = false;
  private String nullDisplay = "--none--";
  private NullObject nullObject = new NullObject();
  private Object selectedItem = null;
  transient private Vector enableListeners;

  // *************************************************************************
  //                              Constructors
  // *************************************************************************

  /**
   * Constructs a new model with an empty option list.
   */
  public MComboBoxModel() {
    setListValueHolder(new ObjectListValueHolder());
    setValueHolder(new ObjectValueHolder());
  }

  /**
   * Constructs a new model with the given items as the option list.
   */
  public MComboBoxModel(Object[] items) {
    if (items != null) {
      setListValueHolder(new ObjectListValueHolder(
          new ArrayList(Arrays.asList(items))));
    } else {
      setListValueHolder(new ObjectListValueHolder());
    }
    setValueHolder(new ObjectValueHolder());
  }

  // *************************************************************************
  //                            Selection Methods
  // *************************************************************************

  /**
   * Set the selected item in the list.
   * @param anItem the selected item in the list.
   */
  public void setSelectedItem(Object anItem) {
    try {
      settingSelection = true;
      if (anItem instanceof NullObject) {
        Object[] values = new Object[listPropertyNames.length];
        // next
          valueHolder.setPropertyValues(
              propertyNames, values);
          this.selectedItem = anItem;
      } else {
        // get index of selection
        int index = listValueHolder.indexOf(anItem);
        if (index > -1) {
          Object[] values = listValueHolder.getPropertyValues(
            listPropertyNames, index);
          valueHolder.setPropertyValues(
              propertyNames, values);
          this.selectedItem = anItem;
        }
      }
    } catch (Exception ex) {
      com.taursys.debug.Debug.debug("Error during setSelectedItem", ex);
    } finally {
      settingSelection = false;
    }
  }

  /**
   * Get the selected item in the list.
   * @return the selected item in the list.
   */
  public Object getSelectedItem() {
    return selectedItem;
  }

  /**
   * Get the current size of the list.
   * @return the current size of the list.
   */
  public int getSize() {
    if (listValueHolder == null)
      return 0;
    else
      if (isNullAllowed())
        return listValueHolder.getRowCount() + 1;
      else
        return listValueHolder.getRowCount();
  }

  /**
   * Get the item from the list at the given index.
   * @param index indicates which item to return.
   * @return the item from the list at the given index.
   */
  public Object getElementAt(int index) {
    if (listValueHolder == null)
      return null;
    else
      if (isNullAllowed())
        if (index == 0) {
          return nullObject;
        } else {
          return listValueHolder.get(index - 1);
        }
      else
        return listValueHolder.get(index);
  }

  // =======================================================================
  //                      ValueHolder Properties
  // =======================================================================

  /**
   * Set the ValueHolder which will store the selection
   * @param valueHolder the ValueHolder which will store the selection
   */
  public void setValueHolder(ValueHolder valueHolder) {
    if (valueHolder != null)
      valueHolder.removeChangeListener(valueHolderChangeListener);
    this.valueHolder = valueHolder;
    if (valueHolder != null)
      valueHolder.addChangeListener(valueHolderChangeListener);
    fireEnableChange(
        new EnableEvent(this, isChangeable()));
  }

  /**
   * Get the ValueHolder which will store the selection
   * @return the ValueHolder which will store the selection
   */
  public ValueHolder getValueHolder() {
    return valueHolder;
  }

  /**
   * Set the property name in the ValueHolder which will store the selection.
   * This name must correspond with the listPropertyName.
   * @param propertyName the property name in the ValueHolder which will store the selection.
   */
  public void setPropertyName(String propertyName) {
    if (propertyNames == null || propertyNames.length < 1)
      propertyNames = new String[] {propertyName};
    else
      propertyNames[0] = propertyName;
  }

  /**
   * Get the property name in the ValueHolder which will store the selection.
   * This name must correspond with the listPropertyName.
   * @return the property name in the ValueHolder which will store the selection.
   */
  public String getPropertyName() {
    if (propertyNames == null || propertyNames.length < 1)
      return null;
    else
      return propertyNames[0];
  }

  /**
   * Set the property names in the ValueHolder which will store the selection.
   * The selection can be associated with multiple properties in the value holder.
   * These property names must correspond to the listPropertyNames.
   * @param propertyNames the property names in the ValueHolder which will store the selection.
   */
  public void setPropertyNames(String[] propertyNames) {
    propertyNames = propertyNames;
  }

  /**
   * Get the property names in the ValueHolder which will store the selection.
   * The selection can be associated with multiple properties in the value holder.
   * These property names must correspond to the listPropertyNames.
   * @return the property names in the ValueHolder which will store the selection.
   */
  public String[] getPropertyNames() {
    return propertyNames;
  }

  /**
   * Invoked whenever a ChangeEvent is generated by the valueHolder.
   */
  private class ValueHolderChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      if (!settingSelection && e instanceof ContentChangeEvent) {
        if (propertyNames != null
            && listPropertyNames != null) {
          // sync selection
          try {
            // get values from valueHolder
            Object[] values = valueHolder.getPropertyValues(propertyNames);
            // find match in list
            int selectedItemIndex = listValueHolder.indexOf(
                listPropertyNames, values);
            // if found, then set selection
            if (selectedItemIndex > -1) {
              selectedItem = listValueHolder.get(selectedItemIndex);
            } else {
              if (isNullAllowed())
                selectedItem = nullObject;
              else
                selectedItem = null;
            }
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        fireEnableChange(
            new EnableEvent(this, !((ContentChangeEvent)e).isContentNull()));
      }
    }
  }

  // =======================================================================
  //                     ListValueHolder Properties
  // =======================================================================

  /**
   * Set the valueHolder with the list of options for this component.
   * @param newListValueHolder the valueHolder with the list of options for
   * this component.
   */
  public void setListValueHolder(ListValueHolder newListValueHolder) {
    if (listValueHolder != null)
      listValueHolder.removeChangeListener(listChangeListener);
    listValueHolder = newListValueHolder;
    if (listValueHolder != null)
      listValueHolder.addChangeListener(listChangeListener);
  }

  /**
   * Get the valueHolder with the list of options for this component.
   * @return the valueHolder with the list of options for this component.
   */
  public ListValueHolder getListValueHolder() {
    return listValueHolder;
  }

  /**
   * Set the property name in the ListValueHolder which will provide the
   * selection value. This name must correspond with the propertyName for the
   * valueHolder.
   * @param listPropertyName the property name in the ListValueHolder which will
   * provide the selection value.
   */
  public void setListPropertyName(String listPropertyName) {
    if (listPropertyNames == null || listPropertyNames.length < 1)
      listPropertyNames = new String[] {listPropertyName};
    else
      listPropertyNames[0] = listPropertyName;
  }

  /**
   * Get the property name in the ListValueHolder which will provide the
   * selection value. This name must correspond with the propertyName for the
   * valueHolder.
   * @return the property name in the ListValueHolder which will provide the
   * selection value.
   */
  public String getListPropertyName() {
    if (listPropertyNames == null || listPropertyNames.length < 1)
      return null;
    else
      return listPropertyNames[0];
  }

  /**
   * Set the property names in the ListValueHolder which will provide the
   * selection values. These names must correspond with the propertyNames for
   * the valueHolder.
   * @param listPropertyNames the property names in the ListValueHolder which
   * will provide the selection values.
   */
  public void setListPropertyNames(String[] listPropertyNames) {
    this.listPropertyNames = listPropertyNames;
  }

  /**
   * Get the property names in the ListValueHolder which will provide the
   * selection values. These names must correspond with the propertyNames for
   * the valueHolder.
   * @return the property names in the ListValueHolder which will provide the
   * selection values.
   */
  public String[] getListPropertyNames() {
    return listPropertyNames;
  }

  /**
   * Invoked whenever a ChangeEvent is generated by the listValueHolder.
   */
  private class ListChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      if (e instanceof ContentValueChangeEvent) {
        fireContentsChanged(this, 0, listValueHolder.size() - 1);
      } else if (e instanceof ListContentChangeEvent) {
        fireContentsChanged(this, 0, listValueHolder.size() - 1);
      } else if (e instanceof ContentChangeEvent) {
        fireContentsChanged(this, 0, listValueHolder.size() - 1);
      } else if (e instanceof StructureChangeEvent) {
        fireContentsChanged(this, 0, listValueHolder.size() - 1);
      }
    }
  }

  // =======================================================================
  //                        Null Value Properties
  // =======================================================================

  /**
   * Sets indicator that a null value is a valid selection.
   * Default is true.
   */
  public void setNullAllowed(boolean nullAllowed) {
    this.nullAllowed = nullAllowed;
    if (nullAllowed && selectedItem == null)
      selectedItem = nullObject;
    else if (!nullAllowed && selectedItem instanceof NullObject)
      selectedItem = null;
  }

  /**
   * Returns indicator that a null value is a valid selection.
   * Default is true.
   */
  public boolean isNullAllowed() {
    return nullAllowed;
  }

  /**
   * Sets text to display in list for a null value.
   */
  public void setNullDisplay(String text) {
    nullObject.setText(text);
  }

  /**
   * Returns text to display in list for a null value.
   */
  public String getNullDisplay() {
    return nullObject.getText();
  }

  // =======================================================================
  //                    Enable Change Related Properties
  // =======================================================================

  /**
   * Removes the given listener from the list that is notified each time the
   * enabled state changes. EnableEvents are generated whenever
   * the contents of the ValueHolder change. They indicate whether or not a
   * component can modify the current contents of the ValueHolder (not-null).
   * @param l the EnableListener to remove from the notify list.
   */
  public synchronized void removeEnableListener(EnableListener l) {
    if (enableListeners != null && enableListeners.contains(l)) {
      Vector v = (Vector) enableListeners.clone();
      v.removeElement(l);
      enableListeners = v;
    }
  }

  /**
   * Adds the given listener to the list that is notified each time the
   * enabled state changes. EnableEvents are generated whenever the contents
   * of the ValueHolder change. They indicate whether or not a component
   * can modify the current contents of the ValueHolder (not-null).
   * @param l the EnableListener to add to the notify list.
   */
  public synchronized void addEnableListener(EnableListener l) {
    Vector v = enableListeners == null ? new Vector(2) : (Vector) enableListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      enableListeners = v;
    }
  }

  /**
   * Notify the listeners that the enabled state has changed. EnableEvents are
   * generated whenever the contents of the ValueHolder change. They indicate
   * whether or not a component can modify the current contents of the
   * ValueHolder (not-null).
   * @param e the EnableEvent to send to the listeners on the notify list.
   */
  protected void fireEnableChange(EnableEvent e) {
    if (enableListeners != null) {
      Vector listeners = enableListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((EnableListener) listeners.elementAt(i)).enableChange(e);
      }
    }
  }

  /**
   * Get indicator whether or not the the property value of the ValueHolder
   * can be changed.  A VO type ValueHolder can only be changed if its object
   * is not null.  A Collection type ValueHolder can only be changes if it is
   * not empty.
   */
  private boolean isChangeable() {
    if (valueHolder == null)
      return false;
    if (valueHolder instanceof VOValueHolder)  {
      return ((VOValueHolder)valueHolder).getObject() != null;
    } else if (valueHolder instanceof CollectionValueHolder) {
      if (((CollectionValueHolder)valueHolder).isEmpty())
        return false;
      else if (valueHolder instanceof VOCollectionValueHolder ||
               valueHolder instanceof VOListValueHolder)
        return ((CollectionValueHolder)valueHolder).getObject() != null;
      else
        return true;
    } else {
      return true;
    }
  }

}
