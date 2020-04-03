// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.domain;

import java.nio.ByteBuffer;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.sdo.sct.ResourceBundleHolder;
import org.sdo.sct.Voucher;
import org.sdo.sct.VoucherHeader;

/**
 * The JPA Entity containing SDO vouchers.
 *
 * <p>These entries record incoming vouchers and their {@link Customer} assignments.
 */
@Entity
@Table(name = "rt_ownership_voucher")
public class OwnershipVoucherEntry {

  private static ResourceBundleHolder resourceBundleHolder =
      new ResourceBundleHolder(OwnershipVoucherEntry.class.getName());

  /**
   * The device serial no.
   *
   * @see DeviceState#getDeviceSerialNo()
   */
  @Id
  @Column(name = "device_serial_no", length = Limits.SERIAL_NUMBER_MAXLEN)
  private String deviceSerialNo;

  /**
   * The Ownership Voucher Entry.
   *
   * @see OwnershipVoucherEntry#getVoucher()
   */
  @Column(name = "voucher", nullable = false)
  @Lob
  private String voucher;

  /**
   * The customer_public_key_id column.
   *
   * @see OwnershipVoucherEntry#getCustomer()
   */
  @ManyToOne
  @JoinColumn(name = "customer_public_key_id")
  private Customer customer;

  /**
   * The device uuid. The uuid value is derived from
   * the voucher contents.
   *
   */
  @Column(name = "uuid", length = Limits.DEVICE_UUID_MAXLEN)
  private String deviceUuid;

  public OwnershipVoucherEntry() {
  }

  /**
   * OwnershipVoucherEntry.
   *
   * @param deviceSerialNo deviceSerialNo
   * @param voucher voucher
   * @param customer customer
   */
  public OwnershipVoucherEntry(final String deviceSerialNo, final String voucher,
      final Customer customer) {

    if (null == deviceSerialNo) {
      throw new IllegalArgumentException(resourceBundleHolder.get().getString("serial.is.null"));
    }

    if (Limits.SERIAL_NUMBER_MAXLEN < deviceSerialNo.length()) {
      throw new IllegalArgumentException(resourceBundleHolder.get().getString("serial.too.long"));
    }

    this.deviceSerialNo = deviceSerialNo;
    this.voucher = voucher;
    this.customer = customer;

    // set the device uuid from the voucher contents
    ByteBuffer bb =
        ByteBuffer.wrap(VoucherHeader.of(Voucher.of(voucher).getOh()).getGuid().getBytes());
    long high = bb.getLong();
    long low = bb.getLong();
    this.deviceUuid = new UUID(high, low).toString();
  }

  /**
   * The {@link Customer} to which the voucher has been assigned.
   *
   * <p>If this field is NULL, the voucher remains the property of this reseller.
   * REST GETs will return the voucher as-is.
   *
   * <p>If this field is non-NULL, the voucher has been assigned to the linked
   * {@link Customer}, and REST GETs will return the amended voucher.
   */
  public Customer getCustomer() {
    return this.customer;
  }

  /**
   * OwnershipVoucherEntry customer.
   *
   * @see OwnershipVoucherEntry#getCustomer()
   */
  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  /**
   * The text of the voucher.
   *
   * <p>This text is always an 'input voucher' - that is, a voucher owned by this reseller.
   * If the voucher is assigned to another owner, the {@link Customer} record will be non-null
   * and the assigned voucher is gettable via REST GET.
   *
   * <p>Assigned vouchers are not recorded in the database.  This enables 'undo' scenarios.
   */
  public String getVoucher() {
    return this.voucher;
  }
}
