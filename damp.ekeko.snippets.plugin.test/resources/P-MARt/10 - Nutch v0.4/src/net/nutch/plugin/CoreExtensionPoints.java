/*
 * Copyright (c) 2003 The Nutch Organization. All rights reserved. Use subject
 * to the conditions in http://www.nutch.org/LICENSE.txt.
 */
package net.nutch.plugin;
/**
 * Hosts all extension point-ids that are required to run nutch, so called core
 * extension points. The core extension points are installed until nutch
 * startup and require a proper extension provided by a plugin to run nutch.
 * 
 * @author joa23
 */
public class CoreExtensionPoints {
  public final static String EXTRACTOR_X_POINT_ID = "net.nutch.extractor.parserXpoint";
  /**
   * Returns all Extension Points that are required to run nutch
   * 
   * @return String[] Array of Extension point ids.
   */
  public static String[] getCoreExtensionPoints() {
    String[] strings = new String[1];
    strings[0] = EXTRACTOR_X_POINT_ID;
    return strings;
  }
}
