// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Codec for UUID/SDO GUID.
 */
class UuidCodec {

  /**
   * Encode the given UUID as an SDO GUID.
   */
  static String encode(final UUID uuid) {

    final byte[] bytes = new byte[16];
    final ByteBuffer bb = ByteBuffer.wrap(bytes);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return ArrayCodec.encode(bytes);
  }
}
