// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class KeyTypeCodecTest {

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "hello world!", "-1", "255", "256"})
  void testError(String encoded) {
    assertThrows(IllegalArgumentException.class, () -> KeyTypeCodec.decode(encoded));
  }

  @ParameterizedTest
  @EnumSource(KeyType.class)
  void testReflexive(KeyType a) {
    assertEquals(a, KeyTypeCodec.decode(KeyTypeCodec.encode(a)));
  }
}
