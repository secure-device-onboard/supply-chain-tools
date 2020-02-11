// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

class DiSetHmacMessageConverter extends AbstractHttpMessageConverter<DiSetHmac> {

  DiSetHmacMessageConverter() {
    super(MediaType.APPLICATION_JSON);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return DiSetHmac.class.isAssignableFrom(clazz);
  }

  @Override
  protected DiSetHmac readInternal(
      Class<? extends DiSetHmac> clazz, HttpInputMessage inputMessage) {

    try {
      return DiSetHmac.decode(inputMessage.getBody());
    } catch (Exception e) {
      throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
    }
  }

  @Override
  protected void writeInternal(DiSetHmac diSetHmac, HttpOutputMessage outputMessage) {
    throw new UnsupportedOperationException(); // we never need to write these
  }
}
