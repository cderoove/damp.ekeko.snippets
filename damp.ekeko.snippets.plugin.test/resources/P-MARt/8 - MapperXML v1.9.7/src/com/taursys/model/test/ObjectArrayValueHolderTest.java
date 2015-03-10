/**
 * ObjectArrayValueHolderTest - A JUnit Test
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
import com.taursys.util.*;

/* JUnitTest case for class: com.taursys.model.ObjectArrayValueHolder */
public class ObjectArrayValueHolderTest extends TestCase {

  public ObjectArrayValueHolderTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method getPropertyValue(..) */
  public void testGetPropertyValue() throws Exception {
    ObjectArrayValueHolder holder = new ObjectArrayValueHolder();
    holder.setArray(new String[] {"A","B","C"});
    holder.next();
    assertEquals("1 value", "A", holder.getPropertyValue(null));
    holder.next();
    assertEquals("2 value", "B", holder.getPropertyValue(null));
    holder.next();
    assertEquals("3 value", "C", holder.getPropertyValue(null));
  }

  /* test for method hasNext(..) */
  public void testHasNext() throws Exception {
    ObjectArrayValueHolder holder = new ObjectArrayValueHolder();
    holder.setArray(new String[] {"A","B","C"});
    assertTrue("1 Expect hasNext is true", holder.hasNext());
    holder.next();
    assertTrue("2 Expect hasNext is true", holder.hasNext());
    holder.next();
    assertTrue("3 Expect hasNext is true", holder.hasNext());
  }

  /* test for method next(..) */
  public void testNext() {
  }

  /* test for method reset(..) */
  public void testReset() {
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {ObjectArrayValueHolderTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
