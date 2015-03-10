/**
 * ServletApp - Master Servlet for application.
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
package com.taursys.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import com.taursys.servlet.ServletForm;
import com.taursys.servlet.ServletFormFactory;

/**
 * ServletApp functions as a main servlet which dispatches requests to registered ServletForms.
 * This ServletApp uses a ServletFormFactory to create, recycle, pool and parse
 * url's for ServletForms.
 * <p>
 * You should override the init method to register the virtual
 * path's and ServletForms' package names for your application. You should also
 * set the ServletFormFactory's defaultClassLoader if mapperxml.jar is in a
 * common path rather than part of your application distribution.
 * <p>
 * The ServletApp routes all doPost requests to the doGet methdod.
 * <p>
 * Below is an example of typical implementation:
 * <pre>
 * public class MyMainServlet extends ServletApp {
 *
 *   public void init(ServletConfig config) throws ServletException {
 *     super.init(config);
 *     getFactory().addPackage("/","com.taursys.examples.simpleweb");
 *     getFactory().setDefaultFormName("com.taursys.examples.simpleweb.ShowHidePage");
 *     getFactory().setDefaultClassLoader(getClass().getClassLoader());
 *     // Set default logging
 *     Debug.setLoggerAdapter(new SimpleLogger(Debug.DEBUG));
 *   }
 * }
 * </pre>
 */
public class ServletApp extends HttpServlet {
  private ServletFormFactory factory = new ServletFormFactory();

  /**
   * Processes the HTTP Get request by dispatching it to a ServletForm.
   * The specific ServletForm is determined by the requested url.  This
   * ServletApp uses a ServletFormFactory to parse the url and return the
   * appropriate ServletForm to service the request.  The request is simply
   * passed to the ServletForm.  After the request is complete, the ServletForm
   * is passed back to the ServletFormFactory for recycling (if supported).
   * You can override this method to make additional application resources
   * available by using the request.setAttribute method.
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // Determine the Presentation Manager
    try {
      ServletForm form = factory.createServletForm(request.getPathInfo());
      try {
        form.doGet(request, response);
      }
      catch (Exception ex) {
        throw new ServletException("Unhandled Exception in ServletForm: "
            + ex.getMessage(), ex);
      } finally {
        factory.recycle(form);
      }
    }
    catch (ServletFormNotFoundException ex) {
      // throw 404
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
        "The requested ServletForm was not found.  If you typed the url, please "
        + "check to be sure it is correct.  It is also possible that the system "
        + "that provides this resource is temporarily unavailable.<br/><hr/>"
        + "Internal error message: " + ex.getMessage() + ").");
    }
  }

  /**
   * Process the HTTP Post request by invoking the doGet method.
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

  /**
   * Gets the ServletFormFactory for this ServletApp.
   * @return ServletFormFactory for this ServletApp
   */
  public ServletFormFactory getFactory() {
    return factory;
  }

  /**
   * Sets the ServletFormFactory for this ServletApp.
   * @param factory to use for this ServletApp
   */
  public void setFactory(ServletFormFactory factory) {
    this.factory = factory;
  }
}
