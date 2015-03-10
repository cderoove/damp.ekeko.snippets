/**
 * VOComparator - a Comparator for sorting value objects in a VOListValueHolder.
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
package com.taursys.model;

import java.util.*;
import com.taursys.debug.Debug;

/**
 * <p><code>VOComparator</code> is a <code>Comparator</code> for sorting value
 * objects in a <code>VOListValueHolder</code>. This can sort by one or more
 * properties of the value object. Ascending or descending can be specified
 * for each property (default is ascending).
 * </p>
 * <p>To sort by a single property, use the <code>setPropertyName</code> method.
 * To sort by more than one property use the <code>setPropertyNames</code>
 * method and pass it a <code>String[]</code> array of property names.
 * </p>
 * <p>To change the order for a single property, use the
 * <code>setAscendingOrder</code> method (<code>true</code>=Ascending
 * <code>false</code>=descending). To change the order for multiple properties,
 * use the <code>setAscendingOrders</code> method and pass it a
 * <code>boolean[]</code> array. The order of the ascending order indicators
 * should match the array of property names.
 * </p>
 * <p>This <code>VOComparator</code> is then attached to the
 * <code>VOListValueHolder</code> using the <code>setComparator</code> method.
 * IMPORTANT: This VOComparator can only be used with one VOListValueHolder
 * at a time - it cannot be shared.
 * </p>
 * <p>Example - sort ascending by a lastName</p>
 * <pre>
 *    VOListValueHolder holder = new VOListValueHolder();
 *    VOComparator comparator = new VOComparator();
 *    ...
 *    comparator.setPropertyName("lastName");
 *    holder.setComparator(comparator);
 * </pre>
 * <p>Example - sort ascending by a salary(descending), yearsWorked(ascending)</p>
 * <pre>
 *    VOListValueHolder holder = new VOListValueHolder();
 *    VOComparator comparator = new VOComparator();
 *    ...
 *    comparator.setPropertyNames(new String[] {"salary", "yearsWorked"});
 *    comparator.setAscendingOrders(new boolean[] {false, true});
 *    holder.setComparator(comparator);
 * </pre>
 * @author Marty Phelan
 * @version 1.0
 */
public class VOComparator implements Comparator {
  private VOValueHolder holder;
  private String[] propertyNames;
  private boolean[] ascendingOrders;
  private boolean ordering;

  /**
   * Constructs a new VOComparator
   */
  public VOComparator() {
  }

  /**
   * Compare the two given value objects based on the <code>propertyName(s)</code>
   * and <code>ascendingOrder(s)</code>. Return 0 if they are equal, negative
   * value if <code>o1</code> should appear before <code>o2</code> in the list,
   * or positive if <code>o1</code> should appear after <code>o2</code> in the
   * list. If both <code>o1</code> and <code>o2</code> are null, they are
   * considered equal. If one is null and the other not null, the null value
   * will appear before the non-null value in the list. The same null rule holds
   * true for the property values.
   * @param o1 the object to base the comparison on
   * @param o2 the object to compare to
   * @return 0 if o1 and o2 could appear in same position, negative if o1
   * should appear before o2, and positive if o1 should appear after o2.
   */
  public int compare(Object o1, Object o2) {
    // Cannot compare if valueHolder is null - say equals and warn
    if (holder == null) {
      Debug.error("VOComparator.compare: Cannot compare - VOValueHolder is null.");
      return 0;
    }
    // Cannot compare if property names are null or empty - say equals and warn
    if (propertyNames == null || propertyNames.length == 0) {
      Debug.error("VOComparator.compare: Cannot compare - sort property name(s) is missing.");
      return 0;
    }
    // Do null test first
    int result = nullTest(o1, o2);
    // continue if equal
    if (result == Integer.MAX_VALUE) {
      // Iterate through properties
      for (int i = 0; i < propertyNames.length; i++) {
        // Cannot compare if propertyName is null
        if (propertyNames[i] == null) {
          Debug.error("VOComparator.compare: Cannot compare - sort property name #"
              + i + " is null.");
          return 0;
        } else {
          try {
            Comparable value1 = (Comparable)holder.getPropertyValue(
                propertyNames[i], o1);
            Comparable value2 = (Comparable)holder.getPropertyValue(
                propertyNames[i], o2);
            // do null test first
            result = nullTest(value1, value2);
            if (result == Integer.MAX_VALUE) {
              result = value1.compareTo(value2);
              if (result != 0)
                return isAscendingOrder(i) ? result : -result;
            } else {
              return result;
            }
          } catch (ModelException ex) {
            Debug.error("VOComparator.compare: cannot compare - "
                + ex.getMessage(), ex);
            return 0;
          }
        }
      }
    }
    return result;
  }

