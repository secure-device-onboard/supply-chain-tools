// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SDO type 'OwnershipVoucherEntry'.
 */
public class VoucherEntry {

  private final String bo;
  private final String sg;

  /**
   * Constructor.
   *
   * @param bo The encoded 'bo' field.
   * @param sg The encoded 'sg' field.
   */
  public VoucherEntry(final String bo, final String sg) {
    this.bo = bo;
    this.sg = sg;
  }

  /**
   * Decodes a string-encoded entry.
   *
   * @param s the encoded string
   *
   * @return the decoded VoucherEntry
   */
  public static VoucherEntry of(final String s) {
    Matcher matcher = Pattern.compile("\\{\"bo\":(.+?),\"pk\":\\[0,0,\\[0]],\"sg\":(.+?)}")
        .matcher(s);
    if (!matcher.matches()) {
      throw new IllegalArgumentException();
    }

    return new VoucherEntry(matcher.group(1), matcher.group(2));
  }

  /**
   * PM.OwnershipVoucher.en[...].bo
   */
  public String getBo() {
    return this.bo;
  }

  /**
   * PM..OwnershipVoucher.en[...].sg
   */
  public String getSg() {
    return this.sg;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "{\"bo\":" + bo
      + ",\"pk\":" + KeyCodec.encode(null)
      + ",\"sg\":" + sg
      + "}";
  }

  /**
   * The nested body ('bo') of the OwnershipVoucherEntry.
   */
  public static class Body {

    private final String hp;
    private final String hc;
    private final String pk;

    /**
     * Constructor.
     *
     * @param hp the encoded 'hp' field.
     * @param hc the encoded 'hc' field.
     * @param pk the encoded 'pk' field.
     */
    public Body(final String hp, final String hc, final String pk) {
      this.hp = hp;
      this.hc = hc;
      this.pk = pk;
    }

    /**
     * Decodes a string-encoded body.
     *
     * @param s the encoded string.
     *
     * @return the decoded body.
     */
    public static Body of(String s) {
      Matcher matcher = Pattern.compile("\\{\"hp\":(.+?),\"hc\":(.+?),\"pk\":(.+?)}").matcher(s);
      if (!matcher.matches()) {
        throw new IllegalArgumentException();
      }

      return new Body(matcher.group(1), matcher.group(2), matcher.group(3));
    }

    /**
     * PM.OwnershipVoucher.en[...].bo.pk
     */
    public String getPk() {
      return this.pk;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return "{\"hp\":" + hp
        + ",\"hc\":" + hc
        + ",\"pk\":" + pk
        + "}";
    }
  }
}
