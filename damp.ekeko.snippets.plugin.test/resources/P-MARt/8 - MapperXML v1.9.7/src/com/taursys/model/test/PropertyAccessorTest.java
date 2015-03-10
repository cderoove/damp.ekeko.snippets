/**
 * PropertyAccesorTest - Unit Test
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
import com.taursys.model.PropertyAccessor;
import com.taursys.model.ModelException;
import com.taursys.model.ModelPropertyAccessorException;
import com.taursys.model.ModelInvocationTargetException;
import java.util.Date;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.beans.IntrospectionException;

/** JUnitTest case for class: com.taursys.model.PropertyAccessor */
public class PropertyAccessorTest extends TestCase {

  public PropertyAccessorTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  // ***********************************************************************
  // *                        CONSTRUCTOR TESTS
  // ***********************************************************************

  /** test for constructor PropertyAccessor(..) with Null Value Object Class */
  public void testConstructorNullValueObjectClass() throws Exception {
    try {
      PropertyAccessor pa = new PropertyAccessor(null, "fullName");
      fail("Did not throw ModelPropertyAccessorException when given a null class");
    }
    catch (ModelPropertyAccessorException ex) {
      assertEquals("reason code",
          ModelPropertyAccessorException.REASON_TARGET_CLASS_IS_NULL,
          ex.getReason());
      assertEquals("Exception propertyName",
          "fullName", ex.getPropertyName());
    }
  }

  /** test for constructor PropertyAccessor(..) with Null Property Name */
  public void testConstructorNullPropertyName() throws Exception {
    try {
      PropertyAccessor pa = new PropertyAccessor(TestValueObject.class, null);
      fail("Did not throw ModelPropertyAccessorException when given a null Property Name");
    }
    catch (ModelPropertyAccessorException ex) {
      assertEquals("reason code",
          ModelPropertyAccessorException.REASON_PROPERTY_NAME_MISSING,
          ex.getReason());
      assertEquals("Exception valueObjectClassName",
          "com.taursys.model.test.TestValueObject", ex.getValueObjectClassName());
    }
  }

  /** test for constructor PropertyAccessor(..) with Blank Property Name */
  public void testConstructorBlankPropertyName() throws Exception {
    try {
      PropertyAccessor pa = new PropertyAccessor(TestValueObject.class, "");
      fail("Did not throw ModelPropertyAccessorException when given a blank Property Name");
    }
    catch (ModelPropertyAccessorException ex) {
      assertEquals("reason code",
          ModelPropertyAccessorException.REASON_PROPERTY_NAME_MISSING,
          ex.getReason());
      assertEquals("Exception valueObjectClassName",
          "com.taursys.model.test.TestValueObject", ex.getValueObjectClassName());
    }
  }

  /** test for constructor PropertyAccessor(..) with Invalid Property Name */
  public void testConstructorInvalidPropertyName() throws Exception {
    try {
      PropertyAccessor pa = new PropertyAccessor(TestValueObject.class, "height");
      fail("Did not throw ModelPropertyAccessorException when given an invalid Property Name");
    }
    catch (ModelPropertyAccessorException ex) {
      assertEquals("reason code",
          ModelPropertyAccessorException.REASON_PROPERTY_NOT_FOUND,
          ex.getReason());
      assertEquals("Exception valueObjectClassName",
          "com.taursys.model.test.TestValueObject", ex.getValueObjectClassName());
      assertEquals("Exception propertyName",
          "height", ex.getPropertyName());
    }
  }

  // ***********************************************************************
  // *            getPropertyValue() TESTS
  // ***********************************************************************

  /** test for method getPropertyValue(..) with String */
  public void testGetPropertyValueString() throws Exception {
    TestValueObject vo = new TestValueObject();
    String value = "John Smith";
    vo.setFullName(value);
    PropertyAccessor pa = new PropertyAccessor(vo.getClass(), "fullName");
    assertEquals("fullName", value, pa.getPropertyValue(vo));
  }

  /** test for method getPropertyValue(..) with Null value object */
  public void testGetPropertyValueNullValueObject() throws Exception {
    PropertyAccessor pa = new PropertyAccessor(TestValueObject.class, "fullName");
    assertEquals("fullName", null, pa.getPropertyValue(null));
  }


