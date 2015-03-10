package com.taursys.model.test;

import junit.framework.TestCase;
import com.taursys.model.*;

/* JUnitTest case for class: com.taursys.model.VOComparator */
public class VOComparatorTest extends TestCase {
  VOValueHolder holder;
  VOComparator comparator;
  TestValueObject vo1;
  TestValueObject vo2;


  public VOComparatorTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
    holder = new VOValueHolder();
    holder.setValueObjectClass(TestValueObject.class);
    comparator = new VOComparator();
    comparator.setVOValueHolder(holder);
    vo1 = new BeverlyCrusher();
    vo2 = new JeanLucPicard();
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method compare(..) */
  public void testCompareInvalidProperty() {
    comparator.setPropertyName("xxxx");
    assertEquals("sort direction", 0, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testComparePropertiesNull() {
    comparator.setPropertyNames(null);
    assertEquals("sort direction", 0, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testComparePropertiesEmpty() {
    comparator.setPropertyNames(new String[] {});
    assertEquals("sort direction", 0, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testCompareHolderNull() {
    comparator.setPropertyName("fullName");
    comparator.setVOValueHolder(null);
    assertEquals("sort direction", 0, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testCompareNullPropertyName() {
    comparator.setPropertyName(null);
    assertEquals("sort direction", 0, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testCompareFirstNull() {
    comparator.setPropertyName("fullName");
    assertEquals("sort direction", -1, comparator.compare(null, vo2));
  }

  /* test for method compare(..) */
  public void testCompareSecondNull() {
    comparator.setPropertyName("fullName");
    assertEquals("sort direction", 1, comparator.compare(vo1, null));
  }

  /* test for method compare(..) */
  public void testCompareBothNull() {
    comparator.setPropertyName("fullName");
    assertEquals("sort direction", 0, comparator.compare(null, null));
  }

  /* test for method compare(..) */
  public void testCompareSinglePropertyAsc() {
    comparator.setPropertyName("fullName");
    assertTrue("sort direction negative", comparator.compare(vo1, vo2) < 0);
  }

  /* test for method compare(..) */
  public void testCompareSinglePropertyAscEqual() {
    comparator.setPropertyName("createDate");
    assertEquals("sort direction", 0, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testCompareSinglePropertyAscFirstNull() {
    vo1.setCreateDate(null);
    comparator.setPropertyName("createDate");
    assertEquals("sort direction", -1, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testCompareSinglePropertyAscSecondNull() {
    vo2.setCreateDate(null);
    comparator.setPropertyName("createDate");
    assertEquals("sort direction", 1, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testCompareSinglePropertyAscBothNull() {
    vo1.setCreateDate(null);
    vo2.setCreateDate(null);
    comparator.setPropertyName("createDate");
    assertEquals("sort direction", 0, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testCompareSinglePropertyDsc() {
    comparator.setPropertyName("fullName");
    comparator.setAscendingOrder(false);
    assertTrue("sort direction positive", comparator.compare(vo1, vo2) > 0);
  }

  /* test for method compare(..) */
  public void testCompareMultiplePropertiesAscFirstPropEqual() {
    comparator.setPropertyNames(new String[] {"createDate","salary"});
    assertEquals("sort direction", -1, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testCompareMultiplePropertiesAscSecondPropEqual() {
    comparator.setPropertyNames(new String[] {"salary", "createDate"});
    assertEquals("sort direction", -1, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testCompareMultiplePropertiesMixedAllSpecified() {
    comparator.setPropertyNames(new String[] {"salary", "birthdate"});
    comparator.setAscendingOrders(new boolean[] {false, true});
    assertEquals("sort direction", 1, comparator.compare(vo1, vo2));
  }

  /* test for method compare(..) */
  public void testCompareMultiplePropertiesMixedFirstSpecified() {
    comparator.setPropertyNames(new String[] {"salary", "birthdate"});
    comparator.setAscendingOrders(new boolean[] {false});
    assertEquals("sort direction", 1, comparator.compare(vo1, vo2));
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {VOComparatorTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
