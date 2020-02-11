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

class DiDoneMessageConverter extends AbstractHttpMessageConverter<DiDone> {

  DiDoneMessageConverter() {
    super(MediaType.APPLICATION_JSON);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return DiDone.class.isAssignableFrom(clazz);
  }

  @Override
  protected DiDone readInternal(
      Class<? extends DiDone> clazz, HttpInputMessage inputMessage)
      throws HttpMessageNotReadableException {

    throw new UnsupportedOperationException(); // we never need to read these
  }

  @Override
  protected void writeInternal(DiDone diDone, HttpOutputMessage outputMessage)
      throws IOException {

    try (Writer w = new OutputStreamWriter(outputMessage.getBody())) {
      w.write(diDone.toString());
    }
  }
}
