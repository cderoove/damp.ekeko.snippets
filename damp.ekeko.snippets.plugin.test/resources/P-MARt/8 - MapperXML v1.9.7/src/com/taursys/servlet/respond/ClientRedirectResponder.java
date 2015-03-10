/**
 * ClientRedirectResponder -  redirects the client to a URL as the response.
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
 * ClientRedirectResponder redirects the client to a URL as the response.
 * @author Marty Phelan
 * @version 1.0
 */
public class ClientRedirectResponder extends AbstractResponder {
  private String redirectURL;

  /**
   * Constructs a new ClientRedirectResponder
   */
  public ClientRedirectResponder() {
  }

  /**
   * Constructs a new ClientRedirectResponder
   * @param redirectURL url where to redirect client
   */
  public ClientRedirectResponder(String redirectURL) {
    this.redirectURL = redirectURL;
  }

  /**
   * Responds by redirecting client the the redirectURL.
   * @throws Exception if problem responding
   */
  public void respond() throws Exception {
    getServletForm().getResponse().sendRedirect(redirectURL);
  }

  /**
   * Set the URL to redirect the client to.
   * @param newRedirectURL the URL to redirect the client to.
   */
  public void setRedirectURL(String newRedirectURL) {
    redirectURL = newRedirectURL;
  }

  /**
   * Get the URL to redirect the client to.
   * @return the URL to redirect the client to.
   */
  public String getRedirectURL() {
    return redirectURL;
  }
}