  /** test for method getPropertyValue(..) with wrong value object class */
  public void testGetPropertyValueWrongVOClass() throws Exception {
    TestAddressValueObject vo = new TestAddressValueObject();
    vo.setCreateDate(new Date(System.currentTimeMillis()+1000));
    PropertyAccessor pa = new PropertyAccessor(TestValueObject.class, "createDate");
    try {
      pa.getPropertyValue(vo);
      fail("Expected ModelPropertyAccessorException");
    } catch (ModelPropertyAccessorException ex) {
      assertEquals("reason code",
          ModelPropertyAccessorException.REASON_ILLEGAL_ARGUMENT_EXCEPTION,
          ex.getReason());
      assertEquals("Exception valueObjectClassName",
          "com.taursys.model.test.TestValueObject", ex.getValueObjectClassName());
      assertEquals("Exception propertyName",
          "createDate", ex.getPropertyName());
      assertEquals("methodName", "getCreateDate", ex.getMethodName());
    }
  }

  // ***********************************************************************
  // *            setPropertyValue() TESTS
  // ***********************************************************************

  /** test for method setPropertyValue(..) String */
  public void testSetPropertyValueString() throws Exception {
    TestValueObject vo = new TestValueObject();
    String value = "John Smith";
    PropertyAccessor pa = new PropertyAccessor(vo.getClass(), "fullName");
    pa.setPropertyValue(vo, value);
    assertEquals("fullName", value, vo.getFullName());
  }

  /** test for method setPropertyValue(..) Null Value Object */
  public void testSetPropertyValueNullValueObject() throws Exception {
    try {
      String value = "John Smith";
      PropertyAccessor pa = new PropertyAccessor(TestValueObject.class, "fullName");
      pa.setPropertyValue(null, value);
      fail("Expected ModelPropertyAccessorException did not occur");
    } catch (ModelPropertyAccessorException ex) {
      assertEquals("reason code",
          ModelPropertyAccessorException.REASON_TARGET_IS_NULL,
          ex.getReason());
      assertEquals("Exception propertyName",
          "fullName", ex.getPropertyName());
      assertEquals("Exception ValueObjectClassName",
          "com.taursys.model.test.TestValueObject", ex.getValueObjectClassName());
    }
  }

  /** test for method setPropertyValue(..) with wrong value object class */
  public void testSetPropertyValueWrongVOClass() throws Exception {
    TestAddressValueObject vo = new TestAddressValueObject();
    PropertyAccessor pa = new PropertyAccessor(TestValueObject.class, "createDate");
    try {
      pa.setPropertyValue(vo, new Date());
      fail("Expected ModelPropertyAccessorException");
    } catch (ModelPropertyAccessorException ex) {
      assertEquals("reason code",
          ModelPropertyAccessorException.REASON_ILLEGAL_ARGUMENT_EXCEPTION,
          ex.getReason());
      assertEquals("Exception valueObjectClassName",
          "com.taursys.model.test.TestValueObject", ex.getValueObjectClassName());
      assertEquals("Exception propertyName",
          "createDate", ex.getPropertyName());
      assertEquals("methodName", "setCreateDate", ex.getMethodName());
    }
  }

  /** test for method setPropertyValue(..) with wrong value object class */
  public void testSetPropertyValueWrongValueClass() throws Exception {
    TestAddressValueObject vo = new TestAddressValueObject();
    PropertyAccessor pa = new PropertyAccessor(TestAddressValueObject.class, "createDate");
    try {
      pa.setPropertyValue(vo, "John Smith");
      fail("Expected ModelPropertyAccessorException");
    } catch (ModelPropertyAccessorException ex) {
      assertEquals("reason code",
          ModelPropertyAccessorException.REASON_ILLEGAL_ARGUMENT_EXCEPTION,
          ex.getReason());
      assertEquals("Exception valueObjectClassName",
          "com.taursys.model.test.TestAddressValueObject", ex.getValueObjectClassName());
      assertEquals("Exception propertyName",
          "createDate", ex.getPropertyName());
      assertEquals("methodName", "setCreateDate", ex.getMethodName());
    }
  }

  /** Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {PropertyAccessorTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
