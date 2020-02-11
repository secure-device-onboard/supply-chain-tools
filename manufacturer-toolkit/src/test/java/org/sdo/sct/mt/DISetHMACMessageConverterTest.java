// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;

class DiSetHmacMessageConverterTest {

  @Test
  void readInternal() {
    DiSetHmacMessageConverter c = new DiSetHmacMessageConverter();
    String hmac = "[32,108,\"1IOGFrfUn1dIRRSxuMbXgCfpdagOvB90dIla9Ki+aN8=\"]";
    String input = "{\"hmac\":" + hmac + "}";
    DiSetHmac diSetHMAC = c.readInternal(
      DiSetHmac.class,
      new MockHttpInputMessage(
        new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII))));
    assertEquals(hmac, diSetHMAC.getHmac());
  }

  @Test
  void readInternal_shortInput_throwsException() {
    DiAppStartMessageConverter c = new DiAppStartMessageConverter();
    String input = "{\"hmac\":[32,108,\"1IOGFrfUn1dIRRSxuMbXgCfpdagOvB90dIla9Ki+aN=\"]}";

    assertThrows(HttpMessageNotReadableException.class, () ->
      c.readInternal(
        DiAppStart.class,
        new MockHttpInputMessage(
          new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII)))));
  }

  @Test
  void writeInternal() {
    DiSetHmacMessageConverter c = new DiSetHmacMessageConverter();

    assertThrows(UnsupportedOperationException.class,
      () -> c.writeInternal(new DiSetHmac(""), new MockHttpOutputMessage()));
  }
}
