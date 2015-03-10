/**
 * JeanLucPicardVO - a specific PersonVO for testing purposes.
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
package com.taursys.swing.test;

import java.util.Date;
import java.math.BigDecimal;

/**
 * JeanLucPicardVO is a specific PersonVO for testing purposes.
 * @author Marty Phelan
 * @version 1.0
 */
public class JeanLucPicardVO extends com.taursys.swing.test.PersonVO {

  /**
   * Constructs a new JeanLucPicardVO
   */
  public JeanLucPicardVO() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Initialize bean properties
   */
  private void jbInit() throws Exception {
    this.setPersonID(18622);
    this.setLastName("Picard");
    this.setFirstName("Jean Luc");
    this.setAddress1("2033 Mission Street");
    this.setCity("San Francisco");
    this.setState("CA");
    this.setPostalCode("94110");
    this.setSalary(new BigDecimal("4500.00"));
    this.setBirthdate(toDate("7/13/2305"));
    this.setNotes("He was raised on a vinyard in France.");
  }
}
