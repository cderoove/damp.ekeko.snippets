/**
 * DefaultTextModelTest - A JUnit Test
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
import com.taursys.model.DefaultTextModel;
import com.taursys.model.VOValueHolder;
import java.util.Date;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.beans.IntrospectionException;
import com.taursys.model.ModelPropertyAccessorException;
import com.taursys.util.DataTypes;


/* JUnitTest case for class: com.taursys.model.DefaultTextModel */
public class DefaultTextModelTest extends TestCase {
  private TestValueObject vo;
  private DefaultTextModel model;
  private VOValueHolder holder;

  public DefaultTextModelTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
    vo = new TestValueObject();
    model = new DefaultTextModel();
    holder = new VOValueHolder();
    // connect objects
    holder.setValueObject(vo);
    model.setValueHolder(holder);
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  // ***********************************************************************
  // *            getText() TESTS - no format
  // ***********************************************************************

  /** test for method getText(..) with String no format */
  public void testGetTextStringNoFormat() throws Exception {
    String value = "John Smith";
    vo.setFullName(value);
    model.setPropertyName("fullName");
    assertEquals("fullName", value, model.getText());
  }

  /** test for method getText(..) with Date no format */
  public void testGetTextDateNoFormat() throws Exception {
    Date bd = new SimpleDateFormat("MM-dd-yyyy").parse("05-05-1955");
    vo.setBirthdate(bd);
    model.setPropertyName("birthdate");
    assertEquals("birthdate", DataTypes.format(DataTypes.TYPE_DATE, bd),
        model.getText());
  }

  /** test for method getText(..) with BigDecimal no format */
  public void testGetTextBigDecimalNoFormat() throws Exception {
    BigDecimal value = new BigDecimal("12345.67");
    vo.setSalary(value);
    model.setPropertyName("salary");
    assertEquals("salary", value.toString(), model.getText());
  }

  /** test for method getText(..) with int no format */
  public void testGetTextIntNoFormat() throws Exception {
    int value = 4;
    vo.setDependents(value);
    model.setPropertyName("dependents");
    assertEquals("dependents", "4", model.getText());
  }

  /** test for method getText(..) with Integer no format */
  public void testGetTextIntegerNoFormat() throws Exception {
    Integer value = new Integer(5);
    vo.setAnInteger(value);
    model.setPropertyName("anInteger");
    assertEquals("anInteger", "5", model.getText());
  }

  /** test for method getText(..) with Integer no format */
  public void testGetTextNullIntegerNoFormat() throws Exception {
    Integer value = null;
    vo.setAnInteger(value);
    model.setPropertyName("anInteger");
    assertEquals("anInteger", "", model.getText());
  }

  /** test for method getText(..) with NULL String no format */
  public void testGetTextNullStringNoFormat() throws Exception {
    vo.setFullName(null);
    model.setPropertyName("fullName");
    assertEquals("fullName", "", model.getText());
  }

  /** test for method getText(..) with boolean no format */
  public void testGetTextBooleanNoFormat() throws Exception {
    vo.setActive(true);
    model.setPropertyName("active");
    assertEquals("fullName", "true", model.getText());
  }

  /** test for method getText(..) with Null Value Object no format */
  public void testGetTextNullValueObject() throws Exception {
    holder.setValueObject(null);
    holder.setValueObjectClass(TestValueObject.class);
    model.setPropertyName("fullName");
    assertEquals("fullName", "", model.getText());
  }

  /** test for method getText(..) with Null Nested Value Object no format */
  public void testGetTextNullNestedValueObject() throws Exception {
    model.setPropertyName("homeAddress.city");
    assertEquals("homeAddress.city", "", model.getText());
  }

  /** test for method getText(..) with Nested Value Object no format */
  public void testGetTextNestedValueObject() throws Exception {
    TestAddressValueObject vo2 = new TestAddressValueObject();
    String city = "Juneau";
    vo2.setCity(city);
    vo.setHomeAddress(vo2);
    model.setPropertyName("homeAddress.city");
    assertEquals("homeAddress.city", city, model.getText());
  }

  // ***********************************************************************
  // *            getText() TESTS - with format
  // ***********************************************************************

  /** test for method getText(..) with String with MessageFormat */
  public void testGetTextStringWithMessageFormat() throws Exception {
    String value = "John Smith";
    MessageFormat format = new MessageFormat("");
    String pattern = "Greetings to the {0} Family";
    vo.setFullName(value);
    model.setPropertyName("fullName");
    model.setFormat(format);
    model.setFormatPattern(pattern);
    assertEquals("fullName", "Greetings to the John Smith Family", model.getText());
  }

  /** test for method getText(..) with Date with format */
  public void testGetTextDateWithFormat() throws Exception {
    SimpleDateFormat format = new SimpleDateFormat();
    String pattern = "MM-dd-yyyy";
    Date bd = new SimpleDateFormat(pattern).parse("05-05-1955");
    vo.setBirthdate(bd);
    model.setPropertyName("birthdate");
    model.setFormat(format);
    model.setFormatPattern(pattern);
    assertEquals("birthdate", "05-05-1955", model.getText());
  }

