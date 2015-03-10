package com.taursys.swing.test;

import java.util.Date;
import java.util.ArrayList;

/**
 * Value Object for an Invoice
 * @author Marty Phelan
 * @version 1.0
 */
public class InvoiceVO {
  private int invoiceNumber;
  private Date issueDate;
  private int customerID;
  private AddressVO shippingAddress;
  private String terms;
  private ArrayList items = new ArrayList();

  /**
   * Constructs a new InvoiceVO with default values.
   */
  public InvoiceVO() {
  }

  /**
   * Constructs a new InvoiceVO with given values.
   * @param invoiceNumber the unique identifier for this invoice.
   * @param issueDate the date this invoice was issued.
   * @param customerID the id of the customer of this invoice.
   * @param shippingAddress an optional shipping address for this invoice.
   * @param terms the payment terms for this invoice.
   * @param items the line items that make up this invoice.
   */
  public InvoiceVO(
      int invoiceNumber
      ,Date issueDate
      ,int customerID
      ,AddressVO shippingAddress
      ,String terms
      ,ArrayList items
      ) {
    this.invoiceNumber = invoiceNumber;
    this.issueDate = issueDate;
    this.customerID = customerID;
    this.shippingAddress = shippingAddress;
    this.terms = terms;
    this.items = items;
  }

  /**
   * Set the unique identifier for this invoice.
   * @param invoiceNumber the unique identifier for this invoice.
   */
  public void setInvoiceNumber(int invoiceNumber) {
    this.invoiceNumber = invoiceNumber;
  }

  /**
   * Get the unique identifier for this invoice.
   * @return the unique identifier for this invoice.
   */
  public int getInvoiceNumber() {
    return invoiceNumber;
  }

  /**
   * Set the date this invoice was issued.
   * @param issueDate the date this invoice was issued.
   */
  public void setIssueDate(Date issueDate) {
    this.issueDate = issueDate;
  }

  /**
   * Get the date this invoice was issued.
   * @return the date this invoice was issued.
   */
  public Date getIssueDate() {
    return issueDate;
  }

  /**
   * Set the id of the customer of this invoice.
   * @param customerID the id of the customer of this invoice.
   */
  public void setCustomerID(int customerID) {
    this.customerID = customerID;
  }

  /**
   * Get the id of the customer of this invoice.
   * @return the id of the customer of this invoice.
   */
  public int getCustomerID() {
    return customerID;
  }

  /**
   * Set an optional shipping address for this invoice.
   * @param shippingAddress an optional shipping address for this invoice.
   */
  public void setShippingAddress(AddressVO shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  /**
   * Get an optional shipping address for this invoice.
   * @return an optional shipping address for this invoice.
   */
  public AddressVO getShippingAddress() {
    return shippingAddress;
  }

  /**
   * Set the payment terms for this invoice.
   * @param terms the payment terms for this invoice.
   */
  public void setTerms(String terms) {
    this.terms = terms;
  }

  /**
   * Get the payment terms for this invoice.
   * @return the payment terms for this invoice.
   */
  public String getTerms() {
    return terms;
  }

  /**
   * Set the line items that make up this invoice.
   * @param items the line items that make up this invoice.
   */
  public void setItems(ArrayList items) {
    this.items = items;
  }

  /**
   * Get the line items that make up this invoice.
   * @return the line items that make up this invoice.
   */
  public ArrayList getItems() {
    return items;
  }

}
