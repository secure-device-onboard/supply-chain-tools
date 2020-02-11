// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StreamUtils;

class DiSetHmac {

  private final String hmac;

  DiSetHmac(final String hmac) {
    this.hmac = hmac;
  }

  static DiSetHmac decode(InputStream inputStream) throws IOException {

    final Pattern pat = Pattern.compile("\\{\"hmac\":(.*?)}");
    final Matcher matcher =
        pat.matcher(StreamUtils.copyToString(inputStream, StandardCharsets.US_ASCII));

    if (!matcher.matches()) {
      throw new IllegalArgumentException();
    }

    return new DiSetHmac(matcher.group(1));
  }

  String getHmac() {
    return hmac;
  }
}
