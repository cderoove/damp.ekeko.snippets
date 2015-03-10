/**
 * XMLWriter - Class to write a given Document to an OutputStream or Writer.
 *
 * Copyright (c) 2001
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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.PrintWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.DocumentType;

/**
 * This class is used to write a given Document to an OutputStream or Writer.
 */
public class XMLWriter extends AbstractWriter {
  protected PrintWriter printWriter;

  /**
   * Default constructor.
   */
  public XMLWriter() {
  }

  /**
   * Writes the specified Document to the OutputStream.
   * @param doc the <code>Document</code> to write
   * @param stream the <code>OutputStream</code> to write to
   */
  public void write(Document doc, OutputStream stream) {
    Writer writer = new OutputStreamWriter(stream);
    printWriter = new PrintWriter(writer);
    write(doc);
  }

  /**
   * Writes the specified Document to the Writer.
   * @param doc the <code>Document</code> to write
   * @param writer the <code>Writer</code> to write to
   */
  public void write(Document doc, Writer writer) {
    printWriter = writer instanceof PrintWriter
      ? (PrintWriter)writer : new PrintWriter(writer);
    write(doc);
  }

  // *******************************************************
  //                    Protected Methods
  // *******************************************************

  /**
   * Write the specified node, recursively. This implementation will
   * write the following type <code>Nodes</code>:
   * <ul>
   * <li>DOCUMENT_NODE - If the <code>Document</code> <code>version</code>
   * or <code>encoding</code> properties are set, then it will produce an
   * initial XML processing instructions node. If the <code>docType</code>
   * property is not null, it will produce a DOCTYPE node.</li>
   * <li>ELEMENT_NODE - invokes the <code>writeElementNode</code> to write the
   * given element node.</li>
   * <li>COMMENT_NODE - writes comment node.</li>
   * <li>ENTITY_REFERENCE_NODE - writes entity node.</li>
   * <li>CDATA_SECTION_NODE - writes CDATA node.</li>
   * <li>TEXT_NODE - invokes <code>normalizeAndPrint</code> to write the
   * contents of a text node.</li>
   * <li>PROCESSING_INSTRUCTION_NODE - writes processing node.</li>
   * </ul>
   * @param node the starting node to write and recurse.
   */
  protected void write(Node node) {

    // is there anything to do?
    if (node == null) {
        return;
    }

    short type = node.getNodeType();
    switch (type) {
      case Node.DOCUMENT_NODE: {
        Document document = (Document)node;
        // Print DOCTYPE if defined
        if (document.getDoctype() != null) {
          DocumentType docType = document.getDoctype();
          printWriter.print("<!DOCTYPE");
          printWriter.print(" " + docType.getName());
          printWriter.print(" PUBLIC \"" + docType.getPublicId() + "\"");
          printWriter.print(" \"" + docType.getSystemId() + "\"");
          printWriter.println(">");
          printWriter.flush();
        }
        write(document.getDocumentElement());
        break;
      }

      case Node.ELEMENT_NODE: {
        writeElementNode(node);
        break;
      }

      case Node.COMMENT_NODE: {
        printWriter.print("<!--");
        String data = node.getNodeValue();
        if (data != null && data.length() > 0) {
          printWriter.print(' ');
          printWriter.print(data);
        }
        printWriter.println("-->");
        printWriter.flush();
        break;
      }

      case Node.ENTITY_REFERENCE_NODE: {
        printWriter.print('&');
        printWriter.print(node.getNodeName());
        printWriter.print(';');
        printWriter.flush();
        break;
      }

      case Node.CDATA_SECTION_NODE: {
        printWriter.print("<![CDATA[");
        printWriter.print(node.getNodeValue());
        printWriter.print("]]>");
        printWriter.flush();
        break;
      }

      case Node.TEXT_NODE: {
        normalizeAndPrint(node.getNodeValue());
        printWriter.flush();
        break;
      }

      case Node.PROCESSING_INSTRUCTION_NODE: {
        printWriter.print("<?");
        printWriter.print(node.getNodeName());
        String data = node.getNodeValue();
        if (data != null && data.length() > 0) {
          printWriter.print(' ');
          printWriter.print(data);
        }
        printWriter.println("?>");
        printWriter.flush();
        break;
      }
    }
  }

