/*
 * Copyright (c) 2003 The Nutch Organization. All rights reserved. Use subject
 * to the conditions in http://www.nutch.org/LICENSE.txt.
 */
package net.nutch.plugin;
import java.util.ArrayList;
/**
 * The <code>ExtensionPoint</code> provide meta information of a extension
 * point.
 * 
 * @author joa23
 */
public class ExtensionPoint {
  private String ftId;
  private String fName;
  private String fSchema;
  private ArrayList fExtensions;
  /**
   * Constructor
   * 
   * @param id
   *            unique extension point Id
   * @param name
   *            name of the extension poin
   * @param schema
   *            xml schema of the extension point
   */
  public ExtensionPoint(String pId, String pName, String pSchema) {
    setId(pId);
    setName(pName);
    setSchema(pSchema);
    fExtensions = new ArrayList();
  }
  /**
   * Returns the unique id of the extension point.
   * 
   * @return String
   */
  public String getId() {
    return ftId;
  }
  /**
   * Returns the name of the extension point.
   * 
   * @return String
   */
  public String getName() {
    return fName;
  }
  /**
   * Returns a path to the xml schema of a extension point.
   * 
   * @return String
   */
  public String getSchema() {
    return fSchema;
  }
  /**
   * Sets the extensionPointId.
   * 
   * @param extension point id
   *            The extensionPointId to set
   */
  private void setId(String pId) {
    ftId = pId;
  }
  /**
   * Sets the extension point name.
   * 
   * @param extensionPointName
   *            The extensionPointName to set
   */
  private void setName(String pName) {
    fName = pName;
  }
  /**
   * Sets the schema.
   * 
   * @param schema
   *            The schema to set
   */
  private void setSchema(String pSchema) {
    fSchema = pSchema;
  }
  /**
   * Install a  coresponding extension to this extension point.
   * 
   * @param extension
   */
  public void addExtension(Extension extension) {
    fExtensions.add(extension);
  }
  /**
   * Returns a array of extensions that lsiten to this extension point
   * 
   * @return Extension[]
   */
  public Extension[] getExtentens() {
    return (Extension[]) fExtensions.toArray(new Extension[fExtensions
                                                           .size()]);
  }
}
