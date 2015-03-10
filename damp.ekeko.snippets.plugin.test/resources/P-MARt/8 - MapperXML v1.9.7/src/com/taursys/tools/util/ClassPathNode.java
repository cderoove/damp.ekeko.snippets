/**
 * ClassPathNode - Tree node for ClassPath tree
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
package com.taursys.tools.util;

import javax.swing.tree.*;


/**
 * ClassPathNode is a tree node for a ClassPath tree.
 * @author Marty Phelan
 * @version 1.0
 */
public class ClassPathNode {
  private String displayName = "";
  private String qualifiedName = "";
  private int type = 0;
  public static final int TYPE_ROOT = 0;
  public static final int TYPE_DIR = 1;
  public static final int TYPE_FILE = 2;

  /**
   * Constructs a new ClassPathNode with blank names and TYPE_ROOT
   */
  public ClassPathNode() {
  }

  /**
   * Constructs a new ClassPathNode with all properties given
   */
  public ClassPathNode(String displayName, String qualifiedName, int type) {
    this.displayName = displayName;
    this.qualifiedName = qualifiedName;
    this.type = type;
  }

  /**
   * Get the display name for this ClassPathNode
   * @return the display name for this ClassPathNode
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Set the display name for this ClassPathNode
   * @param newDisplayName display name for this ClassPathNode
   */
  public void setDisplayName(String newDisplayName) {
    displayName = newDisplayName;
  }

  /**
   * Set the fully qualified name for this class or package
   * @param newQualifiedName the fully qualified name for this class or package
   */
  public void setQualifiedName(String newQualifiedName) {
    qualifiedName = newQualifiedName;
  }

  /**
   * Get the fully qualified name for this class or package
   * @return the fully qualified name for this class or package
   */
  public String getQualifiedName() {
    return qualifiedName;
  }

  /**
   * Set the type of node: root, directory, or file
   * Should be one of: <code>TYPE_ROOT</code>, <code>TYPE_DIR</code>, or
   * <code>TYPE_FILE</code>
   * @param newType the type of node: root, directory, or file
   */
  public void setType(int newType) {
    type = newType;
  }

  /**
   * Get the type of node: root, directory, or file
   * Should be one of: <code>TYPE_ROOT</code>, <code>TYPE_DIR</code>, or
   * <code>TYPE_FILE</code>
   * @return the type of node: root, directory, or file
   */
  public int getType() {
    return type;
  }

  /**
   * Returns the displayName as a String representation of this node.
   * @eturn the displayName as a String representation of this node.
   */
  public String toString() {
    return displayName;
  }
}
