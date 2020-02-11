// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Encodes and decodes SDO Strings.
 */
public class StringCodec {

  private static final int ASCII_PRINTABLE_MAX = 0x7e;
  private static final int ASCII_PRINTABLE_MIN = 0x20;
  private static final char ESCAPE = '\\';
  // Per protocol specification, some extra ascii-printable characters must be unicode escaped.
  private static final Set<Character> EXTRA_ESCAPED_CHARS = new HashSet<>(
      Arrays.asList('"', '[', ']', '{', '}', '\\', '&'));
  private static final int NUM_UNICODE_ESCAPE_HEX_DIGITS = 4;
  private static final int RADIX_HEX = 16;

  /**
   * Decode an SDO-encoded string.
   */
  public static String decode(final CharBuffer s) {

    final StringBuilder builder = new StringBuilder();

    while (s.hasRemaining()) {
      char c = s.get();

      if (ESCAPE == c) { // start of a unicode escape
        c = s.get();

        if ('u' != c) { // \ must be followed by u
          throw new IllegalArgumentException("illegal escape");
        }
        final CharBuffer buf = CharBuffer.allocate(NUM_UNICODE_ESCAPE_HEX_DIGITS);

        try {
          if (NUM_UNICODE_ESCAPE_HEX_DIGITS != s.read(buf)) {
            throw new IllegalArgumentException("short escape");
          }
        } catch (IOException e) {
          throw new RuntimeException(e); // bug smell, shouldn't happen
        }
        buf.flip();

        try {
          c = (char) Integer.valueOf(buf.toString(), RADIX_HEX).intValue();

        } catch (final NumberFormatException e) {
          throw new IllegalArgumentException(e);
        }
        builder.append(c);

      } else {
        builder.append(c);
      }
    }

    return builder.toString();
  }

  /**
   * Encode a string according to the rules laid out in the SDO Protocol Specification.
   */
  public static String encode(final String s) {

    final StringBuilder builder = new StringBuilder();

    final StringCharacterIterator it = new StringCharacterIterator(s);

    for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {

      if (mustEscape(c)) {
        builder.append(
            String.format(ESCAPE + "u%0" + NUM_UNICODE_ESCAPE_HEX_DIGITS + "x", (int) c));

      } else {
        builder.append(c);
      }
    }

    return builder.toString();
  }

  private static boolean isAsciiPrintable(final int c) {
    return c >= ASCII_PRINTABLE_MIN && c <= ASCII_PRINTABLE_MAX;
  }

  private static boolean mustEscape(final char c) {
    return !isAsciiPrintable(c) || EXTRA_ESCAPED_CHARS.contains(c);
  }
}
