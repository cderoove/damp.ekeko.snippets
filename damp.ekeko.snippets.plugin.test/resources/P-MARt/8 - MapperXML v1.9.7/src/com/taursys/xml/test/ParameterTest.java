package com.taursys.xml.test;

import junit.framework.TestCase;
import java.text.*;
import java.util.*;
import com.taursys.html.*;
import com.taursys.model.*;
import com.taursys.model.test.*;
import com.taursys.servlet.*;
import com.taursys.swing.*;
import com.taursys.util.*;
import com.taursys.xml.*;

/* JUnitTest case for class: com.taursys.xml.Parameter */
public class ParameterTest extends TestCase {

  public ParameterTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method getText(..) */
  public void testGetTextStringNull() throws Exception {
    Parameter parameter = new Parameter();
    assertEquals("text value", "", parameter.getText());
  }

  /* test for method setText(..) */
  public void testSetTextStringEmpty() throws Exception {
    Parameter parameter = new Parameter();
    parameter.setText("");
    assertEquals("text value", "", parameter.getText());
  }

  /* test for method setText(..) */
  public void testSetTextStringNull() throws Exception {
    Parameter parameter = new Parameter();
    parameter.setText(null);
    assertEquals("text value", "", parameter.getText());
  }

  /* test for method setText(..) */
  public void testSetTextString() throws Exception {
    Parameter parameter = new Parameter();
    parameter.setText("Marty");
    assertEquals("text value", "Marty", parameter.getText());
  }

  /* test for method setText(..) */
  public void testSetTextDate() throws Exception {
    Parameter parameter = new Parameter(DataTypes.TYPE_DATE);
    parameter.setFormat(SimpleDateFormat.getDateInstance(DateFormat.SHORT));
    parameter.setText("01/31/2002");
    assertEquals("text value", "1/31/02", parameter.getText());
  }

  /* test for method getValue(..) */
  public void testGetValueStringNull() throws Exception {
    Parameter parameter = new Parameter();
    assertNull("object value", parameter.getValue());
  }

  /* test for method getValue(..) */
  public void testGetValueString() throws Exception {
    Parameter parameter = new Parameter();
    parameter.setText("Marty");
    assertEquals("object value", "Marty", parameter.getValue());
  }

  /* test for method getValue(..) */
  public void testGetValueDate() throws Exception {
    Parameter parameter = new Parameter(DataTypes.TYPE_DATE);
    DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
    parameter.setFormat(df);
    parameter.setText("01/31/2002");
    assertEquals("object value", df.parse("01/31/2002"), parameter.getValue());
  }

  /* test for method setValue(..) */
  public void testSetValueStringNull() throws Exception {
    Parameter parameter = new Parameter();
    parameter.setValue(null);
    assertNull("object value", parameter.getValue());
  }

  /* test for method getValue(..) */
  public void testSetValueString() throws Exception {
    Parameter parameter = new Parameter();
    parameter.setValue("Marty");
    assertEquals("object value", "Marty", parameter.getValue());
  }

  /* test for method getValue(..) */
  public void testSetValueDate() throws Exception {
    Parameter parameter = new Parameter(DataTypes.TYPE_DATE);
    DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
    parameter.setValue(df.parse("01/31/2002"));
    assertEquals("object value", df.parse("01/31/2002"), parameter.getValue());
  }

  /* test for method getValue(..) */
  public void testGetValueVODate() throws Exception {
    Parameter parameter = new Parameter();
    JeanLucPicard jean = new JeanLucPicard();
    VOValueHolder holder = new VOValueHolder();
    holder.setValueObject(jean);
    parameter.setValueHolder(holder);
    parameter.setPropertyName("birthdate");
    assertEquals("object value", jean.getBirthdate(), parameter.getValue());
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {ParameterTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
