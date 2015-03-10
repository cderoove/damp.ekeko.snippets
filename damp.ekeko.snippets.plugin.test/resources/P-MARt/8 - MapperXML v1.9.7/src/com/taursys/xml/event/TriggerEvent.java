/**
 * TriggerEvent - Event descriptor used when specific input value is received.
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
 * Event descriptor used when specific trigger(key/value) is received.  It contains
 * information about the source of the event. If this event is used for a
 * single parameter, then the parameter name and the value received properties
 * are set and the Map property is null. If this event is used for propagation
 * of multiple parameters, then the map property is set and the name and value
 * properties are null.
 */
public class TriggerEvent extends EventObject {
  private String name;
  private String value;
  private Map map;

  /**
   * Constructs a new TriggerEvent with all the given properties.
   */
  public TriggerEvent(Object source, String name, String value) {
    super(source);
    this.name = name;
    this.value = value;
  }

  /**
   * Constructs a new TriggerEvent with all the given properties.
   */
  public TriggerEvent(Object source, Map map) {
    super(source);
    this.map = map;
  }

  /**
   * Returns the trigger key name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the trigger value
   */
  public String getValue() {
    return value;
  }

  /**
   * Get the Map of all Parameter keys and values.
   * @return the Map of all Parameter keys and values.
   */
  public Map getMap() {
    return map;
  }
}
