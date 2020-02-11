// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;

class DiDoneMessageConverterTest {

  @Test
  void readInternal() {
    DiDoneMessageConverter c = new DiDoneMessageConverter();
    assertThrows(UnsupportedOperationException.class, () ->
      c.readInternal(DiDone.class, new MockHttpInputMessage(new byte[0])));
  }

  @Test
  void writeInternal() {
    DiDoneMessageConverter c = new DiDoneMessageConverter();
    assertDoesNotThrow(() ->
      c.writeInternal(new DiDone(), new MockHttpOutputMessage()));
  }
}
