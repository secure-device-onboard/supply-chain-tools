// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class IntegerCodecTest {

  @Test
  void test() {

    Integer one = 1;
    assertEquals(one, IntegerCodec.decode(IntegerCodec.encode(one, 8), 8));

    for (Integer outOfBounds: List.of(-1, 256)) {
      assertThrows(IllegalArgumentException.class,
        () -> IntegerCodec.encode(outOfBounds, 8));
      assertThrows(IllegalArgumentException.class,
        () -> IntegerCodec.decode(outOfBounds.toString(), 8));
    }
  }
}
