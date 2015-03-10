/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.util.*;
import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/** Provides access to Nutch configuration parameters.
 *
 * <p>Default values for all parameters are specified in a file named
 * <tt>nutch-default.xml</tt> located on the classpath.  Overrides for these
 * defaults should be in an optional file named <tt>nutch-site.xml</tt>, also
 * located on the classpath.  Typically these files reside in the
 * <tt>conf/</tt> subdirectory at the top-level of a Nutch installation.
 */
    
public class NutchConf {
  private static final Logger LOG =
    LogFormatter.getLogger("net.nutch.util.NutchConf");

  private static List resourceNames = new ArrayList();
  private static Properties properties;

  static {
    resourceNames.add("nutch-default.xml");
    resourceNames.add("nutch-site.xml");
  }

  /** Adds a resource name to the chain of resources read.  The first resource
   * is always <tt>nutch-default.xml</tt>, and the last is always
   * <tt>nutch-site.xml</tt>.  New resources are inserted between these, so
   * they can override defaults, but not site-specifics. */
  public static synchronized void addConfResource(String name) {
    resourceNames.add(resourceNames.size()-1, name); // add second to last
    properties = null;                            // trigger reload
  }

  private static synchronized Properties getProps() {
    if (properties == null) {
      properties = new Properties();
      ListIterator i = resourceNames.listIterator();
      while (i.hasNext()) {
        loadResource((String)i.next(), i.nextIndex()==resourceNames.size());
      }
    }
    return properties;
  }

  /** Returns the value of the <code>name</code> property, or null if no
   * such property exists. */
  public static String get(String name) { return getProps().getProperty(name);}

  /** Returns the value of the <code>name</code> property.  If no such property
   * exists, then <code>defaultValue</code> is returned.
   */
  public static String get(String name, String defaultValue) {
     return getProps().getProperty(name, defaultValue);
  }
  
  /** Returns the value of the <code>name</code> property as an integer.  If no
   * such property is specified, or if the specified value is not a valid
   * integer, then <code>defaultValue</code> is returned.
   */
  public static int getInt(String name, int defaultValue) {
    String valueString = get(name);
    if (valueString == null)
      return defaultValue;
    try {
      return Integer.parseInt(valueString);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /** Returns the value of the <code>name</code> property as a long.  If no
   * such property is specified, or if the specified value is not a valid
   * long, then <code>defaultValue</code> is returned.
   */
  public static long getLong(String name, long defaultValue) {
    String valueString = get(name);
    if (valueString == null)
      return defaultValue;
    try {
      return Long.parseLong(valueString);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /** Returns the value of the <code>name</code> property as a float.  If no
   * such property is specified, or if the specified value is not a valid
   * float, then <code>defaultValue</code> is returned.
   */
  public static float getFloat(String name, float defaultValue) {
    String valueString = get(name);
    if (valueString == null)
      return defaultValue;
    try {
      return Float.parseFloat(valueString);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /** Returns an input stream attached to the configuration resource with the
   * given <code>name</code>.
   */
  public static InputStream getConfResourceAsInputStream(String name) {
    try {
      URL url= NutchConf.class.getClassLoader().getResource(name);

      if (url == null) {
        LOG.info(name + " not found");
        return null;
      } else {
        LOG.info("found resource " + name + " at " + url);
      }

      return url.openStream();
    } catch (Exception e) {
      return null;
    }
  }

  /** Returns a reader attached to the configuration resource with the
   * given <code>name</code>.
   */
  public static Reader getConfResourceAsReader(String name) {
    try {
      URL url= NutchConf.class.getClassLoader().getResource(name);

      if (url == null) {
        LOG.info(name + " not found");
        return null;
      } else {
        LOG.info("found resource " + name + " at " + url);
      }

      return new InputStreamReader(url.openStream());
    } catch (Exception e) {
      return null;
    }
  }

  /** Returns the value of the <code>name</code> property as an boolean.  If no
   * such property is specified, or if the specified value is not a valid
   * boolean, then <code>defaultValue</code> is returned.  Valid boolean values
   * are "true" and "false".
   */
  public static boolean getBoolean(String name, boolean defaultValue) {
    String valueString = get(name);
    if ("true".equals(valueString))
      return true;
    else if ("false".equals(valueString))
      return false;
    else return defaultValue;
  }

  private static void loadResource(String name, boolean quietFail) {
    try {
      URL url = NutchConf.class.getClassLoader().getResource(name);

      if (url == null) {
        if (!quietFail)
          LOG.severe(name + " not found");
        return;
      } else {
        LOG.info("loading " + url);
      }

      Document doc =
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(url.toString());
      Element root = doc.getDocumentElement();
      if (!"nutch-conf".equals(root.getTagName()))
        LOG.severe("bad conf file: top-level element not <nutch-conf>");
      NodeList props = root.getChildNodes();
      for (int i = 0; i < props.getLength(); i++) {
        Node propNode = props.item(i);
        if (!(propNode instanceof Element))
          continue;
        Element prop = (Element)propNode;
        if (!"property".equals(prop.getTagName()))
          LOG.warning("bad conf file: element not <property>");
        NodeList fields = prop.getChildNodes();
        String attr = null;
        String value = null;
        for (int j = 0; j < fields.getLength(); j++) {
          Node fieldNode = fields.item(j);
          if (!(fieldNode instanceof Element))
            continue;
          Element field = (Element)fieldNode;
          if ("name".equals(field.getTagName()))
            attr = ((Text)field.getFirstChild()).getData();
          if ("value".equals(field.getTagName()) && field.hasChildNodes())
            value = ((Text)field.getFirstChild()).getData();
        }
        if (attr != null && value != null)
          properties.setProperty(attr, value);
      }
        
    } catch (Exception e) {
      LOG.severe("error parsing conf file: " + e);
    }
    
  }

  /** For debugging.  List all properties to the terminal and exits. */
  public static void main(String[] args) {
    getProps().list(System.out);
  }
}
