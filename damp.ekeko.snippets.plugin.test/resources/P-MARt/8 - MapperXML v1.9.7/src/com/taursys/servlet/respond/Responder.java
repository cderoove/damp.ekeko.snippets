/**
 * Responder - subcomponent used by a ServletForm to send the response
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

import com.taursys.servlet.ServletForm;

/**
 * Responder is a subcomponent used by a ServletForm to send the response
 * @author Marty Phelan
 * @version 1.0
 */
public interface Responder {

  /**
   * Provide the appropriate response for the ServletForm.
   * Example responses are:
   * <ul>
   * <li>sending back an HTML document</li>
   * <li>sending back a binary JPEG file</li>
   * <li>sending back a redirect url</li>
   * <li>sending NO response</li>
   * <li>sending an HTTP response code (example 403 forbidden)</li>
   * </ul>
   */
  public void respond() throws Exception;

  /**
   * Set the ServletForm this subcomponent supports.
   * This method is normally invoked by the ServletForm when this subcomponent
   * is attached to it via the setResponder method.  You should not set this
   * property.
   * @param newServletForm the ServletForm this subcomponent supports.
   */
  public void setServletForm(ServletForm newServletForm);
}
