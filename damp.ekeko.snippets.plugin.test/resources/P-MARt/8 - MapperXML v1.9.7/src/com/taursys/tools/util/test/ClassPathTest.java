package com.taursys.tools.util.test;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import junit.framework.TestCase;

import com.taursys.tools.util.ClassPath;

/* JUnitTest case for class: com.taursys.tools.util.ClassPath */
public class ClassPathTest extends TestCase {
  private static final String MAPPER_JAR =
      "/home/marty/eclipse/workspace/MapperXML/lib/mapperxml.jar";
  private static final String MAPPER_CLASSES =
      "/home/marty/eclipse/workspace/MapperXML/build/classes";
  private static final String MAPPER_SRC =
      "/home/marty/eclipse/workspace/MapperXML/src";
  private static final String MAPPER_ZIP =
      "/home/marty/eclipse/workspace/MapperXML/build/website.zip";

  public ClassPathTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method getClassTree(..) Using Jar */
  public void testGetClassTree_Jar() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_JAR);
    DefaultMutableTreeNode top = cp.getClassTree();
//    displayNode(top,"");
  }

  /* test for method getClassTree(..) Using directory */
  public void testGetClassTree_Dir() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_CLASSES);
    DefaultMutableTreeNode top = cp.getClassTree();
//    displayNode(top,"");
  }

  /* test for method getPackageTree(..) */
  public void testGetPackageTree() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_JAR);
    DefaultMutableTreeNode top = cp.getPackageTree();
//    displayNode(top,"");
  }

  /* test for method setClassPathString(..) */
  public void testSetClassPathString_SinglePath() {
    ClassPath cp = new ClassPath();
    cp.setClassPathString(MAPPER_JAR);
    assertEquals("size", 1, cp.getPaths().size());
    String result = (String)cp.getPaths().get(0);
    assertEquals("first path", result, MAPPER_JAR);
  }

  /* test for method setClassPathString(..) */
  public void testSetClassPathString_MultiPathNoDup() {
    ClassPath cp = new ClassPath();
    cp.setClassPathString(
        MAPPER_JAR + ";" + MAPPER_CLASSES + ";" + MAPPER_ZIP);
    assertEquals("size", 3, cp.getPaths().size());
    assertEquals("path 0",
      MAPPER_JAR,
      (String)cp.getPaths().get(0));
    assertEquals("path 1",
      MAPPER_CLASSES,
      (String)cp.getPaths().get(1));
    assertEquals("path 2",
      MAPPER_ZIP,
      (String)cp.getPaths().get(2));
  }

  /* test for method setClassPathString(..) */
  public void testSetClassPathString_MultiPathDups() {
    ClassPath cp = new ClassPath();
    cp.setClassPathString(
        MAPPER_JAR + ";" + MAPPER_CLASSES + ";" + MAPPER_JAR + ";" + MAPPER_ZIP);
    assertEquals("size", 3, cp.getPaths().size());
    assertEquals("path 0",
      MAPPER_JAR,
      (String)cp.getPaths().get(0));
    assertEquals("path 1",
      MAPPER_CLASSES,
      (String)cp.getPaths().get(1));
    assertEquals("path 2",
      MAPPER_ZIP,
      (String)cp.getPaths().get(2));
  }

  /* test for method getClassPathString(..) */
  public void testGetClassPathString_Multi() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_JAR);
    cp.addPath(MAPPER_CLASSES);
    cp.addPath(MAPPER_ZIP);
    assertEquals("classPathString",
      MAPPER_JAR + ";" + MAPPER_CLASSES + ";" + MAPPER_ZIP,
      cp.getClassPathString());
  }

  /* test for method addPath(..) */
  public void testAddPath_NonDup() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_JAR);
    assertEquals("size", 1, cp.getPaths().size());
    String result = (String)cp.getPaths().get(0);
    assertEquals("first path", result, MAPPER_JAR);
  }

  /* test for method addPath(..) */
  public void testAddPath_Dup() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_JAR);
    cp.addPath(MAPPER_JAR);
    assertEquals("size", 1, cp.getPaths().size());
    String result = (String)cp.getPaths().get(0);
    assertEquals("first path", result, MAPPER_JAR);
  }

  /* test for method removePath(..) */
  public void testRemovePath() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_JAR);
    cp.removePath(MAPPER_JAR);
    assertEquals("size", 0, cp.getPaths().size());
  }

  /* test for method GetEntriesInPath(..) */
  public void testGetEntriesInPath_SingleClassesDirectory() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_CLASSES);
    assertEquals(" minimum size",
        100,
        Math.min(100, cp.getEntries().size()));
//    printEntries(cp.getEntries());
  }

  /* test for method GetEntriesInPath(..) */
  public void testGetEntriesInPath_SingleJar() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_JAR);
    assertEquals(" minimum size",
        100,
        Math.min(100, cp.getEntries().size()));
//    printEntries(cp.getEntries());
  }

  /* test for method GetEntriesInPath(..) */
  public void testGetEntriesInPath_SingleZip() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_ZIP);
    assertEquals(" minimum size",
        100,
        Math.min(100, cp.getEntries().size()));
//    printEntries(cp.getEntries());
  }

  /* test for method GetEntriesInPath(..) */
  public void testGetEntriesInPath_Mult() {
    ClassPath cp = new ClassPath();
    cp.addPath(MAPPER_CLASSES);
    cp.addPath(MAPPER_JAR);
    cp.addPath(MAPPER_ZIP);
    cp.addPath(MAPPER_SRC);
    assertEquals("minimum size",
        100,
        Math.min(100, cp.getEntries().size()));
  }

  /* test for method GetEntriesInPath(..) */
  public void testGetEntriesInPath_BugFix() {
    TestClassPath cp = new TestClassPath();
    TreeSet entries = new TreeSet();
    entries.add("com/");
    entries.add("com/sun/");
    entries.add("com/sun/");
    entries.add("com/sun/tools/");
    entries.add("com/sun/tools/jdi/");
    entries.add("com/sun/tools/jdi/resources/");
    entries.add("com/sun/tools/jdi/resources/jdi.properties");
    entries.add("com/sun/xml/parser/");
    entries.add("com/sun/xml/parser/AttributeDecl.class");
    entries.add("com/sun/xml/parser/AttributeListEx.class");
    entries.add("com/sun/xml/parser/AttributeListImpl.class");
    entries.add("com/sun/xml/parser/ContentModel.class");
    cp.setInternalEntries(entries);
    cp.addMissingEntries();
    printEntries(cp.getEntries());
    DefaultMutableTreeNode top = cp.getClassTree();
    displayNode(top,"");
  }

  // ========================================================================
  //                          Test Support Methods
  // ========================================================================

  private class TestClassPath extends ClassPath {
    protected void setInternalEntries(TreeSet entries) {
      super.setInternalEntries(entries);
    }
    protected void addMissingEntries() {
      super.addMissingEntries();
    }
  }


  /**
   * Recursively display node starting at given parent for debugging
   */
  private void displayNode(DefaultMutableTreeNode parent, String level) {
    System.out.println(level + parent.getUserObject());
    Enumeration enum_ = parent.children();
    while (enum_.hasMoreElements()) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode)enum_.nextElement();
      displayNode(child, level + "  ");
    }
  }

  /**
   * Print entries on the console for debugging
   */
  private void printEntries(TreeSet entries) {
    Iterator iter = entries.iterator();
    while (iter.hasNext()) {
      System.out.println(iter.next());
    }
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {ClassPathTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
