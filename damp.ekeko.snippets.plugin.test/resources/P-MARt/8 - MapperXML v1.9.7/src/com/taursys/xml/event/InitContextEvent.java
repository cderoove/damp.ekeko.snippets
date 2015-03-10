/**
 * InitContextEvent - is a message for initializing the context of a component
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

import java.util.EventObject;
import java.util.Map;

/**
 * InitContextEvent is a message for initializing the context of a component.
 * @author Marty Phelan
 * @version 1.0
 */
public class InitContextEvent extends EventObject {
  private Map context;

  /**
   * Constructs a new InitContextEvent with the given source and context.
   * @param source the Object that originated this message
   * @param context a <code>Map</code> of key/values which make up the context.
   */
  public InitContextEvent(Object source, Map context) {
    super(source);
    this.context = context;
  }

  /**
   * Get the <code>Map</code> of key/values which make up the context.
   * @return the <code>Map</code> of key/values which make up the context.
   */
  public Map getContext() {
    return context;
  }
}
