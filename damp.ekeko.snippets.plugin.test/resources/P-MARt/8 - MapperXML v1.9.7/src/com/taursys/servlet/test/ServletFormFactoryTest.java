/**
 * JUnitTest case
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
package com.taursys.servlet.test;

import junit.framework.TestCase;
import com.taursys.html.*;
import com.taursys.model.*;
import com.taursys.servlet.*;
import com.taursys.swing.*;
import com.taursys.util.*;
import com.taursys.xml.*;
import java.text.*;
import java.util.*;
import java.math.*;


/* JUnitTest case for class: com.taursys.servlet.ServletFormFactory */
public class ServletFormFactoryTest extends TestCase {

  public ServletFormFactoryTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method createServletForm(..) */
  public void testCreateServletForm() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    ServletForm form = factory.createServletForm(null);
    assertEquals("form class", "com.taursys.servlet.DefaultMessageForm",
        form.getClass().getName());
  }

  /* test for method recycle(..) */
  public void testRecycleSingle() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    ServletForm form = factory.createServletForm(null);
    factory.recycle(form);
    ServletForm form2 = factory.createServletForm(null);
    assertEquals("form instance", form,
        form2);
  }

  /* test for method recycle(..) */
  public void testRecycleMultiple() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    ServletForm form1 = factory.createServletForm(null);
    ServletForm form2 = factory.createServletForm(null);
    factory.recycle(form1);
    factory.recycle(form2);
    ServletForm form3 = factory.createServletForm(null);
    ServletForm form4 = factory.createServletForm(null);
    assertEquals("form instance", form1,
        form3);
    assertEquals("form instance", form2,
        form4);
  }

  /* test for method parseClassName(..) */
  public void testParseClassNameDefault() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    assertEquals("form name", "com.taursys.servlet.DefaultMessageForm",
        factory.parseClassName(null));
  }

  /* test for method parseClassName(..) */
  public void testParseClassNameRoot() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    assertEquals("form name", "com.taursys.servlet.DefaultMessageForm",
        factory.parseClassName("/"));
  }

  /* test for method parseClassName(..) */
  public void testParseClassNameRootSpecifiedDefaultForm() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    factory.setDefaultFormName("x.x.Me");
    assertEquals("form name", "x.x.Me",
        factory.parseClassName("/"));
  }

  /* test for method parseClassName(..) */
  public void testParseClassNameRootFormNameShort() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    factory.addPackage("/","x.x");
    assertEquals("form name", "x.x.M",
        factory.parseClassName("/M.sf"));
  }

  /* test for method parseClassName(..) */
  public void testParseClassNameRootFormNameLong() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    factory.addPackage("/","x.x");
    assertEquals("form name", "x.x.EditForm",
        factory.parseClassName("/EditForm.sf"));
  }

  /* test for method parseClassName(..) */
  public void testParseClassNamePathFormName() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    factory.addPackage("/admin","x.x");
    assertEquals("form name", "x.x.EditForm",
        factory.parseClassName("/admin/EditForm.sf"));
  }

  /* test for method parseClassName(..) */
  public void testParseClassNamePathFormNameAltSuffix() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    factory.addPackage("/admin","x.x");
    factory.setServletFormSuffix(".form");
    assertEquals("form name", "x.x.EditForm",
        factory.parseClassName("/admin/EditForm.form"));
  }

  /* test for method parseClassName(..) */
  public void testParseClassNamePathFormNameNoSuffix() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    factory.addPackage("/admin","x.x");
    factory.setServletFormSuffix("");
    assertEquals("form name", "x.x.EditForm",
        factory.parseClassName("/admin/EditForm"));
  }

  /* test for method addPackage(..) */
  public void testAddPackageNoEndingSlash() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    factory.addPackage("/admin","x.x");
    factory.setServletFormSuffix("");
    assertEquals("form name", "x.x.EditForm",
        factory.parseClassName("/admin/EditForm"));
  }

  /* test for method addPackage(..) */
  public void testAddPackageWithEndingSlash() throws Exception {
    ServletFormFactory factory = new ServletFormFactory();
    factory.addPackage("/admin/","x.x");
    factory.setServletFormSuffix("");
    assertEquals("form name", "x.x.EditForm",
        factory.parseClassName("/admin/EditForm"));
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {ServletFormFactoryTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
