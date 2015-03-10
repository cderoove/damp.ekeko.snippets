/**
 * CloseFormDispatcher - is a Dispatcher for CloseFormEvents
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
package com.taursys.xml.event;

import com.taursys.xml.event.*;
import com.taursys.xml.*;

/**
 * CloseFormDispatcher is a Dispatcher for CloseFormEvents
 * @author Marty Phelan
 * @version 1.0
 */
public class CloseFormDispatcher extends Dispatcher {
  private Component source;

  /**
   * Constructs a new CloseFormDispatcher
   */
  public CloseFormDispatcher(Component source) {
    this.source = source;
  }

  /**
   * <p>Dispatches a <code>CloseFormEvent</code> to the given component.
   * </p>
   * @param c the <code>Component</code> to dispatch to
   * @throws Exception from the components <code>dispatchEvent</code> method
   * if occurs.
   */
  public void dispatchToComponent(Component c) throws Exception {
    c.dispatchEvent(new CloseFormEvent(source));
  }

}
