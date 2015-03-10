/**
 * ComponentFactory - Factory which creates XMLComponents from an XML Doc.
 *
 * Copyright (c) 2000, 2001, 2002
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

import java.util.*;
import org.w3c.dom.*;
import com.taursys.model.ValueHolder;
import javax.swing.tree.*;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import com.taursys.xml.*;
import com.taursys.dom.DOM_1_20000929_DocumentAdapter;
import com.taursys.debug.Debug;

/**
 * Abstract class used to automate the creation of Components based on
 * the XML Document and its Elements. It determines the Component type based
 * on its element tag and "id" attribute). Concrete subclasses must override
 * the <code>initTagTable</code> method and populate the tagTable with
 * suggested components for the elements in the XML document.
 * <p>
 * This class contains two primary methods:
 * <ul>
 * <li><code>getSuggestedComponents</code> - for use by design tools.</li>
 * <li><code>createComponents</code> - to automatically create and bind
 * <code>Components</code> at runtime.</li>
 * </ul>
 */
public abstract class ComponentFactory {
  public static final String ID_DELIMITER = "__";
  public static final String TEMPLATE_NODE = "TEMPLATE_NODE";
  protected Hashtable tagTable = new Hashtable();

  // ***********************************************************************
  // *                    CONSTRUCTORS AND INITIALIZERS
  // ***********************************************************************

  /**
   * Default constructor which initializes tag table by calling initTagTable
   */
  public ComponentFactory() {
    initTagTable();
  }

  // ***********************************************************************
  // *                        GENERAL METHODS
  // ***********************************************************************

  /**
   * Returns a Vector of suggested Component class names for given Element.
   * This method will choose the appropriate Components based on the type of
   * Element given.  The default Component type will be the first in the list.
   * <p>
   * If the Element has an ID, then the DocumentElement Component will be added
   * to the end of the suggestion list.
   * <p>
   * If the id contains the TEMPLATE_NODE keyword, then a Template will be added
   * to the top of the suggestion list.
   * <p>
   * If there are no suggested types of Component for the given Element, then
   * an empty Vector will be returned.
   * <p>
   * Subclasses should override this method if more than the Element tag name
   * is needed to determine the suggested components.
   * @param element to return default Component type for
   * @return a Vector containing any suggested Component class names
   */
  public abstract Vector getSuggestedComponents(Element element);

  /**
   * <p>Creates components based on document, set their properties (including
   * valueHolder) and adds them to the container. As it moves through the
   * document, it first checks to see if the component is already in the
   * container (by matching id's). If it is, the existing component is moved
   * to the proper place in the heirarchy. Otherwise it will create a component.
   * </p>
   * <p>This method builds a component heirarchy which matches the document order
   * and heirarchy. If any newly created component is itself a Container type,
   * then all children of that component are added to it rather than its parent
   * container.
   * </p>
   * <p>Only bound components which are bound to one of the given value holders
   * are created.</p>
   * @param container the Container to add components to
   * @param holders an array of valueholders.
   */
  public void createComponents(Container container, ValueHolder[] holders) {
    // invoke private method to recursively create components
    createComponents(
        container.getDocumentAdapter().getDocument(),
        container,
        holders,
        container);
  }

  // ***********************************************************************
  // *                       PROTECTED AND PRIVATE METHODS
  // ***********************************************************************

  /**
   * Initialize values in the tag table
   */
  protected abstract void initTagTable();

  /**
   * Returns a Vector of suggested Component class names for given Element.
   * This method will choose the appropriate Components based on the type of
   * tag given.  The default Component type will be the first in the list.
   * <p>
   * If an ID is given, then the DocumentElement Component will be added to the
   * end of the suggestion list.
   * <p>
   * If the id contains the TEMPLATE_NODE keyword, then a Template will be added
   * to the top of the suggestion list.
   * <p>
   * If an ID is given and the id does not contain the TEMPLATE_NODE keyword,
   * but the given element has child nodes, then a Template will be added before
   * the DocumentElement suggestion.
   * <p>
   * If there are no suggested types of Component for the given tag, then
   * an empty Vector will be returned.
   * @param tagName to return default Component type for
   * @param id of the tag or null
   * @param element the Element to get suggestions for
   * @return a Vector containing any suggested Component class names
   */
  protected Vector getSuggestedComponents(
      String tagName, String id, Element element) {
    Vector suggestions = (Vector)tagTable.get(tagName);
    if (suggestions == null)
      suggestions = new Vector();
    if (id != null && id.length() > 0) {
      // add Template at beginning or end of suggestions
      if (id.indexOf(TEMPLATE_NODE) > -1) {
        suggestions.add(0,Template.class.getName());
      } else {
        if (DOM_1_20000929_DocumentAdapter.hasChildElements(element))
          suggestions.add(Template.class.getName());
      }
      suggestions.add(DocumentElement.class.getName());
    }
    return suggestions;
  }

  /**
   * Create a component for given element and set its properties.
   * @param id the id of the Element to create the Component for.
   * @param element the Element to create the Component for.
   * @param holders the array of ValueHolders for binding
   */
  protected abstract Component createComponentForElement(
      String id, Element element, ValueHolder[] holders);


  /**
   * <p>Creates components based on document, set their properties (including
   * valueHolder) and adds them to the container. As it moves through the
   * document, it first checks to see if the component is already in the
   * container (by matching id's). If it is, the existing component is moved
   * to the proper place in the heirarchy. Otherwise it will create a component.
   * </p>
   * <p>This method builds a component heirarchy which matches the document order
   * and heirarchy. If any newly created component is itself a Container type,
   * then all children of that component are added to it rather than its parent
   * container.
   * </p>
   * <p>Only bound components which are bound to one of the given value holders
   * are created.</p>
   * @param parentNode the parent node of the children to process and recurse
   * @param parentContainer the current Container to add components to
   * @param holders an array of valueholders.
   * @param rootContainer the top Container which may contain existing
   *    Components
   */
  private void createComponents(Node parentNode, Container parentContainer,
      ValueHolder[] holders, Container rootContainer) {
    Node childNode = parentNode.getFirstChild();
    Component childComponent = null;
    while (childNode != null) {
      if (childNode.getNodeType() == Node.ELEMENT_NODE) {
        String id = ((Element)childNode).getAttribute("id");
        if (id != null && id.length() > 0) {
          // See if already exists
          childComponent = (Component)rootContainer.get(id);
          if (childComponent != null) {
            // remove from existing position
            rootContainer.remove(childComponent);
            traceCreateComponents("Element with existing component - moving component. id=" + id + " element=" + childNode);
          } else {
            // create component
            childComponent = createComponentForElement(
                id, (Element)childNode, holders);
            traceCreateComponents("Creating new component for element. id=" + id
                + " element=" + childNode + " componentType=" + childComponent);
          }
          // Add component to parent (if not null)
          if (childComponent != null)
            parentContainer.add(childComponent);
        } else {
          childComponent = null;
          traceCreateComponents("Skipped element with no id. Element=" + childNode);
        }
        if (childComponent instanceof Container)
          createComponents(
              childNode, (Container)childComponent, holders, rootContainer);
        else
          createComponents(childNode, parentContainer, holders, rootContainer);
      }
      childNode = childNode.getNextSibling();
    }
  }

  private void traceCreateComponents(String msg) {
    Debug.debug("ComponentFactory.createComponents: " + msg);
  }
}
