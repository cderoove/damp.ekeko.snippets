package com.taursys.swing.test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Value Object
 * @author Marty Phelan
 * @version 1.0
 */
public class PersonVO implements Serializable {
  private String lastName;
  private String firstName;
  private int favoriteColorID;
  private BigDecimal salary;
  private String address1;
  private String address2;
  private String city;
  private String state;
  private String postalCode;
  private int personID;
  private Integer supervisorID;
  private java.util.Date birthdate;
  private static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
  private String country;
  private String notes;

  /**
   * Convert the given <code>String</code> to a <code>Date</code> using format MM/dd/yyyy.
   * @param dateString the <code>String</code> value of the <code>Date</code>
   * @return a <code>Date</code> value based on the given <code>String</code>.
   */
  protected static Date toDate(String dateString) {
    try {
      return df.parse(dateString);
    } catch (ParseException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Constructs a new PersonVO with default values.
   */
  public PersonVO() {
  }

  /**
   * Constructs a new PersonVO with given values.
   * @param lastName the last name of this person.
   * @param firstName the first name of this person.
   * @param favoriteColorID the favorite color ID for this person.
   * @param salary the monthly salary for this person.
   * @param address1 the street address line 1 for this person.
   * @param address2 the street address line 2 for this person.
   * @param city the City for this person.
   * @param state the State abbreviation for this person.
   * @param postalCode the postalCode for this person.
   * @param personID the unique identifier for this person.
   * @param supervisorID the personID for this person's supervisor.
   * @param birthdate the birthdate for this person.
   */
  public PersonVO(
      String lastName
      ,String firstName
      ,int favoriteColorID
      ,BigDecimal salary
      ,String address1
      ,String address2
      ,String city
      ,String state
      ,String postalCode
      ,int personID
      ,Integer supervisorID
      ,Date birthdate
      ) {
    this.lastName = lastName;
    this.firstName = firstName;
    this.favoriteColorID = favoriteColorID;
    this.salary = salary;
    this.address1 = address1;
    this.address2 = address2;
    this.city = city;
    this.state = state;
    this.postalCode = postalCode;
    this.personID = personID;
    this.supervisorID = supervisorID;
    this.birthdate = birthdate;
  }

  /**
   * Set the last name of this person.
   * @param lastName the last name of this person.
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Get the last name of this person.
   * @return the last name of this person.
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Set the first name of this person.
   * @param firstName the first name of this person.
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Get the first name of this person.
   * @return the first name of this person.
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Set the favorite color ID for this person.
   * @param favoriteColorID the favorite color ID for this person.
   */
  public void setFavoriteColorID(int favoriteColorID) {
    this.favoriteColorID = favoriteColorID;
  }

  /**
   * Get the favorite color ID for this person.
   * @return the favorite color ID for this person.
   */
  public int getFavoriteColorID() {
    return favoriteColorID;
  }

  /**
   * Set the monthly salary for this person.
   * @param salary the monthly salary for this person.
   */
  public void setSalary(BigDecimal salary) {
    this.salary = salary;
  }

  /**
   * Get the monthly salary for this person.
   * @return the monthly salary for this person.
   */
  public BigDecimal getSalary() {
    return salary;
  }

  /**
   * Set the street address line 1 for this person.
   * @param address1 the street address line 1 for this person.
   */
  public void setAddress1(String address1) {
    this.address1 = address1;
  }

  /**
   * Get the street address line 1 for this person.
   * @return the street address line 1 for this person.
   */
  public String getAddress1() {
    return address1;
  }

  /**
   * Set the street address line 2 for this person.
   * @param address2 the street address line 2 for this person.
   */
  public void setAddress2(String address2) {
    this.address2 = address2;
  }

  /**
   * Get the street address line 2 for this person.
   * @return the street address line 2 for this person.
   */
  public String getAddress2() {
    return address2;
  }

  /**
   * Set the City for this person.
   * @param city the City for this person.
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * Get the City for this person.
   * @return the City for this person.
   */
  public String getCity() {
    return city;
  }

  /**
   * Set the State abbreviation for this person.
   * @param state the State abbreviation for this person.
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * Get the State abbreviation for this person.
   * @return the State abbreviation for this person.
   */
  public String getState() {
    return state;
  }

  /**
   * Set the postalCode for this person.
   * @param postalCode the postalCode for this person.
   */
  public void setPostalCode(String newPostalCode) {
    this.postalCode = postalCode;
  }

  /**
   * Get the postalCode for this person.
   * @return the postalCode for this person.
   */
  public String getPostalCode() {
    return postalCode;
  }

  /**
   * Set the unique identifier for this person.
   * @param personID the unique identifier for this person.
   */
  public void setPersonID(int personID) {
    this.personID = personID;
  }

  /**
   * Get the unique identifier for this person.
   * @return the unique identifier for this person.
   */
  public int getPersonID() {
    return personID;
  }

  /**
   * Set the personID for this person's supervisor.
   * @param supervisorID the personID for this person's supervisor.
   */
  public void setSupervisorID(Integer supervisorID) {
    this.supervisorID = supervisorID;
  }

  /**
   * Get the personID for this person's supervisor.
   * @return the personID for this person's supervisor.
   */
  public Integer getSupervisorID() {
    return supervisorID;
  }

  /**
   * Set the birthdate for this person.
   * @param birthdate the birthdate for this person.
   */
  public void setBirthdate(Date birthdate) {
    this.birthdate = birthdate;
  }

  /**
   * Get the birthdate for this person.
   * @return the birthdate for this person.
   */
  public Date getBirthdate() {
    return birthdate;
  }

  /**
   * Set the country where this person was born
   * @param country the country where this person was born
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * Get the country where this person was born
   * @return the country where this person was born
   */
  public String getCountry() {
    return country;
  }

  /**
   * Set the notes for this person
   * @param notes the notes for this person
   */
  public void setNotes(String notes) {
    this.notes = notes;
  }

  /**
   * Get the notes for this person
   * @return the notes for this person
   */
  public String getNotes() {
    return notes;
  }

  // =======================================================================
  //                          Virtual Properties
  // =======================================================================

  /**
   * Get the full name for this person (first + last)
   * @return the full name for this person (first + last)
   */
  public String getFullName() {
    return firstName + " " + lastName;
  }


  public String toString() {
    return super.toString() + " [personID="+personID+" fullName="+getFullName()+"]";
  }

}
