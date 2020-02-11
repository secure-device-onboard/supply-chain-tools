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

class DiSetCredentialsMessageConverter extends
    AbstractHttpMessageConverter<DiSetCredentials> {

  DiSetCredentialsMessageConverter() {
    super(MediaType.APPLICATION_JSON);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return DiSetCredentials.class.isAssignableFrom(clazz);
  }

  @Override
  protected DiSetCredentials readInternal(
      Class<? extends DiSetCredentials> clazz, HttpInputMessage inputMessage)
      throws HttpMessageNotReadableException {

    throw new UnsupportedOperationException(); // we never need to read these
  }

  @Override
  protected void writeInternal(DiSetCredentials diSetCredentials, HttpOutputMessage outputMessage)
      throws IOException {

    try (Writer w = new OutputStreamWriter(outputMessage.getBody())) {
      w.write(diSetCredentials.toString());
    }
  }
}
