/**
 * VisibleRenderer - Renderer which simply hides or shows a component.
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
package com.taursys.xml.render;

import com.taursys.xml.Component;
import com.taursys.xml.event.RenderException;

/**
 * VisibleRenderer is a Renderer which simply hides or shows a component.
 * @author Marty Phelan
 * @version 1.0
 */
public class VisibleRenderer extends AbstractRenderer {

  /**
   * Constructs a new VisibleRenderer for the given Component
   * @param c component which this renderer belongs to
   */
  public VisibleRenderer(Component c) {
    super(c);
  }

  /**
   * Renders the Component by simply adding or removing it from the document.
   * This is done based on the component's visible property.
   * @throws RenderException if any problem occurs during rendering
   */
  public void render() throws RenderException {
    init();
    if (getComponent().isVisible()) {
      restoreSelf();
    } else {
      removeSelf();
    }
  }
}
