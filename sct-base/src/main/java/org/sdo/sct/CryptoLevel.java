// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Signature;
import java.util.function.Supplier;

/**
 * Enumerates SDO security settings which vary by crypto level.
 *
 * <p>The SDO Protocol Specification defines sets of settings, called 'crypto levels'
 * in these sources.  These crypto levels are mutually exclusive.  If an algorithm
 * from one security level is used, all other algorithms must be selected
 * from that same level.
 *
 * <p>Version 1.12 of the protocol specification describes these crypto levels as
 * "SDO 1.0 & 1.1" and "Future Crypto", respectively.  They are described
 * in detail in appendix C of that document.
 *
 * <p>Since 'future' is a slippery word, these sources name crypto levels
 * by the version of the SDO Protocol Specification in which they first appear.
 */
public enum CryptoLevel {
  CRYPTO_1_0(
    DigestWrapper.Sha256::new,
    "SHA256withECDSA",
    "SHA256withRSA"),
  CRYPTO_1_12(
    DigestWrapper.Sha384::new,
    "SHA384withECDSA",
    "SHA384withRSA");

  private final Supplier<DigestWrapper> digestWrapperBuilder;
  private final String ecSignatureAlgo;
  private final String rsaSignatureAlgo;

  CryptoLevel(
      Supplier<DigestWrapper> digestWrapperBuilder,
      String ecSignatureAlgo,
      String rsaSignatureAlgo) {

    this.digestWrapperBuilder = digestWrapperBuilder;
    this.ecSignatureAlgo = ecSignatureAlgo;
    this.rsaSignatureAlgo = rsaSignatureAlgo;
  }

  /**
   * Returns CryptoLevel of type requested.
   *
   * @return the CryptoLevel of the given key type.
   */
  public static CryptoLevel of(KeyType keyType) {
    switch (keyType) {

      case NONE:
      case RSA2048RESTR:
      case ECDSA_P_256:
        return CRYPTO_1_0;

      case ECDSA_P_384:
        return CRYPTO_1_12;

      default:
        throw new RuntimeException("unexpected switch default"); // porgramming error - missed case
    }
  }

  /**
   * Builds a new {@link DigestWrapper}.
   */
  public DigestWrapper buildDigestWrapper() {
    return digestWrapperBuilder.get();
  }

  /**
   * Builds a new {@link Signature} appropriate for the given key.
   */
  public Signature buildSignature(final Key key, final Provider provider)
      throws NoSuchAlgorithmException {
    return Signature.getInstance(getSignatureAlgorithm(key), provider);
  }

  /**
   * Returns the JCE {@link Signature} algorithm name appropriate for the given key.
   */
  public String getSignatureAlgorithm(final Key key) throws NoSuchAlgorithmException {
    switch (key.getAlgorithm()) {
      case "EC":
        return ecSignatureAlgo;
      case "RSA":
        return rsaSignatureAlgo;
      default:
        throw new NoSuchAlgorithmException(key.getAlgorithm());
    }
  }
}
