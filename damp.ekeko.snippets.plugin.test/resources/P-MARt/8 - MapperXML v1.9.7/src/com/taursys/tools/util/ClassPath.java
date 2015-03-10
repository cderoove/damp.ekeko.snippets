/**
 * ClassPath - Represents a classpath for an application
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

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Enumeration;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.zip.ZipFile;
import com.taursys.debug.Debug;

/**
 * ClassPath represents a classpath for an application. It consists of a
 * set of Strings that contain the path to classes and resources needed by
 * the application. The classpath can contain directory references where
 * classes can be found and jar(or zip) file names which contain classes.
 * <p>
 * This class provides a number of utility methods for classpath related
 * activities.
 * @author Marty Phelan
 * @version 1.0
 */
public class ClassPath {
  private ArrayList paths = new ArrayList();
  private TreeSet entries;
  private DefaultMutableTreeNode classTree = null;
  private DefaultMutableTreeNode packageTree = null;

  /**
   * Constructs a new empty ClassPath
   */
  public ClassPath() {
  }

  /**
   * Constructs a new ClassPath based on the given String representation.
   */
  public ClassPath(String classPath) {
    setClassPathString(classPath);
  }

  /**
   * Set this ClassPath from the given String representation. The given
   * String classPath will be broken into individual paths. They must be
   * separated by a semicolon.
   * @param classPath is a set of paths separated by semicolons.
   */
  public void setClassPathString(String classPath) {
    StringTokenizer tokens = new StringTokenizer(classPath, ";");
    while (tokens.hasMoreTokens()) {
      addPath(tokens.nextToken());
    }
  }

  /**
   * Get the String representation of this classpath. The classpath will
   * consist of all the individiual paths concatenated and separated by
   * semicolons.
   * @return the String representation of this classpath.
   */
  public String getClassPathString() {
    String path = null;
    Iterator iter = paths.iterator();
    while (iter.hasNext()) {
      if (path == null)
        path = "";
      else
        path += ";";
      path += (String)iter.next();
    }
    return path;
  }

  /**
   * Add a path to this classpath. If the given path already exists in this
   * classpath, no action will be taken.
   * @param path to add to this classpath.
   */
  public void addPath(String path) {
    if (!paths.contains(path))
      paths.add(path);
    resetEntries();
  }

  /**
   * Remove a path from this classpath. If the given path is not found
   * in this classpath, no action will be taken.
   * @param path to remove from this classpath.
   */
  public void removePath(String path) {
    paths.remove(path);
    resetEntries();
  }

  /**
   * Set the internal ArrayList of paths for this ClassPath.
   * @param newPaths the internal ArrayList of paths for this ClassPath.
   */
  public void setPaths(ArrayList newPaths) {
    paths = newPaths;
    resetEntries();
  }

  /**
   * Get a copy of the paths for this ClassPath.
   * @return a copy of the paths for this ClassPath.
   */
  public ArrayList getPaths() {
    return new ArrayList(paths);
  }

  /**
   * Get a Tree consisting of all classes based on this ClassPath.
   */
  public DefaultMutableTreeNode getClassTree() {
    if (classTree == null)
      classTree = getTree(new ClassFilter(), true);
    return classTree;
  }

  /**
   * Get a Tree consisting of all packages based on this ClassPath.
   */
  public DefaultMutableTreeNode getPackageTree() {
    if (packageTree == null)
      packageTree = getTree(new PackageFilter(), false);
    return packageTree;
  }

  /**
   * Get the internal set of entries -- for testing purposes only.
   * This is a sorted unique set of all entries obtained from resources
   * in the paths.
   * @return a TreeSet of internal entries
   */
  public TreeSet getEntries() {
    // Make sure entries are set
    if (entries == null)
      getEntriesInPath();
    return entries;
  }

  /**
   * Reset the collection of paths to empty. Also resets internal cached entries.
   */
  public void resetPaths() {
    paths.clear();
    resetEntries();
  }

  // ==================================================================
  //                   Tree List Methods
  // ==================================================================

  /**
   * FileFilter for classes only (excluding inner classes)
   */
  protected class ClassFilter implements FileFilter {
    public boolean accept(File file) {
      return file.getName().toLowerCase().endsWith(".class")
          && file.getName().indexOf('$') == -1;
    }
  }

  /**
   * FileFilter for packages only - no files
   */
  protected class PackageFilter implements FileFilter {
    public boolean accept(File file) {
      return false;
    }
  }

  /**
   * Get the String representation of given path
   */
  protected String getTreePath(DefaultMutableTreeNode node) {
    String path = "";
    while (node.getParent() != null) {
      path = node.getUserObject() + "/" + path;
      node = (DefaultMutableTreeNode)node.getParent();
    }
    return path;
  }

  private String removeFileType(String fileName) {
    int pos = fileName.lastIndexOf(".");
    if (pos != -1) {
      return fileName.substring(0, pos);
    } else {
      return fileName;
    }
  }

