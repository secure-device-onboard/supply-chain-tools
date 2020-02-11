// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.app;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
class WebExceptionHandler {

  @ExceptionHandler
  public ResponseEntity<String> handle(ResponseStatusException ex, HttpServletRequest request) {

    return ResponseEntity.status(ex.getStatus())
      .contentType(MediaType.TEXT_PLAIN)
      .body(ex.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<String> handle(Throwable t, HttpServletRequest request) {

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .contentType(MediaType.TEXT_PLAIN)
      .body(t.getMessage());
  }
}
