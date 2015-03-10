/*
 * Copyright (c) 2003 The Nutch Organization. All rights reserved. Use subject
 * to the conditions in http://www.nutch.org/LICENSE.txt.
 */
package net.nutch.plugin;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.nutch.util.LogFormatter;
import net.nutch.util.NutchConf;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
/**
 * The <code>PluginManifestParser</code> parser just parse the manifest file in all plugin
 * directories.
 * 
 * @author joa23
 */
public class PluginManifestParser {
  public static final Logger LOG = LogFormatter
    .getLogger("net.nutch.plugin.PluginManifestParser");
  /**
   * Returns a list with plugin descriptors.
   * 
   * @return ArrayList
   * @throws IOException
   * @throws MalformedURLException
   * @throws DocumentException
   */
  public static ArrayList parsePluginFolder() {
    ArrayList list = new ArrayList();
    String pluginFolder = NutchConf.get("plugin.folder");
    if (pluginFolder == null)
      throw new IllegalArgumentException("no plugin folder setuped...");
    File file = new File(pluginFolder);
    File[] files = file.listFiles();
    for (int i = 0; i < files.length; i++) {
      File oneSubFolder = files[i];
      if (oneSubFolder.isDirectory()) {
        String manifestPath = oneSubFolder.getAbsolutePath()
          + File.separator + "plugin.xml";
        try {
          list.add(parseManifestFile(manifestPath));
        } catch (MalformedURLException e) {
          LOG.fine(e.toString());
        } catch (DocumentException e) {
          LOG.fine(e.toString());
        }
      }
    }
    return list;
  }
  /**
   * @param manifestPath
   */
  private static PluginDescriptor parseManifestFile(String pManifestPath)
    throws MalformedURLException, DocumentException {
    Document document = parseXML(new File(pManifestPath).toURL());
    String pPath = new File(pManifestPath).getParent();
    return parsePlugin(document, pPath);
  }
  /**
   * @param url
   * @return Document
   * @throws DocumentException
   */
  private static Document parseXML(URL url) throws DocumentException {
    SAXReader reader = new SAXReader();
    Document document = reader.read(url);
    return document;
  }
  /**
   * @param document
   */
  private static PluginDescriptor parsePlugin(Document pDocument, String pPath)
    throws MalformedURLException {
    Element rootElement = pDocument.getRootElement();
    String id = rootElement.attributeValue("id");
    String name = rootElement.attributeValue("name");
    String version = rootElement.attributeValue("version");
    String providerName = rootElement.attributeValue("provider-name");
    String pluginClazz = rootElement.attributeValue("class");
    PluginDescriptor pluginDescriptor = new PluginDescriptor(id, version,
                                                             name, providerName, pluginClazz, pPath);
    parseExtension(rootElement, pluginDescriptor);
    parseExtensionPoints(rootElement, pluginDescriptor);
    parseLibraries(rootElement, pluginDescriptor);
    return pluginDescriptor;
  }
  /**
   * @param rootElement
   * @param pluginDescriptor
   */
  private static void parseLibraries(Element pRootElement,
                                     PluginDescriptor pDescriptor) throws MalformedURLException {
    Element runtime = pRootElement.element("runtime");
    if (runtime == null)
      return;
    List libraries = runtime.elements("library");
    for (int i = 0; i < libraries.size(); i++) {
      Element library = (Element) libraries.get(i);
      String libName = library.attributeValue("name");
      Element exportElement = library.element("extport");
      if (exportElement != null)
        pDescriptor.addExportedLibRelative(libName);
      else
        pDescriptor.addNotExportedLibRelative(libName);
    }
  }
  /**
   * @param rootElement
   * @param pluginDescriptor
   */
  private static void parseExtensionPoints(Element pRootElement,
                                           PluginDescriptor pPluginDescriptor) {
    List list = pRootElement.elements("extension-point");
    if (list != null) {
      for (int i = 0; i < list.size(); i++) {
        Element oneExtensionPoint = (Element) list.get(i);
        String id = oneExtensionPoint.attributeValue("id");
        String name = oneExtensionPoint.attributeValue("name");
        String schema = oneExtensionPoint.attributeValue("schema");
        ExtensionPoint extensionPoint = new ExtensionPoint(id, name,
                                                           schema);
        pPluginDescriptor.addExtensionPoint(extensionPoint);
      }
    }
  }
  /**
   * @param rootElement
   * @param pluginDescriptor
   */
  private static void parseExtension(Element pRootElement,
                                     PluginDescriptor pPluginDescriptor) {
    List extensions = pRootElement.elements("extension");
    if (extensions != null) {
      for (int i = 0; i < extensions.size(); i++) {
        Element oneExtension = (Element) extensions.get(i);
        String pointId = oneExtension.attributeValue("point");
        List extensionImplementations = oneExtension.elements();
        if (extensionImplementations != null) {
          for (int j = 0; j < extensionImplementations.size(); j++) {
            Element oneImplementation = (Element) extensionImplementations
              .get(j);
            String id = oneImplementation.attributeValue("id");
            String extensionClass = oneImplementation
              .attributeValue("class");
            Extension extension = new Extension(pPluginDescriptor,
                                                pointId, id, extensionClass);
            List list = oneImplementation.attributes();
            for (int k = 0; k < list.size(); k++) {
              Attribute attribute = (Attribute) list.get(k);
              String name = attribute.getName();
              if (name.equals("id") && name.equals("class"))
                continue;
              String value = attribute.getValue();
              extension.addAttribute(name, value);
            }
            pPluginDescriptor.addExtension(extension);
          }
        }
      }
    }
  }
}
