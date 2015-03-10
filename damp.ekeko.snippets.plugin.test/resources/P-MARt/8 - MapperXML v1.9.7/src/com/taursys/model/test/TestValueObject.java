/**
 * TestValueObject - ValueObject to use with tests
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
public class TestValueObject {

  public TestValueObject() {
  }
  private String fullName;
  private java.util.Date birthdate;
  private int dependents;
  private java.math.BigDecimal salary;
  private boolean active;
  private com.taursys.model.test.TestAddressValueObject homeAddress;
  private java.util.Date createDate;
  private com.taursys.model.test.TestValueObject supervisor;
  private Integer anInteger;
  public String getFullName() {
    return fullName;
  }
  public void setFullName(String newFullName) {
    fullName = newFullName;
  }
  public void setBirthdate(java.util.Date newBirthdate) {
    birthdate = newBirthdate;
  }
  public java.util.Date getBirthdate() {
    return birthdate;
  }
  public void setDependents(int newDependents) {
    dependents = newDependents;
  }
  public int getDependents() {
    return dependents;
  }
  public void setSalary(java.math.BigDecimal newSalary) {
    salary = newSalary;
  }
  public java.math.BigDecimal getSalary() {
    return salary;
  }
  public void setActive(boolean newActive) {
    active = newActive;
  }
  public boolean isActive() {
    return active;
  }
  public void setHomeAddress(com.taursys.model.test.TestAddressValueObject newHomeAddress) {
    homeAddress = newHomeAddress;
  }
  public com.taursys.model.test.TestAddressValueObject getHomeAddress() {
    return homeAddress;
  }
  public void setCreateDate(java.util.Date newCreateDate) {
    createDate = newCreateDate;
  }
  public java.util.Date getCreateDate() {
    return createDate;
  }
  public String toString() {
    return fullName;
  }
  public void setSupervisor(com.taursys.model.test.TestValueObject newSupervisor) {
    supervisor = newSupervisor;
  }
  public com.taursys.model.test.TestValueObject getSupervisor() {
    return supervisor;
  }
  public void setAnInteger(Integer newAnInteger) {
    anInteger = newAnInteger;
  }
  public Integer getAnInteger() {
    return anInteger;
  }
}
