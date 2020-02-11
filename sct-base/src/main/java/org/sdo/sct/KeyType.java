// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

/**
 * Public Key types, as defined in the SDO Protocol Specification.
 */
public enum KeyType {
  /**
   * No public key present: key is PKNull.
   */
  NONE(0, ""),
  /**
   * RSA2048RESTR means RSA2048 with exponent 65537. This is a restriction of the Intel DAL. The
   * Credential Tool and Owner must ensure that all keys in the Ownership Proxy meet this
   * restriction for Intel DAL devices. Note that the Owner allocates the “Owner2” key, which must
   * also meet this restriction.
   */
  RSA2048RESTR(1, "RSA"),
  /**
   * Elliptic Curve Digital Signature Algorithm P-256.
   *
   * @see <a href="http://dx.doi.org/10.6028/NIST.FIPS.186-4">FIPS 186-4</a>
   */
  ECDSA_P_256(13, "EC"),
  /**
   * Elliptic Curve Digital Signature Algorithm P-384.
   *
   * @see <a href="http://dx.doi.org/10.6028/NIST.FIPS.186-4">FIPS 186-4</a>
   */
  ECDSA_P_384(14, "EC");

  private final int id;
  private final String jceAlgorithm;

  KeyType(final int id, final String jceAlgorithm) {
    this.id = id;
    this.jceAlgorithm = jceAlgorithm;
  }

  public int getId() {
    return this.id;
  }

  public String getJceAlgorithm() {
    return this.jceAlgorithm;
  }
}
