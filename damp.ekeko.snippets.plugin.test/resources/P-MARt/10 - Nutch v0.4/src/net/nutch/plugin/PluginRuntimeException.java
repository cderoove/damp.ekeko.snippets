/*
 * Copyright (c) 2003 The Nutch Organization. All rights reserved. Use subject
 * to the conditions in http://www.nutch.org/LICENSE.txt.
 */
package net.nutch.plugin;
/**
 * <code>PluginRuntimeException</code> will be thrown until a exception in the
 * plugin managemnt occurs.
 * 
 * @author joa23
 */
public class PluginRuntimeException extends Exception {
  /**
   * @see java.lang.Throwable#Throwable(Throwable)
   */
  public PluginRuntimeException(Throwable cause) {
    super(cause);
  }
}
