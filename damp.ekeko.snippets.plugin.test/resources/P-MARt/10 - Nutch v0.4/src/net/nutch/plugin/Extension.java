/*
 * Copyright (c) 2003 The Nutch Organization. All rights reserved. Use subject
 * to the conditions in http://www.nutch.org/LICENSE.txt.
 */
package net.nutch.plugin;
import java.util.HashMap;
/**
 * A <code>Extension</code> is a kind of listener descriptor that will be
 * installed on a concret <code>ExtensionPoint</code> that act as kind of
 * Publisher.
 * 
 * @author joa23
 */
public class Extension {
  private PluginDescriptor fDescriptor;
  private String fId;
  private String fTargetPoint;
  private String fClazz;
  private HashMap fAttributes;
  /**
   * @param pDescriptor
   *            a plugin descriptor
   * @param pExtensionPoint
   *            an extension porin
   * @param pId
   *            an unique id of the plugin
   */
  public Extension(PluginDescriptor pDescriptor, String pExtensionPoint,
                   String pId, String pExtensionClass) {
    fAttributes = new HashMap();
    setDiscriptor(pDescriptor);
    setExtensionPoint(pExtensionPoint);
    setId(pId);
    setClazz(pExtensionClass);
  }
  /**
   * @param point
   */
  private void setExtensionPoint(String point) {
    fTargetPoint = point;
  }
  /**
   * Returns a attribute value, that is setuped in the manifest file and is
   * definied by the extension point xml schema.
   * 
   * @param pKey
   *            a key
   * @return String a value
   */
  public String getAttribute(String pKey) {
    return (String) fAttributes.get(pKey);
  }
  /**
   * Returns the full class name of the extension point implementation
   * 
   * @return String
   */
  public String getClazz() {
    return fClazz;
  }
  /**
   * Return the unique id of the extension.
   * 
   * @return String
   */
  public String getId() {
    return fId;
  }
  /**
   * Adds a attribute and is only used until model creation at plugin system
   * start up.
   * 
   * @param pKey
   *            a key
   * @param pValue
   *            a value
   */
  public void addAttribute(String pKey, String pValue) {
    fAttributes.put(pKey, pValue);
  }
  /**
   * Sets the Class that implement the concret extension and is only used
   * until model creation at system start up.
   * 
   * @param extensionClazz
   *            The extensionClazz to set
   */
  public void setClazz(String extensionClazz) {
    fClazz = extensionClazz;
  }
  /**
   * Sets the unique extension Id and is only used until model creation at
   * system start up.
   * 
   * @param extensionID
   *            The extensionID to set
   */
  public void setId(String extensionID) {
    fId = extensionID;
  }
  /**
   * Returns the Id of the extension point, that is implemented by this
   * extension.
   */
  public String getTargetPoint() {
    return fTargetPoint;
  }
  /**
   * Return an instance of the extension implementatio. Before we create a
   * extension instance we startup the plugin if it is not already done. The
   * plugin instance and the extension instance use the same
   * <code>PluginClassLoader</code>. Each Plugin use its own classloader.
   * The PluginClassLoader knows only own <i>Plugin runtime libraries </i>
   * setuped in the plugin manifest file and exported libraries of the
   * depenedend plugins.
   * 
   * @return Object An instance of the extension implementation
   */
  public Object getExtensionInstance() throws PluginRuntimeException {
    try {
      PluginClassLoader loader = fDescriptor.getClassLoader();
      Class extensionClazz = loader.loadClass(getClazz());
      // lazy loading of Plugin in case there is no instance of the plugin
      // already.
      PluginRepository.getInstance().getPluginInstance(getDiscriptor());
      Object object = extensionClazz.newInstance();
      return object;
    } catch (ClassNotFoundException e) {
      throw new PluginRuntimeException(e);
    } catch (InstantiationException e) {
      throw new PluginRuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new PluginRuntimeException(e);
    }
  }
  /**
   * return the plugin descriptor.
   * 
   * @return PluginDescriptor
   */
  public PluginDescriptor getDiscriptor() {
    return fDescriptor;
  }
  /**
   * Sets the plugin descriptor and is only used until model creation at system
   * start up.
   * 
   * @return PluginDescriptor
   */
  public void setDiscriptor(PluginDescriptor pDescriptor) {
    fDescriptor = pDescriptor;
  }
}
