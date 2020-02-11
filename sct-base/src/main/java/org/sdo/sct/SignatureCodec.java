// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

/**
 * Codec for SDO 'Signature' objects.
 */
public class SignatureCodec {

  /**
   * Encode a byte array as an SDO Signature.
   *
   * @param sg The raw signature bytes.
   *
   * @return The encoded Signature.
   */
  public static String encode(byte[] sg) {
    return "["
      + IntegerCodec.encode(sg.length, 16)
      + ","
      + ArrayCodec.encode(sg)
      + "]";
  }
}
