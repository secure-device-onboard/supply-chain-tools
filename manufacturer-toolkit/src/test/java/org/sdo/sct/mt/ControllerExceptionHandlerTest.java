// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

class ControllerExceptionHandlerTest {

  @Test
  void testEmsg() throws Exception {

    ControllerExceptionHandler ceh = new ControllerExceptionHandler();
    ResponseStatusException t =
      new ResponseStatusException(HttpStatus.NOT_FOUND);
    HttpServletRequest request = mock(HttpServletRequest.class);

    given(request.getServletPath()).willReturn("");
    ResponseEntity<String> r = ceh.handle(t, request);
    Error e = Error.of(r.getBody());
    assertEquals(255, e.getEmsg());

    given(request.getServletPath()).willReturn("/mp/99/msg/100");
    r = ceh.handle(t, request);
    e = Error.of(r.getBody());
    assertEquals(100, e.getEmsg());
  }

  @Test
  void testHttpMessageConversionException() throws Exception {
    ControllerExceptionHandler ceh = new ControllerExceptionHandler();
    HttpMessageConversionException t =
      new HttpMessageNotReadableException("", mock(HttpInputMessage.class));
    HttpServletRequest request = mock(HttpServletRequest.class);

    given(request.getServletPath()).willReturn("");
    ResponseEntity<?> r = ceh.handle(t, request);
    assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());

    t = new HttpMessageNotWritableException("");
    r = ceh.handle(t, request);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class)
  void testHttpStatusCodeException(HttpStatus status) throws Exception {

    ControllerExceptionHandler ceh = new ControllerExceptionHandler();
    HttpStatusCodeException t = new HttpServerErrorException(status);
    HttpServletRequest request = mock(HttpServletRequest.class);

    given(request.getServletPath()).willReturn("");
    ResponseEntity<?> r = ceh.handle(t, request);
    assertEquals(status, r.getStatusCode());
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class)
  void testResponseStatusException(HttpStatus status) throws Exception {

    ControllerExceptionHandler ceh = new ControllerExceptionHandler();
    ResponseStatusException t = new ResponseStatusException(status);
    HttpServletRequest request = mock(HttpServletRequest.class);

    given(request.getServletPath()).willReturn("");
    ResponseEntity<?> r = ceh.handle(t, request);
    assertEquals(status, r.getStatusCode());
  }
}
