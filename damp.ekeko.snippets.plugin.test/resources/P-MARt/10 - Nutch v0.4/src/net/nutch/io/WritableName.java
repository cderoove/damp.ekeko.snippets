/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import net.nutch.db.*;
import java.util.HashMap;
import java.io.IOException;

/** Utility to permit renaming of Writable implementation classes without
 * invalidiating files that contain their class name.
 * @author Doug Cutting
 */
public class WritableName {
  private static HashMap NAME_TO_CLASS = new HashMap();
  private static HashMap CLASS_TO_NAME = new HashMap();

  static {                                        // define primitive types
    WritableName.setName(NullWritable.class, "null");
    WritableName.setName(LongWritable.class, "long");
    WritableName.setName(UTF8.class, "UTF8");
    WritableName.setName(MD5Hash.class, "MD5Hash");
    WritableName.setName(Page.class, "Page");
    WritableName.setName(Link.class, "Link");

    // For backwards compatibility
    WritableName.addName(Page.class, "net.nutch.pagedb.Page");
    WritableName.addName(Link.class, "net.nutch.linkdb.LinkRecord");
  }

  private WritableName() {}                      // no public ctor

  /** Set the name that a class should be known as to something other than the
   * class name. */
  public static synchronized void setName(Class writableClass, String name) {
    CLASS_TO_NAME.put(writableClass, name);
    NAME_TO_CLASS.put(name, writableClass);
  }

  /** Add an alternate name for a class. */
  public static synchronized void addName(Class writableClass, String name) {
    NAME_TO_CLASS.put(name, writableClass);
  }

  /** Return the name for a class.  Default is {@link Class#getName()}. */
  public static synchronized String getName(Class writableClass) {
    String name = (String)CLASS_TO_NAME.get(writableClass);
    if (name != null)
      return name;
    return writableClass.getName();
  }

  /** Return the class for a name.  Default is {@link Class#forName(String)}.*/
  public static synchronized Class getClass(String name) throws IOException {
    Class writableClass = (Class)NAME_TO_CLASS.get(name);
    if (writableClass != null)
      return writableClass;
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw new IOException(e.toString());
    }
  }

}
