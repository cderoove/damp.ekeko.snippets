/**
 * ObjectValueHolder - ValueHolder which stores an Object value internally
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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.taursys.model.event.ContentChangeEvent;
import java.util.Vector;
import com.taursys.util.DataTypes;

/**
 * ValueHolder which stores a Single Object value internally.
 */
public class ObjectValueHolder extends AbstractValueHolder
    implements ChangeListener {
  protected Object obj;
  private ValueHolder parentValueHolder = null;
  private String parentPropertyName = null;

  /**
   * Constructs a new ObjectValueHolder
   */
  public ObjectValueHolder() {
  }

  // ***********************************************************************
  // *                    INTERNAL OBJECT ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Get the internal object for this holder.
   * @return the internal object for this holder.
   */
  public Object getObject() {
    return obj;
  }

  /**
   * Set the internal object for this holder.
   * @param obj the internal object for this holder.
   */
  public void setObject(Object obj) {
    setObject(obj,
        new ContentChangeEvent(this, obj == null));
  }

  /**
   * Set the internal object for this holder. It then fires the
   * given ChangeEvent to all ChangeListeners (if e is not null).
   * @param obj the internal object for this holder.
   * @param e ChangeEvent to fire to all ChangeListeners (if not null)
   */
  public void setObject(Object obj, ChangeEvent e) {
    this.obj = obj;
    if (e != null)
      fireStateChanged(e);
  }

  // ***********************************************************************
  // *                    PROPERTY VALUE ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Get javaDataType always returns DataTypes.TYPE_UNDEFINED (propertyName is ignored)
   * @return always returns DataTypes.TYPE_UNDEFINED (propertyName is ignored)
   */
  public int getJavaDataType(String propertyName) throws ModelException {
    return DataTypes.TYPE_UNDEFINED;
  }

  /**
   * Get the internal value (propertyName is ignored).
   * @param propertyName ignored
   * @return the internal value
   */
  public Object getPropertyValue(String propertyName) throws ModelException {
    return obj;
  }

  /**
   * Sets the internal object (propertyName is ignored).
   * Fires a StateChanged event to any listeners.
   */
  public void setPropertyValue(String propertyName, Object value)
      throws ModelException {
    obj = value;
    if (!isMultiplePropertiesChanging())
      fireContentValueChanged();
  }

  /**
   * Unsupported operation has no effect.
   */
  protected void setPropertyValues(String propertyName, Object value, Object vo)
      throws ModelException {
  }

  /**
   * Unsupported operation has no effect.
   */
  protected void setPropertyValues(String[] propertyNames, Object[] values, Object vo)
      throws ModelException {
  }

  // ***********************************************************************
  // *                         PARENT RELATED METHODS
  // ***********************************************************************

  /**
   * Set the parent ValueHolder for this VOCollectionValueHolder.
   * The parent ValueHolder will provide the Collection for this ValueHolder.
   * If the parent is also a CollectionValueHolder, then whenever the
   * parent moves to a new row, this VOCollectionValueHolder will receive
   * a notification and will retrieve its new collection from the parent.
   * @param parentValueHolder the parent ValueHolder for this VOCollectionValueHolder.
   */
  public void setParentValueHolder(ValueHolder parentValueHolder) {
    if (parentValueHolder != null)
      parentValueHolder.removeChangeListener(this);
    this.parentValueHolder = parentValueHolder;
    if (parentValueHolder != null)
      parentValueHolder.addChangeListener(this);
  }

  /**
   * Get the parent ValueHolder for this VOCollectionValueHolder.
   * The parent ValueHolder will provide the Collection for this ValueHolder.
   * If the parent is also a CollectionValueHolder, then whenever the
   * parent moves to a new row, this VOCollectionValueHolder will receive
   * a notification and will retrieve its new collection from the parent.
   * @return the parent ValueHolder for this VOCollectionValueHolder.
   */
  public ValueHolder getParentValueHolder() {
    return parentValueHolder;
  }

  /**
   * Set the property name of the Collection in the parentValueHolder for this
   * VOCollectionValueHolder.
   * The parent ValueHolder will provide the Collection for this ValueHolder.
   * If the parent is also a CollectionValueHolder, then whenever the
   * parent moves to a new row, this VOCollectionValueHolder will receive
   * a notification and will retrieve its new collection from the parent.
   * @param parentValueHolder the property name of the Collection in the
   * parentValueHolder for this VOCollectionValueHolder.
   */
  public void setParentPropertyName(String parentPropertyName) {
    this.parentPropertyName = parentPropertyName;
  }

  /**
   * Get the property name of the Collection in the parentValueHolder for this
   * VOCollectionValueHolder.
   * The parent ValueHolder will provide the Collection for this ValueHolder.
   * If the parent is also a CollectionValueHolder, then whenever the
   * parent moves to a new row, this VOCollectionValueHolder will receive
   * a notification and will retrieve its new collection from the parent.
   * @return the property name of the Collection in the parentValueHolder
   * for this VOCollectionValueHolder.
   */
  public String getParentPropertyName() {
    return parentPropertyName;
  }

  /**
   * Invoked by the parentValueHolder whenever there is a change in its value.
   * The parent ValueHolder will provide the Collection for this ValueHolder.
   * If the parent is also a CollectionValueHolder, then whenever the
   * parent moves to a new row, this VOCollectionValueHolder will receive
   * a notification and will retrieve its new collection from the parent.
   * @param e the ChangeEvent from the parentValueHolder
   */
  public void stateChanged(ChangeEvent e) {
    try {
      if (parentPropertyName != null && parentPropertyName.length() > 0) {
        Object o = parentValueHolder.getPropertyValue(parentPropertyName);
        setObject(o);
      }
    } catch (Exception ex) {
      com.taursys.debug.Debug.error(
          "Problem getting new value object from parent",ex);
      setObject(null);
    }
  }

  // ***********************************************************************
  // *                   PROTECTED ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Get the value for the given property in the given obj.
   */
  protected Object getPropertyValue(String propertyName, Object vo)
      throws ModelException {
    return vo;
  }

  /**
   * Get the values for the given properties in the given obj.
   */
  protected Object[] getPropertyValues(String[] propertyNames, Object vo)
      throws ModelException {
    return new Object[] {vo};
  }

  // ***********************************************************************
  // *                    CHANGE LISTENER METHODS
  // ***********************************************************************

  /**
   * Adds the specified change listener to receive change events from this
   * ObjectValueHolder. Fires an initial ContentChangedEvent to the given
   * listener if the internal object has a value (not null).
   * Change events are normally generated whenever the contents of the value
   * holder change.
   * @param l the specified change listener to receive change events from this
   * ObjectValueHolder
   */
  public synchronized void addChangeListener(ChangeListener l) {
    super.addChangeListener(l);
//    l.stateChanged(new ContentChangeEvent(this, obj == null));
  }

  /**
   * Notifies all ChangeListeners that the contents of this value holder have
   * changed. A ContentChangeEvent event is fired which specifies whether or
   * not the current contents are null.
   */
  public void fireContentValueChanged() {
    fireStateChanged(new ContentChangeEvent(this, obj == null));
  }
}
