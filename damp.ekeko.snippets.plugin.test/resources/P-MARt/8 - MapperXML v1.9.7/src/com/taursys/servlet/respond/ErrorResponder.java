/**
 * ErrorResponder - sends status code and optional message as response.
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
 * ErrorResponder sends status code and optional message as response.
 * @author Marty Phelan
 * @version 1.0
 */
public class ErrorResponder extends AbstractResponder {
  private int statusCode;
  private String message;

  /**
   * Constructs a new ErrorResponder
   */
  public ErrorResponder() {
  }

  /**
   * Constructs a new ErrorResponder with status code
   */
  public ErrorResponder(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * Constructs a new ErrorResponder with status code and message
   */
  public ErrorResponder(int statusCode, String message) {
    this.statusCode = statusCode;
    this.message = message;
  }

  /**
   * Responds by sending status code and optional message.
   * @throws Exception if problem responding
   */
  public void respond() throws Exception {
    if (message == null) {
      getServletForm().getResponse().sendError(statusCode);
    }
    else {
      getServletForm().getResponse().sendError(statusCode, message);
    }
  }

  /**
   * Set the status code to be used for the response.
   * @param newStatusCode the status code to be used for the response.
   */
  public void setStatusCode(int newStatusCode) {
    statusCode = newStatusCode;
  }

  /**
   * Get the status code to be used for the response.
   * @return the status code to be used for the response.
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Set the optional message to be used for the reponse.
   * If the message is null, only the status code will be sent.
   * @param newMessage the optional message to be used for the reponse.
   */
  public void setMessage(String newMessage) {
    message = newMessage;
  }

  /**
   * Get the optional message to be used for the reponse.
   * If the message is null, only the status code will be sent.
   * @return the optional message to be used for the reponse.
   */
  public String getMessage() {
    return message;
  }
}
