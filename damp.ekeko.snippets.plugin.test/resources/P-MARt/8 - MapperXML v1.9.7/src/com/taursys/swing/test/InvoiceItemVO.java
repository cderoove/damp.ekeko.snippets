package com.taursys.swing.test;

import java.math.BigDecimal;

/**
 * Value Object
 * @author Marty Phelan
 * @version 1.0
 */
public class InvoiceItemVO {
  private int itemNo;
  private int quantity;
  private String productID;
  private BigDecimal unitPrice;
  private int quantityShipped;

  /**
   * Constructs a new InvoiceItemVO with default values.
   */
  public InvoiceItemVO() {
  }

  /**
   * Constructs a new InvoiceItemVO with given values.
   * @param itemNo the unique item number for this invoice.
   * @param quantity the quantity for this invoice item.
   * @param productID the unique product identifier for this invoice item.
   * @param unitPrice the unit price for this invoice item.
   * @param quantityShipped the number of items that where shipped for this line item.
   */
  public InvoiceItemVO(
      int itemNo
      ,int quantity
      ,String productID
      ,BigDecimal unitPrice
      ,int quantityShipped
      ) {
    this.itemNo = itemNo;
    this.quantity = quantity;
    this.productID = productID;
    this.unitPrice = unitPrice;
    this.quantityShipped = quantityShipped;
  }

  /**
   * Set the unique item number for this invoice.
   * @param itemNo the unique item number for this invoice.
   */
  public void setItemNo(int itemNo) {
    this.itemNo = itemNo;
  }

  /**
   * Get the unique item number for this invoice.
   * @return the unique item number for this invoice.
   */
  public int getItemNo() {
    return itemNo;
  }

  /**
   * Set the quantity for this invoice item.
   * @param quantity the quantity for this invoice item.
   */
  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  /**
   * Get the quantity for this invoice item.
   * @return the quantity for this invoice item.
   */
  public int getQuantity() {
    return quantity;
  }

  /**
   * Set the unique product identifier for this invoice item.
   * @param productID the unique product identifier for this invoice item.
   */
  public void setProductID(String productID) {
    this.productID = productID;
  }

  /**
   * Get the unique product identifier for this invoice item.
   * @return the unique product identifier for this invoice item.
   */
  public String getProductID() {
    return productID;
  }

  /**
   * Set the unit price for this invoice item.
   * @param unitPrice the unit price for this invoice item.
   */
  public void setUnitPrice(BigDecimal unitPrice) {
    this.unitPrice = unitPrice;
  }

  /**
   * Get the unit price for this invoice item.
   * @return the unit price for this invoice item.
   */
  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  /**
   * Set the number of items that where shipped for this line item.
   * @param quantityShipped the number of items that where shipped for this line item.
   */
  public void setQuantityShipped(int quantityShipped) {
    this.quantityShipped = quantityShipped;
  }

  /**
   * Get the number of items that where shipped for this line item.
   * @return the number of items that where shipped for this line item.
   */
  public int getQuantityShipped() {
    return quantityShipped;
  }

}
