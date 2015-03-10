package com.taursys.xml.test;

import junit.framework.TestCase;
import com.taursys.html.*;
import com.taursys.model.*;
import com.taursys.model.test.*;
import com.taursys.servlet.*;
import com.taursys.swing.*;
import com.taursys.util.*;
import com.taursys.xml.*;
import java.text.*;
import java.util.*;
import java.math.*;


/* JUnitTest case for class: com.taursys.xml.SelectField */
public class SelectFieldTest extends TestCase {

  public SelectFieldTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method getText(..) */
  public void testGetTextDefaults() throws Exception {
    SelectField select = new SelectField();
    assertEquals("text value", "--none--", select.getText());
  }

  /* test for method setText(..) */
  public void testSetTextNullDefaults() throws Exception {
    SelectField select = new SelectField();
    select.setText(null);
    assertEquals("text value", "--none--", select.getText());
  }

  /* test for method setText(..) */
  public void testSetTextNoneDefaults() throws Exception {
    SelectField select = new SelectField();
    select.setText("--none--");
    assertEquals("text value", "--none--", select.getText());
  }

  /* test for method setText(..) */
  public void testSetTextSecondStringArray() throws Exception {
    String[] array = new String[] {"First","Second","Third"};
    SelectField select = new SelectField(array);
    select.setText("Second");
    assertEquals("text value", "Second", select.getText());
  }

  /* test for method setText(..) */
  public void testSetTextSecondPersonArray() throws Exception {
    TestValueObject[] array = new TestValueObject[] {
        new JeanLucPicard(),
        new BeverlyCrusher(),
        new WillRiker(),
        };
    SelectField select = new SelectField(array);
    select.setText("Beverly Crusher");
    assertEquals("text value", "Beverly Crusher", select.getText());
  }

  /* test for method setText(..) */
  public void testSetTextSupervisor() throws Exception {
    TestValueObject[] array = new TestValueObject[] {
        new JeanLucPicard(),
        new BeverlyCrusher(),
        new WillRiker(),
        };
    SelectField select = new SelectField(array);
    TestValueObject anon = new TestValueObject();
    VOValueHolder holder = new VOValueHolder();
    holder.setValueObject(anon);
    select.setValueHolder(holder);
    select.setPropertyName("supervisor");

    select.setText("Beverly Crusher");

    assertEquals("text value", "Beverly Crusher", anon.getSupervisor().toString());
  }

  /* test for method getSelection(..) */
  public void testGetSelectionDefaults() throws Exception {
    SelectField select = new SelectField();
    assertNull("selection value", select.getSelection());
  }

  /* test for method setSelection(..) */
  public void testSetSelectionNullDefaults() throws Exception {
    SelectField select = new SelectField();
    select.setSelection(null);
    assertNull("selection value", select.getSelection());
  }

  /* test for method setText(..) */
  public void testSetSelectionSupervisor() throws Exception {
    TestValueObject[] array = new TestValueObject[] {
        new JeanLucPicard(),
        new BeverlyCrusher(),
        new WillRiker(),
        };
    SelectField select = new SelectField(array);
    TestValueObject anon = new TestValueObject();
    VOValueHolder holder = new VOValueHolder();
    holder.setValueObject(anon);
    select.setValueHolder(holder);
    select.setPropertyName("supervisor");

    select.setSelection(array[1]);

    assertEquals("text value", "Beverly Crusher", anon.getSupervisor().toString());
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {SelectFieldTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
