/**
 * DefaultSelectModelTest - A JUnit Test
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
import java.util.*;
import java.math.BigDecimal;

/** JUnitTest case for class: com.taursys.model.DefaultSelectModel */
public class DefaultSelectModelTest extends TestCase {
  TestAddressValueObject address;
  VOCollectionValueHolder zipList;
  VOValueHolder holder;
  TestSelectModel model;
  public static final TestAddressValueObject JUNEAU =
      new TestAddressValueObject("Juneau", "AK", "99801");
  public static final TestAddressValueObject DOUGLAS =
      new TestAddressValueObject("Douglas", "AK", "99824");
  public static final TestAddressValueObject AUKE_BAY =
      new TestAddressValueObject("Auke Bay", "AK", "99827");
  public static final TestAddressValueObject KETCHIKAN =
      new TestAddressValueObject("Ketchikan", "AK", "99501");
  public DefaultSelectModelTest(String _name) {
    super(_name);
  }

  public class TestSelectModel extends DefaultSelectModel {
    public boolean isCurrentMatch() throws ModelException {
      return super.isCurrentMatch();
    }
    public boolean isValueNull() throws ModelException {
      return super.isValueNull();
    }
    public void copyValues() throws ModelException {
      super.copyValues();
    }
    public void setNullValues() throws ModelException {
      super.setNullValues();
    }
  }

