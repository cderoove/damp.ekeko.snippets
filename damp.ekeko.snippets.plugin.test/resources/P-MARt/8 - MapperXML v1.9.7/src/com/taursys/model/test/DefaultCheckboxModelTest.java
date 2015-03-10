/**
 * DefaultCheckboxModelTest - A JUnit Test
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
package com.taursys.model.test;

import junit.framework.TestCase;
import com.taursys.model.*;
import java.util.*;
import java.math.BigDecimal;

/* JUnitTest case for class: com.taursys.model.DefaultCheckboxModel */
public class DefaultCheckboxModelTest extends TestCase {
  DefaultCheckboxModel model;
  TestValueObject vo = new TestValueObject();
  VOValueHolder holder = new VOValueHolder();

  public DefaultCheckboxModelTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
    holder.setValueObject(vo);
  }

  /* setUp method for Variant String Y/N test cases */
  protected void setUpStringYN() {
    model = new DefaultCheckboxModel(com.taursys.util.DataTypes.TYPE_STRING);
    model.setSelectedValue("Y");
    model.setUnselectedValue("N");
  }

  /* setUp method for Variant Boolean test cases */
  protected void setUpBoolean() {
    model = new DefaultCheckboxModel(com.taursys.util.DataTypes.TYPE_BOOLEAN);
    model.setSelectedValue("true");
    model.setUnselectedValue("false");
  }

  /* setUp method for Variant Boolean test cases */
  protected void setUpVOboolean() {
    model = new DefaultCheckboxModel();
    model.setValueHolder(holder);
    model.setPropertyName("active");
    model.setSelectedValue("true");
    model.setUnselectedValue("false");
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  // ***********************************************************************
  //              Initial state and null value tests
  // ***********************************************************************

  /**
   * Test the initial state
   */
  public void testInitialState() throws Exception {
    model = new DefaultCheckboxModel();
    assertNull("Initial value sh/b null",
        model.getValueHolder().getPropertyValue("value"));
  }

  /**
   * Test setting selected to false - initial state - value sh/b null
   */
  public void testSetSelectedFalseIntialState() throws Exception {
    model = new DefaultCheckboxModel();
    model.setSelected(false);
    assertNull("Value sh/b null",
        model.getValueHolder().getPropertyValue("value"));
  }

  /**
   * Test setting text value to blank - initial state - value sh/b null
   */
  public void testSetTextBlankIntialState() throws Exception {
    model = new DefaultCheckboxModel();
    model.setText("");
    assertNull("Value sh/b null",
        model.getValueHolder().getPropertyValue("value"));
  }

  /**
   * Test setting selected to true - initial state - value sh/b "true"
   */
  public void testSetSelectedTrueIntialState() throws Exception {
    model = new DefaultCheckboxModel();
    model.setSelected(true);
    assertEquals("Value", "true",
        model.getValueHolder().getPropertyValue("value"));
  }

  // ***********************************************************************
  //              VO boolean Tests
  // ***********************************************************************

  /* test for method getText(..) */
  public void testGetTextVOboolean_true() throws Exception {
    setUpVOboolean();
    vo.setActive(true);
    assertEquals("true", model.getText());
  }

  /* test for method getText(..) */
  public void testGetTextVOboolean_false() throws Exception {
    setUpVOboolean();
    vo.setActive(false);
    assertEquals("false", model.getText());
  }

  /* test for method setText(..) */
  public void testSetTextVOboolean_true() throws Exception {
    setUpVOboolean();
    model.setText("true");
    assertEquals("true", model.getText());
  }

  // ***********************************************************************
  //              Variant Boolean Tests
  // ***********************************************************************

  /* test for method getText(..) */
  public void testGetTextBoolean_true() throws Exception {
    setUpBoolean();
    model.getValueHolder().setPropertyValue("", Boolean.TRUE);
    assertEquals("true", model.getText());
  }

  /* test for method getText(..) */
  public void testGetTextBoolean_false() throws Exception {
    setUpBoolean();
    model.getValueHolder().setPropertyValue("", Boolean.FALSE);
    assertEquals("false", model.getText());
  }

  /* test for method setText(..) */
  public void testSetTextBoolean_true() throws Exception {
    setUpBoolean();
    model.setText("true");
    assertEquals("true", model.getText());
  }

  // ***********************************************************************
  //              Variant String Tests
  // ***********************************************************************

  /* test for method getText(..) */
  public void testGetTextStringYN_Null() throws Exception {
    setUpStringYN();
    assertEquals("N", model.getText());
  }

  /* test for method getText(..) */
  public void testGetTextStringYN_NN() throws Exception {
    setUpStringYN();
    model.getValueHolder().setPropertyValue("", "N");
    assertEquals("N", model.getText());
  }

  /* test for method getText(..) */
  public void testGetTextStringYN_NY() throws Exception {
    setUpStringYN();
    model.getValueHolder().setPropertyValue("", "Y");
    assertEquals("Y", model.getText());
  }

  /* test for method setText(..) */
  public void testGetTextStringYN_NX() throws Exception {
    setUpStringYN();
    model.getValueHolder().setPropertyValue("", "X");
    try {
      model.getText();
      fail("Expected UnknownStateValueException");
    } catch (UnknownStateValueException ex) {
    }
  }

  /* test for method setText(..) */
  public void testSetTextStringYN_Y() throws Exception {
    setUpStringYN();
    model.setText("Y");
    assertEquals("Y", model.getText());
  }

  /* test for method setText(..) */
  public void testSetTextStringYN_N() throws Exception {
    setUpStringYN();
    model.setText("N");
    assertEquals("N", model.getText());
  }

  /* test for method setText(..) */
  public void testSetTextStringYN_Blank() throws Exception {
    setUpStringYN();
    model.setText("");
    assertEquals("N", model.getText());
  }

  /* test for method setText(..) */
  public void testSetTextStringYN_Null() throws Exception {
    setUpStringYN();
    model.setText(null);
    assertEquals("N", model.getText());
  }

  /* test for method setText(..) */
  public void testSetTextStringYN_NX() throws Exception {
    setUpStringYN();
    try {
      model.setText("X");
      fail("Expected UnknownStateValueException");
    } catch (UnknownStateValueException ex) {
    }
  }

  /* test for method setSelected(..) */
  public void testSetSelectedStringYN_Y() throws Exception {
    setUpStringYN();
    model.setSelected(true);
    assertEquals(true, model.isSelected());
  }

  /* test for method isSelected(..) */
  public void testIsSelectedStringYN_Y() throws Exception {
    setUpStringYN();
    model.getValueHolder().setPropertyValue("", "Y");
    assertEquals(true, model.isSelected());
  }

  /* test for method isSelected(..) */
  public void testIsSelectedStringYN_N() throws Exception {
    setUpStringYN();
    model.getValueHolder().setPropertyValue("", "N");
    assertEquals(false, model.isSelected());
  }

  /* test for method isSelected(..) */
  public void testIsSelectedStringYN_Blank() throws Exception {
    setUpStringYN();
    model.getValueHolder().setPropertyValue("", "");
    assertEquals(false, model.isSelected());
  }

  /* test for method isSelected(..) */
  public void testIsSelectedStringYN_Null() throws Exception {
    setUpStringYN();
    model.getValueHolder().setPropertyValue("", null);
    assertEquals(false, model.isSelected());
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {DefaultCheckboxModelTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
