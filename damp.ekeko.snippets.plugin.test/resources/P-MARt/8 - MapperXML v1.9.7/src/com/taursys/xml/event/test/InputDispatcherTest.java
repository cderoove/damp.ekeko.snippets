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
import com.taursys.xml.AbstractField;
import com.taursys.xml.event.InputDispatcher;
import com.taursys.xml.event.InputEvent;
import com.taursys.xml.event.RenderEvent;
import com.taursys.xml.event.RenderException;
import java.util.HashMap;

/* JUnitTest case for class: com.taursys.xml.event.InputDispatcher */
public class InputDispatcherTest extends TestCase {
  protected TestComponent testComponent;
  private InputDispatcher dispatcher;
  private HashMap map;

  public InputDispatcherTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
    testComponent = new TestComponent();
    testComponent.setParameter("parm1");
    dispatcher = new InputDispatcher();
    map = new HashMap();
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  private void addParameter(String key, String value) {
    map.put(key, new String[]{value});
  }

  /* test for method dispatch(..) with parm1 = red */
  public void testDispatchWithValue() throws Exception {
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
    assertEquals("",testComponent.getSavedEvent().getValue());
  }

  /* test for method dispatch(..) with parm1 NOT GIVEN - NULL value */
  public void testDispatchWithNullValue() throws Exception {
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(map);
    assertEquals(null,testComponent.getSavedEvent());
  }

  /* test for method dispatch(..) with parm1 GIVEN and defaultValue set */
  public void testDispatchDefaultWithValue() throws Exception {
    addParameter("parm1", "red");
    testComponent.setDefaultValue("green");
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(map);
    assertEquals("red",testComponent.getSavedEvent().getValue());
  }

  /* test for method dispatch(..) with parm1 NOT GIVEN and defaultValue */
  public void testDispatchDefaultWithNullValue() throws Exception {
    dispatcher.addNotify(testComponent);
    testComponent.setDefaultValue("green");
    dispatcher.dispatch(map);
    assertEquals("green",testComponent.getSavedEvent().getValue());
  }

  // ********************************************************************
  //                 TEST CLASSES
  // ********************************************************************

  public class TestComponent extends AbstractField {
    private InputEvent savedEvent = null;
    protected void processInputEvent(InputEvent e) throws Exception {
      savedEvent = e;
    }
    public InputEvent getSavedEvent() {
      return savedEvent;
    }
    public void processRenderEvent(RenderEvent e) throws RenderException {
    }
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {InputDispatcherTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