  private int nullTest(Object o1, Object o2) {
    if ((o1 != null && o2 != null))
      return Integer.MAX_VALUE;
    if ((o1 == null && o2 == null))
      return 0;
    if (o1 == null)
      return -1;
    else
      return 1;
  }

  /**
   * Set the VOValueHolder that this comparator is linked to. This holder
   * provides accessors to the properties of the value object.
   * @param holder the VOValueHolder that this comparator is linked to.
   */
  public void setVOValueHolder(VOValueHolder holder) {
    this.holder = holder;
  }

  /**
   * Get the VOValueHolder that this comparator is linked to. This holder
   * provides accessors to the properties of the value object.
   * @return the VOValueHolder that this comparator is linked to.
   */
  public VOValueHolder getVOValueHolder() {
    return holder;
  }

  /**
   * Set the first or only propertyName used for the comparison.
   * @param propertyName the first or only propertyName used for the comparison.
   */
  public void setPropertyName(String propertyName) {
    if (propertyNames == null || propertyNames.length == 0)
      propertyNames = new String[] {propertyName};
    else
      propertyNames[0] = propertyName;
  }

  /**
   * Get the first or only propertyName used for the comparison.
   * @return the first or only propertyName used for the comparison.
   */
  public String getPropertyName() {
    if (propertyNames == null || propertyNames.length == 0)
      return null;
    else
      return propertyNames[0];
  }

  /**
   * Set the ascending order flag for the first or only propertyName.
   * The default is true.
   * @param ascendingOrder the ascending order flag for the first or only
   * propertyName.
   */
  public void setAscendingOrder(boolean ascendingOrder) {
    if (ascendingOrders == null || ascendingOrders.length == 0)
      ascendingOrders = new boolean[] {ascendingOrder};
    else
      ascendingOrders[0] = ascendingOrder;
  }

  /**
   * Get the ascending order flag for the first or only propertyName.
   * The default is true.
   * @return the ascending order flag for the first or only
   * propertyName.
   */
  public boolean isAscendingOrder() {
    return isAscendingOrder(0);
  }

  private boolean isAscendingOrder(int index) {
    if (ascendingOrders == null || ascendingOrders.length <= index)
      return true;
    else
      return ascendingOrders[0];
  }

  /**
   * Set the propertyNames (in order) used for the comparison.
   * @param propertyNames the propertyNames (in order) used for the comparison.
   */
  public void setPropertyNames(String[] propertyNames) {
    this.propertyNames = propertyNames;
  }

  /**
   * Get the propertyNames (in order) used for the comparison.
   * @return the propertyNames (in order) used for the comparison.
   */
  public String[] getPropertyNames() {
    return propertyNames;
  }

  /**
   * Set the ascending order flags for each of the properties.
   * The default is true(ascending) if ommitted.
   * @param ascendingOrders the ascending order flags for each of the properties.
   */
  public void setAscendingOrders(boolean[] ascendingOrders) {
    this.ascendingOrders = ascendingOrders;
  }

  /**
   * Get the ascending order flags for each of the properties.
   * The default is true(ascending) if ommitted.
   * @return the ascending order flags for each of the properties.
   */
  public boolean[] getAscendingOrders() {
    return ascendingOrders;
  }
}
