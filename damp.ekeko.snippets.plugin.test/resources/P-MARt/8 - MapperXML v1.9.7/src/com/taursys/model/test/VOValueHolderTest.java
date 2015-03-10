/**
 * VOValueHolderTest - Unit Test
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
import com.taursys.model.VOValueHolder;
import com.taursys.model.test.TestValueObject;
import com.taursys.model.ModelException;
import com.taursys.model.ModelInvocationTargetException;
import com.taursys.model.ModelPropertyAccessorException;
import com.taursys.util.DataTypes;
import java.beans.IntrospectionException;
import java.util.Map;

/** JUnitTest case for class: com.taursys.model.VOValueHolder */
public class VOValueHolderTest extends TestCase {
  private TestVOValueHolder holder;
  private TestValueObject vo;

  public VOValueHolderTest(String _name) {
    super(_name);
  }

  class TestVOValueHolder extends VOValueHolder {
    public Map getPropertyAccessors() {
      return super.getPropertyAccessors();
    }
  }

  /** setUp method for test case */
  protected void setUp() {
    holder = new TestVOValueHolder();
    vo = new TestValueObject();
  }

  /** tearDown method for test case */
  protected void tearDown() {
  }

  /** test for method getPropertyValue(..) Multiple */
  public void testGetPropertyValueMultiple() throws Exception {
    vo.setFullName("John Smith");
    vo.setActive(true);
    holder.setValueObject(vo);
    assertEquals("FullName", "John Smith", holder.getPropertyValue("fullName"));
    assertEquals("active", Boolean.TRUE, holder.getPropertyValue("active"));
  }

  /** test for method getPropertyValue(..) Multiple access of same */
  public void testGetPropertyValueMultipleAccessOfSame() throws Exception {
    vo.setFullName("John Smith");
    holder.setValueObject(vo);
    assertEquals("FullName", "John Smith", holder.getPropertyValue("fullName"));
    assertEquals("FullName", "John Smith", holder.getPropertyValue("fullName"));
    assertEquals("FullName", "John Smith", holder.getPropertyValue("fullName"));
    // Now make sure only 1 entry in Map
    assertEquals("Size of map", 1, holder.getPropertyAccessors().size());
  }

  /** test for method getPropertyValue(..) NullVO with class */
  public void testGetPropertyValueNullVO() throws Exception {
    holder.setValueObjectClass(TestValueObject.class);
    assertNull("fullName", holder.getPropertyValue("fullName"));
  }

  /** test for method getPropertyValue(..) Both Null VO and Class */
  public void testGetPropertyValueBothNull() throws Exception {
    try {
      holder.getPropertyValue("fullName");
      fail("Expected IntrospectionException");
    } catch (ModelPropertyAccessorException ex) {
      assertEquals("reason",
          ModelPropertyAccessorException.REASON_TARGET_AND_CLASS_ARE_NULL,
          ex.getReason());
      assertEquals("propertyName", "fullName", ex.getPropertyName());
    }
  }

  /** test for method setPropertyValue(..) */
  public void testSetPropertyValue() throws Exception {
    holder.setValueObject(vo);
    String value = "John Smith";
    holder.setPropertyValue("fullName", value);
    assertEquals("FullName", value, vo.getFullName());
  }

  /** test for method getJavaDataType(..) */
  public void testGetJavaDataType() throws Exception {
    holder.setValueObject(vo);
    assertEquals("javaDataType", DataTypes.TYPE_BIGDECIMAL, holder.getJavaDataType("salary"));
  }


  public void testParentValueHolder() throws Exception {
    TestAddressValueObject addr =
        new TestAddressValueObject("Seattle", "WA", "98116");
    vo.setHomeAddress(addr);
    VOValueHolder addrHolder = new VOValueHolder();
    addrHolder.setParentValueHolder(holder);
    addrHolder.setParentPropertyName("homeAddress");
    holder.setObject(vo);
    assertEquals("Address city","Seattle", addrHolder.getPropertyValue("city"));
  }

  public void testParentValueHolderMultiChange() throws Exception {
    TestAddressValueObject addr =
        new TestAddressValueObject("Seattle", "WA", "98116");
    vo.setHomeAddress(addr);
    VOValueHolder addrHolder = new VOValueHolder();
    addrHolder.setParentValueHolder(holder);
    addrHolder.setParentPropertyName("homeAddress");
    holder.setObject(vo);
    assertEquals("Address city","Seattle", addrHolder.getPropertyValue("city"));
    vo.setHomeAddress(new TestAddressValueObject("Tacoma", "WA", "98320"));
    holder.fireContentValueChanged();
    assertEquals("Address city","Tacoma", addrHolder.getPropertyValue("city"));
  }

  /** Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {VOValueHolderTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
