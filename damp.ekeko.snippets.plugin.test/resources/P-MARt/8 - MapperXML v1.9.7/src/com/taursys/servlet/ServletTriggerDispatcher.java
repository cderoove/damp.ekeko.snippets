/**
 * ServletTriggerDispatcher - Component responsible for receiving and dispatching Servlet TriggerEvents
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
package com.taursys.servlet;

import com.taursys.xml.event.Dispatcher;
import com.taursys.xml.Trigger;
import com.taursys.xml.Component;
import com.taursys.xml.DocumentElement;
import com.taursys.xml.event.TriggerEvent;
import javax.servlet.ServletRequest;
import java.util.Iterator;
import com.taursys.debug.Debug;

/**
 * Component responsible for receiving and dispatching Servlet TriggerEvents.
 * Triggers should be registered with this component by their parent container
 * to be notified whenever their trigger value arrives.
 * <p>
 * This component will dispatch any matching trigger values present in the ServletRequest
 * to registered Triggers.  The dispatch method provides this behavior.
 * @deprecated ServletForm now uses the inherited TriggerDispatcher. This class
 * is no longer used and will be removed in a future release.
 */
public class ServletTriggerDispatcher extends Dispatcher {
  private ServletRequest request;

  /**
   * Constructs a new dispatcher
   */
  public ServletTriggerDispatcher() {
  }

  /**
   * <p>Dispatches an <code>TriggerEvent</code> to the given component.
   * If the given <code>Component</code> is a <code>DocumentElement</code>,
   * an <code>TriggerEvent</code> with a null <code>value</code> and
   * <code>name</code> is dispatched. If the given <code>Component</code> is a
   * <code>Trigger</code> then normal dispatching occurs (see following).
   * If the given <code>Component</code> is neither of the above two types,
   * then no dispatching occurs.
   * </p>
   * <p>The <code>Trigger</code> will only be dispatched its own input as
   * specified by its <code>parameter</code> property.  If the <code>Trigger</code>'s
   * <code>parameter</code> is present, then the event will contain that value.
   * If the <code>parameter</code> is NOT present, AND the <code>Trigger</code>'s
   * <code>isDefaultTrigger</code> is true, then the event will contain the
   * <code>getText</code> value.  If the <code>parameter</code> is NOT present
   * and <code>isDefaultTrigger</code> is false, no event will be dispatched.
   * </p>
   * @param request the ServletRequest containing the input parameters
   * @throws Exception from the components <code>dispatchEvent</code> method
   * if occurs.
   */
  public void dispatchToComponent(Component c) throws Exception {
    if (request != null) {
      if (c instanceof Trigger) {
        dispatchToTrigger((Trigger)c);
      } else if (c instanceof DocumentElement) {
        c.dispatchEvent(new TriggerEvent(c, null, null));
      }
    } else {
      Debug.error(
          "ServletTriggerDispatcher.dispatchToComponent: request is null");
    }
  }

  private void dispatchToTrigger(Trigger button) throws Exception {
    // Get input parameter name -- skip if blank or null
    String pname = button.getParameter();
    if (pname != null && pname.length()>0) {
      // fetch input value
      String value = request.getParameter(pname);
      // Dispatch if default trigger or value received
      if (value == null && button.isDefaultTrigger()) {
        value = button.getText();
        TriggerEvent e = new TriggerEvent(button, button.getParameter(), value);
        button.dispatchEvent(e);
      } else if (value != null && value.equals(button.getText())) {
        TriggerEvent e = new TriggerEvent(button, button.getParameter(), value);
        button.dispatchEvent(e);
      }
    }
  }

  /**
   * Dispatches a TriggerEvent to each registered component if a matching input value is present in request.
   * Triggers will only be dispatched an event if their specified name/value pair
   * if present in the ServletRequest.
   */
  public void dispatch(ServletRequest request) throws Exception {
    this.request = request;
    // iterate through each registered component and fetch
    Iterator iter = components.iterator();
    while (iter.hasNext()) {
      dispatchToComponent((Component)iter.next());
    }
  }
}
