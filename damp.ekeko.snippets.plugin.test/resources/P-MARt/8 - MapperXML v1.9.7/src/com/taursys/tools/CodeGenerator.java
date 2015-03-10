/**
 * CodeGenerator - Generates code using Velocity
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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.io.IOException;
import java.util.Properties;

/**
 * CodeGenerator is a singleton which generates code using Velocity
 * @author Marty Phelan
 * @version 1.0
 */
public class CodeGenerator {
  private static CodeGenerator singleton;
  private boolean initialized = false;
  private VelocityEngine velocity = null;
  private Properties properties = new Properties();
  public static final String TEMPLATES_PATH = "templatesPath";

  /**
   * Constructs a new CodeGenerator and set default properties
   */
  private CodeGenerator() {
    properties.setProperty(TEMPLATES_PATH, "./");
  }

  /**
   * Sets all properties used by this CodeGenerator
   */
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  /**
   * Gets the singleton instance of the CodeGenerator.
   * @return a singleton CodeGenerator
   */
  public static CodeGenerator getInstance() {
    if (singleton == null) {
      // create a new instance
      singleton = new CodeGenerator();
    }
    return singleton;
  }

  /**
   * Initializes the CodeGenerator engine if not already initialized
   * Initializes the internal VelocityEngine with this CodeGenerator's
   * properties.  All the properties have default values.
   * This method will simply return if this CodeGenerator is already initialized.
   * @throws Exception if problem initializing the engine
   */
  public void initialize() throws Exception {
    if (initialized)
      return;
    // create a new instance of the engine
    velocity = new VelocityEngine();
    // initialize the engine - set logger
    velocity.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
        "com.taursys.debug.VelocitySimpleLogger");
    // Use both class and file Resource loaders
    velocity.setProperty(VelocityEngine.RESOURCE_LOADER, "class, file");
    // Setup class resource loader
    velocity.setProperty("class.resource.loader.description",
        "Velocity Classpath Resource Loader");
    velocity.setProperty("class.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    // Setup file resource loader for current directory
    velocity.setProperty("file.resource.loader.description",
        "Velocity File Resource Loader");
    velocity.setProperty("file.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
    velocity.setProperty("file.resource.loader.path",
        properties.getProperty(TEMPLATES_PATH));
    velocity.setProperty("file.resource.loader.cache",
        "false");
    velocity.setProperty("file.resource.loader.modificationCheckInterval",
        "2");
    // Initialize Velocity Engine
    velocity.init();
  }

  /**
   * Generates the source code using the Velocity Engine and the template
   * @param templateName to produce Java code
   * @param context which contains data to merge into template
   * @param srcPath is path where source should be stored
   * @param packageName is java package name which whill be appended to srcPath
   * @param className is java class name and file name
   * @throws Exception if problem creating file or running template
   */
  public void generateCode(String templateName, Context context, String srcPath,
    String packageName, String className) throws Exception {
    // Get template
    Template t = velocity.getTemplate(templateName);
    // Create writer
    FileWriter writer = new FileWriter(createFile(srcPath, packageName, className));
    // Generate and close
    t.merge(context, writer);
    writer.close();
  }

  /**
   * This method creates a node in a given project, according to the packageName
   * and filename provided.
   *
   * @param sourcePath the base path where to create the source file.
   * @param packageName The name of the package in which the file will be created.
   * @param fileName The name of the file to be created.
   * @return The FileNode which was created.
   * @throws IOException if creation of file fails
   */
  private File createFile(String sourcePath, String packageName, String fileName)
      throws IOException {
    // Build directory path first
    if (!sourcePath.endsWith("/"))
      sourcePath += "/";
    if (packageName != null && packageName.length() > 0)
      sourcePath = sourcePath + packageName.replace('.', '/');
    // Create directory if does not exist
    File file = new File(sourcePath);
    if (!file.exists()) {
      if (!file.mkdirs())
        throw new IOException("Creation of path failed: " + file.getAbsolutePath());
    }
    // Now create file
    file = new File(sourcePath + "/" + fileName + ".java");
    file.createNewFile();
    return file;
  }
}
