/**
 * DocumentAdapter - DocumentAdapter for All DOM Document versions
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
import java.io.OutputStream;
import java.io.Writer;

/**
 * DocumentAdapter for All DOM Document versions. This interface provides a
 * common, version safe access to the DOM document.  This interface contains
 * a small number of methods that must be implemented for any DOM version
 * which this application will support.
 * <p>
 * This should be considered an internal interface and is subject to change.
 */
public interface DocumentAdapter {

  /**
   * Write the document to the given OutputStream.
   * @param stream to render the document to
   */
  public void write(OutputStream stream);

  /**
   * Write the document to the given Writer.
   * @param writer to render the document to
   */
  public void write(Writer writer);

  /**
   * Sets the Text of the Element indicated by the given id to the given value.
   */
  public void setElementText(String elementId, String value);

  /**
   * Stores given value as a text attribute of the element identified by the given id
   * Does nothing if the id is not found.  If the given value is null, it renders the
   * attribute as blank ("").
   */
  public void setAttributeText(String elementId, String attribute, String value);

  /**
   * Returns the Element for the given mapped identifier else null if not found.
   */
  public Element getElementById(String elementId);

  /**
   * Returns the Document/adaptee of this DocumentAdapter
   */
  public Document getDocument();
}
