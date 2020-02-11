// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

class DiAppStartMessageConverter extends AbstractHttpMessageConverter<DiAppStart> {

  DiAppStartMessageConverter() {
    super(MediaType.APPLICATION_JSON);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return DiAppStart.class.isAssignableFrom(clazz);
  }

  @Override
  protected DiAppStart readInternal(
      Class<? extends DiAppStart> clazz, HttpInputMessage inputMessage) {

    try {
      return DiAppStart.decode(inputMessage.getBody());
    } catch (Exception e) {
      throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
    }
  }

  @Override
  protected void writeInternal(DiAppStart diAppStart, HttpOutputMessage outputMessage) {
    throw new UnsupportedOperationException(); // we never need to write these
  }
}
