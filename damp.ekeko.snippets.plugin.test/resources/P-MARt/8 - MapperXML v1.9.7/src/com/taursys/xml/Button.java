/**
 * Button - A visible Trigger
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
package com.taursys.xml;

import com.taursys.xml.event.Dispatcher;
import com.taursys.xml.event.RenderEvent;
import com.taursys.xml.event.RenderDispatcher;
import com.taursys.xml.event.RenderException;
import com.taursys.xml.render.AbstractRenderer;
import com.taursys.xml.render.VisibleRenderer;

/**
 * Button is a visible Trigger
 * @author Marty Phelan
 * @version 1.0
 */
public class Button extends Trigger implements DocumentComponent {
  private String id;
  private VisibleRenderer renderer;

  /**
   * Constructs a new Button
   */
  public Button() {
    super();
    addEventType(RenderEvent.class.getName());
    renderer = createDefaultRenderer();
  }

  /**
   * Returns the id of the node this component is bound to.  This is the node
   * which this component will replicate.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id of the node this component is bound to.  This is the node
   * which this component will replicate.
   */
  public void setId(String newId) {
    id = newId;
  }

  /**
   * Creates the default TextFieldRenderer for this component.
   * By Default this methos returns a new TextFieldRenderer.
   * Override this method to define your own TextFieldRenderer.
   */
  protected VisibleRenderer createDefaultRenderer() {
    return new VisibleRenderer(this);
  }

  /**
   * Get the Renderer for this component.
   * @return the Renderer for this component.
   */
  public VisibleRenderer getRenderer() {
    return renderer;
  }

  /**
   * Responds to a render event for this component.  This uses the renderer
   * subcomponent to actually render the value. It first notifies any
   * RenderListeners of the event. It then invokes the renderer subcomponent
   * to render the value to the document.
   * @param e the current render event message
   * @throws RenderException if problem rendering value to document
   */
  public void processRenderEvent(RenderEvent e) throws RenderException {
    fireRender(e);
    renderer.render();
  }

}
