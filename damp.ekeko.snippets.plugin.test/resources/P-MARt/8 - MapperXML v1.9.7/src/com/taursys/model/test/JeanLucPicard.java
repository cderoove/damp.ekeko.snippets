/**
 * JeanLucPicard - A TestValueObject
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

import java.text.*;
import java.util.*;
import java.math.*;

/**
 * JeanLucPicard is a TestValueObject
 * @author Marty Phelan
 * @version 1.0
 */
public class JeanLucPicard extends com.taursys.model.test.TestValueObject {

  /**
   * Constructs a new JeanLucPicard
   */
  public JeanLucPicard() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
    this.setFullName("Jean Luc Picard");
    this.setActive(true);
    this.setBirthdate(df.parse("03/12/2002"));
    this.setCreateDate(df.parse("01/01/2000"));
    this.setDependents(1);
    this.setSalary(BigDecimal.valueOf(4000L));
  }
}
