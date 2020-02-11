// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

class ErrorMessageConverter extends AbstractHttpMessageConverter<Error> {

  ErrorMessageConverter() {
    super(MediaType.APPLICATION_JSON);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return Error.class.isAssignableFrom(clazz);
  }

  @Override
  protected Error readInternal(
      Class<? extends Error> clazz, HttpInputMessage inputMessage) {

    try {
      return Error.decode(inputMessage.getBody());
    } catch (Exception e) {
      throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
    }
  }

  @Override
  protected void writeInternal(Error error, HttpOutputMessage outputMessage)
      throws IOException {

    try (Writer w = new OutputStreamWriter(outputMessage.getBody())) {
      w.write(error.toString());
    }
  }
}
