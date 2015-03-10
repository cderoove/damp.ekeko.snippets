/**
 * JUnitTest case
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
package com.taursys.xml.event.test;

import junit.framework.TestCase;
import com.taursys.xml.Trigger;
import com.taursys.xml.event.TriggerEvent;
import com.taursys.xml.event.TriggerDispatcher;
import java.util.HashMap;

/* JUnitTest case for class: com.taursys.xml.event.TriggerDispatcher */
public class TriggerDispatcherTest extends TestCase {
  protected TestComponent testComponent;
  protected TriggerDispatcher dispatcher;
  private HashMap map;

  public TriggerDispatcherTest(String _name) {
    super(_name);
  }

  private void addParameter(String key, String value) {
    map.put(key, new String[]{value});
  }

  /* setUp method for test case */
  protected void setUp() {
    testComponent = new TestComponent();
    testComponent.setParameter("parm1");
    testComponent.setText("red");
    dispatcher = new TriggerDispatcher();
    map = new HashMap();
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method dispatch(..) with Match parm1 = red */
  public void testDispatchWithMatchValue() throws Exception {
    addParameter("parm1", "red");
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(map);
    assertEquals("red",testComponent.getSavedEvent().getValue());
  }

  /* test for method dispatch(..) with parm1 = <blank> */
  public void testDispatchWithBlankValue() throws Exception {
    addParameter("parm1", "");
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(map);
    assertEquals(null,testComponent.getSavedEvent());
  }

  /* test for method dispatch(..) with NonMatch parm1 = <blank> */
  public void testDispatchWithNonMatchValue() throws Exception {
    addParameter("parm1", "blue");
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(map);
    assertEquals(null,testComponent.getSavedEvent());
  }

  /* test for method dispatch(..) with parm1 NOT GIVEN - NULL value */
  public void testDispatchWithNullValue() throws Exception {
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(map);
    assertEquals(null,testComponent.getSavedEvent());
  }

  /* test for method dispatch(..) with Match parm1 = red and defaultTrigger set */
  public void testDispatchDefaultWithMatchValue() throws Exception {
    addParameter("parm1", "red");
    testComponent.setDefaultTrigger(true);
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(map);
    assertEquals("red",testComponent.getSavedEvent().getValue());
  }

  /* test for method dispatch(..) with NonMatch parm1 = green and defaultTrigger set */
  public void testDispatchDefaultWithNonMatchValue() throws Exception {
    addParameter("parm1", "green");
    testComponent.setDefaultTrigger(true);
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(map);
    assertEquals(null,testComponent.getSavedEvent());
  }

  /* test for method dispatch(..) with NonMatch parm1 = green and defaultTrigger set */
  public void testDispatchDefaultWithNoValueNoText() throws Exception {
    testComponent.setDefaultTrigger(true);
    testComponent.setText(null);
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(map);
    assertNotNull("Saved event",testComponent.getSavedEvent());
  }

  /* test for method dispatch(..) with parm1 NOT GIVEN and defaultValue */
  public void testDispatchDefaultWithNullValue() throws Exception {
    dispatcher.addNotify(testComponent);
    testComponent.setDefaultTrigger(true);
    dispatcher.dispatch(map);
    assertEquals("red",testComponent.getSavedEvent().getValue());
  }

  // ********************************************************************
  //                 TEST CLASSES
  // ********************************************************************

  public class TestComponent extends Trigger {
    private TriggerEvent savedEvent = null;
    protected void processTriggerEvent(TriggerEvent e) throws Exception {
      savedEvent = e;
    }
    public TriggerEvent getSavedEvent() {
      return savedEvent;
    }
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {TriggerDispatcherTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }

}
