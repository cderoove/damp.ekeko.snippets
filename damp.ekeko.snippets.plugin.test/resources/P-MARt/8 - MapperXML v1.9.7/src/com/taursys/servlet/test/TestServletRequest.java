/**
 * TestServletRequest - partial implementation of ServletRequest for testing
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
package com.taursys.servlet.test;

import javax.servlet.ServletRequest;
import java.util.Enumeration;
import javax.servlet.ServletInputStream;
import java.util.Locale;
import java.io.BufferedReader;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.util.Map;
import java.util.*;

/**
 * ServletRequest object partial implementation for testing purposes
 */
public class TestServletRequest implements ServletRequest {
//  private java.util.Hashtable parameters = new java.util.Hashtable();
  private HashMap parameterMap = new HashMap();

  public TestServletRequest() {
  }

  /**
   * Testing setup method to add simulated parameters to request
   */
  public void addParameter(String key, String value) {
    parameterMap.put(key, new String[] {value});
  }

  /**
   * Testing setup method to add simulated parameters to request
   */
  public void addParameter(String key, String[] values) {
    parameterMap.put(key, values);
  }

  // Implemented methods ===================================================

  public String getParameter(String key) {
    String[] values = getParameterValues(key);
    return values == null ? null : values[0];
  }

  public String[] getParameterValues(String key) {
    return (String[])parameterMap.get(key);
  }

  public Enumeration getParameterNames() {
    return Collections.enumeration(parameterMap.keySet());
  }

  // Unimplemented methods ===================================================

  public Object getAttribute(String name) {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getAttribute() not yet implemented.");
  }
  public Enumeration getAttributeNames() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getAttributeNames() not yet implemented.");
  }
  public String getCharacterEncoding() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getCharacterEncoding() not yet implemented.");
  }
  public int getContentLength() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getContentLength() not yet implemented.");
  }
  public String getContentType() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getContentType() not yet implemented.");
  }
  public ServletInputStream getInputStream() throws IOException {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getInputStream() not yet implemented.");
  }
  public String getProtocol() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getProtocol() not yet implemented.");
  }
  public String getScheme() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getScheme() not yet implemented.");
  }
  public String getServerName() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getServerName() not yet implemented.");
  }
  public int getServerPort() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getServerPort() not yet implemented.");
  }
  public BufferedReader getReader() throws IOException {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getReader() not yet implemented.");
  }
  public String getRemoteAddr() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getRemoteAddr() not yet implemented.");
  }
  public String getRemoteHost() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getRemoteHost() not yet implemented.");
  }
  public void setAttribute(String name, Object o) {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method setAttribute() not yet implemented.");
  }
  public void removeAttribute(String name) {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method removeAttribute() not yet implemented.");
  }
  public Locale getLocale() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getLocale() not yet implemented.");
  }
  public Enumeration getLocales() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getLocales() not yet implemented.");
  }
  public boolean isSecure() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method isSecure() not yet implemented.");
  }
  public RequestDispatcher getRequestDispatcher(String path) {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getRequestDispatcher() not yet implemented.");
  }
  public String getRealPath(String path) {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getRealPath() not yet implemented.");
  }
  public StringBuffer getRequestURL() {
    /**@todo: Implement this javax.servlet.http.HttpServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getRequestURL() not yet implemented.");
  }
  public void setCharacterEncoding(String encoding) throws java.io.UnsupportedEncodingException {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method setCharacterEncoding() not yet implemented.");
  }
  public Map getParameterMap() {
    /**@todo: Implement this javax.servlet.ServletRequest method*/
    throw new java.lang.UnsupportedOperationException("Method getParameterMap() not yet implemented.");
  }
}