  /**
   * Get a Tree consisting of all files matching given filter.
   * @return DefaultMutableTreeNode root of tree containing directories and files.
   */
  public DefaultMutableTreeNode getTree(FileFilter filter, boolean removeFileType) {
    DefaultMutableTreeNode top = new DefaultMutableTreeNode(
        new ClassPathNode());
    DefaultMutableTreeNode parentNode = top;
    DefaultMutableTreeNode child = null;
    int level = 1;
    int parentLength = 0;
    // Make sure entries are set
    if (entries == null)
      getEntriesInPath();
    Iterator iter = entries.iterator();
    while (iter.hasNext()) {
      String item = (String)iter.next();
      String qualifiedName = item.replace('/', '.');
      StringTokenizer tokens = new StringTokenizer(item, "/");
      int tokenCount = tokens.countTokens();
      // check if we have gone up one or more levels
      if (tokenCount < level) {
        String parentPath = null;
        do {
          level--;
          parentNode = (DefaultMutableTreeNode)parentNode.getParent();
          parentPath = getTreePath(parentNode);
          parentLength = parentPath.length();
        } while (!item.startsWith(parentPath));
      // else check if we have gone down a level
      } else if (tokenCount > level) {
        level++;
        parentNode = child;
        parentLength = getTreePath(parentNode).length();
      }
      // Check whether to create directory node
      if (item.endsWith("/")) {
        if (parentLength < 0) {
          // error condition
          Debug.debug("ClassPath.getTree Index out of range: item="
              + item + " parentLength=" + parentLength + " level=" + level);
        } else {
          item = item.substring(parentLength, item.length() - 1);
        }
        child = new DefaultMutableTreeNode(
            new ClassPathNode(item, qualifiedName, ClassPathNode.TYPE_DIR));
        parentNode.add(child);
      // Else if it is an acceptable file, create file node
      } else if (filter.accept(new File(item))) {
        if (parentLength < 0) {
          // error condition
          Debug.debug("ClassPath.getTree Index out of range: item="
              + item + " parentLength=" + parentLength + " level=" + level);
        } else {
          item = item.substring(parentLength);
        }
        if (removeFileType) {
          item = removeFileType(item);
          qualifiedName = removeFileType(qualifiedName);
        }
        child = new DefaultMutableTreeNode(
            new ClassPathNode(item, qualifiedName, ClassPathNode.TYPE_FILE));
        parentNode.add(child);
      }
    }
    return top;
  }

  // ==================================================================
  //                   Entries List Methods
  // ==================================================================

  /**
   * Reset the cached entries and trees, forcing a rebuild from the current paths.
   * The internal entries are purged. They will be rebuilt by the
   * next operation that needs them.
   */
  protected void resetEntries() {
    entries = null;
    classTree = null;
    packageTree = null;
  }

  /**
   * Add missing entries to entries TreeSet. If a zip or jar file is not
   * correct, initial paths may be missing from the entries table. An example
   * would be: <code>com/sun/tools/jdi/resources/jdi.properties</code> followed
   * by <code>com/sun/xml/parser/</code>.  It is missing the entry
   * <code>com/sun/xml/</code>.
   */
  protected void addMissingEntries() {
    ArrayList missingEntries = new ArrayList();
    // for each entry, ensure that its parent directories are present
    Iterator iter = entries.iterator();
    while (iter.hasNext()) {
      String item = (String)iter.next();
      int lastSlash = item.lastIndexOf('/');
      // only process items which contain slashes
      if (lastSlash != -1) {
        StringTokenizer tokens = new StringTokenizer(item.substring(0, lastSlash),"/");
        // ensure that each directory is present else add it
        String dir = "";
        while (tokens.hasMoreTokens()) {
          dir += tokens.nextToken() + "/";
          if (!entries.contains(dir)) {
            missingEntries.add(dir);
          }
        }
      }
    }
    entries.addAll(missingEntries);
  }

  /**
   * Set the internal entries TreeSet for testing purposes only.
   * @param entries the internal entries TreeSet for testing purposes only.
   */
  protected void setInternalEntries(TreeSet entries) {
    this.entries = entries;
  }

  protected void getEntriesInPath() {
    entries = new TreeSet();
    Iterator iter = paths.iterator();
    while (iter.hasNext()) {
      String path = (String)iter.next();
      File file = new File(path);
      String fileName = file.getName().toLowerCase();
      // process as archive or directory
      if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
        processArchive(file);
      } else {
        processDirectory(file.getAbsolutePath().length()+1, file);
      }
    }
    addMissingEntries();
  }

  private void processDirectory(int prefix, File dir) {
    if(!dir.isDirectory() )
      return;
    // store directory only if not root
    if (dir.getAbsolutePath().length() > prefix) {
      String relative = dir.getAbsolutePath().substring(prefix);
      if (!relative.endsWith("/"))
        relative += "/";
      entries.add(relative);
    }
    // process entries
    File[] sub = dir.listFiles();
    for( int i=0; i<sub.length; ++i ) {
      processDirectory(prefix, sub[i]);
      processFile(prefix, sub[i]);
    }
  }

  private void processFile(int prefix, File f) {
    if( !f.isFile() )
      return;
    String relative = f.getAbsolutePath().substring(prefix);
    entries.add(relative);
  }

  private void processArchive(File archive) {
    try {
      ZipFile zf = new ZipFile(archive);
      Enumeration e = zf.entries();
      while( e.hasMoreElements() )
        entries.add(e.nextElement().toString());
      zf.close();
    } catch(IOException ex) {
      Debug.error("Error during process archive", ex);
    }
  }
}
