/**
 * TestAddressValueObject - ValueObject to use with tests
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

/**
 * ValueObject to use with tests
 */
public class TestAddressValueObject {
  private String address1;
  private String address2;
  private String city;
  private String state;
  private String zip;
  private java.util.Date createDate;

  public TestAddressValueObject() {
  }
  public TestAddressValueObject(String newCity, String newState, String newZip) {
    zip = newZip;
    city = newCity;
    state = newState;
  }
  public String getAddress1() {
    return address1;
  }
  public void setAddress1(String newAddress1) {
    address1 = newAddress1;
  }
  public void setAddress2(String newAddress2) {
    address2 = newAddress2;
  }
  public String getAddress2() {
    return address2;
  }
  public void setCity(String newCity) {
    city = newCity;
  }
  public String getCity() {
    return city;
  }
  public void setState(String newState) {
    state = newState;
  }
  public String getState() {
    return state;
  }
  public void setZip(String newZip) {
    zip = newZip;
  }
  public String getZip() {
    return zip;
  }
  public void setCreateDate(java.util.Date newCreateDate) {
    createDate = newCreateDate;
  }
  public java.util.Date getCreateDate() {
    return createDate;
  }
}
