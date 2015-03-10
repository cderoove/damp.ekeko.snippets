/**
 * ParameterDispatcher - Dispatcher of ParameterEvents
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
import com.taursys.xml.Component;
import com.taursys.xml.Container;
import com.taursys.debug.Debug;

/**
 * ParameterDispatcher is a Dispatcher for ParameterEvents.
 * @author Marty Phelan
 * @version 1.0
 */
public class ParameterDispatcher extends Dispatcher {

  /**
   * Constructs a new ParameterDispatcher
   */
  public ParameterDispatcher() {
  }

  /**
   * <p>Dispatches a <code>ParameterEvent</code> to the given component.
   * If the given <code>Component</code> is a <code>Container</code>,
   * a <code>ParameterEvent</code> with the entire value map is dispatched.
   * If the given <code>Component</code> is a <code>Parameter</code> then
   * normal dispatching occurs (see following). If the given
   * <code>Component</code> is neither of the above two types, then no
   * dispatching occurs.
   * </p>
   * <p>A components will only be dispatched its own input as specified by
   * its <code>parameter</code> property.  If the component's
   * <code>parameter</code> is present, then the event will contain that value.
   * If the <code>parameter</code> is NOT present, AND the component's
   * <code>defaultValue</code> is set, then the event will contain the
   * <code>defaultValue</code>.  If the <code>parameter</code> is NOT present
   * and <code>defaultValue</code> is NULL, no event will be dispatched.
   * </p>
   * @param c the <code>Component</code> to dispatch to
   * @throws Exception from the components <code>dispatchEvent</code> method
   * if occurs.
   */
  public void dispatchToComponent(Component c) throws Exception {
    if (map != null) {
      if (c instanceof Parameter) {
        dispatchToParameter((Parameter)c);
      } else if (c instanceof Container) {
        c.dispatchEvent(new ParameterEvent(c, map));
      }
    } else {
      Debug.error(
          "ParameterDispatcher.dispatchToComponent: map is null");
    }
  }

  private void dispatchToParameter(Parameter field) throws Exception {
    // Get input parameter name -- skip if blank or null
    String pname = field.getParameter();
    if (pname != null && pname.length()>0) {
      // fetch input value
      String value = getParameter(pname, field.getDefaultValue());
      // Dispatch event if present (not null)
      if (value != null) {
        field.dispatchEvent(
            new ParameterEvent(field, field.getParameter(), value));
      }
    }
  }

}
