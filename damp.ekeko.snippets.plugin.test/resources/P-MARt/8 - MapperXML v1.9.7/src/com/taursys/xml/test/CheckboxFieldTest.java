package com.taursys.xml.test;

import junit.framework.TestCase;
import com.taursys.model.*;
import com.taursys.servlet.*;
import com.taursys.swing.*;
import com.taursys.util.*;
import com.taursys.xml.*;


/* JUnitTest case for class: com.taursys.xml.CheckboxField */
public class CheckboxFieldTest extends TestCase {
  CheckboxField field;

  public CheckboxFieldTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method createDefaultModel(..) */
  public void testCreateDefaultModel() {
    field = new CheckboxField();
    assertEquals("Model", "com.taursys.model.DefaultCheckboxModel",
        field.getModel().getClass().getName());
  }

  /* test for method createDefaultModel(..) */
  public void testCreateDefaultModel2() {
    field = new CheckboxField(DataTypes.TYPE_BOOLEAN);
    assertEquals("Model", "com.taursys.model.DefaultCheckboxModel",
        field.getModel().getClass().getName());
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {CheckboxFieldTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
