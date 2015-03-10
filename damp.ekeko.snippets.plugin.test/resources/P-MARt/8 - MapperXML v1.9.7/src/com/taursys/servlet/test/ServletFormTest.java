package com.taursys.servlet.test;

import junit.framework.TestCase;
import com.taursys.servlet.*;
import java.util.*;

/* JUnitTest case for class: com.taursys.servlet.ServletForm */
public class ServletFormTest extends TestCase {

  public ServletFormTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method createParameterMap(..) */
  public void testCreateParameterMap() {
    ServletForm form = new ServletForm();
    TestHttpServletRequest request = new TestHttpServletRequest();
    request.addParameter("name", "Marty");
    request.addParameter("colors", new String[] {"red","yellow", "green"});
    form.setRequest(request);
    Map map = form.createParameterMap();
    String[] values = (String[])map.get("name");
    assertEquals("name", "Marty", values[0]);
    values = (String[])map.get("colors");
    assertEquals("color 1", "red", values[0]);
    assertEquals("color 2", "yellow", values[1]);
    assertEquals("color 3", "green", values[2]);
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {ServletFormTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
