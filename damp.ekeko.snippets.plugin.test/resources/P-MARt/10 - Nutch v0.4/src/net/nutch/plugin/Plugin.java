/*
 * Copyright (c) 2003 The Nutch Organization. All rights reserved. Use subject
 * to the conditions in http://www.nutch.org/LICENSE.txt.
 */
package net.nutch.plugin;
/**
 * A nutch-plugin is an container for a set of custom logic that provide
 * extensions to the nutch core functionality or a other plugin that proides a
 * API for extending. A plugin can provide one or a set of extensions.
 * Extensions are components that can be dynamically installed as a kind of
 * listener to extension points. Extension points are a kind of publisher that
 * provide a API and invoke one or a set of installed extensions.
 * 
 * Each nutch-plugin need to extend the abstract <code>Plugin</code> object.
 * The <code>Plugin</code> object is a kind of singelton that is used as a
 * single point of life cycle managemet of nutch-plugin related functionality.
 * 
 * The <code>Plugin</code> will be startuped and shutdown by the nutch plugin
 * management system.
 * 
 * A possible usecase of the <code>Plugin</code> implementation is to create
 * or close a database connection.
 * 
 * @author joa23
 */
public abstract class Plugin {
  private PluginDescriptor fDescriptor;
  /**
   * Constructor
   *  
   */
  public Plugin(PluginDescriptor pDescriptor) {
    setDescriptor(pDescriptor);
  }
  /**
   * Will be invoked until plugin start up. Since the nutch-plugin system use
   * lazy loading the start up is invoked until the first time a extension is
   * used.
   * 
   * @throws PluginRuntimeException
   *             If the startup was without successs.
   */
  public abstract void startUp() throws PluginRuntimeException;
  /**
   * Shutdown the plugin. This happens until nutch will be stopped.
   * 
   * @throws PluginRuntimeException
   *             if a problems occurs until shutdown the plugin.
   */
  public abstract void shutDown() throws PluginRuntimeException;
  /**
   * Returns the plugin descriptor
   * 
   * @return PluginDescriptor
   */
  public PluginDescriptor getDescriptor() {
    return fDescriptor;
  }
  /**
   * @param descriptor
   *            The descriptor to set
   */
  private void setDescriptor(PluginDescriptor descriptor) {
    fDescriptor = descriptor;
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#finalize()
   */
  protected void finalize() throws Throwable {
    super.finalize();
    shutDown();
  }
}
