/**
 * RecycleDispatcher - Subcomponent responsible for receiving and dispatching RecycleEvents
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

import java.util.Iterator;
import com.taursys.xml.Container;
import com.taursys.xml.Component;

/**
 * RecycleDispatcher is a subcomponent responsible for receiving and
 * dispatching RecycleEvents.
 * Components should register with this component to be notified whenever
 * a recycle event occurs.
 * @author Marty Phelan
 * @version 1.0
 */
public class RecycleDispatcher extends Dispatcher {
  private RecycleEvent recycleEvent;

  /**
   * Constructs a new RecycleDispatcher with given Container as parent.
   */
  public RecycleDispatcher(Container c) {
    recycleEvent = new RecycleEvent(c);
  }

  /**
   * Dispatches a <code>ParameterEvent</code> to the given component.
   * The components will only be dispatched its own input as specified by
   * its <code>parameter</code> property.  If the component's
   * <code>parameter</code> is present, then the event will contain that value.
   * If the <code>parameter</code> is NOT present, AND the component's
   * <code>defaultValue</code> is set, then the event will contain the
   * <code>defaultValue</code>.  If the <code>parameter</code> is NOT present
   * and <code>defaultValue</code> is NULL, no event will be dispatched.
   * @param request the ServletRequest containing the input parameters
   * @throws Exception from the components <code>dispatchEvent</code> method
   * if occurs.
   */
  public void dispatchToComponent(Component c) throws Exception {
    c.processRecycleEvent(recycleEvent);
  }

  /**
   * Dispatches a RecycleEvent to all registered components.
   */
  public void dispatch() throws RecycleException {
    Iterator iter = components.iterator();
    while (iter.hasNext()) {
      try {
        dispatchToComponent((Component)iter.next());
      } catch (RecycleException ex) {
        throw ex;
      } catch (Exception ex) {
        // this should never be possible
        throw new RecycleException(RecycleException.REASON_UNSPECIFIED, ex);
      }
    }
  }
}
