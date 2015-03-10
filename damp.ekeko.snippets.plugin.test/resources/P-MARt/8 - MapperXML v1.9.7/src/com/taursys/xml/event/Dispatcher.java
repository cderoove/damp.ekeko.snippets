/**
 * Dispatcher - Component responsible for receiving and dispatching Events
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

import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.Iterator;
import java.util.EventObject;
import com.taursys.xml.Component;

/**
 * Component responsible for receiving and dispatching Events.
 * Components should register with this component to be notified whenever
 * their events occur.
 */
public abstract class Dispatcher {
  protected ArrayList components = new ArrayList();
  protected Map map;
  private int index;

  /**
   * Dispatches an <code>Event</code> to the given component. Concrete
   * Dispatchers must override this method to provide appropriate behavior.
   * @param c the <code>Component</code> to dispatch the event to.
   * @throws Exception from the components <code>dispatchEvent</code> method
   * if occurs.
   */
  public abstract void dispatchToComponent(Component c) throws Exception;

  /**
   * Dispatches an Event to each registered component with the given key/value
   * <code>Map</code>. This method invokes the <code>dispatchToComponent</code>
   * for each registered component.
   * @param map a Map containing message key/values for dispatching
   */
  public void dispatch(Map map) throws Exception {
    this.map = map;
    // iterate through each registered component and fetch
    Iterator iter = components.iterator();
    while (iter.hasNext()) {
      dispatchToComponent((Component)iter.next());
    }
  }

  /**
   * Dispatches an Event to each registered component with an empty key/value
   * map.  This method invokes the <code>dispatchToComponent</code>
   * for each registered component.
   */
  public void dispatch() throws Exception {
    dispatch(Collections.EMPTY_MAP);
  }

  /**
   * Dispatches the given Event to each registered component with an empty key/value
   * <code>Map</code>.
   * @param e the Event to dispatch
   */
  public void dispatch(EventObject e) throws Exception {
    this.map = Collections.EMPTY_MAP;
    // iterate through each registered component and fetch
    Iterator iter = components.iterator();
    while (iter.hasNext()) {
      ((Component)iter.next()).dispatchEvent(e);
    }
  }

  /**
   * Get the parameter value at the current index for the given key. If the key
   * is found then element at the current index in the String[] value is
   * returned, otherwise the given defaultValue is returned. Normally the
   * index will be zero unless the dispatcher is a multi-value type dispatcher.
   * @param key the String key for the parameter value
   * @param defaultValue the String to return if key is not present
   * @return the the first parameter value for the given key
   */
  protected String getParameter(String key, String defaultValue) {
    String[] values = (String[])map.get(key);
    return (values == null || index >= values.length)
        ? defaultValue : values[index];
  }

  /**
   * Adds a component to the notification list unless it is already registered
   */
  public void addNotify(Component c) {
    if (!components.contains(c))
      components.add(c);
  }

  /**
   * Removes a component from the notification list if it is registered
   */
  public void removeNotify(Component c) {
    if (components.contains(c))
      components.remove(c);
  }

  /**
   * Resets the parameter array index to zero. The index is used by the
   * getParameter method to determine which value from the map String
   * array will be retrieved.
   */
  protected void resetIndex() {
    index = 0;
  }

  /**
   * Increments the parameter array index by one. The index is used by the
   * getParameter method to determine which value from the map String
   * array will be retrieved.
   */
  protected void incrementIndex() {
    index++;
  }
}
