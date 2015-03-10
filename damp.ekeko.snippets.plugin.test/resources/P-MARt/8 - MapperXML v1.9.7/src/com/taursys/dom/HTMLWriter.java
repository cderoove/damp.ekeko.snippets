/**
 * HTMLWriter - Specialization of XMLWriter for writing HTML
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.DocumentType;
import java.util.Arrays;
import java.util.List;

/**
 * HTMLWriter is a specialization of XMLWriter for writing HTML Documents.
 * @author Marty Phelan
 * @version 1.0
 */
public class HTMLWriter extends com.taursys.dom.XMLWriter {
  private static final String[] SINGLE_TAGS = new String[] {
    "area",
    "base",
    "br",
    "col",
    "hr",
    "img",
    "input",
    "link",
    "meta",
    "param"
  };
  protected List tags = null;

  /**
   * Constructs a new HTMLWriter
   */
  public HTMLWriter() {
    tags = Arrays.asList(SINGLE_TAGS);
  }

  /**
   * Writes the given empty element node. If the given node is a type that
   * must be empty, then a single tag is written without an end tag.
   * It uses the <code>writeAttributes</code> method
   * to generate the attributes for the node. The following are the empty tag
   * types supported:
   * <ul>
   * <li>area</li>
   * <li>base</li>
   * <li>br</li>
   * <li>col</li>
   * <li>hr</li>
   * <li>img</li>
   * <li>input</li>
   * <li>link</li>
   * <li>meta</li>
   * <li>param</li>
   * </ul>
   * @param node the element node to write
   */
  protected void writeEmptyElementNode(Node node) {
    printWriter.print('<');
    printWriter.print(node.getNodeName());
    writeAttributes(node);
    if (tags.contains(node.getNodeName().toLowerCase())) {
        printWriter.print('>');
    } else {
      printWriter.print("></" + node.getNodeName() + '>');
    }
  }
}
