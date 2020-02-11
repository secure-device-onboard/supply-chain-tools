// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sdo.sct.StringCodec;
import org.springframework.util.StreamUtils;

class DiAppStart {

  private final String mstring;

  DiAppStart(final String m) {
    this.mstring = m;
  }

  static DiAppStart decode(final InputStream inputStream) throws IOException {

    final Pattern pat = Pattern.compile("\\{\"m\":\"([^\"]*)\"}");
    final Matcher matcher =
        pat.matcher(StreamUtils.copyToString(inputStream, StandardCharsets.US_ASCII));

    if (!matcher.matches()) {
      throw new IllegalArgumentException();
    }

    return new DiAppStart(StringCodec.decode(CharBuffer.wrap(matcher.group(1))));
  }

  String getMstring() {
    return mstring;
  }
}
