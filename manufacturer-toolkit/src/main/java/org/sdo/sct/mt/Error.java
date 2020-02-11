// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sdo.sct.IntegerCodec;
import org.sdo.sct.StringCodec;
import org.springframework.util.StreamUtils;

class Error {

  private final int ec;
  private final int emsg;
  private final String em;

  private static final int EC_BITS = 16;
  private static final int EMSG_BITS = 8;

  Error(final int ec, final int emsg, final String em) {
    this.ec = ec;
    this.emsg = emsg;
    this.em = em;
  }

  static Error decode(InputStream inputStream) throws IOException {

    final Pattern pat = Pattern.compile("\\{\"ec\":(\\d+),\"emsg\":(\\d+),\"em\":\"([^\"]*)\"}");
    final Matcher matcher =
        pat.matcher(StreamUtils.copyToString(inputStream, StandardCharsets.US_ASCII));

    if (!matcher.matches()) {
      throw new IllegalArgumentException();
    }

    return new Error(
      IntegerCodec.decode(matcher.group(1), EC_BITS),
      IntegerCodec.decode(matcher.group(2), EMSG_BITS),
      StringCodec.decode(CharBuffer.wrap(matcher.group(3))));
  }

  static Error of(String s) throws IOException {
    return decode(new ByteArrayInputStream(s.getBytes()));
  }

  @Override
  public String toString() {
    return "{"
      + "\"ec\":"
      + IntegerCodec.encode(ec, EC_BITS)
      + ",\"emsg\":"
      + IntegerCodec.encode(emsg, EMSG_BITS)
      + ",\"em\":\""
      + (null != em ? StringCodec.encode(em) : "")
      + "\"}";
  }

  int getEc() {
    return ec;
  }

  String getEm() {
    return em;
  }

  int getEmsg() {
    return emsg;
  }
}
