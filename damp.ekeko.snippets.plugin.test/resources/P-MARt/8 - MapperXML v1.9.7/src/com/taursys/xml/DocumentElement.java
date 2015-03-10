/**
 * DocumentElement - is a simple container element for an XML document.
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

import com.taursys.xml.event.RenderEvent;
import com.taursys.xml.event.RenderDispatcher;
import com.taursys.xml.event.RenderException;
import com.taursys.xml.render.DocumentElementRenderer;
import com.taursys.xml.event.RecycleEvent;
import com.taursys.xml.event.RecycleDispatcher;
import com.taursys.xml.event.RecycleException;

/**
 * <p><code>DocumentElement</code> is a simple container element for an XML
 * document. It functions as an intermediate <code>Container</code> which has
 * its own set of <code>Dispatchers</code> for its children. It responds to
 * <code>ParameterEvents</code>, <code>InputEvents</code> and
 * <code>TriggerEvents</code> by simply propagating the event to its children.
 * </p>
 * <p>In response to a <code>RenderEvent</code>, it simply shows or hides the
 * Document Element and, if visible, dispatches a <code>RenderEvent</code> to
 * the children of the <code>DocumentElement</code>.
 * </p>
 * <p>In response to a <code>RecycleEvent</code>, it simply makes the Document
 * Element visible, and dispatches a <code>RecycleEvent</code> to
 * the children of the <code>DocumentElement</code>.
 * </p>
 * @author Marty Phelan
 * @version 1.0
 */
public class DocumentElement extends DispatchingContainer implements DocumentComponent {
  private String id;
  private DocumentElementRenderer renderer;

  // =======================================================================
  //                    Constructors and Subcomponents
  // =======================================================================

  /**
   * Constructs a new DocumentElement
   */
  public DocumentElement() {
    renderer = createDefaultRenderer();
  }

  /**
   * Creates the default DocumentElementRenderer for this component.
   * By Default this methos returns a new DocumentElementRenderer.
   * Override this method to define your own DocumentElementRenderer.
   */
  protected DocumentElementRenderer createDefaultRenderer() {
    return new DocumentElementRenderer(this);
  }

  /**
   * Get the Renderer for this component.
   * @return the Renderer for this component.
   */
  public DocumentElementRenderer getRenderer() {
    return renderer;
  }

  // =======================================================================
  //                          Property Accessors
  // =======================================================================

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

  // =======================================================================
  //                       Event Handling Methods
  // =======================================================================

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

  /**
   * Processes a RecycleEvent recycling self and children, and then by
   * propagating event to listeners.
   */
  public void processRecycleEvent(RecycleEvent e) throws RecycleException {
    renderer.recycle();
    fireRecycle(e);
  }

}
