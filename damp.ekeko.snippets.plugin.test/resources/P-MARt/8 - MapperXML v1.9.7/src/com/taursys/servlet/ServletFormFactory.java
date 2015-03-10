/**
 * ServletFormFactory - This Factory creates and recycles servlet forms for an application.
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

import java.util.Hashtable;
import java.util.Vector;

/**
 * This Factory creates and recycles servlet forms for an application.
 * This class was designed to be used by a ServletApp.  It will typically
 * be setup in the init method of the ServletApp.  Below is an example of
 * typical usage:
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
public class ServletFormFactory {
  private Hashtable packages = new Hashtable();
  private Hashtable servletForms = new Hashtable();
  private String defaultFormName;
  private String servletFormSuffix = ".sf";
  private ClassLoader defaultClassLoader;

  /**
   * Creates a new ServletFormFactory with a defaultFormName of com.taursys.servlet.DefaultMessageForm.
   * Also sets the defaultClassLoader to this class's classLoader
   */
  public ServletFormFactory() {
    defaultFormName = "com.taursys.servlet.DefaultMessageForm";
    defaultClassLoader = getClass().getClassLoader();
  }

  /**
   * Returns a ServletForm based on the given url.  This method will first see
   * if there are any recycled forms in the servletForms pool.  If so, it will
   * remove that form from the pool and return it.  If none are in the pool,
   * it will create a new one.  This method depends on the parseClassName
   * method to determine the fully qualified class name of the ServletForm
   * based on the given url.
   * @param url containing the encoded ServletForm name
   * @return a ServletForm of the specific class requested in the url
   * @throws ServletFormNotFoundException if cannot parse url, package is not
   * registered, or ServletForm class is not found.
   */
  public synchronized ServletForm createServletForm(String url)
      throws ServletFormNotFoundException {
    String servletFormName = parseClassName(url);
    Vector instances = (Vector)servletForms.get(servletFormName);
    // First try to get from pool
    ServletForm newForm = null;
    if (instances != null && instances.size() > 0) {
        newForm = (ServletForm)instances.remove(0);
    }
    // If none in pool then create a new one
    if (newForm == null ) {
      try {
        newForm = (ServletForm)defaultClassLoader.loadClass(servletFormName).newInstance();
      } catch (Exception ex) {
        throw new ServletFormNotFoundException("Cannot create requested form: "
            + ex);
      }
    }
    return newForm;
  }

  /**
   * Recycle will put given servletForm back into pool if servletForm supports recycling.
   * The servletForm's recycle method returns true if it supports recycling.
   * @param usedServletForm to recycle (if it supports recycling)
   */
  public synchronized void recycle(ServletForm usedServletForm) {
    if (usedServletForm.recycle()) {
      Vector instances = (Vector)servletForms.get(
          usedServletForm.getClass().getName());
      if (instances == null) {
        instances = new Vector();
        servletForms.put(usedServletForm.getClass().getName(), instances);
      }
      instances.add(usedServletForm);
    }
  }

  /**
   * Parses given url and constructs fully qualified ServletForm class name.
   * Returns the defaultFormName if the given url is null or "/".  The
   * url is broken into 2 parts: path and form name.  The package name is
   * retrieved from the packages table using the path as the key. The
   * class name is returned as the package name plus the form name (without
   * the servletFormSuffix).  Examples:
   * <p>
   * Assuming 2 entries in package table:
   * <ul>
   * <li>path="/" packageName="com.taursys.example"</li>
   * <li>path="/admin" packageName="com.taursys.inv.admin"</li>
   * </ul>
   * Example translations:
   * <ul>
   * <li>url="/MyForm.sf" className="com.taursys.example.MyForm"</li>
   * <li>url="/admin/EditItem.sf" className="com.taursys.inv.admin.EditItem"</li>
   * </ul>
   * @param url path to parse - usually obtained through request.getPathInfo()
   * @return fully qualified class name string
   * @throws ServletFormNotFoundException if path not found or form does not
   * end in servletFormSuffix.
   */
  public String parseClassName(String url) throws ServletFormNotFoundException {
    if (url != null && !url.equals("/")) {
      // Extract path/packageName first
      int lastSlash = url.lastIndexOf("/");
      String urlPrefix = url.substring(0,lastSlash +1 );
      String packageName = (String)packages.get(urlPrefix);
      if (packageName == null)
        throw new ServletFormNotFoundException(
            "Prefix " + urlPrefix + " not registered");
      // Prepare and check form name
      String formName = url.substring(lastSlash + 1);
      int sLen = servletFormSuffix.length();
      if (formName.length() < sLen + 1 || !formName.endsWith(servletFormSuffix))
        throw new ServletFormNotFoundException("Name of ServletForm does not end in "
            + servletFormSuffix + ". url=" + url);
      // done
      return packageName + "."
          + formName.substring(0, formName.length() - sLen);
    } else {
      return defaultFormName;
    }
  }

  /**
   * Adds the given path and packageName to the packages table.  The packages
   * table is used in the parseClassName method to determine the fully qualified
   * class name for a given path/ServletFormName.
   * @param path that form names will be registered under
   * @param packageName full package name for forms in given path
   */
  public void addPackage(String path, String packageName) {
    if (path.endsWith("/"))
      packages.put(path, packageName);
    else
      packages.put(path + "/", packageName);
  }

  /**
   * Sets the default ServletForm name for this factory.  This must be
   * a fully qualified class name.
   * @param newDefaultFormName for this factory
   */
  public void setDefaultFormName(String newDefaultFormName) {
    defaultFormName = newDefaultFormName;
  }

  /**
   * Gets the default ServletForm name for this factory.
   * @return the defaultFormName for this factory
   */
  public String getDefaultFormName() {
    return defaultFormName;
  }

  /**
   * Sets the suffix for ServletForms.  The default is ".sf".
   * The suffix cannot be null, but it can be blank if no suffix is wanted.
   * @param newServletFormSuffix the suffix to use
   * @throws IllegalArgumentException if passed a null value.
   */
  public void setServletFormSuffix(String newServletFormSuffix) {
    if (newServletFormSuffix == null)
      throw new java.lang.IllegalArgumentException(
        "servletFormSuffix cannot be null - use blank to indicate no suffix");
    servletFormSuffix = newServletFormSuffix;
  }

  /**
   * Gets the current suffix for ServletForms.  The default is ".sf".
   * @return the current suffix for ServletForms.
   */
  public String getServletFormSuffix() {
    return servletFormSuffix;
  }

  /**
   * Set the default ClassLoader used to load and instantiate ServletForms.
   * This loader is initially set to the ClassLoader for this class.
   * Your ServletApp may need to set this property if the MapperXML classes
   * are loaded from a different ClassLoader than your application classes.
   * If you deploy your web application with mapperxml.jar in the
   * /WEB-INF/lib directory, then you will probably not need to set this
   * property.  You will most commonly set this property within your
   * ServletApp's init method (example):
   * <pre>
   *   getFactory().setDefaultClassLoader(getClass().getClassLoader());
   * </pre>
   * @param newDefaultClassLoader to use to load and instantiate ServletForms.
   */
  public void setDefaultClassLoader(ClassLoader newDefaultClassLoader) {
    defaultClassLoader = newDefaultClassLoader;
  }

  /**
   * Get the default ClassLoader used to load and instantiate ServletForms.
   * This loader is initially set to the ClassLoader for this class.
   * @return the default ClassLoader to use to load and instantiate ServletForms.
   */
  public ClassLoader getDefaultClassLoader() {
    return defaultClassLoader;
  }
}
