/**
 * FormRenderer -
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

import com.taursys.xml.*;
import com.taursys.xml.event.*;
import com.taursys.dom.*;
import com.taursys.servlet.*;
import com.taursys.debug.Debug;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * FormRenderer is ...
 * @author Marty Phelan
 * @version 1.0
 */
public class FormRenderer extends DocumentElementRenderer {
  private Node sourceNode;
  private Document sourceDoc;

  /**
   * Constructs a new FormRenderer
   */
  public FormRenderer(Form de) {
    super(de);
  }

  /**
   * Initializes reference to parentNode and siblingNode.
   * Only acts if componentNode is null and document has not changed
   * @throws RenderException if parent, DocumentAdapter, or Document is null.
   */
  protected void init() throws RenderException {
    super.init();
    Form form = ((Form)getComponent());
    DocumentAdapter da = form.getDocumentAdapter();
    if (da == null)
      throw new RenderException(RenderException.REASON_DOCUMENT_IS_NULL);
    Document d = da.getDocument();
    if (d == null)
      throw new RenderException(RenderException.REASON_DOCUMENT_IS_NULL);
    if (sourceDoc != d) {
      sourceDoc = d;
      sourceNode = null;
    }
    if (sourceNode == null) {
      String id = form.getSourceId();
      if (id != null) {
        sourceNode = da.getElementById(id);
        if (sourceNode == null) {
          Debug.warn("SubFormRenderer.init: Attempt to render a component which is not in document. ID="
            + id);
        }
      } else {
          Debug.warn("SubFormRenderer.init: Attempt to render a component whose ID is null. Class="
            + form.getClass().getName());
      }
    }
  }

  /**
   * Renders the DocumentElement by showing or hiding it.  If it is visible,
   * it invokes the DocumentElement's renderDispatcher to dispatch to the
   * children. It then copies the nested document to this document.
   * @throws RenderException if any problem occurs during rendering
   */
  public void render() throws RenderException {
    init();

    if (getComponent().isVisible()) {

      restoreSelf();

      Form form = ((Form)getComponent());

      // Dispatch a render message to this container's children
      RenderDispatcher d = (RenderDispatcher)
          form.getDispatcher(RenderEvent.class.getName());
      d.dispatch();

      // Copy section of Form's document to parent's document
      // The init() method has obtained componentNode
      if (sourceNode != null && getComponentNode() != null) {
        importContents(sourceNode, getComponentNode());
      }

    } else {
      removeSelf();
    }
  }

  /**
   * Import the contents of the source node recursivly and replace the
   * contents of the destination with it. The contents (children) of the
   * destination node are first removed. Next the source node is imported
   * into this document using a deep-copy. Finally the newly imported node
   * is appended to the destination node.
   * @source the source node (from another document)
   * @destination the destination node which will be appended to
   */
  private void importContents(Node source, Node destination) {
    // remove all children from destId
    Node child;
    while ((child = destination.getFirstChild()) != null) {
      destination.removeChild(child);
    }
    // recusrively copy from source
    destination.appendChild(getDocument().importNode(source, true));
  }
}
