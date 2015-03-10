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
import com.taursys.servlet.ServletParameterDispatcher;
import com.taursys.xml.Parameter;
import com.taursys.xml.event.ParameterEvent;
import java.text.ParseException;

/* JUnitTest case for class: com.taursys.xml.servlet.ServletParameterDispatcher */
public class ServletParameterDispatcherTest extends TestCase {
  protected TestParameter testParm;
  protected TestServletRequest testRequest;
  protected ServletParameterDispatcher dispatcher;

  public ServletParameterDispatcherTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
    testParm = new TestParameter();
    testParm.setParameter("parm1");
    dispatcher = new ServletParameterDispatcher();
    testRequest = new TestServletRequest();
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method dispatch(..) with parm1 = red */
  public void testDispatchWithValue() throws Exception {
    testRequest.addParameter("parm1", "red");
    dispatcher.addNotify(testParm);
    dispatcher.dispatch(testRequest);
    assertEquals("red",testParm.getSavedEvent().getValue());
  }

  /* test for method dispatch(..) with parm1 = <blank> */
  public void testDispatchWithBlankValue() throws Exception {
    testRequest.addParameter("parm1", "");
    dispatcher.addNotify(testParm);
    dispatcher.dispatch(testRequest);
    assertEquals("",testParm.getSavedEvent().getValue());
  }

  /* test for method dispatch(..) with parm1 NOT GIVEN - NULL value */
  public void testDispatchWithNullValue() throws Exception {
    dispatcher.addNotify(testParm);
    dispatcher.dispatch(testRequest);
    assertEquals(null,testParm.getSavedEvent());
  }

  /* test for method dispatch(..) with parm1 GIVEN and defaultValue set */
  public void testDispatchDefaultWithValue() throws Exception {
    testRequest.addParameter("parm1", "red");
    testParm.setDefaultValue("green");
    dispatcher.addNotify(testParm);
    dispatcher.dispatch(testRequest);
    assertEquals("red",testParm.getSavedEvent().getValue());
  }

  /* test for method dispatch(..) with parm1 NOT GIVEN and defaultValue */
  public void testDispatchDefaultWithNullValue() throws Exception {
    dispatcher.addNotify(testParm);
    testParm.setDefaultValue("green");
    dispatcher.dispatch(testRequest);
    assertEquals("green",testParm.getSavedEvent().getValue());
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {ServletParameterDispatcherTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }

  // ********************************************************************
  //                 TEST CLASSES
  // ********************************************************************

  public class TestParameter extends Parameter {
    private ParameterEvent savedEvent = null;
    protected void processParameterEvent(ParameterEvent e) throws ParseException {
      savedEvent = e;
    }
    public ParameterEvent getSavedEvent() {
      return savedEvent;
    }
  }
}
