/**
 * ProjectSettings - Contains settings for current project
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
 * ProjectSettings Contains settings for current project
 * @author Marty Phelan
 * @version 1.0
 */
public class ProjectSettings extends Settings {
  public static final String PROP_FILE_NAME = "project.mpj";
  public static final String PROJECT_PATH = "projectPath";
  public static final String SOURCE_PATH = "sourcePath";
  private UserSettings userSettings;

  /**
   * Constructs a new ProjectSettings and initializes defaults
   */
  public ProjectSettings(Settings userSettings) {
    super("Mapper CodeGen Project Settings");
    // Initialize codeGenProps with defaults
    properties = new Properties(userSettings.getProperties());
    properties.setProperty(PROJECT_PATH,
        userSettings.getProperty(UserSettings.DEFAULT_PROJECT_PATH));
    properties.setProperty(SOURCE_PATH, "");
  }

  /**
   * Gets the full property file name
   */
  protected String getPropertyFilePath() {
    return properties.getProperty(PROJECT_PATH);
  }

  /**
   * Gets the full property file name
   */
  protected String getPropertyFileName() {
    String propertyFilePath = properties.getProperty(PROJECT_PATH);
    if (propertyFilePath.endsWith("/"))
      return propertyFilePath + PROP_FILE_NAME;
    else
      return propertyFilePath + "/" + PROP_FILE_NAME;
  }
}