  /** test for method getText(..) with BigDecimal with format */
  public void testGetTextBigDecimalWithFormat() throws Exception {
    DecimalFormat format = new DecimalFormat();
    String pattern = "#,##0.00";
    BigDecimal value = new BigDecimal("12345.67");
    vo.setSalary(value);
    model.setPropertyName("salary");
    model.setFormat(format);
    model.setFormatPattern(pattern);
    assertEquals("salary", "12,345.67", model.getText());
  }

  /** test for method getText(..) with int with ChoiceFormat */
  public void testGetTextIntWithChoiceFormat() throws Exception {
    ChoiceFormat format = new ChoiceFormat("");
    String pattern = "0#No Dependents|1#One Dependent|1<Multiple Dependents";
    vo.setDependents(2);
    model.setPropertyName("dependents");
    model.setFormat(format);
    model.setFormatPattern(pattern);
    assertEquals("dependents", "Multiple Dependents", model.getText());
  }

  // ***********************************************************************
  // *            setText() TESTS - no format
  // ***********************************************************************

  /** test for method setText(..) String no format */
  public void testSetTextStringNoFormat() throws Exception {
    String value = "John Smith";
    model.setPropertyName("fullName");
    model.setText(value);
    assertEquals("fullName", value, vo.getFullName());
  }

  /**
   * test for method setText(..) Date no format
   * NOTE: There appears to be a problem with dropping seconds when
   * using DateFormat.getInstance.parse(...)
   */
  public void testSetTextDateNoFormat() throws Exception {
    Date value = new Date();
    String textValue = DataTypes.format(DataTypes.TYPE_DATE, value);
    value = (Date)DataTypes.parse(DataTypes.TYPE_DATE, textValue);
    model.setPropertyName("birthdate");
    model.setText(textValue);
    assertEquals("birthdate", value, vo.getBirthdate());
  }

  /** test for method setText(..) BigDecimal no format */
  public void testSetTextBigDecimalNoFormat() throws Exception {
    String textValue = "12345.67";
    BigDecimal value = new BigDecimal(textValue);
    model.setPropertyName("salary");
    model.setText(textValue);
    assertEquals("salary", value, vo.getSalary());
  }

  /** test for method setText(..) Null int no format - expect Exception */
  public void testSetTextNullIntNoFormat() throws Exception {
    model.setPropertyName("dependents");
    try {
      model.setText("");
      fail("Did not throw exception when trying to set primative to null");
    } catch (ModelPropertyAccessorException ex) {
      if (ex.getReason() != ModelPropertyAccessorException.REASON_NULL_VALUE_FOR_PRIMATIVE)
        throw ex;
    }
  }

  /** test for method setText(..) Null Integer no format */
  public void testSetTextNullIntegerNoFormat() throws Exception {
    model.setPropertyName("anInteger");
    model.setText("");
    assertEquals("anInteger", null, vo.getAnInteger());
  }

  /** test for method setText(..) boolean no format */
  public void testSetTextBoolNoFormat() throws Exception {
    String textValue = "true";
    boolean value = true;
    model.setPropertyName("active");
    model.setText(textValue);
    assertEquals("active", value, vo.isActive());
  }

  // ***********************************************************************
  // *            setText() TESTS - with format
  // ***********************************************************************

  /** test for method setText(..) String with MessageFormat */
  public void testSetTextStringWithMessageFormat() throws Exception {
    MessageFormat format = new MessageFormat("");
    String pattern = "Greetings to the {0} Family";
    String textValue = "Greetings to the John Smith Family";
    String value = "John Smith";
    model.setPropertyName("fullName");
    model.setFormat(format);
    model.setFormatPattern(pattern);
    model.setText(textValue);
    assertEquals("fullName", value, vo.getFullName());
  }

  /** test for method setText(..) Date with SimpleDateFormat */
  public void testSetTextDateWithSimpleDateFormat() throws Exception {
    SimpleDateFormat format = new SimpleDateFormat();
    String pattern = "MM-dd-yyyy";
    String textValue = "05-05-1955";
    Date value = new SimpleDateFormat(pattern).parse(textValue);
    model.setPropertyName("birthdate");
    model.setFormat(format);
    model.setFormatPattern(pattern);
    model.setText(textValue);
    assertEquals("birthdate", value, vo.getBirthdate());
  }

  /** test for method setText(..) BigDecimal with DecimalFormat */
  public void testSetTextBigDecimalWithFormat() throws Exception {
    DecimalFormat format = new DecimalFormat();
    String pattern = "#,##0.000000";
    String textValue = "12,345.670245";
    BigDecimal value = new BigDecimal("12345.670245");
    model.setPropertyName("salary");
    model.setFormat(format);
    model.setFormatPattern(pattern);
    model.setText(textValue);
    assertEquals("salary", value, vo.getSalary());
  }

  /** test for method getText(..) with int with ChoiceFormat */
  public void testSetTextIntWithChoiceFormat() throws Exception {
    ChoiceFormat format = new ChoiceFormat("");
    String pattern = "0#No Dependents|1#One Dependent|1<Multiple Dependents";
    String textValue = "One Dependent";
    int value = 1;
    vo.setDependents(value);
    model.setPropertyName("dependents");
    model.setFormat(format);
    model.setFormatPattern(pattern);
    model.setText(textValue);
    assertEquals("dependents", value, vo.getDependents());
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {DefaultTextModelTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
