// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.nio.CharBuffer;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encodes and decodes SDO 'ByteArray' objects.
 */
public class ArrayCodec {

  /**
   * Decodes an SDO-encoded ByteArray to a byte[].
   */
  public static byte[] decode(CharSequence cs) {

    final Pattern pat = Pattern.compile("\"([+/A-Za-z0-9]*={0,2})\"");
    final Matcher matcher = pat.matcher(cs);

    if (!matcher.matches()) {
      throw new IllegalArgumentException();
    }

    return Base64.getDecoder().decode(matcher.group(1));
  }

  /**
   * Encodes a byte[] as an SDO-encoded ByteArray.
   */
  public static String encode(final byte[] array) {
    return "\""
      + Base64.getEncoder().encodeToString(array)
      + "\"";
  }
}
