/**
 * ServletInputDispatcher - Component responsible for receiving and dispatching Servlet InputEvents
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
import com.taursys.xml.Parameter;
import com.taursys.xml.Component;
import com.taursys.xml.DocumentElement;
import com.taursys.xml.event.InputEvent;
import javax.servlet.ServletRequest;
import java.util.Iterator;
import com.taursys.debug.Debug;

/**
 * <p>Subcomponent responsible for receiving and dispatching servlet
 * <code>InputEvents</code>. Components should registered with this
 * <code>Dispatcher</code> via their parent <code>Container</code> to be
 * notified whenever their input arrives.
 * </p>
 * <p>This subcomponent will dispatch any input present in the
 * <code>ServletRequest</code> to registered Components.
 * </p>
 * <p>Only <code>Components</code> which subclass <code>Parameter</code> are
 * allowed to use this <code>Dispatcher</code>.</p>
 * @deprecated ServletForm now uses the inherited InputDispatcher. This class
 * is no longer used and will be removed in a future release.
 */
public class ServletInputDispatcher extends Dispatcher {
  private ServletRequest request;

  /**
   * Constructs a new dispatcher
   */
  public ServletInputDispatcher() {
  }

  /**
   * <p>Dispatches an <code>InputEvent</code> to the given component.
   * If the given <code>Component</code> is a <code>DocumentElement</code>,
   * an <code>InputEvent</code> with a null <code>value</code> and
   * <code>name</code> is dispatched. If the given <code>Component</code> is a
   * <code>Parameter</code> then normal dispatching occurs (see following).
   * If the given <code>Component</code> is neither of the above two types,
   * then no dispatching occurs.
   * </p>
   * <p>The components will only be dispatched its own input as specified by
   * its <code>parameter</code> property.  If the component's
   * <code>parameter</code> is present, then the event will contain that value.
   * If the <code>parameter</code> is NOT present, AND the component's
   * <code>defaultValue</code> is set, then the event will contain the
   * <code>defaultValue</code>.  If the <code>parameter</code> is NOT present
   * and <code>defaultValue</code> is NULL, no event will be dispatched.
   * </p>
   * @param request the ServletRequest containing the input parameters
   * @throws Exception from the components <code>dispatchEvent</code> method
   * if occurs.
   */
  public void dispatchToComponent(Component c) throws Exception {
    if (request != null) {
      if (c instanceof Parameter) {
        dispatchToParameter((Parameter)c);
      } else if (c instanceof DocumentElement) {
        c.dispatchEvent(new InputEvent(c, null, null));
      }
    } else {
      Debug.error(
          "ServletInputDispatcher.dispatchToComponent: request is null");
    }
  }

  private void dispatchToParameter(Parameter field) throws Exception {
    // Get input parameter name -- skip if blank or null
    String pname = field.getParameter();
    if (pname != null && pname.length()>0) {
      // fetch input value
      String value = request.getParameter(pname);
      // Get defaultValue if input value is null
      if (value == null)
        value = field.getDefaultValue();
      // Dispatch event if present (not null)
      if (value != null) {
        field.dispatchEvent(
            new InputEvent(field, field.getParameter(), value));
      }
    }
  }

  /**
   * Dispatches a <code>InputEvent</code> to each registered component.
   * Components will only be dispatched their own input as specified by
   * their <code>parameter</code> property.  If the component's
   * <code>parameter</code> is present, then the event will contain that value.
   * If the <code>parameter</code> is NOT present, AND the component's
   * <code>defaultValue</code> is set, then the event will contain the
   * <code>defaultValue</code>.  If the <code>parameter</code> is NOT present
   * and <code>defaultValue</code> is NULL, no event will be dispatched.
   * @param request the ServletRequest containing the input parameters
   * @throws Exception from the components <code>dispatchEvent</code> method
   * if occurs.
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
