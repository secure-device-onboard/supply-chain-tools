// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ErrorTest {

  @ParameterizedTest
  @ValueSource(strings = {
    "{\"ec\":0,\"emsg\":0,\"em\":\"hello world\"}",
    "{\"ec\":65535,\"emsg\":0,\"em\":\"hello world\"}",
    "{\"ec\":0,\"emsg\":255,\"em\":\"hello world\"}"
  })
  void testParse(String s) throws Exception {

    Error e = Error.of(s);
    assertNotNull(e);
    assertEquals(s, e.toString());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "{\"ec\":-1,\"emsg\":0,\"em\":\"hello world\"}",
    "{\"ec\":0,\"emsg\":-1,\"em\":\"hello world\"}",
    "{\"ec\":65536,\"emsg\":0,\"em\":\"hello world\"}",
    "{\"ec\":0,\"emsg\":256,\"em\":\"hello world\"}"
  })
  void testFailedParse(String s) {

    assertThrows(IllegalArgumentException.class, () -> Error.of(s));
  }
}
