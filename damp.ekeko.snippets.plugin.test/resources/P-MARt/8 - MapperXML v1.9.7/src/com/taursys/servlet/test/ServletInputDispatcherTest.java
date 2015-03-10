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
package com.taursys.servlet.test;

import junit.framework.TestCase;
import com.taursys.servlet.ServletInputDispatcher;
import com.taursys.xml.AbstractField;
import com.taursys.xml.event.InputEvent;
import java.text.ParseException;
import com.taursys.xml.event.RenderException;
import com.taursys.xml.event.RenderEvent;

/* JUnitTest case for class: com.taursys.servlet.ServletInputDispatcher */
public class ServletInputDispatcherTest extends TestCase {
  protected TestComponent testComponent;
  protected TestServletRequest testRequest;
  protected ServletInputDispatcher dispatcher;

  public ServletInputDispatcherTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
    testComponent = new TestComponent();
    testComponent.setParameter("parm1");
    dispatcher = new ServletInputDispatcher();
    testRequest = new TestServletRequest();
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method dispatch(..) with parm1 = red */
  public void testDispatchWithValue() throws Exception {
    testRequest.addParameter("parm1", "red");
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(testRequest);
    assertEquals("red",testComponent.getSavedEvent().getValue());
  }

  /* test for method dispatch(..) with parm1 = <blank> */
  public void testDispatchWithBlankValue() throws Exception {
    testRequest.addParameter("parm1", "");
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(testRequest);
    assertEquals("",testComponent.getSavedEvent().getValue());
  }

  /* test for method dispatch(..) with parm1 NOT GIVEN - NULL value */
  public void testDispatchWithNullValue() throws Exception {
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(testRequest);
    assertEquals(null,testComponent.getSavedEvent());
  }

  /* test for method dispatch(..) with parm1 GIVEN and defaultValue set */
  public void testDispatchDefaultWithValue() throws Exception {
    testRequest.addParameter("parm1", "red");
    testComponent.setDefaultValue("green");
    dispatcher.addNotify(testComponent);
    dispatcher.dispatch(testRequest);
    assertEquals("red",testComponent.getSavedEvent().getValue());
  }

  /* test for method dispatch(..) with parm1 NOT GIVEN and defaultValue */
  public void testDispatchDefaultWithNullValue() throws Exception {
    dispatcher.addNotify(testComponent);
    testComponent.setDefaultValue("green");
    dispatcher.dispatch(testRequest);
    assertEquals("green",testComponent.getSavedEvent().getValue());
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {ServletInputDispatcherTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }

  // ********************************************************************
  //                 TEST CLASSES
  // ********************************************************************

  public class TestComponent extends AbstractField {
    private InputEvent savedEvent = null;
    protected void processInputEvent(InputEvent e) throws ParseException {
      savedEvent = e;
    }
    public InputEvent getSavedEvent() {
      return savedEvent;
    }
    public void processRenderEvent(RenderEvent e) throws RenderException {
    }
  }
}