  /**
   * Write the given element node recursively. This method first checks to
   * see if the given element node has any children. If it does NOT have any
   * children, it invokes the <code>writeEmptyElementNode</code> method.
   * Otherwise, it writes the element node itself. It begins by invoking the
   * <code>writeAttributes</code> method to generate the attributes for the
   * element node. It then invokes the <code>write</code> method for each child.
   * Finally it writes the end tag for the node.
   * @param node the element node to recursively write.
   */
  protected void writeElementNode(Node node) {
    Node child = node.getFirstChild();
    if (child != null) {
      printWriter.print('<');
      printWriter.print(node.getNodeName());
      writeAttributes(node);
      printWriter.print('>');
      printWriter.flush();
      while (child != null) {
        write(child);
        child = child.getNextSibling();
      }
      printWriter.print("</" + node.getNodeName() + '>');
    } else {
      writeEmptyElementNode(node);
    }
    printWriter.flush();
  }

  /**
   * Writes the given empty element node. This implementation simply writes the
   * opening tag with attributes and then the closing tag. It does not use the
   * empty tag shorthand method. It uses the <code>writeAttributes</code> method
   * to generate the attributes for the node.
   * @param node the element node to write
   */
  protected void writeEmptyElementNode(Node node) {
    printWriter.print('<');
    printWriter.print(node.getNodeName());
    writeAttributes(node);
    printWriter.print("></" + node.getNodeName() + '>');
  }

  /**
   * Writes the attributes for the given node in alphabetic order.
   * All attributes are written as attributeName="value". The value is
   * normalized before it is written.
   * @param node the node with the attributes to write.
   * @see #normalizeAndPrint
   */
  protected void writeAttributes(Node node) {
    Attr attrs[] = sortAttributes(node.getAttributes());
    for (int i = 0; i < attrs.length; i++) {
      Attr attr = attrs[i];
      printWriter.print(' ');
      printWriter.print(attr.getNodeName());
      printWriter.print("=\"");
      normalizeAndPrint(attr.getNodeValue());
      printWriter.print('"');
    }
  }

  /**
   * Returns a sorted list of attributes for the given map.
   * @param attrs the attribute map to sort
   * @return an <code>Attr[]</code> of sorted attributes
   */
  protected Attr[] sortAttributes(NamedNodeMap attrs) {
    int len = (attrs != null) ? attrs.getLength() : 0;
    Attr array[] = new Attr[len];
    for (int i = 0; i < len; i++) {
      array[i] = (Attr)attrs.item(i);
    }
    for (int i = 0; i < len - 1; i++) {
      String name = array[i].getNodeName();
      int index = i;
      for (int j = i + 1; j < len; j++) {
        String curName = array[j].getNodeName();
        if (curName.compareTo(name) < 0) {
          name = curName;
          index = j;
        }
      }
      if (index != i) {
        Attr temp = array[i];
        array[i] = array[index];
        array[index] = temp;
      }
    }
    return array;
  }

  /**
   * Normalizes and prints the given string. This method normalizes each
   * character. It replaces any special characters with the &ampXX; notation.
   * @param s the <code>String</code> to normalize and print
   */
  protected void normalizeAndPrint(String s) {
    int len = (s != null) ? s.length() : 0;
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      normalizeAndPrint(c);
    }
  }

  /**
   * Normalizes and print the given character. It replaces any special
   * characters with the &ampXX; notation.
   * @param c the <code>char</code> to normalize and print
   */
  protected void normalizeAndPrint(char c) {
    switch (c) {
      case '<': {
        printWriter.print("&lt;");
        break;
      }
      case '>': {
        printWriter.print("&gt;");
        break;
      }
      case '&': {
        printWriter.print("&amp;");
        break;
      }
      case '"': {
        printWriter.print("&quot;");
        break;
      }
      case '\r':
      case '\n': {
      }
      default: {
        printWriter.print(c);
      }
    }
  }
}
