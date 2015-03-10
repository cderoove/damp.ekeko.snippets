/**
 * ContentChangeEvent - Indicates that the current contents of a value holder have changed.
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

import com.taursys.model.ValueHolder;
import javax.swing.event.ChangeEvent;

/**
 * ContentChangeEvent Indicates that the current contents of a value holder have changed.
 * Either it is a new value object in the holder, or many of the value object's
 * properties have changed.
 * @author Marty Phelan
 * @version 1.0
 */
public class ContentChangeEvent extends ChangeEvent {
  private boolean contentNull = false;

  /**
   * Constructs a new ContentChangeEvent for given source with indication of null state.
   */
  public ContentChangeEvent(ValueHolder source, boolean contentNull) {
    super(source);
    this.contentNull = contentNull;
  }

  /**
   * Constructs a new ContentChangeEvent for given source and indicates contents not null.
   */
  public ContentChangeEvent(ValueHolder source) {
    super(source);
  }

  /**
   * Get flag indicating whether or not the current contents of value holder is null.
   * @return flag indicating whether or not the current contents of value holder is null.
   */
  public boolean isContentNull() {
    return contentNull;
  }
}
