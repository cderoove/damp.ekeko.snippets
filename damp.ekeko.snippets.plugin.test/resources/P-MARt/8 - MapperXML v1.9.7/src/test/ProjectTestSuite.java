package test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This test suite has been automatically generated
 * to execute all JUnitTest tests found in the project.
 * To update the list of tests re-run the UnitTest Batch TestRunner again.
 */

public class ProjectTestSuite extends TestCase {

  public ProjectTestSuite(String _name) {
    super(_name);
  }

  public static Test suite() {
    TestSuite theSuite = new TestSuite();
    theSuite.addTestSuite(com.taursys.dom.test.DOM_1_20000929_DocumentAdapterTest.class);
    theSuite.addTestSuite(com.taursys.html.test.HTMLComponentFactoryTest.class);
    theSuite.addTestSuite(com.taursys.model.test.DefaultSelectModelTest.class);
    theSuite.addTestSuite(com.taursys.model.test.DefaultTextModelTest.class);
    theSuite.addTestSuite(com.taursys.model.test.ObjectArrayValueHolderTest.class);
    theSuite.addTestSuite(com.taursys.model.test.PropertyAccessorTest.class);
    theSuite.addTestSuite(com.taursys.model.test.VOCollectionValueHolderTest.class);
    theSuite.addTestSuite(com.taursys.model.test.VOValueHolderTest.class);
    theSuite.addTestSuite(com.taursys.model.test.VariantValueHolderTest.class);
    theSuite.addTestSuite(com.taursys.model.test.DefaultCheckboxModelTest.class);
    theSuite.addTestSuite(com.taursys.model.test.VOComparatorTest.class);
    theSuite.addTestSuite(com.taursys.servlet.test.ServletParameterDispatcherTest.class);
    theSuite.addTestSuite(com.taursys.servlet.test.ServletFormFactoryTest.class);
    theSuite.addTestSuite(com.taursys.servlet.test.ServletInputDispatcherTest.class);
    theSuite.addTestSuite(com.taursys.servlet.test.ServletTriggerDispatcherTest.class);
    theSuite.addTestSuite(com.taursys.servlet.test.HttpMultiPartServletRequestTest.class);
    theSuite.addTestSuite(com.taursys.servlet.test.ServletFormTest.class);
    theSuite.addTestSuite(com.taursys.tools.util.test.ClassPathTest.class);
    theSuite.addTestSuite(com.taursys.xml.event.test.ParameterDispatcherTest.class);
    theSuite.addTestSuite(com.taursys.xml.event.test.InputDispatcherTest.class);
    theSuite.addTestSuite(com.taursys.xml.event.test.TriggerDispatcherTest.class);
    theSuite.addTestSuite(com.taursys.xml.test.ParameterTest.class);
    theSuite.addTestSuite(com.taursys.xml.test.SelectFieldTest.class);
    theSuite.addTestSuite(com.taursys.xml.test.CheckboxFieldTest.class);
    return theSuite;
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {ProjectTestSuite.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
