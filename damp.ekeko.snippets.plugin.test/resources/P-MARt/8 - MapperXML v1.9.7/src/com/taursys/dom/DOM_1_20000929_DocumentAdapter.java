/**
 * DOM_1_20000929_DocumentAdapter - DocumentAdapter for DOM version 1 09/29/2000
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
package com.taursys.dom;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Text;
import java.util.Hashtable;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import java.io.OutputStream;
import java.io.Writer;

/**
 * DocumentAdapter for DOM version 1 09/29/2000
 */
public class DOM_1_20000929_DocumentAdapter implements DocumentAdapter {
  private Document doc;
  private Hashtable identifierMap;
  private String defaultIdentifier = "id";
  private AbstractWriter xmlWriter;

  /**
   * Constructs a new DOM_1_20000929_DocumentAdapter for given document.
   * Also maps all the identifiers for this document
   */
  public DOM_1_20000929_DocumentAdapter(Document newDoc) {
    setDocument(newDoc);
    xmlWriter = createDefaultWriter();
  }

  /**
   * Write the document to the given OutputStream.
   * @param stream to render the document to
   */
  public void write(OutputStream stream) {
    xmlWriter.write(doc, stream);
  }

  /**
   * Write the document to the given Writer.
   * @param writer to render the document to
   */
  public void write(Writer writer) {
    xmlWriter.write(doc, writer);
  }

  /**
   * Returns the Element for the given mapped identifier else null if not found.
   */
  public Element getElementById(String elementId) {
    if (elementId != null)
      return (Element)identifierMap.get(elementId);
    else
      return null;
  }

  /**
   * Sets the Text of the Element indicated by the given id to the given value.
   * Does nothing if the id is not found.  If the given value is null, it renders the
   * attribute as blank ("").
   */
  public void setElementText(String elementId, String value) {
    setElementText(getElementById(elementId), value);
  }

  /**
   * Stores given value as a text attribute of the element identified by the given id
   * Does nothing if the id is not found.  If the given value is null, it renders the
   * attribute as blank ("").
   */
  public void setAttributeText(String elementId, String attribute, String value) {
    Element element = getElementById(elementId);
    if (element != null) {
      if (value == null)
        value = "";
      element.setAttribute(attribute, value);
    }
  }

  /**
   * Returns the Document/adaptee of this DocumentAdapter
   */
  public Document getDocument() {
    return doc;
  }

  /**
   * Sets the Document/adaptee for this DocumentAdapter.
   * Also maps the identifiers for this document.
   */
  public void setDocument(Document newDoc) {
    doc = newDoc;
    identifierMap = new Hashtable();
    mapIdentifiers(doc);
  }

  /**
   * Recursively maps all the identifiers for the given document starting at the given node
   */
  protected void mapIdentifiers(Node parent) {
    Node child = parent.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        String id = ((Element)child).getAttribute(defaultIdentifier);
        if (id != null && id.length() > 0) {
          identifierMap.put(id, (Element)child);
        }
        mapIdentifiers(child);
      }
      child = child.getNextSibling();
    }
  }

  /**
   * Extracts the named attribute value from the node.  Returns value or null.
   */
  public static String getAttribute(String attributeName, Node node) {
    // Get all attributes for node
    NamedNodeMap attribs = node.getAttributes();
    // Continue only if there are attributes
    if (attribs != null) {
      // Try to extract ID attribute
      Node idNode = attribs.getNamedItem(attributeName);
      if (idNode != null)
        return idNode.getNodeValue();
    }
    return null;
  }

  /**
   * Stores the given value as a text node of the given element.
   * If the element already has a text node, the value is stored in
   * that node, otherwise a new text node is created with the given
   * value and attached to the element.
   */
  public static void setElementText(Element element, String value) {
    if (element != null) {
      // Set value to blank if null -- avoids DOM exception
      if (value == null)
        value = "";
      // Set text node
      Text textNode = findFirstTextNode(element);
      if (textNode != null) {
        textNode.setData(value);
      } else {
        textNode = element.getOwnerDocument().createTextNode(value);
        element.appendChild(textNode);
      }
    }
  }

  /**
   * Get the text from the given Element's text node.
   * @param element the element to get the text from
   * @return the text from the given Element's text node or null.
   */
  public static String getElementText(Element element) {
    if (element != null) {
      Text textNode = findFirstTextNode(element);
      if (textNode != null) {
        return textNode.getData();
      }
    }
    return null;
  }

  /**
   * Returns first TEXT_NODE under given element else returns null
   */
  public static org.w3c.dom.Text findFirstTextNode(Element ele) {
    Node child = ele.getFirstChild();
    while (child != null) {
      // If the child is an element node
      if (child.getNodeType() == Node.TEXT_NODE) {
        return (org.w3c.dom.Text)child;
      }
      child = child.getNextSibling();
    }
    return null;
  }

  /**
   * Indicates whether or not the given Element has child Elements.
   * @param the element to examine for the presence of child elements
   * @return true if has children else false
   */
  public static boolean hasChildElements(Element parent) {
    Node child = parent.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == Node.ELEMENT_NODE)
        return true;
      child = child.getNextSibling();
    }
    return false;
  }

  /**
   * Creates the default AbstractWriter for this subcomponent.
   * This implementation creates a com.taursys.dom HTMLWriter if the
   * current Document is an HTML document, otherwise it creates a
   * com.taursys.dom.XMLWriter.
   * You can override this method to create your own default
   * AbstractWriter.
   */
  protected AbstractWriter createDefaultWriter() {
    if (doc.getDocumentElement().getNodeName().toLowerCase().equals("html")) {
      return new HTMLWriter();
    } else {
      return new XMLWriter();
    }
  }

  /**
   * Set the AbstractWriter used to render this document to a stream or writer.
   * The default is XMLWriter.
   * @param newXMLWriter the AbstractWriter used to render this document
   */
  public void setXMLWriter(AbstractWriter newXMLWriter) {
    xmlWriter = newXMLWriter;
  }

  /**
   * Get the AbstractWriter used to render this document to a stream or writer.
   * The default is XMLWriter.
   * @return the AbstractWriter used to render this document
   */
  public AbstractWriter getXMLWriter() {
    return xmlWriter;
  }
}
