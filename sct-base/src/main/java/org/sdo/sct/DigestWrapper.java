// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.function.Function;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;

/**
 * Wraps a BouncyCastle {@link Digest} to return SDO's {@link Hash} type.
 */
public class DigestWrapper {

  private final Digest digest;
  private Function<byte[], Hash> builder;

  protected DigestWrapper(Digest digest, Function<byte[], Hash> builder) {
    this.digest = digest;
    this.builder = builder;
  }

  /**
   * Complete hash.
   *
   * @see Digest#doFinal(byte[], int)
   */
  public Hash doFinal() {
    byte[] result = new byte[digest.getDigestSize()];
    digest.doFinal(result, 0);
    return builder.apply(result);
  }

  /**
   * Reset the digest.
   *
   * @see Digest#reset()
   */
  public void reset() {
    digest.reset();
  }

  /**
   * Update the digest.
   *
   * @see Digest#update(byte[], int, int)
   */
  public DigestWrapper update(byte[] input) {
    digest.update(input, 0, input.length);
    return this;
  }

  /**
   * Update the digest.
   *
   * @see Digest#update(byte)
   */
  public DigestWrapper update(byte b) {
    digest.update(b);
    return this;
  }

  static class Sha256 extends DigestWrapper {

    Sha256() {
      super(new SHA256Digest(), Hash.Sha256::new);
    }
  }

  static class Sha384 extends DigestWrapper {

    Sha384() {
      super(new SHA384Digest(), Hash.Sha384::new);
    }
  }
}
