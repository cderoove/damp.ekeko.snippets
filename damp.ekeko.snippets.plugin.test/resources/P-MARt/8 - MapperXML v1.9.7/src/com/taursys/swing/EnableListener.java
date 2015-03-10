/**
 * EnableListener - A listener for whether or not a component should be enabled.
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
 * An EnableListener is a listener for whether or not a component should be enabled.
 * @author Marty Phelan
 * @version 2.0
 */
public interface EnableListener extends EventListener {

  /**
   * Invoked whenever an EnableChange event is generated.
   * The EnableChange event indicates whether or not a component should be enabled.
   * @param e EnableEvent indicating whether or not a control component be enabled.
   */
  public void enableChange(EnableEvent e);
}
