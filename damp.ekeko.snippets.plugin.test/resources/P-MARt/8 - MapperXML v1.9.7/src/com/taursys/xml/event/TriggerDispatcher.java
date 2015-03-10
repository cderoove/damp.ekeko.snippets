/**
 * TriggerDispatcher - Dispatches TriggerEvents
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

import com.taursys.xml.Parameter;
import com.taursys.xml.Trigger;
import com.taursys.xml.Component;
import com.taursys.xml.Container;
import com.taursys.debug.Debug;

/**
 * TriggerDispatcher is Dispatcher of TriggerEvents
 * @author Marty Phelan
 * @version 1.0
 */
public class TriggerDispatcher extends Dispatcher {

  /**
   * Constructs a new TriggerDispatcher
   */
  public TriggerDispatcher() {
  }

  /**
   * <p>Dispatches a <code>TriggerEvent</code> to the given component.
   * If the given <code>Component</code> is a <code>Container</code>,
   * a <code>TriggerEvent</code> with the entire value map is dispatched.
   * If the given <code>Component</code> is a <code>Trigger</code> then
   * normal dispatching occurs (see following). If the given
   * <code>Component</code> is neither of the above two types, then no
   * dispatching occurs.
   * </p>
   * <p>The <code>Trigger</code> will only be dispatched its own input as
   * specified by its <code>parameter</code> property.  If the <code>Trigger</code>'s
   * <code>parameter</code> is present, then the event will contain that value.
   * If the <code>parameter</code> is NOT present, AND the <code>Trigger</code>'s
   * <code>isDefaultTrigger</code> is true, then the event will contain the
   * <code>getText</code> value.  If the <code>parameter</code> is NOT present
   * and <code>isDefaultTrigger</code> is false, no event will be dispatched.
   * </p>
   * @param c the <code>Component</code> to dispatch to
   * @throws Exception from the components <code>dispatchEvent</code> method
   * if occurs.
   */
  public void dispatchToComponent(Component c) throws Exception {
    if (map != null) {
      if (c instanceof Trigger) {
        dispatchToTrigger((Trigger)c);
      } else if (c instanceof Container) {
        c.dispatchEvent(new TriggerEvent(c, map));
      }
    } else {
      Debug.error(
          "TriggerDispatcher.dispatchToComponent: map is null");
    }
  }

  private void dispatchToTrigger(Trigger trigger) throws Exception {
    // Get input parameter name -- skip if blank or null
    String pname = trigger.getParameter();
    if (pname != null && pname.length()>0) {
      // fetch input value
      String value = getParameter(pname, null);
      // Dispatch if default trigger or value received
      if (value == null && trigger.isDefaultTrigger()) {
        value = trigger.getText();
        TriggerEvent e = new TriggerEvent(trigger, pname, value);
        trigger.dispatchEvent(e);
      } else if (value != null && value.equals(trigger.getText())) {
        TriggerEvent e = new TriggerEvent(trigger, pname, value);
        trigger.dispatchEvent(e);
      }
    }
  }
}
