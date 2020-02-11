// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;

class ErrorMessageConverterTest {

  @Test
  void readInternal() {
    ErrorMessageConverter c = new ErrorMessageConverter();
    String input = "{\"ec\":0,\"emsg\":255,\"em\":\"hello world!\"}";
    Error error = c.readInternal(
      Error.class,
      new MockHttpInputMessage(
        new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII))));
    assertEquals(0, error.getEc());
    assertEquals(255, error.getEmsg());
    assertEquals("hello world!", error.getEm());
  }

  @Test
  void readInternal_shortInput_throwsException() {
    ErrorMessageConverter c = new ErrorMessageConverter();
    String input = "{\"ec\":0,\"emsg\":255,\"em\":\"hello world!\"";

    assertThrows(HttpMessageNotReadableException.class, () ->
      c.readInternal(
        Error.class,
        new MockHttpInputMessage(
          new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII)))));
  }

  @Test
  void readInternal_numberOutOfBounds_throwsException() {
    ErrorMessageConverter c = new ErrorMessageConverter();
    String input = "{\"ec\":0,\"emsg\":256,\"em\":\"hello world!\"}";

    assertThrows(HttpMessageNotReadableException.class, () ->
      c.readInternal(
        Error.class,
        new MockHttpInputMessage(
          new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII)))));
  }

  @Test
  void writeInternal() throws IOException {
    ErrorMessageConverter c = new ErrorMessageConverter();
    MockHttpOutputMessage m = new MockHttpOutputMessage();

    c.writeInternal(new Error(255, 255, "hello world!"), m);
    assertEquals("{\"ec\":255,\"emsg\":255,\"em\":\"hello world!\"}", m.getBodyAsString());
  }
}
