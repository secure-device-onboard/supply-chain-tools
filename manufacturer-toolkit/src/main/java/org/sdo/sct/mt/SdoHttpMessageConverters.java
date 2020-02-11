// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.stereotype.Component;

@Component
class SdoHttpMessageConverters extends HttpMessageConverters {

  public SdoHttpMessageConverters() {
    super(new DiAppStartMessageConverter(),
        new DiSetCredentialsMessageConverter(),
        new DiSetHmacMessageConverter(),
        new DiDoneMessageConverter(),
        new ErrorMessageConverter());
  }
}
