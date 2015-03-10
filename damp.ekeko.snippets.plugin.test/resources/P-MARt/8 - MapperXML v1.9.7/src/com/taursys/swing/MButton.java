/**
 * MButton - A JButton which can be bound to a ValueHolder to control its enable state.
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

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.taursys.model.event.ContentChangeEvent;
import com.taursys.model.ValueHolder;
import com.taursys.model.ObjectValueHolder;
import com.taursys.model.VOValueHolder;
import com.taursys.model.CollectionValueHolder;
import com.taursys.model.VOCollectionValueHolder;
import com.taursys.model.VOListValueHolder;

/**
 * MButton is a JButton which can be bound to a ValueHolder to control its enable state.
 * It listens for a ContentChangeEvent and inspects the contentNull property
 * of the event.  If contentNull is true, this button disables itself, otherwise
 * it enables itself.
 * @author Marty Phelan
 * @version 1.0
 */
public class MButton extends JButton implements ChangeListener {
  private com.taursys.model.ValueHolder valueHolder;

  /**
   * Constructs a new MButton
   */
  public MButton() {
    setValueHolder(new ObjectValueHolder());
  }

  /**
   * Invoked whenever a change event.  Only responds if the event is a
   * ContentChangeEvent. If the ContentChangeEvent's contentNull is true,
   * this button will be disabled, otherwise it will be enabled.
   */
  public void stateChanged(ChangeEvent e) {
    if (e instanceof ContentChangeEvent) {
      setEnabled(!((ContentChangeEvent)e).isContentNull());
    }
  }

  /**
   * Sets the valueHolder for this button.  The valueHolder is used by
   * this button to control its enable state. This method removes this
   * button as a change listener from the current ValueHolder (if any) and
   * adds this as a change listener to the given ValueHolder.
   * @param the ValueHolder for this button
   */
  public void setValueHolder(ValueHolder newValueHolder) {
    if (valueHolder != null)
      valueHolder.removeChangeListener(this);
    valueHolder = newValueHolder;
    valueHolder.addChangeListener(this);
    setEnabled(isChangeable());
  }

  /**
   * Sets the valueHolder for this button.  The valueHolder is used by
   * this button to control its enable state. This method removes this
   * @return the ValueHolder for this button.
   */
  public com.taursys.model.ValueHolder getValueHolder() {
    return valueHolder;
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
