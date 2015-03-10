/**
 * Settings - Class which contains, stores and retrieves properties
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
package com.taursys.tools;

import java.util.*;
import java.io.*;
import com.taursys.debug.Debug;

/**
 * Settings is a class which contains, stores and retrieves properties
 * @author Marty Phelan
 * @version 1.0
 */
public abstract class Settings {
  protected java.util.Properties properties;
  private String description;

  /**
   * Constructs a new Settings
   */
  public Settings(String description) {
    this.description = description;
  }

  /**
   * Gets the full property file name
   */
  protected abstract String getPropertyFilePath();

  /**
   * Gets the full property file name
   */
  protected abstract String getPropertyFileName();

  /**
   * Loads the CodeGen Settings from the default settings file (if exists)
   * @throws IOException if problem loading properties file
   */
  public void loadSettings() throws java.io.IOException {
    // Load if exists
    File propFile = new File(getPropertyFileName());
    loadSettings(propFile);
  }

  /**
   * Loads the CodeGen Settings from the default settings file (if exists)
   * @throws IOException if problem loading properties file
   */
  public void loadSettings(File propFile) throws java.io.IOException {
    if (propFile.exists()) {
      FileInputStream stream = new FileInputStream(propFile);
      properties.load(stream);
      stream.close();
    }
  }

  /**
   * Saves the CodeGen Settings to the default settings file.
   * @throws IOException if problem loading properties file
   */
  public void saveSettings() throws java.io.IOException {
    File propFile = new File(getPropertyFilePath());
    if (!propFile.exists())
      propFile.mkdirs();
    propFile = new File(getPropertyFileName());
    propFile.createNewFile();
    FileOutputStream stream = new FileOutputStream(propFile);
    properties.store(stream, description);
    stream.close();
  }

  /**
   * Gets the CodeGen Properties
   * @return the CodeGen Properties
   */
  public java.util.Properties getProperties() {
    return properties;
  }

  /**
   * Gets CodeGen property value for given key
   * @return CodeGen property value for given key
   * @param key of requested property
   */
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * Sets the CodeGen property value for the given key
   * @param key of the property
   * @param value for the property
   */
  public void setProperty(String key, String value) {
    properties.setProperty(key, value);
  }

  /**
   * Gets the description of this Settings object.  The description is stored
   * in the properties file during a save.
   * @return the description of this Settings object
   */
  public String getDescription() {
    return description;
  }
}
