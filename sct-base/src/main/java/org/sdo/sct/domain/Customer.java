// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * The JPA Entity representing SDO Customers.
 *
 * <p>A Customer receives vouchers, becoming their new owner through the process
 * of transfer. In order to transfer a voucher to a customer, the owner needs the
 * customer's public key.
 */
@Entity
@Table(name = "rt_customer_public_key",
    uniqueConstraints = {
    @UniqueConstraint(columnNames = {"customer_public_key_id", "customer_descriptor"})})
public class Customer {

  /**
   * The Customer's unique ID.
   */
  @Id
  @GeneratedValue
  @Column(name = "customer_public_key_id")
  private Integer id;
  /**
   * A human-readable description of this customer.
   */
  @Column(name = "customer_descriptor", nullable = false, length = 64)
  private String descriptor;

  /**
   * public key pem.
   *
   * @see Customer#getKey()
   */
  @Column(name = "public_key_pem")
  @Lob
  private String key;

  /**
   * Default constructor.
   *
   * <p>This constructor is used by the JPA framework.
   */
  public Customer() {
  }

  /**
   * Constructor.
   */
  public Customer(Integer id, String descriptor, String key) {
    this.id = id;
    this.descriptor = descriptor;
    this.key = key;
  }

  /**
   * The customer's public key text, if available.
   */
  public String getKey() {
    return this.key;
  }
}
