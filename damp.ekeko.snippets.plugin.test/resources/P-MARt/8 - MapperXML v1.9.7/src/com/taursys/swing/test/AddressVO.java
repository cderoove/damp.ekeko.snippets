package com.taursys.swing.test;

/**
 * Value Object
 * @author Marty Phelan
 * @version 1.0
 */
public class AddressVO {
  private String addressLine1;
  private String addressLine2;
  private String city;
  private String state;
  private String postalCode;
  private String country;
  private String attn;

  /**
   * Constructs a new AddressVO with default values.
   */
  public AddressVO() {
  }

  /**
   * Constructs a new AddressVO with given values.
   * @param addressLine1 the first line of this address.
   * @param addressLine2 the second line of this Address.
   * @param city the city for this address.
   * @param state the state or province for this address.
   * @param postalCode the postal code for this address.
   * @param country the country for this address.
   * @param attn the optional attention line for this address.
   */
  public AddressVO(
      String addressLine1
      ,String addressLine2
      ,String city
      ,String state
      ,String postalCode
      ,String country
      ,String attn
      ) {
    this.addressLine1 = addressLine1;
    this.addressLine2 = addressLine2;
    this.city = city;
    this.state = state;
    this.postalCode = postalCode;
    this.country = country;
    this.attn = attn;
  }

  /**
   * Set the first line of this address.
   * @param addressLine1 the first line of this address.
   */
  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
  }

  /**
   * Get the first line of this address.
   * @return the first line of this address.
   */
  public String getAddressLine1() {
    return addressLine1;
  }

  /**
   * Set the second line of this Address.
   * @param addressLine2 the second line of this Address.
   */
  public void setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
  }

  /**
   * Get the second line of this Address.
   * @return the second line of this Address.
   */
  public String getAddressLine2() {
    return addressLine2;
  }

  /**
   * Set the city for this address.
   * @param city the city for this address.
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * Get the city for this address.
   * @return the city for this address.
   */
  public String getCity() {
    return city;
  }

  /**
   * Set the state or province for this address.
   * @param state the state or province for this address.
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * Get the state or province for this address.
   * @return the state or province for this address.
   */
  public String getState() {
    return state;
  }

  /**
   * Set the postal code for this address.
   * @param postalCode the postal code for this address.
   */
  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  /**
   * Get the postal code for this address.
   * @return the postal code for this address.
   */
  public String getPostalCode() {
    return postalCode;
  }

  /**
   * Set the country for this address.
   * @param country the country for this address.
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * Get the country for this address.
   * @return the country for this address.
   */
  public String getCountry() {
    return country;
  }

  /**
   * Set the optional attention line for this address.
   * @param attn the optional attention line for this address.
   */
  public void setAttn(String attn) {
    this.attn = attn;
  }

  /**
   * Get the optional attention line for this address.
   * @return the optional attention line for this address.
   */
  public String getAttn() {
    return attn;
  }

}