  /* setUp method for test case */
  protected void setUp() {
    address = new TestAddressValueObject();
    zipList = new VOCollectionValueHolder();
    holder = new VOValueHolder();
    model = new TestSelectModel();
    // Setup picklist
    ArrayList zips = new ArrayList();
    zips.add(JUNEAU);
    zips.add(DOUGLAS);
    zips.add(AUKE_BAY);
    zips.add(KETCHIKAN);
    zipList.setCollection(zips);
    // Setup valueHolder
    holder.setValueObject(address);
    // Setup model
    model.setList(zipList);
    model.setValueHolder(holder);
    model.setDisplayPropertyName("zip");
    model.setListPropertyNames(new String[]{"city","state","zip"});
    model.setPropertyNames(new String[]{"city","state","zip"});
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /** test for method getText(..) Valid value */
  public void testGetDisplayOptionListNullAllowedThirdSelected() throws ModelException {
    address.setCity("Douglas");
    address.setState("AK");
    address.setZip("99824");
    Collection c = model.getDisplayOptionList();
    Iterator iter = c.iterator();
    SelectModelOption option = (SelectModelOption)iter.next();
    assertEquals("1st item display value", "--none--", option.getOptionText());
    assertEquals("1st item selected", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("2nd item display value", JUNEAU.getZip(), option.getOptionText());
    assertEquals("2nd item selected", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("3rd item display value", DOUGLAS.getZip(), option.getOptionText());
    assertEquals("3rd item selected", true, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("4th item display value", AUKE_BAY.getZip(), option.getOptionText());
    assertEquals("4th item selected", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("5th item display value", KETCHIKAN.getZip(), option.getOptionText());
    assertEquals("5th item selected", false, option.isSelected());
    if (iter.hasNext())
      fail("A 6th item is present in list - should only be 5 items");
  }

  /** test for method getText(..) Valid value */
  public void testGetDisplayOptionListNullAllowedNullSelected() throws ModelException {
    Collection c = model.getDisplayOptionList();
    Iterator iter = c.iterator();
    SelectModelOption option = (SelectModelOption)iter.next();
    assertEquals("1st item display value", "--none--", option.getOptionText());
    assertEquals("1st item selected", true, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("2nd item display value", JUNEAU.getZip(), option.getOptionText());
    assertEquals("2nd item selected", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("3rd item display value", DOUGLAS.getZip(), option.getOptionText());
    assertEquals("3rd item selected", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("4th item display value", AUKE_BAY.getZip(), option.getOptionText());
    assertEquals("4th item selected", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("5th item display value", KETCHIKAN.getZip(), option.getOptionText());
    assertEquals("5th item selected", false, option.isSelected());
    if (iter.hasNext())
      fail("A 6th item is present in list - should only be 5 items");
  }

  /** test for method getText(..) Valid value */
  public void testGetDisplayOptionListNullNotAllowedThirdSelected() throws ModelException {
    model.setNullAllowed(false);
    address.setCity("Douglas");
    address.setState("AK");
    address.setZip("99824");
    Collection c = model.getDisplayOptionList();
    Iterator iter = c.iterator();
    SelectModelOption option = (SelectModelOption)iter.next();
    assertEquals("display value item 1", JUNEAU.getZip(), option.getOptionText());
    assertEquals("selected item 1", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("display value item 2", DOUGLAS.getZip(), option.getOptionText());
    assertEquals("selected item 2", true, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("display value item 3", AUKE_BAY.getZip(), option.getOptionText());
    assertEquals("selected item 3", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("display value item 4", KETCHIKAN.getZip(), option.getOptionText());
    assertEquals("selected item 4", false, option.isSelected());
    if (iter.hasNext())
      fail("A 5th item is present in list - should only be 4 items");
  }

  /** test for method getText(..) Valid value */
  public void testGetDisplayOptionListNullNotAllowedNullSelected() throws ModelException {
    model.setNullAllowed(false);
    Collection c = model.getDisplayOptionList();
    Iterator iter = c.iterator();
    SelectModelOption option = (SelectModelOption)iter.next();
    assertEquals("display value item 1", JUNEAU.getZip(), option.getOptionText());
    assertEquals("selected item 1", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("display value item 2", DOUGLAS.getZip(), option.getOptionText());
    assertEquals("selected item 2", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("display value item 3", AUKE_BAY.getZip(), option.getOptionText());
    assertEquals("selected item 3", false, option.isSelected());
    option = (SelectModelOption)iter.next();
    assertEquals("display value item 4", KETCHIKAN.getZip(), option.getOptionText());
    assertEquals("selected item 4", false, option.isSelected());
    if (iter.hasNext())
      fail("A 5th item is present in list - should only be 4 items");
  }

  /** test for method getText(..) Valid value */
  public void testGetTextValid() throws ModelException {
    address.setCity("Douglas");
    address.setState("AK");
    address.setZip("99824");
    assertEquals("Display zip","99824", model.getText());
  }

  /** test for method getText(..) Null value */
  public void testGetTextNull() throws ModelException {
    assertEquals("Display zip","--none--", model.getText());
  }

  /** test for method setText(..) Valid */
  public void testSetTextValid() throws ModelException {
    address.setCity("Juneau");
    address.setState("AK");
    address.setZip("99801");
    // Change selection
    model.setText("99501");
    // Check that vo is set
    assertEquals("City","Ketchikan",address.getCity());
    assertEquals("State","AK",address.getState());
    assertEquals("Zip","99501",address.getZip());
  }

  /** test for method setText(..) Invalid */
  public void testSetTextInvalid() throws ModelException {
    address.setCity("Juneau");
    address.setState("AK");
    address.setZip("99801");
    try {
      model.setText("99999");
      fail("Expected a NotInListException");
    } catch (NotInListException ex) {
    }
  }

  /** test for method setText(..) Null */
  public void testSetTextNullAllowedValueNull() throws ModelException {
    address.setCity("Juneau");
    address.setState("AK");
    address.setZip("99801");
    model.setText("--none--");
    assertTrue("Expected null values",model.isValueNull());
  }

  /** test for method setText(..) Null */
  public void testSetTextNullNotAllowedValueNull() throws ModelException {
    address.setCity("Juneau");
    address.setState("AK");
    address.setZip("99801");
    model.setNullAllowed(false);
    try {
      model.setText("--none--");
      fail("Expected NotInListException");
    } catch (NotInListException ex) {
    }
  }

  /** test for method isMatch(..) True */
  public void testIsMatchTrue() throws ModelException {
    address.setCity("Juneau");
    address.setState("AK");
    address.setZip("99801");
    // position list on Juneau
    zipList.next();
    assertTrue("Expected a match",model.isCurrentMatch());
  }

  /** test for method isMatch(..) False */
  public void testIsMatchFalse() throws ModelException {
    address.setCity("Juneau");
    address.setState("AK");
    address.setZip("99801");
    // position list past Juneau
    zipList.next();
    zipList.next();
    assertTrue("Expected NO match",!model.isCurrentMatch());
  }

  /**
   * test for method isValueNull(..) Null value
   */
  public void testIsValueNullTrue() throws ModelException {
    assertTrue("Expected value true for null properties",model.isValueNull());
  }

  /**
   * test for method isValueNull(..) Non Null value
   */
  public void testIsValueNullFalse() throws ModelException {
    address.setCity("Juneau");
    address.setState("AK");
    address.setZip("99801");
    assertTrue("Expected value false for non-null properties",!model.isValueNull());
  }

  /** test for method copyValues(..) */
  public void testCopyValues() throws ModelException {
    address.setCity("Juneau");
    address.setState("AK");
    address.setZip("99801");
    // Position over Ketchikan
    zipList.next();
    zipList.next();
    zipList.next();
    zipList.next();
    // Make sure different
    assertTrue("Expected NO match before copy",!model.isCurrentMatch());
    model.copyValues();
    assertTrue("Expected match after copy",model.isCurrentMatch());
  }

  /** test for method setNullValues(..) */
  public void testSetNullValues() throws ModelException {
    address.setCity("Juneau");
    address.setState("AK");
    address.setZip("99801");
    model.setNullValues();
    assertTrue("Expected null values",model.isValueNull());
  }

  /** test for method getOptionListArray(..) */
  public void testObjectArrayListSetGetText() throws Exception {
    model = new TestSelectModel();
    model.setList(new ObjectArrayValueHolder(
        new String[] {"Juneau","Douglas","Auke Bay","Ketchikan"}));
    model.setText("Douglas");
    assertEquals("Douglas", model.getText());
  }

  public void testGetSelectedItem() throws Exception {
    model = new TestSelectModel();
    model.setList(new ObjectArrayValueHolder(
        new BigDecimal[] {
          new BigDecimal(1),
          new BigDecimal(2),
          new BigDecimal(3),
          new BigDecimal(4),
          }));
    model.setText("3");
    assertEquals(new BigDecimal(3), model.getSelectedItem());
  }

  public void testSetSelectedItem() throws Exception {
    model = new TestSelectModel();
    model.setList(new ObjectArrayValueHolder(
        new BigDecimal[] {
          new BigDecimal(1),
          new BigDecimal(2),
          new BigDecimal(3),
          new BigDecimal(4),
          }));
    model.setSelectedItem(new BigDecimal(3));
    assertEquals(new BigDecimal(3), model.getSelectedItem());
  }

  /** Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {DefaultSelectModelTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
