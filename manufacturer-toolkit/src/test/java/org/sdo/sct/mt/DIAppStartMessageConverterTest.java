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

class DiAppStartMessageConverterTest {

  @Test
  void readInternal() {
    DiAppStartMessageConverter c = new DiAppStartMessageConverter();
    String input = "{\"m\":\"hello world\\u0021\"}";
    DiAppStart diAppStart = c.readInternal(
      DiAppStart.class,
      new MockHttpInputMessage(
        new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII))));
    assertEquals("hello world!", diAppStart.getMstring());
  }

  @Test
  void readInternal_shortInput_throwsException() {
    DiAppStartMessageConverter c = new DiAppStartMessageConverter();
    String input = "{\"getMstring\":\"hello world\\u0021\"";

    assertThrows(HttpMessageNotReadableException.class, () ->
      c.readInternal(
        DiAppStart.class,
        new MockHttpInputMessage(
          new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII)))));
  }

  @Test
  void writeInternal() {
    DiAppStartMessageConverter c = new DiAppStartMessageConverter();

    assertThrows(UnsupportedOperationException.class, () -> c.writeInternal(new DiAppStart("hello world!"), new MockHttpOutputMessage()));
  }
}
