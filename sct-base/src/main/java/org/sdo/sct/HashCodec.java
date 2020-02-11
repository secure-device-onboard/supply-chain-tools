// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

/**
 * Codec for SDO {@link Hash} objects.
 */
public class HashCodec {

  /**
   * Encodes the given {@link Hash}.
   */
  public String encode(Hash hash) {
    return "["
      + IntegerCodec.encode(hash.getSize(), 8)
      + ","
      + IntegerCodec.encode(hash.getCode(), 8)
      + ","
      + ArrayCodec.encode(hash.getBytes())
      + "]";
  }
}
