/**
 * ContentResponder - abstract responder which has a contentType
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
package com.taursys.servlet.respond;

/**
 * ContentResponder is abstract responder which has a contentType
 * @author Marty Phelan
 * @version 1.0
 */
public abstract class ContentResponder extends AbstractResponder {
  private String contentType = "text/html";

  /**
   * Constructs a new ContentResponder
   */
  public ContentResponder() {
  }

  /**
   * Set the mime type for the response content.
   * The default is "text"html"
   * @param newContentType the mime type for the response content.
   */
  public void setContentType(String newContentType) {
    contentType = newContentType;
  }

  /**
   * Get the mime type for the response content.
   * The default is "text"html"
   * @return the mime type for the response content.
   */
  public String getContentType() {
    return contentType;
  }
}
