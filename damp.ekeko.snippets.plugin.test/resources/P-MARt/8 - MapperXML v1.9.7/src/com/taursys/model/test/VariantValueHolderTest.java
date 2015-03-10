/**
 * VariantValueHolderTest - A JUnit Test
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
import com.taursys.model.VariantValueHolder;
import com.taursys.model.*;
import com.taursys.util.DataTypes;
import com.taursys.util.UnsupportedDataTypeException;
import java.beans.IntrospectionException;
import java.math.BigDecimal;

/** JUnitTest case for class: com.taursys.model.VariantValueHolder */
public class VariantValueHolderTest extends TestCase {

  public VariantValueHolderTest(String _name) {
    super(_name);
  }

  /** setUp method for test case */
  protected void setUp() {
  }

  /** tearDown method for test case */
  protected void tearDown() {
  }

  /** test for method getPropertyValue(..) BigDecimal value */
  public void testSetGetPropertyValueBigDecimal() throws Exception {
    VariantValueHolder holder = new VariantValueHolder(DataTypes.TYPE_BIGDECIMAL);
    BigDecimal value = new BigDecimal(1234.56);
    holder.setPropertyValue(null, value);
    assertEquals("BigDecimal value", value, holder.getPropertyValue(null));
  }

  /** test for method getPropertyValue(..) BigDecimal value */
  public void testSetGetPropertyValueBigDecimalNull() throws Exception {
    VariantValueHolder holder = new VariantValueHolder(DataTypes.TYPE_BIGDECIMAL);
    BigDecimal value = null;
    holder.setPropertyValue(null, value);
    assertEquals("BigDecimal value", value, holder.getPropertyValue(null));
  }

  /** test for method getPropertyValue(..) Int value */
  public void testSetGetPropertyValueInt() throws Exception {
    VariantValueHolder holder = new VariantValueHolder(DataTypes.TYPE_INT);
    Integer value = new Integer(2);
    holder.setPropertyValue("", value);
    assertEquals("int value", value, holder.getPropertyValue(""));
  }

  /** test for method getPropertyValue(..) Integer value */
  public void testSetGetPropertyValueIntNull() throws Exception {
    VariantValueHolder holder = new VariantValueHolder(DataTypes.TYPE_INT);
    Integer value = null;
    holder.setPropertyValue("", value);
    assertEquals("int value", value, holder.getPropertyValue(""));
  }

  /** test for method getPropertyValue(..) Integer value */
  public void testSetGetPropertyValueInteger() throws Exception {
    VariantValueHolder holder = new VariantValueHolder(DataTypes.TYPE_INT);
    Integer value = new Integer(2);
    holder.setPropertyValue("", value);
    assertEquals("int value", value, holder.getPropertyValue(""));
  }

  /** test for method getPropertyValue(..) Integer value */
  public void testSetGetPropertyValueIntegerNull() throws Exception {
    VariantValueHolder holder = new VariantValueHolder(DataTypes.TYPE_INT);
    Integer value = null;
    holder.setPropertyValue("", value);
    assertEquals("int value", value, holder.getPropertyValue(""));
  }

  /** test for method getPropertyValue(..) null value */
  public void testSetGetPropertyValueNull() throws Exception {
    VariantValueHolder holder = new VariantValueHolder(DataTypes.TYPE_BIGDECIMAL);
    holder.setPropertyValue("anything", null);
    assertNull("BigDecimal value", holder.getPropertyValue(null));
  }

  /** test for method getPropertyValue(..) Type Mismatch */
  public void testSetPropertyValueTypeMismatch() throws Exception {
    VariantValueHolder holder = new VariantValueHolder(DataTypes.TYPE_BIGDECIMAL);
    try {
      holder.setPropertyValue("anything", "A String");
      fail("Expected ClassCastException");
    } catch (ClassCastException ex) {
      assertEquals("Exception message",
          "Given java.lang.String expected java.math.BigDecimal", ex.getMessage());
    }
  }

  /** test for constructor VariantValueHolder(..) Invalid Data Type */
  public void testCreateInvalidDataType() throws Exception {
    try {
      VariantValueHolder holder = new VariantValueHolder(66);
      fail("Expected UnsupportedDataTypeException");
    } catch (UnsupportedDataTypeException ex) {
    }
  }

  /** test for method getPropertyValue(..) String value */
  public void testSetGetPropertyValueString() throws Exception {
    VariantValueHolder holder = new VariantValueHolder(DataTypes.TYPE_STRING);
    holder.setPropertyValue("anything", "John Smith");
    assertEquals("String value", "John Smith", holder.getPropertyValue(null));
  }

  /** Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {VariantValueHolderTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
