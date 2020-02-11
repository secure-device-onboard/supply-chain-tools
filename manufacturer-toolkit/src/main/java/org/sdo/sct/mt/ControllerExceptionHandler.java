// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
@SuppressWarnings("unused")
public class ControllerExceptionHandler {

  /**
   * Constructor.
   *
   * @param t t
   * @param request request
   */
  @ExceptionHandler
  public ResponseEntity<String> handle(Throwable t, HttpServletRequest request) {

    final HttpStatus status;
    if (t instanceof ResponseStatusException) {
      status = ((ResponseStatusException) t).getStatus();
    } else if (t instanceof HttpStatusCodeException) {
      status = ((HttpStatusCodeException) t).getStatusCode();
    } else if (t instanceof HttpMessageNotReadableException) {
      status = HttpStatus.BAD_REQUEST;
    } else {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    int emsg = 255;
    if (null != request) {
      Matcher matcher = Pattern.compile("/mp/\\d+/msg/(\\d+)").matcher(request.getServletPath());
      if (matcher.matches()) {
        emsg = Integer.parseUnsignedInt(matcher.group(1));
      }
    }

    int ec = 255;
    if (status.is4xxClientError()) {
      ec = (t instanceof HttpMessageNotReadableException) ? 100 : 101;
    }

    String em;
    if (!(null == t.getMessage() || t.getMessage().isBlank())) {
      em = t.getMessage();
    } else {
      em = t.toString();
    }
    Error error = new Error(ec, emsg, em);

    return ResponseEntity.status(status)
      .contentType(MediaType.APPLICATION_JSON)
      .body(error.toString());
  }
}
