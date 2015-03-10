/**
 * TemplateRenderer - Subcomponent which renders a Template by replication.
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
import com.taursys.xml.event.Dispatcher;
import com.taursys.xml.event.RenderEvent;
import com.taursys.xml.event.RenderDispatcher;
import com.taursys.xml.event.RenderException;
import com.taursys.xml.event.RecycleEvent;
import com.taursys.xml.event.RecycleDispatcher;
import com.taursys.xml.event.RecycleException;
import com.taursys.model.CollectionValueHolder;
import com.taursys.xml.Component;
import com.taursys.xml.Container;
import com.taursys.xml.DocumentComponent;
import com.taursys.xml.Template;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * TemplateRenderer is a subcomponent which renders a Template by replication.
 * @author Marty Phelan
 * @version 1.0
 */
public class TemplateRenderer extends DocumentElementRenderer {
  private Collection clones = new ArrayList();

  /**
   * Constructs a new TemplateRenderer
   */
  public TemplateRenderer(Template template) {
    super(template);
  }

  /**
   * Recycles this component by restoring the Document element to a default state.
   * This implementation simply makes it visible, and dispatches a recycle
   * event to any children.
   * <p>
   * If a Document element was left invisible, and the Document changed, then
   * the invisible component would become dereferenced.
   * @throws RecycleException if problem occurs during recycling.
   */
  public void recycle() throws RecycleException {
    super.recycle();
    removeClones();
  }

  /**
   * Renders the Template and its children to the Document.
   * This subcomponent uses replication as its rendering strategy.
   * It begins by removing any cloned node from the Document (from prior use).
   * If the Template isVisible, it begins by restoring itself back into the
   * Document (if removed by prior use) and then iterates over the items
   * in the collectionValue holder.  For each item, it invokes the Template's
   * renderDispatcher so child components will render their values.  It then
   * clones itself, and alters the ID's in the cloned branch by appending a row
   * number to the ID (example "__row3").  Finally, it removes itself from
   * the document.
   *
   * @throws RenderException if any problem occurs during rendering
   */
  public void render() throws RenderException {
    init();
    removeClones();
    if (getComponent().isVisible()) {
      restoreSelf();
      CollectionValueHolder collectionValueHolder =
          ((Template)getComponent()).getCollectionValueHolder();
      if (collectionValueHolder != null) {
        int i = 1;
        collectionValueHolder.reset();
        RenderDispatcher dispatcher = (RenderDispatcher)
            ((Container)getComponent()).getDispatcher(
                RenderEvent.class.getName());
        while (collectionValueHolder.hasNext()) {
          collectionValueHolder.next();
          dispatcher.dispatch();
          cloneSelf("__row" + i);
          i++;
        }
      } else {
        Debug.warn("Template with id=" + ((Template)getComponent()).getId()
            + " has a null CollectionValueHolder during rendering.");
      }
    }
    removeSelf();
  }

  /**
   * Remove all clones from parentNode and clear clones Collection.
   */
  protected void removeClones() {
    // Remove all cloned rows from parent
    Iterator i = clones.iterator();
    while(i.hasNext())
      getParentNode().removeChild((Node)i.next());
    // Clear out clones
    clones.clear();
  }

  /**
   * Populate children of this node and create a clone of this node.
   * Also invokes alterIDs with given appendValue to avoid duplicate ID's
   * (violation of XML specification).
   * @param appendValue to append to ID's to avoid duplicates during cloning
   */
  protected void cloneSelf(String appendValue) {
    // Clone the componentNode
    Node cloneNode = getComponentNode().cloneNode(true);
    // Remove all Id's from branch
    alterIDs((Element)cloneNode, appendValue);
    // Insert this new node BEFORE the this node (keeps original order)
    getParentNode().insertBefore(cloneNode, getComponentNode());
    // Save reference to cloned node for removal
    clones.add(cloneNode);
  }

  /**
   * Alter ID's for this node and its children recursively to avoid duplicates.
   * @param the beginning parent node for the operation.
   * @param the String to append to each nodes existing id
   */
  protected void alterIDs(Element parent, String appendValue) {
    // First do parent
    String id = parent.getAttribute("id");
    if (id != null && id.length() > 0) {
      id += appendValue;
      parent.setAttribute("id", id);
    }
    // Then do children
    Node child = parent.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        // recurse node
        alterIDs((Element)child, appendValue);
      }
      // next node
      child = child.getNextSibling();
    }
  }

  /**
   * Get the collection of cloned nodes which were generated by the render method.
   * @return the collection of cloned nodes which were generated by the render method.
   */
  public Collection getClones() {
    return clones;
  }
}
