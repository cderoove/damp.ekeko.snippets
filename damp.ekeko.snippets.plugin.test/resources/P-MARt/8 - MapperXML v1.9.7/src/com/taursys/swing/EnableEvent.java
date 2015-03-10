/**
 * EnableEvent - Indicates whether or not a component should be enabled.
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

import java.util.*;

/**
 * An event which indicates whether or not a component should be enabled.
 * @author Marty Phelan
 * @version 2.0
 */
public class EnableEvent extends EventObject {
  private boolean enable;

  /**
   * Constructs a new EnableEvent for given source with indication of enable.
   */
  public EnableEvent(Object source, boolean enable) {
    super(source);
    this.enable = enable;
  }

  /**
   * Get flag indicating whether or not the component should be enabled.
   * @return flag indicating whether or not the component should be enabled.
   */
  public boolean isEnable() {
    return enable;
  }
}
