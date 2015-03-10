/**
 * VOCollectionValueHolderTest - A JUnit Test
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
import com.taursys.model.VOCollectionValueHolder;
import java.util.ArrayList;
import java.util.Collection;

/* JUnitTest case for class: com.taursys.model.VOCollectionValueHolder */
public class VOCollectionValueHolderTest extends TestCase {
  private ArrayList list = new ArrayList();
  TestValueObject vo1 = new TestValueObject();
  TestValueObject vo2 = new TestValueObject();
  VOCollectionValueHolder holder = new VOCollectionValueHolder();

  public VOCollectionValueHolderTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
    vo1.setFullName("First Object");
    list.add(vo1);
    vo2.setFullName("Second Object");
    list.add(vo2);
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /**
   * Test iterate list with items (tests hasNext, next, getValueObject)
   */
  public void testIterateList() {
    holder.setCollection(list);
    assertTrue("hasNext", holder.hasNext());
    holder.next();
    assertEquals("VO 1", vo1, holder.getObject());
    holder.next();
    assertEquals("VO 2", vo2, holder.getObject());
    assertEquals("2nd check of VO 2", vo2, holder.getObject());
  }

  /**
   * Test initial state = null VO
   */
  public void testInitialState() {
    holder.setCollection(list);
    assertNull("VO is null", holder.getObject());
  }

  /* test for method reset(..) */
  public void testReset() {
    holder.setCollection(list);
    holder.hasNext();
    holder.next();
    holder.reset();
    assertNull("VO is null", holder.getObject());
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {VOCollectionValueHolderTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
