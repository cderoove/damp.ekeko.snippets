/**
 * ListContentChangeEvent - Indicates that the current contents of a list value holder have changed.
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
package com.taursys.model.event;

import com.taursys.model.ListValueHolder;
import javax.swing.event.ChangeEvent;

/**
 * ListContentChangeEvent Indicates that the current contents of a ListValueHolder have changed.
 * This event is fired when contents are added or removed from the list.
 * @author Marty Phelan
 * @version 1.0
 */
public class ListContentChangeEvent extends ContentChangeEvent {

  /**
   * Constructs a new ContentChangeEvent
   */
  public ListContentChangeEvent(ListValueHolder source, boolean contentNull) {
    super(source, contentNull);
  }
}
