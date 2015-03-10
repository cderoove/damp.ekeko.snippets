/**
 * AbstractWriter - Class to write a given Document to an OutputStream or Writer.
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
import java.io.Writer;
import org.w3c.dom.Document;

/**
 * This class is used to write a given Document to an OutputStream or Writer.
 */
public abstract class AbstractWriter {

  /**
   * Default constructor.
   */
  public AbstractWriter() {
  }

  /**
   * Writes the specified node, recursively to the OutputStream.
   */
  public abstract void write(Document doc, OutputStream stream);

  /**
   * Writes the specified node, recursively to the Writer.
   */
  public abstract void write(Document doc, Writer writer);
}
