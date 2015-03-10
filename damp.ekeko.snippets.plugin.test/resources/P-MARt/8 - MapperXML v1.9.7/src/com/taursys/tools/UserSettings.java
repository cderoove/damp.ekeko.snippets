/**
 * UserSettings - Contains default settings for current user
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
 * UserSettings contains default settings for current user
 * @author Marty Phelan
 * @version 1.0
 */
public class UserSettings extends Settings {
  public static final String PROP_FILE_NAME = "user.mpj";
  private String propertyFilePath = System.getProperty("user.home");
  public static final String AUTHOR = "author";
  public static final String COPYRIGHT = "copyright";
  public static final String DEFAULT_PROJECT_PATH = "defaultProjectPath";
  public static final String TEMPLATES_PATH = CodeGenerator.TEMPLATES_PATH;
  public static final String LAST_PROJECT_PATH = "lastProjectPath";

  /**
   * Constructs a new UserSettings and initializes defaults
   */
  public UserSettings() {
    super("Mapper CodeGen User Settings");
    // Setup default properties
    Properties defaultProps = new Properties();
    defaultProps.setProperty(AUTHOR, "Your Name Here");
    defaultProps.setProperty(COPYRIGHT,"Copyright (c) " +
          GregorianCalendar.getInstance().get(Calendar.YEAR));
    defaultProps.setProperty(TEMPLATES_PATH,"./");
    defaultProps.setProperty(DEFAULT_PROJECT_PATH, propertyFilePath
        + "/" + "projects");
    defaultProps.setProperty(LAST_PROJECT_PATH, "");
    // Initialize codeGenProps with defaults
    properties = new Properties(defaultProps);
  }

  /**
   * Gets the full property file name
   */
  protected String getPropertyFilePath() {
    return propertyFilePath;
  }

  /**
   * Gets the full property file name
   */
  protected String getPropertyFileName() {
    if (propertyFilePath.endsWith("/"))
      return propertyFilePath + PROP_FILE_NAME;
    else
      return propertyFilePath + "/" + PROP_FILE_NAME;
  }
}
