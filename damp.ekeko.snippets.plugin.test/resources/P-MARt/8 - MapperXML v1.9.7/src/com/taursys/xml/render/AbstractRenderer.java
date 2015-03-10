/**
 * AbstractRenderer - Base class for all Renderers
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

import com.taursys.debug.Debug;
import com.taursys.xml.Component;
import com.taursys.xml.Container;
import com.taursys.xml.DocumentComponent;
import com.taursys.xml.event.RenderException;
import com.taursys.dom.DocumentAdapter;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * AbstractRenderer is the base class for all Renderers.  It contains common
 * behavior for removing(hiding) and restoring(showing) the component.
 * @author Marty Phelan
 * @version 1.0
 */
public class AbstractRenderer {
  private Component component = null;
  private boolean removed = false;
  private Node componentNode;
  private Node parentNode;
  private Node siblingNode;
  private Document doc;

  /**
   * Constructs a new AbstractRenderer for the given Component
   * @param c Component which owns this renderer
   */
  public AbstractRenderer(Component c) {
    this.component = c;
  }

  /**
   * Initializes reference to parentNode and siblingNode.
   * Only acts if componentNode is null and document has not changed
   * @throws RenderException if parent, DocumentAdapter, or Document is null.
   */
  protected void init() throws RenderException {
    Container parent = component.getParent();
    if (parent == null)
      throw new RenderException(RenderException.REASON_PARENT_CONTAINER_NULL);
    DocumentAdapter da = parent.getDocumentAdapter();
    if (da == null)
      throw new RenderException(RenderException.REASON_DOCUMENT_IS_NULL);
    Document currentDoc = da.getDocument();
    if (currentDoc == null)
      throw new RenderException(RenderException.REASON_DOCUMENT_IS_NULL);
    if (doc != currentDoc) {
      doc = currentDoc;
      componentNode = null;
    }
    if (componentNode == null) {
      String id = ((DocumentComponent)component).getId();
      if (id != null) {
        componentNode = component.getParent().getDocumentAdapter().getElementById(
            ((DocumentComponent)component).getId());
        if (componentNode != null) {
          parentNode = componentNode.getParentNode();
          siblingNode = componentNode.getNextSibling();
        } else {
          Debug.warn("AbstractRenderer.init: Attempt to render a component which is not in document. ID="
            + ((DocumentComponent)component).getId());
        }
      } else {
          Debug.warn("AbstractRenderer.init: Attempt to render a component whose ID is null. Class="
            + component.getClass().getName());
      }
    }
  }

  /**
   * Remove self from parent/Document (usually done after all nodes populated).
   * Only acts if not already removed (removed=false) and parentNode and
   * componentNode are not null.  The method reset changes the removed flag
   * back to true.
   */
  protected void removeSelf() {
    if (!removed && parentNode != null && componentNode != null) {
      // remove this node from parent node
      parentNode.removeChild(componentNode);
      // Mark as removed
      removed = true;
    }
  }

  /**
   * Restore self to parent/Document.  Tries to restore to original position
   * which is BEFORE the sibling node, otherwise as the first child.
   * Only acts if already removed (removed=true)and parentNode and
   * componentNode are not null.  The method reset changes the removed flag
   * to false.
   */
  protected void restoreSelf() {
    if (removed && parentNode != null && componentNode != null) {
      // Add self back BEFORE sibling (keeps original order)
      parentNode.insertBefore(componentNode, siblingNode);
      // Reset removed flag
      removed = false;
    }
  }

  /**
   * Get the component node which will be replicated.
   * The init method must be invoked before using this method for the first time.
   * @return the component node which will be replicated.
   */
  protected Node getComponentNode() {
    return componentNode;
  }

  /**
   * Get the parent node for the component node.
   * The init method must be invoked before using this method for the first time.
   * @return the parent node for the component node
   */
  public Node getParentNode() {
    return parentNode;
  }

  /**
   * Get the sibling node for the component node or null if none.  This is the
   * sibling that follows the component node.
   * The init method must be invoked before using this method for the first time.
   * @return the sibling node for the component node or null if none
   */
  public Node getSiblingNode() {
    return siblingNode;
  }

  /**
   * Get the component for this renderer
   * @return the component for this renderer
   */
  public Component getComponent() {
    return component;
  }

  /**
   * Get the Document for this renderer
   * @return the Document for this renderer
   */
  public Document getDocument() {
    return doc;
  }

  /**
   * Set an indicator of whether or not the master component node has been removed.
   * @param b an indicator of whether or not the master component node has been removed.
   */
  public void setRemoved(boolean b) {
    removed = b;
  }

  /**
   * Get an indicator of whether or not the master component node has been removed.
   * @return an indicator of whether or not the master component node has been removed.
   */
  public boolean isRemoved() {
    return removed;
  }
}
